package mydata;

import java.util.Vector;

public class ArrayUtilities {

    public static int[][] intarrvecToArr(Vector<int[]> vec){
        int[][] result = new int[vec.size()][];
        for (int i=0;i<result.length;i++)
        	result[i]=vec.elementAt(i);
        return result;
        }

    public static int[] intvecToArr(Vector<Integer> vec){
        int[] result = new int[vec.size()];
        for (int i=0;i<result.length;i++)
        	result[i]=vec.elementAt(i);
        return result;
        }

    public static String[] stringvecToArr(Vector<String> vec){
        String[] result = new String[vec.size()];
        for (int i=0;i<result.length;i++)
        	result[i]=vec.elementAt(i);
        return result;
        }

}
