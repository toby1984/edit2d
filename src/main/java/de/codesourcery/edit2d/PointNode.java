package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class PointNode extends AbstractGraphNode
{
	public final Vector2 p = new Vector2();
	private final LightweightNodeData metaData = new LightweightNodeData();

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
		return "Point #"+nodeId+" "+p+" ( "+getMetaData()+")";
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
	public void update() {
	}

	@Override
	public List<IGraphNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public void translate(EventType eventType, int dx, int dy)
	{
		final IGraphNode root = getRoot();
		if ( ((RootNode) root).queueUpdate( eventType , this , dx , dy ) )
		{
			this.p.x += dx;
			this.p.y += dy;
		}
	}

	@Override
	public void set(int x, int y,boolean notifyObservers)
	{
		final Vector2 pv = getPointInViewCoordinates();

		final int dx = (int) (x - pv.x);
		final int dy = (int) (y - pv.y);

		if ( notifyObservers ) {
			this.translate(EventType.TRANSLATED,dx,dy);
		} else {
			this.p.x += dx;
			this.p.y += dy;
		}
	}

	@Override
	public INodeData getMetaData() {
		return metaData;
	}

	@Override
	public void update(Matrix3 matrix) {
	}

	@Override
	public Rectangle2D.Float getBounds() {
		final Vector2 p = getPointInViewCoordinates();
		return new Rectangle2D.Float( p.x , p.y , 1 , 1 );
	}
}