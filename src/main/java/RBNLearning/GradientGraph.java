/*
* GradientGraph.java 
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

import java.util.*;
import java.io.*;
import RBNpackage.*;
import RBNgui.*;
import RBNExceptions.*;
import RBNutilities.*;
import RBNinference.*;
import mymath.MyMathOps.*;
import myio.StringOps;

/** Main class for RBN parameter learning. The Gradient Graph is a representation of the
 * likelihood function given data consisting of pairs of relational input domains (objects of
 * type RelStruc), and observed values of the probabilistic relations (given as objects of type
 * Instantiation). Each pair may contain a different input domain, or there may be multiple 
 * observations of the probabilistic relations for one input domain. 
 * 
 * Nodes in the gradient graph correspond to ground probability formulas obtained from
 * recursively evaluating the probability formulas corresponding to the ground atoms in the 
 * Instantiations.  Identical ground (sub-) formulas obtained from the evaluation of different 
 * instantiated ground atoms are included only once in the GradientGraph. For this purpose a 
 * hashtable allNodes for the nodes is maintained. The keys for the nodes are constructed as 
 * strings consisting of a concatentation of the index of the data case with the string representation
 * of the ground probability formula.
 * 
 * Example: the probabilistic relation r(x,y) is defined by F(x,y) = (s(x,y):t(y),0.6).
 * In both the first and the second data pair the ground atom r(4,7) is observed to be true.
 * Then two nodes will be constructed, one with key 1.(s(4,7):t(7),0.6), and one with key
 * 2.(s(4,7):t(7),0.6). Since the sub-formulas s(4,7) and t(7) may evaluate differently 
 * in the two data pairs, these formulas have to be distinguished. If, for example, s(4,7)
 * is observed to be true in the first data pair, and false in the second, then a further 
 * nodes with key 1.t(7) will be constructed, but no node 2.t(7).  
 * 
 * 
 * 
 * @author jaeger
 *
 */
public class GradientGraph{

	private Primula myPrimula;
	private LearnModule myLearnModule;

	private Hashtable<String,GradientGraphNode> allNodes;

	private CombFuncNOr combFuncNOr;
	private CombFuncMean combFuncMean;
	private CombFuncInvsum combFuncInvsum;
	private CombFuncESum combFuncESum;


	GradientGraphLikelihoodNode llnode;
	Vector<GradientGraphIndicatorNode> indicators; /* All the indicators for unknown atoms */
	Vector<GradientGraphConstantNode> paramNodes; /* All the constant (i.e. parameter) nodes */


	String[] parameters; /* All the parameters. This array of parameter names is constructed
	 * before the vector paramNodes is constructed. It is needed already
	 * in the construction of the nodes of the graph. The order of the 
	 * two lists of parameters must be consistent, i.e.
	 * parameters[i] = paramNodes.elementAt(i).paramname() 
	 */

	/* For estimating likelihood and gradient from Gibbs sampling
	 * values for unobserved atoms: 'numchains' Markov chains are 
	 * sampled in parallel. For each chain, values are estimated based
	 * on the past 'windowsize' states (=instantiatiations of 
	 * unobserved atoms) of the chain. 
	 */
	int numchains;
	int windowsize;

	/* Contains the log-likelihood of that part of the data that
	 * does not generate an upper ground atom node in the gradient
	 * graph. Used to display the overall likelihood of the model.
	 */
	double likelihoodconst;


//	int[] evalcount;

