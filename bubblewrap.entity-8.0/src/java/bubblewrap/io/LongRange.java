package bubblewrap.io;

import java.util.Collection;

/**
 * A {@linkplain ValueRange> for double values
 * @author kprins
 */
public class LongRange extends ValueRange<Long> {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  public LongRange() {
    super();
  }
  
  /**
   * Public Constructor
   */
  public LongRange(Long...values) {
    super(values);
  }
  
  /**
   * Public Constructor
   */
  public LongRange(Collection<Long> values) {
    super(values);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return the inRange test results for a integer value
   * @param value int value
   * @return true is in range
   */
  public boolean inRange(long value) {
    Long intVal = value;
    return super.inRange(intVal);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override ValueRange">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (value != null)</p>
   */
  @Override
  protected boolean isValidValue(Long value) {
    return (value != null);
  }
  //</editor-fold>
  
}
