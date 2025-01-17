package gov.water.cdec.reservoirs;

import bubblewrap.http.session.HttpUtils;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.wateryr.WyConverter;
import gov.water.cdec.reservoirs.annotations.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A {@linkplain DataImporter} that import the B-120 Water Supply (WS) Forecast data using
 * the WSFcast Tool's Web Service.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirWsFcastMap implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ReservoirWsFcastMap.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="ReservoirWsFcastMap Singleton">
  /**
   * Static class for holding and initiating the ReservoirWsFcastMap singleton in.
   */
  private static class B120WsFcastDataMapHolder {

    private static final ReservoirWsFcastMap INSTANCE = new ReservoirWsFcastMap();
  }

  /**
   * Static method for accessing the Singleton
   * @return B120WsFcastDataMapHolder.INSTANCE
   */
  public static ReservoirWsFcastMap getInstance() {
    return B120WsFcastDataMapHolder.INSTANCE;
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private WsFcast Class">
  private static class WsFcast {

    public final DateTime fcastDt;
    public final JSONObject data;

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public WsFcast(DateTime fcastDt, JSONObject data) {
      super();      
      this.fcastDt = fcastDt;
      this.data = data;
    }
    // </editor-fold>
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Fields">
  /**
   * The RecordId of the B-120 WsFcastSource
   */
  public static final int SOURCE_ID = 1;
  /**
   * The URL for the WSFcast Tool's web service
   * (e.g. http://cdec.water.ca.gov/preciptemp/req/wsfcast?wy=2017&from=3&to=6
   */
  public static final String importUrl = 
                                      "http://cdec.water.ca.gov/preciptemp/req/wsfcast";
//                            "http://webdev01.geiconsultants.com/wsfcast/ws/b120/wsfcast";
//                              "http://localhost:8081/wsfcast/ws/b120/wsfcast";
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the cached reservoirDefMap
   */
  private HashMap<String, ReservoirImpl> reservoirDefMap;
  /**
   * Placeholder for the cached fcastDataMap
   */
  private HashMap<String, B120WsFcastMap> fcastDataMap;
  /**
   * The Last Date that an update
   */
  private DateTime lastUpdateDt;
  /**
   * The Forecast WaterYear
   */
  private Integer waterYr;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  private ReservoirWsFcastMap() {
    super();  
    this.reservoirDefMap = null;
    this.fcastDataMap = new HashMap<>();
    this.lastUpdateDt = null;
    this.waterYr = null;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.fcastDataMap.clear();    
  }
  
  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called by {@linkplain #onExecute(com.gei.fco.importers.DataImportRequest) 
   * this.onExecute} to import the B-120 WSFcast data from the WSFcast Tool web service.
   * @param fcastWy the specified Water Year (assume the current WaterYear if null)
   * @param monthRange the specified Month Range (assumed YTD if null and fcastWy is the 
   * current Water Year or Month 1-6 if null and not the current WY)
   * @param args the request arguments
   * @return the result as an JSON Array.
   */
  private JSONArray importWsFcast(Integer fcastWy) {
    JSONArray result = null;
    try {
      String reqStr = ReservoirWsFcastMap.importUrl;
      //reqStr = HttpUtils.appendToUrl(reqStr,"actionid","wsfcast");
      if (fcastWy != null) {
        reqStr = HttpUtils.appendToUrl(reqStr,"wy",fcastWy.toString());
      }
      
      String dataStr = null;
      URL reqUrl = new URL(reqStr);      
      HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "text/json");
      try {
        conn.connect();
        int conCode = conn.getResponseCode();
        if (conCode != HttpURLConnection.HTTP_OK) {
          String errMsg = conn.getResponseMessage();
          errMsg = (errMsg == null)? 
                        "Unable to connect to WSFcast Server - cause unknown.": errMsg;
          throw new Exception(errMsg);
        } else {
          try (BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String inLine  = null;
            dataStr = "";
            while ((inLine = reader.readLine()) != null) {
              dataStr += inLine;
            }
          }
        }
      } finally {
        conn.disconnect();
      }
      
      String errMsg = null;
      JSONObject resultObj = null;
      if ((dataStr != null) && (dataStr.startsWith("{")) && (dataStr.contains("Error")) &&
              ((resultObj = new JSONObject(dataStr)) != null) &&
              ((errMsg = DataEntry.cleanString(resultObj.optString("Error",null)))
                != null)) {
        throw new Exception(errMsg);
      }
      
      result = new JSONArray(dataStr);
      if ((result == null) || (result.length() == 0)) {
        throw new Exception("The Imported Forecast is empty or in an invalid JSONArray "
                + "format.");
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.importWsFcast Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to Extract the Latest Forecast from the Water Year
   * @param fcastSource the forecast source to update
   * @param basinFcasts the imported basin forecasts
   * @param args the request arguments
   */
  private List<WsFcast> getLoadedFcasts(JSONArray importArray) {
    List<WsFcast> result = new ArrayList<>();
    try {
      JSONObject fcastData = null;
      TimeZone tz = WyConverter.PstTimeZone;
      for (int iFcast = 0; iFcast < importArray.length(); iFcast++) {
        if (((fcastData = importArray.optJSONObject(iFcast)) == null) ||
                (fcastData.length() == 0)) {
          continue;
        }
        
        DateTime fcastDt = null;
        String strVal = DataEntry.cleanString(fcastData.optString("fcastDt", null));
        if ((strVal == null) || 
            ((fcastDt = DateTime.FromString(strVal, "MM/dd/yyyy", tz))
                == null)) {
          throw new Exception("Invalid FcastDt: WsFcast Input:\n " 
                  + fcastData.toString());
        }
        
        WsFcast data = new WsFcast(fcastDt, fcastData);
        result.add(data);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getLoadedFcasts Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Parse the B120WsFcast form the specified list of fcastData for the wsFacstId's 
   * defined in this.resIdMap
   * @param fcastDataList the list of WsFcast data to parse.
   */
  private void parseFcastData(List<WsFcast> fcastDataList) {
    if ((fcastDataList == null) || (fcastDataList.isEmpty())) {
      return;
    }
    try {
      HashMap<String, String> resIdMap = this.getResIdMap();
      for (WsFcast fcastData : fcastDataList) {
        DateTime fcastDt = fcastData.fcastDt;
        JSONArray basinFcasts = fcastData.data.optJSONArray("basinFcasts");
        if ((basinFcasts == null) || (basinFcasts.length() == 0)) {
          throw new Exception("The Forecast's Basin Forecasts is empty");
        }

        JSONObject basin = null;
        String staId = null;
        Double ajMed = null;
        Double aj10 = null;
        Double aj90 = null;
        for (int item = 0; item < basinFcasts.length(); item++) {
          if (((basin = basinFcasts.getJSONObject(item)) == null) ||
            ((staId = DataEntry.cleanUpString(basin.optString("stationId", ""))) == null)
            || (!resIdMap.containsKey(staId))) {
            continue;
          }

          String resId = resIdMap.get(staId);        
          if ((aj90 = basin.optDouble("aj90p", Double.NaN)).isNaN()) {
            aj90 = null;
          }
          if ((ajMed = basin.optDouble("ajMed", Double.NaN)).isNaN()) {
            ajMed = null;
          }

          if ((aj10 = basin.optDouble("aj10p", Double.NaN)).isNaN()) {
            aj10 = null;
          }

          B120WsFcast data = new B120WsFcast(resId, fcastDt, aj90, ajMed, aj10);
          B120WsFcastMap fcastMap = this.getResFcastMap(resId);
          if (fcastMap != null) {
            fcastMap.add(data);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.parseFcastData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Set the new Last Forecast Date - it fires the {@linkplain #FcastDateChanged 
   * FcastDateChanged} event if the date has changed
   * @param lastFcastDt the new date
   */
  public B120WsFcastMap getResFcastMap(String resId) {
    if (this.fcastDataMap == null) {
      this.fcastDataMap = new HashMap<>();
    }
    B120WsFcastMap result = null;
    if (this.fcastDataMap.containsKey(resId)) {
      result = this.fcastDataMap.get(resId);
    }
    
    if (result == null) {
      result = new B120WsFcastMap();
      this.fcastDataMap.put(resId, result);
    }
    return result;
  }
  
  /**
   * Get the HashMap[wsFcastId->resId] from this.reservoirDefMap
   * @return the hashMap
   */
  private HashMap<String, String> getResIdMap() {
    HashMap<String, ReservoirImpl> resDefMap = this.getReservoirDefMap();
    HashMap<String, String> result = new HashMap<>();
    for (ReservoirImpl resDef : resDefMap.values()) {
      result.put(resDef.wsFcastId(), resDef.id());
    }
    return result;
  }
  
  /**
   * Get the Cached reservoirDefMap - initiated from the ReservoirDataMap.class annotation
   * @return this.reservoirDefMap
   */
  private HashMap<String, ReservoirImpl> getReservoirDefMap() {
    if (this.reservoirDefMap == null)  {
      try {
        this.reservoirDefMap = new HashMap<>();
        Class resMapClass = ReservoirDataMap.class;
        ReservoirDefs annotArr = null;
        if ((!resMapClass.isAnnotationPresent(ReservoirDefs.class)) ||
            ((annotArr = (ReservoirDefs) 
                                resMapClass.getAnnotation(ReservoirDefs.class)) == null) ||
            (annotArr.value() == null) || (annotArr.value().length == 0)) { 
          throw new Exception("The ReservoirInfoMap's ReservoirDefs annotaion is not "
                  + "accessible or is undefined or empty");
        }

        ReservoirImpl annotImpl = null;
        for (ReservoirDef annot : annotArr.value()) {
          try {
            if ((annotImpl = new ReservoirImpl(annot)) == null) {
              throw new Exception("Loading ReservoirDef[" + annot.id() + "] failed.");
            }

            if (this.reservoirDefMap.containsKey(annotImpl.id())){
              throw new Exception("Duplicate ReservoirDef[" + annot.id() + "].");
            }

            this.reservoirDefMap.put(annotImpl.id(), annotImpl);          
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.read ReservoirDefs Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }         
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.method Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.reservoirDefMap;
  }
  
  private void temp() {
    DateTime fcastDt = new DateTime(2017, 3, 11, WyConverter.PstTimeZone);
    B120WsFcastMap fcastMap = null;
    B120WsFcast fcast = null;
    String resId = "SHA";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt, 2270.0d, 2580.0d, 3250.0d);
      fcastMap.add(fcast);
    }
    resId = "ORO";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt, 2880.0d, 3320.0d, 4020.0d);
      fcastMap.add(fcast);
    }

    resId = "FOL";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt, 2280.0d, 2520.0d, 3020.0d);
      fcastMap.add(fcast);
    }

    resId = "NML";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt,  1280.0d, 1400.0d, 1630.0d);
      fcastMap.add(fcast);
    }

    resId = "DNP";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt,  2160.0d, 2310.0d, 2630.0d);
      fcastMap.add(fcast);
    }

    resId = "EXC";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt,  1120.0d, 1210.0d, 1410.0d);
      fcastMap.add(fcast);
    }

    resId = "MIL";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt,  2230.0d, 2410.0d, 2690.0d);
      fcastMap.add(fcast);
    }

    resId = "PNF";
    if (((fcastMap = this.getResFcastMap(resId)) != null) && 
            (!fcastMap.contains(fcastDt))) {
      fcast = new B120WsFcast(resId, fcastDt, 2200.0d, 2340.0d, 2580.0d);
      fcastMap.add(fcast);
    } 
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to update the forecast
   */
  public void update(boolean force) {   
    try {
      DateTime today = DateTime.getNow(WyConverter.PstTimeZone);
      Integer curWy = WyConverter.getWaterYear(today);
      if ((this.waterYr == null) || ((!this.waterYr.equals(curWy)))) {
        this.lastUpdateDt = null;
        this.fcastDataMap.clear();
        force = true;
      }
      
      if ((!force) && (this.lastUpdateDt != null) && 
                                  (!today.isAfter(this.lastUpdateDt, Interval.DAYS))) {
        return;
      }
      
      
      List<WsFcast> fcastData = null;
      JSONArray importData = this.importWsFcast(waterYr);
      if ((importData != null) && 
              ((fcastData = this.getLoadedFcasts(importData)) != null) &&
              (!fcastData.isEmpty())){
        this.parseFcastData(fcastData);
      }      
      
      //this.temp();
      
      this.lastUpdateDt = today;
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.update Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }    
  }
  
  /**
   * Get the reservoir's B120WsFacstMap
   * @param resId the reservoir
   * @param requestDt the request date for which to get the latest forecast
   * @return the B120WsFcast or null if not found
   */
  public B120WsFcast getWsFcast(String resId, DateTime requestDt) {
    B120WsFcast result = null;
    if (((resId = DataEntry.cleanString(resId)) != null) || 
            (!this.fcastDataMap.isEmpty()) || (this.fcastDataMap.containsKey(resId))) {
      B120WsFcastMap fcastMap = this.fcastDataMap.get(resId);
      result = fcastMap.getLastFcast(requestDt);
    }
    return result;
  }
  // </editor-fold>
}
