/*
* OneStrucData.java 
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


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import RBNExceptions.*;
import RBNio.FileIO;
import RBNutilities.rbnutilities;

/** An object of the class OneStrucData represents one (partial) observation of 
 * a given set of relations for one given input domain. The set of relations
 * can either be the set of predefined relations in an input domain (then 
 * OneStrucData is the main part of the specification of the input domain), or
 * the set of probabilistic relations (then OneStrucData is the main part of 
 * the specification of a data case, or of evidence).
 * 
 * @author jaeger
 *
 */

public class OneStrucData {

	Vector<OneRelData> allonedata;
	
	public OneStrucData(){
		allonedata = new Vector<OneRelData>();
	}
	
	public OneStrucData(Vector<OneRelData> alldat){
		allonedata = alldat;
	}
	
	public void add(OneRelData ord){
		allonedata.add(ord);
	}

	public void setData(Rel r, int[] args, boolean tv)
	throws RBNIllegalArgumentException
	{
		OneRelData ord = find(r);
		if (ord == null)
			throw new RBNIllegalArgumentException("Cannot find relation " + r.printname());
		ord.add(args,tv,0);
	}
	
	public void setData(String relname, int[] args, boolean tv)
	throws RBNIllegalArgumentException
	{
		OneRelData ord = find(relname);
		if (ord == null)
			throw new RBNIllegalArgumentException("Cannot find relation " + relname);
		ord.add(args,tv,0);
	}
	
	
	
	/** Finds the OneRelData with name 
	 * relname
	 * @param relname
	 * @return
	 */
	public OneRelData find(String relname){
		for (int i=0;i<allonedata.size();i++){
			if (allonedata.elementAt(i).rel().printname().equals(relname))
				return allonedata.elementAt(i);
		}
		return null;
	}
	
	public OneRelData find(Rel r){
		for (int i=0;i<allonedata.size();i++){
			if (allonedata.elementAt(i).rel().equals(r))
				return allonedata.elementAt(i);
		}
		return null;
	}
	
	public void add(Atom at, int tv, String dv){
		switch(tv){
		case 0:
			add(at.rel,at.args,false,dv);
			break;
		case 1:
			add(at.rel,at.args,true,dv);
			break;
		default:
			System.out.println("Cannot add truthvalue " + tv + " to instantiation");
		}
	}



	
	public void add(Atom at, boolean tv,String dv){
		add(at.rel,at.args,tv,dv);
	}

	/** Returns 1 if r,tuple,tv was not already in the data;
	 * 0 otherwise.
	 * @param r
	 * @param tuple
	 * @param tv
	 * @param dv
	 * @return
	 */
	public int add(Rel r, int[] tuple, boolean tv, String dv)
	{
		int result = 0;
		int temp;
		OneRelData thisrelinst = find(r);
		if (thisrelinst == null){
			thisrelinst = new OneRelData(r,dv);
			allonedata.add(thisrelinst);
		}
		if (r.arity == 0)
			return thisrelinst.setGlobal(tv);
		else{
			temp = thisrelinst.add(tuple,tv,0);
			if (temp >= 0)
				return 1;
			else return 0;
		}
	}

	public void add(Rel r, int[][] tuples, boolean tv,String dv)
	{
		OneRelData thisrelinst = find(r);
		if (thisrelinst != null){
			thisrelinst.delete(tuples,!tv);
			thisrelinst.add(tuples,tv);
		}
		else{
			thisrelinst = new OneRelData(r,dv);
			allonedata.add(thisrelinst);
			thisrelinst.add(tuples,tv);
		}
	}


	/** Returns all tuples that are instantiated to true in relation r 
	 * Tuples represented as integer arrays, using the internal indices of 
	 * objects
	 */
	public Vector<int[]> allTrue(Rel r){
		OneRelData ori = find(r);
		if (ori != null)
			return ori.allTrue();
		else
			return new Vector<int[]>();
	}

	/** Returns all tuples that are instantiated to false in relation r 
	 * Tuples represented as integer arrays, using the internal indices of 
	 * objects
	 */
	public Vector<int[]> allFalse(Rel r){
		OneRelData ori = find(r);
		if (ori != null)
			return ori.allFalse();
		else
			return new Vector<int[]>();

	}

//	/** Returns all tuples that are instantiated to false in relation r 
//	 * Tuples represented as integer arrays, using the internal indices of 
//	 * objects
//	 */
//	public Vector<int[]> allUnInstantiated(Rel r){
//		OneRelData ori = findRel(r);
//		if (ori != null)
//			return ori.allFalse();
//		else
//			return new Vector<int[]>();
//
//	}

