package com.billybyte.mathstuff;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.CorrelatedRandomVectorGenerator;
import org.apache.commons.math3.random.GaussianRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.NormalizedRandomGenerator;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;



import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Rounding;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.models.vanilla.NormalDistribution;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.queries.ComplexQueryResult;
import com.mongodb.util.OptionMap;

import Jama.LUDecomposition;
import Jama.Matrix;


public class MathStuff {
	
// math stuff *************************************
	public static Double[] BigDecimalListToDoubleArrray(List<BigDecimal> inList){
		Double[] ret = new Double[inList.size()];
		for(int i = 0;i<inList.size();i++){
			ret[i] = inList.get(i).doubleValue();
		}
		return ret;
		
	}
	
	public static Integer integerFromNumber(Number n){
		if(!RegexMethods.isNumber(n.toString()))return null;
		return new Integer(n.toString());
	}
	
	public static Double[] doubleToDouble(double[] inList){
		Double[] ret = new Double[inList.length];
		for(int i = 0;i<inList.length;i++){
			ret[i] = inList[i];
		}
		return ret;
	}
	
	public static final double cubsplineFromStds(double strikeToCalc,
			double atmStrike, double atmVol,
			double dteAsPercOfYear, 
			double[][] stdsAwayFromAtmVsVolPump){
		// calculate std
		double std = atmVol*atmStrike*Math.sqrt(dteAsPercOfYear);
		
		// calculate strike as std's away from atm
		double strikeAsPercAway = (strikeToCalc-atmStrike)/atmStrike;
		// create arrays
		double[][] percAwayVsVolPump = new double[stdsAwayFromAtmVsVolPump.length][2];
		for(int i = 0;i<stdsAwayFromAtmVsVolPump.length;i++){
			percAwayVsVolPump[i][0] = std*stdsAwayFromAtmVsVolPump[i][0]/atmStrike;
			percAwayVsVolPump[i][1] = stdsAwayFromAtmVsVolPump[i][1];
		}
		return cubspline(strikeAsPercAway,percAwayVsVolPump);
	}
	
	public static final double cubspline(double strikeAsPercAwayFromAtm,double[][] percAwayFromAtmVsVolPumpArray){
		Double[] xx = new Double[percAwayFromAtmVsVolPumpArray.length];
		Double[] yy = new Double[percAwayFromAtmVsVolPumpArray.length];
		for(int i = 0;i<xx.length;i++){
			xx[i] = percAwayFromAtmVsVolPumpArray[i][0];
			yy[i] = percAwayFromAtmVsVolPumpArray[i][1];
		}
		return cubspline(1,strikeAsPercAwayFromAtm,xx,yy);
		
	}

	public static final double cubspline(int Metode, BigDecimal xi, List<BigDecimal> xxIn, List<BigDecimal> yyIn){
		Double xiD = xi.doubleValue();
		Double[] xxInD = BigDecimalListToDoubleArrray(xxIn);
		Double[] yyInD = BigDecimalListToDoubleArrray(yyIn);
		return cubspline(Metode,xiD,xxInD,yyInD);
	}

	public static Number findClosestNumber(Collection<Number> values, Number value){
		List<Number> closestValues = Utils.findClosest(values, value);
		if(closestValues.size()==2){
			Number v0 = closestValues.get(0);
			Number v1 = closestValues.get(0);
			double dif0 = Math.abs(value.doubleValue()-v0.doubleValue());
			double dif1 = Math.abs(value.doubleValue()-v1.doubleValue());
			if(dif0<=dif1){
				return v0;
			}else{
				return v1;
			}
		}else if(closestValues.size()==1){
			return closestValues.get(0);
		}
		return null;
	}
		
	

	public static final double cubspline(int Metode, double xi, Double[] xxIn, Double[] yyIn){

		Double[] xx = new Double[xxIn.length+1];
		xx[0] = null;
		for(int i = 1;i<xx.length;i++){
			xx[i] = xxIn[i-1];
		}

		Double[] yy = new Double[yyIn.length+1];
		yy[0] = null;
		for(int i = 1;i<yy.length;i++){
			yy[i] = yyIn[i-1];
		}

		int i = 0;
		double yi = 0;
		double[] x = null;
		double[] y = null;
		double[] y2 = null;
		int j = 0;
		if (Metode == 1)
		{
		  //Numerical Recipes are 1 based
		  j = 0;
		}
		else
		{
		  //Others are 0 based
		  j = -1;
		}

		for (i = 1; i < xx.length; i++)
		{
		  if ( yy[i]!=null)
		  {
			j = j + 1;
		//VB TO JAVA CONVERTER NOTE: The following block reproduces what 'ReDim Preserve' does behind the scenes in VB:
		//ORIGINAL LINE: ReDim Preserve x(j)
			double[] tempVar = new double[j + 1];
			if (x != null)
				System.arraycopy(x, 0, tempVar, 0, Math.min(x.length, tempVar.length));
			x = tempVar;

		//VB TO JAVA CONVERTER NOTE: The following block reproduces what 'ReDim Preserve' does behind the scenes in VB:
		//ORIGINAL LINE: ReDim Preserve y(j)
			double[] tempVar2 = new double[j + 1];
			if (y != null)
				System.arraycopy(y, 0, tempVar2, 0, Math.min(y.length, tempVar2.length));

			y = tempVar2;
			x[j] = (double)(xx[i]);
			y[j] = (double)(yy[i]);
		  }
		}

		if (Metode == 1)
		{
		  //NR cubic spline
		  //Get y2
		  y2 = new double[x.length ];
		  spline(x, y, x.length-1, (Math.pow(10, 30)), (Math.pow(10, 30)), y2);
		  //Get y
		  yi =splint(x, y, y2, x.length-1, xi);
		}
		else if (Metode == 3)
		{
		  //Own cubic spline
		  yi = SplineX3(xi, x, y);
		}
		//Return
		return yi;
	}

	

