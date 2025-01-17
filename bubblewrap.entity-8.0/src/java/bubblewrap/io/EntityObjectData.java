package bubblewrap.io;

import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.io.interfaces.IObjectData;
import bubblewrap.io.params.ParameterMap;
import bubblewrap.io.params.ParameterMapBase;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Never Used or tested
 * <p>This is an ObjectData Wrapper for any EntityWrapper Class. It serialize the 
 * EntityWrapper's entity (i.e. its Entity Bean) and call is serialObjectData and 
 * deserializeObjectData to serialize and deserialize and non-transient settings.</p>
 * <p><b>NOTE:</b> This wrapper does not restore the same instance of the EntityWrapper, 
 * nor the same EntityBean instance.</p>
 * @author kprins
 */
//@Deprecated
public class EntityObjectData implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Sttic Fields/Methods">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(EntityObjectData.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Placeholder for the Class of the Serialized EntityAjax
   */
  private String entityWrapperClass;
  /**
   * Placeholder for the Wrapper's isSerialixed State.
   */
  private Boolean isSerialized;
  /**
   * Placeholder for the EntityAjax's Serializable ObjectDataMap.
   */
  private ParameterMapBase entityData;
  /**
   * Placeholder for the EntityAjax Bean
   */
  private Serializable entityBean;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityObjectData() {
    super();     
    this.resetObjectData();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Initiate the EntityWrapper class from the class name
   * @return the EntityWrapper class.
   */
  private Class<? extends EntityWrapper> getEntityWrapperClass() {
    Class<? extends EntityWrapper> pResult = null;
    try {
      if (this.entityWrapperClass == null) {
        throw new Exception("The EntityAjax Class has not be specified");
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.method getEntityAjaxClass:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * Reset the the ObjectData
   */
  private void resetObjectData() {
    this.entityWrapperClass = null;
    this.isSerialized = null;
    this.entityData = null;
    this.entityBean = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Wrapper's current isSerialzied state.
   * @return boolean
   */
  public boolean isSerialized() {
    return ((this.isSerialized != null) && (this.isSerialized));
  }
  
  /**
   * Assigned the EntityWrapper instance that must be serialized. It calls the 
   * EntityWrapper's serializeObjectData method to assign any non-bean and non-transient 
   * field values to an EntityFieldDataMap. If successful, this.isSerialzied=true.
   * @param <TWrapper> extends EntityWrapper
   * @param entityWrapper the EntityWrapper to serialize
   * @throws Exception 
   */
  public <TWrapper extends EntityWrapper> void setEntityWrapper(TWrapper entityWrapper) 
          throws Exception {
    String sEntName = "Unassigned";
    try {
      this.resetObjectData();
      if (entityWrapper == null) {
        throw new Exception("The Entity Reference cannot be uanssigned.");
      }
      
      this.entityWrapperClass = entityWrapper.getClass().getName();
      sEntName = entityWrapper.getClass().getSimpleName();
      
      this.entityBean = entityWrapper.getEntity();
      
      this.entityData = new ParameterMap();
      entityWrapper.serializeObjectData(this.entityData);
      
      this.isSerialized = true;
    } catch (Exception pExp) {
      this.resetObjectData();
      logger.log(Level.WARNING, "{0}.setEntityWrapper[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), sEntName, pExp.getMessage()});
      throw pExp;
    }
  }
  
  /**
   * Return an new EntityWrapper instance that contains the serialized data. 
   * @param <TWrapper> extends EntityWrapper
   * @return TWrapper
   * @throws Exception if the deserialization failed.
   */
  @SuppressWarnings("unchecked")
  public <TWrapper extends EntityWrapper> TWrapper getEntityWrapper() throws Exception {
    TWrapper result = null;
    try {
      if (!this.isSerialized()) {
        throw new Exception("The EntityWrapper could be initiated because the Wrapper "
                + "contains no serialized information.");
      }
      
      Class<? extends EntityWrapper> wrapperClasss = this.getEntityWrapperClass();
      result = (TWrapper) EntityWrapper.newFromBean(wrapperClasss, this.entityBean);
      if (result == null) {
        throw new Exception("Unable to initaite a new instance of EntityWrapper["
                + wrapperClasss.getSimpleName() + "].");
      }
      
      if ((this.entityData != null) && (!this.entityData.isEmpty())) {
        result.deserializeObjectData(this.entityData);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="IObjectData Implementation">
  /**
   * IMPLEMENT: If this.isSerialized, serialize the EntityWrapper Class Name, its Bean,
   * and its ObjectData to a ObjectData instance and return the ObjectData. Otherwise,
   * return null. On exception, return null and log the error.
   * @return ObjectData
   */
  @Override
  public ObjectData serializeObject() {
    ObjectData pResult = null;
    try {
      if (this.isSerialized()) {
        pResult = new ObjectData(this);
        pResult.put("entityWrapperClass", this.entityWrapperClass);
        pResult.put("entityBean", this.entityBean);
        pResult.put("entityData", this.entityData);
      }
    } catch (Exception pExp) {
      pResult = null;
      logger.log(Level.WARNING, "{0}.serializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * IMPLEMENT: Deserialize the Wrapper. If the EntityClass and Its serialize bean has 
   * been successfully restored, isSerialized=true.
   * @param pData ObjectData
   */
  @Override
  public void deserializeObject(ObjectData pData) {
    try {
      this.resetObjectData();
      if (pData != null) {
        this.entityWrapperClass = pData.getCasted("entityWrapperClass", null);
        this.entityWrapperClass = DataEntry.cleanString(this.entityWrapperClass);
        if (this.entityWrapperClass != null) {
          this.entityBean = pData.getCasted("entityBean", null);
          this.entityData = pData.getCasted("entityData", null);
          this.isSerialized = (this.entityBean != null);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
}
