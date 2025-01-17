package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 *
 * @author kprins
 */
public class UsernameValidator extends StringValidator {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public UsernameValidator() {    
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="StringValidator Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: true is inputValue=null or {@linkplain 
   * DataEntry#isValidUsername(java.lang.String) DataEntry.isValidUsername(inputValue)}.
   * If the latter returns false, it throws an exception displaying {@linkplain  
   * DataEntry#UsernameHint}</p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    if ((super.onIsValidInput(inputValue)) && (inputValue != null) && 
            (!DataEntry.isValidUsername(inputValue))) {
      this.setErrorMsg("Username '" + inputValue +"' is invalid. " 
              + DataEntry.UsernameHint);
      
    }
    return (!this.hasError());
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return DataEntry.cleanString(inputValue)</p>
   */
  @Override
  public String toString(String inputValue) {
    return DataEntry.cleanString(inputValue);
  }
  
  @Override
  public String toValue(String inputValue) {
    return super.toValue(inputValue);
  }
  //</editor-fold>
}
