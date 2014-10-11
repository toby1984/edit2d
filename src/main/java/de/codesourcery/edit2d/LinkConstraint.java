package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinkConstraint implements INodeObserver {

	private final LinkType type;
	private final List<IGraphNode> nodes = new ArrayList<>();

	private final Set<EventType> categories = new HashSet<>();

	public static enum LinkType {
		XY {
			@Override
			public void nodeTranslated(EventType eventType,IGraphNode node, int dx, int dy) {
				node.translate( eventType , dx, dy );
			}
		},
		X {
			@Override
			public void nodeTranslated(EventType eventType,IGraphNode node, int dx, int dy) {
				node.translate( eventType , dx, 0 );
			}
		},
		Y {
			@Override
			public void nodeTranslated(EventType eventType,IGraphNode node, int dx, int dy) {
				node.translate( eventType , 0, dy );
			}
		};

		public abstract void nodeTranslated(EventType eventType,IGraphNode node, int dx, int dy);
	}

	public List<IGraphNode> getLinkedNodes() {
		return nodes;
	}

	public void replace(IGraphNode oldNode,IGraphNode newNode)
	{
		final int len = nodes.size();
		for ( int i = 0 ; i < len ; i++ ) {
			if ( nodes.get(i) == oldNode ) {
				nodes.set(i,newNode);
			}
		}
	}

	private LinkConstraint(LinkType type,Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		this.categories.addAll(categories);
		this.type=type;

		nodes.add(n1);
		nodes.add(n2);
		if ( additional != null )
		{
			nodes.addAll(Arrays.asList(additional) );
		}
		nodes.forEach( n -> n.addObserver( this ) );
	}

	public static void link(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		new LinkConstraint(LinkType.XY , categories, n1,n2,additional);
	}

	public static void linkX(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		new LinkConstraint(LinkType.X , categories, n1,n2,additional);
	}

	public static void linkY(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		new LinkConstraint(LinkType.Y ,categories,  n1,n2,additional);
	}

	@Override
	public void nodeTranslated(EventType eventType,IGraphNode node, int dx, int dy)
	{
		for ( final IGraphNode n : nodes ) {
			if ( n != node ) {
				type.nodeTranslated( eventType, n , dx , dy );
			}
		}
	}

	@Override
	public boolean invokeFor(EventType eventType)
	{
		return this.categories.contains( eventType );
	}
}