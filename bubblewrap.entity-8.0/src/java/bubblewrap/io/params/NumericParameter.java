package bubblewrap.io.params;

import bubblewrap.core.annotations.Param;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Double. The Double values can be converted
 * to any numeric, string, or boolean values using the cast methods.
 * @author kprins
 */
public class NumericParameter extends Parameter<Double> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static {@link #fromAnnotation(bubblewrap.core.annotations.Param) Parameter.fromAnnotation} method
   * @param annotation the parameter annotation
   */
  public NumericParameter(Param annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public NumericParameter(String key, Double value) {
    super(key,value);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Parameter Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: DataConverter.toValue(strVal, Double.class)</p>
   */
  @Override
  protected Double onParse(String strVal) throws Exception {
    return DataConverter.toValue(strVal, Double.class);
  }
  //</editor-fold>
}
