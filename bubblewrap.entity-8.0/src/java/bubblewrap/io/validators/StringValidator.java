package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An InputValidator for string fields. It checks for the length of the string and whether
 * the string is empty. It also support non-trimmed handling. if this "noTrim" parameter
 * is set prepended or training spaces will be maintained in the conversion. 
 * For supported option see {@linkplain #onSetOptions(org.json.JSONObject) onSetOptions}.
 * @author kprins
 */
public class StringValidator extends InputValidator<String> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Allow Null flag (default = true)
   */
  private Boolean allowNull;
  /**
   * Placeholder for the noTrim flag (default class = false)
   */
  private Boolean noTrim;
  /**
   * Placeholder for the string maximum Length (default = null|0 - no limit
   */
  private Integer maxLength;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public StringValidator() {
    super(); 
    this.allowNull = null;
    this.maxLength = null;
    this.noTrim = null;
  }
  
  /**
   * call to initiate the StringValidator's AllowNull flag (default=true)
   * @param allowNull true to allow; false to deny
   */
  protected void initAllowNull(boolean allowNull) {
    this.allowNull = (allowNull)? null: allowNull;
  }
  
  /**
   * Set the string length limitation (default = 0 - no length limit).
   * @param maxLength the maximum length of the string.
   */
  public void setStringLength(int maxLength) {
    this.maxLength = (maxLength <= 0)? null: maxLength;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether a null value is permitted (default = true)
   * @return the assigned value
   */
  public final boolean getAllowNull() {
    return ((this.allowNull == null) || (this.allowNull));
  }
  /**
   * Get whether a null value is permitted (default = true)
   * @return the assigned value
   */
  public final boolean getNoTrim() {
    return ((this.noTrim != null) && (this.noTrim));
  }
  
  
  /**
   * Get a Valid string's MaxLength. The string has no limit is maxLength=0.
   * @return the assigned value
   */
  public final Integer getMaxLength() {
    return (this.maxLength == null)? 0: this.getMaxLength();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="InputValidator Override">
  /**
   * Call DataEntry.cleanStrign() to remove any leading of trailing spaces before 
   * testing the String validity.
   * Return true if not null or ((allowNull)? (pInput!=Null/"") : true. If the string
   * has a MaxLength limit (>0), return false if pInput.length exceeds maxLength.
   * Assign an Error message if the validation fails.
   * @param input String
   * @return boolean
   */
  @Override
  protected boolean onIsValidInput(String input) {
    boolean isValid = false;
    this.clearErrorMsg();
    Integer maxLen = this.getMaxLength();
    if ((!this.getAllowNull()) && (DataEntry.cleanString(input) == null)) {
      this.setErrorMsg("Your input value cannot be null");
    } else if ((input != null) && (maxLen > 0) && ((input.length() > maxLen))) {
      this.setErrorMsg("Your input exceed the maximum of " + maxLen.toString() 
             + " characters.");      
    }
    return (!this.hasError());
  }
  
  /**
   * Called to "clean" the input string (e.g., removing all symbols, leasing and 
   * trailing spaces) the base method return DataEntry.cleanString(input)
   * @param input the raw input  string
   */
  protected String onClearInput(String input) {   
    return DataEntry.cleanString(input);
  }
  
  /**
   * Return sParValue unchanged.
   * @param input String
   * @return String String
   */
  @Override
  public String toValue(String input) {
    return input;
  }
  
  /**
   * Return pInput trimmed. If MaxLength is specified and exceeded, return a substring
   * consisting of the first MaxLength characters
   * @param input String
   * @return String
   */
  @Override
  public String toString(String input) {
    int maxLen = (this.maxLength == null)? 0: this.maxLength;
    return DataEntry.cleanString(input,maxLen);
  } 
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Parse the cleanString string assuming options in the format: 
   * "maxlength=#,allownull=0|1|true|false,notrim=allownull=0|1|true|false".
   * The default allowNull=true, default noTrim=false.</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.allowNull = null;
    this.maxLength = null;
    this.noTrim = null;
    if ((options == null) || (options.length() == 0)) {
      return;
    }
    JSONArray optionArr = options.optJSONArray("keyvalues");
    String allownull = null;
    String notrim = null;
    int maxlen = 0;
    Boolean isSet = null;
    if (optionArr != null) {
      if (optionArr.length() > 1) {
        maxlen = optionArr.optInt(0, 0);
      }
      if (optionArr.length() > 0) {
        allownull = DataEntry.cleanString(optionArr.optString(optionArr.length()-1, ""));
      }
    } else {
      maxlen = options.optInt("maxlength", 0);
      allownull = DataEntry.cleanString(options.optString("allownull", ""));
      notrim = DataEntry.cleanString(options.optString("notrim", ""));
    }
    if ((allownull != null) &&
            ((isSet = DataConverter.toBoolean(allownull)) != null) && (!isSet)) {
      this.allowNull = isSet;
    }
    if ((notrim != null) &&
            ((isSet = DataConverter.toBoolean(notrim)) != null) && (isSet)) {
      this.noTrim = isSet;
    }
    this.maxLength = (maxlen <= 0)? null: maxlen;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return DataEntry.concatKeyValuePairs("maxlength=" + this.getMaxLength(),
   * "allownull=" +  this.getAllowNull(),"notrim=" + this.getNoTrim()).</p>
   */
  @Override
  public String getOptions() {
    return DataEntry.concatKeyValuePairs("maxlength=" + this.getMaxLength(),
                                         "allownull=" +  this.getAllowNull(),
                                         "notrim=" + this.getNoTrim());
  }
  //</editor-fold>
}
