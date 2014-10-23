package de.codesourcery.edit2d;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class Observers {

	public static void link(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.link(categories,n1,n2,additional);
	}

	public static void linkX(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.linkX(categories,n1,n2,additional);
	}

	public static void linkY(Set<EventType> categories,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.linkY(categories,n1,n2,additional);
	}

	public static void link(EventType type,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.link(asSet(type),n1,n2,additional);
	}

	public static void link(EventType type1,EventType type2,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.link(asSet(type1,type2),n1,n2,additional);
	}

	private static Set<EventType> asSet(EventType t) {
		return Collections.singleton( t );
	}

	private static Set<EventType> asSet(EventType t,EventType...additional) {
		final Set<EventType> result = new HashSet<>();
		result.add(t);
		if ( additional != null ) {
			result.addAll(Arrays.asList(additional));
		}
		return result;
	}

	public static void unlink(IGraphNode toUnlink)
	{
		final IGraphNode root = toUnlink.getRoot();
		toUnlink.visitPostOrder( currentNode ->
		{
			final Predicate<INodeObserver> predicate = LinkConstraint.link( toUnlink );
			root.visitPostOrder( node -> {

				final List<INodeObserver> observers = node.getObservers();
				for (int i = 0; i < observers.size(); i++)
				{
					final INodeObserver o = observers.get(i);
					if ( predicate.test( o ) )
					{
						System.out.println("Removing: "+o);
						observers.remove(i);
						i--;
					}
				}
			});
		});
	}

	public static boolean isLink(INodeObserver o) {
		return o instanceof LinkConstraint;
	}

	public static Predicate<INodeObserver> isXYLinkPredicate(IGraphNode nodeToLink) {
		return ob -> isLink( ob ) && ((LinkConstraint) ob).hasType(LinkConstraint.LinkType.XY) && ((LinkConstraint) ob).getLinkedNodes().contains( nodeToLink );
	}

	public static void linkX(EventType type,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.linkX(asSet(type),n1,n2,additional);
	}

	public static void linkY(EventType type,IGraphNode n1,IGraphNode n2,IGraphNode... additional)
	{
		LinkConstraint.linkY(asSet(type),n1,n2,additional);
	}
}
