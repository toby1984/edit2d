package de.codesourcery.uiedit;

public interface IObserver {

	public void moved(IObservable o, int dx, int dy);
}
