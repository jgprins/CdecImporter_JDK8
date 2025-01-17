package bubblewrap.io.params;

import java.util.Scanner;
import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Integer. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class IntegerParameter extends Parameter<Integer> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If sInStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. All values
   * are assigned as sting and will be casted on retrieval using Parameter.getNumberValue.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static IntegerParameter parse(String inputStr) throws Exception {
    IntegerParameter result = null;
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
          Integer intVal = null;
          if (value != null) {
            intVal = DataConverter.toValue(value, Integer.class);            
          }
          result = new IntegerParameter(key, intVal, null);
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
  public IntegerParameter(Param annotation) {
    super(annotation);        
  }
  /**
   * Public Constructor  
   */
  public IntegerParameter(String key, Integer value, String format) {
    super(key,value,format);        
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Parameter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert strValue to a Integer Value (or null if empty|"")</p>
   */
  @Override
  protected Integer onParse(String strValue) {
    strValue = DataEntry.cleanString(strValue);
    Integer result = null;
    if (strValue != null) {
      result = DataConverter.toValue(strValue, Integer.class);      
    }
    return result;
  }
  // </editor-fold>
}
