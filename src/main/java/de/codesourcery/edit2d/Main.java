package de.codesourcery.edit2d;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;

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

	private RootNode createModel() {
		final RootNode root = new RootNode();
		root.addChildren( new SimplePolygon(100,100,100,100 ) );
		root.update( new Matrix3() );
		return root;
	}

	public void run() {

		final EditorPanel editor = new EditorPanel();
		editor.setModel( createModel() );

		editor.setPreferredSize( new Dimension(800,480 ) );
		editor.setSize( new Dimension(800,480 ) );

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

		final SceneGraphDebugPanel debugPanel = new SceneGraphDebugPanel( editor );
		editor.addSceneObserver( debugPanel );

		final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor , debugPanel );

		final JRadioButton button1 = new JRadioButton("Move" , true );
		button1.addActionListener( ev -> editor.setEditMode( EditMode.MOVE ) );

		final JRadioButton button2 = new JRadioButton("Create points" , true );
		button2.addActionListener( ev -> editor.setEditMode( EditMode.CREATE_POINTS) );

		final JRadioButton button3 = new JRadioButton("DRAW" , true );
		button3.addActionListener( ev -> editor.setEditMode( EditMode.DRAW ) );

		final ButtonGroup group = new ButtonGroup();
		group.add( button1 );
		group.add( button2 );
		group.add( button3 );

		final JPanel buttonPanel = new JPanel();

		buttonPanel.setLayout( new FlowLayout() );
		buttonPanel.add( button1 );
		buttonPanel.add( button2 );
		buttonPanel.add( button3 );

		final JButton resetButton = new JButton("Reset");
		resetButton.addActionListener( ev -> editor.setModel( createModel() ) );

		disableFocus(buttonPanel,button1,button2,button3,debugPanel,resetButton);
		buttonPanel.add( resetButton );

		getContentPane().add( buttonPanel , cnstrs );

		// add editor panel
		cnstrs = new GridBagConstraints();
		cnstrs.fill = GridBagConstraints.BOTH;
		cnstrs.gridheight = 1;
		cnstrs.gridwidth = 1;
		cnstrs.gridx = 0;
		cnstrs.gridy = 1;
		cnstrs.weightx = 1;
		cnstrs.weighty = 0.9;
		getContentPane().add( splitPane ,cnstrs );

		pack();
		setVisible(true);
	}

	private static void disableFocus(JComponent... c) {
		if ( c != null ) {
			Arrays.stream( c ).forEach( component -> component.setFocusable(false ) );
		}
	}
}