/*
 * RBN.java
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

import java.io.*;

import RBNio.*;
import RBNutilities.rbnutilities;
import RBNExceptions.*;
import myio.StringOps;



class RBNelement
{
	Rel prel;           // Relation
	String[] arguments; // Argument list. Must be superset of
	// free variables of prfrm.
	ProbForm prfrm;

	public RBNelement()
	{
	}

	public RBNelement(Rel r, ProbForm pf)
	{
		prel =r;
		prfrm = pf;
	}

	protected String[] arguments() {
		return arguments;
	}

	protected Type[] types(){
		return prel.getTypes();
	}
	
	protected Rel rel() {
		return prel;
	}
}

public class RBN extends java.lang.Object {

	private RBNelement[] elements;

	/** Creates new RBN */
	public RBN() {
	}

	public RBN(int i){
		elements = new RBNelement[i];
		for (int j=0;j<i;j++)
			elements[j] = new RBNelement();
	}

	public RBN(String filename){
		RBNReader rbnrdr =  new RBNReader();
		RBN rbn = new RBN();
		try{
			rbn = rbnrdr.ReadRBNfromFile(filename);
		}
		catch (RBNSyntaxException e){System.out.println(e);}
		catch (IOException e){System.out.println(e);};
		elements = rbn.elements();
	}

	/** @author keith cascio
    	@since 20060515 */
    	public RBN( RBN toCopy ){
    		if( toCopy.elements != null ) this.elements = (RBNelement[]) toCopy.elements.clone();
    	}

    	public RBNelement[] elements(){
    		return elements;
    	}

    	public String[] argumentsAt(int i){
    		return elements[i].arguments();
    	}

    	public boolean multlinOnly(){
    		boolean result = true;
    		for (int i=0;i<elements.length;i++)
    			if (!elements[i].prfrm.multlinOnly())
    				result = false;
    		return result;
    	}

    	/** Returns the index of the relation r in elements;
    	 * Returns -1 if r not found in probRels
    	 */
    	private int indexOf(Rel r){
    		boolean found = false;
    		int ind = 0;
    		while (!found && ind<elements.length){
    			if (elements[ind].rel().equals(r))
    				found = true;
    			if (!found)
    				ind++;
    		}
    		if (found)
    			return ind;
    		else
    			return -1;
    	}

    	public int NumPFs()
    	{
    		return elements.length;
    	}

    	public Rel[] Rels()
    	{
    		Rel[] result = new Rel[elements.length];
    		for (int i=0;i<elements.length;i++)
    			result[i]=elements[i].rel();
    		return result;
    	}


    	public Rel relAt(int i)
    	{
    		return elements[i].rel();
    	}


    	public ProbForm ProbFormAt(int i)
    	{
    		return elements[i].prfrm;
    	}

    	/** Returns the probability formula for relation r */
    	public ProbForm probForm(Rel r){
    		int ind = indexOf(r);
    		if (ind >= 0)
    			return ProbFormAt(ind);
    		else
    			return null;
    	}

    	public String NameAt(int i)
    	{
    		return elements[i].rel().name.name;
    	}

    	public String[] ArgsAt(int i)
    	{
    		return elements[i].arguments;
    	}

    	/** Returns the argument tuple for the ProbForm for r */
    	public String[] args(Rel r){
    		int ind = indexOf(r);
    		if (ind >= 0)
    			return ArgsAt(ind);
    		else
    			return null;
    	}

    	public void insertRel(Rel r, int i)
    	{
    		elements[i].prel=r;
    	}
    	public void InsertArguments(String[] ags, int i)
    	{
    		elements[i].arguments = ags;
    	}

    	public void InsertProbForm(ProbForm pf, int i)
    	{
    		elements[i].prfrm = pf;
    	}

    	/** Returns all the parameters contained in probability formulas
    	 * in the RBN. Two occurrences of parameters with the same name 
    	 * are included only once.
    	 * @return
    	 */
    	public String[] parameters(){
    		String[] result = new String[0];
    		for (int i=0;i<elements.length;i++){
    			result = rbnutilities.arraymerge(result,this.ProbFormAt(i).parameters());
    		}
    		return result;
    	}

    	public void saveToFile(File rbnfile){
    		try{
    			FileWriter filwrt = new FileWriter(rbnfile);
    			for (int i=0;i<elements.length;i++){
    				filwrt.write(NameAt(i));
    				filwrt.write("(");
    				String[] args = ArgsAt(i);
    				Type[] types = typesAt(i);
    				for (int j=0;j<args.length;j++){
    					if (!(types[j] instanceof TypeDomain))
    						filwrt.write("[" + types[j].getName() + "]");
    					filwrt.write(args[j]);
    					if (j<args.length-1)
    						filwrt.write(",");
    				}
    				filwrt.write(")");
    				filwrt.write("=" + '\n');
    				filwrt.write(ProbFormAt(i).asString()+ ";" + '\n');

    			}
    			filwrt.close();
    		}
    		catch (Exception e) {
    			System.err.println(e);	 
    		}
    	}

    	/** Sets all occurrences of parameters appearing in params 
    	 * to their corresponding value in values. params and values
    	 * must be arrays of the same length
    	 * @param params
    	 * @param values
    	 */
    	public void setParameters(String[] params,  double[] values){
    		for (int i=0;i<elements.length ; i++)
    			ProbFormAt(i).setParameters(params,values);
    	}
    	
    	private Type[] typesAt(int i){
    		return elements[i].types();
    	}
}

