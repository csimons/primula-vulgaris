/*
* EditPanel.java 
* a
* Copyright (C) 2005 Aalborg University
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

package RBNgui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.*;
import java.awt.geom.*;

import RBNpackage.*;

public class EditPanel extends JPanel implements MouseListener, MouseMotionListener {


  private int RADIUS = 15;
  private double ARROW_SIZE = 6.0;
  private double ARROW_HALF_SIZE = 3.0;
  private int nodeName = -1;
  private int x, y;
  private boolean nodeClick;
  private boolean first = true;
  private int fromnode = 0;
  private Point from, to;

  private int mode                 = 0;
  private final int ADDNODE        = 1;
  private final int DELETENODE     = 2;
  private final int MOVENODE       = 5;
  private final int ADDTUPLE       = 3;
  private final int DELETERELATION = 4;

  private int xmax = 3000;
  private int ymax = 3000;

  private final Bavaria bavaria;
  private Rel r = new Rel();;
  private int aritynumber;
  private int index;
  private int[] nodes;
  private String addedTuples = "";
  private int relListItem  = -1;
  private int attrListItem = -1;
  private boolean isGridOn = false;
  private int gridSize = 0;
  private int xsetting = 0;

  private Vector openNodeWindows = new Vector();

	private double zoom = 1;

  public EditPanel(Bavaria b){

    bavaria = b;


    this.addMouseMotionListener( this );


    this.addMouseListener( this );

  }

	public void mouseDragged(MouseEvent e){
    if(nodeClick && mode == MOVENODE && e.getModifiers()==InputEvent.BUTTON1_MASK){
      Vector coords = bavaria.getCoords();
      int[] t = (int[])coords.elementAt(nodeName);
      t[0] =  (int)(e.getX()/zoom);
      t[1] =  (int)(e.getY()/zoom);
//      t[0] =  e.getX();
//      t[1] =  e.getY();
      if(t[0] < 0)
        t[0] = 0;
      if(t[0] >= (int)(xmax/zoom))
        t[0] = (int)(xmax/zoom)-1;
      if(t[1] < 0)
        t[1] = 0;
      if(t[1] >= ymax)
        t[1] = ymax-1;
      bavaria.setIsEdited(true);
      repaint();
    }
    mouseMoved(e);
	}
	public void mouseMoved(MouseEvent e){
    bavaria.showCoordinates(e.getX(), e.getY());
	}


	public void mouseClicked(MouseEvent e){
	}
 	public void mouseEntered(MouseEvent e){
	}
 	public void mouseExited(MouseEvent e){
	}
 	public void mousePressed(MouseEvent e){
    x = (int)(e.getX()/zoom);
    y = (int)(e.getY()/zoom);
//    x = e.getX();
//    y = e.getY();


    nodeClick = nodeClicked(x, y);

    //Inside a node and right mouse button clicked so show node information
    if(nodeClick && e.getModifiers()==InputEvent.BUTTON3_MASK){
      openNodeWindow(nodeName);
    }

    //Clicked outside a node and add node mode on
    if (!nodeClick && mode == ADDNODE && e.getModifiers()==InputEvent.BUTTON1_MASK){
      bavaria.addNode(x, y);
      repaint();
    }

    //Clicked inside a node and delete node mode on
    if (nodeClick && mode == DELETENODE && e.getModifiers()==InputEvent.BUTTON1_MASK){
      bavaria.deleteNode(nodeName);
      repaint();
    }

    //Clicked inside a node and add tuple mode on
    if (nodeClick && mode == ADDTUPLE && e.getModifiers()==InputEvent.BUTTON1_MASK) {
     //attribute
      if (r.arity == 1){
        int[] node = {nodeName};
        addedTuples = addedTuples + bavaria.nameAt(nodeName);
        bavaria.addTuple(r, node, addedTuples);
        addedTuples = "";
        repaint();
      }
      //binary relation
      if (r.arity == 2){
        if (first){
          fromnode = nodeName;
          addedTuples = addedTuples + bavaria.nameAt(fromnode);
          bavaria.showMessage(addedTuples);
          first = false;
        }
        else if(!first){
          int[] nodes = {fromnode, nodeName};
          addedTuples = addedTuples + ", " + bavaria.nameAt(nodeName);
          bavaria.addTuple(r, nodes, addedTuples);
          addedTuples = "";
          first = true;
          repaint();
        }
      }
      //arbitrary relation
      if (r.arity >= 3){
        if (first){
          aritynumber = r.arity;
          nodes = new int[aritynumber];
          index = 0;
          nodes[index] = nodeName;
          index++;
          aritynumber--;
          addedTuples = addedTuples + bavaria.nameAt(nodeName);
          bavaria.showMessage(addedTuples);
          first = false;
        }
        else if(!first){
          if (aritynumber > 0){
            nodes[index] = nodeName;
            index++;
            aritynumber--;
            addedTuples = addedTuples + ", " + bavaria.nameAt(nodeName);
            if (aritynumber > 0)
              bavaria.showMessage(addedTuples);
            else{
              bavaria.addTuple(r, nodes, addedTuples);
              addedTuples = "";
              first = true;
            }
          }
        }
      }
    }
	}
 	public void mouseReleased(MouseEvent e){
		Object source = e.getSource();
	}



	public void setZoom( double zoom ){
      if(this.zoom<zoom){
      	xmax = (int)(xmax/this.zoom);
      	ymax = (int)(ymax/this.zoom);
      }
		this.zoom = zoom;
      bavaria.setXSize((int)(xmax*zoom));
      bavaria.setYSize((int)(ymax*zoom));
      bavaria.repaint();
		repaint();
	}

  public double getZoom(){
    return zoom;
  }


  //Returns true, if user clicks a node
  public boolean nodeClicked(long x, long y){

    Vector coordinates = bavaria.getCoords();
    boolean nodeClick = false;

    for (int i=0; i<coordinates.size(); ++i){
      int[] temp = (int[])coordinates.elementAt(i);
      long xc =	temp[0];
      long yc =	temp[1];

      //Checks if the click was inside a node
      if ( (((x-xc)*(x-xc))+((y-yc)*(y-yc))) < RADIUS*RADIUS ){
        nodeClick = true;
        //lets take the newer one (which overlaps the older one)
        nodeName = i;
      }
    }
    return nodeClick;
  }


  //max x-coordinate (move-command needs to know this)
  public void setXSize(int x){
  	xsetting = (int)(x/zoom);
    xmax = x;
    this.setPreferredSize(new Dimension(xmax, ymax)); //needed
  }


  //max y-coordinate (move-command needs to know this)
  public void setYSize(int y){
    ymax = y;
    this.setPreferredSize(new Dimension(xmax, ymax));
  }


  //sets the state and the size of the grid
  public void setGrid(boolean state, int size){
    isGridOn = state;
    gridSize = size;
  }


  public void setMode(int mode){
    this.mode = mode;
  }


  public void setRel(Rel re){
    r = re;
    first = true;   //changing relation
    addedTuples = "";
  }

  //reset the tuple
  public boolean reset(){
    if (first == false){	//are we creating a new tuple
      first = true;
      addedTuples = "";
      return false;
    }
    else
      return true;
  }


  //Snaps the nodes to the grid
  public void snapToGrid(){

    if(isGridOn){
      Vector coordinates = bavaria.getCoords();
      int x_left, x_right;
      int y_up, y_down;


      for(int i=0; i<coordinates.size(); ++i){
        int[] coords = (int[])coordinates.elementAt(i);
        x_left = x_right = coords[0];
        y_up = y_down = coords[1];

        //x coordinate
        while(x_right % gridSize != 0)  //if isGridOn is true then gridSize != 0
          x_right++;  //to the right
        while(x_left % gridSize != 0)
          x_left--;   //to the left

        //which side is closer
        if(coords[0]-x_left < x_right-coords[0])
          coords[0] = x_left;
        else
          coords[0] = x_right;

        //y coordinate
        while(y_up % gridSize != 0)
          y_up--;    //to the north
        while(y_down % gridSize != 0)
          y_down++;  //to the south

        if(coords[1]-y_up < y_down-coords[1])
          coords[1] = y_up;
        else
          coords[1] = y_down;
      }
    }
  }



  //Node window related methods----------------------------------------------------------------

  //Creates a new node window and adds it to the openNodeWindows vector if there doesn't exist one
  public void openNodeWindow(int index){
    boolean found = false;
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      if(n.getIndex() == index){
        found = true;
        break;
      }
    }
    if(!found){
      NodeWindow n = new NodeWindow(bavaria, this, index);
      openNodeWindows.addElement(n);
    }
  }


  //Removes the node window from the openNodeWindows vector
  public void closeNodeWindow(NodeWindow closed){
    openNodeWindows.remove(closed);
  }


  //Will close all the open node windows. Bavaria calls when it gets closed
  public void closeAllNodeWindows(){
    while(openNodeWindows.size() > 0){
      try{
        NodeWindow n = (NodeWindow)openNodeWindows.firstElement();
        n.disposeNodeWindow();
      } catch (Exception e){
      }
    }
  }


  //Adds a new relation (tab) to the node window
  public void addNewRelToNodeWindow(Rel r){
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      n.addNewRelToNodeWindow(r);
     }
  }


  //Removes the relation from the node window
  public void deleteRelFromNodeWindow(Rel r){
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      n.deleteRelFromNodeWindow(r);
    }
  }


  //Adds tuple to the open node window if it belongs to the tuple
  public void addTupleToNodeWindow(Rel r, int[] tuple){
    if(r.arity == 1){
      for(int i=0; i<openNodeWindows.size(); ++i){
        NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
        if(n.getIndex() == tuple[0]){
          n.addTupleToNodeWindow(r, tuple);
          break;
        }
      }
    }
    else{
      for(int i=0; i<openNodeWindows.size(); ++i){
        NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
        n.addTupleToNodeWindow(r, tuple);
      }
    }
  }


  //Removes the tuples which include the deleted node and disposes the deleted node's window
  public void nodeWindowNodeDeleted(int deletedNode){
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      if(n.getIndex() == deletedNode){
        n.disposeNodeWindow();
        --i;
      }
      else{
        n.nodeWindowNodeDeleted(deletedNode);
        if(n.getIndex() > deletedNode)
          n.changeIndex();
      }
    }
  }


  //Removes the tuple from all the open node windows
  public void tupleDeletedFromNodeWindow(Rel r, int[] tuple){
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      n.tupleDeletedFromNodeWindow(r, tuple);
    }
  }


  //Updates the node's name in the open node windows
  public void updateNodeName(int index){
    for(int i=0; i<openNodeWindows.size(); ++i){
      NodeWindow n = (NodeWindow)openNodeWindows.elementAt(i);
      n.updateNodeName(index);
    }
  }

  //Node window related methods ends-----------------------------------------------------------


  //Painting related methods-------------------------------------------------------------------

  public void paintComponent(Graphics g){

    //To use Java 2D API features, you cast the Graphics object passed into a
    //component's rendering method to a Graphics2D object.
    Graphics2D g2 = (Graphics2D)g;
    super.paintComponent(g2);

    Vector elementnames = bavaria.getNames();
    Vector coordinates	= bavaria.getCoords();
    int xc, yc, lkm=0;

		//zoom function
		g2.scale( zoom, zoom );

    //Draw the grid
    if(isGridOn)
      drawGrid(g2, gridSize);

    //coordinates.size should equal elementnames.size
    for(int i=0; i<coordinates.size() && i<elementnames.size(); ++i){
	int[] temp = (int[])coordinates.elementAt(i);
	//System.out.println(temp[0]+"  "+temp[1]);
      xc = temp[0];
      yc = temp[1];
  
  
      //Draw the name of the node
      g2.setPaint(Color.black);
      g2.drawString((String)elementnames.elementAt(i), xc-(RADIUS+2), yc-(RADIUS+2));

      //Draw the attributes as a pie diagram
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      drawPie(g2, xc, yc, bavaria.getAttributesColors(i));

      //Draw the binary relations as a lines
      g2.setStroke(new BasicStroke(3.0f));
      drawLine(g2, bavaria.getBinaryColors(i), coordinates);
      g2.setStroke(new BasicStroke(1.0f));
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	
    }
  }


  //Draws the grid
  public void drawGrid(Graphics2D g2, int size){
    g2.setPaint(Color.lightGray);
    int y = size;
    while(y < (int)(ymax/zoom)){ //draw horizontal lines
      g2.drawLine(0, y, (int)(xmax/zoom), y);
      y = y + size;
    }
    int x = size;
    while(x < (int)(xmax/zoom)){ //draw vertical lines
      g2.drawLine(x, 0, x, (int)(ymax/zoom));
      x = x + size;
    }
  }


  //Draws the attributes as a pie diagram
  public void drawPie(Graphics2D g2, int x, int y, Vector colors) {

    int xc = x;
    int yc = y;
    Rectangle area = new Rectangle(xc-RADIUS, yc-RADIUS, 2*RADIUS, 2*RADIUS);
    double total = colors.size();

    // Draw each pie slice
    int startAngle = 0;
    for (int i=0; i<colors.size(); i++) {
      int arcAngle = (int)(360 / total);

      // Ensure that rounding errors do not leave a gap between the first and last slice
      if (i == colors.size()-1) {
        arcAngle = 360 - startAngle;
      }

      // Set the color and draw a filled arc
      g2.setPaint( (Color)colors.elementAt(i) );
      g2.fillArc(area.x, area.y, area.width, area.height, startAngle, arcAngle);

      startAngle += arcAngle;
    }
    //Finally, draw black edge around the pie
    g2.setPaint(Color.black);
    g2.draw(new Ellipse2D.Double(xc-RADIUS, yc-RADIUS, 2*RADIUS, 2*RADIUS));
  }



  //Draws the binary relations as a lines between the nodes
  public void drawLine(Graphics2D g2, Vector[] v, Vector coords){

    Vector nodes	= v[0];  //binary relations from this node to some other
    Vector colors = v[1];  //the colors of the relations

    int x1=0; int y1=0; int x2=0; int y2=0;  //coordinates
    //variables for calculating the line and arrow
    int x_point1, x_point2, x_point3, y_point1, y_point2, y_point3;
    double dir_x, dir_y, distance;
    double head_x = 0; double head_y = 0; double start_x = 0; double start_y = 0;
    double diff_x = 0; double diff_y = 0; double x_part = 0; double y_part = 0;
    double[] startPoint, headPoint;
    boolean notInSafearea;  //not too close with the other node
    boolean itself;         //in relation with itself

    for(int i=0; i<nodes.size(); ++i){
      Vector lineColor = new Vector();  //colors of the line
      int[] tuple = (int[])nodes.elementAt(i);
      lineColor.addElement((Color)colors.elementAt(i));
      int[] from = (int[])coords.elementAt(tuple[0]);
      int[] to = (int[])coords.elementAt(tuple[1]);

      notInSafearea = true;
      itself = false;

      x1 = from[0];
      y1 = from[1];
      x2 = to[0];
      y2 = to[1];


      if((x1+2*RADIUS+ARROW_SIZE) < x2){	//to the right
        if(y2 < y1-130){	//north-east
          x1 = x1-5;
          y1 = y1-5;
          x2 = x2-5;
          y2 = y2+5;
        }
        else if(y2 > y1+130){  //south-east
          x1 = x1+5;
          y1 = y1+5;
          x2 = x2+5;
          y2 = y2-5;
        }
        else{  //east
          x1 = x1+5;
          y1 = y1-5;
          x2 = x2-5;
          y2 = y2-5;
        }
        startPoint = horizontalIP(from[0], from[1], (double)x1, (double)y1, (double)x2, (double)y2, true);
        start_x = startPoint[0];
        start_y = startPoint[1];
        headPoint = horizontalIP(to[0], to[1], (double)x1, (double)y1, (double)x2, (double)y2, false);
        head_x = headPoint[0];
        head_y = headPoint[1];
      }

      else if((x1-2*RADIUS-ARROW_SIZE) > x2){  //to the left
        if(y2 < y1-130){	//north-west
          x1 = x1-5;
          y1 = y1-5;
          x2 = x2-5;
          y2 = y2+5;
        }
        else if(y2 > y1+130){  //south-west
          x1 = x1+5;
          y1 = y1+5;
          x2 = x2+5;
          y2 = y2-5;
        }
        else{  //west
          x1 = x1-5;
          y1 = y1+5;
          x2 = x2+5;
          y2 = y2+5;
        }
        startPoint = horizontalIP(from[0], from[1], (double)x1, (double)y1, (double)x2, (double)y2, false);
        start_x = startPoint[0];
        start_y = startPoint[1];
        headPoint = horizontalIP(to[0], to[1], (double)x1, (double)y1, (double)x2, (double)y2, true);
        head_x = headPoint[0];
        head_y = headPoint[1];
      }

      else{
        if(y2 < y1-2*RADIUS-ARROW_SIZE){	//north
          x1 = x1-5;
          y1 = y1-5;
          x2 = x2-5;
          y2 = y2+5;
          startPoint = verticalIP(from[0], from[1], (double)x1, (double)y1, (double)x2, (double)y2, true);
          start_x = startPoint[0];
          start_y = startPoint[1];
          headPoint = verticalIP(to[0], to[1], (double)x1, (double)y1, (double)x2, (double)y2, false);
          head_x = headPoint[0];
          head_y = headPoint[1];
        }
        else if(y2 > y1+2*RADIUS+ARROW_SIZE){  //south
          x1 = x1+5;
          y1 = y1+5;
          x2 = x2+5;
          y2 = y2-5;
          startPoint = verticalIP(from[0], from[1], (double)x1, (double)y1, (double)x2, (double)y2, false);
          start_x = startPoint[0];
          start_y = startPoint[1];
          headPoint = verticalIP(to[0], to[1], (double)x1, (double)y1, (double)x2, (double)y2, true);
          head_x = headPoint[0];
          head_y = headPoint[1];
        }
        else if(x1 == x2 && y1 == y2 && tuple[0] == tuple[1]){  //in relation with itself
          notInSafearea = false;
          itself = true;
        }
        else{
          //System.out.println("SAFEAREA");
          notInSafearea = false;	//don't draw anything, too damn close and tuple[0] != tuple[1]
        }
      }


      //Are there multiple binary relations between these two nodes? If yes then we need the
      //colors of those relations so that we can color the line between the nodes.

      for(int j=i+1; j<nodes.size(); ++j){
        int[] temp = (int[])nodes.elementAt(j);
        if (tuple[1] == temp[1]){
          nodes.removeElementAt(j);
          lineColor.addElement((Color)colors.elementAt(j));
          colors.removeElementAt(j);
          --j;
        }
      }


      //If not too close then calculate the arrow, draw and color the line and draw the arrow
      if(notInSafearea){
        diff_x = head_x-start_x;
        diff_y = head_y-start_y;
        distance = Math.sqrt(diff_x * diff_x + diff_y * diff_y);
        dir_x = diff_x/distance;	//cos
        dir_y = diff_y/distance;	//sin

        head_x = head_x - ARROW_SIZE * dir_x;
        head_y = head_y - ARROW_SIZE * dir_y;

        x_point1= (int)(head_x - ARROW_HALF_SIZE*dir_x + ARROW_SIZE*dir_y);
        x_point2= (int)(head_x - ARROW_HALF_SIZE*dir_x - ARROW_SIZE*dir_y);
        x_point3= (int)(head_x + ARROW_SIZE*dir_x);

        y_point1= (int)(head_y - ARROW_HALF_SIZE*dir_y - ARROW_SIZE*dir_x);
        y_point2= (int)(head_y - ARROW_HALF_SIZE*dir_y + ARROW_SIZE*dir_x);
        y_point3= (int)(head_y + ARROW_SIZE*dir_y);

        int archead_x[] = { x_point1, x_point2, x_point3, x_point1 };
        int archead_y[] = { y_point1, y_point2, y_point3, y_point1 };

        x_part = diff_x/lineColor.size();
        y_part = diff_y/lineColor.size();

        for(int k=0; k<lineColor.size(); ++k){
          g2.setPaint((Color)lineColor.elementAt(k));
          g2.draw(new Line2D.Double(start_x, start_y, start_x+x_part, start_y+y_part));
          start_x = start_x+x_part;
          start_y = start_y+y_part;
        }

        g2.fill(new Polygon(archead_x, archead_y, 4));

      }
      //Draw the loopy arrow
			if(itself){
        Rectangle area = new Rectangle(x1, y1, 2*RADIUS, 2*RADIUS);
        int startAngle = 85;
        int arcAngle = (int)(260 / lineColor.size());
        for(int l=0; l<lineColor.size(); ++l){
          g2.setPaint((Color)lineColor.elementAt(l));
          g2.drawArc(area.x, area.y, area.width, area.height, startAngle, -arcAngle);
          startAngle -= arcAngle;
        }

        dir_x = 0;
        dir_y = -1;

        head_x = x1+1;
        head_y = y1 + RADIUS + ARROW_SIZE;

        x_point1= (int)(head_x - ARROW_HALF_SIZE*dir_x + ARROW_SIZE*dir_y);
        x_point2= (int)(head_x - ARROW_HALF_SIZE*dir_x - ARROW_SIZE*dir_y);
        x_point3= (int)(head_x + ARROW_SIZE*dir_x);

        y_point1= (int)(head_y - ARROW_HALF_SIZE*dir_y - ARROW_SIZE*dir_x);
        y_point2= (int)(head_y - ARROW_HALF_SIZE*dir_y + ARROW_SIZE*dir_x);
        y_point3= (int)(head_y + ARROW_SIZE*dir_y);

        int archead_x[] = { x_point1, x_point2, x_point3, x_point1 };
        int archead_y[] = { y_point1, y_point2, y_point3, y_point1 };

        g2.fill(new Polygon(archead_x, archead_y, 4));
			}
		}
	}



  //Calculates the horizontal intersection point
  public double[] horizontalIP(double center_x, double center_y, double x1, double y1, double x2, double y2, boolean toRight){

    double x2c, y2c, xc, yc, a;
    double xl, b, yl;
    double x2i, xi, c;
    double x_coord, y_coord;

    //	circle
    x2c = 1;
    y2c = 1;
    xc	= -2*center_x;
    yc	= -2*center_y;
    a 	= center_x*center_x+center_y*center_y-(RADIUS+1)*(RADIUS+1);

    //System.out.println("circle: "+x2c+"x2 "+y2c+"y2 "+xc+"x "+yc+"y "+a);

    //	line   if x1==x2 then x2-x1=0 !!!
    xl = (y2-y1)/(x2-x1);
    b  = xl*(-x1)+y1;

    //System.out.println("line :"+xl+"x "+b);

    //	intersection formula
    x2i = x2c + xl*xl;
    xi	= 2*xl*b + xc +  yc*xl;
    c 	= b*b + yc*b + a;

    //System.out.println("formula: "+x2i+"x2 "+xi+"x "+c);

    if(toRight){
      x_coord = (-xi + Math.sqrt(xi*xi-4*x2i*c))/(2*x2i);
      y_coord = x_coord*xl+b;
      double[] xy_coords = {x_coord, y_coord};
      return xy_coords;
    }
    else {
      x_coord = (-xi - Math.sqrt(xi*xi-4*x2i*c))/(2*x2i);
      y_coord = x_coord*xl+b;
      double[] xy_coords={x_coord, y_coord};
      return xy_coords;
    }
  }



  //Calculates the vertical intersection point
  public double[] verticalIP(double center_x, double center_y, double x1, double y1, double x2, double y2, boolean upper){

    double x2c, y2c, xc, yc, a;
    double xl, b, yl;
    double y2i, yi, c;
    double x_coord, y_coord;

    //	circle
    x2c = 1;
    y2c = 1;
    xc	= -2*center_x;
    yc	= -2*center_y;
    a 	= center_x*center_x+center_y*center_y-(RADIUS+1)*(RADIUS+1);

    //System.out.println("circle: "+x2c+"x2 "+y2c+"y2 "+xc+"x "+yc+"y "+a);

    //	line
    yl = (x2-x1)/(y2-y1);
    b  = yl*(-y1)+x1;

    //System.out.println("line :"+yl+"x "+b);

    //	intersection formula
    y2i = y2c + yl*yl;
    yi	= 2*yl*b + yc +  xc*yl;
    c 	= b*b + xc*b + a;

    //System.out.println("formula: "+y2i+"x2 "+yi+"x "+c);

    if(upper){
      y_coord = (-yi - Math.sqrt(yi*yi-4*y2i*c))/(2*y2i);
      x_coord = y_coord*yl+b;
      double[] xy_coords = {x_coord, y_coord};
      return xy_coords;
    }
    else {
      y_coord = (-yi + Math.sqrt(yi*yi-4*y2i*c))/(2*y2i);
      x_coord = y_coord*yl+b;
      double[] xy_coords={x_coord, y_coord};
      return xy_coords;
    }
  }
}









