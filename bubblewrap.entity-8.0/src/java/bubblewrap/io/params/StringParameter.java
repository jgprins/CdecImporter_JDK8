package bubblewrap.io.params;

import bubblewrap.core.annotations.Param;
import bubblewrap.io.DataEntry;
import java.util.Scanner;

/**
 * A Parameter class the store all values as String. The string values can be converted
 * to Boolean and Number values using the cast methods.
 * @author kprins
 */
public class StringParameter extends Parameter<String> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static parse of a "Key=Value" string to a Parameter. If sInStr = "Key=", the value
   * will be set to null. If Value is in quotes, the quotes will be removed. All values
   * are assigned as sting and will be casted on retrieval using Parameter.getNumberValue.
   * @param inputStr the "Key=Value" string
   * @return return an instance of parameter with the Key-Value pair.
   * @throws Exception
   */
  public static StringParameter parse(String inputStr) throws Exception {
    StringParameter result = null;
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        String sKey = null;
        String sValue = "";
        Scanner pScanner = new Scanner(inputStr);
        pScanner.useDelimiter("=");
        while (pScanner.hasNext()) {
          if (sKey == null) {
            sKey = pScanner.next();
          } else {
            sValue += pScanner.next();
          }
        }
        
        sKey = DataEntry.cleanString(sKey);
        if (sKey != null) {
          sValue = DataEntry.cleanString(sValue);
          if (sValue != null) {
            if ((sValue.startsWith("\"")) || (sValue.endsWith("\""))) {
              sValue = sValue.replaceAll("\"", "");
              result = new StringParameter(sKey, sValue);
            } else if ((sValue.startsWith("'")) || (sValue.endsWith("'"))) {
              sValue = sValue.replaceAll("'", "");
              result = new StringParameter(sKey, sValue);
            } else {
              result = new StringParameter(sKey, sValue);
            }
          } else {          
            result = new StringParameter(sKey, sValue);
          }
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
   * The Annotation based constructor called by the static {@link #fromAnnotation(bubblewrap.core.annotations.Param) Parameter.fromAnnotation} method
   * @param annotation the parameter annotation
   */
  public StringParameter(Param annotation) {
    super(annotation);
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public StringParameter(String sKey, String sValue) {
    super(sKey,DataEntry.cleanString(sValue));        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Parameter Overrides">
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
