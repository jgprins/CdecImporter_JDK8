package gov.water.cdec.reservoirs;

import bubblewrap.io.schedules.TimeStep;
import bubblewrap.io.timeseries.TimeStepValue;
import bubblewrap.io.wateryr.WyTimeStepValue;
import java.util.HashMap;
import org.json.JSONObject;

/**
 * A WyTimeStepValue for storing the ReservoirTimeSeries daily record values
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirTimeStepValue extends WyTimeStepValue {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A HashMap containing the assigned observed data for the timeStep
   */
  private HashMap<ReservoirSensor, Double> valueMap;
  /**
   * The cumulative Observed Apr-Jul runoff for this date - 0.0 if Pre-AJ - 
   * a fixed value if post-AJ
   */
  private Double obsAj;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ReservoirTimeStepValue(TimeStep timeStep) {
    super(timeStep); 
    this.valueMap = new HashMap<>();
    this.obsAj = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Call to set the Sensor Value
   * @param sensor the ReservoirSensor (cannot be null)
   * @param value the sensor value (can be null)
   */
  protected void setValue(ReservoirSensor sensor, Double value) {
    if (sensor == null) {
      throw new NullPointerException("The value Sensor key is undefined.");
    }
    if ((value == null) || (value.isNaN())) {
      if (this.valueMap.containsKey(sensor)) {
        this.valueMap.remove(sensor);
      }
    } else {
      this.valueMap.put(sensor, value);
    }
  }
  /**
   * Call to get the Sensor Value. 
   * <p>
   * <b>NOTE:</b> If FNF is undefined substitute FNF for QIN and vise versa</p>
   * @param sensor the ReservoirSensor (cannot be null)
   * @return the value (can be null)
   */
  public Double getValue(ReservoirSensor sensor) {
    Double result = null;
    if ((sensor != null) && (this.valueMap.containsKey(sensor))) {
      result = this.valueMap.get(sensor);
    }
    
    /*
     * Substitute QIN for FNF if FNF is undefined and vise versa
     */
    if (result == null) {
      if ((ReservoirSensor.FNF.equals(sensor)) &&
              (this.valueMap.containsKey(ReservoirSensor.QIN))) {
        result = this.valueMap.get(ReservoirSensor.QIN);
      } else if ((ReservoirSensor.QIN.equals(sensor)) &&
              (this.valueMap.containsKey(ReservoirSensor.FNF))) {
        result = this.valueMap.get(ReservoirSensor.FNF);
      }
    }
    return result;
  }
  
  /**
   * Set the TimeStep's Observed cumulative Apr-Jul Runoff (taf)
   * @param obsAj the runoff in taf.
   */
  protected void setObsAj(Double obsAj) {
    this.obsAj = obsAj;
  }
  
  /**
   * Get the TimeStep's Observed cumulative Apr-Jul Runoff (taf)
   * @return this.obsAj or 0.0 if undefined
   */
  public Double getObsAj() {
    return (this.obsAj == null)? 0.0d:  this.obsAj;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="WyTimeStepValue Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Clear this.valueMap</p>
   */
  @Override
  protected void onReset() throws Exception {
    this.valueMap.clear();
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return true if one of this.getValue(sensor) == null.</p>
   */
  @Override
  public boolean isMissingValue() {
    boolean result = this.valueMap.isEmpty();
    if (!result) {
      for (ReservoirSensor sensor : ReservoirSensor.values()) {
        if (this.getValue(sensor) == null) {
          result = true;
          break;
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return this.equals(target)</p>
   */
  @Override
  public <TValue extends TimeStepValue> boolean isValue(TValue target) {
    return (this.equals(target)); 
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Does nothing</p>
   */
  @Override
  protected void onToJSON(JSONObject jsonObj) throws Exception {
    
  }
  // </editor-fold>
}
