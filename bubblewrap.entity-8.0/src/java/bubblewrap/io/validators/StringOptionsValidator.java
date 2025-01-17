package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;

/**
 * A String inputValidator that allows null values.
 * @author kprins
 */
public class StringOptionsValidator extends OptionsValidator<String> {

// <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Parameterless Constructor
   */
  public StringOptionsValidator() {
    super();
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="InputValidator Overrides">
  /**
   * Return the DataEntry.cleanString()
   * @param sParValue String
   * @return String
   */
  @Override
  public String toValue(String sParValue) {
    return DataEntry.cleanString(sParValue);
  }
// </editor-fold>
}
