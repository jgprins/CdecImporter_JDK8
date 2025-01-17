package bubblewrap.navigation.enums;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.enums.BwEnum;

/**
 * BwEnum for managing application administration input/settings
 * @author kprins
 */
public class AdminCategory extends BwEnum<AdminCategory> {
  
  //<editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * Category[Content Management, 0] for content specific items/settings (Default)
   */
  public final static AdminCategory CONTENT = new AdminCategory("Content Management", 0);
  /**
   * Category[User Accounts, 1] for membership specific items/settings
   */
  public final static AdminCategory MEMBERS = new AdminCategory("User Accounts", 1);  
  /**
   * Category[Application Admin, 2] for application administration specific items/settings
   */
  public final static AdminCategory ADMIN = new AdminCategory("Application Settings", 2);
  /**
   * Category[Development, 3] for content specific items/settings
   */
  public final static AdminCategory DEVELOP = new AdminCategory("Development Settings", 3);
  /**
   * Category[System Settings, 5] for system specific items/settings
   */
  public final static AdminCategory SYSTEM = new AdminCategory("System Settings", 5);
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  private AdminCategory(String name, int value) {
    super(name,value);
  }
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Call to get the Application's Delegate AdminCategory class
   * @return the delegate class or AdminCategory
   */
  public static Class<? extends AdminCategory> getAppClass() {
    Class<? extends AdminCategory> result = null;
    BwAppContext appCtx = BwAppContext.doLookup();
    if (appCtx != null) {
      try {
        result = appCtx.getDelegateClass(AdminCategory.class);
      } catch (Exception exp) {
        throw new IllegalArgumentException(AdminCategory.class.getSimpleName()
                + ".getAppClass Error:\n " + exp.getMessage());
      }
    }
    return (result == null)? AdminCategory.class: result;
  }
  
  /**
   * Call to convert the AdminCategory value to the associated Enum value using the
   * Application delegate AdminCategory class
   * @param value the value for which top find a match
   * @return the matching enum value or {@linkplain #CONTENT} is not found.
   */
  public static AdminCategory fromValue(Integer value) {
    AdminCategory result = null;
    if (value != null) {
      try {
        Class<? extends AdminCategory> enumClass = AdminCategory.getAppClass();
        result = BwEnum.valueOf(enumClass, value);
      } catch (Exception exp) {
        result =  null;
      }
    }
    return (result == null)? AdminCategory.CONTENT: result;
  }
  //</editor-fold>
}
