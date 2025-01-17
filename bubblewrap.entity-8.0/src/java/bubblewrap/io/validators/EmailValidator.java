package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * An E-mail validator
 * @author kprins
 */
public class EmailValidator extends StringValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public EmailValidator() {
    super();
  }
  //</editor-fold>

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: throw and exception is inputValue != null and 
   * (!{@linkplain DataEntry#isValidEMail(java.lang.String) 
   *  DataEntry.isValidEMail(inputValue)}.</p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    if (super.onIsValidInput(inputValue)) {
      inputValue = DataEntry.cleanString(inputValue);
      if ((inputValue != null) &&  (!DataEntry.isValidEMail(inputValue))) {
        this.setErrorMsg("Invalid Email '" + inputValue + "'. ");
      }
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
