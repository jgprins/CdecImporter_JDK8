package bubblewrap.io.converters.enums;

/**
 * This enum provide the JavaScripts as defined in "bw.core.js" for the assigning to a
 * Text Input controls onkeypress event to prevent user from entering invalid characters.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum BwOnKeyPress {
  NONE(0,""),
  /**
   * Alpha characters only (a..zA..Z no numbers or white spaces
   */
  ALPHA(1,"return bw.io.onKeypressInteger(event);"),
  /**
   * Alpha characters only (a..zA..Z plus numbers or white spaces)
   */
  ALPHANUMERICC(1,"return bw.io.onKeypressInteger(event);"),
  /**
   * Integer Values - numbers  plus minus sign
   */
  INT(1,"return bw.io.onKeypressInteger(event);"),
  /**
   * Unsigned Integer Values - numbers no minus sign allowed
   */  
  UINT(2,"return bw.io.onKeypressUInteger(event);"),
  /**
   * Decimal Values - numbers plus minus sign and period
   */  
  DEC(2,"return bw.io.onKeypressUInteger(event);");
  
  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The bw.core.js script to assign to a Text Input element's onkeypress event to
   * control the user's input and prevent the user from entering invalid characters
   */
  public final String script;
  /**
   * The enum value value
   */
  public final int value;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor  
   * @param value the option value
   * @param script the option label
   */
  private BwOnKeyPress(int value, String script) {
    this.script = script;
    this.value = value;
  }
  // </editor-fold>
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the BwOnKeyPress associated with <tt>value</tt>
   * @param value the BwOnKeyPress.value to search for
   * @return the matching BwOnKeyPress or NONE if not found.
   */
  public static BwOnKeyPress fromValue(int value) {
    BwOnKeyPress result = BwOnKeyPress.NONE;
    for (BwOnKeyPress enumVal : BwOnKeyPress.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }
  // </editor-fold>
}
