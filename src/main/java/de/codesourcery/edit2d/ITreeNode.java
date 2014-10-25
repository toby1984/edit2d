package de.codesourcery.edit2d;

import java.util.List;

public interface ITreeNode
{
	@FunctionalInterface
	public interface INodeVisitor
	{
		public void visit(IGraphNode n);
	}

	// tree functions
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

	public void addChildren(IGraphNode n1,IGraphNode... additional);

	public void visitPreOrder(INodeVisitor v);

	public void visitPostOrder(INodeVisitor v);
}
