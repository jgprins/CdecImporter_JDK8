package bubblewrap.app.context;

import bubblewrap.app.context.events.BwExtensionEventArgs;
import bubblewrap.app.context.events.BwExtensionEventDelegate;
import bubblewrap.app.context.events.BwExtensionEventHandler;
import bubblewrap.core.annotations.LookupMethod;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.interfaces.IEntityLoader;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.context.*;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;

/**
 * <p>
 * This is a Singleton class for managing the Application Context settings.
 * string the Application Configuration Parameters in
 * a HashMap<String,String>. Each application should hasParameter a derive class that
 * is declared as a Singleton EJB, which implies it must include the following class
 * interfaces: </p><ul>
 * <li>{@linkplain Singleton @Singleton}</li>
 * <li>{@linkplain Startup @Startup}</li></ul>
 * <p>
 * The Implementation class must comply with the following requirements:</p>
 * <ul>
 * <li>The Application specific EJB's class name must be set to "MyAppContext".
 * Several standard FacePage and XHTML tool reference MyAppContext.</li>
 * <li>The MyAppContext class must override both initAppContext and
 * destroyAppContext in assigned it @PostConstruct and @PreDestroy tags to these two
 * overridden method, respectively.</li>
 * <li>Override the following method to initiate the Application specific Context:<ol>
 * <li><b>onInitLocalContext:</b> see {@linkplain #initLocalContext()
 *      initLocalContext} for details</li>
 * <li><b>onInitLocalNavigationInfo:</b> see {@linkplain #initLocalNavigationInfo()
 *      initLocalNavigationInfo} for details</li>
 * <li><b>onInitLocalWorkflowInfo:</b> see {@linkplain #initLocalWorkflowInfo()
 *      initLocalWorkflowInfo} for details</li>
 * <li><b>onInitLocalAppActionHandlers:</b> see {@linkplain
 *    #initLocalAppActionHandlers() initLocalAppActionHandlers} for details</li>
 * </ol>
 * </li>
 * </ul>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Startup
@Singleton(name = "BwAppContext")
public class BwAppContext {

  //<editor-fold defaultstate="collapsed" desc="Private/Protected Static">
  /**
   * Placeholder for the application's Static BwAppContext instance.
   */
  private static BwAppContext _singleton = null;
  /**
   * Static Error Logger for the Facade Class
   */
  protected static final Logger logger
          = Logger.getLogger(BwAppContext.class.getSimpleName());
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * <p>
   * Lookup the Application Singleton Enterprise Java Bean (EJB)</p>
   * <p>
   * <b>Note:</b> This method should not be called during the Application Startup,
   * because calling it will trigger the registration of all extension - a task
   * intended for the first time a Session call this method.</p>
   * @return a reference to the BwAppContext instance
   */
  @LookupMethod
  public synchronized static BwAppContext doLookup() {
    BwAppContext result = null;
    try {
      result = BwAppContext._singleton;
      if (result == null) {
        throw new Exception("Unable to access the shared BubbleWrap Application Context");
      }
      result.registerAppContext();
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "BwAppContext.doLookup Error: \n{0}",
              exp.getMessage());
    }
    return result;
  }

  /**
   * <p>
   * Initiate a BwAppContext for testing purposes.</p>
   * <p>
   * <b>Note:</b> This method should not be called other than from a test unit.</p>
   * @return a reference to the BwAppContext instance
   */
  public static BwAppContext initTestInstance() {
    if (BwAppContext._singleton == null) {
      try {
        BwAppContext appCtx = new BwAppContext();
        if (appCtx == null) {
          throw new Exception("Initiating a new BwAppContext instance failed");
        }
        appCtx.initAppContext();
      } catch (Exception exp) {
        logger.log(Level.SEVERE, "BwAppContext.initInstance Error: \n{0}",
                exp.getMessage());
      }
    }
    return BwAppContext._singleton;
  }

  /**
   * Called to add a Application Specific Extension to the BwAppContext during initiation
   * before the BwAppContext's context is {@linkplain #registerAppContext() registered}.
   * <p>
   * @param appExt the new extension to add
   */
  public static void addAppExtension(BwAppExtension appExt) {
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap Application Context");
      }

      BwAppContext._singleton.onAddAppExtension(appExt);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "BwAppContext.addAppExtension Error: \n{0}",
              exp.getMessage());
    }
  }

  /**
   * Called to add a Application Specific Extension to the BwAppContext during initiation
   * before the BwAppContext's context is {@linkplain #registerAppContext() registered}.
   * <p>
   * @param appExt the new extension to add
   */
  public static void addEntityManager(String puName, EntityManager entityMngr) {
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap Application Context");
      }

      BwAppContext._singleton.onSetEntityManager(puName, entityMngr);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "BwAppContext.addEntityManager Error: \n{0}",
              exp.getMessage());
    }
  }

  /**
   * Called to add the <tt>delegate</tt> for an event listener to the BwAppContext's
   * {@linkplain #registerAppContextHandler registerAppContext} events.   * 
   * <b>NOTE:</b> This event is fired for each registered {@linkplain BwAppExtension} 
   * during the processing BwAppContext's {@linkplain #registerAppContext() 
   * registerAppContext} method. The events are fired AFTER all base BwAppContext content
   * has been registered.</p>
   * @param delegate the event listener's eventHandler delegate (ignored if null)
   */
  public static void addRegisterContextListener(BwExtensionEventDelegate delegate) {
    if (delegate == null) {
      return;
    }    
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap Application Context");
      }

      BwAppContext._singleton.addToRegisterAppContext(delegate);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.addRegisterContextListener Error: \n{1}",
              new Object[]{BwAppContext.class.getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Called to add the <tt>listener</tt> for an event listener to the BwAppContext's
   * {@linkplain #clearAppContextHandlerthis clearAppContext} event.
   * <b>NOTE:</b> This event is fired when the BwAppContext's {@linkplain 
   * #clearAppContext() clearAppContext} method is called </p>
   * @param delegate the event listener's eventHandler delegate (ignored if null)
   */
  public static void addClearContextListener(EventDelegate delegate) {
    if (delegate == null) {
      return;
    }    
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap Application Context");
      }

      BwAppContext._singleton.addToClearAppContext(delegate);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.addClearContextListener Error: \n{1}",
              new Object[]{BwAppContext.class.getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Called to add the <tt>listener</tt> for an event listener to the BwAppContext's
   * {@linkplain #refreshedContextHandler this.refreshedContextHandler} event.
   * <b>NOTE:</b> This event is fired when the BwAppContext's {@linkplain 
   * #refreshContext() refreshContext} method is completed </p>
   * @param delegate the event listener's eventHandler delegate (ignored if null)
   */
  public static void addRefreshedContextListener(EventDelegate delegate) {
    if (delegate == null) {
      return;
    }    
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap Application Context");
      }

      BwAppContext._singleton.addToRefreshedContext(delegate);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.addRefreshedContextListener Error: \n{1}",
              new Object[]{BwAppContext.class.getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Called to remove the <tt>listener</tt> as an event listener to the BwAppContext's
   * {@linkplain #registerAppContextHandler registerAppContext} and 
   * {@linkplain #clearAppContextHandlerthis clearAppContext} events.
   * <p>
   * @param listener the event listener (ignored if null)
   */
  public static void removeEventListener(Object listener) {
    if (listener == null) {
      return;
    }    
    try {
      if (BwAppContext._singleton == null) {
        throw new Exception("Unable to access the global BubbleWrap "
                + "Application Context");
      }
      if (BwAppContext._singleton.registerAppContextHandler != null) {
        BwAppContext._singleton.registerAppContextHandler.remove(listener);
      }
      if (BwAppContext._singleton.clearAppContextHandler != null) {
        BwAppContext._singleton.clearAppContextHandler.remove(listener);
      }
      if (BwAppContext._singleton.clearAppContextHandler != null) {
        BwAppContext._singleton.clearAppContextHandler.remove(listener);
      }
      if (BwAppContext._singleton.refreshedContextHandler != null) {
        BwAppContext._singleton.refreshedContextHandler.remove(listener);
      }
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.removeEventListener Error: \n{1}",
              new Object[]{BwAppContext.class.getSimpleName(), exp.getMessage()});
    }
  }  
  
  /**
   * Get the Application's Name
   * @return String
   */
  public static final String AppName() {
    String sResult = null;
    BwAppContext appCtx = BwAppContext.doLookup();
    BwAppInfo pAppInfo = (appCtx == null) ? null : appCtx.getAppInfo();
    sResult = (pAppInfo == null) ? null : pAppInfo.getAppName();
    return (sResult == null) ? "Unidentified Application" : sResult;
  }

  /**
   * Get the Application's Administrator Name (e.g. <AppName> +" Administrator".
   * if (sAppName = null), return "Administrator".
   * @return String
   */
  public static String AdministratorName() {
    String sResult = null;
    BwAppContext appCtx = BwAppContext.doLookup();
    BwAppInfo pAppInfo = (appCtx == null) ? null : appCtx.getAppInfo();
    sResult = (pAppInfo == null) ? null : pAppInfo.getAppName();
    return (sResult == null) ? "Administrator" : sResult + " Administrator";
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Application Name (to be assigned by the Application).
   */
  private BwAppInfo appInfo;
  /**
   * A List if registered BubbleWrap Application Extensions
   */
  private List<BwAppExtension> appExtensions;
  /**
   * Placeholder for the Contexts Application Parameters Map
   */
  private HashMap<String, String> appParMap;
  
  /**
   * Placeholder for the Parameters Map containing the registered EntityManager by
   * its PU name.
   */
  private HashMap<String,EntityManager> entityManagerRegs;
  /**
   * Placeholder for the Parameters Map containing the registered Application PuManager
   * to use as substitutes for the Session Level PuManager's referenced by class.
   */
  private HashMap<Class<? extends PuEntityManager>,PuEntityManager> appPuMngRegs;
  /**
   * Placeholder for the Application's EntityContext Registry by Entity Class
   */
  private HashMap<Class<? extends Serializable>, EntityContext> entityCtxRegs;
  /**
   * Placeholder for the Application's ForeignKey Registry by ForeignKey.contextKey
   */
  private HashMap<AssociationPath, ForeignKey> foreignKeyRegs;
  /**
   * Placeholder for the Application's Class Registry
   */
  private BwActionHandlerRegistry actionHandlerReg;
  /**
   * An internal flag controlling the the Context's isInitiated state
   */
  private Boolean initiated;
  /**
   * An internal flag set during the Application Registration process to prevent
   * nested calls
   */
  private boolean registering;
 // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Events">
  /**
   * Called to add a EventDelegate to this.RegisterAppContext EventHandler.
   * @param delegate the event Listener's delegate
   */
  private void addToRegisterAppContext(BwExtensionEventDelegate delegate) {
    if (delegate == null) {
      return;
    }
    if (this.registerAppContextHandler == null) {
      this.registerAppContextHandler = new BwExtensionEventHandler();
    }
    this.registerAppContextHandler.add(delegate);
  }

  /**
   * The EventHandler that fires the Register AppContext Event.
   */
  private BwExtensionEventHandler registerAppContextHandler;

  /**
   * Called to fire the Register AppContext Event.
   *
   * @param eventArg the event info
   */
  private void fireRegisterAppContext(BwExtensionEventArgs eventArg) {
    if (this.registerAppContextHandler != null) {
      this.registerAppContextHandler.fireEvent(this, eventArg);
    }
  }

  /**
   * Called to add a EventDelegate to this.RegisterAppContext EventHandler.
   * @param delegate the event Listener's delegate
   */
  private void addToClearAppContext(EventDelegate delegate) {
    if (delegate == null) {
      return;
    }
    if (this.clearAppContextHandler == null) {
      this.clearAppContextHandler = new EventHandler();
    }
    this.clearAppContextHandler.add(delegate);
  }

  /**
   * The EventHandler that fires the Clear AppContext Event.
   */
  private EventHandler clearAppContextHandler;

  /**
   * Called to fire the Clear AppContext Event.
   *
   * @param eventInfo the event info
   */
  private void fireClearAppContext() {
    if (this.clearAppContextHandler != null) {
      this.clearAppContextHandler.fireEvent(this, new EventArgs());
    }
  }

  /**
   * Called to add a EventDelegate to this.RefreshedAppContex EventHandler.
   * @param delegate the event Listener's delegate
   */
  private void addToRefreshedContext(EventDelegate delegate) {
    if (delegate == null) {
      return;
    }
    if (this.refreshedContextHandler == null) {
      this.refreshedContextHandler = new EventHandler();
    }
    this.refreshedContextHandler.add(delegate);
  }

  /**
   * The EventHandler that fires the Refreshed AppContex Event.
   */
  private EventHandler refreshedContextHandler;

  /**
   * Called to fire the Refreshed AppContext Event.
   *
   * @param eventInfo the event info
   */
  private void fireRefreshedContext() {
    if (this.refreshedContextHandler != null) {
      this.refreshedContextHandler.fireEvent(this, new EventArgs());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protected Parameterless Constructor
   */
  public BwAppContext() {
    super();
    this.registerAppContextHandler = null;
    this.clearAppContextHandler = null;
    this.initiated = false;
    this.appParMap = null;
    this.appInfo = null;
    this.actionHandlerReg = null;
    this.registering = false;
    this.entityManagerRegs = null;
    this.appPuMngRegs = null;
  }

  /**
   * <p>
   * A private PostConstruct method to initiate the BwAppContext's core settings:</p>
   * <ul>
   * <li>It assign this instance as static this.mpAppContext</li>
   * <li>It adds BwCoreExtension to the AppExtension list.</li>
   * </ul>
   */
  @PostConstruct
  protected void initAppContext() {
    String sClass = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.initAppContext Start", sClass);
    try {
      BwAppContext._singleton = this;
      this.onAddAppExtension(new BwCoreExtension());
      logger.log(Level.INFO, "{0}.initAppContext Done", sClass);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initAppContext Failed:\n {1}",
              new Object[]{sClass, exp.getMessage()});
      throw exp;
    }
  }

  /**
   * <p>
   * A private PreDestroy method to reset this.mpAppContext and to clear the
   * Application Context's content. Calling {@linkplain #clearAppContext()
   * clearAppContext}.</p>
   */
  @PreDestroy
  protected void destroyAppContext() {
    String sClass = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.destroyAppContext Start", sClass);
    this.clearAppContext();
    if (this.registerAppContextHandler != null) {
      this.registerAppContextHandler.clear();
      this.registerAppContextHandler = null;
    }
    if (this.clearAppContextHandler != null) {
      this.clearAppContextHandler.clear();
      this.clearAppContextHandler = null;
    }
    BwAppContext._singleton = null;
    logger.log(Level.INFO, "{0}.destroyAppContext Done", sClass);
  }

  /**
   * OVERRIDE: Dispose Local resources before calling the super method
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    this.appParMap = null;
    this.entityCtxRegs = null;
    this.actionHandlerReg = null;
    if (this.registerAppContextHandler != null) {
      this.registerAppContextHandler.clear();
      this.registerAppContextHandler = null;
    }
    if (this.clearAppContextHandler != null) {
      this.clearAppContextHandler.clear();
      this.clearAppContextHandler = null;
    }
    if (this.entityManagerRegs != null) {
      this.entityManagerRegs.clear();
      this.entityManagerRegs = null;
    }
    if (this.appPuMngRegs != null) {
      this.appPuMngRegs.clear();
      this.appPuMngRegs = null;
    }
    super.finalize();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="AppContext Registration Management">
  /**
   * Called by the static method to add a new Application Extension. The call is ignored
   * if (appExt=null) or the extension already exist in the list.
   * @param appExt
   */
  private void onAddAppExtension(BwAppExtension appExt) {
    if ((appExt == null)
            || ((this.appExtensions != null) && (this.appExtensions.contains(appExt)))) {
      return;
    }

    if (this.initiated) {
      this.clearAppContext();
    }

    if (this.appExtensions == null) {
      this.appExtensions = new ArrayList<>();
    }
    this.appExtensions.add(appExt);
  }

  /**
   * <p>
   * Called by {@linkplain #doLookup()} to register the Application Context Setting
   * if not yet registered. This call is ignored if the internal isInitiated flag is
   * already set. Both {@linkplain #initAppContext() this.initAppContext} and
   * {@linkplain #refreshContext() this.refreshContext} calls this method to load or
   * initiate the shared Application settings and resources. This process cycle through
   * the registered {@linkplain #appExtensions this.appExtensions} and calls the
   * context/content registration methods in the following order:</p><ul>
   * <li><b>Step 1: Register Class Delegates</b></li>
   * <li><b>Step 2: Register Entity Context</b></li>
   * <li><b>Step 3: Register AppParameters</b></li>
   * <li><b>Step 4: Register ActionHandlers</b></li>
   * <li><b>Step 6: Register Other Local Context Settings, Content, or Resource.</b>
   * <li><b>Step 4: Register NavigationInfo - not associated with a PageView</b></li>
   * <li><b>Step 5: Register PageView Context and the NavigationInfo Associated with
   * the PageView</b></li>
   * </li>
   * </ul>
   * <p>
   * Any Exceptions will interrupt this process, a message will be logged, and the
   * Application Context's content will be cleared..</p>
   */
  private void registerAppContext() {
    if ((this.initiated) || (this.registering)) {
      return;
    }

    String className = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.registerAppContext Start", className);
    try {
      this.registering = true;

      if ((this.appExtensions == null) || (this.appExtensions.isEmpty())) {
        throw new Exception("The Application Entension List is empty. It has no "
                + "Context setting to register.");
      }

      this.appParMap = new HashMap<>();
      
      /* STEP 1: Register Delegated */
      for (BwAppExtension appExt : this.appExtensions) {
        String extName = appExt.getClass().getSimpleName();
        try {
          appExt.registerDelegates(this);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerDelegates Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }
      /* STEP 2: Register Entity Context */
      for (BwAppExtension appExt : this.appExtensions) {
        try {
          appExt.registerEntityContext(this);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerEntityContext Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }
      /* STEP 3: Register App Parameters */
      for (BwAppExtension appExt : this.appExtensions) {
        String extName = appExt.getClass().getSimpleName();
        try {
          appExt.registerAppParameters(this);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerAppParameters Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }
      
      /* STEP 4: Register ActionHandlers */
      for (BwAppExtension appExt : this.appExtensions) {
        try {
          appExt.registerActionHandlers(this);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerActionHandlers Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }
      /* STEP 5: Register Additional Local Context */
      for (BwAppExtension appExt : this.appExtensions) {
        try {
          appExt.registerLocalContext(this);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerLocalContext Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }

      /* STEP 6: fire RegisterAppContext to Register Sub Context */
      for (BwAppExtension appExt : this.appExtensions) {
        try {
          BwExtensionEventArgs args = new BwExtensionEventArgs(appExt);
          this.fireRegisterAppContext(args);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.registerLocalContext Error:\n {1}",
                  new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
        }
      }

      logger.log(Level.INFO, "{0}.registerAppContext Done", className);

      this.initiated = true;
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.registerAppContext Failed:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      this.clearAppContext();
    } finally {
      this.registering = false;
    }
  }

  /**
   * Internally called by destroyAppContext and refresh to clear the Custom AppContext
   * definitions - it calls onClearMyContext after disposing of all registered
   * parameters and class delegates - to allow custom clearing of any set parameters.
   */
  private void clearAppContext() {
    try {
      this.fireClearAppContext();

      if ((this.appExtensions != null) && (!this.appExtensions.isEmpty())) {
        for (int extIdx = this.appExtensions.size() - 1; extIdx >= 0; extIdx--) {
          BwAppExtension appExt = this.appExtensions.get(extIdx);
          if (appExt != null) {
            try {
              appExt.clearContext(this);
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.clearContext Error:\n {1}",
                      new Object[]{appExt.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      }

      this.appInfo = null;
      if (this.appParMap != null) {
        this.appParMap.clear();
        this.appParMap = null;
      }
      if (this.entityCtxRegs != null) {
        this.entityCtxRegs.clear();
        this.entityCtxRegs = null;
      }
      if (this.actionHandlerReg != null) {
        this.actionHandlerReg.clear();
        this.actionHandlerReg = null;
      }

      logger.log(Level.INFO, "{0}.clearMyContext Completed",
              this.getClass().getSimpleName());
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.clearMyContext Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.initiated = false;
      this.registering = false;
    }
  }

  /**
   * Called by AppParameterView after editing the AppParameter Values to reload the
   * MyAppContext. Call this.clearMyContext(which call this.onClearMyContext) followed
   * by a call to this.onInitBwAppContext (which reload/initiate all settings).
   * Errors are logged.
   * <p>
   * <b>Note:</b>When completed it fires the RefreshedContext event</p>
   */
  public void refreshContext() {
    this.clearAppContext();
    this.registerAppContext();
    this.fireRefreshedContext();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="General Public Methods">
  /**
   * Get the Assigned Application Info
   * @return String
   */
  public BwAppInfo getAppInfo() {
    return this.appInfo;
  }

  /**
   * Set the ApplicationName
   * @param sAppName string
   */
  public void setAppInfo(String sAppName, String sOrgName, String sOrgAcronym,
                         String sEmail, String sTelNo) {
    try {
      this.appInfo = new BwAppInfo(sAppName, sOrgName, sOrgAcronym, sEmail, sTelNo);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setAppInfo Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Generate an HTML Report for displaying the myApPContext settings.
   */
  public String getContextReport() {
    String result = null;
    try {
      result = "<h3>Application Information:</h3><ul>";
      if (this.appInfo == null) {
        result += "<li>No Application Information defined</li>";
      } else {
        result += "<li>Application Name = " + this.appInfo.getAppName() + "</li>";
        result += "<li>Organization Name = " + this.appInfo.getOrgName() + "</li>";
        result += "<li>Organization Acronym = "
                + this.appInfo.getOrgAcronym() + "</li>";
        result += "<li>Organization Contact Email = "
                + this.appInfo.getEmail() + "</li>";
        result += "<li>Organization Contact Telephone Number = "
                + this.appInfo.getTelNo() + "</li>";
      }
      result += "</ul>";

      result += "<h3>Application Parameters:</h3><ul>";
      if ((this.appParMap == null) || (this.appParMap.isEmpty())) {
        result += "<li>No parameters defined</li>";
      } else {
        List<String> pKeyList = new ArrayList<>();
        for (String sKey : this.appParMap.keySet()) {
          pKeyList.add(sKey);
        }

        Collections.sort(pKeyList);
        for (String sKey : pKeyList) {
          result += "<li>Parameter[" + sKey + "] = "
                  + this.appParMap.get(sKey) + "</li>";
        }
      }
      result += "</ul>";

//      result += "<h3>Application Parameters:</h3><ul>";
//      if ((this.navInfoRegs == null) || (this.navInfoRegs.isEmpty())) {
//        result += "<li>No Navigation Information Registered</li>";
//      } else {
//        List<NavigationTarget> navTrgList = new ArrayList<>(this.navInfoRegs.keySet());        
//        Collections.sort(navTrgList, new NavigationTargetComparator());
//        PageUrlInfo urlInfo = null;
//        for (NavigationTarget navTrg : navTrgList) {
//          String sNavTrg = navTrg.toString();
//          NavigationInfo pInfo = this.navInfoRegs.get(navTrg);
//          result += "<li><b>NavigationInfo[" + sNavTrg + "]:</b><ul>";
//          result += "<li>Page Title = " + pInfo.getPageTitle()
//                  + "</li>";
//          result += "<li>Fomr Caption = " + pInfo.getFormCaption()
//                  + "</li>";
//          result += "<li>Navigation Crumb Caption = " + pInfo.getCrumbCaption() 
//                  + "</li>";
//          result += "<li>Target PageView = "
//                  + pInfo.getTargetViewClass().getSimpleName() + "</li>";
//          result += "<li>Page Template = "
//                  + pInfo.getPageTemplateClass().getSimpleName() + "</li>";
//          result += "<li>Face Page Url = "
//                  + pInfo.getFacePageUrl(true, true) + "</li>";
//          if ((urlInfo = pInfo.getFacesTemplateUrl()) != null) {
//            result += "<li>Faces Template Url = "
//                  + urlInfo.getPageUrl(true, true) + "</li>";
//          } else {
//            result += "<li>Faces Template Url = unassigned</li>";
//          }
//          result += "<li>Do Access Validation = "
//                  + pInfo.doValidateAccess() + "</li>";
//          result += "<li>Hide NavigationHistory = "
//                  + pInfo.hideNavigationHistory() + "</li>";
//          result += "<li>Is Navigation Root = "
//                  + pInfo.isNavRoot()+ "</li>";
//          result += "</ul></li>";
//        }
//      }
//      result += "</ul>";
      result += "========================================== END OF REPORT "
              + "==========================================";
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getContextReport Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result += "<h3>Error Message</h3>"
              + "<p>Generating the Context Report failde because:</p><ul><li>"
              + exp.getMessage() + "</li></ul>";
    }
    return result;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Managing Delegates Registry">
  /**
   * Called to register a Delegate Class for a Base Class.
   * If pDelegate=null, it is assumed that
   * the delegateClass=baseClass.
   * Call setParamater(pBaseClass.name,pDelegate.name).
   * Throw exception if pBaseClass=null.
   * @param <T>
   * @param baseClass Class<T>
   * @param delegateClass Class<? extends T>
   * @throws Exception
   */
  public <T> void registerDelegateClass(Class<T> baseClass,
                                        Class<? extends T> delegateClass) throws Exception {
    try {
      if (baseClass == null) {
        throw new Exception("The BaseClass cannot be unassigned");
      }

      if (delegateClass == null) {
        delegateClass = baseClass;
      }

      this.setParameter(baseClass.getName(), delegateClass.getName());
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".setDelegate Error:\n " + exp.getMessage());
    }
  }

  /**
   * Get the DelegateClass for baseClass. if none is assigned return baseClass.
   * Throw exception if baseClass is unassigned.
   * @param <T>
   * @param baseClass Class<T>
   * @return Class<? extends T>
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public <T> Class<? extends T> getDelegateClass(Class<T> baseClass)
          throws Exception {
    Class<? extends T> result = null;
    try {
      if (baseClass == null) {
        throw new Exception("The BaseClass cannot be unassigned");
      }

      String className = baseClass.getName();
      if (!this.hasParameter(className)) {
        result = baseClass;
      } else {
        String delegateName = this.getParameter(className);
        if (delegateName == null) {
          result = baseClass;
        } else {
          result = (Class<? extends T>) Class.forName(delegateName);
        }
      }
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getDelegateClass Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Return a instance of Delegate for baseClass based on the settings in the
   * AppContext. If no delegate is set, return the an instance of the base class.
   * pParList and pArgList can be null, but if ParList != null, the pArgList must be
   * not null.
   * @param <T>
   * @param baseClass Class<T>
   * @param parList Class[]
   * @param argList Object[]
   * @return T
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public <T> T getDelegateInstance(Class<T> baseClass,
                                   Class[] parList, Object[] argList) throws Exception {
    T result = null;
    try {
      if (baseClass == null) {
        throw new Exception("The BaseClass cannot be unassigned");
      }

      String className = baseClass.getName();
      Class<? extends T> delegateClass = this.getDelegateClass(baseClass);
      if (delegateClass == null) {
        throw new Exception("Unable to initiate the Delegate Class for Class["
                + className + "]");
      } else if ((delegateClass.getModifiers() & Modifier.ABSTRACT)
              == Modifier.ABSTRACT) {
        throw new Exception("Unable to create an instance. Class["
                + className + "] is an abstract class.");
      }

      if (parList == null) {
        result = delegateClass.newInstance();
      } else {
        Constructor pConst = null;
        String sErrMsg = "";
        try {
          pConst = delegateClass.getConstructor(parList);
        } catch (Exception exp1) {
          pConst = null;
          sErrMsg = "\n" + exp1.getMessage();
        }
        if (pConst == null) {
          throw new Exception("Unable to locate Class[" + className + "].Constructor["
                  + parList.toString() + "]" + sErrMsg);
        }
        result = (T) pConst.newInstance(argList);
      }
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getDelegateInstance Error:\n " + exp.getMessage());
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Managing App. Prameters">
  /**
   * Return the HashMap Key that represent sParName. Return null if sParName is empty
   * or the key does not exists, otherwise return the current key. The search is not
   * case sensitive.
   * @param sParName String
   * @return String
   */
  private String getCleanKey(String sParName) {
    String sCleanKey = null;

    /* Get the Cleaned Parameter from mpParTable.keys */
    sParName = DataEntry.cleanString(sParName);
    if ((sParName != null)
            && (this.appParMap != null) && (!this.appParMap.isEmpty())) {
      for (String sKey : this.appParMap.keySet()) {
        if ((sKey != null) && (sKey.equalsIgnoreCase(sParName))) {
          sCleanKey = sKey;
          break;
        }
      }

      sCleanKey = (sCleanKey == null) ? sParName : sCleanKey;
    }

    return sCleanKey;
  }

  /**
   * Return true if the AppContext hasParameter Parameter[sParName]
   * @param sParName String
   * @return boolean
   */
  public boolean hasParameter(String sParName) {
    return (this.getCleanKey(sParName) != null);
  }

  /**
   * Get the value for Parameter[sParName]. The Key search is not case sensitive.
   * The return value will be null if unassigned or undefined.
   * @param parName String
   * @return String
   */
  public String getParameter(String parName) {
    String result = null;
    try {
      String parKey = this.getCleanKey(parName);
      if (parKey == null) {
        throw new Exception("Unable to locate Parameter[" + parName + "]");
      }

      result = this.appParMap.get(parKey);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getParameter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Get the Boolean Parameter value. Return false if the Parameter is not found or
   * if the Parameter does not support boolean values.
   * @param parName String
   * @return Return the assigned value or null is undefined.
   */
  public Boolean getBoolParameter(String parName) {
    Boolean result = false;
    try {
      String strVal = this.getParameter(parName);
      if (strVal != null) {
        result = DataConverter.toBoolean(strVal);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getBoolParameter Error:\n{1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Get the Integer Parameter value. Return null if the Parameter is not found or
   * if the Parameter does not support Integer values.
   * @param parName String
   * @return Integer
   */
  public Integer getIntParameter(String parName) {
    Integer result = null;
    try {
      String strVal = this.getParameter(parName);
      if (strVal != null) {
        result = DataConverter.toValue(strVal, Integer.class);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getIntParameter Error:\n{1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Get the Boolean Parameter value. Return 0 is not unassigned, or "", or
   * and error occur in parsing the string to integer. Errors are logged.
   * @param parName String
   * @return Double
   */
  public Double getDoubleParameter(String parName) {
    Double result = 0.0;
    try {
      String strVal = this.getParameter(parName);
      if (strVal != null) {
        result = DataConverter.toValue(strVal, Double.class);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getDoubleParameter Error:\n{1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Set the Value for Parameter[parName] It replace the prior set value of an existing
   * parameter
   * @param parName String
   * @param strVal String
   */
  public void setParameter(String parName, String strVal) {
    try {
      String parKey = this.getCleanKey(parName);
      strVal = DataEntry.cleanString(strVal);
      if ((strVal == null) && (parKey != null)) {
        this.appParMap.remove(parKey);
      } else {
        if (parKey == null) {
          parKey = DataEntry.cleanString(parName);
        }
        this.appParMap.put(parKey, strVal);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setParameter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Remove the Key-Value pair if the AppContext haparNameeter the parName
   * @param parName String
   */
  protected void removeParameter(String parName) {
    try {
      String parKey = this.getCleanKey(parName);
      if (parKey != null) {
        this.appParMap.remove(parKey);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.removeParameter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Get the AppContext's current set of Parameter Names.
   * @return Set<String>
   */
  public Set<String> getParameterNames() {
    return this.appParMap.keySet();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Managing Application PuManager Delegates">
  /**
   * Call to registered an Application's EntityManager
   * <p>
   * <b>NOTE:</b> An EntityManager must be injected during the Application 
   * {@linkplain Startup} using {@linkplain Singleton} EJB and cannot be associated 
   * with any Session.</p>
   * @param puName the Persistent Unit Name
   * @param entitMngr the Persistent Unit's EntityManager
   */
  private void onSetEntityManager(String puName, EntityManager entitMngr) {
    String puKey = DataEntry.cleanLoString(puName);            
    if ((puKey == null) || (entitMngr == null)) {
      return;
    }
    try {
      if (this.entityManagerRegs == null) {
        this.entityManagerRegs = new HashMap<>();
      }
      
      if ((!this.entityManagerRegs.isEmpty()) &&
              (this.entityManagerRegs.containsKey(puKey))) {
        throw new Exception("Duplicate EnityManager for PU[" + puKey + "].");
      }
      
      this.entityManagerRegs.put(puKey, entitMngr);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.addAppPuManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to get the registered Application PuEntityManager delegate for the specified
   * <tt>puMngrClass</tt>. This method should only be used be Application Level processes
   * that requires a PuEntityManager that is not associated with any Session.
   * <p>
   * <b>NOTE:</b> An Application PuEntityManager must be initiated and registered during
   * the Application {@linkplain Startup} as a {@linkplain Singleton} EJB and cannot be
   * associated with any Session.</p>
   * @param puMngrClass the Session PuEntityManager class
   * @return the PuEntityManager delegate or null if not found.
   */
  public EntityManager getEntityManager(String puName) {
    EntityManager result = null;
    try {
      String puKey = DataEntry.cleanLoString(puName); 
      if ((puKey != null) && (this.entityManagerRegs != null) &&
              (!this.entityManagerRegs.isEmpty()) &&
              (this.entityManagerRegs.containsKey(puKey))) {
        result = this.entityManagerRegs.get(puKey);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getEntityManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Managing Application PuManager Delegates">
  /**
   * Call to registered an Application PuEntityManager delegate for the
   * specified <tt>puMngrClass</tt>.
   * <p>
   * <b>NOTE:</b> An Application PuEntityManager must be initiated and registered during
   * the Application {@linkplain Startup} as a {@linkplain Singleton} EJB and cannot be
   * associated with any Session.</p>
   * @param puMngrClass
   * @param appDelegate
   */
  public void setAppPuManager(Class<? extends PuEntityManager> puMngrClass,
                              PuEntityManager appDelegate) {
    if ((puMngrClass == null) || (appDelegate == null)) {
      return;
    }
    try {
      if (this.appPuMngRegs == null) {
        this.appPuMngRegs = new HashMap<>();
      }
      
      if ((!this.appPuMngRegs.isEmpty()) &&
              (this.appPuMngRegs.containsKey(puMngrClass))) {
        throw new Exception("Duplicate Delegate for PuManager["
                + puMngrClass.getSimpleName() + "].");
      }
      
      this.appPuMngRegs.put(puMngrClass, appDelegate);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.addAppPuManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to get the registered Application PuEntityManager delegate for the specified
   * <tt>puMngrClass</tt>. This method should only be used be Application Level processes
   * that requires a PuEntityManager that is not associated with any Session.
   * <p>
   * <b>NOTE:</b> An Application PuEntityManager must be initiated and registered during
   * the Application {@linkplain Startup} as a {@linkplain Singleton} EJB and cannot be
   * associated with any Session.</p>
   * @param puMngrClass the Session PuEntityManager class
   * @return the PuEntityManager delegate or null if not found.
   */
  public PuEntityManager getAppPuManager(Class<? extends PuEntityManager> puMngrClass) {
    PuEntityManager result = null;
    try {
      if ((puMngrClass != null) && (this.appPuMngRegs != null) &&
              (!this.appPuMngRegs.isEmpty()) &&
              (this.appPuMngRegs.containsKey(puMngrClass))) {
        result = this.appPuMngRegs.get(puMngrClass);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getAppPuManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Managing EntityContext Registry">
  /**
   * Get the registered EntityContext for the Entity Class.
   * @param <TBean> extends Serializable
   * @param entityClass the Entity Class of interest.
   * @return the registered EntityContext.
   * @exception NullPointerException if no registry entry is found
   */
  @SuppressWarnings("unchecked")
  public <TBean extends Serializable>
          EntityContext<TBean> getEntityContext(Class<TBean> entityClass) {
    EntityContext<TBean> result = null;
    try {
      if ((this.entityCtxRegs == null)
              || (!this.entityCtxRegs.containsKey(entityClass))) {
        throw new NullPointerException("No registered EntityContext for Class["
                + entityClass.getSimpleName() + "].");
      }
      result = this.entityCtxRegs.get(entityClass);
    } catch (NullPointerException exp) {
      logger.log(Level.WARNING, "{0}.getEntityContext Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw exp;
    }
    return result;
  }

  /**
   * Check if EntityContext Registry a registry entry for the Entity Class.
   * @param entityClass the Entity Class of interest.
   * @return true if an registry entry exists.
   */
  public boolean hasEntityContext(Class<? extends Serializable> entityClass) {
    boolean bResult = false;
    if (entityClass != null) {
      try {
        bResult = ((this.entityCtxRegs != null)
                && (this.entityCtxRegs.containsKey(entityClass)));
      } catch (Exception exp) {
        bResult = false;
      }
    }
    return bResult;
  }

  /**
   * <p>
   * Use the specified EntitLoader to locate all entities within the loader's
   * specified path and initiate and register the {@linkplain  EntityContext} for
   * each Entity class</p>
   * <p>
   * <b>Note:</b>EntityContext is registered by the Entity Class' name.</p>
   * @param entityLoader the EntityLoader that can retrieve the Entity Classes and has
   * an assigned {@linkplain PuEntityManager} to assign to the EntityContext.
   */
  @SuppressWarnings("unchecked")
  public void registerPuEntities(IEntityLoader entityLoader) {
    try {
      if (entityLoader == null) {
        throw new NullPointerException("The EntityLoader cannot be unassigned.");
      }

      List<Class> entClassList = entityLoader.getClasses();
      if ((entClassList == null) || (entClassList.isEmpty())) {
        return;
      }

      Class<? extends PuEntityManager> entMngrClass
              = entityLoader.getEntityManagerClass();

      /**
       * Pass#1: Initiate and Register EntityContext for all Entities
       */
      List<EntityContext> puEntities = new ArrayList<>();
      for (Class entClass : entClassList) {
        if (ReflectionInfo.isEntity(entClass)) {
          try {
            if (this.hasEntityContext(entClass)) {
              throw new Exception("Duplicate Entity Class[" + entClass.getName() + "]");
            }

            /* Initiate a new EntityContext for the entity class and assign to the
             * EntityContext registery */
            EntityContext entCtx = EntityContext.newInstance(entClass, entMngrClass);
            if (entCtx != null) {
              this.setEntityContext(entCtx);
              puEntities.add(entCtx);
            }
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.registerEntity Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }
      }

      /**
       * Pass#2: Initiate and Register ForeignKey Association for all EntityContext
       */
      for (EntityContext entCtx : puEntities) {
        entCtx.registerAssociations(this);
      }

      /**
       * Pass#3: Resolve all OneToMany ChildClass references
       */
      for (EntityContext entCtx : puEntities) {
        entCtx.registerChildForeignKeys(this);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.registerEntity Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * <p>
   * Add the {@linkplain EntityContext} pContext to the EntityContext Registry. If
   * the Entity has been previously registered, the previous EntityContext will be
   * overridden. The process is ignored if pContext is unassigned.</p>
   * <p>
   * <b>Note:</b>EntityContext is registered by the Entity Class' name.</p>
   * @param pContext the EntityContext to registers.
   */
  @SuppressWarnings("unchecked")
  private void setEntityContext(EntityContext pContext) {
    Class<? extends Serializable> entityClass
            = (pContext == null) ? null : pContext.getEntityClass();
    if (entityClass == null) {
      return;
    }

    if (this.entityCtxRegs == null) {
      this.entityCtxRegs = new HashMap<>();
    }
    this.entityCtxRegs.put(entityClass, pContext);
  }

  /**
   * Called to generate a EntityContext Report of all registered Entity Classes.
   * @return the report as an HTML formatted string
   */
  public String getEntityContextReport() {
    String result = "<h2 class='bwReport'>Entity Context Report</h2>";
    if (this.entityCtxRegs.isEmpty()) {
      result += "- The Entity Context Registry is Empty.";
    } else {
      for (Class<? extends Serializable> entClass : this.entityCtxRegs.keySet()) {
        EntityContext entCtx = this.entityCtxRegs.get(entClass);
        result += entCtx.toHtmlString();
        List<ForeignKey> fkInfoList = this.getForeignKeysByChild(entClass);
        if ((fkInfoList != null) && (!fkInfoList.isEmpty())) {
          result += "<li><h3 class='bwReport'>Entity[" + entClass.getSimpleName()
                  + "] Parent Associations: </h3><ul>";
          for (ForeignKey fKey : fkInfoList) {
            AssociationDef childAssoc = fKey.childAssociation;
            AssociationDef parentAssoc = fKey.parentAssociation;
            result += "<li>Assocoiation Type =" + fKey.associationPath.fkType
                    + "</li>"
                    + "<li>Child Association:<ul>";
            result += "<li>ChildPath = " + childAssoc.targetPath.toString() + "</li>";
            result += "<li>Cascade = " + childAssoc.cascade.toString() + "</li>";
            result += "<li>isNullable = " + childAssoc.isNullable + "</li>";
            result += "<li>IsOwnerParemt = " + childAssoc.isOwnerParent() + "</li>";
            result += "</ul></li>"
                    + "<li>Parent Association:<ul>";
            result += "<li>ParentPath = " + parentAssoc.targetPath.toString() + "</li>";
            result += "<li>Cascade = " + parentAssoc.cascade.toString() + "</li>";
            result += "</ul></li>";
            result += "</ul></li>";
          }
          result += "</ul>";
        }
        fkInfoList = this.getForeignKeysByParent(entClass);
        if ((fkInfoList != null) && (!fkInfoList.isEmpty())) {
          result += "<li><h3 class='bwReport'>Entity[" + entClass.getSimpleName()
                  + "] Child Associations: </h3><ul>";
          for (ForeignKey fKey : fkInfoList) {
            AssociationDef childAssoc = fKey.childAssociation;
            AssociationDef parentAssoc = fKey.parentAssociation;
            result += "<li>Assocoiation Type =" + fKey.associationPath.fkType
                    + "</li>"
                    + "<li>Parent Association:<ul>";
            result += "<li>ParentPath = " + parentAssoc.targetPath.toString() + "</li>";
            result += "<li>Cascade = " + parentAssoc.cascade.toString() + "</li>";
            result += "</ul></li>"
                    + "<li>Child Association:<ul>";
            result += "<li>ChildPath = " + childAssoc.targetPath.toString() + "</li>";
            result += "<li>Cascade = " + childAssoc.cascade.toString() + "</li>";
            result += "<li>isNullable = " + childAssoc.isNullable + "</li>";
            result += "<li>IsOwnerParemt = " + childAssoc.isOwnerParent() + "</li>";
            result += "</ul></li>";
            result += "</ul></li>";
          }
          result += "</ul>";
        }
      }
    }
    result += "-- End of Entity Context Report --<br/><hr/>";
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Managing Foreignkey Registry">  
  /**
   * Get all the AssociationPaths with matching Parent Class.
   * @param parentClass the parent class to search for
   * @return the list of associations or an empty list if none is found.
   */
  private List<AssociationPath> getAssociationsByParent(Class parentClass) {
    List<AssociationPath> result = new ArrayList<>();
    if ((parentClass != null)
            && (this.foreignKeyRegs != null) && (!this.foreignKeyRegs.isEmpty())) {
      for (AssociationPath assoc : this.foreignKeyRegs.keySet()) {
        if (assoc.isParent(parentClass)) {
          result.add(assoc);
        }
      }
    }
    return result;
  }

  /**
   * Get all the AssociationPaths with matching Child Class.
   * @param childClass the child class to search for
   * @return the association or null if not found
   */
  private List<AssociationPath> getAssociationsByChild(Class childClass) {
    List<AssociationPath> result = new ArrayList<>();
    if ((childClass != null)
            && (this.foreignKeyRegs != null) && (!this.foreignKeyRegs.isEmpty())) {
      for (AssociationPath assoc : this.foreignKeyRegs.keySet()) {
        if (assoc.isChild(childClass)) {
          result.add(assoc);
        }
      }
    }
    return result;
  }

  /**
   * Get the registered ForeignKeyConstraint for the parent EntiForeignKeyparentPath
   * the parent EntityPath of interest.
   * @return the registered ForeignKeyConstraint or null if not found.
   */
  public ForeignKey getForeignKeyForParentPath(EntityPath parentPath) throws Exception {
    ForeignKey result = null;
    try {
      List<ForeignKey> parentFKeys = null;
      if ((parentPath != null)
              && ((parentFKeys = this.getForeignKeysByParent(parentPath.entityClass)) != null)
              && (!parentFKeys.isEmpty())) {
        for (ForeignKey foreignKey : parentFKeys) {
          if (foreignKey.isParent(parentPath)) {
            result = foreignKey;
            break;
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getForeignKeyForParentPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw exp;
    }
    return result;
  }

  /**
   * Get a list of ForeignKeyInfos registered to the parent Entity Class.
   * @param childClass the parent Entity Class of interest.
   * @param fkField the foreign key field name of interest.
   * @return the list of ForeignKeyInfos or an empty list if none has been found.
   */
  public List<ForeignKey> getForeignKeysByParent(Class parentClass) {
    List<ForeignKey> result = new ArrayList<>();
    try {
      List<AssociationPath> assocPaths = null;
      if ((parentClass != null)
              && ((assocPaths = this.getAssociationsByParent(parentClass)) != null)
              && (!assocPaths.isEmpty())) {
        ForeignKey fKey = null;
        for (AssociationPath assocPath : assocPaths) {
          if ((fKey = this.foreignKeyRegs.get(assocPath)) != null) {
            result.add(fKey);
          }
        }

        if (result.size() == 1) {
          fKey = result.get(0);
          if (!fKey.isOwnerParent()) {
            fKey.setOwnerParent(true);
          }
        }
      }
    } catch (NullPointerException exp) {
      logger.log(Level.WARNING, "{0}.getForeignKeysByParent Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.clear();
    }
    return result;
  }

  /**
   * Get the registered ForeignKeyConstraint for the child Entity Path.
   * @param childPath the parent Entity Path of interest.
   * @param fkForeignKeyy field name of interest.
   * @return the registered ForeignKeyConstraint or null if not found.
   */
  public ForeignKey getForeignKeyForChildPath(EntityPath childPath) {
    ForeignKey result = null;
    try {
      List<ForeignKey> childFKeys = null;
      if ((childPath != null)
              && ((childFKeys = this.getForeignKeysByChild(childPath.entityClass)) != null)
              && (!childFKeys.isEmpty())) {
        for (ForeignKey foreignKey : childFKeys) {
          if (foreignKey.isChild(childPath)) {
            result = foreignKey;
            break;
          }
        }
      }
    } catch (NullPointerException exp) {
      logger.log(Level.WARNING, "{0}.getForeignKeyForChildPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Get a list of ForeignKeyInfos registered to the child Entity Class.
   * @param childClass the parent Entity Class of interest.
   * @param fkField the foreign key field name of interest.
   * @return the list of ForeignKeyInfos or an empty list if none has been found.
   */
  public List<ForeignKey> getForeignKeysByChild(Class childClass) {
    List<ForeignKey> result = new ArrayList<>();
    try {
      List<AssociationPath> assocPaths = null;
      if ((childClass != null)
              && ((assocPaths = this.getAssociationsByChild(childClass)) != null)
              && (!assocPaths.isEmpty())) {
        ForeignKey fKey = null;
        for (AssociationPath assocPath : assocPaths) {
          if ((fKey = this.foreignKeyRegs.get(assocPath)) != null) {
            result.add(fKey);
          }
        }
      }
    } catch (NullPointerException exp) {
      logger.log(Level.WARNING, "{0}.getForeignKeysByChild Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      result.clear();
    }
    return result;
  }

  /**
   * Get a registered ForeignKeyConstraint by its parent and child class.
   * @param parentClass the specified parent Class
   * @param childClass the specified child Class
   * @return the registered ForeignKey or null if not found.
   */
  public ForeignKey getForeignKey(Class parentClass, Class childClass) {
    ForeignKey result = null;
    AssociationPath assocPath
            = new AssociationPath(parentClass, childClass, AssociationType.None);
    if ((assocPath != null)
            && (this.foreignKeyRegs != null) && (this.foreignKeyRegs.containsKey(assocPath))) {
      result = this.foreignKeyRegs.get(assocPath);
    }
    return result;
  }

  /**
   * <p>
   * Register the {@link ForeignKey} to the ForeignKeyConstraint Registry.
   * If the ForeignKeyConstraint has been previously registered, the previous
   * ForeignKeyConstraint will be overridden. The process is ignored if foreignKeyInfo
   * is unassigned.</p>ForeignKeyraint the ForeignKeForeignKeyters.
   */
  public void setForeignKey(ForeignKey foreignKey) {
    if (foreignKey == null) {
      return;
    }

    if (this.foreignKeyRegs == null) {
      this.foreignKeyRegs = new HashMap<>();
    }
    this.foreignKeyRegs.put(foreignKey.associationPath, foreignKey);
  }

  /**
   * Check if registered ForeignKeyConstraint exists.
   * @param assocPath the specified AssociationPath to search for
   * @return true if the ForeignKeyConstraint Registry contains the AssociationPath as
   * a key.
   */
  public boolean hasForeignKeyInfo(AssociationPath assocPath) {
    return ((assocPath != null)
            && (this.foreignKeyRegs != null)
            && (this.foreignKeyRegs.containsKey(assocPath)));
  }

  /**
   * Check if registered ForeignKeyConstraint exisForeignKeyentClass the specified parent Class
   * @param chi
   * s the specified child Class
   * @return true if {@link #getForeignKeyConstraint(java.lang.Class, java.lang.Class)
   * this.getForeignKeyConstraint(parentClass, childClass)} != null.
   */
  public boolean hasForeignKeyInfo(Class parentClass, Class childClass) {
    return (this.getForeignKey(parentClass, childClass) != null);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Manage Action Handlers">  
  /**
   * <p>
   * Register the specified actionHandler class to the BwAppContext ActionHandler
   * Registry from which the class can be retrieved by its BaseClass and actionId.
   * To register a actionHandler to the BwAppContext it must comply to the following
   * guidelines:</p>
   * <ul>
   * <li>It actionId must be unique for its BaseClass</li>
   * <li>The ActionHandler class must be assignable to its BaseClass
   * (i.e. baseClass.isAssignableForm(actionClass) = true)</li>
   * </ul>
   * <p>
   * <b>Note:</b>If a former ActionHandler has been registered with actionId, the
   * previous ActionHandler registration will be override.</p>
   * @param <TBase> the generic reference for the base class.
   * @param baseClass the registry class' base class
   * @param actionClass the registry class.
   * @param actionId the class' unique registrationId
   * @throws Exception if any of the parameters is undefined, or baseClass is not
   * assignable from actionClass. Errors are also logged.
   */
  public <TBase> void registerActionHandler(Class<TBase> baseClass,
                                            Class<? extends TBase> actionClass, String actionId) throws Exception {
    try {
      if (actionClass == null) {
        throw new Exception("The Registry Class is unassigned");
      }
      String sClass = actionClass.getSimpleName();

      if (baseClass == null) {
        throw new Exception("The Registry Class[" + sClass
                + "]' BaseClass is unassigned");
      }

      actionId = DataEntry.cleanString(actionId);
      if (actionId == null) {
        throw new Exception("The Registry Class[" + sClass
                + "]' RegistryId is unassigned");
      }

      if (!baseClass.isAssignableFrom(actionClass)) {
        throw new Exception("The Registry Class[" + sClass
                + "] is not assignable from BaseClass["
                + baseClass.getSimpleName() + "].");
      }

      if (this.actionHandlerReg == null) {
        this.actionHandlerReg = new BwActionHandlerRegistry();
      }

      this.actionHandlerReg.put(baseClass, actionClass, actionId);
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.registerActionHanlder Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw exp;
    }
  }

  /**
   * Get the registered ActionHandler class for the specified baseClass and actionId.
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @param actionId the registryId to check for.
   * @return the registered ActionHandler class or null if not found.
   */
  public <TBase> Class<? extends TBase>
          getActionHandler(Class<TBase> baseClass, String actionId) {
    Class<? extends TBase> result = null;
    if (this.actionHandlerReg != null) {
      result = this.actionHandlerReg.get(baseClass, actionId);
    }
    return result;
  }

  /**
   * Check if the registry contains a ActionHandler Class for the specified baseClass
   * and actionId
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @param actionId the registryId to check for.
   * @return true if a valid ActionHandler registry exists.
   */
  protected <TBase> boolean hasActionHandler(Class<TBase> baseClass, String actionId) {
    boolean bResult = false;
    if (this.actionHandlerReg != null) {
      bResult = this.actionHandlerReg.contains(baseClass, actionId);
    }
    return bResult;
  }

  /**
   * Get all ActionHandler classes that are registered for the specified baseClass
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @return a collection of registered ActionHandler classes or null is none has been
   * registered
   */
  protected <TBase> Collection<Class<? extends TBase>>
          getActionHandlers(Class<TBase> baseClass) {
    Collection<Class<? extends TBase>> result = null;
    if (this.actionHandlerReg != null) {
      result = this.actionHandlerReg.getClasses(baseClass);
    }
    return result;
  }
  //</editor-fold>  

  //<editor-fold defaultstate="collapsed" desc="Manage AppActionHandler registratrions">
//  /**
//   * <p>Initiate the AppActionHandler instance (i.e., by calling the Class' newInstance
//   * method) to get its ActionId. Use the ActionId to register the class to the
//   * AppActionRegistry. If a duplicate key exists, it will be overridden. Ignore the
//   * registration of pHandlerClass=null.</p>
//   * @param pHandlerClass Class<? extends AppActionHandler>
//   * @throws Exception
//   */
//  public void regAppActionHandlers(Class<? extends AppActionHandler> pHandlerClass)
//          throws Exception {
//    if (pHandlerClass == null) {
//      return;
//    }
//    
//    AppActionHandler pHandler = pHandlerClass.newInstance();
//    if (pHandler != null) {
//      if (this.mpAppActionRegistry == null) {
//        this.mpAppActionRegistry = new HashMap<>();
//      }
//      String sActionId = DataEntry.cleanString(pHandler.getActionId());
//      if (sActionId == null) {
//        throw new Exception("Class[" + pHandlerClass.getSimpleName()
//                + "]'s ActionId is uanssigned.");
//      }
//      this.mpAppActionRegistry.put(sActionId.toLowerCase(),pHandlerClass.getName());
//    }
//  }
//  
//  /**
//   * Locate the AppActionHandler class in the AppActionregistry using its ActionId. If
//   * the class is found, initiate and return a new instance of the class. Return null
//   * if the AppActionHandler class cannot be found or initiating the instance failed.
//   * Errors are logged.
//   * @param sActionId the registered AppActionHandler's ActionId
//   * @return a new instance of the class or null.
//   * @throws Exception
//   */
//  @SuppressWarnings("unchecked")
//  public AppActionHandler getAppActionHandler(String sActionId) throws Exception {
//    AppActionHandler result = null;
//    try {
//      sActionId = DataEntry.cleanString(sActionId);
//      sActionId = (sActionId == null)? null: sActionId.toLowerCase();
//      if ((sActionId != null) && (this.mpAppActionRegistry != null)
//              && (this.mpAppActionRegistry.containsKey(sActionId))) {
//        String className = this.mpAppActionRegistry.get(sActionId);
//        Class<? extends AppActionHandler> pClass = null;
//        if (className != null) {
//          try {
//            pClass = (Class<? extends AppActionHandler>) Class.forName(className);
//          } catch (Exception pInExp) {
//            logger.log(Level.WARNING, "{0}.getAppActionHandler.Class.forName "
//                    + "Error:\n {1}",
//                    new Object[]{this.getClass().getSimpleName(), pInExp.getMessage()});
//            throw new Exception("Intiating Class[" + className + "] from the "
//                    + "registered class name failed.");
//          }
//        }
//        if (pClass != null) {
//          try {
//            result = pClass.newInstance();
//          } catch (Exception pInExp) {
//            logger.log(Level.WARNING, "{0}.getAppActionHandler.Class.newInstance "
//                    + "Error:\n {1}",
//                    new Object[]{this.getClass().getSimpleName(), pInExp.getMessage()});
//            throw new Exception("Intiating a new instance of Class[" + className +
//                    "] failed. ");
//          }
//        }
//      }
//    } catch (Exception exp) {
//      logger.log(Level.WARNING, "{0}.getAppActionHandler Error:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
//      throw exp;
//    }
//    return result;
//  }
  //</editor-fold>
          
  //<editor-fold defaultstate="collapsed" desc="Old Code To Be Moved">
  //CONVERT: BwAppContext:  Move to BwAdminExtension
  //  /**
  //   * Internally call be initMyContext to load the AppParameter settings into memory to
  //   * be shared by all users.
  //   */
  //  private void loadAppParameters() {
  //    try {
  //      this.mbLoadedAppParams = false;
  //      this.mbLoadingAppParams = true;
  //
  //
  //      AppContextFacade pFacade =
  //            (AppContextFacade) InitialContext.doLookup("java:module/AppContextFacade");
  //      if (pFacade == null) {
  //        throw new NamingException("Unable to Inject View[AppContextFacade]");
  //      }
  //      pFacade.resetHaparNameeters();
  //
  //      if (pFacade.haparNameeters(this)) {
  //        List<AppParameter> pAppParams = pFacade.findAll();
  //        if ((pAppParams != null) && (!pAppParams.isEmpty())) {
  //          for (AppParameter pPar: pAppParams) {
  //            String parName = pPar.getParamName();
  //            String sParValue = pPar.getParamValue();
  //            this.setParameter(parName, sParValue);
  //          }
  //        }
  //      }
  //    } catch (Exception exp) {
  //      logger.log(Level.WARNING, "{0}.loadAppParameters Error:\n {1}",
  //              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
  //    } finally {
  //      this.mbLoadingAppParams = false;
  //      this.mbLoadedAppParams = true;
  //    }
  //  }
  //</editor-fold>
}
