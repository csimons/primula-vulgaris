/*
 * OrdStruc.java
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


public class OrdStruc extends RelStruc
{


    public OrdStruc(int d){
	super(d);
    }

    /** @author keith cascio
    	@since 20060515 */
    public OrdStruc( OrdStruc toCopy ){
    	super( toCopy.elementnames, toCopy.mydata, toCopy.coordinates );
    	this.dom = toCopy.dom;
    }

    /** @author keith cascio
    	@since 20060515 */
    public Object clone(){
    	return new OrdStruc( this );
    }

    public String nameAt(int i){
        return Integer.toString(i);
    }


    public void addRelation(Rel r)
    throws RBNCompatibilityException{
	throw new RBNCompatibilityException("Attempt to add a new relation to an OrdStruc!");
    }

    public int[][] allTrue(CConstr cc,String[] vars)
    throws IllegalArgumentException
    {
        // First check whether all relations used in
        // cc are in the signature of this structure
        Rel[] undefinedsymbs = rbnutilities.arraysubstraction(cc.SSymbs,OrdRels);
        if (undefinedsymbs.length>0)
            throw new IllegalArgumentException("Constraint contains symbols [" + rbnutilities.arrayToString(undefinedsymbs) + "] that do not occur in signature [" + rbnutilities.arrayToString(OrdRels) + "]");

        int k = vars.length;
        int m = rbnutilities.IntPow(dom,k);
        int[][] prelimarray = new int[m][k];

        int numtrue = 0; // number of tuples that satisfy cc
        for (int i=0; i<m ; i++)
        {
            int[] thistuple = rbnutilities.indexToTuple(i,k,dom);
            //System.out.println("i: " + i + " tuple: " + rbnutilities.arrayToString(thistuple));
            if (trueCC(cc,vars,thistuple))
            {
                prelimarray[numtrue] = thistuple;
                numtrue++;
            }
        }

        int[][] result = new int[numtrue][k];
        for (int i =0; i<numtrue; i++) result[i]=prelimarray[i];
        //System.out.println("allTrue for " + cc.asString() + " has " + numtrue + " solutions");
        return result;
    }


    public boolean trueCC(CConstr cc, String[] vars, int[] args)
    throws IllegalArgumentException
    {
        if (cc instanceof CConstrEq)
        {
            CConstrEq cceqsub = (CConstrEq)cc.substitute(vars,args);
            if (!rbnutilities.IsInteger(cceqsub.arguments[0]) || !rbnutilities.IsInteger(cceqsub.arguments[1]))
            throw new IllegalArgumentException("Attempt to evaluate non-ground equality " + cceqsub.arguments[0] + "=" + cceqsub.arguments[1]);
            //System.out.println("checking " + Integer.parseInt(ccsub.arguments[0]) + "<" + Integer.parseInt(ccsub.arguments[1]));
            if (Integer.parseInt(cceqsub.arguments[0])==Integer.parseInt(cceqsub.arguments[1])) return true;
            else return false;
        }

        if (cc instanceof CConstrAtom) return trueOrdAtom((CConstrAtom)cc,vars,args);

        if (cc instanceof CConstrAnd)
        {
            return trueCC(((CConstrAnd)cc).C1,vars,args) && trueCC(((CConstrAnd)cc).C2,vars,args);
        };
        if (cc instanceof CConstrOr)
        {
            return trueCC(((CConstrOr)cc).C1,vars,args) || trueCC(((CConstrOr)cc).C2,vars,args);
        };
        if (cc instanceof CConstrNeg)
        {
            return !trueCC(((CConstrNeg)cc).C1,vars,args);
        }
        else return false; // never executed
    }

    public boolean trueGroundCC(CConstr cc){
	return trueCC(cc, new String[0], new int[0]);
    }
}
