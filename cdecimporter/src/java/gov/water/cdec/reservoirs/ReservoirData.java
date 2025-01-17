package gov.water.cdec.reservoirs;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.io.datetime.DateTime;
import gov.water.cdec.reservoirs.annotations.ReservoirDef;
import gov.water.cdec.reservoirs.annotations.ReservoirImpl;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * The latest complete reservoir observed record
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirData implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ReservoirData.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Reservoir Definitions
   */
  public final ReservoirImpl reservoidDef;
  /**
   * The Observed Data TimeSeries
   */
  private ReservoirTimeSeries timeSeries;
  /**
   * Placeholder for the ReservoirWsFcastMap reference
   */
  private ReservoirWsFcastMap wsFcastMap;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ReservoirData(ReservoirDef reservoidDef) {
    super();  
    if ((this.reservoidDef = new ReservoirImpl(reservoidDef)) == null) {
      throw new NullPointerException("The ReservoirData's Reservoir Definition is"
              + " unassigned");
    }
    this.timeSeries = new ReservoirTimeSeries(reservoidDef);
    this.wsFcastMap = ReservoirWsFcastMap.getInstance();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public isUpdating State Management">
  /**
   * Transient counter for managing the isUpdating state
   */
  private transient int countUpdate = 0;

  /**
   * Get the current isUpdating state
   * @return true if (this.countUpdate > 0)
   */
  public final boolean isUpdating() {
    return (this.countUpdate > 0);
  }

  /**
   * Called to start the Update process. It increment this.countUpdate.
   * <p>
   * <b>NOTE:</b> Every call to beginUpdate must be followed by a call to {@linkplain
   * #endUpdate() this.endUpdate}.</p>
   */
  private void beginUpdate() {
    this.countUpdate = (this.countUpdate < 0) ? 0 : this.countUpdate;
    this.countUpdate++;
  }

  /**
   * Called - after calling {@linkplain #beginUpdate() this.beginUpdate} - to decrement
   * this.countUpdate and to call {@linkplain #onUpdateEnds() this.onUpdateEnds} to
   * process the event.
   */
  private void endUpdate() {
    if (this.countUpdate > 0) {
      this.countUpdate--;
      if (this.countUpdate == 0) {
        this.onUpdateEnds();
      }
    }
  }

  /**
   * Calls to execute any post-process after the ReservoirData's settings have changed
   * (e.g. fire an event).
   */
  protected void onUpdateEnds() {
//    if (this.obsDate == null) {
//      this.onObsDateChanged();
//    }
  }
  
  /**
   * Called by {@linkplain #update(boolean) this.update} while the record is in the 
   * process of being updated until the former process has been completed. It waits for 
   * 120 - 0.5 second intervals (total of 60 seconds)
   * @return true is waited for less than 60 seconds
   */
  private synchronized boolean waitWhileUpdating() {
    int incCnt = 0;
    while ((this.isUpdating()) && (incCnt < 120)) {
      try {
        this.wait(500l);
      } catch (Exception exp) {
        break;
      }
      incCnt++;
    }
    return (incCnt < 120);
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called before retrieving the Reservoir profile data to update the timeseries and
   * forecasted data. If <tt>force</tt> = ture if will force a partial reload of the data
   * regardless of any wait time.
   * @param force true to force a reload.
   */
  public synchronized void update(boolean force) {
    if (this.isUpdating()) {
      if ((!this.waitWhileUpdating()) || (!force)) {
        return;
      }
    }
    
    try {
      this.beginUpdate();
      this.timeSeries.update(force);
      this.wsFcastMap.update(force);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.update Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endUpdate();
    }
  }
  /**
   * Called to get the JSON formatted data of this ReservoirData record for a specified
   * request Date. If <tt>requestDt</tt> = null | after this.timeSeries.lastObservedDt,
   * the record for this.timeSeries.lastObservedDt will be returned.. 
   * <p>
   * <b>NOTE:</b> Call {@linkplain #update(boolean) this.update(force)} before calling 
   * this method to ensure the latest data has been imported.</p>
   * <p>
   * @param json the JSON Object to update
   * @param requestDt the requested Time Step Date
   * @return the formatted JSON = an empty object is this.obsDate = null;
   */
  public synchronized void toJson(JSONObject json, DateTime requestDt) {
    if (json == null) {
      throw new NullPointerException("The JSON Object to update is unassigned");
    }
    if (this.isUpdating()) {
      this.waitWhileUpdating();
    }
    
    String dtFormat = "M/d/yyyy";
    Object nullValue = null;
    Double obsVal = null;
    Double obsAj = null;
    Double storVal = null;
    Double tocVal = null;
    String field = null;
    json.put("id", this.reservoidDef.id());
    json.put("name", this.reservoidDef.name());
    json.put("rescap", this.reservoidDef.capacity());
    ReservoirTimeStepValue timeValue = this.timeSeries.getObservedStepValue(requestDt);
    if (timeValue != null) {      
      json.put("obsdate", timeValue.getDateTime().toLocaleString(dtFormat));
      for (ReservoirSensor sensor : ReservoirSensor.values()) {
        if (((obsVal =  timeValue.getValue(sensor)) == null) || (obsVal.isNaN())) {
          json.put(sensor.field, nullValue);
        } else {    
          obsVal = obsVal * sensor.scaleFactor;
          json.put(sensor.field, obsVal);
        }
      }
      
      field = "perc_stor";
      if (((storVal =  timeValue.getValue(ReservoirSensor.STOR)) == null) || 
              (storVal.isNaN()) || (this.reservoidDef.capacity() <= 0.0d)) {
        json.put(field, nullValue);
        storVal = null;
      } else {
        storVal = storVal * ReservoirSensor.STOR.scaleFactor;
        obsVal = (storVal/this.reservoidDef.capacity())*100;
        json.put(field, obsVal);
      }
      
      field = "encr";
      if ((storVal == null) || 
              ((tocVal =  timeValue.getValue(ReservoirSensor.TOC)) == null) || 
              (tocVal.isNaN())) {
        json.put(field, nullValue);
      } else {
        tocVal = tocVal * ReservoirSensor.TOC.scaleFactor;
        json.put(field, (storVal - tocVal));
      }
      
      obsAj = timeValue.getObsAj();
    }
    
    B120WsFcast fcastData = this.wsFcastMap.getWsFcast(this.reservoidDef.id(), requestDt);
    if (fcastData != null) {   
      obsAj = (obsAj == null)? 0.0: obsAj;      
      json.put("fcastdt", fcastData.fcastDt.toLocaleString(dtFormat));
      json.put("obsAj", obsAj);
      
      field = "aj50";
      if (fcastData.fcastMed == null) {
        json.put(field, nullValue);
      } else {
        json.put(field, (fcastData.fcastMed - obsAj));
      }
      field = "aj90";
      if (fcastData.fcast90 == null) {
        json.put(field, nullValue);
      } else {
        json.put(field, (fcastData.fcast90 - obsAj));
      }
      field = "aj10";
      if (fcastData.fcast10 == null) {
        json.put(field, nullValue);
      } else {
        json.put(field, (fcastData.fcast10 - obsAj));
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
}
