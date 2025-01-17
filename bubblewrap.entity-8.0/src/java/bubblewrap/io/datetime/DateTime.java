/*
 *  The MIT License
 *
 *  Copyright 2010 California Department of Water Resources.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to
 *  deal in the Software without restriction, including without limitation the
 *  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 *  sell copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 *  IN THE SOFTWARE.
 */
package bubblewrap.io.datetime;

import bubblewrap.io.DataEntry;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.wateryr.WyConverter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created on Nov 10, 2010 at 8:59:59 AM
 * @author Harold A. Dunsford Jr. Ph.D.; Extended/Updated by kprins
 * hdunsford@geiconsultants.com & kprins@geiconsultants.com
 * Times are assumed to be in UTC time.  Time zones and
 * display of local time should be handled elsewhere through
 * careful and thoughtful planning on how and where it is
 * translated to take into consideration that daylight savings
 * time, longitude and other factors may be involved.
 */
public class DateTime implements Serializable, Comparable<DateTime> {

  // <editor-fold defaultstate="collapsed" desc="Static Enums">
  /**
   * The Minimum date that the DateTime class can hold.
   */
  public static final DateTime MIN_VALUE = new DateTime(Long.MIN_VALUE);
  /**
   * The Maximum value that the DateTime class can hold.
   */
  public static final DateTime MAX_VALUE = new DateTime(Long.MAX_VALUE);
  /**
   * The Default TimeZone to use for UTC DateTime Conversions
   */
  public final static TimeZone UTCTimeZone = TimeZone.getTimeZone("UTC");
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Creates a new instance of the DateTime class from the specified UTC datetime
   * string formated as "yyyy-MM-ddTHH:mm:ssZ".
   * @param dateTimeUTC String
   * @return DateTime
   * @throws ParseException
   */
  public static DateTime FromString(String dateTimeUTC) throws Exception {
    DateTime result = null;
    try {
      dateTimeUTC = DataEntry.cleanString(dateTimeUTC);
      if (dateTimeUTC != null) {
        SimpleDateFormat dtParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date pDate = dtParser.parse(dateTimeUTC);
        result = new DateTime(pDate.getTime());
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.FromString Error:\n {1}",
              new Object[]{DateTime.class.getSimpleName(), exp.getMessage()});
      throw new Exception(DateTime.class.getSimpleName()
              + ".FromString Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Creates a new instance of the DateTime class from the specified timeZone datetime
   * string formated as specified by sFormat.
   * @param dtString The formated date string
   * @param dtFormat the date format
   * @param timeZone TimeZone
   * @return DateTime
   * @throws ParseException
   */
  public static DateTime FromString(String dtString, String dtFormat,
                                          TimeZone timeZone) throws Exception {
    DateTime result = null;
    try {
      dtString = DataEntry.cleanString(dtString);
      if (dtString != null) {
        timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
        SimpleDateFormat dtParser = new SimpleDateFormat(dtFormat);
        dtParser.setTimeZone(timeZone);
        Date val = dtParser.parse(dtString);
        result = new DateTime(val.getTime(),timeZone);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.FromString Error:\n {1}",
              new Object[]{DateTime.class.getSimpleName(), exp.getMessage()});
      throw new Exception(DateTime.class.getSimpleName()
              + ".FromString Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Get the Current Date (for the Local TimeZone)
   * @return Date the current local time
   */
  public static Date getNowAsDate() {
    Calendar cal = Calendar.getInstance();
    return cal.getTime();
  }

  /**
   * This uses a calendar that should take into account the server's local time and
   * generate an appropriate DateTime for timeZone. If timeZone=null, use
   * TimeZone.getDefault(). Beware that the "Server" may not have
   * the same configuration as the local user, so be cautious when using this option.
   * @param timeZone TimeZone
   * @return DateTime
   */
  public static DateTime getNow(TimeZone timeZone) {
    Calendar cal = null;
    if (timeZone == null) {
      cal = Calendar.getInstance();
    } else {
      cal = Calendar.getInstance(timeZone);
    }
    return new DateTime(cal);
  }

  /**
   * Get Today's DateTime at zero hours (when the day starts)
   * (e.g., if today"04/01/2000", zeroHourToday="04/02/2000 00:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at midnight
   */
  public static DateTime zeroHourToday(TimeZone timeZone) {
    timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
    Calendar cal = Calendar.getInstance(timeZone);
    int iYear = cal.get(Calendar.YEAR);
    int iMon = cal.get(Calendar.MONTH);
    int iDay = cal.get(Calendar.DAY_OF_MONTH);
    cal.set(iYear, iMon, iDay, 00, 0, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return new DateTime(cal);
  }

  /**
   * Get Today's Midnight DateTime (e.g., if today"04/01/2000",
   * midnightToday="04/02/2000 00:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at midnight
   */
  public static DateTime midnightToday(TimeZone timeZone) {
    timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
    Calendar cal = Calendar.getInstance(timeZone);
    int iYear = cal.get(Calendar.YEAR);
    int iMon = cal.get(Calendar.MONTH);
    int iDay = cal.get(Calendar.DAY_OF_MONTH);
    cal.setTimeInMillis(0l);
    cal.set(iYear, iMon, iDay, 24, 0, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return new DateTime(cal);
  }

  /**
   * Get pDateTime's Midnight DateTime (e.g., if today"04/01/2000",
   * midnightToday="04/02/2000 00:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at midnight or DateTime.midnightToday(null) if
   * (pDateTime=null)
   */
  public static DateTime midnightForDate(DateTime date) {
    DateTime result = null;
    if (date == null) {
      result = DateTime.midnightToday(null);
    } else {
      TimeZone timeZone = date.getTimeZone();
      timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
      Calendar dtCal = date.getCalendar();
      Calendar cal = Calendar.getInstance(timeZone);
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 24, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result= new DateTime(cal);
    }
    return result;
  }

  /**
   * Get Today's Noon DateTime (e.g., if today"04/01/2000",
   * noonToday="04/01/2000 12:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at noon
   */
  public static DateTime noonToday(TimeZone timeZone) {
    timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
    Calendar cal = Calendar.getInstance(timeZone);
    int iYear = cal.get(Calendar.YEAR);
    int iMon = cal.get(Calendar.MONTH);
    int iDay = cal.get(Calendar.DAY_OF_MONTH);
    cal.set(iYear, iMon, iDay, 12, 0, 0);
    cal.set(Calendar.MILLISECOND, 0);
    return new DateTime(cal);
  }

  /**
   * Get pDateTime's Noon DateTime (e.g., if today"04/01/2000",
   * noonToday="04/01/2000 12:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at noon or DateTime.noonToday(null) if
   * (pDateTime=null)
   */
  public static DateTime noonForDate(DateTime date) {
    DateTime result = null;
    if (date == null) {
      result = DateTime.noonToday(null);
    } else {
      TimeZone timeZone = date.getTimeZone();
      timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
      Calendar dtCal = date.getCalendar();
      Calendar cal = Calendar.getInstance(timeZone);
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 12, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result= new DateTime(cal);
    }
    return result;
  }

  /**
   * Get pDateTime's DateTime at zero hours (when the day starts)
   * (e.g., if today"04/01/2000", zeroHourToday="04/02/2000 00:00:0000")
   * @param timeZone the TimeZone of interest (if null, use TimeZone.getDefault())
   * @return the DateTime for today at midnight or DateTime.zeroHourToday(null) if
   * (pDateTime=null)
   */
  public static DateTime zeroHourForDate(DateTime date) {
    DateTime result = null;
    if (date == null) {
      result = DateTime.zeroHourToday(null);
    } else {
      TimeZone timeZone = date.getTimeZone();
      Calendar cal = Calendar.getInstance(timeZone);
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 00, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result= new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the ZeroHour DateTime for the the current Date
   * @return the today's zero hour dateTime for TimeZone UTC
   */
  public static DateTime toZeroHourUTCToday() {
    DateTime result = DateTime.getNow(DateTime.UTCTimeZone);
    if (result != null) {
      Calendar cal = Calendar.getInstance(DateTime.UTCTimeZone);
      Calendar dtCal = result.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the ZeroHour DateTime for the UTC (Zulu) TimeZone
   * @param  the date toe convert
   * @return <tt>date</tt>'s zero hour dateTime for TimeZone UTC
   */
  public static DateTime toZeroHourUTC(DateTime date) {
    DateTime result = date;
    if (date == null) {
      result = DateTime.toZeroHourUTCToday();
    } else {
      Calendar cal = Calendar.getInstance(DateTime.UTCTimeZone);
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the DateTime for the UTC TimeZone using the <tt>date</tt>'s date/time samp
   * (i.e., the year, month, day, hours, minutes, seconds, and milli second stays
   * the same, only the timeZone shifts)
   * @param  the date to convert
   * @return <tt>date</tt>'s zero hour dateTime for the UTC TimeZone
   */
  public static DateTime toUTCDate(DateTime date) {
    DateTime result = date;
    if (date == null) {
      result = DateTime.getNow(DateTime.UTCTimeZone);
    } else {
      Calendar cal = Calendar.getInstance(DateTime.UTCTimeZone);
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      int iHrs = dtCal.get(Calendar.HOUR_OF_DAY);
      int iMin = dtCal.get(Calendar.MINUTE);
      int iSec = dtCal.get(Calendar.SECOND);
      int iMilSec = dtCal.get(Calendar.MILLISECOND);
      cal.set(iYear, iMon, iDay, iHrs, iMin, iSec);
      cal.set(Calendar.MILLISECOND, iMilSec);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the DateTime for the specified TimeZone using the same <tt>date</tt>'s date/time
   * samp (i.e., the year, month, day, hours, minutes, seconds, and milli second stays
   * the same, only the timeZone shifts)
   * @param date the specified date (assumed today if unassigned)
   * @param timeZone the specified timezone (assumed {@linkplain #UTCTimeZone} if
   * unassigned)
   * @return <tt>date</tt>'s zero hour dateTime for the specified or UTC TimeZone
   */
  public static DateTime toTimeZoneDate(DateTime date, TimeZone timeZone) {
    DateTime result = date;
    timeZone = (timeZone == null)? DateTime.UTCTimeZone: timeZone;
    if (date == null) {
      result = DateTime.getNow(timeZone);
    } else {
      Calendar cal = Calendar.getInstance(timeZone);
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      int iHrs = dtCal.get(Calendar.HOUR_OF_DAY);
      int iMin = dtCal.get(Calendar.MINUTE);
      int iSec = dtCal.get(Calendar.SECOND);
      int iMilSec = dtCal.get(Calendar.MILLISECOND);
      cal.set(iYear, iMon, iDay, iHrs, iMin, iSec);
      cal.set(Calendar.MILLISECOND, iMilSec);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the ZeroHour DateTime for the the specified Date in the specified TimeZone
   * @param date the specified date (assumed today if unassigned)
   * @param timeZone the specified timezone (assumed {@linkplain #UTCTimeZone} if
   * unassigned)
   * @return the <tt>date</tt>'s (or today's) zero hour dateTime for the <tt>tiemZone</tt>
   * or DateTime.UTCTimeZone timezone.
   */
  public static DateTime toZeroHourInTimeZone(DateTime date, TimeZone timeZone) {
    DateTime result = null;
    timeZone = (timeZone == null)? DateTime.UTCTimeZone: timeZone;
    if (date == null) {
      result = DateTime.zeroHourToday(timeZone);
    } else {
      Calendar cal = Calendar.getInstance(timeZone);
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the ZeroHour DateTime for the the current Date in the local TimeZone
   * @return the today's zero hour dateTime for the local TimeZone
   */
  public static DateTime toZeroHourLocalToday() {
    DateTime result = null;
    Calendar cal = Calendar.getInstance();
    if (cal != null) {
      int iYear = cal.get(Calendar.YEAR);
      int iMon = cal.get(Calendar.MONTH);
      int iDay = cal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the ZeroHour DateTime for the Local TimeZone using the same year, month, day
   * stamp
   * @param  the date toe convert
   * @return <tt>date</tt>'s zero hour dateTime for the local TimeZone
   */
  public static DateTime toZeroHourLocal(DateTime date) {
    DateTime result = date;
    if (date == null) {
      result = DateTime.toZeroHourLocalToday();
    } else {
      Calendar cal = Calendar.getInstance();
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      cal.set(iYear, iMon, iDay, 0, 0, 0);
      cal.set(Calendar.MILLISECOND, 0);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Get the DateTime for the Local TimeZone using the <tt>date</tt>'s date/time samp
   * @param  the date to convert
   * @return <tt>date</tt>'s zero hour dateTime for the local TimeZone
   */
  public static DateTime toLocalDate(DateTime date) {
    DateTime result = date;
    if (date == null) {
      result = DateTime.toZeroHourLocalToday();
    } else {
      Calendar cal = Calendar.getInstance();
      Calendar dtCal = date.getCalendar();
      int iYear = dtCal.get(Calendar.YEAR);
      int iMon = dtCal.get(Calendar.MONTH);
      int iDay = dtCal.get(Calendar.DAY_OF_MONTH);
      int iHrs = dtCal.get(Calendar.HOUR_OF_DAY);
      int iMin = dtCal.get(Calendar.MINUTE);
      int iSec = dtCal.get(Calendar.SECOND);
      int iMilSec = dtCal.get(Calendar.MILLISECOND);
      cal.set(iYear, iMon, iDay, iHrs, iMin, iSec);
      cal.set(Calendar.MILLISECOND, iMilSec);
      result = new DateTime(cal);
    }
    return result;
  }

  /**
   * Called to get the short (e.g., "Jan") or long (e.g., "January") name of the Month.
   * @param month the month index [0..11]
   * @param shortName true to return the short name
   * @return the short/long name of the month or null if month is not in range[0..11]
   */
  public static String getMonthName(int month, boolean shortName) {
    String result = null;
    if ((month >= 0) && (month <= 11)) {
      String formatStr = (shortName)? "MMM": "MMMM";
      DateTime tmpDt = new DateTime(1900, month, 1, null);
      result = tmpDt.toLocaleString(formatStr);
    }
    return result;
  }

  /**
   * Called to get the short (e.g., "Jan") or long (e.g., "January") name of the Month.
   * @param month the month index [0..11]
   * @param year the year
   * @return the number of days in the month or 0 if month is not in range[0..11]
   */
  public static int getDaysInMonth(int month, int year) {
    int result = 0;
    if ((month >= 0) && (month <= 11)) {
      DateTime dt1 = new DateTime(year, month, 1, null);
      int month2 = (month == 11)? 0: month + 1;
      int year2 = (month == 11)? year + 1: year;
      DateTime dt2 = new DateTime(year2, month2, 1, null);
      Long diff = dt1.diff(dt2, TimeUnit.DAYS);
      result = diff.intValue();
    }
    return result;
  }

  /**
   * Private Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(DateTime.class.getName());
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Java tracks time in milliseconds from Jan 1, 1970, UTC, both positive
   * and negative values are permitted.
   */
  private long milliSeconds;
  /**
   * The DateTime's TimeZone (default = 'UTC')
   */
  private TimeZone timeZone;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Creates a default instance based on the current date, using for the Host TimeZone.
   */
  public DateTime() {
    Calendar cal = Calendar.getInstance();
    this.milliSeconds = cal.getTimeInMillis();
    this.timeZone = TimeZone.getDefault();
  }

  /**
   * Sets the DateTime using milliseconds from Jan 1, 1970, UTC.
   * This can be obtained from Calendar.getTimeInMillis(), where
   * the Calendar can be tailored to take into account time zone,
   * daylight savings time and other parameters.
   * @param milliseconds
   */
  public DateTime(long milliseconds) {
    this.milliSeconds = milliseconds;
    this.timeZone = TimeZone.getDefault();
  }

  /**
   * Sets the DateTime using milliseconds from Jan 1, 1970, UTC.
   * This can be obtained from Calendar.getTimeInMillis(), where
   * the Calendar can be tailored to take into account timeZone,
   * daylight savings time and other parameters.
   * @param milliseconds the DateTime in MilliSeconds
   * @param timeZone the TimeZone to use (use {@linkplain TimeZone#getDefault()} if
   * null).
   */
  public DateTime(long milliseconds, TimeZone timeZone) {
    this(milliseconds);
    this.timeZone = (timeZone == null)? this.timeZone: timeZone;
  }

  /**
   * A Java calendar handles the more sophisticated nuances of setting
   * up a local date and time.  The time from the specified calendar
   * automatically supports the millisecond UTC time as getTimeInMillis.
   * @param pDate Date - The calendar to use to set this DateTime.
   */
  public DateTime(Date pDate) {
    if (pDate == null) {
      throw new NullPointerException("The Date parameter cannot be null.");
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(pDate);
    this.milliSeconds = calendar.getTimeInMillis();
    this.timeZone = calendar.getTimeZone();
  }

  /**
   * A Java calendar handles the more sophisticated nuances of setting
   * up a local date and time.  The time from the specified calendar
   * automatically supports the millisecond UTC time as getTimeInMillis.
   * @param pDate Date - The calendar to use to set this DateTime.
   *  @param timeZone the TimeZone to use (use {@linkplain TimeZone#getDefault()} if
   * null).
   */
  public DateTime(Date pDate, TimeZone timeZone) {
    this(pDate);
    this.timeZone = (timeZone == null)? this.timeZone: timeZone;
  }

  /**
   * A Java calendar handles the more sophisticated nuances of setting
   * up a local date and time.  The time from the specified calendar
   * automatically supports the millisecond UTC time as getTimeInMillis.
   * @param calendar Calendar - The calendar to use to set this DateTime.
   */
  public DateTime(Calendar calendar) {
    if (calendar == null) {
      throw new NullPointerException("The calendar parameter cannot be null.");
    }
    this.milliSeconds = calendar.getTimeInMillis();
    this.timeZone = calendar.getTimeZone();
  }

  /**
   *
   * @param dtYear the date's Year (must be defined)
   * @param dtMonth The month of the year (0 = January, 11 = December)
   * @param dayOfMonth the the day of the specified month (1 to 31)
   * @param timeZone (optional) the applicable time zone - default is the Locale TimeZone
   */
  public DateTime(Integer dtYear, Integer dtMonth, Integer dayOfMonth,
          TimeZone timeZone) {
    try {
      this.setDate(dtYear, dtMonth, dayOfMonth, timeZone);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".New Error:\n " + exp.getMessage());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Set the DateTime">
  /**
   * Set the Year, Month, and DayofMonth for the DateTime Instance Calendar (i.e. for
   * the default host TimeZone). If any of these parameters are unassigned (null) it
   * will be ignored. An exception is thrown if the values are out of range.
   * @param iYear the year
   * @param iMonth the month of the year (0 = January, 11 = December)
   * @param iDayOfMonth Integer - The integer day of the month (1 to 31)
   * @throws InvalidDateException
   */
  public final void setDate(Integer iYear, Integer iMonth, Integer iDayOfMonth)
          throws InvalidDateException {
    this.setDate(iYear, iMonth, iDayOfMonth, null);
  }

  /**
   * Set the Year, Month, and DayofMonth for the DateTime Instance Calendar (i.e. for
   * the preset TimeZone). If any of these parameters are unassigned (null) it will
   * be ignored. An exception is thrown if the values are out of range.
   * @param iYear The year
   * @param iMonth The month of the year (0 = January, 11 = December)
   * @param iDayOfMonth The integer day of the month (1 to 31)
   * @param timeZone the required time zone (use current TimeZone if null).
   * @throws InvalidDateException
   */
  public final void setDate(Integer iYear, Integer iMonth, Integer iDayOfMonth,
          TimeZone timeZone) throws InvalidDateException {
    Calendar calendar = null;
    try {
      calendar = this.getCalendar();
      if (iYear != null) {
        calendar.set(Calendar.YEAR, iYear);
      }
      if (iMonth != null) {
        calendar.set(Calendar.MONTH, iMonth); // month is zero based index in java
      }
      if (iDayOfMonth != null) {
        calendar.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
      }
      if (timeZone != null) {
        calendar.setTimeZone(timeZone);
      }
      this.milliSeconds = (calendar == null) ? 0 : calendar.getTimeInMillis();
      this.timeZone = calendar.getTimeZone();
    } catch (Exception exp) {
      InvalidDateException rethrow = new InvalidDateException(iYear, iMonth, iDayOfMonth);
      rethrow.initCause(exp);
      throw rethrow;
    }

  }

  /**
   * Set the DateTime based on a defined Date and a default Calendar. If pDate=null,
   * set DateTime=Today.
   * @param pDate Date
   */
  public final void setDate(Date pDate) {
    Calendar calendar = Calendar.getInstance();
    if (pDate == null) {
      calendar.setTime(pDate);
    }
    this.milliSeconds = calendar.getTimeInMillis();
    this.timeZone = calendar.getTimeZone();
  }

  /**
   * Sets the time.  The date will be held constant.  Null values will
   * be ignored, rather than set to 0.
   * @param iHour Integer - The integer hour.
   * @param iMinute Integer - The integer minute.
   * @param iSeconds Double - The double seconds, which can include decimal terms.
   * @throws InvalidDateException
   */
  public final void setTime(Integer iHour, Integer iMinute, Double iSeconds)
          throws InvalidDateException {
    this.setTime(iHour, iMinute, iSeconds, null);
  }

  /**
   * Sets the time.  The date will be held constant.  Null values will
   * be ignored, rather than set to 0.
   * @param iHour Integer - The integer hour.
   * @param iMinute Integer - The integer minute.
   * @param iSeconds Double - The double seconds, which can include decimal terms.
   * @param timeZone TimeZone (e.g., TimeZone.getTimeZone("PST")) - use the Host
   * TimeZone if unassigned
   * @throws InvalidDateException
   */
  public final void setTime(Integer iHour, Integer iMinute, Double iSeconds,
          TimeZone timeZone) throws InvalidDateException {
    Calendar calendar = this.getCalendar();
    if (timeZone != null) {
      calendar.setTimeZone(timeZone);
    }
    try {
      if (iHour != null) {
        calendar.set(Calendar.HOUR_OF_DAY, iHour);
      }
      if (iMinute != null) {
        calendar.set(Calendar.MINUTE, iMinute);
      }
      if (iSeconds != null) {
        int iSec = (int) Math.floor(iSeconds);
        calendar.set(Calendar.SECOND, iSec);
        int iMilSec = (int) Math.floor((iSeconds - iSec) * 1000);
        calendar.set(Calendar.MILLISECOND, iMilSec);
      }
      this.milliSeconds = calendar.getTimeInMillis();
      this.timeZone = calendar.getTimeZone();
    } catch (Exception exp) {
      InvalidDateException rethrow =
              new InvalidDateException(iHour, iMinute, iSeconds);
      rethrow.initCause(exp);
      throw rethrow;
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="DateTime Operators">
  /**
   * Returns a DateTime that is the result of subtracting the specified time
   * span from this DateTime class.
   * @param pSpan DateTimeSpan - The span of time to subtract.
   * @return DateTime.
   */
  public DateTime subtract(DateTimeSpan pSpan) {
    if (pSpan == null) {
      throw new NullPointerException("The span parameter cannot be null.");
    }
    return new DateTime(this.milliSeconds - pSpan.getTotalMilliseconds(),
              this.timeZone);
  }

  /**
   * Subtracts the specified date from this DateTime and return a DateTimeSpan.
   * Since both times are in UTC time, this simply subtracts the milliseconds,
   * constructing a new TimeSpane class from that value.
   * @param pStartDate DateTime The date that occurs before this date.
   * @return DateTimeSpan - The DateTimeSpan difference between the two DateTime values.
   */
  public DateTimeSpan subtract(DateTime pStartDate) {
    if (pStartDate == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    return new DateTimeSpan(pStartDate, this);
  }

  /**
   * .
   * Since both times are in UTC time, this simply subtracts the milliseconds,
   * constructing a new TimeSpane class from that value.
   * @param pStartDate DateTime The date that occurs before this date.
   * @return DateTimeSpan - The DateTimeSpan difference between the two DateTime values.
   */
  public long getTime(DateTime pStartDate, TimeUnit eUnit) {
    if (pStartDate == null) {
      throw new NullPointerException("The Start Date cannot be unassigned.");
    }
    long lStartTime = pStartDate.milliSeconds;
    long lEndTime = this.milliSeconds;
    long lDiff = (lEndTime - lStartTime);
    return eUnit.convert(lDiff, TimeUnit.MILLISECONDS);
  }

  /**
   * Returns a DateTime that is the result of adding the specified time span
   * to this DateTime.
   * @param pSpan DateTimeSpan -the span of time to add to this date.
   * @return DateTime.
   */
  public DateTime add(DateTimeSpan pSpan) {
    if (pSpan == null) {
      throw new NullPointerException("The span parameter cannot be null.");
    }
    return new DateTime(this.milliSeconds + pSpan.getTotalMilliseconds(),
            this.timeZone);
  }

  /**
   * Add a and incremental time of a specified TimeUnit to this DateTime and return
   * a new DateTime
   * @param lTime a time increment (positive or negative)
   * @param eUnit the increments {@linkplain TimeUnit}
   * @return the new DateTime
   */
  public DateTime addTime(long lTime, TimeUnit eUnit) {
    long lDiff = (eUnit.equals(TimeUnit.MILLISECONDS))? lTime:
                                        TimeUnit.MILLISECONDS.convert(lTime, eUnit);
    long lResult = this.milliSeconds + lDiff;
    return new DateTime(lResult, this.timeZone);
  }

  /**
   * Return a new DateTime with iYears added. It uses the instance's Calendar for
   * adding the years.
   * @param iYear int
   * @return DateTime
   */
  public DateTime addYears(int iYear) {
    Calendar calendar = this.getCalendar();
    calendar.add(Calendar.YEAR, iYear);
    return new DateTime(calendar);
  }

  /**
   * Return a new DateTime with iMonths added. It uses the instance's Calendar for
   * adding the months.
   * @param iMonths
   * @return
   */
  public DateTime addMonths(int iMonths) {
    Calendar calendar = this.getCalendar();
    calendar.add(Calendar.MONTH, iMonths);
    return new DateTime(calendar);
  }

  /**
   * Adds the specified number of 24 hour spans to the date time of this instance's
   * Calendar and return a new DateTime. This supports decimal form, but care should
   * be taken that the days are in Calendar's TimeZone, which could consider daylight
   * savings time.
   * @param dDays double
   * @return DateTime
   */
  public DateTime addDays(double dDays) {
    long lTime = Math.round(dDays*24*3600*1000);
    return this.addTime(lTime, TimeUnit.MILLISECONDS);
  }

  /**
   * Adds the specified number hours to the date time of this instance's
   * Calendar and return a new DateTime. This supports decimal form, but care should
   * be taken that the hours are in the Calendar's TimeZone, which could consider
   * daylight savings time.
   * @param dHours Double.
   * @return DateTime
   */
  public DateTime addHours(double dHours) {
    long lTime = Math.round(dHours*3600*1000);
    return this.addTime(lTime, TimeUnit.MILLISECONDS);
  }

  /**
   * Adds the specified number minutes to the date time of this instance's
   * Calendar and return aa new DateTime. This supports decimal form, but care should
   * be taken that the minutes are in the Calendar's TimeZone, which could consider
   * daylight savings time.
   * @param dMinutes double
   * @return DateTime
   */
  public DateTime addMinutes(double dMinutes) {
    long lTime = Math.round(dMinutes*60*1000);
    return this.addTime(lTime, TimeUnit.MILLISECONDS);
  }

  /**
   * Adds the specified number seconds to the date time of this instance's
   * Calendar and return aa new DateTime. This supports decimal form, but care should
   * be taken that the seconds are in the Calendar's TimeZone, which could consider
   * daylight savings time.
   * @param dSeconds
   * @return
   */
  public DateTime addSeconds(double dSeconds) {
    long lTime = Math.round(dSeconds*1000);
    return this.addTime(lTime, TimeUnit.MILLISECONDS);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Get DateTime Properties">
  /**
   * Gets the long count of milliseconds since Epoch, January 1, 1970 00:00:00.000 GMT.
   * The term, like the date time considered here is in UTC time, and local time
   * changes are not considered.
   * @return
   */
  public long getTotalMilliseconds() {
    return this.milliSeconds;
  }

  /**
   * Gets the Year of this DateTime.  This specifically uses this instance's Calendar
   * and TimeZone.
   * @return the current 4-digit year
   */
  public int getYear() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.YEAR);
  }

  /**
   * Gets the Month of this DateTime.  This specifically uses this instance's Calendar
   * and TimeZone.
   * @return return a current date month (zero base - [0...11])
   */
  public int getMonth() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.MONTH);
  }

  /**
   * Gets the Week of this DateTime's Year.  This specifically uses this instance'
   * s Calendar and TimeZone.
   * @return return a current date's week-of-year (zero base - [0...51])
   */
  public int getWeekofYear() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.WEEK_OF_YEAR);
  }

  /**
   * Gets the DayOfMonth of this DateTime.  This specifically uses this instance's
   * Calendar and TimeZone.
   * @return return a current date date-of-month [1..31]
   */
  public int getDayOfMonth() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.DAY_OF_MONTH);
  }

  /**
   * Gets the DayOfWeek of this DateTime.  This specifically uses this instance's
   * Calendar and TimeZone.
   * @return the {@linkplain Calendar} Day of the week constants starting the week at
   * {@linkplain Calendar#SUNDAY SUNDAY(1)} through {@linkplain Calendar#SATURDAY
   * SATURDAY(7)}
   */
  public int getDayOfWeek() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.DAY_OF_WEEK);
  }

  /**
   * Gets the integer century for the specified date and time.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return the current year/100*100
   */
  public int getCentury() {
    // Integer division effectivly truncates the remainder and is faster
    // than Math.Floor.
    return (this.getYear() / 100) * 100;
  }

  /**
   * Gets the integer representing the floored integer count of 4 year cycles
   * occurring since the most recent century.  So 1900, 1901, 1902 and 1903
   * are all 0, but 1904 is 1.  2000 starts at 0 again.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return int
   */
  public int getQuadrennium() {
    return (this.getYear() - getCentury()) / 4;
  }

  /**
   * Gets the integer day of the year.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return the current date/time day-of-year [1..366]
   */
  public int getDayOfYear() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.DAY_OF_YEAR);
  }

  /**
   * Gets the hour of the day from 0 to 23.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return current hour [0..23]
   */
  public int getHour() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.HOUR_OF_DAY);
  }

  /**
   * Gets the minute of the hour, from 0 to 59.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return minutes in the current hour [0..59]
   */
  public int getMinute() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.MINUTE);
  }

  /**
   * Gets the seconds of the minute, from 0 to 59.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return seconds in the current hour [0..59]
   */
  public int getSecond() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.SECOND);
  }

  /**
   * Gets the integer millisecond, from 0 to 999.
   * This specifically uses this instance's Calendar and TimeZone.
   * @return minutes in the current hour [0..999]
   */
  public int getMillisecond() {
    Calendar calendar = this.getCalendar();
    return calendar.get(Calendar.MILLISECOND);
  }

  /**
   * Return the Date object representing the DateTime using a GreorianCalendar and
   * converted to the local TimeZone - converted of this.TimeZone (e.g.
   * DateTime[3/20/2015 00:00:00 +0:00 UTC] returns
   * Date[3/19/2015 16:00:00 -8:00 PST] is the local TimeZone = PST).
   * @return this instance Date in local TimeZone
   */
  public Date getAsDate() {
    Calendar calendar = this.getCalendar();
    return calendar.getTime();
  }

  /**
   * Return the Date object representing the DateTime Date-Time stamp in the Local
   * TimeZone (e.g. DateTime[3/20/2015 00:00:00 +0:00 UTC] returns
   * Date[3/20/2015 00:00:00 -8:00 PST] is the local TimeZone = PST).
   * @return the Date
   */
  public Date getAsLocalDate() {
    DateTime localDt = DateTime.toLocalDate(this);
    return (localDt == null)? this.getCalendar().getTime():
                              localDt.getCalendar().getTime();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Get the Classes TimeZone/Calender">
  /**
   * CAN OVERRIDE: Get the DateTime class' TimeZone. (Default is the UTC TimeZone)
   * @return TimeZone
   */
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  /**
   * CAN OVERRIDE: Get the DateTime's Calendar. Base method returns a GregorianCalendar
   * for the TimeZone=getTimeZone()
   * @return Calendar
   */
  public Calendar getCalendar() {
    TimeZone timeZone = this.getTimeZone();
    Calendar calendar = null;
    if (timeZone == null) {
      calendar = new GregorianCalendar();
    } else {
      calendar = new GregorianCalendar(timeZone);
    }
    calendar.setTimeInMillis(milliSeconds);
    return calendar;
  }

  /**
   * CAN OVERRIDE: Get the DateTime's Locale (for formatting). Base Method return the
   * default Locale.
   * @return Locale
   */
  public Locale getLocale() {
    return Locale.getDefault();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Converters">
  /**
   * Convert this DateTime to a DateTiem fro the specified TimeZone
   * @param timeZone the new TimeZone (if null use {@linkplain TimeZone#getDefault()}
   * @return the DateTiem for the TimeZone.
   */
  public DateTime forTimeZone(TimeZone timeZone) {
    timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
    DateTime result = this.clone();
    result.timeZone = timeZone;
    return result;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Compare Methods">
  /**
   * A chronological comparison of this DateTime to another DateTime.
   * @param otherDt the DateTime to compare to
   * @return 1 if this DateTime is later, -1 if this DateTime is earlier or pOther=null,
   * and 0 if they are at the same time to the millisecond.
   */
  @Override
  public int compareTo(DateTime otherDt) {
    int result = 0;
    if (otherDt == null) {
      result = -1;
    } else if (this.milliSeconds > otherDt.milliSeconds) {
      result = 1;
    } else if (milliSeconds < otherDt.milliSeconds) {
      result = -1;
    }
    return result;
  }
  
  /**
   * A chronological comparison of this DateTime to another DateTime based on a 
   * specified <tt>interval</tt>.
   * @param otherDt the DateTime to compare to
   * @param interval the time interval for which to compare
   * @return 1 if this DateTime is later, -1 if this DateTime is earlier or pOther=null,
   * and 0 if they are at the same time to the millisecond.
   */
  public int compareTo(DateTime other, Interval interval) {
    int result = 0;
    if (other == null) {
      result = -1;
    } else  if ((interval == null) || Interval.MILLISECONDS.equals(interval)) {
      result = this.compareTo(other);
    } else {
      Integer curVal = null;
      Integer otherVal = null;
      switch (interval) {
        case WATERYEARS:
          Integer curWy = WyConverter.getWaterYear(this);
          Integer otherWy = WyConverter.getWaterYear(other);
          result = curWy.compareTo(otherWy);
        case YEARS:
          curVal = this.getYear();
          otherVal = other.getYear();
          result = curVal.compareTo(otherVal);
          break;
        case MONTHS:
          if ((result = this.compareTo(other, Interval.YEARS)) == 0) {
            curVal = this.getMonth();
            otherVal = other.getMonth();
            result = curVal.compareTo(otherVal);
          }
          break; 
        case WEEKS:
          if ((result = this.compareTo(other, Interval.YEARS)) == 0) {
            curVal = this.getWeekofYear();
            otherVal = other.getWeekofYear();
            result = curVal.compareTo(otherVal);
          }
          break;          
        case DAYS:
          if ((result = this.compareTo(other, Interval.MONTHS)) == 0) {
            curVal = this.getDayOfMonth();
            otherVal = other.getDayOfMonth();
            result = curVal.compareTo(otherVal);
          }
          break;          
        case HOURS:          
          if ((result = this.compareTo(other, Interval.DAYS)) == 0) {
            curVal = this.getHour();
            otherVal = other.getHour();
            result = curVal.compareTo(otherVal);
          }
          break;          
        case MINUTES:          
          if ((result = this.compareTo(other, Interval.HOURS)) == 0) {
            curVal = this.getMinute();
            otherVal = other.getMinute();
            result = curVal.compareTo(otherVal);
          }
          break;         
        case SECONDS:          
          if ((result = this.compareTo(other, Interval.MINUTES)) == 0) {
            curVal = this.getSecond();
            otherVal = other.getSecond();
            result = curVal.compareTo(otherVal);
          }
          break;
      }
    }
    return result;
  }

  /**
   * Performs a chronological test on the two dates.
   * This returns
   * @param otherDt the DateTime to compare to
   * @return false if they are at the same time.  The equals
   * method can test for date equality.
   */
  public boolean isAfter(DateTime otherDt) {
    if (otherDt == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    return (this.compareTo(otherDt) > 0);
  }

  /**
   * Performs a chronological test on the two dates based on a specified <tt>interval</tt>.
   * This returns
   * @param otherDt the DateTime to compare to
   * @param interval the time interval for which to compare
   * @return false if they are at the same time.  The equals
   * method can test for date equality.
   */
  public boolean isAfter(DateTime otherDt, Interval interval) {
    if (otherDt == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    return (this.compareTo(otherDt, interval) > 0);
  }

  /**
   * Performs a chronological test on the two dates.
   * @param otherDt the DateTime to compare to
   * @return  true if the this date occurs before the other date, but
   * not if they are at the same time.
   */
  public boolean isBefore(DateTime otherDt) {
    if (otherDt == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    return (this.compareTo(otherDt) < 0);
  }

  /**
   * Performs a chronological test on the two dates based on a specified 
   * <tt>interval</tt>.
   * @param otherDt the DateTime to compare to
   * @param interval the time interval for which to compare
   * @return  true if the this date occurs before the other date, but
   * not if they are at the same time.
   */
  public boolean isBefore(DateTime otherDt, Interval interval) {
    if (otherDt == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    return (this.compareTo(otherDt, interval) < 0);
  }

  /**
   * Calculates the absolute difference between this DateTime and <tt>otherDt</tt> and
   * return the result in the units specified by <tt>outTimeUnit</tt>.
   * @param otherDt the other DateTime
   * @param outTimeUnit the output TimeUnit (assumed MILLISECONDS is unassigned)
   * @return the absolute difference between the two dates in milliseconds and return
   * the result converted to the specified <tt>outTimeUnit</tt> using the {@linkplain
   * TimeUnit#convert(long, java.util.concurrent.TimeUnit)} method.
   */
  public long diff(DateTime otherDt, TimeUnit outTimeUnit) {
    if (otherDt == null) {
      throw new NullPointerException("The date parameter cannot be null.");
    }
    if (outTimeUnit == null) {
      outTimeUnit = TimeUnit.MILLISECONDS;
    }
    long diff = 0l;
    if (outTimeUnit.equals(TimeUnit.DAYS)) {
      DateTime dt1 = DateTime.toZeroHourUTC(this);
      DateTime dt2 = DateTime.toZeroHourUTC(otherDt);
      diff = Math.abs(dt1.milliSeconds - dt2.milliSeconds);
    } else {
      diff = Math.abs(this.milliSeconds - otherDt.milliSeconds);
    }
    return (outTimeUnit == null)? diff: outTimeUnit.convert(diff, TimeUnit.MILLISECONDS);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Overload 1: True if pOther instance of a DateTime and 
   * the times are exactly the same down to the millisecond.</p>
   */
  @Override
  public boolean equals(Object other) {
    boolean result = false;
    if ((other != null) && (other instanceof DateTime)) {
      DateTime otherDt = (DateTime) other;
      result = (this.milliSeconds == otherDt.milliSeconds);
    }
    return result;
  }
  
  /**
   * Overload 2: A TimeZone sensitive comparison by <tt>interval</tt> of <tt>other</tt> 
   * with this instance's <tt>interval</tt> and return true if matched. It converts
   * <tt>other</tt> to a new DateTime in this.timeZone, and then calls {@linkplain 
   * #equals(bubblewrap.io.datetime.DateTime, bubblewrap.io.schedules.enums.Interval) 
   * Overload 3} to get the comparison.
   * @param other the other date 
   * @param interval the interval to compare
   * @param convertTimeZone true to convert <tt>other.timeZone</tt> to this.timeZone
   * before doing the comparison.
   * @return true if it is a match or false if not or other = null.
   */
  public boolean equals(DateTime other, Interval interval, boolean convertTimeZone) {    
    boolean result = false;
    if (other != null) {
      DateTime target = other;
      if ((convertTimeZone) && (!this.getTimeZone().equals(other.getTimeZone()))) {
        target = new DateTime(other.milliSeconds, this.getTimeZone());
      }
      result = this.equals(target, interval);
    }
    return result;
  }
  
  /**
   * Overload 3: Compare the specified <tt>interval</tt> of <tt>other</tt> with this 
   * instance's <tt>interval</tt> and return true if matched. This comparison ignore the 
   * TimeZone of this or the other date. To make the comparison TimeZone-sensitive, user 
   * {@linkplain #equals(bubblewrap.io.datetime.DateTime, 
   * bubblewrap.io.schedules.enums.Interval, boolean) Overload 2}.
   * <p>It <tt>interval</tt> = null | MILLISECONDS, call {@linkplain 
   * #equals(java.lang.Object) Overload 1}
   * @param other the other date 
   * @param interval the interval to compare
   * @return true if it is a match or false if not or other = null.
   */
  public boolean equals(DateTime other, Interval interval) {
    boolean result = false;
    if (other != null) {
      if ((interval == null) || (interval.equals(Interval.MILLISECONDS))) {
        result = this.equals(other);
      } else {
        switch (interval) {
          case WATERYEARS:
            Integer curWy = WyConverter.getWaterYear(this);
            Integer otherWy = WyConverter.getWaterYear(other);
            result = DataEntry.isEq(curWy, otherWy);
          case YEARS:
            result = (this.getYear() == other.getYear());
            break;
          case MONTHS:
            result = ((this.getYear() == other.getYear()) &&
                      (this.getMonth() == other.getMonth()));
            break; 
          case WEEKS:
            result = ((this.getYear() == other.getYear())  &&
                      (this.getWeekofYear() == other.getWeekofYear()));
            break;          
          case DAYS:
            result = ((this.getYear() == other.getYear()) &&
                      (this.getMonth() == other.getMonth()) &&
                      (this.getDayOfMonth() == other.getDayOfMonth()));
            break;          
          case HOURS:
            result = ((this.getYear() == other.getYear()) &&
                      (this.getMonth() == other.getMonth()) &&
                      (this.getDayOfMonth() == other.getDayOfMonth()) &&
                      (this.getHour()== other.getHour()));
            break;          
          case MINUTES:
            result = ((this.getYear() == other.getYear()) &&
                      (this.getMonth() == other.getMonth()) &&
                      (this.getDayOfMonth() == other.getDayOfMonth()) &&
                      (this.getHour()== other.getHour()) &&
                      (this.getMinute()== other.getMinute()));
            break;         
          case SECONDS:
            result = ((this.getYear() == other.getYear()) &&
                      (this.getMonth() == other.getMonth()) &&
                      (this.getDayOfMonth() == other.getDayOfMonth()) &&
                      (this.getHour()== other.getHour()) &&
                      (this.getMinute()== other.getMinute()) &&
                      (this.getSecond()== other.getSecond()));
            break;
        }
      }
    }
    return result;
  }

  /**
   * OVERRIDE: Return the hashcode for the DateTime instance
   * @return int
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + (int) (this.milliSeconds ^ (this.milliSeconds >>> 32));
    return hash;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return a clone of this DateTime</p>
   */
  @Override
  public DateTime clone()  {
    DateTime pClone = new DateTime();
    pClone.milliSeconds = this.milliSeconds;
    pClone.timeZone = this.timeZone;
    return pClone;
  }

  /**
   * OVERRIDE:  Generates an appropriate text string for database access. It return
   * the UTC time in Zulu Format (i.e. "yyyy-MM-ddTHH:mm:ssZ").
   */
  @Override
  public String toString() {
    String sResult = null;
    try {
      Date myDate = this.getAsDate();
      DateFormat pParser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
      pParser.setTimeZone(this.getTimeZone());
      sResult = pParser.format(myDate);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toString Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return sResult;
  }

  /**
   * Return a Date/Time Formatted string for this instance's date, Locale,
   * and TimeZone. iStyle specified the DateTime format (e.g., DateFormat.SHORT, MEDIUM,
   * ,LONG, or FULL).
   * @param dateStyle int (valid DateFormat constants)
   * @return String
   */
  public String toLocaleString(int dateStyle) {
    String sResult = null;
    try {
      DateFormat pParser = DateFormat.getDateInstance(dateStyle, this.getLocale());
      pParser.setTimeZone(this.getTimeZone());
      Date pDate = this.getAsDate();
      sResult = pParser.format(pDate);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toLocaleString Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return sResult;
  }


  /**
   * Return a Date/Time Formatted string for this instance's <tt>dateStyle</tt>,
   * <tt>timeStyle</tt>, Locale, and TimeZone. The specified dateStyle and tiemStyle
   * must be the DateTime constant (e.g., DateFormat.SHORT, MEDIUM, LONG, or FULL) or -1
   * to ignore this setting. if both values are -1, it sets both daetStyle and tiemStyle
   * = {@linkplain DateFormat#FULL}.
   * @param dateStyle a valid {@linkplain DateFormat} constants or -1 to ignore
   * @param timeStyle a valid {@linkplain DateFormat} constants or -1 to ignore
   * @return the formated date/time string
   */
  public String toLocaleString(int dateStyle, int timeStyle) {
    String result = null;
    try {
      DateFormat dtParser = null;
      if ((dateStyle >= 0) && (timeStyle >= 0)) {
        dtParser = DateFormat.getDateTimeInstance(dateStyle, timeStyle, this.getLocale());
      } else if (dateStyle >= 0) {
        dtParser = DateFormat.getDateInstance(dateStyle, this.getLocale());
      } else if (timeStyle >= 0) {
        dtParser = DateFormat.getTimeInstance(timeStyle, this.getLocale());
      } else {
        dateStyle = DateFormat.FULL;
        timeStyle = DateFormat.FULL;
        dtParser = DateFormat.getDateTimeInstance(dateStyle, timeStyle, this.getLocale());
      }

      dtParser.setTimeZone(this.getTimeZone());
      Date curDate = this.getAsDate();
      result = dtParser.format(curDate);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toLocaleString Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Return a Date/Time Formatted string for this instance's date, Locale,
   * and TimeZone. sFormat specified the DateTime format (e.g., "MM/dd/yyyy").
   * @param sFormat String (valid DateFormat format)
   * @return String
   */
  public String toLocaleString(String sFormat) {
    String sResult = null;
    try {
      sFormat = DataEntry.cleanString(sFormat);
      if (sFormat == null) {
        sFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
      }

      DateFormat pParser = new SimpleDateFormat(sFormat);
      pParser.setTimeZone(this.getTimeZone());
      Date pDate = this.getAsDate();
      sResult = pParser.format(pDate);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toLocaleString Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return sResult;
  }
  // </editor-fold>
}
