package de.codesourcery.edit2d;

import java.util.List;

public interface IObservable
{
	public void addObserver(INodeObserver o);

	public void removeObserver(INodeObserver o);

	public List<INodeObserver> getObservers();
}
