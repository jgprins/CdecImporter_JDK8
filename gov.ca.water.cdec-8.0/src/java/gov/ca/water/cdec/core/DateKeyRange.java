package gov.ca.water.cdec.core;

import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateKeyRange {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Minimum Date
   */
  private DateKey min;
  /**
   * The Maximum Date
   */
  private DateKey max;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public DateKeyRange() {
    super();  
    this.min = null;
    this.max = null;
  }
  /**
   * Public Constructor with an array of DateKeys  
   */
  public DateKeyRange(DateKey...dates) {
    super();  
    this.grow(dates);
  }
  /**
   * Public Constructor with an array of Dates
   */
  public DateKeyRange(Date...dates) {
    super();  
    this.grow(dates);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Check if the range values if defined.
   * @return ((this.min == null) || (this.max == null))
   */
  public final boolean isNull() {
    return ((this.min == null) || (this.max == null));
  }
  /**
   * Check if the range values if defined or the range is only one day
   * @return (this.isNull | (this.min.equals(this.max)))
   */
  public final boolean isEmpty() {
    return ((this.min == null) || (this.max == null) || (this.min.equals(this.max)));
  }
  /**
   * Get the Minimum value of the range
   * @return this.min (can be null if this.isNull)
   */
  public final DateKey getMin() {
    return this.min;
  }
  /**
   * Get the Maximum value of the range
   * @return this.max (can be null if this.isNull)
   */
  public final DateKey getMax() {
    return this.max;
  }
  
  /**
   * Grow the range based on the <tt>dates</tt> passed in. Ignored if dates = null|empty
   * @param dates the array of dates
   */
  public final void grow(Date...dates) {
    if ((dates != null) && (dates.length > 0)) {
      DateKey key = null;
      for (Date date : dates) {
        if (((date != null) && (key = new DateKey(date)) != null)) {
          this.grow(key);
        }
      }
    }
  }
  
  /**
   * Grow the range based on the <tt>dates</tt> passed in. Ignored if dates = null|empty
   * @param dates the array of DateKeys
   */      
  public final void grow(DateKey...dates) {
    if ((dates != null) && (dates.length > 0)) {
      for (DateKey date : dates) {
        if (date == null) {
          continue;
        }
        
        if (this.isNull()) {
          this.min = date; 
          this.max = date; 
        } else if (date.isBefore(this.min)) {
          this.min = date; 
        } else if (this.max.isBefore(date)) {
          this.max = date; 
        }
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "DateKeyRange[ min=" + this.min + "; max=" + this.max + "]."</p>
   */
  @Override
  public String toString() {
    return "DateKeyRange[ min=" + this.min + "; max=" + this.max + "].";
  }
  // </editor-fold>
}
