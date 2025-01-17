package gov.ca.water.cdec.importers;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ImportDay {
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Initiate a new ImportDay based on the specified <tt>date</tt>
   * @param date the date to convert (assumed today if null)
   * @return the new instance
   */
  public static ImportDay fromDate(Date date) {
    ImportDay result = null;
    try {
      if (date == null) {
        date = Calendar.getInstance().getTime();
      }
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(date.getTime());
      int year = cal.get(Calendar.YEAR);
      int month = cal.get(Calendar.MONTH);
      int day = cal.get(Calendar.DAY_OF_MONTH);
      
      result = new ImportDay(year, month, day);
    } catch (Exception exp) {
      throw new IllegalArgumentException(ImportDay.class.getSimpleName()
              + ".fromDate Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Initiate a new ImportDay for the current date
   * @return the new instance
   */
  public static ImportDay toDay() {
    Date today = Calendar.getInstance().getTime();
    return ImportDay.fromDate(today);
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Day's Year (1900..)
   */
  public final int year ;
  /**
   * The Day's month [0..11]
   */
  public final int month;
  /**
   * The Day's day-of-month (1 based)
   */
  public final int day;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ImportDay(int year, int month, int day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the ImportDay as Date
   * @return the Date value
   */
  public Date asDate() {
    Calendar cal = Calendar.getInstance();
    cal.set(this.year, this.month, this.day, 0, 0, 0);
    return cal.getTime();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: this.year + "-" + this.month + "-" + this.day</p>
   */
  @Override
  public String toString() {
    return this.year + "-" + this.month + "-" + this.day;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a hashCode on this.year, month, and day.</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 37 * hash + this.year;
    hash = 37 * hash + this.month;
    hash = 37 * hash + this.day;
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return true if (obj instance of ImportDay) and this.year, this.month, and
   * this.day math the <tt>obj</tt> properties</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof ImportDay));
    if (result) {
      ImportDay other = (ImportDay) obj;
      result = ((this.year == other.year) &&
                (this.month == other.month) &&
                (this.day == other.day));
    }
    return result;
  }
  // </editor-fold>
}
