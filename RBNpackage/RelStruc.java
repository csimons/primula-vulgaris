/*
 * RelStruc.java
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
import java.awt.Color;
import java.io.*;

import RBNExceptions.*;
import RBNutilities.*;
import RBNio.*;
import RBNLearning.*;

import com.mysql.jdbc.*;
import java.sql.*;

import org.dom4j.Element;

public abstract class RelStruc implements Cloneable{
	
	public static int BLP_FORMAT = 1;
	public static int MLN_FORMAT = 2;
	

	/* Domain of structure is
	 * {0,...,dom-1}
	 */
	public int dom;

	Vector elementnames; // Internally the domainelements of the
	// structure are taken to be the numbers
	// 0..n. 'elementnames[i]' provides an
	// alternative name for i, which has been
	// supplied by the user and is used to
	// denote i in all outputs.
	Vector<int[]> coordinates; // stores (x,y)-coordinates for every domainelement
	// for use in graphical display of structure.

	static Rel[] OrdRels = new Rel[4];

	static{
		OrdRels[0] = new Rel("less",2);
		OrdRels[1] = new Rel("pred",2);
		OrdRels[2] = new Rel("zero",1);
		OrdRels[3] = new Rel("last",1);
	}

//	Vector<RelInt> relinterpretations;  // Vector of RelInts
	
	OneStrucData mydata;

	/** Creates new RelStruc */
	public RelStruc(){
		elementnames       = new Vector();
		coordinates        = new Vector();
		mydata 			   = new OneStrucData();
	}

	/** Creates new RelStruc with a domain of n elements*/
	public RelStruc(int n){
		dom = n;
		elementnames       = new Vector();
		coordinates        = new Vector();
		mydata 			   = new OneStrucData();
	}

	public RelStruc(Vector en, OneStrucData data, Vector coords) {
		//if( (en == null) || (srels == null) || (coords == null) ) throw new IllegalStateException();
		dom = en.size();
		elementnames = en;
		mydata = data;
		coordinates = coords;
	}

	/** @author keith cascio
    	@since 20060515 */
	public RelStruc( RelStruc toCopy ){
		this( toCopy.elementnames, toCopy.mydata, toCopy.coordinates );
	}

	/** @author keith cascio
    	@since 20060515 */
	abstract public Object clone();

	public int domSize(){
		return dom;
	}

	public String nameAt (int i){
		return (String)elementnames.get(i);
	}

	public String namesAt (int[] args){
		String result = "(";

		for (int i=0;i<args.length-1;i++)
			result = result + nameAt(args[i]) + ",";
		if (args.length>0)
			result = result + nameAt(args[args.length-1]);
		result = result +")";
		return result;
	}

	public String[] namesAtAsArray (int[] args){
		String[] result = new String[args.length];
		for (int i=0;i<args.length;i++)
			result[i]=nameAt(args[i]);
		return result;
	}

	private boolean nameExists(String name){
		for (int i=0;i<elementnames.size();i++){
			if (name.equals(nameAt(i))) return true;
		}
		return false;
	}

	public int setName(String name, int i){
		// returns 0 if name already used.
		// does not distinguish case where name is already
		// used at position i, and elementnames.setElementAt(name, i) would
		// only be redundant, but would not introduce
		// any duplicate names.
		if (nameExists(name)) return 0;
		else{
			elementnames.setElementAt(name, i);
			return 1;
		}
	}

//	public RelInt relAt(int i){
//		return relinterpretations.get(i);
//	}

