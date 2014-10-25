package de.codesourcery.edit2d;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector2;

public class PointNode extends AbstractGraphNode
{
	public final Vector2 p = new Vector2();

	public PointNode(Point p) {
		this(p.x,p.y);
	}

	public PointNode(Point2D.Float p) {
		this(p.x,p.y);
	}

	public PointNode(float x,float y) {
		this.p.set(x,y);
	}

	public PointNode(Vector2 p) {
		this.p.set(p);
	}

	@Override
	public String toString() {
		return "Point #"+nodeId+" "+p;
	}

	@Override
	public float distanceTo(float x, float y) {
		return getCenterInViewCoordinates().dst( x , y);
	}

	@Override
	public ITranslationHandle getTranslationHandle(float viewX, float viewY,float pickRadius)
	{
		if ( distanceTo(viewX,viewY) <= pickRadius )
		{
			return new ITranslationHandle() {

				@Override
				public void translate(float dx, float dy) {
					PointNode.this.translate(dx,dy);
				}

				@Override
				public float distanceTo(float viewX, float viewY) {
					return PointNode.this.distanceTo(viewX,viewY);
				}

				@Override
				public IGraphNode getNode() {
					return PointNode.this;
				}
			};
		}
		return null;
	}

	@Override
	public IRotationHandle getRotationHandle(float viewX, float viewY,float pickRadius)
	{
		return null;
	}

	@Override
	public Vector2 getCenterInViewCoordinates() {
		return new Vector2(p).mul( getParent().getCombinedMatrix() );
	}

	@Override
	public boolean contains(float  x, float y) {
		final Vector2 view = getCenterInViewCoordinates();
		return Math.floor( view.dst( x ,  y ) ) <= 1f;
	}

	@Override
	public void update() {
	}

	@Override
	public List<IGraphNode> getChildren() {
		return Collections.emptyList();
	}

	@Override
	public void update(Matrix3 matrix) {
	}

	@Override
	public Rectangle2D.Float getBounds() {
		final Vector2 p = getCenterInViewCoordinates();
		return new Rectangle2D.Float( p.x , p.y , 1 , 1 );
	}

	@Override
	public Vector2 modelToView(Vector2 v) {
		return getParent().modelToView(v);
	}

	@Override
	public Vector2 viewToModel(Vector2 v) {
		return getParent().viewToModel(v);
	}

	@Override
	public Matrix3 getModelMatrix() {
		return getParent().getModelMatrix();
	}

	@Override
	public void updateCombinedMatrix(Matrix3 parent) {
	}

	@Override
	public Matrix3 getCombinedMatrix() {
		return getParent().getCombinedMatrix();
	}

	@Override
	public void translate(float dx, float dy)
	{
		this.p.x += dx;
		this.p.y += dy;
	}

	@Override
	public void rotate(float angleInDeg) {
		// rotating a point around it's origin has no effect
	}

	@Override
	public void set(float x, float y) {
		this.p.x = x;
		this.p.y = y;
	}
}