package bubblewrap.admin.events;

import bubblewrap.core.enums.IntFlag;
import bubblewrap.core.events.RequestArgs;
import bubblewrap.io.DataEntry;
import bubblewrap.navigation.enums.AppActions;
import bubblewrap.navigation.enums.AppTasks;
import java.math.BigInteger;

/**
 * <p>The RequestArgs is used be a caller to validate the user access based on an
 * Action, an AppTask, and an optional SecurityLevel. The requestHandler must either
 * {@linkplain #grantAccess() grantAccess} or {@linkplain #denyAccess(java.lang.String)
 * denyAccess}. In the latter case a reason for the denial can be assigned.</p>
 * 
 * <p>On completion of the access validation, the caller can check if the request was
 * handled, and decide how to handle an unhandled request. If unhandled this.result will
 * always return 'true'. If handled, the caller can check the hasError and if 'true',
 * the assigned error message(s) can be retrieved. Otherwise, it can check this.result
 * to check whether the access has been granted or denied.</p>
 * <p><b>Note:</b> if the caller call the allowMultipleErrors to set the flag that
 * allow multiple handler to assign error messages (reasons for denial).</p>
 * @author kprins
 */
public class ValidateAccessRequestArgs extends RequestArgs<Boolean> {

  // <editor-fold defaultstate="collapsed" desc="Private Methods">
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
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public constructor with ActionEnums[eAction] and Application Task -
   * both must be defined and valid
   * @param appTask the Application Task (Type {@linkplain AppTasks}) to evaluate
   * @param action the Action (Type {@linkplain AppActions}) to evaluate
   */
  public ValidateAccessRequestArgs(AppTasks appTask, AppActions action) {
    this(appTask, action, null);
  }
  
  /**
   * Public constructor with ActionEnums[eAction] and Application Task -
   * both must be defined and valid
   * @param appTask the Application Task (Type {@linkplain AppTasks}) to evaluate
   * @param action the Action (Type {@linkplain AppActions}) to evaluate
   * @param requiredAccess the required AccessCode to Evaluate (Could be null)
   */
  public ValidateAccessRequestArgs(AppTasks appTask, AppActions action, 
          IntFlag requiredAccess) {
    super();
    if (appTask == null) {
      throw new NullPointerException("The Application Task cannot be unassigned.");
    }
    if (action == null) {
      throw new NullPointerException("The Application Action cannot be unassigned.");
    }    
    this.appAction = action;
    this.appTask = appTask;
    this.accessCode = requiredAccess;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="RequestArgs Override">
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Return true (allow access) if not handled and the caller does not
   * ignored this.isHandled state.</p>
   */
  @Override
  protected Boolean getUnhandledResult() {
    return true;
  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Return false if handled and an error was assigned during the 
   * validation or a denial reason was assigned.</p>
   */
  @Override
  protected Boolean getOnErrorResult() {
    return false;
  }

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Publicly expose the method to allow callers to set the flag.</p>
   */
  @Override
  public void allowMultipleErrors() {
    super.allowMultipleErrors();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
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
   * Check if the access is allowed.
   * @return this.getResult() (default = true).
   */
  public boolean hasAccess() {
    return this.getResult();
  }
  
  /**
   * Called to grantAccess to the request. If this request has been previously handled
   * and access was denied, this call will be ignored.
   */
  public void grantAccess() {
    if (!this.isHandled()) {
      this.setResult(true);
    }
  }
  
  /**
   * Call to deny access to the requested Action-Task-SecurityLevel request. If sReason
   * is unassigned, this.result is set to false. If access has been granted by a prior 
   * handler, the request will be reset and the access will be denied. If a prior 
   * handler denied the access and assigned a error message, th current reason for
   * denial will be appended if the request is a Multi
   * @param sReason the reason for the denial (can be null).
   */
  public void denyAccess(String sReason) {
    sReason = DataEntry.cleanString(sReason);
    if ((this.isHandled()) && (this.getResult() == true)) {
      this.resetArgs();
    }
    
    if (sReason == null) {
      this.setResult(false);
    } else {
      this.setErrorMsg(sReason);
    }
  }      
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: Return a the action, task, and secirtyLevel</p>
   */
  @Override
  public String toString() {
    return "AccessValidationRequest[appTask=" + this.appTask.toString()
            + "; action=" + this.appAction.toString()
            + ((this.accessCode == null) ? ""
            : ("; secLevel=" + this.accessCode))
            + "]";
  }
  // </editor-fold>
}
