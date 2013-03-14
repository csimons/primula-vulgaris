/*
* GradientGraphProbFormNode.java 
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

public abstract class GradientGraphProbFormNode extends GradientGraphNode{

//	/** The ground probability formula that this node represents */
//	ProbForm formula; 

	String probformasstring;

	/* Upper and lower bounds on the value of this node given
	 * a current partial evaluation.
	 * Set to [-1,-1] if these bounds have not been evaluated
	 * for the current setting at the indicator nodes
	 */
	double[] bounds;

//	/** When this node represents the top-level probability formula of a ground 
//	* atom, then truthval gives the truth value of this ground atom in the data
//	* (if this atom is instantiated in the data), or the truth value according to
//	* the current setting of the corresponding indicator node
//	*
//	* truthval = 0 (1) means that ground atom is false (true),
//	* truth val = -1 means that ground atom is un-instantiated
//	*
//	* For nodes not representing a top-level probability formula, this field
//	* is irrelevant.
//	*/
//	int truthval;


	public GradientGraphProbFormNode(GradientGraph gg,
			ProbForm pf,
			RelStruc A,
			Instantiation I)
	throws RBNCompatibilityException
	{
		super(gg);
		probformasstring = pf.asString(0);
		
//		formula = pf;
//		truthval = tv;
		bounds = new double[2];
		bounds[0]=-1;
		bounds[1]=-1;
		dependsOnParam = new boolean[gg.numberOfParameters()];
		for (int i=0; i< dependsOnParam.length; i++){
			if (pf.dependsOn(gg.parameterAt(i),A,I))
				dependsOnParam[i]=true;
			else dependsOnParam[i]=false;
		}
	}

	/** dependsOnParam[i] is true if the probform of this node depends on 
	 * the i'th parameter, as given by the order defined by gg
	 */
	protected boolean[] dependsOnParam;

	public static GradientGraphProbFormNode constructGGPFN(GradientGraph gg,
			ProbForm pf, 
			Hashtable<String,GradientGraphNode> allnodes, 
			RelStruc A, 
			Instantiation I,
			int inputcaseno,
			int observcaseno)
	throws RuntimeException,RBNCompatibilityException
	{
		/* First try to find the GradientGraphProbFormNode in allnodes: */
		if (pf == null) System.out.println("pf is null");
		String key;
		if (pf instanceof ProbFormConstant)
			key = pf.asString();
		else 
			key = inputcaseno + "."  + observcaseno + "."  +  pf.asString();
		
		Object ggn = allnodes.get(key);
		if (ggn != null)
			return (GradientGraphProbFormNode)ggn;
		else{
			GradientGraphProbFormNode result = null;
			if (pf instanceof ProbFormIndicator)
				result =  new GradientGraphIndicatorNode(gg,pf,A,I,inputcaseno,observcaseno);
			if (pf instanceof ProbFormConstant)
				result =  new GradientGraphConstantNode(gg,pf,A,I);
			if (pf instanceof ProbFormConvComb)
				result =  new GradientGraphConvCombNode(gg,pf,allnodes,A,I,inputcaseno,observcaseno);
			if (pf instanceof ProbFormCombFunc)
				result =  new GradientGraphCombFuncNode(gg,pf,allnodes,A,I,inputcaseno,observcaseno);
			allnodes.put(key,result);
			return result;
		}

	}


	public double lowerBound(){
		return bounds[0];
	}

	public double upperBound(){
		return bounds[1];
	}

	public void resetBounds(){
		bounds[0]=-1;
		bounds[1]=-1;
	}


	/** The name of this node. The name identifies the function represented
	 * by a node. 
	 */
	public String name(){
		return probformasstring;
	}

//	public String name(RelStruc A){
//		return formula.asString(0,A);
//	}

	public boolean dependsOn(int param){
		return dependsOnParam[param];
	}

//	public void setTruthVal(int tv){
//	truthval = tv;
//	}

//	public int truthval(){
//	return truthval;
//	}
}
