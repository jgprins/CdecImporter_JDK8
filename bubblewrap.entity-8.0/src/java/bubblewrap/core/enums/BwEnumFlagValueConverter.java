package bubblewrap.core.enums;

import bubblewrap.io.converters.FieldValueConverter;

/**
 * A FieldValue Converter to convert a BwEnumFlag value to an integer
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwEnumFlagValueConverter extends FieldValueConverter {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwEnumFlagValueConverter() {
    super();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements FieldValueConverter">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: If value is {@linkplain #isValidInput(java.lang.Object) 
   * this.isValdiInput}m return null if value is null, the value if value is an Integer
   * or return ((BwEnum) value).value.intValue()</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object toFieldValue(Object value) {
    Integer result = null;
    if ((value != null)) {
      if (value instanceof String) {
        String strVal = (String) value;
        result = Integer.parseInt(strVal);
      } else if (value instanceof Integer) {
        result = (Integer) value;
      } else if (value instanceof BwEnumFlag) {
        BwEnumFlag enumVal = (BwEnumFlag) value;
        result = enumVal.value.intValue();
      }
    }
    return result;
  }
  
  /**
   * Convert value to String if (value != null)) and an instance of BwEnumFlag.
   * @param value
   * @return the String value or null if value = null.
   */
  @Override
  public String toStringValue(Object value) {
    String result = null;
    if ((value != null) && (value instanceof BwEnumFlag)) {
      BwEnumFlag enumVal = (BwEnumFlag) value;
      result = Integer.toString(enumVal.value.intValue());
    } 
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true if ((value == null) || (value instanceof Integer) || 
   * (value instanceof BwEnumFlag))</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return ((value == null) || (value instanceof Integer) || 
            (value instanceof BwEnumFlag));
  }
  //</editor-fold>
}
