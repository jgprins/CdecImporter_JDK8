package bubblewrap.entity.core;

import bubblewrap.admin.context.AdminContext;
import bubblewrap.admin.interfaces.IAccessValidator;
import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.enums.FacadeFilterEnums;
import bubblewrap.core.enums.IntFlag;
import bubblewrap.core.events.*;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.context.*;
import bubblewrap.entity.enums.BwFieldAlias;
import bubblewrap.entity.enums.EntityFieldRefs;
import bubblewrap.entity.validators.FieldValidator;
import bubblewrap.http.request.annotation.HttpParameter;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.params.ParameterMapBase;
import bubblewrap.io.validators.InputValidator;
import bubblewrap.navigation.enums.AppActions;
import bubblewrap.navigation.enums.AppTasks;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;

/** 
 * <p>A Wrapper for the Entity Bean class to support the business logic applicable to
 * the bean. The Wrapper access the bean through its registered
 * {@linkplain EntityFacade}. The EntityWrapper is the class through which its entity
 * beans should be read, initiated, edited, or deleted.</p>
 * <p>The EntityWrapper is accessible through is associated EnityView, EntityInfo, 
 * EntitySelector, or EntitySearch classes.</p>
 * TPuMngr extends PuEntityManager
 * @author kprins
 * @version 1.00.001 (09/12/16) Add the forceAccess fag to SubmitNew and SubmitEdits
 * @version 1.00.002 (09/14/16) modify the SubmitNew and RefreshRecord methods.
 * @version 1.00.003 (09/15/16) fix bug in isUniqueInParent.
 * @version 1.00.004 (11/01/16) Add the {@linkplain #assignManualPk() assignManualPk}
 * method and update the submitNew method to initiate the new records Manual |
 * Composite PrimaryKey before calling this.facade.create(this,entityBean).
 */
