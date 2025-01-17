package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A Boolean Validator for True/False Parameters that are stored in the database as 0/1.
 * For supported option see {@linkplain #onSetOptions(org.json.JSONObject) onSetOptions}.
 * @author kprins
 */
public class BoolValidator extends InputValidator<Boolean> {
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Allow Null flag (default class = true)
   */
  private Boolean allowNull;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Default Public Constructor
   */
  public BoolValidator(){
    super();
    this.allowNull = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * call to initiate the StringValidator's AllowNull flag (default=true)
   * @param allowNull true to allow; false to deny
   */
  public void initAllowNull(boolean allowNull) {
    this.allowNull = (allowNull)? null: allowNull;
  }
  
  /**
   * Get whether a null value is permitted
   * @return the assigned value
   */
  public boolean getAllowNull() {
    return ((this.allowNull == null) || (this.allowNull));
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override InputValidator">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: ((this.getAllowNull()) || (inputValue != null))</p>
   */
  @Override
  public boolean onIsValidInput(Boolean inputValue) {    
    if ((!this.getAllowNull()) && (inputValue == null)) {
      this.setErrorMsg("Your input value cannot be null");
    }
    return (!this.hasError());
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return true if inputValue != null, "1","-1", "yes" or "true". - not 
   * case sensitive.</p>
   */
  @Override
  public Boolean toValue(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    return DataEntry.inStringArray(inputValue, "1", "-1", "true", "yes");
  }

  /**
   * Return (null|false)="0";true="1"
   * @param inputValue Boolean
   * @return String
   */
  @Override
  public String toString(Boolean inputValue) {
    return ((inputValue == null) || (!inputValue)) ? "0" : "1";
  }

  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: Parse the cleanString string assuming options in the format:
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
        value = DataEntry.cleanString(optionArr.optString(optionArr.length() - 1, ""));
      }
    } else {
      value = DataEntry.cleanString(options.optString("allownull", ""));
    }
    if ((value != null)
            && ((isSet = DataConverter.toBoolean(value)) != null) && (!isSet)) {
      this.allowNull = isSet;
    }
  }

  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: Return "allownull=" + this.allowNull</p>
   */
  @Override
  public String getOptions() {
    return "allownull=" + this.getAllowNull();
  }
  // </editor-fold>
}
