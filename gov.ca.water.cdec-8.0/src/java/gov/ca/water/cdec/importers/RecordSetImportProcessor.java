package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.EntityMergeDelegate;
import gov.ca.water.cdec.core.MapperDelegate;
import gov.ca.water.cdec.facades.CdecBaseFacade;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class RecordSetImportProcessor<TEntity extends Serializable,
                            TKey extends Serializable> extends ImportProcessor<TEntity> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the processor facade assigned by the CdecDataImporter
   */
  private CdecBaseFacade<TEntity> facade;
  /**
   * A Flag that can be set to remove target records that are missing form the 
   * import source. (default = false)
   */
  private Boolean removeMissing;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public RecordSetImportProcessor(String processName, Boolean removeMissing) {
    super(processName);  
    this.facade = null;
    this.removeMissing = ((removeMissing == null) || (!removeMissing))? null: true;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the processor's facade for entity type TEntity
   * @param facade the facade reference
   */
  public final void setFacade(CdecBaseFacade<TEntity> facade) {
    this.facade = facade;
  }
  
  /**
   * Get the processor's facade for entity type TEntity
   * @return the facade reference
   */
  protected final CdecBaseFacade<TEntity> getFacade() {
    return this.facade;
  }
  
  /**
   * Get whether to remove target records that are missing form the 
   * import source. (default = false)
   * @return true to remove missing records
   */
  protected final boolean doRemoveMissing() {
    return ((this.removeMissing != null) && (this.removeMissing));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Abstract Methods">
  /**
   * Get an instance of the Source Mapper Delegate used in generating a SrcMap of the 
   * imported JSON Objects
   * @return the MapperDelegate instance
   */ 
  protected abstract MapperDelegate<TKey, JSONObject> getSrcMapper();
  /**
   * Get an instance of the Target Bean Mapper Delegate used in generating a TrgMap of the 
   * existing Records in the Database
   * @return the MapperDelegate instance
   */
  protected abstract MapperDelegate<TKey, TEntity> getTrgMapper();
  /**
   * Get an instance of the Entity Merger Delegate used by the facade to merge to 
   * merge the Source (imported) and Target (existing) record sets.
   * @return the EntityMergeDelegate instance.
   */
  protected abstract EntityMergeDelegate<TKey, JSONObject, TEntity> getMerger();  
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="ImportProcessor Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Set this.facade = null</p>
   */
  @Override
  protected void onResetProcessor() {
    this.facade = null;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Parse the Args[IMPORT_STR] to a List of JSONObjects and use this.srcMapper 
   * to convert the list to a SrcMap[TKey,JsonObject]. assign to args[IMPORT_DATA].
   * If the Args[IMPORT_STR] = null|""|"[]" or the List of JSONObjects is empty,
   * call args.setNotFound.</p>
   */
  @Override 
  protected void onParseImportData(ImportArgs args) {
    try {
      MapperDelegate<TKey, JSONObject> mapper = null;
      String dataStr = args.getParameter(ImportKeys.IMPORT_STR, null);
      JSONArray parsedObjs = null;   
      List<JSONObject> objList = null;
      if (((dataStr = ImportUtils.cleanString(dataStr)) == null) ||
          ((parsedObjs = new JSONArray(dataStr)) == null) || 
          (parsedObjs.length() == 0) ||
          ((objList =  ImportUtils.jsonArrToObjectList(parsedObjs)) == null) ||
          (objList.isEmpty())) {
        args.setNotFound();
      } else {
        if ((mapper = this.getSrcMapper()) == null) {
          throw new Exception("The ImportProcessor does not support a Source Mapper "
                  + "Delegate or initiating the MapperDelegate failed.");
        }
        
        HashMap<TKey, JSONObject> srcMap = new HashMap<>();
        mapper.toMap(objList, srcMap);        
        if (srcMap.isEmpty()) {
          args.setNotFound();
        } else {
          args.setParameter(ImportKeys.IMPORT_DATA, srcMap);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onParseImportData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      args.setErrorMsg(exp.getMessage());
    }
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Retrieve srcMap from args[IMPORT_DATA], get {@linkplain #getTrgMapper() 
   * this.trgMapper}, {@linkplain #getMerger() this.merger} and {@linkplain #getFacade() 
   * this.facade} and call {@linkplain CdecBaseFacade#mergeAll(java.util.HashMap, 
   * gov.ca.water.cdec.core.MapperDelegate, gov.ca.water.cdec.core.EntityMergeDelegate)  
   * this.facade.mergeAll(srcMap, trgMapper, mergeDelegate)}</p>
   */
  @Override
  protected void onMergeData(ImportArgs args) {
    try {
      HashMap<TKey, JSONObject> srcMap = args.getParameter(ImportKeys.IMPORT_DATA, null);
      if ((srcMap == null) ||(srcMap.isEmpty())) {
        throw new Exception("The imported Data Map is no longer accessible.");
      }
      
      MapperDelegate<TKey, TEntity> trgMapper = this.getTrgMapper();
      if (trgMapper == null) {
        throw new Exception("The ImportProcessor does not support a Target Mapper "
                + "Delegate or initiating the MapperDelegate failed.");
      }
      
      EntityMergeDelegate mergeDelegate = this.getMerger();
      if (mergeDelegate == null) {
        throw new Exception("The ImportProcessor does not support a Entity Merge "
                + "Delegate or initiating the EntityMergeDelegate failed.");
      }
      CdecBaseFacade<TEntity> facade = this.getFacade();
      if (facade == null) {
        throw new Exception("The Processor's Entity Facade is not accessible.");
      }
      facade.mergeAll(srcMap, trgMapper, mergeDelegate, this.doRemoveMissing());
    } catch (Exception exp) {
      String errMsg = "onMergeData Error:\n " + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }
  // </editor-fold>
}
