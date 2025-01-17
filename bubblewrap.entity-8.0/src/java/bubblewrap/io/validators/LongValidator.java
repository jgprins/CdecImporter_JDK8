package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An Input Validator class for converting String value to integer and vise versa.
 * Default settings allow null values.
 * For supported option see {@linkplain #onSetOptions(org.json.JSONObject) onSetOptions}.
 * @author kprins
 */
public class LongValidator extends InputValidator<Long> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the AllowNull flag (default class = true)
   */
  private Boolean allowNull;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public LongValidator() {
    super();
    this.allowNull = null;
  }
  
  /**
   * Initiate the Validator
   * @param allowNull true is a null value is allowed
   */
  protected void initValidator(boolean allowNull) {
    this.allowNull = (allowNull)? null: allowNull;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Method">
  /**
   * Get whether this Validator allows a null value
   * @return true is null is allowed
   */
  public boolean getAllowNull() {
    return ((this.allowNull == null) || (this.allowNull));
  }

  // <editor-fold defaultstate="collapsed" desc="InputValidator Overrides">
  /**
   * Return true is inputValue!=null and true if ((allowNull) and (inputValue=null))
   * @param inputValue Integer
   * @return Boolean
   */
  @Override
  public boolean onIsValidInput(Long inputValue) {
    if ((!this.getAllowNull()) && (inputValue == null)) {
      this.setErrorMsg("Your input value cannot be null");
    }
    return (!this.hasError());
  }

  /**
   * Return the integer value of inputValue - return null if inputValue=""/null or if
   * an error occur when parsing the integer value.
   * @param inputValue String
   * @return Integer
   */
  @Override
  public Long toValue(String inputValue) {
    Long result = null;
    inputValue = DataEntry.cleanString(inputValue);
    if (inputValue != null) {
      try {
        result = Long.parseLong(inputValue);
      } catch (NumberFormatException exp) {
        result = null;
      }
    }
    return result;
  }

  /**
   * Return null is inputValue=null or inputValue.toSting() otherwise.
   * @param inputValue
   * @return String
   */
  @Override
  public String toString(Long inputValue) {
    String result = null;
    if (inputValue != null) {
      result = inputValue.toString();
    }
    return result;
  }  
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Parse the cleanString string assuming options in the format: 
   * "allownull=0|1|true|false" - default = false.</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.allowNull = null;
    if ((options == null) || (options.length() == 0)) {
      return;
    }
    JSONArray optionArr = options.optJSONArray("keyvalues");
    String value = null;
    Boolean isSet = null;
    if (optionArr != null) {
      if (optionArr.length() > 0) {
        value = DataEntry.cleanString(optionArr.optString(optionArr.length()-1, ""));
      }
    } else {
      value = DataEntry.cleanString(options.optString("allownull", ""));
    }
    if ((value != null) &&
            ((isSet = DataConverter.toBoolean(value)) != null) && (!isSet)) {
      this.allowNull = isSet;
    }
  }
    
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "allownull=" + this.allowNull</p>
   */
  @Override
  public String getOptions() {
    return "allownull=" + this.getAllowNull();
  } 
  // </editor-fold>
}
