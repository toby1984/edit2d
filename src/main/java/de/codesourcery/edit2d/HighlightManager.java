package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.edit2d.IGraphNode.Flag;

public class HighlightManager {

	private final List<IGraphNode> highlighted = new ArrayList<>();

	public boolean setHighlight(IGraphNode n)
	{
		if (n == null) {
			throw new IllegalArgumentException("n must not be NULL");
		}

		if ( ! highlighted.isEmpty() )
		{
			if ( highlighted.size() == 1 && highlighted.contains( n ) )
			{
				return false;
			}
			clearHighlights();
		}
		n.visitPostOrder( node -> node.setFlag(Flag.HIGHLIGHTED) );
		highlighted.add( n );
		return true;
	}

	public List<IGraphNode> getHighlighted() {
		return new ArrayList<>( highlighted );
	}

	public boolean clearHighlights()
	{
		if ( ! highlighted.isEmpty() )
		{
			for ( final IGraphNode current : highlighted )
			{
				current.visitPostOrder( node -> node.clearFlag(Flag.HIGHLIGHTED) );
			}
			highlighted.clear();
			return true;
		}
		return false;
	}

}
