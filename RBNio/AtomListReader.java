/*
* AtomListReader.java 
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

package RBNio;

import java.io.*;
import java.util.*;

import RBNpackage.*;
import RBNutilities.*;
import RBNExceptions.*;
import RBNLearning.*;
import RBNpackage.*;
import RBNgui.TypeSelectionPanel;
import myio.*;

public class AtomListReader {

	RelData result = new RelData();
	RelDataForOneInput rdfoi;
	SparseRelStruc inputdom = new SparseRelStruc();	
	OneStrucData osd;
	String relname;
	TypeDomain domtype = new TypeDomain();
	
	Hashtable<String,String> constants = new Hashtable<String,String>();
	Hashtable<String,Rel> relations = new Hashtable<String,Rel>();
	Hashtable<String,Rel> candidatetypes = new Hashtable<String,Rel>();
	Hashtable<String,Rel> typrels = new Hashtable<String,Rel>();
	Hashtable<String,TypeRel> types = new Hashtable<String,TypeRel>();
 	Hashtable<String,TypeRel> typesofconstants = new Hashtable<String,TypeRel>();

	
	public RelData readAL(File alfile)
	throws FileNotFoundException,ParseException,InterruptedException
	{
		
		AtomListParser parser = new AtomListParser(new FileInputStream(alfile));
		
//		System.out.println("parse 1");
		/* Determine the types */
		parser.setParseNo(1);
		parser.readALFile(this);
		Vector<Rel> ctvec = new Vector<Rel>();
		for (Enumeration<Rel> e = candidatetypes.elements(); e.hasMoreElements();){
			Rel nextrel = e.nextElement();
			ctvec.add(nextrel);
		}
		Vector<String> ctnames = new Vector<String>();
		for (int i=0;i<ctvec.size();i++)
			ctnames.add(ctvec.elementAt(i).name.name);
		
		Vector<String> selectedTypes = new TypeSelectionPanel(ctnames).showInputDialog();
		
		/* Construct the Types */
		for (int i=0;i<selectedTypes.size();i++){
			relname = selectedTypes.elementAt(i);
			Rel newrel = new Rel(relname,1);
			System.out.println("add typerel " + relname);
			typrels.put(relname,newrel);
			types.put(relname, new TypeRel(newrel));
			inputdom.addRelation(newrel);
		}
		
		/* Construct domain and interpretation of types in inputdom */
//		System.out.println("parse 2");
		parser.ReInit(new java.io.FileInputStream(alfile));
		parser.setParseNo(2);
		parser.readALFile(this);
		
		/* Initialize rdfoi */
		rdfoi = new RelDataForOneInput(inputdom);
		osd = new OneStrucData();
		
		/* Construct typed probabilistic Relations */
//		System.out.println("parse 3");
		parser.ReInit(new java.io.FileInputStream(alfile));
		parser.setParseNo(3);
		parser.readALFile(this);
		
		for (Enumeration<Rel> e = relations.elements(); e.hasMoreElements();)
			osd.addRelation(e.nextElement(),"?");
			
		/* Add the tuples to the probabilistic Relations */
//		System.out.println("parse 4");
		parser.ReInit(new java.io.FileInputStream(alfile));
		parser.setParseNo(4);
		parser.readALFile(this);
	
		rdfoi.addCase(osd);
		result.add(rdfoi);
		return result;
	}

	public void addElement(String elname,String rname){
		if (constants.put(elname,elname)==null){
			inputdom.addNode(elname);
		}
		Rel rel = typrels.get(rname);
		
		if (rel != null){
			String[] addtup = {elname};
			inputdom.addTuple(rel,addtup);
			typesofconstants.put(elname,types.get(rname));
		}

	}

	public void addRelation(String relname,Vector<String> args){
		if (!typrels.containsKey(relname) && 
				!relations.containsKey(relname)){
			Type[] typesofthis = new Type[args.size()];
			Type nexttype;
			for (int i=0;i<args.size();i++){
				nexttype = typesofconstants.get(args.elementAt(i));
				if (nexttype != null)
					typesofthis[i]=nexttype;
				else typesofthis[i]=domtype;
			}
			Rel rel = new Rel(relname,args.size(),typesofthis);
			relations.put(relname,rel);
		};
	}

	public void addTuple(String relname, Vector<String> tup, boolean tv){
		if (!typrels.containsKey(relname)){
			osd.add(new Rel(relname,tup.size()),
					inputdom.getIndexes(StringOps.vectorToArray(tup)),
					tv,
			"?");
		}
	}

	public void addCandidateType(Rel rel){
		candidatetypes.put(rel.name.name,rel);
	}
}
