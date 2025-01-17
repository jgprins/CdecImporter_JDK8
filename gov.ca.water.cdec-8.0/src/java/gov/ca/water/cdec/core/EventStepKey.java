package gov.ca.water.cdec.core;

import gov.ca.water.cdec.enums.EventStep;
import java.util.Calendar;
import java.util.Date;

/**
 * A TimeStamp key used by the TimeSeriesMap and other methods to represents a TimeZone
 * indifferent Time Stamp
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class EventStepKey extends TimeStepKey<EventStepKey> {

  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Key's Year value
   */
  public final int year;
  /**
   * The Key's Month value
   */
  public final int month;
  /**
   * The Key's Day value
   */
  public final int day;
  /**
   * The Key's Hour (of the day - 24-hour) value
   */
  public final int hour;
  /**
   * The Key's minute value
   */
  public final int minute;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public EventStepKey(Date actualDt, EventStep eventStep) {
    super(actualDt);
    Calendar cal = Calendar.getInstance();
    cal.setTime(actualDt);
    this.year = cal.get(Calendar.YEAR);
    this.month = cal.get(Calendar.MONTH);
    this.day = cal.get(Calendar.DAY_OF_MONTH);
    switch (eventStep) {
      case MINUTES:
        this.minute = cal.get(Calendar.MINUTE);
        this.hour = cal.get(Calendar.HOUR_OF_DAY);        
        break;
      case HOURS:
        this.hour = cal.get(Calendar.HOUR_OF_DAY);
        this.minute = 0;
        break;
      default:
        this.hour = 0;
        this.minute = 0;
    }
  }
    // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether this Date is before <tt>other</tt>
   * @param other
   * @return ((other != null) && ((this.year &lt; other.year) ||
   * ((this.year == other.year) && (this.month &lt; other.month)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day &lt; other.day))))
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day == other.day) && (this.hour &lt; other.hour)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day == other.day) && (this.hour == other.hour) &&
   * (this.minute &lt; other.minute))))
   */
  @Override
  public boolean isBefore(EventStepKey other) {
    return ((other != null) && ((this.year < other.year) ||
           ((this.year == other.year) && (this.month < other.month)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day < other.day)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day == other.day) && (this.hour < other.hour)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day == other.day) && (this.hour == other.hour) &&
            (this.minute < other.minute))));
  }
  /**
   * Get whether this Date is after <tt>other</tt>
   * @param other
   * @return ((other != null) && ((this.year &gt; other.year) ||
   * ((this.year == other.year) && (this.month &gt; other.month)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day &gt; other.day)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day == other.day) && (this.hour &gt; other.hour)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day == other.day) && (this.hour == other.hour) &&
   * (this.minute &gt; other.minute))))
   */
  @Override
  public boolean isAfter(EventStepKey other) {
    return ((other != null) && ((this.year > other.year) ||
           ((this.year == other.year) && (this.month > other.month)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day > other.day)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day == other.day) && (this.hour > other.hour)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day == other.day) && (this.hour == other.hour) &&
            (this.minute > other.minute))));
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return calendar(this.year,this.month,this.day,this.hour,this.minute).date
   * </p>
   */
  @Override
  public Date getDate() {
    Date result = null;
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.YEAR, this.year);
      calendar.set(Calendar.MONTH, this.month); 
      calendar.set(Calendar.DAY_OF_MONTH, this.day);
      calendar.set(Calendar.HOUR_OF_DAY, this.hour);
      calendar.set(Calendar.MINUTE, this.minute);
      calendar.set(Calendar.SECOND, 0);
      calendar.set(Calendar.MILLISECOND, 0);
      result = calendar.getTime();
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() 
              + ".getDate Error: \n\t" + exp.getMessage());
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return (((obj != null) && (obj instanceof EventStepKey)) and 
   * this.Year, month, day, hour, and minute match others.year, month, day, hour, 
   * and minute.</p>
   */
  @SuppressWarnings("checked")
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EventStepKey));
    if (result) {
      EventStepKey other = (EventStepKey) obj;
      result = ((this.year == other.year)
              && (this.month == other.month)
              && (this.day == other.day)
              && (this.hour == other.hour)
              && (this.minute == other.minute));
    }
    return result;
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a HashCode based in this.year, month, day, hour, and minute</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + this.year;
    hash = 47 * hash + this.month;
    hash = 47 * hash + this.day;
    hash = 47 * hash + this.hour;
    hash = 47 * hash + this.minute;
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return "{yr:" + this.year + "; mon:" + this.month + "; day:" + this.day
   * + "; hour:" + this.hour + "; minute:" + this.minute+ "}"</p>
   */
  @Override
  public String toString() {
    return "{yr:" + this.year + "; mon:" + this.month + "; day:" + this.day 
            + "; hour:" + this.hour + "; minute:" + this.minute + "}";
  }
  //</editor-fold>
}
