package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class LineNode extends AbstractGraphNode
{
	private static final float EPSILON = 0.0001f;

	private final List<IGraphNode> children = new ArrayList<>();

	public LineNode(Point p0,Point p1) {
		this( new PointNode(p0) ,new PointNode(p1) );
	}

	public LineNode(Point2D.Float p0,Point2D.Float p1) {
		this( new PointNode(p0) ,new PointNode(p1) );
	}

	@Override
	public Vector2 getCenterInViewCoordinates()
	{
		return new Vector2( p0() ).add( p1() ).scl(0.5f);
	}

	public LineNode(PointNode p0,PointNode p1) {
		addChildren( p0, p1 );
	}

	public Vector2 p0() {
		return ((PointNode) children.get(0)).getCenterInViewCoordinates();
	}

	public Vector2 p1() {
		return ((PointNode) children.get(1)).getCenterInViewCoordinates();
	}

	public float length() {
		return p0().dst(p1());
	}

	@Override
	public String toString() {
		switch ( getChildCount() ) {
			case 2:
				return "LineNode #"+nodeId+" "+p0()+" -> "+p1();
			case 1:
				return "LineNode #"+nodeId+" "+p0()+" -> <not set>";
			case 0:
				return "LineNode #"+nodeId+" <no points>";
			default:
				throw new IllegalStateException("Line with "+getChildCount()+" children?");
		}
	}

	public Vector2 getClosestPointOnLine(int x,int y)
	{
		final Vector2 v = p0();
		final Vector2 w = p1();
		final Vector2 p = new Vector2(x,y);

		final float l2 = v.dst2(w); // =length_squared(v, w);  // i.e. |w-v|^2 -  avoid a sqrt
		if (l2 == 0.0) {
			return v; // distance(p, v);   // v == w case
		}
		// Consider the line extending the segment, parameterized as v + t (w - v).
		// We find projection of point p onto the line.
		// It falls where t = [(p-v) . (w-v)] / |w-v|^2
		final float t = new Vector2(p).sub(v).dot( new Vector2(w).sub(v) ) / l2;
		if (t < 0.0) {
			return v; // distance(p, v);       // Beyond the 'v' end of the segment
		}
		else if (t > 1.0) {
			return w; // distance(p, w);  // Beyond the 'w' end of the segment
		}
		return v.add( w.sub(v).scl( t ) );  // Projection falls on the segment
	}

	public boolean split(int x,int y) {

		final Vector2 sp = getClosestPointOnLine(x,y);
		if ( sp.equals( p0() ) || sp.equals( p1() ) ) {
			return false;
		}

		final Vector2 s = viewToModel( sp );

		final PointNode splitPoint1 = new PointNode( s );
		final PointNode splitPoint2 = new PointNode( s );

		final PointNode end = (PointNode) child(1);
		setChild( 1 , splitPoint1 );

		final LineNode newLine = new LineNode( splitPoint2 , end  );

		newLine.copyMetaDataFrom( this );

		getParent().insertChild( getParent().indexOf( this )+1 , newLine );

		if ( getParent() instanceof SimplePolygon) {
			((SimplePolygon) getParent() ).assertValid();
		}
		return true;
	}

	public boolean isHorizontalLine() {
		return Math.abs( p0().y - p1().y ) <= EPSILON;
	}

	public boolean isVerticalLine() {
		return Math.abs( p0().x - p1().x ) <= EPSILON;
	}

	@Override
	public float distanceTo(float x, float y) {
		return Intersector.distanceSegmentPoint(p0(), p1(), new Vector2(x,y) );
	}


	@Override
	public ITranslationHandle getTranslationHandle(float viewX, float viewY,float pickRadius)
	{
		final float d1 = child(0).distanceTo(viewX, viewY);
		final float d2 = child(1).distanceTo(viewX, viewY);

		if ( d1 <= pickRadius && d2 <= pickRadius ) {
			if ( d1 < d2 ) {
				return child(0).getTranslationHandle(viewX, viewY, pickRadius);
			}
			return child(1).getTranslationHandle(viewX, viewY, pickRadius);
		}
		if ( d1 <= pickRadius )
		{
			return child(0).getTranslationHandle(viewX, viewY, pickRadius);
		}
		if ( d2 <= pickRadius ) {
			return child(1).getTranslationHandle(viewX, viewY, pickRadius);
		}

		if ( distanceTo(viewX, viewY) <= pickRadius ) {
			return new ITranslationHandle()
			{
				@Override
				public void translate(float dx, float dy) {
					LineNode.this.translate(dx,dy);
				}

				@Override
				public float distanceTo(float viewX, float viewY) {
					return LineNode.this.distanceTo( viewX,  viewY );
				}

				@Override
				public IGraphNode getNode() { return LineNode.this; }

			};
		}
		return null;
	}

	@Override
	public IRotationHandle getRotationHandle(float viewX, float viewY,float pickRadius)
	{
		return null;
	}

	@Override
	public Float getBounds() {
		final Vector2 p0 = p0();
		final Vector2 p1 = p1();

		final float xmin = Math.min(p0.x,p1.x);
		final float xmax = Math.max(p0.x,p1.x);
		final float ymin = Math.min(p0.y,p1.y);
		final float ymax = Math.max(p0.y,p1.y);
		return new Rectangle2D.Float(xmin,ymin,xmax-xmin,ymax-ymin);
	}

	@Override
	public boolean contains(float x, float y)
	{
		final Vector2 p0 = p0();
		final Vector2 p1 = p1();

		final float xmin = Math.min(p0.x,p1.x);
		final float xmax = Math.max(p0.x,p1.x);
		final float ymin = Math.min(p0.y,p1.y);
		final float ymax = Math.max(p0.y,p1.y);

		if ( x >= xmin && x <= xmax && y >= ymin && y <= ymax ) {
			return distanceTo(x,y) <= 1;
		}
		return false;
	}

	@Override
	public List<IGraphNode> getChildren() {
		return children;
	}

	@Override
	public void update(Matrix3 matrix) {
	}

	@Override
	public void update() {
	}

	@Override
	public Vector2 modelToView(Vector2 v) {
		return getParent().modelToView( v );
	}

	@Override
	public Vector2 viewToModel(Vector2 v) {
		return getParent().viewToModel( v );
	}

	@Override
	public Matrix3 getModelMatrix() {
		return getParent().getModelMatrix();
	}

	@Override
	public void updateCombinedMatrix(Matrix3 parent) {
	}

	@Override
	public Matrix3 getCombinedMatrix() {
		return getParent().getCombinedMatrix();
	}

	@Override
	public void translate(float dx, float dy)
	{
		getChildren().forEach( c -> c.translate(dx, dy ) );
	}

	@Override
	public void rotate(float angleInDeg) {
		// makes no sense to rotate
	}
}