	public GradientGraph(Primula mypr, RelData data, LearnModule mylm)
	throws RBNCompatibilityException
	{
		myPrimula = mypr;
		RBN rbn = mypr.getRBN();
		myLearnModule = mylm;
		allNodes = new Hashtable<String,GradientGraphNode>();
		combFuncNOr = new CombFuncNOr();
		combFuncMean = new CombFuncMean();
		combFuncInvsum = new CombFuncInvsum();
		combFuncESum = new CombFuncESum();

		indicators = new Vector<GradientGraphIndicatorNode>();
		likelihoodconst = 0;

//		evalcount = new int[2];

		/* Determine number of parameters. This is needed when constructing the nodes */
		parameters = new String[0];
		ProbForm nextpf;
		if (rbn==null) System.out.println("rbn is null");
		for (int i=0; i<rbn.NumPFs(); i++){
			nextpf = rbn.ProbFormAt(i);
			parameters = rbnutilities.arraymerge(parameters,nextpf.parameters());
		}

		llnode = new GradientGraphLikelihoodNode(this);
		/* Create all the ground probability formulas for the atoms in data 
		 *  
		 */
		RelDataForOneInput rdoi;
		RelStruc A;
		OneStrucData osd;
		Rel nextrel;
		Vector<int[]> inrel;
		String[] vars; /* The argument list for nextpf */
		ProbForm groundnextpf;
		GradientGraphProbFormNode fnode;
		double pfeval;
		boolean dependsonmissing = false;
		
		/* First get (approximate) count of all upper ground atom nodes that 
		 * need to be constructed (to support progress report)
		 */
		int ugacounter = 0;
		for (int inputcaseno=0; inputcaseno<data.size(); inputcaseno++){
			rdoi = data.caseAt(inputcaseno);
			for (int observcaseno=0; observcaseno<rdoi.numObservations(); observcaseno++){
				osd = rdoi.oneStrucDataAt(observcaseno);
				for (int i=0; i<rbn.NumPFs(); i++){
					nextrel = rbn.relAt(i);
					ugacounter = ugacounter + osd.allFalse(nextrel).size();
					ugacounter = ugacounter + osd.allTrue(nextrel).size();
				}
			}
		}

		int processedcounter = 0;
		int currentpercentage = 0;
		myPrimula.appendMessageThis("0%");

		for (int inputcaseno=0; inputcaseno<data.size(); inputcaseno++){
			rdoi = data.caseAt(inputcaseno);
			A = rdoi.inputDomain();
			for (int observcaseno=0; observcaseno<rdoi.numObservations(); observcaseno++){
				osd = rdoi.oneStrucDataAt(observcaseno);
				/* For some compatibility issues with older code, cast osd also as
				 * an Instantiation object 
				 */
				Instantiation osdI = new Instantiation(osd);

				for (int i=0; i<rbn.NumPFs(); i++){
					nextpf = rbn.ProbFormAt(i);
					vars = rbn.argumentsAt(i);
					nextrel = rbn.relAt(i);
					for (int ti = 0; ti <= 1 ; ti++) {

						if (ti == 0)
							inrel = osd.allFalse(nextrel);
						else
							inrel = osd.allTrue(nextrel);
						for (int k=0;k<inrel.size();k++){
							groundnextpf = nextpf.substitute(vars,(int[])inrel.elementAt(k));
							pfeval = groundnextpf.evaluate(A,osdI,new String[0],new int[0],false);
							
							
							if (myLearnModule.aca()){
								dependsonmissing = groundnextpf.dependsOn("unknown_atom",A,osdI);
							}
							
							if (pfeval == -1 && !(myLearnModule.aca() && dependsonmissing)){
								/* if pfeval != -1, then this groundnextpf has a constant value
								 * independent of parameter settings or instantiation of unknown
								 * atoms. For a correct numeric value of the likelihood this value
								 * would need to be considered, but for maximizing the likelihood
								 * it is irrelevant
								 */
								fnode = GradientGraphProbFormNode.constructGGPFN(this,
										groundnextpf,
										allNodes,
										A,									 						 
										osdI,
										inputcaseno,
										observcaseno);
								if (ti==0)			
									llnode.addToChildren(fnode,false);
								else
									llnode.addToChildren(fnode,true);
								if (!fnode.parents().contains(llnode))
									fnode.addToParents(llnode);
							}
							else{
								if (ti==0)
									likelihoodconst = likelihoodconst + Math.log(1-pfeval);
								else
									likelihoodconst = likelihoodconst + Math.log(pfeval);
							}
							processedcounter++;
							if ((10*processedcounter)/ugacounter > currentpercentage){
								myPrimula.appendMessageThis("X");
								currentpercentage++;
							}
						}
					}/* for  truefalse  */
				} /* for int i; i<rbn.NumPFs()*/
			} /* int j=0; j<rdoi.numObservations(); */
			myPrimula.appendMessageThis("100%");
		}
		/* Construct ProbFormNodes for all Indicator nodes,
		 * and set the mypf fields in the indicator nodes
		 */

		/* lastindicator is the index of the last indicator
		 * node for which a ProbFormNode has already been
		 * constructed
		 */
		int lastindicator = -1;
		Atom at;
		int inputcaseno;
		int observcaseno;

		GradientGraphIndicatorNode nextggin;
		int[] nextarg;
		while (indicators.size() - lastindicator -1 >0){
			lastindicator++;
			nextggin = indicators.elementAt(lastindicator);
			at = nextggin.myatom();
			nextarg = at.args();
			inputcaseno = nextggin.inputcaseno();
			observcaseno = nextggin.observcaseno();
			nextpf = rbn.probForm(at.rel());
			vars = rbn.args(at.rel());
			groundnextpf = nextpf.substitute(vars,nextarg);
			/** Note that (arbitrarily) the truthval of the constructed
			 * node is set to true. This initial setting must always be overridden
			 * by some sample value for this node
			 */
			fnode =  GradientGraphProbFormNode.constructGGPFN(this,
					groundnextpf,
					allNodes,
					data.elementAt(inputcaseno).inputDomain(),
					new Instantiation(data.elementAt(inputcaseno).oneStrucDataAt(observcaseno)),
					inputcaseno,
					observcaseno);
			llnode.addToChildren(fnode,nextggin);
		}




		/* Now bring the parameter nodes into the right order: 
		 * TODO: it could happen that for some parameter no parameter node
		 * has been constructed (because no atom in the data actually depended 
		 * on this parameter)  -- still needs to be dealt with!!!
		 */
		Vector<GradientGraphConstantNode> prelimParamNodes = new Vector<GradientGraphConstantNode>();
		Enumeration e = allNodes.elements();
		GradientGraphNode nextggn;
		while (e.hasMoreElements()){
			nextggn = (GradientGraphNode)e.nextElement();
			if (nextggn instanceof GradientGraphConstantNode)
				prelimParamNodes.add((GradientGraphConstantNode)nextggn);
		}
		paramNodes = new Vector<GradientGraphConstantNode>();
		boolean found;
		for (int i=0; i< parameters.length; i++){
			found = false;
			for (int j=0; j< prelimParamNodes.size(); j++)
				if (((GradientGraphConstantNode)prelimParamNodes.elementAt(j)).paramname().equals(parameters[i])){
					paramNodes.add((GradientGraphConstantNode)prelimParamNodes.elementAt(j));
					found = true;
				}
			if (!found)
				paramNodes.add(null);
		}


		if (indicators.size() > 0){
			numchains = myLearnModule.getNumChains();
			windowsize = myLearnModule.getWindowSize();
		}
		else numchains = 0;

		myPrimula.showMessageThis("#Ground atoms:" + llnode.childrenSize());
		myPrimula.showMessageThis("#Unobserved atoms:" + indicators.size());
		myPrimula.showMessageThis("#Internal nodes:" + allNodes.size());


	}


