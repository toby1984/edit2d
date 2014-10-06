package de.codesourcery.uiedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;

import de.codesourcery.uiedit.Port.PortLocation;
import de.codesourcery.uiedit.Port.PortType;

public class Main extends JFrame {

	public static void main(String[] args) {
		new Main().run();
	}

	public Main() {
		super("test");
	}

	public void run() {

		final EditorPanel p = new EditorPanel();

		final Point p0 = new Point(50,50);
		final Point p1 = new Point(100,50 );
		final Point p2 = new Point(100,100);
		final Point p3 = new Point(50,100 );

		final TrackablePoint point0 = new TrackablePoint(p0);
		final TrackablePoint point1 = new TrackablePoint(p1);
		final TrackablePoint point2 = new TrackablePoint(p2);
		final TrackablePoint point3 = new TrackablePoint(p3);

		final TrackableRectangle rect = new TrackableRectangle( point0 ,point1, point2 , point3 );

		final Port port = Port.addPort( rect , PortLocation.N , PortType.IN );

		p.addWidget( rect );

		p.setPreferredSize( new Dimension(640,480 ) );
		p.setSize( new Dimension(640,480 ) );

		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( p ,BorderLayout.CENTER );

		pack();
		setVisible(true);
	}
}
