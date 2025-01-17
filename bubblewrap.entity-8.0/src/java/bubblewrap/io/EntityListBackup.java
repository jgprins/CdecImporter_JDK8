package bubblewrap.io;

import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.io.BlobSerializer;
import bubblewrap.io.interfaces.IObjectData;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * The class is for backup and restoring the values of a defined list of record 
 * associated with a EntityView. It only backup the value of field specified in a field
 * list specified during backup. 
 * @author kprins
 */ 
public class EntityListBackup implements IObjectData {
 
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Return the BackupId for recordClass. Return null if recordClass is unassigned . 
   * Set the Id as recordClass.simpleName + "_Backup" if ownerId=null or 
   * recordClass.simpleName + "_" + ownerId
   * @param recordClass extends EntityWrapper
   * @param ownerId String (can be null)
   * @return String
   */
  public static String getEntityListBackupId(Class<? extends EntityWrapper> recordClass,
          String ownerId) {
    String sResult = null;
    try {
      if (recordClass == null) {
        throw new Exception("The EntityView Class is unasssigned.");
      }
      
      ownerId = DataEntry.cleanString(ownerId);
      if (ownerId == null) {
        sResult = recordClass.getSimpleName() + "_Backup";
      } else {
        sResult = recordClass.getSimpleName() + "_" + ownerId.toLowerCase();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityListBackupId Error:\n {1}",
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
  private List<EntityBackup> backupData;
  /**
   * Placeholder for the backed-up EntityWrapper class name
   */
  private Class<? extends EntityWrapper> recordClass;
  /**
   * Placeholder of a identifying OwnerId
   */
  private String ownerId;
  /**
   * Placeholder for this backupId.
   */
  private String backupId;
  //</editor-fold>
 
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityListBackup() {
    super();   
    this.resetBackup();
  }  
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Return the EntityListBackup's BackupId. Return null if not backed up.
   * @return String
   */
  public String getBackupId() {
    return this.backupId;
  }
  
  /**
   * Return the EntityListBackup's OwnerId. Could be null if it was not specified.
   * @return String
   */
  public String getOwnerId() {
    return this.ownerId;
  }
  
  /**
   * Return true if an EntityList has been backed up.
   * @return boolean
   */
  public boolean isBackedUp() {
    return ((this.backupId != null) && (this.recordClass != null));
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
   * Get the class of the backed up Records
   * @return the assigned Class (extends EntityWrapper) 
   */
  @SuppressWarnings("unchecked")
  public Class<? extends EntityWrapper> getRecordClass()  {
    return this.recordClass;
  }
  
  /**
   * Check if recordClass and sOwnerID represents this backup. It calls the static
   * getEntityListBackupId and compare the result with this.backupId.
   * @param recordClass extends EntityWrapper
   * @param ownerId String
   * @return boolean
   */
  public final boolean isBackup(Class<? extends EntityWrapper> recordClass,
          String ownerId) {
    boolean bResult = false;
    try {
      if ((recordClass != null) && (this.isBackedUp())) {
        String sId = EntityListBackup.getEntityListBackupId(recordClass, ownerId);
        bResult = ((sId != null) && (sId.equalsIgnoreCase(this.backupId)));
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isBackup Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return bResult;
  }
  
  /**
   * Called to backup all the values defined in pFieldList for the records in pRecordset
   * to be restored at a later state. It generates a EntityBack for each record and
   * maintain it in a backupData list.It throws an exception if recordClass is
   * unassigned, pRecordSet is unassigned or empty or pFieldlsit is unassigned or empty.
   * All exceptions are logged.
   * @param recordClass extends EntityWrapper
   * @param recordSet {@literal  List<EntityWrapper>}
   * @param fieldList {@literal  List<String>}
   * @throws Exception
   */
  public final void backupRecords(
          Class<? extends EntityWrapper> recordClass,  String ownerId, 
          List<EntityWrapper> recordSet, 
          List<String> fieldList) throws Exception {
    this.resetBackup();
    try {
      if (recordClass == null) {
        throw new Exception("The EntityWrapper Class reference is unassigned.");
      }
      
      if ((recordSet == null) || (recordSet.isEmpty())) {
        throw new Exception("The Recordset is unassigned or empty.");
      }
      
      if ((fieldList == null) || (fieldList.isEmpty())) {
        throw new Exception("The backup Field List is unassigned or empty.");
      }
      
      this.recordClass = recordClass;
      this.backupId = EntityListBackup.getEntityListBackupId(recordClass,ownerId);
      this.backupData = new ArrayList<>();
      this.ownerId = DataEntry.cleanString(ownerId);
      
      /** backup each record in the recordset and add the backup to the list */
      for (EntityWrapper record : recordSet) {
        if (record == null) {
          continue;
        }
        EntityBackup pBackup = new EntityBackup();
        pBackup.backupRecord(record, fieldList);
        if (pBackup.isBackedUp()) {
          this.backupData.add(pBackup);
        }
      }
    } catch (Exception pExp) {
      this.resetBackup();
      logger.log(Level.WARNING, "{0}.backupRecords Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  
  /**
   * Called to restored the records in pRecordSet for recordClass. It locates the
   * EntityBackup for each record and calls the EntityBackup.restoreRecord method to
   * restore the records backed up values. It skips this process if this.isEmpty or
   * pRecordset is unassigned or empty. It throws an exception is recordClass is
   * undefined and does not match the backed up ViewClass. All exceptions are logged.
   * @param recordClass extends EntityWrapper
   * @param ownerId String
   * @param recordSet List<EntityWrapper>
   * @throws Exception
   */
  public void restoreRecords(Class<? extends EntityWrapper> recordClass, String ownerId,
          List<EntityWrapper> recordSet) throws Exception {
    try {
      if ((this.isEmpty()) || (recordSet == null) || (recordSet.isEmpty())) {
        return;
      }
      
      if (!this.isBackup(recordClass, ownerId)) {
        throw new Exception("The EntityView Class[" + recordClass.getSimpleName()
                + "] and OwnerId[" + ownerId + "] do not match the backup's"
                + " View Class and OwnerId references.");
      }
      
      /** Restore each record by locating its entityBackup in backupData list and if 
       * located call the entityBackup's restoreData passing the record */
      for (EntityWrapper record : recordSet) {
        EntityBackup pBackup = (record == null)? null: this.getEntityBackup(record);
        if (pBackup == null) {
          continue;
        }
        pBackup.restoreRecord(record);
      }
      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.restoreRecords Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  /**
   * <p>Called to restored the backed up records for recordClass. It initiates the the
   * EntityAajx record for each backed up record and calls the EntityBackup's
   * restoreRecord method to restore the records backed up values.</p>
   * <p>It throws an exception is recordClass is undefined and does not match the
   * backed up ViewClass. All exceptions are logged.</p>
   * @param recordClass extends EntityWrapper
   * @param ownerId String
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public void restoreRecords(Class<? extends EntityWrapper> recordClass, String ownerId) 
          throws Exception {
    try {
      if (this.isEmpty()) {
        return;
      }
      
      if (recordClass == null) {
        throw new Exception("The EntityWrapper Class reference is unassigned.");
      } else if (!this.isBackup(recordClass, ownerId)) {
        throw new Exception("The EntityView Class["
                + recordClass.getSimpleName()
                + "] and OwnerId[" + ownerId + "] do not match the backup's"
                + " View Class and OwnerId references.");
      }
      
      /* Initiate the Record for each EntityBackup and call the EntityBackup's
       * restoreRecord method to restroe the records data */
      EntityFacade facade = EntityWrapper.getFacadeByWrapper(recordClass);
      for (EntityBackup recordBackup : this.backupData) {
        if ((recordBackup == null) && (!recordBackup.isBackedUp())) {
          continue;
        }
        
        Serializable recordId = recordBackup.getRecordId();
        Serializable entBean = facade.find(recordId);
        if (entBean != null) {
          EntityWrapper record = EntityWrapper.newFromBean(recordClass, entBean);
          if (record != null) {
            recordBackup.restoreRecord(record);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.restoreRecords Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
  }
  
  /**
   * Locate the EntityBackup for pRecord. Return null if !this.isBackUp or the record's
   * backup is not found.
   * @param record record if type EntityWrapper
   * @return EntityBackup
   */
  public EntityBackup getEntityBackup(EntityWrapper record) {
    EntityBackup pResult = null;
    try {
      if ((record != null) && (!this.isEmpty())) {
        for (EntityBackup pBackup : this.backupData) {
          if ((pBackup != null) && (pBackup.isBackedUpRecord(record))) {
            pResult = pBackup;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityBackup Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * Reset the EntityList Backup
   */
  public final void resetBackup() {
    this.backupId = null;
    this.backupData = null;
    this.recordClass = null;
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
        pResult.put("entityViewClass", this.recordClass);
        pResult.put("ownerid", this.ownerId);
        if ((this.backupData != null) && (!this.backupData.isEmpty())) {
          Integer iCnt = 0;
          for (int iRec = 0; iRec < this.backupData.size(); iRec++) {
            EntityBackup pBackUp = this.backupData.get(iRec);
            if (pBackUp.isEmpty()) {
              continue;
            }
            
            iCnt++;
            byte[] pObjData = BlobSerializer.toByteArray(pBackUp);
            String sKey = "Rec" + iCnt.toString();
            pResult.put(sKey, pObjData);
          }
          pResult.put("recordCount", iCnt);
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
        this.recordClass = pData.getCasted("entityViewClass",null);
        this.ownerId = pData.getCasted("ownerId",null);
        
        Integer iRecCount = pData.getCasted("recordCount",0);
        if ((iRecCount != null) && (iRecCount > 0)) {
          this.backupData = new ArrayList<>();
          for (int iRec = 1; iRec <= iRecCount; iRec++) {
            String sKey = "Rec" + Integer.toString(iRec);
            byte[] pObjData = pData.getCasted(sKey, null);
            if ((pObjData != null) && (pObjData.length > 0)) {
              EntityBackup pBackup = BlobSerializer.fromByteArray(pObjData);
              if (pBackup != null) {
                this.backupData.add(pBackup);
              }
            }
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>    
}
