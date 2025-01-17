package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.EntityMergeDelegate;
import gov.ca.water.cdec.core.MapperDelegate;
import gov.ca.water.cdec.entities.Station;
import java.net.URL;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class StationImportProcessor extends RecordSetImportProcessor<Station, String>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public StationImportProcessor() {
    super("ImportStations", true);  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="RecordSetImportProcess Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a MapperDelegate with onGetKey implemented to return the 
   * JSONObject["stationId"] as the key.</p>
   */
  @Override
  protected MapperDelegate<String, JSONObject> getSrcMapper() {
    MapperDelegate<String, JSONObject> result = new MapperDelegate<String, JSONObject>() {

      @Override
      public String onGetKey(JSONObject value) throws Exception {
        return ImportUtils.cleanString(value.optString("stationId",""));
      }
    };
    
    return result;       
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a MapperDelegate with onGetKey implemented to return the 
   * value.stationId as the key.</p>
   */
  @Override
  protected MapperDelegate<String, Station> getTrgMapper() {
    MapperDelegate<String, Station> result = new MapperDelegate<String, Station>() {

      @Override
      public String onGetKey(Station value) throws Exception {
        String result = null;
        if (value != null) {
          result = value.getStationId();
        }
        return result;
      }
    };
    
    return result;
  }

  /**
   * 
   * @return 
   */
  @Override
  protected EntityMergeDelegate<String, JSONObject, Station> getMerger() {
    EntityMergeDelegate<String, JSONObject, Station> result = 
                               new EntityMergeDelegate<String, JSONObject, Station>() {

      /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: always return false - don't update the Station records</p>
       */
      @Override
      public boolean updateMerge(JSONObject srcObject, Station trgBean) throws Exception {
        boolean result = false;
        Double srcDbl = null;
        Integer srcInt = null;
        Double trgDbl = null;
        Integer trgInt = null;
        int missVal = -9999;
        if ((srcInt = srcObject.optInt("elevation",missVal)) >= missVal) {
          if (((trgInt = trgBean.getElevation()) == null) ||
              (!trgInt.equals(srcInt))) {
            trgBean.setElevation(srcInt);
            result = true;
          }
        }
        
        if (!(srcDbl = srcObject.optDouble("latitude", Double.NaN)).isNaN()) {
          if (((trgDbl = trgBean.getLatitude()) == null) ||
              (!trgDbl.equals(srcDbl))) {
            trgBean.setLatitude(srcDbl);
            result = true;
          }
        }
        
        if (!(srcDbl = srcObject.optDouble("longitude", Double.NaN)).isNaN()) {
          if (((trgDbl = trgBean.getLongitude()) == null) ||
              (!trgDbl.equals(srcDbl))) {
            trgBean.setLongitude(srcDbl);
            result = true;
          } 
        }
        return result;
      }
      /**
       * {@inheritDoc}
       * <p>
       * OVERRIDE: parse record: {"stationId":"CLR", "stationName":"CLEAR CREEK", 
       * "elevation":3300,"latitude":40.639,"longitude":-122.667,
       * "nearbyCity":"WHISKEYTOWN","hydroNum":5,"basinNum":7001, 
       * "countyNum":45,"operator":50,"mapNumber":8,"collectNum":0} and initiate a new
       * Station Bean.
       */
      @Override
      public Station newMerge(String srcKey, JSONObject srcObject) throws Exception {
        Station result = null;
        if ((srcKey = ImportUtils.cleanString(srcKey)) == null) {
          throw new Exception("The stationId is unassigned or an empty string");
        } else if ((srcObject == null) || (srcObject.length() == 0)) {
          throw new Exception("The JSONObject is unassigned, null, or not a JSONObject");
        }
        String strVal = null;
        Double dblVal = null;
        Integer intVal = null;
        int missVal = -9999;
        Station bean = new Station(srcKey);
        
        if ((strVal = 
                ImportUtils.cleanString(srcObject.optString("stationName",""))) != null) {
          bean.setStationName(strVal);
        }
        
        if ((intVal = srcObject.optInt("elevation",missVal)) >= missVal) {
          bean.setElevation(intVal);
        } else {
          bean.setElevation(missVal);
        }
        
        if (!(dblVal = srcObject.optDouble("latitude", Double.NaN)).isNaN()) {
          bean.setLatitude(dblVal);
        }
        
        if (!(dblVal = srcObject.optDouble("longitude", Double.NaN)).isNaN()) {
          bean.setLongitude(dblVal);
        }
        
        if ((strVal = 
                ImportUtils.cleanString(srcObject.optString("nearbyCity",""))) != null) {
          bean.setNearbyCity(strVal);
        }
        
        if ((intVal = srcObject.optInt("hydroNum",-1)) >= 0) {
          bean.setHydroNum(intVal);
        }
        
        if ((intVal = srcObject.optInt("basinNum",-1)) >= 0) {
          bean.setBasinNum(intVal);
        }
        
        if ((intVal = srcObject.optInt("countyNum",-1)) >= 0) {
          bean.setCountyNum(intVal);
        }
        
        if ((intVal = srcObject.optInt("operator",-1)) >= 0) {
          bean.setOperator(intVal);
        }
        
        if ((intVal = srcObject.optInt("mapNumber",-1)) >= 0) {
          bean.setMapNumber(intVal);
        }
        
        if ((intVal = srcObject.optInt("collectNum",-1)) >= 0) {
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
  public ImportProcessor<Station> nextTryClone() {
    return new StationImportProcessor();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: set args[IMPORT_URL] = 
   * http://cdec.water.ca.gov/preciptemp/req/StationServlet</p>
   */
  @Override
  protected void onInitImportUrl(ImportArgs args) {
    try {
      String urlStr = "https://cdec.water.ca.gov/preciptemp/req/StationServlet";
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
