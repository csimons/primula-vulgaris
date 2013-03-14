/*
* OneRelData.java 
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

package RBNpackage;

import java.util.Vector;

import mymath.MyMathOps;
import RBNutilities.rbnutilities;
import myio.*;

import org.dom4j.Element;

/** An object of the class OneRelData represents one observation of 
 * all or some atoms of one probabilistic relation for one given input
 * domain.
 * 
 * @author jaeger
 *
 */
public class OneRelData {
	
	Rel rel;
	/** The default value for atoms of this relation: 'false' or '?' */
	String defaultval;
	
	/**Vector of int[]; elements are maintained in
	 *lexical order: 001 < 010 < 020 etc.
	 */
	Vector<int[]> trueAtoms;  
	Vector<int[]> falseAtoms; 
	/* For relations of arity 0 (globals): r()=true is
	 * represented by trueAtoms = ([0]), falseAtoms = ();
	 * r() = false is represented by trueAtoms = (), falseAtoms = ([0])
	 * r() uninstantiated is represented by trueAtoms = (), falseAtoms = ()
	 */

	OneRelData()
	{
	}

	public OneRelData(Rel r, String dv)
	{
		rel = r;
		defaultval = dv;
		trueAtoms = new Vector<int[]>();
		falseAtoms = new Vector<int[]>();
	}

	/* Returns 1 if this global relation was not already set to
	 * tv; 0 else;
	 */
	int setGlobal(boolean tv){
		int result = 0;
		if (rel.arity != 0){
			throw new RuntimeException("setGlobal applied to relation of arity >0");
		}
		if (tv){
			if (trueAtoms.size()==0){
				falseAtoms = new Vector<int[]>();
				trueAtoms.add(new int[1]);
				result = 1;
			}
		}
		else {
			if (falseAtoms.size()==0){
				trueAtoms = new Vector<int[]>();
				falseAtoms.add(new int[1]);
				result = 1;
			}			

		}
		return result;
	}

	

	void add(int[][] tuples, boolean tv){
		int[] nexttuple;
		int start = 0;
		for (int i=0;i<tuples.length;i++){
			nexttuple = tuples[i];
			start = add(nexttuple,tv,start);
		}
	}

	/* adds tuple; startindex is an index such that
	 * tuple > this.true(false)Atoms[startindex]
	 * Returns position at which tuple was inserted
	 * and -1 if tuple was already there.
	 */
	public int add(int[] tuple, boolean tv, int startindex)
	{
		int pos = startindex;
		delete(tuple,!tv);
		Vector<int[]> atoms;
		if (tv) atoms = trueAtoms;
		else atoms = falseAtoms;
		boolean containsalready = false;
		if (atoms.size()==0)
			atoms.add(tuple);
		else{
			int[] nexttup = (int[])atoms.elementAt(startindex);
			boolean movright = false;
			int compval=rbnutilities.arrayCompare(tuple,nexttup);
			if (compval==-1)
				movright = true;
			if (compval==0)
				containsalready=true;
			while (movright && pos < atoms.size()-1){
				pos++;
				nexttup = (int[])atoms.elementAt(pos);
				compval = rbnutilities.arrayCompare(tuple,nexttup);
				if (compval != -1)
					movright = false;
				if (compval == 0)
					containsalready = true;
			}
			if (pos==atoms.size()-1){
				if (compval==-1)  // last element in atoms smaller than
					// new tuple
					atoms.add(tuple);
				if (compval==1)  // last element in atoms larger than
					// new tuple
					atoms.add(pos,tuple);
			}
			else{
				if (!containsalready)
					atoms.add(pos,tuple);
			}
		}
		if (containsalready){
		return -1;
		}
		else return pos;
	}


	/** Returns all the atoms instantiated to true as 
	 * a vector of int[]. Objects are represented by
	 * their internal index
	 */ 
	public Vector<int[]> allTrue(){
		return trueAtoms;
	}

	public int numtrue(){
		return trueAtoms.size();
	}

	public int numfalse(){
		return falseAtoms.size();
	}

	/** Returns all the atoms instantiated to false as 
	 * a vector of int[]. Objects are represented by
	 * their internal index
	 */ 
	public Vector<int[]>  allFalse(){
		return falseAtoms;
	}

	/** Returns all the atoms which are not instantiated
	 * to either true or false. d is the domainsize, i.e.
	 * the maximal index of an object to be considered.
	 */
	public Vector<int[]>  allUnInstantiated(int d){
		Vector<int[]>  result = new Vector<int[]> ();
		int[] nextatom;
		int nextintrue = 0;
		int nextinfalse = 0;
		for (int i=0;i< MyMathOps.intPow(d,rel.getArity());i++){
			nextatom = rbnutilities.indexToTuple(i,rel.getArity(),d);
			if (nextintrue < trueAtoms.size() && 
					rbnutilities.arrayEquals(nextatom,(int[])trueAtoms.elementAt(nextintrue)))
				nextintrue++;
			else
				if (nextinfalse < falseAtoms.size() && 
						rbnutilities.arrayEquals(nextatom,(int[])falseAtoms.elementAt(nextinfalse)))
					nextinfalse++;
				else 
					result.add(nextatom);
		}
		return result;
	}

	/** Returns all the atoms instantiated to true as 
	 * a vector of strings. Objects are represented by
	 * their name in structure A
	 */ 
	public Vector<String> allTrue(RelStruc A){
		Vector<String>  result = new Vector<String> ();
		for (int i=0;i<trueAtoms.size();i++)
			result.add( A.namesAt( (int[])trueAtoms.elementAt(i) ));
		return result;
	}

