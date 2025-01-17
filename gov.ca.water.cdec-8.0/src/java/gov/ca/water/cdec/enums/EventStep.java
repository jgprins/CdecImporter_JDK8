package gov.ca.water.cdec.enums;

import gov.ca.water.cdec.core.EventStepKey;

/**
 * The parameter past into the {@linkplain EventStepKey} to specify how the Date to
 * EventStepKey settings are rounded.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum EventStep {
  // <editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * TiemSTamp in Minutes (Default Options)
   */
  MINUTES(0, "Minutes"),
  /**
   * TimeStamp in Hours - ignore the minutes
   */
  HOURS(1, "Hours"),
  /**
   * TimeStamp in Days - ignore the time stamp part of date
   */
  DAYS(3, "Days");
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
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor
   *
   * @param value the option value
   * @param label the option label
   */
  private EventStep(int value, String label) {
    this.label = label;
    this.value = value;
  }
  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the EventStep associated with <tt>value</tt>
   *
   * @param value the EventStep.value to search for
   * @return the matching EventStep or MINUTES if not found.
   */
  public static EventStep fromValue(Integer value) {
    EventStep result = EventStep.MINUTES;
    if (value != null) {
      for (EventStep enumVal : EventStep.values()) {
        if (enumVal.value == value) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }
  // </editor-fold>
}
