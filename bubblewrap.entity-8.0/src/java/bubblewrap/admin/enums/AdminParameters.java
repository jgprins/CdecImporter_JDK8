package bubblewrap.admin.enums;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import bubblewrap.io.params.ParameterMap;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A set a Admin Parameter Keys (constants) used in getting and setting the Parameter
 * Settings.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class AdminParameters implements Serializable {  
  
  //<editor-fold defaultstate="collapsed" desc="Static Logged">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                    Logger.getLogger(AdminParameters.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Session Parameter Management">
  /**
   * The key for registering the AdminParameters
   */
  private static final String SessionKey = "adminparameters";
  
  /**
   * Called to get the Session's set of cached parameters
   * @return a {@linkplain ParameterMap} instance for storing the session parameters.
   */
  private static ParameterMap getSessionParameters(boolean initNew) {
    ParameterMap result = null;
    try {
      result = SessionHelper.getClassParameter(ParameterMap.class, 
                                                          AdminParameters.SessionKey);
      if ((result == null) && (initNew)) {
        result = new ParameterMap();
        SessionHelper.setClassParameter(ParameterMap.class, 
                                                  AdminParameters.SessionKey, result);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.SessionParameters.get Error:\n {1}",
              new Object[]{AdminParameters.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to get the Session's set of cached parameters. It calls and return 
   * {@linkplain #getSessionParameters(boolean) 
   * AdminParameters.getSessionParameters(true)}
   * @return a {@linkplain ParameterMap} instance for storing the session parameters.
   */
  public static ParameterMap SessionParameters() {    
    return AdminParameters.getSessionParameters(true);
  }
  
  /**
   * Get a Session Parameter value. If the Session Parameter is unassigned it returns
   * the <tt>defautlValue</tt>, else it return the assigned value.
   * @param <TVal> extends Serializable
   * @param parKey the parameter Key - return the default if null|""
   * @param defaultValue the value to return if the parameter is unassigned.
   * @return teh assign or default value.
   */
  public static <TVal extends Serializable> TVal getSessionParameter(String parKey, 
                                                                   TVal defaultValue) {
    TVal result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      ParameterMap sessionMap = null;
      if ((parKey != null) && 
              ((sessionMap = AdminParameters.getSessionParameters(true)) != null) &&
              (sessionMap.containsKey(parKey))) {
        result = sessionMap.get(parKey, defaultValue);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getSessionParameter Error:\n {1}",
              new Object[]{AdminParameters.class.getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Set a Session Parameter. If <tt>parValue</tt> = null|"" the parameter will 
   * be removed - defaulting to the system parameter setting (if applicable).
   * @param parKey the parameter's map Key (must be assigned)
   * @param parValue the new parameter value (null|"" to reset value).
   */
  public static void setSessionParameter(String parKey, String parValue) {
    try {
      parKey = DataEntry.cleanString(parKey);
      parValue = DataEntry.cleanString(parValue);
      if (parKey == null) {
        throw new Exception("The Parameter Key is undefined.");
      }
      
      ParameterMap sessionMap = AdminParameters.getSessionParameters(true);
      sessionMap.put(parKey, parValue);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSessionParameter Error:\n {1}",
              new Object[]{AdminParameters.class.getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Admin App Parameter Keys">
  /**
   * Get the Log-on NavigationTarget Key - for setting the ActionTarget of the 
   * page to navigate to after log-on.
   */
  public static final String LogonNavTrgKey = "Admin.LogonNavTrg";
  /**
   * Static reference to the DoLogonKey - for the flag that control whether the 
   * application requires user log-on or not (0-No/1=Yes)
   */
  public static final String DoLogonKey = "Admin.DoLogon";
  /**
   * The Session Parameter Key for the Last Active Menu.  
   */
  public static final String LastMenuKey = "NavTrg.LastMenu";
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the String Parameter value. It first check is a SessionParameter is assigned
   * and return the value if assigned. Otherwise, it get the value assigned to
   * {@linkplain BwAppContext}. If no assigned value is found or if an error occurred,
   * the <tt>defaultValue</tt> is returned. Errors are trapped and logged.
   * @param parKey the parameterKey to search for
   * @param defaultValue the default value to return if the value is undefined
   * @return the current value or the defaultValue if not found or if it is unassigned.
   */
  public static String getParameter(String parKey, String defaultValue) {
    String result = null;
    try {
      result = AdminParameters.getSessionParameter(parKey, null);
      if (result == null) {
        BwAppContext appCtx = null;
        if (((parKey = DataEntry.cleanString(parKey)) != null) &&
                ((appCtx = BwAppContext.doLookup()) != null)) {
          result = appCtx.getParameter(parKey);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "AdminParameters.getParameter Error:\n {0}",
              exp.getMessage());
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Boolean Parameter value. It first check is a SessionParameter is assigned
   * and return the value if assigned. Otherwise, it get the value assigned to
   * {@linkplain BwAppContext}. If no assigned value is found or if an error occurred,
   * the <tt>defaultValue</tt> is returned.  Errors are trapped and logged.
   * @param parKey the parameterKey  to search for
   * @param defaultValue the default  value to return if the value is undefined
   * @return the current value converted to a boolean value or the defaultValue if not
   * found or if it is unassigned.
   */
  public static Boolean getBoolParameter(String parKey, Boolean defaultValue) {
    Boolean result = null;
    try {
      result = AdminParameters.getSessionParameter(parKey, null);
      if (result == null) {
        BwAppContext appCtx = null;
        if (((parKey = DataEntry.cleanString(parKey)) != null) &&
                ((appCtx = BwAppContext.doLookup()) != null)) {
          result = appCtx.getBoolParameter(parKey);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "AdminParameters.getBoolParameter Error:\n {0}",
              exp.getMessage());
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Integer Parameter value.  It first check is a SessionParameter is assigned
   * and return the value if assigned. Otherwise, it get the value assigned to
   * {@linkplain BwAppContext}. If no assigned value is found or if an error occurred,
   * the <tt>defaultValue</tt> is returned.  Errors are trapped and logged.
   * @param parKey the parameterKey  to search for
   * @param defaultValue the default  value to return if the value is undefined
   * @return the current value converted to a Integer value or the defaultValue if not
   * found or if it is unassigned.
   */
  public static Integer getIntParameter(String parKey, Integer defaultValue) {
    Integer result = null;
    try {
      result = AdminParameters.getSessionParameter(parKey, null);
      if (result == null) {
        BwAppContext appCtx = null;
        if (((parKey = DataEntry.cleanString(parKey)) != null) &&
                ((appCtx = BwAppContext.doLookup()) != null)) {
          result = appCtx.getIntParameter(parKey);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "AdminParameters.getIntParameter Error:\n {0}",
              exp.getMessage());
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Boolean Parameter value.  It first check is a SessionParameter is assigned
   * and return the value if assigned. Otherwise, it get the value assigned to
   * {@linkplain BwAppContext}. If no assigned value is found or if an error occurred,
   * the <tt>defaultValue</tt> is returned.  Errors are trapped and logged.
   * @param parKey the parameterKey  to search for
   * @param defaultValue the default  value to return if the value is undefined
   * @return the current value converted to a double value or the defaultValue if not
   * found or if it is unassigned.
   */
  public static Double getDoubleParameter(String parKey, Double defaultValue) {
    Double result = null;
    try {
      result = AdminParameters.getSessionParameter(parKey, null);
      if (result == null) {
        BwAppContext appCtx = null;
        if (((parKey = DataEntry.cleanString(parKey)) != null) &&
                ((appCtx = BwAppContext.doLookup()) != null)) {
          result = appCtx.getDoubleParameter(parKey);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "AdminParameters.getDoubleParameter Error:\n {0}",
              exp.getMessage());
    }
    return (result == null)? defaultValue: result;
  }
  //</editor-fold>
}
