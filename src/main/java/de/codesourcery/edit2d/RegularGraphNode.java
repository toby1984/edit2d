package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public abstract class RegularGraphNode extends AbstractGraphNode {

	protected final List<IGraphNode> children = new ArrayList<>();

	public final Matrix3 modelMatrix = new Matrix3();
	public final Matrix3 combinedMatrix = new Matrix3();
	public final Matrix3 invMatrix = new Matrix3();

	private float dx;
	private float dy;

	private float angleInDeg;

	@Override
	public void rotate(float angleInDeg)
	{
		this.angleInDeg += angleInDeg;
		if ( this.angleInDeg >= 360.0f ) {
			this.angleInDeg -= 360;
		} else if ( this.angleInDeg < 0 ) {
			this.angleInDeg += 360;
		}
		updateModelMatrix();
		setFlag(Flag.DIRTY);
	}

	private void updateModelMatrix()
	{
		modelMatrix.setToTranslation( this.dx , this.dy );
		modelMatrix.mul( new Matrix3().setToRotation( angleInDeg ) );
	}

	@Override
	public void translate(float dx,float dy)
	{
		this.dx += dx;
		this.dy += dy;
		updateModelMatrix();
		setFlag(Flag.DIRTY);
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
		return "translate = "+dx+" / "+dy+" , rotate = "+angleInDeg+" , flags = "+super.toString();
	}

	@Override
	public Vector2 modelToView(Vector2 v) {
		return new Vector2(v).mul( combinedMatrix );
	}

	@Override
	public void updateCombinedMatrix(Matrix3 parent)
	{
		this.combinedMatrix.set( parent );
		this.combinedMatrix.mul( modelMatrix );

		this.invMatrix.set( this.combinedMatrix );
		this.invMatrix.inv();
		clearFlag(Flag.DIRTY);
	}

	@Override
	public Matrix3 getModelMatrix() {
		return modelMatrix;
	}

	@Override
	public void copyMetaDataFrom(IGraphNode other)
	{
		super.copyMetaDataFrom( other );
		if ( other instanceof RegularGraphNode )
		{
			final RegularGraphNode that = (RegularGraphNode) other;
			this.modelMatrix.set( that.modelMatrix );
			this.combinedMatrix.set( that.combinedMatrix );
			this.invMatrix.set( that.invMatrix );
			this.dx = that.dx;
			this.dy = that.dy;
			this.angleInDeg = that.angleInDeg;
		}
	}

	@Override
	public final List<IGraphNode> getChildren() {
		return children;
	}
}