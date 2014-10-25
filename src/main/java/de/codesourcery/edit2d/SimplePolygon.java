package de.codesourcery.edit2d;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.NoSuchElementException;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class SimplePolygon extends RegularGraphNode
{
	public SimplePolygon(float x,float y,float width,float height)
	{
		final float halfWidth = width/2f;
		final float halfHeight = width/2f;

		final Point2D.Float p0 = new Point2D.Float(-halfWidth,-halfHeight);
		final Point2D.Float p1 = new Point2D.Float(+halfWidth,-halfHeight);
		final Point2D.Float p2 = new Point2D.Float(+halfWidth,+halfHeight);
		final Point2D.Float p3 = new Point2D.Float(-halfWidth,+halfHeight);

		final LineNode l0 = new LineNode(  p0 ,p1 );
		final LineNode l1 = new LineNode(  p1 ,p2 );
		final LineNode l2 = new LineNode(  p2 ,p3 );
		final LineNode l3 = new LineNode(  p3 ,p0 );

		translate( x , y );

		addChildren( l0,l1,l2, l3 );

		assertValid();
	}

	public SimplePolygon() {
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

	@Override
	public Vector2 getCenterInViewCoordinates()
	{
		final Rectangle2D.Float b = getBounds();
		return new Vector2( b.x + b.width/2f , b.y + b.height/2f );
	}

	@Override
	public float distanceTo(float x, float y)
	{
		return getCenterInViewCoordinates().dst(x,y);
	}

	@Override
	public boolean contains(float x, float y)
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
		final Vector2 p = p1.getCenterInViewCoordinates();

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

		final Vector2 viewCoords = ((PointNode) successor.child(0)).getCenterInViewCoordinates();

		predecessor.child(1).set( viewCoords.x , viewCoords.y );

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
		}
	}

	private boolean isPointsAtSameLocation(PointNode p1,PointNode p2) {

		final Vector2 pv1 = p1.getCenterInViewCoordinates();
		final Vector2 pv2 = p2.getCenterInViewCoordinates();
		final float dst = (float) Math.floor( pv1.dst( pv2 ) );
		final boolean result = dst <= 1.0;
		if ( ! result ) {
			System.err.println("distance: "+dst);
		}
		return result;
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

	@Override
	public ITranslationHandle getTranslationHandle(float viewX, float viewY,float pickRadius)
	{
		final ITranslationHandle handle = super.getTranslationHandle(viewX,viewY,pickRadius);
		if ( handle == null && contains(viewX,viewY ) )
		{
			return new ITranslationHandle() {

				@Override
				public float distanceTo(float viewX, float viewY) {
					return SimplePolygon.this.distanceTo(viewX,viewY);
				}

				@Override
				public IGraphNode getNode() { return SimplePolygon.this; }

				@Override
				public void translate(float dx, float dy) {
					SimplePolygon.this.translate(dx,dy);
				}
			};
		}
		if ( handle != null )
		{
			if ( handle.getNode() instanceof PointNode)
			{
				// find companion point
				final LineNode line = (LineNode) handle.getNode().getParent();
				final int pointIdx = line.indexOf( handle.getNode() );
				PointNode companion;
				if ( pointIdx == 0 ) {
					companion = (PointNode) predecessor( line ).child(1);
				} else if ( pointIdx == 1 ) {
					companion = (PointNode) successor( line ).child(0);
				} else {
					throw new RuntimeException("Internal error");
				}
				return new ITranslationHandle() {

					@Override
					public void translate(float dx, float dy) {
						handle.translate(dx, dy);
						companion.translate(dx,dy);
					}

					@Override
					public float distanceTo(float viewX, float viewY) {
						return handle.distanceTo(viewX, viewY);
					}

					@Override
					public IGraphNode getNode() {
						return handle.getNode();
					}
				};

			}
			else if ( handle.getNode() instanceof LineNode)
			{
				final LineNode succ = successor( (LineNode) handle.getNode() );
				final LineNode pred = predecessor( (LineNode) handle.getNode() );
				return new ITranslationHandle() {

					@Override
					public void translate(float dx, float dy) {
						handle.translate(dx, dy);
						pred.child(1).translate(dx,dy);
						succ.child(0).translate(dx,dy);
					}

					@Override
					public float distanceTo(float viewX, float viewY) {
						return handle.distanceTo(viewX, viewY);
					}

					@Override
					public IGraphNode getNode() {
						return handle.getNode();
					}
				};
			}
		}
		return handle;
	}

	@Override
	public IRotationHandle getRotationHandle(float viewX, float viewY,float pickRadius)
	{
		if ( contains(viewX, viewY) )
		{
			return new IRotationHandle()
			{
				@Override
				public void rotate(float deltaAngleInDeg) {
					SimplePolygon.this.rotate( deltaAngleInDeg );
				}

				@Override
				public float distanceTo(float viewX, float viewY) {
					return SimplePolygon.this.distanceTo(viewX, viewY);
				}

				@Override
				public IGraphNode getNode() { return SimplePolygon.this; }
			};
		}
		return null;
	}
}