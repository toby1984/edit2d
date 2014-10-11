package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
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
		Observer.link( asSet(EventType.TRANSLATE_POINT,EventType.TRANSLATE_LINE , EventType.PARENT_MOVED ) , l0.child( 1 ) , l1.child( 0 ) );
		Observer.link( asSet(EventType.TRANSLATE_POINT,EventType.TRANSLATE_LINE , EventType.PARENT_MOVED ) , l1.child( 1 ) , l2.child( 0 ) );
		Observer.link( asSet(EventType.TRANSLATE_POINT,EventType.TRANSLATE_LINE , EventType.PARENT_MOVED ) , l2.child( 1 ) , l3.child( 0 ) );
		Observer.link( asSet(EventType.TRANSLATE_POINT,EventType.TRANSLATE_LINE , EventType.PARENT_MOVED ) , l3.child( 1 ) , l0.child( 0 ) );
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

	protected Rectangle2D.Float bounds() {

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
		final Rectangle2D.Float b = bounds();
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
		final Rectangle2D.Float rect = bounds();
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
}