package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * A validator for a 5-digit zip code.
 * @author kprins
 */
public class ShortZipCodeValidator extends StringValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public ShortZipCodeValidator() {
    super();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override StrignValidator">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: It throws and exception if inputValue!=null or not 5-digits</p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanNumericString(inputValue);
    if ((super.onIsValidInput(inputValue)) &&
        (inputValue != null)) {
      String rexExp = "[0-9]5$";
      if ((inputValue != null) && (!inputValue.matches(rexExp))) {
        this.setErrorMsg("The ZipCode entry '" + inputValue +"' is invalid. "
                + "Must consist of five (5) numeric characters");

      }
    }
    return (!this.hasError());
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return DataEntry.cleanString(inputValue);</p>
   */
  @Override
  public String toString(String inputValue) {
    return DataEntry.cleanString(inputValue);
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return DataEntry.cleanString(inputValue);</p>
   */
  @Override
  public String toValue(String inputValue) {
    return DataEntry.cleanString(inputValue);
  }
  //</editor-fold>
}