	protected void addToIndicators(GradientGraphIndicatorNode ggin){
		indicators.add(ggin);
	}

	protected double computeCombFunc(int cf, double[] args){
		switch (cf){
		case Primula.CF_NOR:
			return combFuncNOr.evaluate(args);
		case Primula.CF_MEAN:
			return combFuncMean.evaluate(args);
		case Primula.CF_INVSUM:
			return combFuncInvsum.evaluate(args);
		case Primula.CF_ESUM:
			return combFuncESum.evaluate(args);
		}
		return 0;
	}

	public double[] currentLikelihood(){
		return llnode.likelihood();
	}



	public double[] currentParameters(){
		double[] result = new double[paramNodes.size()];
		for (int i=0;i<paramNodes.size();i++){
			if (paramNodes.elementAt(i)!= null)
				result[i]=paramNodes.elementAt(i).value();
			else result[i] = 0.5;
		}
		return result;
	}

	//     public void evaluate(int sno){
	// 	evaluateLikelihood(sno);
	//     }

	public int numberOfParameters(){
		return parameters.length;
	}

	public String[] parameters(){
		return parameters;
	}

	public String parameterAt(int i){
		return parameters[i];
	}

	//     public void updateAll(){
	// 	updateLikelihood();
	// 	updatePartDerivatives();
	//     }


	/** Computes the empirical likelihood and empirical partial derivatives 
	 * of the current sample.
	 * The value and gradient fields contain the values for the last sample.
	 *
	 * When numchains=0, then value = likelihoodsum and gradient = gradientsum 
	 * are the correct values.
	 *
	 */ 
	public void evaluateLikelihoodAndPartDerivs(boolean likelihoodonly){
		resetValues(likelihoodonly);
		llnode.resetLikelihoodSum();
		if (!likelihoodonly){
			llnode.resetGradientSum();
		}

		if (numchains==0){
			llnode.evaluate();
			if (!likelihoodonly){
				llnode.evaluateGradients();
			}
		}
		else{
			for (int i=0;i<numchains*windowsize;i++){

				resetValues(likelihoodonly);
				setTruthVals(i);
				llnode.evaluate();
				llnode.setSampleLikelihood(i);
				llnode.updateLikelihoodSum();
				if (!likelihoodonly){
					llnode.evaluateGradients();
					llnode.updateGradSum();
				}
			}

		}
	}




	public void evaluateBounds(){
		llnode.evaluateBounds();
	}


	/* Resets to null the value fields in nodes in this GradientGraph. 
	 * If valueonly=false, then also the gradients are reset
	 *  
	 */
	public void resetValues(boolean valueonly){
		llnode.resetValue();
		if (!valueonly)
			llnode.resetGradient();
		Enumeration e = allNodes.elements();
		GradientGraphNode ggn;
		while (e.hasMoreElements()){
			ggn = (GradientGraphNode)e.nextElement();
			ggn.resetValue();
			if (!valueonly)
				ggn.resetGradient();
		}	
	}

	/** Resets to [-1,-1] the bounds in all nodes */
	public void resetBounds(){
		llnode.resetBounds();
		Enumeration e = allNodes.elements();
		GradientGraphProbFormNode ggn;
		while (e.hasMoreElements()){
			ggn = (GradientGraphProbFormNode)e.nextElement();
			ggn.resetBounds();
		}
	}

//	public void resetSamplePartDerivs(){
//	llnode.resetSampleGradient();
//	}


