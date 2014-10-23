package de.codesourcery.edit2d;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;


public class NodeData extends LightweightNodeData {

	/* !!!!!!!!!!!!!!!!!!!!!!!!!
	 * Make sure to adjust copyFrom(INodeData) when adding fields here
	 * !!!!!!!!!!!!!!!!!!!!!!!!!
	 */

	public final Matrix3 modelMatrix = new Matrix3();
	public final Matrix3 combinedMatrix = new Matrix3();
	public final Matrix3 invMatrix = new Matrix3();

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
		return new Vector2(v).mul( invMatrix );
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

		this.invMatrix.set( this.combinedMatrix );
		this.invMatrix.inv();
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

	@Override
	public void copyFrom(INodeData other)
	{
		super.copyFrom( other );
		if ( other instanceof NodeData )
		{
			final NodeData that = (NodeData) other;
			this.modelMatrix.set( that.modelMatrix );
			this.combinedMatrix.set( that.combinedMatrix );
			this.invMatrix.set( that.invMatrix );
			this.dx = that.dx;
			this.dy = that.dy;
		}
	}
}