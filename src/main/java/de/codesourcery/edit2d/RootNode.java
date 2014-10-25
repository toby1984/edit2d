package de.codesourcery.edit2d;

import java.awt.geom.Rectangle2D;

import com.badlogic.gdx.math.Vector2;

public class RootNode extends RegularGraphNode
{
	@Override
	public Vector2 getCenterInViewCoordinates() {

		if ( hasNoChildren() ) {
			return new Vector2(0,0);
		}
		int count = 0;
		final Vector2 result = child(0).getCenterInViewCoordinates();
		for ( int i = 1 ; i < getChildCount() ; i++ ) {
			count++;
			result.add( child(i).getCenterInViewCoordinates() );
		}
		result.scl( 1f / count );
		return result;
	}

	@Override
	public String toString() {
		return "RootNode #"+nodeId;
	}

	@Override
	public float distanceTo(float x, float y) {
		return Float.POSITIVE_INFINITY;
	}

	@Override
	public boolean contains(float x, float y) {
		return false;
	}

	@Override
	public java.awt.geom.Rectangle2D.Float getBounds()
	{
		Rectangle2D.Float result = null;
		for ( final IGraphNode child : getChildren() ) {
			if ( result == null ) {
				result = child.getBounds();
			} else {
				result.add( child.getBounds() );
			}
		}
		return result == null ? new Rectangle2D.Float() : result ;
	}
}