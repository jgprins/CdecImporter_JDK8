package gov.ca.water.cdec.core;

import java.util.Calendar;
import java.util.Date;

/**
 * A Date key used by the TimeSeriesMap and other methods to represents a TimeZone
 * indifferent Day Stamp
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateKey extends TimeStepKey<DateKey> {

  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Key's Year value
   */
  public final int year;
  /**
   * The Key's Month value (0 based)
   */
  public final int month;
  /**
   * The Key's Day value
   */
  public final int day;
    //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public DateKey(Date actualDt) {
    super(actualDt);
    Calendar cal = Calendar.getInstance();
    cal.setTime(actualDt);
    this.year = cal.get(Calendar.YEAR);
    this.month = cal.get(Calendar.MONTH);
    this.day = cal.get(Calendar.DAY_OF_MONTH);
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="TimeStepKey Overrides">
  /**
   * Get whether this Date is before <tt>other</tt>
   * @param other
   * @return ((other != null) && ((this.year &lt; other.year) ||
   * ((this.year == other.year) && (this.month &lt; other.month)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day &lt; other.day))))
   */
  @Override
  public boolean isBefore(DateKey other) {
    return ((other != null) && ((this.year < other.year) ||
           ((this.year == other.year) && (this.month < other.month)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day < other.day))));
  }
  /**
   * Get whether this Date is after <tt>other</tt>
   * @param other
   * @return ((other != null) && ((this.year &gt; other.year) ||
   * ((this.year == other.year) && (this.month &gt; other.month)) ||
   * ((this.year == other.year) && (this.month == other.month) && 
   * (this.day &gt; other.day))))
   */
  @Override
  public boolean isAfter(DateKey other) {
    return ((other != null) && ((this.year > other.year) ||
           ((this.year == other.year) && (this.month > other.month)) ||
           ((this.year == other.year) && (this.month == other.month) && 
            (this.day > other.day))));
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return calendar(this.year,this.month,this.day).date</p>
   */
  @Override
  public Date getDate() {
    Date result = null;
    try {
      Calendar calendar = Calendar.getInstance();
      calendar.set(Calendar.YEAR, this.year);
      calendar.set(Calendar.MONTH, this.month); 
      calendar.set(Calendar.DAY_OF_MONTH, this.day);
      calendar.set(Calendar.HOUR_OF_DAY, 0);
      calendar.set(Calendar.MINUTE, 0);
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
   * OVERRIDE: Return (((obj != null) && (obj instanceof DateKey)) and this.Year, month,
   * and day match others.year, month, and day.</p>
   */
  @SuppressWarnings("checked")
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof DateKey));
    if (result) {
      DateKey other = (DateKey) obj;
      result = ((this.year == other.year) &&
                (this.month == other.month) &&
                (this.day == other.day));
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return "{yr:" + this.year + "; mon:" + this.month + "; day:" + this.day
   * + "}"</p>
   */
  @Override
  public String toString() {
    return "{yr:" + this.year + "; mon:" + this.month + "; day:" + this.day + "}";
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a HashCode based in this.year, month, and day</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 61 * hash + this.year;
    hash = 61 * hash + this.month;
    hash = 61 * hash + this.day;
    return hash;
  }
  //</editor-fold>
}