public class EntityWrapper<TBean extends Serializable> implements Serializable {
 
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
          Logger.getLogger(EntityWrapper.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="A Void EntityWrapper Class">
  /**
   * A Void class - not constructible
   */
  public class Void extends EntityWrapper<Void> {
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public Void() {
      super();
      throw new IllegalArgumentException("A Void class is not constructable.");
    }
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Call the super method before disposing local resources</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="ParentCache Class">
  /**
   * A Internal class for defines a ParentWrapper Cache for storing the parent
   * Wrapper reference and two flag to indicate whether the parent is a defined
   * foreignKey and whether the parent is a defined ForeignKey and the Child
   * OwnerParent
   */
  protected class ParentCache implements Serializable {
    
    /**
     * The Cache's parent EntityWrapper
     */
    public final EntityWrapper parent;
    /**
     * Flag set if the parent is a defined ForeignKey
     */
    public final boolean isFK;
    /**
     * Flag set if the parent is a defined ForeignKey and the Child OwnerParent
     */
    public final boolean isOwner;
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public ParentCache(EntityWrapper parent, boolean isFK, boolean isOwner) {
      if (parent == null) {
        throw new NullPointerException("The ParentCache's Parent EntityWrapper "
                + "cannot be unassigned");
      }
      this.parent = parent;
      this.isFK = isFK;
      this.isOwner = ((isFK) && (isOwner));
    }
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Call the super method before disposing local resources</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
    //</editor-fold>
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Retrieve the generic Entity Bean Class from the extended EntityWrapper Class
   * @param wrapperClass a class that extends EntityWrapper
   * @return the Entity Bean Class
   */
  @SuppressWarnings("unchecked")
  public static <TEnt extends Serializable> Class<TEnt>
          getEntityClass(Class<? extends EntityWrapper> wrapperClass) {
    Class<TEnt> result = null;
    if (wrapperClass == null) {
      throw new NullPointerException("The EntityWrapper class is not specified");
    }
    
    result = (Class<TEnt>)
            ReflectionInfo.getGenericClass(EntityWrapper.class, wrapperClass, 0);
    return result;
  }
  
  /**
   * Lookup and return the Entity's Facade based on the specified 
   * EntityWrapper class. It retrieves the EntityWrapper's Entity Class and call 
   * {@linkplain #getFacadeByBean(java.lang.Class) getFacadeByBean()} to retrieve
   * the EntityFacade
   * @param <TBean> extends Serializable
   * @param <TWrapper> EntityWrapper
   * @param wrapperClass the specified EntityWrapper class
   * @return the EntityWrapper's EntityFacade
   * @throws Exception if the wrapperClass=null, or its bean class is not accessible,
   * or getFacadeByBean throws an exception.
   */
  public static <TBean extends Serializable, TWrapper extends EntityWrapper<TBean>>
          EntityFacade<TBean> getFacadeByWrapper(Class<TWrapper> wrapperClass)  
          throws Exception {
    EntityFacade<TBean> result = null;
    if (wrapperClass == null) {
      throw new Exception("EntityWrapper Class is not specified");
    }
    
    Class<TBean> beanClass = EntityWrapper.getEntityClass(wrapperClass);
    if (beanClass == null) {
      throw new Exception("Unable to retrieve EntityWrapper[" 
              + wrapperClass.getSimpleName() + "]'s generic Entity Class information.");
    }
    
    return EntityWrapper.getFacadeByBean(beanClass);
  }
  
  /**
   * Overload 2: Lookup and return the Entity's Facade by its generic Entity (bean) 
   * class. This  method retrieves the Entity class' registered {@linkplain 
   * EntityContext} from the {@linkplain BwAppContext}, the Entity class' registered 
   * {@linkplain PuEntityManager} class from the EntityContext. It then lookup the 
   * PuEntityManager by its class and retrieve the EntityFacade from the 
   * PuEntityManager by the Entity class.
   * @param <TBean> extends Serializable
   * @param beanClass the specified Entity class
   * @return the Entity's EntityFacade
   * @throws Exception if the BwAppContext, EntityContext, PuEntityManager, or 
   * EntityFacade is not accessible or registered.
   */
  public static <TBean extends Serializable>
          EntityFacade<TBean> getFacadeByBean(Class<TBean> beanClass)  
          throws Exception {
    EntityFacade<TBean> result = null;
    if (beanClass == null) {
      throw new Exception("The Entity Class is unassigned.");
    }
    
    BwAppContext appCtx = BwAppContext.doLookup();
    if (appCtx == null) {
      throw new NullPointerException("Unable to access the Application's"
              + " BwAppContext.");
    }
    EntityContext<TBean> entCtx = appCtx.getEntityContext(beanClass);
    if (entCtx == null) {
      throw new Exception("Unable to access the EntityContext for Entity[" 
              + beanClass.getSimpleName() +"].");
    }
    
    PuEntityManager puMngr = PuEntityManager.doLookup(entCtx.getPuManagerClass());
    if (puMngr == null) {
      throw new Exception("Unable to access the PuEntityManager for Entity[" 
              + beanClass.getSimpleName() +"].");
    }
    
    result = puMngr.getFacade(beanClass);
    if (result == null) {
      throw new Exception("Unable to access the EntityFacade for Entity[" 
              + beanClass.getSimpleName() +"].");
    }
    return result;
  }
  
  /**
   * <p>Create a new EntityWrapper Instance of class specified class by retrieving the
   * Entity Bean based on the specified recordId. It retrieves the Wrapper class' Entity
   * class, its EntityContext, and from that its PuEntityManager. It uses the
   * PuEntityManager to access the entity class' facade, which is used to retrieve the
   * entity bean for the specified recordId.</p>
   * <p><b>Note:</b> If the EntityFacade has an assigned FkFilter with a matching 
   * recordId, the FkFilter will be return.</p>
   * @param wrapperClass the EntityWrapper class
   * @param recordId the entity bean's recordId (must be not null)
   * @return the new instance of the EntityWrapper
   * @throws Exception if any of the entity's associated components or the entity bean
   * cannot be accessed.
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TWrapper extends EntityWrapper<TBean>> 
          TWrapper newFromRecId(Class<TWrapper> wrapperClass, Serializable recordId) 
          throws Exception {
    TWrapper result = null;
    if (wrapperClass == null) {
      throw new Exception("EntityWrapper Class is not specified");
    }
    
    if (recordId == null) {
      throw new Exception("Entity's RecordId is not specified");
    }
    
    Class<TBean> beanClass = EntityWrapper.getEntityClass(wrapperClass);
    if (beanClass == null) {
      throw new Exception("Unable to retrieve EntityWrapper[" 
              + wrapperClass.getSimpleName() + "]'s generic Entity Class information.");
    }
    
    EntityFacade<TBean> entFacade = EntityWrapper.getFacadeByBean(beanClass);
    if (entFacade == null) {
      throw new Exception("Unable to access the EntityFacade for Entity[" 
              + beanClass.getSimpleName() +"].");
    }
    
    if ((entFacade.hasPkFilter()) && (entFacade.isPkFilterRecordId(recordId))) {
      result = (TWrapper) entFacade.getPkFilter();
    } else {
      TBean entBean = entFacade.find(recordId);
      if (entBean == null) {
        throw new Exception("Unable to retrieve the Entity for RecordId[" 
                + recordId.toString() + "].");
      }

      Class[] pArgs = {beanClass};
      Object[] pObjs = {entBean};

      Constructor pConst = wrapperClass.getConstructor(pArgs);
      if (pConst == null) {
        throw new Exception("Unable to Locate the Record Class["
                + wrapperClass.getSimpleName() + "]'s Constructor");
      }

      result = (TWrapper) pConst.newInstance(pObjs);
    }
    return result;
  }
  
  /**
   * Create a new Instance of the specified class EntityWrapper class using the 
   * specified Entity Bean. If pBean=null, a new Bean will be created.
   * @param wrapperClass the EntityWrapper class
   * @param entBean the entity Bean
   * @return a new EntityWrapper instance
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TWrapper extends EntityWrapper<TBean>> 
          TWrapper newFromBean(Class<TWrapper> wrapperClass, TBean entBean) 
          throws Exception {
    TWrapper result = null;
    if (wrapperClass == null) {
      throw new Exception("EntityWrapper Class is not specified");
    }
    
    Class<TBean> pEntityClass = EntityWrapper.getEntityClass(wrapperClass);
    if ((entBean != null) && (!pEntityClass.equals(entBean.getClass()))) {
      throw new Exception("Entity[" + entBean.toString()
              + "] does not match the required "
              + "EntityClass[" + pEntityClass.getSimpleName() + "]");
    }
    
    Class[] pArgs = {pEntityClass};
    Object[] pObjs = {entBean};
    
    Constructor pConst = wrapperClass.getConstructor(pArgs);
    if (pConst == null) {
      throw new Exception("Unable to Locate the Record Class["
              + wrapperClass.getSimpleName() + "]'s Constructor");
    }
    
    result = (TWrapper) pConst.newInstance(pObjs);
    return result;
  }
  
  /**
   * STATIC: Return a list of EntityWrapper instances for the EntityBeans in childBeans
   * using the static {@linkplain #newFromBean(java.lang.Class, java.io.Serializable) 
   * newFromBean} to initiate the child EntityWrappers. Exceptions are logged.
   * @param <TWrapper> extends EntityWrapper of type TBean
   * @param <TBean> extends Serializable
   * @param childBeans a list of child beans
   * @param childWrapperClass the class of the child bean's wrapper
   * @return a list if child wrappers
   */
  @SuppressWarnings("unchecked")
  public static <TWrapper extends EntityWrapper<TBean>, TBean extends Serializable> 
          List<TWrapper> getChildList(Collection<TBean> childBeans, 
          Class<TWrapper> childWrapperClass) {
    List<TWrapper> pChildList = new ArrayList<>();
    if ((childBeans != null) && (!childBeans.isEmpty())) {
      try {
        for (TBean pBean : childBeans) {
          TWrapper pChild = 
                        (TWrapper) EntityWrapper.newFromBean(childWrapperClass, pBean);
          if (pChild != null) {
            pChildList.add(pChild);
          }
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "EntityWrapper.getChildList Error:\n {0}",
                exp.getMessage());
        pChildList.clear();
      }
    }
    return pChildList;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityWrapper's Entity Bean
   */
  private TBean entityBean;
  /**
   * Placeholder for the Entity's PuEntityManager
   */
  private PuEntityManager puManager;
  /**
   * Placeholder for the Entity's Facade
   */
  private EntityFacade<TBean> entityFacade;
  /**
   * Placeholder for the Entity's Facade
   */
  private EntityContext<TBean> entityContext;  
  /**
   * Placeholder for the Child Wrapper's ParentCache with the parent Bean class as
   * the mapKey
   */
  private HashMap<Class<? extends Serializable>, ParentCache> parentCache;
  /**
   * Placeholder for the Parent Wrapper's ForeignKey Child Collection Cache with
   * the Child Bean Class as the Map Key
   */
  private HashMap<Class<? extends Serializable>, 
                             Collection<? extends EntityWrapper>> childCollectionCache;
  /**
   * Private Placeholder for error messages - (default=null)
   */
  private String errorMsg;
  /**
   * Placeholder for the Editing Counter that can be use during batch editing of the
   * Entities properties
   */
  private int editingCount = 0;
  /**
   * A flag set when the Wrapper fields are editor a Field Editor (e.g., a 
   * bubblewrap.jsf.forms.ContentForm). S
   */
  private Boolean fieldEditor;
  /**
   * Internal flag set to true while the submitNew or SubmitEdit processes if ongoing.
   * No calls to these two methods should be made when this.isSubmitting=true.
   */
  private boolean isSubmitting = false;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="EventHandlers">
  /**
   * EventHandler for sending a Field Changed event.
   */
  public final FieldChangedEventHandler FieldChanged;
  /**
   * Method called to fire the Field Changed event.
   */
  public void fireFieldChanged(FieldChangedEventArgs args) {
    this.FieldChanged.fireEvent(this, args);
  }
  
  /**
   * EventHandler for sending a Add Child event.
   */
  public final ChildChangedEventHandler AddChild;
  /**
   * Method called by {@linkplain EntityFacade#addToFkParent(java.io.Serializable, 
   * java.io.Serializable) EntityFacade.addToFkParent} to fire 
   * the AddChild event. Before it fires the event, it calls {@linkplain 
   * #onAddChild(bubblewrap.core.events.ChildChangedEventArgs) this.onAddChild}
   * to allow the parent to update any cached list before other listeners access these 
   * list.
   */
  protected void fireAddChild(ChildChangedEventArgs args) {
    this.onAddChild(args);
    this.AddChild.fireEvent(this, args);
  }
  
  /**
   * EventHandler for sending a Delete Child event.
   */
  public final ChildChangedEventHandler DeleteChild;
  /**
   * Method called by {@linkplain EntityFacade#removeFromFkParents(
   * bubblewrap.entity.core.EntityWrapper) EntityFacade.removeFromFkParents} to fire 
   * the Delete Child event. Before it fires the event, it calls {@linkplain 
   * #onDeleteChild(bubblewrap.core.events.ChildChangedEventArgs) this.onDeleteChild}
   * to allow the parent to update any cached list before other listeners access these 
   * list.
   */
  protected void fireDeleteChild(ChildChangedEventArgs args) {
    this.onDeleteChild(args);
    this.DeleteChild.fireEvent(this, args);
  }
  
  /**
   * EventHandler for sending a RecordRefreshed  event. Called after the record
   * has been {@linkplain #refreshRecord() refreshed} and the Wrapper has been 
   * {@linkplain #resetRecord() reset}.
   */
  public final EventHandler RecordRefreshed;
  /**
   * Method called to fie the RecordRefreshed  event.
   */
  protected void fireRecordRefreshed() {
    this.RecordRefreshed.fireEvent(this, new EventArgs());
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * A parameterless constructor - call overload 2 with entBean=null.
   */
  protected EntityWrapper() {
    this(null);
  }
  
  /**
   * A protected constructor with a reference to the EntityWrapper's Bean.
   * @param entBean the entity bean reference (null to initiate a new bean)
   */
  protected EntityWrapper(TBean entBean) {
    this.FieldChanged = new FieldChangedEventHandler();
    this.AddChild = new ChildChangedEventHandler();
    this.DeleteChild = new ChildChangedEventHandler();
    this.RecordRefreshed = new EventHandler();
    this.parentCache = null;
    this.childCollectionCache = null;
    this.entityBean = null;
    this.entityFacade = null;
    this.entityContext = null;
    this.errorMsg = null;    
    this.isSubmitting = false;
    this.editingCount = 0;
    this.setEntity(entBean);
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call super method before clearing the entityFacade and entityContext. 
   * It then calls {@linkplain #clearEntity()} to release entity-related resources.
   * Override {@linkplain #onClearEntity()} instead of this method to release additional
   * entity-related resources.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.FieldChanged.clear();
    this.RecordRefreshed.clear();
    this.AddChild.clear();
    this.DeleteChild.clear();
    this.entityFacade = null;
    this.entityContext = null;
    this.parentCache = null;
    this.childCollectionCache = null;
    this.clearEntity();
  }
  //</editor-fold>  

  // <editor-fold defaultstate="collapsed" desc="Public Reflection Methods">
  /**
   * FINAL: Get the Entity Bean's Class
   * @return the generically assigned Entity Bean class
   */
  @SuppressWarnings("unchecked")
  public final Class<TBean> getEntityClass() {
    Class thisClass = this.getClass();
    return ReflectionInfo.getGenericClass(EntityWrapper.class, thisClass, 0);
  }

  /**
   * Get the EntityView's associated Facade Class
   * @return the generically assigned PuEntityManager class
   */
  @SuppressWarnings("unchecked")
  protected final Class<? extends PuEntityManager> getPuManagerClass() {
    EntityContext entCtx = this.getEntityContext();
    Class<? extends PuEntityManager> result =
                            (entCtx == null)? null: entCtx.getPuManagerClass();
    return result;
  }

  /**
   * Get the EntityView's associated Facade Class
   * @return the generically assigned PuEntityManager class
   */
  @SuppressWarnings("unchecked")
  protected final PuEntityManager getPuManager() {
    if (this.puManager == null) {
      try {
        Class<? extends PuEntityManager> puClass = this.getPuManagerClass();
        this.puManager = PuEntityManager.doLookup(puClass);
        if (this.puManager == null) {
          throw new NamingException("Unable to access the Wrapper's "
                  + "PuEntityManager[" + puClass.getSimpleName() + "].");
        }        
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.getPuManager Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        throw new IllegalArgumentException("Unable to initiate " 
                + this.getClass().getSimpleName() + ".PuManager.",exp);
      }
    }
    return this.puManager;
  }

  /**
   * Get a Reference to the EntityView associated Facade. Calls {@linkplain 
   * #initFacade()} to initiate the facade if it does not yet exists.
   * @return the internal facade reference.
   */
  @SuppressWarnings("unchecked")
  public final EntityFacade<TBean> getFacade() {
    if (this.entityFacade == null) {
      try {
        PuEntityManager puMngr = this.getPuManager();
        Class<TBean> entClass = this.getEntityClass();
        if (entClass == null) {
          throw new NamingException("Unable to access the Wrapper's "
                  + "Entity Bean class.");
        }
        
        this.entityFacade = puMngr.getFacade(entClass);
        if (this.entityFacade == null) {
          throw new NamingException("Unable to initiate EntityFacade for Entity Bean["
                  + entClass.getSimpleName() + "].");
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.initFacade Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        throw new IllegalArgumentException("Unable to initiate " 
                + this.getClass().getSimpleName() + ".entityFacade.",exp);
      }
    }
    return this.entityFacade;
  }

  /**
   * Get the FacadeHelper's EntityLink Definition. Throw an exception if the
   * FacadeHelper or EntityLink is not undefined
   * @return EntityLink
   */
  public final EntityContext<TBean> getEntityContext() {
    if (this.entityContext == null) {
      try {
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new NullPointerException("Unable to access the Application's "
                  + "BwAppContext.");
        }

        Class<TBean> entClass = this.getEntityClass();
        if (entClass == null) {
          throw new NamingException("Unable to access the Wrapper's "
                  + "Entity Bean class.");
        }
        
        this.entityContext = appCtx.getEntityContext(entClass);
      } catch (NullPointerException | NamingException exp) {
        logger.log(Level.WARNING, "{0}.initFacade Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        throw new IllegalArgumentException("Unable to initiate " 
                + this.getClass().getSimpleName() + ".entityContext.",exp);
      }
    }
    return this.entityContext;
  }

  /**
   * Get a reference to the EntityWrapper's AccessValidator. 
   * @return AdminContext as the IAccessValidator or null if no access validation is
   * required.
   */
  private IAccessValidator getAccessValidator() {
    IAccessValidator result = null;
    EntityContext<TBean> entCtx = this.getEntityContext();
    if ((entCtx != null) && (entCtx.doAccessValidation())) {
      AdminContext pContext = AdminContext.doLookup();
      result = pContext;
    }
    return result;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Entity Management Methods">
  /**
   * Get reference the EntityWrapper's Entity Bean
   * @return the assigned bean
   */
  public final TBean getEntity() {
    return this.entityBean;
  }
  
  /**
   * Call to assign the EntityWrapper's underlying Entity Bean. If (entityBean==null),
   * it calls {@linkplain #onBeforeNewEntity()} else it calls {@linkplain #onSetEntity(
   * java.io.Serializable) onSetEntity(entityBean)}.
   * @param entityBean the wrapper underlying entity bean (or null to initiate a new
   * bean).
   * @throws NamingException
   */
  protected final void setEntity(TBean entityBean) throws IllegalArgumentException {
    if (entityBean == null) {
      this.onInitNewEntity();
    } else {
      this.onSetEntity(entityBean);
    }
  }
  
  /**
   * <p>This method is called by {@linkplain #setEntity(java.io.Serializable) 
   * setEntity()}to initiate a new Entity. This process includes the following steps:
   * </p><ol>
   *  <li>It sets this.entityBean=null;</li>
   *  <li>Initiates a new instance of the EntityWrapper Bean class (say newBean)</li>
   *  <li>Calls {@linkplain #onBeforeNewEntity(java.io.Serializable) 
   *    onBeforeNewEntity(newBean)} to custom initiate the new instance</li>
   *  <li>Calls {@linkplain #onBeforeSetEntity(java.io.Serializable)
   *    onBeforeSetEntity(newBean)} to further initiate and validate the
   *    newBean properties.</li>
   *  <li>Assigns the new instance to this.entityBean</li>
   *  <li>Calls {@linkplain #onAfterSetEntity(java.io.Serializable) 
   *    onAfterSetEntity(newBean)} to initiate any sub-component, etc. based on the
   *    newBean properties.</li>
   * </ol>
   * @exception IllegalArgumentException if initiation of a new instance of the
   * EntityWrapper Bean class failed. It calls {@linkplain #clearEntity()} on any 
   * Exception.
   */
  @SuppressWarnings("unchecked")
  private void onInitNewEntity() {
    this.entityBean = null;
    try {
      Class beanClass = this.getEntityClass();
      TBean newBean = (TBean) beanClass.newInstance();
      this.onBeforeNewEntity(newBean);
      this.onBeforeSetEntity(newBean);
      this.entityBean = newBean;
      this.onAfterSetEntity(newBean);
    } catch (Exception exp) {
      this.clearEntity();
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".initNewEntity failed.\n" + exp.getMessage());
    }
  }
  
  /**
   * CAN OVERRIDE: Can override to initiate a new entity bean for the
   * EntityWrapper. It is called by {@linkplain #onInitNewEntity()} before assigning the
   * entityBean as the EntityWrapper's bean. The base method does nothing.
   * @param entityBean the new instance the EntityWrapper Bean class to initiate
   * @throws Exception thrown to terminate the process due to an error.
   */
  @SuppressWarnings("unchecked")
  protected void onBeforeNewEntity(TBean entityBean) throws Exception {
  }
  
  /**
   * <p>This method is called by {@linkplain #setEntity(java.io.Serializable) setEntity()}
   * when a new entity bean is assigned as the EntityWrapper's bean. This process 
   * includes the following steps:
   * </p><ol>
   *  <li>It calls {@linkplain #onBeforeSetEntity(java.io.Serializable) 
   *    onBeforeSetEntity(entityBean)} to allow for any initiation or validating of the 
   *    bean's properties.</li>
   *  <li>Assigns the new instance to this.entityBean</li>
   *  <li>Calls {@linkplain #onAfterSetEntity(java.io.Serializable) 
   *    onAfterSetEntity(entityBean)} to initiate any sub-component, etc. based on the
   *    entityBean properties.</li>
   * </ol>
   * @param entityBean the new bean to assign the EntityWrapper's bean
   * @exception IllegalArgumentException when entityBean=null or this process fails. 
   * This will terminate the process.
   */
  private void onSetEntity (TBean entityBean) {
    try {
      if (entityBean == null) {
        throw new IllegalArgumentException("The Entity Bean cannot be unassigned");
      }
      this.onBeforeSetEntity(entityBean);
      this.entityBean = entityBean;
      this.onAfterSetEntity(entityBean);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".onSetEntity Error:\n " + exp.getMessage());
    }
  }
  
  /**
   * CAN OVERRIDE: Can override to set entity properties, to validate the bean before it
   * is assigned as the EntityWrapper's bean.  It is called by {@linkplain 
   * #onSetEntity(java.io.Serializable) onSetEntity()} BEFORE assign the entity bean
   * as this.entityBean. Base method does nothing
   * @param entityBean the new entity bean
   * @throws Exception when the process fails or to terminate the process.
   */
  protected void onBeforeSetEntity(TBean entityBean) throws Exception {    
  }
  
  /**
   * CAN OVERRIDE: Can override to post initiate related resource that depends on the
   * EntityWrapper's bean. It is called by {@linkplain #onInitNewEntity()} and {@linkplain 
   * #onSetEntity(java.io.Serializable) onSetEntity()} AFTER assigning the
   * entityBean as the EntityWrapper's bean. The base method does nothing.
   * @param entityBean the new instance the EntityWrapper Bean class
   * @throws Exception thrown to terminate the process due to an error.
   */
  @SuppressWarnings("unchecked")
  protected void onAfterSetEntity(TBean entityBean) throws Exception {
  }
  
  /**
   * Called by refreshRecord and Finalize to clear the reference to the entityBean (and any 
   * other related resources. This method calls {@linkplain #onClearEntity()} before
   * setting this.entityBean=null.
   */
  protected final void clearEntity() {
    try {
      this.onClearEntity();
    } catch (Exception exp) {
    } finally {
      this.entityBean = null;
    }
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #clearEntity()} before reseting the entityBean.
   */
  protected void onClearEntity() {
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Entity Identification Methods">
  /**
   * <p>Get the Entity's Primary Key value as an using the entityContext.idField.</p> 
   * <p><b>Note:</b>The recordId type is typically either a Integer for numeric IDs
   *  or a String of GUIDs.</p>
   * @param <TRecId> extends Serializable
   * @return the value for entityBean.field[entityContext.idField]
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */
  @SuppressWarnings("unchecked")
  @HttpParameter(readOnly = true)
  public <TRecId extends Serializable> TRecId getRecordId() {
    TRecId result = null;
    if (this.entityBean != null) {
      EntityContext<TBean> entCtx = this.getEntityContext();
      FieldInfo fieldInfo = entCtx.getPrimaryKey();
      if (fieldInfo == null) {
        throw new NullPointerException("Entity[" + this.getEntityClass().getSimpleName()
                + "'s recordId Field is not defined in its EntityContext class or its "
                + "EntityContext is not acessible.");
      }

      try {
        result = (TRecId) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      } catch (Exception ecp) {
        result = null;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".getRecordId Error:\n" + ecp.getMessage());
      }
    }
    return result;
  }

  /**
   * CAN OVERRIDE: Get the Entity's recordName - If the EntityContext contains a 
   * RecordName Field, this field's value will be returned. Override only if a 
   * custom recordName (e.g. a composite of two fields) is applicable.
   * @return the assigned recordName or this.toString if the entity or recordName field
   * is undefined.
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */
  @HttpParameter
  public String getRecordName() {
    String result = null;
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.RECORD_NAME)) == null)) {
      result = this.toString();
    } else {
      try {
        result = (String) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      } catch (Exception exp) {
        result = null;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".getRecordName Error:\n" + exp.getMessage());
      }
    }
    return result;
  }
  
  /**
   * <p>CAN OVERRIDE: Set the Entity's recordName - If the EntityContext contains a
   * RecordName Field, this field's value will be assign if the currently assigned
   * value is not equal to newName (case sensitive). Override only if a
   * custom recordName (e.g. a composite of two fields) is applicable.</p>
   * <p><b>Note:</b> The name assignment will ignored if the RecordName's FieldInfo is 
   * not defined, the field is ReadOnly, or the value not valid.</p>
   */
  public void setRecordName(String newVal) {
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.RECORD_NAME)) == null)) {
      return;
    } 
    
    String curValue = null;
    try {
      curValue = (String) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      newVal = DataEntry.cleanString(newVal);
      if ((!DataEntry.isEq(newVal, curValue, false)) &&
              (this.canEditField(EntityFieldRefs.recordName)) &&
              (this.validateField(fieldInfo.entityPath.fieldName, newVal))) {
        this.setFieldValue(this.entityBean, fieldInfo, newVal);
        this.submitFieldEdits(!this.isEditing(),false);
        FieldChangedEventArgs args = 
                            new FieldChangedEventArgs("recordName", curValue, newVal);
        this.fireFieldChanged(args);
      }
    } catch (Exception exp) {
      this.setErrorMsg(this.getClass().getSimpleName()
              + ".setRecordName Error:\n" + exp.getMessage());
    }
  }  

  /**
   * Get the Entity's display Index - If the EntityContext contains a displayIdx Field, 
   * its value will be returned. 
   * @return the assigned displayIdx or zero (0) if the entity or displayIdx field
   * is undefined.
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */
  @HttpParameter
  public Integer getDisplayIdx() {
    Integer result = 0;
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean != null) && ((entCtx = this.getEntityContext()) != null) &&
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.DISPLAY_IDX)) != null)) {
      try {
        result = (Integer) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      } catch (Exception exp) {
        result = 0;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".getDisplayIdx Error:\n" + exp.getMessage());
      }
    }
    return result;
  }
  
  /**
   * Set the Entity's display Index - If the EntityContext contains a displayIdx Field
   * <p><b>Note:</b> The value assignment will ignored if the FieldInfo is 
   * not defined, the field is ReadOnly, or the value not valid.</p>
   * @param newVal new value to assign
   */
  public void setDisplayIdx(Integer newVal) {
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.DISPLAY_IDX)) == null)) {
      return;
    } 
    
    Integer curValue = null;
    try {
      curValue = (Integer) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      newVal = (newVal == null)? 0: newVal;
      if ((!DataEntry.isEq(newVal, curValue)) &&
              (this.canEditField(EntityFieldRefs.displayIdx)) &&
              (this.validateField(fieldInfo.entityPath.fieldName, newVal))) {
        this.setFieldValue(this.entityBean, fieldInfo, newVal);
        this.submitFieldEdits(!this.isEditing(),false);
        FieldChangedEventArgs args = 
                            new FieldChangedEventArgs("displayIdx", curValue, newVal);
        this.fireFieldChanged(args);
      }
    } catch (Exception exp) {
      this.setErrorMsg(this.getClass().getSimpleName()
              + ".setDisplayIdx Error:\n" + exp.getMessage());
    }
  }

  /**
   * Get the Entity's disabled state - If the EntityContext contains a disabled Field, 
   * its value will be returned. 
   * <p><b>Note:</b> Always return false if this.isNew</p>
   * @return the assigned disabled state or false if this.isNew or the entity or 
   * disabled field is undefined.
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */
  @HttpParameter
  public Boolean getDisabled() {
    Boolean result = false;    
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean != null) && ((entCtx = this.getEntityContext()) != null) &&
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.DISABLED)) != null)) {
      try {
        result = (Boolean) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      } catch (Exception exp) {
        result = false;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".getDisabled Error:\n" + exp.getMessage());
      }
    }
    return result;
  }

  /**
   * Set the Entity's Disabled state - If the EntityContext contains a disabled Field
   * <p><b>Note:</b> The value assignment will ignored if the FieldInfo is 
   * not defined, the field is ReadOnly, or the value not valid.</p>
   * @param newVal new value to assign
   */
  public void setDisabled(Boolean newVal) {
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.DISABLED)) == null)) {
      return;
    } 
    
    newVal = (newVal == null)? false: newVal;
    Boolean curValue = null;
    try {
      curValue = (Boolean) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      if ((!DataEntry.isEq(newVal, curValue)) &&
              (this.canEditField(EntityFieldRefs.disabled)) &&
              (this.validateField(fieldInfo.entityPath.fieldName, newVal))) {
        this.setFieldValue(this.entityBean, fieldInfo, newVal);
        this.submitFieldEdits(!this.isEditing(),false);
        FieldChangedEventArgs args = 
                            new FieldChangedEventArgs("disabled", curValue, newVal);
        this.fireFieldChanged(args);
      }
    } catch (Exception exp) {
      this.setErrorMsg(this.getClass().getSimpleName()
              + ".setDisabled Error:\n" + exp.getMessage());
    }
  }
  
  /**
   * Get the Entity's isSystemItem state - If the EntityContext contains a 
   * systemItem Field, its value will be returned if . 
   * @return the assigned isSystemItem state or 
   * {@linkplain #isParentSystemItem()} if the entity or systemItem field is undefined.
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */
  public final Boolean isSystemItem() {
    Boolean result = false;
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean != null) && ((entCtx = this.getEntityContext()) != null) &&
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.SYSTEM_ITEM)) != null)) {
      try {
        result = (Boolean) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      } catch (Exception exp) {
        result = false;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".isSystemItem Error:\n" + exp.getMessage());
      }
    } else {
      result = this.isParentSystemItem();
    }    
    return result;
  }

  /**
   * Set the Entity's SystemItem state - If the EntityContext contains a SystemItem
   * Field
   * <p><b>Note:</b> The value assignment will ignored if the FieldInfo is 
   * not defined, the field is ReadOnly, or the value not valid.</p>
   * @param newVal new value to assign
   */
  public void setSystemItem(Boolean newVal) {
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.SYSTEM_ITEM)) == null)) {
      return;
    } 
    
    newVal = (newVal == null)? false: newVal;
    Boolean curValue = null;
    try {
      curValue = (Boolean) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      if ((!DataEntry.isEq(newVal, curValue)) &&
              (this.canEditField(EntityFieldRefs.systemItem)) &&
              (this.validateField(fieldInfo.entityPath.fieldName, newVal))) {
        this.setFieldValue(this.entityBean, fieldInfo, newVal);
        this.submitFieldEdits(!this.isEditing(),true);
        FieldChangedEventArgs args = 
                            new FieldChangedEventArgs("systemItem", curValue, newVal);
        this.fireFieldChanged(args);
      }
    } catch (Exception exp) {
      this.setErrorMsg(this.getClass().getSimpleName()
              + ".setSystemItem Error:\n" + exp.getMessage());
    }
  }
  
  /**
   * Get the Entity's required UserAccess Code - If the EntityContext contains a 
   * AccessCode Field, its assigned value (assuming an integer) will be returned as
   * an {@linkplain IntFlag}.
   * @return the assigned required UserAccess Code or null if the entity or 
   * AccessCode field is undefined.
   * @exception NullPointerException if either the field name is undefined or the 
   * retrieving of the value failed.
   */  
  public IntFlag getAccessCode() {
    IntFlag result = null;    
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean != null) && ((entCtx = this.getEntityContext()) != null) &&
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.ACCESS_CODE)) != null)) {
      try {
        Long intCode = 
                    (Long) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
        if (intCode != null) {
          result = IntFlag.valueOf(intCode.intValue());
        }
      } catch (Exception exp) {
        result = null;
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".getAccessCode Error:\n" + exp.getMessage());
      }
    }
    return result;
  }
  
  /**
   * Set the Entity's Security Index - If the EntityContext contains a SecurityLevel
   * Field
   * <p><b>Note:</b> The value assignment will ignored if the FieldInfo is 
   * not defined, the field is ReadOnly, or the value not valid.</p>
   * @param newVal new value to assign
   */
  public void setAccessCode(IntFlag newVal) {
    EntityContext<TBean> entCtx = null;
    FieldInfo fieldInfo = null;
    if ((this.entityBean == null) || ((entCtx = this.getEntityContext()) == null) ||
            ((fieldInfo = entCtx.getAliasField(BwFieldAlias.ACCESS_CODE)) == null)) {
      return;
    } 
    
    BigInteger curValue = null;
    try {
      curValue = (BigInteger) ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
      if ((!DataEntry.isEq(newVal, curValue)) &&
              (this.canEditField(EntityFieldRefs.accessCode)) &&
              (this.validateField(fieldInfo.entityPath.fieldName, newVal))) {
        this.setFieldValue(this.entityBean, fieldInfo, newVal);
        this.submitFieldEdits(!this.isEditing(),false);
        FieldChangedEventArgs args = 
                            new FieldChangedEventArgs("accessCode", curValue, newVal);
        this.fireFieldChanged(args);
      }
    } catch (Exception exp) {
      this.setErrorMsg(this.getClass().getSimpleName()
              + ".setAccessCode Error:\n" + exp.getMessage());
    }
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage Parent Cache">
  /**
   * Called to reset the parent cache is defined
   */
  public void resetParentCache() {
    if (this.parentCache != null) {
      this.parentCache.clear();
    }
    this.parentCache = null;
  }
  
  /**
   * Called to reset the cached parent of a the specified parent class.
   * @param parentClass the parent wrapper's bean class
   */
  public final void clearParentCache(Class<? extends Serializable> parentClass){
    if ((parentClass != null) && (this.parentCache != null) && 
            (this.parentCache.containsKey(parentClass))) {
      this.parentCache.remove(parentClass);
    }
  }
      
  /**
   * Called to reset the cached parent of a the specified parent class.
   * @param parentWrapperClass the parent wrapper class
   */
  @SuppressWarnings("unchecked")
  private <TWrapper extends EntityWrapper<TParent>, TParent extends Serializable> 
        TWrapper getCachedParent(Class<TParent> parentClass) {
    TWrapper result = null;
    ParentCache cache = null;
    if ((parentClass != null) && (this.parentCache != null) && 
            (this.parentCache.containsKey(parentClass)) &&
            ((cache = this.parentCache.get(parentClass)) != null)) {
      result = (TWrapper) cache.parent;
    }    
    return result;
  }
  
  /**
   * Overload 1: Call Overload 2 with childField=null. Otherwise, this.facade will
   * be queried to get the {@link ForeignKey} that define the parent-child
   * relationship. If this parent is a ForeignKey of the child, 
   * {@linkplain ParentCache#isFK} will be set to true. The ForeignKeyForeignKey be
   * used to set ParentCache's isOwnerParent property.
   * <p><b>Note:</b>Previously cache entries will be overridden</p>
   * @param <TWrapper> extends EntityWrapper of type TParent
   * @param <TParent> the parent bean class
   * @param parentWrapperClass the Parent EntityWrapper Class
   * @return a EntityWrapper for the assigned parent bean
   */
  public final <TWrapper extends EntityWrapper<TParent>, TParent extends Serializable> 
          void setParent(TWrapper parentWrapper) {  
    Class<? extends Serializable> parentClass = null;
    if ((parentWrapper == null) || (parentWrapper.isNew()) || 
            ((parentClass = parentWrapper.getEntityClass()) == null)) {
      return;
    }
    
    boolean isOwner = false;
    boolean isFk = false;
    ForeignKey fKey = null;
    EntityFacade<TBean> facade = this.getFacade();
    if ((facade != null) && 
            ((fKey = facade.getParentForeignKey(parentClass)) != null)) {
      isFk = true;
      isOwner = fKey.isOwnerParent();
    }
    
    ParentCache cache = new ParentCache(parentWrapper, isFk, isOwner);
    if (this.parentCache == null) {
      this.parentCache = new HashMap<>();
    }
    this.parentCache.put(parentClass, cache);
  }  
          
  /**
   * Overload 1: Call Overload 2 with childField=null
   * @param <TWrapper> extends EntityWrapper of type TParent
   * @param <TParent> the parent bean class
   * @param parentWrapperClass the Parent EntityWrapper Class
   * @return a EntityWrapper for the assigned parent bean
   */
  public final <TWrapper extends EntityWrapper<TParent>, TParent extends Serializable> 
          TWrapper getParent(Class<TWrapper> parentWrapperClass) {
    return this.getParent(parentWrapperClass, null);
  }
  
  /**
   * Overload 2: After retrieving the <tt>parentBeanClass</tt> from the specified 
   * <tt>parentWrapper</tt>, get the Parent EntityWrapper as follows:<ul>
   * <li>if (this.isNew) - return this.puManager.getFkFilter(parentBeanClass)</li>
   * </ul>ELSE<ul>
   * <li>Return {@linkplain #getCachedParent(java.lang.Class) 
   * this.cachedParenr(parentBeanClass)} if found</li>    
   * <li><b>ELSE</b> get pkParent = this.puManager.getFkFilter(parentBeanClass), 
   * compare it to this.entity[childField] value and if pkParent match, return 
   * pkParent</li>
   * <li><b>ELSE</b> retrieve the parent bean from this.entity[childField] or if the
   * return type if not a parentBean, assume it is the parent bean ID and use the parent 
   * facade to retrieve the parentBean. If the parent bean is located, initiate
   * and return the Parent Wrapper.</li>
   * <li><b>ELSE</b> return null.</li>
   * </ul> 
   * <p>If the parent Wrapper is found, retrieve the ForeignKey Info from the facade
   * and is the parent is a foreignKey with {@link AssocationDef#cache
   * ForeignKey.parentAssociation.cache} call {@linkplain #setParent(
   * bubblewrap.entity.core.EntityWrapper) this.setParent} to cache the parent.
   * <p>If the childField = null, retrieve the ForeignKey Info from the facade to
   * resolve the childField. Throw and exception if the field cannot be resolved.
   * <p><b>Note:</b> All exceptions are trapped and logged and null is returned.</p>
   * @param <TWrapper> extends EntityWrapper of type TParent
   * @param <TParent> the parent bean class
   * @param parentWrapperClass the Parent EntityWrapper Class
   * @param childField (optional) Can be set to retrieve a Parent for FkFilter 
   * referenced parent if the the child bean support a field other than the ForeignKey
   * field to return a reference to a Parent Bean (instead of the bean's RecordId).
   * @return a EntityWrapper for the assigned parent bean
   */
  @SuppressWarnings("unchecked")
  public final <TWrapper extends EntityWrapper<TParent>, TParent extends Serializable> 
          TWrapper getParent(Class<TWrapper> parentWrapperClass, String childField) {
    TWrapper result = null;
    try {
      if (parentWrapperClass == null) {
        throw new NullPointerException("The parent EntityWrapper Class is undefined");
      }    
      Class<TParent> parentBeanClass = 
                                      EntityWrapper.getEntityClass(parentWrapperClass);
      if (parentBeanClass == null) {
        throw new Exception("Unable to retrieve the Bean Class from EntityWrapper "
                + "Class[" + parentWrapperClass.getSimpleName() + "].");
      }
      
      PuEntityManager puMngr = this.getPuManager();
      if (this.isNew()) {
        result = (TWrapper) puMngr.getPkFilter(parentBeanClass);
      } else if ((result = this.getCachedParent(parentBeanClass)) == null) {
        EntityFacade<TBean> facade = this.getFacade();
        ForeignKey parentFk = facade.getParentForeignKey(parentBeanClass);        
        TWrapper pkParent = (TWrapper) puMngr.getPkFilter(parentBeanClass);
        if (parentFk != null) {        
          childField  = parentFk.childAssociation.targetPath.fieldName;
        } else {
          childField = DataEntry.cleanString(childField);
        }        

        if (childField == null) {
          throw new Exception("Invalid use of method. Parent Class["
              + parentBeanClass.getSimpleName() + "] is non-persistent parent of "
              + "Class[" + this.getEntityClass().getSimpleName() + "] and the "
              + " field or method to retrieve the parent reference is not defined.");
        }

        FieldInfo childFieldInfo = this.getEntityContext().getFieldInfo(childField);
        if (childFieldInfo == null) {
          throw new Exception("Unable to extract the FieldInfo for ChildField["
                  + childField + "].");
        }
          
        TParent myParentBean = null;
        TParent pkParentBean = (pkParent == null)? null: pkParent.getEntity();
        
        if (parentBeanClass.equals(childFieldInfo.returnType)) {
          myParentBean = ReflectionInfo.getFieldValue(this.getEntity(), childField);
        } else {
          Serializable parentId = 
                          ReflectionInfo.getFieldValue(this.getEntity(), childField);
          if (parentId != null) {
            EntityFacade<TParent> parentFacade = puMngr.getFacade(parentBeanClass);
            myParentBean = parentFacade.find(parentId);
          }          
        }
        
        if (myParentBean != null) {
          if (DataEntry.isEq(myParentBean, pkParentBean)) {
            result = pkParent;
          } else {
            result = EntityWrapper.newFromBean(parentWrapperClass, myParentBean);
          }
        }
        
        if ((result != null) && (parentFk != null) && 
                                              (parentFk.parentAssociation.cache)) {
          this.setParent(result);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getParent Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to checked if this.parentCache contains an ownerParent
   * @param <TParent> parent EntityWrapper class
   * @return the parent if found, else null
   */
  @SuppressWarnings("unchecked")
  protected final <TParent extends EntityWrapper> TParent getOwnerParent() {
    TParent result = null;
    try {
      if ((this.parentCache != null) && (!this.parentCache.isEmpty())) {
        for (ParentCache cache : this.parentCache.values()) {
          if (cache.isOwner) {
            result = (TParent) cache.parent;
            break;
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.result Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }  
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage Child Collection Cache">  
  protected <TWrapper extends EntityWrapper<TChild>, TChild extends Serializable>
                  Collection<TWrapper> getChildCollection(Class<TWrapper> childClass) {
    return this.getChildCollection(childClass, null);
  }
                  
  /**
   * Call to retrieve the previously cached and sorted ForeignKey Child Wrapper 
   * Collection. If not yet cached, the new collection will be initiated sorted and
   * cached before it is returned. Sorting is ignored of the <tt>comparator</tt> = null.
   * <p>
   * If this.facade contains {@link ForeignKeyConstraint} for the child, and this
   * ForeignKeyrParent of the child, and the doCachParent is set, it will
   * assign this wrapper as the parent of each child. Also, if the doCachChildren
   * flag is set it will cache the child collection.
   * <p><b>Note:</b>All error are trapped and logged</p>
   * @param <TWrapper> the Child Wrapper class
   * @param <TChild> the Child Bean class
   * @param childClass the Child Wrapper class
   * @param comparator the EntityWrapperComparator for sorting the child collection.
   * @return a list of List child wrappers or null is none is defined or an error
   * occurred
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected <TWrapper extends EntityWrapper<TChild>, TChild extends Serializable>
                  Collection<TWrapper> getChildCollection(Class<TWrapper> childClass,
                  EntityWrapperComparator<TWrapper> comparator) {
    Collection<TWrapper> result = null;
    try {
      Class<TChild> childBeanClass = null;
      if ((childClass == null) ||
              ((childBeanClass = EntityWrapper.getEntityClass(childClass)) == null)) {
        throw new Exception("The Specified Child Wrapepr Class or it associated Entity "
                + "Class is unassigned or could not be resolved.");
      }
      if ((this.childCollectionCache != null) &&
              (this.childCollectionCache.containsKey(childBeanClass))) {
        result = (List<TWrapper>) this.childCollectionCache.get(childBeanClass);
      } else if (childBeanClass != null) {
        Collection<TChild> childBeans
                = ReflectionInfo.getChildCollection(this.getEntity(), childBeanClass);
        if ((childBeans != null) && (!childBeans.isEmpty())) {
          List<TWrapper> childList = new ArrayList<>();
          try {
            for (TChild pBean : childBeans) {
              TWrapper childWrapper = 
                            (TWrapper) EntityWrapper.newFromBean(childClass, pBean);
              if (childWrapper != null) {
                childList.add(childWrapper);
              }
            }
            
            if (comparator != null) {
              try {
                Collections.sort(childList, comparator);
              } catch (Exception exp2) {
                logger.log(Level.WARNING, "{0}.initChildCollection.sort Error:\n {1}",
                        new Object[]{this.getClass().getSimpleName(), exp2.getMessage()});
              }
            }
          } catch (Exception innerExp) {
            childList.clear();
            logger.log(Level.WARNING, "{0}.initChildCollection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), innerExp.getMessage()});
          }
          result = childList;
        }
        
        EntityFacade<TBean> facade = null;
        ForeignKey childFk = null;
        if ((result != null) && ((facade = this.getFacade()) != null) &&
              ((childFk = facade.getChildForeignKey(childBeanClass)) != null)) {
          if ((!result.isEmpty()) && (childFk.isOwnerParent())) {
            for (TWrapper child : result) {
              child.setParent(this);
            }
          }
          
          if (childFk.childAssociation.cache) {
            this.setChildCollection(childBeanClass, result);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getCachedChildCollection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
   
  /**
   * Called to assign the <tt>chidlCollection</tt> to this.childCollectionCache.
   * Null or empty collection will be ignored. Prior cached entries will be
   * overridden.
   * @param <TWrapper> the wrapper class
   * @param <TChild> the wrapper's Bean class
   * @param childCollection the collection of Child Wrappers
   */
  @SuppressWarnings("unchecked")
  public final <TWrapper extends EntityWrapper<TChild>, TChild extends Serializable>
        void setChildCollection(Collection<TWrapper> childCollection) {
    if ((childCollection != null) && (!childCollection.isEmpty())) {
      try {        
        Class<TChild> childBeanClass = null;
        for (EntityWrapper child : childCollection) {
          if (child != null) {
            childBeanClass =  child.getEntityClass();
            break;
          }
        }
        
        if (childBeanClass == null) {
          throw new Exception("Unable retieve the ChildBean class from the child "
                  + "collection");
        }
        
        this.setChildCollection(childBeanClass, childCollection);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.setChildCollection Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  
  /**
   * Called to assign the childCollection to this.childCollectionCache
   * @param <TWrapper> the wrapper class
   * @param <TChild> the wrapper's Bean class
   * @param childBeanClass  the wrapper's Bean class
   * @param childCollection the collection of Child Wrappers
   */
  protected final <TWrapper extends EntityWrapper<TChild>, TChild extends Serializable>
                void setChildCollection(Class<TChild> childBeanClass,
                Collection<TWrapper> childCollection) {
    if ((childCollection != null) &&
                          (childCollection != null) && (!childCollection.isEmpty())) {
      if (this.childCollectionCache == null) {
        this.childCollectionCache = new HashMap<>();
      }
      this.childCollectionCache.put(childBeanClass, childCollection);      
    }
  }
                
  /**
   * Called to clear this.childCollectionCache
   */
  public final void clearAllCachedChildCollection() {
    if (this.childCollectionCache != null) {
      this.childCollectionCache.clear();
      this.childCollectionCache = null;
    }
  }
  
  /**
   * Called to clear this.childCollectionCache for the specified Wrapper class.
   * @param <TWrapper> the wrapper class
   * @param <TChild> the wrapper's Bean class
   * @param childClass the specified Wrapper class
   */
  public final <TWrapper extends EntityWrapper<TChild>, TChild extends Serializable>
          void clearCachedChildCollection(Class<TWrapper> childClass) {
    Class<TChild> childBeanClass = null;
    if ((this.childCollectionCache != null) && 
              (!this.childCollectionCache.isEmpty()) && (childClass != null) &&
            ((childBeanClass = EntityWrapper.getEntityClass(childClass)) != null) &&
            (this.childCollectionCache.containsKey(childBeanClass))) {
      this.childCollectionCache.remove(childBeanClass);
    }
  }
                                    
  /**
   * Called by {@linkplain #refreshRecord() this.refreshRecord} after successfully
   * refreshing this.entity to sync this wrapper's entity with that of all its cached
   * parents.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void syncParentChildCollections() {
    try {
//      EntityFacade<TBean> facade = null;
//      if ((this.parentCache != null) && (!this.parentCache.isEmpty()) &&
//              ((facade = this.getFacade()) != null)) {
//        for (EntityWrapper parent : this.parentCache.values()) {
//          facade.syncParentChildEntity(parent, this);
//        }
//      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.syncParentFkCollections Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
       
  /**
   * Called by {@linkplain #resetRecord(java.io.Serializable, boolean) this.resetRecord}
   * to refreshed all cached child Wrapper COllection with their associated entity
   * bean collection.  It cycles through this.syncAllCachedChildCollections and
   * calls {@linkplain #syncCachedChildCollection(java.lang.Class, java.util.Collection,
   * java.io.Serializable) this.syncCachedChildCollection} for each cached collection.
   */
  private void syncAllCachedChildCollections() {
    try {
      if ((this.childCollectionCache == null) || 
                                              (this.childCollectionCache.isEmpty())) {
        return;
      }
      
      for (Class<? extends Serializable> childBeanClass : 
                                                  this.childCollectionCache.keySet()) {
        Collection<? extends Serializable> childBeans
                = ReflectionInfo.getChildCollection(this.getEntity(), childBeanClass);
        this.syncCachedChildCollection(childBeanClass, childBeans, null);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.syncAllCachedChildCollections Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called by {@linkplain #syncAllCachedChildCollections 
   * this.syncAllCachedChildCollections} or {@linkplain 
   * EntityFacade#syncParentChildEntity(bubblewrap.entity.core.EntityWrapper, 
   * bubblewrap.entity.core.EntityWrapper) EntityFacade.syncParentChildEntity} to
   * to sync the cached Child Wrapper Collection's Entities with this parent refreshed
   * list of entities for the specified.
   * <p>If a cached wrapper's match entity is found, it calls the child wrappers
   * {@linkplain #resetRecord(java.io.Serializable, boolean) 
   * this.resetRecord(newBean, resetParent=false)} to update the child wrapper's bean.
   * @param childBeanClass the child collection's entityBean class
   * @param childCol this parent bean's collection of child beans
   * @param excludeBean a child bean to exclude from the update
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public void syncCachedChildCollection(
            Class<? extends Serializable> childBeanClass, 
            Collection<? extends Serializable> childCol, Serializable excludeBean) {
    try {
      if ((childBeanClass != null) && (this.childCollectionCache != null) &&
          (this.childCollectionCache.containsKey(childBeanClass))) {
        Collection<? extends EntityWrapper> childWrapperList = null;
        if ((childCol == null) || (childCol.isEmpty())) {
          this.childCollectionCache.remove(childBeanClass);
        } else if ((childWrapperList = this.childCollectionCache.get(childBeanClass))
                    != null) {
          for (EntityWrapper childWrapper : childWrapperList) {
            Serializable oldBean = childWrapper.getEntity();
            if ((oldBean == null) || 
                    ((excludeBean != null) && (excludeBean.equals(oldBean)))) {
              continue;
            }
            
            for (Serializable newBean : childCol) {
              if (oldBean.equals(newBean)) {
                childWrapper.resetRecord(newBean, false);
                break;
              }
            }
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.syncCachedChildCollection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * <p>Called to remove a Child from the ChildCollection without Deleting the Child. It
   * updates this Entity ChildCollection and resets the Child's Field[sChildFKField].
   * If successful, it calls both this EntityWrapper and the Child's submitEdits(!Editing).
   * </p>
   * <p>This process is skipped if the ChildCollection cannot be found (i.e. using the
   * FacadeHelper.getChildCollection method) or the collection does not contains 
   * pChild.entity, or sChildFKField is not found in pChild.entity (i.e., the
   * FacadeHelper.setFieldValue failed), or when any other error occur. All errors are
   * logged. No exception is thrown.</p>
   * @param <TChild extends Serializable>
   * @param child the child to reset
   */
  @SuppressWarnings("unchecked")
  public final <TChild extends Serializable> void 
                                   removeChild(EntityWrapper<TChild> child) {
    if (child == null) {
      return;
    }
    
    try {
      TChild childBean = child.getEntity();
      Class<TChild> childClass = child.getEntityClass();
      
      EntityFacade<TBean> facade = this.getFacade();
      ForeignKey childFk = null;
      if ((facade == null) || ((facade.getChildForeignKey(childClass)) == null)) {
        throw new Exception("Entity[" + this.getEntityClass().getSimpleName() 
                    + "] does not have any registered Child ForeignKey for "
                    + "child Class[" + childClass.getSimpleName() + "].");
      }
              
      String childField = childFk.childAssociation.targetPath.fieldName;
      Collection<TChild> childCol = 
                      ReflectionInfo.getChildCollection(this.getEntity(), childClass);
      if ((childCol != null) && (childCol.contains(childBean))) {
        childCol.remove(childBean);        
        ReflectionInfo.setChildCollection(this.getEntity(), childClass, childCol);
        this.submitEdits(!this.isEditing());
      }
      
      child.clearParentCache(this.getClass());
      ReflectionInfo.setFieldValue(childBean, childField, null);
      child.submitEdits(!child.isEditing());
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.removeChild Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Access Validation Event Handling">
  /**
   * Get the EntityWrapper's applicable EntityValidation Settings
   * @return this.entityContext.entityValidation(this.class)
   */
  protected EntityValidation getEntityValidation() {
    EntityContext<TBean> entCtx = this.getEntityContext();
    return entCtx.getEntityValidation(this.getClass());
  }
  
  /**
   * Get the whether any access validation is required for this entity - as defined 
   * by {@linkplain #getEntityValidation() this.entityValidation} and 
   * this.facade's isInitiating state.
   * @return (not this.facade.isInitiating) and this.entityValidation.doValidation.
   */
  protected final boolean doAccessValidation() {
    EntityFacade<TBean> facade = this.getFacade();      
    EntityValidation validation = this.getEntityValidation();
    return ((!facade.isInitiating()) && (validation.doValidation));
  }
  
  /**
   * Get the Entity Default access/action AppTask
   * @return this.entityContext.entityValidation(this.class).appTask.
   */
  protected final AppTasks getAppTask() {
    EntityValidation validation = this.getEntityValidation();
    return validation.appTask;
  } 
  
  /**
   * Get the whether user has access to read/view a record .
   * @return {@linkplain #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks, 
   * bubblewrap.navigation.enums.AppActions, java.lang.String, boolean) 
   * this.hasTaskActionAccess(null, AppActions.READ, null, false)}
   */
  public final boolean allowRead() {    
    return this.hasTaskActionAccess(null, AppActions.READ, null, false);
  }
  
  /**
   * Get the whether user has access to edit the record (or some fields of the record).
   * @return {@linkplain #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks, 
   * bubblewrap.navigation.enums.AppActions, java.lang.String, boolean) 
   * this.hasTaskActionAccess(null, AppActions.EDIT, null, false)}
   */
  public final boolean allowEdits() {    
    return this.hasTaskActionAccess(null, AppActions.EDIT, null, false);
  }  
  
  /**
   * Get the whether user has access to delete this record.
   * @return {@linkplain #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks, 
   * bubblewrap.navigation.enums.AppActions, java.lang.String, boolean) 
   * this.hasTaskActionAccess(null, AppActions.DELETE, null, false)}
   */
  public final boolean allowDelete() {    
    return this.hasTaskActionAccess(null, AppActions.DELETE, null, false);
  }  
  
  /**
   * Get the whether user has access to add a new record .
   * @return {@linkplain #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks, 
   * bubblewrap.navigation.enums.AppActions, java.lang.String, boolean) 
   * this.hasTaskActionAccess(null, AppActions.ADD, null, false)}
   */
  public final boolean allowAdd() {    
    return this.hasTaskActionAccess(null, AppActions.ADD, null, false);
  } 
  
  /**
   * <p>This method is called by FacePages to validate the permission to edit a specific
   * field.  It resolve as follows:</p> <ul>
   * <li><b>Is this.isNew:</b> return true</li>
   * <li><b>If fieldName="recordId":</b> return false - never editable</li>
   * <li><b>Else:</b> Set appAction=EDIT or [RENAME,MOVE,DISABLE] if fieldName = 
   * [recordName,displayIdx,disabled], respectively and return the result from a call
   * to {@linkplain #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks, 
   * bubblewrap.navigation.enums.AppActions, java.lang.String, boolean) 
   * this.hasTaskActionAccess(appTask=null,appAction,fieldName,setErr=false}.
   * </li>
   * </ul>
   * <p><b>Note:</b> To custom handle the field access validation override the
   * {@linkplain #onValidateField(java.lang.String, java.lang.Object) onValidateField}
   * method.</p>
   * @param fieldName the field to validate
   * @return true if the field can be edit
   */
  public final boolean canEditField(String fieldName) {
    boolean result = true;
    if (!this.isNew()) {
      fieldName = DataEntry.cleanString(fieldName);
      AppActions appAction = AppActions.EDIT;
      if ((fieldName != null) && 
              (fieldName.equalsIgnoreCase(EntityFieldRefs.recordId))) {
        result = false;
      } else {        
        if (fieldName.equalsIgnoreCase(EntityFieldRefs.recordName)) {
          appAction = AppActions.RENAME;
        } else if (fieldName.equalsIgnoreCase(EntityFieldRefs.displayIdx)) {
          appAction = AppActions.MOVE;
        } else if (fieldName.equalsIgnoreCase(EntityFieldRefs.disabled)) {
          appAction = AppActions.DISABLE;
        } 
        result = this.hasTaskActionAccess(null, appAction, fieldName, false);
      }
    }
    return result;
  }
  
  /**
   * Overload 1. Call the private {@linkplain #hasTaskActionAccess(
   * bubblewrap.navigation.enums.AppTasks, bubblewrap.navigation.enums.AppActions, 
   * java.lang.String, boolean) Overload 2} with stErr = true
   * @param appTask the appTask to validate the request access for 
   * (type {@linkplain AppTasks}). Can be null to default to this.appTask.
   * @param action the specific action (type {@linkplain AppActions})
   * @param fieldName (optional) field name.
   * @return true if access if granted. 
   */
  protected final boolean hasTaskActionAccess(AppTasks appTask, AppActions action, 
          String fieldName) {
    return this.hasTaskActionAccess(appTask, action, fieldName, true);
  }
  
  /**
   * <p>Overload 2. Call to validate a user's access to a specified appTask, appAction, 
   * and optional field reference. This call is handled as follows:</p><ul>
   * <li>If (appTask=null, set appTask=this.appTask and if null, deny access and log 
   *  an error message.</li>
   * <li>Initiate a AccessValidationEventArgs argument (args) and call {@linkplain 
   * #validateAccess(bubblewrap.core.events.AccessValidationEventArgs) 
   * this.validateAccess(args)} to handle the request. If access is denied, get the
   * reason for denial and assign its this.errorMsg and log the message.
   * </li></ul>
   * @param appTask the appTask to validate the request access for 
   * (type {@linkplain AppTasks}). Can be null to default to this.appTask.
   * @param action the specific action (type {@linkplain AppActions})
   * @param fieldName (optional) field name.
   * @param setErr if true, any trapped errors and a reason for denying access will
   * be assigned to this,errorMsg. Otherwise errors are denied and access is denied.
   * @return true if (!this.doAccessValidation) or this.args.hasAccess=true. 
   */
  private boolean hasTaskActionAccess(AppTasks appTask, AppActions action, 
          String fieldName, boolean setErr) {
    boolean result = true;
    try {
      if (appTask == null) {
        appTask = this.getAppTask();
        if (appTask == null) {
          throw new Exception("User Access this Entity's cannot be determined because "
                  + "it's AppTask is unassigned. Check the entity's AccessValidation "
                  + "annotation settings.");
        }
      }

      fieldName = DataEntry.cleanString(fieldName);
      AccessValidationEventArgs args = 
                    new AccessValidationEventArgs(appTask, action, null, fieldName);
      this.validateAccess(args);
      if (!args.hasAccess()) {
        String reason = args.getDenyReason();

        reason = (reason == null)? "Access denied for no specied reason": reason;
        throw new Exception("Access to Request[" +args.toString() + "] was denied "
                + "because:\n " + reason);
      }        
    } catch (Exception exp) {
      result = false;
      if (setErr) {
        this.setErrorMsg(exp.getMessage());
      }
    }
    return result;
  }

  /**
   * <p>A public method called to validate the current user's access to this bean's
   * content for the specified Access Validation Arguments (i.e., AppTask, AppAction,
   * AccessCode and and FieldName). It is also internally called by {@linkplain 
   * #hasTaskActionAccess(bubblewrap.navigation.enums.AppTasks,
   * bubblewrap.navigation.enums.AppActions, java.lang.String)  
   * this.hasTaskActionAccess} to process the request.</p>
   * This method is using the following approach:</p><ul>
   *  <li>If (this.facade != null) and (facade.isInitiating) grant access to any action
   *  </li>
   *  <li>If (this.isSystemItem) deny access for any Add, or Delete Action.</li>
   *  <li>If (!facade.isInitiating) check is the facade(the entity) allows Edit, Add, 
   *    or Delete and deny access if the requested action is not allowed.</li>
   *  <li>Check this.entityValidatio and deny access if (action.isAdd and 
   *  (!allowAdd)) or (action.isEdit and (!allowEdits)) or (action.isDelete and 
   *  (!allowDelete))</li>
   *  <li>If (this.doAccessValdiation), check if {@linkplain #getAccessValidator() 
   *    this.accessValidator} is assigned, assign args.accessCode=this.accessCode
   *    and call its {@linkplain  IAccessValidator#validateAccess(
   *    bubblewrap.admin.events.ValidateAccessRequestArgs) validateAccess(reqArgs)} 
   *    method to handle the request.</li>
   *  <li>>If (!args.isHandled), call :<ul>
   *    <li>if args.fieldName != null, get the fieldInfo and call its {@linkplain 
   *    FieldInfo#allowEdits(java.lang.Class, java.lang.Boolean)  FieldInfo.allowEdits}
   *    method to validate edit access on the field. Deny access if not.</li> 
   *    <li>Finally, is not handled call the protected {@linkplain #onValidateAccess(
   *    bubblewrap.admin.events.ValidateAccessRequestArgs) onValidateAccess(args)} 
   *    method to handle the validation locally.</li>
   *  </ul> </li>
   *  <li>If an Exception is throws, access is denied and the error message is 
   *    assigned as the reason for the denial. The error is also logged.</li>
   * </ul>
   * @param args the validate request arguments
   */
  public final void validateAccess(AccessValidationEventArgs args) {
    if (args == null) {
      return;
    }
        
    try {
      /*
       * Allow any action if this.facade.isInitiating.
       */
      EntityFacade<TBean> facade = this.getFacade();
      if ((facade != null) && (facade.isInitiating())) {
        args.grantAccess();
        return;
      }
      
      AppActions action = args.getAction();
      if (action == null) {
        throw new Exception("The Request's AppAction is unassigned.");
      }
      
      /**
       * Deny access if this.isSystemItem and action.isAdd or action.isDelete.
       * Permission for editing systemItem fields are set by field (custom)
       */
      if ((this.isSystemItem()) && 
          ((action.isAdd()) || (action.isDelete()))) {
        args.denyAccess(this.toString() + " is a SystemItem and cannot be added "
                + "or deleted.");
        return;
      }
      
      EntityValidation validation = this.getEntityValidation();
      if ((action.isAdd()) && (!validation.allowAdd())) {
        args.denyAccess("The Entity's Validation Settings do not all adding of "
                + "new Entities.");
      } else if ((action.isEdit()) && (!validation.allowEdits())) {
        args.denyAccess("The Entity's Validation Settings do not all editing of "
                + "Entities.");
      } else if ((action.isDelete()) && 
                                  (!this.isNew()) && (!validation.allowDelete())) {
        args.denyAccess("The Entity's Validation Settings do not all deleting of "
                + "existing Entities.");
      }
      if (!args.hasAccess()) {
        return;
      }
      boolean retry = false;
      /**
       * If (validation.doValidation), assign the accessCode, call the accessValdiator's
       * validateAccess method to validate that the user has access to record.
       */
      if (validation.doValidation) {
        this.onInitAccessCode(args);
        IntFlag accessCode = null;
        if ((!args.hasAccess()) && ((accessCode = this.getAccessCode()) != null)) {
          args.setAccessCode(accessCode);      
        }

        IAccessValidator validator = this.getAccessValidator();
        if (validator != null) {
          validator.validateAccess(this, args);        
        }
      }

      /**
       * If not handled: and a fieldName is assigned and the field's 
       * FieldInfo.allowEdits = false, deny access.
       * ELSE call this.onValidateAccess(args) to check if access is allowed.
       */
      if ((!args.isHandled()) && (args.hasAccess())) {
        String fieldName = args.getFieldName();
        EntityContext<TBean> entCtx = null;
        FieldInfo fieldInfo = null;
        if ((fieldName != null) && ((entCtx = this.getEntityContext()) != null) &&
                ((fieldInfo = entCtx.getFieldInfo(fieldName)) != null) && 
                (!fieldInfo.allowEdits(this.getClass(), this.isSystemItem()))) {
          args.denyAccess("The Entity's EditAccess Settings do not all editing of"
                  + " Field[" + fieldName + "].");
        }
      }
      
      if ((!args.isHandled()) && (args.hasAccess())) {
        this.onValidateAccess(args);
      }
    } catch (Exception exp) {
      args.denyAccess(exp.getMessage());
      logger.log(Level.WARNING, "{0}.ValidateAccess[{1}] Error:\n\t {2}",
              new Object[]{this.getClass().getSimpleName(), args.toString(), 
                exp.getMessage()});
    }
  }
  
  /**
   * <p>CAN OVERRIDE: Called by {@link #validateAccess(
   * bubblewrap.core.events.AccessValidationEventArgs) this.validateAccess} to initiate
   * the AccessCode of the <tt>reqArgs</tt> before checking the records accessCode 
   * field and assign the value (if defined). The field value is ignored if this 
   * method assign the reqArgs.accessCode</p>
   * <p>The base method does nothing - leaving the validation to the calling method.</p>
   * <p><b>Note:</b> A local handling of the event should only deny access if 
   * applicable, because the user has access by default.</p>
   * @param pArgs the AccessValidationEventArgs passed from the caller
   */
  protected void onInitAccessCode(AccessValidationEventArgs reqArgs) {
  }

  /**
   * <p>CAN OVERRIDE: Called by {@link #validateAccess(
   * bubblewrap.core.events.AccessValidationEventArgs) this.validateAccess} to handle 
   * the access validation locally AFTER requesting this.accessValidator to handle the 
   * request and the argument return unhandled.</p>
   * <p>The base method does nothing - leaving the validation to the calling method.</p>
   * <p><b>Note:</b> A local handling of the event should only deny access if 
   * applicable, because the user has access by default.</p>
   * @param pArgs the AccessValidationEventArgs passed from the caller
   */
  protected void onValidateAccess(AccessValidationEventArgs reqArgs) {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Entity State Management">
  /**
   * CAN OVERRIDE: Called by isSystemItem to check if it's parent is a 
   * SystemItem, which implies that this item is a SystemItem too. 
   * default = false.
   * @return Boolean
   */
  protected boolean isParentSystemItem() {
    return false;
  }

  /**
   * Return true if this is a new record (ie.e., RecordID == null)
   * @return Boolean
   */
  public final boolean isNew() {
    return (this.getRecordId() == null);
  }
  // </editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="RecordAction Methods">  
  /**
   * <p>Called by the recordAction BWCompontent to get a list of value of {@linkplain 
   * AppActions} supported for a defined <tt>entityKey</tt> (typically 
   * representing a child or linked entity) based on specific conditions.</p> 
   * <p>Exceptions are logged and the return will be null.</p>
   * @param entityKey the key for child or link entity
   * @return list of actionIds supported by this record or null if entityKey=null|"".
   */
  public final List<Integer> getRecordActions(String entityKey) {
    List<Integer> result = null;
    try {
      entityKey = DataEntry.cleanString(entityKey);
      List<AppActions> actionList = null;
      if ((entityKey != null) && 
              ((actionList = this.onGetRecordActions(entityKey)) != null) &&
              (!actionList.isEmpty())) {
        result = new ArrayList<>();
        for (AppActions appAction : actionList) {
          result.add(appAction.getValue().intValue());
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getRecordActions Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return ((result == null) || (result.isEmpty()))? null: result;
  }
  
  /**
   * <p>CAN BE OVERRIDE: Called by getShowAction to validate whether a control for a
   * defined eAction and entityKey should be displayed. Base method returns null.</p>
   * @param entityKey the key for child or link entity
   * @return list of AppActions supported by this record.
   * @throws Exception
   */
  protected List<AppActions> onGetRecordActions(String entityKey)
          throws Exception {
    return null;
  }
  
  /**
   * Called by the RecordAction EzCompontent to execute the record action request.
   * This method returns null if sEntity=null|"" or else returns the result from
   * this.onExecRecordAction. Exceptions are logged and assigned a this.errorMsg.
   * It returns null is an error occurred.
   * @param actionId the {@linkplain AppActions}.value 
   * @param entityKey the key for child or link entity
   * @return String
   */
  public final String execRecordAction(int actionId, String entityKey) {
    String result = null;
    try {
      /* Respond to a Refresh Page action */
      AppActions action = AppActions.findByValue(actionId);                      
//      NavigationContext navCtx = NavigationContext.doLookup();
//      if ((navCtx.isRefreshSubmitValues()) || (action == null) ||
//              ((entityKey = DataEntry.cleanString(entityKey)) == null)) {
//        result = navCtx.getCurrentPageUrl();
//      } else {
        result = this.onExecRecordAction(action, entityKey);
      //}
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.execRecordAction Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * CAN BE OVERRIDE: Called by execRecordAction to execute the record action request.
   * The base method does nothing and returns null.
   * @param action the AppAction to execute
   * @param entityKey the key for child or link entity
   * @return the url to redirect the navigation too or null to return to the current
   * FacePage
   * @throws Exception
   */
  protected String onExecRecordAction(AppActions action, String sEntityKey)
          throws Exception {
    return null;
  }
  
  /**
   * Called by the RecordAction EzCompontent get the Action tip (title). Return null
   * if sEntity=null|"" or sResult=this.onGetRecordActionTip. If sResult = null, the 
   * method returns a generic tip= "Click to " +eAction.name+ " the record".
   * Exceptions are logged and the return will be null.
   * @param actionId the {@linkplain AppActions}.value 
   * @param entityKey the key for child or link entity
   * @return return a tip to display with the control
   */
  public final String getRecordActionTip(int actionId, String entityKey) {
    String result = null;
    try {
      AppActions action = AppActions.findByValue(actionId); 
      if (((entityKey = DataEntry.cleanString(entityKey)) != null) && 
                                                                 (action != null)) {
        result = DataEntry.cleanString(this.onGetRecordActionTip(action, entityKey));
        if (result == null) {
          String actionName = 
                  (action.equals(AppActions.READ))? "view": action.name().toLowerCase();
          result = "Click to " + actionName + " the record";
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getRecordActionTip Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * <p>CAN BE OVERRIDE: Called by getRecordActionTip to get the Action tip (title).
   * The base method returns null.</p>
   * @param action the AppAction to execute
   * @param entityKey the key for child or link entity
   * @return return a tip to display with the control
   * @throws Exception
   */
  protected String onGetRecordActionTip(AppActions action, String entityKey)
          throws Exception {
    return null;
  }
  
  //getRecordActionImage
  /**
   * Called by the RecordAction EzCompontent to get the Action Image to display in the 
   * actionBar. Set sResult=this.onGetRecordActionImage. If sResult = null, the method 
   * returns a generic image name= action.name+ ".gif".
   * Exceptions are logged and the return will be null.
   * @param actionId the {@linkplain AppActions}.value 
   * @return the image name
   */
  public final String getRecordActionImage(int actionId) {
    String result = null;
    try {
      AppActions action = AppActions.findByValue(actionId); 
      if (action != null) {
        result = DataEntry.cleanString(this.onGetRecordActionImage(action));
        if (result == null) {
          result = action.name() + ".gif";
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getRecordActionImage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * CAN BE OVERRIDE: Called by getRecordActionImage to get the Action Image to display 
   * in the actionBar. The base method returns null.
   * @param action the AppAction to get the image name for
   * @return the image name
   * @throws Exception
   */
  protected String onGetRecordActionImage(AppActions action)
          throws Exception {
    return null;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="PrimaryKey/ForeignKey Management">
  /**
   * <p>Assign this instance as the PrimaryKey Filter (for ForeignKeys related to
   * this class) be calling this.facade.setPkFilter(this). 
   * Throws an IllegalArgumentException if this.IsNew().</p>    
   * <p><b>NOTE:</b> This call will trigger a call to afterSetAsPkFilter and calls to
   * onSetAsPkFilter for each ForeignKey calls links to this EntityWrapper as defined
   * in its EntityContext class.</p>
   */
  @SuppressWarnings("unchecked")
  public final void setAsPkFilter() {
    if (this.isNew()) {
      throw new IllegalArgumentException(this.toString() + " isNew/Unsaved and cannot "
              + "be a Entity Filter");
    }
    EntityFacade<TBean> facade = this.getFacade();      
    facade.setPkFilter(this);
  }

  /**
   * <p>Reset the EntityFacade if and only if the EntityFacade's PrimaryKey Filter is
   * this instance.  Ignored if this.isNew().</p>
   * <p><b>NOTE:</b> This call will trigger a call to beforeResetAsPkFilter and 
   * calls to onResetAsPkFiler for each ForeignKey calls links to this EntityWrapper
   * as defined in its EntityContext - if this instance was the assigned parent  
   * for any of these filters.</p>
   */
  @SuppressWarnings("unchecked")
  public final void resetAsPkFilter() {
    if (this.isNew()) {
      return;
    }

    EntityFacade<TBean> facade = this.getFacade();      
    if (facade.isPkFilterRecord(this)) {
      facade.resetPkFilter();
    }
  }

  /**
   * Check if this instance is the class' assigned PpriamryKey Filter
   * @return (!isNew) and (facade.isPkFilter(this).
   */
  @SuppressWarnings("unchecked")
  public final boolean isPkFilter() {
    boolean result = (!this.isNew());
    if (result) {
      EntityFacade<TBean> facade = this.getFacade();      
      result = facade.isPkFilter(this);
    }
    return result;
  }
  
  /**
   * Called by the {@link EntityFacade#setPkFilter(bubblewrap.entity.core.EntityWrapper) 
   * EntityFacade.setPkFilter()} or {@link EntityFacade#resetPkFilter()} BEFORE 
   * resetting/changing Facade's PrimaryKey Filter and if this instance is the Facade's
   * current PrimaryKey. It calls {@link #onBeforeResetAsPkFilter()} to handle the 
   * event. All exceptions are trapped and logged.
   */
  public final void beforeResetAsPkFilter() {  
    try {
      this.onBeforeResetAsPkFilter();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.beforeResetAsPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * CAN OVERRIDE: Called by {@link #afterSetAsPkFilter()}, which is called by 
   * EntityFacade.setPkFilter() when this instance was assigned as the EntityFacade's
   * PrimaryKey Filter. Base method does nothing.
   * @throws Exception 
   */
  protected void onBeforeResetAsPkFilter() throws Exception {
  }
  
  /**
   * Called by the {@link EntityFacade#setPkFilter(bubblewrap.entity.core.EntityWrapper)
   * EntityFacade.setPkFilter()} when this instance was assigned as EntityFacade's 
   * PrimaryKey Filter. It calls {@link #onAfterSetAsPkFilter()} to handle the event. 
   * All exceptions are trapped and logged.
   */
  public final void afterSetAsPkFilter() {  
    try {
      this.onAfterSetAsPkFilter();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.afterSetAsPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * CAN OVERRIDE: Called by {@link #afterSetAsPkFilter()}, which is called by 
   * EntityFacade.setPkFilter() when this instance was assigned as the EntityFacade's
   * PrimaryKey. Base method does nothing.
   * @throws Exception 
   */
  protected void onAfterSetAsPkFilter() throws Exception {
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Error Management">
  /**
   * Get whether he record has an assigned error
   * @return Boolean
   */
  public final Boolean hasErrorMsg() {
    return (DataEntry.cleanString(this.getErrorMsg()) != null);
  }

  /**
   * Get the currently assigned Error Message if this instance and any sub-components.
   * It calls onGetErrorMsg to get the latter. if both this instance and the sub-component
   * has errors, the sub-component's errors is appended using a "; \n\r" separator.
   * @return String
   */
  public final String getErrorMsg() {
    String sResult = this.errorMsg;
    String sSubErr = DataEntry.cleanString(this.onGetErrorMsg());
    if (sResult == null) {
      sResult = sSubErr;
    } else if (sSubErr != null) {
      sResult += "; \n\r" + sSubErr;
    }
    return (sResult == null) ? "" : sResult;
  }

  /**
   * CAN OVERRIDE: Return the errors of sub-components (as applicable). 
   * The base Method returns null.
   * @return String
   */
  protected String onGetErrorMsg() {
    return null;
  }

  /**
   * Set/Append the currently assigned error message with sErrMsg. 
   * Ignored if sErrMsg=null. Call clearErrorMsg to clear the message. If the message is
   * not empty, append the new message with a "; \n\r" separator.
   * @param sErrMsg String
   */
  public final void setErrorMsg(String sErrMsg) {
    sErrMsg = DataEntry.cleanString(sErrMsg);
    if (sErrMsg == null) {
      return;
    }

    if (this.errorMsg == null) {
      this.errorMsg = sErrMsg;
    } else {
      this.errorMsg += "; \n\r" + sErrMsg;
    }
  }

  /**
   * Clear the currently assigned error messages
   */
  public final void clearErrorMsg() {
    this.errorMsg = null;
    this.onClearErrorMsg();
  }

  /**
   * CAN OVERRIDE: Call be clearErrorMsg to clear the errors of any sub-components if 
   * the error of this instance is cleared. The base method does nothing
   */
  protected void onClearErrorMsg() {
  }
  // </editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Field|Record Edit & Submit methods">
  /**
   * Set the Wrapper's isFieldEditor flag. If true, field level edits will be 
   * submitted to the database when calling the {@linkplain #submitFieldEdits(boolean,
   * boolean) this.submitFieldEdits} methods, else changes will only be submitted when
   * calling one of this.submitEdits overrides.
   * @return the current setting (default = false).
   * @since 1.00.001
   */
  protected boolean isFieldEditor() {
    return ((this.fieldEditor != null) && (this.fieldEditor));
  }
  
  /**
   * Set the Wrapper's isFieldEditor flag. See {@linkplain #isFieldEditor() 
   * this.isFieldEditor}.
   * @param fieldEditor true to set the flag.
   */
  public final void setFieldEditor(boolean fieldEditor) {
    this.fieldEditor = (!fieldEditor)? null: true;
  }
  /**
   * Get the EntityWrapper's isSubmitting state - set while to processes to submit a new
   * bean or save edit to a new bean is in progress.
   * @return boolean
   * @since 1.00.001
   */
  public final boolean isSubmitting() {
    return this.isSubmitting;
  }

  /**
   * Increment the Initiate Counter, which prevent saving of incremental edits during
   * batch processing. It calls {@linkplain #onStartEdits() this.onStartEdits} when 
   * this.editingCount is 1 (i.e., when {@linkplain #isEditing() this.isEditing} changed
   * to true). All errors are trapped and ignored.
   */
  public final synchronized void startEdits() {
    if ((this.editingCount <= 0)) {
      this.editingCount = 1;
      try {
        this.onStartEdits();
      } catch (Exception exp) {
      }
    } else {
      this.editingCount++;
    }
  }
  
  /**
   * CAN OVERRIDE: Called be {@linkplain #startEdits() this.startEdits} when {@linkplain
   * #isEditing() this.isEditing} changed to true. The base method does nothing.
   */
  protected void onStartEdits() {}

  /**
   * Get the records batch editing state (i.e, true if the Editing Count > 0)
   * @return boolean
   */
  public final boolean isEditing() {
    return (this.editingCount > 0);
  }
  
  /**
   * Overload 1: Call {@linkplain #endEdits(boolean) Overload 2} with forceAccess = false.
   */
  public final synchronized void endEdits() {
    this.endEdits(false);
  }

  /**
   * Overload 2: Decrement the Editing Count and if (Editing Count == 0) call 
   * this.submitNew(forceAccess) if this.isNew or  else submitEdits(true, forceAccess)
   * method to force the saving the current edits.
   * <p>It calls {@linkplain #onEndEdits() this.onEndEdits} before decrementing the count
   * and when (Editing Count == 1), to make final changes to Entity Properties while
   * this.isEditing. All errors are trapped and ignored.
   */
  public final synchronized void endEdits(boolean forceAccess) {
    if (this.editingCount <= 0) {
      return;
    } else if (this.editingCount == 1) {
      try {
        /* call this.onEndEdits while this.isEditing = true */
        this.onEndEdits();
      } catch (Exception exp) {
      }
    }
    
    this.editingCount = this.editingCount - 1;
    if (this.editingCount == 0) {
      if (this.isNew()) {
        this.submitNew(forceAccess);
      } else {
        this.submitEdits(true, forceAccess);
      }
    }
  }
  
  /**
   * CAN OVERRIDE: Called be {@linkplain #endEdits() this.endEdits} before {@linkplain
   * #isEditing() this.isEditing} changed to false and before entity changes are 
   * submitted to the database. The base method does nothing.
   */
  protected void onEndEdits() {}
  
  /**
   * Called by field SET-methods when the field value has changed after the new field 
   * value was validated to submit the changed field value to the database. Typically
   * user with AJAX field editors that allow record submittal if a field value change,
   * instead of waiting for all value to be updated before the record is validated and
   * submittal.
   * <p>
   * <b>NOTE:</b> This request is ignore if this.isNew. When this.isNew only record
   * level submittal is allowed.</p>
   * @param forceWrite true to force the submittal even if this.isEditing
   * @param forceAccess true to force the user's access to the record.
   * @since 1.00.001
   */
  protected final void submitFieldEdits(boolean forceWrite, boolean forceAccess) {
    try {
      if ((!this.isNew()) && (this.isFieldEditor())) {
        this.submitEdits(forceWrite, forceAccess);
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      logger.log(Level.WARNING, "{0}.submitFieldEdits Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
//    } catch (Exception exp) {
//              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
  }

  /**
   * Overload 1: Calls the {@linkplain #submitEdits(boolean, boolean) Overload 3} with 
   * forceWrite=false and forceAccess = false.
   */
  public final void submitEdits() {
    this.submitEdits(false, false);
  }
  /**
   * Overload 2: Calls the {@linkplain #submitEdits(boolean, boolean) Overload 3} with 
   * forceAccess = false.
   * @param forceWrite true to force the submittal even if this.isEditing
   */  
  public final void submitEdits(boolean forceWrite) {
    this.submitEdits(forceWrite, false);
  }

  /**
   * Overload 3: Submit the current the record (this.entity). This call is skipped if:
   * <ul>
   *  <li>this.isNew</li>
   *  <li>(!forceWrite) && this.isEditing</li>
   *  <li>(!((this.allowEdits()) || ((facade.isInitiating())) && (!forceAccess))</li>
   * </ul>
   * <p>If not skipped the record is submitted as follows:</p><ul>
   *  <li>if (setInitiating = ((forceAccess) && (!facade.isInitiating()) call
   *    facade.beginInit() to force facade.isInitiating</li>
   *  <li>if ((facadeDoEdit != facade.doEdits()) set this.facadeHelper.doEdits=true</li>
   *  <li>Validate Record: It clears all current errors and call validateRecord 
   *  to check if all required field assignment conditions are met. If this returns and 
   *  error, the process is stopped without logging the errorMsg.</li>
   *  <li>Start Try-Finally setting this.mbSubmitting=true</li>
   *  <li>Calls this.onBeforeSubmitEdits() </li>
   *  <li>Calls this.onSaveRecord(this.mbBean) </li>
   *  <li>Close try-finally setting this.mbSubmitting=false</li>
   *  <li>Calls this.onAfterSubmitEdits() </li>
   *  <li>Finally if (facadeDoEdit) set this.facadeHelper.doEdits=false and/or
   *    if setInitiating call facade.endInit()</li>
   * </ul>
   * <p>Errors are trapped and assign to the.errorMsg</p>
   * <p><b>NOTE:</b> If (forceWrite) or (facadeHelp.isInitiating), it will force a 
   * submit even if the the record isReadOnly() or isEditing or the 
   * facadeHelper.doEdits=false. Use sparely and with caution. If you want to make 
   * sure a field value is saved after validating the that the field value can be 
   * changed it is recommended to call this.SubmitEidts(!this.isEditing()). This will
   * prevent submittals while in an isEditing state.</p>
   * @param forceWrite true to force the submittal even if this.isEditing
   * @param forceAccess true to force the user's access to the record.
   * @since 1.00.001 - add forceAccess
   */
  public final void submitEdits(boolean forceWrite, boolean forceAccess) {
    EntityFacade<TBean> facade = null;
    boolean facadeDoEdit = false;
    boolean setInitiating = false;
    try {
      facade = this.getFacade();
      if (facade == null) {
        logger.log(Level.SEVERE, "{0}.submitEdits Error:\n "
              + "The Entity's Facade is not accessible.", this.getClass().getSimpleName());
        return;
      }

      if ((this.isNew()) || ((!forceWrite) && (this.isEditing()))) {
        return;
      }
      
      if (setInitiating = ((forceAccess) && (!facade.isInitiating()))) {
        facade.beginInit();
      }

      boolean hasAccess = ((this.allowEdits()) || (facade.isInitiating()));
      if (!hasAccess) {
        return;
      }
      
      if (facadeDoEdit != facade.doEdits()) {
        facade.setDoEdits(true);
      }

      if (!this.validateRecord()) {
        return;
      }

      try {
        this.isSubmitting = true;
        this.onBeforeSubmitEdits();
        TBean savedBean = facade.edit(this.entityBean);
      } finally {
        this.isSubmitting = false;
      }
      this.onAfterSubmitEdits();
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    } finally {
      if (facade != null) {
        if (facadeDoEdit) {
          facade.setDoEdits(false);
        }
        if (setInitiating) {
          facade.endInit();
        }
      }
    }
  }
  
  /**
   * CAN OVERRIDE: Called by submitEdits-AFTER the new field values were validate-
   * and BEFORE it is save to the database-for the custom saving of Entity related
   * settings and/or parameters. When an Exception is thrown the process will be 
   * stopped and the message will be assigned as the EntityWrapper's Error Message.
   * The base method does nothing.
   * @throws Exception
   */
  protected void onBeforeSubmitEdits() throws Exception {
  }

  /**
   * CAN OVERRIDE: Called by submitNew-AFTER the record has been successfully updated
   * into the underlying dataset. This method should no throw an exception. The record
   * will not automatically removed on an error. The base method does nothing.
   */
  protected void onAfterSubmitEdits() {
  }

  /**
   * Overload 1: Submit the new record. Call {@linkplain #submitNew(boolean) Overload 2}
   * with forceAccess =  false.
   */
  public final void submitNew() {
    this.submitNew(false);
  }

  /**
   * Overload 2: Submit the new record. This process is skipped if <ul>
   * <li>this.isNew = false.</li>
   * <li>((!this.allowAdd()) && (!((facade.isInitiating()) || (this.forceAccess)))</li>
   * </ul>
   * 
   * <p>If not skipped the record is submitted as follows:</p><ul>
   *  <li>if (setInitiating = ((forceAccess) && (!facade.isInitiating()) call
   *    facade.beginInit() to force facade.isInitiating</li>
   *  <li>if ((facadeDoEdit != facade.doEdits()) set this.facadeHelper.doEdits=true</li>
   *  <li>Validate Record: It clears all current errors and call validateRecord 
   *  to check if all required field assignment conditions are met. If this returns and 
   *  error, the process is stopped without logging the errorMsg.</li>
   *  <li>Start Try-Finally setting this.mbSubmitting=true</li>
   * <li>Call recId = facade.initRecordId(this.entityBean) to initiate the record's 
   * primaryKey</li>
   *  <li> Call facade.assignToFkParents(this) to add the new record's foreignKey parent
   * reference</li>
   *  <li>Calls this.onBeforeSubmitNew() </li>
   *  <li>Calls facade.create(this.entityBean) - if the process fails call
   *  - facade.removeFromFkParents(this) and facade.resetRecordId(this.entityBean)</li>
   *  <li>Close try-finally setting this.mbSubmitting=false</li>
   *  <li>Calls this.onAfterSubmitNew() </li>
   *  <li>Finally if (facadeDoEdit) set this.facadeHelper.doEdits=false and/or
   *    if setInitiating call facade.endInit()</li>
   * </ul>
   * The process is halted if an error is recorded. Errors are assigned as this.errorMsg.
   * @param forceAccess true to force the user's access to the record.
   * @since 1.00.001 - add forceAccess
   * @since 1.00.002 - add onSubmitNewFailed
   */
  public final void submitNew(boolean forceAccess) {
    EntityFacade<TBean> facade = this.getFacade();
    boolean facadeDoEdit = false;
    boolean setInitiating = false;
    try {
      if (!this.isNew()) {
        return;
      } 
      if (facade == null) {
        throw new Exception("The Entity's Facade is not initiated.");
      }
      
      if (setInitiating = ((forceAccess) && (!facade.isInitiating()))) {
        facade.beginInit();
      }

      boolean hasAccess = ((this.allowAdd()) || (facade.isInitiating()));
      if (!hasAccess) {
        return;
      }
      
      if (facadeDoEdit != facade.doEdits()) {
        facade.setDoEdits(true);
      }

      if (!this.validateRecord()) {
        return;
      }
      
      try {
        this.isSubmitting = true;
        this.onBeforeSubmitNew();
        
        facade.assignToFkParents(this); 
        /* @since 1.00.004 */
        boolean addedRecId = false;
        try {          
          /* @since 1.00.004 */
          if (!facade.isAutoAssignedPk()) {
            addedRecId = this.assignManualPk();
          }
          if (!facade.create(this.entityBean)) {
            throw new Exception("Submitting the persistent record failed. Please see "
                    + "server log for more details.");
          }
        } catch (Exception inExp2) {
          facade.removeFromFkParents(this);
          /* @since 1.00.004 */
          if (addedRecId) {
            facade.resetRecordId(this.entityBean);
          }
          throw inExp2;
        }
      } catch (Exception inExp1) {
        throw inExp1;
      } finally {
        this.isSubmitting = false;
      }
      /* Only reach if the submit of the new record was successful */
      try {
        this.onAfterSubmitNew();
      } catch (Exception inExp) {        
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      /* @since 1.00.002 */
      try {
        this.onSubmitNewFailed();
      } catch (Exception inExp) {        
      }
    } finally {
      if (facade != null) {
        if (facadeDoEdit) {
          facade.setDoEdits(false);
        }
        if (setInitiating) {
          facade.endInit();
        }
      }
    }
  }
  
  /**
   * Called by submitNew when the Entity has a Manual or Composite PrimaryKey that must 
   * be manually initiated. It calls {@linkplain #onAssignManualPk() 
   * this.onAssignManualPk} to custom handle the assignment.
   * <p>It is called just before creating (submitting) the new record and throws and 
   * exception if recordId is still unassigned after calling this.onAssignManualPk.
   * <p>
   * <b>NOTE:</b> The Record's recordId is reset if the submitting of the new record
   * failed.</p>
   * @throws Exception if request failed.
   * @since 1.00.004
   */
  private boolean assignManualPk() throws Exception {
    boolean result = false;
    try {
      this.onAssignManualPk();
      if (this.isNew()) {
        throw new Exception("The Manual Primary Key has not been initiated. Use the "
                + "protected Method[onAssignManualPk] to assign the Manual Primary Key.");
      }
      result = true;
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.assignManualPk Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw exp;
    }
    return result;
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #assignManualPk() this.assignManualPk} to handle 
   * the custom assignment of Manual | Composite PrimaryKeys.
   * <p>Inheritors with a Manual | Composite PrimaryKey should override this method.
   * The base method does nothing.
   * @throws Exception if an error occur and the submitNew process should be stopped.
   */
  protected void onAssignManualPk() throws Exception{}
  
  /**
   * CAN OVERRIDE: Called by submitNew-AFTER the new field values were validate-
   * and BEFORE inserting the record-for the custom handling of the saving of related
   * parameters or settings before the inserting of the record.  If a Exception
   * is thrown, the process will be stopped and the error will be assigned to the
   * EntityWrapper's Error Message. The base method does nothing.
   * @throws Exception
   * @since 1.00.004
   */
  protected void onBeforeSubmitNew() throws Exception {}

  /**
   * CAN OVERRIDE: Called by submitNew-AFTER the record has been successfully inserted
   * into the underlying dataset. This method should not throw an exception. The record
   * will not automatically removed on an error. The base method does nothing.
   */
  protected void onAfterSubmitNew() {}

  /**
   * CAN OVERRIDE: Called by submitNew-AFTER an attempt to insert the record into the 
   * underlying dataset failed . This method should not throw an exception. 
   * The base method does nothing.
   * @since 1.00.002
   */
  protected void onSubmitNewFailed() {}

  /**
   * CAN OVERRIDE: Called by removeRecord for the custom handling of the deletion of
   * existing resources/settings related to the EntityBean before removing the record
   * permanently.  Any exceptions will be ignored and the record will be removed
   * regardless. The base method does nothing.
   * @throws Exception
   */
  protected void onBeforeRemove() throws Exception {}

  /**
   * removeRecord (delete) the current record. Throw an exception is deletion of
   * records or this specific are not allowed. Fist check getAllowDelete and throw
   * exception is not allowed. The call onBeforeRemove, this.FacadeHelper's {@linkplain 
   * FacadeHelper#removeFromFkParents(myapp.core.EntityWrapper) removeFromFkParents},
   * this.resetAsEntityFilter, and onRemoveRecord, respectively in this order.
   */
  public final void removeRecord() throws Exception {
    EntityFacade<TBean> facade = null;
    try {
      this.clearErrorMsg();
      if (!this.isNew()) {
        facade = this.getFacade();
        if (facade == null) {
          throw new Exception("The Entity's Facade is not initiated.");
        }

        if (!this.allowDelete()) {
          throw new Exception("Deleting of " + this.getClass().getSimpleName()
                  + "[" + this.getRecordName() + "] is not allowed");
        }

        this.onBeforeRemove();
        facade.removeFromFkParents(this);
        this.resetAsPkFilter();
        facade.remove(this.entityBean);
      }
      this.clearEntity();
      this.onInitNewEntity();
      this.resetRecord(this.getEntity(), false);
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".Remove Error:\n " + exp.getMessage());
    } 
  }
  
  /**
   * removeRecord the current record within a FacadeHelper.BeginInit & .EndInt clause.  
   * This method requires no user access permission to delete the record. 
   * Thus, use with care.
   * @throws Exception 
   */
  public final void removeWithPermission() throws Exception {
    EntityFacade<TBean> facade = this.getFacade();
    if (facade == null) {
      throw new Exception("Unable to access the EntityFacade for Class[" 
              + this.getClass().getSimpleName() + "].");
    }
    try {
      facade.beginInit();
      this.removeRecord();      
    } finally {
      facade.endInit();
    }
  }

  /**
   * Called to refresh/reset the values of the underlying EntityBean to its most current
   * saved values. if IsNew() or is the original bean is no longer accessible, call
   * onBeforeNewEntity to replace the current EntityBean with a new blank Entity.
   * Otherwise, user the FacadeHeler to locate the original bean and reload it by
   * calling setEntity().
   * @throws IllegalArgumentException if the process failed.
   * @since 1.00.002 - call this.clearEntity when this.isNew before calling 
   * this.onInitNewEntity
   */
  @SuppressWarnings("unchecked")
  public final void refreshRecord() {
    try {
      if (this.isNew()) {
        this.clearEntity();
        this.onInitNewEntity();
      } else if (this.entityBean != null) {
        EntityFacade<TBean> facade  = this.getFacade();
        if (facade == null) {
          throw new Exception("Unable to access the Instance Facade.");
        }
        boolean success = false;
        String errMsg = null;
        TBean newBean = null;
        try {
          newBean = facade.refresh(this.entityBean);
          success = true;
        } catch (Exception innerExp) {
          errMsg = "Reloading the Entity record failed" 
                  + ((innerExp.getMessage() == null)? ".":
                  " because: " + innerExp.getMessage());
        } 
        
        if (success) {
          this.resetRecord(newBean, true);
        } else {
          throw new Exception(errMsg);
        }
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName() 
              + ".refreshRecord Errror:\n"
              + exp.getMessage());
    } 
  }

  /**
   * Private method called by {@linkplain #refreshRecord() this.refreshRecord} after 
   * the {@linkplain EntityFacade#refresh(java.io.Serializable) 
   * EntityFacade.refresh(this.entityBean)} was called to refresh this Wrapper's 
   * underlying entity bean.
   * <p>It calls {@linkplain #clearEntity() this.clearEntity} followed by a call to 
   * {@linkplain #setEntity(java.io.Serializable) this.setEntity(newBean)} to assign 
   * the newBean as this.entityBean.
   * <p>If (!this.isNew) it call {@linkplain #resetParentCache() this.resetParentCache}
   * if <tt>resetParent</tt> = true before calling {@linkplain 
   * #syncAllCachedChildCollections() this.syncAllCachedChildCollections} to sync this
   * parent foreignKey child collections with any cached collections.
   * <p>Last, it call the protected {@linkplain #onResetRecord() to reset the current
   * bean, onResetRecord} method before firing {@linkplain #RecordRefreshed 
   * this.RecordRefreshed} event.
   * <p><b>Note:</b>All error are trapped and logged.</p>
   */
  private void resetRecord(TBean newBean, boolean resetParent) {
    try {
      this.clearEntity();
      this.setEntity(newBean);
      this.clearErrorMsg();
      
      if (!this.isNew()) {
        if (resetParent) {
          this.syncParentChildCollections();
        }

        this.syncAllCachedChildCollections();
      }
      this.onResetRecord();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetRecord Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.fireRecordRefreshed();
    }
  }

  /**
   * <p>CAN OVERRIDE: A protected method called by the refreshRecord method after the 
   * EntityWrapper's bean has been refreshed. It can be overridden to refresh 
   * locally maintain fields and cached resources that are bean specific. 
   * The base method does nothing</p>
   */
  protected void onResetRecord() {
  }
  
  /**
   * <p>CAN OVERRIDE: Called by {@linkplain #fireAddChild(
   * bubblewrap.core.events.ChildChangedEventArgs) this.fireAddChild} after the child
   * has been added to this parent's collection to allow the parent to refresh/update
   * cached lists before listeners access these list.
   * @param args the event arguments
   */
  protected void onAddChild(ChildChangedEventArgs args) {
  }
  
  /**
   * <p>CAN OVERRIDE: Called by {@linkplain #fireDeleteChild(
   * bubblewrap.core.events.ChildChangedEventArgs) this.fireDeleteChild} after the child
   * had been removed from this parent's collection to allow the parent to refresh or
   * update cached lists before listeners access these list.
   * @param args the event arguments
   */
  protected void onDeleteChild(ChildChangedEventArgs args) {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Validate Input Methods">
  /**
   * Get the Entity's FieldInfo as registered in its EnityContext
   * @param fieldName the field to search for
   * @return the fieldInfo or null if not found
   */
  public final FieldInfo getFieldInfo(String fieldName) {
    FieldInfo result = null;
    EntityContext<TBean> entCtx = null;
    if ((entCtx = this.getEntityContext()) != null) {
      result = entCtx.getFieldInfo(fieldName);
    }
    return result;
  }
  
  /**
   * Check if this.entityContext can a FieldInfo for <tt>fieldName</tt> 
   * @param fieldName the field to search for
   * @return (this.getFieldInfo(fieldName) != null)
   */
  public final boolean hasFieldInfo(String fieldName) {
    return (this.getFieldInfo(fieldName) != null);
  }
  
  /**
   * Set pEntity.Field[sField]'s value. Throw an exception if the field does not
   * support a SET-method or the field does not exist in the entity or pEnity is not an
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
  private <TBean extends Serializable, TValue> void setFieldValue(TBean entityBean, 
          FieldInfo fieldInfo, TValue value) throws Exception {
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
    
    if ((!this.isNew()) && 
                      (!fieldInfo.allowEdits(this.getClass(), this.isSystemItem()))) {
      throw new Exception("The '" + entityClass.getSimpleName()
              + "." + fieldName + "' is ReadOnly. The value cannot be set.");
    }
    
    if (fieldInfo.setMethod == null) {
      throw new Exception("The SET-method for field " 
              + entityClass.getSimpleName() + "." + fieldName + " is not supported");
    }
    
    try {
      Object[] args = {value};
      Object result = fieldInfo.setMethod.invoke(entityBean, args);
    } catch (IllegalAccessException | IllegalArgumentException 
            | InvocationTargetException exp) {
      throw new Exception("Setting Field '" + entityClass.getSimpleName()
              + "." + fieldName + "' failed because:" + exp.getMessage());
    }
  }
  
  /**
   * Called to convert the current field value to a formatted string using the Field's
   * assigned InputValidator (see {@linkplain #getInputValidator(java.lang.String)
   * getInputValidator).
   * @param fieldName the field's name
   * @return the formatted field or null if unassigned.
   */
  @SuppressWarnings("unchecked")
  protected String getValueAsString(String fieldName) {
    String result = null;
    try{
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName == null) {
        throw new Exception("The Field Name is unassigned.");
      }
      
      Class entClass = this.getEntityClass();
      FieldInfo fieldInfo = ReflectionInfo.getFieldInfo(entClass, fieldName);
      if (fieldInfo == null) {
        throw new Exception("Entity[" + this.getEntityClass().getSimpleName() 
                + "] does not has a Field[" + fieldName + "].");
      }
      
      Serializable curValue = ReflectionInfo.getFieldValue(this.entityBean, fieldName);
      if (curValue != null) {
        InputValidator validator = this.getInputValidator(fieldName);
        if (validator !=  null) {
          result = validator.toString(curValue);
        }
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called to convert the input string to a "clean" string for submitting to the Entity
   * field. Is first call the {@linkplain DataEntry#cleanString(java.lang.String) 
   * cleanString} method and is the result = null, the result = null. Otherwise, it will
   * retrieve the field's FieldInfo and its (optional) InputValidator. If the latter is
   * assigned it will call its toValue method to "clean" the input string.
   * @param fieldName the field's name (must be assigned and a valid entity field name
   * @param inValue
   * @return 
   */
  protected String getStringAsValue(String fieldName, String inValue) {
    String result = null;
    try{
      result = DataEntry.cleanString(inValue);
      if (result != null) {
        InputValidator validator = this.getInputValidator(fieldName);
        if ((validator !=  null) && (validator.isValueClass(String.class))) {
          result = (String) validator.toValue(result);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getStringAsValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to convert a input string to a a field numeric value for submitting to the 
   * Entity field. Is first call the {@linkplain DataEntry#cleanString(java.lang.String) 
   * cleanString} method and is the result = null, the result = null. Otherwise, it will
   * retrieve the field's FieldInfo and its InputValidator.  If the latter is assigned
   * it will call the InputValidator.toValue method to convert the input string to the
   * number. Otherwise, it will the {@linkplain DataConverter#toValue(java.lang.String, 
   * java.lang.Class) DataConverter.toValue} method to handle the conversion.
   * @param <TNum> extends Number
   * @param fieldName the field name
   * @param inValue the input string value
   * @param numClass the Number class
   * @return the converted value or null if inValue = null|"" or the process failed.
   */
  @SuppressWarnings("unchecked")
  protected <TNum extends Number> TNum getStringAsValue(String fieldName, 
              String inValue, Class<TNum> numClass) {
    TNum result = null;
    try{
      inValue = DataEntry.cleanString(inValue);
      if (inValue != null) {
        InputValidator validator = this.getInputValidator(fieldName);
        if ((validator !=  null) && (validator.isValueClass(numClass))) {
          result = (TNum) validator.toValue(inValue);
        } else if (Number.class.isAssignableFrom(numClass)) {
          result = (TNum) DataConverter.toValue(inValue, numClass);                  
        }
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called to retrieve a Field's InputValidator - if assigned as a field constraint.
   * Errors are trapped and log.
   * @param fieldName the name of the field
   * @return the InputValidator or null is unassigned or an error occur. 
   */
  @SuppressWarnings("unchecked")
  protected InputValidator getInputValidator(String fieldName) {
    InputValidator result= null;
    try{
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName == null) {
        throw new Exception("The Field Name is unassigned.");
      }
      
      FieldInfo fieldInfo = this.getFieldInfo(fieldName);
      if (fieldInfo == null) {
        throw new Exception("Entity[" + this.getEntityClass().getSimpleName() 
                + "] does not has a Field[" + fieldName + "].");
      }
      
      result = fieldInfo.getInputValidator();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getInputValidator Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Check if the proposed field value for the specified field is a unique value. It 
   * calls the {@linkplain EntityFacade#isUnique(java.lang.String, java.lang.Object, 
   * java.lang.Object, int) EntityFacade#isUnique} method with recID=this.recordId.
   * @param fieldName the field to validate 
   * @param fieldValue the new field value to validate
   * @param filterOptions the filter options (Type {@linkplain FacadeFilterEnums}).
   * @return Return true if the field Value for the specified field is unique in the 
   * dataset - ignoring the current records value.
   */
  public boolean isUnique(String fieldName, Object fieldValue, int filterOptions) 
          throws Exception {
    boolean result = true;
    if (fieldValue != null) {
      EntityFacade<TBean> facade = this.getFacade();
      result = 
             facade.isUnique(fieldName, fieldValue, this.getRecordId(), filterOptions);
    }
    return result;
  }

  /**
   * Return true if the proposed value for the specified Field is unique in the 
   * specified parent's Child Collection - ignoring the current record value. It 
   * retrieves this class' parentForeignKeyInfo for the parentClass' entityClass to
   * get the Child fieldname and the child field's return type (from its FieldInfo).
   * It call {@linkplain #getParent(java.lang.Class)} to get this instance parent and
   * this.facade's {@linkplain EntityFacade#isUniqueInParent(java.lang.String, 
   * java.lang.Object, java.lang.String, java.lang.Object, java.lang.Object, int) 
   * isUniqueInParent} method to validate the specified fieldValue is unique in this
   * instance's parent's context.
   * @param fieldName the field to validate 
   * @param fieldValue the new field value to validate
   * @param parentClass the Parent EntityWrapper class
   * @param filterOptions the filter options (Type {@linkplain FacadeFilterEnums}).
   * @return true if the field Value is unique in the list of the parent's Child 
   * collection - ignoring the current record value. Return true if fieldValue=null or
   * if this.parent=null.
   * @throws Exception if the fieldName or parentClass is unassigned or the 
   * ForeignKey and Child Field's FieldInfo in not accessible.
   * @since 1.00.003 - fix bug with wrong parent-in-child field.
   */  
  @SuppressWarnings("unchecked")
  public Boolean isUniqueInParent(String fieldName, Object fieldValue, 
          Class<? extends EntityWrapper> parentClass, int filterOptions) 
          throws Exception {
    Boolean result = true;
    if (fieldValue == null) {
      return result;
    }
    
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new Exception("The Child Field is unassigned");
    }
    if (parentClass == null) {
      throw new Exception("The Parent Class is unassigned");
    }
        
    Class parentBeanClass = EntityWrapper.getEntityClass(parentClass);   
    
    EntityFacade<TBean> facade = this.getFacade();
    ForeignKey parentFk = facade.getParentForeignKey(parentBeanClass);
    if (parentFk == null) {
      throw new Exception("Entity[" + this.getEntityClass().getSimpleName() +
                  "] does not have any registered Parent ForeignKey for "
              + "parent Class[" + parentBeanClass.getSimpleName() + "].");
    }
    
    String childField = parentFk.childAssociation.targetPath.fieldName;
    FieldInfo childFieldInfo = this.getEntityContext().getFieldInfo(childField);
    if (childFieldInfo == null) {
      throw new Exception("The FieldInfo for Field[" + childField 
              + "] is inaccessible");
    }

    EntityWrapper parent = this.getParent(parentClass, childField);
    if (parent == null) {
      return result;
    }    
    
    Object fkId = null;
    if (parentBeanClass.equals(childFieldInfo.returnType)) {
      fkId = parent.getEntity();
    } else {
      fkId = parent.getRecordId();
    }
    result = facade.isUniqueInParent(fieldName, fieldValue, childField, fkId, 
                                                    this.getRecordId(), filterOptions);
    return result;
  }
  
  /**
   * <p>Validate a specific field value. Only called to validate a single field. It
   * retrieves the specified field's FiedlValdiate from the EntityContext. If defined,
   * it calls the FieldValidators {@linkplain FieldValidator#isValidInput(
   * bubblewrap.entity.core.EntityWrapper, java.lang.Object, boolean) 
   * isValidInput(fieldName,value,false)} method. If the validation fails, 
   * it set this.errorMsg as the Validator's error message and return false.</p>
   * <p>If the validation pass, it calls {@linkplain #onValidateField(java.lang.String, 
   * java.lang.Object) onValidateField} and trapped any exception thrown. The reported 
   * error is assigned as this.errorMsg.</p>
   * @param fieldName the name of the field or a field reference to validate
   * @param value the value to validate
   * @return true if the validation was successful.
   */
  public final boolean validateField(String fieldName, Object value) {
    boolean result = true;
    try {
      if ((fieldName = DataEntry.cleanString(fieldName)) != null) {
        EntityContext<TBean> entCtx = null;
        FieldValidator validator = null;
        if (((entCtx = this.getEntityContext()) != null) &&
                ((validator = entCtx.getFieldValidator(fieldName)) != null) &&
                (!validator.isValidInput(this, value, false))){
          throw new Exception((validator.hasError())? validator.getErrorMsg():
                    "The entered value for Field[" + fieldName 
                    + "] does not comply with the field's contraint settings.");          
        }
        if (!this.hasErrorMsg()) {
          this.onValidateField(fieldName, value);
        }
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      result = false;
    }
    return result;
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #validateField(java.lang.String, 
   * java.lang.Object) to validate a specific field's value. If the validation failed,
   * an exception should be throw with an applicable error message. This message will
   * be assigend as this.errorMsg. The Base Method does nothing
   * @param fieldName the name of the field or a field reference to validate
   * @param value the value to validate
   */
  protected void onValidateField(String fieldName, Object value) throws Exception {
  }

  /**
   * Called to validate the entire record. Call clearErrorMsg before starting the 
   * process. It first request a list of fields from the EntityContext. If the
   * list is not empty, it retrieve the currently assign value of each of these field
   * and field's FiedlValdiate from the EntityContext. If latter is defined, it calls 
   * the FieldValidators {@linkplain FieldValidator#isValidInput(
   * bubblewrap.entity.core.EntityWrapper, java.lang.Object, boolean) 
   * isValidInput(fieldName,value,false)} method and if valid it calls 
   * {@linkplain #onValidateField(java.lang.String, java.lang.Object) onValidateField} 
   * and {@linkplain #validateField(java.lang.String, java.lang.Object) 
   * validateField} for any custom validation of the record before it is submitted to 
   * the database. All errors are trapped and assign as this.errorMsg
   * @return true if the validation returned no error message.
   */
  public final boolean validateRecord() {
    this.clearErrorMsg();
    try {
       this.onBeforeValidateRecord();
      
      EntityContext<TBean> entCtx = null;
      FieldValidator validator = null;
      List<String> validationnFields = null;
      if ((!this.hasErrorMsg()) && ((entCtx = this.getEntityContext()) != null) &&
              ((validationnFields = entCtx.getValidationFields()) != null) &&
              (!validationnFields.isEmpty())){
        for (String fieldName : validationnFields) {
          FieldInfo fieldInfo = entCtx.getFieldInfo(fieldName);
          if ((fieldInfo == null) || (!fieldInfo.hasMethods()) 
                                                            || (fieldInfo.isPrimaryKey)) {
            continue;
          }
          Object value = ReflectionInfo.getFieldValue(this.entityBean, fieldInfo);
          if (((validator = entCtx.getFieldValidator(fieldName)) != null) &&
                  (!validator.isValidInput(this, value, true))) {
            throw new Exception(validator.getErrorMsg());                      
          }

          this.onValidateField(fieldName, value);
        }      
      }
      
      if (!this.hasErrorMsg()) {
        this.onAfterValidateRecord();
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    }
    
    return (!this.hasErrorMsg());
  }

  /**
   * <p>CAN OVERRIDE: Called by {@linkplain #validateRecord()  validateRecord} to 
   * prepare the record for validation. </p> 
   * <p>Exceptions are trapped by the validateRecord 
   * method and assigned as this.errorMsg. The base method does nothing.</p>
   */
  protected void onBeforeValidateRecord() throws Exception {}

  /**
   * <p>CAN OVERRIDE: Called by {@linkplain #validateRecord()  validateRecord} -
   * after conducting the default validation (per each field's {@linkplain FieldInfo} -
   * to do any custom record validate on specified entity input fields. </p> 
   * <p> Exceptions are trapped by the validateRecord method and assigned as 
   * this.errorMsg. The base method does nothing.</p>
   */
  protected void onAfterValidateRecord() throws Exception {}
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="EntityWrapper ObjectData Serialization">
  /**
   * CAN OVERRIDE: This method is called when the EntityWrapper is serialized using an
   * EntityWrapperObjectData wrapper.  The wrapper serialize the EntityBean. Override this
   * method to save non-transient and non-bean properties. The base method does nothing.
   * @see EntityWrapperObjectData
   * @param fieldData ParameterMap
   * @throws Exception
   */
  public void serializeObjectData(ParameterMapBase fieldData) throws Exception {
  }
  
  /**
   * CAN OVERRIDE: This method is called when the EntityWrapper is deserialized using an
   * EntityWrapperObjectData wrapper.  The wrapper deserialize the EntityBean.
   * Override this method to restore non-transient and non-bean properties.
   * The base method does nothing.
   * @see EntityWrapperObjectData
   * @param fieldData ParameterMap
   * @throws Exception
   */
  public void deserializeObjectData(ParameterMapBase fieldData) throws Exception {
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return this.entity.toString()</p>
   */
  @Override
  public String toString() {
    return (this.entityBean == null)? super.toString(): this.entityBean.toString();
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode based on this.entityBean</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.entityBean);
    return hash;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of EntityWrapper.
   * Otherwise, return true if the following match: .</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EntityWrapper));
    if (result) {
      EntityWrapper trgObj = (EntityWrapper) obj;
      result = DataEntry.isEq(this.getRecordId(),trgObj.getRecordId());
    }
    return result;
  }
  
  //</editor-fold>

}
