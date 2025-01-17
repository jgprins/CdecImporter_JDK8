package gov.ca.water.cdec.core;

import java.io.Serializable;
import java.util.Date;

/**
 * A Date key used by the TimeSeriesMap and other methods to represents a TimeZone
 * indifferent Day Stamp
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeStepKey<TKey extends TimeStepKey> 
                                              implements Serializable, Comparable<TKey> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  protected TimeStepKey(Date actualDt) {
    super();
    if (actualDt == null) {
      throw new NullPointerException("The TimeStepKey's Actual Date cannot be "
              + "uanssigned.");
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether this Date is before <tt>other</tt>
   * @param other the TimeStepKey to compare with
   * @return true is <tt>other</tt> !=null and is BEFOTE <tt>this</tt> TimeStep
   */
  public abstract boolean isBefore(TKey other);
  /**
   * Get whether this Date is after <tt>other</tt>
   * @param other the TimeStepKey to compare with
   * @return true is <tt>other</tt> !=null and is AFTER <tt>this</tt> TimeStep
   */
  public abstract boolean isAfter(TKey other);
  /**
   * Get the Data represented by the the TiemStepKey using the local's Calendar and 
   * TimeZone.
   * @return the actual date build from the TimeStepKey's settings
   */
  public abstract Date getDate();
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Comparable">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if (this.equals(other); return 0; if (other == null) | this.isBefore(other)
   * return -1, else return 1</p>
   */
  @Override
  public int compareTo(TKey other) {
    int result = 0;
    if (!this.equals(other)) {
      if (other == null) {
        result = -1;
      } else if (this.isBefore(other)) {
        result = -1;
      } else  {
        result = 1;
      }
    }
    return result;
  }
  //</editor-fold>
}
