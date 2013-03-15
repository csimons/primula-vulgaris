/*
* Rel.java
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

import java.awt.Color;
import java.io.*;
import org.dom4j.Element;
import org.dom4j.Document;



public class Rel implements Serializable{

    public RelName name;
    public int arity;
    private Type[] argtypes;
    
    public Color color;
    private Color[] attributecolors = {new Color(198,17,16), new Color(16,120,16), new Color(0,10,210),
                    new Color(255,145,0), new Color(50,195,66), new Color(100,55,0), new Color(130,0,0),
                    new Color(25,25,25), new Color(114,114,114), new Color(100,50,198), new Color(192,48,254),
                    new Color(255,0,255), new Color(205,205,0), new Color(0,255,0), new Color(0,70,0),
                    new Color(0,60,60), new Color(185,50,50), Color.cyan, new Color(185,155,60), Color.black};

    private Color[] binarycolors = {new Color(185,155,60), new Color(50,195,66), new Color(192,48,254),
                    new Color(0,70,0), new Color(255,145,0), new Color(205,205,0), new Color(0,10,210), new Color(185,50,50),
                    new Color(0,60,60), new Color(100,50,198), new Color(114,114,114), new Color(255,0,255),
                    new Color(198,17,16), new Color(100,55,0), new Color(25,25,25), new Color(0,255,0), Color.cyan,
                    new Color(16,120,16), new Color(130,0,0), Color.black};


    private static int attributecounter = 0;
    private static int binarycounter = 0;

 
    public Rel(){
        name = new RelName();
        arity = 0;
    }

    public Rel(String n, int a){
    	name = new RelName(n);
    	arity =a;
    	argtypes=new TypeDomain[a];
    	for (int i=0;i<a;i++)
    		argtypes[i]=new TypeDomain();
    	if (arity == 1)
    		color = chooseAttributeColor();
    	else if (arity == 2 && (n.equals("pred") || n.equals("less")))
    		color = Color.black;
    	else if (arity == 2)
    		color = chooseBinaryColor();
    }

    public Rel(String n, int a, Type[] types){
    	name = new RelName(n);
    	arity =a;
    	argtypes= types;
    	if (arity == 1)
    		color = chooseAttributeColor();
    	else if (arity == 2 && (n.equals("pred") || n.equals("less")))
    		color = Color.black;
    	else if (arity == 2)
    		color = chooseBinaryColor();
    }
    public boolean equals( Rel r ){
    	//System.out.println(r.toStringWArity() + " equals " + this.toStringWArity() +" ?");
    	return this.name.equals( r.name ) && (this.arity == r.arity);
    }

    public int getArity(){
	return arity;
    }

    public String printname(){
        return name.name;
    }

    //Bavaria needs the following methods
    public String toString(){
	if (arity == 1 || arity == 2)
	    return name.name;
	else
	    return this.toStringWArity();
    }

    public String toStringWArity(){
	return name.name+"/"+arity;
    }


    public Color chooseAttributeColor(){
	Color c = attributecolors[attributecounter];
      if(attributecounter < attributecolors.length-1)
        attributecounter = attributecounter + 1;
      return c;
    }

    public Color chooseBinaryColor(){
      Color c = binarycolors[binarycounter];
      if(binarycounter < binarycolors.length-1)
        binarycounter = binarycounter + 1;
      return c;
    }

    public static void resetTheColorCounters(){
      attributecounter = 0;
      binarycounter    = 0;
    }

  // Thrane
  	public void setColor( Color color ){
  		this.color = color;
  	}

  	public Color getColor(){
  		return color;
  	}
  // Thrane

  	public Type[] getTypes(){
  		return argtypes;
  	}

  	public String getTypesAsString(){
  		String result = "";
  		for (int i=0;i<arity;i++){
  			if (argtypes[i]==null)
  				System.out.println("argtypes " + i + " = null in " + this.printname());
  			result = result + argtypes[i].getName() + ",";
  		}
  		result = result.substring(0,result.length()-1);
  		return result;
  	}

  	/** Adds to root an element containing the 
  	 * header information for this Rel
  	 * @param root
  	 */
  	public void addRelHeader(Element root, String def, String inputoutput){
 
  		Element relel = root.addElement("Rel");
  		relel.addAttribute("name", name.name);
  		relel.addAttribute("arity", Integer.toString(arity));
 		relel.addAttribute("argtypes", getTypesAsString());
 		relel.addAttribute("valtype", "boolean");
 		relel.addAttribute("default", def);
 		relel.addAttribute("type", inputoutput);
  		if (arity==1 || arity == 2)
  			relel.addAttribute("color", color.toString());
  	}
}

