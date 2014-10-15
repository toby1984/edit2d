package de.codesourcery.edit2d;

public interface ISceneObserver {

	public void subtreeStructureChanged(IGraphNode changedNode);

	public void subtreeValuesChanged(IGraphNode changedNode);
}
