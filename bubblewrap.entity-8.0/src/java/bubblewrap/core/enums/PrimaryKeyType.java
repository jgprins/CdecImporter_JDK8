package bubblewrap.core.enums;

import bubblewrap.core.annotations.EnumLabel;
import java.util.Date;

/**
 * The definition of an ENtity's PrimaryKey type
 * @author kprins
 */
public enum PrimaryKeyType {
  
  //<editor-fold defaultstate="expanded" desc="Public static enum values">
  /**
   * Automatic incremented Integer ID field. [{@value}]
   */
  AUTOINTEGER("Automatic Numbering Integer Primary Key"),
  /**
   * Automatic incremented Long ID field. [{@value}]
   */
  AUTOLONG("Automatic Numbering Long Primary Key"),
  /**
   * Automatic assigned GUID ID fields. [{@value}]
   */
  GUID("GUID Primary Key"),
  /**
   * A Date (java.util.date) Primary Key. [{@value}]
   */
  DATETIME("Date-Time Primary Key"),
  /**
   * A composite Primary Key of defined by its return type - required on submit. 
   * [{@value}]
   */
  COMPOSIT("A composite Primary Key of defined by its return type - "
          + "required on submit"),
  /**
   * A Manually assigned Primary Key of any type - required on submit [{@value}]
   */
  MANUAL("Manually assigned Primary Key - required on submit");
  //</editor-fold>  
  
  public final String label;
  private PrimaryKeyType(String label) {
    this.label = label;
  }

  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Called to get the Default PrimaryKeyType for a specified field return type
   * @param fieldType the field return type
   * @return the default PrimaryKeyType
   */
  public static PrimaryKeyType getDefaultPrimaryKeyType(Class<?> fieldType) {
    PrimaryKeyType result = PrimaryKeyType.MANUAL;
    if (fieldType != null) {
      if (Integer.class.equals(fieldType)) {
        result = PrimaryKeyType.AUTOINTEGER;
      } else if (Long.class.equals(fieldType)) {
        result = PrimaryKeyType.AUTOLONG;
      } else if (String.class.equals(fieldType)) {
        result = PrimaryKeyType.GUID;
      } else if (Date.class.equals(fieldType)) {
        result = PrimaryKeyType.DATETIME;
      }
    }
    return result;
  }
  
  /**
   * Get whether the specified PrimaryKey Type is an auto-recorded ID, instead
   * of being a manual assigned that cannot be null on submit.
   * @param keyType PrimaryKeyType
   * @return true if auto-assigned
   */
  public static boolean isAutoRecorded(PrimaryKeyType keyType) {
    boolean result = false;
    switch (keyType){
      case AUTOINTEGER:
      case AUTOLONG:
      case GUID:
      case DATETIME:
        result = true;
        break;
    }
    return result;
  }
  //</editor-fold>
}
