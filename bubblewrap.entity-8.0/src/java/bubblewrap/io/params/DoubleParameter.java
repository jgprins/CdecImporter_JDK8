package bubblewrap.io.params;

import java.util.Scanner;
import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;

/**
 * A Parameter class the store all values as Double. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class DoubleParameter extends Parameter<Double> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If sInStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. All values
   * are assigned as sting and will be casted on retrieval using Parameter.getNumberValue.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static DoubleParameter parse(String inputStr) throws Exception {
    DoubleParameter result = null;
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
          Double dblVal = null;
          if (value != null) {
            dblVal = DataConverter.toValue(value, Double.class);            
          }
          result = new DoubleParameter(key, dblVal, null);
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
  public DoubleParameter(Param annotation) {
    super(annotation);        
  }
  /**
   * Public Constructor  
   */
  public DoubleParameter(String key, Double value, String format) {
    super(key,value,format);        
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Parameter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert strValue to a Double Value (or null if empty|"")</p>
   */
  @Override
  protected Double onParse(String strValue) {
    strValue = DataEntry.cleanString(strValue);
    Double result = null;
    if (strValue != null) {
      result = DataConverter.toValue(strValue, Double.class);      
    }
    return result;
  }
  // </editor-fold>
}