	/** Returns all the atoms instantiated to false as 
	 * a vector of strings. Objects are represented by
	 * their name in structure A
	 */ 
	public Vector<String>  allFalse(RelStruc A){
		Vector<String>  result = new Vector<String> ();
		for (int i=0;i<falseAtoms.size();i++)
			result.add( A.namesAt( (int[])falseAtoms.elementAt(i) ));
		return result;
	}

	/** Delete all atoms containing a 
	 * @param a
	 */
	public void delete(int a){
		int pos = 0;
		int[] nextatom;
		while (pos < trueAtoms.size()){
			nextatom = trueAtoms.elementAt(pos);
			if (rbnutilities.inArray(nextatom,a))
				trueAtoms.remove(pos);
			else 
				pos++;
		}
		pos = 0;
		while (pos < falseAtoms.size()){
			nextatom = falseAtoms.elementAt(pos);
			if (rbnutilities.inArray(nextatom,a))
				falseAtoms.remove(pos);
			else
				pos++;
		}
	}

	public void delete(int[] tuple,boolean tv)
	{
		Vector<int[]> atoms;
		if (tv) atoms = trueAtoms;
		else atoms = falseAtoms;
		int[] currtuple;
		int i = 0;
		while (i<atoms.size()){
			currtuple =  (int[])atoms.elementAt(i);
			if (rbnutilities.arrayEquals(currtuple,tuple)) atoms.remove(i);
			else i++;
		}
	}

	public void delete(int[][] tuples,boolean tv)
	{
		Vector<int[]>  atoms;
		if (tv) atoms = trueAtoms;
		else atoms = falseAtoms;
		int[] nexttuple;
		int removeindex = 0;
		int i = 0;
		int compval;
		while (i<atoms.size() && removeindex < tuples.length){
			nexttuple =  (int[])atoms.elementAt(i);
			compval = rbnutilities.arrayCompare(tuples[removeindex],nexttuple);
			switch (compval){
			case 0:
				atoms.remove(i);
				removeindex++;
				break;
			case 1:
				removeindex++;
				break;
			case -1:
				i++;
			}

		}
	}


	public Rel rel(){
		return rel;
	}

	public String dv(){
		return defaultval;
	}


	public String printAsString(RelStruc A, String pref){
		/* pref is a string prefixed to every result line
		 * used for example to prefix the gnuplot comment symbol
		 * when result is written into a logfile used for plotting
		 */
		String result = "";
		for (int j=0;j<trueAtoms.size();j++){
			result = result + pref +  rel.name.name
			+ A.namesAt((int[])trueAtoms.elementAt(j))+ " = true"
			+ '\n';
		}
		for (int j=0;j<falseAtoms.size();j++){
			result = result + pref +  rel.name.name
			+ A.namesAt((int[])falseAtoms.elementAt(j)) + " = false"
			+ '\n';
		}
		return result;
	}

	int truthValueOf(int[] tuple)
	{
		if (rel.arity ==0){
			if (trueAtoms.size() > 0)
				return 1;
			if (falseAtoms.size() >0)
				return 0;
			return -1;
		}
		else {
			int result = -1;
			for (int i = 0; i<trueAtoms.size();i++){
				if (rbnutilities.arrayEquals((int[])trueAtoms.elementAt(i),tuple)) result = 1;
			}
			for (int i = 0; i<falseAtoms.size();i++)
			{
				if (rbnutilities.arrayEquals((int[])falseAtoms.elementAt(i),tuple)) result = 0;
			}
			if (result == -1 && defaultval.equals("false"))
				result =0;
			return result;
		}
	}

	boolean isEmpty(){
		if (trueAtoms.size()>0 || falseAtoms.size()>0) return false;
		else return true;
	}

	/**Returns the binary tuples from the specified node to some other node
    *This method is usable ONLY with binary relations
    */
    public Vector getBinDirs(int node){
      Vector<int[]> hits = new Vector<int[]>();
      for(int i=0; i<trueAtoms.size(); ++i){
        int[] temp = (int[])trueAtoms.elementAt(i);
        if(temp[0] == node)
          hits.addElement(temp);
      }
      return hits;
    }

    public void addRelData(Element el, RelStruc struc){
    	for (int i=0;i<trueAtoms.size();i++){
    		Element dl = el.addElement("d");
    		dl.addAttribute("rel", rel.name.name);
    		dl.addAttribute("args", struc.namesAt(trueAtoms.elementAt(i)));
    		dl.addAttribute("val", "true");
    	}
    	if (defaultval != "false"){
    		for (int i=0;i<falseAtoms.size();i++){
    			Element dl = el.addElement("d");
    			dl.addAttribute("rel", rel.name.name);
    			dl.addAttribute("args", struc.namesAt(falseAtoms.elementAt(i)));
    			dl.addAttribute("val", "false");
    		}
    	}

    }
    
    /**
     * Replaces all arguments b of trueAtoms and falseAtoms lists
	 * by b-1 if b>a (needed after the deletion of node with index a from
	 * the underlying SparseRelStruc)
     * @param a
     */
    public void shiftArgs(int a){
    	int[] currtuple;
    	if (rel.arity != 0){
    		for (int i=0;i<trueAtoms.size();i++){
    			currtuple = (int[])trueAtoms.elementAt(i);
    			rbnutilities.arrayShiftArgs(currtuple,a);
    		}
    		for (int i=0;i<falseAtoms.size();i++){
    			currtuple = (int[])falseAtoms.elementAt(i);
    			rbnutilities.arrayShiftArgs(currtuple,a);
    		}
    	}
    }
}
