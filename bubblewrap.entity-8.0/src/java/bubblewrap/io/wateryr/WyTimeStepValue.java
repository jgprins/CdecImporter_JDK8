package bubblewrap.io.wateryr;

import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeStep;
import bubblewrap.io.timeseries.TimeStepValue;
import bubblewrap.io.wateryr.enums.WYPeriod;
import org.json.JSONObject;

/**
 * A TimeStepValue that capture the WaterYear Day (i.e., the day index since the start of
 * the Water Year) and the steps {@linkplain WYPeriod}
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class WyTimeStepValue extends TimeStepValue {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the steps WyDay
   */
  protected Integer wyDay;
  /**
   * Placeholder for the steps WyPeriod
   */
  protected WYPeriod wyPeriod;
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * A Constructor with a TimeStep
   * @param timeStep
   */
  public WyTimeStepValue(TimeStep timeStep) {
    super(timeStep);
    DateTime stepDt = this.getDateTime();
    if (stepDt == null) {
      throw new NullPointerException("The TimeStepValue's DateTime is undefined.");
    }
    this.wyDay = WyConverter.toWyDay(stepDt);
    this.wyPeriod = WyConverter.toWyPeriod(stepDt);
  }
//</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the TimeStep WaterYear Day index (i.e., the day index between 0..365) since the
   * Water Year Start Date (Oct 1), with Oct 1 equaling wyDay=0.
   * @return the assigned WaterYeat Day based on this.timeStep
   */
  public Integer getWyDay() {
    return this.wyDay;
  }
  /**
   * Get the WYPeriod for the TimeStep. See {@link WyConvert#toWyPeriod(bubblewrap.io.datetime.DateTime) WyConverter.toWyPeriod} for details).
   * @return the assigned WaterYeat Day based on this.timeStep
   */
  public WYPeriod getWyPeriod() {
    return this.wyPeriod;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override TimeStepValue">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method and if target instance of WyTimeStepValue
   * assigned other.wyDay and other.wyPeriod based on other.dateTime</p>
   * @throws NullPointerException is other.dateTime is unassigned.
   */
  @Override
  protected <TValue extends TimeStepValue> void assignTo(TValue target) {
    super.assignTo(target);
    if ((target != null) && (target instanceof WyTimeStepValue)) {
      WyTimeStepValue other = (WyTimeStepValue) target;
      DateTime stepDt = other.getDateTime();
      if (stepDt == null) {
        throw new NullPointerException("The TimeStep or its Date is undefined.");
      }
      other.wyDay = WyConverter.toWyDay(stepDt);
      other.wyPeriod = WyConverter.toWyPeriod(stepDt);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: After de-serializing the super class' timeStep, initiate this.wyDay and
   * this.wyPeriod based on this.dateTime</p>
   */
  @Override
  protected void onFromJSON(JSONObject jsonObj) throws Exception {
    DateTime stepDt = this.getDateTime();
    if (stepDt == null) {
      throw new NullPointerException("The TimeStep or its Date is undefined.");
    }
    this.wyDay = WyConverter.toWyDay(stepDt);
    this.wyPeriod = WyConverter.toWyPeriod(stepDt);
  }
  // </editor-fold>
}
