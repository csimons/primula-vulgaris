/*
 * Primula.java
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



import javax.swing.*;

import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.zip.*;


import MLNParser.MLNParserFacade;
import RBNpackage.*;
import RBNio.*;
import RBNinference.*;
import RBNExceptions.*;
import RBNLearning.RelData;
import RBNutilities.*;

//import myio.*;

import edu.ucla.belief.ui.primula.*;
import edu.ucla.belief.ace.PrimulaSystemSnapshot;

public class Primula extends JFrame implements PrimulaUIInt, ActionListener, ItemListener, KeyListener {

	public static final Color COLOR_YELLOW          = new Color(189, 187, 127);
	public static final Color COLOR_YELLOW_SELECTED = new Color(249, 245, 107);//58,57,98
	public static final Color COLOR_BLUE            = new Color(114, 122, 136);//218,17,53
	public static final Color COLOR_BLUE_SELECTED   = new Color(162, 195, 255);//218,36,100
	public static final Color COLOR_GREEN           = new Color(129, 166, 135);//129,22,65
	public static final Color COLOR_GREEN_SELECTED  = new Color(128, 255, 128);//129,93,90
	// public static final Color COLOR_RED             = new Color(189, 127, 127);//0,33,74
	public static final Color COLOR_RED             = new Color(159, 135, 135);//0, 15, 63
	public static final Color COLOR_RED_SELECTED    = new Color(255, 181, 181);//0, 29, 100

	private JMenuBar mb               = new JMenuBar();
	private JMenu editMenu            = new JMenu("Domain");
	private JMenuItem evModule        = new JMenuItem("Start Inference Module");
	// +Learn
	private JMenuItem lrnModule     = new JMenuItem("Start Learn Module");
	private JMenuItem loadOrdered     = new JMenuItem("Create OrderedStruc");
	private JMenuItem loadSparse      = new JMenuItem("Load Relational Structure");
	private JMenuItem startBavaria    = new JMenuItem("Start Bavaria");
	private JMenu runmenu             = new JMenu("Run");
	private JMenuItem constructCPTBN    = new JMenuItem("Construct Bayesian Network");
	private JMenuItem saveRBN   = new JMenuItem("Save RBN");
	private JMenuItem dataConvert   = new JMenuItem("Convert Relational Data");


	private JMenuItem itemInvokeSamIam= new JMenuItem("Open SamIam");
	private JMenuItem itemForgetAll   = new JMenuItem("Reset external software locations");
	private JMenuItem exit            = new JMenuItem("Exit");
	private JMenu optionsmenu         = new JMenu("Options");
	private JMenu rbnSystems          = new JMenu("Bayes Network System");
	private JMenu decMode             = new JMenu("Decompose Mode");
	private JMenu helpmenu            = new JMenu("Help");
	private JMenuItem itemabout           = new JMenuItem("About Primula");
	//    private JMenu sampleOrd            = new JMenu("Sampling Order");
	private static JTextArea messageArea  = new JTextArea(15, 20);
	private JLabel rstsrcLabel            = new JLabel("Domain source:");
	private JLabel rstsrc                 = new JLabel("");
	private JLabel rbnsrcLabel            = new JLabel("Model source:");
	private JLabel bnoutLabel             = new JLabel("BN output:");
	private JTextField rbnfilename        = new JTextField(15);
	private JTextField bnoutfilename      = new JTextField(15);
	private JButton loadRBN               = new JButton("Browse");
	private JButton saveBN                = new JButton("Browse");
	private JScrollPane scrollPane        = new JScrollPane();
	private JFileChooser bnetFileChooser      = new JFileChooser( "." );
	private JFileChooser relmodelFileChooser      = new JFileChooser( "." );
	private JFileChooser domainFileChooser      = new JFileChooser( "." );
	private javax.swing.filechooser.FileFilter myFilterRST, myFilterRBN; //keith cascio 20061201
	private javax.swing.filechooser.FileFilter myFilterRDEF, myFilterMLN, myFilterPL, myFilterDB,myFilterFOIL;
	private String messages               = "";
	private static boolean isBavariaOpen  = false;
	private static boolean isEvModuleOpen = false;
	private static boolean isLrnModuleOpen = false;
	private boolean strucEdited           = false; //edited in bavaria

	private JPanel srcLabels      = new JPanel(new GridLayout(3, 1));
	private JPanel rbnInputFields = new JPanel(new BorderLayout());
	private JPanel bnoutInputFields  = new JPanel(new BorderLayout());
	private JPanel inputFields    = new JPanel(new GridLayout(3, 1));
	private JPanel southPanel     = new JPanel(new BorderLayout());

	public static final int CF_NOR = 0;
	public static final int CF_MEAN = 1;
	public static final int CF_INVSUM = 2;
	public static final int CF_ESUM = 3;

	public static final int OPTION_NOT_EVIDENCE_CONDITIONED = 0;
	public static final int OPTION_EVIDENCE_CONDITIONED     = 1;
	public static final int OPTION_NOT_QUERY_SPECIFIC       = 0;
	public static final int OPTION_QUERY_SPECIFIC           = 1;

	public static final int OPTION_DECOMPOSE  = 0;
	public static final int OPTION_DECOMPOSE_DETERMINISTIC        = 1;
	public static final int OPTION_NOT_DECOMPOSE  = 2;
	public static final int OPTION_NOT_ELIMINATE_ISOLATED_ZERO_NODES  = 0;
	public static final int OPTION_ELIMINATE_ISOLATED_ZERO_NODES      = 1;
	public static final int OPTION_NO_LAYOUT = 0;
	public static final int OPTION_LAYOUT = 1;
	public static final int OPTION_JAVABAYES                = 0;
	public static final int OPTION_HUGIN                    = 1;
	public static final int OPTION_NETICA                   = 2;
	public static final int OPTION_SAMIAM                   = 3;

	private JRadioButtonMenuItem javaBayes;
	private JRadioButtonMenuItem hugin;
	private JRadioButtonMenuItem netica;
	private JRadioButtonMenuItem samiam;
	private JRadioButtonMenuItem decnone;
	private JRadioButtonMenuItem decstandard;
	private JRadioButtonMenuItem decdet;

	private JCheckBoxMenuItem querySpecific;
	private JCheckBoxMenuItem evidenceConditioned;
	private JCheckBoxMenuItem layoutItem;
	private JCheckBoxMenuItem eliminateIsolatedZeroNodes;


	protected int querymode = OPTION_NOT_QUERY_SPECIFIC ;
	protected int evidencemode = OPTION_EVIDENCE_CONDITIONED;
	protected int decomposemode = OPTION_DECOMPOSE;
	protected int isolatedzeronodesmode = OPTION_ELIMINATE_ISOLATED_ZERO_NODES;
	protected int layoutmode = OPTION_LAYOUT;
	protected int bnsystem = OPTION_SAMIAM;

	private final String STRUCTURE_MODIFIED = "Current structure modified. Continue?";
	private final String INST_AND_QUERIES_LOST = "This action will cause current instantiations and queries to be lost. Continue?";

	/** @author keith cascio
	@since  20061105 */
	public static final String  STR_OPTION_DEBUG = "debug";
	public static       boolean FLAG_DEBUG       = false;

	protected File srsfile;
	protected File rbnfile;
	protected File bnoutfile;
	protected EvidenceModule evidenceModule;
	// +Learn
	protected LearnModule learnModule;
	protected Bavaria bavaria;
	protected RelStruc rels;
	protected RBN rbn;
	protected Instantiation inst = new Instantiation();
	protected AtomList queryatoms = new AtomList();

	/** @author keith cascio
	@since 20060728 */
	public RBN getRBN(){
		return Primula.this.rbn;
	}

	/** @author keith cascio
	@since 20060728 */
	public RelStruc getRels(){
		return Primula.this.rels;
	}

	/** @author keith cascio
	@since 20060515 */
	public PrimulaSystemSnapshot snapshot(){
		if( (this.rbn == null) || (this.rels == null) ) return null;

		PrimulaSystemSnapshot ret = new PrimulaSystemSnapshot(
				this.rbn,
				this.rels,
				this.inst,
				this.queryatoms,
				this.srsfile,
				this.rbnfile,
				this.bnoutfile,
				this.querymode,
				this.evidencemode,
				this.decomposemode,
				this.isolatedzeronodesmode,
				this.layoutmode,
				this.bnsystem,
				this.getPreferences().getACESettings()
		);
		return ret;
	}

	/** @author keith cascio
	@since  20061201 */
	public void setDecomposeMode( int mode ){
		Primula.this.decomposemode = mode;
	}

	private SamiamManager mySamiamManager;
	private boolean myFlagSystemExitEnabled = true;
	public static final String STR_FILENAME_LOGO = "small_logo.jpg";
	private Preferences myPreferences;
	private JMenuItem btnDebugAceCompile = new JMenuItem( "DEBUG ace compile" );

	/**
       @author Keith Cascio
       @since 040804
	 */
	public void setTheSamIamUI( edu.ucla.belief.ui.primula.SamiamUIInt ui ){
		//THE_SAMIAM_UI = ui;
		getSamiamManager().setSamiamUIInstance( ui );
	}

	/**
       @author Keith Cascio
       @since 050404
	 */
	public SamiamManager getSamiamManager(){
		if( mySamiamManager == null ) mySamiamManager = new SamiamManager( this );
		return mySamiamManager;
	}

	/** @author keith cascio
	@since 20060602 */
	public void forgetAll(){
		if( myPreferences != null ) myPreferences.forgetAll();
		if( mySamiamManager != null ) mySamiamManager.forgetAll();
		if( evidenceModule != null ) evidenceModule.forgetAll();
	}

	/**
       @author Keith Cascio
       @since 040804
	 */
	public JFrame asJFrame(){
		return this;
	}

	/**
       @arg flag Sets whether the JVM should terminate when the user closes Primula.  Set this to false if you call Primula from another Java program and you want to prevent Java from exiting when the user exits Primula.
       @since 040804
	 */
	public void setSystemExitEnabled( boolean flag ){
		myFlagSystemExitEnabled = flag;
	}

	/**
       @ret true if a user action that closes Primula will cause the JVM to terminate as well.
       @since 040804
	 */
	public boolean isSystemExitEnabled(){
		return myFlagSystemExitEnabled;
	}

	/**
       @author Keith Cascio
       @since 040804
	 */
	public void exitProgram(){
		myPreferences.saveOptionsToFile();
		if( isSystemExitEnabled() ) System.exit( 0 );
		else setVisible( false );
	}

	/**
       @author Keith Cascio
       @since 040804
	 */
	private void init()
	{
		//ImageIcon icon = new ImageIcon("small_logo.jpg");
		ImageIcon icon = getIcon( STR_FILENAME_LOGO );

		if( icon.getImageLoadStatus() == MediaTracker.COMPLETE ){//image ok
			this.setIconImage(icon.getImage());
		}
		this.setTitle("Primula");
		this.pack();

		myPreferences = new Preferences( true );
	}

	/**
       @author Keith Cascio
       @since 042104
	 */
	public ImageIcon getIcon( String fileName ){
		ClassLoader myLoader = this.getClass().getClassLoader();
		java.net.URL urlImage = myLoader.getResource( fileName );
		if( urlImage == null ){
			System.err.println( "Warning: loader.getResource(\""+fileName+"\") failed." );
			return new ImageIcon( fileName );
		}
		else return new ImageIcon( urlImage );
	}

	/**
       @author Keith Cascio
       @since 040504
	 */
	public String makeNetworkName()
	{
		if( bnoutfile == null ) return makeAlternateName();
		else return bnoutfile.getPath();
	}

	/**
       @author Keith Cascio
       @since 040504
	 */
	public String makeAlternateName()
	{
		String strRST = (rstsrc == null ) ? "no_RST" : rstsrc.getText();
		if( strRST.length() < (int)1 ) strRST = "no_RST";
		String strRBN = (rbnfilename == null ) ? "no_RBN" : rbnfilename.getText();
		if( strRBN.length() < (int)1 ) strRBN = "no_RBN";

		return pluckNameFromPath( strRST ) + "_" + pluckNameFromPath( strRBN ) + ".net";
	}

	/**
       @author Keith Cascio
       @since 040504
	 */
	public static String pluckNameFromPath( String path )
	{
		int index0 = path.lastIndexOf( File.separator );
		if( index0 < (int)0 ) index0 = (int)0;
		else ++index0;
		int index1 = path.lastIndexOf( "." );
		if( index1 < (int)0 ) index1 = path.length();
		return path.substring( index0, index1 );
	}

	/**
       @author Keith Cascio
       @since 040804
	 */
	public edu.ucla.belief.ui.primula.SamiamUIInt getSamIamUIInstanceThis(){
		//edu.ucla.belief.ui.primula.SamiamUIInt ui = getSamIamUIInstance();
		//if( ui != null ) ui.setPrimulaUIInstance( this );
		//return ui;
		return getSamiamManager().getSamIamUIInstance();
	}

	/**
       @author Keith Cascio
       @since 050404
	 */
	public Preferences getPreferences(){
		return myPreferences;
	}

	public Primula()
	{
		//Creates the menus

		editMenu.add(loadOrdered);
		editMenu.add(loadSparse);
		editMenu.add(startBavaria);
		mb.add(editMenu);
		runmenu.add(evModule);
		// +Learn
		runmenu.add(lrnModule);
		runmenu.add(constructCPTBN);
		runmenu.add(saveRBN);
		runmenu.add(dataConvert);
		runmenu.add(itemInvokeSamIam);
		if( Primula.FLAG_DEBUG ) runmenu.add( btnDebugAceCompile );//keith cascio 20060516
		runmenu.add(exit);
		mb.add(runmenu);

		ButtonGroup rbnGroup = new ButtonGroup();
		javaBayes = new JRadioButtonMenuItem("Java Bayes");
		//javaBayes.setSelected(true);
		rbnGroup.add(javaBayes);
		rbnSystems.add(javaBayes);
		hugin = new JRadioButtonMenuItem("Hugin");
		rbnGroup.add(hugin);
		rbnSystems.add(hugin);
		netica = new JRadioButtonMenuItem("Netica");
		rbnGroup.add(netica);
		rbnSystems.add(netica);
		samiam = new JRadioButtonMenuItem("To SamIam");
		samiam.setSelected(true);
		rbnGroup.add(samiam);
		rbnSystems.add(samiam);

		ButtonGroup decmGroup = new ButtonGroup();
		decnone =  new JRadioButtonMenuItem("none");
		decmGroup.add(decnone);
		decMode.add(decnone);
		decstandard =  new JRadioButtonMenuItem("normal");
		decstandard.setSelected(true);
		decmGroup.add(decstandard);
		decMode.add(decstandard);
		decdet =  new JRadioButtonMenuItem("deterministic");
		decmGroup.add(decdet);
		decMode.add(decdet);


		JMenu inferenceOptions = new JMenu("Construction Mode");
		querySpecific = new JCheckBoxMenuItem("Query specific");
		inferenceOptions.add(querySpecific);
		evidenceConditioned = new JCheckBoxMenuItem("Evidence conditioned");
		evidenceConditioned.setSelected(true);
		inferenceOptions.add(evidenceConditioned);
		//     JCheckBoxMenuItem deterministicNetwork = new JCheckBoxMenuItem("Build deterministic BN",false);
		//     inferenceOptions.add(deterministicNetwork);
		layoutItem = new JCheckBoxMenuItem("Skip layout",false);
		inferenceOptions.add(layoutItem);
		eliminateIsolatedZeroNodes = new JCheckBoxMenuItem("Show isolated prob.0 nodes",false);
		inferenceOptions.add(eliminateIsolatedZeroNodes);
		//	adaptiveSampling = new JCheckBoxMenuItem("sample adaptive",false);
		// inferenceOptions.add(adaptiveSampling);


		inferenceOptions.add(decMode);
		//	inferenceOptions.add(sampleOrd);

		optionsmenu.add(rbnSystems);
		optionsmenu.add(inferenceOptions);
		optionsmenu.add(itemForgetAll);
		itemForgetAll.setToolTipText( "Forget the locations of all external software dependencies, i.e. samiam, inflib, ace, etc." );
		mb.add(optionsmenu);
		setJMenuBar(mb);

		helpmenu.add(itemabout);
		mb.add(helpmenu);

		myFilterRST = new Filter_rst() ;
		myFilterRDEF = new Filter_rdef();
		myFilterPL = new Filter_pl() ;
		myFilterFOIL = new Filter_foil() ;
		myFilterDB = new Filter_db(false); 
		myFilterRBN = new Filter_rbn(); 
		myFilterMLN = new Filter_mln();
		
		domainFileChooser.addChoosableFileFilter( myFilterRST);
		domainFileChooser.addChoosableFileFilter( myFilterRDEF );
		domainFileChooser.addChoosableFileFilter( myFilterPL);
		domainFileChooser.addChoosableFileFilter( myFilterRDEF);
		domainFileChooser.addChoosableFileFilter( myFilterFOIL);
		
		relmodelFileChooser.addChoosableFileFilter( myFilterRBN);
		relmodelFileChooser.addChoosableFileFilter(myFilterMLN );
		bnetFileChooser.addChoosableFileFilter(new Filter_bif());
		bnetFileChooser.addChoosableFileFilter(new Filter_net());
		bnetFileChooser.addChoosableFileFilter(new Filter_dne());
		
		bnetFileChooser.setAcceptAllFileFilterUsed( true );
		domainFileChooser.setAcceptAllFileFilterUsed( true );
		relmodelFileChooser.setAcceptAllFileFilterUsed( true );
		
		//actionlistener for loading ordered strucs
		loadOrdered.addActionListener( this );


		//actionlistener for loading sparserelstrucs from the file
		loadSparse.addActionListener( this );


		//actionlistener for loading rbn from the file (via browse-button)
		loadRBN.addActionListener( this );
		//keylistener for loading rbn from the file (via textfield)
		rbnfilename.addKeyListener( this );

		//actionlistener for choosing bn-output file (via browse-button)
		saveBN.addActionListener( this );


		//keylistener for for choosing bn-output file (via textfield)
		bnoutfilename.addKeyListener( this );

		//actionlistener for starting the Bavaria editor
		startBavaria.addActionListener( this );


		//actionlistener for constructing standard BN
		constructCPTBN.addActionListener( this );
		
		//actionlistener for saving the RBN
		saveRBN.addActionListener( this );
		
		dataConvert.addActionListener( this );
		itemInvokeSamIam.addActionListener( this );
		itemForgetAll.addActionListener( this );

		//actionlistener for opening the evidence module
		evModule.addActionListener( this );
		// +Learn
		lrnModule.addActionListener( this );

		javaBayes.addItemListener( this );

		hugin.addItemListener( this );

		netica.addItemListener( this );
		samiam.addItemListener( this );

		decnone.addItemListener( this );

		decstandard.addItemListener( this );
		decdet.addItemListener( this );
		querySpecific.addItemListener( this );
		evidenceConditioned.addItemListener( this );

		eliminateIsolatedZeroNodes.addItemListener( this );
		//	adaptiveSampling.addItemListener( this );
		layoutItem.addItemListener( this );

		//actionlistener for exiting the program
		exit.addActionListener( this );

		btnDebugAceCompile.addActionListener( this );

		itemabout.addActionListener( this );

		//creating the layout
		srcLabels.add(rstsrcLabel);
		srcLabels.add(rbnsrcLabel);
		srcLabels.add(bnoutLabel);

		rbnfilename.setBackground(Color.white);
		rbnInputFields.add(rbnfilename, BorderLayout.CENTER);
		rbnInputFields.add(loadRBN, BorderLayout.EAST);

		bnoutfilename.setBackground(Color.white);
		bnoutInputFields.add(bnoutfilename, BorderLayout.CENTER);
		bnoutInputFields.add(saveBN, BorderLayout.EAST);

		rstsrc.setForeground(Color.black);
		inputFields.add(rstsrc);
		inputFields.add(rbnInputFields);
		inputFields.add(bnoutInputFields);

		southPanel.add(srcLabels, BorderLayout.WEST);
		southPanel.add(inputFields, BorderLayout.CENTER);

		scrollPane.getViewport().add(messageArea);
		messageArea.setEditable(false);

		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(scrollPane, BorderLayout.CENTER);
		contentPane.add(southPanel, BorderLayout.SOUTH);


		//inner class for closing the window
		this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						exitProgram();
					}
				}
		);

		this.init();
	}

	public void actionPerformed( ActionEvent e ) {
		Object source = e.getSource();
		if( source  == loadOrdered ){
			if (!isBavariaOpen){
				final JDialog sizewin      = new JDialog(Primula.this, true);
				final JTextField sizeField = new JTextField(5);
				JLabel sizeLabel           = new JLabel("Enter size: ");
				JButton ok                 = new JButton("Ok");
				JButton cancel             = new JButton("Cancel");

				JPanel size                = new JPanel(new BorderLayout());
				JPanel buttons             = new JPanel(new GridLayout(1, 2));

				size.add(sizeLabel, BorderLayout.WEST);
				size.add(sizeField, BorderLayout.CENTER);
				buttons.add(ok);
				buttons.add(cancel);

				ok.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						try{
							int dom = (new Integer(sizeField.getText())).intValue();
							if (dom >= 1){
								if(inst.isEmpty() && queryatoms.isEmpty()){
									if(strucEdited){
										if(confirm(STRUCTURE_MODIFIED)){
											newOrdStruc(dom);
											sizewin.dispose();
										}
										else
											sizewin.dispose();
									}
									else{
										newOrdStruc(dom);
										sizewin.dispose();
									}
								}
								else{
									if(confirm(INST_AND_QUERIES_LOST)){
										if(strucEdited){
											if(confirm(STRUCTURE_MODIFIED)){
												newOrdStruc(dom);
												sizewin.dispose();
											}
											else
												sizewin.dispose();
										}
										else{
											newOrdStruc(dom);
											sizewin.dispose();
										}
									}
									else
										sizewin.dispose();
								}
							}
							else
								sizeField.setText("");
						}catch (Exception ex){
							sizeField.setText("");
						}
					}
				});
				cancel.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e){
						sizewin.dispose();
					}
				});

				Container contentPane = sizewin.getContentPane();
				contentPane.setLayout(new BorderLayout());
				contentPane.add(size, BorderLayout.CENTER);
				contentPane.add(buttons, BorderLayout.SOUTH);

				sizewin.pack();
				sizewin.setLocation(Primula.this.getLocation());
				sizewin.show();
			}
			else
				this.showMessage("Please, close the Bavaria window first");
		}
		else if( source == saveBN ){
			int value = bnetFileChooser.showDialog(Primula.this, "Select");
			if (value == JFileChooser.APPROVE_OPTION){
				bnoutfile = bnetFileChooser.getSelectedFile();
				bnoutfilename.setText(bnoutfile.getPath());
			}
		}
		else if( source == loadSparse ){
			//actionlistener for loading sparserelstrucs from the file
			if (!isBavariaOpen){
				domainFileChooser.setFileFilter( myFilterRST );
				int value = domainFileChooser.showDialog(Primula.this, "Load");
				if (value == JFileChooser.APPROVE_OPTION) {
					srsfile = domainFileChooser.getSelectedFile();
					if(inst.isEmpty() && queryatoms.isEmpty()){
						if(strucEdited){
							if(confirm(STRUCTURE_MODIFIED))
								loadSparseRelFile(srsfile);
						}
						else
							loadSparseRelFile(srsfile);
					}
					else{
						if(confirm(INST_AND_QUERIES_LOST)){
							if(strucEdited){
								if(confirm(STRUCTURE_MODIFIED))
									loadSparseRelFile(srsfile);
							}
							else
								loadSparseRelFile(srsfile);
						}
					}
				}
			}
			else
				this.showMessage("Please, close the Bavaria window first\nor use the Bavaria load command");
		}
		else if( source == loadRBN ){
			relmodelFileChooser.resetChoosableFileFilters();
			relmodelFileChooser.addChoosableFileFilter(myFilterRBN);
			relmodelFileChooser.addChoosableFileFilter(myFilterMLN);
			relmodelFileChooser.setFileFilter(myFilterRBN);
			int value = relmodelFileChooser.showDialog(Primula.this, "Load");
			if (value == JFileChooser.APPROVE_OPTION) {
				File selectedFile = relmodelFileChooser.getSelectedFile();
				if (myFilterRBN.accept(selectedFile))
						loadRBNFunction(selectedFile);
					else if (myFilterMLN.accept(selectedFile)){
						File mlnFile = selectedFile;
						relmodelFileChooser.resetChoosableFileFilters();
						Filter_db cwdbFilter = new Filter_db(false);
						relmodelFileChooser.addChoosableFileFilter(cwdbFilter);
						relmodelFileChooser.setFileFilter(cwdbFilter);
						value = relmodelFileChooser.showDialog(Primula.this, "Load domain data");

						File cwdbfile = null;
						if (value == JFileChooser.APPROVE_OPTION) 
							cwdbfile = relmodelFileChooser.getSelectedFile();

						relmodelFileChooser.resetChoosableFileFilters();
						Filter_db owdbFilter = new Filter_db(true);
						relmodelFileChooser.addChoosableFileFilter(owdbFilter);
						value = relmodelFileChooser.showDialog(Primula.this, "Load evidence data");
						File owdbfile = null;
						if (value == JFileChooser.APPROVE_OPTION) 
							owdbfile = relmodelFileChooser.getSelectedFile();

						this.LoadMLN(mlnFile,cwdbfile,owdbfile);
						if (cwdbfile != null)
							this.rstsrc.setText(" "+cwdbfile.getName());
						else
							this.rstsrc.setText(" "+mlnFile.getName());

					}/* end extension mln */
				}
		}
		else if( source == startBavaria ){
			//actionlistener for starting the Bavaria editor
			//rst loaded from file
			if(rels != null && rels instanceof SparseRelStruc && !isBavariaOpen && srsfile != null){
				SparseRelStruc temp = (SparseRelStruc)rels;
				if(temp.getCoords().size() == 0)
					temp.createCoords();
				bavaria = new Bavaria(temp, srsfile, Primula.this, strucEdited);
				isBavariaOpen = true;
				rstsrc.setText("Bavaria RelStruc Editor");
			}

			//not a rst file (ordstruc)
			else if (rels != null && rels instanceof OrdStruc){
				if(inst.isEmpty() && queryatoms.isEmpty()){
					rels = new SparseRelStruc();
					new Bavaria((SparseRelStruc)rels, Primula.this, strucEdited);
					isBavariaOpen = true;
					rstsrc.setText("Bavaria RelStruc Editor");
					if(isEvModuleOpen)
						evidenceModule.newElementNames();
				}
				else{
					if(confirm(INST_AND_QUERIES_LOST)){
						rels = new SparseRelStruc();
						new Bavaria((SparseRelStruc)rels, Primula.this, strucEdited);
						isBavariaOpen = true;
						rstsrc.setText("Bavaria RelStruc Editor");
						if(isEvModuleOpen)
							evidenceModule.newElementNames();
						else{
							inst.reset();
							queryatoms.reset();
						}
					}
				}
			}

			//rst file created in Bavaria
			else if (rels != null && rels instanceof SparseRelStruc && !isBavariaOpen && srsfile == null){
				new Bavaria((SparseRelStruc)rels, null, Primula.this, strucEdited);
				isBavariaOpen = true;
				rstsrc.setText("Bavaria RelStruc Editor");
			}

			//create a new rst file
			else if (rels == null && !isBavariaOpen){
				rels = new SparseRelStruc();
				new Bavaria((SparseRelStruc)rels, Primula.this, strucEdited);
				isBavariaOpen = true;
				rstsrc.setText("Bavaria RelStruc Editor");
			}
		}
		else if( source == constructCPTBN ){
			//actionlistener for constructing standard BN
			boolean nogo = false;
			String message = "";
			if (rbn == null){
				nogo = true;
				message = message + " Please load rbn first";
			}
			if (rels == null){
				nogo = true;
				message = message + " Please load RelStruc first";
			}
			if (rbn != null){
				if (!rbn.multlinOnly() & decomposemode != OPTION_NOT_DECOMPOSE){
					nogo = true;
					message = message + " Please choose decompose:none for rbn containing non multilinear comb. functions";
				}
			}
			if(!nogo){
				try {
					BayesConstructor constructor = null;
					if( bnsystem == OPTION_SAMIAM )
						constructor = new BayesConstructor(Primula.this ,
								inst,
								queryatoms,
								makeNetworkName());

					else constructor = new BayesConstructor(rbn,
							rels,
							inst,
							queryatoms,
							bnoutfile);
					constructor.constructCPTNetwork(evidencemode,
							querymode,
							decomposemode,
							isolatedzeronodesmode,
							layoutmode,
							bnsystem);
				}
				catch(RBNCyclicException ex) {this.showMessage(ex.toString());}
				catch(RBNCompatibilityException ex){this.showMessage(ex.toString());}
				catch(RBNIllegalArgumentException ex){this.showMessage(ex.toString());};
			}
			else this.showMessage(message);
		}
		else if (source == saveRBN){
			File rbnfile;
			relmodelFileChooser.setFileFilter(myFilterRBN);
			int value = relmodelFileChooser.showDialog(Primula.this, "Save");
			if (value == JFileChooser.APPROVE_OPTION) {
				rbnfile = relmodelFileChooser.getSelectedFile();
				rbn.saveToFile(rbnfile);
			}
		}
		else if (source == dataConvert){
			File sourcefile;
			File targetfile;
			RelData rdata = new RelData(); 
			
			domainFileChooser.resetChoosableFileFilters();
//			domainFileChooser.addChoosableFileFilter(myFilterRDEF);
			domainFileChooser.addChoosableFileFilter(myFilterPL);
			int value = domainFileChooser.showDialog(Primula.this, "Load");
			if (value == JFileChooser.APPROVE_OPTION) {
				try{
					sourcefile = domainFileChooser.getSelectedFile();
//					if (myFilterRDEF.accept(sourcefile)){
//						RDEFReader rdefreader = new RDEFReader();
//						rdata = (RelData)rdefreader.readRDEF(sourcefile.getPath(),null);
//					}
					if (myFilterPL.accept(sourcefile)){
						AtomListReader alreader = new AtomListReader();
						rdata = alreader.readAL(sourcefile);
					}
				}
				catch (Exception ex){ ex.printStackTrace();}
			}
			domainFileChooser.resetChoosableFileFilters();
			domainFileChooser.addChoosableFileFilter(myFilterRDEF);
//			domainFileChooser.addChoosableFileFilter(myFilterFOIL);	
			value = domainFileChooser.showDialog(Primula.this, "Save");
			if (value == JFileChooser.APPROVE_OPTION) {
				try{
					targetfile = domainFileChooser.getSelectedFile();
					if (myFilterRDEF.accept(targetfile))
							rdata.saveToRDEF(targetfile);
//					if (myFilterFOIL.accept(targetfile))
//						rdata.saveToFOIL(targetfile);

				}
				catch (Exception ex){ ex.printStackTrace();}
			}	
		}
		else if( source == itemInvokeSamIam ){
			//SamiamUIInt ui = getSamIamUIInstanceThis();
			//if( ui != null ) ui.asJFrame().setVisible( true );
			getSamiamManager().openSamiam();
		}
		else if( source == itemForgetAll ){
			Primula.this.forgetAll();
		}
		else if( source == evModule ){
			//actionlistener for opening the evidence module
			if(!isEvModuleOpen){
				evidenceModule = new EvidenceModule(this
				);
				isEvModuleOpen = true;
			}
		}
		// +Learn
		else if( source == lrnModule ){
			//actionlistener for opening the learn module
			if(!isLrnModuleOpen){
				learnModule = new LearnModule(this);
				isLrnModuleOpen = true;
			}
		}
		else if( source == exit ){
			if(strucEdited){
				if(confirm(STRUCTURE_MODIFIED) == true)
					exitProgram();
			}
			else
				exitProgram();
		}
		else if( source == btnDebugAceCompile ){
			//String pathRST = "./blockmap_large.rst";
			//String pathRBN = "./randblock_trans.rbn";
			String pathRST = "./holmes_2.rst";
			String pathRBN = "./holmes_2.rbn";
			loadSparseRelFile( srsfile = new File( pathRST ) );
			loadRBNFunction(             new File( pathRBN ) );
			evModule.doClick();
			//evidenceModule.aceCompile();
			//evidenceModule.getACEControl().getActionCompile().actionPerformed( null );
		}
		else if( source == itemabout ){
			JOptionPane.showMessageDialog(null,"Primula version 2.2 " + '\n' + "(C) 2009");
		}
	}

	public void keyPressed(KeyEvent e){
		//Invoked when a key has been pressed.
		Object source = e.getSource();
		if( source == rbnfilename ){
			char c = e.getKeyChar();
			if(c == KeyEvent.VK_ENTER){
				loadRBNFunction(new File(rbnfilename.getText()));
			}
		}
		else if( source == bnoutfilename ){
			//keylistener for for choosing bn-output file (via textfield)
			char c = e.getKeyChar();
			if(c == KeyEvent.VK_ENTER){
				bnoutfile = new File(bnoutfilename.getText());
			}
		}
	}
	public void keyReleased(KeyEvent e){
		//Invoked when a key has been released.
	}
	public void keyTyped(KeyEvent e){
		//Invoked when a key has been typed.
	}


	//keylistener for loading rbn from the file (via textfield)
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if( source == javaBayes ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				bnsystem = OPTION_JAVABAYES;
		}
		else if( source == hugin ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				bnsystem = OPTION_HUGIN;
		}
		else if( source == netica ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				bnsystem = OPTION_NETICA;
		}
		else if( source == samiam ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				bnsystem = OPTION_SAMIAM;
		}
		else if( source == decnone ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				decomposemode = OPTION_NOT_DECOMPOSE;
		}
		else if( source == decstandard ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				decomposemode = OPTION_DECOMPOSE;
		}
		else if( source == decdet ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				decomposemode = OPTION_DECOMPOSE_DETERMINISTIC;
		}
