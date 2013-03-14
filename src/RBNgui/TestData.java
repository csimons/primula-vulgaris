package RBNgui;

import java.util.*;

class TestData extends Observable{
  private int[] tal;
	
 public TestData(){ }

	public void setData(int[] tal){
		this.tal = tal;
		setChanged();
	}
	
	public int[] getData(){
		return tal;
	}
}
