package com.billybyte.marketdata.futures;

import java.util.Calendar;
import java.util.List;

import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.ShortNameInfo;
import com.billybyte.marketdata.ShortNameProcessor;
import com.billybyte.marketdata.ShortNameProcessorInterface;
import com.billybyte.marketdata.SecEnums.DayType;

public class ExpiryFromTimeOffsetList implements ExpiryRuleInterface {
//	final String product;
	final ShortNameProcessorInterface shortNameProcessor = new ShortNameProcessor(".",null);
	final List<DayOffset> offsetList;
	final String locale;
	transient ShortNameInfo shortNameInfo=null;
	
	@SuppressWarnings("unused")
	private ExpiryFromTimeOffsetList(){
		offsetList=null;
		locale=null;
	}
	
	
	public ExpiryFromTimeOffsetList(String locale,List<DayOffset> offsetList) {
		super();
		this.locale = locale;
		if(offsetList==null){
			throw Utils.IllArg(this, "null offsetList arg in constructor");
		}
		if(shortNameProcessor==null){
			throw Utils.IllArg(this, "null shortNameProcessor arg in constructor");
		}
		this.offsetList = offsetList;
	}


	@Override
	public Calendar getExpiry(String shortName) {
		ShortNameInfo shortNameInfo = shortNameProcessor.getShortNameInfo(shortName);
		if(shortNameInfo==null){
			throw Utils.IllArg(this, "bad shortName arg in getExpiry");
		}
		int contractYear = shortNameInfo.getContractYear();
		if(contractYear<1970 || contractYear>2100){
			throw Utils.IllArg(this, "bad contractYear in getExpiry "+contractYear );
		}
		int contractMonth = shortNameInfo.getContractMonth();
		if(contractMonth<1 || contractMonth>12){
			throw Utils.IllArg(this, "bad contractMonth in getExpiry "+contractMonth );
		}
		Integer contractDay = shortNameInfo.getContractDay();
		if(contractDay!=null){
			// if the contractDay is not null, then the expiry year month and 
			// day will be the contract year, month and day
			Calendar ret = Calendar.getInstance();
			ret.set(contractYear,contractMonth-1,contractDay);
			ret.set(Calendar.HOUR_OF_DAY,23);
			ret.set(Calendar.MINUTE,59);
			ret.set(Calendar.SECOND,59);
			return ret;
		}
		Calendar currDay = Calendar.getInstance();
		currDay.set(contractYear,contractMonth-1,1);
		for(DayOffset dayOrMonthOffset: offsetList){
			int offsetValue = dayOrMonthOffset.qty;
			DayType dt = dayOrMonthOffset.dayType;
			if(dt==DayType.CALENDAR_DAY){
				currDay = Dates.addToCalendar(currDay, offsetValue, Calendar.DAY_OF_MONTH, true);
			}else if(dt==DayType.BUSINESS_DAY){
				currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
			}else if(dt==DayType.NTH_MONDAY){
				return moveToNthDay(currDay, Calendar.MONDAY, offsetValue);
			}else if(dt==DayType.NTH_TUESDAY){
				return moveToNthDay(currDay, Calendar.TUESDAY, offsetValue);
			}else if(dt==DayType.NTH_WEDNESDAY){
				currDay = moveToNthDay(currDay, Calendar.WEDNESDAY, offsetValue);
			}else if(dt==DayType.NTH_THURSDAY){
				return moveToNthDay(currDay, Calendar.THURSDAY, offsetValue);
			}else if(dt==DayType.NTH_FRIDAY){
				return moveToNthDay(currDay, Calendar.FRIDAY, offsetValue);
			}else if(dt==DayType.FULL_MONTH){
				currDay.add(Calendar.MONTH, offsetValue);
			}else if(dt==DayType.IF_FRIDAY_APPLY_BUSINESS_DAY){
				if(currDay.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
					currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
				}
			}else if(dt==DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY){
				// apply the date math if the day is a holiday
				// a holiday is defined as not being a sat or sun, and not being a business day
				// make sure it's not a sat or sun
				if(currDay.get(Calendar.DAY_OF_WEEK)!=Calendar.SATURDAY && currDay.get(Calendar.DAY_OF_WEEK)!=Calendar.SUNDAY){
					if(!Dates.isBusinessDay(locale,currDay)){
						currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
					}
				}
			}else if(dt==DayType.IF_WEEKEND_APPLY_BUSINESS_DAY){
				if(currDay.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || currDay.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY){
					currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);	
				}
			}else if(dt==DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY){
				int currentWeekOfMonth = currDay.get(Calendar.WEEK_OF_MONTH);
				int thanksgivingWeekOfMonth  = Dates.getWeekOfThanksgiving(currDay.get(Calendar.YEAR)) ;//thanksgiving.get(Calendar.WEEK_OF_MONTH);
				if(currDay.get(Calendar.MONTH)==Calendar.NOVEMBER && currentWeekOfMonth==thanksgivingWeekOfMonth){
					currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
				}
			}else if(dt==DayType.IF_NOVEMBER_APPLY_BUSINESS_DAY){
				if(currDay.get(Calendar.MONTH)==Calendar.NOVEMBER ){
					currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
				}
			}else if(dt==DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY){
				int currentWeekOfMonth = currDay.get(Calendar.WEEK_OF_MONTH);
				int thanksgivingWeekOfMonth  = Dates.getWeekOfThanksgiving(currDay.get(Calendar.YEAR)) ;//thanksgiving.get(Calendar.WEEK_OF_MONTH);
				if(currDay.get(Calendar.MONTH)==Calendar.NOVEMBER && currentWeekOfMonth==thanksgivingWeekOfMonth){
					if (currDay.get(Calendar.DAY_OF_WEEK)==Calendar.WEDNESDAY){
						currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
					}
				}
			}else if(dt==DayType.IF_FRIDAY_AFTER_THANKSGIVING_APPLY_BUSINESS_DAY){
				int currentWeekOfMonth = currDay.get(Calendar.WEEK_OF_MONTH);
				int thanksgivingWeekOfMonth  = Dates.getWeekOfThanksgiving(currDay.get(Calendar.YEAR)) ;//thanksgiving.get(Calendar.WEEK_OF_MONTH);
				if(currDay.get(Calendar.MONTH)==Calendar.NOVEMBER && currentWeekOfMonth==thanksgivingWeekOfMonth){
					if (currDay.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
						currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
					}
				}
			}else if(dt==DayType.IF_FRIDAY_BEFORE_COLUMBUS_DAY_APPLY_CALENDAR_DAY){
				int currentWeekOfMonth = currDay.get(Calendar.WEEK_OF_MONTH);
				int weekBeforeMonth  = Dates.getWeekOfColumbusDay(currDay.get(Calendar.YEAR))-1;
				if(currDay.get(Calendar.MONTH)==Calendar.OCTOBER && currentWeekOfMonth==weekBeforeMonth){
					if (currDay.get(Calendar.DAY_OF_WEEK)==Calendar.FRIDAY){
						currDay = Dates.addToCalendar(currDay, offsetValue, Calendar.DAY_OF_MONTH, true);
					}
				}
			}else if(dt==DayType.IF_DECEMBER_APPLY_CALENDAR_DAY){
				int mon = currDay.get(Calendar.MONTH);
				if(mon==11){
					Calendar ret = Calendar.getInstance();
					ret.set(contractYear,0,offsetValue);
					currDay = ret;//
				}else{
					// do nothing
				}
			}else if(dt==DayType.IF_DECEMBER_APPLY_BUSINESS_DAY){
				int mon = currDay.get(Calendar.MONTH);
				if(mon==11){
					currDay = Dates.addBusinessDays(this.locale,currDay, offsetValue);
				}else{
					// do nothing
				}
			}else if(dt==DayType.SAME_DAY){
				currDay = Calendar.getInstance();
			}
		}
		return currDay;
	}

	/**
	 * 
	 * @param currDay : day to start from
	 * @param dayToMoveTo : day of week to move to (e.g Calendar.WEDNESDAY)
	 * @param nThOccurrenceOfThatDay : the nth occurrence of the above day to move to
	 * 			If you are moving to the 3rd occurrence of a Wednesday, then this value would be 3.
	 * 			
	 * @return Calendar of day that represents this nth occurrence
	 * 
	 * Example: 
	 * 		currDay = 8/1/2011
	 * 		dayToMoveTo = Calendar.WEDNESDAY
	 * 		nThOccurrenceOfThatDay = 3
	 *   since 8/1/2011 is on a Monday, the 1st Wednesday is the 8/3/2011, and the 3rd Wednesday
	 *        is 8/17/2011
	 *        So, we will return a Calendar object with the date set to 8/17/2011.
	 *   If the currDay that was passed equaled 8/4/2011, then we would return 8/24/2011.
	 */
	private Calendar moveToNthDay(Calendar currDay,int dayToMoveTo,int nThOccurrenceOfThatDay){
		Calendar ret = (Calendar)currDay.clone();
		int direction=0;
		if(nThOccurrenceOfThatDay>0){
			direction = 1;
		}else if(nThOccurrenceOfThatDay<0){
			direction = -1;
		}
		
		// move to first occurrence of dayToMoveTo
		
		for(int i = 0;i<7;i++){
			if(ret.get(Calendar.DAY_OF_WEEK)==dayToMoveTo){
				// found dayToMoveTo
				break;
			}
			ret = Dates.addToCalendar(ret,direction,Calendar.DAY_OF_MONTH,false);
		}
		// now jump forward the number of weeks specified by nThOccurrenceOfThatDay
		// BP bug fix for negative values of nThOccurenceOfThatDay
//		int daysForward = (nThOccurrenceOfThatDay-(1*direction))*7*direction;
		int daysForward = (nThOccurrenceOfThatDay-(1*direction))*7*Math.abs(direction);
		ret = Dates.addToCalendar(ret,daysForward,Calendar.DAY_OF_MONTH,false);
		return ret;
	}
}
