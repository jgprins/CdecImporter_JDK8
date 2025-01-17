package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.EntityMergeDelegate;
import gov.ca.water.cdec.core.MapperDelegate;
import gov.ca.water.cdec.entities.*;
import java.net.URL;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class SensorDefImportProcessor extends RecordSetImportProcessor<SensorDef, Integer>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SensorDefImportProcessor() {
    super("ImportSensorDefs", true);  
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
  protected MapperDelegate<Integer, JSONObject> getSrcMapper() {
    MapperDelegate<Integer, JSONObject> result = new MapperDelegate<Integer, JSONObject>() {

      @Override
      public Integer onGetKey(JSONObject value) throws Exception {
        Integer result = null;    
        if ((result = value.optInt("sensorNum",-1)) <= 0) {
          result = null;
        }
        
        if (result == null) {
          throw new Exception("Invalid SensorDef PK for record:/n" + value.toString());
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
  protected MapperDelegate<Integer, SensorDef> getTrgMapper() {
    MapperDelegate<Integer, SensorDef> result = new MapperDelegate<Integer, SensorDef>() {

      @Override
      public Integer onGetKey(SensorDef value) throws Exception {
        Integer result = null;
        if (value != null) {
          result = value.getSensorNum();
        }
        return result;
      }
    };
    
    return result;
  }

  @Override
  protected EntityMergeDelegate<Integer, JSONObject, SensorDef> getMerger() {
    EntityMergeDelegate<Integer, JSONObject, SensorDef> result = 
                              new EntityMergeDelegate<Integer, JSONObject, SensorDef>() {
        /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: always return false - don't update the Station records</p>
       */
      @Override
      public boolean updateMerge(JSONObject srcObject, SensorDef trgBean) 
                                                                       throws Exception {
        boolean result = false;
        if ((trgBean == null) || (srcObject == null) || (srcObject.length() == 0)) {
          throw new Exception("The JsonObject is unassigned, "
                  + "null, or not a JSONObject or the Target record is unassigned.");
        }
        Integer sensorNo = null;
        Integer intVal = null;
        String strVal = null;
        
        if (((sensorNo = srcObject.optInt("sensorNum",-1)) > 0) && 
                (sensorNo.equals(trgBean.getSensorNum()))) {          
          if (((strVal = 
             ImportUtils.cleanString(srcObject.optString("sensShortName",""))) != null)
             && (strVal.equals(trgBean.getSensShortName()))) {
            trgBean.setSensShortName(strVal);
            result = true;
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("sensLongName",""))) != null)
             && (strVal.equals(trgBean.getSensLongName()))) {
            trgBean.setSensLongName(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("sensUnits",""))) != null)
             && (strVal.equals(trgBean.getSensUnits()))) {
            trgBean.setSensUnits(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefPe",""))) != null)
             && (strVal.equals(trgBean.getShefPe()))) {
            trgBean.setShefPe(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefDur",""))) != null)
             && (strVal.equals(trgBean.getShefDur()))) {
            trgBean.setShefDur(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefType","Z"))) != null)
             && (strVal.equals(trgBean.getShefType()))) {
            trgBean.setShefType(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefSource","Z"))) != null)
             && (strVal.equals(trgBean.getShefSource()))) {
            trgBean.setShefSource(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefExtrema","Z"))) != null)
             && (strVal.equals(trgBean.getShefExtrema()))) {
            trgBean.setShefExtrema(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("shefProb","Z"))) != null)
             && (strVal.equals(trgBean.getShefProb()))) {
            trgBean.setShefProb(strVal);
            result = true; 
          }        
          if (((strVal = 
               ImportUtils.cleanString(srcObject.optString("fmtType","0"))) != null)
             && (strVal.equals(trgBean.getFmtType()))) {
            trgBean.setFmtType(strVal);
            result = true; 
          }
          if (((intVal = srcObject.optInt("sensOrder",0)) >= 0)
             && (strVal.equals(trgBean.getSensOrder()))) {
            trgBean.setSensOrder(intVal);
            result = true;  
          }
        }
        return result;
      }

      /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: parse record: {
       * "sensorNum":209,
       * "shefPe":"PN",
       * "shefDur":"H",
       * "shefType":"Z",
       * "shefSource":"Z",
       * "shefExtrema":"Z",
       * "shefProb":"Z",
       * "sensShortName":"AUXPREC",
       * "sensLongName":"PRECIPITATION AUX",
       * "sensUnits":"INCHES",
       * "fmtType":"2",
       * "sensOrder":407} and initiate a new SensorDef Bean.
       */
      @Override
      public SensorDef newMerge(Integer srcKey, JSONObject srcValue) throws Exception {
        SensorDef result = null;
        if (srcKey == null) {
          throw new Exception("The enw Record's SrcKey is unassigned.");
        } else if ((srcValue == null) || (srcValue.length() == 0)) {
          throw new Exception("The JsonObject is unassigned, null, or not a JSONObject");
        }
        Integer intVal = null;
        String strVal = null;
        
        SensorDef bean = new SensorDef(srcKey);
        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("sensShortName",""))) != null)) {
          bean.setSensShortName(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("sensLongName",""))) != null)) {
          bean.setSensLongName(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("sensUnits",""))) != null)) {
          bean.setSensUnits(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefPe",""))) != null)) {
          bean.setShefPe(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefDur",""))) != null)) {
          bean.setShefDur(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefType","Z"))) != null)) {
          bean.setShefType(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefSource","Z"))) != null)) {
          bean.setShefSource(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefExtrema","Z"))) != null)) {
          bean.setShefExtrema(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("shefProb","Z"))) != null)) {
          bean.setShefProb(strVal);
        }        
        if (((strVal = 
             ImportUtils.cleanString(srcValue.optString("fmtType","0"))) != null)) {
          bean.setFmtType(strVal);
        }
        if ((intVal = srcValue.optInt("sensOrder",0)) >= 0) {
           bean.setSensOrder(intVal);
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
   * OVERRIDE: return new StationDefImportProcessor()</p>
   */
  @Override
  public ImportProcessor<SensorDef> nextTryClone() {
    return new SensorDefImportProcessor();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: set args[IMPORT_URL] = 
   * https://cdec.water.ca.gov/preciptemp/req/SensorDefServlet</p>
   */
  @Override
  protected void onInitImportUrl(ImportArgs args) {
    try {
      String urlStr = "https://cdec.water.ca.gov/preciptemp/req/SensorDefServlet";
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
