package bubblewrap.io.datetime;

import bubblewrap.core.selectors.EnumSelector;
import java.util.Calendar;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * Get a Enum for the Week Days (i.e., Monday - Friday, with a full name, an abbreviation
 * of the name (e.g., "Sun", "Mon"..) and a {@linkplain #getDayOfWeek dayOfWeek} property
 * that is relative to the assign {@linkplain #FIRST_DAY_OF_WEEK}. The latter should be
 * defined on an application level on startup.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum WeekDay {
  
  // <editor-fold defaultstate="expanded" desc="Public Enum Values">
  SUN(Calendar.SUNDAY, "Sunday", "Sun"),
  MON(Calendar.MONDAY, "Monday", "Mon"),
  TUE(Calendar.TUESDAY, "Tuesday", "Tue"),
  WED(Calendar.WEDNESDAY, "WednesDay", "Wed"),
  THU(Calendar.THURSDAY, "Thursday", "Thu"),
  FRI(Calendar.FRIDAY, "Friday", "Fri"),
  SAT(Calendar.SATURDAY, "Saturday", "Sat");
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * A Defined enum value (not its ordinate)
   */
  public final int value;
  /**
   * A Display label for the enum option
   */
  public final String label;
  /**
   * An abbreviation of the Day's name (e.g., "Sun", "Mon"..)
   */
  public final String abbr;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor  
   * @param value the option value
   * @param label the option label
   * @param abbr an abbreviation of the Day's name (e.g., "Sun", "Mon"..)
   */
  private WeekDay(int value, String label, String abbr) {
    this.label = label;
    this.value = value;
    this.abbr = abbr;
  }
  
  /**
   * Get the WeekDay's DayOfWeek index relative to {@linkplain #FIRST_DAY_OF_WEEK}. The
   * latter should be set on an application level.
   * @return the DayOfWeek index [1..7]
   */
  public final int getDayOfWeek() {
    int result = (this.value - WeekDay.FIRST_DAY_OF_WEEK.value);
    result = (result < 0)? (7 + result): result;
    result++;
    return result;
  }
  // </editor-fold>
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * The application constant to define which day should be used as the First Day of
   * the week in calculation each WeekDay's {@linkplain #getDayOfWeek() dayOfWeek} index.
   * This constant should be defined on an application level on startup.
   * Default = {@linkplain #MON}
   */
  public static WeekDay FIRST_DAY_OF_WEEK = WeekDay.MON;
  
  /**
   * Get the WeekDay associated with <tt>value</tt>. 
   * <p>
   * <b>NOTE:</b> The WeekDay.value's correspond to the Calender's Week Day constants
   * and the {@linkplain DateTime#getDayOfWeek()} values.</p>
   * @param value the WeekDay.value to search for
   * @return the matching WeekDay or SUN if not found.
   */
  public static WeekDay fromValue(int value) {
    WeekDay result = WeekDay.SUN;
    for (WeekDay enumVal : WeekDay.values()) {
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
  public static class Selector extends EnumSelector<WeekDay> {

    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Parameterless Constructor
     */
    public Selector() {
      super(WeekDay.class);
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Override EnumSelector">    
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Add the selectable items to the SelectItem List</p>
     */
    @Override
    protected void onBuildSelectOptionList(List<SelectItem> selectItems,
                                           List<WeekDay> selectOptions) throws Exception {

      for (WeekDay enumval : selectOptions) {
        selectItems.add(new SelectItem(enumval.value, enumval.label));
      }
    }
    // </editor-fold>
  }
  //</editor-fold>
}
