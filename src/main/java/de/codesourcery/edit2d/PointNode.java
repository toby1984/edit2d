package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class PointNode extends AbstractGraphNode
{
	public final Vector2 p = new Vector2();
	private final LightweightNodeData metaData = new LightweightNodeData() {

		@Override
		public Vector2 viewToModel(Vector2 v) {
			return getParent().getMetaData().viewToModel( v );
		}

		@Override
		public Vector2 modelToView(Vector2 v) {
			return getParent().getMetaData().modelToView( v );
		}
	};

	public PointNode(Point p) {
		this(p.x,p.y);
	}

	public PointNode(Point2D.Float p) {
		this(p.x,p.y);
	}

	public PointNode(float x,float y) {
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
	public float distanceTo(float x, float y) {
		return getCenterInViewCoordinates().dst( x , y);
	}

	@Override
	public Vector2 getCenterInViewCoordinates() {
		return new Vector2(p).mul( getParent().getMetaData().getCombinedMatrix() );
	}

	@Override
	public boolean contains(float  x, float y) {
		final Vector2 view = getCenterInViewCoordinates();
		return Math.floor( view.dst( x ,  y ) ) <= 1f;
	}

	@Override
	public void update() {
	}

	@Override
	public List<IGraphNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public void translate(EventType eventType, float dx, float dy)
	{
		final IGraphNode root = getRoot();
		if ( ((RootNode) root).queueTranslate( eventType , this , dx , dy ) )
		{
			System.out.println("*** Translating point by "+dx+","+dy);
			this.p.x += dx;
			this.p.y += dy;
		} else {
			System.err.println("--- Not translating point,already moved");
		}
	}

	@Override
	public void set(float x, float y,boolean notifyObservers)
	{
		final Vector2 pv = getCenterInViewCoordinates();

		final float dx = (x - pv.x);
		final float dy = (y - pv.y);

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
		final Vector2 p = getCenterInViewCoordinates();
		return new Rectangle2D.Float( p.x , p.y , 1 , 1 );
	}
}