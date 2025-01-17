package bubblewrap.core.enums;

import bubblewrap.io.converters.FieldValueConverter;

/**
 * A FieldValue Converter to convert a enum value to an integer
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EnumValueConverter extends FieldValueConverter {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public EnumValueConverter() {
    super();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements FieldValueConverter">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: If value is {@linkplain #isValidInput(java.lang.Object) 
   * this.isValdiInput}m return null if value is null, the value if value is an Integer
   * or return ((Enum) value).ordinal.</p>
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
      } else if (value.getClass().isEnum()) {
        Enum enumVal = (Enum) value;
        result = enumVal.ordinal();
      }
    }
    return result;
  }
  
  /**
   * Convert value to String if (value != null)) and an instance of BwEnumFlag or
   * BwEnumFlagSet.
   * @param value
   * @return the String value or null if value = null.
   */
  @Override
  public String toStringValue(Object value) {
    String result = null;
    if ((value != null) && (value.getClass().isEnum())) {
      Enum enumVal = (Enum) value;
      result = Integer.toString(enumVal.ordinal());
    } 
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true if ((value == null) || (value instanceof Integer) || 
   * (value.getClass().isEnum()))</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return ((value == null) || (value instanceof Integer) || 
            (value.getClass().isEnum()));
  }
  //</editor-fold>
}
