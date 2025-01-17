package bubblewrap.io.params;

import bubblewrap.core.annotations.Param;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Boolean. The Boolean values can be 
 * converted to any string, or boolean values using the cast methods.
 * @author kprins
 */
public class BooleanParameter extends Parameter<Boolean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static {@link #fromAnnotation(bubblewrap.core.annotations.Param) Parameter.fromAnnotation} method
   * @param annotation the parameter annotation
   */
  public BooleanParameter(Param annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public BooleanParameter(String key, Boolean value) {
    super(key,value);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Parameter Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: DataConverter.toBoolean(strVal)</p>
   */
  @Override
  protected Boolean onParse(String strVal) throws Exception {
    return DataConverter.toBoolean(strVal);
  }
  //</editor-fold>
}
