package de.codesourcery.uiedit;

import java.awt.Point;
import java.util.List;

public interface ITrackable extends IObservable
{
	public float minimumDistanceTo(Point point);

	public void addChild(ITrackable t);

	public List<ITrackable> getChildren();

	public void translate(int dx,int dy);

	public void translateNoNotify(int dx,int dy);

	public ITrackable getParent();

	public void setParent(ITrackable parent);

	public ITrackable getClosestTrackable(Point point,float maxRadius);

	public boolean contains(Point point);

	public boolean isEditable();
}
