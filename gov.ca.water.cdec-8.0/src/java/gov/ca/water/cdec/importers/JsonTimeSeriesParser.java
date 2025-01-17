package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.enums.DurationCodes;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A TimeSeriesDataParser for Parsing TimeSeries Data from SHEF-format text stings. It currently 
 * only supports SHEF.A formats.
 * @author kprins
 */
public class JsonTimeSeriesParser<TEntity extends Serializable,
                                  TStepKey extends TimeStepKey<TStepKey>,
                                  TMap extends TimeSeriesMap<TEntity, TStepKey>> 
                                  extends TimeSeriesDataParser<TEntity, TStepKey, TMap> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Get the Date-Time format for the specified DurationCode
   * @param durCode the DurationCode to search for
   * @return the date/time format
   */
  protected static String getDateTimeFormat(DurationCodes durCode) {
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
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for TimeZone of the downloaded data (optional - can be null)
   */
  private TimeZone timeZone;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor 
   */
  public JsonTimeSeriesParser() {
    this(null);
  }
  
  /**
   * Overload 2:Public Constructor with a TimeZone reference
   * @param timeZone the TimeZone of the downloaded data (can be null to accept
   * the default={@linkplain #defaultTimeZone})
   */
  public JsonTimeSeriesParser(TimeZone timeZone) {
    super();
    this.timeZone = timeZone;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="TimeSeriesDataParser Override">
  @Override
  protected void onParseData(String dataStr, TMap timeSeriesMap, 
                                        CdecSensorInfo sensorInfo) throws Exception {
    
    Date actualDt = null;
    Double obsValue = null;
    String flag = null;
    Integer sensorId = null;
    Date obsDt = null;

    try {
      DurationCodes durCode = sensorInfo.sensorType.getDurationCode();
      String dtFormat = JsonTimeSeriesParser.getDateTimeFormat(durCode);
      if (dtFormat == null) {
        throw new Exception("Unable to resolve the Date-Time format for DurationCode["
        + durCode.toString() + "].");
      }
      
      boolean hasObsData = ((durCode.equals(DurationCodes.D)) || 
                               (durCode.equals(DurationCodes.M)));
      
      
      JSONArray parsedObjs = null;   
      if (((dataStr = ImportUtils.cleanString(dataStr)) == null) ||
          ((parsedObjs = new JSONArray(dataStr)) == null) || 
          (parsedObjs.length() == 0)) {
        return;
      }
      
      JSONObject jsonObj = null;
      String valStr = null;
      Integer recCnt = 0;
      for (int index = 0; index < parsedObjs.length(); index++) {
        if (((jsonObj = parsedObjs.getJSONObject(index)) == null) ||
                (jsonObj.length() == 0)) {
          continue;
        }
        
        sensorId = null;
        actualDt = null;
        obsValue = null;
        obsDt = null;
        flag = null;
        recCnt++;
        try {
          if ((sensorId = jsonObj.optInt("sensorId", -1)) <= 0) {
            throw new Exception("The sensorId is undefined.");
          } else if (sensorId != timeSeriesMap.sensorId) {
            throw new Exception("The record's sensorId does not matched the "
                    + "TimeSeriesMap's sensorId.");
          }
          
//          if (sensorId == 3203) {
//            logger.log(Level.INFO, "onParseData[Sensor[{0}]; Json:\n{1}",
//              new Object[]{sensorId, dataStr});
//          }

          if (((valStr = 
               ImportUtils.cleanString(jsonObj.optString("actualDate",""))) == null) ||
             ((actualDt = ImportUtils.dateFromString(valStr, dtFormat, 
                            this.getTimeZone())) == null)) {
            throw new Exception("The Actual Date is undefined.");
          }

          if (hasObsData) {
            if (((valStr = 
                 ImportUtils.cleanString(jsonObj.optString("obsDate",""))) == null) ||
                ((obsDt = ImportUtils.dateFromString(valStr, dtFormat, 
                                this.getTimeZone())) == null)) {
              obsDt = actualDt;
            }
          }
          
          if ((obsValue = jsonObj.optDouble("value", Double.NaN)).isNaN()){
            obsValue = null;
          }
          
          flag = jsonObj.optString("dataFlag", "");
          
          TEntity record = timeSeriesMap.newRecord(actualDt);
          timeSeriesMap.setRecordObsData(record, obsDt, obsValue, flag);
          timeSeriesMap.add(record);
        } catch (Exception pExp) {
          throw new Exception("Record[" + recCnt + "] Error:\n " + pExp.getMessage() 
                  +"\nDataStr = \n" + dataStr);
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".onParseRecord Error:\n " + pExp.getMessage());
    }
  }
  //</editor-fold>
}
