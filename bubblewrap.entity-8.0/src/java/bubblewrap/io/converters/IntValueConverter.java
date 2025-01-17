package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.enums.BwOnKeyPress;
import org.json.JSONObject;

/**
 * An FieldValueConverter for Integer field values.  See {@linkplain
 * #onSetOptions(org.json.JSONObject) onSetOptions} for details on the supported
 * conversion options.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class IntValueConverter extends FieldValueConverter<Integer> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * True to add a "+" sign in front of positive values
   */
  private Boolean showPlus;
  /**
   * True to add thousand separators
   */
  private Boolean showSeparator;
  /**
   * A Prefix to append to the front of the formated string
   */
  private String prefix;
  /**
   * A Prefix to append to the back of the formated string
   */
  private String suffix;
  /**
   * The string to return if the field value = null.
   */
  private String nullValue;
  /**
   * True to add a "+" sign in front of positive values
   */
  private Boolean unsigned;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public IntValueConverter() {
    super(); 
    this.reset();
  }
  
  /**
   * Reset of format parameters
   */
  private void reset() {
    this.showPlus = null;
    this.showSeparator = null;
    this.prefix = null;
    this.suffix = null;
    this.nullValue = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Check if the Integer String should include a Thousand Separator
   * @return 
   */
  private Boolean getShowSeparator() {
    return ((this.showSeparator != null) && (this.showSeparator));
  }
  /**
   * Check if the Integer String should include a Thousand Separator
   * @return 
   */
  private Boolean getShowPlus() {
    return ((this.showPlus != null) && (this.showPlus));
  }
  /**
   * Check if the Integer String should include a Thousand Separator
   * @return 
   */
  private Boolean isUnsigned() {
    return ((this.unsigned != null) && (this.unsigned));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="FieldValueConverter Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: If value is a String or a numeric value, convert to value to an integer 
   * and return the converted value, else return null
   * </p>
   */
  @Override
  public Integer toFieldValue(Object value) {
    Integer result = null;
    if (value != null) {
      if (value instanceof String) {
        String strVal = (String) value;
        strVal = DataEntry.cleanString(strVal);
        if (strVal != null) {
          result = DataConverter.toValue(strVal, Integer.class);          
        }
      } else if (value instanceof Integer) {
        result = (Integer) value;
      } else if (value instanceof Number) {
        Number numVal = (Number) value;
        result = DataConverter.convertTo(numVal, Integer.class);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return the formatted string of a Integer or Numeric value rounded to an 
   * Integer or return the value is it is a String, else return this.nullValue</p>
   */
  @Override
  public String toStringValue(Object value) {
    String result = null;
    Integer intVal = null;
    if (value != null) {
      if (value instanceof String) {
        intVal = this.toFieldValue(value);
      }
    } else {
      if (value instanceof Integer) {
        intVal = (Integer) value;
      } else if (value instanceof Number) {
        Number numVal = (Number) value;
        intVal = DataConverter.convertTo(numVal, Integer.class);
      }
      
    }
    
    if (intVal != null) {
      Long lngVal = intVal.longValue();
      if ((this.isUnsigned()) && (lngVal < 0)) {
        lngVal = -1l * lngVal;
      }
      result = DataEntry.getLongFormat(lngVal, this.getShowSeparator(), 
              this.getShowPlus(), this.prefix, this.suffix, this.nullValue);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return ((value == null) || (value instanceof String) || (value 
   * instanceof Number))</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return ((value == null) || (value instanceof String) || (value instanceof Number));
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Set the  the options are defined as:
   * </p><ul>
   * <li>"showplus=0/1|true/false" - if set to true|1, a "+" sign will be pre-append to
   * positive values</li>
   * <li>"showseparator=0/1|true/false" - if true, a "," will be used to a a thousand
   * separator.</li>
   * <li>"prefix='...'" - the string inside the single quotes will be used as a prefix.
   * </li>
   * <li>"suffix='...'" - the string inside the single quotes will be used as a suffix.
   * </li>
   * <li>"nullvalue='...'" - the string inside the single quotes will be returned if the
   * value is null.
   * </li>
   * <li>"unsigned=0/1|true/false" - if set to true|1, the absolute integer value will be
   * returned</li>
   * </ul>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.reset();
    if ((options == null) || (options.length() == 0)) {
      return;
    }    
    
    Boolean isSet = null;
    String value = DataEntry.cleanString(options.optString("showplus", ""));
    if ((value != null) &&
            ((isSet = DataConverter.toBoolean(value)) != null) && (isSet)) {
      this.showPlus = isSet;
    }
    value = DataEntry.cleanString(options.optString("showseparator", ""));
    if ((value != null) &&
            ((isSet = DataConverter.toBoolean(value)) != null) && (isSet)) {
      this.showSeparator = isSet;
    }
    value = DataEntry.cleanString(options.optString("unsigned", ""));
    if ((value != null) &&
            ((isSet = DataConverter.toBoolean(value)) != null) && (isSet)) {
      this.unsigned = isSet;
    }
    value = options.optString("prefix", "");
    if (DataEntry.cleanString(value) != null) {
      this.prefix = value;
    }
    value = options.optString("suffix", "");
    if (DataEntry.cleanString(value) != null) {
      this.suffix = value;
    }
    value = options.optString("nullvalue", "");
    if (DataEntry.cleanString(value) != null) {
      this.nullValue = value;
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if (this.unsigned) return {@linkplain BwOnKeyPress#UINT}.{@linkplain 
   * BwOnKeyPress#script script} else return {@linkplain BwOnKeyPress#INT}.script</p>
   */
  @Override
  public String onKeypress() {
    String result = null;
    if (this.isUnsigned()) {
      result = BwOnKeyPress.UINT.script;
    } else {
      result = BwOnKeyPress.INT.script;
    }
    return result;
  }
  // </editor-fold>  
}
