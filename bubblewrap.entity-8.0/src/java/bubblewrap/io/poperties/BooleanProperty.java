package bubblewrap.io.poperties;

import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.poperties.annotation.PropertyDef;

/**
 * A Property class the store all values as Boolean. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class BooleanProperty extends Property<Boolean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static 
   * {@link #initParam(bubblewrap.core.annotations.Param) Property.initProperty} method
   * @param annotation the parameter annotation
   */
  public BooleanProperty(PropertyDef annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public BooleanProperty(String key, Boolean value) {
    super(key,value);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Property Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return DataConverter.toBoolean(strVal)</p>
   */
  @Override
  protected Boolean onParse(String strVal) throws Exception {
    return DataConverter.toBoolean(strVal);
  }
  //</editor-fold>
}
