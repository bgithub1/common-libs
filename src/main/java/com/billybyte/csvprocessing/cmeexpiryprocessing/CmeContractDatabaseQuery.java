package com.billybyte.csvprocessing.cmeexpiryprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.neodatis.odb.Objects;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.neodatis.NeoDatisSingleClassDataBase;

public class CmeContractDatabaseQuery implements QueryInterface<List<Tuple<String, String>>, List<CmeContract>> {
	private final NeoDatisSingleClassDataBase<String, CmeContract> database;
	private final Object database_Lock = new Object();
	
	@SuppressWarnings("unused")
	private CmeContractDatabaseQuery(){
		this.database = null;
	};
	
	public CmeContractDatabaseQuery(
			NeoDatisSingleClassDataBase<String, CmeContract> database) {
		super();
		this.database = database;
	}

	public CmeContractDatabaseQuery(String databasePath){
		this(new NeoDatisSingleClassDataBase<String, CmeContract>(databasePath,CmeContract.class));
	}

	@Override
	public List<CmeContract> get(List<Tuple<String, String>> key, int timeoutValue, TimeUnit timeUnitType) {
		synchronized (database_Lock) {
			if(key==null)return null;
			if(key.size()<=0)return null;
			List<CmeContract> contracts  =	database.getLikeWithMultipleConditionsLogicallyAnded(key);
			return contracts;
		}
	}
	
	public void closeDb(){
		synchronized (database_Lock) {
			this.database.close();
		}
	}
	
	public static void printList(List<CmeContract>contracts){
		if(contracts==null)return;
		Utils.prtObMess(CmeContractDatabaseQuery.class, "beg set");

		for(CmeContract c: contracts){
			if(c!=null){
				Utils.prtObMess(CmeContractDatabaseQuery.class, c.toString());
			}
		}
		Utils.prtObMess(CmeContractDatabaseQuery.class, "End set");

	}
	public static void main(String[] args) {
		String databasePath = "../PortfolioData/CmeData/cmeContracts.database";
		CmeContractDatabaseQuery query= new CmeContractDatabaseQuery(databasePath);
		// create a tuple list of conditions
		List<Tuple<String, String>> conditions = new ArrayList<Tuple<String,String>>();
		Tuple<String,String> c1 = new Tuple<String, String>("Exp_Contract_Code","NGV11");
		conditions.add(c1);
		List<CmeContract> contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
//
//		
//		contracts = query.get("CLN%", 1000, TimeUnit.MILLISECONDS);
//		printList(contracts);
//		contracts = query.get("CL%", 1000, TimeUnit.MILLISECONDS);
//		printList(contracts);

		conditions.clear();
		c1 = new Tuple<String, String>("Exp_Contract_Code","CLZ%");
		conditions.add(c1);
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","FUT"));
		conditions.add(new Tuple<String, String>("Commodity_Code","CL"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
	
		
		
		conditions.clear();
		c1 = new Tuple<String, String>("Exp_Contract_Code","GC%");
		conditions.add(c1);
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
		
		conditions.clear();
		c1 = new Tuple<String, String>("Exp_Contract_Code","GCM%");
		conditions.add(c1);
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

		conditions.clear();
		c1 = new Tuple<String, String>("Exp_Contract_Code","GC(V|M)(11|12|13|14)");
		conditions.add(c1);
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
		
		conditions.clear();
		c1 = new Tuple<String, String>("Exp_Contract_Code","OGZ(11|12|13|14|15|16|17|18)");
		conditions.add(c1);
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","C%"));
		conditions.add( new Tuple<String, String>("PRODUCT_TYPE_CODE","FUT"));
		conditions.add( new Tuple<String, String>("Commodity_Name","Corn"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","C(H|K|N|U)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","OOF"));
		conditions.add(new Tuple<String, String>("Option_Type","AME"));
		conditions.add(new Tuple<String, String>("Commodity_Name","Corn"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
		
		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","06(F|H|K|N|Q|U|X)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","OOF"));
		conditions.add(new Tuple<String, String>("Option_Type","AME"));
		conditions.add(new Tuple<String, String>("Commodity_Name","Soybean Meal"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
	
		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","ES(Z|H|M|U)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","FUT"));
		conditions.add(new Tuple<String, String>("Commodity_Code","ES"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","EC(Z|H|M|U)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","FUT"));
		conditions.add(new Tuple<String, String>("Commodity_Code","EC"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","ED(Z|H|M|U)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","FUT"));
		conditions.add(new Tuple<String, String>("Commodity_Code","ED"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);

	
		conditions.clear();
		conditions.add(new Tuple<String, String>("Exp_Contract_Code","ED(Z|H|M|U)(11|12|13|14|15|16|17|18)"));
		conditions.add(new Tuple<String, String>("PRODUCT_TYPE_CODE","OOF"));
		conditions.add(new Tuple<String, String>("Option_Type","AME"));
		conditions.add(new Tuple<String, String>("Commodity_Name","Eurodollar"));
		contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		printList(contracts);
	}

}
