/*
 *ProbFormConstant.java 
 * 
 * Copyright (C) 2003 Max-Planck-Institut fuer Informatik,
 *                    Helsinki Institute for Information Technology
 *
 * contact:
 * jaeger@cs.auc.dk   www.cs.auc.dk/~jaeger/Primula.html
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

package RBNpackage;

import java.util.*;
import java.io.*;
import RBNutilities.*;



/** A ProbFormConstant represents a probability constant. The formula can
 * also represent an unknown parameter. In this case, the string paramname 
 * contains the name of the parameter. In all cases, a  numeric value is
 * associated with the ProbFormConstant, so that it is always a valid probability
 * formula that can be evaluated. When the formula represents a parameter, then
 * this numeric value is a current estimate of the parameter value
 */
public class ProbFormConstant extends ProbForm
{
	public double cval;
	private String paramname;

	public ProbFormConstant(){
		SSymbs = new Rel[0];
		RSymbs = new Rel[0];
		cval = 0;
		paramname = "";
	}

	public ProbFormConstant(double v){
		SSymbs = new Rel[0];
		RSymbs = new Rel[0];
		cval = v;
		paramname = "";
	}


	public ProbFormConstant(String pn){
		SSymbs = new Rel[0];
		RSymbs = new Rel[0];
		cval = 0.5;
		paramname = pn;
	}

	public String[] freevars()
	{
		String[] result = new String[0];
		return result;
	}

	public boolean multlinOnly(){
		return true;
	}

	public ProbForm substitute(String[] vars, int[] args)
	{
		if (paramname == "")
			return new ProbFormConstant(cval);
		else
			return new ProbFormConstant(paramname);
	}

	public ProbForm substitute(String[] vars, String[] args)
	{
		if (paramname == "")
			return new ProbFormConstant(cval);
		else
			return new ProbFormConstant(paramname);
	}

	public ProbForm conditionEvidence(RelStruc A, Instantiation inst)
	{
		return this;
	}


	public  Vector makeParentVec(RelStruc A){
		return new Vector();
	}

	public  Vector makeParentVec(RelStruc A, Instantiation inst){
		return new Vector();
	}


	public ProbForm conditionEvidence(Instantiation inst){
		return new ProbFormConstant(cval);
	}


	public boolean dependsOn(String variable, RelStruc A, Instantiation data){
		//System.out.println("dependsOn for " + this.asString());
		if ( paramname != "" && paramname.equals(variable)){
			//  System.out.println("return true");
			return true;}
		else 
			return false;
	}

	public double evaluate(RelStruc A, Instantiation inst, String[] vars, int[] tuple, boolean useCurrentCvals)
	{
		if (paramname != "" && !useCurrentCvals)
			return -1;
		else 
			return cval;
	}


	public  double evalSample(RelStruc A,Hashtable atomhasht,Instantiation inst, long[] timers){
		return cval;
	}


	public  int evaluatesTo(RelStruc A){
		if (cval == 0) return 0;
		if (cval == 1) return 1;
		else
			return -1;
	}

	public  int evaluatesTo(RelStruc A,Instantiation inst, boolean usesampleinst, Hashtable atomhasht){
		return evaluatesTo(A);
	}

	public ProbForm sEval(RelStruc A){
		return this;
	}


	public String getParamName(){
		return paramname;
	}

	public double getCval(){
		return cval;
	}

	public String[] parameters(){
		if (paramname=="")
			return new String[0];
		else{
			String[] result = new String[1];
			result[0]=paramname;
			return result;
		}
	}

	public String asString(int depth)
	{
		String tabstring = "";
		for (int i=0;i<depth;i++)
			tabstring = tabstring +"  ";

		String result = new String();
		result = tabstring + this.asString();
		return result;
	}


	public String asString()
	{
		String result = new String();
		if (paramname == "")
			result = String.valueOf(cval);
		else 
			result = paramname;
		return result;
	}

	public String asString(int depth,RelStruc A)
	{
		return this.asString(depth);
	}

	public String asString(RelStruc A){
		return this.asString();
	}

	public void setParameters(String[] params,  double[] values){
		if (!paramname.equals("")){
			for (int i=0;i<params.length;i++){
				if (params[i].equals(paramname)){
					cval = values[i];
					paramname = "";
				}
					
			}
		}
	}
}