//		else if( source == ordforward ){
//		if (e.getStateChange() == ItemEvent.SELECTED){
//		sampleordmode = OPTION_SAMPLEORD_FORWARD;
//		if(isEvModuleOpen)
//		evidenceModule.newSampleordMode(sampleordmode);
//		}
//		}
//		else if( source == ordripple ){
//		if (e.getStateChange() == ItemEvent.SELECTED){
//		sampleordmode = OPTION_SAMPLEORD_RIPPLE;
//		if(isEvModuleOpen)
//		evidenceModule.newSampleordMode(sampleordmode);
//		}
//		}
		else if( source == querySpecific){
			if (e.getStateChange() == ItemEvent.SELECTED)
				querymode = OPTION_QUERY_SPECIFIC;
			else querymode = OPTION_NOT_QUERY_SPECIFIC;
		}
		else if( source == evidenceConditioned ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				evidencemode = OPTION_EVIDENCE_CONDITIONED;
			else evidencemode = OPTION_NOT_EVIDENCE_CONDITIONED;
		}
		else if( source == eliminateIsolatedZeroNodes ) {
			if (e.getStateChange() == ItemEvent.SELECTED)
				isolatedzeronodesmode = OPTION_NOT_ELIMINATE_ISOLATED_ZERO_NODES;
			else isolatedzeronodesmode = OPTION_ELIMINATE_ISOLATED_ZERO_NODES;
		}
