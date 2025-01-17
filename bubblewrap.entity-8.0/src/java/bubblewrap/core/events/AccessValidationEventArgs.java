package bubblewrap.core.events;

import bubblewrap.core.enums.IntFlag;
import bubblewrap.io.DataEntry;
import bubblewrap.navigation.enums.AppActions;
import bubblewrap.navigation.enums.AppTasks;

/**
 * <p>A EventArgs used in validating a user's access to a specific task and action with
 * the optional use of validating access to a specific field and/or for a specified
 * accessCode.</p>
 * <p>It is used by the {@linkplain AccessValidationEventHandler} </p>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class AccessValidationEventArgs extends EventArgs {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The name of the field that will be changed
   */
  private String fieldName;
  /**
   * The Application Task to Validate
   */
  private AppTasks appTask;
  /**
   * The Action (ActionEnums) to validate
   */
  private AppActions appAction;
  /**
   * The Required AccessCode to validate (Default=Null|ZERO|No Access restrictions)
   */
  private IntFlag accessCode;
  /**
   * Placeholder for the cancellation message
   */
  private String denyReason;
  /**
   * Placeholder for the hasAccess Flag
   */
  private Boolean hasAccess;
  //</editor-fold>
   
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public AccessValidationEventArgs(AppTasks appTask, AppActions appAction, 
            IntFlag accessCode, String fieldName) {
    super();    
    this.appTask = appTask;
    this.appAction = appAction;
    this.fieldName = DataEntry.cleanString(fieldName);
    this.accessCode = accessCode;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get the name of the field that changed (optional)
   * @return the assigned value
   */
  public String getFieldName() {
    return this.fieldName;
  }
  
  /**
   * Check if the otherName matches this EventInfo's assigned FieldName
   * @param otherName the name to check against this.fieldName
   * @return true if it is a match.
   */
  public boolean isField(String otherName) {
    return ((this.fieldName != null) &&
            (DataEntry.isEq(this.fieldName, otherName, true)));
  }
  
  /**
   * Get the Task Action to Validate
   * @return the assigned {@linkplain AppActions} value
   */
  public AppActions getAction() {
    return this.appAction;
  }

  /**
   * Get the Application Task to Validate
   * @return the assigned {@linkplain AppTasks}
   */
  public AppTasks getAppTask() {
    return this.appTask;
  }
  
  /**
   * Check whether the request include a SecurityLevel Validation
   * @return true if the SecurityLevel is assigned.
   */
  public boolean hasAccessCode() {
    return (this.accessCode != null);
  }
  
  /**
   * Get the SecurityLevel to Validate
   * @return the assigned SecurityLevel (can be null)
   */
  public IntFlag getAccessCode() {
    return this.accessCode;
  }
  
  /**
   * Set the Required AccessCode to be used in the validation - ignored if already set.
   * @param requiredAccess the required AccessCode.
   */
  public void setAccessCode(IntFlag requiredAccess) {
    if ((this.accessCode == null) && (requiredAccess != null)) {
      this.accessCode = requiredAccess;
    }
  }
  
  /**
   * Check if the user has access. Default = true
   * @return true if access was granted of if not handled.
   */
  public boolean hasAccess() {
    return ((this.hasAccess == null) || (this.hasAccess));
  }
  
  /**
   * Get the reason for an access denial
   * @return teh assigned message if access was denied (can be null).
   */
  public String getDenyReason() {
    return this.denyReason;
  }
  
  /**
   * Called to grant Access to the requested Action-Task-SecurityLevel request. 
   * <p><b>Note:</b> By default the user has access, but it can be denied by calling
   * the {@linkplain #denyAccess(java.lang.String) denyAccess} method. The grantAccess
   * should only be called to force the user's access (e.g., when the user has 
   * temporary elevated access privileges).  It overrides any prior denials and will
   * not allow subsequent denials.</p>
   */
  public void grantAccess() {
    if (!this.isHandled()) {
      this.isHandled();
      this.hasAccess = true;
      this.denyReason = null;
    }
  }
  
  /**
   * Call to deny access to the requested Action-Task-SecurityLevel request. 
   * <p><b>Note:</b> By default the user has access, but it can be denied by calling
   * the denyAccess method. However, if the {@linkplain #grantAccess() grantAccess} 
   * method is called after access was denied, the denial will be overridden. If access
   * was granted or the access was already denied, the call to denyAccess will be 
   * ignored.</p>
   * @param sReason the reason for the denial (can be null).
   */
  public void denyAccess(String sReason) {
    if (this.isHandled()) {
      return;
    }
    this.hasAccess = false;
    this.denyReason = DataEntry.cleanString(sReason);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "AppTask={this.appTask}; AppAction={this.AppAction)(;
   * AccessCdoe={this.accessCode})(; Field={this.fieldName})"</p>
   */
  @Override
  public String toString() {
    String result = "AppTask=" + ((this.appTask==null)? "null": this.appTask.name());
    result += "; AppAction=" + ((this.appAction==null)? "null": this.appAction.name());
    if (this.accessCode != null) {
      result += "; AccessCode=" + this.accessCode.toString();
    }
    if (this.fieldName != null) {
      result += "; Field=" + this.fieldName;
    }
    return result;
  }
  //</editor-fold>
}
