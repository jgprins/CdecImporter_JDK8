package bubblewrap.entity.core;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.enums.*;
import bubblewrap.core.events.*;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.context.*;
import bubblewrap.entity.context.ForeignKey;
import bubblewrap.entity.enums.*;
import bubblewrap.entity.filters.*;
import bubblewrap.entity.interfaces.IEntityFilter;
import bubblewrap.io.DataEntry;
import bubblewrap.io.MapperDelegate;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

/**
 * <p>The EntityFacade is a stateful class in support of persistent Entity Bean. It
 * provides all the support for retrieving, creating, updating or deleting individual
 * records in the underlying database - and maintain the parent-child relationships
 * defined by registered {@link ForeignKey ForeignKeys}.</p> 
 * <p>It is provide the functionality to retrieve a record or a recordset using the 
 * following options: <ul>
 * <li><b>Using {@linkplain IEntityFilter EntityFilters}:</b> You can build a complex 
 * EntityFilter and use any of the <i>find</i> or <i>findAll</i> overrides to retrieve
 * the records.</li>
 * <li><b>Using {@linkplain NamedQuery NamedQueries} annotation:</b> A NamedQuery 
 * annotation can be added to an Entity class, </li>
 * </ul>
 * @author kprins
 * @version 1.0.
 */