//		else if( source == adaptiveSampling ){
//		if (e.getStateChange() == ItemEvent.SELECTED)
//		adaptivemode = OPTION_SAMPLE_ADAPTIVE;
//		else adaptivemode = OPTION_NOT_SAMPLE_ADAPTIVE;
//		if(isEvModuleOpen)
//		evidenceModule.newAdaptiveMode(adaptivemode);
//		}
		else if( source == layoutItem ){
			if (e.getStateChange() == ItemEvent.SELECTED)
				layoutmode = OPTION_NO_LAYOUT;
			else layoutmode = OPTION_LAYOUT;
		}

	}

	//creates a new ordered structure
	public void newOrdStruc(int dom){
		rels = new OrdStruc(dom);
		strucEdited = false;
		srsfile = null;
		if(isEvModuleOpen)
			evidenceModule.newElementNames();
		else{
			inst.reset();
			queryatoms.reset();
		}
		rstsrc.setText("Ordered Structure size "+dom);
	}


	//loads the sparserel structure from file
	public void loadSparseRelFile(File srsfile){
		
		strucEdited = false;
		try{
			Rel.resetTheColorCounters();
			if (myFilterRST.accept(srsfile)){
				SparseRelStrucReader relreader = new SparseRelStrucReader();
				rels = relreader.readSparseRelStrucFromFile(srsfile.getPath());
			}
			else if (myFilterRDEF.accept(srsfile)){
				RDEFReader rdefreader = new RDEFReader();
				RelData rdata = rdefreader.readRDEF(srsfile.getPath(),null);
				rels = new SparseRelStruc(rdata.caseAt(0).inputDomain());
			}
			rstsrc.setText(srsfile.getName());
		}catch (Exception ex){
			rels = null;
			srsfile = null;
			rstsrc.setText("");
			this.showMessage(ex.toString());
		}
		if(isEvModuleOpen)
			evidenceModule.newElementNames();
		else{
			inst.reset();
			queryatoms.reset();
		}
	}


	//loads the rbn file
	public void loadRBNFunction(File input_file){

		if(inst.isEmpty() && queryatoms.isEmpty()){

			rbn = new RBN(input_file.getPath());
			rbnfile = input_file;
			rbnfilename.setText(rbnfile.getPath());

			Rel.resetTheColorCounters();
			if(isEvModuleOpen)
				evidenceModule.updateRBNRelations();
		}
		else{
			if(confirm(INST_AND_QUERIES_LOST)){
				try{
					rbn = new RBN(input_file.getPath());
					rbnfile = input_file;
					rbnfilename.setText(rbnfile.getPath());
				}catch (Exception ex){
					rbn = null;
					rbnfile = null;
					rbnfilename.setText("");
					this.showMessage(ex.toString());
				}
				Rel.resetTheColorCounters();
				inst.reset();
				queryatoms.reset();
				if(isEvModuleOpen)
					evidenceModule.updateRBNRelations();
//				else{
//					inst.reset();
//					queryatoms.reset();
//				}
			}
			else //replace the current text with the real filename
				rbnfilename.setText(rbnfile.getPath());
		}
	}


	/** @author Keith Cascio */
	public void showMessageThis(String message){
		showMessage( message );
	}

	/** @author Keith Cascio */
	public void appendMessageThis(String message){
		appendMessage( message );
	}

	/** @author Keith Cascio */
	public void setIsBavariaOpenThis(boolean b){
		setIsBavariaOpen( b );
	}

	/** @author Keith Cascio */
	public void setIsEvModuleOpenThis(boolean b){
		setIsEvModuleOpen( b );
	}

	
	//shows the messages
	public static void showMessage(String message){
		messageArea.append("\n"+message);
		messageArea.repaint((long)10.00);
	}

	// without starting a newline:
	public static void appendMessage(String message){
		messageArea.append(message);
		messageArea.repaint((long)10.00);
	}

	//sets the state of the Bavaria window
	public static void setIsBavariaOpen(boolean b){
		isBavariaOpen = b;
	}

