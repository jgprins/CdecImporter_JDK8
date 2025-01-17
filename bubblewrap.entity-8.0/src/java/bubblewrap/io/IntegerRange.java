package bubblewrap.io;

import java.util.Collection;

/**
 * A {@linkplain ValueRange> for double values
 * @author kprins
 */
public class IntegerRange extends ValueRange<Integer> {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  public IntegerRange() {
    super();
  }
  
  /**
   * Public Constructor
   */
  public IntegerRange(Integer...values) {
    super(values);
  }
  
  /**
   * Public Constructor
   */
  public IntegerRange(Collection<Integer> values) {
    super(values);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return the inRange test results for a integer value
   * @param value int value
   * @return true is in range
   */
  public boolean inRange(int value) {
    Integer intVal = value;
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
  protected boolean isValidValue(Integer value) {
    return (value != null);
  }
  //</editor-fold>
  
}
