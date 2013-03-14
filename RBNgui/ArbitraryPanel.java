/*
 * ArbitraryPanel.java 
 * 
 * Copyright (C) 2005 Aalborg University
 *
 * contact:
 * jaeger@cs.aau.dk    www.cs.aau.dk/~jaeger/Primula.html
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

public class ArbitraryPanel extends JPanel implements MouseListener, KeyListener, FocusListener {

    private JLabel arbitraryLabel        = new JLabel("Higher arities");
    private JList arbitraryList          = new JList();
    private DefaultListModel listModel   = new DefaultListModel();
    private JLabel addArbitraryLabel     = new JLabel("Add:");
    private JTextField addArbitraryField = new JTextField(10);
    private JLabel addArityLabel         = new JLabel("Arity:");
    private JTextField addArityField     = new JTextField(10);
    private JPanel labelPanel            = new JPanel(new GridLayout(2,1));
    private JPanel fieldPanel            = new JPanel(new GridLayout(2,1));
    private JPanel addAndArity           = new JPanel(new BorderLayout());
    private JScrollPane scrollList       = new JScrollPane();
    private int arity   = 0;
    private String name = "";

    private int mode;
    private final int ADDNODE        = 1;
    private final int MOVENODE       = 5;
    private final int DELETENODE     = 2;
    private final int ADDTUPLE       = 3;
    private final int DELETERELATION = 4;

    private Bavaria bavaria;

    public ArbitraryPanel(Bavaria b){

	bavaria = b;

	arbitraryList.setBackground(Color.white);
	addArbitraryField.setBackground(Color.white);
	addArityField.setBackground(Color.white);

	labelPanel.add(addArbitraryLabel);
	labelPanel.add(addArityLabel);

	fieldPanel.add(addArbitraryField);
	fieldPanel.add(addArityField);

	addAndArity.add(labelPanel, BorderLayout.WEST);
	addAndArity.add(fieldPanel, BorderLayout.CENTER);

	arbitraryList.setModel(listModel);
	arbitraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	scrollList.getViewport().add(arbitraryList);

	this.setLayout(new BorderLayout());
	this.add(arbitraryLabel, BorderLayout.NORTH);
	this.add(scrollList , BorderLayout.CENTER);
	this.add(addAndArity, BorderLayout.SOUTH);


	arbitraryList.addMouseListener( this );


	addArbitraryField.addKeyListener( this );


	addArbitraryField.addFocusListener( this );

	addArityField.addKeyListener( this );

	addArityField.addFocusListener( this );
    }

    public void focusGained(FocusEvent e){
	Object source = e.getSource();
    }

    public void focusLost(FocusEvent e) {
	Object source = e.getSource();
	if( source == addArbitraryField ){
	    name = addArbitraryField.getText();
	    if (arity > 2 && (name.trim()).length() > 0){
		Rel r = new Rel(name+"_"+arity, arity);
		bavaria.addRelation(r);
		listModel.addElement(r);
		arbitraryList.ensureIndexIsVisible(listModel.size()-1);
		addArbitraryField.setText("");
		addArityField.setText("");
		name = "";
		arity = 0;
	    }
	}
	else if( source == addArityField){
	    try {
		arity = (new Integer(addArityField.getText())).intValue();
	    } catch (Exception ex){
		arity = 0;
	    }
	    if (arity > 2 && (name.trim()).length() > 0){
		Rel r = new Rel(name+"_"+arity, arity);
		bavaria.addRelation(r);
		listModel.addElement(r);
		arbitraryList.ensureIndexIsVisible(listModel.size()-1);
		addArbitraryField.setText("");
		addArityField.setText("");
		name = "";
		arity = 0;
	    }
	}

    }

    public void keyPressed(KeyEvent e){
	Object source = e.getSource();
	if( source == addArbitraryField ){
	    char c = e.getKeyChar();
	    if(c == KeyEvent.VK_ENTER){
		name = addArbitraryField.getText();
		if (arity > 2 && (name.trim()).length() > 0){
		    Rel r = new Rel(name, arity);
		    bavaria.addRelation(r);
		    listModel.addElement(r);
		    arbitraryList.ensureIndexIsVisible(listModel.size()-1);
		    addArbitraryField.setText("");  
		    addArityField.setText("");
		    name = "";
		    arity = 0;
		}
	    }
	}
	else if( source == addArityField ){
	    char c = e.getKeyChar();
	    if(c == KeyEvent.VK_ENTER){
		try {
		    arity = (new Integer(addArityField.getText())).intValue();
		} catch (Exception ex){
		    arity = 0;
		}
		if (arity > 2 && (name.trim()).length() > 0){
		    Rel r = new Rel(name, arity);
		    bavaria.addRelation(r);
		    listModel.addElement(r);
		    arbitraryList.ensureIndexIsVisible(listModel.size()-1);
		    addArbitraryField.setText("");
		    addArityField.setText("");
		    name = "";
		    arity = 0;
		    addArbitraryField.requestFocus();
		}
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
	if( source == arbitraryList ){
	    int index;
	    index = arbitraryList.locationToIndex(e.getPoint());
	    if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE){
		arbitraryList.clearSelection();
		addArbitraryLabel.requestFocus();
	    }
	    if (mode == ADDTUPLE){
		if (index >= 0){
		    bavaria.clearSelections(3);
		    Rel r = (Rel)listModel.elementAt(index);
		    bavaria.addTuple(r);
		}
		else
		    bavaria.addTuple(new Rel());  //so that arity equals 0
	    }
	    if (mode == DELETERELATION){
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
			arbitraryList.clearSelection();
			addArityLabel.requestFocus();
		    }
		}
	    }
	}
    }
    public void mouseReleased(MouseEvent e){
	Object source = e.getSource();
	
	if( source == arbitraryList ){
	    int index = arbitraryList.locationToIndex(e.getPoint());

	    if (mode == ADDNODE || mode == MOVENODE || mode == DELETENODE || mode == DELETERELATION){
		arbitraryList.clearSelection();
		addArbitraryLabel.requestFocus();
	    }
	    if (mode == ADDTUPLE && index >= 0){
		arbitraryList.setSelectedIndex(index);
		arbitraryList.ensureIndexIsVisible(index);
	    }
	}
    } 



    public void getArbitraryNames(Vector<Rel> arbitraryrels){
    	for(int i=0; i<arbitraryrels.size(); ++i){
    		listModel.addElement(arbitraryrels.elementAt(i));
    	}
    }


    public void setMode(int mode){
    	this.mode = mode;
    }

    public void clearSelections(){
    	arbitraryList.clearSelection();
    }

    public void empty(){
	listModel.clear();
    }


}