//	public  int adaptivemode(){
//	return adaptivemode;
//	}

	//sets the state of the evidence module window
	public static void setIsEvModuleOpen(boolean b){
		isEvModuleOpen = b;
	}

	//sets the state of the evidence module window
	public static void setIsLearnModuleOpen(boolean b){
		isLrnModuleOpen = b;
	}


	//sets the current rel struc
	public void setRelStruc(RelStruc srel){
		rels = srel;
		if(isEvModuleOpen)
			evidenceModule.newElementNames();
		else{
			inst.reset();
			queryatoms.reset();
		}
		if (isBavariaOpen)
			bavaria.setRelStruc((SparseRelStruc)srel);
	}


	//sets the current input file
	public void setInputFile(File inputFile){
		if(inputFile == null)
			srsfile = null;
		else
			srsfile = inputFile;
	}


	//user adds or renames a node in the Bavaria window
	public void addOrRenameEvidenceModuleNode(){
		if(isEvModuleOpen)
			evidenceModule.addOrRenameElementName();
	}


	//user deletes a node in the Bavaria window
	public void deleteElementFromEvidenceModule(int node){
		if(isEvModuleOpen)
			evidenceModule.deleteElementName(node);
	}


	//ask confirmation
	public boolean confirm(String text){
		int result = JOptionPane.showConfirmDialog(this, text, "Confirmation", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION)
			return true;
		else //result == JOptionPane.NO_OPTION
			return false;
	}

	/** @author keith cascio
	@since 20061023 */
	public Instantiation getInstantiation(){
		return this.inst;
	}

	/** @author keith cascio
	@since 20061023 */
	public boolean instContainsAll( Instantiation old ){
		if( inst == null ) return (old == null) || old.isEmpty();
		else               return inst.containsAll( old );
	}

	//returns true if the instantiation is empty (used by Bavaria)
	public boolean isInstEmpty(){
		return inst.isEmpty();
	}

	//returns true if the atomlist is empty (used by Bavaria)
	public boolean isQueryatomsEmpty(){
		return queryatoms.isEmpty();
	}

	//user has edited the structure in the Bavaria
	public void setStrucEdited(boolean b){
		strucEdited = b;
		if( isEvModuleOpen ) evidenceModule.relationalStructureEdited();//keith cascio 20060725
	}


	public int evidencemode(){
		return evidencemode;
	}

	/** Opens Bavaria with the current rels */
