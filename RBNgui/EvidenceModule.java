/*
 * EvidenceModule.java
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

import RBNpackage.*;
import RBNinference.*;
import RBNExceptions.*;
import RBNLearning.RelData;
import RBNLearning.RelDataForOneInput;
import RBNio.*;
import RBNutilities.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.*;
import myio.*;

import edu.ucla.belief.ui.primula.SamiamManager;
import edu.ucla.belief.ace.*;

public class EvidenceModule extends JFrame implements Observer, 
ActionListener, MouseListener, Control.ACEControlListener{

	public static final int OPTION_SAMPLEORD_FORWARD = 0;
	public static final int OPTION_SAMPLEORD_RIPPLE = 1;
	public static final int OPTION_NOT_SAMPLE_ADAPTIVE = 0;
	public static final int OPTION_SAMPLE_ADAPTIVE = 1;


	private JLabel attributesLabel               = new JLabel("Attributes");
	private JList attributesList                 = new JList();
	private DefaultListModel attributesListModel = new DefaultListModel();
	/** keith cascio 20060511 ... */
	private JScrollPane attributesScrollList;//     = new JScrollPane();
	/** ... keith cascio */

	private JLabel binaryLabel                   = new JLabel("Binary relations");
	private JList binaryList                     = new JList();
	private DefaultListModel binaryListModel     = new DefaultListModel();
	/** keith cascio 20060511 ... */
	private JScrollPane binaryScrollList;//         = new JScrollPane();
	/** ... keith cascio */

	private JLabel arbitraryLabel                = new JLabel("Arbitrary relations");
	private JList arbitraryList                  = new JList();
	private DefaultListModel arbitraryListModel  = new DefaultListModel();
	/** keith cascio 20060511 ... */
	private JScrollPane arbitraryScrollList;//      = new JScrollPane();
	/** ... keith cascio */

	private JLabel elementNamesLabel               = new JLabel("Element names");
	private JList elementNamesList                 = new JList();
	private DefaultListModel elementNamesListModel = new DefaultListModel();
	private JScrollPane elementNamesScrollList     = new JScrollPane();

	private JLabel instantiationsLabel               = new JLabel("Instantiations");
	private JList instantiationsList                 = new JList();
	private DefaultListModel instantiationsListModel = new DefaultListModel();
	private JScrollPane instantiationsScrollList     = new JScrollPane();

	private JLabel queryatomsLabel           = new JLabel("Query atoms");
	//fields to display sample size and weight
	private JLabel sampleSizeText = new JLabel("Sample Size");
	private JTextField sampleSize = new JTextField();
	private JLabel weightText	 = new JLabel("Weight");
	private JTextField weight = new JTextField();

	private JScrollPane queryatomsScrollList = new JScrollPane();
	//den nye queryatom tabel
	private QueryTableModel dataModel  = new QueryTableModel();;
	private JTable querytable          = new JTable();
	private JButton trueButton     = new JButton("True");
	private JButton falseButton    = new JButton("False");
	private JButton queryButton    = new JButton("Query");
	private JLabel infoMessage     = new JLabel(" ");
	private Border emptySpace      = BorderFactory.createEmptyBorder(10,25,5,25);
	//  final private Color backgroundColor;
	private JButton toggleTruthButton  		= new JButton("Toggle");
	private JButton delInstButton      		= new JButton("Delete");
	private JButton delAllInstButton   		= new JButton("Clear");
	private JButton saveInstButton    		= new JButton("Save");
	private JButton loadInstButton  		= new JButton("Load");

	private JButton delQueryAtomButton	  = new JButton("Delete");
	private JButton delAllQueryAtomButton = new JButton("Clear");

	private JPanel samplePanel         = new JPanel(new GridLayout(1,4,3,1));
	private JPanel deletesamplePanel   = new JPanel(new BorderLayout());

	private JPanel attributesPanel     = new JPanel(new BorderLayout());
	private JPanel binaryPanel         = new JPanel(new BorderLayout());
	private JPanel arbitraryPanel      = new JPanel(new BorderLayout());
	private JPanel arityPanel          = new JPanel(new GridLayout(1, 3, 0, 3));
	private JPanel elementNamesPanel   = new JPanel(new BorderLayout());
	private JPanel instantiationsPanel = new JPanel(new BorderLayout());

	//  private JPanel listsPanel          = new JPanel(new GridLayout(1, 3, 10, 1));
	private JPanel atomsPanel          		= new JPanel(new BorderLayout());
	private JPanel instButtonsPanel    		= new JPanel(new GridLayout(1, 3));
//	private JPanel truthButtonsPanel   		= new JPanel(new GridLayout(1, 2));
	private JPanel buttonsPanel        		= new JPanel(new FlowLayout());
	private JPanel buttonsAndInfoPanel 		= new JPanel(new BorderLayout());
	private JPanel queryatomsPanel 		 		= new JPanel(new BorderLayout());
	private JPanel queryatomsButtonsPanel = new JPanel(new GridLayout(1,2));
	private JPanel qbPanel = new JPanel(new BorderLayout());

	private JPanel eiPanel = new JPanel(new GridLayout(1,2));
	/** keith cascio 20060511 ... */
	private JPanel qeiPanel = new JPanel( new GridBagLayout() );//new GridLayout(2,1));
	/** ... keith cascio */

	private JPanel samplingPanel = new JPanel(new GridLayout(1,4));
	private JButton settingsSampling  = new JButton("Settings");
	private JButton startSampling = new JButton("Start");
	private JButton pauseSampling  = new JButton("Pause");
	private JButton stopSampling = new JButton("Stop");
	//   private	JPanel samplingfile 			 = new JPanel(new GridLayout(2,1));

	/** keith cascio 20060511 ... */
	private JPanel  acePanel          = new JPanel( new GridLayout( 1, 4 ) );
	private JButton aceButtonSettings = new JButton( "Settings" );
	//private JButton aceButtonCompile  = new JButton( "Compile" );
	//private JButton aceButtonLoad     = new JButton( "Load" );
	//private JButton aceButtonCompute  = new JButton( "Compute" );
	private JProgressBar aceProgressBar;

	private SettingsPanel                myACESettingsPanel;
	private Control                      myACEControl;
	/** ... keith cascio */



	private boolean first_bin = true;  //user has selected the first element
	private boolean first_arb = true;
	private boolean firstbinarystar = false;
	private int[] tuple = new int[1];
	private int index;
	private int aritynumber;
	private String addedTuples = "";

	private Primula myprimula;

	private Instantiation inst;
	private AtomList queryatoms;
	private Rel rel;
	private InstAtom selectedInstAtom;
	private Atom selectedQueryAtom;
	private boolean truthValue = true;
	private boolean queryModeOn = false;
	private int delAtom;
	private SampleThread samp;
	private boolean sampling;
	private boolean pause = false;
	private EvidenceModule evi = this;
	
	private File savefile;
	private JFileChooser fileChooser = new JFileChooser( "." );
	private javax.swing.filechooser.FileFilter myFilterRDEF;
	
	//private int evidencemode;
	//private int querymode;

//	private int isolatedzeronodesmode;


	private int [][] instantiations;
