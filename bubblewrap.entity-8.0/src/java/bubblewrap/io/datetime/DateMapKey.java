package bubblewrap.io.datetime;

import bubblewrap.io.schedules.enums.Interval;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Map Key that can be used for Maps that use a DateTime value as a key. The
 * DateMapKey requires an {@linkplain Interval} to compare two keys or a DateTime value
 * with a DateMapKey. When using a DateMapKey in a Map, a dateValue can be used to
 * retrieve the matching value.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateMapKey implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The MapKey's DateValue
   */
  public DateTime dateTime;
  /**
   * The Interval used in Date Comparisons
   */
  public Interval interval;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   * @param dateTime
   * @param interval
   */
  public DateMapKey(DateTime dateTime, Interval interval) {
    super();
    if ((this.dateTime = dateTime) == null) {
      throw new NullPointerException("The MapKey's DateTime value cannot be null");
    }
    if ((this.interval = interval) == null) {
      throw new NullPointerException("The MapKey's Interval cannot be null");
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE:  "DateMapKey[ date=" + this.dateTime.toString()
   * + "; interval=" + this.interval.label + "]"</p>
   */
  @Override
  public String toString() {
    return "DateMapKey[ date=" + this.dateTime.toString()
            + "; interval=" + this.interval.label + "]";
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return (obj != null) and ((obj instance of DateMapKey) and this.dateTime
   * matches obj.dateTime using this.interval for comparison) or (obj instance of
   * DateTime) and this.dateTime matches obj using this.interval for comparison</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = (obj != null);
    if (result) {
      if (obj instanceof DateMapKey){
        DateMapKey other = (DateMapKey) obj;
        result = this.dateTime.equals(other.dateTime, this.interval);
      } else  if (obj instanceof DateTime) {
        DateTime other = (DateTime) obj;
        result = this.dateTime.equals(other, this.interval);
      } else {
        result = false;
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a hashCode based on this.dateTime</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 97 * hash + Objects.hashCode(this.dateTime);
    return hash;
  }
  // </editor-fold>
}
