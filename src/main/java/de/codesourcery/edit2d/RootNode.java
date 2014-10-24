package de.codesourcery.edit2d;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.badlogic.gdx.math.Vector2;

public class RootNode extends RegularGraphNode
{
	protected final Map<IGraphNode,NodeUpdate> nodeUpdates = new IdentityHashMap<>();

	protected static final class NodeUpdate
	{
		public final EventType type;
		public Consumer<INodeObserver> task;

		public NodeUpdate(EventType type,Consumer<INodeObserver> task) {
			this.type = type;
			this.task = task;
		}

		public void apply(INodeObserver observer) {
			task.accept( observer );
		}
	}

	@Override
	public Vector2 getCenterInViewCoordinates() {

		if ( hasNoChildren() ) {
			return new Vector2(0,0);
		}
		int count = 0;
		final Vector2 result = child(0).getCenterInViewCoordinates();
		for ( int i = 1 ; i < getChildCount() ; i++ ) {
			count++;
			result.add( child(i).getCenterInViewCoordinates() );
		}
		result.scl( 1f / count );
		return result;
	}

	public boolean queueTranslate(EventType type,IGraphNode n,float dx,float dy)
	{
		final Consumer<INodeObserver> task = observer -> observer.nodeTranslated( type ,n , dx , dy );
		final boolean queued = queueUpdate( type , n , task );
		if ( queued )
		{
			System.out.println("QUEUED: "+type+" ("+dx+","+dy+") @ "+n);
			for ( final IGraphNode child : n.getChildren() )
			{
				queueTranslate( EventType.PARENT_MOVED , child , dx , dy );
			}
		} else {
			System.out.println("NOT QUEUED: "+type+" ("+dx+","+dy+") @ "+n);
		}
		return queued;
	}

	public boolean queueRotate(EventType type,IGraphNode n,float angleInDeg)
	{
		final Consumer<INodeObserver> task = observer -> observer.nodeRotated( type ,n , angleInDeg);
		final boolean queued = queueUpdate( type , n , task );
		if ( queued )
		{
			System.out.println("QUEUED: "+type+" "+angleInDeg+" degrees @ "+n);
//			for ( final IGraphNode child : n.getChildren() )
//			{
//				queueRotate( EventType.PARENT_MOVED , child , angleInDeg );
//			}
		} else {
			System.out.println("NOT QUEUED: "+type+" "+angleInDeg+" degrees @ "+n);
		}
		return queued;
	}

	private boolean queueUpdate(EventType type,IGraphNode n,Consumer<INodeObserver> task)
	{
		final NodeUpdate existing = nodeUpdates.get(n);
		if ( existing == null ) {
			nodeUpdates.put( n , new NodeUpdate(type,task));
			return true;
		}
		return false;
	}

	public void processNodeUpdates()
	{
		final Set<IGraphNode> visited = new HashSet<>();

		do {
			IGraphNode key = null;
			for (  final Iterator<IGraphNode> it = nodeUpdates.keySet().iterator() ; it.hasNext() ; ) {
				final IGraphNode current = it.next();
				if ( ! visited.contains( current ) ) {
					key = current;
					break;
				}
			}
			if ( key == null ) {
				break;
			}
			System.out.println("Processing node updates on: "+key);

			visited.add( key );
			final NodeUpdate value = nodeUpdates.get(key);
			key.getObservers().stream().filter( ob -> ob.invokeFor( value.type ) ).forEach( value::apply );
		} while ( true );
		nodeUpdates.clear();
	}

	@Override
	public String toString() {
		return "RootNode #"+nodeId;
	}

	@Override
	public float distanceTo(float x, float y) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public boolean contains(float x, float y) {
		return false;
	}

	@Override
	public java.awt.geom.Rectangle2D.Float getBounds()
	{
		Rectangle2D.Float result = null;
		for ( final IGraphNode child : getChildren() ) {
			if ( result == null ) {
				result = child.getBounds();
			} else {
				result.add( child.getBounds() );
			}
		}
		return result == null ? new Rectangle2D.Float() : result ;
	}
}