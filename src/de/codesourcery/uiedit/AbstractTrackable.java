package de.codesourcery.uiedit;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTrackable implements ITrackable {

	protected final List<IObserver> observer = new ArrayList<>();
	protected final List<ITrackable> children = new ArrayList<>();

	private ITrackable parent;

	@Override
	public void addChild(ITrackable t) {
		this.children.add(t);
	}

	@Override
	public List<ITrackable> getChildren() {
		return children;
	}

	@Override
	public void setParent(ITrackable parent) {
		this.parent = parent;
	}

	@Override
	public ITrackable getParent() {
		return parent;
	}

	@Override
	public final void addObserver(IObserver o) {
		this.observer.add(o);
	}

	@Override
	public boolean isEditable() {
		return parent == null || parent.isEditable();
	}
}
