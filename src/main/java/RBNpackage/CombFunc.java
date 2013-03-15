/*
 * CombFunc.java
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

public abstract class CombFunc extends Object 
{
    public String name;
    
    public static boolean isCombFuncName(String str){
        boolean result = false;
        if (str.equals("mean")) result = true;
        if (str.equals("n-or")) result = true;
	if (str.equals("invsum")) result = true;
	if (str.equals("esum")) result = true;
        return result;
    }
    
    public abstract  double evaluate(double[] args);

    /* args is a vector with 1,0,-1 entries.
     * checks whether any vector that has a 1 where
     * args is 1, a 0 where args is 0, and some
     * arbitrary value where args is -1 will be 
     * evaluated to 1 (return 1), to 0 (return 0),
     * or is not guaranteed to evaluate to either
     * 1 or 0 (return -1)
     */
    public  abstract int evaluatesTo(int[] args);
    
    
}