	/** Returns all tuples that are instantiated to true in relation r 
	 * Tuples represented as string arrays, using the names of objects
	 * as defined in A
	 */
	public Vector<String> allTrue(Rel r, RelStruc A){
		OneRelData ori = find(r);
		if (ori != null)
			return ori.allTrue(A);
		else
			return new Vector<String>();

	}

	/** Returns all tuples that are instantiated to false in relation r 
	 * Tuples represented as string arrays, using the names of objects
	 * as defined in A
	 */
	public Vector<String> allFalse(Rel r, RelStruc A){
		OneRelData ori = find(r);
		if (ori != null)
			return ori.allFalse(A);
		else
			return new Vector<String>();

	}

//	public void reset(){
//		allonedata = new Vector();
//	}

	public boolean isEmpty(){
		return ((allonedata == null) || allonedata.isEmpty());
	}

//	/** Returns the OneRelData for relation r if exists, otherwise
//	 * returns null
//	 */
//	private OneRelData findRel(Rel r)
//	{
//		OneRelData thisrelinst = null;
////		System.out.print("looking in " + this.toString() + " for " + r.toStringWArity());
//		for (int i=0;i<allonedata.size();i++)
//		{
//			if (((OneRelData)allonedata.elementAt(i)).rel.equals(r))
//			{
//				thisrelinst = (OneRelData)allonedata.elementAt(i);
//			}
//		}
//
//		return thisrelinst;
//	}



	public void delete(Atom at){
		delete(at.rel,at.args);
	}


	public void delete(Rel r, int[] tuple)
	{
		delete(r,tuple,true);
		delete(r,tuple,false);
	}

	public void delete(Rel r, int[] tuple, boolean tv){
		OneRelData thisrelinst = find(r);

		if (thisrelinst != null){
			thisrelinst.delete(tuple,tv);
			if (thisrelinst.isEmpty()) allonedata.remove(thisrelinst);
		}
	}

//	public void delete(int a){
//		// delete all InstAtoms with a in the arguments
//		for (int i=0;i<allonedata.size();i++){
//			OneRelData thisrelinst = (OneRelData)allonedata.elementAt(i);
//			int pos = 0;
//			while (pos < thisrelinst.trueAtoms.size())
//				if (rbnutilities.inArray((int[])thisrelinst.trueAtoms.elementAt(pos),a))
//					thisrelinst.trueAtoms.remove(pos);
//				else pos++;
//			pos = 0;
//			while (pos < thisrelinst.falseAtoms.size())
//				if (rbnutilities.inArray((int[])thisrelinst.falseAtoms.elementAt(pos),a))
//					thisrelinst.falseAtoms.remove(pos);
//				else pos++;
//			if (thisrelinst.isEmpty()) allonedata.remove(thisrelinst);
//		}
//	}

	/** Delete all atoms containing a and subtract 1 from
	 * all elements with index > a
	 * @param a
	 */
	public void deleteShift(int a){
		for (int i=0;i<allonedata.size();i++){
			OneRelData thisrelinst = (OneRelData)allonedata.elementAt(i);
			thisrelinst.delete(a);
			thisrelinst.shiftArgs(a);
			if (thisrelinst.isEmpty()) allonedata.remove(thisrelinst);
		}
	}


	/** delete all instantiations of the relation relname */
	public void delete(Rel r){
		OneRelData thisrelinst = find(r);
		if (thisrelinst != null)
			allonedata.remove(find(r));
		else
			System.out.println("relation not found");
	}


	public void shiftArgs(int a){
		// Replaces all arguments b of trueAtoms and falseAtoms lists
		// by b-1 if b>a (needed after the deletion of node with index a from
		// the underlying SparseRelStruc)
		for (int k=0;k<allonedata.size();k++){
			OneRelData thisrelinst = (OneRelData)allonedata.elementAt(k);
			thisrelinst.shiftArgs(a);
		}
	}




