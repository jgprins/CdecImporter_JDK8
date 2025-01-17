package bubblewrap.navigation.enums;

import bubblewrap.core.enums.EnumFlagSet;
import bubblewrap.core.enums.IEnumFlag;
import bubblewrap.core.enums.IntFlag;
import bubblewrap.navigation.core.NavigationTarget;

/**
 * The AppActions enum values include all AppTasks action supported by the BubbleWrap
 * framework.  These actions are used to define {@linkplain NavigationTarget}, which in
 * turn is use for managing user access to the content or tasks defined by the 
 * NavigationTarget. These actions are categorized in the following groups:<ul>
 * <li><b>Record Processing Actions:</b> - see {@linkplain 
 * #getRecordProcessActions(boolean, boolean, boolean, boolean) getRecordProcessActions}
 * </li>
 * <li><b>Record Edit Actions:</b> - see {@linkplain 
 * #getRecordEditActions(boolean, boolean, boolean, boolean) getRecordEditActions}
 * </li>
 * <li><b>Message/Input Actions:</b> - see {@linkplain 
 * #getMsgInputActions(boolean, boolean, boolean, boolean) getMsgInputActions}
 * </li>
 * <li><b>User Access Actions:</b> - see {@linkplain 
 * #getUserAccessActions(boolean, boolean, boolean, boolean, boolean) 
 * getUserAccessActions}
 * </li>
 * <li><b>Content Management Actions:</b> - see {@linkplain 
 * #getCntMngActions(boolean, boolean, boolean, boolean, boolean) getCntMngActions}
 * </li>
 * <li><b>Email Actions:</b> - see {@linkplain 
 * #getEmailActions(boolean, boolean, boolean, boolean) getEmailActions}
 * </li>
 * <li><b>Workflow Actions:</b> - see {@linkplain 
 * #getWorkflowActions(boolean, boolean, boolean, boolean) getWorkflowActions}
 * </li>
 * <li><b>Custom Actions:</b> - see {@linkplain 
 * #getCustomActions(boolean, boolean, boolean, boolean) getCustomActions}
 * </li>
 * </ul>
 * @author kprins implements IEnumFlag
 */
public enum AppActions implements IEnumFlag {
  
  //<editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * None - value={0x00000000}
   */
  NONE (0x00000000),
  
