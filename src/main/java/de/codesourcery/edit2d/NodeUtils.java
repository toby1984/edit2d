package de.codesourcery.edit2d;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.badlogic.gdx.math.Matrix3;

import de.codesourcery.edit2d.IGraphNode.Flag;

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
			if ( current.hasFlag(Flag.HIGHLIGHTED) ) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	public static boolean deleteNodes(List<IGraphNode> toDelete) {

		if ( toDelete.isEmpty() ) {
			return false;
		}

		final List<IGraphNode> toRemove = new ArrayList<>( toDelete );
		while ( ! toRemove.isEmpty() )
		{
			System.out.println("Deleting "+toDelete);

			final IGraphNode node = toRemove.remove(0);

			if ( node instanceof PointNode) // point removed
			{
				// point removed from line that is part of a polygon
				if ( node.getParent() instanceof LineNode && node.getParent().getParent() instanceof SimplePolygon )
				{
					final PointNode pn = (PointNode) node;
					((SimplePolygon) node.getParent().getParent()).removePointsAt( pn );
				} else {
					throw new RuntimeException("Don't know how to remove point from "+node.getParent() );
				}
			}
			else if ( node instanceof LineNode && node.getParent() instanceof SimplePolygon ) // line removed from polygon
			{
				if ( node.getParent().getChildCount() < 3 ) {
					toRemove.add( node.getParent() );
				} else {
					((SimplePolygon) node.getParent()).removeLine( (LineNode) node );
				}
			}
			else
			{
				node.remove();

				if ( node.getParent().hasNoChildren() && !( node.getParent() instanceof RootNode) )
				{
					toRemove.add( node.getParent() );
				}
			}
		}
		return true;
	}

	public static IGraphNode findCommonParentPolygon(List<IGraphNode> nodes)
	{
		return findCommonParent( nodes.stream().map( n -> {
			if ( n instanceof LineNode ) {
				return n.getParent();
			}
			if ( n instanceof PointNode )
			{
				if ( n.getParent() instanceof LineNode) {
					if ( n.getParent().getParent() instanceof SimplePolygon) {
						return n.getParent().getParent();
					}
				}
			}
			return n;
		}).collect(Collectors.toList() ) );
	}

	public static IGraphNode findCommonParent(List<IGraphNode> nodes)
	{
		if ( nodes.isEmpty() ) {
			return null;
		}

		if ( nodes.size() == 1 ) {
			return nodes.get(0).getParent();
		}

		final List<IGraphNode[]> paths = nodes.stream().map( n -> getPathToRoot(n) ).collect( Collectors.toList() );

		final int maxCommonLength = paths.stream().mapToInt( n -> n.length ).min().getAsInt();

		final int len = paths.size();

		for ( int result = 0 ; result < maxCommonLength ; result++ )
		{
			final IGraphNode expected = paths.get(0)[result];
			for ( int j = 1 ; j < len ; j++ )
			{
				final IGraphNode actual = paths.get(j)[result];
				if ( expected != actual )
				{
					if ( result == 0 ) {
						return null; // no common path
					}
					return paths.get(0)[result-1];
				}
			}
			result++;
		}
		// all paths are the same,pick from first
		final IGraphNode[] path = paths.get(0);
		return path[ path.length - 2 ];
	}

	/**
	 * Returns the path to the tree root node INCLUDING the input node.
	 *
	 * @param node
	 * @return Path starting at tree root
	 */
	public static IGraphNode[] getPathToRoot(IGraphNode node) {

		final List<IGraphNode> result = new ArrayList<>();
		IGraphNode current = node;
		while(current != null ) {
			result.add( current );
			current = current.getParent();
		}
		Collections.reverse( result );
		return result.toArray( new IGraphNode[ result.size() ] );
	}

	public static List<PointNode> getPointsAt(IGraphNode node,int x,int y) {
		final List<PointNode> result = new ArrayList<>();
		getPointsAt(node.getRoot(),x,y,result);
		return result;
	}

	private static void getPointsAt(IGraphNode current,int x,int y,List<PointNode> result) {

		if ( current instanceof PointNode)
		{
			final float d = current.distanceTo(x, y);
			System.out.println("distance: "+current+" -> ("+x+","+y+") = "+d);
			if ( d < 2 ) {
				result.add( (PointNode) current );
			}
		}
		else
		{
			for ( final IGraphNode child : current.getChildren() )
			{
				getPointsAt(child,x,y,result);
			}
		}
	}

	public static String matrixToString(Matrix3 matrix) {

		final DecimalFormat df = new DecimalFormat("0000.000");
		final StringBuilder builder = new StringBuilder();
		for ( int row = 0 ; row < 3 ; row++ )
		{
			for ( int col = 0 ; col < 3 ; col++ )
			{
				final int offset = row*3 + col;
				builder.append( df.format( matrix.val[offset] ) );
				if ( (col+1) < 3 ) {
					builder.append(" | ");
				}
			}
			builder.append("\n");
		}
		return builder.toString();
	}
}