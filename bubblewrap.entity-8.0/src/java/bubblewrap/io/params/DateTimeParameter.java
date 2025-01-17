package bubblewrap.io.params;

import java.util.Scanner;
import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;

/**
 * A Parameter class the store all values as DateTime. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class DateTimeParameter extends Parameter<DateTime> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If inputStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. The value
   * is converted to a DateTime calling the static {@linkplain DateTime#FromString(
   * java.lang.String) FromString(instr)} method, which assumed the data is in
   * "yyyy-MM-ddTHH:mm:ssZ" format.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static DateTimeParameter parse(String inputStr) throws Exception {
    DateTimeParameter result = null;
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
          value = DataEntry.cleanString(value, true);
          DateTime date = null;
          if (value != null) {
            date = DateTime.FromString(value);            
          }
          result = new DateTimeParameter(key, date, null);
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
  public DateTimeParameter(Param annotation) {
    super(annotation);        
  }
  
  /**
   * Public Constructor  
   */
  public DateTimeParameter(String key, DateTime value, String format) {
    super(key,value, format);        
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Parameter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert strValue to a DateTime Value (or null if empty|""). The value
   * is converted to a DateTime calling the static {@linkplain DateTime#FromString(
   * java.lang.String) FromString(instr)} method, which assumed the date is in
   * "yyyy-MM-ddTHH:mm:ssZ" format.</p>
   */
  @Override
  protected DateTime onParse(String strValue){
    strValue = DataEntry.cleanString(strValue);
    DateTime result = null;
    if (strValue != null) {
      try {
        String format = DataEntry.cleanString(this.getFormat());
        if (format == null) {
          result = DateTime.FromString(strValue);           
        } else {
          result = DateTime.FromString(strValue, format, null); 
        }
      } catch (Exception exp) {        
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (this.value == null)? null: value.toString(), which convert the
   * date to "yyyy-MM-ddTHH:mm:ssZ" string format.</p>
   */
  @Override
  protected String onAsString() throws Exception {
    String result = null;
    DateTime value = this.getValue();
    if (value != null) {
      String format = DataEntry.cleanString(this.getFormat());
      if (format == null) {
        result = value.toString();
      } else {
        result = value.toLocaleString(format);
      }
    }
    return result; 
  }  
  // </editor-fold>
}
