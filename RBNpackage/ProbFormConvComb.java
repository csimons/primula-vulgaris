/*
 * ProbFormConvComb.java
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
import RBNExceptions.*;
import RBNutilities.*;



public class ProbFormConvComb extends ProbForm {

	ProbForm F1,F2,F3;

	public ProbFormConvComb()
	{}

	/** Creates new ProbFormConvComb */
	public ProbFormConvComb(ProbForm f1, ProbForm f2, ProbForm f3) {
		SSymbs = rbnutilities.arraymerge(f1.SSymbs,f2.SSymbs);
		SSymbs = rbnutilities.arraymerge(SSymbs, f3.SSymbs);
		RSymbs = rbnutilities.arraymerge(f1.RSymbs,f2.RSymbs);
		RSymbs = rbnutilities.arraymerge(RSymbs, f3.RSymbs);
		F1 = f1;
		F2 = f2;
		F3 = f3;
	}


	public String[] freevars()
	{
		String result[];
		result = rbnutilities.arraymerge(F1.freevars(),F2.freevars());
		result = rbnutilities.arraymerge(result,F3.freevars());
		return result;
	}


	public boolean multlinOnly(){
		return (F1.multlinOnly() && F2.multlinOnly() && F3.multlinOnly());
	}

	public ProbForm substitute(String[] vars, int[] args)
	{        return new ProbFormConvComb(F1.substitute(vars,args),F2.substitute(vars,args),F3.substitute(vars,args));
	}

	public ProbForm substitute(String[] vars, String[] args)
	{
		return new ProbFormConvComb(F1.substitute(vars,args),F2.substitute(vars,args),F3.substitute(vars,args));
	}


	public  Vector makeParentVec(RelStruc A)
	throws RBNCompatibilityException
	{
		return makeParentVec(A,new Instantiation());
	}

	public  Vector makeParentVec(RelStruc A, Instantiation inst)
	throws RBNCompatibilityException
	{
		//System.out.println("makeParentVec for " + this.asString());
		Vector atomvec1 = F1.makeParentVec(A,inst);
		Vector atomvec2 = F2.makeParentVec(A,inst);
		Vector atomvec3 = F3.makeParentVec(A,inst);
		Vector result = atomvec1;
		double v1,v2,v3;
		v1=F1.evaluate(A,inst,new String[0],new int[0],false);
		v2=F2.evaluate(A,inst,new String[0],new int[0],false);
		v3=F3.evaluate(A,inst,new String[0],new int[0],false);
		//System.out.println(v1 + " " + v2 + " " +v3 );

		if (v1 != -1)
			atomvec1 = new Vector();

		if (v1==0)
			atomvec2 = new Vector();
		if (v1==1)
			atomvec3 = new Vector();

		if ((v2==v3) && v2!=-1)
		{
			atomvec1 = new Vector();
			atomvec2 = new Vector();
			atomvec3 = new Vector();
		}

		result = atomvec1;
		result = rbnutilities.combineAtomVecs(result,atomvec2);
		result = rbnutilities.combineAtomVecs(result,atomvec3);
		return result;
	}

	public ProbForm conditionEvidence(RelStruc A, Instantiation inst)
	throws RBNCompatibilityException
	{
		ProbForm newF1 = F1.conditionEvidence(A,inst);
		ProbForm newF2 = F2.conditionEvidence(A,inst);
		ProbForm newF3 = F3.conditionEvidence(A,inst);
		if ((newF1 instanceof ProbFormConstant)&&
				(newF2 instanceof ProbFormConstant)&&
				(newF3 instanceof ProbFormConstant))
		{
			double value = ((ProbFormConstant)newF1).cval*((ProbFormConstant)newF2).cval+(1-((ProbFormConstant)newF1).cval)*((ProbFormConstant)newF3).cval;
			return new ProbFormConstant(value);									     
		}
		else return new ProbFormConvComb(newF1,newF2,newF3);

	}

	public ProbForm conditionEvidence(Instantiation inst){
		return new ProbFormConvComb(F1.conditionEvidence(inst),F2.conditionEvidence(inst),F3.conditionEvidence(inst));
	}

	public ProbForm f1(){
		return F1;
	}

	public ProbForm f2(){
		return F2;
	}

	public ProbForm f3(){
		return F3;
	}


	public boolean dependsOn(String variable, RelStruc A, Instantiation data)
	throws RBNCompatibilityException
	{
		int e1 = F1.evaluatesTo(A,data,false,null);
		if (e1==1) 
			return F2.dependsOn(variable,A,data);
		if (e1==0)
			return F3.dependsOn(variable,A,data);
		else
			return (F1.dependsOn(variable,A,data) || F2.dependsOn(variable,A,data)  || F3.dependsOn(variable,A,data));
	}


	public double evaluate(RelStruc A, Instantiation inst, String[] vars, int[] tuple, boolean useCurrentCvals)
	throws RBNCompatibilityException
	{
		double ev1 = F1.evaluate(A,inst,vars,tuple,useCurrentCvals);
		double ev2 = F2.evaluate(A,inst,vars,tuple,useCurrentCvals);
		double ev3 = F3.evaluate(A,inst,vars,tuple,useCurrentCvals);


		if (ev1 == -1) {
			if (ev2==ev3)
				return ev2;
			else
				return -1;
		}

		if ((ev1 != 0) && (ev2==-1) )
			return -1;

		if ((ev1 != 1) && (ev3==-1) )
			return -1;

		return ev1*ev2 + (1-ev1)*ev3;

	}


	public  double evalSample(RelStruc A,Hashtable atomhasht,Instantiation inst, long[] timers)
	throws RBNCompatibilityException
	{
		double v1;
		double v2 =0;
		double v3 =0;
		v1 =  F1.evalSample(A,atomhasht,inst,timers);
		if (v1 != 0.0)
			v2 =  F2.evalSample(A,atomhasht,inst,timers);
		if (v1 != 1.0)
			v3 =  F3.evalSample(A,atomhasht,inst,timers);
		return v1*v2+(1-v1)*v3;
	}


	public int evaluatesTo(RelStruc A)
	throws RBNCompatibilityException
	{	
		int e1 = F1.evaluatesTo(A);
		int e2 = F2.evaluatesTo(A);
		int e3 = F3.evaluatesTo(A);
		if (e1==1 && e2==0) return 0;
		if (e1==1 && e2==1) return 1;
		if (e1==0 && e3==0) return 0;
		if (e1==0 && e3==1) return 1;
		if (e2==0 && e3==0) return 0;
		if (e2==1 && e3==1) return 1;
		return -1;
	}


	public int evaluatesTo(RelStruc A,Instantiation inst, boolean usesampleinst, Hashtable atomhasht)
	throws RBNCompatibilityException
	{	
		//System.out.println("evaluatesTo for " + this.asString());
		int e1 = F1.evaluatesTo(A,inst,usesampleinst,atomhasht);
		int e2 = F2.evaluatesTo(A,inst,usesampleinst,atomhasht);
		int e3 = F3.evaluatesTo(A,inst,usesampleinst,atomhasht);
		if (e1==1 && e2==0) return 0;
		if (e1==1 && e2==1) return 1;
		if (e1==0 && e3==0) return 0;
		if (e1==0 && e3==1) return 1;
		if (e2==0 && e3==0) return 0;
		if (e2==1 && e3==1) return 1;
		return -1;
	}

	public String[] parameters()
	{
		String result[];
		result = rbnutilities.arraymerge(F1.parameters(),F2.parameters());
		result = rbnutilities.arraymerge(result,F3.parameters());
		return result;
	}

	public String asString(int depth)
	{
		String tabstring = "";
		for (int i=0;i<depth;i++)
			tabstring = tabstring +"  ";
		return tabstring + "("+ '\n'+ 
			F1.asString(depth+1) + ":"  +'\n'+ 
			F2.asString(depth+1)  +'\n'+ 
			tabstring + "," +'\n'+ 
			F3.asString(depth+1
					) +'\n'+ 
			tabstring + ")";
	}

	public String asString()
	{
		return "("+ F1.asString() + ":" +'\n'+ F2.asString(1)  +'\n'+ "," +'\n' + F3.asString(1)  +'\n'+ ")";

	}

	public String asString(RelStruc A)
	{
		return "("+ F1.asString(A) + ":" +'\n'+ F2.asString(1,A)  +'\n'+ "," +'\n' + F3.asString(1,A)  +'\n'+ ")";

	}
	public String asString(int depth, RelStruc A)
	{

		String tabstring = "";
		for (int i=0;i<depth;i++)
			tabstring = tabstring +"  ";
		return tabstring + "("+ '\n'+ 
			F1.asString(depth,A) + ":"  +'\n'+ 
			F2.asString(depth+1,A)  +'\n'+ 
			tabstring + "," +'\n'+ 
			F3.asString(depth+1,A) +'\n'+ 
			tabstring + ")";
	}

	public ProbForm sEval(RelStruc A)
	throws RBNCompatibilityException
	{
		ProbForm f1 = F1.sEval(A);
		ProbForm f2 = F2.sEval(A);
		ProbForm f3 = F3.sEval(A);
		return new ProbFormConvComb(f1,f2,f3);
	}

	public ProbForm subPF(int i){
		switch (i){
		case 1: return F1;
		case 2: return F2;
		case 3: return F3;
		default: return null;
		}
	}

	public void setParameters(String[] params,  double[] values){
		F1.setParameters(params,values);
		F2.setParameters(params,values);
		F3.setParameters(params,values);
	}
}
