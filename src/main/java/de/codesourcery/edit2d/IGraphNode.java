package de.codesourcery.edit2d;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.badlogic.gdx.math.Matrix3;

public interface IGraphNode
{
	public static final AtomicLong NODE_ID = new AtomicLong(0);

	public interface INodeVisitor
	{
		public void visit(IGraphNode n);
	}

	public boolean isDirty();

	public void addObserver(INodeObserver o);

	public void removeObserver(INodeObserver o);

	public void update();

	public List<IGraphNode> getChildren();

	public IGraphNode getParent();

	public void setParent(IGraphNode parent);

	public IGraphNode child(int index);

	public void insertChild(int index,IGraphNode n);

	public void setChild(int index,IGraphNode n);

	public int getChildCount();

	public boolean hasChildren();

	public boolean hasNoChildren();

	public void removeChild(IGraphNode child);

	public void remove();

	public int indexOf(IGraphNode child);

	public IGraphNode getRoot();

	public List<INodeObserver> getObservers();

	public void addChildren(IGraphNode n1,IGraphNode... additional);

	public float distanceTo(int x,int y);

	public void visitPreOrder(INodeVisitor v);

	public void visitPostOrder(INodeVisitor v);

	public void translate(EventType eventType,int dx, int dy);

	public void set(int x,int y , boolean notifyObservers);

	public boolean contains(int x,int y);

	public INodeData getMetaData();

	public void update(Matrix3 matrix);

	public Rectangle2D.Float getBounds();
}
