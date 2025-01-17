package bubblewrap.io.poperties;

import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.poperties.annotation.PropertyDef;

/**
 * A Property class the store all values as Double. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class DoubleProperty extends Property<Double> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static 
   * {@link #initParam(bubblewrap.core.annotations.Param) Property.initProperty} method
   * @param annotation the parameter annotation
   */
  public DoubleProperty(PropertyDef annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public DoubleProperty(String key, Double value) {
    super(key,value);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Property Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return DataConverter.toValue(strVal, Double.class)</p>
   */
  @Override
  protected Double onParse(String strVal) throws Exception {
    return DataConverter.toValue(strVal, Double.class);
  }
  //</editor-fold>
}
