/*
 * Instantiation.java
 *
 * 
 * Copyright (C) 2003 Max-Planck-Institut fuer Informatik,
 *                    Helsinki Institute for Information Technology
 *
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
import java.io.File;
import RBNLearning.RelData;
import RBNLearning.RelDataForOneInput;

/* An Instantiation is a list of
 * truth assignments to selected ground
 * atoms constructed from RSymbs and
 * dom - elements
 */
public class Instantiation extends java.lang.Object
{
	
	OneStrucData mydata;
	
	/** @author keith cascio
	@since 20061020 */
	public enum SetRelationship {
		different(      false, false ),
		strictsubset(   false, true  ),
		strictsuperset( true,  false ),
		identical(      true,  true  );

		public boolean isSuperset(){
			return myFlagSuper;
		}

		public boolean isSubset(){
			return myFlagSub;
		}

		public static SetRelationship forInclusion( boolean flagSuper, boolean flagSub ){
			return values()[ (flagSuper ? 2 : 0) + (flagSub ? 1 : 0) ];
		}

		private SetRelationship( boolean flagSuper, boolean flagSub ){
			this.myFlagSuper = flagSuper;
			this.myFlagSub   = flagSub;
		}

		private boolean myFlagSuper, myFlagSub;
	}

	/** @author keith cascio
	@since 20061020 */
	public boolean containsAll( Instantiation other ){
		if(      other.data().isEmpty() ) return true;
		else if(  mydata.isEmpty() ) return false;

		return new HashSet(  mydata.allInstAtoms() ).containsAll( new HashSet( other.data().allInstAtoms() ) );
	}

	/** @author keith cascio
	@since 20061020 */
	public SetRelationship characterize( Instantiation other ){
		if(      mydata.isEmpty()  ) return other.data().isEmpty() ? SetRelationship.identical : SetRelationship.strictsuperset;
		else if( other.data().isEmpty() ) return SetRelationship.strictsubset;

		Set<InstAtom>    myatoms = new HashSet<InstAtom>(  mydata.allInstAtoms() );
		Set<InstAtom> otheratoms = new HashSet<InstAtom>( other.data().allInstAtoms() );

		boolean flagSuper = otheratoms.containsAll(    myatoms );
		boolean flagSub   =    myatoms.containsAll( otheratoms );

		return SetRelationship.forInclusion( flagSuper, flagSub );
	}



	/** Creates new Instantiation */
	public Instantiation() {
		mydata = new OneStrucData();
	}

//	/** @author keith cascio
//    	@since 20060515 */
//	public Instantiation( Instantiation toCopy ){
//		if( toCopy.relinsts != null ) this.relinsts = new Vector( toCopy.relinsts );
//		else this.relinsts = new Vector();
//	}
	
	public Instantiation( Instantiation toCopy ){
		if( toCopy.data() != null ) this.mydata = toCopy.data().copy();
		else this.mydata = new OneStrucData();
	}

	public Instantiation(OneStrucData dat){
	 mydata = dat;
	}
	
	/** @author keith cascio
    	@since 20060515 */
	public int size(){
		return (this.mydata == null) ? 0 : this.mydata.numRels();
	}

	public void add(Atom at, int tv, String dv){
		mydata.add(at,tv,dv);
	}


	public void add(Atom at, boolean tv,String dv){
		add(at.rel,at.args,tv,dv);
	}

	public void add(Rel r, int[] tuple, boolean tv, String dv)
	{
		mydata.add(r, tuple, tv, dv);
	}

	public void add(Rel r, int[][] tuples, boolean tv, String dv)
	{
		mydata.add(r, tuples, tv, dv);
	}


	/** Returns all tuples that are instantiated to true in relation r 
	 * Tuples represented as integer arrays, using the internal indices of 
	 * objects
	 */
	public Vector<int[]> allTrue(Rel r){
		return mydata.allTrue(r);
	}
//
	/** Returns all tuples that are instantiated to false in relation r 
	 * Tuples represented as integer arrays, using the internal indices of 
	 * objects
	 */
	public Vector<int[]> allFalse(Rel r){
		return mydata.allFalse(r);
	}

	/** Returns all tuples that are instantiated to false in relation r 
	 * Tuples represented as string arrays, using the names of objects
	 * as defined in A
	 */
	public Vector<String> allFalse(Rel r, RelStruc A){
		return mydata.allFalse(r, A);
	}

	public void reset(){
		mydata = new OneStrucData();
	}

	public boolean isEmpty(){
		return mydata.isEmpty();
	}

	public void delete(Atom at){
		mydata.delete(at);
	}


	public void delete(Rel r, int[] tuple)
	{
		mydata.delete(r, tuple);
	}

	public void delete(Rel r, int[] tuple, boolean tv){
		mydata.delete(r, tuple, tv);
	}

	public void deleteShift(int a){
		mydata.deleteShift(a);
	}

	public void shiftArgs(int a){
		mydata.shiftArgs(a);
	}

	public Instantiation copy(){
		return new Instantiation(mydata.copy());
	}
//
////	public void showInst(){
////	for (int i=0;i<relinsts.size();i++){
////	OneRelInst thisrelinst = (OneRelInst)relinsts.elementAt(i);
////	thisrelinst.showOneRelInst();
////	System.out.println();
////	}
////	}
//
	public String printAsString(RelStruc A,String pref){
		return mydata.printAsString(A, pref);
	}
//
//	public String printSummary(){
//		String result ="";
//		OneRelInst thisrelinst;
//		for (int i=0;i<relinsts.size();i++){
//			thisrelinst = (OneRelInst)relinsts.elementAt(i);
//			result = result + thisrelinst.rel().printname() +" true: " 
//			+ thisrelinst.numtrue() + " false: " + thisrelinst.numfalse() + '\n'; 
//		}
//		return result;
//	}
//
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
		return mydata.truthValueOf(r, tuple);
	}

	public Vector<InstAtom> allInstAtoms(){
		return mydata.allInstAtoms();
	}

//	public void saveToRDEF(String filename, RelStruc A){
//		mydata.saveToRDEF(filename, A);
//	}
	
	public void saveToBLPDatFile(String filename,int domsize){
		mydata.saveToBLPDatFile(filename, domsize);
	}
	
	public OneStrucData data(){
		return mydata;
	}

	public void saveToRDEF(File savefile,RelStruc rs){
		RelData thisasdata = new RelData();
		RelDataForOneInput rdfoi = new RelDataForOneInput(rs);
		rdfoi.addCase(mydata);
		thisasdata.add(rdfoi);
		thisasdata.saveToRDEF(savefile);
	}
}
