package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * An Web site Address Validator validator
 * @author kprins
 */
public class WebUrlValidator extends UrlValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public WebUrlValidator() {
    super();
  }
  //</editor-fold>

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: throw and exception is inputValue != null and 
   * (!{@linkplain DataEntry#isValidWebAddress(java.lang.String) 
   *  DataEntry.isValidWebAddress(inputValue)}.</p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    if ((super.onIsValidInput(inputValue)) && (inputValue != null) && 
            (!DataEntry.isValidWebAddress(inputValue))) {
      this.setErrorMsg("Invalid Web Site Address '" + inputValue + "'. ");
    }
    return (!this.hasError());
  }
  
  @Override
  public String toString(String inputValue) {
    return super.toString(inputValue);
  }

  @Override
  public String toValue(String inputValue) {
    return super.toValue(inputValue);
  }
}
