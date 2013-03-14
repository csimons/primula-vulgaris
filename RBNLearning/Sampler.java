/*
* Sampler.java 
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

//import java.util.*;
//import java.io.*;
import RBNpackage.*;
import RBNgui.*;
import RBNExceptions.*;
import RBNutilities.*;
import RBNinference.*;
import RBNio.*;
import mymath.*;
import myio.*;

public class Sampler{

	private Primula myPrimula;
	private PFNetwork pfnetw;

	public Sampler(){
		myPrimula = null;
		pfnetw = null;
	}

	
	
	
//	public SparseRelStruc sampleOneStruc(){
//		BayesConstructor bcons = new BayesConstructor(myPrimula, new Instantiation(), new AtomList());
//		SparseRelStruc result = ((SparseRelStruc)(myPrimula.getRels())).cloneDomain(true);
//		Rel[] probRels = myPrimula.getRBN().Rels();
//
//		for (int i = 0;i<probRels.length;i++){
//			probRels[i].setColor(new Color(205,205,0));
//			result.addRelation(probRels[i]);
//		}
//		try{
//			PFNetwork pfnetw = bcons.constructPFNetwork(Primula.OPTION_NOT_EVIDENCE_CONDITIONED,
//					Primula.OPTION_NOT_QUERY_SPECIFIC,
//					Primula.OPTION_ELIMINATE_ISOLATED_ZERO_NODES);
//
//			pfnetw.prepareForSampling(EvidenceModule.OPTION_SAMPLEORD_FORWARD,
//					EvidenceModule.OPTION_NOT_SAMPLE_ADAPTIVE,
//					new boolean[5],
//					3, 
//					new AtomList(), 
//					1,
//					1,
//					null);
//			pfnetw.sampleInst(0,false);
//
//
//			for (int i = 0; i<pfnetw.allnodesSize(); i++){
//				//System.out.print("netwnode " + i);
//				if (pfnetw.sampleValAt(i)==1){
//					result.addTuple(pfnetw.relAt(i), pfnetw.atomAt(i).args());
//				}
//			}
//		}
//		catch(RBNCompatibilityException e){System.out.println(e);}
//		catch(RBNInconsistentEvidenceException e){System.out.println(e);}
//		catch(RBNIllegalArgumentException e){System.out.println(e);}
//		catch(RBNCyclicException e){System.out.println(e);}
//		catch(java.io.IOException e){System.out.println(e);};
//
//
//		return result;
//	}


	public void makeSampleStruc(Primula pr){
		myPrimula = pr;
		BayesConstructor bcons = new BayesConstructor(myPrimula, new Instantiation(), new AtomList());
		try{
			pfnetw = bcons.constructPFNetwork(Primula.OPTION_NOT_EVIDENCE_CONDITIONED,
					Primula.OPTION_NOT_QUERY_SPECIFIC,
					Primula.OPTION_NOT_ELIMINATE_ISOLATED_ZERO_NODES);
			pfnetw.prepareForSampling(EvidenceModule.OPTION_SAMPLEORD_FORWARD,
					EvidenceModule.OPTION_NOT_SAMPLE_ADAPTIVE,
					new boolean[5],
					3, 
					new AtomList(), 
					1,
					1,
					null);
		}
		catch(RBNCompatibilityException e){System.out.println(e);}
		catch(RBNIllegalArgumentException e){System.out.println(e);}
		catch(RBNCyclicException e){System.out.println(e);}
		catch(RBNInconsistentEvidenceException e){System.out.println(e);}
		catch(java.io.IOException e){System.out.println(e);};
	}


	/** Samples OneStrucData with a percentage of pcmissing values missing (completely at random) */

	public OneStrucData sampleOneStrucData(double pcmissing){

		OneStrucData result = new OneStrucData();
		double rand;

		try{
			if (pfnetw==null)
				throw new java.lang.NullPointerException("Initialize sampling structure before sampling!");

			pfnetw.sampleInst(0,false);

			for (int i = 0; i<pfnetw.allnodesSize(); i++){
				rand = Math.random();
				if (100*rand >= pcmissing){
					if (pfnetw.sampleValAt(i)==1){
						result.add(pfnetw.relAt(i), pfnetw.atomAt(i).args(),true,"?");
					}
					else
						result.add(pfnetw.relAt(i), pfnetw.atomAt(i).args(),false,"?");
				}
			}
		}
		catch(RBNCompatibilityException e){System.out.println(e);}
		catch(RBNInconsistentEvidenceException e){System.out.println(e);}

		return result;
	}
	/** Constructs a random graph (given by binary relation 'edge') over
	 * 'size' nodes. Edges are independent with probability 'edgeprob'.
	 * When acyclic = true then an acyclic graph is produced.
	 * 
	 * Graph consists of compnum connected components. The nodes in component k
	 * are in the relation comp_k.
	 * 
	 * Unary relations 'node' containing all nodes, and 'root' containing all
	 * nodes without edge-predecessors are also created.
	 * 
	 */ 
	public SparseRelStruc makeRandomGraph(int size, 
			double edgeprob, boolean acyclic, int compnum){
		SparseRelStruc result = new SparseRelStruc(size*compnum);
		Rel noderel = new Rel("node",1);
		result.addRelation(noderel);
		Rel edgerel = new Rel("edge",2);
		result.addRelation(edgerel);
		Rel rootrel = new Rel("root",1);
		result.addRelation(rootrel);

		double coin;
		boolean isroot;
		for (int k=0; k< compnum; k++){
			Rel comprel = new Rel("comp_" + k,1);
			result.addRelation(comprel);
			for (int i=k*size;i<(k+1)*size;i++){
				int tup[] = {i};
				result.addTuple(comprel,tup);
				isroot = true;
				if (!acyclic){
					for (int j=k*size; j<(k+1)*size; j++){
						coin = Math.random();
						if (coin<edgeprob){
							int edgetup[]={j,i};
							result.addTuple(edgerel, edgetup);
							isroot = false;
						}
					}
				}
				else
					for (int j=k*size;j<i;j++){
						coin = Math.random();
						if (coin<edgeprob){
							int edgetup[]={j,i};
							result.addTuple(edgerel, edgetup);
							isroot = false;
						}
					}

				result.addTuple(noderel, tup);
				if (isroot){
					result.addTuple(rootrel, tup);
				}
			}
		}
		return result;	
	}

	/** Constructs a random graph (given by binary relation 'edge') over
	 * 'size' nodes. Edges are independent with probability 'edgeprob'.
	 * Nodes are colored red,blue,green with probabilities redprob,blueprob,(1-redprob-blueprob)
	 */ 
	public SparseRelStruc makeRandomColoredGraph(int size, double edgeprob,double redprob, double blueprob){
		SparseRelStruc result = new SparseRelStruc(size);
		Rel edgerel = new Rel("edge",2);
		Rel red = new Rel("red",1);
		Rel blue = new Rel("blue",1);
		Rel green = new Rel("green",1);
		result.addRelation(edgerel);
		result.addRelation(red);
		result.addRelation(blue);
		result.addRelation(green);

		double coin;
		for (int i=0;i<size;i++){
			coin=Math.random();
			int tupcol[]={i};
			if (coin < redprob)
				result.addTuple(red,tupcol);
			else{
				if (coin < redprob+blueprob)
					result.addTuple(blue,tupcol);
				else
					result.addTuple(green,tupcol);
			}
			for (int j=0;j<size;j++){
				coin = Math.random();
				if (coin<edgeprob){
					int tup[]={i,j};
					result.addTuple(edgerel, tup);
				}
			}
		}
		return result;	
	}


	/** Constructs a domain of size 'size' with objects of types 'types'.
	 * typeprob[i] is the probability that an object will be assigned type types[i].
	 * The elements of typeprobs should sum up to 1.
	 */ 
	public SparseRelStruc makeRandomTypedDomain(int size, String[] types, double[] typeprobs){
		SparseRelStruc result = new SparseRelStruc(size);
		Rel[] typerels = new Rel[types.length];
		for (int i=0;i<types.length;i++){
			typerels[i] = new Rel(types[i],1);
			result.addRelation(typerels[i]);
		}

		double coin;

		for (int i=0;i<size;i++){
			boolean assigned = false;
			coin=Math.random();
			int tupcol[]={i};
			int typeindex = -1;
			double probsum = 0;
			while (!assigned){
				typeindex++;
				probsum = probsum + typeprobs[typeindex];
				if (coin <= probsum){
					result.addTuple(typerels[typeindex],tupcol);
					assigned = true;
				}
			}
		}
		return result;	
	}

	/** Constructs a random pedigree-like structure: nodes have either two or zero
	 * parents
	 */
	public SparseRelStruc makeRandomPedigree(int size, double founderprop){
		SparseRelStruc result = new SparseRelStruc(size);
		Rel fathrel = new Rel("father",2);
		Rel mothrel = new Rel("mother",2);
		Rel foundrel = new Rel("founder",1);
		result.addRelation(fathrel);
		result.addRelation(mothrel);
		result.addRelation(foundrel);

		double coin;
		int firstpar;
		int secondpar;

		for (int i=0;i<2;i++){
			int tup[] = {i};
			result.addTuple(foundrel,tup);
		}
		for (int i=2;i<size;i++){
			coin = Math.random();
			if (coin<founderprop){ /* i is a new founder */ 
				int tup[] = {i};
				result.addTuple(foundrel,tup);
			}
			else{
				firstpar = (int)Math.floor(Math.random()*i);
				secondpar = firstpar;
				while (secondpar == firstpar){
					secondpar = (int)Math.floor(Math.random()*i);
				}
				int tupfp[] = {firstpar,i};
				int tupsp[] = {secondpar,i};
				result.addTuple(mothrel,tupfp);
				result.addTuple(fathrel,tupsp);
			}
		}

		return result;
	}

	/** 
	 * Constructs a random structure for investigating slotchain like
	 * dependencies: domain consists of 'numtypes' different types of objects;
	 * 'numoftype' objects of each type. Types are type_1,...,type_numtypes.
	 *  Between objects of type_i and type_i+1 there are 'numrels' different 
	 *  relations 'rel_i_k' k=0,...,numrels-1.
	 *  Each object of type i has exactly 'numsuccs' successors of type
	 *  i+1 in each rel_i_k
	 *  
	 *  There are numcomp disjoint connected components of this structure
	 *  
	 * @param size
	 * @param numsuccs
	 * @param numtypes
	 * @return
	 */
	public SparseRelStruc makeSlotchainBase(int numoftype, int numrels, 
			int numsuccs, int numtypes, int numcomp){
		SparseRelStruc result = new SparseRelStruc(numoftype*numtypes*numcomp);
		Rel[] typerels = new Rel[numtypes];
		for (int i=0;i<numtypes;i++){
			typerels[i]=new Rel("type_" + i , 1 );
			result.addRelation(typerels[i]);
		}

		Rel[] comprels = new Rel[numcomp];
		for (int k=0;k<numcomp;k++){
			comprels[k]= new Rel("comp_" + k, 1);
			result.addRelation(comprels[k]);
		}

		Rel[][] linkrels = new Rel[numtypes][numrels];
		for (int i=0;i<numtypes-1;i++){
			TypeRel[] types = new TypeRel[2];
			types[0] = new TypeRel(typerels[i]);
			types[1] = new TypeRel(typerels[i+1]);
			for (int k=0;k<numrels;k++){
				linkrels[i][k]=new Rel("rel_" + i + "_" + k, 2 , types );
				result.addRelation(linkrels[i][k]);
			}
		}
		for (int k=0;k<numcomp;k++){
			int offset = k*numoftype*numtypes;

			for (int i=0;i<numtypes;i++){
				for (int h = offset; h<offset+numoftype; h++ ){
					int tup[] = {i*numoftype + h};
					result.addTuple(comprels[k],tup);
					result.addTuple(typerels[i],tup);    				
				}
			}

			for (int i=0;i<numtypes-1;i++){
				for (int o=0;o<numoftype;o++){
					for (int r=0;r<numrels;r++){
						int[] succs = randomGenerators.multRandInt((i+1)*numoftype, 
								(i+2)*numoftype-1, numsuccs);
						for (int s=0;s<numsuccs;s++){
							int tup[] = {offset+o+i*numoftype,offset+succs[s]};
							result.addTuple(linkrels[i][r], tup );
						}
					}
				}
			}
		}


		return result;
	}

	/** Build a structure representing authors, papers and citations
	 * 
	 * numauthors: number of authors
	 * posauthors: probability of an author being a positive example (high h number)
	 * 
	 * @param numauthors 
	 * @return
	 */
	public SparseRelStruc makeCitationGraph(int numauthors,
			double posauthors,
			double meanpapers,
			double meancites)
	throws RBNCompatibilityException,RBNSyntaxException
	{
		SparseRelStruc result = new SparseRelStruc(numauthors);
		Rel authorrel = new Rel("Author",1);
		result.addRelation(authorrel);
		TypeRel authortype  = new TypeRel(authorrel);
		Rel paperrel = new Rel("Paper",1);
		result.addRelation(paperrel);
		TypeRel papertype = new TypeRel(paperrel);
		TypeRel trelargs1[] = {authortype};
		Rel posauthor = new Rel("Positive",1,trelargs1);
		result.addRelation(posauthor);
		TypeRel trelargs2[] = {authortype,papertype};
		Rel author2paper = new Rel("Author2Paper",2,trelargs2);
		result.addRelation(author2paper);
		TypeRel trelargs3[] = {papertype,papertype};
		Rel cites = new Rel("Cites",2,trelargs3);
		double rand;
		
		/* Randomly assigning authors to be positive */
		for (int i=0;i<numauthors;i++){
			int addtuple[] = {i};
			result.addTuple(authorrel,addtuple);
			rand = Math.random();
			if (rand < posauthors){
				result.addTuple(posauthor,addtuple);
			}
		}
		/* For each author generate papers according to Poisson
		 * distribution with mean meanpapers
		 */
		int numpapers;
		for (int i=0;i<numauthors;i++){
			numpapers = MyRandom.randomPoisson(meanpapers);
			for (int h=0;h<numpapers;h++){
				result.addNode();
				int addtuple1[] = {result.domSize()-1};
				result.addTuple(paperrel,addtuple1);
				/* result.domSize()-1 is the index of a new object */
				int addtuple2[] = {i, result.domSize()-1};
				result.addTuple(author2paper,addtuple2);
			}
		}
		
		RBNReader cconstrparser = new RBNReader();
		

		/* Create citations of papers. Number of citing papers: Poisson
		 * with mean meancites*numpapers(author). Distribution over papers by one author:
		 * uniform for 
		 */
		
		String queryargs1[] = {"x"};
		int[][] papersofauthor;
		int totalcitesforauthor;
		
		for (int i=0;i<numauthors;i++){
			/* Get the papers of nextauth */
			CConstr query = cconstrparser.stringToCConstr("Author2Paper("+Integer.toString(i)+",x)");
			papersofauthor = result.allTrue(query,queryargs1);
			totalcitesforauthor = MyRandom.randomPoisson(papersofauthor.length * meancites);
			int intargs[] = {i};
			int ispositive = result.truthValueOf(posauthor,intargs);
			for (int h=0;h<totalcitesforauthor;h++){
				/* Generate a new citing paper, and make it reference a random
				 * paper of author i 
				 */
				result.addNode();
				int addtuple3[] = {result.domSize()-1};
				result.addTuple(paperrel,addtuple3);
				/* Determine the index within papersofauthor of the paper that this
				 * paper cites
				 */
				int citethis=0;
				switch (ispositive){
				case 0: citethis = 0;
				break;
				case 1: citethis = MyRandom.randomInteger(papersofauthor.length-1);
				break;
				case -1: 
					System.out.println("Undefined truth value!");
				}
				/* Create the citation link */
				int indexofcitedpaper = papersofauthor[citethis][0];
				int addtuple4[] = {result.domSize()-1,indexofcitedpaper};
				result.addTuple(cites,addtuple4);
			}

		}
		
		

		return result;
	}
}
