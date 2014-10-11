package de.codesourcery.edit2d;

import java.awt.Point;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class LineNode extends AbstractGraphNode
{
	private static final float EPSILON = 0.0001f;

	public LineNode(Point p0,Point p1) {
		this( new PointNode(p0) ,new PointNode(p1) );
	}

	public LineNode(PointNode p0,PointNode p1) {
		addChildren( p0, p1 );
	}

	public Vector2 p0() {
		return ((PointNode) children.get(0)).getPointInViewCoordinates();
	}

	public Vector2 p1() {
		return ((PointNode) children.get(1)).getPointInViewCoordinates();
	}

	@Override
	public String toString() {
		return "LineNode "+p0()+" -> "+p1();
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

		final Vector2 splitPoint = getClosestPointOnLine(x,y);
		if ( splitPoint.equals( p0() ) || splitPoint.equals( p1() ) ) {
			return false;
		}

		final Vector2 s = getMetaData().viewToModel( splitPoint );
		final Vector2 p1 = ((PointNode) child(1)).p;

		final PointNode newPoint1 = new PointNode( s );
		final PointNode newPoint2 = new PointNode( s );
		setChild( 1 , newPoint1 );

		final LineNode newLine = new LineNode( newPoint2 , new PointNode(p1) );
		newLine.getMetaData().setModelMatrix( getMetaData().getModelMatrix() );
		newLine.update( getParent().getMetaData().getCombinedMatrix() );

		Observer.link( EventType.TRANSLATE_POINT ,EventType.PARENT_MOVED, newPoint1 , newPoint2 );

		getParent().insertChild( getParent().indexOf( this )+1 , newLine );
		return true;
	}

	public boolean isHorizontalLine() {
		return Math.abs( p0().y - p1().y ) <= EPSILON;
	}

	public boolean isVerticalLine() {
		return Math.abs( p0().x - p1().x ) <= EPSILON;
	}

	@Override
	public float distanceTo(int x, int y) {
		return Intersector.distanceSegmentPoint(p0(), p1(), new Vector2(x,y) );
	}

	@Override
	public boolean contains(int x, int y)
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
}