	private OneRelData copyOneRelData(OneRelData clonethis){

		OneRelData result = new OneRelData(clonethis.rel(), clonethis.dv());

		for (int i=0;i<clonethis.trueAtoms.size();i++)
			result.trueAtoms.add(rbnutilities.clonearray((int[])clonethis.trueAtoms.elementAt(i)));
		for (int i=0;i<clonethis.falseAtoms.size();i++)
			result.falseAtoms.add(rbnutilities.clonearray((int[])clonethis.falseAtoms.elementAt(i)));
		return result;
	}

	public OneStrucData copy(){
		OneStrucData result = new OneStrucData();
		for (int i=0;i<this.allonedata.size();i++)
			result.allonedata.add(this.copyOneRelData((OneRelData)this.allonedata.elementAt(i)));
		return result;
	}



	public String printAsString(RelStruc A,String pref){
		String result = "";
		for (int i=0;i<allonedata.size();i++){
			OneRelData thisrelinst = (OneRelData)allonedata.elementAt(i);
			result = result + thisrelinst.printAsString(A,pref);
		}
		return result;
	}

	public String printSummary(){
		String result ="";
		OneRelData thisrelinst;
		for (int i=0;i<allonedata.size();i++){
			thisrelinst = (OneRelData)allonedata.elementAt(i);
			result = result + thisrelinst.rel().printname() +" true: " 
			+ thisrelinst.numtrue() + " false: " + thisrelinst.numfalse() + '\n'; 
		}
		return result;
	}

	/** Returns 1,0, or -1 according to whether at is true, false, or 
	 * undefined according to this instantiation.
	 */
	public int truthValueOf(Atom at){
		return truthValueOf(at.rel,at.args);
	}


	/** Returns 1,0, or -1 according to whether r(tuple) is true, false, or 
	 * undefined according to this instantiation.
	 */
	public int truthValueOf(Rel r, int[] tuple)
	{
		OneRelData thisrelinst = find(r);
		if (thisrelinst != null)
		{
			return thisrelinst.truthValueOf(tuple);
		}
		else {
			return -1;
		}
	}

	public Vector<InstAtom> allInstAtoms(){
		// returns Vecor of InstAtom
		Vector<InstAtom>  result = new Vector<InstAtom> ();
		for (int i=0;i<allonedata.size();i++){
			OneRelData thisrelinst = (OneRelData)allonedata.elementAt(i);
			if (thisrelinst.rel.arity == 0){
				if (thisrelinst.trueAtoms.size() > 0)
					result.add(new InstAtom(thisrelinst.rel,new int[0],true));
				if (thisrelinst.falseAtoms.size() > 0)
					result.add(new InstAtom(thisrelinst.rel,new int[0],false));

			}
			else{
				for (int j=0;j<thisrelinst.trueAtoms.size();j++){
					result.add(new InstAtom(thisrelinst.rel,(int[])thisrelinst.trueAtoms.elementAt(j),true));
				}
				for (int j=0;j<thisrelinst.falseAtoms.size();j++){
					result.add(new InstAtom(thisrelinst.rel,(int[])thisrelinst.falseAtoms.elementAt(j),false));
				}
			}
		}
		return result;
	}

	public void saveToBLPDatFile(String filename,int domsize){
		try{
			BufferedWriter logwriter = FileIO.openOutputFile(filename);
			OneRelData ri;
			Rel r;
			String rname;
			Vector atomlist;
			int[] nexttup;
			logwriter.write("begin(1)." + '\n');
			for (int i=0;i< allonedata.size();i++){
				ri = (OneRelData)allonedata.elementAt(i);
				r = ri.rel();
				rname = r.printname();
				atomlist = ri.allTrue();
				for (int j=0;j< atomlist.size();j++){
					nexttup = (int[])atomlist.elementAt(j);
					logwriter.write(rname + "(" + rbnutilities.arrayToString(nexttup,"o") + ") = true." + '\n');
				}
				atomlist = ri.allFalse();
				for (int j=0;j< atomlist.size();j++){
					nexttup = (int[])atomlist.elementAt(j);
					logwriter.write(rname + "(" + rbnutilities.arrayToString(nexttup,"o") + ") = false." + '\n');
				}
				atomlist = ri.allUnInstantiated(domsize);
				for (int j=0;j< atomlist.size();j++){
					nexttup = (int[])atomlist.elementAt(j);
					logwriter.write(rname + "(" + rbnutilities.arrayToString(nexttup,"o") + ") = '$unknown'." + '\n');
				}
			}

			logwriter.write("end(1).");
			logwriter.close();
		}
		catch (IOException e){System.out.println(e);}
	}
	
