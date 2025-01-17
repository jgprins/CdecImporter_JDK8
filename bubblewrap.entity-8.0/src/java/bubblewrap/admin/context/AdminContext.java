package bubblewrap.admin.context;

import bubblewrap.admin.enums.AdminParameters;
import bubblewrap.admin.events.LoggedOnEventArgs;
import bubblewrap.admin.events.LoggedOnEventDelegate;
import bubblewrap.admin.interfaces.IAccessValidator;
import bubblewrap.admin.interfaces.IUserInfo;
import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.events.AccessValidationEventArgs;
import bubblewrap.core.events.AccessValidationEventHandler;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import bubblewrap.navigation.core.NavigationTarget;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The AdminContext if a Session ManageBean that provides access to the login user's
 * information and manages the user access validation.</p> 
 * <p>As an EventSender, it send and handles the following events:</p><ul>
 *  <li><b>ONLOGON:</b> - this event after a use is successfully Logged in. No response
 *    is handled.</li>
 *   <li><b>ONLOGGOF:</b> - this event is user selected to logoff. This event is send
 *    to listeners before the AdminContext invalidate the Session (destroy the sessions
 *    and all its contents). Listeners should dispose their Session resources when this
 *    event is fired - especially if the Listener is also listening to an Application
 *    level EventSender.</li>
 * </ul> 
 * <p><b>Note:</b> The AdminContext's {@linkplain #getAccessValidator() 
 * UserAccessValidator} should be registered as a delegate for the UserAccessValidator
 * class in the {@linkplain BwAppContext}.</p>
 * @author kprins
 */
