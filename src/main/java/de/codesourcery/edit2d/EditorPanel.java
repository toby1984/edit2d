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

import de.codesourcery.edit2d.IGraphNode.Flag;

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

	protected Vector2 rotationCenter;
	protected final Point lastPos = new Point();
	protected IHandle dragged = null;

	protected final class MyMouseAdapter extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			if ( isEditMode( EditMode.MOVE ) || isEditMode( EditMode.ROTATE ) )
			{
				synchronized(modelLock)
				{
					if ( dragged == null && e.getButton() == MouseEvent.BUTTON1 )
					{
						IHandle candidate;
						if ( isEditMode( EditMode.MOVE ) ) {
							candidate = rootNode.getTranslationHandle( e.getX() , e.getY() , SELECTION_RADIUS );
						} else if ( isEditMode(EditMode.ROTATE ) ) {
							candidate = rootNode.getRotationHandle( e.getX() , e.getY() , SELECTION_RADIUS );
						} else {
							throw new RuntimeException("Unreachable code reached");
						}

						if ( candidate != null && candidate.getNode().hasFlag(Flag.SELECTABLE) )
						{
							if ( ! isEditMode( EditMode.ROTATE ) || (isEditMode( EditMode.ROTATE ) && candidate.getNode().hasFlag(Flag.CAN_ROTATE) ) )
							{
								if ( isEditMode( EditMode.ROTATE ) )
								{
									rotationCenter = new Vector2( candidate.getNode().getCenterInViewCoordinates() );
								}
								lastPos.setLocation( e.getPoint() );
								dragged = candidate;
							}
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
			if ( isEditMode( EditMode.MOVE ) || isEditMode( EditMode.ROTATE) )
			{
				boolean repaintNeeded = false;
				synchronized (modelLock)
				{
					if ( dragged != null && e.getButton() == MouseEvent.BUTTON1 )
					{
						dragged = null;
						rotationCenter = null;
						repaintNeeded = true;
					}
				}
				if ( repaintNeeded && isEditMode( EditMode.ROTATE) ) {
					repaint();
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
			synchronized( modelLock )
			{
				if ( isEditMode( EditMode.MOVE ) )
				{
					if ( dragged != null )
					{
						float dx = e.getX() - lastPos.x;
						float dy = e.getY() - lastPos.y;

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

						Vector2 oldV = new Vector2(lastPos.x,lastPos.y);
						Vector2 newV = new Vector2(e.getX(),e.getY());

						oldV = dragged.getNode().viewToModel( oldV );
						newV = dragged.getNode().viewToModel( newV );

						dx= newV.x - oldV.x;
						dy= newV.y - oldV.y;

						((ITranslationHandle) dragged).translate( dx, dy );
					}

					lastPos.setLocation( e.getPoint() );
					if ( dragged != null ) {
						subtreeValuesChanged(dragged.getNode());
					}
					repaint();
				}
				else if ( isEditMode( EditMode.ROTATE ) )
				{
					if ( dragged != null )
					{
						final float lastAngle = angleInDeg( rotationCenter , new Vector2(lastPos.x,lastPos.y) );
						final float newAngle = angleInDeg( rotationCenter , new Vector2( e.getX() , e.getY() ) );
						final float delta = lastAngle - newAngle;
						((IRotationHandle) dragged).rotate( delta );
						System.out.println("Angle: "+newAngle);
						lastPos.setLocation( e.getPoint() );
						repaint();
					} else {
						lastPos.setLocation( e.getPoint() );
					}
				}
			}
		}
	};

	protected static float angleInDeg(Vector2 origin,Vector2 point) {
		final float deg = radToDeg( Math.atan2( point.y - origin.y , point.x - origin.x ) );
		return deg < 0  ? -deg : 360f - deg;
	}

	protected static float radToDeg(double rad) {
		return (float) (rad * (180d/Math.PI));
	}

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
		dragged = null;
		rotationCenter = null;
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
				g.drawLine( 0 , round( pointOnLine.y ) , getWidth() , round( pointOnLine.y ) );
				g.drawLine( round( pointOnLine.x ) , 0 , round( pointOnLine.x ), getHeight() );
			}

			graphics.setColor(Color.GREEN);

			rootNode.update();

			final List<IGraphNode> highlighted = new ArrayList<>();

			rootNode.visitPreOrder( node ->
			{
				if ( node.hasFlag(Flag.HIGHLIGHTED) ) {
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

			if ( isEditMode( EditMode.ROTATE) && dragged != null ) {

				graphics.setColor(Color.RED);
				graphics.drawLine( round( rotationCenter.x ) , round( rotationCenter.y ) , lastPos.x , lastPos.y );
			}
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
			graphics.drawLine( round( p0.x ) , round( p0.y ) , round( p1.x ) , round( p1.y ) );
		}
		else if ( t instanceof PointNode  )
		{
			final Vector2 p = ((PointNode) t).getCenterInViewCoordinates();
			if ( t.hasFlag(Flag.HIGHLIGHTED) ) {
				graphics.setColor( HIGHLIGHT_COLOR );
			} else {
				graphics.setColor( EditorPanel.POINT_COLOR );
			}
			graphics.drawOval( round(p.x - 3) , round(p.y - 3 ) , 7 , 7 );
		}
	}

	protected static int round(double value) {
		return (int) Math.round(value);
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

	protected boolean isEditMode(EditMode mode) {
		return mode.equals( getEditMode() );
	}

	protected EditMode getEditMode() {
		if ( editModeOverride != null ) {
			return editModeOverride;
		}
		return currentEditingMode;
	}
}