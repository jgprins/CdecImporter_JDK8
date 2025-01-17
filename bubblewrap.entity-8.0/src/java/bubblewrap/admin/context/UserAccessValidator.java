package bubblewrap.admin.context;

import bubblewrap.admin.events.LoggedOnEventArgs;
import bubblewrap.admin.events.LoggedOnEventHandler;
import bubblewrap.admin.interfaces.IUserInfo;
import bubblewrap.core.events.AccessValidationEventArgs;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.navigation.core.NavigationTarget;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

/**
 * <p>An abstract UserAccessValidator class used by the {@linkplain AdminContext} class
 * access the logged on user's UserInfo, to validate the logged on user's access, and to
 * listen to the UserAccessValidator;s LogOn and LogOff event.</p>
 * @author Koos Prins
 */
public abstract class UserAccessValidator<TUser extends IUserInfo, 
                         TPrincipal extends BwPrincipal<TUser>> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger
          = Logger.getLogger(UserAccessValidator.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder of the current logged on user's key Information
   */
  private IUserInfo loggedOnUser;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Event Senders">
  /**
   * EventHandler for sending a User LoggedOn event.
   */
  public final LoggedOnEventHandler UserLoggedOn;
  /**
   * Method called to fie the User LoggedOn event.
   * @param userName the username of the newly logged-on user.
   */
  protected void fireUserLoggedOn(String userName) {
    this.UserLoggedOn.fireEvent(this, new LoggedOnEventArgs(userName));
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
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected UserAccessValidator() {  
    this.loggedOnUser = null;
    this.UserLoggedOn = new LoggedOnEventHandler();
    this.UserLoggedOff = new EventHandler();
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.UserLoggedOn.clear();
    this.UserLoggedOff.clear();
    this.resetValidator();    
  }
  
  /**
   * Called to reset the Validator (i.e., unload all its user validation information. 
   * It calls {@linkplain #onResetValidator()} to handle this custom unloading process
   * and catches all errors. Finally if this.loggedOnUser is set, it calls {@linkplain 
   * #fireOnLogoff() fireOnLogoff} to notify listeners that the user is logging off
   * and then reset this.loggedOnUser 
   */
  private void resetValidator() {
    try{
      this.onResetValidator();
    } catch (Exception exp) {
    } finally {
      if (this.loggedOnUser != null) {
        this.fireUserLoggedOff();
        this.loggedOnUser = null;
      }
    }
  }  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Valdiation Methods">
  /**
   * Get the Validator's isLoggedOn state - the validator has user access validation 
   * data for the logged in user.
   * @return true if the Validator has a successfully logged on user.
   */
  public final boolean isLoggedOn() {
    return (this.loggedOnUser != null);
  }
  
  /**
   * Validate the User identified only by a userId is the validate logged-on user,
   * @param userId the user's identifier
   * @return true if this.isLoggedOn and the user's userId is a match.
   */
  public final boolean isLoggedOnUser(String userId) {
    return (this.loggedOnUser == null)? false: 
                    (DataEntry.isEq(this.loggedOnUser.getUserId(), userId, true));
  }
  
  /**
   * <p>Called by the Custom User Administration Module to log in a new user. This 
   * method handles the call as follows:</p><ul>
   *  <li>It validates that pUser is assigned and throw and exception if not.</li>
   *  <li>it calls {@linkplain  #resetValidator()} to dispose any prior loaded
   *    validation data.</li>
   *  <li>It calls {@linkplain #onLogon(IUserInfo) onLogon} to handle the custom
   *    loading of the access validation data.</li>
   *  <li>If successfully logged in, it calls {@linkplain #fireOnLogon() fireOnLogon}
   *    to notify listeners that a user was successfully logged in.</li>
   *  <li>It returns this.isLoggedOn on completion of the call.</li>
   * </ul>
   * <p>All exceptions are caught, resetValidator is called to unload any partially
   * loaded data and the exception is logged.</p>
   * <p><b>Note:</b> This method should only be called once the input userInfo has been
   * validated (i.e., the user record exist and the user access - using as password
   * has been validated).</p>
   * @param userInfo the information of the user that is logging in.
   */
  public final boolean logon(IUserInfo userInfo) {
    try {
      if (userInfo == null) {
        throw new Exception("The User's Logon Information is unassigned. ");
      }
      
      this.resetValidator();
      this.onLogon(userInfo);
      this.loggedOnUser = userInfo;
      this.fireUserLoggedOn(userInfo.getName());
    } catch (Exception pExp) {
      this.resetValidator();
      logger.log(Level.WARNING, "{0}.Logon Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.isLoggedOn();
  }
  
  /**
   * Called by the AdminContext reset the logging user without resetting then Session.
   * It calls {@linkplain #resetValidator()} to dispose any prior loaded validation 
   * data and to reset the isLoggedOn state. 
   * <p><b>Note:</b>It fires the {@linkplain #UserLoggedOff UserLoggedOff} event.</p>
   */
  public final void reset() {
    try{
      this.resetValidator();          
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.reset Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to log-off the current logged-in user. This process calls {@linkplain 
   * #resetValidator() this.resetValidator()} to reset the current user's access 
   * information. If the FacesContext is accessible, it invalidate (reset) the Session
   * and redirect the httpResponse to the HomePage. 
   * <p><b>Note:</b>It fires the {@linkplain #UserLoggedOff UserLoggedOff} event.</p>
   * @return the HomePage's URL
   */
  public final void logoff() {
    try {
      this.resetValidator(); 
      HttpSession session = SessionHelper.getHttpSession();
      if (session != null) {
        session.invalidate();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.loggoff Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
  
  /**
   * <p>Called by the {@linkplain AdminContext} to established whether the reset of
   * passwords is supported by this application. It calls {@linkplain
   * #onCanResetPassWord() this.onCanResetPassWord} and return the result.</p> 
   * <p>All Exceptions are trapped and logged and the result is set to false.</p>
   * @return true if the feature is supported. 
   */
  public boolean canResetPassword() {
    boolean result = false;
    try{
      result = this.onCanResetPassWord();
    } catch (Exception exp) {
      result = false;
      logger.log(Level.WARNING, "{0}.canResetPassword Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * <p>Called by the {@linkplain AdminContext} to process the user's request to reset the
   * password. It calls {@linkplain #canResetPassword() this.canResetPassword} to check
   * if this feature is supported and if throw and exception. Else, it will call
   * {@linkplain #onResetPassWord() this.onResetPassWord} to start the custom handling
   * of this request.</p>
   * <p>All Exceptions are trapped and and the error will be displayed in an AlertView 
   * page</p>
   * @return 
   */
  public String resetPassword() {
    String result = null;
    String pageTitle = "Reset Password Request Error";
    String msgCaption = "Reset Password Error Message";
    String alertMsg = null;
    try {
      if (!this.canResetPassword()) {
        AdminContext adminCtx = AdminContext.doLookup();
        throw new IllegalArgumentException("The reset of passwords is not supported by "
                + "the <b>"
                + adminCtx.getAppName() + "</b> web application. Please contact the"
                + "System Administrator if you feel you have received the message in "
                + "error.");
      }
      
      result = this.onResetPassWord();
    } catch (Exception exp) {
      alertMsg = "<p><i>Reset Password Request</i> failed because:</p><p><i>"
              + exp.getMessage() + "</i></p>";
    }
    return result;
  }
  
  //<editor-fold defaultstate="collapsed" desc="Protected Abstract Methods">
  /**
   * ABSTRACT: Called to validate the access for a specified Action ({@linkplain 
   * ActionEnums}), an Application Task, and an Optional access Code and Field Name. 
   * Implementors must handle the request by denying access is applicable. Use the
   * {@linkplain AccessValidationEventArgs#grantAccess()  grantAccess} option only if 
   * to force access.
   * @param args the request arguments
   * @param sender the request sender
   */
  public abstract void validateAccess(Object sender, AccessValidationEventArgs args);
  
  /**
   * ABSTRACT: Called to retrieve the information for a non-logged on user by Username
   * @param userName the user's unique username
   * @return the BwPrincipal or null if not found.
   */
  public abstract TPrincipal getUserByName(String userName);
  
  /**
   * ABSTRACT: Called to retrieve the information for a non-logged on user by userId
   * @param userId the user's unique userId
   * @return the BwPrincipal or null if not found.
   */
  public abstract TPrincipal getUserById(String userId);
  
  /**
   * <p>ABSTRACT: Called by {@linkplain #logon(bubblewrap.admin.interfaces.IUserInfo)
   * logon}, which is called by the Admin Module when a new user is logging on. It is
   * called before assigning this user as the new logged-on user and before firing the
   * {@linkplain #UserLoggedOn UserLoggedOn} Event.</p>
   * <p>The implementor should load the user access credentials. If an error occurred 
   * during this process, an Exception should be thrown to prevent the user from 
   * logging on. The isLoggedOn state will only change once this process was 
   * successfully completed.</p>
   * @param userInfo the information of the user that is logging in.
   */
  protected abstract void onLogon(IUserInfo userInfo) throws Exception;
  
  /**
   * ABSTRACT: Called by {@linkplain #resetValidator()} when a user logging off or when 
   * the Validator is been disposed to unload the user's access validation data. Any 
   * exceptions will be trapped and ignored.
   */
  protected abstract void onResetValidator() throws Exception;
  
  /**
   * ABSTRACT: Called by {@linkplain #resetPassword()} when a users request to reset 
   * their password because they forgot it. Implementors should return the url of the
   * next page (e.g., with instructions). If this option is not available, it should
   * throw an exception which will display a "Reset Password Request Error".
   * @return the url to the next page to display.
   */
  protected abstract boolean onCanResetPassWord() throws Exception;
  /**
   * ABSTRACT: Called by {@linkplain #resetPassword()} when a users request to reset 
   * their password because they forgot it. Implementors should return the url of the
   * next page (e.g., with instructions). If this option is not available, it should
   * throw an exception which will display a "Reset Password Request Error".
   * @return the url to the next page to display.
   */
  protected abstract String onResetPassWord() throws Exception;
  
  /**
   * ABSTRACT: Called by {@linkplain #getUserByName(java.lang.String) this.getUserByName}
   * or {@linkplain #getUserById(java.lang.String) this.getUserById} to initiate the
   * UserAccessValidator's custom Principal instance
   * @param user the user record (never null)
   * @return the TPrincipal instance
   * @throws Exception is an error occurred.
   */
  protected abstract TPrincipal onInitPrincipal(TUser user) throws Exception;

  /**
   * ABSTRACT: Called by {@linkplain AdminContext#getLoginPageNavTrg()} to retrieve the
   * {@linkplain NavigationTarget} for the User Account Login Page.
   * @return the login NavigationTarget
   * @throws Exception 
   */
  public abstract String getLogonUrl() throws Exception;
  //</editor-fold>

  
}
