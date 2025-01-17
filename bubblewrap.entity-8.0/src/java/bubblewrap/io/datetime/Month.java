package bubblewrap.io.datetime;

import bubblewrap.core.selectors.EnumSelector;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum Month {
  //<editor-fold defaultstate="collapsed" desc="Enum Values">
  Jan(0, "January", "Jan"),
  Feb(1, "February", "Feb"),
  Mar(2, "March", "Mar"),
  Apr(3, "April", "Apr"),
  May(4, "May", "May"),
  Jun(5, "June", "Jun"),
  Jul(6, "July", "Jul"),
  Aug(7, "August", "Aug"),
  Sep(8, "September", "Sep"),
  Oct(9, "October", "Oct"),
  Nov(10, "November", "Nov"),
  Dec(11, "December", "Dec");
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Enum Defintion">
  /**
   * The Month's date index [0..11]
   */
  public final int value;
  /**
   * The Month's full name (e.g., "January")
   */
  public final String longName;
  /**
   * The Month's short name (e.g., "Jan")
   */
  public final String shortName;
  /**
   * The Month-of-Year index [1..12]
   */
  public final int yearIndex;
  Month(int value, String longName, String shortName) {
    this.value = value;
    this.yearIndex = value+1;
    this.longName = longName;
    this.shortName = shortName;
  }
  
  /**
   * Get the Number of Days in the Month
   * @param year the Year of interest
   * @return the number of days in the month
   */
  public int daysInMonth(int year) {
    int result = 30;
    switch (this) {
      case Jan:
      case Mar:
      case May:
      case Jul:
      case Aug:
      case Oct:
      case Dec:
        result = 31;
        break;
      case Feb:
        result = 28;
        Double lastLeap = (Math.floor(1.0d* year/4.0d) * 4.0d);
        if (lastLeap.intValue() == year) {
          result += 1;
        }
        break;
    }
    return result;
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the AjFcastOption associated with <tt>value</tt>
   * @param value the AjFcastOption.value to search for
   * @return the matching AjFcastOption or AVERAGE if not found.
   */
  public static Month fromValue(int value) {
    Month result = null;
    if ((value < 0) || (value > 11)) {
      throw new IllegalArgumentException("Value[" + value + "] is out of bounds[0..11].");
    }
    for (Month enumVal : Month.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Enum Selector class">
  /**
   * An extension of EnumSelector
   */
  public static class Selector extends EnumSelector<Month> {
    
    private Boolean shortName;
    
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Parameterless Constructor (this.doShortName = false
     */
    public Selector() {
      this(false);
    }
    
    /**
     * Parameterless Constructor
     */
    public Selector(boolean doShortName) {
      super(Month.class);
      this.shortName = (!doShortName)? null: doShortName;
    }
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Override EnumSelector">
    @Override
    protected void onBuildSelectOptionList(List<SelectItem> selectItems,
                                 List<Month> selectOptions) throws Exception {
      boolean doShort = ((this.shortName != null) && (this.shortName));
      for (Month enumval : selectOptions) {
        if (doShort) {
          selectItems.add(new SelectItem(enumval.value, enumval.shortName));
        } else {
          selectItems.add(new SelectItem(enumval.value, enumval.longName));
        }
      }
    }
    // </editor-fold>
  }
}