	/* Tries to randomly generate numchains instantiations of the
	 * indicator variables with nonzero probability given the
	 * current parameter values. Returns true if successful.
	 */
	public boolean initIndicators(LearnThread mythread){

		double coin;
		boolean abort = false;

		llnode.initSampleLikelihoods(numchains*windowsize);
		for (int i=0;i<indicators.size();i++)
			indicators.elementAt(i).initSampledVals(numchains*windowsize);

		boolean success;

		int failcount=0;
		int maxfailcount = myLearnModule.getMaxFails()*numchains;

		/* Find initial instantiations with nonzero probability */
		for (int k=0;k<numchains && !abort;k++){
			success = false;
			while (!success && !abort){
				for (int i=0;i<indicators.size();i++){
					coin = Math.random();
					if (coin>0.5)
						indicators.elementAt(i).setSampleVal(k,true);
					else
						indicators.elementAt(i).setSampleVal(k,false);
				}
				resetValues(true);
				setTruthVals(k);
				llnode.evaluate();
				if (llnode.likelihood()[0]!=0)
					success=true;   
				else{
					failcount++;
					if (failcount > maxfailcount)
						abort = true;
				}
			}
		}

		/* Perform windowsize-1 many steps of Gibbs sampling */
		if (!abort){
			for (int j=1;j<windowsize;j++){
				gibbsSample(j,mythread);
				if (myLearnModule.verbose())
					System.out.print(",");
			}
		}
		return !abort;
	}

	/** Performs one round of Gibbs sampling. 
	 * Each variable is resampled once.
	 * 
	 * windowindex is the index of the oldest among the this.windowsize samples
	 * that are being stored. In the GradientGraphIndicatorNode.sampledVals
	 * arrays the values windowindex+0,...,windowindex+numchains-1 are 
	 * overwritten
	 */
	public void gibbsSample(int windowindex,LearnThread mythread){
		double[] oldsamplelik;
		double[] newsamplelik;
		double likratio; 
		double coin;
		/* the index of the most recent sample */
		int recentindex;
		if (windowindex != 0)
			recentindex = windowindex -1;
		else recentindex = windowsize - 1;

		GradientGraphIndicatorNode ggin;
		for (int k=0;k<numchains && !mythread.isstopped() ;k++){

			setTruthVals(recentindex*numchains+k);
			resetValues(true);
			llnode.evaluate();
			for (int i=0;i<indicators.size() && !mythread.isstopped();i++){
				ggin = (GradientGraphIndicatorNode)indicators.elementAt(i);
				oldsamplelik=llnode.likelihood();
				ggin.toggleCurrentInst();
				ggin.reEvaluateUpstream();
				newsamplelik=llnode.likelihood();
				likratio=SmallDouble.toStandardDouble(SmallDouble.divide(newsamplelik,oldsamplelik));
				coin = Math.random();
				/* Accept the new toggled instantiation with probability 
				 * likratio/(1+likratio). Otherwise toggle back!
				 */
				if (coin>likratio/(1+likratio)){
					ggin.toggleCurrentInst();
					ggin.reEvaluateUpstream();
				}
				ggin.setSampleVal(windowindex*numchains+k);
			}
		}
	}



	/** Sets the truthval fields in the ProbFormNodes corresponding
	 * to unobserved atoms to the truthvalues in the sno's sample
	 *
	 * If sno<0 do nothing!
	 */
	public void setTruthVals(int sno){
		if (sno >=0)
			for (int i=0;i<indicators.size();i++){
				indicators.elementAt(i).setCurrentInst(sno);
			}
	}


	public void showLikelihoodNode(RelStruc A){
		System.out.println("Likelihood" + llnode.value());
	}



	public void showAllNodes(int verbose,RelStruc A){
		if (verbose >0){
			System.out.println("**** Node " + llnode.name());
			if (llnode.value == null)
				System.out.println("**** Value null");
			else
				System.out.println("**** Value " + llnode.value());
			// System.out.println("**** Bounds " + llnode.lowerBound() + "," + llnode.upperBound());
			System.out.println();
		}
		if (verbose >5){
			Enumeration e = allNodes.elements();
			GradientGraphNode nextggn;
			while (e.hasMoreElements()){
				nextggn = (GradientGraphNode)e.nextElement();
				System.out.println("**** Node " + nextggn.name());
				if (nextggn.value == null)
					System.out.println("**** Value null");
				else
					System.out.println("**** Value " + nextggn.value());
				// System.out.println("**** Bounds " + nextggn.lowerBound() + "," + nextggn.upperBound());
				System.out.println();
			}
		}
	}



	/** Returns lambda*firstpoint + (1-lambda)*secondpoint */
	private double[] midpoint(double[] firstpoint, double[] secondpoint, double lambda){
		double[] result = new double[firstpoint.length];
		for (int i=0;i<result.length;i++)
			result[i]=lambda*firstpoint[i]+(1-lambda)*secondpoint[i];
		return result;
	}