  //<editor-fold defaultstate="collapsed" desc="Basic Record/Content Processing">
  /**
   * Record Action: For ReadOnly access
   */
  READ (0x00000001),
  /**
   * Record Action: For Read/Write access
   */
  EDIT (0x00000002),
  /**
   * Record Action: Allow Adding of new Records/content
   */
  ADD (0x00000004),
  /**
   * Record Action: Allow Deleting of new Records/content
   */
  DELETE (0x00000008),
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Record Level Edit Processes">
  /**
   * Record Edit Action: Allow Reset of edits/content
   */
  RESET (0x00000012),
  /**
   * Record Edit Action: Allow Disabling a record/content
   */
  DISABLE (0x00000022),
  /**
   * Record Edit Action: Allow Renaming a record/content
   */
  RENAME (0x00000042),
  /**
   * Record Edit Action: Allow Moving a record/content
   */
  MOVE (0x00000082),
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Message/Comment/Input Actions">
  /**
   * Form Action: For displaying Content/Information only
   */
  INFO(0x00000101),
  /**
   * Form Action: For Prompting a Use for a questions (e.g. Yes/No/Cancel response)
   */
  QUESTION (0x00000201),
  /**
   * Form Action: For requesting/allow user input
   */
  INPUT (0x00000403),
  /**
   * Form Action: For approving/reviewing Content/records
   */
  APPROVE (0x00000803),
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Content Management/Processing">
  /**
   * Content Management Action: For archiving content
   */
  ARCHIVE (0x0000100F),
  /**
   * Content Management Action: For retrieving content
   */
  RETRIEVE (0x0000101F),
  /**
   * Content Management Action: For sharing content
   */
  SHARE (0x0000210F),
  /**
   * Content Management Action: For entering QC comments on content
   */
  COMMENT (0x0000240F),
  /**
   * Content Management Action: For publishing content
   */
  PUBLISH (0x0000480F),
  /**
   * Content Management Action: For uploading content
   */
  UPLOAD (0x0000400F),
  /**
   * Content Management Action: For downloading content
   */
  DOWNLOAD (0x00008001),
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Login/Access Validation">
  /**
   * User Action: For log into the user account
   */
  LOGON (0x00010002),
  /**
   * User Action: For the user account password
   */
  RESETPW (0x00010012),
  /**
   * User Action: For resetting the user account properties
   */
  RESETUSER (0x00020012),
  /**
   * User Action: For editing the user account properties
   */
  PROFILE (0x00040002),
  /**
   * User Action: For validating the user account
   */
  VALIDATE (0x00080002),
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Email Processing">
  /**
   * Email Action: For sending e-mail
   */
  SEND (0x00100001),
  /**
   * Email Action: For receiving e-mail
   */
  RECIEVE (0x0020000F),
  /**
   * Email Action: For forwarding e-mail
   */
  FORWARD (0x00400001),
  /**
   * Email Action: For adding attachments to e-mails
   */
  ATTACH (0x00800001),
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Workflow/Database/Modeling Processes">
  /**
   * Workflow Action: For registering content
   */
  REGISTER (0x010000FF),
  /**
   * Workflow Action: For invoking a process/action
   */
  INVOKE (0x02000033),
  /**
   * Workflow Action: For executing a process/action
   */
  EXECUTE (0x04000033),
  /**
   * Workflow Action: For aborting and executing process
   */
  ABORT (0x08000023),
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Temporary/Custom Use ">
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM1 (0x10000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM2 (0x20000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM3 (0x30000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM4 (0x40000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM5 (0x50000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM6 (0x60000001),
  /**
   * Custom Action: not to be mapped for specific custom tasks
   */
  CUSTOM7 (0x70000001);
//</editor-fold>
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="AppAction Class Definition">
  private final IntFlag value;

  //<editor-fold defaultstate="collapsed" desc="Constructors">  
  /**
   * Public Constructor
   */
  private AppActions(int value) {
    this.value = IntFlag.valueOf(value);
  }
  
  @Override
  public IntFlag getValue() {
    return this.value;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Properties">
  /**
   * Check if this AppAction represents a READ action
   * @return true if bit[1] is set
   */
  public boolean isRead() {
    return this.value.isBitSet(1);
  }
  
  /**
   * Check if this AppAction represents a EDIT action
   * @return true if bit[2] is set
   */
  public boolean isEdit() {
    return this.value.isBitSet(2);
  }
  
  /**
   * Check if this AppAction represents a ADD action
   * @return true if bit[3] is set
   */
  public boolean isAdd() {
    return this.value.isBitSet(3);
  }
  
  /**
   * Check if this AppAction represents a DELETE action.
   * @return true if bit[4] is set
   */
  public boolean isDelete() {
    return this.value.isBitSet(4);
  }
  //</editor-fold>
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Find the AppAction matching the <tt>intVal</tt> value.
   * @param intVal the integer value of the AppAction
   * @return the AppAction or null if not found
   */
  public static AppActions findByValue(int intVal) {
    return AppActions.findByValue(IntFlag.valueOf(intVal));
  }
  
  /**
   * Find the AppAction matching the <tt>intFlag</tt> value.
   * @param intFlag the IntFlag value of the AppAction
   * @return the AppAction or null if not found
   */
  public static AppActions findByValue(IntFlag intFlag) {
    AppActions result = null;
    if (intFlag != null) {
      for (AppActions enumVal : AppActions.values()) {
        if (intFlag.equals(enumVal.getValue())) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Get all the Record Processing Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allRecordProcessActions() {
    return AppActions.getRecordProcessActions(true, true, true, true);
  }
  
  /**
   * Get the Record Processing Actions based on the flag settings
   * @param read true to add {@linkplain #READ}
   * @param edit true to add {@linkplain #EDIT}
   * @param add true to add {@linkplain #ADD}
   * @param delete true to add {@linkplain #DELETE}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getRecordProcessActions(boolean read,
          boolean edit, boolean add, boolean delete) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (read) {
      result.add(AppActions.READ);
    }
    if (edit) {
      result.add(AppActions.EDIT);
    }
    if (add) {
      result.add(AppActions.ADD);
    }
    if (delete) {
      result.add(AppActions.DELETE);
    }
    return result;
  }
  
  /**
   * Get all the Record Edit Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allRecordEditActions() {
    return AppActions.getRecordEditActions(true, true, true, true);
  }
  
  /**
   * Get the Record Edit Actions based on the flag settings
   * @param reset true to add {@linkplain #RESET}
   * @param disabled true to add {@linkplain #DISABLED}
   * @param rename true to add {@linkplain #RENAME}
   * @param move true to add {@linkplain #MOVE}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getRecordEditActions(boolean reset,
          boolean disabled, boolean rename, boolean move) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (reset) {
      result.add(AppActions.RESET);
    }
    if (disabled) {
      result.add(AppActions.DISABLE);
    }
    if (rename) {
      result.add(AppActions.RENAME);
    }
    if (move) {
      result.add(AppActions.MOVE);
    }
    return result;
  }
  
  /**
   * Get all the Message/Input Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allMsgInputActions() {
    return AppActions.getMsgInputActions(true, true, true, true);
  }
  
  /**
   * Get the Message/Input Actions based on the flag settings
   * @param info true to add {@linkplain #INFO}
   * @param question true to add {@linkplain #QUESTION}
   * @param input true to add {@linkplain #INPUT}
   * @param approve true to add {@linkplain #APPROVE}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getMsgInputActions(boolean info,
          boolean question, boolean input, boolean approve) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (info) {
      result.add(AppActions.INFO);
    }
    if (question) {
      result.add(AppActions.QUESTION);
    }
    if (input) {
      result.add(AppActions.INPUT);
    }
    if (approve) {
      result.add(AppActions.APPROVE);
    }
    return result;
  }
  
  /**
   * Get all the User Access Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allUserAccessActions() {
    return AppActions.getUserAccessActions(true, true, true, true, true);
  }
  
  /**
   * Get the User Access Actions based on the flag settings
   * @param logon true to add {@linkplain #LOGON}
   * @param resetpw true to add {@linkplain #RESETPW}
   * @param resetuser true to add {@linkplain #RESETUSER}
   * @param profile true to add {@linkplain #PROFILE}
   * @param valdiate true to add {@linkplain #VALIDATE}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getUserAccessActions(boolean logon,
          boolean resetpw, boolean resetuser, boolean profile, boolean valdiate) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (logon) {
      result.add(AppActions.LOGON);
    }
    if (resetpw) {
      result.add(AppActions.RESETPW);
    }
    if (resetuser) {
      result.add(AppActions.RESETUSER);
    }
    if (profile) {
      result.add(AppActions.PROFILE);
    }
    if (valdiate) {
      result.add(AppActions.VALIDATE);
    }
    return result;
  }
  
  /**
   * Get all the Content Management Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allCntMngActions() {
    return AppActions.getCntMngActions(true, true, true, true, true, true, true);
  }
  
  /**
   * Get the Content Management Actions based on the flag settings
   * @param archive true to add {@linkplain #ARCHIVE}
   * @param retrieve true to add {@linkplain #RETRIEVE}
   * @param share true to add {@linkplain #SHARE}
   * @param comment true to add {@linkplain #COMMENT}
   * @param publish true to add {@linkplain #PUBLISH}
   * @param upload true to add {@linkplain #UPLOAD}
   * @param download true to add {@linkplain #DOWNLOAD}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getCntMngActions(boolean archive,
          boolean retrieve, boolean share, boolean comment, boolean publish, 
          boolean upload, boolean download) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (archive) {
      result.add(AppActions.ARCHIVE);
    }
    if (retrieve) {
      result.add(AppActions.RETRIEVE);
    }
    if (share) {
      result.add(AppActions.SHARE);
    }
    if (comment) {
      result.add(AppActions.COMMENT);
    }
    if (publish) {
      result.add(AppActions.PUBLISH);
    }
    if (upload) {
      result.add(AppActions.UPLOAD);
    }
    if (download) {
      result.add(AppActions.DOWNLOAD);
    }
    return result;
  }
  
  /**
   * Get all the Email Processing Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allEmailActions() {
    return AppActions.getEmailActions(true, true, true, true);
  }
  
  /**
   * Get the Email Processing Actions based on the flag settings
   * @param send true to add {@linkplain #INFO}
   * @param recieve true to add {@linkplain #RECIEVE}
   * @param forward true to add {@linkplain #FORWARD}
   * @param attach true to add {@linkplain #ATTACH}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getEmailActions(boolean send,
          boolean recieve, boolean forward, boolean attach) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (send) {
      result.add(AppActions.SEND);
    }
    if (recieve) {
      result.add(AppActions.RECIEVE);
    }
    if (forward) {
      result.add(AppActions.FORWARD);
    }
    if (attach) {
      result.add(AppActions.ATTACH);
    }
    return result;
  }
  
  /**
   * Get all the Workflow Processing Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allWorkflowActions() {
    return AppActions.getWorkflowActions(true, true, true, true);
  }
  
  /**
   * Get the Workflow Processing Actions based on the flag settings
   * @param register true to add {@linkplain #REGISTER}
   * @param invoke true to add {@linkplain #INVOKE}
   * @param execute true to add {@linkplain #EXECUTE}
   * @param abort true to add {@linkplain #ABORT}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getWorkflowActions(boolean register,
          boolean invoke, boolean execute, boolean abort) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (register) {
      result.add(AppActions.REGISTER);
    }
    if (invoke) {
      result.add(AppActions.INVOKE);
    }
    if (execute) {
      result.add(AppActions.EXECUTE);
    }
    if (abort) {
      result.add(AppActions.ABORT);
    }
    return result;
  }
  
  /**
   * Get all the Workflow Processing Actions
   * @return the Set of AppActions
   */
  public static EnumFlagSet<AppActions> allCustomActions() {
    return AppActions.getCustomActions(true, true, true, true, true, true, true);
  }
  
  /**
   * Get the Custom Actions based on the flag settings
   * @param custom1 true to add {@linkplain #CUSTOM1}
   * @param custom2 true to add {@linkplain #CUSTOM2}
   * @param custom3 true to add {@linkplain #CUSTOM3}
   * @param custom4 true to add {@linkplain #CUSTOM4}
   * @param custom5 true to add {@linkplain #CUSTOM5}
   * @param custom6 true to add {@linkplain #CUSTOM6}
   * @param custom7 true to add {@linkplain #CUSTOM7}
   * @return the Set of AppActions or an empty set if no flags were set
   */
  public static EnumFlagSet<AppActions> getCustomActions(boolean custom1,
          boolean custom2, boolean custom3, boolean custom4,
          boolean custom5, boolean custom6, boolean custom7) {
    EnumFlagSet<AppActions> result = EnumFlagSet.noneOf(AppActions.class);
    if (custom1) {
      result.add(AppActions.CUSTOM1);
    }
    if (custom2) {
      result.add(AppActions.CUSTOM2);
    }
    if (custom3) {
      result.add(AppActions.CUSTOM3);
    }
    if (custom4) {
      result.add(AppActions.CUSTOM4);
    }
    if (custom5) {
      result.add(AppActions.CUSTOM5);
    }
    if (custom6) {
      result.add(AppActions.CUSTOM6);
    }
    if (custom7) {
      result.add(AppActions.CUSTOM7);
    }
    return result;
  }
//</editor-fold>
}
