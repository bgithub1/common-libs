package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.mongo.MongoXml;

public class SettleSetQueryFromMongo implements QueryInterface<Set<String>,Map<String,BigDecimal>>{

	private final MongoXml<BigDecimal> volXmlColl;
	
	public SettleSetQueryFromMongo(MongoXml<BigDecimal> volXmlColl){
		this.volXmlColl = volXmlColl;
	}
	
	@Override
	public Map<String, BigDecimal> get(Set<String> key, int timeoutValue, TimeUnit timeUnitType) {		
		return volXmlColl.findFromSet(key);
	}

}
