package bubblewrap.io.datetime;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Java Calendar can track calculations in terms of milliseconds from
 * Jan 1, 1970.  Since a .Net DateTimeSpan doesn't exist in java, this class will
 * serve as a stand in to convert a user entered time span in terms of
 * Days, Hours, Minutes, Seconds and Milliseconds into totalMilliseconds.
 * Created on Nov 9, 2010 at 3:07:07 PM
 * @author Harold A. Dunsford Jr. Ph.D. and K.Prins
 * 
 */
public class DateTimeSpan implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Creates an appropriate DateTimeSpan from the specified total milliseconds.
   * @param milliseconds  The total number of milliseconds in the DateTimeSpan.
   * @return A DateTimeSpan class measuring the total milliseconds.
   */
  public static DateTimeSpan FromTotalMilliseconds(long milliseconds) {
    return new DateTimeSpan(milliseconds);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the DateTimeSpan in milli-seconds
   */
  private long mlMilliSeconds = 0;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Parameterless constructor
   */
  public DateTimeSpan() {
  }

  /**
   * Constructs a new time span using the milliseconds specified explicitly.
   * @param pMilliSecons long
   */
  public DateTimeSpan(long pMilliSecons) {
    this.mlMilliSeconds = pMilliSecons;
  }

  /**
   * Defines a new span of time based on integer units from each of the
   * members.
   * @param days int - The days part of the time span
   * @param hours int - The hours part of the time span
   * @param minutes int - The Minutes part of the time span
   * @param seconds int - The seconds part of the time span
   * @param milliseconds int - the milliseconds part of the time span
   */
  public DateTimeSpan(int days, int hours, int minutes, int seconds,
          int milliseconds) {
    this.mlMilliSeconds = milliseconds + seconds * 1000 + minutes * 60000
            + hours * 3600000 + days * 86400000;
  }

  /**
   * Defines a new time span using the millisecond separation between two
   * dates.
   * @param startDate The start date of the DateTimeSpan.
   * @param endDate The end date of the DateTimeSpan.
   */
  public DateTimeSpan(DateTime startDate, DateTime endDate) {
    if (startDate == null) {
      throw new NullPointerException("The startDate parameter cannot be null.");
    }
    if (endDate == null) {
      throw new NullPointerException("The endDate parameter cannot be null.");
    }
    this.mlMilliSeconds = startDate.getTotalMilliseconds()
            - endDate.getTotalMilliseconds();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Gets the long number of total milliseconds in the DateTimeSpan.
   * @return long
   */
  public long getTotalMilliseconds() {
    return this.mlMilliSeconds;
  }

  /**
   * Sets the DateTimeSpan in milliseconds
   * @param value long
   */
  public void setTotalMilliseconds(long value) {
    this.mlMilliSeconds = value;
  }

  /**
   * Gets the number of hours in the DateTimeSpan.
   * @return the hours as a double number.
   */
  public double getTotalHours() {
    Long lTime = Long.valueOf(this.mlMilliSeconds);
    Double dTime = lTime.doubleValue();
    return dTime / (1000 * 60 * 60);
  }

  /**
   * Gets the double number of total days in the time span.
   * @return the days as a double number.
   */
  public double getTotalDays() {
    Long lTime = Long.valueOf(this.mlMilliSeconds);
    Double dTime = lTime.doubleValue();
    return dTime / (1000 * 60 * 60 * 24);
  }

  /**
   * Gets the number of of full days in the time span.
   * @return the days as a integer (rounded down).
   */
  public int getFullDays() {
    Double dDays = Math.floor(this.getTotalDays());    
    return dDays.intValue();
  }

  /**
   * Adds this time span to the specified Calendar.  We would just add total
   * milliseconds, but the 32 bit integer input parameter can only handle
   * a maximum of about 23 days worth.  Therefore we add days and the
   * milliseconds term for that day separately.
   * @param pCalendar Calendar
   */
  void addToDate(Calendar pCalendar) {
    if (pCalendar == null) {
      throw new NullPointerException("The Calendar parameter cannot be null.");
    }

    long lTotTime = pCalendar.getTimeInMillis();
    lTotTime += this.mlMilliSeconds;
    pCalendar.setTimeInMillis(lTotTime);
  }

  /**
   * Returns a DateTimeSpan that is 1/2 this time span.
   * @return DateTimeSpan.
   */
  public DateTimeSpan halfSpan() {
    return new DateTimeSpan(mlMilliSeconds / 2);
  }

  /**
   * Returns a DateTimeSpan that is twice this time span.
   * @return DateTimeSpan
   */
  public DateTimeSpan doubleSpan() {
    return new DateTimeSpan(mlMilliSeconds * 2);
  }

  /**
   * Compares this instance to the other DateTimeSpan, and returns true if
   * this DateTimeSpan has a larger number of milliseconds.
   * @param pOther DateTimeSpan
   * @return Boolean
   */
  public boolean isLongerThan(DateTimeSpan pOther) {
    if (pOther == null) {
      throw new NullPointerException("TimeSpan cannot be null.");
    }
    return (this.mlMilliSeconds > pOther.getTotalMilliseconds());
  }

  /**
   * Compares this instance to the other DateTimeSpan, and returns true
   * if the DateTimeSpan has a larger number of milliseconds
   * @param pOther DateTimeSpan
   * @return boolean
   */
  public boolean isShorterThan(DateTimeSpan pOther) {
    if (pOther == null) {
      throw new NullPointerException("TimeSpan cannot be null.");
    }
    return (mlMilliSeconds < pOther.getTotalMilliseconds());
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * OVERRIDES: Override equals to support the idea of two TimeSpans being
   * equal as long as the underlying milliseconds are the same.
   * The == operator will still always use a reference equality.
   * @param pOther Object
   * @return boolean
   */
  @Override
  public boolean equals(Object pOther) {
    // Identity is always equal
    boolean bIsEqual = (pOther == this);
    if ((!bIsEqual) && (pOther != null) && (pOther instanceof DateTimeSpan)) {
      DateTimeSpan ts = (DateTimeSpan) pOther;
      bIsEqual = (this.mlMilliSeconds == ts.getTotalMilliseconds());
    }
    return bIsEqual;
  }

  /**
   * OVERRIDES: Return the Total Milliseconds as a String
   * @return
   */
  @Override
  public String toString() {
    return Long.toString(this.mlMilliSeconds);
  }

  /**
   * OVERRIDES:  This method is not responsible for
   * producing unique values for each possible DateTimeSpan, that would
   * be impossible.  Instead this tries to spread timeSpan values
   * out so that time spans in the most used range, (spans from
   * .1 second to 6 years are uniquely spread out, but performance
   * on TimeSpans separated by a few milliseconds will get lumped
   * together in hash tables, and the hash table indexing wraps
   * around once you get to 6.8 years.
   * @return int
   */
  @Override
  public int hashCode() {
    return (int) ((this.mlMilliSeconds / 100) % Integer.MAX_VALUE);
  }
  // </editor-fold>
}
