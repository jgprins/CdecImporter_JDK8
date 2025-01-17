package bubblewrap.navigation.core;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.io.DataEntry;
import bubblewrap.io.params.ParameterMap;
import bubblewrap.navigation.enums.AppActions;
import bubblewrap.navigation.enums.AppTargets;
import bubblewrap.navigation.enums.AppTasks;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The NavigationTarget is used for multiple purposes, of which the more important 
 * are:</p><ol>
 * <li>Used as a HashMap key for registering the NavigationTarget's NavigationInfo in
 * the {@linkplain BwAppContext}.</li>
 * <li>Defining the navigation paths, target view, and host template for a facePage.
 * </li>
 * <li>Defining the next navigation page for {@linkplain FormAction FormActions}</li>
 * <li>Defining the action to be taken by a FormAction, a {@linkplain WebFormInfo}, or
 * any method that can multiple outcomes depending on the current NavigationTarget.</li>
 * <li>Validating the user's access for a specific action or the content specified
 * target.</li>
 * </ol>
 * <p>A NavigationTarget (e.g. 
 * "RECORD|USERACCT|EDIT|bwcontact.profile") 
 * supports four (4) parameters:<p>
 * <ul> 
 *  <li><b>A Target:</b> - A {@linkplain AppTargets} constant defining the target on 
 *    which to perform the designated action(e.g., RECORD, LIST,..)</li>
 *  <li><b>A Task :</b> - A {@linkplain AppTasks} that designate the system defined 
 *    Application Task to be perform. (e.g., "CNTMNG", "APPADMIN", etc.).</li>
 *  <li><b>An Action:</b> - An {@linkplain AppActions} value that define the action to 
 *    perform on the designated Target (e.g., READ, EDIT,..). </li>
 *  <li><b>A SubTask:</b> - An optional string that defines the targeted entity and/or
 *    action. The subTask can comprise sub-targets separated by a period (e.g. 
 *    "bwcontact.profile"</li>
 * </ul> 
 * 
 * <p><b>For Access Validation:</b> Only the AppTask and Action is used in the general 
 * user access validation. The Target and SubmitTask assignment could be used for more
 * custom access control to specific content. Access validation is only used is the 
 * Application supports Log-on (i.e. the Application Parameter[doLogon]!=NoLogon)</p>
 * 
 * <p><b>For Navigation:</b> To be used in the FacePage navigation, the Target's
 * Navigation Information (NavigationInfo) must be registered with the BwAppContext.
 * This registration requires a NavigationTarget definition consisting if an Action, 
 * Target, Task, and SubmitTask. For Example "RECORD|USERMNG|READ|Username". See 
 * {@linkplain NavigationInfo} for navigation information registration options.</p>
 * 
 * <p>Parsing an NavigationTarget string are subjected to the following constraints:
 * </p><ul>
 *  <li>The NavigationTarget.target must be valid {@linkplain AppTargets} value 
 *    (e.g. "RECORD"). The string check is not case-sensitive.
 *  </li>
 *  <li> The NavigationTarget.target must be valid {@linkplain AppTargets} 
 *    value (e.g. "APPADMIN") - Default="CNTMNG". 
 *    The string check is not case-sensitive.</li>
 *  <li>The NavigationTarget.action must be valid {@linkplain AppActions} value 
 *    (e.g. "READ"). The string check is not case-sensitive.</li>
 *  <li>The SubTask is optional and is up to developer to define unique 
 *    NavigationInfo for each NavigationTarget. The string check is not case-sensitive.
 *    The SubTask can include an entity reference (e.g., "bwcontact") and an optional 
 *    action/sub-target, which is joined to the subTask by an '.' character (e.g., 
 *    "bwcontact.profile"). 
 *  </li>
 * </ul>
 * 
 * <p><b>NOTES:</b></p><ol>
 * <li>The default NavigationTarget = "WEBPAGE|CNTMNG|READ|home" (i.e. the
 *  {@linkplain AppPageView#AppHomeNavTrg}), which navigate to the applications home 
 *  page.
 * </li>
 * </ol>
 * @see NavigationContext
 * @see NavigationInfo
 * @author kprins
 */
public class NavigationTarget implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * <p>Overload 1: Static parser from a formatted NavigationTarget string. It calls 
   * {@linkplain #fromString(java.lang.String, bubblewrap.navigation.enums.AppTasks) 
   * Overload 2}  with deafultTask={@linkplain AppTasks#CNTMNG}</p>
   * @param navTrgStr the input Navigation Target
   * @return the parsed NavigationTarget or null if the process fails.
   * @exception IllegalArgumentException if the input string is undefined or any of
   * the AppTasks, AppTargets, or AppActions settings cannot be resolved.
   */
  public static NavigationTarget fromString(String navTrgStr) {
    return NavigationTarget.fromString(navTrgStr, AppTasks.CNTMNG);
  }
  
  /**
   * <p>Static parser from a formatted NavigationTarget string. The standard form for a
   * NavigationTarget is "Target[|AppTask[|Action[|SubmitTask.SubTask.. 
   * [:processViewClass]]" string. The parsing of the NavigationTarget string is 
   * subjected to the following constraints:</p><ul>
   *  <li>The Target assignment is required at all cases.</li>
   *  <li>AppTask and Action is optional if no SubmitPath is required.</li>
   *  <li>To specify a SubmitPath a AppTask and Action must be define too.</li>
   *  <li>If Action is omitted, the default Action=READ is assumed.</li>
   *  <li>If AppTask is omitted, the defaultTask is used.</li>
   *  <li>If both AppTask and Action is omitted [defaultTask]|READ is assumed.</li>
   *  <li>Only Action supported by the specified AppTask (as defined for each 
   *    {@linkplain AppTasks}) can be used. All other entries will throw an 
   *    IllegalAgrumentException</li>
   *  <li>SubTask can contain multiple periods to separated sub targets/tasks. 
   *    There is no limit in limit number of sub-tasks.</li>
   * </ul>
   * if Action and/or Target is unassigned the default "INFO" and "ANY" will be used
   * If AppTask is not defined, assign the sDefaultTask, which can be null.
   * If Submit Task is not defined set to null (no submit task).
   * if (sNavTrg is an empty string or null) the default NavigationTarget[INFO|ANY]
   * will be return, which navigates to the HomePage
   * @param navTrgStr String
   * @param defaultTask the default {@linkplain AppTasks} to use if navTrg does not 
   * include an AppTask assignment. If null, assumed defaultTask="CNTMNG"
   * @return the resolved NavigationTarget.
   * @exception IllegalArgumentException if the input string is undefined or any of
   * the AppTasks, AppTargets, or AppActions settings cannot be resolved.
   */
  @SuppressWarnings("unchecked")
  public static NavigationTarget fromString(String navTrgStr, AppTasks defaultTask) {
    NavigationTarget result = null;
    AppActions appAction = null;
    AppTargets appTarget = null;
    AppTasks appTask = null;
    String submitTask = null;
    
    navTrgStr = DataEntry.cleanString(navTrgStr);
    if (navTrgStr == null) {
      throw new IllegalArgumentException("The NavigationTarget definition is empty.");
    }

    String enumName = null;    
    List<String> subString = DataEntry.splitString(navTrgStr, "|");
    if ((subString.size() > 0) && 
                  ((enumName = DataEntry.cleanUpString(subString.get(0))) != null)) {
      appTarget = Enum.valueOf(AppTargets.class, enumName);
    }
    if ((subString.size() > 1) && 
                  ((enumName = DataEntry.cleanUpString(subString.get(1))) != null)) {
      appTask = Enum.valueOf(AppTasks.class, enumName);
    }
    if ((subString.size() > 2) && 
                  ((enumName = DataEntry.cleanUpString(subString.get(2))) != null)) {
      appAction = Enum.valueOf(AppActions.class, enumName);
    }
    if ((subString.size() > 3) && 
                  ((enumName = DataEntry.cleanUpString(subString.get(1))) != null)) {
      submitTask = DataEntry.cleanLoString(subString.get(3));
    }
    
    appTask = (appTask == null)? defaultTask: appTask;
    
    result = new NavigationTarget(appTarget, appTask, appAction, submitTask);
    return result;
  }

  /**
   * Return the Default NavigationTarget[ANY|CNTMNG|READ]
   * @return NavigationTarget
   */
  public static NavigationTarget defaultNavigationTarget() {
    return new NavigationTarget(null, null, null, null);
  }

  /**
   * Private Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = 
                                    Logger.getLogger(NavigationTarget.class.getName());
  // </editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Designated Target (default= ANY)
   */
  private AppTargets appTarget;
  /**
   * The Designated AppTask (default= CNTMNG)
   */
  private AppTasks appTask;
  /**
   * The Designated Action (default= READ)
   */
  private AppActions appAction;
  /**
   * Optional Entity specific subTask (default=null)
   */
  private String subTask;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Constructor with a designated target, task and action
   * @param target the designated Navigation Target (assumed ANY if null)
   * @param task the designated Navigation Task (assumed CNTMNG if null)
   * @param action the designated Navigation Action (assumed READ if null)
   */
  public NavigationTarget(AppTargets target, AppTasks task, AppActions action) {
    this(target, task, action, null);
  }

  /**
   * Constructor with a designated target, task, action and subTask - if a actionStep
   * is required, it can be assigned using the set method.
   * @param target the designated Navigation Target (assumed ANY if null)
   * @param task the designated Navigation Task (assumed CNTMNG if null)
   * @param action the designated Navigation Action (assumed READ if null)
   * @param subTask the submit path (can be null, but can only be assigned through the
   * constructor)
   */
  public NavigationTarget(AppTargets target, AppTasks task, AppActions action, 
                                                                String subTask) {
    task = (task == null)? AppTasks.CNTMNG: task;
    action = (action == null)? AppActions.READ: action;    
    subTask = DataEntry.cleanString(subTask);
    this.appTarget = (target == null)? AppTargets.ANY: target;
    this.appAction = action;
    this.appTask = task;
    subTask = DataEntry.cleanString(subTask);
    this.subTask = (subTask == null)? null: subTask.toLowerCase();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public methods">
   /**
   * Get the NavigationTarget's Target
   * @return the assigned {@linkplain AppTargets} (default=ANY)
   */
  public final AppTargets getTarget() {
    return this.appTarget;
  }
   
  /**
   * Get the NavigationTarget's Application Task
   * @return the assigned {@linkplain AppTargets} (default=CNTMNG)
   */
  public final AppTasks getAppTask() {
    return this.appTask;
  }
  
  /**
   * Get the NavigationTarget's Action
   * @return the assigned {@linkplain AppActions} (Default=READ)
   */
  public final AppActions getAction() {
    return this.appAction;
  }

  /**
   * Get the NavigationTarget's Entity specific subTask
   * @return the assigned subTask 
   */
  public final String getSubTask() {
    return this.subTask;
  }
      
  /**
   * Check if the  NavigationTarget's target is an Entity content.
   * @return true if the NavigationTarget.Target = (RECORD | LIST)
   */
  public boolean isEntityTarget() {
    return ((AppTargets.RECORD.equals(this.appTarget)) || 
            (AppTargets.LIST.equals(this.appTarget)));
  }

  /**
   * Append the settings to sUrl as "?action=..&target=...&apptask=...&submittask=..."
   * The appTask and submitTask settings will be omitted is not defined.
   * @param baseUrl String
   * @return String
   */
  public final String appendToUrl(String baseUrl) {
    String result = DataEntry.cleanString(baseUrl);
    if (result != null) {
      String sParams = "target=" + this.appTarget.toString() + "&" 
                     + "task=" + this.appTask.toString() + "&" 
                     + "action=" + this.appAction.toString();

      if (this.subTask != null) {
        sParams += "&subtask=" + this.subTask;
      }

      if (result.indexOf("?") < 0) {
        result += "?";
      } else if (!result.endsWith("?")) {
        result += "&";
      }

      result += sParams;
    }
    return result;
  }

  /**
   * Return a Generic Crumb Caption for the NavigationTarget as a Proper case string
   * constructed using "<Action> <Target>([SubmitTask]) where SubmitTask is optional.
   * (e.g. LIST|mytaskREAD|.submitPath) => "Read List[submitPath]"). This is used as the
   * default NavigationCrumb Caption if no other is accessible or defined.
   * @return String
   */
  public String getAsCaption() {
    String result = this.appAction.toString() + " " + this.appTarget.toString();
    result = DataEntry.toProper(result);
    result += (this.subTask == null)? "": ("[" + this.subTask + "]");
    return result;
  }

  /**
   * Return the String values of the NavigationTarget was a ParameterMapBase with 
   * keys[name,target,appTask,& submitTask].  Missing values is set to "-"
   * @return return a {@linkplain ParameterMapBase}
   */
  public ParameterMap asParameterMap()  {
    ParameterMap pResult = new ParameterMap();
    try {
      pResult.put("target", this.appTarget.toString());
      pResult.put("action", this.appAction.toString());
      pResult.put("apptask",this.appTask.toString());
      pResult.put("subtask",((this.subTask == null)? "-": this.subTask));
    } catch (Exception pExp) {
      pResult = null;
      logger.log(Level.WARNING, "{0}.asParamTable Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * Compare if sNavTrg matches this.toString - case insensitive comparison
   * @param navTrgStr String
   * @return true if it is a match
   */
  public boolean equals(String navTrgStr) {
    NavigationTarget other = NavigationTarget.fromString(navTrgStr);
    return ((other != null) && (this.equals(other)));
  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Returns true if 'obj' is not null and an instance of NavigationTarget
   * with matching AppTarget, AppTask, AppAction and if (this.subTask != null) and
   * equal to obj.subTask. It will return false if obj.subTask=null and 
   * this.asubTask != null. Comparing the two subTask's is case insensitive.
   * <p><b>Notes:</b></p><ol>
   * <li>The subTask comparison constrain is to support partial search for 
   * NavigationInfo registered in the BwAppContaxt using its NavigationTarget as a 
   * registration Map Key.</li>
   * <li>When registering NavigationInfo, only full NavigationTargets (i.e., with the
   * subTask defined) should be used to prevent duplicate keys and overriding of 
   * previously registered NavigationInfo</li>
   * <li>The processView class assignments are ignored in the comparison.</li>
   * </ol>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof NavigationTarget));
    if ((result) && (obj != this)) {
      NavigationTarget other=  (NavigationTarget) obj;
      result = ((this.appTarget.equals(other.appTarget)) &&
                (this.appTask.equals(other.appTask)) &&
                (this.appAction.equals(other.appAction)) &&
                (((this.subTask == null) && (other.subTask != null)) ||
                 ((this.subTask != null) && (other.subTask == null)) ||
                 (DataEntry.isEq(this.subTask, other.subTask, true))));
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a Hash Code based on the Target, AppTask, Action and SubmitTask
   * settings.</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 29 * hash + Objects.hashCode(this.appTarget);
    hash = 29 * hash + Objects.hashCode(this.appTask);
    hash = 29 * hash + Objects.hashCode(this.appAction);
    return hash;
  }

  
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Return the ActionTask's settings as a string in the format:
   * "{target}|{apptask}{action}(|({subtask})(@{processViewClass.name}))".
   * The {subtask}and/or {actionStep} substrings are omitted if undefined.</p>
   */
  @Override
  public String toString() {
    String result = this.appTarget.toString() + "|" 
            + this.appTask.toString() + "|"
            + this.appAction.toString();
    if (this.subTask != null) {
      result += "|" + this.subTask;
    }
    return result;
  }

  /**
   * Return a Clone of the current NavigationTarget
   * @return NavigationTarget
   */
  @Override
  public NavigationTarget clone() {
    NavigationTarget result = 
       new NavigationTarget(this.appTarget, this.appTask, this.appAction, this.subTask);
   return result;
  }
  // </editor-fold>
}