	/** Determines the direction for the linesearch given a current theta 
	 * and gradient
	 */
	private double[] getDirection(double[] theta, double[] gradient){
		double[] result = new double[gradient.length];
		/* Penalize the gradient components that are leading towards the
		 * boundary of the parameter space:
		 */
		for (int i=0 ;i<result.length;i++){
			if (gradient[i]<0)
				result[i]=gradient[i]*theta[i];
			else
				result[i]=gradient[i]*(1-theta[i]);
			// 	    result[i]=gradient[i]*Math.min(1-theta[i],theta[i]);
		}
		return result;
	}

	/** Searches for likelihood-optimizing parameters, starting at
	 * currenttheta
	 *
	 * Returns array of length n+4, where n is the number of parameter nodes
	 * in the Gradient Graph. 
	 * 
	 * The result array contains:
	 * 
	 * [0..n-1]: the current parameter values at the end of thetasearch
	 * 
	 * [n,n+1]: the likelihood value of the current parameters expressed 
	 * as a 'SmallDouble'
	 * 
	 * [n+2]: the kth root of the likelihood value, for k the number of 
	 * children of the likelihood node. This gives a 'per observed atom'
	 * likelihood value that is more useful than the overall likelihood.
	 * 
	 * [n+3]: the log-likelihood of the whole data computed as 
	 * k*log(result[n+2])+this.likelihoodconst
	 * 
	 */
	private double[] thetasearch(double[] currenttheta, 
			LearnThread mythread){
		double[] gradient;
		double[] oldthetas = currenttheta;
		double[] newlikelihood = new double[2];

		double paramdiff;
		double paramratio;
		boolean terminate = false;
		boolean parameterchanged = false;
		boolean omitnext = true;
		boolean phase1 = true;
		boolean phase2 = false;
		int iterationcount = 0;
		int windowindex = 0;

		/* Search consists of 2 phases: in the first phase, all parameters 
		 * are considered simultaneously, i.e. proper gradient ascent.
		 * 
		 * 1st phase ends when terminate1=true.
		 * 
		 * 
		 * In the second phase, one parameter at a time is optimized in 
		 * round-robin fashion. Parameters that have not changed signigicantly
		 * (according to the termination criterion for this phase)
		 * are omitted for the next LearnModule.omitrounds many rounds
		 * of optimization. 
		 * 
		 */


		int partderiv = -1;
		/* omitforrounds[i]=k means that parameter with index i will be 
		 * omitted for optimization in the next k rounds of phase 2
		 */
		int[] omitforrounds = new int[parameters.length];

		while (!terminate && !mythread.isstopped()){
			/* compute the gradient */
			evaluateLikelihoodAndPartDerivs(false);
			if (numchains==0)
				if (phase1)
					gradient = llnode.gradientAsDouble();
				else
					gradient = llnode.gradientAsDouble(partderiv);
			else
				if (phase1)
					gradient = llnode.gradientsumAsDouble();
				else
					gradient =  llnode.gradientsumAsDouble(partderiv);   
			if (myLearnModule.verbose()){
				System.out.println("Parameters: " + rbnutilities.arrayToString(currenttheta));
				if (numchains > 0)
					System.out.println("Likelihood: " + rbnutilities.arrayToString(llnode.likelihoodsum()));
				else 
					System.out.println("Likelihood: " + rbnutilities.arrayToString(llnode.likelihood()));
				System.out.println("Gradient: " + rbnutilities.arrayToString(gradient));
			}
			/* Linesearch in direction of gradient */
			//time = System.currentTimeMillis();

			/****************************************
			 * call linesearch
			 ****************************************/
			if (myLearnModule.verbose() )
				System.out.print("linesearch: ");
			currenttheta = linesearch(currenttheta,getDirection(currenttheta,gradient),
					mythread);

			setParameters(currenttheta);
			evaluateLikelihoodAndPartDerivs(true);

			/****************************************
			 * check for termination conditions and 
			 * update omitcounter if in phase 2
			 ****************************************/
			if (phase2){
				paramdiff = Math.abs(currenttheta[partderiv]-oldthetas[partderiv]);
				if (oldthetas[partderiv]>0)
					paramratio = currenttheta[partderiv]/oldthetas[partderiv];
				else paramratio = 1000;
				if (paramdiff < myLearnModule.getLineDistThresh() ||
						Math.abs(1-paramratio) < myLearnModule.getParamratiothresh())
					omitforrounds[partderiv] = myLearnModule.omitRounds();
				else
					parameterchanged = true;
			}

			/* End of phase 1, beginning phase 2 */
			if (
					phase1 && (mymath.MyMathOps.euclDist(oldthetas,currenttheta) < myLearnModule.getLineDistThresh() ||
							iterationcount > myLearnModule.getMaxIterations())
			)
			{
				phase1 = false;
				phase2 = true;
				iterationcount = 0;
				if (myLearnModule.verbose())
					System.out.println("phase 2");
			}

			/* Termination at end of phase 2 */
			if ( phase2 && partderiv == parameters.length-1 && 
					(!parameterchanged ||
							iterationcount > myLearnModule.getMaxIterations())
			)
				terminate = true;

			/* Increment the index of the parameter to be optimized */
			if (phase2){
				while (omitnext){
					partderiv++;
					if (partderiv == parameters.length){
						partderiv = 0;
						iterationcount++;
						parameterchanged = false;
					}
					if (omitforrounds[partderiv] > 0)
						omitforrounds[partderiv]--;
					else omitnext = false;
				}
				omitnext = true;

				if (myLearnModule.verbose())
					System.out.println("Parameter " + partderiv 
							+ " (" + parameters[partderiv] + ")"
							+ StringOps.arrayToString(omitforrounds,"[","]"));
			}


			/****************************************
			 * Gibbs sample
			 ****************************************/
			if (!terminate){
				if (numchains==0)
					newlikelihood = llnode.likelihood();
				else
					newlikelihood = llnode.likelihoodsum();


				if (myLearnModule.verbose() && numchains > 0)
					System.out.print("<sampling ... ");

				gibbsSample(windowindex,mythread);
				windowindex++;
				if (windowindex == windowsize)
					windowindex = 0;
				if (myLearnModule.verbose()  && numchains > 0)
					System.out.println("done>");

			}


			iterationcount++;
			oldthetas = currenttheta;
		}

		double[] result = new double[currenttheta.length+4];
		for (int i=0;i<currenttheta.length;i++)
			result[i]=currenttheta[i];
		result[currenttheta.length]=newlikelihood[0];
		result[currenttheta.length+1]=newlikelihood[1];	

		result[currenttheta.length+2]=SmallDouble.nthRoot(newlikelihood,llnode.numChildren());

		result[currenttheta.length+3] = llnode.numChildren()*Math.log(result[currenttheta.length+2])+this.likelihoodconst;
//		System.out.println("Evalcounts: " + StringOps.arrayToString(evalcount, "[","]"));
		return result;
	}


