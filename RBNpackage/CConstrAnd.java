/*
* CConstrAnd.java
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
import RBNutilities.*;


public class CConstrAnd extends CConstr {

    CConstr C1,C2;
    
    /** Creates new CConstrAnd */
    public CConstrAnd(CConstr c1,CConstr c2) 
    {
        SSymbs = rbnutilities.arraymerge(c1.SSymbs,c2.SSymbs);
        C1 = c1;
        C2 = c2;
    }
    
    public String[] freevars()
    {
        return rbnutilities.arraymerge(C1.freevars(),C2.freevars());
        
    }
    
    public CConstr substitute(String[] vars, int[] args)
    {
        return new CConstrAnd(C1.substitute(vars,args),C2.substitute(vars,args));
    }
     public CConstr substitute(String[] vars, String[] args)
    {
        return new CConstrAnd(C1.substitute(vars,args),C2.substitute(vars,args));
    }
    
    /*
    public boolean satisfied(RelStruc relstr, int[] args)
    {
        return (C1.satisfied(relstr,args) && C2.satisfied(relstr,args));
    }
    */
    
    public String asString()
    {
        String result;
        result = "(" + C1.asString() + " & " + C2.asString() + ")";
        return result;
    }
    
    public String asString(RelStruc A)
    {
        String result;
        result = "(" + C1.asString(A) + " & " + C2.asString(A) + ")";
        return result;
    }

}
