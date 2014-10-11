package de.codesourcery.edit2d;

public interface INodeObserver
{
	public boolean invokeFor(EventType eventType);

	public void nodeTranslated(EventType eventType,IGraphNode node,int dx,int dy);
}
