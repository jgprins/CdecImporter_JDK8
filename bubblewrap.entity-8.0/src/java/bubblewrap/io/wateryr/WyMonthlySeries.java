package bubblewrap.io.wateryr;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeInterval;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.timeseries.TimeSeries;

/**
 * A TimeStepValue that capture the WaterYear Day (i.e., the day index since the start of
 * the Water Year) and the steps {@linkplain WYPeriod}
 * @author rmarquez
 * @param <TValue>
 */
public abstract class WyMonthlySeries<TValue extends WyTimeStepValue> extends TimeSeries<TValue>  {
  // <editor-fold defaultstate="collapsed" desc="Public Static Fields">
  /**
   * The Minimum WaterYear limit (Default = 1900)
   */
  public static int minimumWaterYear = 1900;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private Integer waterYear;
  // </editor-fold>


  public WyMonthlySeries() {
    super(new TimeInterval(Interval.MONTHS, 1));
  }


  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called by inheritors to set the Series' WaterYear - which will set the series Start
   * and End Date. Skipped if this.waterYear = <tt>waterYr</tt>.
   * @param waterYr the Series' Water Year - valid range is [{@linkplain
   * #minimumWaterYear..Current WaterYear]}
   */
  protected void setWaterYear(int waterYr) {
    int curWy = WyConverter.getWaterYear(DateTime.getNow(WyConverter.PstTimeZone));
    if ((waterYr < WyTimeSeries.minimumWaterYear) || (waterYr > curWy)) {
      throw new IllegalArgumentException("The specified Water Year[" + waterYr
              + "] is invalid expected a value in range["
              + WyTimeSeries.minimumWaterYear + ".." + curWy + "].");
    }
    Integer newVal = waterYr;
    if (!DataEntry.isEq(this.waterYear, newVal)) {
      DateTime startDt = WyConverter.getWyStartDt(waterYr);
      DateTime endDt = WyConverter.getWyStartDt(waterYr+1);
      endDt = endDt.addDays(-1.0d);

      this.waterYear = waterYr;
      this.onSetTimeSeries(startDt, endDt);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Series' Water Year
   * @return the assigned year.
   */
  public int getWaterYear() {
    return (this.waterYear == null)? 0: this.waterYear;
  }

  /**
   * Get the TimeSeries value by its WaterYear Day designation
   * @param wyDay the day in the water year
   * @return the assigned value.
   */
  public TValue getWyTimeValue(int wyDay)  {
    DateTime stepDt = WyConverter.fromWyDay(wyDay, this.waterYear, this.getTimeZone());
    return this.getTimeValue(stepDt);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: return super.toString() + "\nwy=" + this.waterYear
   * </p>
   */
  @Override
  public String toString() {
    return super.toString() + "\nwy=" + this.waterYear;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Assign this.waterYear to target if target is instance of WyTimeSeries</p>
   */
  @Override
  public <TSeries extends TimeSeries<TValue>> void assignTo(TSeries target) {
    super.assignTo(target);
    if ((target != null) && (target instanceof WyTimeSeries)) {
      WyMonthlySeries other = (WyMonthlySeries) target;
      other.waterYear = this.waterYear;
    }
  }
  // </editor-fold>


}
