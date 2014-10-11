package de.codesourcery.edit2d;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;

import com.badlogic.gdx.math.Vector2;

public class EditorPanel extends JPanel {

	private final RootNode root = new RootNode();

	protected static final Color REGULAR_COLOR = Color.GREEN;
	protected static final Color HIGHLIGHT_COLOR = Color.BLUE;
	protected static final Color POINT_COLOR = Color.RED;

	protected static final int SELECTION_RADIUS = 10;

	public static enum EditMode {
		MOVE,DRAW,CREATE_POINTS;
	}

	private EditMode editMode = EditMode.MOVE;

	protected Vector2 pointOnLine = null;

	private final MouseAdapter mouseListener = new MouseAdapter()
	{
		private final Point lastPos = new Point();
		private IGraphNode dragged = null;
		private final HighlightManager highlighted = new HighlightManager();

		@Override
		public void mousePressed(MouseEvent e)
		{
			if ( editMode == EditMode.MOVE )
			{
				if ( dragged == null && e.getButton() == MouseEvent.BUTTON1 )
				{
					synchronized(root)
					{
						IGraphNode candidate = NodeUtils.findClosestNode( root,e.getX(), e.getY() , SELECTION_RADIUS );
						if ( candidate == null || ! candidate.getMetaData().isSelectable() )
						{
							candidate = NodeUtils.findContainingNode( root , e.getX() ,  e.getY() );
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

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if ( editMode == EditMode.CREATE_POINTS && e.getButton() == MouseEvent.BUTTON1 )
			{
				final IGraphNode candidate = NodeUtils.findClosestNode( root,e.getX(), e.getY() , SELECTION_RADIUS );
				if ( candidate instanceof LineNode)
				{
					if ( ((LineNode) candidate).split( e.getX() , e.getY() ) ) {
						repaint();
					}
				}
			}
		};

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if ( editMode == EditMode.MOVE )
			{
				if ( dragged != null && e.getButton() == MouseEvent.BUTTON1 )
				{
					dragged = null;
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			IGraphNode candidate = NodeUtils.findClosestNode( root,e.getX(), e.getY() , 15 );
			if ( candidate == null ) {
				candidate = NodeUtils.findContainingNode( root , e.getX() , e.getY() );
			}

			if ( candidate != null )
			{
				if ( highlighted.setHighlight( candidate ) ) {
					repaint();
				}
			}
			else if ( highlighted.clearHighlights() ) {
				repaint();
			}

			if ( editMode == EditMode.CREATE_POINTS && candidate instanceof LineNode ) {
				pointOnLine = ((LineNode) candidate).getClosestPointOnLine( e.getX() ,  e.getY() );
				repaint();
			} else {
				pointOnLine = null;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if ( editMode == EditMode.MOVE )
			{
				if ( dragged != null )
				{
					final int dx = e.getX() - lastPos.x;
					final int dy = e.getY() - lastPos.y;

					synchronized( root )
					{
						EventType t;
						if ( dragged instanceof PointNode) {
							t = EventType.TRANSLATE_POINT;
						} else if ( dragged instanceof LineNode) {
							t = EventType.TRANSLATE_LINE;
						} else if ( dragged instanceof SimplePolygon) {
							t = EventType.TRANSLATE_POLY;
						} else {
							throw new RuntimeException("Unhandled graph node: "+dragged);
						}

						if ( dragged instanceof LineNode )
						{
							final LineNode l = (LineNode) dragged;
							if ( l.isHorizontalLine() ) {
								dragged.translate( t , 0 , dy );
							} else if ( l.isVerticalLine() ) {
								dragged.translate( t , dx , 0 );
							} else {
								dragged.translate( t , dx , dy );
							}
						}
						else
						{
							dragged.translate( t , dx, dy );
						}
					}
					lastPos.setLocation( e.getPoint() );
					repaint();
				}
			}
		}
	};

	public EditorPanel()
	{
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
	}

	public IGraphNode getRoot() {
		return root;
	}

	public void setEditMode(EditMode editMode)
	{
		if (editMode == null) {
			throw new IllegalArgumentException("editMode must not be NULL");
		}
		this.editMode = editMode;
		repaint();
	}

	@Override
	public void paint(Graphics g) {

		final Graphics2D graphics = (Graphics2D) g;
		super.paint( g );

		if ( pointOnLine != null ) {
			g.setColor(Color.RED);
			g.drawLine( 0 , (int) pointOnLine.y , getWidth() , (int) pointOnLine.y );
			g.drawLine( (int) pointOnLine.x , 0 , (int) pointOnLine.x, getHeight() );
		}

		graphics.setColor(Color.GREEN);

		synchronized(root)
		{
			root.processNodeUpdates();

			root.update();

			root.visitPreOrder( v ->
			{
				render( v , graphics );
			});
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
			if ( t.getMetaData().isHighlighted() || editMode == EditMode.CREATE_POINTS )
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
	}
}