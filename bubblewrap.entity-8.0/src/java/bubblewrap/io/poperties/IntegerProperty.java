package bubblewrap.io.poperties;

import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.poperties.annotation.PropertyDef;

/**
 * A Property class the store all values as Integer. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class IntegerProperty extends Property<Integer> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static {@link #initParam(bubblewrap.core.annotations.Param) Property.initProperty} method
   * @param annotation the parameter annotation
   */
  public IntegerProperty(PropertyDef annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public IntegerProperty(String key, Integer value) {
    super(key,value);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Property Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return DataConverter.toValue(strVal, Integer.class)</p>
   */
  @Override
  protected Integer onParse(String strVal) throws Exception {
    return DataConverter.toValue(strVal, Integer.class);
  }
  //</editor-fold>
}
