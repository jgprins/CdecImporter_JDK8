package bubblewrap.io.poperties;

import bubblewrap.io.*;
import bubblewrap.io.poperties.annotation.PropertyDef;

/**
 * A Property class the store all values as String. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class StringProperty extends Property<String> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The Annotation based constructor called by the static {@link 
   * #initParam(bubblewrap.core.annotations.Param) Property.initProperty} method
   * @param annotation the parameter annotation
   */
  public StringProperty(PropertyDef annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public StringProperty(String key, String value) {
    super(key,DataEntry.cleanString(value));        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Property Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return strVal unchanged</p>
   */
  @Override
  protected String onParse(String strVal) throws Exception {
    return strVal;
  }
  //</editor-fold>
}