	public double[] learnParameters(LearnThread mythread)
	{
		if (myLearnModule.verbose())
			System.out.println("** start learnParameters ** ");

		/* Returns:
		 * resultArray[0:paramNodes.size()-1] : the parameter values learned in the
		 *                                      order given by paramNodes
		 * resultArray[paramNodes.size():paramNodes.size()+1]: the likelihood value for
		 *                                                     the parameters represented as a small double.
		 *                                                     When data is incomplete, then this likelihood is with
		 *                                                     respect to the last sample.
		 *                                                     
		 * resultArray[paramNodes.size()+2]: the kth root of the likelihood value, where k is  
		 * the number of  children of the likelihood node (cf. thetasearch).     
		 * 
		 * resultArray[paramNodes.size()+3]: the log-likelihood of the full data (cf. thetasearch)                                            
		 */
		double[] resultArray = new double[paramNodes.size()+4];
		double[] lastthetas;


		/* First find an initial setting of the parameters and an 
		 * initial sample, such that at least a proportion of
		 * sampleSuccessRate samples were not aborted
		 */

		boolean success = false;
		if (myLearnModule.verbose())
			System.out.print("< Initialize Markov Chains ... ");
		while (!success && !mythread.isstopped()){
			setParametersRandom();
			if (initIndicators(mythread))
				success = true;
			else
				myPrimula.showMessageThis("Failed to sample missing values");
		}
		if (myLearnModule.verbose())
			System.out.println("done >");

		double[] currenttheta = currentParameters();

		/************************************************
		 *
		 *  call thetasearch 
		 *
		 ************************************************/
		if (paramNodes.size()>0){
			lastthetas =thetasearch(currenttheta,mythread);

			for (int k=0;k<lastthetas.length;k++)
				resultArray[k]=lastthetas[k];
		}
		else{ /* Only compute likelihood for the given parameters;
		 * the first two components of the resultArray are not used */
			double[] likelihoods = computeLikelihood(mythread);
			resultArray[2]=likelihoods[0];
			resultArray[3]=likelihoods[1];
		}

		return resultArray;
	}



