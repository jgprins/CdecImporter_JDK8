package bubblewrap.io.params;

import java.util.Scanner;
import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Short. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class ShortParameter extends Parameter<Short> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If sInStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. All values
   * are assigned as sting and will be casted on retrieval using Parameter.getNumberValue.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static ShortParameter parse(String inputStr) throws Exception {
    ShortParameter result = null;
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
          Short numVal = null;
          if (value != null) {
            numVal = DataConverter.toValue(value, Short.class);            
          }
          result = new ShortParameter(key, numVal, null);
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
  public ShortParameter(Param annotation) {
    super(annotation);        
  }
  /**
   * Public Constructor  
   */
  public ShortParameter(String key, Short value, String format) {
    super(key, value, format);        
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Parameter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert strValue to a Short Value (or null if empty|"")</p>
   */
  @Override
  protected Short onParse(String strValue) {
    strValue = DataEntry.cleanString(strValue);
    Short result = null;
    if (strValue != null) {
      result = DataConverter.toValue(strValue, Short.class);      
    }
    return result;
  }
  // </editor-fold>
}
