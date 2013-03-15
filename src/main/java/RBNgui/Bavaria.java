/*
 * Bavaria.java
 * 
 * Copyright (C) 2005 Aalborg University
 *
 * contact:
 * jaeger@cs.aau.dk   http://www.cs.aau.dk/~jaeger/Primula.html
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package RBNgui;

import java.awt.*;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.util.Vector;
import java.io.*;

import RBNpackage.*;
import RBNio.*;
import RBNLearning.*;


public class Bavaria extends JFrame implements ActionListener{


	private JMenuBar mb            = new JMenuBar();
	private JMenu file_menu        = new JMenu("File");
	private JMenuItem empty        = new JMenuItem("New");
	private JMenuItem load         = new JMenuItem("Load");
	private JMenuItem save         = new JMenuItem("Save");
	private JMenuItem saveas       = new JMenuItem("Save As");
	private JMenuItem exit         = new JMenuItem("Exit");

	private JMenu graphics_menu          = new JMenu("Graphics");
	private JMenu x_dimension            = new JMenu("Set x dimension");
	private ButtonGroup x_group          = new ButtonGroup();
	private JRadioButtonMenuItem x3000   = new JRadioButtonMenuItem("3000");
	private JRadioButtonMenuItem x5000   = new JRadioButtonMenuItem("5000");
	private JRadioButtonMenuItem x10000  = new JRadioButtonMenuItem("10000");
	private JRadioButtonMenuItem x20000  = new JRadioButtonMenuItem("20000");
	private JRadioButtonMenuItem x30000  = new JRadioButtonMenuItem("30000");
	private JMenu y_dimension            = new JMenu("Set y dimension");
	private ButtonGroup y_group          = new ButtonGroup();
	private JRadioButtonMenuItem y3000   = new JRadioButtonMenuItem("3000");
	private JRadioButtonMenuItem y5000   = new JRadioButtonMenuItem("5000");
	private JRadioButtonMenuItem y10000  = new JRadioButtonMenuItem("10000");
	private JRadioButtonMenuItem y20000  = new JRadioButtonMenuItem("20000");
	private JRadioButtonMenuItem y30000  = new JRadioButtonMenuItem("30000");

	private JMenu grid_size              = new JMenu("Set grid size");
	private ButtonGroup grid_group       = new ButtonGroup();
	private JRadioButtonMenuItem none    = new JRadioButtonMenuItem("none");
	private JRadioButtonMenuItem x15     = new JRadioButtonMenuItem("15 x 15");
	private JRadioButtonMenuItem x30     = new JRadioButtonMenuItem("30 x 30");
	private JRadioButtonMenuItem x60     = new JRadioButtonMenuItem("60 x 60");
	private JRadioButtonMenuItem x120    = new JRadioButtonMenuItem("120 x 120");
	private JMenuItem snap_to_grid       = new JMenuItem("Snap to grid");
	private JMenuItem center_graph       = new JMenuItem("Center graph");
	private JCheckBoxMenuItem showCoords = new JCheckBoxMenuItem("Show coordinates");

	private JMenu zoom              		= new JMenu("Zoom");
	private ButtonGroup zoom_group      = new ButtonGroup();
	private JRadioButtonMenuItem z1		  = new JRadioButtonMenuItem("100%");
	private JRadioButtonMenuItem z2     = new JRadioButtonMenuItem("75%");
	private JRadioButtonMenuItem z4     = new JRadioButtonMenuItem("50%");
	private JRadioButtonMenuItem z8     = new JRadioButtonMenuItem("25%");

	private JButton addNode        = new JButton("Add Node");
	private JButton moveNode       = new JButton("Move Node");
	private JButton deleteNode     = new JButton("Delete Node");
	private JButton addTuple       = new JButton("Add Tuple");
	private JButton deleteRelation = new JButton("Delete Relation");

	private JLabel infoText        = new JLabel(" ");
	private JLabel coordinates     = new JLabel(" ");
	private JButton reset          = new JButton("Reset");

	private JScrollPane scrollPane = new JScrollPane();
	private JPanel buttons         = new JPanel(new GridLayout(1, 5));
	private JPanel arityPanel      = new JPanel(new GridLayout(3, 1));
	private JPanel coordsAndReset  = new JPanel(new BorderLayout(2, 0));
	private JPanel bottomPanel     = new JPanel(new BorderLayout());

	private final int ADDNODE        = 1;
	private final int MOVENODE       = 5;
	private final int DELETENODE     = 2;
	private final int ADDTUPLE       = 3;
	private final int DELETERELATION = 4;

	private int SCROLLPANE_WIDTH  = 458; //default width for centerView()
	private int SCROLLPANE_HEIGHT = 425;
	//private final int SCROLLPANE_WIDTH  = 658; //default width for centerView()
	//private final int SCROLLPANE_HEIGHT = 625;

	private final static String TITLE = "Bavaria";
	private final String STRUCTURE_MODIFIED = "Current structure modified. Continue?";
	private final String INST_AND_QUERIES_LOST = "This action will cause current instantiations and queries to be lost. Continue?";

	private int xmax = 3000;
	private int ymax = 3000;

	private JFileChooser fileChooser = new JFileChooser( "." );
	private Filter_rst rstFilter = new Filter_rst();
	private Filter_rdef rdefFilter = new Filter_rdef();
	
	private boolean isEdited         = false;
	private boolean isSaved          = false;  //save file exists
	private File file;

	private EditPanel editPanel;
	private AttributesPanel attributesPanel;
	private BinaryPanel binaryPanel;
	private ArbitraryPanel arbitraryPanel;

	private SparseRelStruc struc;
	private Primula mainWindow;

	private Color background;

	public Bavaria(SparseRelStruc srs, final Primula mw, boolean edited){

		struc = srs;
		mainWindow = mw;
		isEdited = edited;

		editPanel       = new EditPanel(this);
		attributesPanel = new AttributesPanel(this);
		binaryPanel     = new BinaryPanel(this);
		arbitraryPanel  = new ArbitraryPanel(this);

		editPanel.setBackground(Color.white);
		scrollPane.getViewport().add(editPanel);

		arityPanel.add(attributesPanel);
		arityPanel.add(binaryPanel);
		arityPanel.add(arbitraryPanel);
		arityPanel.setPreferredSize(new Dimension(160, 579));


		//Creates the File-menu
		file_menu.add(empty);
		file_menu.add(load);
		file_menu.add(save);
		file_menu.add(saveas);
//		file_menu.add(savecolor);
		file_menu.addSeparator();
		file_menu.add(exit);
		mb.add(file_menu);
		file_menu.setMnemonic(KeyEvent.VK_F);


		//Creates the Graphics-menu
		x_group.add(x3000);
		x3000.setSelected(true);
		x_group.add(x5000);
		x_group.add(x10000);
		x_group.add(x20000);
		x_group.add(x30000);
		x_dimension.add(x3000);
		x_dimension.add(x5000);
		x_dimension.add(x10000);
		x_dimension.add(x20000);
		x_dimension.add(x30000);
		graphics_menu.add(x_dimension);

		y_group.add(y3000);
		y3000.setSelected(true);
		y_group.add(y5000);
		y_group.add(y10000);
		y_group.add(y20000);
		y_group.add(y30000);
		y_dimension.add(y3000);
		y_dimension.add(y5000);
		y_dimension.add(y10000);
		y_dimension.add(y20000);
		y_dimension.add(y30000);
		graphics_menu.add(y_dimension);


		zoom_group.add( z1 );
		z1.setSelected(true);
		zoom_group.add( z2 );
		zoom_group.add( z4 );
		zoom_group.add( z8 );
		zoom.add( z1 );
		zoom.add( z2 );
		zoom.add( z4 );
		zoom.add( z8 );
		graphics_menu.add( zoom );

		grid_group.add(none);
		none.setSelected(true);
		grid_group.add(x15);
		grid_group.add(x30);
		grid_group.add(x60);
		grid_group.add(x120);
		grid_size.add(none);
		grid_size.add(x15);
		grid_size.add(x30);
		grid_size.add(x60);
		grid_size.add(x120);
		graphics_menu.add(grid_size);
		graphics_menu.add(snap_to_grid);
		graphics_menu.add(center_graph);
		graphics_menu.add(showCoords);
		showCoords.setSelected(true);
		mb.add(graphics_menu);
		setJMenuBar(mb);

		
		fileChooser.addChoosableFileFilter(rstFilter);
		fileChooser.addChoosableFileFilter(rdefFilter);



		//the new menu-command (new is a reserved word in Java so new = empty)
		empty.setMnemonic(KeyEvent.VK_N);
		empty.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		empty.addActionListener( this );

		exit.setMnemonic(KeyEvent.VK_X);
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
		exit.addActionListener( this );


		load.setMnemonic(KeyEvent.VK_L);
		load.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		load.addActionListener( this );


		save.setMnemonic(KeyEvent.VK_S);
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		save.addActionListener( this );


		saveas.setMnemonic(KeyEvent.VK_A);
		saveas.addActionListener( this );

		//set x-dimension---------------------------------------------------------------------

		x3000.addActionListener( this );

		x5000.addActionListener( this );

		x10000.addActionListener( this );

		x20000.addActionListener( this );

		x30000.addActionListener( this );

		//set y-dimension---------------------------------------------------------------------

		y3000.addActionListener( this );
		y5000.addActionListener( this );      

		y10000.addActionListener( this );

		y20000.addActionListener( this );

		y30000.addActionListener( this );

		//set grid size------------------------------------------------------------------

		none.addActionListener( this );

		x15.addActionListener( this );

		x30.addActionListener( this );

		x60.addActionListener( this );

		x120.addActionListener( this );

		// set zoom---------------------------------------------------------------------

		z1.addActionListener( this );
		z2.addActionListener( this );
		z4.addActionListener( this );
		z8.addActionListener( this );

		//-------------------------------------------------------------------------------

		snap_to_grid.addActionListener( this );

		center_graph.addActionListener( this );

		showCoords.addActionListener( this );

		//set background color
		addNode.setBackground(Primula.COLOR_YELLOW_SELECTED);
		moveNode.setBackground(Primula.COLOR_YELLOW);
		deleteNode.setBackground(Primula.COLOR_YELLOW);
		addTuple.setBackground(Primula.COLOR_GREEN);
		deleteRelation.setBackground(Primula.COLOR_RED);
		//Creates the action-bar
		buttons.add(addNode);
		buttons.add(moveNode);
		buttons.add(deleteNode);
		buttons.add(addTuple);
		buttons.add(deleteRelation);

//		background = moveNode.getBackground();
		//default
//		addNode.setBackground(new Color(164, 164, 164));
		editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		attributesPanel.setMode(ADDNODE);
		binaryPanel.setMode(ADDNODE);
		editPanel.setMode(ADDNODE);
		arbitraryPanel.setMode(ADDNODE);

		addNode.addActionListener( this );

		moveNode.addActionListener( this );

		deleteNode.addActionListener( this );
		addTuple.addActionListener( this );

		deleteRelation.addActionListener( this );
		reset.addActionListener( this );



		//Creates the coordinates and reset button panel
		coordsAndReset.add(coordinates, BorderLayout.WEST);
		coordsAndReset.add(reset, BorderLayout.EAST);

		//Creates the information text and coordinates+reset button panel
		infoText.setForeground(Color.black);
		bottomPanel.add(infoText, BorderLayout.CENTER);
		bottomPanel.add(coordsAndReset, BorderLayout.EAST);

		//Creates the layout
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout(5, 2));
		contentPane.add(buttons, BorderLayout.NORTH);
		contentPane.add(arityPanel, BorderLayout.WEST);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);


		//Inner class for closing the window
		this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						//System.exit(0);
						Rel.resetTheColorCounters();
						mainWindow.setIsBavariaOpen(false);
						editPanel.closeAllNodeWindows();
						dispose();
					}
				}
		);

		//rst-file already loaded or created
		setCanvasSize();
		centerView();

		ImageIcon icon = new ImageIcon("small_logo.jpg");
		if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) //image ok
			this.setIconImage(icon.getImage());
		this.setTitle(TITLE);
		//if you change window size you'll have to change SCROLLPANE_WIDTH/HEIGHT too
		//this.setSize(850, 750);
		this.setSize(650, 550);
		this.show();

	}

	public void actionPerformed( ActionEvent e ) {
		Object source = e.getSource();
		if( source == addNode ){
			editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			editPanel.setMode(ADDNODE);
			editPanel.setRel(new Rel());
			attributesPanel.setMode(ADDNODE);
			binaryPanel.setMode(ADDNODE);
			arbitraryPanel.setMode(ADDNODE);
			clearSelections(0);
			addNode.setBackground(Primula.COLOR_YELLOW_SELECTED);
			moveNode.setBackground(Primula.COLOR_YELLOW);
			deleteNode.setBackground(Primula.COLOR_YELLOW);
			addTuple.setBackground(Primula.COLOR_GREEN);
			deleteRelation.setBackground(Primula.COLOR_RED);
		}
		else if( source == moveNode ){
			editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
			editPanel.setMode(MOVENODE);
			editPanel.setRel(new Rel());
			attributesPanel.setMode(MOVENODE);
			binaryPanel.setMode(MOVENODE);
			arbitraryPanel.setMode(MOVENODE);
			clearSelections(0);
			moveNode.setBackground(Primula.COLOR_YELLOW_SELECTED);
			addNode.setBackground(Primula.COLOR_YELLOW);
			deleteNode.setBackground(Primula.COLOR_YELLOW);
			addTuple.setBackground(Primula.COLOR_GREEN);
			deleteRelation.setBackground(Primula.COLOR_RED);
		}
		else if( source == deleteNode ){
			editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			editPanel.setMode(DELETENODE);
			editPanel.setRel(new Rel());
			attributesPanel.setMode(DELETENODE);
			binaryPanel.setMode(DELETENODE);
			arbitraryPanel.setMode(DELETENODE);
			clearSelections(0);
			deleteNode.setBackground(Primula.COLOR_YELLOW_SELECTED);
			addNode.setBackground(Primula.COLOR_YELLOW);
			moveNode.setBackground(Primula.COLOR_YELLOW);
			addTuple.setBackground(Primula.COLOR_GREEN);
			deleteRelation.setBackground(Primula.COLOR_RED);
		}
		else if( source == addTuple ){
			editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			editPanel.setMode(ADDTUPLE);
			attributesPanel.setMode(ADDTUPLE);
			binaryPanel.setMode(ADDTUPLE);
			arbitraryPanel.setMode(ADDTUPLE);
			addTuple.setBackground(Primula.COLOR_GREEN_SELECTED);
			addNode.setBackground(Primula.COLOR_YELLOW);
			moveNode.setBackground(Primula.COLOR_YELLOW);
			deleteNode.setBackground(Primula.COLOR_YELLOW);
			deleteRelation.setBackground(Primula.COLOR_RED);
		}
		else if( source == deleteRelation ){
			editPanel.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			editPanel.setMode(DELETERELATION);
			editPanel.setRel(new Rel());
			attributesPanel.setMode(DELETERELATION);
			binaryPanel.setMode(DELETERELATION);
			arbitraryPanel.setMode(DELETERELATION);
			clearSelections(0);
			deleteRelation.setBackground(Primula.COLOR_RED_SELECTED);
			addNode.setBackground(Primula.COLOR_YELLOW);
			moveNode.setBackground(Primula.COLOR_YELLOW);
			deleteNode.setBackground(Primula.COLOR_YELLOW);
			addTuple.setBackground(Primula.COLOR_GREEN);
		}
		else if( source == reset ){
			if (editPanel.reset() == false)
				infoText.setText("reset");
		}
		else if( source == empty ){
			if(mainWindow.isInstEmpty() && mainWindow.isQueryatomsEmpty()){
				if(isEdited){
					if(confirm(STRUCTURE_MODIFIED, JOptionPane.YES_NO_OPTION)){
						setIsEdited(false);
						emptyFunction();
					}
				}
				else
					emptyFunction();
			}
			else{
				if(confirm(INST_AND_QUERIES_LOST, JOptionPane.YES_NO_OPTION)){
					if(isEdited){
						if(confirm(STRUCTURE_MODIFIED, JOptionPane.YES_NO_OPTION)) {
							setIsEdited(false);
							emptyFunction();
						}
					}
					else
						emptyFunction();
				}
			}
		}
		else if( source == exit ){
			//System.exit(0);
			Rel.resetTheColorCounters();
			mainWindow.setIsBavariaOpen(false);
			editPanel.closeAllNodeWindows();
			dispose();
		}
		else if( source == load ){
			int value = fileChooser.showDialog(Bavaria.this, "Load");
			if (value == JFileChooser.APPROVE_OPTION) {
				if(mainWindow.isInstEmpty() && mainWindow.isQueryatomsEmpty()){
					if(isEdited){
						if(confirm(STRUCTURE_MODIFIED, JOptionPane.YES_NO_OPTION)){
							setIsEdited(false);
							loadFunction();
							editPanel.setZoom(1);
						}
					}
					else{
						loadFunction();
						editPanel.setZoom(1);
					}
				}
				else{
					if(confirm(INST_AND_QUERIES_LOST, JOptionPane.YES_NO_OPTION)){
						if(isEdited){
							if(confirm(STRUCTURE_MODIFIED, JOptionPane.YES_NO_OPTION)) {
								setIsEdited(false);
								loadFunction();
								editPanel.setZoom(1);
							}
						}
						else{
							loadFunction();
							editPanel.setZoom(1);
						}
					}
				}
			}
		}
		else if( source == save ){
			if ( !isSaved ) {
				int value = fileChooser.showDialog(Bavaria.this, "Save");
				if(value == JFileChooser.APPROVE_OPTION) {
					//user has typed the filename into the UI or selected the file from a list in the UI
					file = fileChooser.getSelectedFile();
					saveFile(file);
					mainWindow.showMessage("SparseRelStruc "+file.getName()+" saved");
				}
			}
			else{
				saveFile(file);
				showMessage("Saved "+file.getName());
				mainWindow.showMessage("SparseRelStruc "+file.getName()+" saved");
			}
		}
		else if( source == saveas ){
			int value = fileChooser.showDialog(Bavaria.this, "Save");
			if(value == JFileChooser.APPROVE_OPTION) {
				//user has typed the filename into the UI or selected the file from a list in the UI
				file = fileChooser.getSelectedFile();
				saveFile(file);
				mainWindow.setInputFile(file);
				mainWindow.showMessage("SparseRelStruc "+file.getName()+" saved");
			}
		}

		//set x-dimension---------------------------------------------------------------------
		else if( source == x3000 ) {
			setXSize((int)(3000*editPanel.getZoom()));
		}
		else if( source == x5000 ){
			setXSize((int)(5000*editPanel.getZoom()));
		}
		else if( source == x10000 ) {
			setXSize((int)(10000*editPanel.getZoom()));
		}
		else if( source == x20000 ){
			setXSize((int)(20000*editPanel.getZoom()));        
		}
		else if( source == x30000 ){	  
			setXSize((int)(30000*editPanel.getZoom()));
		}

		//set y-dimension---------------------------------------------------------------------
		else if( source == y3000 ){
			setYSize((int)(3000*editPanel.getZoom()));
		}
		else if( source == y5000 ){
			setYSize((int)(5000*editPanel.getZoom()));
		}
		else if( source == y10000 ){
			setYSize((int)(10000*editPanel.getZoom()));
		}
		else if( source == y20000 ){
			setYSize((int)(20000*editPanel.getZoom()));
		}
		else if( source == y30000 ){
			setYSize((int)(30000*editPanel.getZoom()));
		}
		//set grid size------------------------------------------------------------------
		else if( source == none ){
			editPanel.setGrid(false, 0);
			editPanel.repaint();
		}
		else if( source == x15 ){
			editPanel.setGrid(true, 15);
			editPanel.repaint();
		}
		else if( source == x30 ){
			editPanel.setGrid(true, 30);
			editPanel.repaint();
		}
		else if( source == x60 ){
			editPanel.setGrid(true, 60);
			editPanel.repaint();
		}
		else if( source == x120 ){
			editPanel.setGrid(true, 120);
			editPanel.repaint();
		}

		//-------------------------------------------------------------------------------
		else if( source == snap_to_grid ){
			editPanel.snapToGrid();
			editPanel.repaint();
		}
		else if( source == center_graph ){
			boolean getNewValues = false;
			boolean needScaling = false;
			int[] maxCoords = getMaximumXandY();
			int[] minCoords = getMinimumXandY();

			//graph is larger than the canvas
			if(maxCoords[0] >= xmax){
				needScaling = true;
				if(confirm("Bavaria needs to rescale the graph", JOptionPane.OK_CANCEL_OPTION)){
					scaleX(maxCoords[0]);
					if(maxCoords[1] >= ymax)
						scaleY(maxCoords[1]);
					getNewValues = true;
					needScaling = false;
				}
			}
			else if(maxCoords[1] >= ymax){
				needScaling = true;
				if(confirm("Bavaria needs to rescale the graph", JOptionPane.OK_CANCEL_OPTION)){
					scaleY(maxCoords[1]);
					getNewValues = true;
					needScaling = false;
				}
			}

			//if graph is scaled then we need new maximum and minimum values
			if(getNewValues){
				maxCoords = getMaximumXandY();
				minCoords = getMinimumXandY();
			}

			//count the center of the graph and move it to the center of the canvas
			if(!needScaling){ //user canceled rescaling
				double xGraphCenter = (minCoords[0] + maxCoords[0]) / 2.0;
				double yGraphCenter = (minCoords[1] + maxCoords[1]) / 2.0;
				if(xGraphCenter >= 0 && yGraphCenter >= 0){ //there is at least one node
					centerGraph(xGraphCenter, yGraphCenter);
					editPanel.repaint();
				}
			}
		}
		else if( source == showCoords ){
			if(showCoords.isSelected() == false)
				coordinates.setText(" ");
		}
		else if( source == z1 ){
			editPanel.setZoom(1);
			centerView();
		}
		else if( source == z2 ){
			editPanel.setZoom(0.75);
			centerView();
		}
		else if( source == z4 ){
			editPanel.setZoom(0.5);
			centerView();

		}
		else if( source == z8 ){
			editPanel.setZoom(0.25);
			centerView();
		}
	}


	public Bavaria(SparseRelStruc srs, File srsfile, final Primula mw, boolean edited){

		this(srs, mw, edited);

		if(srsfile != null){
			file = srsfile;
			setFrameTitle(srsfile.getName());
			isSaved = true;
		}

		attributesPanel.getAttributesNames(struc.getAttributes());
		binaryPanel.getBinaryrelsNames(struc.getBinaryRelations());
		arbitraryPanel.getArbitraryNames(struc.getArbitraryRelations());
		editPanel.setRel(new Rel());
	}


	//creates a new empty SparseRelStruc when the user chooses new command from the file menu.
	public void emptyFunction(){
		editPanel.closeAllNodeWindows();
		struc = new SparseRelStruc();
		mainWindow.setRelStruc(struc);
		mainWindow.setInputFile(null);
		setFrameTitle(TITLE);
		isSaved = false;
		editPanel.repaint();
		emptyRelationLists();
		Rel.resetTheColorCounters();
	}


	//loads a new SparseRelStruc when the user chooses load command from the file menu.
	public void loadFunction(){
		

		try {
			file = fileChooser.getSelectedFile();
			Rel.resetTheColorCounters();

			if (rstFilter.accept(file)){
				SparseRelStrucReader srelreader = new SparseRelStrucReader();
				struc = srelreader.readSparseRelStrucFromFile(file.getPath());
			}
			if (rdefFilter.accept(file)){
				RDEFReader rdefreader = new RDEFReader();
				RelData rdata = rdefreader.readRDEF(file.getPath(),null);
				struc = new SparseRelStruc(rdata.caseAt(0).inputDomain());
			}
				

			if(struc.getCoords().size() == 0){
				struc.createCoords();
			}

			mainWindow.setRelStruc(struc);
			mainWindow.setInputFile(file);
			mainWindow.showMessage("SparseRelStruc "+file.getName()+" loaded");
			emptyRelationLists();
			editPanel.closeAllNodeWindows();
			attributesPanel.getAttributesNames(struc.getAttributes());
			binaryPanel.getBinaryrelsNames(struc.getBinaryRelations());
			arbitraryPanel.getArbitraryNames(struc.getArbitraryRelations());
			setCanvasSize();
			centerView();
			setFrameTitle(file.getName());
			editPanel.setRel(new Rel());
			editPanel.repaint();
			isSaved = true;
			showMessage("");
		} catch(Exception ex){
			mainWindow.showMessage("Error in file loading");
			ex.printStackTrace();
		}
	}


	//sets the size of the canvas
	public void setCanvasSize(){
		boolean confirmed = false;
		int[] maxsize = getMaximumXandY();
		int x = maxsize[0];
		int y = maxsize[1];

		if(x < 3000){ //default size
//			setXSize(3000);
			setXSize((int)(3000*editPanel.getZoom()));
			x3000.setSelected(true);
		}
		else if(x >= (int)(3000*editPanel.getZoom()) && x < (int)(5000*editPanel.getZoom())){
			setXSize((int)(5000*editPanel.getZoom()));
			x5000.setSelected(true);
		}
		else if(x >= (int)(5000*editPanel.getZoom()) && x < (int)(10000*editPanel.getZoom())){
			setXSize((int)(10000*editPanel.getZoom()));
			x10000.setSelected(true);
		}
		else if(x >= (int)(10000*editPanel.getZoom()) && x < (int)(20000*editPanel.getZoom())){
			setXSize((int)(20000*editPanel.getZoom()));
			x20000.setSelected(true);
		}
		else if(x >= (int)(20000*editPanel.getZoom()) && x < (int)(30000*editPanel.getZoom())){
			setXSize((int)(30000*editPanel.getZoom()));
			x30000.setSelected(true);
		}

		if(y < 3000){ //default size
			setYSize((int)(3000*editPanel.getZoom()));
			y3000.setSelected(true);
		}
		else if(y >= (int)(3000*editPanel.getZoom()) && y < (int)(5000*editPanel.getZoom())){
			setYSize((int)(5000*editPanel.getZoom()));
			y5000.setSelected(true);
		}
		else if(y >= (int)(5000*editPanel.getZoom()) && y < (int)(10000*editPanel.getZoom())){
			setYSize((int)(10000*editPanel.getZoom()));
			y10000.setSelected(true);
		}
		else if(y >= (int)(10000*editPanel.getZoom()) && y < (int)(20000*editPanel.getZoom())){
			setYSize((int)(20000*editPanel.getZoom()));
			y20000.setSelected(true);
		}
		else if(y >= 20000 && y < (int)(30000*editPanel.getZoom())){
			setYSize((int)(30000*editPanel.getZoom()));
			y30000.setSelected(true);
		}

		if(x >= (int)(30000*editPanel.getZoom())){
			setXSize((int)(30000*editPanel.getZoom()));
			x30000.setSelected(true);
			if(y >= (int)(30000*editPanel.getZoom())){
				setYSize((int)(30000*editPanel.getZoom()));
				y30000.setSelected(true);
			}
			if(confirm("Bavaria needs to rescale the graph", JOptionPane.OK_CANCEL_OPTION)){
				scaleX(x);
				if(y >= (int)(30000*editPanel.getZoom()))
					scaleY(y);
			}
		}
		else if(y >= (int)(30000*editPanel.getZoom())){
			setYSize((int)(30000*editPanel.getZoom()));
			y30000.setSelected(true);
			if(confirm("Bavaria needs to rescale the graph", JOptionPane.OK_CANCEL_OPTION))
				scaleY(y);
		}
	}


	//sets the x size of the canvas
	public void setXSize(int size){
		xmax = size;
		editPanel.setXSize(xmax);
		scrollPane.getViewport().setViewSize(new Dimension((int)(size*editPanel.getZoom()), ymax));
	}


	//sets the y size of the canvas
	public void setYSize(int size){
		ymax = size;
		editPanel.setYSize(ymax);
		scrollPane.getViewport().setViewSize(new Dimension((int)(size*editPanel.getZoom()), ymax));
	}


	//returns the maximum x and maximum y coordinate from the coordinates vector
	//returns [-1, -1] if coordinates size is 0
	public int[] getMaximumXandY(){
		Vector coordinates = struc.getCoords();
		int maxX = -1;
		int maxY = -1;

		for(int i=0; i<coordinates.size(); ++i){
			int[] coords = (int[])coordinates.elementAt(i);
			int x = coords[0];
			int y = coords[1];
			if(x > maxX)
				maxX = x;
			if(y > maxY)
				maxY = y;
		}
		int[] maxXY = {maxX, maxY};
		return maxXY;
	}


	//returns the minimum x and minimum y coordinate from the coordinates vector
	//returns [-1, -1] if coordinates size is 0
	public int[] getMinimumXandY(){
		Vector coordinates = struc.getCoords();
		int minX = -1;
		int minY = -1;

		if(coordinates.size() != 0){
			int[] coords = (int[])coordinates.elementAt(0);
			int x = coords[0];
			int y = coords[1];
			minX = x;
			minY = y;

			for(int i=1; i<coordinates.size(); ++i){
				coords = (int[])coordinates.elementAt(i);
				x = coords[0];
				y = coords[1];
				if(x < minX)
					minX = x;
				if(y < minY)
					minY = y;
			}
		}
		int[] minXY = {minX, minY};
		return minXY;
	}


	//calculates the new coordinates for the nodes to center the graph
	public void centerGraph(double xGraphCenter, double yGraphCenter){
		Vector coordinates = struc.getCoords();

		for(int i=0; i<coordinates.size(); ++i){
			int[] coords = (int[])coordinates.elementAt(i);
			coords[0] = (int)(1.0*coords[0] - xGraphCenter + ((xmax/editPanel.getZoom()) / 2));
			coords[1] = (int)(1.0*coords[1] - yGraphCenter + ((ymax/editPanel.getZoom()) / 2));
		}

		moveView(xGraphCenter, yGraphCenter);
	}


	//calculates the new coordinates for the scrollbars to move the view. when the
	//user centers the graph the view moves just like the graph, i.e.  if
	//centering the  graph leads to a shift of the graph by x to the right and y
	//to the bottom, then also the current view moves by these amounts (so that
	//the same portion of the graph remains visible)
	public void moveView(double xGraphCenter, double yGraphCenter){

		double distance_x = xmax/2 - xGraphCenter;


		int view_width = scrollPane.getViewport().getExtentSize().width;

		if(xmax > view_width && view_width >= 0){ //cannot move scrollbars
			int old_x = scrollPane.getHorizontalScrollBar().getValue();
			int new_x = (old_x + (int)distance_x);

			if (new_x >= 0)
				new_x = new_x % (xmax - view_width +1);
			else
				new_x = (xmax - view_width) + new_x;
			scrollPane.getHorizontalScrollBar().setValue(new_x);
		}

		double distance_y = ymax/2 - yGraphCenter;
		int view_height = scrollPane.getViewport().getExtentSize().height;
		if(ymax > view_height && view_height >= 0){ //cannot move scrollbars
			int old_y = scrollPane.getVerticalScrollBar().getValue();
			int new_y = (old_y + (int)distance_y);
			if (new_y >= 0)
				new_y = new_y % (ymax - view_height +1);
			else
				new_y = (ymax - view_height) + new_y;
			scrollPane.getVerticalScrollBar().setValue(new_y);
		}
	}


	//Centers the current view on the center of the loaded structure. This is not
	//the same thing that moveView does.
	public void centerView(){
		int[] maxCoords = getMaximumXandY();
		int[] minCoords = getMinimumXandY();
		double xGraphCenter = (minCoords[0] + maxCoords[0]) / 2.0;
		double yGraphCenter = (minCoords[1] + maxCoords[1]) / 2.0;
		double new_x, new_y;

		if(xGraphCenter >= 0 && yGraphCenter >= 0){ //there is at least one node
			int view_width = scrollPane.getViewport().getExtentSize().width;
			if(view_width == 0) //no window open yet
				new_x = xGraphCenter - (SCROLLPANE_WIDTH / 2.0);  //default width
			else
				new_x = xGraphCenter - (view_width / 2.0);

			int view_height = scrollPane.getViewport().getExtentSize().height;
			if(view_height == 0) //no window open yet
				new_y = yGraphCenter - (SCROLLPANE_HEIGHT / 2.0);
			else
				new_y = yGraphCenter - (view_height / 2.0);

			//Java checks that the values are valid
			scrollPane.getHorizontalScrollBar().setValue( (int)(new_x*editPanel.getZoom()) );
			scrollPane.getVerticalScrollBar().setValue( (int)(new_y*editPanel.getZoom()) );
		}
	}


	//rescales the x-axel of the graph so that it will fit into the canvas
	public void scaleX(int x){
		Vector coordinates = struc.getCoords();
		double ratio = 1.0 * xmax / (x + 1);

		for(int i=0; i<coordinates.size(); ++i){
			int[] coords = (int[])coordinates.elementAt(i);
			coords[0] = (int)(coords[0] * ratio);
		}
	}


	//rescales the y-axel of the graph so that it will fit into the canvas
	public void scaleY(int y){
		Vector coordinates = struc.getCoords();
		double ratio = 1.0 * ymax / (y + 1);

		for(int i=0; i<coordinates.size(); ++i){
			int[] coords = (int[])coordinates.elementAt(i);
			coords[1] = (int)(coords[1] * ratio);
		}
	}


	//information message
	public void showMessage(String message){
		infoText.setText(message);
	}


	//does the user want to see the coordinates or not
	public void showCoordinates(int x, int y){
		if(showCoords.isSelected())
			coordinates.setText((int)(x/editPanel.getZoom())+", "+(int)(y/editPanel.getZoom()));
	}


	//ask confirmation to the action presented by the string text
	public boolean confirm(String text, int type){
		int result = JOptionPane.showConfirmDialog(this, text,
				"Confirmation", type);
		if (result == JOptionPane.YES_OPTION || result == JOptionPane.OK_OPTION)
			return true;
		else //result == JOptionPane.NO_OPTION || JOptionPane.CANCEL_OPTION
			return false;
	}


	//0=clear all, 1=clear binary&arbitrary 2=clear attribute&arbitrary 3=clear attribute&binary
	public void clearSelections(int target){
		if(target == 0){
			attributesPanel.clearSelections();
			binaryPanel.clearSelections();
			arbitraryPanel.clearSelections();
		}
		if(target == 1){
			binaryPanel.clearSelections();
			arbitraryPanel.clearSelections();
		}
		if(target == 2){
			attributesPanel.clearSelections();
			arbitraryPanel.clearSelections();
		}
		if(target == 3){
			attributesPanel.clearSelections();
			binaryPanel.clearSelections();
		}
	}


	public void emptyRelationLists(){
		attributesPanel.empty();
		binaryPanel.empty();
		arbitraryPanel.empty();
	}


	public void addNode(int xc, int yc){
		struc.addNode(xc, yc);
		mainWindow.addOrRenameEvidenceModuleNode();
		setIsEdited(true);
	}


	public void deleteNode(int node){
		editPanel.nodeWindowNodeDeleted(node);
		struc.deleteNode(node);
		mainWindow.deleteElementFromEvidenceModule(node);
		setIsEdited(true);
	}

	public Vector getCoords(){
		return struc.getCoords();
	}

	public Vector getNames(){
		return struc.getNames();
	}

	public String nameAt(int index){
		return struc.nameAt(index);
	}

	public int setName(String name, int index){
		int i = struc.setName(name, index);
		if(i != 0){
			mainWindow.addOrRenameEvidenceModuleNode();
			setIsEdited(true);
		}
		return i;
	}

	public void addRelation(Rel r){
		struc.addRelation(r);
		setIsEdited(true);
		if(r.arity != 1)
			editPanel.addNewRelToNodeWindow(r);
	}

	public void deleteRelation(Rel r){
		struc.deleteRelation(r);
		setIsEdited(true);
		editPanel.deleteRelFromNodeWindow(r);
		editPanel.repaint();
	}

	public void addTuple(Rel r){
		editPanel.setRel(r);
	}

	public void addTuple(Rel r, int[] tuple, String addedTuples){
		if (struc.addTuple(r, tuple) == 1){
			editPanel.addTupleToNodeWindow(r, tuple);
			showMessage("Tuple ("+ addedTuples +") is added to relation "+ r.name.name);
			setIsEdited(true);
		}
		else
			showMessage("Tuple already in relation");
	}

	public void deleteTuple(Rel r, int[] tuple){
		struc.deleteTuple(r, tuple);
		setIsEdited(true);
	}

	public void setFrameTitle(String name){
		this.setTitle(name);
	}

	public Vector getAttributesColors(int node){
		return struc.getAttributesColors(node);
	}

	public Vector[] getBinaryColors(int node){
		return struc.getBinaryColors(node);
	}

	public Vector[] getAttrRelsAndTuples(int node){
		return struc.getAttrRelsAndTuples(node);
	}

	public Vector[] getOtherRelsAndTuples(int node){
		return struc.getOtherRelsAndTuples(node);
	}

	//current structure has been modified
	public void setIsEdited(boolean b){
		isEdited = b;
		mainWindow.setStrucEdited(b);
	}


	public void saveFile(File savefile){
//		javax.swing.filechooser.FileFilter filter = 
//			new javax.swing.filechooser.FileNameExtensionFilter("RDEF file", "rdef");
		if (rdefFilter.accept(savefile)){
			struc.saveToRDEF(savefile);
			isSaved = true;
			setIsEdited(false);
			setFrameTitle(savefile.getName());
		}
		else if (rstFilter.accept(savefile)){
			SparseRelStrucReader.saveFile(struc, savefile, mainWindow);
			isSaved = true;
			setIsEdited(false);
			setFrameTitle(savefile.getName());
		}
		else
			mainWindow.showMessage("Unknown file format " + savefile.getName());

	}

//	public void saveFile(File savefile){

//	Vector names = struc.getNames();
//	Vector coords = struc.getCoords();
//	Vector rels	= struc.getRels();

//	try{
//	PrintWriter writer = new PrintWriter(new FileOutputStream(savefile));

//	writer.print("DOMAIN: {");
//	for (int i=0; i<names.size(); ++i){
//	writer.print((String)names.elementAt(i));
//	if (i+1 < names.size())
//	writer.print(", ");
//	}
//	writer.println("};");

//	writer.print("COORDINATES: {");
//	for (int i=0; i<coords.size(); ++i){
//	int[] xy = (int[])coords.elementAt(i);
//	writer.print("("+ xy[0] +","+ xy[1] +")");
//	if (i+1 < names.size())
//	writer.print(" ");
//	}
//	writer.println("};");


//	for (int i=0; i<rels.size(); ++i){
//	SparseRel srel = (SparseRel)rels.elementAt(i);
//	writer.print("RELATION: "+ srel.rel().name.name);
//	writer.print("/"+ srel.rel().arity);
//	Vector interpretation = srel.getTuples();
//	if(srel.rel().arity == 1){
//	writer.print( " color "+srel.rel().getColor().getRGB());
//	writer.print(" {");
//	for (int j=0; j<interpretation.size(); ++j){
//	int[] temp = (int[])interpretation.elementAt(j);
//	if (j+1 < interpretation.size()){
//	writer.print(temp[0]+" ");
//	}
//	else{
//	writer.print(temp[0]);
//	}
//	}
//	}
//	if(srel.rel().arity >=2){
//	Color color = srel.rel().getColor();
//	if( color != null ){
//	writer.print( " color "+color.getRGB());
//	}
//	writer.print(" {");
//	for (int j=0; j<interpretation.size(); ++j){
//	int[] temp = (int[])interpretation.elementAt(j);
//	if (j+1 < interpretation.size()){
//	writer.print("(");
//	for(int k=0; k<temp.length-1; ++k){
//	writer.print(temp[k]+",");
//	if(k+1==temp.length-1){
//	writer.print(temp[k+1]+") ");
//	}
//	}
//	}
//	else{
//	writer.print("(");
//	for(int k=0; k<temp.length-1; ++k){
//	writer.print(temp[k]+",");
//	if(k+1==temp.length-1){
//	writer.print(temp[k+1]+")");
//	}
//	}
//	}
//	}
//	}
//	writer.println("};");
//	}

//	writer.close();
//	isSaved = true;
//	setIsEdited(false);
//	setFrameTitle(savefile.getName());

//	}catch (Exception ex){
//	Primula.showMessage("error happened during saving");
//	}
//	}


	/*ONLY FOR TESTING********
  public static void main(String[] args){
    Bavaria win = new Bavaria(new SparseRelStruc(), null);
  }
	 */

	/* Not yet functional method for displaying struc in a Bavaria 
	 * window. To be used for "animation" of sampling procedure.
	 */
	public void setRelStruc(SparseRelStruc newstruc){

		//	try {

		struc = newstruc;
		
		Rel.resetTheColorCounters();

		if(struc.getCoords().size() == 0){
//			System.out.println("no coords..");
			struc.createCoords();
		}

		
		emptyRelationLists();
		editPanel.closeAllNodeWindows();
		attributesPanel.getAttributesNames(struc.getAttributes());
		binaryPanel.getBinaryrelsNames(struc.getBinaryRelations());
		arbitraryPanel.getArbitraryNames(struc.getArbitraryRelations());
		setCanvasSize();
		centerView();
		//setFrameTitle(file.getName());
		editPanel.setRel(new Rel());
		//System.out.println("repaint...");
		editPanel.repaint();
//		isSaved = true;
//		showMessage("");
		//	} catch(Exception ex){
		//	    System.out.println(ex);
		//	}
	}


}







