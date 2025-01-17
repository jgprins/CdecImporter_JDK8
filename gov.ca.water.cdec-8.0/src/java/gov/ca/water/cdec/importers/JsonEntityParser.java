package gov.ca.water.cdec.importers;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Abstract class to parse 
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class JsonEntityParser<TEntity extends Serializable, 
                                                          TPkType extends Serializable> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(JsonEntityParser.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public JsonEntityParser() {
    super();  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called to custom parse the <tt>jasonData</tt> and initiate a new Entity
   * @param jsonData the JsonObject containing the entity properties
   * @return the new Entity instance 
   * @throws Exception is the process failed
   */
  protected abstract TEntity onParseEntity(JSONObject jsonData) throws Exception;
  
  /**
   * Called to add entity to the <tt>entityMap</tt> using the Entity's primary key as
   * the Map key.
   * @param entityMap the result map
   * @param entity the new entity (will not be null - could have an invalid primary key)
   * @throws Exception is the process failed
   */
  protected abstract void addToMap(HashMap<TPkType,TEntity> entityMap, TEntity entity) 
          throws Exception;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to parse an array of JsonObjects to a Entity Map with the Entity's Primary
   * Key as the map key.
   * <p>Errors are trapped and logged. Up to 20 individual record errors are trapped and
   * logged before the process fails.
   * @param jsonArr the array of JSON Objects
   * @return the resulting hashMap - could be empty - never null.
   */
  public HashMap<TPkType,TEntity> parseEntitySet(JSONArray jsonArr) {
    HashMap<TPkType,TEntity> result = new HashMap<>();
    try {
      if ((jsonArr == null) || (jsonArr.length() == 0)) {
        throw new Exception("The jsonData is unassigned, null, or not an array");
      }
      
      int errCount = 0;
      JSONObject jsonObj = null;
      for (int index = 0; index < jsonArr.length(); index++) {
        if (((jsonObj = jsonArr.optJSONObject(index)) == null) ||
                (jsonObj.length() == 0)) {
          continue;
        }
        try {
          TEntity entity = this.onParseEntity(jsonObj);
          if (entity != null) {
            this.addToMap(result, entity);
          }
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.parseEntity Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          errCount++;
          if (errCount == 20) {
            throw new Exception("The process is stopped because up to 20 parsing errors "
                    + "were reported.");
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.parseEntitySet Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to parse a single JsonObject - representing an entity of type TEntity - to
   * a new Entity instance.
   * <p>Errors are trapped and a null entity is returned.
   * @param jsonData the JSON Object
   * @return the new instance or null if the process failed.
   */
  public TEntity parseEntity(JSONObject jsonData) {
    TEntity result = null;
    try {
      if ((jsonData == null) || (jsonData.length() == 0)) {
        throw new Exception("The jsonData is unassigned, not an object, or is null");
      }
      result = this.onParseEntity(jsonData);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.parseEntity Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  // </editor-fold>
}
