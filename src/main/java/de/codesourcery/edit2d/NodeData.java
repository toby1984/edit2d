package de.codesourcery.edit2d;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;


public class NodeData extends LightweightNodeData {

	public final Matrix3 modelMatrix = new Matrix3();
	public final Matrix3 combinedMatrix = new Matrix3();

	private float dx;
	private float dy;

	@Override
	public void translate(float dx,float dy) {
		this.dx += dx;
		this.dy += dy;
		modelMatrix.setToTranslation( this.dx ,  this.dy );
		markDirty();
	}

	@Override
	public Vector2 viewToModel(Vector2 v)
	{
		// TODO: Always calculating the inverse here might become too slow... maybe do this in updateCombinedMatrix() instead
		return v.mul( getCombinedMatrix().inv() );
	}

	@Override
	public Matrix3 getCombinedMatrix() {
		return combinedMatrix;
	}

	@Override
	public String toString() {
		return "translate = "+dx+" / "+dy+" , selectable = "+isSelectable();
	}

	@Override
	public Vector2 modelToView(Vector2 v) {
		return new Vector2(v).mul( combinedMatrix );
	}

	@Override
	public void updateCombinedMatrix(Matrix3 parent)
	{
		this.combinedMatrix.set( modelMatrix );
		this.combinedMatrix.mul( parent );
		clearFlag(Flag.DIRTY);
	}

	@Override
	public Matrix3 getModelMatrix() {
		return modelMatrix;
	}

	@Override
	public void setModelMatrix(Matrix3 m) {
		this.modelMatrix.set( m );
		markDirty();
	}
}