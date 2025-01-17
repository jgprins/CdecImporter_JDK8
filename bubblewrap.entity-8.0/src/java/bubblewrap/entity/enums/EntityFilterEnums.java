package bubblewrap.entity.enums;

/**
 * Enum values for Filter Conditions - used by EntityFilters
 * @author kprins
 */
public final class EntityFilterEnums {
  
  //<editor-fold defaultstate="collapsed" desc="Public Bitmap Enum Values">
  public static final int EQUAL = 1;
  public static final int GREATER = 2;
  public static final int GREATEROREQUAL = 3;
  public static final int NOT = 4;
  public static final int NOTEQUAL = 5;
  public static final int LESSOREQUAL = 6;
  public static final int LESS = 7;
  public static final int IN = 8;
  public static final int NOTIN = 12;
  public static final int LIKE = 16;
  public static final int NOTLIKE = 20;
  public static final int BETWEEN = 32;
  public static final int NOTBETWEEN = 36;
  //</editor-fold>
  
  /**
   * Check if the Condition includes the IN clause (IN or NOTIN)
   * @param condition the EntityFilterEnums
   * @return true if the IN clause is included.
   */
  public static boolean isIn(int condition) {
    return ((condition & EntityFilterEnums.IN) == EntityFilterEnums.IN);
  }
}
