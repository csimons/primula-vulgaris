/*
* ProbForm.java 
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



public abstract class ProbForm
{
    
    public Rel[] SSymbs;
    public Rel[] RSymbs;
    
    public ProbForm()
    {}
        
    public abstract String asString(int depth);
    
    public abstract String asString();

    public abstract String asString(int depth, RelStruc A);
    
    public abstract String asString(RelStruc A);

  

    /** Simplify ProbForm by substituting  values of instantiated R-atoms
     * and evaluating subformulas no longer dependent on any uninstantiated
     * R-atom.
     */
    public abstract ProbForm conditionEvidence(RelStruc A, Instantiation inst)
    throws RBNCompatibilityException;

    public abstract ProbForm conditionEvidence(Instantiation inst);
  

    /** Checks whether this prob.form depends on the unknown parameter 'variable'
     * when prob.form is evaluated over input structure A and relative to 
     * instantiation (data) data. Only for ground probforms!
     * 
     * For argument variable = "unknown_atom" the method returns true if
     * this ProbForm depends on a ground atom not instantiated in data
     */
    public abstract boolean dependsOn(String variable, RelStruc A, Instantiation data)
	throws RBNCompatibilityException;

    /** Evaluate this ProbForm for input structure A, instantiation inst, under the
     * substitution tuple for vars. Returns -1 if the value of probform is not defined
     * because it depends on a probabilistic atom not instantiated in inst.
     * If useCurrentCvals=false then also returns -1 if value depends on an unknown parameter.
     * If useCurrentCvals=true, then evaluation at ProbFormConstant's is done with regard to
     * their cval field, even when their paramname != "".
     */
    public abstract double evaluate(RelStruc A, Instantiation inst, String[] vars, int[] tuple, boolean useCurrentCvals)
    throws RBNCompatibilityException;  
    
    /** Evaluate this probform over RelStruc A. For ground atoms on which probform
     * depends, a ComplexBNGroundAtomNode is accessible via 
     * atomhasht (using Atom.asString() as hashcode)
     * If this ComplexBNGroundAtomNode is not instantiated, then
     * the sample method of that node has to be called
     *
     */
    public abstract double evalSample(RelStruc A, Hashtable atomhasht, Instantiation inst, long[] timers)
	throws RBNCompatibilityException;


    /** Returns 0 if this probform evaluates to zero over 
     * structure A and with respect to instantiation inst, but
     * irrespective of any instantiation of other
     * probabilistic atoms. When probform contains unknown parameters,
     * then evaluatesTo is computed with regard to the current
     * setting of cval at the parameter ProbFormConstants.
     * Returns 1 if ... evaluates to one ....
     * Returns -1 if neither of the above
     *
     * When usesampleinst = true, then evaluation is not w.r.t.
     * instantiation inst, but w.r.t. to sampleinst fields at 
     * PFNetworkNodes which are accessible via atomhasht
     */
    public abstract int evaluatesTo(RelStruc A, Instantiation inst, boolean usesampleinst, Hashtable atomhasht) 
	throws RBNCompatibilityException;

    public abstract int evaluatesTo(RelStruc A) throws RBNCompatibilityException;


    /** returns the free variables of the formula */
    public abstract String[] freevars();
    

    /** returns the vector of (ground!) Atoms on which the
     * evaluation of the probform depends
     */
    public abstract Vector makeParentVec(RelStruc A)
	throws RBNCompatibilityException;

    /** same as previous but with respect to the given
     * truth values in the Instantiation argument
     */
    public abstract Vector makeParentVec(RelStruc A, Instantiation inst)
	throws RBNCompatibilityException;

    /** returns true if ProbForm only contains
     * multilinear combination functions
     */
    public abstract boolean multlinOnly();
 
    /** Returns all the parameters that this ProbForm depends on */
    public abstract String[] parameters();

    /**
     * Returns a ProbForm in which the dependence on A
     * is already pre-evaluated (substitution lists in 
     * combination functions, and values of ProbFormSFormula)
     */  
    public abstract ProbForm sEval(RelStruc A) throws RBNCompatibilityException;


    /** returns the formula obtained by substituting
     * args for the vars in the formula.
     * Produces an error if vars are not among 
     * the free variables of the formula
     */
    public abstract ProbForm substitute(String[] vars, int[] args);
    
    public abstract ProbForm substitute(String[] vars, String[] args);
    
    /** Sets all occurrences of parameters appearing in params 
     * to their corresponding value in values. params and values
     * must be arrays of the same length
     * @param params
     * @param values
     */
    public abstract void setParameters(String[] params,  double[] values);


}