//	public Bavaria openBavaria(){
//		return new Bavaria(new SparseRelStruc(), Primula.this, strucEdited);
//	}

	/**
	 * @author Alberto García Collado
	 * @param mln the file where the mln is stored
	 * @param owdb the file where the open world assuptions are declared
	 * @param cwdb the file where the close world assuptions are declared
	 */
	public void LoadMLN(File mln, File cwdb, File owdb) {
		MLNParserFacade facade = new MLNParserFacade();
		facade.ReadMLN(mln, cwdb, owdb);
		this.rbn = facade.getRBN();
		this.rels = facade.getRelStruc();
		this.inst = facade.getInstantiation();
		this.rbnfilename.setText(mln.getPath());
		if(isEvModuleOpen){
			evidenceModule.updateRBNRelations();
			evidenceModule.newElementNames();
			evidenceModule.updateInstantiationList();
		}
	}


	public void updateMessageArea(){
		messageArea.repaint();
	}
	
	public void setInst(Instantiation newinst){
		inst = newinst;
	}
	
	private void loadDefaults(){
		String rbninputfilestring = "/home/jaeger/B/Primula-Develop/New/Primula-beta/Examples2.2/RBNinputs/nested_combfunc_param.rbn";
		String rstinputfilestring = "/home/jaeger/B/Primula-Develop/New/Primula-beta/Examples2.2/RDEFinputs/randgraph_col_100nodes.rdef";

		srsfile = new File(rstinputfilestring);
		rbnfile = new File(rbninputfilestring);

		loadRBNFunction(rbnfile);
		loadSparseRelFile(srsfile);



		//bnoutfile = new File("/home/jaeger/B/Primula-Develop/New/Primula/Examples-beta/Huginoutputs/distributed.net");
	}

	
	public static void main( String[] args ){
		//System.out.println( "classpath: \"" + System.getProperty( "java.class.path" ) + "\"" );
		//System.out.println( "library path: \"" + System.getProperty( "java.library.path" ) + "\"" );
		for( String arg : args ){
			if( STR_OPTION_DEBUG.equals( arg ) ) FLAG_DEBUG = true;
		}

		Primula win = new Primula();
		SamiamManager.centerWindow( win );
		win.show();
//		win.loadDefaults();
	}
	

}
