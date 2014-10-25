package de.codesourcery.edit2d;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public interface IGraphNode extends ITreeNode
{
	public static final AtomicLong NODE_ID = new AtomicLong(0);

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

		public static String toString(int flags) {
			return Arrays.stream( Flag.values() ).filter( flag -> flag.isSet( flags ) ).map( Flag::name ).collect( Collectors.joining("|"));
		}
	}

    // projections
	public Vector2 modelToView(Vector2 v);

	public Vector2 viewToModel(Vector2 v);

	public Matrix3 getModelMatrix();

	public void updateCombinedMatrix(Matrix3 parent);

	public Matrix3 getCombinedMatrix();

	// flags
	public boolean hasFlag(Flag flag);

	public IGraphNode setFlag(Flag flag);

	public IGraphNode clearFlag(Flag flag);

	public IGraphNode setFlag(Flag flag,boolean onOff);

	// metadata functions
	public void update();

	public void update(Matrix3 matrix);

	public void copyMetaDataFrom(IGraphNode other);

	// 2D functions
	public float distanceTo(float x,float y);

	public Vector2 getCenterInViewCoordinates();

	public void translate(float dx, float dy);

	public void rotate(float angleInDeg);

	public void set(float x,float y);

	public boolean contains(float x,float y);

	public Rectangle2D.Float getBounds();

	// handles
	public IRotationHandle getRotationHandle(float viewX,float viewY,float pickRadius);

	public ITranslationHandle getTranslationHandle(float viewX,float viewY,float pickRadius);
}