//	private boolean firstrun = true;
//	private boolean firstrunstar = true;
//	private boolean firstrunsparserel = true;
//	private int instantiationpos = 0;
//	private int size = 1;

	/* in adaptive sampling and for the query nodes the samples are assigned in a cyclic
	 * fashion to num_subsamples_adapt, resp. num_subsamples_minmax.
	 * For adaptive sampling: Variance of sampleweights in
	 * the different subsamples is used to determine
	 * the weight with which the current estimated probabilities
	 * are used for the sampling probabilities
	 * For querynodes: variance (and max/min values) in the
	 * different subsamples is displayed to provide some error estimate
	 *
	 */
	private int adaptivemode;
	private int sampleordmode;
	private int cptparents = 3; // Max. number of parents for nodes with standard cpt
	private int num_subsamples_minmax = 10;
	private int num_subsamples_adapt = 10;

	private boolean[] samplelogmode = new boolean[5];
	/* True components of samplelogmode determine what is to be logged:
	 * [0]: Sampling order
	 * [1]: Current Evidence
	 * [2]: Compact Trace
	 * [3]: Full Trace (only one of [2] or [3] can be true)
	 * [4]: Network statistics
	 */

	private boolean settingswindowopen = false;
	private RBNgui.Settings swindow;
	private BufferedWriter logwriter = null;
	private String logfilename = "";



	public EvidenceModule( Primula myprimula_param ){

		myprimula = myprimula_param;
		sampling = false;
		inst = myprimula.inst;
		queryatoms = myprimula.queryatoms;
		//evidencemode = emode;
		//querymode = qmode;
		//isolatedzeronodesmode = izeronodesmode;
		sampleordmode = OPTION_SAMPLEORD_FORWARD;
		adaptivemode = OPTION_NOT_SAMPLE_ADAPTIVE;
		for (int i=0;i<samplelogmode.length;i++)
			samplelogmode[i]=false;

		readElementNames();
		readRBNRelations();

		updateInstantiationList();
		updateQueryatomsList();

		//Creating the lists starts-----------------------------------------

		attributesList.setModel(attributesListModel);
		attributesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		/** keith cascio 20060511 ... */
		attributesScrollList = new JScrollPane( attributesList );//attributesScrollList.getViewport().add(attributesList);
		Dimension sizePreferred = attributesScrollList.getPreferredSize();
		sizePreferred.height = 64;
		attributesScrollList.setPreferredSize( sizePreferred );
		/** ... keith cascio */
		attributesPanel.add(attributesLabel, BorderLayout.NORTH);
		attributesPanel.add(attributesScrollList, BorderLayout.CENTER);

		binaryList.setModel(binaryListModel);
		binaryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		/** keith cascio 20060511 ... */
		binaryScrollList = new JScrollPane( binaryList );//binaryScrollList.getViewport().add(binaryList);
		binaryScrollList.setPreferredSize( sizePreferred );
		/** ... keith cascio */
		binaryPanel.add(binaryLabel, BorderLayout.NORTH);
		binaryPanel.add(binaryScrollList, BorderLayout.CENTER);

		arbitraryList.setModel(arbitraryListModel);
		arbitraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		/** keith cascio 20060511 ... */
		arbitraryScrollList = new JScrollPane( arbitraryList );//arbitraryScrollList.getViewport().add(arbitraryList);
		arbitraryScrollList.setPreferredSize( sizePreferred );
		/** ... keith cascio */
		arbitraryPanel.add(arbitraryLabel, BorderLayout.NORTH);
		arbitraryPanel.add(arbitraryScrollList, BorderLayout.CENTER);

		arityPanel.add(attributesPanel);
		arityPanel.add(binaryPanel);
		arityPanel.add(arbitraryPanel);

		elementNamesList.setModel(elementNamesListModel);
		elementNamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		elementNamesScrollList.getViewport().add(elementNamesList);
		elementNamesPanel.add(elementNamesLabel, BorderLayout.NORTH);
		elementNamesPanel.add(elementNamesScrollList, BorderLayout.CENTER);

		instantiationsList.setModel(instantiationsListModel);
		instantiationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		instantiationsScrollList.getViewport().add(instantiationsList);
		instantiationsPanel.add(instantiationsLabel, BorderLayout.NORTH);
		instantiationsPanel.add(instantiationsScrollList, BorderLayout.CENTER);
		instButtonsPanel.add(toggleTruthButton);
		toggleTruthButton.setToolTipText("Toggle truth value of selected atom");
		instButtonsPanel.add(delInstButton);
		delInstButton.setToolTipText("Delete selected atom");
		instButtonsPanel.add(delAllInstButton);
		delAllInstButton.setToolTipText("Clear all evidence");
		instButtonsPanel.add(saveInstButton);
		saveInstButton.setToolTipText("Save instantiations to file");
		instButtonsPanel.add(loadInstButton);
		loadInstButton.setToolTipText("Load instantiations and input domain from file");

		instantiationsPanel.add(instButtonsPanel, BorderLayout.SOUTH);

		samplePanel.add(sampleSizeText);
		sampleSizeText.setHorizontalAlignment( JLabel.RIGHT );
		samplePanel.add(sampleSize);
		sampleSize.setHorizontalAlignment( JTextField.LEFT );
		sampleSize.setEditable(false);
		sampleSize.setBackground(new Color(255, 255, 255));
		samplePanel.add(weightText);
		weightText.setHorizontalAlignment( JLabel.RIGHT );
		samplePanel.add(weight);
		weight.setEditable(false);
		weight.setHorizontalAlignment( JTextField.LEFT );
		weight.setBackground(new Color(255, 255, 255));

		/** keith cascio 20060511 ... */
		JPanel panelSamplingOuter = new JPanel( new GridBagLayout() );
		GridBagConstraints cSampling = new GridBagConstraints();

		cSampling.fill      = GridBagConstraints.BOTH;
		cSampling.gridwidth = GridBagConstraints.REMAINDER;
		cSampling.weightx   = cSampling.weighty = 1;
		panelSamplingOuter.add( samplePanel,                  cSampling );
		panelSamplingOuter.add( Box.createVerticalStrut( 8 ), cSampling );
		panelSamplingOuter.add( samplingPanel,                cSampling );
		panelSamplingOuter.setBorder(BorderFactory.createTitledBorder("Sampling"));
		/** ... keith cascio */

		queryatomsButtonsPanel.add(delQueryAtomButton);
		delQueryAtomButton.setToolTipText("Delete query atom");
		queryatomsButtonsPanel.add(delAllQueryAtomButton);

		delAllQueryAtomButton.setToolTipText("Delete all query atoms");
		deletesamplePanel.add(queryatomsButtonsPanel, BorderLayout.NORTH);
		/** keith cascio 20060511 ... */
		//deletesamplePanel.add(samplePanel, BorderLayout.SOUTH);
		/** ... keith cascio */

		querytable.setModel(dataModel);
		querytable.setShowHorizontalLines(false);
		//dimensionen skal rettes til
		querytable.setPreferredScrollableViewportSize(new Dimension(146, 100));
		//table header values
		querytable.getColumnModel().getColumn(0).setHeaderValue("Query Atoms");
		querytable.getColumnModel().getColumn(1).setHeaderValue("P");
		querytable.getColumnModel().getColumn(2).setHeaderValue("Min");
		querytable.getColumnModel().getColumn(3).setHeaderValue("Max");
		querytable.getColumnModel().getColumn(4).setHeaderValue("Var");
		/** keith cascio 20060511 ... */
		querytable.getColumnModel().getColumn(5).setHeaderValue( edu.ucla.belief.ace.Settings.STR_ACE_DISPLAY_NAME );
		/** ... keith cascio */

		querytable.getColumnModel().getColumn(0).setPreferredWidth(150);

		queryatomsScrollList.getViewport().add(querytable);
		queryatomsPanel.add(queryatomsLabel, BorderLayout.NORTH);
		queryatomsPanel.add(queryatomsScrollList, BorderLayout.CENTER);
		queryatomsPanel.add(deletesamplePanel, BorderLayout.SOUTH);
		//		queryatomsPanel.setPreferredSize(queryatomsPanel.getPreferredSize());

		//  	trueButton.setBackground(new Color(164, 164, 164));

		//MouseListeners
		attributesList.	addMouseListener( this );
		binaryList.addMouseListener( this );
		arbitraryList.addMouseListener( this );
		elementNamesList.addMouseListener( this );
		instantiationsList.addMouseListener( this );
		querytable.addMouseListener( this);
		//ActionListerners
		trueButton.addActionListener( this );
		falseButton.addActionListener( this );
		queryButton.addActionListener( this );
		toggleTruthButton.addActionListener( this );
		delInstButton.addActionListener( this );
		delAllInstButton.addActionListener( this );
		saveInstButton.addActionListener( this );
		loadInstButton.addActionListener( this );
		delQueryAtomButton.addActionListener( this );
		delAllQueryAtomButton.addActionListener(this);
		settingsSampling.addActionListener( this );
		startSampling.addActionListener( this );
		pauseSampling.addActionListener( this );
		stopSampling.addActionListener( this );
		//setting background color
		trueButton.setBackground(Primula.COLOR_BLUE_SELECTED);
		trueButton.setToolTipText("Add atoms instantiated to true");
		falseButton.setBackground(Primula.COLOR_BLUE);
		falseButton.setToolTipText("Add atoms instantiated to false");
		queryButton.setBackground(Primula.COLOR_BLUE);
		queryButton.setToolTipText("Add atoms to query list");
		settingsSampling.setBackground(Primula.COLOR_BLUE);
		toggleTruthButton.setBackground(Primula.COLOR_YELLOW);
		delInstButton.setBackground(Primula.COLOR_YELLOW);
		delAllInstButton.setBackground(Primula.COLOR_YELLOW);
		saveInstButton.setBackground(Primula.COLOR_RED);
		loadInstButton.setBackground(Primula.COLOR_RED);
		delQueryAtomButton.setBackground(Primula.COLOR_YELLOW);
		delAllQueryAtomButton.setBackground(Primula.COLOR_YELLOW);
		startSampling.setBackground(Primula.COLOR_GREEN);
		stopSampling.setBackground(Primula.COLOR_GREEN);
		pauseSampling.setBackground(Primula.COLOR_GREEN);

		//    backgroundColor = falseButton.getBackground();

		atomsPanel.add(instantiationsPanel, BorderLayout.CENTER);
		atomsPanel.add(queryatomsPanel, BorderLayout.SOUTH);

		eiPanel.add(elementNamesPanel);
		eiPanel.add(instantiationsPanel);

		/** keith cascio 20060511 ... */
		//samplingPanel.setBorder(BorderFactory.createTitledBorder("Sampling"));
		/** ... keith cascio */
		samplingPanel.add(settingsSampling);
		samplingPanel.add(startSampling);
		startSampling.setToolTipText("Start sampling");
		samplingPanel.add(pauseSampling);
		pauseSampling.setToolTipText("Pause sampling");
		samplingPanel.add(stopSampling);
		stopSampling.setToolTipText("Stop sampling");

		/** keith cascio 20060511 ... */
		//acePanel.setBorder( BorderFactory.createTitledBorder( edu.ucla.belief.ace.Settings.STR_ACE_DISPLAY_NAME ) );
		acePanel.add( aceButtonSettings );
		JButton[] buttons = new JButton[] {
				//new JButton( getACEControl().getActionCompile()     ),
				//new JButton( getACEControl().getActionLoad()        ),
				//new JButton( getACEControl().getActionCompute()     ),
				new JButton( getACEControl().getActionFastForward() ) };

		for( JButton button : buttons ){
			if( buttons.length == 1 ){
				for( int i=0; i<2; i++ ) acePanel.add( Box.createHorizontalStrut(8) );
			}
			acePanel.add( button );
			button.setBackground( Primula.COLOR_GREEN );
		}

		aceButtonSettings.addActionListener( this );
		aceButtonSettings.setBackground( Primula.COLOR_BLUE );
		aceButtonSettings.setToolTipText( edu.ucla.belief.ace.Settings.STR_ACE_DISPLAY_NAME + ", settings" );

		JPanel pnlAceOuter = new JPanel( new GridBagLayout() );
		pnlAceOuter.setBorder( BorderFactory.createTitledBorder( edu.ucla.belief.ace.Settings.STR_ACE_DISPLAY_NAME ) );
		GridBagConstraints cAceOuter = new GridBagConstraints();

		cAceOuter.fill      = GridBagConstraints.BOTH;
		cAceOuter.gridwidth = GridBagConstraints.REMAINDER;
		cAceOuter.weightx   = cAceOuter.weighty = 1;
		pnlAceOuter.add( acePanel,                            cAceOuter );
		pnlAceOuter.add( Box.createVerticalStrut( 4 ),        cAceOuter );

		cAceOuter.gridwidth = 1;
		pnlAceOuter.add( getACEProgressBar(), cAceOuter );
		cAceOuter.fill      = GridBagConstraints.NONE;
		cAceOuter.weightx   = cAceOuter.weighty = 0;
		pnlAceOuter.add( Box.createHorizontalStrut( 4 ),      cAceOuter );

		cAceOuter.gridwidth = GridBagConstraints.REMAINDER;
		JButton btn = new JButton( getACEControl().getActionCancel() );
		pnlAceOuter.add( btn,                                 cAceOuter );
		btn.setMargin(     new Insets(0,0,0,0) );
		btn.setBackground( Primula.COLOR_GREEN.brighter() );
		btn.setFont( btn.getFont().deriveFont( (float)(btn.getFont().getSize() - 1) ) );
		pnlAceOuter.add( Box.createVerticalStrut( 4 ),        cAceOuter );

		JPanel pnlInferenceAlternatives = new JPanel( new GridBagLayout() );
		GridBagConstraints cAlternatives = new GridBagConstraints();

		cAlternatives.fill      = GridBagConstraints.BOTH;
		cAlternatives.gridwidth = GridBagConstraints.REMAINDER;
		cAlternatives.weightx   = cAlternatives.weighty = 1;
		pnlInferenceAlternatives.add( Box.createVerticalStrut( 8 ), cAlternatives );
		pnlInferenceAlternatives.add( panelSamplingOuter,           cAlternatives );
		pnlInferenceAlternatives.add( pnlAceOuter,                  cAlternatives );

		//resetACEEnabledState( getACEControl() );
		/** ... keith cascio */

		buttonsPanel.add(trueButton);
		buttonsPanel.add(falseButton);
		buttonsPanel.add(queryButton);


		/** keith cascio 20060511 ... */
		GridBagConstraints cQEI = new GridBagConstraints();
		cQEI.fill      = GridBagConstraints.BOTH;
		cQEI.gridwidth = GridBagConstraints.REMAINDER;
		cQEI.weightx   = cQEI.weighty = 1;
		JSplitPane qeiSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,arityPanel,eiPanel);
		//qeiPanel.add(qeiSplit);
		/** ... keith cascio */

		qbPanel.add(queryatomsPanel ,BorderLayout.CENTER);
		/** keith cascio 20060511 ... */
		qbPanel.add( pnlInferenceAlternatives, BorderLayout.SOUTH );//qbPanel.add(samplingPanel, BorderLayout.SOUTH);
		/** ... keith cascio */

		buttonsAndInfoPanel.add(qeiSplit, BorderLayout.CENTER);
		buttonsAndInfoPanel.add(buttonsPanel, BorderLayout.SOUTH);

		//Creates the main layout
		Container contentPane = this.getContentPane();
		JPanel lowerPanel = new JPanel(new BorderLayout());
		lowerPanel.add(qbPanel, BorderLayout.CENTER );
		lowerPanel.add(infoMessage, BorderLayout.SOUTH );
		JSplitPane querySampleSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,buttonsAndInfoPanel,lowerPanel);
		contentPane.setLayout(new BorderLayout());
		contentPane.add(querySampleSplit);

		//Inner class for closing the window
		this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						if (settingswindowopen)
							swindow.dispose();
						dispose();
						Primula.setIsEvModuleOpen(false);
					}
				}
		);

		fileChooser.addChoosableFileFilter(myFilterRDEF = new Filter_rdef());
		fileChooser.setFileFilter( myFilterRDEF );
		
		ImageIcon icon = new ImageIcon("small_logo.jpg");
		if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
			this.setIconImage(icon.getImage());		//image ok
		}
		this.setTitle("Inference Module");
		this.setSize(600, 680);
		/** keith cascio 20060511 ... */
		SamiamManager.centerWindow( this );
		/** ... keith cascio */
		this.setVisible(true);
	}

	public void actionPerformed( ActionEvent e ) 
	{
		Object source = e.getSource();

		if( source == trueButton ){
			first_bin = first_arb = true;
			trueButton.setBackground(Primula.COLOR_BLUE_SELECTED);
			falseButton.setBackground(Primula.COLOR_BLUE);
			queryButton.setBackground(Primula.COLOR_BLUE);
			elementNamesList.clearSelection();
			truthValue = true;
			queryModeOn = false;
			infoMessage.setText(" ");
		}
		else if( source == falseButton ){
			first_bin = first_arb = true;
			falseButton.setBackground(Primula.COLOR_BLUE_SELECTED);
			trueButton.setBackground(Primula.COLOR_BLUE);
			queryButton.setBackground(Primula.COLOR_BLUE);
			elementNamesList.clearSelection();
			truthValue = false;
			queryModeOn = false;
			infoMessage.setText(" ");
		}
		else if( source == queryButton ){
			first_bin = first_arb = true;
			queryButton.setBackground(Primula.COLOR_BLUE_SELECTED);
			trueButton.setBackground(Primula.COLOR_BLUE);
			falseButton.setBackground(Primula.COLOR_BLUE);
			elementNamesList.clearSelection();
			queryModeOn = true;
			infoMessage.setText(" ");
		}
		else if( source == toggleTruthButton ){
			if(selectedInstAtom != null){
				if(selectedInstAtom.truthval == true){
					inst.add(selectedInstAtom.rel, selectedInstAtom.args, false,"?");
				}
				else{
					inst.add(selectedInstAtom.rel, selectedInstAtom.args, true,"?");
				}
				updateInstantiationList();
			}
		}
		else if( source == delInstButton){
			if(selectedInstAtom != null){
				int selected = instantiationsList.getSelectedIndex();
				inst.delete(selectedInstAtom.rel, selectedInstAtom.args);
				updateInstantiationList();
				int listsize = instantiationsList.getModel().getSize()-1;

				if(selected >= listsize ){
					selected--;
				}
				if(selected != -1){
					instantiationsList.setSelectedIndex(selected);
					Vector instAtoms = inst.allInstAtoms();
					selectedInstAtom = (InstAtom) instAtoms.elementAt(selected);
				}
				else selectedInstAtom = null;
			}
		}
		else if( source == delAllInstButton ){
			inst.reset();
			updateInstantiationList();
		}
		else if ( source == saveInstButton ){

			
			if (savefile != null)
				fileChooser.setSelectedFile(savefile);
			int value = fileChooser.showDialog(EvidenceModule.this, "Select");
			if (value == JFileChooser.APPROVE_OPTION) {
				savefile = fileChooser.getSelectedFile();
				inst.saveToRDEF(savefile,myprimula.getRels());
			}
		}
		else if ( source == loadInstButton ){
			
			if (myprimula.confirm("Input domain will be re-read from file. Edits to current input domain may be lost. Continue?")){
				if (savefile != null)
					fileChooser.setSelectedFile(savefile);
				int value = fileChooser.showDialog(EvidenceModule.this, "Select");
				if (value == JFileChooser.APPROVE_OPTION) {
					savefile = fileChooser.getSelectedFile();
					RDEFReader rdefrdr = new RDEFReader();
					String datnm = savefile.getPath();
					try{
						RelData tempreldata = (RelData)rdefrdr.readRDEF(datnm,null);
						RelDataForOneInput temprdfoi = tempreldata.caseAt(0);
						inst = new Instantiation(temprdfoi.oneStrucDataAt(0));
						myprimula.setRelStruc(temprdfoi.inputDomain());
						myprimula.setInst(inst);
						updateInstantiationList();
					}
					catch (RBNIllegalArgumentException ex){System.out.println(ex);}
				}
			}
		}
		else if( source == delQueryAtomButton ){
			if(selectedQueryAtom != null){
				dataModel.removeQuery(delAtom);
				generateQueryatoms();
				updateQueryatomsList();
				Vector queries = queryatoms.allAtoms();
				int listsize = queries.size()-1;
				if( delAtom >= listsize ){
					delAtom--;
				}
				if(delAtom != -1){
					delAtom--;
					if( delAtom == -1 ){
						delAtom++;
						querytable.setRowSelectionInterval(delAtom, delAtom);
						selectedQueryAtom = (Atom)queries.elementAt(delAtom );
					}
					else{
						querytable.setRowSelectionInterval(delAtom, delAtom);
						selectedQueryAtom = (Atom)queries.elementAt(delAtom );
					}
				}
			}
		}
		else if(source == delAllQueryAtomButton){
			dataModel.removeAllQueries();
			generateQueryatoms();
			updateQueryatomsList();
		}
		else if( source == settingsSampling ){
			if (!settingswindowopen){
				swindow = new RBNgui.Settings( EvidenceModule.this );
				settingswindowopen = true;
			}
		}
		else if( source == startSampling ){
			sampling = true;
			PFNetwork pfn = null;
			if (!noLog()){
				if (logfilename != "")
					logwriter = myio.FileIO.openOutputFile(logfilename);
				else logwriter = new BufferedWriter(new OutputStreamWriter(System.out));

			}

			try{
				BayesConstructor constructor = null;
				constructor = new BayesConstructor(myprimula.rbn,myprimula.rels,inst,queryatoms,myprimula);
				pfn = constructor.constructPFNetwork(myprimula.evidencemode,
						Primula.OPTION_QUERY_SPECIFIC,
						myprimula.isolatedzeronodesmode);
				pfn.prepareForSampling(sampleordmode,
						adaptivemode,
						samplelogmode,
						cptparents,
						queryatoms,
						num_subsamples_minmax,
						num_subsamples_adapt,
						logwriter);
			}
			catch(RBNCompatibilityException ex){System.out.println(ex.toString());}
			catch(RBNIllegalArgumentException ex){System.out.println(ex.toString());}
			catch(RBNCyclicException ex){System.out.println(ex.toString());}
			catch (RBNInconsistentEvidenceException ex){System.out.println("Inconsistent Evidence");}
			catch (IOException ex){System.out.println(ex.toString());}


			samp = new SampleThread(evi, pfn, queryatoms,num_subsamples_minmax,samplelogmode,logwriter);
			samp.start();
			infoMessage.setText(" Starting Sampling ");
			startSampling.setEnabled( false );
			trueButton.setEnabled( false );
			falseButton.setEnabled( false );
			queryButton.setEnabled( false );
			toggleTruthButton.setEnabled( false );
			delInstButton.setEnabled( false );
			delAllInstButton.setEnabled( false );
			delQueryAtomButton.setEnabled( false );
			delAllQueryAtomButton.setEnabled( false );
		}
		else if( source == pauseSampling){
			if(pause == true){
				pause = false;
			}
			else{
				pause = true;
			}
			samp.setPause(pause);
			if (pause){
				infoMessage.setText(" Pause Sampling ");
			}
			else{
				infoMessage.setText(" Resume Sampling ");
			}

		}
		else if( source == stopSampling){
			sampling = false;
			samp.setRunning(false);
			if (!noLog()){
				try{
					logwriter.flush();
					if (logfilename != "")
						logwriter.close();
				}
				catch (java.io.IOException ex){System.err.println(ex);};
			}
			infoMessage.setText(" Stop Sampling ");
			pause = false;
			startSampling.setEnabled( true );
			trueButton.setEnabled( true );
			falseButton.setEnabled( true );
			queryButton.setEnabled( true );
			toggleTruthButton.setEnabled( true );
			delInstButton.setEnabled( true );
			delAllInstButton.setEnabled( true );
			delQueryAtomButton.setEnabled( true );
			delAllQueryAtomButton.setEnabled( true );
		}
		/** keith cascio 20060511 ... */
		else if( source == aceButtonSettings ) doAceSettings();
		/** ... keith cascio */
	}

	/** @author keith cascio
	@since 20060602 */
	public void forgetAll(){
		if( myACEControl != null ) myACEControl.forgetAll();
	}

	/** @author keith cascio
	@since 20060511 */
	private void doAceSettings(){
		if( myACESettingsPanel == null ) myACESettingsPanel = new SettingsPanel();
		myACESettingsPanel.show( (Component)EvidenceModule.this, myprimula.getPreferences().getACESettings() );
	}

	/** @author keith cascio
	@since 20060511 */
	public Control getACEControl(){
		if( myACEControl == null ){
			myACEControl = new Control( myprimula );
			myACEControl.setParentComponent( (Component) this );
			myACEControl.setProgressBar( EvidenceModule.this.getACEProgressBar() );
			myACEControl.set( myprimula.getPreferences().getACESettings() );
			myACEControl.addListener( (Control.ACEControlListener) this );
			myACEControl.setDataModel( EvidenceModule.this.dataModel );
			myACEControl.setInfoMessage( EvidenceModule.this.infoMessage );
		}
		return myACEControl;
	}

	/** @author keith cascio
	@since  20060728 */
	private JProgressBar getACEProgressBar(){
		if( aceProgressBar == null ){
			aceProgressBar = new JProgressBar();
			aceProgressBar.setStringPainted( true );
		}
		return aceProgressBar;
	}

	/** interface Control.ACEControlListener
    	@author keith cascio
	@since 20060511 */
	public void aceStateChange( Control control ){
		//EvidenceModule.this.resetACEEnabledState( control );
		if( !control.isReadyCompute() ) dataModel.resetACE();
		//clearACEMessage();
	}

	/** @author keith cascio
	@since  20060725 */
	public void relationalStructureEdited(){
		if( myACEControl != null ) myACEControl.clear();
	}

	public void mouseClicked(MouseEvent e) {
		Object source = e.getSource();
	}

	//          Invoked when the mouse button has been clicked (pressed and released) on a component.
	public void mouseEntered(MouseEvent e) {
		Object source = e.getSource();
	}

	//          Invoked when the mouse enters a component.
	public void mouseExited(MouseEvent e) {
		Object source = e.getSource();
	}
	//          Invoked when the mouse exits a component.
	public void mousePressed(MouseEvent e) {
		Object source = e.getSource();

		if(source == attributesList){
			first_bin = first_arb = true;
			binaryList.clearSelection();
			arbitraryList.clearSelection();
			elementNamesList.clearSelection();
			int index = attributesList.locationToIndex(e.getPoint());
			if(index >= 0){
				rel = (Rel)attributesListModel.elementAt(index);
				infoMessage.setText(rel.name.name);
			}
		}
		else if( source == binaryList ){
			first_bin = first_arb = true;
			attributesList.clearSelection();
			arbitraryList.clearSelection();
			elementNamesList.clearSelection();
			int index = binaryList.locationToIndex(e.getPoint());
			if(index >= 0){
				rel = (Rel)binaryListModel.elementAt(index);
				infoMessage.setText(rel.name.name);
			}
		}
		else if( source == arbitraryList ){
			first_bin = first_arb = true;
			attributesList.clearSelection();
			binaryList.clearSelection();
			elementNamesList.clearSelection();
			int index = arbitraryList.locationToIndex(e.getPoint());
			if(index >= 0){
				rel = (Rel)arbitraryListModel.elementAt(index);
				infoMessage.setText(rel.name.name);
			}
			if (rel.arity == 0){
				infoMessage.setText(rel.name.name+"()");
				if(queryModeOn){
					queryatoms.add(rel,new int[0]);
					updateQueryatomsList();
				}
				else{
					inst.add(new Atom(rel,new int[0]),truthValue,"?");
					updateInstantiationList();
				}
			}
		}
		else if( source == elementNamesList ){
			SparseRelStruc sparserst = (SparseRelStruc)myprimula.rels;
			Vector elementNames = sparserst.getNames();
			Vector attributeNames = sparserst.getAttributes();
			int selected;
			if(!sampling){
				if(rel != null){  //relation should be selected first
					selected = elementNamesList.locationToIndex(e.getPoint());
					if(selected >= 0){
						//an attribute
						if(rel.arity == 1){
							//MJ->
							tuple = new int[1];
							//<-MJ
							int[] node = {selected};
							addedTuples = (String)elementNamesListModel.elementAt(selected);
							if(queryModeOn){
								addAtoms(rel, node);
							}
							else{
								instantiations = new int[1][tuple.length];
								//System.out.println("tuple.length: " + tuple.length);
//								firstrunstar = true;
//								instantiationpos = 0;
//								size = 1;
								addInstantiation(node);
								inst.add(rel, instantiations, truthValue,"?");
								updateInstantiationList();
								infoMessage.setText(rel.name.name+" ("+addedTuples+") "+truthValue+" added");
							}
						}
						//a binary relation
						else if(rel.arity == 2){
							if(first_bin){
								tuple = new int[2];
								tuple[0] = selected;
								addedTuples  = (String)elementNamesListModel.elementAt(tuple[0]);
								first_bin = false;
								if(elementNamesListModel.elementAt(selected).equals("*")){
									firstbinarystar=true;
								}
								if(queryModeOn){
									infoMessage.setText(rel.name.name+" ("+addedTuples+",...)");
								}
								else{
									infoMessage.setText(rel.name.name+" ("+addedTuples+",...) "+truthValue);
								}
							}
							else if(!first_bin){
								tuple[1] = selected;
								first_bin = true;
								addedTuples = addedTuples + ", " + (String)elementNamesListModel.elementAt(tuple[1]);
								if(queryModeOn){
									addAtoms(rel, tuple);
								}
								else{
									//************
									instantiations = new int[1][tuple.length];
//									firstrunstar = true;
//									instantiationpos = 0;
//									size = 1;
									addInstantiation(tuple);
									inst.add(rel, instantiations, truthValue,"?");
									updateInstantiationList();
									infoMessage.setText(rel.name.name+" ("+addedTuples+") "+truthValue+" added");
									tuple = new int[0];
								}
							}
							firstbinarystar = false;
						}
						//an arbitrary relation
						else if(rel.arity >= 3){
							if(first_arb){
								aritynumber = rel.arity;
								tuple = new int[aritynumber];
								index = 0;
								tuple[index] = selected;
								addedTuples = (String)elementNamesListModel.elementAt(tuple[index]);
								++index;
								--aritynumber;
								first_arb = false;
								if(queryModeOn){
									infoMessage.setText(rel.name.name+" ("+addedTuples+",...)");
								}
								else{
									infoMessage.setText(rel.name.name+" ("+addedTuples+",...) "+truthValue);
								}
							}
							else if(!first_arb){
								tuple[index] = selected;
								addedTuples = addedTuples + ", " + (String)elementNamesListModel.elementAt(tuple[index]);
								++index;
								--aritynumber;
								if(aritynumber==0){
									first_arb = true;
									if(queryModeOn){
										infoMessage.setText("This can take a few minuts, please wait.");
										addAtoms(rel, tuple);
										tuple = new int[0];
									}
									else{
										infoMessage.setText("This can take a few minuts, please wait.");
										//************
//										size = 1;
										instantiations = new int[1][tuple.length];
//										firstrunstar = true;
//										instantiationpos = 0;

										addInstantiation(tuple);
										inst.add(rel, instantiations, truthValue,"?");
										updateInstantiationList();
										infoMessage.setText(rel.name.name+" ("+addedTuples+") "+truthValue+" added");
										tuple = new int[0];
									}
									addedTuples = "";
								}
								else{
									if(queryModeOn)
										infoMessage.setText(rel.name.name+" ("+addedTuples+",...) ");
									else
										infoMessage.setText(rel.name.name+" ("+addedTuples+",...) "+truthValue);
								}
							}
						}
					}
				}
				else
					infoMessage.setText("Please, choose the relation first");
			}
			else{
				JOptionPane.showMessageDialog(null, "Stop sampling before adding a new query", "Stop sampling", JOptionPane.ERROR_MESSAGE);
			}
		}
		else if( source == instantiationsList){
			int index = instantiationsList.locationToIndex(e.getPoint());
			if(index >= 0){
				Vector instAtoms = inst.allInstAtoms();
				selectedInstAtom = (InstAtom)instAtoms.elementAt(index);
			}
			else
				selectedInstAtom = null;
		}
		else if( source == querytable ){
			int index = querytable.rowAtPoint(e.getPoint());
			if(index>=0){
				delAtom = index;
				Vector queries = queryatoms.allAtoms();
				selectedQueryAtom = (Atom)queries.elementAt(index);
			}
			else{
				selectedQueryAtom = null;
			}
		}
	}
	//          Invoked when a mouse button has been pressed on a component.
	public void mouseReleased(MouseEvent e) {
		Object source = e.getSource();
	}
	//          Invoked when a mouse button has been released on a component.




	//reads the element names from the relstruc
	private void readElementNames(){
		if(myprimula.rels instanceof SparseRelStruc){
			SparseRelStruc sparserst = (SparseRelStruc)myprimula.rels;
			Vector elementNames = sparserst.getNames();
			for(int i=0; i<elementNames.size(); ++i){
				elementNamesListModel.addElement((String)elementNames.elementAt(i));
			}
			Vector<Rel> attributeNames = sparserst.getAttributes();
			for(int j =0; j<attributeNames.size();j++){
				elementNamesListModel.addElement("["+attributeNames.elementAt(j)+"*]");
			}
		}
		if(myprimula.rels instanceof OrdStruc){
			OrdStruc ordStruc = (OrdStruc)myprimula.rels;
			for(int i=0; i<ordStruc.dom; ++i){
				elementNamesListModel.addElement(ordStruc.nameAt(i));
			}
		}
		elementNamesListModel.addElement("*");
	}

	//new rst-file loaded or OrdStruc created
	public void newElementNames(){
		elementNamesListModel.clear();
		readElementNames();
		//inst.reset();
		instantiationsListModel.clear();
		queryatoms.reset();
		dataModel.reset();
		infoMessage.setText(" ");
		first_bin = first_arb = true;
		selectedInstAtom = null;
		selectedQueryAtom = null;

		if( myACEControl != null ) myACEControl.clear();//keith cascio 20061201
	}

	public void newAdaptiveMode(int admode){
		adaptivemode = admode;
	}

	public void newSampleordMode(int sordmode){
		sampleordmode = sordmode;
	}


	//user adds an new element or renames the element name (in Bavaria)
	public void addOrRenameElementName(){
		int selected = elementNamesList.getSelectedIndex();
		elementNamesListModel.clear();
		readElementNames();
		updateInstantiationList();
		updateQueryatomsList();
		if(selected != -1)
			elementNamesList.setSelectedIndex(selected);
	}


	//user deletes the element (in Bavaria)
	public void deleteElementName(int node){
		elementNamesListModel.clear();
		readElementNames();
		inst.deleteShift(node);
		updateInstantiationList();
		queryatoms.delete(node);
		queryatoms.shiftArgs(node);
		updateQueryatomsList();
		for(int i=0; i<tuple.length; ++i){
			if(tuple[i] == node){
				infoMessage.setText("Tuple cancelled (included a deleted node)");
				first_bin = first_arb = true;
			}
		}
	}

	//reads the relation names from the rbn-file
	public void readRBNRelations(){
		if(myprimula.rbn != null){
			Rel[] rels = myprimula.rbn.Rels();
			for(int i=0; i<rels.length; ++i){
				if(rels[i].arity == 1)
					attributesListModel.addElement(rels[i]);
				else if(rels[i].arity == 2)
					binaryListModel.addElement(rels[i]);
				//else if(rels[i].arity >= 3)
				else
					arbitraryListModel.addElement(rels[i]);
			}
		}
	}


	//new RBN file loaded
	public void updateRBNRelations(){
		attributesListModel.clear();
		binaryListModel.clear();
		arbitraryListModel.clear();
		readRBNRelations();
		//inst.reset();
		instantiationsListModel.clear();
		queryatoms.reset();
		dataModel.reset();
		elementNamesList.clearSelection();
		infoMessage.setText(" ");
		first_bin = first_arb = true;
		rel = null;
		selectedInstAtom = null;
		selectedQueryAtom = null;

		if( myACEControl != null ) myACEControl.clear();//keith cascio 20060515
	}


	private void addInstantiation(int[] tuple){
		Vector<int[]> elementsForCoordinate = new Vector<int[]>();
		int[] nextComponent;
		String stringAtTupleIndex;
		for(int i=0; i<tuple.length; i++){
			stringAtTupleIndex = (String)elementNamesListModel.elementAt(tuple[i]);
			if(stringAtTupleIndex.equals("*")){
				nextComponent = new int[myprimula.getRels().domSize()];
				for(int j=0; j<nextComponent.length; j++)
					nextComponent[j]=j;
			}
			else if(stringAtTupleIndex.startsWith("[")){
				String attrname = stringAtTupleIndex.substring(1,stringAtTupleIndex.length()-2);
				Vector<int[]> elementsOfAttr = myprimula.getRels().allTrue(new Rel(attrname,1));
				/* Turn vector of int[1] into int[]:*/
				nextComponent = new int[elementsOfAttr.size()];
				for(int j=0; j<nextComponent.length; j++)
					nextComponent[j]= ((int[])elementsOfAttr.elementAt(j))[0];
			}
			else{ /* tuple[i] is the domain element with index i */
				nextComponent = new int[1];
				nextComponent[0]=tuple[i];
			}
			elementsForCoordinate.add(nextComponent);
		}
		instantiations = rbnutilities.cartesProd(elementsForCoordinate);
	}

	//updates the instantiation list
