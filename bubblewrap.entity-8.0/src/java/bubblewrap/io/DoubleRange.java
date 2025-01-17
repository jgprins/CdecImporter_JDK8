package bubblewrap.io;

import bubblewrap.io.converters.DataConverter;
import java.util.Collection;
import java.util.Iterator;

/**
 * A {@linkplain ValueRange> for Double values
 * @author kprins
 */
public class DoubleRange extends ValueRange<Double> {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public DoubleRange() {
    super();
  }
  
  /**
   * Public Constructor
   */
  public DoubleRange(Double...values) {
    super(values);
  }
  
  /**
   * Public Constructor
   */
  public DoubleRange(Collection<Double> values) {
    super(values);
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return the for a double value
   * @param value double
   * @return true is in range
   */
  public boolean inRange(double value) {
    Double dblVal = value;
    return super.inRange(dblVal);
  }
  
  /**
   * Return the for a double value
   * @param value int value
   * @return true is in range
   */
  public boolean inRange(int value) {
    Integer intVal = value;
    Double dblVal = DataConverter.convertTo(intVal, Double.class);
    return super.inRange(dblVal);
  }
  
  /**
   * Return the for a double value
   * @param value Integer
   * @return true is in range
   */
  public boolean inRange(Integer value) {
    Double dblVal = DataConverter.convertTo(value, Double.class);
    return super.inRange(dblVal);
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Override ValueRange">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: ((value != null) && (!value.isNaN())) allow {@linkplain 
   * Double#NEGATIVE_INFINITY} and {@linkplain Double#POSITIVE_INFINITY}</p>
   */
  @Override
  protected boolean isValidValue(Double value) {
    return ((value != null) && (!value.isNaN()));
  }
  //</editor-fold>
}
