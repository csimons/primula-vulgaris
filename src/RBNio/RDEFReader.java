/*
 * RDEFReader.java 
 * 
 * Copyright (C) 2009 Aalborg University
 *
 * contact:
 * jaeger@cs.auc.dk    www.cs.auc.dk/~jaeger/Primula.html
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

import java.util.*;

import RBNpackage.*;
import RBNutilities.*;
import RBNExceptions.*;
import RBNLearning.*;

import myio.*;
import mymath.*;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.SAXReader;


public class RDEFReader {

	public static final int READONESTRUC =0;
	public static final int READRELDATA =1;
	
	private static class stringKeyPairComparator implements Comparator{
		
		/* The object arguments are presumed of the type
		 * [String,[Integer,Object]]
		 * 
		 *  Comparison is based on Integer component.
		 *  
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object ob1, Object ob2){
			
			int i1 = (Integer) ((Object[])((Object[])ob1)[1])[0];
			int i2 = (Integer) ((Object[])((Object[])ob2)[1])[0];
			
			if (i1 == i2)
				return 0;
			if (i1 > i2)
				return 1;
			else 
				return -1;
			
		}
	}
	
/** RelData primarily stores information on observed 
 * probabilistic relations. Specification of underlying 
 * input structures can be given either as an argument A, or
 * can be found in the rdef file. Only one of these should be
 * true. Therefore, if the rdef is found to contain specifications
 * of input structures (declarations of predefined relations, domain
 * specifications), then A must be null.
 * 
 * 
 * @param rdef
 * @param A
 * @return
 * @throws RBNIllegalArgumentException
 */
	public RelData readRDEF(String rdef, RelStruc A)
	throws RBNIllegalArgumentException
	{
		
		RelData reldata = new RelData();
		
		Vector<OneRelData> predrels = new Vector<OneRelData>();
		Vector<OneRelData> probrels = new Vector<OneRelData>();
		
		
		
		try{
			SAXReader reader = new SAXReader();
			Document doc = reader.read(rdef);
			Element root = doc.getRootElement();
			
			/* Read the relation declaraions */
			Element reldecs = root.element("Relations");
			String rname;
			int rarity;
			String dv;
			String type;
			String argtypes;
			Rel rel;
			
			for ( Iterator i = reldecs.elementIterator("Rel"); i.hasNext(); ) {
				Element reldec = (Element) i.next();
				rname = reldec.attributeValue("name");
				rarity = Integer.parseInt(reldec.attributeValue("arity"));
				dv = reldec.attributeValue("default");
				type = reldec.attributeValue("type");
				argtypes = reldec.attributeValue("argtypes");
				rel = new Rel(rname,rarity,typeStringToArray(argtypes,rarity));
				if (type.equals("predefined")){
					if (A != null)
						throw new RBNioException("Multiple specifications of input domains");
					else
						predrels.add(new OneRelData(rel,dv));
				}
				if (type.equals("probabilistic"))
					probrels.add(new OneRelData(rel,dv));
			}

			
			
			Element datael = root.element("Data");
			for ( Iterator i = datael.elementIterator("DataForInputDomain"); i.hasNext(); ) {
				reldata.add(parseDataForOneInput((Element)i.next(),predrels,probrels,A));
			}
			
		}
		catch (Exception e) {
			System.err.println(e);
		}
		return reldata;
	}
	
	private OneStrucData parseOneDataElement(Vector<OneRelData> initrels, 
			Element datael, 
			Hashtable<String,Object[]> namehasht,
			boolean havedomdec,
			boolean haverelstruc)
	throws RBNIllegalArgumentException
			{
		OneStrucData result = new OneStrucData();
		/* Initialize the result by a copy of initrels */
		for (int i=0;i<initrels.size();i++)
			result.add(new OneRelData(initrels.elementAt(i).rel(),initrels.elementAt(i).dv()));

		for ( Iterator i = datael.elementIterator("d"); i.hasNext();) {
			Element nextdat = (Element) i.next();
			String argstr = nextdat.attributeValue("args");
			String[] argarr = myio.StringOps.stringToStringArray(argstr);
			int[] intargs = new int[argarr.length];
			for (int k = 0;k<argarr.length;k++){
				Integer intval = (Integer)(namehasht.get(argarr[k])[0]);
				if (intval == null){
					/* If the rdef contains a domain declaration, or a RelStruc argument is 
					 * given, then namehasht must contain all names encountered in the data
					 */
					if (havedomdec)
						throw new RBNioException("Data contains undeclared object " + argarr[k]);
					if (haverelstruc)
						throw new RBNioException("Data contains object " + argarr[k] + " not existing in RelStruc");

					Integer nextind = new Integer(namehasht.size());
					Object[] elementinfo = new Object[2];
					elementinfo[0]=nextind;
					elementinfo[1]=null;
					namehasht.put(argarr[k],elementinfo);
					intargs[k]=nextind;
				}
				else
					intargs[k]=intval;
			}
			String truthval = nextdat.attributeValue("val");
			boolean tv = truthval.equals("true");
			result.setData(nextdat.attributeValue("rel"),intargs,tv);
		}
		return result;
	}
	
	private RelDataForOneInput parseDataForOneInput(Element el,
			Vector<OneRelData> predrels,
			Vector<OneRelData> probrels,
			RelStruc A){


		RelDataForOneInput result = new RelDataForOneInput();

		/* In a DataForInputDomain element of the rdef file objects are denoted by
		 * their name. For the internal representation in  OneRelData
		 * this has to be transformed into integer indices 0..n-1.
		 * 
		 * There are three possibilities:
		 * 
		 * - the DataForInputDomain element contains a "Domain" element, in which case the indices
		 * are taken from there
		 * 
		 * - there is no "Domain" element, but A != null. It then must be 
		 * the case that the names in the data are contained in A.elementnames.
		 * The correspondence between names and indices is given by
		 * A.elementnames.
		 * 
		 * - otherwise, objects are assigned indices in the order in which 
		 * they appear in the data portion.
		 * 
		 * In both cases a hashtable is constructed for mapping names (strings)
		 * to indices.
		 *  
		 * 
		 */


		Hashtable<String,Object[]> namehasht = new Hashtable<String,Object[]>();

		boolean havedomdec = false;
		boolean haverelstruc = (A != null);
		Object[] elementinfo;

		Element domel = el.element("Domain");
		try{
			if (domel != null){
				if (haverelstruc)
					throw new RBNioException("Multiple specifications of input domains");
				else{
					havedomdec=true;
					for ( Iterator i = domel.elementIterator("obj"); i.hasNext();) {
						Element nextobj = (Element) i.next();
						elementinfo = new Object[2];
						elementinfo[0]=Integer.parseInt(nextobj.attributeValue("ind"));
						if (nextobj.attributeValue("coords")!=null)
							elementinfo[1]=StringOps.stringToIntegerVector(nextobj.attributeValue("coords")).asArray();
						else elementinfo[1]=null;
						namehasht.put(nextobj.attributeValue("name"),elementinfo);
					}
				}
			}

			if (!havedomdec && haverelstruc){
				Vector<String> elementnames = A.getNames();
				Vector<int[]> coords = A.getCoords();

				for (int i=0;i<elementnames.size();i++){
					elementinfo = new Object[2];
					elementinfo[0]=(Integer)i;
					elementinfo[1]=coords.elementAt(i);
					namehasht.put(elementnames.elementAt(i),elementinfo);
				}
			}



			RelStruc inputrs;
			if (haverelstruc)
				inputrs = A;
			else{
				Element predreldata = el.element("PredefinedRels");
				OneStrucData inputdata = parseOneDataElement(predrels, 
						predreldata, 
						namehasht,
						havedomdec,
						haverelstruc);
				/* Construct the vector of elementnames and coordinates. This is 
				 * the vector of keys in namehasht sorted according to 
				 * their integer values. Note that the correspondence 
				 * between elementnames and indices 0,...,n-1 that is 
				 * usually assumed in a RelStr may not be enforced int
				 * the rdef file specification of the input structure.
				 */
				Vector<Object> stringinfopairs = new Vector<Object>();
				for (Iterator it = namehasht.entrySet().iterator(); it.hasNext();){
					Object[] nextpair = new Object[2];
					Map.Entry<String,Object[]> me = (Map.Entry<String,Object[]>)it.next();
					nextpair[0]=me.getKey();
					nextpair[1]=me.getValue();
					stringinfopairs.add(nextpair);
				}
				Object[] stringinfopairsarr = stringinfopairs.toArray();
				Arrays.sort(stringinfopairsarr, new stringKeyPairComparator());
				Vector<String> elementnames = new Vector<String>();
				Vector<int[]> coordinates = new Vector<int[]>();
				for (int i=0;i<stringinfopairsarr.length;i++){
					elementnames.add((String)((Object[])stringinfopairsarr[i])[0]);
					coordinates.add((int[])((Object[])((Object[])stringinfopairsarr[i])[1])[1]);
				}
				inputrs = new SparseRelStruc(elementnames,inputdata,coordinates);
			}
			
			result.setA(inputrs);
			
			/* Now start reading the data */

			OneStrucData nextonestruc;

			for (Iterator i = el.elementIterator("ProbabilisticRelsCase");i.hasNext();){
				Element nextdatael = (Element)i.next();
				nextonestruc =  parseOneDataElement(probrels,
						nextdatael,
						namehasht,
						havedomdec,
						haverelstruc);
				result.addCase(nextonestruc);
			}

		}
		catch (RBNIllegalArgumentException e){System.out.println(e);}
		return result;
	}

	private Type[] typeStringToArray(String ts, int arity){
		Type[] result = new Type[arity];
		String nexttype;
		int nextcomma;
		for (int i=0;i<arity;i++)
		{
			nextcomma = ts.indexOf(",");
			if (nextcomma != -1){
				nexttype = ts.substring(0,nextcomma);
				ts = ts.substring(nextcomma+1);
			}
			else{
				nexttype = ts;
				ts = "";
			}
			if (nexttype.equals("Domain"))
				result[i]=new TypeDomain();
			else
				result[i]=new TypeRel(nexttype);
		}
		return result;

	}
	
}