//	private void addInstantiation(Rel rel, int[] tuple, boolean truth){
//	System.out.println("addInstantiation " + rel.printname() + ", " + rbnutilities.arrayToString(tuple));
//	SparseRelStruc rstnew = new SparseRelStruc();
//	rstnew = (SparseRelStruc)myprimula.rels;

//	int[] temp = new int[tuple.length];
//	int pos = 0;
//	int length = tuple.length;
//	for(int x=0; x<tuple.length; x++){
//	temp[x] = tuple[x];
//	}
//	for(int i=0; i<length; i++){
//	if(elementNamesListModel.elementAt(tuple[i]).equals("*")){
//	Vector v = rstnew.getNames();
//	int oldsize = 1;
//	if(!firstrunstar){
//	int k =0;
//	while(k <= pos){
//	oldsize = oldsize * v.size();
//	k++;
//	}
//	}
//	else{
//	firstrunstar = false;
//	oldsize = v.size();
//	}
//	if(oldsize>size){
//	size = oldsize;
//	instantiations = new int[size][tuple.length];
//	}
//	for(int j=0; j<v.size(); j++){
//	temp[pos] = j;
//	addInstantiation(rel, temp, truthValue);
//	}
//	}
//	else if(((String)elementNamesListModel.elementAt(tuple[i])).startsWith("[")){
//	Vector attributeNames = rstnew.getAttributes();
//	SparseRel srel;
//	for(int j =0; j<attributeNames.size();j++){
//	srel = (SparseRel)attributeNames.elementAt(j);		    
//	if(((String)elementNamesListModel.elementAt(tuple[i])).equals("["+srel.rel()+"*]")){
//	Vector tuples = srel.getTuples();
//	//System.out.println("tuples size "+tuples.size());
//	int oldsize = 1;
//	if(!firstrunstar){
//	int k =0;
//	while(k <= pos){
//	oldsize = oldsize * tuples.size();
//	k++;
//	}
//	}
//	else{
//	firstrunstar = false;
//	oldsize = tuples.size();
//	}
//	if(oldsize>size){
//	size = oldsize;
//	instantiations = new int[size][tuple.length];
//	}

