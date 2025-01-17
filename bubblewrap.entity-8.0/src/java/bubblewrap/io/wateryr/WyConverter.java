package bubblewrap.io.wateryr;

import bubblewrap.io.datetime.DateRange;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.datetime.DateTimeComparator;
import bubblewrap.io.schedules.TimeInterval;
import bubblewrap.io.schedules.TimeStep;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.wateryr.enums.WYPeriod;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class WyConverter {

  // <editor-fold defaultstate="collapsed" desc="Public Static Field">
  /**
   * The default TimeZone for generating DateTimes ({@linkplain TimeZone#getTimeZone(
   * java.lang.String) TimeZone.getTimeZone("PST")}).
   */
  public static TimeZone PstTimeZone = TimeZone.getTimeZone("PST");
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the Water Year of the <tt>curDt</tt>
   * @param curDt the date to evaluate
   * @return curDt.year is curDt.Month &lt; 9, else curDt.year+1
   */
  public static Integer getWaterYear(DateTime curDt) {
    Integer result = null;
    if (curDt != null) {
      result = curDt.getYear();
      result += ((curDt.getMonth() >= 9)? 1: 0);
    }
    return result;
  }
  
  /**
   * Get the start date (Oct 1) of the Water Year for TimeZone = 
   * {@linkplain #PstTimeZone}.
   * @param waterYr the Water Year
   * @return DateTime(waterYr-1, 9, 1, PstTimeZone)
   */
  public static DateTime getWyStartDt(int waterYr) {
    return WyConverter.getWyStartDt(waterYr, WyConverter.PstTimeZone);
  }
  
  /**
   * Get the start date (Oct 1) of the specified Water Year
   * @param waterYr the Water Year
   * @param timeZone the specified time zone (assumed {@linkplain #PstTimeZone} if not 
   * defined.
   * @return DateTime(waterYr-1, 9, 1,timeZone)
   */
  public static DateTime getWyStartDt(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr-1, 9, 1, timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }
  
  /**
   * Get the end date (Sep 30th)  of the Water Year for TimeZone = 
   * {@linkplain #PstTimeZone}.
   * @param waterYr the Water Year
   * @return DateTime(waterYr, 8, 30, PstTimeZone)
   */
  public static DateTime getWyEndDt(int waterYr) {
    return WyConverter.getWyEndDt(waterYr, WyConverter.PstTimeZone);  
  }
  
  /**
   * Get the end date (Sep 30th) of the specified Water Year
   * @param waterYr the Water Year
   * @param timeZone the specified time zone (assumed {@linkplain #PstTimeZone} if not 
   * defined.
   * @return DateTime(waterYr, 8, 30,timeZone)
   */
  public static DateTime getWyEndDt(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 8, 30, timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }
  
  /**
   * Get a Water Year date Range with min = wyStartDt(waterYr, timeZone) and max = 
   * wyEndDt(waterYr, timeZone), using DateTimeComparator[interval=DAYS] and timeZone =
   * {@linkplain #PstTimeZone}
   * @param waterYr the input water year
   * @return the date range
   */
  public static DateRange getWyDateRange(int waterYr) {
    return WyConverter.getWyDateRange(waterYr, WyConverter.PstTimeZone);  
  }
  
  /**
   * Get a Water Year date Range with min = wyStartDt(waterYr, timeZone) and max = 
   * wyEndDt(waterYr, timeZone), using DateTimeComparator[interval=DAYS].
   * @param waterYr the input water year
   * @param timeZone the timeZone (assumed {@linkplain #PstTimeZone} if not defined.
   * @return the date range
   */
  public static DateRange getWyDateRange(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime startDt = WyConverter.getWyStartDt(waterYr, timeZone);
    DateTime endDt = WyConverter.getWyEndDt(waterYr, timeZone);
    return new DateRange(new DateTimeComparator(Interval.DAYS), startDt, endDt);
  }
  
  /**
   * Get the WaterYear Day for the specified date (i.e., the number of days since the 
   * start of the Water Year (Oct 1st = day zero).
   * @param curDt the current date
   * @return the number of days (0..365).
   * @exception NullPointerException if curDt = null.
   */
  public static int toWyDay(DateTime curDt) {
    if (curDt == null) {
      throw new NullPointerException("The WyDay's specified Date is unassigned.");
    }
    
    DateTime endDt = DateTime.toZeroHourInTimeZone(curDt, DateTime.UTCTimeZone);
    int wy = endDt.getYear();
    int mon = endDt.getMonth();
    wy = (mon >= 9)? wy+1: wy;
    DateTime startDt = WyConverter.getWyStartDt(wy, endDt.getTimeZone());
    Long numDays = endDt.diff(startDt, TimeUnit.DAYS);
    return numDays.intValue();
  }
  
  /**
   * Get the WaterYear Day for the specified date (i.e., the number of days since the 
   * start of the Water Year (Oct 1st = day zero).
   * @param waterYr the Water Year
   * @param month the calender month (0..11)
   * @param day the month day
   * @param timeZone the time zone (can be null to user PST)
   * @return the Water Year Days from Oct 1
   */
  public static int toWyDay(int waterYr, int month, int day, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    int year = (month >= 9)? waterYr-1: waterYr;
    DateTime curDt = new DateTime(year, month, day, timeZone);
    if (curDt == null) {
      throw new NullPointerException("The WyDay's specified Date is unassigned.");
    }
    return WyConverter.toWyDay(curDt);
  }
  
  /**
   * Get the {@linkplain WYPeriod} for the specified date
   * @param curDt the date the convert
   * @return the date's WYPeriod
   */
  public static WYPeriod toWyPeriod(DateTime curDt) {
    WYPeriod result = null;
    if (curDt != null) {
      int month = curDt.getMonth();
      if ((month >= 9) || (month <= 2)) {
        result = WYPeriod.PreAJ;
      } else if ((month >= 3) && (month <= 6)) {
        result = WYPeriod.AJ;
      } else {
        result = WYPeriod.PostAJ;
      }
    }
    return result;
  }
  
  /**
   * Convert a wyDay for a specified Water Year to a Date in the default TimeZone
   * {@linkplain #PstTimeZone}).
   * @param wyDay the Water Year Day (0..365, with 0 = Oct 1)
   * @param waterYr the Water Year 
   * @return the water year date 
   * @exception IllegalArgumentException if wyDay is out of bounds
   */
  public static DateTime fromWyDay(int wyDay, int waterYr) {
    return WyConverter.fromWyDay(wyDay, waterYr, null);
  }
  
  /**
   * Convert a wyDay for a specified Water Year to a Date in the specified TimeZone
   * @param wyDay the Water Year Day (0..365, with 0 = Oct 1)
   * @param waterYr the Water Year 
   * @param timeZone the TimeZone (can be null, default = {@linkplain #PstTimeZone})
   * @return the water year date 
   * @exception IllegalArgumentException if wyDay is out of bounds
   */
  public static DateTime fromWyDay(int wyDay, int waterYr, TimeZone timeZone) {
    DateTime result = null;
    if ((wyDay < 0) || (wyDay > 365)) {
      throw new IllegalArgumentException("Invalid wyDay[" + wyDay 
                                              + "]. Expected value in range[0..365].");
    }
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime startDt = WyConverter.getWyStartDt(waterYr, DateTime.UTCTimeZone);
    DateTime endDt = startDt.addDays(wyDay);    
    return DateTime.toTimeZoneDate(endDt, timeZone);
  }
  
  /**
   * Get a new date in the same time zone <tt>numDays</tt> from <tt>startDt</tt>.
   * It uses a {@linkplain TimeInterval) with {@linkplain Interval#DAYS} to calculate
   * the new time - avoiding problems with daylight saving
   * @param startDt the start date
   * @param numDays the number of days to add (can be negative to get prior days)
   * @return the calculated DateTime
   */
  public static DateTime addWyDay(DateTime startDt, long numDays) {
    DateTime result = startDt;
    if ((result != null) && (numDays != 0)) {
      TimeInterval timeInt = new TimeInterval(Interval.DAYS, 1l);
      TimeStep timeStep = timeInt.getTimeStep(startDt);
      if (numDays > 0) {
        timeStep = Interval.DAYS.getNextStep(timeStep, numDays);
      } else {
        timeStep = Interval.DAYS.getPriorStep(timeStep, (-1l * numDays));
      }
      result = timeStep.getDateTime();
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static WyDays">
  /**
   * Get the February 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#AJ}) for the {@link #PstTimeZone WyConverter.PstTimeZone} TimeZone.
   * @param waterYr the water year
   * @return WyConverter.getApr1WyDate(waterYr, null)
   */
  public static DateTime getFeb1WyDate(int waterYr) {
    return WyConverter.getFeb1WyDate(waterYr, null);
  }

  /**
   * Get the February 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the specified TimeZone.
   * @param waterYr the water year
   * @param timeZone the specified TimeZone (Default = {@link #PstTimeZone WyConverter.PstTimeZone}
   * @return DateTime.toZeroHourInTimeZone(DateTime(waterYr, 1, 1, timeZone))
   */
  public static DateTime getFeb1WyDate(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 1, 1,  timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }
  
  /**
   * Get the April 1 wyDay for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#AJ})
   * @param waterYr the water year
   * @return wyDate for DateTime(waterYr, 3,1,PstTimeZone)
   */
  public static int getApr1WyDay(int waterYr) {
    DateTime curDt = new DateTime(waterYr, 3, 1, PstTimeZone);
    return WyConverter.toWyDay(curDt);
  }

  /**
   * Get the April 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#AJ}) for the {@link #PstTimeZone WyConverter.PstTimeZone} TimeZone.
   * @param waterYr the water year
   * @return WyConverter.getApr1WyDate(waterYr, null)
   */
  public static DateTime getApr1WyDate(int waterYr) {
    return WyConverter.getApr1WyDate(waterYr, null);
  }

  /**
   * Get the April 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the specified TimeZone.
   * @param waterYr the water year
   * @param timeZone the specified TimeZone (Default = {@link #PstTimeZone WyConverter.PstTimeZone}
   * @return DateTime.toZeroHourInTimeZone(DateTime(waterYr, 3, 1, timeZone))
   */
  public static DateTime getApr1WyDate(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 3, 1,  timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }

  /**
   * Get the August 1 wyDay for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}).
   * @param waterYr the water year
   * @return wyDate for DateTime(waterYr, 7,1,PstTimeZone)
   */
  public static int getAug1WyDay(int waterYr) {
    DateTime curDt = new DateTime(waterYr, 7, 1, PstTimeZone);
    return WyConverter.toWyDay(curDt);
  }

  /**
   * Get the August 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the {@link #PstTimeZone WyConverter.PstTimeZone} TimeZone.
   * @param waterYr the water year
   * @return WyConverter.getAug1WyDate(waterYr, null)
   */
  public static DateTime getAug1WyDate(int waterYr) {
    return WyConverter.getAug1WyDate(waterYr, null);
  }

  /**
   * Get the August 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the specified TimeZone.
   * @param waterYr the water year
   * @param timeZone the specified TimeZone (Default = {@link #PstTimeZone 
   * WyConverter.PstTimeZone}
   * @return DateTime.toZeroHourInTimeZone(DateTime(waterYr, 7,1,timeZone))
   */
  public static DateTime getAug1WyDate(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 7, 1, timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }
  
  /**
   * Get the January 1 wyDay for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}).
   * @param waterYr the water year
   * @return wyDate for DateTime(waterYr, 7,1,PstTimeZone)
   */
  public static int getJan1WyDay(int waterYr) {
    DateTime curDt = new DateTime(waterYr, 0, 1, PstTimeZone);
    return WyConverter.toWyDay(curDt);
  }
  
  /**
   * Get the January 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the {@link #PstTimeZone WyConverter.PstTimeZone} TimeZone.
   * @param waterYr the water year
   * @return WyConverter.getAug1WyDate(waterYr, null)
   */
  public static DateTime getJan1WyDate(int waterYr) {
    return WyConverter.getJan1WyDate(waterYr, null);
  }

  /**
   * Get the January 1 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the specified TimeZone.
   * @param waterYr the water year
   * @param timeZone the specified TimeZone (Default = {@link #PstTimeZone 
   * WyConverter.PstTimeZone}
   * @return DateTime.toZeroHourInTimeZone(DateTime(waterYr, 7,1,timeZone))
   */
  public static DateTime getJan1WyDate(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 0, 1, timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }
  
  /**
   * Get the December 31 wyDay for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}).
   * @param waterYr the water year
   * @return wyDate for DateTime(waterYr, 11,31,PstTimeZone)
   */
  public static int getDec31WyDay(int waterYr) {
    DateTime curDt = new DateTime(waterYr, 11, 31, PstTimeZone);
    return WyConverter.toWyDay(curDt);
  }
  
  /**
   * Get the December 31 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the {@link #PstTimeZone WyConverter.PstTimeZone} TimeZone.
   * @param waterYr the water year
   * @return WyConverter.getAug1WyDate(waterYr, null)
   */
  public static DateTime getDec31WyDate(int waterYr) {
    return WyConverter.getDec31WyDate(waterYr, null);
  }

  /**
   * Get the December 31 wyDate for the specified WaterYear (i.e., start {@linkplain 
   * WYPeriod#PostAJ}) for the specified TimeZone.
   * @param waterYr the water year
   * @param timeZone the specified TimeZone (Default = {@link #PstTimeZone 
   * WyConverter.PstTimeZone}
   * @return DateTime.toZeroHourInTimeZone(DateTime(waterYr, 11,31,timeZone))
   */
  public static DateTime getDec31WyDate(int waterYr, TimeZone timeZone) {
    timeZone = (timeZone == null)? WyConverter.PstTimeZone: timeZone;
    DateTime result = new DateTime(waterYr, 11, 31, timeZone);
    return DateTime.toZeroHourInTimeZone(result, timeZone);
  }

  /**
   * Get the September 30th wyDay for the specified WaterYear (i.e., end of {@linkplain 
   * WYPeriod#PostAJ} or the water year).
   * @param waterYr the water year
   * @return wyDate for DateTime(waterYr, 8, 30,PstTimeZone)
   */
  public static int getEndWyDay(int waterYr) {
    DateTime curDt = new DateTime(waterYr, 8, 30, PstTimeZone);
    return WyConverter.toWyDay(curDt);
  }
  
  /**
   * Check is <tt>waterYr</tt> is the current Water Year
   * @param waterYr the water year to evaluate
   * @return waterYr.equals(WyConverter.getWaterYear(curDt)) or false if waterYr = null.
   */
  public static boolean isCurrentWy(Integer waterYr) {
    boolean result = false;
    if (waterYr != null) {
      DateTime curDt = DateTime.getNow(DateTime.UTCTimeZone);
      result = waterYr.equals(WyConverter.getWaterYear(curDt));
    }
    return result;
  }
  // </editor-fold>
}
