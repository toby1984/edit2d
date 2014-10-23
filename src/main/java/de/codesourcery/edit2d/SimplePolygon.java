package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class SimplePolygon extends RegularGraphNode
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

		assertValid();
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

	private LineNode findLine(PointNode pn)
	{
		for ( final IGraphNode child : children )
		{
			if ( child.indexOf( pn ) != -1 ) {
				return (LineNode) child;
			}
		}
		throw new NoSuchElementException("No line of this polygon contains "+pn);
	}

	public void removePointsAt(PointNode p1)
	{
		final Vector2 p = p1.getPointInViewCoordinates();

		final List<PointNode> points = NodeUtils.getPointsAt( p1  , (int) p.x , (int) p.y );
		if ( points.size() != 2 ) {
			throw new RuntimeException("Found "+points.size()+" points while looking for companion of "+p1);
		}
		removePoints(points.get(0),points.get(1));
	}

	public void removePoints(PointNode p1,PointNode p2)
	{
		removeLine( findLine(p2) );
	}

	public void removeLine(LineNode lineToRemove) {

		final int idx = indexOf(lineToRemove);
		if ( idx == -1 ) {
			throw new IllegalArgumentException("Line "+lineToRemove+" is no part of "+this);
		}

		System.out.println("removeLine(): Removing line "+lineToRemove+" @ "+idx);

		if ( getChildCount() <= 3 ) {
			throw new UnsupportedOperationException("Refusing to remove line from polygon with only "+getChildCount()+" lines");
		}

		final LineNode predecessor = predecessor( lineToRemove );
		final LineNode successor = successor( lineToRemove );

		System.out.println("removeLine(): Predecessor "+predecessor+" @ "+indexOf(predecessor));
		System.out.println("removeLine(): Successor "+successor+" @ "+indexOf(successor));

		// TODO: The following code relies on the fact that lines in a polygon are always
		// TODO: linked in a clock-wise fashion

		unlink( lineToRemove );

		final Vector2 viewCoords = ((PointNode) successor.child(0)).getPointInViewCoordinates();

		predecessor.child(1).set( (int) viewCoords.x , (int) viewCoords.y , false );
		Observers.link( new HashSet<>( Arrays.asList( EventType.values() ) ) , predecessor.child(1), successor.child(0) );

		lineToRemove.remove();
		assertValid();
	}

	public void assertValid() {

		if ( getChildCount() < 3 ) {
			throw new IllegalStateException("Polygon has less than 3 lines ??");
		}

		for ( int i = 0 ; i < getChildCount() ; i++ )
		{
			final LineNode previous = (LineNode) ( (i != 0 ) ? child(i-1) : child( getChildCount() -1 ) );
			final LineNode current = (LineNode) child(i);
			final LineNode next = (LineNode) ( (i != getChildCount()-1 ) ? child(i+1) : child(0) );

			if ( ! isPointsAtSameLocation( (PointNode) previous.child(1), (PointNode) current.child(0) ) ) {
				throw new IllegalStateException("Starting point of line #"+i+" is not the same as end point of line #"+(i-1));
			}

			if ( ! isPointsAtSameLocation( (PointNode) current.child(1), (PointNode) next.child(0) ) ) {
				throw new IllegalStateException("End point of line #"+i+" is not the same as end point of line #"+(i+1));
			}

			if ( current.child(1).getObservers().size() != 1 ) { // TODO: Check for debugging stuff, remove when done ... will fail when user is able to create arbitrary links between points
				throw new IllegalStateException("End point of line #"+i+" has invalid observer count "+current.child(1).getObservers().size()+" , expected exactly 2. "+current);
			}

			if ( current.child(0).getObservers().size() != 1 ) { // TODO: Check for debugging stuff, remove when done ... will fail when user is able to create arbitrary links between points
				throw new IllegalStateException("Starting point of line #"+i+" has invalid observer count "+current.child(0).getObservers().size()+" , expected exactly 2. "+current);
			}

			if ( ! LinkConstraint.areXYLinked( previous.child(1) , current.child(0) ) ) {
				throw new IllegalStateException("Line #"+i+" is broken: End point of line "+previous+" is not linked with starting point of line "+current);
			}
			if ( ! LinkConstraint.areXYLinked( current.child(1) , next.child(0) ) ) {
				throw new IllegalStateException("Line #"+i+" is broken: End point of line "+current+" is not linked with starting point of line "+next);
			}
		}
	}

	private boolean isPointsAtSameLocation(PointNode p1,PointNode p2) {

		final Vector2 pv1 = p1.getPointInViewCoordinates();
		final Vector2 pv2 = p2.getPointInViewCoordinates();
		final float dst = (float) Math.floor( pv1.dst( pv2 ) );
		final boolean result = dst <= 1.0;
		if ( ! result ) {
			System.err.println("distance: "+dst);
		}
		return result;
	}

	private void unlink(LineNode lineToRemove) {
		Observers.unlink( lineToRemove.child(0) );
		Observers.unlink( lineToRemove.child(1) );
		Observers.unlink( lineToRemove );
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

	@Override
	public String toString() {
		return "SimplePolygon #"+nodeId;
	}
}