	/** Performs a linesearch for parameter settings optimizing 
	 * log-likelihood starting from oldthetas in the direction
	 * gradient
	 * 
	 * returns new parameter settings
	 */
	private double[] linesearch(double[] oldthetas, 
			double[] gradient, 
			LearnThread mythread){

		if (iszero(gradient))
			return oldthetas;

		double[] leftbound=oldthetas;
		double[] rightbound = new double[oldthetas.length];
		double[] middle1 = new double[oldthetas.length];
		double[] middle2 = new double[oldthetas.length];


		double[] leftvalue;
		double[] rightvalue;
		double[] middlevalue1;
		double[] middlevalue2;

		double lratio;

		/* First find the point where the line oldthetas+lambda*gradient intersects
		 * the boundary of the parameter space
		 */
		double lambda = Double.POSITIVE_INFINITY;
		for (int i=0;i<oldthetas.length;i++){
			if (gradient[i]<0)
				lambda = Math.min(lambda,-oldthetas[i]/gradient[i]);
			if (gradient[i]>0)
				lambda = Math.min(lambda,(1-oldthetas[i])/gradient[i]);
		}
		for (int i=0;i<oldthetas.length;i++)
			rightbound[i]= oldthetas[i]+lambda*gradient[i];

		/** Initialize the search. The first 2 lines are redundant 
		 * if llnode.likelihoodsum holds the correct value for oldthetas
		 */
		setParameters(oldthetas);
		evaluateLikelihoodAndPartDerivs(true);
		leftvalue = llnode.likelihood();

		/** The following can easily result in rightvalue = NaN or -infinity!*/
		setParameters(rightbound);
		evaluateLikelihoodAndPartDerivs(true);
		rightvalue = llnode.likelihood();
		boolean terminate = false;
		while (!terminate && !mythread.isstopped()) {
			if (myLearnModule.verbose())
				System.out.print("+");
//			System.out.println("left bound: " + rbnutilities.arrayToString(leftbound) 
//			+ '\n' + "right bound: " + rbnutilities.arrayToString(rightbound) );
//			System.out.println("left ll: "   + rbnutilities.arrayToString(leftvalue) +
//			"  right ll: "  + rbnutilities.arrayToString(rightvalue) );
			middle1 = midpoint(leftbound,rightbound,0.75);
			middle2 = midpoint(leftbound,rightbound,0.25);


			setParameters(middle1);
			evaluateLikelihoodAndPartDerivs(true);
			middlevalue1=llnode.likelihood();

			setParameters(middle2);
			evaluateLikelihoodAndPartDerivs(true);
			middlevalue2=llnode.likelihood();

//			System.out.println("middle1: " + rbnutilities.arrayToString(middle1)+ "/" + rbnutilities.arrayToString(middlevalue1) +
//			" middle2: " + rbnutilities.arrayToString(middle2)+ "/" + rbnutilities.arrayToString(middlevalue2) );
//			System.out.println("middle1: "  + rbnutilities.arrayToString(middlevalue1) +
//			+ '\n' + "middle2: "  + rbnutilities.arrayToString(middlevalue2) );

			if (compareLikelihood(middlevalue1,middlevalue2)){
				rightbound = middle2;
				rightvalue = middlevalue2;
			}
			else if (compareLikelihood(middlevalue2,leftvalue)){
				leftbound = middle1;
				leftvalue = middlevalue1;
			}
			else{
				rightbound = middle1;
				rightvalue = middlevalue1;
			}

			//lratio=likelihoodRatio(leftvalue,rightvalue);
			lratio = SmallDouble.toStandardDouble(SmallDouble.divide(leftvalue,rightvalue));
//			System.out.println("Dist: " + mymath.MyMathOps.euclDist(rightbound,leftbound) +
//					       "  Lik: " + lratio);
			if (mymath.MyMathOps.euclDist(rightbound,leftbound) < myLearnModule.getLineDistThresh())
				terminate = true;

			if (lratio < 1+myLearnModule.getLineLikThresh() && lratio > 1-myLearnModule.getLineLikThresh())
				terminate = true;
		}
		if (myLearnModule.verbose())
			System.out.println();


		if (compareLikelihood(leftvalue,rightvalue))
			return leftbound;
		else 
			return rightbound;

//		return midpoint(leftbound,rightbound,0.5);
	}

	/** Sets the parameter values to thetas. thetas[i] will be the
	 * value of the parameter in the i'th position in this.paramNodes
	 */
	public void setParameters(double[] thetas){
		if (thetas.length != paramNodes.size())
			System.out.println("Size mismatch in GradientGraph.setParameters!");
		for (int i=0;i<thetas.length;i++)
			if (paramNodes.elementAt(i)!=null)
				paramNodes.elementAt(i).setCurrentParamVal(thetas[i]);
	}

	public double[] getParameters(){
		double[] result = new double[paramNodes.size()];
		for (int i=0;i<paramNodes.size();i++)
			result[i]=paramNodes.elementAt(i).getCurrentParamVal();
		return result;
	}


	public void setParametersRandom(){
		for (int i=0;i<paramNodes.size();i++)
			if (paramNodes.elementAt(i)!=null)
				paramNodes.elementAt(i).setCurrentParamVal(Math.random());
	}

	public void setParametersUniform(){
		for (int i=0;i<paramNodes.size();i++)
			if (paramNodes.elementAt(i)!=null)
				paramNodes.elementAt(i).setCurrentParamVal(0.5);
	}