	public static  final void spline(double[] x, double[] y, int N, double yp1, double ypn, double[] y2)
	{
	//Given arrays x(1:n) and y(1:n) containing a tabulated function, i.e., y i = f(xi), with
	//x1<x2< :::<xN , and given values yp1 and ypn for the first derivative of the inter-
	//polating function at points 1 and n, respectively, this routine returns an array y2(1:n) of
	//length n which contains the second derivatives of the interpolating function at the tabulated
	//points xi. If yp1 and/or ypn are equal to 1 * 10^30 or larger, the routine is signaled to set
	//the corresponding boundary condition for a natural spline, with zero second derivative on
	//that boundary.

	//Parameter: NMAX is the largest anticipated value of n.
		  int Nmax = 0;
		  Nmax = 500;
		  int i = 0;
		  int k = 0;
		  double p = 0;
		  double qn = 0;
		  double sig = 0;
		  double un = 0;
		  double[] u = new double[Nmax];
		  //The lower boundary condition is set either to be natural
		  if (yp1 > 9.9E+29)
		  {
			y2[1] = 0D;
			u[1] = 0D;
		  }
		  else
		  {
			//or else to have a specicied first derivative.
			y2[1] = -0.5;
			u[1] = (3D / (x[2] - x[1])) * ((y[2] - y[1]) / (x[2] - x[1]) - yp1);
		  }

		  //This is the decomposition loop of the tridiagonal
		  //algorithm. y2 and u are used for temporary
		  //storage of the decomposed factors.
		  for (i = 2; i < N; i++)
		  {
			sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
			p = sig * y2[i - 1] + 2D;
			y2[i] = (sig - 1D) / p;
			u[i] = (6D * ((y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1])) / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
		  }

		  //The upper boundary condition is set either to be natural
		  if (ypn > 9.9E+29)
		  {
			qn = 0D;
			un = 0D;
		  }
		  else
		  {
			//or else to have a specified first derivative.
			qn = 0.5;
			un = (3D / (x[N] - x[N - 1])) * (ypn - (y[N] - y[N - 1]) / (x[N] - x[N - 1]));
		  }
		  y2[N] = (un - qn * u[N - 1]) / (qn * y2[N - 1] + 1D);

		  //This is the backsubstitution loop of the tridiagonal algorithm.
		  for (k = N - 1; k >= 1; k--)
		  {
			y2[k] = y2[k] * y2[k + 1] + u[k];
		  }
	}

	private static final double splint(double[] xa, double[] ya, double[] y2a, int N, double x)
	{
	//Given the arrays xa(1:n) and ya(1:n) of length n, which tabulate a function (with the
	//xai 's in order), and given the array y2a(1:n), which is the output from spline above,
	//and given a value of x, this routine returns a cubic-spline interpolated value y.
		  int k = 0;
		  int khi = 0;
		  int klo = 0;
		  double A = 0;
		  double B = 0;
		  double h = 0;

		  //We will the right place in the table by means of bisection.
		  klo = 1;
		  khi = N;

		  while (khi - klo > 1)
		  {
			k = (khi + klo) / 2;
			if (xa[k] > x)
			{
			  khi = k;
			}
			else
			{
			  klo = k;
			}
		  };//Wend;

		  //klo and khi now bracket the input value of x.
		  h = xa[khi] - xa[klo];
		  if (h == 0)
		  {
			  Utils.prtObErrMess(Rounding.class, "bad xa input in splint");
		  }

		  //Cubic spline polynomial is now evaluated.
		  A = (xa[khi] - x) / h;
		  B = (x - xa[klo]) / h;
		  return A * ya[klo] + B * ya[khi] + (((Math.pow(A, 3)) - A) * y2a[klo] + ((Math.pow(B, 3)) - B) * y2a[khi]) * ((Math.pow(h, 2))) / 6D;
	}	

	

	

	

