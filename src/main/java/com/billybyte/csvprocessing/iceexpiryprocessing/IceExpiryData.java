package com.billybyte.csvprocessing.iceexpiryprocessing;

public class IceExpiryData {
	private final String FDD;
	private final String FND;
	private final String FTD;
	private final String LDD;
	private final String LTD;
	
	private final String shortName;

	@SuppressWarnings("unused")
	private IceExpiryData(){
		this.shortName = null;
		LTD = null;
		FTD = null;
		FND = null;
		FDD = null;
		LDD = null;
	}
	public IceExpiryData(String shortName, String ltd, String ftd, String fnd,
			String fdd, String ldd) {
		super();
		this.shortName = shortName;
		LTD = ltd;
		FTD = ftd;
		FND = fnd;
		FDD = fdd;
		LDD = ldd;
	}
	
	

	public String getFDD() {
		return FDD;
	}

	public String getFND() {
		return FND;
	}

	public String getFTD() {
		return FTD;
	}

	public String getLDD() {
		return LDD;
	}

	public String getLTD() {
		return LTD;
	}

	public String getShortName() {
		return shortName;
	}
	@Override
	public String toString() {
		return shortName+","+LTD+","+FND+","+FDD+","+FTD+","+LDD;
	}
	
}
