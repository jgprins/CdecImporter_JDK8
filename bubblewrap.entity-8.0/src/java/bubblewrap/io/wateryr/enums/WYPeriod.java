package bubblewrap.io.wateryr.enums;

import bubblewrap.io.IntegerRange;

/**
 * The Period of a Water Year - from a SnowMelt prospective
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum WYPeriod {
  // <editor-fold defaultstate="expanded" desc="Enum Values">
  PreAJ(0,"Pre-AJ Perio") {
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: return IntegerRange(Oct 1..March 30)</p>
     */
    @Override
    public IntegerRange getWyDayRange(int waterYr) {
      int startDay = WyDate.Oct1.getWyDay(waterYr);
      int endDay = WyDate.Apr1.getWyDay(waterYr)-1;
      return new IntegerRange(startDay, endDay);
    }
  },
  AJ(1,"AJ Period") {
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: return IntegerRange(Apr 1..July 31)</p>
     */
    @Override
    public IntegerRange getWyDayRange(int waterYr) {
      int startDay = WyDate.Apr1.getWyDay(waterYr);
      int endDay = WyDate.Aug1.getWyDay(waterYr)-1;
      return new IntegerRange(startDay, endDay);
    }
  },
  PostAJ(2, "Post-AJ Period") {
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: return IntegerRange(Aug 1..Sep 30)</p>
     */
    @Override
    public IntegerRange getWyDayRange(int waterYr) {
      int startDay = WyDate.Aug1.getWyDay(waterYr);
      int endDay = WyDate.Sep30.getWyDay(waterYr);
      return new IntegerRange(startDay, endDay);
    }
  };
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
   * @param value the option value
   * @param label the option label
   */
  private WYPeriod(int value, String label) {
    this.label = label;
    this.value = value;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Method">
  /**
   * ABSTRACT: Get the  WYPeriod's WaterYear Day range for the specified waterYr.
   * @param waterYr the specified waterYr
   * @return the WyDay Range
   */
  public abstract IntegerRange getWyDayRange(int waterYr);
//</editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the WYPeriod associated with <tt>value</tt>
   * @param value the WYPeriod.value to search for
   * @return the matching WYPeriod or null if not found.
   */
  public static WYPeriod fromValue(Integer value) {
    WYPeriod result = null;
    if (value != null) {
      for (WYPeriod enumVal : WYPeriod.values()) {
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
