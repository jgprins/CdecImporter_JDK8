package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.enums.BwOnKeyPress;
import org.json.JSONObject;

/**
 * A Generic Numeric Field Convert without any formating using to String and parse method
 * to convert to and from string values.  See {@linkplain
 * #onSetOptions(org.json.JSONObject) onSetOptions} for details on the supported
 * conversion options.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class NumberValueConverter<TNum extends Number> extends FieldValueConverter<TNum> {
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder the Number's Generic Type
   */
  public final Class<TNum> numberType;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public NumberValueConverter(Class<TNum> numberType) {
    super(); 
    if (numberType == null) {
      throw new IllegalArgumentException("The Number Type Class cannot be unassigned.");
    }
    this.numberType = numberType;
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
  public TNum toFieldValue(Object value) {
    TNum result = null;
    if (value != null) {
      if (value instanceof String) {
        String strVal = (String) value;
        strVal = DataEntry.cleanString(strVal);
        if (strVal != null) {
          result = DataConverter.toValue(strVal, this.numberType);          
        }
      } else if (this.numberType.isInstance(value)) {
        result = (TNum) value;
      } else if (value instanceof Number) {
        Number numVal = (Number) value;
        result = DataConverter.convertTo(numVal, this.numberType);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return the value is of type String or value.toString if value is a Number
   * </p>
   */
  @Override
  public String toStringValue(Object value) {
    String result = null;
    if (value != null) {
      if (value instanceof String) {
        TNum numVal = this.toFieldValue(value);
        if (numVal != null) {
          result = numVal.toString();
        }
      } else if (value instanceof Number) {
        result = value.toString();
      }
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
   * OVERRIDE: No options are supported
   */
  @Override
  protected void onSetOptions(JSONObject options) {
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: {@linkplain BwOnKeyPress#DEC}.{@linkplain 
   * BwOnKeyPress#script script}.</p>
   */
  @Override
  public String onKeypress() {
    return BwOnKeyPress.DEC.script;
  }
  // </editor-fold>  
}