//@ManagedBean(name = "admincontext")
//@SessionScoped
//@Stateful
public abstract class AdminContext<TUser extends IUserInfo, 
                                    TPrincipal extends BwPrincipal<TUser>> 
                                    implements Serializable, IAccessValidator {
  
  // <editor-fold defaultstate="collapsed" desc="Static methods/Fields">
  /**
   * Public static placeholder for the application Administrator account's username.
   * (default = "Administrator")
   */
  public static String adminUsername = "Administrator";
   /**
   * public static placeholder for the Administrator User Account's BwPrincipal
   */
  private static BwPrincipal adminPrincipal = null;
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(AdminContext.class.getName());
 
  /**
   * Public Static method called to get the Session AdminContext instance.
   * Throws a NullPointerExecption if the initiation
   */
  @SuppressWarnings("unchecked")
  public static AdminContext doLookup() {
    AdminContext result = null;
    try {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new Exception("Unable to access the BwAppContext");
      }
      
      Class<? extends AdminContext> ctxClass = 
                                          appCtx.getDelegateClass(AdminContext.class);
      if ((ctxClass == null) || (ctxClass.equals(AdminContext.class))) {
        throw new Exception("The Application's Delegate Class for for Class[AppContext] "
                + "is not registered in the BwAppContext.");
      }
      result = SessionHelper.getManagedBean(ctxClass);
    } catch (Exception pExp) {
      logger.log(Level.SEVERE, "AdminContext.doLookup Error: \n{0}", pExp.getMessage());
      throw new NullPointerException("AdminContext.doLookup Error: \n"
              + pExp.getMessage());
    }
    return result;
  }

  /**
   * Get the Admin Principal of the logged-in user. If (!doLoggon) or if (!isLoggedIn), 
   * it will return null.
   * @return the current logged in user's BwPrincipal or null.
   */
  @SuppressWarnings("unchecked")
  public static <TUser extends IUserInfo, TPrincipal extends BwPrincipal<TUser>> 
        TPrincipal getLoggedinPrincipal() {
    TPrincipal result = null;
    try {
      AdminContext<TUser, TPrincipal> context = AdminContext.doLookup();
      if ((context != null) && (context.isLoggedIn())) {
        result = context.getLoggedInUser();
      }
    } catch (Exception e) {
    }
    return result;
  }

  /**
   * Get the Admin Principal of the logged-in user and return it's userId. Otherwise,
   * return null.
   * @return String
   */
  public static String getLoggedinUserId() {
    String result = null;
    try {
      AdminContext adminCtx = AdminContext.doLookup();
      if ((adminCtx != null) && (adminCtx.isLoggedIn())) {
        BwPrincipal pUser = adminCtx.getLoggedInUser();
        result = (pUser != null) ? pUser.getUserId() : null;
      }
    } catch (Exception e) {
    }
    return result;
  }
  

  /**
   * Validate the pUser is the Application's Administrator
   * @param user BwPrincipal
   * @return true if pUser represents the Application Administrator
   */
  public static boolean isAdministrator(BwPrincipal user) {
    boolean result = false;
    try {
      if (user != null) {
        BwPrincipal adminUser = AdminContext.getAdminPrincipal();
        if (adminUser != null) {
          result = user.isUser(adminUser.getUserId());
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "AdminContext.isAdministrator Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
 
  /**
   * Get the Administration Account's BwPrincipal. If not already initiated, and
   * AdminContext.doLogon, call the AdminContext.accessValidator.getUserByName with
   * sUserName=AdminContext.adminUsername to retrieve the Administration Account's
   * BwPrincipal. Assign it to the private static AdminContext.mpAdminPrincipal
   * property.
   * <p>All exceptions are logged.</p>
   * @return the BwPrincipal or null if not doLogon or accessing the Administration's
   * BwPrincipal failed.
   */
  @SuppressWarnings("unchecked")
  public static <TUser extends IUserInfo, TPrincipal extends BwPrincipal<TUser>> 
        TPrincipal getAdminPrincipal() {
    try {
      if (AdminContext.adminPrincipal == null) {
        AdminContext adminCtx = AdminContext.doLookup();
        if (adminCtx == null) {
          throw new Exception("The Session's AdminContext is no longer accessible");
        }
        
        if (adminCtx.doLogon()) {
          UserAccessValidator validator = adminCtx.getAccessValidator();
          if (validator == null) {
            throw new Exception("The AdminContext's User Access Validator is not "
                    + "accessible");
          }
          
          AdminContext.adminPrincipal 
                                = validator.getUserByName(AdminContext.adminUsername);
        }
      }   
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getAdminPrincipal Error:\n {1}",
              new Object[]{"AdminContext", pExp.getMessage()});
    }
    return (TPrincipal) AdminContext.adminPrincipal;
  }
  
  //<editor-fold defaultstate="collapsed" desc="Private SessionUser class">
  /**
   * A IUserInfo implementation to create a "fake" user with a user id as the SessionId
   */
  private static class SessionUser implements IUserInfo {
    
    /**
     * The current session's SI
     */
    private String sessionId;
    
    //<editor-fold defaultstate="collapsed" desc="protected Constructor">
    /**
     * Constructor
     */
    protected SessionUser() {
      this.sessionId = SessionHelper.getSessionId();
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="IUserInfo Overrides">
    @Override
    public String getName() {
      return this.sessionId;
    }
    
    @Override
    public String getUserId() {
      return this.sessionId;
    }
    
    @Override
    public String getFullName() {
      return  "Session[" + this.sessionId + "]";
    }
    
    @Override
    public String getEmail() {
      return null;
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  /**
   * Get a BwPrincipal that for the current {@linkplain SessionUser}. 
   * If (adminCtx.doLogon) and (adminCtx.isLoggedIn), return adminCtx.loggedInUser else
   * return the BwPrincipal for a default {@linkplain SessionUser}
   * @return the BwPrincipal
   * @throws Exception if initiating BwPrincipal failed.
   */
  public static BwPrincipal getSessionUser() {
    BwPrincipal result = null;
    try {
      AdminContext adminCtx = AdminContext.doLookup();
      if ((adminCtx.doLogon()) && (adminCtx.isLoggedIn())) {
        result = adminCtx.getLoggedInUser();
      } else {
        IUserInfo user = new SessionUser();
        result = new BwPrincipal(user);      
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(exp.getMessage());
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the reference to the MyAppContext
   */
  private BwAppContext appContext;
  /**
   * Placeholder for the LoggedIn Principal's User Access Validator
   */
  private UserAccessValidator<TUser, TPrincipal> accessValidator;
  /**
   * Placeholder for the Logged-in user BwPrincipal information.
   */
  private TPrincipal logonPrincipal;
  /**
   * The Counter that controls the AdminAccess's Internal Elevated Access State
   */
  private Integer elevatedAccessCounter;
  /**
   * Placeholder for the Flag controlling whether the application supports log-on or
   * not - see {@linkplain #doLogon()} for more details)
   */
  private Boolean doUserLogon;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Event Senders">
  /**
   * EventHandler for sending a User LoggedOn event.
   */
  public final EventHandler UserLoggedOn;
  /**
   * Method called to fie the User LoggedOn event.
   */
  protected void fireUserLoggedOn() {
    this.UserLoggedOn.fireEvent(this, new EventArgs());
  }
  
  /**
   * EventHandler for sending a User LoggedOff event.
   */
  public final EventHandler UserLoggedOff;
  /**
   * Method called to fie the User LoggedOff event.
   */
  protected void fireUserLoggedOff() {
    this.UserLoggedOff.fireEvent(this, new EventArgs());
  }
  
  /**
   * EventHandler for sending a Access Validation event.
   */
  public final AccessValidationEventHandler AccessValidation;
  /**
   * Method called to fie the Access Validation event.
   */
  protected void fireAccessValidation(Object sender, AccessValidationEventArgs args) {
    this.AccessValidation.fireEvent(sender, args);
  }
  //</editor-fold> 

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public constructor
   */
  public AdminContext() {
    this.UserLoggedOn = new EventHandler();
    this.UserLoggedOff = new EventHandler(); 
    this.AccessValidation = new AccessValidationEventHandler();
    AdminContext.adminPrincipal = null;
    this.logonPrincipal = null;
    this.appContext = null;
    this.accessValidator = null;
    this.elevatedAccessCounter = 0;
    AdminContext.adminPrincipal = null;
    this.logonPrincipal = null;
    this.doUserLogon = null;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call super method before releasing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.UserLoggedOff.clear();
    this.UserLoggedOn.clear();
    this.AccessValidation.clear();
    if (this.accessValidator != null) {
      this.accessValidator.UserLoggedOn.remove(this);
      this.accessValidator.UserLoggedOff.remove(this);
      this.accessValidator = null;
    }
    this.elevatedAccessCounter = 0;
    AdminContext.adminPrincipal = null;
    this.logonPrincipal = null;
    this.doUserLogon = null;
    this.appContext = null;
  }
  
  /**
   * <p>Call to reset the doUserLogon status. If {@linkplain #isLoggedIn() 
   * this.isLoggedIn} call the {@linkplain UserAccessValidator#reset() 
   * this.accessValidator.reset()} to reset the current logged in user without 
   * resetting the session. The reset all cached settings.</p>
   * <p>Once all is reset it calls {@linkplain #doLogon() this.doLogon) to reinitiate 
   * the accessValidator, and if true, restore the prior loggedon user and the 
   * elevatedAccessCounter setting.</p>
   */
  public final void resetDoLogon() {
    TPrincipal user = this.getLoggedInUser();
    if ((this.isLoggedIn()) && (this.accessValidator != null)) {
      this.accessValidator.reset();
    }
    Integer accessCnt = this.elevatedAccessCounter;
    this.elevatedAccessCounter = 0;
    AdminContext.adminPrincipal = null;
    this.logonPrincipal = null;
    this.doUserLogon = null;
    try{
      if (this.doLogon()) {
        if ((user != null) && (this.accessValidator != null)) {
          this.accessValidator.logon(user);
        }
        this.elevatedAccessCounter = accessCnt;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetDoLogon Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Access to BwAppContext">
  /**
   * Initiate the Reference to the MyAppContext or return the prior initiated
   * reference.
   * @return BwAppContext
   */
  public final BwAppContext getAppContext() {
    if (this.appContext == null) {
      try {
        this.appContext = BwAppContext.doLookup();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getAppContext Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
    return this.appContext;
  }
  
  /**
   * get the Application Name assigned to the myAppContext
   * @return the assigned name or null if unassigned.
   */
  public final String getAppName() {
    return BwAppContext.AppName();
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Account Maintenance Methods">
  /**
   * Called by the FacePage to check if the Reset Password feature if supported by the 
   * application. It calls the {@linkplain UserAccessValidator#canResetPassword()}
   * method and return the result - if {@linkplain #doLogon() this.doLogon} = true
   * @return true if the application support the feature.
   */
  public final boolean canResetPassword() {
    boolean result = false;
    try{
      UserAccessValidator validator = null;
      if ((this.doLogon()) && ((validator = this.getAccessValidator()) != null)) {
        result = validator.canResetPassword();
      }
    } catch (Exception exp) {
      result = false;
      logger.log(Level.WARNING, "{0}.canResetPassword Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * <p>Called by the FacePage to start the user's "Reset Password" request.
   * If {@linkplain #doLogon() this.doLogon} = false or {@linkplain
   * #getAccessValidator() this.accessValidator} is not accessible, it throws an
   * Exception. Else, it calls {@linkplain UserAccessValidator#resetPassword()} to
   * process the request and return the url to the next page to display.</p>
   * <p>All Exceptions are trapped and displayed as a "Reset Password Request Error"
   * in a AlertView page.</p>
   * @return the url to the next page.
   */
  public final String resetPassword() {
    String result = null;
    String pageTitle = "Reset Password Request Error";
    String msgCaption = "Reset Password Error Message";
    String alertMsg = null;
    try {
      UserAccessValidator validator = null;
      if ((!this.doLogon()) || ((validator = this.getAccessValidator()) == null)) {
        throw new Exception("The <b>" + this.getAppName() + "</b> web "
                + "application is not setup to maintain user accounts. You have "
                + "reached this request in error. Please report this to the System "
                + "Administrator.");
      }
      result = validator.resetPassword();
    } catch (Exception exp) {
      alertMsg = "<p><i>Reset Password Request</i> failed because:</p><p><i>"
              + exp.getMessage() + "</i></p>";
      throw new IllegalArgumentException(alertMsg);
    }
    return result;
  }
  
  /**
   * Called to retrieve the NavigationTarget for the application's Login Page.
   * Typically called from {@linkplain PageView#getNavTrgUrl(
   * bubblewrap.navigation.core.NavigationTarget, java.lang.Boolean) 
   * PageView.getNavTrgUrl} to redirect the user to the LoginPage if not yet logged in.
   * @return this.accessValidator.loginPageNavTrg.
   * @throws Exception if the validator is not accessible or does not support the 
   * Login Page NavigationTarget
   */
  public String getLogonUrl() {
    String result = null;
    try {
      if (!this.doLogon()) {
        throw new Exception("The <b>" + this.getAppName() + "</b> web "
                + "application's does not support User Accounts and user logons.");
      }
      UserAccessValidator validator = null;
      if ((!this.doLogon()) || ((validator = this.getAccessValidator()) == null)) {
        throw new Exception("The <b>" + this.getAppName() + "</b> web "
                + "application is not setup to maintain user accounts. You have "
                + "reached this request in error. Please report this to the System "
                + "Administrator.");
      }
      
      result = DataEntry.cleanString(validator.getLogonUrl());
      if (result == null) {
        throw new Exception("The <b>" + this.getAppName() + "</b> web "
                + "application's User Account Administation Module does not have "
                + "an assigned Logon Page URL.  Please report this to "
                + "the System Administrator.");
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getLogonUrl Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp.getMessage());
    }
    return result;
  } 
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public/Protected logon Methods">
  /**
   * Return whether this application supports User Log-on and user access validation.
   * If this parameter is not yet initiated, it resolve its state as follows:<ul>
   *  <li>Is access the BwAppContext and is the Singleton is not accessible it sets
   *    doLogon = false and log and error message</li>
   *  <li>It retrieve AppParameter[{@linkplain AdminContext#DoLogonKey}] and sets 
   *    doLogon = false if undefined or false.</li>
   *  <li>If retrieves from BwAppContext the assigned Class Delegate from 
   *    Class[UserAccessValidator] and if assigned it initiate an instance of the
   *    class and assign itself as a EventLister to the UserAccessValidator.</li>
   *  <li>It set doLogon=true is the UserAccessValidator has been successfully 
   *    initiated.</li>
   * </ul>
   * <p>All Errors are trapped and logged and doLogon=false is any errors occur.</p>
   * @return true if the application supports log-on.
   */
  public boolean doLogon() {
    if (this.doUserLogon == null) {
      if (this.accessValidator != null) {
        this.accessValidator.UserLoggedOn.remove(this);
        this.accessValidator.UserLoggedOff.remove(this);
        this.accessValidator = null;
      }
      try {
        this.beginRunWithElevatedAccess();
        BwAppContext appCtx = this.getAppContext();
        if (appCtx == null) {
          throw new Exception("Unable to access the BwAppContext.");
        }
        
        this.doUserLogon = appCtx.getBoolParameter(AdminParameters.DoLogonKey);
        this.doUserLogon = (this.doUserLogon == null)? false: this.doUserLogon;
        if (this.doUserLogon) {
          Class<? extends UserAccessValidator> delegateClass 
                                  = appCtx.getDelegateClass(UserAccessValidator.class);
          if (delegateClass == null) {
            throw new Exception("The BwAppContext's UserAccessValidator delegate "
                    + "class is not assigned.");
          }

          /* 
           * If the UserAccessValidator delegate is assigend, initiate the validator
           */
          try {
            this.accessValidator = delegateClass.newInstance();
            if (this.accessValidator == null) {
              throw new Exception("Initiating a new " 
                      + delegateClass.getClass().getSimpleName()
                      + " instance failed.");
            }
            
            /* 
             * Assign the BwAdminContext as an eventListener of the UserAccessValidator
             */
            this.accessValidator.UserLoggedOn.add(
                                              new ValidatorUserLoggedOnDelegate(this));
            this.accessValidator.UserLoggedOff.add(
                                              new ValidatorUserLoggedOffDelegate(this));
          } catch (Exception pExp) {
            throw new Exception(this.getClass().getSimpleName()
                    + ".initAccessValidator Error:\n " + pExp.getMessage());
          }
          
          /*
           * Check if the Admin UserAccount if activated.
           */
          TPrincipal adminUser = 
                        this.accessValidator.getUserByName(AdminContext.adminUsername);
          if (adminUser == null) {
            throw new Exception("The Admin User Account is not properly activated.");
          }
          AdminContext.adminPrincipal = adminUser;
        }
      } catch (Exception pExp) {
        this.doUserLogon = false;
        this.accessValidator = null;
        logger.log(Level.WARNING, "{0}.doLogon Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      } finally {
        this.endRunWithElevatedAccess();
      }
    }
    return this.doUserLogon;
  }

  /**
   * Check if the the current logged-in user is the administrator
   * @return boolean
   */
  public boolean isAdminLogon() {
    boolean bResult = false;
    if (this.logonPrincipal != null) {
      bResult = AdminContext.isAdministrator(this.logonPrincipal);
    }
    return bResult;
  }

  /**
   * Check if the Application's Logged-In State (i.e., the session has a
   * logged in user).
   * @return boolean
   */
  public boolean isLoggedIn() {
    return (this.logonPrincipal != null);
  }
  
//  /**
//   * Check the Application's Logged-In State (i.e., the session has a logged in user) and
//   * if not and {@linkplain #doLogon() this.doLogon}, set the <tt>mv.viewName</tt> =
//   * {@linkplain #logonUrl}.
//   * @param mv the ModelAndView to update
//   * @return return true if logged-in.
//   */
//  public boolean isLoggedIn(ModelAndView mv) {
//    boolean result = (this.isLoggedIn());
//    if ((!result) && (this.doLogon())) {
//      mv.setViewName(this.getLogonUrl());
//    }
//    return result;
//  }

  /**
   * Public method to determine if the Session has a logged in user and whether that
   * user is pUser. Call the equals method to compare.
   * @param user the UserInfo to evaluate
   * @return boolean
   */
  public boolean isLoggedIn(TUser user) {
    return ((this.logonPrincipal != null) && (user != null)
            && (this.logonPrincipal.isUser(user)));
  }

  /**
   * Public method to determine if the Session has a logged in user and whether that
   * user's UserId is sUserId. Call the equals method to compare.
   * @param userId String
   * @return boolean
   */
  public boolean isLoggedIn(String userId) {
    return ((this.logonPrincipal != null) && (userId != null)
            && (this.logonPrincipal.isUser(userId)));
  }

  /**
   * Get a reference to the Logged-in User's Principal.
   * @return the logged-in BwPrincipal or null if not logged-in
   */
  public TPrincipal getLoggedInUser() {
    return this.logonPrincipal;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Manage RunWidthElevatedAccess State">
  /**
   * Increment the ElevatedAccessCounter with every call to beginRunWithElevatedAccess
   * Every beginRunWithElevatedAccess must be followed by endRunWithElevatedAccess,
   * preferably within try-finally context.
   */
  public void beginRunWithElevatedAccess() {
    this.elevatedAccessCounter =  ((this.elevatedAccessCounter == null) 
            || (this.elevatedAccessCounter < 0)) ? 0
            : this.elevatedAccessCounter + 1;
  }

  /**
   * Decrement the ElivatedAccessCounter as long as it is > 0.
   */
  public void endRunWithElevatedAccess() {
    if (this.elevatedAccessCounter == null) {
      this.elevatedAccessCounter = 0;
    } else if (this.elevatedAccessCounter > 0) {
      this.elevatedAccessCounter--;
    }
  }

  /**
   * Check if the Session is running with Elevated Access Privileges - having access to
   * all accounts.
   * @return true if the ElivatedAccessCounter > 0..
   */
  public boolean doRunWithElevatedAccess() {
    return ((this.elevatedAccessCounter != null) 
                              && (this.elevatedAccessCounter > 0));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="User Access Validation">
  /**
   * Called to initiate the AdminContext UserAccessValidator. If not yet initiated,
   * it will initiate a new instance of the validator if (this.doLogon).  The validator
   * class must be assigned as a delegate class to the BwAppContext. An exception is 
   * throw if the delegate class is unassigned or the initiation of the new instance 
   * of the class fails.
   * @return the reference to the UserAccessValidator or null if (!this.doLogon)
   * @throws Exception if the initiating the UserAccessValidator failed.
   */
  public UserAccessValidator<TUser,TPrincipal> getAccessValidator() {
    return (this.doLogon())? this.accessValidator: null;
  }
  
  /**
   * Check if access is allowed regardless of the target.
   * @return true if (this.doRunWithElevatedAccess) or (!this.doLogon) or
   * (isLoggedIn and isAdminLogon)
   */
  private boolean doAllowAccess() {
    return ((this.doRunWithElevatedAccess()) || (!this.doLogon()) 
            || ((this.isLoggedIn()) && (this.isAdminLogon())));
  }

  /**
   * This method is called by WebPageViews or the {@linkplain 
   * #hasAccess(java.lang.String) hasAccess} to validate the user's access to the 
   * specified NavigationTarget's Task-Action. Access is granted if: <ul>
   *   <li>The nNavTrg's NavigationInfo.doValidateAccess=false</li>
   *   <li>Otherwise, a args=AccessValidationEventArgs is initiated using the 
   *     NavigationTarget's Action and AppTask and {@linkplain #validateAccess(
   *     java.lang.Object, bubblewrap.core.events.AccessValidationEventArgs) 
   *     validateAccess} is called. If the call returns not args.hasAccess. 
   *     The reason for the access denial will be logged and the access will be denied. 
   *     Otherwise, access is granted if args.hasAccess.</li>
   * </ul>    
   * <p><b>Note:</b> This call return true if {@linkplain #doAllowAccess()
   * this.doAllowAccess}=true without further access validation.</p>
   * @param navTrg the NavigationTarget for which access must be validated.
   * @return true is access is granted
   */
  public boolean hasNavigationAccess(Object sender, NavigationTarget navTrg) {
    boolean result = (navTrg != null);
    if ((result) && (!this.doAllowAccess())) {
//      NavigationInfo info = null;
//      try {
//        info = NavigationTarget.getInfo(navTrg);
//      } catch(Exception pNoErr) {
//        info = null;
//      }
//      
//      if ((info != null) && (info.doValidateAccess())) {
        AccessValidationEventArgs args 
                = new AccessValidationEventArgs(navTrg.getAppTask(), navTrg.getAction(),
                      null, null);
        try {
          this.validateAccess(sender, args);
          if (args.hasAccess()) {
            String reason = args.getDenyReason();
            reason = (reason != null)? reason: "Unknown reason.";
            throw new Exception("Access to NavTrg[" + navTrg.toString() 
                    + "] was denied because:\n" + reason);
          }

          result = args.hasAccess();
        } catch (Exception pExp) {
          logger.log(Level.WARNING, "{0}.hasNavigationTargetAccess Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
        }
      }
    //}
    return result;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="IAccessValidator Implementation">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Called the Validate the Access for the Action, Application Task, 
   * and accessCode specified in the event arguments. 
   * The Validation is handled as follows:</p>
   * <ul>
   *  <li><b>If ({@linkplain #doAllowAccess() this.doAllowAccess}):</b> - access is 
   *    granted to all request</li>
   *  <li><b>Else If (!{@linkplain #isLoggedIn() this.isLoggedIn}):</b> - access is 
   *    denied to all request</li>
   *  <li><b>Else:</b> - it calls the {@linkplain UserAccessValidator#validateAccess(
   *    java.lang.Object, bubblewrap.core.events.AccessValidationEventArgs) 
   *    UserAccessValidator.validateAccess(sender,args)} method to handles the 
   *    validation.  If args return unhandled, it fires the {@linkplain 
   *    #AccessValidation AccessValidation} event to allow any listeners to handle
   *    the request.</li>
   * </ul>
   * <p>Exception messages are logged and access is denied with error message as the
   * reason for the denial. It is up to the called how these error messages
   * will be handled.</p>
   */
  @Override
  public void validateAccess(Object sender, AccessValidationEventArgs args) {
    try {
      if (this.doAllowAccess()) {
        args.grantAccess();
//      } else if ((!this.isLoggedIn())){
//        throw new Exception("User Access is denied because the user is not logged in.");
      } else {
        UserAccessValidator validator = this.getAccessValidator();
        if (validator != null) {
          validator.validateAccess(sender, args);
        }
        
        if (!args.isHandled()) {
          this.fireAccessValidation(sender, args);
        }
      }
    } catch (Exception exp) {
      args.denyAccess(exp.getMessage());
//      logger.log(Level.INFO, "{0}.validateAccess Error:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="EventDelegates">  
  /**
   * A LoggedOnEventDelegate for handling the event when a new user logged in.
   * It set the AdminContext's logonPrincipal and fire the UserLoggedOn event.
   */
  private class ValidatorUserLoggedOnDelegate extends LoggedOnEventDelegate {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public ValidatorUserLoggedOnDelegate(AdminContext listener) {
      super(listener);
    }
    //</editor-fold>
    
    @Override
    public void onEvent(Object sender, LoggedOnEventArgs args) {
      AdminContext listener = null;
      UserAccessValidator<TUser, TPrincipal> validator = null;
      if ((sender != null) && ((listener = (AdminContext) this.getListener()) != null) 
              && ((validator = listener.getAccessValidator()) !=  null) &&
                 (sender == validator)) {
        TPrincipal user = validator.getUserByName(args.userName);
        if (user == null) {
          throw new IllegalArgumentException("The User's BwPrincipal cannot be "
                  + "unassigned");
        }      
        listener.logonPrincipal = user;
        listener.fireUserLoggedOn();
      }
    }
  }
  
  /**
   * A EventDelegate for handling the event when a suer log off. It resets the 
   * AdminContext's logonPrincipal and fire the UserLoggedOff event.
   */
  private class ValidatorUserLoggedOffDelegate extends EventDelegate {

    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public ValidatorUserLoggedOffDelegate(AdminContext listener) {
      super(listener);
    }
    //</editor-fold>
    
    @Override
    public void onEvent(Object sender, EventArgs args) {
      AdminContext listener = null;
      UserAccessValidator validator = null;
      if ((sender != null) && ((listener = (AdminContext) this.getListener()) != null) 
              && ((validator = listener.getAccessValidator()) !=  null) &&
                 (sender == validator)) {
        listener.logonPrincipal = null;
        listener.fireUserLoggedOff();
      }
    }
  }
  //</editor-fold>
}

//<editor-fold defaultstate="collapsed" desc="CONVERT: Move to AdminAccessValidator">
// CONVERT: Move to AdminAccessValidator
//  /**
//   * Initiate the TaskAccessTable if not currently initiated.
//   */
//  private void initTaskAccessTable() {
//    if (this.mpTaskAccess != null) {
//      return;
//    }
//
//    try {
//      this.beginRunWithElevatedAccess();
//      this.mpTaskAccess = new TaskAccessTable();
//    } finally {
//      this.endRunWithElevatedAccess();
//    }
//  }
//
//  /**
//   * Called by logon to Initiate the Task-Action Access for the newly logged-in user
//   * based on the user's UserGroup (and it UserGroup-Role and Application Role) and it
//   * UserRole (and its UserRole) Assignments. For each Application role it calls the
//   * mergeRoleTasks method passing the SecurityLevel of either the UserGroup or the
//   * UserRole to validate the user's access to the associated RoleTasks.
//   * @param pUserRec UserInfoAjax
//   * @throws Exception
//   */
//  private void initTaskAccess(UserInfoAjax pUserRec) throws Exception {
//    try {
//      if (this.mpTaskAccess == null) {
//        this.initTaskAccessTable();
//      }
//
//      if (this.mpTaskAccess == null) {
//        throw new Exception("The Task Access Table failed to initialize.");
//      }
//
//      UserGroupAjax pGroup = (pUserRec == null) ? null : pUserRec.getUsergroup();
//      if (pGroup != null) {
//        int iSecurityLevel = pGroup.getSecuritylevel();
//        List<UserGroupRoleAjax> pGrpRoleList = pGroup.getUserGroupRoleCollection();
//        if ((pGrpRoleList != null) && (!pGrpRoleList.isEmpty())) {
//          for (UserGroupRoleAjax pGrpRole : pGrpRoleList) {
//            AppRoleAjax pRole = (pGrpRole == null) ? null : pGrpRole.getAppRole();
//            this.mergeRoleTasks(pRole, iSecurityLevel);
//          }
//        }
//      }
//
//      List<UserRoleAjax> pUserRoleList = pUserRec.getUserRoleCollection();
//      if ((pUserRoleList != null) && (!pUserRoleList.isEmpty())) {
//        for (UserRoleAjax pUserRole : pUserRoleList) {
//          AppRoleAjax pRole = (pUserRole == null) ? null : pUserRole.getAppRole();
//          int iSecurityLevel = pUserRole.getSecurityLevel();
//          this.mergeRoleTasks(pRole, iSecurityLevel);
//        }
//      }
//    } catch (Exception pExp) {
//      throw new Exception(this.getClass().getSimpleName()
//              + ".initTaskAccess Error:\n " + pExp.getMessage());
//    }
//  }
//
//  /**
//   * Get each Application Role-Task assignment for pRole and it its SecurityLevel
//   * requirements equal to or less than iSecurityLevel (the user or usergroups security)
//   * the call mpTaskAccess.mergeRoleTaskAccess(pRoleTask) to set the Task-Action's
//   * hasAccess = true.
//   * @param pRole AppRoleAjax
//   * @param iSecurityLevel int
//   */
//  private void mergeRoleTasks(AppRoleAjax pRole, int iSecurityLevel) throws Exception {
//    try {
//      List<AppRoleTaskAjax> pRoleTaskList =
//              (pRole == null) ? null : pRole.getAppRoleTaskCollection();
//      if ((pRoleTaskList != null) && (!pRoleTaskList.isEmpty())) {
//        for (AppRoleTaskAjax pRoleTask : pRoleTaskList) {
//          if (iSecurityLevel >= pRoleTask.getSecuritylevel()) {
//            this.mpTaskAccess.mergeRoleTaskAccess(pRoleTask);
//          }
//        }
//      }
//    } catch (Exception pExp) {
//      throw new Exception(this.getClass().getSimpleName()
//              + ".mergeRoleTasks Error:\n " + pExp.getMessage());
//    }
//  }
//</editor-fold>
