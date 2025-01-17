package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 *
 * @author kprins
 */
public class IntegerOptionsValidator extends OptionsValidator<Integer> {

// <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Parameter Constructor
   */
  public IntegerOptionsValidator() {
    super();
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="InputValidator Overrides">
  /**
   * Return the integer value of inputValue - return null if inputValue=""/null or if
   * an error occur when parsing the integer value.
   * @param inputValue String
   * @return Integer
   */
  @Override
  public Integer toValue(String inputValue) {
    Integer iResult = null;
    inputValue = DataEntry.cleanString(inputValue);
    if (inputValue != null) {
      try {
        iResult = Integer.parseInt(inputValue);
      } catch (NumberFormatException pExp) {
        iResult = null;
      }
    }
    return iResult;
  }

  /**
   * Return null is inputValue=null or inputValue.toSting() otherwise.
   * @param inputValue
   * @return String
   */
  @Override
  public String toString(Integer inputValue) {
    String sResult = null;
    if (inputValue != null) {
      sResult = inputValue.toString();
    }
    return sResult;
  }
// </editor-fold>
}
