/*
* NodeWindow.java 
* x
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
import javax.swing.event.*;
import java.util.*;

import RBNpackage.*;


public class NodeWindow extends JFrame implements KeyListener, ActionListener, 
	MouseListener, ChangeListener{

  private JTextField nodeNameField = new JTextField(10);
  private JLabel nodeNameLabel     = new JLabel("Node name:");
  private JLabel nodeNameReserved  = new JLabel(" ");
  private JLabel attributesLabel   = new JLabel("Attributes");
  private JTabbedPane tabbedPane   = new JTabbedPane();
  private JList attributesList;
  private Bavaria bavaria;
  private EditPanel editPanel;
  private JButton deleteAttribute = new JButton("Delete Attribute");

  private Vector[] attributes; //arity 1
  private Vector[] otherRels;  //arity 2 or bigger
  private Vector attributeRels;
  private Vector attributeTuples;
  private Vector binAndArityRels;
  private Vector binAndArityTuples;

  private int attrListItem = -1;
  private int relListItem  = -1;
  private int index = -1;

  private DefaultListModel attributesListModel;
  private Vector binAndArityListModels = new Vector();
  private Vector binAndArityLists      = new Vector();
  /*binAndArityRels i == binAndArityTuples i == binAndArityListModels i == binAndArityLists i
    == tab number i */


  public NodeWindow(Bavaria b, EditPanel e, int ind){

    index     = ind;
    bavaria   = b;
    editPanel = e;

    attributes = bavaria.getAttrRelsAndTuples(index);
    otherRels  = bavaria.getOtherRelsAndTuples(index);

    attributeRels 	  = attributes[0];
    attributeTuples   = attributes[1];
    binAndArityRels   = otherRels[0];
    binAndArityTuples	= otherRels[1];

    //renaming the node
    nodeNameField.setText(bavaria.nameAt(index));
    nodeNameField.addKeyListener( this );


    //create attributes list
    attributesListModel              = new DefaultListModel();
    attributesList			 = new JList();
    JScrollPane attributesScrollList = new JScrollPane();

    for(int i=0; i<attributeRels.size(); ++i){
      attributesListModel.addElement((Rel)attributeRels.elementAt(i));
    }

    attributesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    attributesList.setModel(attributesListModel);
    attributesScrollList.getViewport().add(attributesList);

    deleteAttribute.addActionListener( this );

    attributesList.addMouseListener( this );
    JPanel attributesPanel = new JPanel(new BorderLayout());
    attributesPanel.add(attributesLabel , BorderLayout.NORTH);
    attributesPanel.add(attributesScrollList, BorderLayout.CENTER);
    attributesPanel.add(deleteAttribute, BorderLayout.SOUTH);
    //end attributes list


    //create tabs
    for(int i=0; i<binAndArityRels.size(); ++i){
      Rel currentRel = (Rel)binAndArityRels.elementAt(i);
      Vector tuples  = (Vector)binAndArityTuples.elementAt(i);
      JPanel tab = createTab(currentRel, tuples);
      tabbedPane.addTab(currentRel.name.name, tab);
    }
    //end tabs

    tabbedPane.addChangeListener( this );

    JPanel nameFieldAndLabels = new JPanel(new BorderLayout(3, 3));
    nameFieldAndLabels.add(nodeNameLabel, BorderLayout.WEST);
    nameFieldAndLabels.add(nodeNameField, BorderLayout.CENTER);
    nameFieldAndLabels.add(nodeNameReserved, BorderLayout.SOUTH);

    JPanel relationPanel = new JPanel(new GridLayout(2, 1, 3, 3));
    relationPanel.add(attributesPanel);
    relationPanel.add(tabbedPane);

    Container contentPane = this.getContentPane();
    contentPane.setLayout(new BorderLayout(5, 2));
    contentPane.add(nameFieldAndLabels, BorderLayout.NORTH);
    contentPane.add(relationPanel, BorderLayout.CENTER);

    ImageIcon icon = new ImageIcon("small_logo.jpg");
    if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) //image ok
      this.setIconImage(icon.getImage());
    this.setTitle(bavaria.nameAt(index));
    this.setSize(300, 500);
    this.show();

    this.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent event) {
        editPanel.closeNodeWindow(NodeWindow.this);
        dispose();
      }
    });
  }

	public void actionPerformed( ActionEvent e ) {
		Object source = e.getSource();
		if( source == deleteAttribute ){
      if(attrListItem != -1){
        Rel removed = (Rel)attributesListModel.remove(attrListItem);
        int[] tuple = (int[])attributeTuples.remove(attrListItem);
        bavaria.deleteTuple(removed, tuple);
        attrListItem = -1;
        editPanel.repaint();
      }
    }
	}

	public void keyPressed(KeyEvent e) {
		Object source = e.getSource();
		if( source == nodeNameField ){
      char c = e.getKeyChar();
      if(c == KeyEvent.VK_ENTER){
        if(bavaria.setName(nodeNameField.getText(), index) == 0){
          nodeNameReserved.setForeground(Color.black);
          nodeNameReserved.setText("Name already in use");
        }
        else {
          editPanel.repaint();
          editPanel.updateNodeName(index);
          setTitle(bavaria.nameAt(index));
          nodeNameReserved.setText(" ");
        }
      }
    }
	}
 	public void keyReleased(KeyEvent e){
	}
 	public void keyTyped(KeyEvent e){
		Object source = e.getSource();
	}

	public void stateChanged(ChangeEvent e){
		Object source = e.getSource();
		if( source == tabbedPane ){
      for(int i=0; i<binAndArityLists.size(); ++i){
        JList temp = (JList)binAndArityLists.elementAt(i);
        temp.clearSelection();
        tabbedPane.requestFocus();
      }
      relListItem = -1;
      //System.out.println(tabbedPane.getSelectedIndex());
    }
	}

	public void mouseClicked(MouseEvent e){
	}
 	public void mouseEntered(MouseEvent e){
	}
 	public void mouseExited(MouseEvent e){
	}
 	public void mousePressed(MouseEvent e){
		Object source = e.getSource();
		if( source == attributesList ){
      attrListItem = attributesList.locationToIndex(e.getPoint());
      if(attrListItem == -1){
        attributesList.clearSelection();
        attributesLabel.requestFocus();
      }
    }
	}
 	public void mouseReleased(MouseEvent e){
	}
  //Returns index of the node
  public int getIndex(){
    return index;
  }


  //Changes index of the node
  public void changeIndex(){
    index = index-1;
  }


  //Disposes this window
  public void disposeNodeWindow(){
    editPanel.closeNodeWindow(this);
    this.dispose();
  }


  //Creates a tab for the relations with arity 2 or bigger
  public JPanel createTab(Rel re, final Vector tuples){
    final Rel r = re;
    final DefaultListModel binAndArityListModel = new DefaultListModel();
    final JList binAndArityList = new JList();
    JScrollPane binAndArityScrollList = new JScrollPane();

    //creates a list items
    for(int j=0; j<tuples.size(); ++j){
      int[] temp = (int[])tuples.elementAt(j);
      String listItem = "(";
      for(int k=0; k<temp.length; ++k){
        if(k < temp.length-1)
          listItem = listItem + bavaria.nameAt(temp[k]) + ", ";
        else
          listItem = listItem + bavaria.nameAt(temp[k]);
      }
      listItem = listItem + ")";
      binAndArityListModel.addElement(listItem);
    }

    binAndArityList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    binAndArityList.setModel(binAndArityListModel);
    binAndArityScrollList.getViewport().add(binAndArityList);

    binAndArityListModels.addElement(binAndArityListModel);
    binAndArityLists.addElement(binAndArityList);

    JButton deleteTuple = new JButton("Delete Tuple");

    deleteTuple.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        if(relListItem != -1){
          int i = tabbedPane.getSelectedIndex();
          DefaultListModel temp = (DefaultListModel)binAndArityListModels.elementAt(i);
          temp.remove(relListItem);

          Vector v = (Vector)binAndArityTuples.elementAt(i);
          int[] tuple = (int[])v.remove(relListItem);
          editPanel.tupleDeletedFromNodeWindow(r, tuple);
          bavaria.deleteTuple(r, tuple);
          relListItem = -1;
          editPanel.repaint();
        }
      }
    });

    binAndArityList.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent e){
        relListItem = binAndArityList.locationToIndex(e.getPoint());
        if(relListItem == -1){
          binAndArityList.clearSelection();
          tabbedPane.requestFocus();
        }
      }
    });

    JPanel relationTabPanel = new JPanel(new BorderLayout());
    relationTabPanel.add(binAndArityScrollList, BorderLayout.CENTER);
    relationTabPanel.add(deleteTuple, BorderLayout.SOUTH);

    return relationTabPanel;
  }


  //Removes the tuple from the list
  public void tupleDeletedFromNodeWindow(Rel r, int[] tuple){
    int i = binAndArityRels.indexOf(r);
    if(i != -1){
       Vector tuples = (Vector)binAndArityTuples.elementAt(i);
       DefaultListModel temp = (DefaultListModel)binAndArityListModels.elementAt(i);
       int index = tuples.indexOf(tuple);
       if(index != -1){
         temp.remove(index);
         tuples.remove(tuple);
       }
    }
  }


  //Updates the node's name in the lists (where arity >= 2)
  public void updateNodeName(int index){
    for(int i=0; i<binAndArityTuples.size(); ++i){
      Vector tuples = (Vector)binAndArityTuples.elementAt(i);
      DefaultListModel temp = (DefaultListModel)binAndArityListModels.elementAt(i);
      for(int j=0; j<tuples.size(); ++j){
        int[] tuple = (int[])tuples.elementAt(j);
        for(int k=0; k<tuple.length; ++k){
          if(tuple[k] == index){
            //updates the list item
            String listItem = "(";
            for(int l=0; l<tuple.length; ++l){
              if(l < tuple.length-1)
                listItem = listItem + bavaria.nameAt(tuple[l]) + ", ";
              else
                listItem = listItem + bavaria.nameAt(tuple[l]);
            }
            listItem = listItem + ")";
            try{
              temp.set(j, listItem);
            }catch (Exception e){
            }
            break;
          }
        }
      }
    }
  }


  //Removes the tuples which include the deleted node
  public void nodeWindowNodeDeleted(int deletedNode){
    for(int i=0; i<binAndArityTuples.size(); ++i){
      Vector tuples = (Vector)binAndArityTuples.elementAt(i);
      DefaultListModel temp = (DefaultListModel)binAndArityListModels.elementAt(i);
      for(int j=0; j<tuples.size(); ++j){
        int[] tuple = (int[])tuples.elementAt(j);
        for(int k=0; k<tuple.length; ++k){
          if(tuple[k] == deletedNode){
            tuples.removeElementAt(j);
            temp.removeElementAt(j);
            --j;
            break;
          }
        }
      }
    }
  }


  //Adds tuple to the open node window if it belongs to the tuple
  public void addTupleToNodeWindow(Rel r, int[] tuple){
    if(r.arity == 1){
      attributesListModel.addElement(r);
      attributeTuples.addElement(tuple);
    }
    else{
      for(int i=0; i<tuple.length; ++i){
        if(tuple[i] == index){
          int j = binAndArityRels.indexOf(r);
          if(j != -1){
            DefaultListModel temp = (DefaultListModel)binAndArityListModels.elementAt(j);
            String listItem = "(";
            for(int k=0; k<tuple.length; ++k){
              if(k < tuple.length-1)
                listItem = listItem + bavaria.nameAt(tuple[k]) + ", ";
              else
                listItem = listItem + bavaria.nameAt(tuple[k]);
            }
            listItem = listItem + ")";
            temp.addElement(listItem);
            Vector tuples = (Vector)binAndArityTuples.elementAt(j);
            tuples.addElement(tuple);
          }
          break;
        }
      }
    }
  }


  //Adds a new relation (tab) to the node window
  public void addNewRelToNodeWindow(Rel r){
    if(r.arity != 1){
      binAndArityRels.addElement(r);
      binAndArityTuples.addElement(new Vector());
      JPanel tab = createTab(r, new Vector());
      tabbedPane.addTab(r.name.name, tab);
    }
  }


  //Removes the relation from the node window
  public void deleteRelFromNodeWindow(Rel r){
    if(r.arity == 1){
      int i = attributesListModel.indexOf(r);
      if(i != -1){ //node belongs to this relation
        attributesListModel.remove(i);
        attributeTuples.remove(i);
        attrListItem = -1;
      }
    }
    else {
      int i = binAndArityRels.indexOf(r);
      if(i != -1){
        binAndArityRels.remove(i);
        binAndArityTuples.remove(i);
        binAndArityListModels.remove(i);
        binAndArityLists.remove(i);
        tabbedPane.remove(i);
        relListItem = -1;
      }
    }
  }
  
}




