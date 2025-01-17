package bubblewrap.core.enums;

/**
 * A set of enum value to identify an Entity's Primary Key generation type
 * @author kprins
 */
public class PrimaryKeyTypeEnums {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Enum Valies">
  /**
   * An Auto Number (Integer) Primary Key [{@value}]
   */
  public static final int AUTONUMBER = 0;
  /**
   * An Unique ID (String[32]) Primary Key [{@value}]
   */
  public static final int GUID = 1;
  /**
   * A Date (java.util.date) Primary Key [{@value}]
   */
  public static final int DATETIME = 2;
  //</editor-fold>
}