	private static final double SplineX3(double x, double[] xx, double[] yy)
	{
	//|-------------------------------------------------------------------------------
	//| Function returns y value for a corresponding x value, based on cubic spline.
	//| Will never oscillates or overshoot. No need to solve matrix.
	//| Also calculate constants for cubic in case needed (for integration).
	//| xx(0 to No_of_lines) is x values
	//|    * Must be unique (no two consequetive ones the same)
	//|    * Must be in ascending order
	//|    * No of lines = Number of points - 1
	//| yy(0 to No_of_lines) is y values
	//|
	//| Uses function dxx to prevent div by zero.
	//|
	//| Developer: C Kruger, Guildford, UK
	//| Date: December 2001
	//|-------------------------------------------------------------------------------

	int i = 0;
	int j = 0;
	int Nmax = 0;
	int Num = 0;

	//1st and 2nd derivative for left and right ends of line
	double[] gxx = new double[2];
	double[] ggxx = new double[2];

	//Constants for cubic equations

	double A = 0; //Also for linear extrapolation
	double B = 0; //Also for linear extrapolation
	double C = 0;
	double D = 0;

	//Number of lines = points - 1
	Nmax = xx.length-1;
	//(1a) Find LineNumber or segment. Linear extrapolate if outside range.
	Num = 0;
	if (x < xx[0] | x > xx[Nmax])
	{
	  //X outisde range. Linear interpolate
	  //Below min or max?
	  if (x < xx[0])
	  {
		  Num = 1;
	  }
	  else
	  {
		  Num = Nmax;
	  }

	  B = (yy[Num] - yy[Num - 1]) / dxx(xx[Num], xx[Num - 1]);
	  A = yy[Num] - B * xx[Num];
	  return A + B * x;

	  //(1b) Find LineNumber or segment. Linear extrapolate if outside range.
	}
	else
	{
	  //X in range. Get line.
	  for (i = 1; i <= Nmax; i++)
	  {
		if (x <= xx[i])
		{
		  Num = i;
		  break;
		}
	  }
	}

	//(2) Calc first derivative (slope) for intermediate points
	for (j = 0; j <= 1; j++) //Two points around line
	{
	  i = Num - 1 + j;
	  if (i == 0 || i == Nmax)
	  {
		//Set very large slope at ends
		gxx[j] = (Math.pow(10, 30));
	  }
	  else if ((yy[i + 1] - yy[i] == 0) | (yy[i] - yy[i - 1] == 0))
	  {
		//Only check for 0 dy. dx assumed NEVER equals 0 !
		gxx[j] = 0;
	  }
	  else if (((xx[i + 1] - xx[i]) / (yy[i + 1] - yy[i]) + (xx[i] - xx[i - 1]) / (yy[i] - yy[i - 1])) == 0)
	  {
		//Pos PLUS neg slope is 0. Prevent div by zero.
		gxx[j] = 0;
	  }
	  else if ((yy[i + 1] - yy[i]) * (yy[i] - yy[i - 1]) < 0)
	  {
		//Pos AND neg slope, assume slope = 0 to prevent overshoot
		gxx[j] = 0;
	  }
	  else
	  {
		//Calculate an average slope for point based on connecting lines
		gxx[j] = 2 / (dxx(xx[i + 1], xx[i]) / (yy[i + 1] - yy[i]) + dxx(xx[i], xx[i - 1]) / (yy[i] - yy[i - 1]));
	  }
	}

	//(3) Reset first derivative (slope) at first and last point
	if (Num == 1)
	{
	  //First point has 0 2nd derivative
	  gxx[0] = 3 / 2.0 * (yy[Num] - yy[Num - 1]) / dxx(xx[Num], xx[Num - 1]) - gxx[1] / 2;
	}
	if (Num == Nmax)
	{
	  //Last point has 0 2nd derivative
	  gxx[1] = 3 / 2.0 * (yy[Num] - yy[Num - 1]) / dxx(xx[Num], xx[Num - 1]) - gxx[0] / 2;
	}

	//(4) Calc second derivative at points
	ggxx[0] = -2 * (gxx[1] + 2 * gxx[0]) / dxx(xx[Num], xx[Num - 1]) + 6 * (yy[Num] - yy[Num - 1]) / (Math.pow(dxx(xx[Num], xx[Num - 1]), 2));
	ggxx[1] = 2 * (2 * gxx[1] + gxx[0]) / dxx(xx[Num], xx[Num - 1]) - 6 * (yy[Num] - yy[Num - 1]) / (Math.pow(dxx(xx[Num], xx[Num - 1]), 2));

	//(5) Calc constants for cubic
	D = 1 / 6.0 * (ggxx[1] - ggxx[0]) / dxx(xx[Num], xx[Num - 1]);
	C = 1 / 2.0 * (xx[Num] * ggxx[0] - xx[Num - 1] * ggxx[1]) / dxx(xx[Num], xx[Num - 1]);
	B = (yy[Num] - yy[Num - 1] - C * ((Math.pow(xx[Num], 2)) - (Math.pow(xx[Num - 1], 2))) - D * ((Math.pow(xx[Num], 3)) - (Math.pow(xx[Num - 1], 3)))) / dxx(xx[Num], xx[Num - 1]);
	A = yy[Num - 1] - B * xx[Num - 1] - C * (Math.pow(xx[Num - 1], 2)) - D * (Math.pow(xx[Num - 1], 3));

	//Return function
	return A + B * x + C * (Math.pow(x, 2)) + D * (Math.pow(x, 3));
	//'Alternative method based on Numerical Recipes.
	//'Shorter but does not calc cubic constants A, B, C, D
	//i = Num
	//A = (xx(i) - x) / (xx(i) - xx(i - 1))
	//B = 1 - A
	//Cy = 1 / 6 * (A ^ 3 - A) * (6 * (yy(i) - yy(i - 1)) - 2 * (gxx(i) + 2 * gxx(i - 1)) * (xx(i) - xx(i - 1)))
	//Dy = 1 / 6 * (B ^ 3 - B) * (2 * (2 * gxx(i) + gxx(i - 1)) * (xx(i) - xx(i - 1)) - 6 * (yy(i) - yy(i - 1)))

	//'Return function
	//SplineX3 = A * yy(i - 1) + B * yy(i) + Cy + Dy

	}

