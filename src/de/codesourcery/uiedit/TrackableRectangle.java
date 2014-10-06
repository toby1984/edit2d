package de.codesourcery.uiedit;

import java.awt.Point;
import java.awt.Rectangle;

public class TrackableRectangle extends AbstractTrackable  {

	private final TrackablePoint point0;
	private final TrackablePoint point1;
	private final TrackablePoint point2;
	private final TrackablePoint point3;

	public final TrackableLine l0;
	public final TrackableLine l1;
	public final TrackableLine l2;
	public final TrackableLine l3;

	public TrackableRectangle(TrackablePoint point0,TrackablePoint point1,TrackablePoint point2,TrackablePoint point3)
	{
		final Point p0 = point0.point;
		final Point p1 = point1.point;
		final Point p2 = point2.point;
		final Point p3 = point3.point;

		this.point0 = point0;
		this.point1 = point1;
		this.point2 = point2;
		this.point3 = point3;

		l0 = new TrackableLine( point0 , point1 );
		l1 = new TrackableLine( point1 , point2 );
		l2 = new TrackableLine( point2 , point3 );
		l3 = new TrackableLine( point0 , point3 );

		point0.addObserver( (o,dx,dy) -> p3.x = p0.x );
		point0.addObserver( (o,dx,dy) -> p1.y = p0.y );

		point1.addObserver( (o,dx,dy) -> p0.y = p1.y );
		point1.addObserver( (o,dx,dy) -> p2.x = p1.x );

		point2.addObserver( (o,dx,dy) -> p1.x = p2.x );
		point2.addObserver( (o,dx,dy) -> p3.y = p2.y );

		point3.addObserver( (o,dx,dy) -> p0.x = p3.x );
		point3.addObserver( (o,dx,dy) -> p2.y = p3.y );

		l0.addObserver( (o,dx,dy) -> l2.translateNoNotify( dx , 0 ) );
		l1.addObserver( (o,dx,dy) -> l3.translateNoNotify( 0 , dy ) );
		l2.addObserver( (o,dx,dy) -> l0.translateNoNotify( dx , 0 ) );
		l3.addObserver( (o,dx,dy) -> l1.translateNoNotify( 0 , dy ) );
	}

	@Override
	public float minimumDistanceTo(Point point)
	{
		return Math.min( Math.min( Math.min( l0.minimumDistanceTo( point ) , l1.minimumDistanceTo( point ) ) , l2.minimumDistanceTo( point ) ) , l3.minimumDistanceTo( point ) );
	}

	@Override
	public void translate(int dx, int dy)
	{
		l0.translateNoNotify( dx , dy);
		l1.translateNoNotify( dx , dy);
		l2.translateNoNotify( dx , dy);
		l3.translateNoNotify( dx , dy);

		children.forEach( c -> c.translate( dx , dy) );

		observer.forEach( o -> o.moved(this, dx, dy) );
	}

	@Override
	public void translateNoNotify(int dx, int dy) {
		l0.translateNoNotify( dx , dy);
		l1.translateNoNotify( dx , dy);
		l2.translateNoNotify( dx , dy);
		l3.translateNoNotify( dx , dy);

		children.forEach( c -> c.translateNoNotify(dx, dy) );
	}

	@Override
	public ITrackable getClosestTrackable(Point point, float maxRadius)
	{
		ITrackable result = l0.getClosestTrackable(point, maxRadius);
		if ( result != null ) {
			return result;
		}

		result = l1.getClosestTrackable(point, maxRadius);
		if ( result != null ) {
			return result;
		}

		result = l2.getClosestTrackable(point, maxRadius);
		if ( result != null ) {
			return result;
		}

		result = l3.getClosestTrackable(point, maxRadius);
		if ( result != null ) {
			return result;
		}

		if ( contains( point ) ) {
			return this;
		}
		return null;
	}

	public int xmin() {
		return Math.min( Math.min( Math.min( point0.point.x , point1.point.x ) , point2.point.x ) , point3.point.x );
	}

	public int ymin() {
		return Math.min( Math.min( Math.min( point0.point.y , point1.point.y ) , point2.point.y ) , point3.point.y );
	}

	public int xmax() {
		return Math.max( Math.max( Math.max( point0.point.x , point1.point.x ) , point2.point.x ) , point3.point.x );
	}

	public int ymax() {
		return Math.max( Math.max( Math.max( point0.point.y , point1.point.y ) , point2.point.y ) , point3.point.y );
	}

	public Rectangle getBounds() {
		return new Rectangle( xmin() , ymin() , xmax()-xmin() , ymax() - ymin() );
	}

	@Override
	public boolean contains(Point p)
	{
		return p.x >= xmin() && p.x <= xmax() && p.y >= ymin() && p.y <= ymax();
	}
}