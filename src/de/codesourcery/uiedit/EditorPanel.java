package de.codesourcery.uiedit;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

public class EditorPanel extends JPanel {

	private final List<ITrackable> widgets = new ArrayList<>();

	private final MouseAdapter mouseListener = new MouseAdapter()
	{
		private final Point lastPos = new Point();
		private ITrackable dragged = null;

		@Override
		public void mousePressed(MouseEvent e)
		{
			if ( dragged == null && e.getButton() == MouseEvent.BUTTON1 )
			{
				ITrackable candidate = null;
				float minDistance = 0;
				for ( final ITrackable t : widgets )
				{
					final ITrackable part = t.getClosestTrackable( e.getPoint() ,  15 );
					if ( part != null )
					{
						final float d = part.minimumDistanceTo( e.getPoint() );
						if ( candidate == null || d < minDistance ) {
							candidate = part;
							minDistance = d;
						}
					}
				}
				if ( candidate != null && candidate.isEditable() )
				{
					lastPos.setLocation( e.getPoint() );
					dragged = candidate;
				} else {
					for ( final ITrackable t : widgets )
					{
						if ( t.contains( e.getPoint() ) && t.isEditable() )
						{
							dragged = t;
							lastPos.setLocation( e.getPoint() );
							break;
						}
					}
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			if ( dragged != null && e.getButton() == MouseEvent.BUTTON1 )
			{
				dragged = null;
			}
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if ( dragged != null )
			{
				final int dx = e.getX() - lastPos.x;
				final int dy = e.getY() - lastPos.y;

				dragged.translate(dx, dy );
				lastPos.setLocation( e.getPoint() );
				repaint();
			}
		}
	};

	public EditorPanel()
	{
		addMouseListener( mouseListener );
		addMouseMotionListener( mouseListener );
	}

	public void addWidget(ITrackable w) {
		this.widgets.add(w);
	}

	@Override
	public void paint(Graphics g) {

		final Graphics2D graphics = (Graphics2D) g;
		super.paint( g );

		graphics.setColor(Color.GREEN);
		for ( final ITrackable t : widgets ) {
			render( t , graphics );
		}
	}

	private void render(ITrackable t, Graphics2D graphics)
	{
		if ( t instanceof TrackablePoint )
		{
			final Point p = ((TrackablePoint) t).point;
			graphics.drawRect( p.x , p.y , 1 , 1 );

			t.getChildren().forEach( c -> render( c , graphics ) );
		}
		else if ( t instanceof TrackableLine )
		{
			final Point p0 = ((TrackableLine) t).p0;
			final Point p1 = ((TrackableLine) t).p1;
			graphics.drawLine( p0.x ,p0.y , p1.x , p1.y );

			t.getChildren().forEach( c -> render( c , graphics ) );
		}
		else if ( t instanceof TrackableRectangle )
		{
			final TrackableRectangle r = (TrackableRectangle) t;
			render( r.l0 , graphics );
			render( r.l1 , graphics );
			render( r.l2 , graphics );
			render( r.l3 , graphics );
			t.getChildren().forEach( c -> render( c , graphics ) );
		} else {
			System.err.println("Unhandled: "+t);
		}
	}
}
