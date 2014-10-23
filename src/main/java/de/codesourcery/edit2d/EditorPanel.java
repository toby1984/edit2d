package de.codesourcery.edit2d;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;

import com.badlogic.gdx.math.Vector2;

public class EditorPanel extends JPanel {

	private final Object modelLock = new Object();

	private RootNode rootNode = new RootNode();

	protected static final Color REGULAR_COLOR = Color.GREEN;
	protected static final Color HIGHLIGHT_COLOR = Color.BLUE;
	protected static final Color POINT_COLOR = Color.RED;

	protected static final int SELECTION_RADIUS = 10;

	// @GuardedBy( sceneObservers )
	protected final List<ISceneObserver> sceneObservers = new ArrayList<>();

	protected final HighlightManager highlightManager = new HighlightManager();

	public static enum EditMode {
		MOVE,DRAW,CREATE_POINTS,ROTATE;
	}

	private EditMode currentEditingMode = EditMode.MOVE;

	protected Vector2 pointOnLine = null;

	protected EditMode editModeOverride = null;

	private final MyMouseAdapter mouseListener = new MyMouseAdapter();

	private final KeyAdapter keyListener = new KeyAdapter() {

		@Override
		public void keyPressed(java.awt.event.KeyEvent e)
		{
			if ( e.getKeyCode() == KeyEvent.VK_SPACE )
			{
				if ( editModeOverride != EditMode.MOVE)
				{
					setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR ) );
					editModeOverride = EditMode.MOVE;
				}
			}
		}

		@Override
		public void keyReleased(java.awt.event.KeyEvent e)
		{
			switch( e.getKeyCode() )
			{
				case KeyEvent.VK_DELETE:
					synchronized( modelLock )
					{
						final List<IGraphNode> nodesToDelete = highlightManager.getHighlighted();
						final IGraphNode commonParent = NodeUtils.findCommonParentPolygon( nodesToDelete );
						if ( ! nodesToDelete.isEmpty() && NodeUtils.deleteNodes( nodesToDelete ) )
						{
							highlightManager.clearHighlights();
							if ( commonParent != null ) {
								subtreeStructureChanged( commonParent );
							} else {
								nodesToDelete.forEach( n -> subtreeStructureChanged( n ) );
							}
							repaint();
						}
					}
					break;
				case KeyEvent.VK_SPACE:
					if ( editModeOverride == EditMode.MOVE) {
						setCursor( Cursor.getDefaultCursor() );
						editModeOverride = null;
					}
					break;
				default:
					// $$FALL-THROUGH$$
			}
		}

	};

	protected final class MyMouseAdapter extends MouseAdapter
	{
		private final Point lastPos = new Point();
		private IGraphNode dragged = null;

		@Override
		public void mousePressed(MouseEvent e)
		{
			if ( getEditMode() == EditMode.MOVE )
			{
				synchronized(modelLock)
				{
					if ( dragged == null && e.getButton() == MouseEvent.BUTTON1 )
					{
						IGraphNode candidate = NodeUtils.findClosestNode( rootNode,e.getX(), e.getY() , SELECTION_RADIUS );
						if ( candidate == null || ! candidate.getMetaData().isSelectable() )
						{
							candidate = NodeUtils.findContainingNode( rootNode , e.getX() ,  e.getY() );
						}
						if ( candidate != null && candidate.getMetaData().isSelectable() )
						{
							lastPos.setLocation( e.getPoint() );
							dragged = candidate;
						}
					}
				}
			}
		}

		public void modelChanged() {
			synchronized( modelLock ) {
				dragged = null;
			}
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ( getEditMode() == EditMode.CREATE_POINTS && e.getButton() == MouseEvent.BUTTON1 )
			{
				synchronized(modelLock)
				{
					final IGraphNode candidate = NodeUtils.findClosestNode( rootNode,e.getX(), e.getY() , SELECTION_RADIUS );
					if ( candidate instanceof LineNode)
					{
						if ( ((LineNode) candidate).split( e.getX() , e.getY() ) ) {
							subtreeStructureChanged( candidate.getParent() );
							repaint();
						}
					}
				}
			}
		};

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if ( getEditMode() == EditMode.MOVE )
			{
				synchronized (modelLock) {
					if ( dragged != null && e.getButton() == MouseEvent.BUTTON1 )
					{
						dragged = null;
					}
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			synchronized(modelLock)
			{
				IGraphNode candidate = NodeUtils.findClosestNode( rootNode,e.getX(), e.getY() , 15 );
				if ( candidate == null ) {
					candidate = NodeUtils.findContainingNode( rootNode , e.getX() , e.getY() );
				}

				if ( candidate != null )
				{
					if ( highlightManager.setHighlight( candidate ) ) {
						setToolTipText( candidate.toString() );
						repaint();
					}
				}
				else if ( highlightManager.clearHighlights() ) {
					setToolTipText( null );
					repaint();
				}

				if ( getEditMode() == EditMode.CREATE_POINTS && candidate instanceof LineNode ) {
					pointOnLine = ((LineNode) candidate).getClosestPointOnLine( e.getX() ,  e.getY() );
					repaint();
				} else {
					pointOnLine = null;
				}
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if ( getEditMode() == EditMode.MOVE )
			{
				synchronized( modelLock )
				{
					if ( dragged != null )
					{
						int dx = e.getX() - lastPos.x;
						int dy = e.getY() - lastPos.y;

						final EventType t = EventType.TRANSLATED;
						if ( dragged instanceof LineNode )
						{
							final LineNode l = (LineNode) dragged;
							if ( l.isHorizontalLine() ) {
								dx = 0;
							} else if ( l.isVerticalLine() ) {
								dy = 0;
							}
						}
						System.out.flush();
						System.err.flush();
						System.err.println("--------- Mouse dragged , translating "+dx+","+dy);
						System.err.flush();
						dragged.translate( t , dx, dy );
					}
					lastPos.setLocation( e.getPoint() );
					if ( dragged != null ) {
						subtreeValuesChanged(dragged);
					}
					repaint();
				}
			}
		}
	};

	protected void subtreeStructureChanged(IGraphNode changedNode) {
		invokeObservers( o -> o.subtreeStructureChanged( changedNode ) );
	}

	private void invokeObservers(Consumer<ISceneObserver> consumer)
	{
		final List<ISceneObserver> copy;
		synchronized(sceneObservers) {
			copy = new ArrayList<>( sceneObservers );
		}
		copy.forEach( ob -> consumer.accept( ob ) );
	}

	protected void subtreeValuesChanged(IGraphNode changedNode) {
		invokeObservers( o -> o.subtreeValuesChanged( changedNode ) );
	}

	public EditorPanel()
	{
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
		addKeyListener( keyListener );
		setFocusable(true);
		requestFocus();
		setRequestFocusEnabled(true);
	}

	public void addSceneObserver(ISceneObserver o)
	{
		synchronized (sceneObservers) {
			this.sceneObservers.add(o);
		}
		synchronized( modelLock )
		{
			o.modelChanged( this.rootNode );
		}
	}

	public void setEditMode(EditMode editMode)
	{
		if (editMode == null) {
			throw new IllegalArgumentException("editMode must not be NULL");
		}
		this.editModeOverride = null;
		this.currentEditingMode = editMode;
		repaint();
	}

	@Override
	public void paint(Graphics g) {

		final Graphics2D graphics = (Graphics2D) g;
		super.paint( g );

		synchronized(modelLock)
		{
			if ( pointOnLine != null ) {
				g.setColor(Color.RED);
				g.drawLine( 0 , (int) pointOnLine.y , getWidth() , (int) pointOnLine.y );
				g.drawLine( (int) pointOnLine.x , 0 , (int) pointOnLine.x, getHeight() );
			}

			graphics.setColor(Color.GREEN);

			rootNode.processNodeUpdates();

			rootNode.update();

			final List<IGraphNode> highlighted = new ArrayList<>();

			rootNode.visitPreOrder( node ->
			{
				if ( node.getMetaData().isHighlighted() ) {
					highlighted.add(node);
					return;
				}
				if ( node instanceof SimplePolygon)
				{
					try {
						((SimplePolygon) node).assertValid();
					} catch(final Exception e) {
						e.printStackTrace();
					}
				}
				render( node , graphics );
			});

			highlighted.forEach( node -> render( node , graphics ) );
		}
	}

	private void render(IGraphNode t, Graphics2D graphics)
	{
		if ( t instanceof LineNode ) {
			final LineNode l = (LineNode) t;
			final Vector2 p0 = l.p0();
			final Vector2 p1 = l.p1();

			if ( NodeUtils.isHighlighted( t ) ) {
				graphics.setColor( HIGHLIGHT_COLOR );
			} else {
				graphics.setColor( REGULAR_COLOR );
			}
			graphics.drawLine( (int) p0.x , (int) p0.y , (int) p1.x , (int) p1.y );
		}
		else if ( t instanceof PointNode  )
		{
			final Vector2 p = ((PointNode) t).getPointInViewCoordinates();
			if ( t.getMetaData().isHighlighted() ) {
				graphics.setColor( HIGHLIGHT_COLOR );
			} else {
				graphics.setColor( EditorPanel.POINT_COLOR );
			}
			graphics.drawOval( (int) (p.x - 3) , (int) (p.y - 3 ) , 7 , 7 );
		}
	}

	public void highlight(IGraphNode n) {
		highlightManager.setHighlight( n );
		repaint();
	}

	public void setModel(RootNode root)
	{
		synchronized( modelLock )
		{
			this.rootNode = root;
			pointOnLine = null;
			mouseListener.modelChanged();
			invokeObservers( ob -> ob.modelChanged( root ) );
		}
		repaint();
	}

	protected EditMode getEditMode() {
		if ( editModeOverride != null ) {
			return editModeOverride;
		}
		return currentEditingMode;
	}
}