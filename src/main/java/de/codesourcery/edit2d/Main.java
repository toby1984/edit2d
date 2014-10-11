package de.codesourcery.edit2d;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JComboBox;
import javax.swing.JFrame;

import com.badlogic.gdx.math.Matrix3;

import de.codesourcery.edit2d.EditorPanel.EditMode;

public class Main extends JFrame {

	public static void main(String[] args) {
		new Main().run();
	}

	public Main() {
		super("test");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void run() {

		final EditorPanel p = new EditorPanel();

		p.getRoot().addChildren( new SimplePolygon(50,50,50,50 ) );

		p.getRoot().update( new Matrix3() );

		p.setPreferredSize( new Dimension(640,480 ) );
		p.setSize( new Dimension(640,480 ) );

		getContentPane().setLayout( new GridBagLayout() );

		// add edit mode combobox
		GridBagConstraints cnstrs = new GridBagConstraints();
		cnstrs.fill = GridBagConstraints.HORIZONTAL;
		cnstrs.gridheight = 1;
		cnstrs.gridwidth = 1;
		cnstrs.gridx = 0;
		cnstrs.gridy = 0;
		cnstrs.weightx = 1;
		cnstrs.weighty = 0.1;

		final JComboBox<EditorPanel.EditMode> editMode = new JComboBox<>( EditMode.values() );
		editMode.setSelectedItem( EditMode.MOVE );
		editMode.addActionListener( event -> p.setEditMode( (EditMode) editMode.getSelectedItem() ) );
		getContentPane().add( editMode , cnstrs );

		// add editor panel
		cnstrs = new GridBagConstraints();
		cnstrs.fill = GridBagConstraints.BOTH;
		cnstrs.gridheight = 1;
		cnstrs.gridwidth = 1;
		cnstrs.gridx = 0;
		cnstrs.gridy = 1;
		cnstrs.weightx = 1;
		cnstrs.weighty = 0.9;
		getContentPane().add( p ,cnstrs );

		pack();
		setVisible(true);
	}
}