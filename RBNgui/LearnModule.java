/*
* LearnModule.java 
* 
* Copyright (C) 2009 Aalborg University
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
import RBNio.*;
import RBNutilities.*;
import RBNLearning.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import java.util.*;


public class LearnModule extends JFrame implements ActionListener
{
	
	private JTabbedPane tabbedPane   = new JTabbedPane();
	
	private JPanel dataPanel = new JPanel(new GridLayout(2,1));
	private JPanel lowerlearnPanel = new JPanel(new GridLayout(2,1));
	private JPanel learnButtons = new JPanel(new FlowLayout());
	private JPanel sampleoptions = new JPanel(new GridLayout(2,1));
	private JPanel samplesizepanel = new JPanel(new FlowLayout());
	private JPanel percmisspanel = new JPanel(new FlowLayout());
	private JPanel restartspanel = new JPanel(new FlowLayout());
	private JPanel datasrcPanel = new JPanel(new FlowLayout());
	
	
	private JFileChooser fileChooser = new JFileChooser( "." );
	private javax.swing.filechooser.FileFilter myFilterRDEF;
	


	private JLabel samplesizelabel  = new JLabel("Sample size");
	private JLabel percmisslabel  = new JLabel("Percent missing");
	private JLabel restartlabel  = new JLabel("Restarts");

	
	private JTextField dataFileName        = new JTextField(15);
	private JTextField textsamplesize = new JTextField(3);
	private JTextField textpercmiss = new JTextField(3);
    private JTextField textnumrestarts = new JTextField(5);
    
	private JButton loadDataButton         = new JButton("Load");
	private JButton sampleDataButton       = new JButton("Sample");
	private JButton saveDataButton         = new JButton("Save");
	private JButton learnButton         = new JButton("Learn");
	private JButton stoplearnButton         = new JButton("Stop");
	private JButton setParamButton         = new JButton("Set");
	private JButton learnSettingButton         = new JButton("Settings");
	
	
	
	private JTable parametertable         = new JTable();
	private ParameterTableModel parammodel = new ParameterTableModel();
	private JScrollPane parameterScrollList = new JScrollPane();

	private JSplitPane learnsplitpane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,parameterScrollList,lowerlearnPanel);

	private Primula myprimula;
	private Primula mystaticprimula;
	private RelData data;
	private File datafile;
	private LearnSettings settingswindow;
	
	private boolean settingswindowopen;
	/* Parameters for the data sampling process */
	private int samplesize;
	private double percmiss;
	/* Parameters for the learning process */
	private int numchains;
	private int restarts;
	private int windowsize;
	private int maxfails;
	private int maxiterations;
	private double linedistancethresh;
	private double linelikelihoodthresh;
	private double gradientdistancethresh;
	private double paramratiothresh;
	private int omitrounds;
	private boolean verbose;
	private boolean aca;
	
	LearnThread lthread;
	
	public LearnModule(Primula mypr){

		myprimula = mypr;
		data = null;
		datafile = null;
		settingswindowopen = false;
		samplesize = 1;
		restarts = -1; /*-1 is for open-ended restarts */
		numchains = 3;
		windowsize = 5;
		maxfails = 5;
		maxiterations = 30;
		linedistancethresh = 0.01;
		linelikelihoodthresh = 0.001;
		gradientdistancethresh = 0.001;
		paramratiothresh = 0.03;
		omitrounds = 1;
		percmiss = 0.0;
		verbose = false;
		aca = false;
		
		fileChooser.addChoosableFileFilter(myFilterRDEF = new Filter_rdef());

		/* Data Tab */
		/* Data File Load */
		loadDataButton.addActionListener(this);
		loadDataButton.setBackground(Primula.COLOR_BLUE);
		sampleDataButton.addActionListener(this);
		sampleDataButton.setBackground(Primula.COLOR_RED);
		saveDataButton.addActionListener(this);
		saveDataButton.setBackground(Primula.COLOR_GREEN);
		textsamplesize.addActionListener(this);
		textpercmiss.addActionListener(this);
		
		datasrcPanel.add(loadDataButton);
		datasrcPanel.add(sampleDataButton);
		datasrcPanel.add(saveDataButton);

		samplesizepanel.add(samplesizelabel);
		samplesizepanel.add(textsamplesize);
		percmisspanel.add(percmisslabel);
		percmisspanel.add(textpercmiss);

		sampleoptions.setBorder(BorderFactory.createTitledBorder("Sampling Options"));
		textsamplesize.setText("" + samplesize);
		textpercmiss.setText("" + percmiss);
		
		
		dataPanel.add(datasrcPanel);
		sampleoptions.add(samplesizepanel);
		sampleoptions.add(percmisspanel);
		dataPanel.add(sampleoptions);
		
		/* Learn Tab */
		/* Parameter Table */
		parametertable.setModel(parammodel);
	  	parametertable.getColumnModel().getColumn(0).setHeaderValue("Parameter");
	  	parametertable.getColumnModel().getColumn(1).setHeaderValue("Value");
	  	parameterScrollList.getViewport().add(parametertable);
		parameterScrollList.setMinimumSize(new Dimension(0,100));
		learnButton.addActionListener(this);
	  	learnButton.setBackground(Primula.COLOR_GREEN);
	  	stoplearnButton.addActionListener(this);
	  	stoplearnButton.setBackground(Primula.COLOR_RED);
	  	setParamButton.addActionListener(this);
	  	setParamButton.setBackground(Primula.COLOR_YELLOW);
	  	learnSettingButton.addActionListener(this);
	  	learnSettingButton.setBackground(Primula.COLOR_BLUE);
	  	learnButtons.add(learnButton);
	  	learnButtons.add(stoplearnButton);
	  	learnButtons.add(setParamButton);
	  	learnButtons.add(learnSettingButton);
	  	
	  	restartspanel.add(restartlabel);
	  	restartspanel.add(textnumrestarts);
	  	
		//learnPanel.add(parameterScrollList);
		lowerlearnPanel.add(learnButtons);
		lowerlearnPanel.add(restartspanel);

		/* Loading parameters into table! */
		//parammodel.setParameters(mypr.getRBN().parameters());
		
		
		/* Main Pane */
		tabbedPane.add("Data",dataPanel);
		tabbedPane.add("Learning",learnsplitpane);
		
		//Inner class for closing the window
		this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						dispose();
						Primula.setIsLearnModuleOpen(false);
					}
				}
		);


		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
		contentPane.add(tabbedPane);
		
		ImageIcon icon = new ImageIcon("small_logo.jpg");
		if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) //image ok
			this.setIconImage(icon.getImage());
		this.setTitle("Learn Module");
		this.setSize(350, 300);
		this.setVisible(true);

	}
	
	public void actionPerformed( ActionEvent e ) {
		Object source = e.getSource();	
		if (source == loadDataButton){
			try{
				fileChooser.setFileFilter( myFilterRDEF );
				int value = fileChooser.showDialog(LearnModule.this, "Select");
				if (value == JFileChooser.APPROVE_OPTION) {
					datafile = fileChooser.getSelectedFile();
					RDEFReader rdefrdr = new RDEFReader();
					String datnm = datafile.getPath();
					data = (RelData)rdefrdr.readRDEF(datnm,null);
					dataFileName.setText(datnm);
				}		
			}
			catch (RBNIllegalArgumentException ex){System.out.println(ex);}
		}
		if (source == sampleDataButton){
			Sampler sampl = new Sampler();
			sampl.makeSampleStruc(myprimula);
			mystaticprimula.showMessage("Sampling ... 0% ");
			data = new RelData();
			RelDataForOneInput dataforinput = new RelDataForOneInput(myprimula.getRels());
			int completion = 0;
			for (int i=0;i<samplesize;i++){
				dataforinput.addCase(sampl.sampleOneStrucData(percmiss));
				if (10*i/samplesize>completion){
					mystaticprimula.appendMessage("X");
					completion++;
				}					
			}
			data.add(dataforinput);
			mystaticprimula.appendMessage("100%");
		}			
		
		if (source == saveDataButton){
			if (data != null){
				fileChooser.setFileFilter( myFilterRDEF );
				if (datafile != null)
					fileChooser.setSelectedFile(datafile);
				int value = fileChooser.showDialog(LearnModule.this, "Select");
				if (value == JFileChooser.APPROVE_OPTION) {
					datafile = fileChooser.getSelectedFile();
					String datnm = datafile.getPath();
					data.saveToRDEF(datnm);
				}
			}
			else
				myprimula.showMessageThis("No data to save");
		}			

		if (source == learnButton){
			lthread = new LearnThread(myprimula, 
					data, 
					parammodel, 
					parametertable,
					textnumrestarts,
					this);
			lthread.start(); 
		}
		if (source == stoplearnButton){
			lthread.setStopped();
		}
		if (source == setParamButton){
			myprimula.getRBN().setParameters(parammodel.getParameters(),parammodel.getEstimates());
		}
		if (source == learnSettingButton){
			if (!settingswindowopen){
				settingswindow = new RBNgui.LearnSettings(this);
				settingswindowopen = true;
			}
		}

		if( source == textsamplesize ){
			try{
				samplesize = new Integer(textsamplesize.getText());
			}
			catch(NumberFormatException exception){
			}
		}
		else if( source == textpercmiss ){
			try{
				percmiss = new Double(textpercmiss.getText());
			}
			catch(NumberFormatException exception){
			}
		}
	}
	public void setLearnSampleSize(Integer lss){
		numchains = lss;
	}
	public void setWindowSize(Integer gr){
		windowsize = gr;
	}

	public void setMaxIterations(Integer mi){
		maxiterations = mi;
	}
	
	public void setMaxFails(Integer mf){
		maxfails = mf;
	}

	public void setLineDistThresh(double d){
		linedistancethresh = d;
	}

	public void setLineLikThresh(double d){
		linelikelihoodthresh = d;
	}

	public void setGradDistThresh(double d){
		gradientdistancethresh = d;
	}

	
	public int getMaxIterations(){
		return maxiterations;
	}
	
	public int getMaxFails(){
		return maxfails;
	}

	public double getLineDistThresh(){
		return linedistancethresh;
	}

	public double getLineLikThresh(){
		return linelikelihoodthresh;
	}

	public double getGradDistThresh(){
		return gradientdistancethresh;
	}

	
	public int getWindowSize(){
		return windowsize;
	}

	public void setRestarts(Integer rs){
		restarts = rs;
	}

	public int getRestarts(){
		return restarts;
	}
	
	public void setSettingsOpen(boolean b){
		settingswindowopen = b;
	}
	
	public int getNumChains(){
		return numchains;
	}
	
	public void setVerbose(boolean v){
		verbose = v;
	}
	
	public boolean verbose(){
		return verbose;
	}
	
	public void setAca(boolean v){
		aca = v;
	}
	
	public boolean aca(){
		return aca;
	}
	
	
	public double getParamratiothresh(){
		return paramratiothresh;
	}
	
	public int omitRounds(){
		return omitrounds;
	}
}
