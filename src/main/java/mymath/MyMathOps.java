package mymath;
import myio.*;
import java.text.DecimalFormat;

public class MyMathOps extends java.lang.Object{

    public static double[] arrayAdd(double[] a1, double[] a2){
	if (a1.length != a2.length)
	    throw new ArithmeticException("cannot add arrays of unequal length");
	else {
	    double[] result = new double[a1.length];
	    for (int i=0;i<result.length;i++)
		result[i]=a1[i]+a2[i];
	    return result;
	}
    }

//    public static void arrayNormalize(double[] a){
//	double sum = 0;
//	for (int i=0;i<a.length;i++)
//	    sum = sum + a[i];
//	for (int i=0;i<a.length;i++)
//	    a[i] = a[i]/sum;
//    }

    /* Normalize to sum to one */
    public static double arrayNormalize(double[] a){
    	double sum = 0;
    	for (int i=0;i<a.length;i++)
    		sum = sum + a[i];
    	for (int i=0;i<a.length;i++){
    		if (sum > 0)
    			a[i] = a[i]/sum;
    		else 
    			a[i]=1/(double)a.length;
    	}
    	return sum;
        }
 
    public static double[] arrayNormalize(int[] a){
    	double[] result = new double[a.length];
    	int sum = 0;
    	
    	for (int i=0;i<a.length;i++)
    		sum = sum + a[i];
    	for (int i=0;i<a.length;i++){
    		if (sum > 0)
    			result[i] = ((double)a[i])/sum;
    		else 
    			result[i]=1/(double)a.length;
    	}
    	return result;
        }
    
    public static void arrayNormalizeEuclid(double[] a){
	double sum = 0;
	for (int i=0;i<a.length;i++)
	    sum = sum + a[i]*a[i];
	sum = Math.sqrt(sum);
	for (int i=0;i<a.length;i++)
	    a[i] = a[i]/sum;
    }

    public static double[] arrayScalMult(double[] a, double l){
	double[] result = new double[a.length];
	for (int i=0;i<result.length;i++)
	    result[i]=l*a[i];
	return result;
    }

    public static double[] arrayConcat(double[] a, double[] b){
	double[] result = new double[a.length + b.length];
	for (int i=0;i<a.length;i++)
	    result[i]= a[i];
	for (int i=0;i<b.length;i++)
	    result[a.length + i]= b[i];
	return result;
    }


    public static double ce(double[] p, double[] q){
	// returns -1 to indicate infinite CE
	double result = 0;
	if (p.length != q.length){
	    System.out.println("Cannot compute Cross-Entropy for Vectors of different length!");
	    return 0;
	}
	for (int i=0;i<p.length;i++){
	    if (q[i]==0){
		if (p[i]>0)
		    return -1;
	    }
	    if (p[i]>0)
		result = result + p[i]*Math.log(p[i]/q[i]);
	}
	return result;
    }

    public static double innerProduct(double[] a, double[] b){
	double result = 0;
	if (a.length != b.length){
	    System.out.println("Cannot compute inner product for Vectors of different length!");
	    return 0;
	}
	for (int i=0;i<a.length;i++)
	    result = result + a[i]*b[i];
	return result;
    }

       public static int intPow(int k, int l)
        // returns k to the power of l
        {
            int result =1;
            for (int i =0 ; i<l; i++) result = result*k;
            return result;
        }

    public static int[] intarrayadd(int[] a, int[] b){
	if (a.length != b.length) 
	    throw new IllegalArgumentException("Attempting to add vectors of unequal length");
	int[] result = new int[a.length];
	for (int i = 0; i<a.length; i++) result[i] = a[i]+ b[i];
	return result;
    } 

    public static int[] indexto01array(int ind,int size){
	// computes the 0/1-array with index ind in enumeration
	// of all 0/1 arrays of length size (i.e. binary representation
	// of ind with leading zeros
	int[] result = new int[size];
	int rem;
	for (int i=0;i<size;i++){
	    rem = ind % 2;
	    ind = ind/2;
	    result[size-1-i]=rem;
	}
	return result;
    }

    /* array entries start with 0 */
    public static int arraytoindex(int[] arrayarg , int[] dimarg){
    	int result = 0;
//    	System.out.println("In: " + StringOps.arrayToString(arrayarg,"[","]") 
//    				+ StringOps.arrayToString(dimarg,"[","]"));
    	if (arrayarg.length == 1)
    		return arrayarg[0];
    	else{
    		int[] subarrayarg = new int[arrayarg.length -1];
    		int[] subdimarg = new int[dimarg.length -1];
    		for (int i=0;i<subarrayarg.length;i++){
    			subarrayarg[i]=arrayarg[i];
    			subdimarg[i]=dimarg[i];
    		}
    		result = compprod(subdimarg)*arrayarg[arrayarg.length-1]
    		                                      	+arraytoindex(subarrayarg,subdimarg);
    	}
//    	System.out.println("Out: " + result);
    	return result;
    }
    
    public static int compprod(int[] arg){
    	int result = 1;
    	for (int i=0;i<arg.length;i++)
    		result = result * arg[i];
    	return result;
    }
    
    public static double roundDouble(double roundthis, int digits){
	//System.out.println("Rounding " + roundthis );
	double result = roundthis;
	result = Math.floor(result*Math.pow(10,digits)+0.5);
	result = result/ Math.pow(10,digits);
	//System.out.println("  ... returning " + result );
	return result;
    }
    
    public static String formatDouble(double formatthis, int length){
    	String formatstring = "0.000E0";
    	DecimalFormat df = new DecimalFormat(formatstring);
    	String result=df.format(formatthis);
    	return result;
        }
        
    /** Computes the Euclidean distance between a and b (must be of same length) */
    public static double euclDist(double[] a, double[] b){
        double result = 0;
        for (int i =0 ; i<a.length ; i++)
            result = result + Math.pow(a[i]-b[i],2.0);
        result = Math.sqrt(result);
        return result;
    }
    
    /** modifies a and b such that
     * new a: normalized version of original a
     * new b: unit length vector, orthogonal to a, s.t. new a and new b 
     * span the same space as a and b
     * @param a
     * @param b
     */
    public static void orthogonalize(double[] a, double[] b){
    	arrayNormalizeEuclid(a);
    	double inprod = innerProduct(a,b);
    	double[] newb = arrayAdd(b,arrayScalMult(a,-inprod));
    	for (int i=0;i<b.length;i++)
    		b[i]=newb[i];
    	arrayNormalizeEuclid(b);
  
    }

}
