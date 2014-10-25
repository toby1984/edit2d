package de.codesourcery.edit2d;

public interface IHandle
{
	public float distanceTo(float viewX,float viewY);

	public IGraphNode getNode();
}
