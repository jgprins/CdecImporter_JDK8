package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.EntityMergeDelegate;
import gov.ca.water.cdec.core.MapperDelegate;
import gov.ca.water.cdec.entities.Sensor;
import gov.ca.water.cdec.entities.SensorPK;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class SensorImportProcessor extends RecordSetImportProcessor<Sensor, SensorPK>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SensorImportProcessor() {
    super("ImportSensors", true);  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="RecordSetImportProcess Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a MapperDelegate with onGetKey implemented to return the 
   * JsonObject["sensorId"] as the key.</p>
   */
  @Override
  protected MapperDelegate<SensorPK, JSONObject> getSrcMapper() {
    MapperDelegate<SensorPK, JSONObject> result = 
                                          new MapperDelegate<SensorPK, JSONObject>() {
      @Override
      public SensorPK onGetKey(JSONObject value) throws Exception {
        SensorPK result = null;
        String strVal = null;
        Integer sensorNo = null;
        String stationId = null;
        String durCode = null;
        
        if ((sensorNo = value.optInt("sensorNum",-1)) <= 0) {
          sensorNo = null;
        }
        
        stationId = ImportUtils.cleanString(value.optString("stationId",""));
        durCode = ImportUtils.cleanString(value.optString("durCode",""));
        
        if ((sensorNo != null) && (stationId != null) && (durCode != null)) {
          result = new SensorPK(stationId, sensorNo, durCode);
        }
        
        if (result == null) {
          throw new Exception("Invalid SensorPK for record:/n" + value.toString());
        }
        return result;
      }
    };
    
    return result;       
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a MapperDelegate with onGetKey implemented to return the 
   * value.sensorId as the key.</p>
   */
  @Override
  protected MapperDelegate<SensorPK, Sensor> getTrgMapper() {
    MapperDelegate<SensorPK, Sensor> result = new MapperDelegate<SensorPK, Sensor>() {

      @Override
      public SensorPK onGetKey(Sensor value) throws Exception {
        SensorPK result = null;
        if (value != null) {
          result = value.getSensorPK();
        }
        return result;
      }
    };
    
    return result;
  }

  @Override
  protected EntityMergeDelegate<SensorPK, JSONObject, Sensor> getMerger() {
    EntityMergeDelegate<SensorPK, JSONObject, Sensor> result = 
                              new EntityMergeDelegate<SensorPK, JSONObject, Sensor>() {
       
      private TimeZone tz = TimeZone.getTimeZone("PST");

      /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: always return false - don't update the Station records</p>
       */
      @Override
      public boolean updateMerge(JSONObject srcObject, Sensor trgBean) throws Exception {
        boolean result = false;
        if ((trgBean == null) || (srcObject == null) || (srcObject.length() == 0)) {
          throw new Exception("The JsonObject is unassigned, "
                  + "null, or not a JSONObject or the Target record is unassigned.");
        }
        String dtFormat = "yyyy-MM-dd HH:mm:ss";
        Integer sensorId = null;
        Double dblVal = null;
        Integer intVal = null;
        String strVal = null;
        
        if ((sensorId = srcObject.optInt("sensorId",-1)) <= 0) {
          sensorId = null;
        }
        if ((sensorId != null) && (!sensorId.equals(trgBean.getSensorId()))) {
          trgBean.setSensorId(sensorId);
          result = true;
        }
          
        if ((dblVal = srcObject.optDouble("rangeMin", Double.NaN)).isNaN()){
          dblVal = null;
        }
        trgBean.setRangeMin(dblVal);

        if ((dblVal = srcObject.optDouble("rangeMax", Double.NaN)).isNaN()){
          dblVal = null;
        }
        trgBean.setRangeMax(dblVal);

        if ((intVal = srcObject.optInt("collectNum",-1)) <= 0) {
          intVal = null;
        }
        trgBean.setCollectNum(intVal);

        Date startDt = null;
        Date endDt = null;
        if (((strVal = 
          ImportUtils.cleanString(srcObject.optString("startDate",""))) != null) &&
          ((startDt = ImportUtils.dateFromString(strVal, dtFormat,this.tz)) != null)) {
          Date curDate = trgBean.getStartDate();
          if ((startDt != null) && ((curDate == null) || (!startDt.equals(curDate)))) {
            trgBean.setStartDate(startDt);
            result = true;
          }
        }
        if (((strVal = 
          ImportUtils.cleanString(srcObject.optString("endDate",""))) != null) &&
          ((endDt = ImportUtils.dateFromString(strVal, dtFormat,this.tz)) != null)) {
          Date curDate = trgBean.getEndDate();
          if ((endDt != null) && ((curDate == null) || (!endDt.equals(curDate)))) {
            trgBean.setEndDate(endDt);
            result = true;
          }
        }         
        return result;
      }

      /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: parse record: {"stationId":"BLS","sensorId":1840,"sensorNum":16,
       * "durCode":"E","startDate":"1994-12-17 00:00:00","endDate":"2015-12-31 00:00:00",
       * "rangeMin":0.0,"rangeMax":200.0,"collectNum":2} and initiate a new Sensor Bean.
       */
      @Override
      public Sensor newMerge(SensorPK srcKey, JSONObject srcValue) throws Exception {
        Sensor result = null;
        if (srcKey == null) {
          throw new Exception("The new Record's SrcKey is unassigned.");
        } else if ((srcValue == null) || (srcValue.length() == 0)) {
          throw new Exception("The JsonObject is unassigned, null, or not a JSONObject");
        }
        
        int sensorId = srcValue.optInt("sensorId",-1);
        if (sensorId <= 0) {
          throw new Exception("The new Sensor's SensorId is undefined.");
        }
        String dtFormat = "yyyy-MM-dd HH:mm:ss";
        Integer intVal = null;
        Double dblVal = null;
        String strVal = null;
        Date startDt = null;
        
        Sensor bean = new Sensor(srcKey, sensorId);
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("startDate",""))) != null) &&
             ((startDt = ImportUtils.dateFromString(strVal, dtFormat,this.tz)) != null)) {
          bean.setStartDate(startDt);
        }
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("endDate",""))) != null) &&
             ((startDt = ImportUtils.dateFromString(strVal, dtFormat,this.tz)) != null)) {
          bean.setEndDate(startDt);
        } 
        if (!(dblVal = srcValue.optDouble("rangeMin", Double.NaN)).isNaN()){
          bean.setRangeMin(dblVal);
        }
        if (!(dblVal = srcValue.optDouble("rangeMax", Double.NaN)).isNaN()){
          bean.setRangeMax(dblVal);
        }
        if ((intVal = srcValue.optInt("collectNum",-1)) >= 0) {
           bean.setCollectNum(intVal);
        } 
        result = bean;
        return result;
      }
    };
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return new StationImportProcessor()</p>
   */
  @Override
  public ImportProcessor<Sensor> nextTryClone() {
    return new SensorImportProcessor();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: set args[IMPORT_URL] = 
   * https://cdec.water.ca.gov/preciptemp/req/SensorServlet</p>
   */
  @Override
  protected void onInitImportUrl(ImportArgs args) {
    try {
      String urlStr = "https://cdec.water.ca.gov/preciptemp/req/SensorServlet";
      URL importUrl = new URL(urlStr);
      if (importUrl == null) {
        throw new Exception("The CDEC Import Url[" + urlStr + "] is invalid.");
      }
      
      args.setParameter(ImportKeys.IMPORT_URL, importUrl);
    } catch (Exception exp) {
      String errMsg = "onInitImportUrl Error:\n " + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }
  // </editor-fold>

}
