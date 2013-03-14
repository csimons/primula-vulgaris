/*
* LearnSettings.java 
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import javax.swing.*; 

public class LearnSettings extends JFrame implements ActionListener, ItemListener {

	private JLabel samplesizelabel = new JLabel("Gibbs chains");
	private JLabel restartslabel = new JLabel("Restarts (-1 = until stopped)");
	private JLabel gibbsroundslabel = new JLabel("Gibbs Window Size");
	private JLabel maxiterationslabel = new JLabel("Max. iterations gradient search");
	private JLabel linedistancelabel = new JLabel("Distance threshold");
	private JLabel linelikelihoodlabel = new JLabel("Likelihood threshold (linesearch)");
//	private JLabel gradientdistancelabel = new JLabel("Distance threshold (gradient search)");
	private JLabel maxfailslabel = new JLabel("Max. fails (Sample missing)");
//	private JLabel verboselabel = new JLabel("Verbose");
	
	private JTextField samplesizetext = new JTextField(5);
	private JTextField restartstext = new JTextField(5);
	private JTextField gibbsroundstext = new JTextField(5);
	private JTextField maxiterationstext = new JTextField(5);
	private JTextField linedistancetext = new JTextField(5);
	private JTextField linelikelihoodtext = new JTextField(5);
	private JTextField gradientdistancetext = new JTextField(5);
	private JTextField maxfailstext = new JTextField(5);

	
	private JPanel samplesizepanel = new JPanel(new FlowLayout());
	private JPanel restartspanel = new JPanel(new FlowLayout());
	private JPanel gibbsroundspanel = new JPanel(new FlowLayout());
	private JPanel maxiterationspanel = new JPanel(new FlowLayout());
	private JPanel linedistancepanel = new JPanel(new FlowLayout());
	private JPanel linelikelihoodpanel = new JPanel(new FlowLayout());
	private JPanel gradientdistancepanel = new JPanel(new FlowLayout());
	private JPanel maxfailspanel = new JPanel(new FlowLayout());
	
	private JPanel generaloptions = new JPanel(new GridLayout(3,1));
	private JPanel incompleteoptions = new JPanel(new GridLayout(4,1));
	private JPanel terminateoptions = new JPanel(new GridLayout(3,1));
	
	private JCheckBox verbosecheckbox = new JCheckBox("Verbose");
	private JCheckBox acacheckbox = new JCheckBox("ACA");

	private LearnModule learnmodule;
	
	public LearnSettings(LearnModule lm){
		
		learnmodule = lm;
		this.addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						learnmodule.setSettingsOpen(false);
						dispose();
					}
				}
		);
		samplesizepanel.add(samplesizelabel);
		samplesizepanel.add(samplesizetext);
		
		restartspanel.add(restartslabel);
		restartspanel.add(restartstext);
		
		gibbsroundspanel.add(gibbsroundslabel);
		gibbsroundspanel.add(gibbsroundstext);
		
		
		maxiterationspanel.add(maxiterationslabel);
		maxiterationspanel.add(maxiterationstext);
		
		linedistancepanel.add(linedistancelabel);
		linedistancepanel.add(linedistancetext);
		
		linelikelihoodpanel.add(linelikelihoodlabel);
		linelikelihoodpanel.add(linelikelihoodtext);
	

		maxfailspanel.add(maxfailslabel);
		maxfailspanel.add(maxfailstext);
		
		generaloptions.add(restartspanel);
		generaloptions.add(verbosecheckbox);
		generaloptions.setBorder(BorderFactory.createTitledBorder("General"));
		
		terminateoptions.add(maxiterationspanel);
		terminateoptions.add(linedistancepanel);
		terminateoptions.add(linelikelihoodpanel);
		terminateoptions.setBorder(BorderFactory.createTitledBorder("Termination"));
		
		incompleteoptions.add(samplesizepanel);
		incompleteoptions.add(gibbsroundspanel);
		incompleteoptions.add(maxfailspanel);
		incompleteoptions.add(acacheckbox);
		incompleteoptions.setBorder(BorderFactory.createTitledBorder("Incomplete Data"));
		
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.Y_AXIS));
//		contentPane.add(restartspanel);
//		contentPane.add(samplesizepanel);
//		contentPane.add(gibbsroundspanel);
//
//		contentPane.add(maxiterationspanel);
//		contentPane.add(gradientdistancepanel);	
//		contentPane.add(linedistancepanel);
//		contentPane.add(linelikelihoodpanel);
//		contentPane.add(maxfailspanel);
//		
//		contentPane.add(verbosecheckbox);
//		contentPane.add(acacheckbox);
		
		contentPane.add(generaloptions);
		contentPane.add(terminateoptions);
		contentPane.add(incompleteoptions);
		
		samplesizetext.setText(""+learnmodule.getNumChains());
		samplesizetext.addActionListener(this);
		restartstext.setText(""+learnmodule.getRestarts());
		restartstext.addActionListener(this);
		gibbsroundstext.setText(""+learnmodule.getWindowSize());
		gibbsroundstext.addActionListener(this);
		maxiterationstext.setText(""+learnmodule.getMaxIterations());
		maxiterationstext.addActionListener(this);
		linedistancetext.setText(""+learnmodule.getLineDistThresh());
		linedistancetext.addActionListener(this);
		linelikelihoodtext.setText(""+learnmodule.getLineLikThresh());
		linelikelihoodtext.addActionListener(this);
		gradientdistancetext.setText(""+learnmodule.getGradDistThresh());
		gradientdistancetext.addActionListener(this);
		maxfailstext.setText(""+learnmodule.getMaxFails());
		maxfailstext.addActionListener(this);
		verbosecheckbox.setSelected(false);
		verbosecheckbox.addItemListener(this);
		acacheckbox.setSelected(false);
		acacheckbox.addItemListener(this);

		ImageIcon icon = new ImageIcon("small_logo.jpg");
		if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) //image ok
			this.setIconImage(icon.getImage());
		this.setTitle("Learning Settings");
		this.setSize(350, 500);
		this.setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();	

		if( source == samplesizetext ){
			try{
				Integer tempint = new Integer(samplesizetext.getText());
				learnmodule.setLearnSampleSize(tempint.intValue());  
			}
			catch(NumberFormatException exception){
			}
		}

		else if ( source == restartstext ){
			try{
				Integer tempint = new Integer(restartstext.getText());
				learnmodule.setRestarts(tempint.intValue());  
			}
			catch(NumberFormatException exception){
			}
		}
		else if ( source == gibbsroundstext ){
			try{
				Integer tempint = new Integer(gibbsroundstext.getText());
				learnmodule.setWindowSize(tempint.intValue());  
			}
			catch(NumberFormatException exception){
			}
		}		
		else if ( source == maxiterationstext ){
			try{
				Integer tempint = new Integer(maxiterationstext.getText());
				learnmodule.setMaxIterations(tempint.intValue());  
			}
			catch(NumberFormatException exception){
			}
		}
		else if ( source == linedistancetext ){
			try{
				Double tempdoub = new Double(linedistancetext.getText());
				learnmodule.setLineDistThresh(tempdoub.doubleValue());  
			}
			catch(NumberFormatException exception){
			}
		}
		else if ( source == linelikelihoodtext ){
			try{
				Double tempdoub = new Double(linelikelihoodtext.getText());
				learnmodule.setLineLikThresh(tempdoub.doubleValue());  
			}
			catch(NumberFormatException exception){
			}
		}
		else if ( source == gradientdistancetext ){
			try{
				Double tempdoub = new Double(gradientdistancetext.getText());
				learnmodule.setGradDistThresh(tempdoub.doubleValue());  
			}
			catch(NumberFormatException exception){
			}
		}
		else if ( source == maxfailstext ){
			try{
				Integer tempint = new Integer(maxfailstext.getText());
				learnmodule.setMaxFails(tempint.intValue());  
			}
			catch(NumberFormatException exception){
			}
		}

	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getSource();
		if ( source == verbosecheckbox ){
			if (e.getStateChange() == ItemEvent.SELECTED){
				learnmodule.setVerbose(true);
			}
			else
				learnmodule.setVerbose(false);
		}
		if ( source == acacheckbox ){
			if (e.getStateChange() == ItemEvent.SELECTED){
				learnmodule.setAca(true);
			}
			else
				learnmodule.setAca(false);
		}
	}
}
