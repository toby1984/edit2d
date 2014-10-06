package de.codesourcery.uiedit;

import java.awt.Point;
import java.awt.geom.Point2D;

public class TrackableLine extends AbstractTrackable
{
	public final Point p0;
	public final Point p1;

	public final TrackablePoint point0;
	public final TrackablePoint point1;

	public TrackableLine(TrackablePoint p0,TrackablePoint p1)
	{
		this.p0 = p0.point;
		this.p1 = p1.point;
		this.point0 = p0;
		this.point1 = p1;
	}

	@Override
	public float minimumDistanceTo(Point point)
	{
		return minimumDistance(p0,p1,point);
	}

	private float minimumDistance(Point v, Point w, Point p)
	{
		// taken from http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment

		// Return minimum distance between line segment vw and point p
		final float l2 = length_squared(v, w);  // i.e. |w-v|^2 -  avoid a sqrt
		if (l2 == 0.0) {
			return (float) p.distance( v );   // v == w case
		}
		// Consider the line extending the segment, parameterized as v + t (w - v).
		// We find projection of point p onto the line.
		// It falls where t = [(p-v) . (w-v)] / |w-v|^2
		final float t = dot( sub(p , v ) , sub( w , v ) ) / l2;
		if (t < 0.0) {
			return (float) p.distance(v);       // Beyond the 'v' end of the segment
		}
		if (t > 1.0) {
			return (float) p.distance(w);  // Beyond the 'w' end of the segment
		}
		final Point2D.Float projection = add( v , scale(t , sub(w , v) ) );  // Projection falls on the segment
		return (float) p.distance(projection);
	}

	private float length_squared(Point a,Point b) {
		final float dx = a.x - b.x;
		final float dy = a.y - b.y;
		return dx*dx+dy*dy;
	}

	private Point2D.Float scale(float f, Point a)
	{
		return new Point2D.Float( f*a.x, f*a.y);
	}
	private Point sub(Point a,Point b) {
		return new Point(a.x-b.x , a.y - b.y );
	}

	private Point2D.Float add(Point a,Point2D.Float b) {
		return new Point2D.Float(a.x+b.x , a.y + b.y );
	}

	private float dot(Point a,Point b)
	{
		return a.x*b.x+a.y*b.y;
	}

	public boolean isHorizontal() {
		return p0.y == p1.y;
	}

	public boolean isVertical() {
		return p0.x == p1.x;
	}

	@Override
	public void translate(int dx, int dy)
	{
		p0.translate( dx,  dy );
		p1.translate( dx,  dy );

		children.forEach( c -> c.translate(dx, dy) );

		for ( final IObserver o : observer ) {
			o.moved( this, dx, dy );
		}
	}

	@Override
	public void translateNoNotify(int dx, int dy)
	{
		point0.translateNoNotify(dx, dy);
		point1.translateNoNotify(dx, dy);
		children.forEach( c -> c.translateNoNotify(dx, dy) );
	}

	@Override
	public ITrackable getClosestTrackable(Point point, float maxRadius)
	{
		if ( p0.distance( point ) <= maxRadius ) {
			return point0;
		}
		if ( p1.distance( point ) <= maxRadius ) {
			return point1;
		}
		if ( this.minimumDistanceTo( point ) <= maxRadius ) {
			return this;
		}
		return null;
	}

	public int xmin() {
		return Math.min(p0.x,p1.x);
	}

	public int ymin() {
		return Math.min(p0.y,p1.y);
	}

	public int xmax() {
		return Math.max(p0.x,p1.x);
	}

	public int ymax() {
		return Math.max(p0.y,p1.y);
	}

	@Override
	public boolean contains(Point point)
	{
		if ( isHorizontal() )
		{
			return point.x >= xmin() && point.x <= xmax() && point.y == this.p0.y;
		}
		if ( isVertical() ) {
			return point.x == this.p0.x && point.y >= ymin() && point.y <= ymax();
		}
		return minimumDistanceTo( point ) <= 1;
	}
}