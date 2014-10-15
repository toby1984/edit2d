package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class PointNode implements IGraphNode
{
	public final Vector2 p = new Vector2();

	private final List<INodeObserver> observers = new ArrayList<>();
	private final LightweightNodeData metaData = new LightweightNodeData();

	private IGraphNode parent;

	public PointNode(Point p) {
		this(p.x,p.y);
	}

	public PointNode(int x,int y) {
		this.p.set(x,y);
	}

	public PointNode(Vector2 p) {
		this.p.set(p);
	}

	@Override
	public String toString() {
		return "Point "+p+" ( "+getMetaData()+")";
	}

	@Override
	public float distanceTo(int x, int y) {
		return getPointInViewCoordinates().dst( x , y);
	}

	public Vector2 getPointInViewCoordinates()
	{
		return new Vector2(p).mul( getParent().getMetaData().getCombinedMatrix() );
	}

	@Override
	public boolean contains(int x, int y) {
		final Vector2 view = getPointInViewCoordinates();
		return x == (int) view.x && y == (int) view.y;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void addObserver(INodeObserver o) {
		this.observers.add(o);
	}

	@Override
	public void update() {
	}

	@Override
	public List<IGraphNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public IGraphNode getParent() {
		return parent;
	}

	@Override
	public void setParent(IGraphNode parent)
	{
		this.parent = parent;
	}

	@Override
	public IGraphNode child(int index) {
		throw new NoSuchElementException("Points have no children");
	}

	@Override
	public IGraphNode getRoot() {
		return getParent() == null ? this : getParent().getRoot();
	}

	@Override
	public List<INodeObserver> getObservers() {
		return observers;
	}

	@Override
	public void addChildren(IGraphNode n1, IGraphNode... additional) {
		throw new UnsupportedOperationException("Points cannot have children");
	}

	@Override
	public void visitPreOrder(INodeVisitor v) {
		v.visit( this );
	}

	@Override
	public void visitPostOrder(INodeVisitor v) {
		v.visit( this );
	}

	@Override
	public void translate(EventType eventType, int dx, int dy)
	{
		if ( ((RootNode) getRoot()).queueUpdate( eventType , this , dx , dy ) )
		{
			this.p.x += dx;
			this.p.y += dy;
		}
	}


	@Override
	public void set(int x, int y)
	{
		final int dx = (int) (x - this.p.x);
		final int dy = (int) (y - this.p.y);
		this.translate(EventType.TRANSLATED,dx,dy);
	}

	@Override
	public INodeData getMetaData() {
		return metaData;
	}

	@Override
	public void update(Matrix3 matrix) {
	}

	@Override
	public void insertChild(int index, IGraphNode n) {
		throw new UnsupportedOperationException("points cannot have children");
	}

	@Override
	public void setChild(int index, IGraphNode n) {
		throw new UnsupportedOperationException("points cannot have children");
	}

	@Override
	public int indexOf(IGraphNode child) {
		return -1;
	}

	@Override
	public void removeObserver(INodeObserver o) {
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
	public Rectangle2D.Float getBounds() {
		final Vector2 p = getPointInViewCoordinates();
		return new Rectangle2D.Float( p.x , p.y , 1 , 1 );
	}

	@Override
	public void removeChild(IGraphNode child) {
		throw new UnsupportedOperationException("removeChild() not supported by points");
	}

	@Override
	public void remove() {
		getParent().removeChild( this );
	}

	@Override
	public int getChildCount() {
		return 0;
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public boolean hasNoChildren() {
		return true;
	}
}