	private static  final double dxx(double x1, double x0)
	{
	double tempdxx = 0;
	//Calc Xi - Xi-1 to prevent div by zero
	tempdxx = x1 - x0;
	if (tempdxx == 0)
	{
		tempdxx = (Math.pow(10, 30));
	}

	return tempdxx;
	}

	static BigDecimal TWOFIFTYTWO = new BigDecimal("252");
	

	public static Matrix choleskyLowerTriangle(Matrix matrix){
		double element;
		Matrix a = matrix.copy();
		Matrix L_Lower = new Matrix(a.getRowDimension(),a.getColumnDimension());
		int N = a.getRowDimension();
		for(int i =0;i<N;i++){
			 for(int j = 0;j<N;j++){
			     element = a.get(i, j);
			     for(int k=0;k<=i-1;k++){
	                 element = element - L_Lower.get(i, k) * L_Lower.get(j, k);
			     }
				 if( i == j){
				 	if(element>=0) {
			             L_Lower.set(i, i,Math.sqrt(element));
				 	}else{
			             L_Lower.set(i, i, 0.0);
				 	}
				 }else if(i<j){
	             	 if (L_Lower.get(i,i)==0) {
	             	 	L_Lower.set(j,i,0);
	             	 }else{
					 	L_Lower.set(j, i, element / L_Lower.get(i, i));
	             	 }
				}

			 }
		}
		return L_Lower;
	}
	
	public static Matrix generateFilledMatrix(int rows, int cols, double value){
		return new Matrix(rows, cols, value);
	}
	
