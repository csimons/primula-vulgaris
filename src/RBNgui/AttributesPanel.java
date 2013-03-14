/*
* AttributesPanel.java 
* 
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
import java.util.Vector;

import RBNpackage.*;

public class AttributesPanel extends JPanel implements MouseListener, KeyListener{

  private JLabel attributesLabel     = new JLabel("Attributes");
  private JList attributesList       = new JList();
  private DefaultListModel listModel = new DefaultListModel();
  private JLabel addAttrLabel        = new JLabel("Add:");
  private JTextField addAttrField    = new JTextField(10);
  private JPanel addPanel            = new JPanel(new BorderLayout());
  private JScrollPane scrollList     = new JScrollPane();

  private int mode;
  private final int ADDNODE        = 1;
  private final int MOVENODE       = 5;
  private final int DELETENODE     = 2;
  private final int ADDTUPLE       = 3;
  private final int DELETERELATION = 4;

  private Bavaria bavaria;

  public AttributesPanel(Bavaria b){

    bavaria = b;

    addAttrField.setBackground(Color.white);
    attributesList.setBackground(Color.white);

    addPanel.add(addAttrLabel, BorderLayout.WEST);
    addPanel.add(addAttrField, BorderLayout.CENTER);

    attributesList.setModel(listModel);
    attributesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    attributesList.setCellRenderer(new MyListCellRenderer());

    scrollList.getViewport().add(attributesList);

    this.setLayout(new BorderLayout());
    this.add(attributesLabel, BorderLayout.NORTH);
    this.add(scrollList, BorderLayout.CENTER);
    this.add(addPanel, BorderLayout.SOUTH);


    attributesList.addMouseListener( this );
    addAttrField.addKeyListener( this );
      
  }

 	public void keyPressed(KeyEvent e){
		Object source = e.getSource();
		if( source == addAttrField ){
      char c = e.getKeyChar();
      if (c == KeyEvent.VK_ENTER){
        Rel r = new Rel(addAttrField.getText(), 1);
        bavaria.addRelation(r);
        listModel.addElement(r);
        addAttrField.setText("");
        attributesList.ensureIndexIsVisible(listModel.size()-1);
      }
    }
	}
 	public void keyReleased(KeyEvent e){ 
	}
 	public void keyTyped(KeyEvent e){
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
      int index = attributesList.locationToIndex(e.getPoint());
			if(e.getButton() == 3 ){
				Rel r = (Rel)listModel.get(index);
				Color old = r.getColor();
				Color ny = JColorChooser.showDialog( AttributesPanel.this, "Choose a color", old );
				if(ny != null){
					r.setColor( ny );
				}
				repaint();
				bavaria.repaint();
			}
      else if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE){
        attributesList.clearSelection();
        addAttrLabel.requestFocus();
      }
      else if (mode == ADDTUPLE){
        if (index >= 0){
          bavaria.clearSelections(1);
          Rel r = (Rel)listModel.elementAt(index);
          bavaria.addTuple(r);
        }
        else
          bavaria.addTuple(new Rel());  //so that arity equals 0
      }
      else if (mode == DELETERELATION){
        if (index >= 0){
          String relName = ((Rel)listModel.elementAt(index)).name.name;
          int result = JOptionPane.showConfirmDialog(bavaria,
          "Do you really want to delete the whole relation " + relName + "?",
          "Delete Relation", JOptionPane.YES_NO_OPTION);
          if(result == JOptionPane.YES_OPTION){
            Rel r = (Rel)listModel.remove(index);
            bavaria.deleteRelation(r);
          }
          else if(result == JOptionPane.NO_OPTION){
            attributesList.clearSelection();
            addAttrLabel.requestFocus();
          }
        }
      }
    }
	}
 	public void mouseReleased(MouseEvent e){
		Object source = e.getSource();
		if( source == attributesList ){
      int index = attributesList.locationToIndex(e.getPoint());
      if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE || mode == DELETERELATION){
        attributesList.clearSelection();
        addAttrLabel.requestFocus();
      }
      if (mode == ADDTUPLE && index >= 0){
        attributesList.setSelectedIndex(index);
        attributesList.ensureIndexIsVisible(index);
      }
    }
	}


  public void getAttributesNames(Vector<Rel> attributes){
    for(int i=0; i<attributes.size(); ++i){
      listModel.addElement(attributes.elementAt(i));
    }
  }


  public void setMode(int mode){
    this.mode = mode;
  }


  public void clearSelections(){
    attributesList.clearSelection();
  }

  public void empty(){
    listModel.clear();
  }

}
