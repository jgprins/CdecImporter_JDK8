package gov.water.cdec.reservoirs;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.http.session.HttpUtils;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.UnitConverter;
import bubblewrap.io.datetime.DateRange;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.datetime.DateTimeComparator;
import bubblewrap.io.schedules.TimeStep;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.wateryr.WyConverter;
import bubblewrap.io.wateryr.WyTimeSeries;
import bubblewrap.io.wateryr.enums.WyDate;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.ca.water.cdec.enums.DurationCodes;
import gov.water.cdec.reservoirs.annotations.*;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirTimeSeries extends WyTimeSeries<ReservoirTimeStepValue>{

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The reservoir's Core information
   */
  private ReservoirImpl reservoirDef;
  /**
   * The next date for which a record of the sensor has to be imported
   */
  private HashMap<ReservoirSensor, DateTime> nextDtMap;
  /**
   * The Last Observed Date - last date that has a full record - no missing values.
   */
  private DateTime lastObsDt;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * The EventHandler that fires the ObsDate Changed Event.
   */
  public final EventHandler ObsDateChanged;

  /**
   * Called to fire the ObsDate Changed Event.
   */
  protected final void fireObsDateChanged() {
    this.ObsDateChanged.fireEvent(this, new EventArgs());
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ReservoirTimeSeries(ReservoirDef reservoirDef) {
    super();  
    if ((this.reservoirDef = new ReservoirImpl(reservoirDef)) == null) {
      throw new NullPointerException("The ReservoirTimeSeries' Reservoir Definition "
              + "cannot be undefined.");
    }   
    this.ObsDateChanged = new EventHandler();
    this.nextDtMap = new HashMap<>();
    this.lastObsDt = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Get the current LastObsDt - it is Oct 1 if the date has not yet been set.
   * @return the currently assigned date.
   * @throws NullPointerException if this.waterYear = null.
   */
  private DateTime getLastObsDt() {
    if (this.lastObsDt ==null) {
      Integer waterYr = this.getWaterYear();
      if ((waterYr == null) || (waterYr == 0)) {
        throw new NullPointerException("The TimeSeries' Water Year is not true initiated");
      }
      this.lastObsDt = WyConverter.getWyStartDt(waterYr, WyConverter.PstTimeZone);
    }
    return lastObsDt;
  }
  
  /**
   * Set the new LastObsDt - it fires the {@linkplain #ObsDateChanged 
   * ObsDateChanged} event if the date has changed.
   * @param obsDt 
   */
  private void setLastObsDt(DateTime obsDt) {
    if ((obsDt != null) && (!DataEntry.isEq(this.lastObsDt, obsDt, Interval.DAYS))) {
      this.lastObsDt = obsDt;
      this.fireObsDateChanged();
    }
  }
  
  /**
   * Get the Sensor's Next Date to attempt to import an updated recordset.
   * @param sensor the ReservoirSensor
   * @return the saved date or null if not found
   */
  private DateTime getNextImportDt(ReservoirSensor sensor) {
    DateTime result = null;
    if ((sensor != null) && (this.nextDtMap.containsKey(sensor))) {
      result = this.nextDtMap.get(sensor);
    }
    return result;
  }
    
  /**
   * Set the Sensor's Next Date to attempt to import an updated recordset.
   * @param sensor the ReservoirSensor
   * @param nextDt the next Date (can be null)
   */
  private void setNextImportDt(ReservoirSensor sensor, DateTime nextDt) {
    if (sensor == null) {
      return;
    }
    
    if (nextDt == null) {
      if (this.nextDtMap.containsKey(sensor)) {
        this.nextDtMap.remove(sensor);
      }
    } else {
      this.nextDtMap.put(sensor, nextDt);
    }
  }
  
  /**
   * Called by Update to initiate the Import date Range and call {@linkplain 
   * #loadSensorData(gov.water.cdec.reservoirs.ReservoirSensor, 
   * bubblewrap.io.datetime.DateRange) this.loadSensorData} for each ReservoirSensor
   * @param now today's date
   * @return true if successful
   */
  private void loadObservedData(DateTime now, boolean force) {
    try {
      DateTime startDt = this.getLastObsDt();
      DateTime endDt = now.addTime(-1, TimeUnit.DAYS);
      DateRange dtRange = new DateRange(
                                  new DateTimeComparator(Interval.DAYS), startDt, endDt);
      
      DateTime nextDt = null;
      for (ReservoirSensor sensor : ReservoirSensor.values()) {
        if ((force) || ((nextDt = this.getNextImportDt(sensor)) == null) ||
                (now.isAfter(nextDt))) {
          if (this.loadSensorData(sensor, dtRange)) {
            nextDt = DateTime.toZeroHourInTimeZone(now, WyConverter.PstTimeZone);
            nextDt = nextDt.addTime(30, TimeUnit.HOURS);
          } else {
            nextDt = now.addTime(30, TimeUnit.MINUTES);
            DateTime noon = DateTime.toZeroHourInTimeZone(now, WyConverter.PstTimeZone);
            noon = noon.addTime(12, TimeUnit.HOURS);
            if (nextDt.isAfter(noon)) {
              nextDt = noon.addTime(18, TimeUnit.HOURS);
            }
          }
          this.setNextImportDt(sensor, nextDt);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.loadObservedData[res={1}] Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), this.reservoirDef.id(),
                exp.getMessage()});
    }
  }
  
  /**
   * Called to import the Date for a specified ReservoirSensor and date range
   * @param sensor the ReservoirSensor
   * @param dtRange the date range
   * @return true if successful
   */
  private boolean loadSensorData(ReservoirSensor sensor, DateRange dtRange) {
    boolean result = false;
    try {
      SensorImpl sensorDef = this.reservoirDef.getSensorDef(sensor);
      if (sensorDef == null) {
        throw new Exception("Teh Sensor Defintion is undefined or not supported");
      }
      URL importUrl = this.getImportURL(sensorDef, dtRange);
      String dataStr = this.onImportData(importUrl);
      if ((dataStr == null) || (DataEntry.isEq(dataStr, "[]", false))) {
        throw new Exception("The Improtted data set is empty");
      }
      
      DateRange importDates = this.onParseData(sensor, sensorDef, dataStr);
      result = ((importDates != null) && (!importDates.isEmpty()) &&
                (dtRange.getMax().equals(importDates.getMax(), Interval.DAYS)));
    } catch (Exception exp) {
      result = false;
      logger.log(Level.WARNING, "{0}.loadSensorData[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), sensor.field, 
                exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to generate the Import URL for importing the data from CDEC
   * @param sensorDef the Sensor Definition
   * @param dtRange the date range
   * @return the URL
   */
  private URL getImportURL(SensorImpl sensorDef, DateRange dtRange) {
    URL result = null;
    try {
      if (sensorDef == null) {
        throw new Exception("The Sensor Definition is undefined");
      }
      CdecSensorTypes sensorType= sensorDef.sensorType();
      DurationCodes durCode = sensorType.getDurationCode();
      String urlStr = durCode.importUrl;
      if (urlStr == null) {
        throw new Exception("DurationCodes[" + durCode.toString() +"]'s Import Url is "
                + "undefined or not supported.");
      }
      
      if ((dtRange == null) || (dtRange.isEmpty())) {
        throw new Exception("The Requests Date Range is not defined.");
      }
      
      Integer sensorNo = sensorType.sensorNo;
      String format = "yyyy-MM-dd";
      if ((durCode.equals(DurationCodes.H)) || (durCode.equals(DurationCodes.E))) {
        format += "'T'hh:mm:ss";
      } else if (durCode.equals(DurationCodes.M)) {
        format = "yyyy-MM";
      }
      String startDtStr = dtRange.getMin().toLocaleString(format);
      String endDtStr =  dtRange.getMax().toLocaleString(format);
      
      urlStr = HttpUtils.appendToUrl(urlStr, "Start", startDtStr);
      urlStr = HttpUtils.appendToUrl(urlStr, "End", endDtStr);
      urlStr = HttpUtils.appendToUrl(urlStr, "SensorNums", sensorNo.toString());
      urlStr = HttpUtils.appendToUrl(urlStr, "Stations",
              "'" + sensorDef.stationId() + "'");
      
      result = new URL(urlStr);
      if (result == null) {
        throw new Exception("The CDEC Import Url[" + urlStr + "] is invalid.");
      }
    } catch (Exception exp) {
      throw new RuntimeException("Get ImportUrl Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }
  
  /**
   * Import the Sensor's Data from CDEC using the specified importURL/
   * @param importUrl the sensor's URL
   * @return the imported (un-parsed) data string.
   */
  private String onImportData(URL importUrl) {
    String result = null;
    try {
      if (importUrl == null) {
        throw new Exception("The Import URL is undefined or accessible.");
      }
      HttpURLConnection conn = null;
      String dataStr = null;
      try {
        conn = (HttpURLConnection) importUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        
        InputStream inStream = conn.getInputStream();
        int respondCode = conn.getResponseCode();
        if (respondCode == HttpURLConnection.HTTP_OK) {
          int iRead = 0;
          byte[] readBuffer = new byte[4096];
          String subStr = null;
          
          /** Read the first line to validate that the data is correct **/
          iRead = inStream.read(readBuffer);
          if (iRead > 0) {
            subStr = new String(readBuffer, 0, iRead, "UTF-8");
            if ((subStr == null) || ((subStr.startsWith("<!DOCTYPE")))) {
              throw new Exception("Connection[" + importUrl.toString() + "] failed.");
            }
            dataStr = subStr;
          }
          
          if (dataStr != null) {
            while (true) {
              iRead = inStream.read(readBuffer);
              if (iRead <= 0) {
                break;
              }
              subStr = new String(readBuffer, 0, iRead, "UTF-8");
              if (subStr != null) {
                dataStr += subStr;
              }
            }
          }
        } else {
          throw new Exception("Connection[" + importUrl.toString() + "] failed.");
        }
        
        result = dataStr;
      } finally {
        if (conn != null) {
          conn.disconnect();
        }
      }
    } catch (Exception exp) {
      throw new RuntimeException("Import Data Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Date-Time format for the specified DurationCode
   * @param durCode the DurationCode to search for
   * @return the date/time format
   */
  private String getDateTimeFormat(DurationCodes durCode) {
    String result = null;
    if (DurationCodes.H.equals(durCode)) {
      result = "yyyy-MM-dd HH:mm:ss";
    } else if (DurationCodes.D.equals(durCode)) {
      result = "yyyy-MM-dd HH:mm";
    } else if (DurationCodes.M.equals(durCode)) {
      result = "yyyy-MM-dd HH:mm:ss";
    } else if (DurationCodes.E.equals(durCode)) {
      result = "yyyy-MM-dd HH:mm:ss";
    }
    return result;
  }
  
  /**
   * Parse an sensor's imported data string for each imported records and assign it to the
   * import date's TimeStepValue.
   * @return the date range for which valid data has been imported.
   */
  private DateRange onParseData(ReservoirSensor sensor, SensorImpl sensorDef, 
                                                                        String dataStr) {
    DateRange result = new DateRange(new DateTimeComparator(Interval.DAYS));
    try {
      DurationCodes durCode = sensorDef.getDurationCode();
      String dtFormat = this.getDateTimeFormat(durCode);
      if (dtFormat == null) {
        throw new Exception("Unable to resolve the Date-Time format for DurationCode["
        + durCode.toString() + "].");
      }
      
      JSONArray recArr = new JSONArray(dataStr);
      if ((recArr == null) || (recArr.length() == 0)) {
        throw new Exception("the Imported data set is empty");
      }
      
      TimeZone tz = WyConverter.PstTimeZone;
      ReservoirTimeStepValue timeStep = null;
      Integer sensorId = null;
      JSONObject record = null;
      String valStr = null;
      DateTime recDt = null;
      Double obsVal = null;
      Integer recCnt = 0;
      for (Object recObj : recArr.getArrayList()) {
        recCnt++;
        if ((!(recObj instanceof JSONObject)) || 
                                          ((record = (JSONObject) recObj) == null)) {
          continue;
        }
        try {
          if (((valStr = DataEntry.cleanString(record.optString("actualDate", null))) 
                  == null) || 
             ((recDt = DateTime.FromString(valStr, dtFormat, tz)) == null)) {
            throw new Exception("Undefined 'actualDate'");
          }
          
          if ((obsVal = record.optDouble("value", Double.NaN)).isNaN()) {
            obsVal = null;
          }
          
          if ((timeStep = this.getTimeValue(recDt)) == null) {
            throw new Exception("Unable to locate the TimeStep for Date["
                                                  + recDt.toLocaleString(dtFormat) + "");
          }
          timeStep.setValue(sensor, obsVal);
          if (obsVal != null) {
            result.grow(recDt);
          }
        } catch (Exception exp) {
          throw new Exception(this.getClass().getSimpleName()
                  + ".Parse Record Error:\n " + exp.getMessage() 
                  + "\n" + record.toString());
        }
      }
    } catch (Exception exp) {
      throw new RuntimeException("Parse Data Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called by {@linkplain #onValidate() this.onValidate} to update the AJ-TimeSteps
   * cumulative observed Apr-Jun runoff between Apr 1 and obsDt. On and after Aug1, the
   * observed Apr-Jun runoff is a constant value.
   * @param obsDt the current Observed Date
   */
  private void calcAjObs(DateTime obsDt) {
    Integer waterYr = null;
    Integer apr1WyDay = null;
    Integer aug1WyDay = null;
    Integer obsWyDay = null;
    if (((waterYr = this.getWaterYear()) != null) && 
            (obsDt != null) &&
            ((apr1WyDay = WyDate.Apr1.getWyDay(waterYr)) != null) &&
            ((obsWyDay = WyConverter.toWyDay(obsDt)) != null) &&
            ((aug1WyDay = WyDate.Aug1.getWyDay(waterYr)) != null) &&
            (obsWyDay >= apr1WyDay)){
      Double obsAj = 0.0d;
      ReservoirTimeStepValue timeStep = null;
      Double factor = UnitConverter.cfs.to(UnitConverter.acft_day, 0.001);
      Double lastValue = 0.0d;
      Double curValue = null;
      for (int wyDay = apr1WyDay; wyDay <= obsWyDay; wyDay++) {
        if ((timeStep = getWyTimeValue(wyDay)) == null) {
          continue;
        }

        if (wyDay >= aug1WyDay) {
          obsAj = (lastValue * factor);
        } else if (((curValue = timeStep.getValue(ReservoirSensor.FNF)) != null) &&
                (!curValue.isNaN())) {
          obsAj += (curValue * factor);
          lastValue = curValue;
        } else {
          obsAj += (lastValue * factor);
        }
        timeStep.setObsAj(obsAj);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
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
  
  /**
   * Called to check if the TimeSeries requires an update, and if true, it calls 
   * {@linkplain #loadObservedData(bubblewrap.io.datetime.DateTime) 
   * loadObservedData(today)} to execute the update. If successfully updates, it find 
   * and assigned the new last Observed Date, which will trigger the {@linkplain 
   * #ObsDateChanged ObsDateChanged} event.
   */
  public synchronized void update(boolean force) {
    if (this.isUpdating()) {
      if ((!this.waitWhileUpdating()) || (!force)) {
        return;
      }
    }
    
    try {
      this.startUpdating();
      DateTime now = DateTime.getNow(WyConverter.PstTimeZone);
      Integer waterYr = WyConverter.getWaterYear(now);
      Integer curWy = this.getWaterYear();
      if ((this.isEmpty()) || (curWy == null) || (curWy == 0) ||
              (!waterYr.equals(curWy))) {
        this.resetTimeSeries();
        this.setWaterYear(waterYr);
        force = true;
      }
        
      this.loadObservedData(now, force);      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.update Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endUpdating();
    }
  }
  
  /**
   * Get the Observed TimeStepValue for the specified <tt>requestDt</tt>. If (requestDt =
   * null) or after this.lastObsDt, return the timeStep for this.lastObsDt, else return
   * the timeStep for requestDt
   * @param requestDt the Request Date (null to get latest Observed Date)
   * @return the TimeStepValue or null if this.lastObsDt = null. 
   */
  public ReservoirTimeStepValue getObservedStepValue(DateTime requestDt) {
    ReservoirTimeStepValue result = null;
    DateTime stepDt = this.lastObsDt;
    if (requestDt != null) {
      if ((this.lastObsDt != null) && 
                                    (requestDt.isBefore(this.lastObsDt, Interval.DAYS))){
        stepDt = requestDt;
      }
    }
    if (stepDt != null) {
      result = this.getTimeValue(stepDt);
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="WyTimeSeries Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return "ReservoirTimeSeries[ res =" + this.reservoirDef.id() + "]";
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Update the this.lastObsDt - firing {@linkplain #ObsDateChanged
   * this.ObsDateChanged} event if the date has changed. It also calls {@linkplain 
   * #calcAjObs(bubblewrap.io.datetime.DateTime) this.calcAjObs} to update the 
   * cumulative AJ volume for all AJ and post-AJ time steps</p>
   */
  @Override
  protected void onValidate() throws Exception {
    DateTime obsDt = this.lastObsDt;
    try {
      DateTime today = DateTime.getNow(WyConverter.PstTimeZone);
      Integer wyDay = WyConverter.toWyDay(today);
      ReservoirTimeStepValue timeStep = null;
      while (wyDay >= 0) {
        if (((timeStep = this.getWyTimeValue(wyDay)) != null) &&
                (!timeStep.isMissingValue())) {
          obsDt = timeStep.getDateTime();
          break;
        }
        wyDay--;
      }
      
      this.calcAjObs(obsDt);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onValidate Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.setLastObsDt(obsDt);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Reset this.nextDtMap and this.lastObservedDt</p>
   */
  @Override
  protected void onResetTimeSeries() {
    this.nextDtMap.clear();
    this.lastObsDt = null;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return new ReservoirTimeStepValue(timeStep)</p>
   */
  @Override
  protected ReservoirTimeStepValue onNewTimeValue(TimeStep timeStep) {
    return new ReservoirTimeStepValue(timeStep);
  }
  // </editor-fold>
}
