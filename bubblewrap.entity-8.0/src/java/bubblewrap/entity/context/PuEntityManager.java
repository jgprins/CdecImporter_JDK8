package bubblewrap.entity.context;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateful;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.*;
import javax.transaction.UserTransaction;
import org.springframework.context.ApplicationContext;
 
/**
 * <p>An PuEntityManager maintains the {@linkplain EntityFacade EntityFacades} for
 * the EntityBeans that can be accessed through a particular Persistent Unit.
 * Inheritors can customize this class as follows:</p><ul>
 *  <li>Override {@linkplain #getEntityManager() getEntityManager} to return the
 *    applicable Persistent Unit's EntityManager. The latter can use a @{@linkplain 
 *    PersistenceContext} annotation to inject the Persistent Unit's EntityManager.
 *    Example:<br/><code>
 *    &nbsp;&nbsp;@PersistenceContext(unitName = "fcodssPU")<br/>
 *    &nbsp;&nbsp;private EntityManager entMngr;</code>
 *  </li>
 *  <li>If the Facades are managed on by Session, the PuEntityManager should be configured 
 *    as Management bean. It is be adding a {@linkplain ManagedBean} annotation and a
 *    {@linkplain SessionScoped} annotation to set is scope.</li>
 *  <li>If the Facades are managed on an application level, is can be configured as a
 *    {@linkplain Singleton} that will be initiated at {@linkplain Startup}.</li>
 *  <li>In both cases the PuManager must be configured as a {@linkplain Stateful} EJB</li>
 * </ul>
 * @author kprins
 */
