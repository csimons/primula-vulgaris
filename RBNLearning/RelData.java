/*
* RelData.java 
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

package RBNLearning;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import RBNio.FileIO;
import RBNpackage.*;
import RBNutilities.rbnutilities;

/** RelData consists of pairs of input structures and 
 * (vectors of) OneStrucData representing observations of 
 * the probabilistic relations, i.e. objects of class
 * RelDataForOneInput. In principle it would be possible to 
 * have in a RelData object RelDataForOneInput objects with
 * varying sets of predefined and probabilistic relations. 
 * However, in actual usage, RelData objects will only 
 * contain RelDataForOneInput objects with the same signatures
 * (predefined and probabilistic relations). 
 * 
 *  
 *  
 * @author jaeger
 *
 */
public class RelData {
	
	Vector<RelDataForOneInput> cases;
	
	public RelData(){
		cases = new Vector<RelDataForOneInput>();
	}
	
	public void add(RelDataForOneInput rdc){
		cases.add(rdc);
	}
	
	public int size(){
		return cases.size();
	}
	
	public RelDataForOneInput elementAt(int i){
		return cases.elementAt(i);
	}
	
	
	public void saveToRDEF(String filename){
		try{
			FileWriter filwrt = new FileWriter(filename);
			saveToRDEF(filwrt);
		}
		catch (Exception e) {
			e.printStackTrace();	 
		}
	}

	public void saveToRDEF(File f){
		try{
			FileWriter filwrt = new FileWriter(f);
			saveToRDEF(filwrt);
		}
		catch (Exception e) {
			e.printStackTrace();	 
		}
	}

	public void saveToRDEF(FileWriter fwriter){
		 try{
			 XMLWriter writer = new XMLWriter(
					 fwriter,
					 new OutputFormat("   ", true)
			 );
			 Document doc = this.toDocument();
			 writer.write(doc);
			 writer.close();
		 }
		 catch (Exception e) {
			 System.err.println(e);	 
		 }
	 }

	 public RelDataForOneInput caseAt(int i){
		 return cases.elementAt(i);
	 }
	 
	private Document toDocument(){
		 Document result= DocumentHelper.createDocument();
		 Element root = result.addElement( "root" );
		 /* Add relation declarations */
		 Element reldecs = root.addElement("Relations");

		 RelDataForOneInput firstcase = cases.elementAt(0);
		 /* Adding declarations for the predefined relations */
		 Vector<Rel> predefinedrels = firstcase.inputDomain().getRels();
		 for (int i=0;i<predefinedrels.size();i++)
			 predefinedrels.elementAt(i).addRelHeader(reldecs,"false","predefined");
		 /* Adding declarations for the probabilistic relations 
		  * -- only if this RelData object does not represent 
		  * an input domain only, in which case observedAtoms().size()==0
		  */
		 if (firstcase.observedAtoms().size()>0) {
			 OneStrucData firstobservedats = firstcase.observedAtoms().elementAt(0);
			 for (int i=0;i<firstobservedats.size();i++)
				 firstobservedats.relAt(i).addRelHeader(reldecs,
						 firstobservedats.dvAt(i),
				 "probabilistic");
		 }
		 /* Now iterating over the cases */
		 Element datael = root.addElement("Data");
		 for (int i=0;i<cases.size();i++){
			 Element inputdomel = datael.addElement("DataForInputDomain");
			 cases.elementAt(i).addToElement(inputdomel);
		 }
		 return result;
	}

	 /** Saves first RelDataForOneInput into FOIL format*/
	 public void saveToFOIL(File filename){
		 cases.elementAt(0).saveToFOIL(filename);
	 }


}
