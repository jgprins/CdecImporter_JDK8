package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * 9-digit Zip-code in the format '99999-9999'.
 * @author kprins
 */
public class LongZipCodeValidator extends StringValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public LongZipCodeValidator() {
    super();
  }
  //</editor-fold>

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanNumericString(inputValue);
    if ((super.onIsValidInput(inputValue)) &&
        (inputValue != null)) {
      String rexExp1 = "[0-9]5$";
      String rexExp2 = "[0-9]9$";
      if ((inputValue != null) && 
              (!((inputValue.matches(rexExp1)) || inputValue.matches(rexExp2)))) {
        this.setErrorMsg("The ZipCode entry '" + inputValue +"' is invalid. "
                + "Must consist of five (5) numeric characters or five (9) numeric "
                + "characters (excluding the '-' seperator). It will be reformatted to "
                + "'99999-9999'.");

      }
    }
    return (!this.hasError());
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: if the length of input &gt; 5 convert to "99999-9999" format.</p>
   */
  @Override
  public String toString(String input) {
    input  = super.toString(input);
    String result = input;
    if ((input != null) && (input.length() > 5)) {
      result = input.substring(0, 4) + "-" + input.substring(5);
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return DataEntry.cleanNumericString(input)</p>
   */
  @Override
  public String toValue(String input) {
    return DataEntry.cleanNumericString(input);
  }
}
