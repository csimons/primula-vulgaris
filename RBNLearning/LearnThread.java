/*
* LearnThread.java 
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

package RBNLearning;

import RBNExceptions.RBNCompatibilityException;
import RBNpackage.*;
import RBNgui.*;
import RBNutilities.*;
import java.util.*;
import javax.swing.*;


public class LearnThread extends Thread {
	
	Primula myprimula;
	LearnModule myLearnModule;
	RelData data;
	ParameterTableModel parammodel;
	JTable parametertable;
	JTextField numrestartsfield;
	
	private boolean isstopped = false;
	
	public LearnThread(Primula mypr,
			RelData d, 
			ParameterTableModel parmod,
			JTable partab,
			JTextField nrest,
			LearnModule mylm){
		myprimula = mypr;
		myLearnModule = mylm;
		data = d;
		parammodel = parmod;
		parametertable = partab;
		numrestartsfield = nrest;
	}
	
	public void run()
	{
		if (data != null){
			try{
				myprimula.showMessageThis("Building Gradient Graph ...");
				GradientGraph gg = new GradientGraph(myprimula,
						data,
						myLearnModule);

				boolean computeLikOnly = (gg.parameters().length == 0);
				
				parammodel.setParameters(gg.parameters());
				parammodel.fireTableDataChanged();
				parametertable.updateUI();
				
				numrestartsfield.setText("" );
				double[] paramvals = new double[gg.parameters().length+1];
				
				myprimula.showMessageThis("start optimization ...");
				/* Current best likelihoods represented as pairs of 
				 * doubles (for use with SmallDouble methods)
				 */
				double[] currentbestlik = new double[2];
				double[] newlik = new double[2];
				/* The sum of likelihood values obtained in several restarts.
				 * Used for pure likelihood computation only (computeLikOnly = true)
				 */
				double[] liksum = new double[2];
				
				/* First learning ! */
				double[] results = gg.learnParameters(this);
				if (!computeLikOnly){
					currentbestlik[0]=results[results.length-4];
					currentbestlik[1]=results[results.length-3];
				}
				else{
					liksum[0]=results[results.length-2];
					liksum[1]=results[results.length-1];
				}
					
				for (int i =0;i<gg.parameters().length;i++)
					paramvals[i]=results[i];
				paramvals[paramvals.length-1]=results[results.length-1];
				parammodel.setEstimates(paramvals);
				parametertable.updateUI();
				
				
				numrestartsfield.setText("0" );
				//numrestartsfield.updateUI();
				
				/* rest is the number of completed restarts */
				int rest = 0;
				while (!isstopped() && (rest < myLearnModule.getRestarts() 
							|| myLearnModule.getRestarts() == -1)){
					results = gg.learnParameters(this);
					if (!computeLikOnly){
						newlik[0]=results[results.length-4];
						newlik[1]=results[results.length-3];
					}
					else{
						liksum[0]=liksum[0]+results[results.length-2];
						liksum[1]=liksum[1]+results[results.length-1];
					}

					if (!computeLikOnly && SmallDouble.compareSD(currentbestlik,newlik)==-1 
							&& !isstopped()){
						currentbestlik[0]=newlik[0];
						currentbestlik[1]=newlik[1];
						for (int j =0;j<gg.parameters().length;j++)
							paramvals[j]=results[j];
						paramvals[paramvals.length-1]=results[results.length-1];
						//paramvals[paramvals.length-2]=results[results.length-2];
						
							parammodel.setEstimates(paramvals);
							parametertable.updateUI();
						

					}
					if (computeLikOnly && !isstopped()){
						paramvals[paramvals.length-1]=liksum[1]/(rest+2);
						parammodel.setEstimates(paramvals);
						parametertable.updateUI();
					}
					rest++;
					numrestartsfield.setText(""+rest);
					//numrestartsfield.updateUI();
					
				}
				
				myprimula.showMessageThis("done");
			}
			catch (RBNCompatibilityException ex){System.out.println(ex);}
		}	
	}

	public void setStopped(){
		isstopped = true;
	}
	
	public boolean isstopped(){
		return isstopped;
	}
}
