package bubblewrap.app.context;

import java.util.logging.Level;

/**
 * This is the BwAppExtension to register the BubbleWrap Core Library settings.  There 
 * is no need to add this BwAppExtension in the DependOn path. It is automatically added
 * to to the BwAppContext's AppExtention list.
* @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
  */
public class BwCoreExtension extends BwAppExtension {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwCoreExtension() {  
    super();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement BwAppExtension">
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: does nothing</p>
   */
  @Override
  public void clearContext(BwAppContext appCtx) throws Exception {
    //throw new UnsupportedOperationException("Not supported yet.");
  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: does nothing</p>
   */
  @Override
  public void registerDelegates(BwAppContext pAppCtx) {
    //throw new UnsupportedOperationException("Not supported yet.");
  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: </p>
   */
  @Override
  public void registerAppParameters(BwAppContext pAppCtx) throws Exception  {
 
  }
  
//  /**
//   * {@inheritDoc }
//   * <p>IMPLEMENT: </p>
//   */
//  @Override
//  public void registerNavigationInfo(BwAppContext appCtx) throws Exception  {
////    NavigationTarget navTrg = null;
////    NavigationInfo navInfo = null;
////    PageUrlInfo pageInfo = null;
////    PageUrlInfo hostTmpInfo = null;
////    AppTasks appTask = null;
////    String urlPath = null;
////    String submitPath = null;
////    Class<? extends PageView> viewClass = null;
////    boolean doAccess = false;
////    boolean noHistory = false;
////    try {
////
////    } catch (Exception pExp) {
////      logger.log(Level.WARNING, "{0}.registerNavigationInfo Error:\n {1}", 
////              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
////      throw pExp;
////    }
//  }
//  
//  /**
//   * {@inheritDoc }
//   * <p>IMPLEMENT: Register the following classes: {AppPageView, AlertView,..}</p>
//   */
//  @Override
//  public void registerViewContext(BwAppContext appCtx) throws Exception  {
//    if (appCtx == null) {
//      return;
//    }
//    JarPageViewLoader jarLoader = new JarPageViewLoader("bubblewrap.pages.views");
//    if (jarLoader != null) {
//      appCtx.registerPageViews(jarLoader);
//    }
//    
////    NavigationTarget navTrg = null;
////    ViewContext viewCtx = null;
////    AppTasks appTask = null;
////    
//    /* Register View[AppPageView] */
////    appTask= AppTasks.CNTMNG;
////    navTrg = AppPageView.getNavigationTarget();
////    viewCtx = new ViewContext(AppPageView.class, appTask, navTrg);
////    appCtx.setViewContext(viewCtx);
////    
////    /* Register View[AlertView] */
////    appTask= AppTasks.CNTMNG;
////    navTrg = AlertView.getNavigationTarget();
////    viewCtx = new ViewContext(AlertView.class, appTask, navTrg);
////    appCtx.setViewContext(viewCtx);
////    
////    /* Register View[AppPageView] */
////    appTask= AppTasks.CNTMNG;
////    navTrg = AppContextView.getNavigationTarget();
////    viewCtx = new ViewContext(AppContextView.class, appTask, navTrg);
////    appCtx.setViewContext(viewCtx);
//  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Do nothing - the core application has no entity beans</p>
   */
  @Override
  public void registerEntityContext(BwAppContext pAppCtx) throws Exception  {
   
  }

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT:Register The following Classes: SessionExpiredActionHandler,
   * BrowserNotSupportedActionHandler.</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void registerActionHandlers(BwAppContext appCtx) throws Exception {    
//    Class<? extends AppActionHandler> appActionClass = null;
//    String actionId = null;
//    try {
//      /** Register AppActionHandler[SessionExpiredActionHandler] **/
//      appActionClass = SessionExpiredActionHandler.class;
//      actionId = SessionExpiredActionHandler.ActionId;
//      appCtx.registerActionHandler(AppActionHandler.class, appActionClass, actionId);
//      
//      /** Register AppActionHandler[SessionExpiredActionHandler] **/
//      appActionClass = BrowserNotSupportedActionHandler.class;
//      actionId = BrowserNotSupportedActionHandler.ActionId;
//      appCtx.registerActionHandler(AppActionHandler.class, appActionClass, actionId);
//    } catch (Exception pExp) {
//      logger.log(Level.WARNING, "{0}.registerActionHandlers Error:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
//    
//    Class<? extends BwAjaxActionHandler> ajaxActionClass = null;
//    try {
//      /** Register BwAjaxActionHandler[NavEventActionHandler] **/
//      ajaxActionClass = NavEventActionHandler.class;
//      actionId = NavEventActionHandler.ActionId;
//      appCtx.registerActionHandler(BwAjaxActionHandler.class, 
//                                                            ajaxActionClass, actionId);
//    } catch (Exception pExp) {
//      logger.log(Level.WARNING, "{0}.registerActionHandlers Error:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Does nothing</p>
   */
  @Override
  public void registerLocalContext(BwAppContext appCtx) throws Exception {
    //throw new UnsupportedOperationException("Not supported yet.");
  }
  //</editor-fold>
}
