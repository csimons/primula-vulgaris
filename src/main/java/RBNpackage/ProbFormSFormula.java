/*
 * ProbFormSFormula.java
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

import RBNExceptions.*;
import java.util.*;

public class ProbFormSFormula extends ProbForm {

    private CConstr myform;
    
    /** Creates new ProbFormSFormula */
    public ProbFormSFormula() {
    }

    public ProbFormSFormula(CConstr ccon) {
        SSymbs = ccon.SSymbs;
        RSymbs = new Rel[0];
        myform = ccon;
    }
    
    public String[] freevars(){
        return myform.freevars();
    }
        
    public boolean multlinOnly(){
	return true;
    }

    
    public ProbForm substitute(String[] vars, int[] args){
        CConstr subscons = myform.substitute(vars,args);
        return new ProbFormSFormula(subscons);
    }
    
    public ProbForm substitute(String[] vars, String[] args){
        CConstr subscons = myform.substitute(vars,args);
        return new ProbFormSFormula(subscons);
    }
    
    public  Vector makeParentVec(RelStruc A){
	return new Vector();
    }

    public  Vector makeParentVec(RelStruc A, Instantiation inst){
	return new Vector();
    }
    
     public ProbForm conditionEvidence(RelStruc A, Instantiation inst)
     throws RBNCompatibilityException
     // must be ground formula!
    {
	return new ProbFormConstant(evaluate(A));
    }

    public ProbForm conditionEvidence(Instantiation inst){
        return new ProbFormSFormula(myform);
    }
    

    public boolean dependsOn(String variable, RelStruc A, Instantiation data)
	throws RBNCompatibilityException
    {
	return false;
    }

    public double evaluate(RelStruc A, Instantiation inst, String[] vars, int[] tuple, boolean useCurrentCvals)
    throws RBNCompatibilityException
    {
        if(A.trueCC(myform,vars,tuple)) return 1;
        else return 0;
    }
    


	    
    public double evaluate(RelStruc A, String[] vars, int[] tuple)
    throws RBNCompatibilityException
    {
        if(A.trueCC(myform,vars,tuple)) return 1;
        else return 0;
    }
    
    public double evaluate(RelStruc A)
	throws RBNCompatibilityException
    {
        if(A.trueGroundCC(myform)) return 1;
        else return 0;
    }
    

    public  double evalSample(RelStruc A,Hashtable atomhasht,Instantiation inst, long[] timers)
	throws RBNCompatibilityException
    {
	return evaluate(A);
    }


    public int evaluatesTo(RelStruc A)
	throws RBNCompatibilityException
    {
	double eval = evaluate(A);
	if (eval==0) return 0;
	if (eval==1) return 1;
	return -1; // never executed
    }

    public int evaluatesTo(RelStruc A,Instantiation inst, boolean usesampleinst, Hashtable atomhasht)
	throws RBNCompatibilityException
    {
	return evaluatesTo(A);
    }
    
    public String[] parameters(){
	return new String[0];
    }

    public String asString(int depth)
    {
        String tabstring = "";
        for (int i=0;i<depth;i++)
            tabstring = tabstring +"  ";
         
        String result = new String();
        result = tabstring + "sformula(" + myform.asString() +  ")";
        return result;
    }
     
    public String asString(int depth,RelStruc A)
    {
        String tabstring = "";
        for (int i=0;i<depth;i++)
            tabstring = tabstring +"  ";
         
        String result = new String();
        result = tabstring + "sformula(" + myform.asString(A) +  ")";
        return result;
    }
     
    
    public String asString()
    { 
        return "sformula(" + myform.asString()+ ")" ;
    }
     
    public String asString(RelStruc A)
    { 
        return "sformula(" + myform.asString(A)+ ")" ;
    }
     

    public ProbForm sEval(RelStruc A)
	throws RBNCompatibilityException
    {
	return new ProbFormConstant((double)this.evaluatesTo(A));
    }
    
	public void setParameters(String[] params,  double[] values){
	}
}