	public static double[] getVectorFromCqrBigDecMap(
			Set<String> snSet,Map<String,ComplexQueryResult<BigDecimal>> in){
		List<String> orderedList = 
				new ArrayList<String>(new TreeSet<String>(snSet));
		double[] ret = new double[orderedList.size()];
		for(int i = 0;i<orderedList.size();i++){
			ret[i] = in.get(orderedList.get(i)).getResult().doubleValue();
		}
		return ret;
	}
	public static double[][] buildCorrMatrixFromCqrBigDecMap(Set<String> snSet,Map<String,ComplexQueryResult<BigDecimal>> in){
		
		TreeSet<String> ts = new TreeSet<String>(snSet);
		double[][] ret = new double[ts.size()][ts.size()];
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry: in.entrySet()){
			BigDecimal value = entry.getValue().getResult();
			String[] parts = entry.getKey().split("__");
			if(parts.length!=2)return null;
			int index0 = ts.headSet(parts[0]).size();
			int index1 = ts.headSet(parts[1]).size();
			ret[index0][index1] = value.doubleValue();
			ret[index1][index0] = value.doubleValue();
		}
		return ret;
	}
	
	public static double[][] buildCorrMatrixFromCqrBigDecMap(List<String> snList,Map<String,ComplexQueryResult<BigDecimal>> in){
		
		int len = snList.size();
		double[][] ret = new double[len][len];
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry: in.entrySet()){
			BigDecimal value = entry.getValue().getResult();
			String[] parts = entry.getKey().split("__");
			if(parts.length!=2)return null;
			int index0 = snList.indexOf(parts[0]);
			int index1 = snList.indexOf(parts[1]);
			ret[index0][index1] = value.doubleValue();
			ret[index1][index0] = value.doubleValue();
		}
		return ret;
	}
	
	public static Matrix buildCorrMatrix(TreeSet<String> underlyings,Map<String,BigDecimal> correlMap){
		double[][] dMatrix = new double[underlyings.size()][underlyings.size()];
		int i = 0;
		for(String underlying0:underlyings){
			int j = 0;
			for(String underlying1:underlyings){
				if(underlying0.compareTo(underlying1)==0){
					dMatrix[i][j] = 1.0;
				}else{
					String correlName = underlying0+"__"+underlying1;
					if(underlying0.compareTo(underlying1)>0){
						correlName = underlying1+"__"+underlying0;
					}
					try {
						dMatrix[i][j] = correlMap.get(correlName).doubleValue();
					} catch (Exception e) {
						Utils.prtObErrMess(MathStuff.class, "Exception calculating correlation matrix for pair name: " + correlName);
						e.printStackTrace();
					}
				}
				j = j+1;
			}
			i = i+1;
		}
		return new Matrix(dMatrix);
	}

	public static double[][] buildCorrMatrixDouble(
			TreeSet<String> underlyings,Map<String,BigDecimal> correlMap){
		Matrix m = buildCorrMatrix(underlyings, correlMap);
		return m.getArray();
	}

	
	public static List<String[]> buildCorrMatrixCsv(
			TreeSet<String> allNames,
			Map<String,BigDecimal> correlMap){
		double[][]  matrix = MathStuff.buildCorrMatrixDouble(allNames, correlMap);
		int matrixLen = matrix.length;
		int dispMatrixLen = matrixLen+1;
		int dispMatrixWid = dispMatrixLen;
		String[][] displayMatrix = new String[dispMatrixLen][dispMatrixWid];
		displayMatrix[0][0] = " ";
		List<String> orderedList = new ArrayList<String>(allNames);
		for(int i = 1;i<dispMatrixWid;i++){
			displayMatrix[0][i] = orderedList.get(i-1);
		}
		for(int i = 0;i<orderedList.size();i++){
			int rowOfMatrix = i;
			int rowOfDisplay  = i+1;
			displayMatrix[rowOfDisplay][0] = orderedList.get(rowOfMatrix);
			for(int colOfMatrix = 0;colOfMatrix<matrixLen;colOfMatrix++){
				int displayCol = colOfMatrix+1;
				displayMatrix[rowOfDisplay][displayCol] = new Double(matrix[rowOfMatrix][colOfMatrix]).toString();
			}
		}
		
		List<String[]> matrixCsv = new ArrayList<String[]>();
		for(int i = 0;i<dispMatrixLen;i++){
			matrixCsv.add(displayMatrix[i]);
		}
		
		return matrixCsv;
	}
	
	public static Matrix fixUpperTriangle(Matrix m){
		Matrix copy = m.copy();
		for(int i = 0;i<copy.getRowDimension();i++){
			for(int j = 0;j<copy.getColumnDimension();j++){
				double val = copy.get(i,j);
				if(Double.isNaN(val) || Double.isInfinite(val)){
					Utils.prt("fixing element ("+i+","+j+")");
					copy.set(i, j,0.0);
				}
			}
		}
		return copy;
	}

	public static Double excelPercentile(double [] inData, double percentile) { 
		double[] data = new double[inData.length];
		for(int i = 0;i<data.length;i++){
			data[i] = inData[i];
		}
	    Arrays.sort(data);
	    double index = percentile*(data.length-1);
	    int lower = (int)Math.floor(index);
	    if(lower<0) { // should never happen, but be defensive
	       return data[0];
	    }
	    if(lower>=data.length-1) { // only in 100 percentile case, but be defensive
	       return data[data.length-1];
	    }
	    double fraction = index-lower;
	    // linear interpolation
	    Double result=data[lower] + fraction*(data[lower+1]-data[lower]);
	    return result;
	 }
	
	
	public static <T> List<List<T>> getCombinations(List<T> inList,int numOfItemsInCombination){
		  ICombinatoricsVector<T> initialVector = Factory.createVector(
			      inList);

		   // Create a simple combination generator to generate 3-combinations of the initial vector
		   Generator<T> gen = Factory.createSimpleCombinationGenerator(initialVector, numOfItemsInCombination);

		   // Print all possible combinations
//		   for (ICombinatoricsVector<T> combination : gen) {
//		      System.out.println(combination);
//		   }
//		  
		   List<ICombinatoricsVector<T>> vectorList = 
				   gen.generateAllObjects();
		   List<List<T>> ret = new ArrayList<List<T>>();
		   for(ICombinatoricsVector<T> v:vectorList){
			   ret.add(v.getVector());
		   }
		   return ret;
	}
	
	
	public static BigDecimal calcCorrelation(List<BigDecimal> s1Values, List<BigDecimal> s2Values, int roundingPrec) {
		if(s1Values.size()!=s2Values.size()) {
			// must be same size
			return null;
		}
		double[] sdArr1  = getReturns(s1Values);
		double[] sdArr2 = getReturns(s2Values);
		
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		
		double corr = pc.correlation(sdArr1, sdArr2);
		
		return new BigDecimal(corr).setScale(roundingPrec, RoundingMode.HALF_EVEN);
	}
	
	
	public static BigDecimal calcCorrelationFlat(List<BigDecimal> s1Values, List<BigDecimal> s2Values, int roundingPrec) {
		if(s1Values.size()!=s2Values.size()) {
			// must be same size
			return null;
		}
		double[] sdArr1  = convToDoubleArr(s1Values);
		double[] sdArr2 = convToDoubleArr(s2Values);
		
		
		PearsonsCorrelation pc = new PearsonsCorrelation();
		
		double corr = pc.correlation(sdArr1, sdArr2);
		
		return new BigDecimal(corr).setScale(roundingPrec, RoundingMode.HALF_EVEN);
	}
	
	
	private static List<BigDecimal> calcPercDiffList(List<BigDecimal> s, int roundingPrec) {
		
		List<BigDecimal> ret = new ArrayList<BigDecimal>();
		
		for(int i=0;i<s.size()-1;i++) {
			
			BigDecimal diff = s.get(i+1).subtract(s.get(i));
			BigDecimal percChange = diff.divide(s.get(i), roundingPrec, RoundingMode.HALF_EVEN);
			ret.add(percChange);
			
		}
		
		return ret;
	}
	
	private static double[] convToDoubleArr(List<BigDecimal> s) {
		double[] ret = new double[s.size()];
		for(int i=0;i<s.size();i++) {
			ret[i] = s.get(i).doubleValue();
		}
		return ret;
	}

	public static double stdDevPercReturns(List<BigDecimal> bdValues){
		double[] doubles = new double[bdValues.size()-1];
		for(int i = 1;i<bdValues.size();i++){
			doubles[i-1] = bdValues.get(i).doubleValue()/bdValues.get(i-1).doubleValue();
		}
		return stdDev(doubles);
		
	}

	/**
	 * Get Standard Deviation of Percent Returns from TreeMap<String, BigDecimal> dateToValuesMap,
	 *   where the keys are yyyyMmDd strings.  Make the values into
	 *   percent returns, and return Standard Deviation of Percent Returns
	 * @param dateToValuesMap TreeMap<String, BigDecimal>
	 * @return
	 */
	public static double stdDevPercReturns(TreeMap<String, BigDecimal> dateToValuesMap){
		List<BigDecimal> bdList = new ArrayList<BigDecimal>(dateToValuesMap.values());
		return stdDevPercReturns(bdList);
	}

	
	public static double[] getReturns(List<BigDecimal> valueList){
		double[] ret = new double[valueList.size()-1];
		for(int i = 1;i<valueList.size();i++){
			double vI = valueList.get(i).doubleValue();
			double vIminus1 = valueList.get(i-1).doubleValue();
			double val = vI/vIminus1 - 1;
			ret[i-1]=val;
		}
		return ret;
	}
	
	public static Matrix generateRandoms(int rows, int cols){
		double[][] da = new double[rows][cols];
		for(int i = 0;i<rows;i++){
			for(int j = 0;j<cols;j++)
			da[i][j] = NormalDistribution.inverseCumulativeDistribution(Math.random());
		}
		return new Matrix(da);
	}
	
	public static Matrix generateDuplicateRowMatrix(int numDupes,double[] row){
		return generateDuplicateRowMatrix(numDupes,new Matrix(row,row.length).transpose());
	}
	

	/**
	 * clone rows of m numDupes times
	 * 
	 * @param numDupes
	 * @param m
	 * @return
	 */
	public static Matrix generateDuplicateRowMatrix(int numDupes,Matrix m){
		int rows = m.getRowDimension();
		int cols = m.getColumnDimension();
		double[][] original = m.getArray();
		double[][] d = new double[numDupes][cols];
		for(int i = 0;i<numDupes;i++){
			int curRow = i*rows;
			for(int j = 0;j<rows;j++){
				d[curRow+j] = original[j];
			}
		}
		return new Matrix(d);
	}
	
	public static Matrix getCorrelatedValues(int trials,Matrix volVector,Matrix atmVector, Matrix corrMatrix){
		Matrix correlatedVolChgs = getCorrelatedPercentChanges(trials, volVector, corrMatrix);
		Matrix ones = new Matrix(correlatedVolChgs.getRowDimension(),correlatedVolChgs.getColumnDimension(),1.0);
		Matrix correlatedPriceChgFactors = correlatedVolChgs.plus(ones);
		Matrix atmsDuped = MathStuff.generateDuplicateRowMatrix(trials, atmVector.transpose());
		Matrix ret = atmsDuped.arrayTimes(correlatedPriceChgFactors);		
		return ret;
	}
	
	public static Matrix getCorrelatedValueChanges(int trials,Matrix volVector,Matrix atmVector, Matrix corrMatrix){
		Matrix correlatedVolChgs = getCorrelatedPercentChanges(trials, volVector, corrMatrix);
		Matrix atmsDuped = MathStuff.generateDuplicateRowMatrix(trials, atmVector.transpose());
		Matrix ret = atmsDuped.arrayTimes(correlatedVolChgs);	
		return ret;
	}

	public static Matrix getCorrelatedPercentChanges(int trials,Matrix volVector, Matrix corrMatrix){
		Matrix corrRandoms = MathStuff.generateCorrelatedRandoms(trials, null, corrMatrix.getArray());
		Matrix volsDuped = MathStuff.generateDuplicateRowMatrix(trials, volVector.transpose());
		Matrix ret = volsDuped.arrayTimes(corrRandoms);
		return ret;
	}


	
	public static Matrix generateCorrelatedRandoms(
			int trials,
			double[] means,
			 double[][] corrMatrix){
		Matrix cm = new Matrix(corrMatrix);
		Matrix upperTriangle = cm.chol().getL().transpose();
		upperTriangle = fixUpperTriangle(upperTriangle);
		Matrix randoms = MathStuff.generateRandoms(trials,cm.getColumnDimension());
		Matrix ret = randoms.times(upperTriangle);
	    return ret;
	}
	
	public static double getMatrixMax(Matrix m){
		double ret = Double.MIN_VALUE;
		for(int i =0;i<m.getRowDimension();i++){
			for(int j = 0;j<m.getColumnDimension();j++){
				double val = m.get(i, j);
				if(val>ret){
					ret = val;
				}
			}
		}
		return ret;
	}
	
	public static double getMatrixMin(Matrix m){
		double ret = Double.MAX_VALUE;
		for(int i =0;i<m.getRowDimension();i++){
			for(int j = 0;j<m.getColumnDimension();j++){
				double val = m.get(i, j);
				if(val<ret){
					ret = val;
				}
			}
		}
		return ret;
	}

	
	public static double stdDev(List<BigDecimal> bdValues){
		double[] doubles = new double[bdValues.size()];
		for(int i = 0;i<bdValues.size();i++){
			doubles[i] = bdValues.get(i).doubleValue();
		}
		return stdDev(doubles);
	}

	/**
	 * Get Standard Deviation from TreeMap<String, BigDecimal> dateToValuesMap,
	 *   where the keys are yyyyMmDd strings
	 * @param dateToValuesMap TreeMap<String, BigDecimal>
	 * @return
	 */
	public static double stdDev(TreeMap<String, BigDecimal> dateToValuesMap){
		List<BigDecimal> bdList = new ArrayList<BigDecimal>(dateToValuesMap.values());
		return stdDev(bdList);
	}

	
	public static double stdDev(double[] doubles){
		StandardDeviation std = new StandardDeviation();
		return std.evaluate(doubles);
	}
	
	/**
	 * 
	 * @param unknownShortNamesSet
	 * @param pearsons - Map<String, BigDecimal>  includes all correlations
	 *    including correlations between the portfolio and the hedges
	 * @param knownExposure
	 * @return
	 */
	public static Map<String, Double> getMinVarianceSolutionMap(
			String shortNameOfPortfolio,
			Set<String> unknownShortNamesSet,
			Map<String, BigDecimal> pearsons,
			double knownExposure,
			String corrPairSnSeparator){
		TreeSet<String> orderedSet = new TreeSet<String>(unknownShortNamesSet);
		orderedSet.add("1");  // add a name of the portfolio 
		
		Map<String, BigDecimal> pearsonsWith1 = 
				CollectionsStaticMethods.replaceShortNameInPairMap(
						shortNameOfPortfolio, "1", corrPairSnSeparator, pearsons);
		
		double[][] pearsonsDouble = MathStuff.buildCorrMatrixDouble(orderedSet, pearsonsWith1);
		double[] minVarianceArr = getMinVarianceSolution(pearsonsDouble,knownExposure);
		if(minVarianceArr==null){
			return null;
		}
		
		Map<String,Double> ret = new TreeMap<String, Double>();
		List<String> orderedList = new ArrayList<String>(orderedSet);
		for(int i = 1;i<orderedList.size();i++){
			String name = orderedList.get(i);
			ret.put(name, minVarianceArr[i-1]);
		}
		return ret;
	}
	
	public static double[] getMinVarianceSolution(
			double[][] pearsonsMatrix,
			double knownExposure){
		return getMinStd(new Matrix(pearsonsMatrix),knownExposure);
	}
	
	public static double getVarianceMapFromDoublePearsonMap(
			TreeMap<String, Double> vectorOfExposuresMap,
			Map<String,Double> pearsonsMap,
			String pairSeparator){
		double[] vectorOfExposures = primitiveVectorFromCollection(
				vectorOfExposuresMap.values());
		TreeSet<String> snSet = new TreeSet<String>(vectorOfExposuresMap.keySet());
		Map<String, Double> subMap = 
				CollectionsStaticMethods.getSubPairMapFromStringSet(
						snSet, pearsonsMap, pairSeparator);
		double[][] pearsons = 
				primitiveSquareMatrixFromDoublePairMap(snSet, subMap, pairSeparator);
		double ret = 
				getVariance(vectorOfExposures, pearsons);
		return ret;
	}
	

	public static double getVarianceMapFromBigDecimalPearsonMap(
			TreeMap<String, Double> vectorOfExposuresMap,
			Map<String,BigDecimal> pearsonsMap,
			String pairSeparator){
		double[] vectorOfExposures = primitiveVectorFromCollection(
				vectorOfExposuresMap.values());
		TreeSet<String> snSet = new TreeSet<String>(vectorOfExposuresMap.keySet());
		Map<String, BigDecimal> subMap = 
				CollectionsStaticMethods.getSubPairMapFromStringSet(
						snSet, pearsonsMap, pairSeparator);
		double[][] pearsons = 
				primitiveSquareMatrixFromBigDecimalPairMap(snSet, subMap, pairSeparator);
		double ret = 
				getVariance(vectorOfExposures, pearsons);
		return ret;
	}

	
	public static double[] primitiveVectorFromCollection(
			Collection<Double> values){
		double[] ret = new double[values.size()];
		int i = 0;
		for(Double d : values){
			ret[i] = d;
			i+=1;
		}
		return ret;
	}
	
	public static double[][] primitiveSquareMatrixFromDoublePairMap(
			TreeSet<String> keysForPairs,
			Map<String, Double> pairMap,
			String pairSeparator){
		double[][] ret = new double[keysForPairs.size()][keysForPairs.size()];
		List<String> orderedList = new ArrayList<String>(keysForPairs);
		for(int i = 0;i<orderedList.size();i++){
			for(int j = i; j<orderedList.size();j++){
				String key = orderedList.get(i)+pairSeparator+orderedList.get(j);
				Double value = pairMap.get(key);
				ret[i][j] = value;
				ret[j][i] = value;
			}
		}
		return ret;
	}

	public static double[][] primitiveSquareMatrixFromBigDecimalPairMap(
			TreeSet<String> keysForPairs,
			Map<String, BigDecimal> pairMap,
			String pairSeparator){
		double[][] ret = new double[keysForPairs.size()][keysForPairs.size()];
		List<String> orderedList = new ArrayList<String>(keysForPairs);
		for(int i = 0;i<orderedList.size();i++){
			for(int j = i; j<orderedList.size();j++){
				String key = orderedList.get(i)+pairSeparator+orderedList.get(j);
				Double value = pairMap.get(key).doubleValue();
				ret[i][j] = value;
				ret[j][i] = value;
			}
		}
		return ret;
	}
	
	public static double getVariance(
			double[] vectorOfExposures,
			double[][] pearsonsMatrix){
		double[][] voe2dimensional = new double[1][vectorOfExposures.length];
		for(int i = 0;i<vectorOfExposures.length;i++){
			voe2dimensional[0][i] = vectorOfExposures[i];
		}
		Matrix v = new Matrix(voe2dimensional);
		Matrix P = new Matrix(pearsonsMatrix);
		Matrix vTimesP = v.times(P);
		Matrix result = vTimesP.times(v.transpose());
		double scalar = result.get(0, 0);
		return scalar;
	}

	
	/**
	 * When P is Positive Semidefinite the derivation below holds	
	 * Let v =  vector of unexposures (green)	
	 * Let Q =  matrix of inner P (light blue)	
	 * Let q be  vector of first column of P w/o the first element (which is 1).  (Darker blue vector)	
	 * Let a be a scaler equal to the know exposure (purple)	
	 * Let Qinv be the inverse of Q	
	 * Let x be a vector representing the solution to the min problem	
	 * 
	 * Then:	
	 * 	  x = -a * Qinv * q
	 * 
	 * @return
	 */
	public static double[] getMinStd(
			Matrix pearsonsMatrix,
			double knownExposure){
		if(!isPositiveSemiDefinite(pearsonsMatrix)){
			return null;
		}
//		double[] eigenValues = pearsonsMatrix.eig().getRealEigenvalues();
//		for(double eigenValue : eigenValues){
//			if(eigenValue<0){
//				return null;
//			}
//		}
		int rowDim = pearsonsMatrix.getRowDimension();
		int colDim = pearsonsMatrix.getColumnDimension();
		if(rowDim!=colDim){
			return null;
		}
		int retDim = rowDim-1;
		Matrix Q = new Matrix(retDim,retDim);
		for(int i = 1;i<rowDim;i++){
			for(int j =1 ; j<colDim ; j++ ){
				double value = pearsonsMatrix.get(i, j);
				Q.set(i-1, j-1, value);
			}
		}
		
		Matrix Qinv = Q.inverse();
		
		Matrix q = new Matrix(retDim,1);
		for(int i = 1;i<rowDim;i++){
			double value = pearsonsMatrix.get(i,0);
			q.set(i-1, 0, value);
		}
		
		Matrix x = Qinv.times(q);
		x = x.times(-1*knownExposure);
		double[] ret = new double[retDim];
		for(int i = 0;i<retDim;i++){
			ret[i] = x.get(i, 0);
		}
		return ret;
	}

	
	public static int[][] mmultInt(int[][] M, int[][]N){
		if(M.length<1 && M[0].length!=N.length){
			return null;
		}
		int[][] ret = new int[M.length][N[0].length];
		for(int i=0;i<M.length;i++){
		  for(int j=0;j<N[0].length;j++){
		    for(int k=0;k<M[0].length;k++){
		          ret[i][j] += M[i][k]*N[k][j];
		    }
		  }
		}
		return ret;

	}

	public static double[][] mmultDouble(double[][] M, double[][]N){
		if(M.length<1 && M[0].length!=N.length){
			return null;
		}
		double[][] ret = new double[M.length][N[0].length];
		for(int i=0;i<M.length;i++){
		  for(int j=0;j<N[0].length;j++){
		    for(int k=0;k<M[0].length;k++){
		          ret[i][j] += M[i][k]*N[k][j];
		    }
		  }
		}
		return ret;

	}

	public static BigDecimal[][] mmultBigDecimal(BigDecimal[][] M, BigDecimal[][]N){
		if(M.length<1 && M[0].length!=N.length){
			return null;
		}
		BigDecimal[][] ret = new BigDecimal[M.length][N[0].length];
		for(int i=0;i<M.length;i++){
		  for(int j=0;j<N[0].length;j++){
		    for(int k=0;k<M[0].length;k++){
		          ret[i][j] = ret[i][j].add(M[i][k].multiply(N[k][j]));
		    }
		  }
		}
		return ret;

	}
	
	public static boolean isPositiveSemiDefinite(Matrix m){
		double[] eigenValues = m.eig().getRealEigenvalues();
		for(double eigenValue : eigenValues){
			if(eigenValue<0){
				return false;
			}
		}
		return true;
	}

	/**
	 * Try to fix non positive-semi-definite matrices
	 * @param pearson double[][]  pearson matrix (Hermitian)
	 * @return
	 */
	public static double[][] cleanPearsons(double[][] pearson){
		Matrix pearMatrix = new Matrix(pearson);
		if(isPositiveSemiDefinite(pearMatrix)){
			return pearson;
		}
		
		Matrix newPear=null;
		for(int c =0;c<10;c++){
			Matrix eigenDiag = pearMatrix.eig().getD();
			for(int i = 0;i<eigenDiag.getRowDimension();i++){
				for(int j = 0;j<eigenDiag.getRowDimension();j++){
					double ij = eigenDiag.get(i, j);
					if(ij<0){
						eigenDiag.set(i, j, 0.0);
					}
				}
			}
			Matrix eigenVect = pearMatrix.eig().getV();
			newPear = eigenVect.times(eigenDiag).times(eigenVect.transpose());
			for(int i = 0;i<newPear.getColumnDimension();i++){
				newPear.set(i, i, 1.0);
			}
			
			if(isPositiveSemiDefinite(newPear)){
				return newPear.getArray();
			}
			// try again
			pearMatrix = newPear.copy();
		}
		// if you get here, you could not fix it
		return null;
	}

	public static final int getPrecision(BigDecimal value){
		String s = value.toString();
		String [] parts = s.split("\\.");
		if(parts.length<2)return 0;
		String decimalPart = parts[1];
		return decimalPart.length();
		
	}
	
	public static final BigDecimal precisionConvert(
			BigDecimal numberToConvert,
			BigDecimal example,
			RoundingMode roundingMode){
		int newScale = getPrecision(example);
		return numberToConvert.setScale(newScale, roundingMode);
	}
	
//	public static double[] generateRandomNormal(int numToGenerate){
//		double[] ret = new double[numToGenerate];
//		for(int i =0;i<numToGenerate;i++){
//			ret[i] = NormalDistribution.inverseCumulativeDistribution(Math.random());
//		}
//		return ret;
//	}
}
