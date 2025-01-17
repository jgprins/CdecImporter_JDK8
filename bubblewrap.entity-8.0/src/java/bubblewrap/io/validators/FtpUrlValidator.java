package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * An FTP Site Address validator
 * @author kprins
 */
public class FtpUrlValidator extends UrlValidator {  
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public FtpUrlValidator() {
    super();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="UrlValidator Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: throw and exception is inputValue != null and
   * (!{@linkplain DataEntry#isValidWebAddress(java.lang.String)
   *  DataEntry.isValidWebAddress(inputValue)}.</p>
   */
  @Override
  protected boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    if ((super.onIsValidInput(inputValue)) &&
        (inputValue != null) &&  (!DataEntry.isValidFtpAddress(inputValue))) {
      this.setErrorMsg("Invalid FTP Site address '" + inputValue + "'. ");
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
//</editor-fold>
}
