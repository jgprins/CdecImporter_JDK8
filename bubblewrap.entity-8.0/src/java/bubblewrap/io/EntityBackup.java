package bubblewrap.io;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.io.interfaces.IObjectData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The EntityBackup is a wrapper IObejctClass for a HashMap to back up and restore 
 * selected fields of an EntityAjax record's EntityBean. When successfully backed up
 * this EntityBack has a unique ID (this.backId), and a backupKey, which can be used to
 * identify it for restoring purposes. It also maintains a reference to the backed up 
 * record's class and recordId for identification purposes.
 * @author kprins
 */
public class EntityBackup implements IObjectData {
 
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Return the BackupId for pRecord. Return null if pRecord or pRecord.entity is 
   * unassigned or not accessible. Set the Id's prefix as the simpleName of the
   * pRecord.entity.class and the suffix as "NewRecord" if pRecord.isNew or
   * pRecord.recordId otherwise. Examples: "MyBean_NewRecord" or "MyBean_102"
   * @param record the EntityWrapper to backup
   * @return String
   */
  public static String getEntityBackupId(EntityWrapper record) {
    String sResult = null;
    try {
      Serializable entity = (record == null)? null: record.getEntity();
      if (entity == null) {
        throw new Exception("The record or the record's entity bean is unasssigned or "
                + "not accessible.");
      }
      
      sResult = entity.getClass().getSimpleName() + "_";
      sResult += ((record.isNew()? "NewRecord": record.getRecordId().toString()));
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityBackupId Error:\n {1}",
              new Object[]{EntityBackup.class.getSimpleName(), pExp.getMessage()});
    }
    return sResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(EntityBackup.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A HashMap containing the Backed-up data
   */
  private HashMap<String,Object> backupData;
  /**
   * Placeholder for the backed-up entities class
   */
  private String wrapperClassName;
  /**
   * Placeholder for the backed-up record's recordId
   */
  private Serializable recordId;
  /**
   * Placeholder for this backupId.
   */
  private String backupId;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityBackup() {
    super();    
    this.resetBackup();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return the EntityBackup's BackupId. Return null if not backed up.
   * @return String
   */
  public String getBackupId() {
    return this.backupId;
  }
  
  /**
   * Return true if an Entity has been backed up.
   * @return boolean
   */
  public boolean isBackedUp() {
    return ((this.backupId != null)
            && (this.wrapperClassName != null)
            && (this.recordId != null));
  }
  
  /**
   * Return true if !this.isBackedUp or the backup contains no backed up values
   * @return boolean
   */
  public boolean isEmpty() {
    return ((!this.isBackedUp())
            || (this.backupData == null) || (this.backupData.isEmpty()));
  }
  
  /**
   * Get the class of the backed up record
   * @return a Class that extends EntityWrapper
   * @throws ClassNotFoundException
   */
  @SuppressWarnings("unchecked")
  public Class<? extends EntityWrapper> getEntityWrapperClass()
          throws ClassNotFoundException {
    return (Class<? extends EntityWrapper>) ((this.wrapperClassName == null)? null:
            Class.forName(this.wrapperClassName));
  }
  
  /**
   * Return the backup record's recordId. Returns null if not backed up.
   * @param <T extends Serializable>
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T getRecordId() {
    return (T)this.recordId;
  }
  
  /**
   * Return true if pRecord != null, this.isBackedUp and pRecord.recordId=this.recordId.
   * If (pRecord.isNew), return true if this.recordId="NewRecord".
   * @param pRecord EntityWrapper
   * @return boolean
   */
  public final boolean isBackedUpRecord(EntityWrapper pRecord) {
    boolean bResult = false;
    try {
      if ((pRecord != null) && (this.isBackedUp())) {
        Class<? extends EntityWrapper> pClass = this.getEntityWrapperClass();
        if (pClass.isInstance(pRecord)) {
          if (pRecord.isNew())  {
            bResult = this.recordId.equals("NewRecord");
          } else {
            bResult = this.recordId.equals(pRecord.getRecordId());
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isBackedUpRecord Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return bResult;
  }
  
  /**
   * <p>Called the back up the field values of pRecord as defined in pFieldList. It 
   * throws exceptions if pFieldList is empty, pRecord or is entity bean is unassigned 
   * or not accessible or the reading of the field values failed. All exceptions are 
   * logged.</p>
   * <p><b>NOTE:</b> The field names must match the entity bean field names. For
   * example if the bean gas a property 'getMyProp' and 'setMyProp', then the field
   * name is 'myProp'</p>
   * @param record EntityWrapper
   * @param pFieldList List<String>
   * @throws Exception
   */
  public final <TWrapper extends EntityWrapper> void backupRecord(TWrapper record,
          List<String> pFieldList) throws Exception {
    this.resetBackup();
    try {
      if ((pFieldList == null) || (pFieldList.isEmpty())) {
        throw new Exception("The List of backup fields are undefined or empty");
      }
      
      Serializable entBean = (record == null)? null: record.getEntity();
      if (entBean == null) {
        throw new Exception("The backup record or its entity bean is not defined or "
                + "accessible");
      }
      
      this.wrapperClassName = record.getClass().getName();
      this.backupData = new HashMap<>();
      this.recordId = (record.isNew())? "NewRecord": record.getRecordId();
      
      for (String fieldName : pFieldList) {
        Object fieldValue = ReflectionInfo.getFieldValue(entBean, fieldName);
        if (fieldValue != null) {
          this.backupData.put(fieldName, fieldValue);
        }
      }
      
      this.backupId = EntityBackup.getEntityBackupId(record);
    } catch (Exception pExp) {
      this.resetBackup();
      logger.log(Level.WARNING, "{0}.backupRecord Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  
  /**
   * Restore the Backed-up values and if (!pRecord.isNew), save the changes to the
   * record. Throws an exception if the record does not match the backed up record or
   * the restore process failed.  All exceptions are 
   * logged.
   * @param record EntityWrapper
   * @throws Exception
   */
  public final  void restoreRecord(EntityWrapper record) throws Exception {
    try {
      Serializable entBean = (record == null)? null: record.getEntity();
      if ((entBean == null) || (this.isEmpty())) {
        return;
      }
      
      if (!this.isBackedUpRecord(record)) {
        throw new Exception("Record[" + record.toString() + "] does not match the "
                + "backed up record.");
      }
      
      Class<? extends Serializable> pClass = entBean.getClass();
      for (String fieldName : this.backupData.keySet()) {
        Object fieldValue = this.backupData.get(fieldName);
        ReflectionInfo.setFieldValue(entBean, fieldName, fieldValue);
      }
      
      if (!record.isNew()) {
        record.submitEdits(true);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.restoreBackup Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  
  /**
   * Called to reset the Backup
   */
  public final void resetBackup() {
    this.backupId = null;
    this.wrapperClassName = null;
    this.recordId = null;
    this.backupData = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement IObjectData">
  /**
   * IMPLEMENT: If (isBackedUp), create an ObjectData, save the entityClass and its
   * backed-up data to the ObjectData and return the map. Errors are logged.
   * @return ObjectData
   */
  @Override
  public ObjectData serializeObject() {
    ObjectData pResult = null;
    try {
      if (this.backupId != null) {
        pResult = new  ObjectData(this);
        
        pResult.put("backupId", this.backupId);
        pResult.put("entityAjaxClass", this.wrapperClassName);
        if ((this.backupData != null) && (!this.backupData.isEmpty())) {
          pResult.put("backupData", this.backupData);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.serializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * IMPLEMENT: If (pData != null) && (!pData.isEmpty), restore the instance fields
   * from pData. Errors are logged.
   * @param pData ObjectData
   */
  @Override
  public void deserializeObject(ObjectData pData) {
    try {
      if ((pData == null) || (pData.isEmpty())) {
        return;
      }
      
      this.backupId = pData.getCasted("backupId",null);
      if (this.backupId != null) {
        this.wrapperClassName = pData.getCasted("entityAjaxClass",null);
        this.backupData = pData.getCasted("backupData",null);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>  
}
