/*
* GradientGraphIndicatorNode.java 
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

import java.util.*;
import java.io.*;
import RBNpackage.*;
import RBNgui.*;
import RBNExceptions.*;
import RBNutilities.*;
import RBNinference.*;


public class GradientGraphIndicatorNode extends GradientGraphProbFormNode{

	/** Ground atom represented by this node */
	Atom myatom;
	/** Index of RelDataCase from which this ground atom derives */
	int inputcaseno;
	int observcaseno;
	
//	/** Set to 1 if the ground atom is instantiated to true in the data,
//	* to 0 if ground atom false in data, to -1 if value for this ground
//	* atom is missing in the data (GradientGraphIndicatorNodes with instValInData != -1 
//	* may never need to be constructed!).
//	*/
//	int instValInData;


	/** The current instantiation for this indicator;
	 * currentInst = -1 if not currently instantiated
	 */
	int currentInst;


//	/** The node in this GradientGraph that contains the probability formula
//	* defining the probability for this ground atom
//	*/
//	GradientGraphProbFormNode mypf;

	/** Represents a current value for this Node in EM procedure
	 * Only needed when instValInData = -1
	 */
	boolean[] sampledVals;

	public GradientGraphIndicatorNode(GradientGraph gg,
			ProbForm pf,  
			RelStruc A,
			Instantiation I,
			int inputcasenoarg,
			int observcasenoarg)
	throws RuntimeException, RBNCompatibilityException
	{
		super(gg,pf,A,I);
		inputcaseno = inputcasenoarg;
		observcaseno = observcasenoarg;
		currentInst = -1;
		sampledVals = new boolean[0];
		if (!(pf instanceof ProbFormIndicator)){
			System.out.println("Cannot create GradientGraphIndicatorNode from ProbForm " + pf.asString());
		}
		myatom = ((ProbFormIndicator)pf).atom();
		gg.addToIndicators(this);
		children = null;
	}



	public double evaluate(){
		return currentInst;
	}


	public void evaluateBounds(){
		switch(currentInst){
		case -1:
			bounds[0]=0;
			bounds[1]=1;
			break;
		case 0:
			bounds[0]=0;
			bounds[1]=0;
			break;
		case 1:
			bounds[0]=1;
			bounds[1]=1;
			break;
		}
	}

	public double evaluateGrad(int param){
		return 0.0;
	}

	public Atom myatom(){
		return myatom;
	}

	public int inputcaseno(){
		return inputcaseno;
	}
	
	public int observcaseno(){
		return observcaseno;
	}
	
	


	/** Sets the current instantiation according to 
	 * the value in the sno's sample
	 */
	public void setCurrentInst(int sno){
		if (sampledVals[sno])
			currentInst = 1;
		else
			currentInst = 0;
	}

	/** Sets value in sno's sample to tv */
	public void setSampleVal(int sno, boolean tv){
		sampledVals[sno]=tv;
	}

	/** Sets value in sno's sample to current instantiation */
	public void setSampleVal(int sno){
		if (currentInst==1)
			sampledVals[sno]=true;
		else if (currentInst==0)
			sampledVals[sno]=false;
		else
			throw new RBNRuntimeException("Trying to set undefined truth value");
			
	}

	/** Toggles value in sno's sample */
	public void toggleSampleVal(int sno){
		if (sampledVals[sno])
			sampledVals[sno]=false;
		else
			sampledVals[sno]=true;
	}

	/** Sets the current instantiation according to 
	 * the truth value tv
	 */
	public void setCurrentInst(boolean tv){
		if (tv)
			currentInst = 1;
		else
			currentInst = 0;
	}

	public int getCurrentInst(){
		return currentInst;
	}



	/** initializes  sampledVals to an array of size 'size' */
	public void initSampledVals(int size){
			sampledVals = new boolean[size];
	}

	public void toggleCurrentInst(){
		if (currentInst==1)
			currentInst=0;
		else
			currentInst=1;
	}


	/** Resets the currentInst field to -1, i.e. node 
	 * becomes un-instantiated
	 */
	public void unset(){
		currentInst = -1;
	}
}