	public int numRels(){
		return allonedata.size();
	}
	
	 /** returns all the relations with arity 1 */
	 public Vector<Rel> getAttributes(){
		 Rel rel;
		 Vector<Rel> attributes = new Vector<Rel>();
		 for(int i=0; i<allonedata.size(); ++i){
			 rel = allonedata.elementAt(i).rel();
			 if (rel.arity == 1)  //is an attribute
				 attributes.addElement(rel);
		 }
		 return attributes;
	 }

	 /**  returns all the relations with arity 2 */
	 public Vector<Rel> getBinaryRelations(){
		 Rel rel;
		 Vector<Rel> attributes = new Vector<Rel>();
		 for(int i=0; i<allonedata.size(); ++i){
			 rel = allonedata.elementAt(i).rel();
			 if (rel.arity == 2)  
				 attributes.addElement(rel);
		 }
		 return attributes;
	 }
	 
	 /**  returns all the relations with arity >=3 */
	 public Vector<Rel> getArbitraryRelations(){
		 Rel rel;
		 Vector<Rel> attributes = new Vector<Rel>();
		 for(int i=0; i<allonedata.size(); ++i){
			 rel = allonedata.elementAt(i).rel();
			 if (rel.arity >= 3)  
				 attributes.addElement(rel);
		 }
		 return attributes;
	 }

	 public int size(){
		 return allonedata.size();

	 }
	 
	 public Rel relAt(int i){
		 return allonedata.elementAt(i).rel();
	 }
	 
	 public void addRelation(Rel r, String dv){
		 if (find(r) == null)
			 allonedata.add(new OneRelData(r,dv));
	 }
	 
	 public OneRelData dataAt(int i){
		 return allonedata.elementAt(i);
	 }
	 
//	 private Document toDocument(RelStruc struc){
//		 Document result= DocumentHelper.createDocument();
//		 Element root = result.addElement( "root" );
//		 struc.addDomainDec(root);
//		 Element reldecs = root.addElement("Relations");
//		 for (int i=0;i<allonedata.size();i++)
//			 relAt(i).addRelHeader(reldecs,allonedata.elementAt(i).dv());
//		 Element dat = root.addElement("Data");
//		 for (int i=0;i<allonedata.size();i++)
//			 allonedata.elementAt(i).addRelData(dat,struc);
//		 return result;
//	 }

	 public void addAtomsToElement(Element el, RelStruc struc){
		 for (int i=0;i<allonedata.size();i++)
			 allonedata.elementAt(i).addRelData(el,struc);
	 }

	 
	 /** The RelStruc argument is used to save the data using the 
	  * real names for the objects. If struc=null then objects will
	  * be represented using their internal integer indices.
	  * @param filename
	  * @param struc
	  */
//	 public void saveToRDEF(String filename, RelStruc struc){
//		 try{
//			 FileWriter filwrt = new FileWriter(filename);
//			 saveToRDEF(filwrt,struc);
//		 }
//		 catch (Exception e) {
//			 System.err.println(e);	 
//		 }
//	 }
//
//
//	 public void saveToRDEF(File file, RelStruc struc){
//		 try{
//			 FileWriter filwrt = new FileWriter(file);
//			 saveToRDEF(filwrt,struc);
//		 }
//		 catch (Exception e) {
//			 System.err.println(e);	 
//		 }
//	 }
//
//
//	 public void saveToRDEF(FileWriter fwriter, RelStruc struc){
//		 try{
//			 XMLWriter writer = new XMLWriter(
//					 fwriter,
//					 new OutputFormat("   ", true)
//			 );
//			 Document doc = this.toDocument(struc);
//			 writer.write( doc);
//			 writer.close();
//		 }
//		 catch (Exception e) {
//			 System.err.println(e);
//		 }
//	 }

	 public Vector<Rel> getRels(){
		 Vector<Rel> result = new Vector<Rel>();
		 for (int i=0;i<allonedata.size();i++){
			result.add(allonedata.elementAt(i).rel()); 
		 }
		 return result;
	 }
	 
	 public String dvAt(int i){
		 return allonedata.elementAt(i).dv();
	 }
}


