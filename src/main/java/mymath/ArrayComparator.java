package mymath;

import java.util.Comparator;

public class ArrayComparator implements Comparator<double[]>{

	int colToCompare;
	boolean descending;
	
	public ArrayComparator(int cc, boolean d){
		colToCompare = cc;
		descending = d;
	}
	
	public int compare(double[] a1, double[] a2){
		int result =0;
		if (a1[colToCompare] > a2[colToCompare]) 
			result = 1 ;
		if (a1[colToCompare] < a2[colToCompare]) 
			result = -1 ;
		if (descending)
			result = - result;
		return result;
		
	}
}
