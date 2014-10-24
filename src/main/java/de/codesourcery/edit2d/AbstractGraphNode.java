package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;

public abstract class AbstractGraphNode implements IGraphNode
{
	protected final List<INodeObserver> observers = new ArrayList<>();
	protected IGraphNode parent;

	protected final long nodeId = NODE_ID.incrementAndGet();

	@Override
	public final boolean isDirty() {
		return getMetaData().isDirty();
	}

	@Override
	public final void addObserver(INodeObserver o) {
		this.observers.add(o);
	}

	@Override
	public final void visitPreOrder(INodeVisitor v) {
		v.visit( this );
		getChildren().forEach( node -> node.visitPreOrder( v ) );
	}

	@Override
	public final void visitPostOrder(INodeVisitor v)
	{
		final List<IGraphNode> copy = new ArrayList<>( getChildren() ); // TODO: Why the copying ???
		copy.forEach( node -> node.visitPostOrder(v) );
		v.visit(this);
	}

	@Override
	public final IGraphNode getParent() {
		return parent;
	}

	@Override
	public final void setParent(IGraphNode parent) {
		this.parent = parent;
	}

	@Override
	public final IGraphNode getRoot() {
		return getParent() == null ? this : getParent().getRoot();
	}

	@Override
	public final List<INodeObserver> getObservers() {
		return observers;
	}

	@Override
	public final void removeObserver(INodeObserver o) {
		final int len = observers.size();
		for ( int i = 0 ; i < len ; i++ ) {
			if ( observers.get(i) == o )
			{
				observers.remove( i );
				i--;
			}
		}
	}

	@Override
	public final IGraphNode child(int index) {
		return getChildren().get(index);
	}

	@Override
	public final void insertChild(int index,IGraphNode n)
	{
		getChildren().add( index ,  n );
		n.setParent( this );
	}

	@Override
	public final void setChild(int index,IGraphNode n)
	{
		getChildren().set( index ,  n );
		n.setParent( this );
	}

	@Override
	public final void addChildren(IGraphNode n1,IGraphNode... additional)
	{
		n1.setParent( this );
		getChildren().add( n1 );

		if ( additional != null ) {
			for ( final IGraphNode g : additional )
			{
				g.setParent(this);
				getChildren().add(g);
			}
		}
	}

	@Override
	public final void remove() {
		getParent().removeChild( this );
	}

	@Override
	public final boolean hasChildren() {
		return getChildCount() > 0;
	}

	@Override
	public final boolean hasNoChildren() {
		return getChildCount() == 0 ;
	}

	@Override
	public final int getChildCount() {
		return getChildren().size();
	}

	@Override
	public final void removeChild(IGraphNode child) {
		getChildren().remove( child );
		child.setParent(null);
	}

	@Override
	public final int indexOf(IGraphNode child) {
		return getChildren().indexOf(child);
	}

	@Override
	public void update()
	{
		if ( isDirty() )
		{
			update( getParent().getMetaData().getCombinedMatrix() );
		} else {
			for ( final IGraphNode child : getChildren() )
			{
				child.update();
			}
		}
	}

	@Override
	public void translate(EventType eventType, float dx, float dy)
	{
		if ( ((RootNode) getRoot()).queueTranslate( eventType , this , dx , dy ) ) {
			getMetaData().translate( dx ,  dy );
		}
	}

	@Override
	public void rotate(EventType eventType,float angleInDeg) {
		if ( ((RootNode) getRoot()).queueRotate( eventType , this , angleInDeg ) ) {
			getMetaData().rotate( angleInDeg );
		}
	}

	@Override
	public void set(float x, float y,boolean notifyObservers) {
		throw new RuntimeException("Unsupported operation set(int,int,boolean)");
	}

	@Override
	public void update(Matrix3 matrix)
	{
		System.out.println("update(): Called on "+this+" with "+NodeUtils.matrixToString(matrix));
		getMetaData().updateCombinedMatrix( matrix );
		for ( final IGraphNode child : getChildren() )
		{
			child.update( getMetaData().getCombinedMatrix() );
		}
	}
}