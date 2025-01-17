package bubblewrap.app.context;

//import bubblewrap.navigation.core.NavigationInfo;
//import bubblewrap.pages.context.ViewContext;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * <p>The BwAppExtension class is an abstract class for registering a BubbleWrap
 * extension's Delegate Classes, Application Parameters, {@linkplain NavigationInfo}, 
 * {@linkplain ViewContext}, Workflows, and AppActions to the {@linkplain BwAppContext}. 
 * Inheritors must override this class' abstract components to handle the registration 
 * of the above components.
 * </p>
 * <p>The extension class must also be setup with the following design interface tags:
 * </p><ul>
 *  <li>{@linkplain Singleton @Singleton}</li>
 *  <li>{@linkplain Startup @Startup}</li></ul>
 *  <li>{@linkplain DependsOn @DependsOn(value={"BwAppContext",....})} - listing the 
 *    BwAppContext and any other BwAppExtension on which this BwAppExtension depends.
 *  </li>
 * </ul>
 * <p>The extension class also have a private, protected, or public method with a
 * {@linkplain PostConstruct @PostConstruct} interface tag, which must call {@linkplain 
 * #registerExtension() this.registerExtension()} to register this extension to the
 * BwAppContext.</p>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
//@Startup
//@DependsOn(value={"BwAppContext"})
public abstract class BwAppExtension implements Serializable {  
  
  //<editor-fold defaultstate="collapsed" desc="Shared Static Log">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                      Logger.getLogger(BwAppExtension.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected BwAppExtension() {    
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Method">
  /**
   *
   */
  protected final void registerExtension() {
    BwAppContext.addAppExtension(this);
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="expanded" desc="Public Abstract Methods">
  /**
   * ABSTRACT: Called by the BwAppContext when a call to it's {@linkplain
   * BwAppContext#clearAppContext() clearAppContext} method is triggered. Extensions
   * are called in reverse registration order.
   * @param appCtxa reference to the Application Context.
   * @throws Exception is any fatal error - it will be logged, but will not interrupt
   * the clearAppContext process.
   */
  public abstract void clearContext(BwAppContext appCtx) throws Exception;

  /**
   * <p>
   * ABSTRACT: Must be implement to register any delegate classes by calling the
   * {@linkplain BwAppContext#setDelegateClass(java.lang.Class, java.lang.Class)
   * pAppCtx.setDelegateClass} method.</p>
   * @param appCtx a reference to the Application Context.
   * @throws Exception is any fatal error to prevent application form completing the
   * registration
   */
  public abstract void registerDelegates(BwAppContext appCtx) throws Exception;

  /**
   * <p>
   * ABSTRACT: Must be implement to register new application parameters or to
   * override core parameters with extension specific settings by calling the
   * {@linkplain BwAppContext#setParameter(java.lang.String, java.lang.String)
   * pAppCtx.setParameter} method.</p>
   * @param appCtx a reference to the Application Context.
   * @throws Exception is any fatal error to prevent application form completing the
   * registration
   */
  public abstract void registerAppParameters(BwAppContext appCtx) throws Exception;

  /**
   * <p>
   * ABSTRACT: Must be implement to register new Entity Context or to
   * override core Entity Context with extension specific settings by calling the
   * {@linkplain BwAppContext#setEntityContext(bubblewrap.entity.context.EntityContext)
   * pAppCtx.setEntityContext} method.</p>
   * @param appCtx a reference to the Application Context.
   * @throws Exception is any fatal error to prevent application form completing the
   * registration
   */
  public abstract void registerEntityContext(BwAppContext appCtx) throws Exception;

  /**
   * <p>
   * ABSTRACT: Must be implement to register new Workflow or HttpRequest Actions
   * handlers. An actionHandler is registered by calling the null   {@linkplain 
   * BwAppContext#registerActionHandler(java.lang.Class, java.lang.Class,
   * java.lang.String) pAppCtx.registerActionHandler} method.</p>
   * @param appCtx a reference to the Application Context.
   * @throws Exception is any fatal error to prevent application form completing the
   * registration
   */
  public abstract void registerActionHandlers(BwAppContext appCtx) throws Exception;

  /**
   * <p>
   * ABSTRATC: Must be implement to handle the initiation of any local context that
   * are not registered with the BwAppContext. This call is make to all AppExtensions
   * after all the BwAppContext is successfully registered.</p>
   * @param appCtxa reference to the Application Context.
   * @throws Exception is any fatal error to prevent application form completing the
   * registration
   */
  public abstract void registerLocalContext(BwAppContext appCtx) throws Exception;
  // </editor-fold>
}
