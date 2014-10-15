package de.codesourcery.edit2d;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class SceneGraphDebugPanel extends JPanel implements ISceneObserver
{
	private final MyTreeModel treeModel;
	private final JTree tree;

	protected final IGraphNode root;

	protected final class MyTreeModel implements TreeModel {

		private final List<TreeModelListener> listener = new ArrayList<>();

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public Object getChild(Object parent, int index) {
			return ((IGraphNode) parent).child(index);
		}

		@Override
		public int getChildCount(Object parent) {
			return ((IGraphNode) parent).getChildren().size();
		}

		@Override
		public boolean isLeaf(Object node) {
			return ((IGraphNode) node).getChildren().isEmpty();
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			throw new UnsupportedOperationException("valueForPathChanged not implemented yet");
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			return ((IGraphNode) parent).indexOf( (IGraphNode) child );
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			listener.add( l );
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			listener.remove(l);
		}

		public void subtreeStructureChanged(IGraphNode changedNode)
		{
			final TreeModelEvent event = new TreeModelEvent(this, createPath( changedNode ) );
			listener.forEach( l -> l.treeStructureChanged( event ) );
		}

		public void subtreeValuesChanged(IGraphNode changedNode)
		{
			changedNode.visitPostOrder( node ->
			{
				// TODO: Currently sending one event per node ... Performance will be REALLY bad when a huge subtree gets updated...
				final TreeModelEvent event = new TreeModelEvent(this, createPath( node ) );
				listener.forEach( l -> l.treeNodesChanged( event ) );
			});
		}

		private TreePath createPath(IGraphNode n) {

			final List<IGraphNode> path = new ArrayList<>();
			IGraphNode current = n;
			while ( current != null ) {
				path.add( current );
				current = current.getParent();
			}
			Collections.reverse( path );
			return new TreePath( path.toArray() );
		}
	}

	public SceneGraphDebugPanel(IGraphNode root)
	{
		this.root = root;
		this.treeModel = new MyTreeModel();
		this.tree = new JTree( treeModel );

		setLayout( new BorderLayout() );
		add( tree , BorderLayout.CENTER );

		final TreeCellRenderer cellRenderer = new DefaultTreeCellRenderer() {

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,boolean leaf, int row, boolean hasFocus)
			{
				final Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				return result;
			}
		};
		tree.setCellRenderer( cellRenderer );
	}

	@Override
	public void subtreeStructureChanged(IGraphNode changedNode) {
		treeModel.subtreeStructureChanged( changedNode );
	}

	@Override
	public void subtreeValuesChanged(IGraphNode changedNode) {
		treeModel.subtreeValuesChanged( changedNode );
	}
}