public class EntityFacade <TBean extends Serializable> 
                                               implements Serializable { 

  // <editor-fold defaultstate="collapsed" desc="Static Field/methods">
  /**
   * The EntityFacade's Logger
   */
  protected final static Logger logger 
                                = Logger.getLogger(EntityFacade.class.getSimpleName());
  /**
   * A Flag that can be set to debug the FcoFacade processes (default = false), When set
   * to true, trapped errors will be logged. Otherwise, the errors will be ignored.
   */
  public static Boolean _DEBUG_ON = false;
    
  /**
   * Set pEntity.Field[sField]'s value. Throw an exception if the field does not
   * support a SET-method or the field does not exist in the entity or pEntity is not an
   * instance of pClass.
   * @param pClass a Serializable Class
   * @param entityBean a Serializable instance of pClass
   * @param fieldInfo the field's EntityFieldInfo
   * @param value the field's new value (can be null is null is allowed).
   * @throws Exception if the field does not support a SET-method or the field does not
   * exist in the entity or pEntity is not an instance of pClass or assigning the value
   * to the Entity's field failed.
   */
  @SuppressWarnings("unchecked")
  private static <TBean extends Serializable, TValue> void 
          setFieldValue(TBean entityBean, FieldInfo fieldInfo, TValue value) 
          throws Exception {
    if (entityBean == null) {
      throw new Exception("The Entity Instance if undefined");
    }
    
    if (fieldInfo == null) {
      throw new Exception("The EntityFieldInfo is undefined");
    }
    String fieldName = fieldInfo.entityPath.fieldName;
    
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    if (entityClass == null) {
      throw new Exception("Entity Class is not accessable");
    }
    
    if (fieldInfo.setMethod == null) {
      throw new Exception("The SET-method for "
              + "'" + fieldInfo.entityPath.toString() + "' is not supported");
    }
    
    try {
      Object[] args = {value};
      Object result = fieldInfo.setMethod.invoke(entityBean, args);
    } catch (IllegalAccessException | IllegalArgumentException 
            | InvocationTargetException pExp) {
      throw new Exception("Setting Field '" + entityClass.getSimpleName()
              + "." + fieldName + "' failed because:" + pExp.getMessage());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for TBean's EnityManager
   */
  private PuEntityManager puManager;
  /**
   * Placeholder for TBean's EnityManager
   */
  private EntityManager entMngr;
  /**
   * A Transient EntityTransaction used if this.puManager.useJTA = false.
   */
  private transient EntityTransaction transaction;
  /**
   * Placeholder for TBean's EnityManager
   */
  private Class<TBean> entityClass;
  /**
   * Placeholder for TBean's EntityContext
   */
  private EntityContext<TBean> entityContext;
  /**
   * The registered ForeignKeyInfo for Parents
   */
  private List<ForeignKey> parentFKList;
  /**
   * The registered ForeignKeyInfo for Children
   */
  private List<ForeignKey> childFKList;
  /**
   * The current primary key instance for the Entity Bean class.
   */
  private EntityWrapper<TBean> pkFilter;
  /**
   * The Specified Sort setting for recordSet searches
   */
  private EntitySort entitySort;
  /**
   * A Placeholder for the TBean's ActiveOnly Filter 
   * (Default = EntityBoolFilter[field=disabled; value=false]
   */
  private IEntityFilter<TBean> activeOnlyFilter;
  /**
   * Flag controlling whether only active (disabled=false) records are returned in 
   * a recordset search
   */
  private Boolean activeOnly;
  /**
   * Flag controlling whether ForeignKey filters should be set in a recordset search
   */
  private Boolean doPkFilter = true;
  /**
   * Flag controlling whether Entity Edits are allowed
   */
  private Boolean doEdits = false;
  /**
   * Counter for maintaining the isInitiating state for nested calls.
   */
  private int initCount = 0;
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Event Senders">
  /**
   * EventHandler for sending a PkFilter Changed Event
   */
  public final EventHandler PkFilterChanged;
  /**
   * Method called to fire the PkFilter Changed Event
   */
  protected void firePkFilterChanged() {
    this.PkFilterChanged.fireEvent(this, new EventArgs());
  }
  //</editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with parameters to initialize the EntityFacade
   * @param entClass the entityBean's class
   * @param entMngr the Facade's Persistent Unit EntityManager reference
   */
  public EntityFacade(Class<TBean> entClass, PuEntityManager puManager) {
    try {
      if (entClass == null) {
        throw new NullPointerException("The Entity Class reference cannot be "
                + "unassigned");
      }
      
      if (puManager == null) {
        throw new NullPointerException("The Persistent Unit EntityManager wrapper "
                + "reference cannot be unassigned");
      }
      this.PkFilterChanged = new EventHandler();
      
      this.entityClass = entClass;
      this.puManager = puManager;
      this.entMngr = puManager.getEntityManager();
      if (entMngr == null) {
        throw new NullPointerException("The PuEntityManager's EntityManager is "
                + "unassigned.");
      }

      this.entityContext = null;
      this.parentFKList = null;
      this.childFKList = null;
      this.pkFilter = null;
              
      this.activeOnly = false;
      this.entitySort = null;
      this.doPkFilter = true;
      this.doEdits = false;
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.new Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException("Initiating the EntityFacase for Bean Class["
              + this.getEntityClassName() + "] failed.", exp);
    }
  }

  /**
   * OVERRIDE: Release local resources before calling the super method
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    this.PkFilterChanged.clear();
    this.entMngr = null;
    this.entityContext = null;
    this.parentFKList = null;
    this.childFKList = null;
    this.pkFilter = null;
    super.finalize();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * Get Entity Class supported by the EntityFacade
   * @return Class<TBean>
   */
  public final Class<TBean> getEntityClass() {
    return this.entityClass;
  }

  /**
   * Get the SimpleName of the Facade's Entity Class
   * @return the simpleName of the supported bean's class
   */
  public final String getEntityClassName() {
    Class<TBean> pClass = this.getEntityClass();
    return (pClass == null) ? "" : pClass.getSimpleName();
  }
  
  /**
   * Get a reference to the EntityFacade's assigned entityContext
   * @return assigned entityContext
   */
  public final EntityContext<TBean> getEntityContext() {
    if (this.entityContext == null) {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new NullPointerException("Unable to access the Applications "
                + "BwAppContext.");
      }
      this.entityContext = appCtx.getEntityContext(this.entityClass);
    }
    return this.entityContext;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="ForeignKey Management">
  /**
   * Get the ForeignKeyInfo for this Bean parent classes
   * @return a list of ForeignKeyInfo - the list is empty if none is registered.
   */
  public final List<ForeignKey> getParentForeignKeys() {
    if (this.parentFKList == null) {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new NullPointerException("Unable to access the Applications "
                + "BwAppContext.");
      }
      this.parentFKList = appCtx.getForeignKeysByChild(this.getEntityClass());
      if (this.parentFKList == null) {
        this.parentFKList = new ArrayList<>();
      }
    }
    return parentFKList;
  }
  
  /**
   * Get the ForeignKeyConstraint for this Child Bean classes
   * @return a list of ForeignKForeignKeyist is empty if none is registered.
   */
  public final List<ForeignKey> getChildForeignKeys() {
    if (this.childFKList == null) {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new NullPointerException("Unable to access the Applications "
                + "BwAppContext.");
      }
      this.childFKList = appCtx.getForeignKeysByParent(this.getEntityClass());
      if (this.childFKList == null) {
        this.childFKList = new ArrayList<>();
      }
    }
    return childFKList;
  }
  
  /**
   * Called to get the Entity's Parent ForeignKey constraint for the OwnerParent 
   * @return the foreignKey or null if no owner or no foreignKeys are assigned
   */
  public final ForeignKey  getParentForeignKey(Class<? extends Serializable> parentClass) {
    ForeignKey result = null;
    List<ForeignKey> parentList = this.getParentForeignKeys();
    if ((parentList != null) && (!parentList.isEmpty())) {
      for (ForeignKey foreignKeyInfo : parentList) {
        if (foreignKeyInfo.isParent(parentClass)) {
          result = foreignKeyInfo;
          break;
        }
      }
    }
    return result;
  } 
      
  /**
   * Get TBean class' ForeignKeyInfo for a specified child EntityBean class.
   * @param childClass the child bean class.
   * @return return the matching ForeignKey class or null if no match.
   */ 
  public final ForeignKey getChildForeignKey(Class<? extends Serializable> childClass) {
    ForeignKey result = null;
    List<ForeignKey> childList = this.getChildForeignKeys();
    if ((childList != null) && (!childList.isEmpty())) {
      
      for (ForeignKey foreignKey : childList) {
        if (foreignKey.isChild(childClass)) {
          result = foreignKey;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Check if this bean class are relForeignKey
   * @return Return true if the bean has parent ForeignKey Constraints
   */
  public boolean hasParentForeignKeys() {
    List<ForeignKey> parentList = this.getParentForeignKeys();
    return ((parentList != null) && (!parentList.isEmpty()));
  }
  
  /**
   * Check if this bean class are related to parent beans that are set
   * @return true if the bean has parent ForeignKeys and least one is set.
   */
  @SuppressWarnings("unchecked")
  public boolean hasSetParentPkFilters() {
    boolean result = false;
    List<ForeignKey> parentList = this.getParentForeignKeys();
    if ((parentList != null) && (!parentList.isEmpty()) && (this.puManager != null)) {
      for (ForeignKey foreignKey : parentList) {
        Class<? extends Serializable> parentClass = 
                                        foreignKey.associationPath.parentClass;
        EntityFacade parentFacade = this.puManager.getFacade(parentClass);
        if ((parentFacade != null) && (parentFacade.hasPkFilter())) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Validate the ForeingKey based on the ParentClass, retrieve the
 the pkFilter filter assignment for that Parent.
   * @param parentClass the parent class
   */
  public <TParent extends Serializable> EntityWrapper<TParent>
                                        getParentPkFilter(Class<TParent> parentClass) {
    EntityWrapper<TParent> result = null;
    if (this.getParentForeignKey(parentClass) != null) {
      result = (this.puManager ==null)? null: this.puManager.getPkFilter(parentClass);
    }
    return result;
  }
  
  /**
   * Call to reset the PrimaryKey Filter of the Parent ForeignKeys. It retrieve the 
   * EntityFacade of each parent ForeignKeys and call its resetPkFilter method.
   */
  @SuppressWarnings("unchecked")
  public void resetParentPkFilters() {
    List<ForeignKey> parentFKeys = this.getParentForeignKeys();
    try {
      if ((parentFKeys == null) || (parentFKeys.isEmpty()) || (this.puManager ==null)) {
        return;
      }
      
      for (ForeignKey foreignKey : parentFKeys) {
        Class<? extends Serializable> parentClass = 
                                                foreignKey.associationPath.parentClass;
        EntityFacade parentFacade = this.puManager.getFacade(parentClass);
        if (parentFacade != null) {
          parentFacade.resetPkFilter();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.resetParentPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="PrimaryKey Filter Management">
  /**
   * Get the Facade's PrimaryKey Filter
   * @return the assigned value or null if unassigned.
   */
  public EntityWrapper<TBean> getPkFilter() {
    return this.pkFilter;
  }
  
  /**
   * Set the Facade's PrimaryKey - override prior settings. If the setting is changing
   * and this.pkFilter!=null, call this.pkFilter.beforeResetAsPkFilter(). After 
   * changing the pkFilter, call the new priamryKey's afterSetAsPkFilter() method.
   * It also fires the PrimaryKeyChangedEvent to notify listener that the PrimaryKey 
   * has changed.
   * @param pkFilter the new PrimaryKey Filter (can be null). 
   */
  public void setPkFilter(EntityWrapper<TBean> pkFilter) {
    if (this.pkFilter != pkFilter) {
      if (this.pkFilter != null) {
        this.pkFilter.beforeResetAsPkFilter();
      }
      this.pkFilter = pkFilter;
      if (pkFilter != null) {
        pkFilter.afterSetAsPkFilter();
      }
      this.firePkFilterChanged();
    }
  }
  
  /**
   * Reset the current PrimaryKey (set it to null). If the this.pkFilter!=null, 
   * call this.pkFilter.beforeResetAsPkFilter() before resetting the key. It also
   * fires the {@linkplain #PkFilterChanged this.PkFilterChanged} Event to notify 
   * listener that the PkFilter has changed.
   */
  public void resetPkFilter() {
    if (this.pkFilter != null) {
      this.pkFilter.beforeResetAsPkFilter();
      this.pkFilter = null;
      this.firePkFilterChanged();
    }
  }
  
  /**
   * Check is a his.pkFilter is assigned
   * @return true if not null.
   */
  public boolean hasPkFilter() {
    return (this.pkFilter != null);
  }
  
  /**
   * Check if the assigned preimaryKey is the target instance
   * @param target a target EntityWrapper
   * @return true if neither is null and if is the same instance.
   */
  public boolean isPkFilter(EntityWrapper<TBean> target) {
    return ((this.pkFilter != null) && (target != null)
            && (this.pkFilter == target));
  }
  
  /**
   * Check if the assigned pkFilter.equals(target)
   * @param target a target EntityWrapper
   * @return true if neither is null and if this.pkFilter.equals(target).
   */
  public boolean isPkFilterRecord(EntityWrapper<TBean> target) {
    return ((this.pkFilter != null) && (target != null)
             && DataEntry.isEq(this.pkFilter, target));
  }
  
  /**
   * Check if the assigned pkFilter.recordId.equals(recordId)
   * @param recordId a target Entity's recordId
   * @return true if neither is null and if this.pkFilter.recordId.equals(recordId).
   */
  public boolean isPkFilterRecordId(Serializable recordId) {
    Object pkRecId = (this.pkFilter == null)? null: this.pkFilter.getRecordId();
    return ((pkRecId != null) && (recordId != null)
             && DataEntry.isEq(pkRecId, recordId));
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="EntityManager Delegate Methods">
  /**
   * Check is the facade's entity has an auto-assigned PrimaryKey type. It retrieves
   * the Entity's PrimaryKeyType (pkType) form its EntityContext and return {@linkplain 
   * PrimaryKeyType#isAutoRecorded(bubblewrap.core.enums.PrimaryKeyType) 
   * PrimaryKeyType.isAutoRecorded(pkType)}
   * @return true if auto-assigned 
   */
  public boolean isAutoAssignedPk() {
    boolean result = false;
    EntityContext<TBean> entCtx = this.getEntityContext();
    PrimaryKeyType pkType = (entCtx == null)? null: entCtx.getPrimaryKeyType();
    return ((pkType != null) && (PrimaryKeyType.isAutoRecorded(pkType)));
  }
  
  /**
   * Insert a new entity to the underlying dataset. If the EntityContext.RecordIdField is
   * defined and the current RecordID=null, use the EntityContext.RecordIdType to
   * to generate a new id as follows:</p><ul>
   * <li>RecPrimaryKeyTypeID: NewId = DataEntry.newUniqueId();</li>
   * <li>RecordPrimaryKeyTypeNTEGER: NewId = this.getMaxInteger(sRecIdFld)+1;</li>
   * </ul>
   * If the returned value is not null, assign it to entityBean before inserting the record.
   * Else log an exception and leave recordId unassigned..
   * @param entityBean of class TBean
   * @return true if a new recordId was assigned.
   */
  public boolean initRecordId(TBean entityBean) {
    boolean result = false;
    try {
      EntityContext<TBean> entCtx = this.getEntityContext();
      FieldInfo idFieldInfo = null;
      Object curId = null;
      if ((entCtx != null) &&
          ((idFieldInfo = entCtx.getPrimaryKey()) != null) &&
          ((curId = ReflectionInfo.getFieldValue(entityBean, idFieldInfo)) == null)) {
        Object newId = null;
        PrimaryKeyType idType = entCtx.getPrimaryKeyType();
        switch (idType) {
          case GUID:         
            newId = DataEntry.newUniqueId();
            break;
          case AUTOINTEGER:
            Integer iNewId = this.getMaxInteger(idFieldInfo.entityPath.fieldName);
            iNewId = ((iNewId == null) || (iNewId == 0))? 1: iNewId+1;
            newId = iNewId;
            break;
          case AUTOLONG:
            Long lNewId = this.getMaxLong(idFieldInfo.entityPath.fieldName);
            lNewId = ((lNewId == null) || (lNewId == 0))? 1: lNewId+1;
            newId = lNewId;
            break;
          case DATETIME:        
            newId = DateTime.getNowAsDate();
            break;
          case COMPOSIT: 
          case MANUAL:  
            throw new Exception("The RecordId for RecordIdType[" + idType.toString()
                    + "] is not assigned.");
          default:
            throw new Exception("RecordIdType[" + idType.toString()
                    + "] is not supported");
        }
          
        if (newId == null) {
          throw new Exception("Inititiating RecordId for Class[" 
                  + this.getEntityClassName() +"] failed.");
        }
        EntityFacade.setFieldValue(entityBean, idFieldInfo, newId);
        result = true;
      }
    } catch (Exception pExp) {
      logger.log(Level.SEVERE, "{0}.initRecordId Error:\n{1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to reset the entityBean's recordId if a submit/create failed.
   * @param entityBean the entity to reset.
   */
  public void resetRecordId (TBean entityBean){
    try {
      EntityContext<TBean> entCtx = null;
      FieldInfo idFieldInfo = null;
      if ((entityBean != null) &&((entCtx = this.getEntityContext()) != null)
              && ((idFieldInfo = entCtx.getPrimaryKey()) != null)) {
        EntityFacade.setFieldValue(entityBean, idFieldInfo, null);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetRecordId Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Create/Insert the record after checking its recordId - Log any errors
   * @param entityBean a entity bean of the supported class
   * @throws Exception if the process fails
   */
  public boolean create(TBean entityBean) throws Exception {
    boolean result = false;
    if (entityBean == null) {
      throw new Exception("The new entity bean cannot be unassigned.");
    }

    String err = null;
    UserTransaction userTx = null;
    boolean addRecordId = false;
    try {
      if ((this.puManager != null)
              && ((userTx = this.puManager.getUserTransaction()) != null)) {
        userTx.begin();
        this.entMngr.joinTransaction();
      }

      addRecordId = this.initRecordId(entityBean);
      this.entMngr.persist(entityBean);
      if (userTx != null) {
        userTx.commit();
      }
      result = true;
    } catch (EntityExistsException exp1) {
      err = "Entity " + this.getEntityClassName() + "[" + entityBean.toString()
              + "] aleady exists.";
    } catch (TransactionRequiredException exp2) {
      err = "Creating Entity " + this.getEntityClassName() + "[" + entityBean.toString()
              + "] failed. The EntityManager requires a Transaction.";
    } catch (PersistenceException exp3) {
      err = "Entity " + this.getEntityClassName() + "[" + entityBean.toString()
              + "] can not be created due to a persistence error.";
    } catch (Exception pExp) {
      err = "Entity[" + this.getEntityClassName() + "] Error: " + pExp.getMessage();
    }

    if ((!result) || (err != null)) {
      err = (err != null) ? err : "Creating Entity " + this.getEntityClassName()
              + "[" + entityBean.toString() + "] failed. Reason unkown";
      logger.log(Level.SEVERE, "{0}.create Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), err});
      if (userTx != null) {
        userTx.rollback();
      }

      /* Reset the recordId on error if previously set */
      if (addRecordId) {
        this.resetRecordId(entityBean);
      }
      throw new Exception(err);
    }
    return result;
  }

  /**
   * Save edits to the persistent entity bean
   * @param bean an instance of the supported class
   * @return the saved instance
   * @throws Exception if the process fails
   */
    public TBean edit(TBean bean) throws Exception {
    TBean result = bean;
    if (bean == null) {
      throw new Exception("The entity bean cannot be unassigned.");
    }
    
    String err = null;
    UserTransaction userTx = null;
    boolean success = false;
    try {
      if ((this.puManager != null) && 
                              ((userTx = this.puManager.getUserTransaction()) != null)) {
        userTx.begin();
        this.entMngr.joinTransaction();
      }
//      EntityManager em = this.puManager.getEntityManager();
//      if (em == null) {
//        throw new IllegalArgumentException("Initiating the EntityManager Failed.");
//      }
      result = this.entMngr.merge(bean);  
//      result = this.entMngr.merge(bean);  
      if (userTx != null) {
        userTx.commit();
      }
      success = true;
    } catch (IllegalArgumentException exp1) {
      err = "Submit Entity " + this.getEntityClassName() + "[" + bean.toString() 
              + "] Error: \n" + exp1.getMessage();
    } catch (TransactionRequiredException exp2) {
      err = "Submit Entity " + this.getEntityClassName() + "[" + bean.toString() 
              + "] failed. The EntityManager requires a Transaction.";
    } catch (Exception pExp) {
      err = pExp.getMessage();
    }
    
    if ((!success) || (err != null)) {
      err = (err != null)? err: "Submit Entity " + this.getEntityClassName() 
              + "[" + bean.toString()  + "] failed. Reason unkown";
      logger.log(Level.SEVERE, "{0}.edit Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), err});
      if (userTx != null) {
        userTx.rollback();
      }
      throw new Exception(err);      
    }
    return result;
  }

  /**
   * removeRecord the record from the persistent data source
   * @param bean a instance of the supported class
   * @throws Exception if the process fails
   */
    public void remove(TBean bean) throws Exception {
    String err = null;
    if (bean == null) {
      throw new Exception("The entity bean cannot be unassigned.");
    }
    
    UserTransaction userTx = null;
    boolean success = false;
    try {
      if ((this.puManager != null) && 
                              ((userTx = this.puManager.getUserTransaction()) != null)) {
        userTx.begin();
        this.entMngr.joinTransaction();
      }
      
      this.entMngr.remove(this.entMngr.merge(bean));
      
      if (userTx != null) {
        userTx.commit();
      }
      success = true;
    } catch (IllegalArgumentException exp1) {
      err = "Remove Entity " + this.getEntityClassName() + "[" + bean.toString() 
              + "] Error: \n" + exp1.getMessage();
    } catch (TransactionRequiredException exp2) {
      err = "Removing Entity " + this.getEntityClassName() + "[" + bean.toString() 
              + "] failed. The EntityManager requires a Transaction.";
    } catch (Exception pExp) {
      err = pExp.getMessage();
    }
    
    if ((!success) || (err != null)) {
      err = (err != null)? err: "Remove Entity " + this.getEntityClassName() 
              + "[" + bean.toString()  + "] failed. Reason unkown";
      logger.log(Level.SEVERE, "{0}.remove Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), err});
      if (userTx != null) {
        userTx.rollback();
      }
      throw new Exception(err);      
    } else {
      
    }
  }

  /**
   * refreshRecord the persistent Entity from the Database.
   * @param bean a Serializable instance of the supported class
   * @return the new retrieved Serializable instance
   * @throws Exception if the process fails
   */
    public TBean refresh(TBean bean) throws Exception {
    TBean result = bean;
    try {
      Object recordId = null;
      if ((bean == null) && ((recordId = this.getBeanRecordId(bean)) != null)) {
        EntityManagerFactory factory = null;
        Class<TBean> beanClass = null;
        Cache entityCache = null;
        if ((this.entMngr != null) && 
                ((factory = this.entMngr.getEntityManagerFactory()) != null) &&
                ((entityCache = factory.getCache()) != null) &&
                ((beanClass = this.getEntityClass()) != null) &&
                (entityCache.contains(beanClass, recordId))) {
          entityCache.evict(beanClass, recordId);
        } else if (this.entMngr != null) {
          UserTransaction userTx = null;
          try {
            if ((this.puManager != null) && 
                              ((userTx = this.puManager.getUserTransaction()) != null)) {
              userTx.begin();
              this.entMngr.joinTransaction();
            }
            this.entMngr.detach(bean);
            if (userTx != null) {
              userTx.commit();
            }
          } catch (Exception innerExp) {
            if (userTx != null) {
              userTx.rollback();
            }
          }
        }
        result = this.find(recordId);
      }
    } catch (IllegalStateException | SecurityException | SystemException pExp) {
      logger.log(Level.SEVERE, "{0}.refresh Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw pExp;
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Transaction Management">
  /**
   * Begin a new the EntityManager transaction - ignored if this.doUseJTA
   */
  public void beginTransaction() {
    if ((this.puManager != null) && (!this.puManager.doUseJTA()) &&
            (this.entMngr != null)) {
      this.transaction = this.entMngr.getTransaction();
      if (this.transaction != null) {
        this.transaction.begin();
      }
    }
  }

  /**
   * Commit the EntityManager's current transaction - ignored if this.doUseJTA
   */
  public void commitTransaction() {
    if ((this.puManager != null) && (!this.puManager.doUseJTA()) &&
            (this.entMngr != null)) {
      if ((this.transaction == null) && 
              ((this.transaction = this.entMngr.getTransaction()) != null)){
        this.transaction.commit();
      }
      this.transaction = null;
    }
  }

  /**
   * Rollback the EntityManager's current transaction - ignored if this.doUseJTA
   */
  public void rollbackTransaction() {
    if ((this.puManager != null) && (!this.puManager.doUseJTA()) &&
            (this.entMngr != null)) {
      if ((this.transaction == null) && 
              ((this.transaction = this.entMngr.getTransaction()) != null)){
        this.transaction.rollback();
      }
      this.transaction = null;
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Find Bean Methods">
  /**
   * Find and return the record with matching RecordId=id.
   * @param recordId the requested Object's primary key ID
   * @return instance of TBean or null if not found
   */
  @SuppressWarnings("unchecked")
    public TBean find(Object recordId) {
    TBean result = (this.entMngr == null) ? null : 
                                 (TBean) this.entMngr.find(this.entityClass, recordId);
    return result;
  }

  /**
   * Return the Entity as an object of class pClass with primary key ID.
   * @param <TClass> extends Serializable
   * @param entityClass the class of the entity
   * @param recordId the entity's recordId (primary key value)
   * @return a instance of TClass or null if not found.
   */
    public <TClass extends Serializable> TClass
                                  findAny(Class<TClass> entityClass, Object recordId) {
    return ((entityClass == null) || (recordId == null))? null : 
                                        this.entMngr.find(entityClass, recordId);
  }

  /**
   * Return the Entity as an object, instead of a cast Entity
   * @param recordName String
   * @return the TBean.recordId of the instance found by its recordName=sRecName (or 
   * null if not found)
   */
    public Object findRecIdByName(String recordName, int facadfilterOptions) {
    Object result = null;
    try {
      recordName = DataEntry.cleanString(recordName);
      EntityContext<TBean> entCtx = this.getEntityContext();
      if ((recordName != null) && (entCtx != null)) {
        FieldInfo recNameField =  entCtx.getAliasField(BwFieldAlias.RECORD_NAME);
        FieldInfo recIdField = entCtx.getPrimaryKey();
        if ((recNameField != null) && (recIdField != null)) {
          TBean record = this.findByField(recNameField.entityPath.fieldName, recordName, 
                                                                  facadfilterOptions);
          if (record != null) {
            result = ReflectionInfo.getFieldValue(record, recIdField);
          }
        }
      }
    } catch (Exception pExp) {
      result = null;
    }
    return result;
  }

  /**
   * This methods return the first item in the recordset retrieve by
   * using the same standard filters as used by findAll, after applying the
   * additional EntityValueFilter defined by (sFieldName,pValue).  Thus,
   * returning the a value in a subset within the default EntityView recordset.
   * Throw exceptions if fieldName is undefined or not found in the Entity.
   * If pValue=null, it returns record where the field is undefined (null).
   * @param fieldName the field to search on
   * @param fieldValue the value to search for.
   * @return an instance of TBean or null if not found
   * @throws Exception
   */
    public TBean findByField(String fieldName, Object fieldValue) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.findByField(fieldName, fieldValue, filterOptions);
  }

  /**
   * This methods return the first item in the recordset retrieve by
   * using the same standard filters as defined by filterOptions, after applying the
   * additional EntityValueFilter defined by (sFieldName,pValue).  Thus,
   * returning the a value in a subset within the default EntityView recordset.
   * Throw exceptions if fieldName is undefined or not found in the Entity.
   * If pValue=null, it returns record where the field is undefined (null).
   * @param fieldName the field to search on
   * @param fieldValue the value to search for.
   * @param filterOptions the FilterOptionEnums to apply
   * @return an instance of TBean or null if not found
   * @throws Exception
   */
    public TBean findByField(String fieldName, Object fieldValue, int filterOptions)
          throws Exception {
    TBean pResult = null;
    FilterRange range = new FilterRange(1);
    List<TBean> pResultList = 
                    this.findRangeByField(range, fieldName, fieldValue, filterOptions);
    if ((pResultList != null) && (!pResultList.isEmpty())) {
      pResult = pResultList.get(0);
    }
    return pResult;
  }

  /**
   * This methods return the first item in the recordset retrieved by using the
   * same standard filters as used by findAll, after applying the additional
   * filter defined by pFilter.  Thus, returning the a record within the default
   * EntityView recordset.
   * @param entityFilter the IEntityFilter to apply
   * @return an instance of TBean or null if not found
   * @throws Exception
   */
    public TBean findByFilter(IEntityFilter<TBean> entityFilter) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.findByFilter(entityFilter, filterOptions);
  }

  /**
   * This methods return the first item in the recordset retrieved by using the
   * same standard filters as defined by filterOptions, after applying the additional
   * filter defined by pFilter.  Thus, returning the a record within the default
   * EntityView recordset.
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions the FilterOptionEnums to apply
   * @return an instance of TBean or null if not found
   * @throws Exception
   */
    public TBean findByFilter(IEntityFilter<TBean> entityFilter, int filterOptions)
          throws Exception {
    TBean result = null;
    FilterRange range = new FilterRange(1);
    List<TBean> resultList = this.findRangeByFilter(range,  entityFilter, filterOptions);
    if ((resultList != null) && (!resultList.isEmpty())) {
      result = resultList.get(0);
      
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Find Recordsets">
  /**
   * This methods return records using the same standard filters as used by
   * findAll, after applying the additional EntityValueFilter defined by
   * (sFieldName,pValue).  Thus, returning the subset within the default
   * EntityView recordset. Throw exceptions is fieldName is undefined or not
   * found in the Entity. If pValue=null, it return records where the field is
   * undefined (null).
   * @param fieldName the field to search on
   * @param fieldValue the value to search for
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
    public List<TBean> findAllByField(String fieldName, Object fieldValue)
          throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.findAllByField(fieldName, fieldValue, filterOptions);
  }

  /**
   * This methods return records using the same standard filters as defined by
   * filterOptions, after applying the additional EntityValueFilter defined by
   * (sFieldName,pValue).  Thus, returning the subset within the default
   * EntityView recordset. Throw exceptions is fieldName is undefined or not
   * found in the Entity. If pValue=null, it return records where the field is
   * undefined (null).
   * @param fieldName the field to search on
   * @param fieldValue the value to search for
   * @param filterOptions the FilterOptionEnums to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findAllByField(String fieldName, Object fieldValue, 
          int filterOptions) throws Exception {
    if ((fieldName == null) || (fieldName.trim().equals(""))) {
      throw new Exception("FieldName cannot be undefined");
    }

    FieldInfo fldInfo = this.getFieldInfo(fieldName);
    if (fldInfo == null) {
      throw new Exception("Unable to locate Field[" + fieldName
              + "] in Entity[" + this.getEntityClassName() + "]");
    }
    
    if ((fldInfo.valueConverter != null) && 
      (fldInfo.valueConverter.isValidInput(fieldValue))) {
      fieldValue = fldInfo.valueConverter.toFieldValue(fieldValue);
    }

    @SuppressWarnings("unchecked")
    IEntityFilter<TBean> entityFilter = new EntityValueFilter<>(fieldName,
                                                fieldValue, EntityFilterEnums.EQUAL);
    if (entityFilter == null) {
      throw new Exception("Initiating a ValueFilter for Field[" + fieldName +
              "] failed.");
    }

    return this.findAllByFilter(entityFilter, filterOptions);
  }

  /**
   * This methods return records using the same standard filters as used by
   * findAll, after applying the additional filter defined by pFilter.  Thus,
   * returning the subset within the default EntityView recordset.
   * If pFilter ==null, the result will be equivalent to calling FindAll().
   * @param entityFilter the IEntityFilter to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
    public List<TBean> findAllByFilter(IEntityFilter<TBean> entityFilter) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.findAllByFilter(entityFilter, filterOptions);
  }

  /**
   * This methods return records using the standard filters define by filterOptions
   * after applying the additional filter defined by pFilter.  Thus,
   * returning the subset within the default EnityView recordset.
   * If pFilter == null, the result that are only filters by filterOptions.
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions the FilterOptionEnums to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findAllByFilter(IEntityFilter<TBean> entityFilter, 
          int filterOptions) throws Exception {
    List<TBean> result = new ArrayList<>();
    EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
    if ((entityFilter != null) && (entityFilter.isSet())) {
      groupFilter.addFilter(entityFilter);
    }

    this.addActiveOnlyFilter(groupFilter, filterOptions);
    this.addSystemItemFilter(groupFilter, filterOptions);
    this.addFKFilter(groupFilter, filterOptions);

    CriteriaBuilder cb = this.entMngr.getCriteriaBuilder();
    CriteriaQuery cq = cb.createQuery(this.entityClass);
    Root<TBean> rt = cq.from(this.entityClass);

    groupFilter.setFilter(rt, cb, cq);
    cq.distinct(groupFilter.isDistinct());

    Query query = entMngr.createQuery(cq);
    
    List<TBean> pList = query.getResultList();
    if (pList != null) {
      if (this.entitySort != null) {
        Comparator<TBean> comparator = new EntityComparator<>(this.entitySort);
        if (comparator != null) {
          Collections.sort(pList, comparator);
        }
      }
      result = pList;
    }
    return result;
  }

  /**
   * This method is called by the EnityView to retrieve the records in the
   * dataset that satisfy conditions defined in a group filter build based on
   * filterOptions, where filterOptions include (ACTIVEONLY if (activeOnly));
   * (NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter)). If no record
   * are found or if an error occur, it returns and empty List.  
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findAll() throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter()); 
    return this.findAllByFilter(null, filterOptions);
  }

  /**
   * This method is called by the EnityView to retrieve the sub set of records
   * in the specified range (i.e., new FilterRange(loIdx=0, size=10) from the
   * dataset that satisfy conditions defined in a group filter build based on
   * filterOptions, where filterOptions = (ACTIVEONLY if (this.activeOnly));
   * (NOTSYSTEM is (this.doEdit)); and  (FKFILTER if (this.doPkFilter)). If no record
   * are found or if an error occur, it returns and empty List.
   * @param range the range of value to return
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findRange(FilterRange range) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
                                                this.doEdits(), this.doPkFilter());

    return this.findRangeByFilter(range, null, filterOptions);
  }

  /**
   * This methods return sub set of records in the specified range (i.e.,
   * [iStartIndex,iEndIndex]) using the same standard filters as defined by
   * filterOptions, after applying the additional EntityValueFilter defined by
   * (sFieldName,pValue).  Thus, returning the subset within the default
   * EntityView recordset. Throw exceptions is fieldName is undefined or not
   * found in the Entity. If pValue=null, it return records where the field is
   * undefined (null).
   * @param range the range of value to return
   * @param fieldName the field to search on
   * @param fieldValue the value to search for
   * @param filterOptions the FilterOptionEnums to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findRangeByField(FilterRange range, String fieldName, 
                               Object fieldValue, int filterOptions) throws Exception {
    if ((fieldName == null) || (fieldName.trim().equals(""))) {
      throw new Exception("FieldName cannot be undefined");
    }

    FieldInfo fldInfo = this.getFieldInfo(fieldName);
    if (fldInfo == null) {
      throw new Exception("Unable to locate Field[" + fieldName
              + "] in Entity[" + this.getEntityClassName() + "]");
    }
    
    if ((fldInfo.valueConverter != null) && 
            (fldInfo.valueConverter.isValidInput(fieldValue))) {
      fieldValue = fldInfo.valueConverter.toFieldValue(fieldValue);
    }

    IEntityFilter<TBean> entityFilter = new EntityValueFilter<>(fieldName,
            fieldValue, EntityFilterEnums.EQUAL);
    if (entityFilter == null) {
      throw new Exception("Initiating a ValueFilter for Field[" + fieldName +
              "] failed.");
    }

    return this.findRangeByFilter(range, entityFilter, filterOptions);
  }
  
  /**
   * This method is called by the EntityView to retrieve the sub set of records
   * in the specified range (i.e., [iStartIndex,iEndIndex]) from the
   * dataset that satisfy conditions defined in a group filter build based on pFilter,
   * filterOptions, where filterOptions could include (ACTIVEONLY | NOTSYSTEM | FKFILTER).
   * If no record are found or if an error occur, it returns and empty List.
   * if this Sort Field is set it will return a sorted result set.
   * @param range the range of value to return
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions the FilterOptionEnums to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public List<TBean> findRangeByFilter(FilterRange range, 
                                IEntityFilter<TBean> entityFilter) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.findRangeByFilter(range, entityFilter, filterOptions);
  }
  /**
   * This method is called by the EntityView to retrieve the sub set of records
   * in the specified range (i.e., [iStartIndex,iEndIndex]) from the
   * dataset that satisfy conditions defined in a group filter build based on pFilter,
   * filterOptions, where filterOptions could include (ACTIVEONLY | NOTSYSTEM | FKFILTER).
   * If no record are found or if an error occur, it returns and empty List.
   * if this Sort Field is set it will return a sorted result set.
   * @param range the range of value to return
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions the FilterOptionEnums to apply
   * @return a List<TBean> of result or null if no results were found
   * @throws Exception
   */
    public List<TBean> findRangeByFilter(FilterRange range, 
            IEntityFilter<TBean> entityFilter, int filterOptions) throws Exception {
    List<TBean> result = new ArrayList<>();
    EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
    if ((entityFilter != null) && (entityFilter.isSet())) {
      groupFilter.addFilter(entityFilter);
    }
    this.addActiveOnlyFilter(groupFilter, filterOptions);
    this.addSystemItemFilter(groupFilter, filterOptions);
    this.addFKFilter(groupFilter, filterOptions);

    CriteriaBuilder cb = this.entMngr.getCriteriaBuilder();
    @SuppressWarnings("unchecked")
    CriteriaQuery cq = cb.createQuery(this.entityClass);
    @SuppressWarnings("unchecked")
    Root<TBean> rt = cq.from(this.entityClass);

    cq.distinct(groupFilter.isDistinct());
    groupFilter.setFilter(rt, cb, cq);

    @SuppressWarnings("unchecked")
    Query query = entMngr.createQuery(cq);
    if (range != null) {
      query.setMaxResults(range.getSize());
      query.setFirstResult(range.getLowIndex());
    }
    @SuppressWarnings("unchecked")
    List<TBean> resultList = query.getResultList();
    if (resultList != null) {
      if (this.entitySort != null) {
        Comparator<TBean> pComparer = new EntityComparator<>(this.entitySort);
        if (pComparer != null) {
          Collections.sort(resultList, pComparer);
        }
      }
      result = resultList;
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Get Record Counts">
  /**
   * This method is called by the EnityView get the record count in the
   * dataset that satisfy conditions defined in a group filter build based on
   * filterOptions, where filterOptions include (ACTIVEONLY if (activeOnly));
   * (NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter)). Return 0 on Error.
   * @return int
   */
    @SuppressWarnings("unchecked")
  public int count() {
    int result = 0;
    try {
      EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
      int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
              this.doEdits(), this.doPkFilter());
      this.addActiveOnlyFilter(groupFilter, filterOptions);
      this.addSystemItemFilter(groupFilter, filterOptions);
      this.addFKFilter(groupFilter, filterOptions);

      EntityContext<TBean> entCtx = this.getEntityContext();      
      CriteriaBuilder pCb = this.entMngr.getCriteriaBuilder();
      CriteriaQuery pCq = pCb.createQuery(this.entityClass);
      Root<TBean> pRoot = (Root<TBean>) pCq.from(this.entityClass);

      FieldInfo pkField = entCtx.getPrimaryKey();
      groupFilter.setFilter(pRoot, pCb, pCq);
      pCq.select(pCb.count(pRoot.get(pkField.entityPath.fieldName)));

      Query query = entMngr.createQuery(pCq);
      result = ((Long) query.getSingleResult()).intValue();
    } catch (Exception pExp) {
      result = 0;
      logger.log(Level.WARNING, "{0}.count Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }

  /**
   * This methods return the record count using the same standard filters as
   * used by count, after applying the additional EntityValueFilter defined by
   * (sFieldName,pValue).  Thus, returning the count for subset within the
   * default EnityView recordset. Throw exceptions is fieldName is undefined
   * or not found in the Entity. If pValue=null, it returns the cunt for records
   * where the field is undefined (null).
   * @param fieldName the filter field
   * @param fieldValue the value to filter on.
   * @return the number of records found
   * @throws Exception
   */
    public int countByField(String fieldName, Object fieldValue) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.countByField(fieldName, fieldValue, filterOptions);
  }

  /**
   * This methods return the record count using the same standard filters as
   * defined by filterOptions, after applying the additional EntityValueFilter defined
   *  by (sFieldName,pValue).  Thus, returning the count for subset within the
   * default EnityView recordset. Throw exceptions is fieldName is undefined
   * or not found in the Entity. If pValue=null, it returns the cunt for records
   * where the field is undefined (null).
   * @param fieldName the field to search on
   * @param fieldValue the Value to search for
   * @param filterOptions the FacadfilterOptionsEnums to apply
   * @return the number of records found
   * @throws Exception
   */
    @SuppressWarnings("unchecked")
  public int countByField(String fieldName, Object fieldValue, int filterOptions)
          throws Exception {
    if ((fieldName == null) || (fieldName.trim().equals(""))) {
      throw new Exception("FieldName cannot be undefined");
    }
    
    FieldInfo fldInfo = this.getFieldInfo(fieldName);
    if (fldInfo == null) {
      throw new Exception("Unable to locate Field[" + fieldName
              + "] in Entity[" + this.getEntityClassName() + "]");
    }
    
    if ((fldInfo.valueConverter != null) &&
            (fldInfo.valueConverter.isValidInput(fieldValue))) {
      fieldValue = fldInfo.valueConverter.toFieldValue(fieldValue);
    }

    EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
    IEntityFilter entityFilter = new EntityValueFilter<>(fieldName,
            fieldValue, EntityFilterEnums.EQUAL);
    if (entityFilter == null) {
      throw new Exception("Initiating a ValueFilter for Field[" + fieldName +
              "] failed.");
    }

    return countByFilter(entityFilter, filterOptions);
  }

  /**
   * This methods return the record count using the standard filters as used by
   * count after applying the additional filter defined by pFilter.  Thus,
   * returning the subset within the default EnityView recordset.
   * If pFilter == null, the result is the same as calling count().
   * @param pFilter the IEntityFilter<V> to apply
   * @return the number of records found
   * @throws Exception
   */
    public int countByFilter(IEntityFilter<TBean> entityFilter) throws Exception {
    int filterOptions = FacadeFilterEnums.setFilter(this.doActiveOnly(),
            this.doEdits(), this.doPkFilter());
    return this.countByFilter(entityFilter, filterOptions);
  }

  /**
   * This methods return the record count using the standard filters define by
   * filterOptions after applying the additional filter defined by pFilter.  Thus,
   * returning the subset within the default EnityView recordset.
   * If pFilter==null, the result is the count for records filters by filterOptions.
   * @param entityFilter the IEntityFilter to apply
   * @param bActiveOnly true if only active record are search for
   * @return the number of records found
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
    public int countByFilter(IEntityFilter<TBean> entityFilter, int filterOptions)
          throws Exception {
    int result = 0;
    EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
    if ((entityFilter != null) && (entityFilter.isSet())) {
      groupFilter.addFilter(entityFilter);
    }
    this.addActiveOnlyFilter(groupFilter, filterOptions);
    this.addSystemItemFilter(groupFilter, filterOptions);
    this.addFKFilter(groupFilter, filterOptions);

    CriteriaBuilder cb = this.entMngr.getCriteriaBuilder();
    CriteriaQuery cq = cb.createQuery();
    Root<TBean> rt = (Root<TBean>) cq.from(this.entityClass);

    groupFilter.setFilter(rt, cb, cq);

    cq.distinct(groupFilter.isDistinct());
    cq.select(cb.count(rt));

    Query query = entMngr.createQuery(cq);
    result = ((Long) query.getSingleResult()).intValue();
    return result;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Get Maximum Value of NumericField">
  /**
   * Get the Maximum Integer for Field[sFieldName] - no filter Applied
   * @param fieldName the field to search on
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
  public Integer getMaxInteger(String fieldName) throws Exception {
    Integer result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        result = this.getMax(Integer.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMaxInteger Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Maximum Integer for Field[sFieldName] - With a set filter (or null) and
   * the filterOptions (FacadeFilterEnums) set. Where filterOptions includes (ACTIVEONLY if
 (bActiveonly));(NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter))
   * @param fieldName the field to search on
   * @param entityFilter the IEntityFilter<V> to apply
   * @param filterOptions the FacadeFilterEnums to apply
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
  public Integer getMaxInteger(String fieldName, IEntityFilter<TBean> entityFilter, 
          int filterOptions) throws Exception {
    Integer result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
        if ((entityFilter != null) && (entityFilter.isSet())) {
          groupFilter.addFilter(entityFilter);
        }
        
        this.addActiveOnlyFilter(groupFilter, filterOptions);
        this.addSystemItemFilter(groupFilter, filterOptions);
        this.addFKFilter(groupFilter, filterOptions);
        
        result = this.getMax(Integer.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMaxInteger Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Maximum Long for Field[fieldName] - no filter Applied
   * @param fieldName the field to search on
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
  public Long getMaxLong(String fieldName) throws Exception {
    Long result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        result = this.getMax(Long.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMaxLong Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Maximum Double for Field[sFieldName] - no filter Applied
   * @param fieldName the field to search on
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
  public Double getMaxDouble(String fieldName) throws Exception {
    Double result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        result = this.getMax(Double.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMaxDouble Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Maximum Double for Field[sFieldName] - With a set filter (or null) and
   * the filterOptions (FacadeFilterEnums) set. Where filterOptions includes (ACTIVEONLY if
 (bActiveonly));(NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter))
   * @param fieldName the field to search on
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions the FacadeFilterEnums to apply
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
  public Double getMaxDouble(String fieldName, IEntityFilter<TBean> entityFilter, 
          int filterOptions) throws Exception {
    Double result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
        if ((entityFilter != null) && (entityFilter.isSet())) {
          groupFilter.addFilter(entityFilter);
        }
        
        this.addActiveOnlyFilter(groupFilter, filterOptions);
        this.addSystemItemFilter(groupFilter, filterOptions);
        this.addFKFilter(groupFilter, filterOptions);
        
        result = this.getMax(Double.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMaxDouble Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * The Private Method called to get the calculate Maximum Value for Field[sFieldName]
   * It will check if the field's Type matches Y
   * @param <Y> a Number class
   * @param beanClass the Number class
   * @param fieldName the field to search on
   * @param entityFilter the filter to apply
   * @return the maximum value or null if no records were found
   * @throws Exception
   */
    @SuppressWarnings({"unchecked", "unchecked"})
  public <Y extends Number> Y getMax(Class<Y> beanClass, String fieldName,
                                IEntityFilter<TBean> entityFilter) throws Exception {
    CriteriaBuilder pCb = this.entMngr.getCriteriaBuilder();
    CriteriaQuery pCq = pCb.createQuery(this.entityClass);
    Root<TBean> pRoot = (Root<TBean>) pCq.from(this.entityClass);
    
    EntityType<TBean> pEntType = pRoot.getModel();
    Attribute pAttr = pEntType.getAttribute(fieldName);
    if (!beanClass.equals(pAttr.getJavaType())) {
      throw new Exception("Field[" + fieldName +"].type["
              + pAttr.getJavaType().getSimpleName() + "] does not match the requested "
              + "result Type[" +beanClass.getSimpleName() + "].");
    }
    
    if (entityFilter != null) {
      entityFilter.setFilter(pRoot, pCb, pCq);
    }
    
    pCq.select(pCb.max(pRoot.get(fieldName).as(beanClass)));
    
    Query query = entMngr.createQuery(pCq);
    return ((Y) query.getSingleResult());
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Get Minimum Value of NumericField">
  /**
   * Get the Minimum Integer for Field[sFieldName] - no filter Applied
   * @param fieldName the field to search on
   * @return the minimum value or null if no records were found
   * @throws Exception
   */
  public Integer getMinInteger(String fieldName) throws Exception {
    Integer result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        result = this.getMin(Integer.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMinInteger Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Minimum Integer for Field[sFieldName] - With a set filter (or null) and
   * the filterOptions (FacadeFilterEnums) set. Where filterOptions includes (ACTIVEONLY if
 (bActiveonly));(NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter))
   * @param fieldName the field to search on
   * @param entityFilter the IEntityFilter to apply
   * @param filterOptions int (FacadeFilterEnums)
   * @return the minimum value or null if no records were found
   * @throws Exception
   */
  public Integer getMinInteger(String fieldName, IEntityFilter<TBean> entityFilter,
          int filterOptions) throws Exception {
    Integer result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
        if ((entityFilter != null) && (entityFilter.isSet())) {
          groupFilter.addFilter(entityFilter);
        }
        
        this.addActiveOnlyFilter(groupFilter, filterOptions);
        this.addSystemItemFilter(groupFilter, filterOptions);
        this.addFKFilter(groupFilter, filterOptions);
        
        result = this.getMin(Integer.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMinInteger Error:\n " + pExp.getMessage());
    }
    return result;
  }
  /**
   * Get the Minimum Double for Field[sFieldName] - no filter Applied
   * @param fieldName the field to search on
   * @return the minimum value or null if no records were found
   * @throws Exception
   */
  public Double getMinDouble(String fieldName) throws Exception {
    Double result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        result = this.getMin(Double.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMinDouble Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Minimum Double for Field[sFieldName] - With a set filter (or null) and
   * the filterOptions (FacadeFilterEnums) set. Where filterOptions includes (ACTIVEONLY if
 (bActiveonly));(NOTSYSTEM is (doEdit)); and  (FKFILTER if (doPkFilter))
   * @param fieldName the field to search on
   * @param entityFilter the IEntityFilter to Apply
   * @param filterOptions int (FacadeFilterEnums)
   * @return the minimum value or null if no records were found
   * @throws Exception
   */
  public Double getMinDouble(String fieldName, IEntityFilter<TBean> entityFilter, 
          int filterOptions) throws Exception {
    Double result = null;
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true);
        if ((entityFilter != null) && (entityFilter.isSet())) {
          groupFilter.addFilter(entityFilter);
        }
        
        this.addActiveOnlyFilter(groupFilter, filterOptions);
        this.addSystemItemFilter(groupFilter, filterOptions);
        this.addFKFilter(groupFilter, filterOptions);
        
        result = this.getMin(Double.class, fieldName, null);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getMinDouble Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * The Private Method called to get the calculate Maximum Value for Field[sFieldName]
   * It will check if the field's Type matches Y
   * @param <Y> extends Number
   * @param beanClass the Number class
   * @param fieldName the field to search on
   * @param entityFilter the EntityFilter to apply
   * @return the minimum value or null if no records were found
   * @throws Exception
   */
    @SuppressWarnings({"unchecked", "unchecked"})
  public <Y extends Number> Y getMin(Class<Y> beanClass, String fieldName,
                            IEntityFilter<TBean> entityFilter) throws Exception {
    CriteriaBuilder pCb = this.entMngr.getCriteriaBuilder();
    CriteriaQuery pCq = pCb.createQuery(this.entityClass);
    Root<TBean> pRoot = (Root<TBean>) pCq.from(this.entityClass);
    
    EntityType<TBean> entityType = pRoot.getModel();
    Attribute attribute = entityType.getAttribute(fieldName);
    if (!beanClass.equals(attribute.getJavaType())) {
      throw new Exception("Field[" + fieldName +"].type["
              + attribute.getJavaType().getSimpleName() 
              + "] does not match the requested "
              + "result Type[" +beanClass.getSimpleName() + "].");
    }
    
    if (entityFilter != null) {
      entityFilter.setFilter(pRoot, pCb, pCq);
    }
    
    pCq.select(pCb.min(pRoot.get(fieldName).as(beanClass)));
    
    Query query = entMngr.createQuery(pCq);
    return ((Y) query.getSingleResult());
  }
//</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Custom Query">
  /**
   * Called to execute a data load query with a custom defined SQL <tt>querySql</tt>.
   * The assigned <tt>delegate</tt> should be designed to process the query's returned
   * result set. 
   * <p>
   * <b>NOTE:</b> If {@linkplain #_DEBUG_ON} = true, all query or data processing errors
   * will be trapped and logged. Otherwise, all errors are ignored</p>
   * @param querySql the custom SQL string
   * @param delegate the delegate to set the query parameters and process the queries 
   * result set.
   */
    public void executeQuery(String querySql, final SqlQueryDelegate delegate) {
    if (delegate == null) {
      throw new NullPointerException("The SqlQueryDelegate is not defined");
    }
    
    final String sql = DataEntry.cleanString(querySql);
    if (sql == null) {
      throw new NullPointerException("The query's SQL statement cannot be empty.");
    }
    
    if (this.entMngr == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    
    Query qry = this.entMngr.createQuery(sql);
    if (qry == null) {
      throw new IllegalArgumentException("Unable to initiate Query[" + sql + "].");
    }
    
    try {      
      /* Assign the Query Parameters */
      delegate.assignParameters(qry);
      
      /* Execute the Query */
      List<Object> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from Query is empty. "
                + "Query SQL = " + qry.toString());
      }
      
      delegate.loadQuery(qryResult);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.excuteQuery Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    
//    Session session = this.entMngr.unwrap(Session.class);
//    if (session != null) {
//      session.doWork(new Work() {
//        @Override
//        public void execute(Connection conn) throws SQLException {
//          try (java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
//              delegate.loadQuery(rs);
//            } catch (Exception inExp) {
//              if (_DEBUG_ON) {
//                logger.log(Level.WARNING, "{0}.delegate.executeQuery Error:\n {1}",
//                      new Object[]{this.getClass().getSimpleName(), inExp.getMessage()});            
//              }
//            }            
//          } catch (Exception exp) {
//            if (_DEBUG_ON) {
//              logger.log(Level.WARNING, "{0}.executeQuery Error:\n {1}",
//                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
//            }
//          } 
//        }
//      });    
//    }
  }
  
  /**
   * Called to execute a data load query with a custom defined SQL <tt>querySql</tt>.
   * The assigned <tt>delegate</tt> should be designed to process the query's returned
   * result set. 
   * <p>
   * <b>NOTE:</b> All query or data processing errors will be trapped and logged.</p>
   * @param namedQuery the NamedQuery defined as an attribute to the Entity.
   * @param delegate the delegate to set the query parameters and process the queries 
   * result set.
   */
    public <TResult> void executeQuery(String namedQuery, 
                                          final NamedQueryDelegate<TResult> delegate) {
    if (delegate == null) {
      throw new NullPointerException("The NamedQueryDelegate is not defined");
    }
    
    if ((namedQuery = DataEntry.cleanString(namedQuery)) == null) {
      throw new NullPointerException("The NamedQuery's name cannot be undefined");
    }
    
    if (this.entMngr == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    
    Query qry = this.entMngr.createNamedQuery(namedQuery);
    if (qry == null) {
      throw new IllegalArgumentException("Unable to access NamedQuery[" 
                                                                    + namedQuery + "].");
    }
    
    try {
      /* Assign the Query Parameters */
      delegate.assignParameters(qry);
      /* Execute the Query */
      List<TResult> qryResult = qry.getResultList();
      if (qryResult == null) {
        throw new Exception("The result list from NamedQuery[" + namedQuery 
                + "] = null. Query SQL = " + qry.toString());
      }
      
      delegate.loadQuery(qryResult);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.excuteQuery Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Entity/Input Validations">
  /**
   * Check the underlying table for duplicate values pValue in sField ignoring
   * records with Field[sRecIDField]=pRecID.  Return true if pValue = false.
   * It search for record based on the filter defined by fieldName, pValue, and
   * filterOptions (see findAllByField for more details).   
   * @param fieldName the Field to search on
   * @param fieldValue the new value
   * @param recordId the primary key ID of the current record
   * @param filterOptions the FacfilterOptionsEnums to apply
   * @return true if the pValue is unique or the current record value
   * @throws Exception
   */
  public boolean isUnique(String fieldName, Object fieldValue, Object recordId,
          int filterOptions) throws Exception {
    boolean result = true;
    if (fieldValue == null) {
      throw new Exception("The field value is unassigned");
    }

    EntityContext<TBean> entCtx = this.getEntityContext();      
    FieldInfo pkField = entCtx.getPrimaryKey();
    List<TBean> resultList = this.findAllByField(fieldName, fieldValue, filterOptions);
    if (!resultList.isEmpty()) {
      if (recordId == null) {
        result = false;
      } else {
        for (TBean bean : resultList) {
          Object objId = ReflectionInfo.getFieldValue(bean, pkField);
          if (!recordId.equals(objId)) {
            result = false;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Check the underlying table for duplicate values pValue in sField with the same 
   * Parent (as defined by sFkField and pFkId)ignoring records with 
   * Field[sRecIDField]=pRecID.  Return true if pValue is unique.
   * It search for record based on the filter defined by fieldName, pValue, and
   * filterOptions (see findAllByField for more details).   
   * @param sField the Field to search on
   * @param fieldValue the new value
   * @param fkField the parent ForeignKey field
   * @param fkId the Parent's PrimaryKey ID
   * @param recId the primary key ID of the current record
   * @param filterOptions the FacfilterOptionsEnums to apply
   * @return true if the pValue is unique or the current record value
   * @throws Exception
   */
  public boolean isUniqueInParent(String fieldName, Object fieldValue, String fkField, 
          Object fkId, Object recId, int filterOptions) throws Exception {
    boolean result = true;
    if (fieldValue == null) {
      throw new Exception("The field value is unassigned");
    }

    fkField = DataEntry.cleanString(fkField);
    boolean doParent = ((fkField != null) && (fkId != null));
    EntityContext<TBean> entCtx = this.getEntityContext();      
    FieldInfo pkField = entCtx.getPrimaryKey();
    List<TBean> resultList = this.findAllByField(fieldName, fieldValue, filterOptions);
    if (!resultList.isEmpty()) {
      if (recId == null) {
        if (!doParent) {
          result = false;
        } else {
          for (TBean bean : resultList) {
            Object parentId = ReflectionInfo.getFieldValue(bean, fkField);
            if ((parentId != null) && (parentId.equals(fkId))) {
              result = false;
              break;
            }
          }
        }
      } else {
        for (TBean bean : resultList) {
          Object objId = ReflectionInfo.getFieldValue(bean, pkField);
          if (recId.equals(objId)) {
            continue;
          }
          if (doParent) {
            Object parentId = ReflectionInfo.getFieldValue(bean,fkField);
            if ((parentId != null) && (parentId.equals(fkId))) {
              result = false;
              break;
            }
          } else {
            result = false;
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Get whether the Entity[pRecID]'s Field[msSystemItemFld] is set to true.
   * Return false if msSystemItemFld is undefined or is not supported in the
   * EnityClass and if this.isInitiating().
   * Throw an exception if the record cannot be located.
   * @param pRecID Object
   * @return boolean
   * @throws Exception
   */
  public boolean isSystemItem(Object recordId) throws Exception {
    boolean bIsItem = false;
    EntityContext<TBean> entCtx = this.getEntityContext();      
    FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.SYSTEM_ITEM);
    if ((!this.isInitiating()) && (fieldInfo != null) && (recordId != null)) {
      TBean pRecord = this.find(recordId);
      if (pRecord == null) {
        throw new Exception("Unable to locate Enity[" + recordId.toString() + "]");
      }

      Object pIsSysItem = ReflectionInfo.getFieldValue(pRecord, fieldInfo);
      bIsItem = ((pIsSysItem != null) && ((Boolean) pIsSysItem));
    }
    return bIsItem;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Manage Filters">
  /**
   * Called to get the ActiveOnlyFilter (called by addActiveOnlyFilter). If the filter 
   * is not assigned, initiate the default filter (this.EntityContext.DisabledField=false).
   * @return the currently assigned ActiveOnlyFilter (or null if not supported)
   * @throws Exception 
   */
  private IEntityFilter<TBean> getActiveOnlyFilter() throws Exception {
    if (this.activeOnlyFilter == null) {
      EntityContext<TBean> entCtx = this.getEntityContext();      
      FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.DISABLED);
      if (fieldInfo != null) {
        EntityBoolFilter<TBean> entityFilter = 
                          new EntityBoolFilter<>(fieldInfo.entityPath.fieldName, false, 
                                                 EntityFilterEnums.EQUAL);
        this.activeOnlyFilter = entityFilter;
      }
    }
    return this.activeOnlyFilter;
  }
  
  /**
   * Called by Facades to assign (or reset) a custom ActiveOnly filter for TBean
   * @param pFilter a Custom EntityFilter or null to reset the current filter and use
   * the default value
   */
  public void setActiveOnlyFilter(IEntityFilter<TBean> entityFilter) {
    this.activeOnlyFilter = entityFilter;
  }
  
  /**
   * This method will add the filter (msDisabledField=false) to the group filter
   * if: 1) groupFilter != null, 2) doActive=true, and 3) filterOptions includes
   * ENABLEDONLY.  Otherwise no filter will be added.
   * @param groupFilter EntityGroupFilter<TBean>
   * @param filterOptions FacadeFilterOptions
   */
  @SuppressWarnings("unchecked")
  private void addActiveOnlyFilter(EntityGroupFilter<TBean> groupFilter,
          int filterOptions) throws Exception {
    boolean addFilter = ((filterOptions & FacadeFilterEnums.ENABLEDONLY) != 0);
    if ((groupFilter != null) && (addFilter) && (this.doActive())) {
      IEntityFilter<TBean> entityFilter = this.getActiveOnlyFilter();
      if (entityFilter != null) {
        groupFilter.addFilter(entityFilter);
      }
    }
  }

  /**
   * This method will add the filter (msSystemItem=false)  to the group filter 
   * if: 1) groupFilter != null, 2) doIsSystem=true, and 3) filterOptions includes 
   * NOTSYSTEM.  Otherwise no filter will be added. (&& (bDoEdit)_
   * @param groupFilter EntityGroupFilter
   * @param filterOptions FacadeFilterOptions
   */
  @SuppressWarnings("unchecked")
  private void addSystemItemFilter(EntityGroupFilter groupFilter,
          int filterOptions) throws Exception {
    boolean addFilter = ((filterOptions & FacadeFilterEnums.NOTSYSTEM) != 0);
    if ((groupFilter != null) && (this.doIsSystem()) && (addFilter)) {
      EntityContext<TBean> entCtx = this.getEntityContext();      
      FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.SYSTEM_ITEM);
      EntityBoolFilter<TBean> entityFilter =
        new EntityBoolFilter<>(fieldInfo.entityPath.fieldName, EntityFilterEnums.EQUAL);
      if (entityFilter != null) {
        entityFilter.setValue(false);
        groupFilter.addFilter(entityFilter);
      }
    }
  }

  /**
   * This method will add the filters to the group filter based on the assigned
   * Foreign Keys if: 1) groupFilter != null, 2) hasForeignKeys=true, and
   * 3) filterOptions includes NOTSYSTEM.  Otherwise no filter will be added.
   * @param groupFilter EntityGroupFilter && (this.doPkFilter())
   * @param filterOptions FacadeFilterOptions
   */
  @SuppressWarnings("unchecked")
  private void addFKFilter(EntityGroupFilter<TBean> groupFilter,
          int filterOptions) throws Exception {
    boolean addFilter = ((filterOptions & FacadeFilterEnums.FKFILTER) != 0);
    List<ForeignKey> parentFkList = this.getParentForeignKeys();
    if ((groupFilter == null) || (!addFilter) || (parentFkList == null)
            || (parentFkList.isEmpty()) || (this.puManager == null)) {
      return;
    }

    for (ForeignKey parentFk : parentFkList) {
      Class<? extends Serializable> parentClass = parentFk.associationPath.parentClass;
      if (parentClass == null) {
        continue;
      }

      String childField = parentFk.childAssociation.targetPath.fieldName;
      EntityWrapper parentWrapper = this.puManager.getPkFilter(parentClass);
      Serializable parentBean = 
                              (parentWrapper == null)? null: parentWrapper.getEntity();
      if ((parentBean == null) || (this.getFieldInfo(childField) == null)) {
        continue;
      }
      
      if (!parentFk.childAssociation.isTargetReturnType(parentClass)) {
        String parentField = parentFk.parentAssociation.targetPath.fieldName;
        Object parentValue = ReflectionInfo.getFieldValue(parentBean, parentField); 
        EntityValueFilter<TBean, Object> pFilter =
             new EntityValueFilter<>(childField, parentValue, EntityFilterEnums.EQUAL);
        if (pFilter != null) {
          groupFilter.addFilter(pFilter);
        }
     } else {
        EntityValueFilter<TBean, Serializable> pFilter =
             new EntityValueFilter<>(childField, parentBean, EntityFilterEnums.EQUAL);
        if (pFilter != null) {
          groupFilter.addFilter(pFilter);
        }
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Entity Class Queries">
  /**
   * This method checks whether a Collection field for sFKClassName and it
   * associated Method exist (i.e., both its hasField and hasMethod return true)
   * This method calls getChildCollectionField to get the field for the class
   * and then call getFieldExists to verify the the field and its get Method
   * exists.
   * @param fkClassName String
   * @return true if the collection is supported
   */
  private boolean getCollectionExists(String fkClassName) {
    String sField = 
                 ReflectionInfo.getChildCollectionField(this.entityClass, fkClassName);
    return (sField != null);
  }
  /**
   * Overload 1: Get the FieldInfo for the specified <tt>fieldName</tt>. 
   * @param fieldName the field's name
   * @return {@linkplain #getFieldInfo(java.lang.String, boolean) 
   * this.getFieldInfo(fieldName, false)}
   */
  private FieldInfo getFieldInfo(String fieldName) {
    return this.getFieldInfo(fieldName, false);
  }
  
  /**
   * Overload 1: Get the FieldInfo for the specified <tt>fieldName</tt>. It retrieved 
   * the fieldInfo from this.entityContent and return the FieldInfo depending on 
   * whether the fieldInfo is found, the field is a delegate field and 
   * allowDelegate = true|false.
   * not a delegate field.
   * @param fieldName the field's name
   * @return the fieldInfo or null if not found or (allowDelegate = true) and 
   * {@linkplain FieldInfo#isDelegate() fieldInfo.isDelegate} = true.
   */
  private FieldInfo getFieldInfo(String fieldName, boolean allowDelegate) {
    EntityContext ctx = this.getEntityContext();
    FieldInfo result = ctx.getFieldInfo(fieldName);
    if ((result != null) && (!allowDelegate) && (result.isDelegate())) {
      result = null;
    }
    return  result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Facade Settings/Flags">
  /**
   * Start Entity Initiation bey setting the Initiation Mode = true (which override the 
   * AllowRead, AllowEdit, AllowDelete, or IsSystem states. if already initiated, it
   * increment the miInitCount.  
   * NOTE: Every beginInit() MUST be followed by an endInit().
   */
  public void beginInit() {
    this.initCount = (this.initCount <= 0)? 1: this.initCount+1;
  }

  /**
   * Return true is the Entity Initiation Mode is active
   * @return
   */
  public boolean isInitiating() {
    return (this.initCount > 0);
  }

  /**
   * Decrement the miInitCount. The Entity Initiation Mode is turned off when the count
   * reach zero.
   */
  public void endInit() {
    if (this.initCount > 0) {
      this.initCount--;
    }
  }

  /**
   * Return true if the msRecordNameField is set and the Entity contains a field
   * by that name.
   */
  public Boolean doRecordName() {
    EntityContext<TBean> entCtx = this.getEntityContext();
    return entCtx.hasAliasField(BwFieldAlias.RECORD_NAME);
  }

  /**
   * Get bean's recordId if the EntityContext.idField != null and it the Bean
   * class contains the field.
   * @param bean the bean instance
   * @return the assigned record Id
   */
  public Object getBeanRecordId(TBean bean) {
    Object result = null;
    String className = this.getEntityClassName();
    try {
      if (bean != null) {
        EntityContext<TBean> entCtx = this.getEntityContext();
        FieldInfo fieldInfo = null;
        if ((entCtx != null) && ((fieldInfo = entCtx.getPrimaryKey()) != null)) {
          result = ReflectionInfo.getFieldValue(bean, fieldInfo);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}[{1}].getBeanRecordId Error:\n {2}",
          new Object[]{this.getClass().getSimpleName(), className, pExp.getMessage()});
    }
    return result;
  }

  /**
   * Get pBean's RecordName if the EntityContext.recordNameField != null and it the Bean
   * Class contains the field.
   * @param bean V
   * @return String
   */
  public String getBeanRecordName(TBean bean) {
    String result = null;
    String className = this.getEntityClassName();
    try {
      if (bean != null) {
        EntityContext<TBean> entCtx = this.getEntityContext();
        FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.RECORD_NAME);
        if (fieldInfo != null) {
          result = (String) ReflectionInfo.getFieldValue(bean, fieldInfo);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}[{1}].getBeanRecordName Error:\n {2}",
          new Object[]{this.getClass().getSimpleName(), className, pExp.getMessage()});
    }
    return result;
  }

  /**
   * Return true if this.EntityContext.DisabledField is set and the Entity contains a field
   * by that name or a custom this.activeOnlyFilter is assigned.
   */
  public Boolean doActive() {
    EntityContext<TBean> entCtx = this.getEntityContext();
    return ((entCtx.hasAliasField(BwFieldAlias.DISABLED)) || 
            (this.activeOnlyFilter != null));
  }

  /**
   * Get pBean's Disabled State if the EntityContext.disabledField != null and it the Bean
   * Class contains the field. - return false if undefined or not supported.
   * @param bean V
   * @return Boolean
   */
  public Boolean getBeanDisabled(TBean bean) {
    Boolean result = null;
    String sClass = this.getEntityClassName();
    try {
      if (bean != null) {
        EntityContext entCtx = this.getEntityContext();
        FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.DISABLED);
        if (fieldInfo != null) {
          result = (Boolean) ReflectionInfo.getFieldValue(bean, fieldInfo);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}[{1}].getBeanDisabled Error:\n {2}", 
              new Object[]{this.getClass().getSimpleName(), sClass, pExp.getMessage()});
    }
    return ((result != null) && (result));
  }

  /**
   * Return true if the msDisplayIdxField is set and the Entity contains a field
   * by that name.
   */
  public Boolean doDisplayIdx() {
    EntityContext entCtx = this.getEntityContext();
    return (entCtx.hasAliasField(BwFieldAlias.DISPLAY_IDX));
  }

  /**
   * Get pBean's DisplayIdx if the EntityContext.displayIdxField != null and it the Bean
   * Class contains the field. - return 0 if undefined or not supported.
   * @param bean Object
   * @return Integer
   */
  public Integer getBeanDisplayIdx(TBean bean) {
    Integer result = null;
    String sClass = this.getEntityClassName();
    try {
      if (bean != null) {
        EntityContext entCtx = this.getEntityContext();
        FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.DISPLAY_IDX);
        if (fieldInfo != null) {
          result = (Integer) ReflectionInfo.getFieldValue(bean, fieldInfo);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}[{1}].getBeanDisplayIdx Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), sClass, pExp.getMessage()});
    }
    return (result == null)? 0: result;
  }

  /**
   * Return true if the msSystemItemField is set and the Entity contains a field.
   * Return false if this.isInitiating()=true, regardless of whether the Entity supports
   * the SystemItem Field.
   * by that name.
   */
  public Boolean doIsSystem() {
    EntityContext entCtx = this.getEntityContext();
    return ((!this.isInitiating()) && (entCtx.hasAliasField(BwFieldAlias.SYSTEM_ITEM)));
  }

  /**
   * Get pBean's IsSystem State if the EntityContext.displayIdxField != null and it the 
   * Bean Class contains the field. - return false if undefined or not supported.
   * @param bean Object
   * @return Boolean
   */
  public Boolean getBeanIsSystemItem(TBean bean) {
    Boolean result = null;
    String className = this.getEntityClassName();
    try {
      if (bean != null) {
        EntityContext entCtx = this.getEntityContext();
        FieldInfo fieldInfo = entCtx.getAliasField(BwFieldAlias.SYSTEM_ITEM);
        if (fieldInfo != null) {
          result = (Boolean) ReflectionInfo.getFieldValue(bean, fieldInfo);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}[{1}].getBeanIsSystemItem Error:\n {2}", 
           new Object[]{this.getClass().getSimpleName(), className, pExp.getMessage()});
    }
    return ((result != null) && (result));
  }

  /**
   * Get whether the ActiveOnly Filter flag is set
   * @return Boolean
   */
  public Boolean doActiveOnly() {
    return this.activeOnly;
  }

  /**
   * Set the ActiveOnly Filter flag
   * @param activeOnly Boolean
   */
  public void setActiveOnly(Boolean activeOnly) {
    this.activeOnly = ((activeOnly != null) && (activeOnly));
  }

  /**
   * Get the DoEdits flag setting
   * @return
   */
  public Boolean doEdits() {
    return this.doEdits;
  }

  /**
   * Set the DoEdits flag.
   * @param doEdits
   */
  public void setDoEdits(Boolean doEdits) {
    this.doEdits = ((doEdits != null) && (doEdits));
  }

  /**
   * Get whether hasForeignKeys and the DoFkFilter flag is set.
   * @return true if set
   */
  public boolean doPkFilter() {
    return (this.doPkFilter);
  }

  /**
   * Set the DoFilter flag - which if set will add the entity's PrimaryKey if
   * the Entity has a foreign key and it is set.
   * @param doPkFilter true to set - ignored if !this.hasParentForeignKeys.
   */
  public void setPkFilter(Boolean doPkFilter) {
    doPkFilter = ((doPkFilter != null) && (doPkFilter))? 
                                            (this.hasParentForeignKeys()) : doPkFilter;
    this.doPkFilter = doPkFilter;
  }
  // </editor-fold>
 
  // <editor-fold defaultstate="collapsed" desc="Sort methods">
  /**
   * Get whether sorting is in ascending (true) or descending (false) order
   * @return Boolean
   */
  public Boolean getSortAsc() {
    return ((this.entitySort == null) || (this.entitySort.getSortAsc()));
  }
  
  /**
   * Get whether sorting is in ascending (true) or descending (false) order
   * @return Boolean
   */
  public Boolean getSortAsc(String fieldName) {
    Boolean result = false;
    if (this.entitySort != null) {
      if (this.entitySort.isField(fieldName)) {
        result = this.entitySort.getSortAsc();
      } else {
        result = this.entitySort.getSortAsc(fieldName);
      }
    }
    return result;
  }

  /**
   * Set the Sort Order of the first field in this.entitySort
   * @param sortAsc the sort order: Ascending=true, Descending=False
   */
  public void setSortAsc(Boolean sortAsc) {
    if (this.entitySort != null) {
      this.entitySort.setSortAsc(sortAsc);
    }
  }
    
  /**
   * Set the Sort Order of a specific field in the this.entitySort.
   * @param fieldName the fieldName which sort order must be updated.
   * @param sortAsc the sort order: Ascending=true, Descending=False
   */
  public void setSortAsc(String fieldName, Boolean sortAsc) {
    if (this.entitySort != null) {
      this.entitySort.setSortAsc(fieldName, sortAsc);
    }
  }

  /**
   * Get the currently set Sort Field name
   * @return String
   */
  public String getSortField() {
    return ((this.entitySort == null)) ? "" : this.entitySort.getSortIdField();
  }

  /**
   * Set the SortField name. If the entitySort does not exist or does not include the 
   * specified field, the field will be appended to the sort order list. Otherwise, 
   * the field's sort order will be reversed. Ignored if sortField = ""|null or if 
   * {@linkplain #getFieldInfo(java.lang.String, boolean) 
   * this.getFieldInfo(sortField, true)} !=  null.
   * @param sortField the field to sort on.
   */
  public void setSortField(String sortField) {
    sortField = DataEntry.cleanString(sortField);
    if ((sortField == null) || (this.getFieldInfo(sortField, true) != null)) {
      return;
    }
    
    if (this.entitySort == null) {
      this.entitySort = new EntitySort(sortField, true);
    } else if (this.entitySort.hasField(sortField)) {
      this.entitySort.reverseSortOrder(sortField);
    } else {
      this.entitySort.addSubSort(sortField, true);
    }
  }

  /**
   * Add the Sort Field and set its initial sort order. If a prior sort field has been,
   * the field will been appended to the sort order list. Otherwise, it will become the
   * first sort field. Ignored if sortField = ""|null or if {@linkplain 
   * #getFieldInfo(java.lang.String, boolean) this.getFieldInfo(sortField, true)} != 
   * null.
   * @param sortField the field to sort on.
   * @param sortAsc the sort order: Ascending=true; Descending=false.
   */
  public void addSortField(String sortField, boolean sortAsc) {
    sortField = DataEntry.cleanString(sortField);
    if ((sortField != null) && (this.getFieldInfo(sortField, true) != null)) {
      return;
    }
    
    if (this.entitySort == null) {
      this.entitySort = new EntitySort(sortField, sortAsc);
    } else {
      this.entitySort.addSubSort(sortField, sortAsc);
    }
  }
  
  /**
   * Set the Sort field using a EntitySort. It replaced any prior assignments
   * @param entsort the new EntitySort (can be null to clear settings)
   */
  public void setEntitySort(EntitySort entsort) {
    this.entitySort = entsort;
  }
  
  /**
   * Get the currently assigned Entity Sort
   * @return the current EnitySort
   */
  public EntitySort getEntitySort() {
    return this.entitySort;
  }
  
  /**
   * Clear all sort settings
   */
  public void clearEntitySort() {
    this.entitySort = null;
  }
  
  /**
   * Clear the Entity Sort of a specified field.
   * @param fieldName the field which sort setting must be cleared
   */
  public void clearEntitySort(String fieldName) {
    if (this.entitySort != null) {
      if (this.entitySort.isField(fieldName)) {
        this.entitySort = null;
      } else {
        this.entitySort.removeSubSort(fieldName);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Parent-Child Methods">
  /**
   * <p>This method is called by a EntityWrapper's {@linkplain EntityWrapper#submitNew()
   * submitNew} method to assign the ForeignKeys Parent's to the bean foreignKey fields
   * (as defined in the ForeignKeyConstraint) and to update the bean's in-memory 
   * ForeignKey parent's ChildCollections. Nothing happens if the Child has no assigned 
   * ForeignKey parents or the ForeignKeys are not set (i.e., no assigned 
   * parent PrimaryKeys).</p>
   * @param childWrapper the EntityWrapper child that will be updated.
   */
  @SuppressWarnings("unchecked")
  public void assignToFkParents(EntityWrapper<TBean> childWrapper) {
    try {
      List<ForeignKey> parentFkList = this.getParentForeignKeys();
      TBean childBean = (childWrapper == null)? null: childWrapper.getEntity();
      if ((this.puManager == null) || (childBean == null) || (parentFkList == null)
              || (parentFkList.isEmpty())) {
        return;
      }

      /*String sFKClassName = (pBean == null) ? null : 
       *           pBeForeignKeytSimpleName(); */
      for (ForeignKey parentFk : parentFkList) {
        EntityWrapper parentWrapper = null;
        Serializable parentBean = null;
        Class<? extends Serializable> parentClass = 
                                          parentFk.associationPath.parentClass;
        if ((parentClass == null) ||
               ((parentWrapper = this.puManager.getPkFilter(parentClass)) == null) ||
                ((parentBean = parentWrapper.getEntity()) == null)) {
          continue;
        }

        /* If the FKField is currently unassigned - assign the parent reference */
        Object parentValue = null;
        boolean isParentReturnType = 
                              parentFk.childAssociation.isTargetReturnType(parentClass);
        if (!isParentReturnType) {
          String parentField = parentFk.parentAssociation.targetPath.fieldName;
          parentValue = ReflectionInfo.getFieldValue(parentBean, parentField);
        } else {
          parentValue = parentBean;
        }

        if (parentValue == null) {
          continue;
        }

        String childField = parentFk.childAssociation.targetPath.fieldName;
        Object curValue = ReflectionInfo.getFieldValue(childBean, childField);
        if (!DataEntry.isEq(curValue, parentValue)) {
            ReflectionInfo.setFieldValue(childBean, childField, parentValue);

          /* Call addToFkParent only if pParent is an assigent parent and 
           * (!pKey.isIdReference) to asure that pChild is in the Child Collection */
          if (isParentReturnType) {
            if (this.addToFkParent(parentBean, childBean)) {
              ChildChangedEventArgs args = 
                            new ChildChangedEventArgs(childWrapper.getRecordId(), 
                            childBean.getClass());
              parentWrapper.fireAddChild(args);
            }
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.SEVERE, "assignToFkParents Error:\n{0}", pExp.getMessage());
    }
  }

  /**
   * <p>Called to add a childBean to its parentBean's child Collection.
   * This if called in two cases:</p><ul>
   *  <li><b>EntityWrapper.isNew when Saved:</b> - the {@linkplain 
   *    EntityWrapper#submitNew() submitNew} calls the {@linkplain 
   *    #assignToFkParents(bubblewrap.entity.core.EntityWrapper) 
   *    assignToFkParents} method to to assign any foreignKey parents to the 
   *    child and then call this method to update the parent after child.parent was 
   *    assigned.</li>
   *  <li><b>(not EntityWrapper.isNew) when changing the parent:</b> - This method is 
   *    called after the after childBean.parent has been updated and the child has been 
   *    saved to update its ForeignKey parents (which is in memory).</li>
   * </ul>
   * <p>It updates parentBean's child collection by locating the parent entity, its 
   *  child collection, and if the chidlBean is not in the list, add it to the list.
   * </p>
   * <p><b>NOTES:</b></p><ol>
   *  <li>It does not save the changes to parent, because the changes are committed
   *    when the child is saved. It is only intend to update the in-memory parent that
   *    are assigned as the child's foreignKey parent.</li>
   *  <li>It should not be called if the foreignKey is an IdReference (i.e, it is not
   *    a foreign key maintained in the database.</li></ol> 
   * @param parentBean the Entity Bean parent that need to be updated
   * @param childBean the EntityWrapper child to add to the parent
   * @return true if the parent's collection was updated
   */
  @SuppressWarnings("unchecked")
  private <TParent extends Serializable> boolean addToFkParent(TParent parentBean,
                                                                    TBean childBean) {
    boolean result = false;
    try {
      if ((parentBean == null) || (childBean == null)) {
        return result;
      }

      Class<TBean> childClass = (Class<TBean>) childBean.getClass();
      Collection childCol = 
                          ReflectionInfo.getChildCollection(parentBean, childClass);
      if ((childCol != null) && (!childCol.contains(childBean))) {
         childCol.add(childBean);
         ReflectionInfo.setChildCollection(parentBean, childClass, childCol);
         result = true;
      }      
    } catch (Exception pExp) {
      logger.log(Level.SEVERE, "addToFkParent Error:\n{0}", pExp.getMessage());
    }
    return result;
  }

  /**
   * <p>Called by the {@linkplain EntityWrapper#removeRecord() removeRecord} to remove 
   * the child from all its parents defined via a set ForeignKey with an assigned 
   * Parent. Ignored if the pChild=null or the Child has no assigned Parent.</p>
   * <p><b>NOTE:</b> It does not save the changes to parent, because the changes are 
   * committed when the child is saved. It is only intend to update the in-memory parent
   * that are assigned as the child's foreignKey parent.</p>
   * @param childWrapper EntityWrapper
   */
  @SuppressWarnings("unchecked")
  public void removeFromFkParents(EntityWrapper<TBean> childWrapper) {
    try {
      List<ForeignKey> parentFkList = this.getParentForeignKeys();
      TBean childBean = (childWrapper == null)? null: childWrapper.getEntity();
      if ((this.puManager == null) || (childBean == null) || (parentFkList == null)
              || (parentFkList.isEmpty())) {
        return;
      }

      Object childId = childWrapper.getRecordId();
      Class<TBean> childClass = this.getEntityClass();
      for (ForeignKey parentFk : parentFkList) {
        Class<? extends Serializable> parentClass = 
                                              parentFk.associationPath.parentClass;
        EntityWrapper parentWrapper = this.puManager.getPkFilter(parentClass);
        Serializable parentBean = 
                              (parentWrapper == null)? null: parentWrapper.getEntity();
        if ((parentWrapper == null) || (parentBean == null)) {
          continue;
        }
        
        Collection<TBean> childCol = 
                        ReflectionInfo.getChildCollection(parentBean, childClass);
        if ((childCol != null) && (childCol.contains(childBean))) {
          childCol.remove(childBean);
          ReflectionInfo.setChildCollection(parentBean, childClass, childCol);
          
          ChildChangedEventArgs args = new ChildChangedEventArgs(childId, childClass);
          parentWrapper.fireDeleteChild(args);
        }
      }

    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.removeFromFkParents Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Called to sync a parent's collection of foreignKey children (of type TBean) by 
   * refreshing each entity and replacing it in the parent collection. It calls
   * {@linkplain #syncParentChildEntity(myapp.core.EntityWrapper, 
   * myapp.core.EntityWrapper) this.syncParentChildEntity(parent,null)}
   * @param <TParent> the parent Bean class
   * @param parent the parent to update
   */
  public <TParent extends Serializable> void 
                                syncParentChildEntities(EntityWrapper<TParent> parent) { 
    this.syncParentChildEntity(parent, null);
  }
  
  /**
   * Called to sync a parent's collection of foreignKey children (of type TBean) by 
   * refreshing each entity and replacing it in the parent collection. If <tt>child</tt>
   * if defined, it will use the child's entity instead of a new refreshed entity.
   * <p>
   * <b>NOTE:</b> The Method must be called to the child's EntityFacade</p>
   * @param <TParent> the parent Bean class
   * @param parent the parent EntityWrapper instance containing a collection of child 
   * entity class
   * @param child the child EntityWrapper containing the updated-refreshed child 
   * entity that has to be synchronized with the parent's list - can be null.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public <TParent extends Serializable> void 
        syncParentChildEntity(EntityWrapper<TParent> parent, EntityWrapper<TBean> child) { 
    try {
      TParent parentBean = null;
      if ((parent == null) || ((parentBean = parent.getEntity()) == null)) {
        return;
      }
      
      String fkClassName = this.getEntityClassName();
      Class<TBean> childClass = this.getEntityClass();
      if (ReflectionInfo.getChildCollectionField(parentBean.getClass(), 
                                                                fkClassName) != null) {
        TBean childBean = (child == null)? null: child.getEntity();          
        Collection<TBean> childCol = 
                             ReflectionInfo.getChildCollection(parentBean, childClass);
        if ((childCol == null) || (childCol.isEmpty())) {
          Collection<TBean> newChildCol = new ArrayList<>();
          newChildCol.add(childBean);
          childCol = newChildCol;
        } else {
          List<TBean> beanList = new ArrayList<>(childCol);
          for (TBean bean : beanList) {
            TBean newBean = null;
            if (childBean.equals(bean)) {
              newBean = childBean;
            } else {
              newBean = this.refresh(bean);
            }
            
            if ((newBean != null) && (childCol.remove(bean))) {
              childCol.add(childBean);
            }
          }
        }
        ReflectionInfo.setChildCollection(parentBean, childClass, childCol);
        parent.submitEdits();
        parent.syncCachedChildCollection(childClass, childCol, childBean);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.syncParentChildEntity Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
        
  /**
   * Validate the the EnityClass support a one-to-many relationship with
   * child class sFKClassName. Throw an exception if not valid.
   * @param childClassName String
   * @throws Exception
   */
  public void validateChildFKField(String childClassName) throws Exception {
    if (!this.getCollectionExists(childClassName)) {
      throw new Exception("Enity[" + this.getEntityClassName()
              + "]'s children on Type[" + childClassName
              + "] is not accessible because the entity does not "
              + " support a One-To-Many relationship with this class.");
    }
  }

  /**
   * Validate the sFKField is a Child ForeinKey Field (i.e., there is a defined
   * One-to-Many relationship on that field) and if true, check whether the
   * field contains any Children.  Note that sFKField must be the class name of
   * the Child Entity (e.g., actionAccess for Entity[ActionAccess]). This process
   * will be throw an exception if validateChildFKField failed, or
   * getChildCollection failed. and return true if the getChildCollection
   * returns a non-empty collection.
   * @param entityBean instance of TBean
   * @param childClass the child bean Class (extends Serializable)
   * @return boolean
   * @throws Exception
   */
  public <TChild extends Serializable> boolean 
          validateFKChildrenExist(TBean entityBean, Class<TChild> childClass)
          throws Exception {
    if (childClass == null) {
      throw new Exception("The ForeignKey Fields Class is undefined");
    }
    String childClassName = childClass.getSimpleName();
    this.validateChildFKField(childClassName);
    Collection pCol = ReflectionInfo.getChildCollection(entityBean, childClass);
    return ((pCol != null) && (!pCol.isEmpty()));
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Merge Methods">
  /**
   * This method is called during a merge process if target records that are not in the
   * source record set (<tt>srcMap</tt>) should be removed from the underlying table.
   * <p>
   * <b>NOTE:</b> Always call mergeRemove before call {@linkplain #merge(
   * java.util.HashMap, java.util.HashMap, gov.ca.water.cdec.core.EntityMergeDelegate) 
   * this.merge} to avoid unique constraint violations.</p>
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param trgMap the target map of records to update
   */
    public <TKey extends Serializable, TSrc> void mergeRemove(HashMap<TKey, TSrc> srcMap, 
                                                       HashMap<TKey, TBean> trgMap) {
    if ((srcMap == null) || (trgMap == null) || (trgMap.isEmpty())) {
      return;
    }
    try {
      for (TKey trgKey : trgMap.keySet()) {
        if ((trgKey != null) && (!srcMap.containsKey(trgKey))) {
          TBean bean = trgMap.get(trgKey);
          if (bean != null) {
            this.remove(bean);
            logger.log(Level.INFO, "Remove Record {0}", trgKey.toString());
          }
        }
      }
      /* Flush EntityManager to commit changes */
      if (this.entMngr != null) {
        this.entMngr.flush();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.mergeRemove Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp);
    }
  }
  
  /**
   * This method is called to merge two sets of data: <tt>srcMap</tt> containing new
   * records not in the existing database and <tt>trgMap</tt> containing existing records
   * retrieved from the existing database.
   * <p>
   * All records in the srcMap that are not in the trgMap will be inserted as new
   * records, calling the <tt>delegate's</tt> {@linkplain EntityMergeDelegate#newMerge(
   * java.io.Serializable) newMerge} method to initiate the new record.
   * <p>
   * All records in the srcMap that are in the trgMap will be updates if the records
   * has changed. the <tt>delegate's</tt> {@linkplain EntityMergeDelegate#updateMerge(
   * java.io.Serializable, java.io.Serializable) updateMerge} method is called to
   * check if the record has changed and if true to update the target record and then
   * return true - to indicate that the update record must be submitted to the database.
   * <p>
   * <b>NOTE:</b> To prevent unique constraint violations, all record updates are 
   * completed before the new records are inserted.</p>
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param trgMap the target map of records to update
   * @param delegate the Merge Delegate to initiate new records or to update existing
   * records.
   * @throws IllegalArgumentException is the process failed.
   */
    public <TKey extends Serializable, TSrc> void
          merge(HashMap<TKey, TSrc> srcMap, HashMap<TKey, TBean> trgMap,
                EntityMergeDelegate<TSrc, TBean> delegate) {
    try {
      List<TBean> insertList = new ArrayList<>();
      List<TBean> updateList = new ArrayList<>();
      for (Map.Entry<TKey, TSrc> e : srcMap.entrySet()) {
        if (trgMap.containsKey(e.getKey())) {
          TBean target = trgMap.get(e.getKey());          
          if (delegate.updateMerge(e.getValue(), target)) {
//            this.edit(target);
            updateList.add(target);
          }
        } else {
          TBean target = delegate.newMerge(e.getValue());
          if (target != null) {
            insertList.add(target);
          }
        }
      }
      
      this.mergeUpdate(updateList);
      this.mergeInsert(insertList);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.merge Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp);
    }
  }

  /**
   * Insert all the records into the database
   * 
   * @param records
   * @throws Exception by the entity manager
   */
  private void mergeInsert(List<TBean> records) throws Exception {
    if ((records != null) && (records.size() > 0)){
      for (TBean bean : records) {
        this.create(bean);
      }
      
      /* Flush EntityManager to commit changes */
//      if (this.entMngr != null) {
//        this.entMngr.flush();
//      }
    }
  }
  
  /**
   * Update all the records in the database
   * 
   * @param records
   * @throws Exception by the entity manager
   */
  private void mergeUpdate(List<TBean> records) throws Exception {
    if ((records != null) && (records.size() > 0)){
      for (TBean bean : records) {
        this.edit(bean);
      }
      
      /* Flush EntityManager to commit changes */
//      if (this.entMngr != null) {
//        this.entMngr.flush();
//      }
    }
  }
          
  /**
   * This method is called to merge a new set of record (<tt>srcMap</tt>) with all 
   * existing records of type TBean using a <tt>mapDelegate</tt> - to convert all 
   * existing records to a target map - and a <tt>mergeDelegate</tt> - to assist with
   * initiating new records or update the existing records. The process is skipped if 
   * <tt>srcMap</tt> = null|Empty.
   * <p>
   * It calls {@linkplain #findAll() this.findAll} to retrieve all existing records and
   * the use the <tt>mapDelegate</tt> to convert the existing records to a target Map.
   * It then calls {@linkplain #merge(java.util.HashMap, java.util.HashMap, 
   * gov.ca.water.cdec.core.EntityMergeDelegate) this.merge(srcMap, trgMap, 
   * mergeDelegate)} to handle the merging of the records.
   * <p>
   * The process will fail is generating the target Map fails. All errors are trapped and
   * logged.
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param mapDelegate the delegate for converting the existing record to a target Map
   * @param mergeDelegate the Merge Delegate to initiate new records or to update existing
   * records.
   * @throws IllegalArgumentException is the process failed.
   */
  public <TKey extends Serializable, TSrc> void
        mergeAll(HashMap<TKey, TSrc> srcMap, MapperDelegate<TKey, TBean> mapDelegate,
        EntityMergeDelegate<TSrc, TBean> mergeDelegate, boolean removeMissing) {
    if ((srcMap == null) || (srcMap.isEmpty())) {
      return;
    }
    
    try {
      if (mapDelegate == null) {
        throw new Exception("The Mapper Delegate to convert the existing records to a "
                + "target map is not specified.");
      }  
      if (mergeDelegate == null) {
        throw new Exception("The Entity Merge Delegate to assist with merging the source "
                + "and target maps is not specified.");
      }  
      
      HashMap<TKey, TBean> trgMap = new HashMap<>();
      List<TBean> trgList = this.findAll();
      if ((trgList != null) && (!trgList.isEmpty())) {
        mapDelegate.toMap(trgList, trgMap);
      }
      
      if (removeMissing) {
        this.mergeRemove(srcMap, trgMap);
      }
      
      this.merge(srcMap, trgMap, mergeDelegate);      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.merge Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp);
    }
  }
  // </editor-fold>
}
