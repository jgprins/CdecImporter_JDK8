package bubblewrap.navigation.enums;

import bubblewrap.io.DataEntry;
import bubblewrap.navigation.core.NavigationTarget;

/**
 * The Application Tasks supported by the BubbleWrap framework. These tasks are used
 * in the {@linkplain NavigationTarget} definition to define unique paths to content
 * and to manage user access to restricted content.
 * @author kprins
 */
public enum AppTasks {
  //<editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * The Task for Initiating and Managing the Web Site Settings (isSystem=true)
   * Category: SYSTEM
   */
  WEBMNG(1, "Initiate and Manage the Web Site Settings.",
          AdminCategory.SYSTEM),
  /**
   * The Task for Registering System Settings
   * Category: SYSTEM
   */
  DBMNG(2, "The task of registering require system records to a new database or "
          + "resetting existing records to default values.",
          AdminCategory.SYSTEM),
  /**
   * Access and Manage Workflow and Task Definitions.
   * Category: ADMIN
   */
  WFDEF (3, "Access and Manage Workflow and Task Definitions.",
          AdminCategory.ADMIN),
  /**
   * Manage the Application's Admin Module - registering Roles and Role-Task Access,
   * User Groups and User Group Role assignments
   * Category: ADMIN
   */
  ADMINMNG(4,"Manage the Application's Admin Module - registering Roles and Role-Task "
          + "Access, User Groups and User Group Role assignments.",
          AdminCategory.ADMIN),
  /**
   * Manage the User Accounts and the User Group and User Role Assignments and
   * accessing user accounts.
   * Category: MEMBERS
   */
  USERMNG(5, "Manage the User Accounts and the User Group and User Role Assignments "
          + "and accessing user accounts.",
          AdminCategory.MEMBERS),
  /**
   * User Log on, profile editing, account validation, reset password, or user settings.
   * Category: MEMBERS
   */
  USERACCT(6, "User Log on, profile editing, account validation, reset password, or "
          + "user settings.",
          AdminCategory.MEMBERS),
  /**
   * Manage the General Application Settings, Lookup Values, etc.
   * Category: ADMIN
   */
  APPADMIN(7, "Manage the General Application Settings, Lookup Values and "
          + "other Assignments.",
          AdminCategory.ADMIN),
  /**
   * Execute Workflows, Manage Workflow Threads, and Manage Workflow Archive & Logs
   * Category: CONTENT
   */
  WFEXEC(8, "Execute Workflows, Manage Workflow Threads, and Manage Workflow Archive "
          + "and Logs.",
          AdminCategory.CONTENT),
  /**
   * Managing Content Management Settings.
   * Category: CONTENT
   */
  CNTADMIN(9, "Managing Content Management Settings.",
          AdminCategory.CONTENT),
  /**
   * Manage the application's contents including editing, approving, removing,
   * archiving, retrieving content.
   * Category: CONTENT
   */
  CNTMNG(10, "Manage the application's contents including editing, approving, "
          + "removing, archiving, retrieving content.",
          AdminCategory.CONTENT),
  /**
   * Sending, receiving, or forwarding messages with or without attachments.
   * Category: CONTENT
   */
  EMAIL (11, "Sending, receiving, or forwarding messages with or without attachments.",
          AdminCategory.CONTENT),
  
  /**
   * Access unrestricted or public content.
   * Category: CONTENT
   */
  BROWSE(12, "Access unrestricted or public content.",
          AdminCategory.CONTENT);
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="AppTask Class definitions">
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  public final int value;
  /**
   * A short description of the task
   */
  public final String taskDesc;
  /**
   * Task's AdminCategory.
   */
  public final AdminCategory adminCategory;
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  private AppTasks(int value, String desc, AdminCategory category) {
    this.value = value;
    this.taskDesc = DataEntry.cleanString(desc);
    this.adminCategory = category;    
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * Get whether this is a System Task
   * @return true if (this.adminCategory = SYSTEM).
   */
  public boolean isSystem() {
    return (AdminCategory.SYSTEM.equals(this.adminCategory));
  }
  //</editor-fold>
  //</editor-fold>  
}
