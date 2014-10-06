package de.codesourcery.uiedit;

import java.awt.Point;
import java.awt.Rectangle;

public class Port extends TrackableRectangle
{
	public static enum PortType {
		IN,OUT;
	}

	public static enum PortLocation {
		N,S,E,W;
	}

	private final PortType type;

	public Port(TrackablePoint point0, TrackablePoint point1, TrackablePoint point2, TrackablePoint point3,PortType type)
	{
		super(point0, point1, point2, point3);
		this.type = type;
	}

	public static Port addPort(TrackableRectangle r,PortLocation loc,PortType type) {

		final Rectangle rect = r.getBounds();

		final int cx = rect.x + rect.width/2;
		final int cy = rect.y + rect.height/2;

		final float widthPerc = 0.1f;
		final float heightPerc = 0.1f;

		final int portWidth = (int) (rect.width*widthPerc);
		final int halfWidth = portWidth/2;

		final int portHeight = (int) (rect.height*heightPerc);
		final int halfHeight = portHeight/2;

		Point p0,p1,p2,p3;
		switch( loc )
		{
			case N:
				p0 = new Point( cx - halfWidth , rect.y );
				p1 = new Point( cx + halfWidth , rect.y );
				p2 = new Point( cx + halfWidth , rect.y + portHeight );
				p3 = new Point( cx - halfWidth , rect.y + portHeight );
				break;
			case E:
				p0 = new Point( rect.x + rect.width - portWidth , cy - halfHeight );
				p1 = new Point( rect.x + rect.width             , cy - halfHeight );
				p2 = new Point( rect.x + rect.width             , cy + halfHeight );
				p3 = new Point( rect.x + rect.width - portWidth , cy + halfHeight );
				break;
			case S:
				p0 = new Point( cx - halfWidth , rect.y + rect.height - portHeight );
				p1 = new Point( cx + halfWidth , rect.y + rect.height - portHeight );
				p2 = new Point( cx + halfWidth , rect.y + rect.height );
				p3 = new Point( cx - halfWidth , rect.y + rect.height );
			case W:
				p0 = new Point( rect.x , cy - halfHeight );
				p1 = new Point( rect.x + portWidth , cy - halfHeight );
				p2 = new Point( rect.x + portWidth             , cy + halfHeight );
				p3 = new Point( rect.x , cy + halfHeight );
			default:
				throw new RuntimeException("Unhandled location: "+loc);
		}
		final Port result = new Port(new TrackablePoint(p0),
				new TrackablePoint(p1),
				new TrackablePoint(p2),
				new TrackablePoint(p3),
				type);

		switch( loc )
		{
			case N:
				r.l0.addChild( result );
				break;
			case E:
				r.l1.addChild( result );
				break;
			case S:
				r.l2.addChild( result );
				break;
			case W:
				r.l3.addChild( result );
				break;
			default:
				throw new RuntimeException("Unhandled location: "+loc);

		}
		return result;
	}

	@Override
	public boolean isEditable() {
		return false;
	}
}