//	for(int k =0; k<tuples.size(); k++){
//	int[] temp2 = (int[])tuples.elementAt(k);
//	temp[pos] = temp2[0];
//	addInstantiation(rel, temp, truthValue);
//	}
//	}
//	}
//	}
//	else{
//	if(pos == length-1 && instantiationpos<size){
//	for(int j=0; j <temp.length; j++){
//	System.out.println("Size "+size);
//	// 			            System.out.print("position "+instantiationpos+"  "+j);
//	instantiations [instantiationpos][j] = temp[j];
//	System.out.println(" item "+instantiations[instantiationpos][j] );
//	}
//	instantiationpos++;
//	}
//	}
//	pos++;
//	}
//	temp = null;

//	//  keith cascio 20061023
//	//  following line incorrect - call ACEControl.primulaEvidenceChanged() only from updateInstantiationList()
//	////if( myACEControl != null ) myACEControl.primulaEvidenceChanged();//keith cascio 20060609
//	}


	public void updateInstantiationList(){
		//		selectedInstAtom = null;
		instantiationsListModel.clear();
		inst = myprimula.inst;
		Vector instAtoms = inst.allInstAtoms();
		for(int i=0; i<instAtoms.size(); ++i){
			InstAtom temp = (InstAtom)instAtoms.elementAt(i);
			int[] nodes = temp.args;
			String names = "(";
			for(int j=0; j<nodes.length; ++j){
				if(j+1 < nodes.length){
					names = names + elementNamesListModel.elementAt(nodes[j]) + ", ";
				}
				else{  //last item
					names = names + elementNamesListModel.elementAt(nodes[j]);
				}
			}
			names = names + ")";
			String listItem = (String)(temp.rel.name.name)  + names + " = " + temp.truthval;
			instantiationsListModel.addElement(listItem);
		}

		if( myACEControl != null ) myACEControl.primulaEvidenceChanged();//keith cascio 20061010
	}



	//updates the query atoms list
	private void addAtoms(Rel rel, int[] tuple){
		SparseRelStruc rstnew = new SparseRelStruc();
		rstnew = (SparseRelStruc)myprimula.rels;

		int[] temp = new int[tuple.length];
		int pos = 0;
		int length = tuple.length;
		for(int x=0; x<tuple.length; x++){
			temp[x] = tuple[x];
		}
		for(int i=0; i<length; i++){
			if(elementNamesListModel.elementAt(tuple[i]).equals("*")){
				Vector v = rstnew.getNames();
				for(int j=0; j<v.size(); j++){
					temp[pos] = j;
					addAtoms(rel, temp);
				}
			}
			else if(((String)elementNamesListModel.elementAt(tuple[i])).startsWith("[")){
				Vector<Rel> attributeNames = rstnew.getAttributes();
				Rel nextattr;
				for(int j =0; j<attributeNames.size();j++){
					nextattr = attributeNames.elementAt(j);
					if(((String)elementNamesListModel.elementAt(tuple[i])).equals("["+ nextattr +"*]")){
						Vector<int[]> tuples = rstnew.allTrue(nextattr);
						for(int k =0; k<tuples.size(); k++){
							int[] temp2 = tuples.elementAt(k);
							temp[pos] = temp2[0];
							addAtoms(rel, temp);
						}
					}
				}
			}
			else{
				if(pos == length-1){
					queryatoms.add(rel, temp);
				}
			}
			pos++;
		}
		updateQueryatomsList();
		infoMessage.setText(rel.name.name+" ("+addedTuples+") added");
		temp = null;
	}

	public void updateQueryatomsList(){
		selectedQueryAtom = null;
		dataModel.reset();
		Vector queries = queryatoms.allAtoms();
		for(int i=0; i<queries.size(); ++i){
			Atom temp = (Atom)queries.elementAt(i);
			int nodes[] = temp.args;
			Rel rel = temp.rel;
			String names = ""+rel.name.name + "(";
			for(int j=0; j<nodes.length; ++j){
				if(j+1 < nodes.length){
					names = names + elementNamesListModel.elementAt(nodes[j]) + ", ";
				}
				else { //last item
					names = names + elementNamesListModel.elementAt(nodes[j]);
				}
			}
			names = names + ")";
			String listItem = names;
			dataModel.addQuery(listItem);
		}
		querytable.updateUI();

		if( myACEControl != null ) myACEControl.primulaQueryChanged();//keith cascio 20060620
	}

	private void generateQueryatoms(){
		LinkedList relstruct = new LinkedList();
		queryatoms.reset();
		LinkedList queryatoms = dataModel.getQuery();
		for(int i=0; i<queryatoms.size(); i++){
			String atom = ""+queryatoms.get(i);
			//System.out.println("in generateQueryAtoms: " + atom);
			String rel = atom.substring(0, atom.indexOf("("));
			//rel = rel.substring(0, atom.indexOf(" "));
			LinkedList elementNames = new LinkedList();
			int comma = atom.indexOf("(")+1;
			for(int j = atom.indexOf("("); j<atom.length(); j++){
				String temp =""+ atom.charAt(j);
				if(temp.equals(",")){
					String element = atom.substring(comma, j);
					elementNames.add(element);
					comma = j+2;
				}
			}
			String element = atom.substring(comma, atom.indexOf(")"));
			elementNames.add(element);
			int[] tuple = new int[elementNames.size()];
			Rel relnew = new Rel();
			if(elementNames.size() == 1){
				for(int m=0;m<attributesListModel.size();m++){
					if(attributesListModel.get(m).toString().equals(rel)){
						relnew = (Rel)attributesListModel.get(m);
					}
				}
			}
			else if(elementNames.size() == 2){
				for(int m=0;m<binaryListModel.size();m++){
					if(binaryListModel.get(m).toString().equals(rel)){
						relnew = (Rel)binaryListModel.get(m);
						//System.out.println("binaryListModel: "+binaryListModel.get(m).toString());
					}
				}
			}
			else {
				for(int m=0;m<arbitraryListModel.size();m++){
					if(((Rel)arbitraryListModel.get(m)).printname().equals(rel)){
						relnew = (Rel)arbitraryListModel.get(m);
					}
				}
			}
			int [] args = new int [elementNames.size()];
			for(int n=0; n<elementNames.size(); n++){
				for(int o=0; o<elementNamesListModel.size(); o++){
					if(elementNamesListModel.get(o).equals(elementNames.get(n))){
						args[n] = o;
					}
				}
			}
			TempAtoms temp = new TempAtoms(relnew, args);
			relstruct.add(temp);
		}
		for(int t=0; t<relstruct.size(); t++){
			TempAtoms temp = (TempAtoms)relstruct.get(t);
			addAtoms(temp.getRel(), temp.getArgs());
		}
	}

	public String getLogfilename(){
		return logfilename;
	}
	public void setLogfilename(String logfilename){
		this.logfilename = logfilename;
	}

	public int getSampleOrdMode(){
		return sampleordmode;
	}

	public void setSampleOrdMode(int sampleordmode){
		this.sampleordmode = sampleordmode;
	}

	public int getAdaptiveMode(){
		return adaptivemode;
	}

	public void setAdaptiveMode(int adaptivemode){
		this.adaptivemode = adaptivemode;
	}

	public void setSettingsOpen(boolean b){
		settingswindowopen = b;
	}

	public boolean[] getSampleLogMode(){
		return samplelogmode;
	}

	public boolean getSampleLogMode(int i){
		return samplelogmode[i];
	}

	public void setCPTParents(int np){
		this.cptparents = np;
	}

	public int getCPTParents(){
		return cptparents;
	}

	public void setNumSubsamples_minmax(int nss){
		this.num_subsamples_minmax = nss;
	}

	public int getNumSubsamples_minmax(){
		return num_subsamples_minmax;
	}

	public void setNumSubsamples_adapt(int nss){
		this.num_subsamples_adapt = nss;
	}

	public int getNumSubsamples_adapt(){
		return num_subsamples_adapt;
	}



	private boolean noLog(){
		boolean result = true;
		for (int i=0;i<samplelogmode.length;i++){
			if (samplelogmode[i])
				result = false;
		}
		return result;
	}

	public void setSampleLogMode(int i, boolean b){
		this.samplelogmode[i] = b;
	}


	//     public void setDummyDouble(double dummydouble ){
		// 	this.dummydouble = dummydouble;
	//     }

	//     public double getDummyDouble(){
	// 	return dummydouble;
	//     }


	public void update(Observable o, Object arg){
		dataModel.resetProb();
		double [] prob= ((SampleProbs)o).getProbs();
		for(int i=0; i<prob.length; i++){
			dataModel.addProb(""+prob[i]);
		}
		dataModel.resetMinProb();
		double [] minprob = ((SampleProbs)o).getMinProbs();
		for(int i=0; i<minprob.length; i++){
			dataModel.addMinProb(""+minprob[i]);
		}
		dataModel.resetMaxProb();
		double [] maxprob = ((SampleProbs)o).getMaxProbs();
		for(int i=0; i<maxprob.length; i++){
			dataModel.addMaxProb(""+maxprob[i]);
		}
		dataModel.resetVar();
		double [] var = ((SampleProbs)o).getVar();
		for(int i=0; i<var.length; i++){
			dataModel.addVar(""+var[i]);
		}
		/** keith cascio 20060511 ... */
		//dataModel.resetACE();
		/** ... keith cascio */

		sampleSize.setText(""+((SampleProbs)o).getSize());
		Double dweight = new Double(((SampleProbs)o).getWeight());
		weight.setText(""+ myio.StringOps.doubleConverter(dweight.toString()));
		querytable.updateUI();
	}
}
