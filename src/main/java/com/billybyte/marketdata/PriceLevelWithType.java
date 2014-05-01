package com.billybyte.marketdata;
import java.math.BigDecimal;





public class PriceLevelWithType implements PriceLevelData{
	public enum TickField{
		BIDSIZE(0),BID(1),ASK(2),ASKSIZE(3),LASTSIZE(5),LAST(4),HIGH(6),LOW(7),VOLUME(8),CLOSE(9);
		
		private int ibInt;
		TickField(int ibInt){
			this.ibInt = ibInt;
		}
		
		public int getIbInt(){
			return this.ibInt;
		}
	}

	final private BigDecimal price;
	final private int size;
	final private long time;
	final private TickField tickField;

	public PriceLevelWithType(PriceLevelData priceLevelData){
		super();
		this.price = priceLevelData.getPrice();
		this.size = priceLevelData.getSize();
		this.time = priceLevelData.getTime();
		this.tickField = null;
		
	}
	public PriceLevelWithType(BigDecimal price, int size, long time,
			TickField tickField) {
		super();
		this.price = price;
		this.size = size;
		this.time = time;
		this.tickField = tickField;
	}

	
	@Override
	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public long getTime() {
		return time;
	}
	
	public TickField getTickField(){
		return tickField;
	}

	@Override
	public String toString() {
		if(price==null){
			return "null"+","+size+","+time+","+tickField;
		}else{
			return price.toString()+","+size+","+time+","+tickField;
		}
	}
	
}
