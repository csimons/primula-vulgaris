/*
 * RBNReader.java 
 * 
 * Copyright (C) 2003 Max-Planck-Institut fuer Informatik,
 *                    Helsinki Institute for Information Technology
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

import RBNpackage.*;
import RBNExceptions.*;
import java.io.*;
import java.util.*;
import RBNutilities.*;

public class RBNReader extends java.lang.Object {

	/** Creates new RBNReader */
	public RBNReader() {
	}

	public RBN ReadRBNfromFile(String filename) throws IOException, RBNSyntaxException, IllegalArgumentException {
		
		String[] pfstrings = new String[0];

		try {
			pfstrings = GetStrings(filename);
		} catch (IOException e) {
			System.out.println(e);
		}

		RBN intermediate = new RBN(pfstrings.length);
		RBN result = new RBN();

		/**
		 * Table of probrels that have been defined by a probformula 
		 * Used to maintain consistency between defined probabilistic 
		 * relations and probabilistic relations referenced by indicator
		 * formulas.
		 * 
		 * The key of a probrel is the string <name>/<arity>
		 **/
		Hashtable<String, Rel> probrelsdefined = new Hashtable<String, Rel>(pfstrings.length, (float) 1.0);

		/**
		 * Vector of probrels that have been referenced by an indicator formula 
		 * Used to maintain consistency between defined probabilistic 
		 * relations and probabilistic relations referenced by indicator
		 * formulas.
		 **/
		Vector<Rel> probrelsreferenced = new Vector<Rel>();
		
		
		/*
		 * Table of types that have been found in the arguments of probabilistic
		 * relations
		 */
		Hashtable<String, Type> knowntypes = new Hashtable<String, Type>();



		String thisstring;
		int nonaux = 0;
		int current;
		String thisname;
		String[] thisarguments;
		Type[] thistypes;
		ProbForm thisprobform;
		String[] split;

		for (current = 0; current < pfstrings.length; current++) {
			thisstring = pfstrings[current];
			/* thisstring: <name>(<arguments>)=<probformula> */
			split = splitOnName(thisstring);
			thisname = split[0];
			
			split = splitOnDelim(split[1], '(');

			/*
			 * split[0]: (<arguments>) split[1]: =<probformula> 
			 */
			Object[][] parsedargs=parseArgumentList(split[0].substring(1, split[0].length() - 1),knowntypes);
			thisarguments=(String[])parsedargs[0];
			thistypes=(Type[])parsedargs[1];
			
			intermediate.insertRel(new Rel(thisname,thistypes.length,thistypes), current);
			intermediate.InsertArguments(thisarguments, current);

			if (thisname.charAt(0) != '@') {
				nonaux++;
				Rel nextrel = new Rel(thisname, thisarguments.length);
				probrelsdefined.put(nextrel.toStringWArity(), nextrel);
			}
			/* Here the parsing of the probform: */
			thisprobform = StringToProbForm(split[1].substring(1), intermediate, current, probrelsdefined,
					probrelsreferenced,knowntypes);

			String[] checkvars = rbnutilities.arraysubstraction(thisprobform.freevars(), thisarguments);
			if (checkvars.length != 0)
				throw new RBNSyntaxException("Probability formula " + thisprobform.asString()
						+ " contains undeclared free variables " + rbnutilities.arrayToString(checkvars));
			intermediate.InsertProbForm(thisprobform, current);

		}

		/*
		 * Check whether all indicators reference declared probabilistic
		 * relations
		 */
		for (int i = 0; i < probrelsreferenced.size(); i++) {
			String refrel = (probrelsreferenced.elementAt(i)).toStringWArity();
			if (probrelsdefined.get(refrel) == null)
				throw new RBNSyntaxException("Probabilistic relation " + refrel
						+ " is not defined by probability formula");
		}

		result = new RBN(nonaux);
		current = 0;
		for (int i = 0; i < nonaux; i++) {
			while (intermediate.NameAt(current).charAt(0) == '@')
				current++;
			result.insertRel(intermediate.relAt(current), i);
			result.InsertArguments(intermediate.ArgsAt(current), i);
			result.InsertProbForm(intermediate.ProbFormAt(current), i);
			current++;
		}
		return result;
	}

	
	/**
	 * Returns new String that is obtained from str by removing all
	 * non-printable characters
	 **/
	private String RemoveSpecials(String str)
	{
		String result = "";
		char currentchar = 10; // vacuous initialization
		for (int i = 0; i < str.length(); i++) {
			currentchar = str.charAt(i);
			if (33 <= (int) currentchar && (int) currentchar <= 126)
				result = result + currentchar;
		}
		return result;
	}
	
	/**
	 * Returns the contents of file <filename> as an array of strings. Each
	 * relation declaration or formula definition in the file becomes one entry
	 * in the array.
	 **/
	public String[] GetStrings(String filename)

	throws IOException {
		BufferedReader bf = FileIO.openInputFile(filename);
		StreamTokenizer tknizer = new StreamTokenizer(bf);

		LinkedList<String> ll = new LinkedList<String>(); // to initially
		// store the strings
		// read
		String tknstring;

		tknizer.commentChar(37); // %
		tknizer.whitespaceChars(59, 59); // ;
		// tknizer.eolIsSignificant(false);
		tknizer.wordChars(00, 36); // exclude 37: % and 38: &
		tknizer.wordChars(38, 58);
		tknizer.wordChars(60, 126);

		while (tknizer.nextToken() != tknizer.TT_EOF) {
			tknstring = tknizer.sval;
			tknstring = RemoveSpecials(tknstring);
			ll.add(tknstring);
		}
		// Copy LinkedList into array:
		String[] result = new String[ll.size()];
		int pos = 0;
		ListIterator<String> li = ll.listIterator();
		while (li.hasNext()) {
			result[pos] = li.next();
			pos++;
		}
		return result;
	}

	/**
	 * Parses string into CConstr
	 **/
	public CConstr stringToCConstr(String ccstr) throws RBNSyntaxException
	{
		// if (ccstr.length()==0) throw new RBNSyntaxException("Trying to
		// convert empty string into CConstr");
		if (ccstr.length() == 0)
			return new CConstrEmpty();

		int firstchar = ccstr.charAt(0);

		String[] split;

		if (firstchar == '(')
		// And or Or
		{
			split = splitOnCConstr(ccstr.substring(1));
			String first = split[0];
			char op = split[1].charAt(0);
			String second = split[1].substring(1, split[1].length() - 1);
			switch (op) {
			case '&':
				return new CConstrAnd(stringToCConstr(first), stringToCConstr(second));
			case '|':
				return new CConstrOr(stringToCConstr(first), stringToCConstr(second));
			default:
				throw new RBNSyntaxException(" Expected to see boolean operator & or | instead of " + op);
			}
		}
		if (firstchar == '~')
			return new CConstrNeg(stringToCConstr(ccstr.substring(1)));

		if (Character.isLetter((char) firstchar))
		// either S-atom or equality
		{
			split = splitOnName(ccstr);
			switch (split[1].charAt(0)) {
			case '(':
				// S-atom
			{
				String relname = split[0];
				String[] arguments = (String[])parseArgumentList(split[1].substring(1, split[1].length() - 1),null)[0];
				Rel rel = new Rel(relname, arguments.length);
				return new CConstrAtom(rel, arguments);
			}
			case '=':
				// equality
			{
				String firstarg = split[0];
				String secondarg = split[1].substring(1);
				if (!isName(secondarg))
					throw new RBNSyntaxException("String " + secondarg + " should be an identifier");
				return new CConstrEq(firstarg, secondarg);
			}
			}
		}
		throw new RBNSyntaxException("Cannot convert " + ccstr + "into CConstr");
	}

	
	/**
	 * Parses pfstr into ProbForm. 
	 * 
	 * Arguments:
	 * 
	 * rbn: an intermediate rbn containing probabilistic relations for all formulas
	 * read so far, including macro definitions.
	 * 
	 * 
	 **/
	private ProbForm StringToProbForm(String pfstr, 
			RBN rbn, 
			int c, 
			Hashtable<String, Rel> probrelsdefined,
			Vector<Rel> probrelsreferenced, 
			Hashtable<String,Type> knowntypes) 
	throws RBNSyntaxException
	{
		//System.out.println("StringToProbForm for " + pfstr);
		String[] splits;
		String identifier;
		String[] args = new String[0];

		if (pfstr.length() == 0)
			throw new RBNSyntaxException("Trying to convert empty string into ProbForm");

		int firstchar = pfstr.charAt(0);

		if ((48 <= firstchar && firstchar <= 57)
				|| firstchar == '.' 
				||  firstchar == '#')
		// probability constant
		{
			if (firstchar != '#') {
				double p = Double.parseDouble(pfstr);
				return new ProbFormConstant(p);
			} else {
				return new ProbFormConstant(pfstr);
			}
		}

		if (firstchar == '@')
		// reference to probability formula
		// find appropriate formula in rbn
		{
			splits = splitOnName(pfstr);
			// splits[0]: <name> (name starting with @)
			// splits[1]: <arguments>

			identifier = splits[0];
			args=(String[])parseArgumentList(splits[1].substring(1, splits[1].length() - 1),null)[0];

			for (int i = 0; i < c; i++) {
				if (rbn.NameAt(i).equals(identifier)) {
					return rbn.ProbFormAt(i).substitute(rbn.ArgsAt(i), args);
				}
			}
			throw new RBNSyntaxException("Cannot find ProbForm with identifier " + identifier);

		}

		if (firstchar == '(')
		// convex combination
		// (F1:F2,F3)
		{
			ProbForm F1, F2, F3;
			splits = splitOnProbForm(pfstr.substring(1));
			// returns splits[0]: F1
			// splits[1]: :F2,F3)
			F1 = StringToProbForm(splits[0], rbn, c, probrelsdefined, probrelsreferenced,null);
			if (splits[1].charAt(0) != ':')
				throw new RBNSyntaxException("Expected initial ':' in string " + splits[1] + " while processing "
						+ pfstr);
			splits = splitOnProbForm(splits[1].substring(1));
			// returns splits[0]: F2
			// splits[1]: ,F3)
			F2 = StringToProbForm(splits[0], rbn, c, probrelsdefined, probrelsreferenced,null);
			if (splits[1].charAt(0) != ',')
				throw new RBNSyntaxException("Expected initial ',' in string " + splits[1] + " while processing "
						+ pfstr);
			splits = splitOnProbForm(splits[1].substring(1));
			// returns splits[0]: F3
			// splits[1]: )

			F3 = StringToProbForm(splits[0], rbn, c, probrelsdefined, probrelsreferenced,null);
			if (!splits[1].equals(")"))
				throw new RBNSyntaxException("String " + splits[1] + " should be ')'  while processing " + pfstr);
			return new ProbFormConvComb(F1, F2, F3);
		}

		if (Character.isLetter((char) firstchar))
		// Can be an indicator, a combination
		// function, or an sformula.
		{

			splits = splitOnName(pfstr);
			// System.out.println("split name " + splits[0]);
			identifier = splits[0];
			if (splits[1].length() == 0)
				throw new RBNSyntaxException("Identifier " + splits[0] + "followed by empty string");
			if (RelName.legalRelName(identifier))
			// case indicator function
			{
				Rel thisrel;
				Rel existsrel;
				int arity;

				args=(String[])parseArgumentList(splits[1].substring(1, splits[1].length() - 1),null)[0];
				arity = args.length;
				thisrel = new Rel(splits[0], arity);
				existsrel = probrelsdefined.get(thisrel.toStringWArity());
				if (existsrel != null)
					return new ProbFormIndicator(existsrel, args);
				else {
					probrelsreferenced.add(thisrel);
					return new ProbFormIndicator(thisrel, args);
				}
			}
			if (CombFunc.isCombFuncName(identifier))
			// case combination function
			{
				String thiscombf;
				ProbForm[] thisprobforms;
				String[] thisquantvars = new String[0];
				CConstr thiscconstr;

				thiscombf = splits[0];

				// extract F1,...,Fk from
				// splits[1]: {F1,...,Fk| <vars>: <cconstr>}
				String rest = splits[1];
				LinkedList<ProbForm> probformslist = new LinkedList<ProbForm>();
				while (rest.charAt(0) != '|') {
					// remove ',' resp. '{' in first iteration:
					rest = rest.substring(1);
					splits = splitOnProbForm(rest);
					rest = splits[1];
					probformslist.add(StringToProbForm(splits[0], rbn, c, probrelsdefined, probrelsreferenced,null));
				}
				// Turn Linked List into array:
				thisprobforms = new ProbForm[probformslist.size()];
				for (int i = 0; i < probformslist.size(); i++)
					thisprobforms[i] = (ProbForm) probformslist.get(i);
				// now rest: |<vars>: <cconstr>}
				// extract <vars>:
				String varstring;
				int oc = rest.indexOf(':');
				varstring = rest.substring(1, oc);
				rest = rest.substring(oc + 1);
				thisquantvars=(String[])parseArgumentList(varstring,null)[0];
				// now rest: <cconstr>}
				thiscconstr = stringToCConstr(rest.substring(0, rest.length() - 1));
				return new ProbFormCombFunc(thiscombf, thisprobforms, thisquantvars, thiscconstr);
			}
			if (identifier.equals("sformula")) {
				CConstr thiscconstr = stringToCConstr(splits[1].substring(1, splits[1].length() - 1));
				return new ProbFormSFormula(thiscconstr);
			}

		}

		 throw new RBNSyntaxException("Cannot parse " + pfstr + " as ProbForm");
		//return new ProbFormConstant(.5);
	}

	public String[] splitOnDelim(String str, int delim) throws RBNSyntaxException
	/*
	 * delim can be one of the paired delimiters (,),{,},[,] or any special
	 * symbol like :,|,etc. For paired delimiters Function only depends on type
	 * of delimiter, but not on whether it is the opening or closing form. When
	 * e.g. delim = '(' and str is of the form (str1)rest with matching '(' and
	 * ')', then result[0] is (str1) and result[1] is rest. When delim is not a
	 * paired delimiter, then Function simply splits on first occurrence of
	 * delim: For |str1|rest where str1 does not contain '|' result[0] is
	 * |str1|, result[1] is rest.
	 */
	{
		String[] result = new String[2];
		int closingpar, openingpar;
		switch (delim) {
		case '(':
			openingpar = 40;
			closingpar = 41;
			break;
		case ')':
			openingpar = 40;
			closingpar = 41;
			break;
		case '{':
			openingpar = 123;
			closingpar = 125;
			break;
		case '}':
			openingpar = 123;
			closingpar = 125;
			break;
		case '[':
			openingpar = 91;
			closingpar = 93;
			break;
		case ']':
			openingpar = 91;
			closingpar = 93;
			break;
		default:
			openingpar = delim;
			closingpar = delim;
		}

		if (str.length() == 0 || str.charAt(0) != openingpar)
			throw new RBNSyntaxException("Expected to see initial  " + (char) openingpar + " in string " + str);
		int open = 1;
		int i = 1;
		while (open > 0 && i < str.length()) {
			if (str.charAt(i) == closingpar)
				open--;
			if (str.charAt(i) == openingpar)
				open++;
			i++;
		}
		if (open == 0) {
			result[0] = str.substring(0, i);
			result[1] = str.substring(i);
			return result;
		} else
			throw new RBNSyntaxException("Parenthesis mismatch in " + str);
	}

	private boolean isNameChar(int ch)
	// Keep consistent with RelName.isLegal(char) !
	// Here we may allow additional characters
	// to be used only in combination-function names
	// or reserved words.
	{
		boolean result = false;
		if (64 <= ch && ch <= 90)
			result = true; // @,A-Z
		if (97 <= ch && ch <= 122)
			result = true; // a-z
		if (48 <= ch && ch <= 57)
			result = true; // 0-9
		if (95 == ch)
			result = true; // underscore _
		if (45 == ch)
			result = true; // hyphen -
		if (46 == ch)
			result = true; // dot .
		if (35 == ch)
			result = true; // #
		return result;
	}

	private boolean isName(String str) {
		boolean result = true;
		for (int i = 0; i < str.length(); i++)
			if (!isNameChar(str.charAt(i)))
				result = false;
		return result;
	}

	/*
	 * Takes a comma separated list of (optionally) typed identifiers
	 * [type1]name1,...,[typek]namek; Returns a 2 x k array where
	 * result[0] contains  [type1,...,typek], 
	 * and result [1] contains [name1,...,namek]
	 *
	 * Variables without explicit type declaration are given type "Domain"
	 * 
	 * Method can be used with knowntypes=null when str is known to be an
	 * untyped list of arguments, as in atoms occurring in cc-constraints
	 */
	private Object[][] parseArgumentList(String str,  Hashtable<String,Type> knowntypes)
	throws RBNSyntaxException
		

	{
		//System.out.println("parse " + str);
		LinkedList<String> ll = new LinkedList<String>();
		LinkedList<Type> tl = new LinkedList<Type>();
		String[] split;
		String rest = str;
		String typename;
		Type thistype;
		String[] varnamearray;
		Type[] typearray;

		while (rest.length() > 0) {
			/* First add to the type list tl the declared type, or "Domain" type
			 * if no explicit type declaration present
			 */
			if (rest.charAt(0) == 91) {/*
										 * starts with [ -- type specification
										 * follows
										 */
				split = splitOnDelim(rest, 91); /*
												 * split[0]: [typei] 
												 * split[1]: namei,[typei+1]...,namek
												 */
				rest = split[1];
				typename = split[0].substring(1, split[0].length() - 1);
				thistype = knowntypes.get(typename);
					if (thistype != null)
						tl.add(thistype);
					else{
							if (RelName.legalRelName(typename))
								thistype = new TypeRel(typename);
							else if (typename.equals("Integer"))
									thistype = new TypeInteger();
							else if (typename.equals("Domain"))
								thistype = new TypeDomain();
							else 
								throw new IllegalArgumentException("Not a legal type name in declaration " + split[0]);
					
							knowntypes.put(typename,thistype);
							tl.add(thistype);
							}
							
			}
			else {  /* No type declaration in front of first variable */
				if (knowntypes != null) { 
					thistype = knowntypes.get("Domain");
					if (thistype == null){
						thistype = new TypeDomain();
						knowntypes.put("Domain",thistype);
					}
					tl.add(thistype);
				}
			}
			/* Now extract the variable name and add to ll */
			split = splitOnName(rest);
			ll.add(split[0]);
			rest = split[1];
			if (rest.length() >= 2) {
				// remove the comma:
				rest = rest.substring(1);
			} else {
				if (rest.length() == 1)
					throw new IllegalArgumentException("Cannot process " + str);
			}
		}
		varnamearray = new String[ll.size()];
		typearray = new Type[tl.size()];
		for (int i = 0; i < ll.size(); i++){
			varnamearray[i] = ll.get(i);
			//System.out.println("add varname " + varnamearray[i]);
		}
		for (int i = 0; i < tl.size(); i++){
			typearray[i] = tl.get(i);
			//System.out.println("add type " + typearray[i].getName());
		}
		Object[][] result = new Object[2][];
		result[0]=varnamearray;
		result[1]=typearray;
		return result;

	}

	
	/**
	 * Splits string of the form <name><SpecChar><rest> with 
	 * <name>: valid name 
	 * <SpecChar> : first illegal character for names 
	 * into <name> and  <SpecChar><rest>
	 **/
	public String[] splitOnName(String str)
	{
		//System.out.println("Split on name " + str);
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";
		int pos = 0;
		if (!isNameChar(str.charAt(pos)))
			throw new IllegalArgumentException("Did not expect initial special character in " + str);

		while (pos < str.length() && isNameChar(str.charAt(pos))) {
			result[0] = result[0] + str.charAt(pos);
			pos++;
		}
		if (pos < str.length())
			result[1] = str.substring(pos);
		//System.out.println("1: " + result[0] + " 2: " + result[1]);
		return result;
	}

	public String[] splitOnCConstr(String str) throws RBNSyntaxException
	/*
	 * Splits string of the form <CConstr><rest> into <CConstr> and <rest>
	 */
	{
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";

		if (str.length() == 0)
			throw new IllegalArgumentException("Argument string empty in splitOnCConstr");

		int firstchar = str.charAt(0);

		if (firstchar == '(') {
			result = splitOnDelim(str, '(');
			return result;
		}
		if (firstchar == '~') {
			result = splitOnCConstr(str.substring(1));
			return result;
		}
		if (Character.isLetter((char) firstchar))
		// Atom or Equality
		{
			String[] split = splitOnName(str);
			String[] nextsplit;
			if (split[1].charAt(0) == '(')
			// Atom
			{
				nextsplit = splitOnDelim(split[1], '(');
				result[0] = split[0] + nextsplit[0];
				result[1] = nextsplit[1];
				return result;
			}
			if (split[1].charAt(0) == '=')
			// Equality
			{
				nextsplit = splitOnName(split[1].substring(1));
				result[0] = split[0] + "=" + nextsplit[0];
				result[1] = nextsplit[1];
				return result;
			}
		}
		// must never get here:
		throw new RBNSyntaxException("Error in splitOnCConstr for argument " + str);
	}

	/**
	 * Splits string of the form <ProbForm><rest> into <ProbForm> and <rest>
	 **/
	public String[] splitOnProbForm(String str) throws RBNSyntaxException
	{
		//System.out.println("Split on ProbForm " + str);
		String[] result = new String[2];
		result[0] = "";
		result[1] = "";

		String[] firstsplit;
		String[] secondsplit;

		if (str.length() == 0)
			throw new IllegalArgumentException("Argument string empty in splitOnProbForm");

		int firstchar = str.charAt(0);

		if ((48 <= firstchar && firstchar <= 57) // 0-9
				|| firstchar == '.'  
				|| firstchar == '#')
			// probability constant -- can be
			// extracted with splitOnName
			// Note: identifiers may contain
			// . or 0, but must start with
			// letter!
			result = splitOnName(str);

		if (firstchar == '@')
		// reference to probability formula
		{
			firstsplit = splitOnName(str);
			secondsplit = splitOnDelim(firstsplit[1], '(');
			result[0] = firstsplit[0] + secondsplit[0];
			result[1] = secondsplit[1];
		}

		if (firstchar == '(')
			// convex combination
			result = splitOnDelim(str, '(');
		if (Character.isLetter((char) firstchar))
		// starts with an identifier:
		// could be either indicator or
		// combination function.
		// Cases distinguished by opening
		// parenthesis following
		// identifier
		{
			firstsplit = splitOnName(str);
			if (firstsplit[1].length() == 0)
				throw new IllegalArgumentException("Identifier followed by empty string");
			if (firstsplit[1].charAt(0) == 40 || firstsplit[1].charAt(0) == 123) {
				secondsplit = splitOnDelim(firstsplit[1], firstsplit[1].charAt(0));
				result[0] = firstsplit[0] + secondsplit[0];
				result[1] = secondsplit[1];
			} else
				throw new IllegalArgumentException("Expected to see ( or { following " + firstsplit[0]);

		}

		//System.out.println("*1: " + result[0] + " *2: " + result[1]);
		return result;
	}

}
