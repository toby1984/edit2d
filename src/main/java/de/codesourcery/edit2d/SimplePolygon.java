package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class SimplePolygon extends AbstractGraphNode
{
	public SimplePolygon(int x,int y,int width,int height)
	{
		final Point p0 = new Point(x,y);
		final Point p1 = new Point(x+width,y);
		final Point p2 = new Point(x+width,y+height);
		final Point p3 = new Point(x,y+height);

		final LineNode l0 = new LineNode(  p0 ,p1 );
		final LineNode l1 = new LineNode(  p1 ,p2 );
		final LineNode l2 = new LineNode(  p2 ,p3 );
		final LineNode l3 = new LineNode(  p3 ,p0 );

		addChildren( l0,l1,l2, l3 );

		// link start/end points of consecutive lines
		Observers.link( asSet(EventType.TRANSLATED , EventType.PARENT_MOVED ) , l0.child( 1 ) , l1.child( 0 ) );
		Observers.link( asSet(EventType.TRANSLATED , EventType.PARENT_MOVED ) , l1.child( 1 ) , l2.child( 0 ) );
		Observers.link( asSet(EventType.TRANSLATED , EventType.PARENT_MOVED ) , l2.child( 1 ) , l3.child( 0 ) );
		Observers.link( asSet(EventType.TRANSLATED , EventType.PARENT_MOVED ) , l3.child( 1 ) , l0.child( 0 ) );
	}

	public SimplePolygon() {
	}

	private static Set<EventType> asSet(EventType t1,EventType...eventTypes)
	{
		final Set<EventType> result = new HashSet<>();
		result.add(t1);
		if ( eventTypes != null ) {
			result.addAll(Arrays.asList( eventTypes ) );
		}
		return result;
	}

	@Override
	public Rectangle2D.Float getBounds() {

		float xmin =  10000000;
		float xmax = -10000000;
		float ymin =  10000000;
		float ymax = -10000000;
		for ( final IGraphNode node : getChildren() )
		{
			final LineNode l = (LineNode) node;
			final float minX = Math.min( l.p0().x , l.p1().x );
			final float minY = Math.min( l.p0().y , l.p1().y );
			final float maxX = Math.max( l.p0().x , l.p1().x );
			final float maxY = Math.max( l.p0().y , l.p1().y );
			xmin = Math.min( xmin ,  minX );
			ymin = Math.min( ymin ,  minY );
			xmax = Math.max( xmax ,  maxX );
			ymax = Math.max( ymax ,  maxY );
		}
		return new Rectangle2D.Float(xmin,ymin,xmax-xmin,ymax-ymin);
	}

	protected Vector2 center() {
		final Rectangle2D.Float b = getBounds();
		return new Vector2( b.x + b.width/2 , b.y + b.height/2 );
	}

	@Override
	public float distanceTo(int x, int y)
	{
		return center().dst(x,y);
	}

	@Override
	public boolean contains(int x, int y)
	{
		final Rectangle2D.Float rect = getBounds();
		if ( rect.contains( x, y ) )
		{
			// cast ray along positive X axis
			final Vector2 p0 = new Vector2(x,y);
			final Vector2 p1 = new Vector2( rect.x + rect.width , y );
			int count = getIntersectionCount( p0 , p1 );
			if ( count == 0 )
			{
				// cast ray along negative X axis
				p1.set( rect.x , y );
				count = getIntersectionCount( p1 , p0 );
				if ( count == 0 )
				{
					// cast ray along negative Y axis
					p1.set( x , rect.y );
					count = getIntersectionCount( p0 , p1 );
					if ( count == 0 )
					{
						// cast ray along positive Y axis
						p1.set( x , rect.y + rect.height );
						count = getIntersectionCount( p0 , p1 );
						if ( count != 0 ) {
							return isOdd(count);
						}
					} else {
						return isOdd(count);
					}
				} else {
					return isOdd(count);
				}
			} else {
				return isOdd(count);
			}
		}
		return false;
	}

	private static boolean isOdd(int x) {
		return (x%2) != 0;
	}

	private int getIntersectionCount(Vector2 p0,Vector2 p1)
	{
		int intersectionCount = 0;
		for ( final IGraphNode child : getChildren() ) {
			final LineNode l = (LineNode) child;
			if ( intersects( l , p0 ,p1 ) ) {
				intersectionCount++;
			}
		}
		return intersectionCount;
	}

	private boolean intersects(LineNode line , Vector2 p0, Vector2 p1)
	{
		final Vector2 result = new Vector2();
		return Intersector.intersectSegments( line.p0() , line.p1() , p0 , p1 , result );
	}

	public void removePoint(PointNode pointToRemove)
	{
		// find line
		LineNode affectedLine = null;
		for ( final IGraphNode child : children )
		{
			if ( child.indexOf( pointToRemove ) != -1 ) {
				affectedLine = (LineNode) child;
				break;
			}
		}
		if ( affectedLine == null ) {
			throw new NoSuchElementException("No line of this polygon contains "+pointToRemove);
		}

		final int pointIdx = affectedLine.indexOf( pointToRemove );
		if ( pointIdx < 0 || pointIdx > 1) {
			throw new IllegalStateException("Point has invalid point index "+pointIdx);
		}

		final int remainingIdx = 1 - pointIdx;
		final PointNode remainingPoint = (PointNode) affectedLine.child(remainingIdx);
		final Vector2 viewCoordinates = remainingPoint.getPointInViewCoordinates();
		pointToRemove.set( (int) viewCoordinates.x , (int) viewCoordinates.y );

		if ( pointIdx == 0 ) {
			predecessor( affectedLine ).child(1).set( (int) viewCoordinates.x , (int) viewCoordinates.y );
		} else {
			successor( affectedLine ).child(0).set( (int) viewCoordinates.x , (int) viewCoordinates.y );
		}

		if ( affectedLine.length() < 1.0 ) {
			System.out.println("Line too short, removing "+affectedLine);
			removeLine( affectedLine );
		}
	}

	public void removeLine(LineNode lineToRemove) {

		if ( getChildCount() <= 3 ) {
			throw new UnsupportedOperationException("Refusing to remove line from polygon with only "+getChildCount()+" lines");
		}

		final LineNode predecessor = predecessor( lineToRemove );
		final LineNode successor = successor( lineToRemove );

		// TODO: The following code relies on the fact that lines in a polygon are always
		// TODO: linked in a clock-wise fashion

		Observers.unlink( lineToRemove );

		Observers.link( new HashSet<>( Arrays.asList( EventType.values() ) ) , predecessor.child(1), successor.child(0) );

		lineToRemove.remove();
	}

	private LineNode successor(LineNode n) {
		final int idx = indexOf( n );
		if ( idx < (getChildCount()-1) ) {
			return (LineNode) child( idx + 1);
		}
		return (LineNode) child( 0 );
	}

	private LineNode predecessor(LineNode n) {
		final int idx = indexOf( n );
		if ( idx == 0 ) {
			return (LineNode) child( getChildCount()-1 );
		}
		return (LineNode) child( idx - 1 );
	}
}