/*
 * ProbFormCombFunc.java
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


public class ProbFormCombFunc extends ProbForm{

	public CombFunc mycomb;
	public ProbForm pfargs[];
	public String quantvars[];
	public CConstr cconstr;

	public int mycombInt;

	public static final int NOR = 1;
	public static final int MEAN = 2;
	public static final int INVSUM = 3;
	public static final int ESUM = 4;


	public ProbFormCombFunc()
	{}

	/** Creates new ProbFormCombFunc */
	public ProbFormCombFunc(CombFunc mc,ProbForm[] pfa, String[] qvars, CConstr cc) 
	throws IllegalArgumentException
	{
		// Construct SSymbs and RSymbs
		SSymbs = new Rel[0];
		RSymbs = new Rel[0];

		if (pfa.length > 0)
			for (int i = 0; i<pfa.length; i++)
			{
				rbnutilities.arraymerge(SSymbs,pfa[i].SSymbs);
				rbnutilities.arraymerge(RSymbs,pfa[i].RSymbs);
			}
		SSymbs = rbnutilities.arraymerge(SSymbs,cc.SSymbs);

		// Construct mycomb
		mycomb = mc;
		if (mc instanceof CombFuncNOr) mycombInt = NOR;
		if (mc instanceof CombFuncMean) mycombInt = MEAN;
		if (mc instanceof CombFuncInvsum) mycombInt = INVSUM;
		if (mc instanceof CombFuncESum) mycombInt = ESUM;


		// Construct pfargs
		pfargs = pfa; 
		// Construct quantvars
		quantvars = qvars;
		// Construct cconstr
		cconstr = cc;

	}

	public ProbFormCombFunc(String mc,ProbForm[] pfa, String[] qvars, CConstr cc) 
	throws IllegalArgumentException
	{

		// Construct SSymbs and RSymbs
		SSymbs = new Rel[0];
		RSymbs = new Rel[0];

		if (pfa.length > 0)
			for (int i = 0; i<pfa.length; i++)
			{
				rbnutilities.arraymerge(SSymbs,pfa[i].SSymbs);
				rbnutilities.arraymerge(RSymbs,pfa[i].RSymbs);
			}
		SSymbs = rbnutilities.arraymerge(SSymbs,cc.SSymbs);

		// Construct mycomb
		if (mc.equals("n-or")) mycombInt = NOR;
		if (mc.equals("mean")) mycombInt = MEAN;
		if (mc.equals("invsum")) mycombInt = INVSUM;
		if (mc.equals("esum")) mycombInt = ESUM;

		//if (mc.equals("logical")) sw = 3;
		//if (mc.equals("size")) sw = 4;
		switch(mycombInt)
		{
		case NOR:  mycomb = new CombFuncNOr(); break;
		case MEAN:  mycomb = new CombFuncMean(); break;
		case INVSUM:  mycomb = new CombFuncInvsum(); break;
		case ESUM:  mycomb = new CombFuncESum(); break;
		default: throw new IllegalArgumentException("Illegal Argument for construction of ProbFormCombFunc: " + mc);
		}
		// Construct pfargs
		pfargs = pfa; 
		// Construct quantvars
		quantvars = qvars;
		// Construct cconstr
		cconstr = cc;

	}

	public  String[] freevars()
	{
		String result[]={};
		// first collect all the free variables from the pfargs formulas
		for (int i = 0 ; i<pfargs.length ; i++)
			result = rbnutilities.arraymerge(result,pfargs[i].freevars());
		// subtract the variables in quantvars
		result = rbnutilities.arraysubstraction(result,quantvars);
		return result;
	}

	public boolean multlinOnly(){	
		if (!(mycomb instanceof MultLinCombFunc)) 
			return false;
		boolean result = true;
		for (int i=0;i<pfargs.length;i++)
			if (!pfargs[i].multlinOnly()) 
				result = false;
		return result;
	}

	public ProbForm substitute(String[] vars, int[] args)
	{
		ProbFormCombFunc result;
		CConstr subcconstr;
		/* Construct new substitution arguments by 
		 * eliminating the variables that appear in 
		 * quantvars and their associated 
		 * substitution values from vars and args
		 */
		String[] subsvars;
		subsvars = rbnutilities.arraysubstraction(vars,quantvars);
		int[] subsargs = rbnutilities.CorrArraySubstraction(subsvars,vars,args);


		// Perform substitution on pfargs
		ProbForm[]  subpfargs = new ProbForm[pfargs.length];
		for (int i = 0; i<pfargs.length; i++)
			subpfargs[i]=pfargs[i].substitute(subsvars,subsargs);
		//Perform substitution on cconstr
		subcconstr = cconstr.substitute(vars,args);

		result = new ProbFormCombFunc(mycomb.name,subpfargs,quantvars,subcconstr);
		return result;
	}

	public ProbForm substitute(String[] vars, String[] args)
	{
		ProbFormCombFunc result;
		ProbForm[]  subpfargs = new ProbForm[pfargs.length];
		CConstr subcconstr;


		// Rename all the variables bound 
		// by combination function
		String[] freev = freevars();
		String[] reserved = new String[vars.length+args.length+freev.length];
		for (int i = 0;i<vars.length;i++)
			reserved[i]=vars[i];
		for (int i = 0;i<args.length;i++)
			reserved[vars.length+i]=args[i];
		for (int i = 0;i<freev.length;i++)
			reserved[vars.length+args.length+i]=freev[i];

		String[] newquantvars = rbnutilities.NewVariables(reserved,quantvars.length);

		for (int i = 0; i<pfargs.length; i++)
		{
			subpfargs[i]=pfargs[i].substitute(quantvars,newquantvars);
		}
		subcconstr = cconstr.substitute(quantvars,newquantvars);

		// Now perform the original substitution
		for (int i = 0; i<pfargs.length; i++)
		{
			subpfargs[i]=subpfargs[i].substitute(vars,args);
		}
		subcconstr = subcconstr.substitute(vars,args);
		result = new ProbFormCombFunc(mycomb.name,subpfargs,newquantvars,subcconstr);
		return result;
	}


	public  Vector makeParentVec(RelStruc A)
	throws RBNCompatibilityException{
		return makeParentVec(A,new Instantiation());
	}

	public  Vector makeParentVec(RelStruc A, Instantiation inst)
	throws RBNCompatibilityException{
		//System.out.println("makeParentVec for " + this.asString() + ": ");
		Vector result = new Vector();
		ProbForm nextprobform;

		int[][] subslist = A.allTrue(cconstr,quantvars);

		for (int i=0; i<pfargs.length; i++)
		{
			for (int j=0; j<subslist.length; j++)
			{
				nextprobform = pfargs[i].substitute(quantvars,subslist[j]);
				result = rbnutilities.combineAtomVecs(result,nextprobform.makeParentVec(A,inst));
			}
		}
		return result;
	}


	public ProbForm conditionEvidence(Instantiation inst)
	{
		ProbFormCombFunc result;

		// Perform conditionEvidence on pfargs
		ProbForm[]  condpfargs = new ProbForm[pfargs.length];
		for (int i = 0; i<pfargs.length; i++)
			condpfargs[i]=pfargs[i].conditionEvidence(inst);

		result = new ProbFormCombFunc(mycomb.name,condpfargs,quantvars,cconstr);
		return result;
	}


	public ProbForm conditionEvidence(RelStruc A, Instantiation inst)
	throws RBNCompatibilityException
	{
		//System.out.println("condition Evidence for " + this.asString());
		ProbForm nextcondpfarg;

		int[][] subslist = A.allTrue(cconstr,quantvars);

		double[] condpfargs =new double[pfargs.length*subslist.length];
		boolean allconstant = true;
		int i = 0;
		int j = 0;
		int currentindex = 0;
		while (i<pfargs.length && allconstant){
			j=0;
			while (j<subslist.length && allconstant){
				nextcondpfarg = pfargs[i].substitute(quantvars,subslist[j]).conditionEvidence(A,inst);
				if (!(nextcondpfarg instanceof ProbFormConstant)) allconstant = false;
				else condpfargs[currentindex]=((ProbFormConstant)nextcondpfarg).cval;
				j++;
				currentindex++;
			}
			i++;
		}

		if (allconstant){
			//System.out.println("returned " + mycomb.evaluate(condpfargs));
			return new ProbFormConstant(mycomb.evaluate(condpfargs));
		}
		else{
			//System.out.println("returned old");
			return this;
		}
	}


	public boolean dependsOn(String variable, RelStruc A, Instantiation data)
	throws RBNCompatibilityException
	{
		Boolean result = false;
		/* One should test whether any ground instances of the arguments depends on variable.
		 * E.g.:
		 *      n-or{(r(v):#t,0)|w: l(v,w)}
		 * This formula depends formally on #t.
		 * However, when there are no a,b in A with l(a,b), and r(a) true or undetermined in the
		 * data, then this formula (in this context) does not actually depend on #t
		 */
		int[][] subslist = A.allTrue(cconstr,quantvars);
		for (int i=0; i<pfargs.length; i++){
			for (int j=0; j<subslist.length; j++){
				result = (result || pfargs[i].substitute(quantvars,subslist[j]).dependsOn(variable,A,data)); 
			}
		}
		return result;
	}

	public double evaluate(RelStruc A, Instantiation inst, String[] vars, int[] tuple, boolean useCurrentCvals)
	throws RBNCompatibilityException
	{
		ProbFormCombFunc subspfcf = (ProbFormCombFunc)this.substitute(vars,tuple);


		//        CConstr subscc = this.cconstr.substitute(vars,tuple);

//		/*
//		* generate list of all substitution tuples for quantvars
//		* that satisfy the cconstr
//		*/
//		int[][] subslist = A.allTrue(subscc,quantvars);
		int[][] subslist = tuplesSatisfyingCConstr(A, vars, tuple);

		/* Initialize array of arguments for combination function */
		double[] combargs = new double[subspfcf.pfargs.length*subslist.length];

		/* Evaluate the probability formulas in pfargs and 
		 * enter results into combargs
		 */
		int nextindex;
		double nextvalue;
		for (int i=0; i<subspfcf.pfargs.length; i++)
		{
			for (int j=0; j<subslist.length; j++)
			{
				nextindex = i*subslist.length + j;
				nextvalue = subspfcf.pfargs[i].evaluate(A,inst,quantvars,subslist[j],useCurrentCvals);
				if (nextvalue == -1) return -1;
				combargs[nextindex]=nextvalue;
			}
		}
		/*
        apply mycomb to the resulting array
		 */
		 return mycomb.evaluate(combargs);
	}

	public  double evalSample(RelStruc A, Hashtable atomhasht, Instantiation inst, long[] timers)
	throws RBNCompatibilityException
	{
		long inittime; 
		/* Same code as in evaluate and evaluatesTo: */
		CConstr scc = this.cconstr;
		inittime=System.currentTimeMillis();
		int[][] subslist = A.allTrue(scc,quantvars);
		timers[3]=timers[3]+System.currentTimeMillis()-inittime;

		ProbForm groundpf;
		double[] combargs = new double[this.pfargs.length*subslist.length];
		int nextindex;
		double nextvalue;
		for (int i=0; i<this.pfargs.length; i++)
		{
			for (int j=0; j<subslist.length; j++)
			{
				nextindex = i*subslist.length + j;
				groundpf = this.pfargs[i].substitute(quantvars,subslist[j]);
				nextvalue = groundpf.evalSample(A,atomhasht,inst,timers);
				combargs[nextindex]=nextvalue;
			}
		}

		return mycomb.evaluate(combargs);
	}

	public int evaluatesTo(RelStruc A, Instantiation inst, boolean usesampleinst, Hashtable atomhasht)
	throws RBNCompatibilityException
	{
		/* First create an argument vector for the combination function
		 * as in evaluate(...). Arguments are 1,0,-1 according to 
		 * recursive calls on subformulas
		 */
		CConstr scc = this.cconstr;
		/*
		 * generate list of all substitution tuples for quantvars
		 * that satisfy the cconstr
		 */

		int[][] subslist = A.allTrue(scc,quantvars);

		/* Initialize array of arguments for combination functin */
		int[] combargs = new int[this.pfargs.length*subslist.length];

		/* Recursive calls
		 */
		int nextindex;
		int nextvalue;
		for (int i=0; i<this.pfargs.length; i++)
		{
			for (int j=0; j<subslist.length; j++)
			{
				nextindex = i*subslist.length + j;
				nextvalue = this.pfargs[i].substitute(quantvars,subslist[j]).evaluatesTo(A,inst,usesampleinst,atomhasht);
				combargs[nextindex]=nextvalue;
			}
		}

		/* Now ask mycomb what it is going to do with this...
		 */
		return mycomb.evaluatesTo(combargs);
	}


	public int evaluatesTo(RelStruc A)
	throws RBNCompatibilityException
	{
		/* First create an argument vector for the combination function
		 * as in evaluate(...). Arguments are 1,0,-1 according to 
		 * recursive calls on subformulas
		 */
		CConstr scc = this.cconstr;
		/*
		 * generate list of all substitution tuples for quantvars
		 * that satisfy the cconstr
		 */
		int[][] subslist = A.allTrue(scc,quantvars);

		/* Initialize array of arguments for combination functin */
		int[] combargs = new int[this.pfargs.length*subslist.length];

		/* Recursive calls
		 */
		int nextindex;
		int nextvalue;
		for (int i=0; i<this.pfargs.length; i++)
		{
			for (int j=0; j<subslist.length; j++)
			{
				nextindex = i*subslist.length + j;
				nextvalue = this.pfargs[i].substitute(quantvars,subslist[j]).evaluatesTo(A);
				combargs[nextindex]=nextvalue;
			}
		}

		/* Now ask mycomb what it is going to do with this...
		 */
		return mycomb.evaluatesTo(combargs);
	}


	public  String[] parameters()
	{
		String result[]={};
		for (int i = 0 ; i<pfargs.length ; i++)
			result = rbnutilities.arraymerge(result,pfargs[i].parameters());
		return result;
	}

	public String asString(int depth)
	/* precedes string representation of formula with depth 
	 * tabs
	 */ 
	{
		String result;
		String tabstring = "";

		for (int i=0;i<depth;i++)
			tabstring = tabstring +"  ";

		result = tabstring + mycomb.name+"{";
		for (int i = 0; i<pfargs.length-1; i++)
		{
			result = result +'\n' + pfargs[i].asString(depth+1) + ",";
		}
		if (pfargs.length >= 1) 
			result = result +'\n' +pfargs[pfargs.length-1].asString(depth+1);
		result = result + '\n' +"  "+ tabstring + "| " 
		+ rbnutilities.arrayToString(quantvars) + " : " +cconstr.asString();
		result =  result +'\n' + tabstring + "}";
		return result;
	}

	public String asString()
	{
		String result;
		result = mycomb.name+"{";
		for (int i = 0; i<pfargs.length-1; i++)
		{
			result = result +'\n' + pfargs[i].asString(1) + ",";
		}
		if (pfargs.length >= 1) 
			result = result +'\n'  +pfargs[pfargs.length-1].asString(1);
		result = result +'\n'+ '\t' + "| " ;
		result = result +'\n'+ '\t' + rbnutilities.arrayToString(quantvars) + " : ";
		result =  result +'\n'+ '\t' + cconstr.asString();
		result =  result +'\n'+ "}";
		return result;
	}

	public String asString(int depth, RelStruc A)
	/* precedes string representation of formula with depth 
	 * tabs
	 */ 
	{
		String result;
		String tabstring = "";

		for (int i=0;i<depth;i++)
			tabstring = tabstring +"  ";

		result = tabstring + mycomb.name+"{";
		for (int i = 0; i<pfargs.length-1; i++)
		{
			result = result +'\n' + pfargs[i].asString(depth+1,A) + ",";
		}
		if (pfargs.length >= 1) 
			result = result +'\n' +pfargs[pfargs.length-1].asString(depth+1,A);
		result = result + '\n' +"  "+ tabstring + "| " ;
		result = result +  '\n' +"  "+ tabstring + rbnutilities.arrayToString(quantvars) + " : ";
		result =  result +'\n' +"  "+ tabstring +cconstr.asString(A);
		result =  result +'\n' + tabstring + "}";
		return result;
	}

	public String asString(RelStruc A)
	{
		String result;
		result = mycomb.name+"{";
		for (int i = 0; i<pfargs.length-1; i++)
		{
			result = result +'\n' + pfargs[i].asString(1,A) + ",";
		}
		if (pfargs.length >= 1) 
			result = result +'\n'  +pfargs[pfargs.length-1].asString(1,A);
		result = result +'\n'+ '\t' + "| " ;
		result = result +'\n'+ '\t' + rbnutilities.arrayToString(quantvars) + " : ";
		result =  result +'\n'+ '\t' + cconstr.asString(A);
		result =  result +'\n'+ "}";
		return result;
	}

	/** Returns the combination function used in this probform according to 
	 * the integer encoding:
	 *  N-or = 1 (= static int ProbFormCombFunc.NOR);
	 *  Mean = 2 (= static int ProbFormCombFunc.MEAN);
	 * InvSum = 3 (= static int ProbFormCombFunc.INVSUM);
	 * ESUM = 3 (= static int ProbFormCombFunc.INVSUM);
	 */
	public int myCombInt(){
		return mycombInt;
	}

	/** Returns the number of probability formulas in the argument of this formula's
	 * combination function
	 */
	public int numPFargs(){
		return pfargs.length;
	}

	/** Returns the i'th probability formula in the argument of this formula's combination
	 * function
	 */
	public ProbForm probformAt(int i){
		return pfargs[i];
	}


	/** Returns the quantvars of this combination function */
	public String[] quantvars(){
		return quantvars;
	}

	public ProbForm sEval(RelStruc A)
	throws RBNCompatibilityException
	{
		int[][] subslist = A.allTrue(cconstr,quantvars);

		ProbForm[]  sevalpfargs = new ProbForm[pfargs.length*subslist.length];
		for (int i = 0; i<pfargs.length; i++){
			for (int j=0;j<subslist.length;j++){
				sevalpfargs[subslist.length*i+j]=pfargs[i].substitute(quantvars,subslist[j]).sEval(A);
			}
		}

		return new ProbFormCombFunc(mycomb,sevalpfargs,new String[0],new CConstrEmpty());

	}

	/** Returns the set of all tuples in A that satisfy the CConstr of this formula
	 * after the substituion vars/tuple has been performed
	 */
	public int[][] tuplesSatisfyingCConstr(RelStruc A,  String[] vars, int[] tuple)
	throws RBNCompatibilityException
	{
		CConstr subscc = this.cconstr.substitute(vars,tuple);
		return  A.allTrue(subscc,quantvars);
	}

	public void setParameters(String[] params,  double[] values){
		for (int i = 0; i<pfargs.length; i++)
			pfargs[i].setParameters(params,values);
	}

}
