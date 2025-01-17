package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 *
 * @author kprins
 */
public class PhoneNoValidator extends StringValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public PhoneNoValidator() {
    super();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="StringValidator Overrides">  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: throw and exception is inputValue != null and 
   * (!{@linkplain DataEntry#isValidTelNo(java.lang.String) 
   *  DataEntry.isValidTelNo(inputValue)}.</p>
   */
  @Override
  protected boolean  onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    if ((super.onIsValidInput(inputValue)) &&
        (inputValue != null) && (!DataEntry.isValidTelNo(inputValue))) {
      this.setErrorMsg("Telephone Number '" + inputValue +"' is invalid. " 
              + "A valid telphone number is a 10-digit number including the ara code "
              + "and number (e.g. (800) 999-9999. A numeric characters are ignored.");
      
    }
    return (!this.hasError());
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: DataEntry.getTelNoAsString(inputValue)</p>
   */
  @Override
  public String toString(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    return (inputValue == null)? null: DataEntry.getTelNoAsString(inputValue);
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: returns DataEntry.getTelNoasNumber(inputValue)</p>
   */
  @Override
  public String toValue(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    return (inputValue == null)? null: DataEntry.getTelNoasNumber(inputValue);
  }
  //</editor-fold>
}
