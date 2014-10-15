package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;

public abstract class AbstractGraphNode implements IGraphNode {

	protected final List<IGraphNode> children = new ArrayList<>();
	protected final List<INodeObserver> observers = new ArrayList<>();

	protected final NodeData metaData = new NodeData();

	private IGraphNode parent;

	@Override
	public final void visitPreOrder(INodeVisitor v) {
		v.visit( this );
		children.forEach( node -> node.visitPreOrder( v ) );
	}

	@Override
	public final void removeObserver(INodeObserver o)
	{
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
	public final void remove() {
		getParent().removeChild( this );
	}

	@Override
	public final void removeChild(IGraphNode child) {
		children.remove( child );
		child.setParent(null);
	}

	@Override
	public final List<INodeObserver> getObservers() {
		return observers;
	}

	@Override
	public final IGraphNode getRoot()
	{
		return getParent() == null ? this : getParent().getRoot();
	}

	@Override
	public final int indexOf(IGraphNode child) {
		return children.indexOf(child);
	}

	@Override
	public final void addObserver(INodeObserver o) {
		this.observers.add(o);
	}

	@Override
	public final void update()
	{
		if ( isDirty() )
		{
			update( getParent().getMetaData().getCombinedMatrix() );
		} else {
			for ( final IGraphNode child : children )
			{
				child.update();
			}
		}
	}

	@Override
	public final void visitPostOrder(INodeVisitor v)
	{
		final List<IGraphNode> copy = new ArrayList<>(this.children);
		copy.forEach( node -> node.visitPostOrder(v) );
		v.visit(this);
	}

	@Override
	public final IGraphNode getParent() {
		return parent;
	}

	@Override
	public final void setParent(IGraphNode parent) {
		if ( this.parent != null && parent != null && this.parent != parent ) {
			throw new IllegalStateException("Parent already set on "+this);
		}
		this.parent = parent;
	}

	@Override
	public final void translate(EventType eventType, int dx, int dy)
	{
		if ( ((RootNode) getRoot()).queueUpdate( eventType , this , dx , dy ) ) {
			metaData.translate( dx ,  dy );
		}
	}

	@Override
	public void set(int x, int y) {
	}

	@Override
	public final boolean isDirty() {
		return metaData.isDirty();
	}

	@Override
	public final IGraphNode child(int index) {
		return children.get(index);
	}

	@Override
	public final void insertChild(int index,IGraphNode n)
	{
		children.add( index ,  n );
		n.setParent( this );
	}

	@Override
	public final void setChild(int index,IGraphNode n)
	{
		children.set( index ,  n );
		n.setParent( this );
	}

	@Override
	public final void addChildren(IGraphNode n1,IGraphNode... additional)
	{
		n1.setParent( this );
		children.add( n1 );

		if ( additional != null ) {
			for ( final IGraphNode g : additional )
			{
				g.setParent(this);
				children.add(g);
			}
		}
	}

	@Override
	public final void update(Matrix3 matrix)
	{
		metaData.updateCombinedMatrix( matrix );
		for ( final IGraphNode child : children )
		{
			child.update( metaData.combinedMatrix );
		}
	}

	@Override
	public final NodeData getMetaData() {
		return metaData;
	}

	@Override
	public final List<IGraphNode> getChildren() {
		return children;
	}

	@Override
	public final int getChildCount() {
		return children.size();
	}

	@Override
	public final boolean hasChildren() {
		return ! children.isEmpty();
	}

	@Override
	public final boolean hasNoChildren() {
		return children.isEmpty();
	}
}