	public String showGraphInfo(int verbose, RelStruc A){
		String result = "";
		result = result + "% Number of indicator nodes: " + indicators.size() +'\n';
		result = result + "% Number of upper ground atom nodes: " + llnode.childrenSize() +'\n';

		// 	result = result + "Parameter nodes: ";
		// 	for (int i=0;i<paramNodes.size();i++)
		// 	    System.out.print(paramNodes.elementAt(i).name()+ "  ");
		// 	result = result + ();
		// 	result = result + ("Parameter values: " + rbnutilities.arrayToString(currentParameters()));

		// 	result = result + ();
		result = result + "% Total number of nodes: " + allNodes.size() +'\n';
		result = result + "% Total number of links: " + numberOfEdges() +'\n';

		showAllNodes(verbose,A);
		return result;
	}

	/** Prints a list of  likelihood values for all possible parameter settings
	 * obtained by varying each parameter from 0.0 to 1.0 using a stepsize of incr
	 */
	public void showAllLikelihoods(double incr){
		double[] nextsetting = new double[paramNodes.size()];
		for (int i=0;i<nextsetting.length;i++)
			nextsetting[i] = 0.0;
		int nextindex = nextsetting.length - 1;
		double nextll;
		double max = 0;
		double[] best = nextsetting.clone();
		while (nextindex >= 0){
			setParameters(nextsetting);
			evaluateLikelihoodAndPartDerivs(true);
			nextll = llnode.value();
			if (nextll > max){
				max = nextll;
				best = nextsetting.clone();
			}
			//System.out.println(rbnutilities.arrayToString(nextsetting)+": " + nextll);
			/* Find the next parameter setting */
			nextindex = nextsetting.length - 1;
			while (nextindex >= 0 && nextsetting[nextindex]>=0.9999)
				nextindex--;
			if (nextindex >=0){
				nextsetting[nextindex]=nextsetting[nextindex]+incr;	
				for (int i=nextsetting.length - 1;i>nextindex;i--)
					nextsetting[i]=0.0;
			}
		}	   
		System.out.println("Best: " + rbnutilities.arrayToString(best)+": " + max);
	}

	private void unsetIndicators(){
		for (int i=0;i<indicators.size();i++)
			indicators.elementAt(i).unset();	
	}


	/** Returns true if  l1 represents a larger or equal
	 * likelihood than l2. 
	 * Likelihoodvalues are given by l[0] * 1El[1]
	 */
	private boolean compareLikelihood(double[] l1, double[] l2){
		if (likelihoodRatio(l1,l2)>=1)
			return true;
		else
			return false;
	}

	private double likelihoodRatio(double[] l1, double[] l2){

		int power1 = (int)(Math.log(l1[0])/Math.log(10));
		int power2 = (int)(Math.log(l2[0])/Math.log(10));
		double decims1 = l1[0]/Math.pow(10,power1);
		double decims2 = l2[0]/Math.pow(10,power2);
		power1=power1-(int)l1[1];
		power2=power2-(int)l2[1];
		return (decims1/decims2)*Math.pow(10,power1-power2);

	}


	/** Returns the number of nodes in the graph */
	public int numberOfNodes(){       
		return allNodes.size()+1;
	}

	/** Returns the number of indicator nodes in the graph */
	public int numberOfIndicators(){       
		return indicators.size();
	}


	/** Returns the number of links in the graph */
	public int numberOfEdges(){
		int result = llnode.childrenSize();
		Enumeration e = allNodes.elements();

		while (e.hasMoreElements())
			result = result + ((GradientGraphNode)e.nextElement()).childrenSize();

		return result;
	}

	/** Determines whether grad is the zero
	 * vector (or sufficiently close to zero).
	 * @param grad
	 * @return
	 */
	private boolean iszero(double[] grad){
		boolean result = true;
		for (int i=0;i<grad.length;i++){
			if (Math.abs(grad[i])>0)
				result = false;
		}
		return result;
	}

	/** Computes the likelihood value given the current parameter setting
	 * by Gibbs sampling. It is assumed that initIndicators() has been 
	 * successfully executed.
	 * 
	 * Returns double array with result[0]=per node likelihood, 
	 * result[1]=data log-likelihood
	 * 
	 * @return
	 */	
	public double[] computeLikelihood(LearnThread mythread){
		double[] result = new double[2];
		double nextnodelik;

		for (int windowindex = 0; windowindex<windowsize; windowindex++)
			gibbsSample(windowindex,mythread);


		evaluateLikelihoodAndPartDerivs(true);

		if (llnode.numChildren()>0)
			nextnodelik=SmallDouble.nthRoot(currentLikelihood(),llnode.numChildren());
		else
			nextnodelik = 1.0;

		result[0] = result[0] + nextnodelik;
		result [1] = result[1] +  llnode.numChildren()*Math.log(nextnodelik)+this.likelihoodconst;


		return result;

	}


}