public abstract class PuEntityManager implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Logger/Methods">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger = 
                                  Logger.getLogger(PuEntityManager.class.getName());
  
  /**
   * <p>Call to lookup and return a reference to the PuEntityManager for a specific
   * Persistent Unit (as identified by its class. It supports two cases:</p><ul>
   *  <li>If the specified class is a singleton, it looks for a static method with
   *    a return type if the specified class and if found invoke the method and return
   *    the result.</li>
   *  <li>Otherwise, assume it is a Session ManagedBean and call the {@linkplain 
   *    SessionHelper#getManagedBean(java.lang.Class) SessionHelper#getManagedBean}
   *    method passing the specified class. Return the result.</li>
   * </ul>
   * @param <TMngr> extends PuEntityManager
   * @param puClass specified PuEntityManager class
   * @return and instance of the specified PuEntityManager class
   * @throws NamingException if any error occurred during the location of the methods
   * or class.
   */
  @SuppressWarnings("unchecked")
  public static <TMngr extends PuEntityManager> PuEntityManager
                            doLookup(Class<TMngr> puClass) throws NamingException {
    PuEntityManager result = null;
    BwAppContext appCtx = null;
    if (SessionHelper.hasHttpRequest()) {
      result = SessionHelper.getManagedBean(puClass);
    } else if ((appCtx = BwAppContext.doLookup()) != null) {
      result = appCtx.getAppPuManager(puClass);
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the PuEntityManager's Persistent Unit Name
   */
  private String unitName;
  /**
   * 
   */
  private String emfName;
  /**
   * 
   */
  private String txName;
  /**
   * Placeholder for the EntityManagerFactory used to initiate the EntityManager
   */
  private EntityManagerFactory emf; 
  /**
   * Flag indicating if the JTA transactions is used (default = null|true)
   */
  private Boolean useJTA;
  /**
   * Placeholder for the UserTrasnaction used by the facade if applicable
   */
  @Resource
  private UserTransaction userTx;
  /**
   * Placeholder for a HashMap&lt;class,EntityFacade&gt; for maintaining the 
   * EntityFacades for this manager's scope and persistent until
   */
  private HashMap<Class<? extends Serializable>, EntityFacade> facadeMap;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public PuEntityManager(String unitName) {
    this(unitName,null,null);
  }
  /**
   * Public Constructor
   */
  public PuEntityManager(String unitName,String emfName) {
    this(unitName,emfName,null);
  }
  /**
   * Public Constructor
   */
  public PuEntityManager(String unitName,String emfName,String txName) {
    this.unitName = DataEntry.cleanString(unitName);
    if (this.unitName == null) {
      throw new NullPointerException("The PuEntityManager Persistent Unit name cannot "
              + "be undefined.");
    }
    this.emfName = DataEntry.cleanString(emfName);
    this.txName = DataEntry.cleanString(txName);
    try {      
      ApplicationContext appCtx = null;
      if ((this.emfName != null) && 
          ((appCtx = SessionHelper.getApplicationContext()) != null)){
        this.emf = (EntityManagerFactory) appCtx.getBean(this.emfName);
      }
    } catch (Exception exp) {
      
    }
   
    if (this.emf == null){
      this.emf = Persistence.createEntityManagerFactory(this.unitName);
    }
            
    this.userTx = null;
    this.facadeMap = null;
    this.useJTA = null;
  }
  
  /**
   * OVERRIDE: Dispose Local resources before calling the super method
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.emf = null;
    this.userTx = null;
    if (this.facadeMap != null) {
      this.facadeMap.clear();
      this.facadeMap = null;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * Called to initiate a new EntityFacade if it not already exists. It retrieve the
   * entity's EntityContext from the {@linkplain BwAppContext} and this FacadeManager's
   * Persistent Unit's {@linkplain #getEntityManager() EntityManager} to initiate the
   * new EntityFacade. If successful, the facade is added to the facadeMap and returned.
   * @param <TBean> extends entity bean
   * @param entityClass the entity bean class
   * @return the new facadeMap
   * @exception IllegalArgumentException if the AppContext, EntityContext, or
   * EntityManager is not accessible or if initiating the EntityFacade failed.
   */
  @SuppressWarnings("unchecked")
  private <TBean extends Serializable> EntityFacade<TBean>
          addFacade(Class<TBean> entityClass) {
    EntityFacade<TBean> result = null;
    try {
      if (entityClass == null) {
        throw new Exception("The Facade's Entity Class reference is unassigned.");
      } else if (this.containsFacade(entityClass)) {
        result = facadeMap.get(entityClass);
      } else {
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new Exception("Unable to access the Application Context.");
        }
        
        EntityContext<TBean> entCtx = appCtx.getEntityContext(entityClass);
        if (entCtx == null) {
          throw new Exception("The Entity Context for Entity["
                  + entityClass.getSimpleName() + "] is not registered.");
//        } else if (!this.getClass().equals(entCtx.getPuManagerClass())) {
//          throw new Exception("The Entity Class[" + entityClass.getSimpleName() 
//                  + "] is not supported by PuEntityManager[" 
//                  + this.getClass().getSimpleName() + "].");
        }
        
        EntityManager entMngr = this.getEntityManager();
        if (entMngr == null) {
          throw new Exception("Unable to access the Persistent Unit's EntityManager");
        }
        
        EntityFacade<TBean> facade = new EntityFacade<>(entityClass, this);
        if (facade == null) {
          throw new Exception("Initiating the Entity["
                  + entityClass.getSimpleName() + "]'s Facade failed.");
        }
        
        if (this.facadeMap == null) {
          this.facadeMap = new HashMap<>();
        }
        this.facadeMap.put(entityClass, facade);        
        
        result = facade;
      }
    } catch (Exception pExp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".addFacade Error:\n " + pExp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to get the EnityBean's facade. If the facade is not yet initiated, it will
   * be initiated and added to the internal facadeMap
   * @param <TBean> extends entity bean
   * @param entityClass the entity bean class
   * @return the registered EntityFacade
   * @exception NullPointerException if enityClass = null.
   * @exception IllegalArgumentException if the facade must be initiated and one of
   * the components is not defined (see {@linkplain #addFacade(java.lang.Class)
   * addFacade}).
   */
  @SuppressWarnings("unchecked")
  public <TBean extends Serializable> EntityFacade<TBean>
          getFacade(Class<TBean> entityClass) {
    EntityFacade<TBean> result = null;
    if (entityClass == null) {
      throw new NullPointerException("The Facade's Entity Class reference is "
              + "unassigned.");
    } else if (this.containsFacade(entityClass)) {
      result = facadeMap.get(entityClass);
    } else {
      result = this.addFacade(entityClass);
    }
    return result;
  }
  
  /**
   * Check if the EntityFacade for a specified Entity Class has already been registered.
   * @param entityClass the specified EntityClass
   * @return true if found.
   */
  public boolean containsFacade(Class<? extends Serializable> entityClass) {
    return ((this.facadeMap == null) || (entityClass == null))?
            false: facadeMap.containsKey(entityClass);
  }
     
  /**
   * Set the flag indicating if the persistent unit use JTA transaction
   * (default = null|true)
   * @param doUseJTA the flag setting
   */
  protected void setUseJTA(boolean doUseJTA) {
    this.useJTA = doUseJTA;
  }
     
  /**
   * Get the flag indicating if the persistent unit use JTA transaction
   * (default = null|true)
   * @return ((this.useJTA == null) || (this.useJTA))
   */
  public boolean doUseJTA() {
    return ((this.useJTA == null) || (this.useJTA));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="PrimaryKey FilterManagement">
  /**
   * Get the PrimaryKey of the EntityFacade of the specified bean class.
   * @param <TBean> extends Serializable
   * @param beanClass the specified bean class
   * @return the primaryKey EntityWrapper is the EntityFacade and has an assigned
   * primaryKey.
   */
  public <TBean extends Serializable> EntityWrapper<TBean>
                                              getPkFilter(Class<TBean> beanClass) {
    EntityWrapper<TBean> result = null;
    try {
      EntityFacade<TBean> facade = this.getFacade(beanClass);
      if (facade != null) {
        result = facade.getPkFilter();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the PrimaryKey of the EntityFacade of the specified bean class.
   * @param <TBean> extends Serializable
   * @param beanClass the specified bean class
   * @return the true primaryKey filter for the bean class is set.
   */
  public <TBean extends Serializable> boolean hasPkFilter(Class<TBean> beanClass) {
    boolean result = false;
    try {
      if (this.containsFacade(beanClass)) {
        EntityFacade<TBean> facade = this.getFacade(beanClass);
        if (facade != null) {
          result = facade.hasPkFilter();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.hasPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the PrimaryKey of the EntityFacade of the specified bean class.
   * @param <TBean> extends Serializable
   * @param beanClass the specified bean class
   * @return the primaryKey EntityWrapper is the EntityFacade and has an assigned
   * primaryKey.
   */
  public <TBean extends Serializable> void
          setPkFilter(Class<TBean> beanClass, EntityWrapper<TBean> primKey) {
    try {
      EntityFacade<TBean> facade = this.getFacade(beanClass);
      if (facade == null) {
        throw new IllegalStateException("Initiating a EntityFacade for Class[" +
                beanClass.getSimpleName() +"] failed.");
      }
      facade.setPkFilter(primKey);
    } catch (IllegalStateException pExp) {
      logger.log(Level.WARNING, "{0}.setPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
          
    }
  }
  
  /**
   * Get the PrimaryKey of the EntityFacade of the specified bean class.
   * @param <TBean> extends Serializable
   * @param beanClass the specified bean class
   * @return the primaryKey EntityWrapper is the EntityFacade and has an assigned
   * primaryKey.
   */
  public <TBean extends Serializable> void resetPkFilter(Class<TBean> beanClass) {
    try {
      if (this.containsFacade(beanClass)) {
        EntityFacade<TBean> facade = this.getFacade(beanClass);
        if (facade != null) {
          facade.resetPkFilter();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.resetPkFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * ABSTRACT: Called to get the PuEntityManager's EntityManager. Implementation can
   * call {@linkplain #onGetEntityManager() this.onGetEntityManager} to initiate an
   * EntityManager using a local resource (this.useJTA = false).
   * @return the PuEntityManager's EntityManager
   */
  public abstract EntityManager getEntityManager();
  
  /**
   * CAN OVERRIDE: Called to initiate an EntityManager the PuManager's 
   * EntityManagerFactory by calling {@linkplain Persistence#createEntityManagerFactory(
   * java.lang.String) Persistence.createEntityManagerFactory(this.unitName)} and the 
   * call the factory's {@linkplain EntityManagerFactory#createEntityManager() 
   * createEntityManager} method to initiate the EntityManager. 
   * <p>
   * <b>NOTE:</b> It also set this.usJTA = false;</p>
   * @return the initiated EntityManager.
   * @throws IllegalArgumentException is the process failed.
   */
  protected EntityManager onGetEntityManager() {
    EntityManager result = null;
    if (this.emf == null) {
      throw new NullPointerException("The EntityManager Factory is not accessible.");
    }
    try{
      result = emf.createEntityManager();
      if (result == null) {
        throw new Exception("The EntityManager[unitName=" 
                + this.unitName + "] is not currently accessible.");
      }
      this.useJTA = false;
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onGetEntityManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(this.getClass().getSimpleName() + 
              ".onGetEntityManager Error:\n" + exp.getMessage() );
    }
    return result;
  }
  
  /**
   * Call to get the PuManager's user transaction used by the facade - if applicable.
   * The transaction is lookup by calling 
   * InitialContext.doLookup("java:comp/UserTransaction"). If not applicable this 
   * call will return null and a information log will be entered.
   * @return the injected UserTransaction (can be null).
   */
  public UserTransaction getUserTransaction() {
    if (this.userTx == null) {
      try {
        this.userTx = 
              (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
      } catch (Exception exp) {
      }
      
      if(this.userTx == null){
        try {
          ApplicationContext appCtx = null;
          if (this.txName != null && 
            ((appCtx = SessionHelper.getApplicationContext()) != null)){
            this.userTx = (UserTransaction) appCtx.getBean(this.txName);
          }
        } catch (Exception exp) {
        }
      }
      
      if (this.userTx == null) {
        logger.log(Level.INFO, "The UserTransaction is not accessible");
      }
    }
    return this.userTx;
  }
  //</editor-fold>
}
