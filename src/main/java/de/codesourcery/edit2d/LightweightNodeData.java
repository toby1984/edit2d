package de.codesourcery.edit2d;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

/**
 * Node data without model matrices.
 *
 * @author tobias.gierke@code-sourcery.de
 */
public abstract class LightweightNodeData implements INodeData {

	public static enum Flag
	{
		SELECTABLE(1),DIRTY(2),HIGHLIGHTED(4),CAN_ROTATE(8);

		private final int bitMask;

		private Flag(int bitMask) {
			this.bitMask = bitMask;
		}

		public int set(int input) {
			return input | bitMask;
		}

		public int clear(int input) {
			return input & ~bitMask;
		}

		public boolean isSet(int input) {
			return (input&bitMask) != 0;
		}
	}

	/* !!!!!!!!!!!!!!!!!!!!!!!!!
	 * Make sure to adjust copyFrom(INodeData) when adding fields here
	 * !!!!!!!!!!!!!!!!!!!!!!!!!
	 */
	protected int flags = Flag.CAN_ROTATE.set( Flag.SELECTABLE.set(0) );

	protected final void setFlag(Flag flag,boolean onOff) {
		if ( onOff ) {
			setFlag(flag);
		} else {
			clearFlag(flag);
		}
	}

	protected final void setFlag(Flag flag) {
		flags = flag.set( flags );
	}

	@Override
	public final void set(float x,float y) {
		final Vector2 delta = viewToModel( new Vector2(0,0) );
		final float dx = x - delta.x;
		final float dy = y - delta.y;
		translate(dx,dy);
	}

	protected final void clearFlag(Flag flag)
	{
		flags = flag.clear( flags );
	}

	protected final boolean isSet(Flag flag) {
		return flag.isSet( this.flags );
	}

	@Override
	public final boolean isDirty() {
		return isSet(Flag.DIRTY);
	}

	@Override
	public final void markDirty() {
		setFlag(Flag.DIRTY);
	}

	@Override
	public final void setSelectable(boolean isSelectable) {
		setFlag(Flag.SELECTABLE , isSelectable );
	}

	@Override
	public final boolean isSelectable() {
		return isSet(Flag.SELECTABLE);
	}

	@Override
	public void setCanRotate(boolean yesNo) {
		setFlag(Flag.CAN_ROTATE , yesNo);
	}

	@Override
	public boolean canRotate() {
		return isSet(Flag.CAN_ROTATE);
	}

	@Override
	public final boolean isHighlighted() {
		return isSet(Flag.HIGHLIGHTED);
	}

	@Override
	public void setHighlighted(boolean yesNo) {
		setFlag(Flag.HIGHLIGHTED,yesNo);
	}

	@Override
	public void copyFrom(INodeData other)
	{
		this.flags = ((LightweightNodeData) other).flags;
	}

	/*
	 * ========== UNSUPPORTED OPERATIONS ==============
	 */

	@Override
	public Vector2 modelToView(Vector2 v) {
		throw new UnsupportedOperationException("modelToView() not implemented");
	}

	@Override
	public void translate(float dx, float dy) {
		throw new UnsupportedOperationException("translate() not implemented");
	}

	@Override
	public void rotate(float angleInDeg) {
		throw new UnsupportedOperationException("rotate() not implemented");
	}

	@Override
	public Matrix3 getCombinedMatrix() {
		throw new UnsupportedOperationException("getCombinedMatrix() not implemented");
	}

	@Override
	public void updateCombinedMatrix(Matrix3 parent) {
		throw new UnsupportedOperationException("updateCombinedMatrix() not implemented");
	}

	@Override
	public Matrix3 getModelMatrix() {
		throw new UnsupportedOperationException("getModelMatrix() not implemented");
	}
}