//	public RelInt findRelInt(Rel r)
//	{
//		RelInt result = null;
//		boolean found = false;
//		int ind = 0;
//		while (!found && ind < relinterpretations.size())
//		{
//			if (r.equals((relinterpretations.get(ind)).rel()))
//			{
//				result = relinterpretations.get(ind);
//				found = true;
//			}
//			else ind++;
//		}
//		return result;
//	}


	public void createCoords(){
		int counter = 15;
		while(coordinates.size() < elementnames.size()){
			int[] coords = {50+counter, 50};
			coordinates.add(coords);
			counter = counter + 15;
		}
	}

	public void addNode()
	{
		dom++;
		elementnames.add(Integer.toString(dom));
		coordinates.add(new int[2]);
	}

	public void addNode(String st)
	{
		dom++;
		elementnames.add(st);
		coordinates.add(new int[2]);
	}

	public void addNode(String st, int xc, int yc)
	{
		dom++;
		elementnames.add(st);
		int coords[] = {xc,yc};
		coordinates.add(coords);
	}

	public void addNode(int xc, int yc){
		elementnames.add(Integer.toString(dom));
		dom = dom + 1;
		int[] coords = {xc, yc};
		coordinates.add(coords);
	}



	 public int addTuple(Rel r, int[] tuple)
	{
		 return mydata.add(r,tuple,true,"false");

	}

	 public void deleteTuple(Rel r, int[] tuple)
	 {
		 mydata.delete(r, tuple, true);

	 }

	 public abstract void addRelation(Rel r)
	 throws RBNCompatibilityException;
	 /* Adds r to the RelStruc by initializing a new RelInt for r */


	 public void deleteRelation(Rel r)
	 {
		 mydata.delete(r);
	 }

	 public Vector getCoords(){
		 return coordinates;
	 }

	 public Vector getNames(){
		 return elementnames;
	 }

