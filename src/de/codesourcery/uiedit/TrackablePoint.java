package de.codesourcery.uiedit;

import java.awt.Point;

public class TrackablePoint extends AbstractTrackable {

	public final Point point;

	public TrackablePoint(Point p) {
		this.point = p;
	}

	@Override
	public float minimumDistanceTo(Point point) {
		return (float) this.point.distance( point );
	}

	@Override
	public void translate(int dx, int dy) {
		this.point.translate(dx, dy);
		children.forEach( c -> c.translate(dx, dy ) );

		for ( final IObserver o : observer ) {
			o.moved( this, dx, dy );
		}
	}

	@Override
	public ITrackable getClosestTrackable(Point point,float maxRadius) {
		if ( this.point.distance( point ) <= maxRadius ) {
			return this;
		}
		return null;
	}

	@Override
	public boolean contains(Point point) {
		return this.point.x == point.x && this.point.y == point.y;
	}

	@Override
	public void translateNoNotify(int dx, int dy) {
		this.point.translate(dx, dy );
		children.forEach( c -> c.translateNoNotify(dx, dy ) );
	}
}