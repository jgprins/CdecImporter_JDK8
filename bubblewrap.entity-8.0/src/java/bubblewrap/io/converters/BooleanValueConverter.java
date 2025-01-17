package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.BoolValidator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class BooleanValueConverter extends FieldValueConverter {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(BooleanValueConverter.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private BoolValidator validator;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public BooleanValueConverter() {
    super();  
    this.validator = new BoolValidator();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override FieldValueConverter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: convert the input <tt>value</tt> to a Boolean value. If value = null or
   * (!this.isValidInput(value)), return null else return
   * this.validator.castAsValue(value).</p>
   */
  @Override
  public Object toFieldValue(Object value) {
    Boolean result = null;
    if ((value != null) && (this.isValidInput(value))) {
      if ((Boolean.class.equals(value.getClass()))
              || (boolean.class.equals(value.getClass()))) {
        result = (Boolean) value;
      } else {
        result = this.validator.castAsValue(value);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return "0" is value = null, an invalid boolean value, or false, else
   * return "1"</p>
   */
  @Override
  public String toStringValue(Object value) {
    Boolean boolVal = null;
    return ((value == null) || (!this.isValidInput(value))
            || ((boolVal = this.validator.castAsValue(value)) == null)
            || (!boolVal)) ? "0" : "1";
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return this.validator.isValidInput(value)</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return this.validator.isValidInput(value);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if options != null | empty, set jsonStr = options.toString() and
   * call this.validator.setOptions(jsonStr).</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    if ((options == null) || (options.length() == 0)) {
      return;
    }
    String jsonStr = options.toString();
    this.validator.setOptions(jsonStr);   
  }
  // </editor-fold>
}
