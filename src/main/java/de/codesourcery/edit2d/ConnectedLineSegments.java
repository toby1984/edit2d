package de.codesourcery.edit2d;

import java.awt.geom.Rectangle2D;
import java.util.List;

import com.badlogic.gdx.math.Vector2;

public class ConnectedLineSegments extends RegularGraphNode
{
	public ConnectedLineSegments(LineNode line) {
		addChildren(line);
	}

	@Override
	public float distanceTo(float x, float y)
	{
		float distance = 0;
		IGraphNode min = null;
		for ( IGraphNode child : getChildren() )
		{
			float d = child.distanceTo(x, y);
			if ( min == null || d < distance ) {
				min = child;
				distance = d;
			}
		}
		return distance;
	}

	public PointNode getLastPoint() {

		final List<IGraphNode> children = getChildren() ;
		if ( children.isEmpty() ) {
			throw new IllegalStateException("No children??");
		}
		return (PointNode) child( getChildCount()-1).child(1);
	}

	@Override
	public Vector2 getCenterInViewCoordinates() {

		Vector2 result = new Vector2(0,0);
		for ( IGraphNode child : getChildren() ) {
			Vector2 centerInViewCoordinates = child.getCenterInViewCoordinates();
			result.x += centerInViewCoordinates.x;
			result.y += centerInViewCoordinates.y;
		}
		return getChildCount() == 0 ? result : result.scl( 1f/getChildCount());
	}

	@Override
	public boolean contains(float x, float y) {
		return false;
	}

	@Override
	public Rectangle2D.Float getBounds()
	{
		if ( hasNoChildren() ) {
			return new Rectangle2D.Float(0,0,0,0);
		}

		Rectangle2D.Float result = null;
		for ( IGraphNode child : getChildren() )
		{
			if ( result == null ) {
				result = child.getBounds();
			} else {
				Rectangle2D.Float dst = new Rectangle2D.Float();
				Rectangle2D.Float.union( result , child.getBounds(), dst );
				result = dst;
			}
		}
		return result;
	}
}
