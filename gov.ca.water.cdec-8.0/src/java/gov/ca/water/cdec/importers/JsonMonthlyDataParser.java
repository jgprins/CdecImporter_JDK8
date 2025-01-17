package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.MonthlyData;
import gov.ca.water.cdec.enums.DurationCodes;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class JsonMonthlyDataParser extends 
                            JsonTimeSeriesParser<MonthlyData, DateKey, MonthlyDataMap> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public JsonMonthlyDataParser() {
    super(TimeZone.getTimeZone("PST"));  
  }

  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override JsonDataParser">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Custom Implementing for handling the MonthlyData format that include 
   * primary MonthlyDataPk[sensorId,actualDt].</p>
   */
  @Override
  protected void onParseData(String dataStr, MonthlyDataMap timeSeriesMap,
                                             CdecSensorInfo sensorInfo) throws Exception {
    Date actualDt = null;
    Double obsValue = null;
    String flag = null;
    Integer sensorId = null;
    Date obsDt = null;

    try {
      DurationCodes durCode = sensorInfo.sensorType.getDurationCode();
      if (!durCode.equals(DurationCodes.M)) {
        throw new Exception("Invalid DuractioNode. Expected DurationCode[M], got "
                + "DurationCode[" + durCode.toString() + "]");
      }
      String dtFormat = JsonTimeSeriesParser.getDateTimeFormat(durCode);
      if (dtFormat == null) {
        throw new Exception("Unable to resolve the Date-Time format for DurationCode["
        + durCode.toString() + "].");
      }
      
      JSONArray parsedObjs = null;   
      if (((dataStr = ImportUtils.cleanString(dataStr)) == null) ||
          ((parsedObjs = new JSONArray(dataStr)) == null) || 
          (parsedObjs.length() == 0)) {
        return;
      }
      
      JSONObject jsonObj = null;
      JSONObject pkElem = null;
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
          if ((pkElem = jsonObj.optJSONObject("monthlyDataPK")) == null){
            throw new Exception("Unable to access the Monthly Data Primary Key.");
          }
          if ((sensorId = pkElem.optInt("sensorId", -1)) <= 0) {
            throw new Exception("The Monthly Data Primary Key's sensorId is undefined.");
          } else if (sensorId != timeSeriesMap.sensorId) {
            throw new Exception("The Monthly Data Primary Key's sensorId does not match "
                    + "the TimeSeriesMap's sensorId.");
          }

          if (((valStr = 
               ImportUtils.cleanString(pkElem.optString("actualDate",""))) == null) ||
             ((actualDt = ImportUtils.dateFromString(valStr, dtFormat, 
                            this.getTimeZone())) == null)) {
            throw new Exception("The Monthly Data Primary Key's Actual Date is "
                    + "undefined.");
          }
          
          if (((valStr = 
                 ImportUtils.cleanString(jsonObj.optString("obsDate",""))) == null) ||
              ((obsDt = ImportUtils.dateFromString(valStr, dtFormat, 
                              this.getTimeZone())) == null)) {
            obsDt = actualDt;
          }

          if ((obsValue = jsonObj.optDouble("value", Double.NaN)).isNaN()){
            obsValue = null;
          }
          
          flag = jsonObj.optString("dataFlag", "");
          
          MonthlyData record = timeSeriesMap.newRecord(actualDt);
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
