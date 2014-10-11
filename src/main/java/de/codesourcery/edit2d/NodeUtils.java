package de.codesourcery.edit2d;

public class NodeUtils {

	public static IGraphNode findClosestNode(IGraphNode node,int x, int y, float minimumDistance)
	{
		float minDistance = node.distanceTo( x ,  y );
		IGraphNode result = null;
		if ( minDistance <= minimumDistance ) {
			result = node;
		}
		for ( final IGraphNode child : node.getChildren() )
		{
			final IGraphNode candidate = findClosestNode( child , x , y , Math.min(minDistance, minimumDistance ) );
			if ( candidate != null )
			{
				final float d = candidate.distanceTo( x , y );
				if ( result == null || d <= minDistance ) {
					result = candidate;
					minDistance = d;
				}
			}
		}

		// special case for lines: prefer endpoint over whole line
		if ( result instanceof LineNode) {

			final float d1 = result.child(0).distanceTo( x ,  y );
			final float d2 = result.child(1).distanceTo( x ,  y );
			if ( d1 <= d2 ) {
				if ( d1 <= minimumDistance ) {
					return result.child(0);
				}
				if ( d2 <= minimumDistance ) {
					return result.child(1);
				}
			} else {
				if ( d2 <= minimumDistance ) {
					return result.child(1);
				}
				if ( d1 <= minimumDistance ) {
					return result.child(0);
				}
			}
		}
		return result;
	}

	public static IGraphNode findContainingNode(IGraphNode node, int x, int y)
	{
		IGraphNode finalResult = null;
		if ( node.contains(x, y) ) {
			finalResult = node;
		}
		for ( final IGraphNode child : node.getChildren() )
		{
			final IGraphNode result = findContainingNode( child , x ,  y );
			if ( result != null ) {
				return result;
			}
		}
		return finalResult;
	}

	public static boolean isHighlighted(IGraphNode n)
	{
		IGraphNode current = n;
		while ( current != null ) {
			if ( current.getMetaData().isHighlighted() ) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}
}