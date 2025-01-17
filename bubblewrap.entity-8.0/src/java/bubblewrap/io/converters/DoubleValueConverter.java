package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.enums.BwOnKeyPress;
import org.json.JSONObject;

/**
 * A FieldValueConverter for double field values.  See {@linkplain
 * #onSetOptions(org.json.JSONObject) onSetOptions} for details on the supported
 * conversion options.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DoubleValueConverter extends FieldValueConverter<Double> {

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
   * The number of decimal digits to display
   */
  private Integer digits;
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
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public DoubleValueConverter() {
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
    this.digits = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Check if the Double String should include a Thousand Separator
   * @return 
   */
  private Boolean getShowSeparator() {
    return ((this.showSeparator != null) && (this.showSeparator));
  }
  /**
   * Check if the Double String should include a Thousand Separator
   * @return 
   */
  private Boolean getShowPlus() {
    return ((this.showPlus != null) && (this.showPlus));
  }
  /**
   * Get the number of decimal digits to display (default = 2)
   * @return the assigned value or the default value if unassigned.
   */
  private Integer getDigits() {
    return (this.digits == null)? 2: this.digits;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="FieldValueConverter Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: If value is a String or a numeric value, convert to value to a Double 
   * and return the converted value, else return null
   * </p>
   */
  @Override
  public Double toFieldValue(Object value) {
    Double result = null;
    if (value != null) {
      if (value instanceof String) {
        String strVal = (String) value;
        strVal = DataEntry.cleanString(strVal);
        if (strVal != null) {
          result = DataConverter.toValue(strVal, Double.class);          
        }
      } else if (value instanceof Double) {
        result = (Double) value;
      } else if (value instanceof Number) {
        Number numVal = (Number) value;
        result = DataConverter.convertTo(numVal, Double.class);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return the formatted string of a Double or Numeric value rounded to an 
   * Double or return the value is it is a String, else return this.nullValue</p>
   */
  @Override
  public String toStringValue(Object value) {
    String result = null;
    Double dblVal = null;
    if (value != null) {
      if (value instanceof String) {
        dblVal = this.toFieldValue(value);
      } else {
        if (value instanceof Double) {
          dblVal = (Double) value;
        } else if (value instanceof Number) {
          Number numVal = (Number) value;
          dblVal = DataConverter.convertTo(numVal, Double.class);
        }
      }
    }
    if (dblVal != null) {
      result = DataEntry.getDoubleFormat(dblVal, this.getDigits(), 
              this.getShowSeparator(), this.getShowPlus(), 
              this.prefix, this.suffix, this.nullValue);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return ((value == null) || (value instance of String) || (value 
   * instance of Number))</p>
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
   * <li>"digit=#" - if set number of digits to display (&ge; 0)</li>
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
    int intVal = options.optInt("digits", -1);
    this.digits = (intVal < 0)? null: intVal;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return {@linkplain BwOnKeyPress#DEC}.{@linkplain 
   * BwOnKeyPress#script script}.</p>
   */
  @Override
  public String onKeypress() {
    return BwOnKeyPress.DEC.script;
  }
  // </editor-fold>  
}
