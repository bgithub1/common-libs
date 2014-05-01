package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import Jama.Matrix;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

import junit.framework.TestCase;

public class TestCholesky extends TestCase{
	
	Map<String, BigDecimal> correlMap;
	Map<String, ComplexQueryResult<BigDecimal>> correlCqrMap;
	TreeSet<String> underlyings ;
	
	@Override
	protected void setUp() throws Exception {
		String csvNameOrPath = "testCorrelationPairs.csv";
		Class<?> classInPkgOfReource =this.getClass();
		String colNameOfKey = "pairName";
		String colNameOfData = "corr";
		Class<String> classOfKey = String.class;
		
		Class<BigDecimal> classOfData = BigDecimal.class;
		correlMap = 
				CollectionsStaticMethods.mapFromCsv(
						classInPkgOfReource,csvNameOrPath,
						colNameOfKey,
						classOfKey, 
						colNameOfData,
						classOfData);
		
		underlyings = 
				new TreeSet<String>();
		for(String key:correlMap.keySet()){
			String[] split = key.split(MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR);
			underlyings.add(split[0]); // add first part of name
			underlyings.add(split[1]); // add first part of name
		}

		correlCqrMap = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(Entry<String, BigDecimal> entry:correlMap.entrySet()){
			correlCqrMap.put(
					entry.getKey(),
					new ComplexQueryResult<BigDecimal>(
							null,entry.getValue()));
		}
		super.setUp();
	}
	
	static Set<String> getTestUnderlyings(){
		String csvNameOrPath = "testCorrelationPairs.csv";
		Class<?> classInPkgOfReource =TestCholesky.class;
		String colNameOfKey = "pairName";
		String colNameOfData = "corr";
		Class<String> classOfKey = String.class;
		
		Class<BigDecimal> classOfData = BigDecimal.class;
		Map<String, BigDecimal> correlMap = 
				CollectionsStaticMethods.mapFromCsv(
						classInPkgOfReource,csvNameOrPath,
						colNameOfKey,
						classOfKey, 
						colNameOfData,
						classOfData);
		
		Set<String> ret = 
				new TreeSet<String>();
		for(String key:correlMap.keySet()){
			String[] split = key.split(MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR);
			ret.add(split[0]); // add first part of name
			ret.add(split[1]); // add first part of name
		}
		return ret;
	}

	@Override
	protected void tearDown() throws Exception {
		underlyings=null;
		correlMap=null;
		correlCqrMap = null;
		super.tearDown();
	}

	public void test1(){
		
		Utils.prt("mFromMap");
		Matrix mFromMap = MathStuff.buildCorrMatrix(underlyings, correlMap);
		prtArray(mFromMap.getArray());
		
		Matrix cholesky = 
				MathStuff.choleskyLowerTriangle(mFromMap);
		assertTrue(MathStuff.isPositiveSemiDefinite(cholesky));
		Utils.prt("cholesky");
		prtArray(cholesky.getArray());
		
		Matrix chol = mFromMap.chol().getL();
		assertTrue(MathStuff.isPositiveSemiDefinite(chol));
		Utils.prt("chol");
		prtArray(chol.getArray());

		
		assertTrue(same(chol,cholesky));
		
		Matrix luFromCqrMap = 
				new Matrix(
						MathStuff.buildCorrMatrixFromCqrBigDecMap(
								underlyings, correlCqrMap)).chol().getL();
		assertTrue(same(luFromCqrMap,chol));
		
		
	}
	
	private void prtArray(double[][] d){
		for(int i =0; i < d.length;i++){
			String line="";
			for(int j=0;j<d[i].length;j++){
				line += d[i][j] +",";
			}
			Utils.prt(line);
		}
	}

	private boolean same(Matrix a,Matrix b){
		if(a.getColumnDimension()!=b.getColumnDimension())return false;
		if(a.getRowDimension()!=b.getRowDimension())return false;
		for(int i =0; i<a.getRowDimension();i++){
			for(int j=0;j<a.getColumnDimension();j++){
				double va = Math.round(a.get(i, j)*10000);
				double vb = Math.round(a.get(i, j)*10000);
				if(va!=vb)return false;
			}
		}
		return true;
	}
}
