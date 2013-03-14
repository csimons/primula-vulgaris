/*
* BinaryPanel.java 
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

public class BinaryPanel extends JPanel implements MouseListener, KeyListener{

  private JLabel binaryLabel         = new JLabel("Binary relations");
  private JList binaryList           = new JList();
  private DefaultListModel listModel = new DefaultListModel();
  private JLabel addBinLabel         = new JLabel("Add:");
  private JTextField addBinField     = new JTextField(10);
  private JPanel addPanel            = new JPanel(new BorderLayout());
  private JScrollPane scrollList     = new JScrollPane();

  private int mode;
  private final int ADDNODE        = 1;
  private final int MOVENODE       = 5;
  private final int DELETENODE     = 2;
  private final int ADDTUPLE       = 3;
  private final int DELETERELATION = 4;

  private Bavaria bavaria;

  public BinaryPanel(Bavaria b){

    bavaria = b;

    addBinField.setBackground(Color.white);
    binaryList.setBackground(Color.white);

    addPanel.add(addBinLabel, BorderLayout.WEST);
    addPanel.add(addBinField, BorderLayout.CENTER);

    binaryList.setModel(listModel);
    binaryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    binaryList.setCellRenderer(new MyListCellRenderer());

    scrollList.getViewport().add(binaryList);

    this.setLayout(new BorderLayout());
    this.add(binaryLabel, BorderLayout.NORTH);
    this.add(scrollList , BorderLayout.CENTER);
    this.add(addPanel, BorderLayout.SOUTH);


    binaryList.addMouseListener( this );
    addBinField.addKeyListener( this );
  }

	public void keyPressed(KeyEvent e) {
		Object source = e.getSource();
		if( source == addBinField ){
      char c = e.getKeyChar();
      if(c == KeyEvent.VK_ENTER){
        Rel r =  new Rel(addBinField.getText(), 2);
        bavaria.addRelation(r);  //should also check the input...
        listModel.addElement(r);
        addBinField.setText("");
        binaryList.ensureIndexIsVisible(listModel.size()-1);
      }
    }
	}
 	public void keyReleased(KeyEvent e){
		Object source = e.getSource();
	}
 	public void keyTyped(KeyEvent e){
		Object source = e.getSource();
	}

	public void mouseClicked(MouseEvent e){
		Object source = e.getSource();
	}
 	public void mouseEntered(MouseEvent e){
		Object source = e.getSource();
	}
 	public void mouseExited(MouseEvent e){
		Object source = e.getSource();
	}
 	public void mousePressed(MouseEvent e){
		Object source = e.getSource();
		if( source == binaryList ){
      int index = binaryList.locationToIndex(e.getPoint());
			if(e.getButton() == 3 ){
				Rel r = (Rel)listModel.get(index);
				Color old = r.getColor();
				Color ny = JColorChooser.showDialog( BinaryPanel.this, "Choose a color", old );
				if(ny != null){
					r.setColor( ny );
				}
				repaint();
				bavaria.repaint();
			}
      else if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE){
        binaryList.clearSelection();
        addBinLabel.requestFocus();
      }
      else if (mode == ADDTUPLE){
        if (index >= 0){
          bavaria.clearSelections(2);
          Rel r = (Rel)listModel.elementAt(index);
          bavaria.addTuple(r);
        }
        else
          bavaria.addTuple(new Rel());  //so that arity equals 0
      }
      else if (mode == DELETERELATION){
        if(index >= 0){
          String relName = ((Rel)listModel.elementAt(index)).name.name;
          int result = JOptionPane.showConfirmDialog(bavaria,
          "Do you really want to delete the whole relation " + relName + "?",
          "Delete Relation", JOptionPane.YES_NO_OPTION);
          if(result == JOptionPane.YES_OPTION){
            Rel r = (Rel)listModel.remove(index);
            bavaria.deleteRelation(r);
          }
          else if(result == JOptionPane.NO_OPTION){
            binaryList.clearSelection();
            addBinLabel.requestFocus();
          }
        }
      }
    }
	}
 	public void mouseReleased(MouseEvent e){
		Object source = e.getSource();
		if( source == binaryList ){
      int index = binaryList.locationToIndex(e.getPoint());
      if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE || mode == DELETERELATION){
        binaryList.clearSelection();
        addBinLabel.requestFocus();
      }
      if (mode == ADDTUPLE && index >= 0){
        binaryList.setSelectedIndex(index);
        binaryList.ensureIndexIsVisible(index);
      }
		}
	}

  public void getBinaryrelsNames(Vector<Rel> binaryrels){
    for(int i=0; i<binaryrels.size(); ++i){
      listModel.addElement(binaryrels.elementAt(i));
    }
  }


  public void setMode(int mode){
    this.mode = mode;
  }


  public void clearSelections(){
    binaryList.clearSelection();
  }

  public void empty(){
    listModel.clear();
  }

}
