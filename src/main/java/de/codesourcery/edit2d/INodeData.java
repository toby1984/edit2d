package de.codesourcery.edit2d;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public interface INodeData {

	public void translate(float dx, float dy);

	public void rotate(float angleInDeg);

	public void set(float x,float y);

	public boolean isDirty();

	public void markDirty();

	public Vector2 modelToView(Vector2 v);

	public Vector2 viewToModel(Vector2 v);

	public Matrix3 getModelMatrix();

	public void updateCombinedMatrix(Matrix3 parent);

	public Matrix3 getCombinedMatrix();

	public void setSelectable(boolean isSelectable);

	public boolean isSelectable();

	public boolean canRotate();

	public void setCanRotate(boolean yesNo);

	public boolean isHighlighted();

	public void setHighlighted(boolean yesNo);

	public void copyFrom(INodeData other);
}