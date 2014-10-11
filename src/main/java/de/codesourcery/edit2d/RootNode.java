package de.codesourcery.edit2d;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class RootNode extends AbstractGraphNode
{
	protected final Map<IGraphNode,NodeUpdate> nodeUpdates = new IdentityHashMap<>();

	protected static final class NodeUpdate
	{
		public final EventType type;
		public int dx;
		public int dy;

		public NodeUpdate(EventType type,int dx, int dy) {
			this.type = type;
			this.dx = dx;
			this.dy = dy;
		}

		public void merge(EventType type,int dx, int dy) {
			this.dx += dx;
			this.dy += dy;
		}
	}

	public boolean queueUpdate(EventType type,IGraphNode n,int dx,int dy)
	{
		final boolean queued = internalUpdate( type , n , dx , dy );
		if ( queued )
		{
			for ( final IGraphNode child : n.getChildren() )
			{
				queueUpdate( EventType.PARENT_MOVED , child , dx , dy );
			}
		}
		return queued;
	}

	private boolean internalUpdate(EventType type,IGraphNode n,int dx,int dy)
	{
		final NodeUpdate existing = nodeUpdates.get(n);
		if ( existing == null ) {
			nodeUpdates.put( n , new NodeUpdate(type,dx,dy));
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

			final IGraphNode finalKey = key;
			visited.add( key );
			final NodeUpdate value = nodeUpdates.get(key);
			key.getObservers().stream().filter( ob -> ob.invokeFor( value.type ) ).forEach( observer -> observer.nodeTranslated( value.type , finalKey ,  value.dx , value.dy ) );
		} while ( true );
		nodeUpdates.clear();
	}

	@Override
	public float distanceTo(int x, int y) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public boolean contains(int x, int y) {
		return false;
	}
}