//	 public Vector getRels(){
//		 return relinterpretations;
//	 }

	 public int getSize(){
		 return dom;
	 }

	 //returns the colours of the attributes of the specified node
	 public Vector getAttributesColors(int n){
		 Rel rel;
		 int[] node = {n};
		 Vector colors = new Vector();
		 Vector<Rel> attributes = getAttributes();
		 for(int i=0; i<attributes.size(); ++i){
			 rel = attributes.elementAt(i);
			 if (mydata.truthValueOf(rel,node) == 1){
				 colors.addElement(rel.color);
			 }
		 }
		 if(colors.size() == 0)
			 colors.addElement(Color.white);
		 return colors;
	 }


	 /** Returns the binary relation tuples from this node to some other node and
	  * the colors of the relations 
	  */
	 public Vector[] getBinaryColors(int node){
		 OneRelData ord;
		 Vector nodes = new Vector();
		 Vector colors = new Vector();
		 for (int i=0;i<mydata.size();i++){
			 ord = mydata.dataAt(i);
			 if (ord.rel().arity == 2){
				 Vector temp = ord.getBinDirs(node);
				 for (int j=0; j<temp.size(); ++j){
					 nodes.addElement((int[])temp.elementAt(j));
					 colors.addElement(ord.rel().color);
				 }
			 }
		 }
		 Vector[] tuplesAndColors = {nodes, colors};
		 return tuplesAndColors;
	 }


	 //returns all the relations with arity 1
	 public Vector<Rel> getAttributes(){
		 return mydata.getAttributes();
	 }


	 //returns all the relations with arity 2
	 public Vector<Rel> getBinaryRelations(){
		 return mydata.getBinaryRelations();
	 }

	 //returns all the relations with arity >= 3
	 public Vector<Rel> getArbitraryRelations(){
		 return mydata.getArbitraryRelations();
	 }




	 /** Returns true if r is one of the OrdRels **/
	 public boolean isOrdRel(Rel r){
		 boolean result = false;
		 for (int i=0;i<OrdRels.length;i++)
			 if (r.equals(OrdRels[i])) 
				 result = true;
		 return result;
	 }


	 public boolean trueCC(CConstr cc, String[] vars, int[] args)
	 throws IllegalArgumentException,RBNCompatibilityException
	 {
		 if (cc instanceof CConstrEmpty){
			 return true;
		 }
		 if (cc instanceof CConstrEq)
		 {
			 CConstrEq cceqsub = (CConstrEq)cc.substitute(vars,args);
			 if (!rbnutilities.IsInteger(cceqsub.arguments[0]) || !rbnutilities.IsInteger(cceqsub.arguments[1]))
				 throw new IllegalArgumentException("Attempt to evaluate non-ground equality " + cceqsub.arguments[0] + "=" + cceqsub.arguments[1]);
			 if (Integer.parseInt(cceqsub.arguments[0])==Integer.parseInt(cceqsub.arguments[1])) return true;
			 else return false;
		 }

		 if (cc instanceof CConstrAtom)
		 {
			 Rel crel = ((CConstrAtom)cc).relation;
			 for (int i=0;i<OrdRels.length;i++)
				 if (crel.equals(OrdRels[i])) return trueOrdAtom((CConstrAtom)cc,vars,args);

			 // perform substitution and check whether atom is ground
			 CConstrAtom ccsub = (CConstrAtom)cc.substitute(vars,args);
			 boolean ground = true;
			 for (int i=0;i<crel.arity;i++)
				 if (!rbnutilities.IsInteger(ccsub.arguments[i]))
					 ground = false;
			 if (!ground)
				 throw new IllegalArgumentException("Attempt to evaluate non-ground atom " + ccsub.asString());


			 int tv = mydata.truthValueOf(crel,rbnutilities.stringArrayToIntArray(ccsub.arguments));
			 if (tv == 1)
				 return true;
			 if (tv == 0)
				 return false;
			 else throw new IllegalArgumentException("Cannot determine truth value of " + ccsub.asString());


		 };

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


	 public boolean trueGroundCC(CConstr cc)
	 throws RBNCompatibilityException
	 {
		 return trueCC(cc, new String[0], new int[0]);
	 }



	 public boolean trueOrdAtom(CConstrAtom at){
		 // check whether at is ground:
		 boolean isground = true;
		 int firstarg;
		 int secondarg;
		 for (int i=0;i<at.arguments.length;i++)
			 if (!rbnutilities.IsInteger(at.arguments[i])) isground = false;
		 if (!isground)
			 throw new IllegalArgumentException("Attempt to evaluate non-ground atom " + at.asString());
		 if (at.relation.equals(OrdRels[0])){
			 firstarg = Integer.parseInt(at.arguments[0]);
			 secondarg = Integer.parseInt(at.arguments[1]);
			 if (firstarg < secondarg) return true;
			 else return false;
		 }
		 if (at.relation.equals(OrdRels[1])){
			 firstarg = Integer.parseInt(at.arguments[0]);
			 secondarg = Integer.parseInt(at.arguments[1]);
			 if (firstarg +1 == secondarg) return true;
			 else return false;
		 }
		 if (at.relation.equals(OrdRels[2])){
			 firstarg = Integer.parseInt(at.arguments[0]);
			 if (firstarg  == 0) return true;
			 else return false;
		 }
		 if (at.relation.equals(OrdRels[3])){
			 firstarg = Integer.parseInt(at.arguments[0]);
			 if (firstarg  == dom-1) return true;
			 else return false;
		 }
		 throw new RuntimeException("Program should never reach this line!");
	 }


	 public boolean trueOrdAtom(CConstrAtom at, String[] vars, int[] args){
		 CConstrAtom gat = (CConstrAtom)at.substitute(vars,args);
		 return trueOrdAtom(gat);
	 }

 
	 /** Returns all elements in the domain of type rtype.
	  * Throws RBNIllegalArgumentException if RelStruc does
	  * not contain a unary relation corresponding to the
	  * relation defining rtype.
	  * 
	  * @param rtype
	  * @return
	  * @throws RBNIllegalArgumentException
	  */
	 public int[] allElements(Type rtype)
	 throws RBNIllegalArgumentException
	 {
		 int[] result = null;
		 if (rtype instanceof TypeInteger)
			 throw new RBNIllegalArgumentException("Cannot handle Integer Type yet");
		 
		 if (rtype instanceof TypeDomain){
			 result = new int[dom];
			 for (int i=0;i<dom;i++)
				 result[i]=i;
		 }

		 if (rtype instanceof TypeRel){
			 Vector<int[]> alltrue = mydata.allTrue(((TypeRel)rtype).getRel());
			 result = new int[alltrue.size()];
			 for (int i=0;i<result.length;i++)
				 result[i]=alltrue.elementAt(i)[0];
		 }
		 return result;
	 }

	 /* The following is only a default implementation of
	  * allTrue that does not make use of the specific representation
	  * of relations in RelStruc
	  */
	 public int[][] allTrue(CConstr cc,String[] vars)// the elements of vars must be distinct!
	 throws IllegalArgumentException,RBNCompatibilityException
	 {
		 int k = vars.length;
		 int m = rbnutilities.IntPow(dom,k);
		 int[][] prelimarray = new int[m][k];

		 int numtrue = 0; // number of tuples that satisfy cc
		 for (int i=0; i<m ; i++)
		 {
			 int[] thistuple = rbnutilities.indexToTuple(i,k,dom);

			 if (trueCC(cc,vars,thistuple))
			 {
				 prelimarray[numtrue] = thistuple;
				 numtrue++;
			 }

		 }

		 int[][] result = new int[numtrue][k];
		 for (int i =0; i<numtrue; i++) result[i]=prelimarray[i];
		 return result;
	 }

	 public Vector<int[]> allTrue(Rel r){
		 return mydata.allTrue(r);
	 }

	 /** Returns an array of integer tuples of length
	  * types.length. The array contains all tuples of 
	  * domainelements such that the i'th component in
	  * the tuple is an element in the relation types[i];
	  * 
	  * Throws an error if types contains a type not 
	  * corresponding to a unary relation in this RelStruc
	  * 
	  * @param types
	  * @return
	  */
	 public int[][] allTypedTuples(Type[] types)
	 throws RBNIllegalArgumentException
	 {
		 Vector<int[]> domains = new Vector<int[]>();
		 for (int i=0;i<types.length;i++){
			 domains.add(allElements(types[i]));
		 }
		 return rbnutilities.cartesProd(domains);
		 
	 }
	 
	 public int[][] allArgTuples(Rel r)
	 throws RBNIllegalArgumentException
	 {
		 return allTypedTuples(r.getTypes());
	 }
	 


	 /** Saves the RelStruc into a file using the BLP or MLN syntax for logical specifications */
	 public void saveToAtomFile(String filename, int format){
		 try{	  
			 BufferedWriter thiswriter = FileIO.openOutputFile(filename);
			 if (format == BLP_FORMAT)
				 thiswriter.write("Prolog {" + '\n');

			 Rel nextrel;
			 String rname;
			 Vector<int[]> truetuples;

			 
			 for (int i=0;i<mydata.size();i++){
				 nextrel = mydata.relAt(i);
				 rname = nextrel.name.name;
				 truetuples = mydata.allTrue(nextrel);
				 for (int j=0;j<truetuples.size();j++){
					 thiswriter.write(rname + 
							 "(" +  rbnutilities.arrayToString(truetuples.elementAt(j),"o") 
							 + ")");
					 if (format == BLP_FORMAT)
						 thiswriter.write(".");
					 thiswriter.write('\n');
				 }
			 }

			 if (format == BLP_FORMAT)
				 thiswriter.write("}");
			 thiswriter.flush();
			 thiswriter.close();
		 }
		 catch (IOException e){System.out.println(e);}

	 }


	 public void saveToRDEF(File f){
		 RelData thisasdata = new RelData();
		 thisasdata.add(new RelDataForOneInput(this));
		 thisasdata.saveToRDEF(f);
	 }
	 
	 
	 /** Saves the RelStruc to a collection of text file in 
	  * Proximity format
	  * 
	  * path is a directory path
	  */
	 
	 public void saveToProximityText(String path)
	 throws RBNioException
	 {
		 try{
			 String domainfile = path + "objects.data";
			 String linksfile = path + "links.data";
			 String attributefile = path + "attributes.data";
			 String linksvaluefile = path + "L_attr_linktype.data";
			 Rel rel;
			 int ar;
			 BufferedWriter domainwriter = FileIO.openOutputFile(domainfile);
			 for (int i=0;i<dom;i++)
				 domainwriter.write(Integer.toString(i) +'\n');
			 domainwriter.close();
			 
			 BufferedWriter linkswriter = FileIO.openOutputFile(linksfile);
			 BufferedWriter attributewriter = FileIO.openOutputFile(attributefile);
			 BufferedWriter linksvaluewriter = FileIO.openOutputFile(linksvaluefile);
			 
			 attributewriter.write("linktype" + '\t' + "L" + '\t' + "str" 
					 + '\t' + "L_attr_linktype.data" + '\n');
			 int linkindex = 0;
			 
			 for (int i=0;i<mydata.size();i++){
				 rel = mydata.relAt(i);
				 ar = rel.getArity();
				 String relname = rel.name.name;
				 if (ar>2 || ar==0)
					 throw new RBNioException("Cannot save relation with arity" 
							 + rel.getArity() + " to Proximity text format");
				 if (ar == 1){					 
					 String thisattrfile = "O_attr_" + relname + ".data";
					 BufferedWriter thisattrwriter = FileIO.openOutputFile(path + thisattrfile);
					 attributewriter.write(relname + '\t' + "O" + '\t' 
							 + "int" + '\t' + thisattrfile+ '\n');
					 Vector<int[]> trueobjs = this.allTrue(rel);
					 for (int h=0;h<trueobjs.size();h++){
						 thisattrwriter.write(Integer.toString(trueobjs.elementAt(h)[0]) + '\t' +  "1"+ '\n');
					 }
					 /* The objects for which this attribute is false cannot be 
					  * recovered with this.allFalse(rel), because this would require
					  * that objects are explicitly instantiated to false in this.mydata,
					  * but this is not the case. Instead retrieve objects with 
					  * thisattr=false via allTrue for CConstraints
					  */
					 String vars[] = {"x"};
					 CConstrAtom  cat = new CConstrAtom(rel,vars); 
					 CConstrNeg cneg = new CConstrNeg(cat);
					 int[][] falseobjs = this.allTrue(cneg,vars);
					 for (int h=0;h<falseobjs.length;h++){
						 thisattrwriter.write(Integer.toString(falseobjs[h][0]) + '\t' +  "0"+ '\n');
					 }
					 thisattrwriter.close();
				 }
				 else{ // ar=2
					 Vector<int[]> truelinks = this.allTrue(rel);
					 for (int h=0;h<truelinks.size();h++){
						 int[] nextlink = truelinks.elementAt(h);
						 linkswriter.write(Integer.toString(linkindex) + '\t' 
								 + Integer.toString(nextlink[0]) + '\t' 
								 + Integer.toString(nextlink[1]) + '\n');
						 linksvaluewriter.write(linkindex + '\t' + "\"" + relname + "\"" + '\n');
						 linkindex++;
					 }
				 }
			 }
			 linkswriter.close();
			 attributewriter.close();
			 linksvaluewriter.close();
		}
		catch (IOException e){System.out.println(e);}
		catch (RBNCompatibilityException e){System.out.println(e);}
	 }
	 
	 
	 /** Saves the RelStruc to a new MySQL database **/
	 public void saveToMysql(String dbname){
		 //java.sql.Statement stm;
		 java.sql.PreparedStatement pst;
		 String commandstring;
		 Rel rel;
		 Vector<int[]> truetuples;
		 int[] nexttup; 
		 
		 try{
	            Class.forName("com.mysql.jdbc.Driver");
	            Properties props = new Properties();
	            props.setProperty("user","mysql");
//	            Enumeration menum = DriverManager.getDrivers();
//	            while (menum.hasMoreElements())
//	                System.out.println(menum.nextElement().getClass().getName());

	            java.sql.Connection myconnection = DriverManager.getConnection("jdbc:mysql://localhost/?user=root");
//	            if (myconnection != null)
//	                myconnection.setCatalog(dbname);
//	            else System.out.println("null connection");

	            pst = myconnection.prepareStatement("CREATE DATABASE " + dbname);
	            pst.execute();
	            
	            pst  = myconnection.prepareStatement("USE " + dbname);
	            pst.execute();
	            
	            for (int i=0;i<mydata.size();i++){
	            	/* Create table for relations */
	            	rel = mydata.relAt(i);
	            	
	            	commandstring = "CREATE TABLE " + rel.name.name + "(";
	            	for (int j=1;j<=rel.arity;j++)
	            		commandstring = commandstring + "arg" + j  + " INT ," ;
	            	/* Remove the last comma: */
	            	commandstring = commandstring.substring(0,commandstring.length()-1);
	            	commandstring = commandstring + ")";
	            	
	            	
	            	pst = myconnection.prepareStatement(commandstring);
	            	pst.execute();
	            	
	            	
//	            	pst = myconnection.prepareStatement("FLUSH TABLES");
//	            	pst.execute();
	            	
	            	/* Now fill the table with the tuples: */
	            	truetuples = mydata.allTrue(rel);
	            	for (int j=0;j<truetuples.size();j++){
	            		nexttup = truetuples.elementAt(j);
	            		commandstring = "INSERT INTO " + rel.name.name + " VALUES (";
	            		for (int k=0;k<rel.arity;k++)
	            			commandstring = commandstring + nexttup[k] + ",";
	            		/* Remove the last comma: */
		            	commandstring = commandstring.substring(0,commandstring.length()-1);
		            	commandstring = commandstring + ")";
		            	pst = myconnection.prepareStatement(commandstring);
		            	pst.execute();
	            	}
	            	
	            }
	            //stm = myconnection.createStatement();
	            

	        }
	        catch(java.sql.SQLException e){System.out.println(e);}
	        catch(java.lang.ClassNotFoundException e){System.out.println(e);}



	 }

	 /** Saves structure into collection of text files into directory path
	  * 
	  * @param path
	  */
	 public void saveToTextFiles(String path){
		 Rel rel;
		 String filename;
		 BufferedWriter writer;
		 Vector<int[]> truetuples;
		 int[] nexttup; 
		 
		 try{
	            
	            for (int i=0;i<mydata.size();i++){
	            	/* Create file for relations */
	            	rel = mydata.relAt(i);
	            	filename = path + rel.name.name;
	            	writer = FileIO.openOutputFile(filename);
	            	
	            	truetuples = mydata.allTrue(rel);
	            	for (int j=0;j<truetuples.size();j++){
	            		nexttup = truetuples.elementAt(j);
	            		for (int h=0;h<nexttup.length-1;h++)
	            			writer.write(Integer.toString(nexttup[h])+'\t');
	            		writer.write(Integer.toString(nexttup[nexttup.length-1])+'\t');
	            		writer.write('\n');
	            	}
	            	writer.close();
	            }
		 }
		 catch(IOException e){System.out.println(e);}
	 }

	 
	 
	 public OneStrucData getData(){
		 return mydata;
	 }
	 
	 protected void setData(OneStrucData dat){
		 mydata = dat;
	 }
	 
	 public void addDomainDec(Element el){
		 Element domel = el.addElement("Domain");
		 boolean havenames = (elementnames.size() == dom);
		 boolean havecoords = (coordinates.size() == dom);
		 for (int i=0;i<dom;i++){
			 Element nextdel = domel.addElement("obj");
			 nextdel.addAttribute("ind",Integer.toString(i));
			 if (havenames)
				 nextdel.addAttribute("name",nameAt(i));
			 if (havecoords)
				 nextdel.addAttribute("coords",rbnutilities.arrayToString(coordinates.elementAt(i)));
		 }
		 
	 }
	 
	    public void addTuple(Rel r, String[] tuple){
	        this.addTuple(r, this.getIndexes(tuple));
	    }
	    public int[] getIndexes(String[] tuple){
	        int[] args = new int[tuple.length];
	        for(int i=0;i<tuple.length; i++){
	            if (elementnames.indexOf(tuple[i])==-1)
	                this.addNode( tuple[i]);
	            args[i]= elementnames.indexOf(tuple[i]);
	        }
	        return args;
	    }

	    public Vector<Rel> getRels(){
	    	return mydata.getRels();
	    }
	    
	    public int truthValueOf(Rel r, int[] args){
	    	return mydata.truthValueOf(r,args);
	    }
}
