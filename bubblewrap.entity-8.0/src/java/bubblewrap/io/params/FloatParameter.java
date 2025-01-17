package bubblewrap.io.params;

import java.util.Scanner;
import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Float. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class FloatParameter extends Parameter<Float> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If sInStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. All values
   * are assigned as sting and will be casted on retrieval using Parameter.getNumberValue.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static FloatParameter parse(String inputStr) throws Exception {
    FloatParameter result = null;
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        String key = null;
        String value = "";
        Scanner strScanner = new Scanner(inputStr);
        strScanner.useDelimiter("=");
        while (strScanner.hasNext()) {
          if (key == null) {
            key = strScanner.next();
          } else {
            value += strScanner.next();
          }
        }
        
        key = DataEntry.cleanString(key);
        if (key != null) {
          value = DataEntry.cleanString(value);
          Float numVal = null;
          if (value != null) {
            numVal = DataConverter.toValue(value, Float.class);            
          }
          result = new FloatParameter(key, numVal, null);
        }
      }
    } catch (Exception pExp) {
      result = null;
    }
    return result;
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FloatParameter(Param annotation) {
    super(annotation);        
  }
  /**
   * Public Constructor  
   */
  public FloatParameter(String key, Float value, String format) {
    super(key,value);        
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Parameter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert strValue to a Double Value (or null if empty|"")</p>
   */
  @Override
  protected Float onParse(String strValue) {
    strValue = DataEntry.cleanString(strValue);
    Float result = null;
    if (strValue != null) {
      result = DataConverter.toValue(strValue, Float.class);      
    }
    return result;
  }
  // </editor-fold>
}
