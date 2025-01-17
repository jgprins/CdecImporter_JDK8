package bubblewrap.io.validators;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javax.faces.model.SelectItem;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A InputValidator to supports discreet options as set via the setOptions methods.
 * For supported option see {@linkplain #onSetOptions(org.json.JSONObject) onSetOptions}.
 * @author kprins
 */
public abstract class OptionsValidator<TValue extends Serializable> 
                                                      extends InputValidator<TValue> {

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Fields">
  /**
   * Placeholder for the LinkedHashMap of Valid Options (not null) where the key is
   * of TValue and value represents the Label for TValue (default = key.toString())
   */
  private LinkedHashMap<TValue, String> inputOptions;
  /**
   * Placeholder for the Allow Null flag (default = true)
   */
  private Boolean allowNull;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protected Constructor - call super()
   */
  protected OptionsValidator() {
    super();
    this.allowNull = null;
    this.inputOptions = null;
  }
  
  /**
   * call to initiate the StringValidator's AllowNull flag (default=true)
   * @param allowNull true to allow; false to deny
   */
  protected void initAllowNull(boolean allowNull) {
    this.allowNull = (allowNull)? null: allowNull;
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
   * Reset the selection options.
   */
  public void clearOptions() {
    this.inputOptions = null;
  }

  /**
   * Append pValue to the end of the selection Options list.
   * @param value new selection option value to added
   */
  public void addOptionValue(TValue value) {
    this.addOptionValue(value, null);
  }

  /**
   * Add individual selection options at a specific index. if iIndex=null or &gt; 
   * this.inputOptions.size, the value will be appended. Duplicate values are ignored.
   * @param value new selection option value to added
   */
  public void addOptionValue(TValue value, String label) {
    if (value instanceof String) {
      value =  (value == null)? null: (TValue) DataEntry.cleanString(value.toString());
    } else if (value instanceof Double) {
      value = ((value == null) || (Double.isNaN((Double) value)))? null: value;
    }
    if (value != null) {
      if (this.inputOptions == null) {
        this.inputOptions = new LinkedHashMap<>();
      }

      if (!this.inputOptions.containsKey(value)) {
        if ((label = DataEntry.cleanString(label)) == null) {
          label = value.toString();
        }
        this.inputOptions.put(value,label);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="InputValidator Overrides">
  /**
   * Return 
   * @param inputValue Integer
   * @return true if ((!allowNull) and (inputValue!=null)) and if 
   * ((inputValue!=null) && this.hasOptions and (inputValue is a valid options)
   */
  @Override
  public boolean onIsValidInput(TValue inputValue) {
    this.clearErrorMsg();
    if ((inputValue == null) && (!this.getAllowNull()) && (this.hasSelectOptions())) {
      this.setErrorMsg("The specified input cannot not be null.");
    } else if ((inputValue != null) && (this.hasSelectOptions()) && 
               (!this.inputOptions.containsKey(inputValue))) {
      this.setErrorMsg("The specified input is not a valid selection option.");
    }
    return (!this.hasError());
  }

  /**
   * Check if the validator has assigned options 
   * @return return true the Options are undefined
   */
  @Override
  public boolean hasSelectOptions() {
    return ((this.inputOptions != null) && (!this.inputOptions.isEmpty()));
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return the SelectItem (valId,valLabel) where valId is the string value 
   * of each this.inputOptions.keys and valLabel = this.inputOptions.get(key) for 
   * each key. The first option is a Blank entry - SelectItem("","Select...").</p>
   */
  @Override
  protected List<SelectItem> onGetSelectOptions() throws Exception {
    List<SelectItem> result = new ArrayList<>();
    result.add(new SelectItem("", "Select..."));
    if (this.hasSelectOptions()) {
      for (TValue option: this.inputOptions.keySet()) {
        if (option != null) {
          String valId = option.toString();
          String valLabel = this.inputOptions.get(option);
          result.add(new SelectItem(valId, valLabel));
        }
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Parse the cleanString string assuming options in the format: 
   * {"options=[#,#..]","allownull=0|1|true|false"} where "#" represents a valid 
   * selection option of type TValue.
   * The default allowNull=true, default noTrim=false.</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.allowNull = null;
    this.clearOptions();
    if ((options == null) || (options.length() == 0)) {
      return;
    }

    try {     
      JSONArray kvpArray = options.optJSONArray("keyvalues");
      JSONArray optionsArr = null;
      String allownull = null;
      Boolean isSet = null;
      if (kvpArray != null) {
        if (kvpArray.length() > 0) {
          optionsArr = kvpArray.optJSONArray(0);
        }
        if (kvpArray.length() > 1) {
          allownull = DataEntry.cleanString(kvpArray.optString(kvpArray.length()-1, ""));
        }
      } else {
        optionsArr = options.optJSONArray("options");
        allownull = DataEntry.cleanString(options.optString("allownull", ""));
      }
      
      if ((optionsArr != null) && (optionsArr.length() > 0)) {
        String strVal = null;
        TValue optVal = null;
        for (int iOpt = 0; iOpt < optionsArr.length(); iOpt++) {
          Object obj = optionsArr.get(iOpt);
          if (obj instanceof JSONArray) {
            JSONArray pair = (JSONArray) obj;
            String label = null;
            if (pair.length() == 1) {
              if (((strVal = DataEntry.cleanString(pair.optString(0,""))) != null) &&
                  ((optVal = this.toValue(strVal)) != null)) {
                this.addOptionValue(optVal);
              }
            } else if (pair.length() > 1) {
              if (((strVal = DataEntry.cleanString(pair.optString(0,""))) != null) &&
                  ((optVal = this.toValue(strVal)) != null) &&
                  ((label = DataEntry.cleanString(pair.optString(1,""))) != null)) {
                this.addOptionValue(optVal, label);
              }
            }
          } else if (((strVal = DataEntry.cleanString(optionsArr.optString(iOpt,""))) 
                                != null) && ((optVal = this.toValue(strVal)) != null)) {
            this.addOptionValue(optVal);
          }
        }
      }
      if ((allownull != null) &&
            ((isSet = DataConverter.toBoolean(allownull)) != null) && (!isSet)) {
        this.allowNull = isSet;
      }
    } catch (Exception exp) {
      this.inputOptions.clear();
      this.setErrorMsg(exp.getMessage());
    }
  }

  /**
   * Return the Options as the toString values of the mpOptions entries separated with
   * a ";". if (!this.allowNull), a ",0 is append and the option values is included in
   * "{}" brackets
   * @return String
   */
  @Override
  public String getOptions() {
    String result = null;
    String options = null;
    if ((this.inputOptions != null) && (!this.inputOptions.isEmpty())) {
      for (TValue option : this.inputOptions.keySet()) {
        String strVal = (option == null) ? null : 
                                              DataEntry.cleanString(option.toString());
        if (strVal != null) {
          String label = DataEntry.cleanString(this.inputOptions.get(option));
          if (!strVal.equals(label)) {
            strVal = "[" + strVal + "," + label + "]";
          }
          if (options == null) {
            options = strVal;
          } else {
            options += "," + strVal;
          }
        }
      }
    } 
    if (options == null) {
      options = "options=[]"; 
    } else {
      options = "options=[" + options + "]"; 
    }
    
    result = DataEntry.concatKeyValuePairs(options,"allownull=" + this.getAllowNull());
    return result;
  }
// </editor-fold>
}
