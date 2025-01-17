package bubblewrap.app.context;

import bubblewrap.io.DataEntry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the abstract method for all Application Registry classes. 
 * @author kprins
 */
public abstract class BwAppDeployer {

  // <editor-fold defaultstate="collapsed" desc="Static Methods/Fields">
  /**
   * Static Error Logger for the Facade Class
   */
  protected final static Logger logger = 
                                  Logger.getLogger(BwAppDeployer.class.getSimpleName());
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Fields">
  /**
   * Placeholder for the Classes name
   */
  private String msName = null;
  /**
   * Placeholder for Error Message recorded during the initiateApp process
   */
  private String msErrMsg = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor(s)">
  /**
   * Constructor - If deployerName is undefined, set this.Name=Class.simpleName, else 
   * use deployerName
   * @param deployerName a unique name for the deployer
   */
  public BwAppDeployer(String deployerName) {
    deployerName = DataEntry.cleanString(deployerName);
    this.msName = (deployerName == null)? this.getClass().getSimpleName(): deployerName;
  }

  /**
   * CAN OVERRIDE: Called by the constructor. Can be overridden by inheritors to add
   * custom initiation of the Class instance
   */
  protected void onInitRegistry() {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods/Properties">
  /**
   * Get the BwAppDeployer's Name
   * @return String
   */
  public String getName() {
    return this.msName;
  }

  /**
   * Get the Error Message. Return an empty string if undefined.
   * @return String
   */
  public String getErrMsg() {
    return (this.msErrMsg == null) ? "" : this.msName + " Error:\n " + this.msErrMsg;
  }

  /**
   * Set an error message - if a previous message has been record, the new message will
   * be appended with a line break
   * @param sErrMsg String
   */
  protected void setErrMsg(String sErrMsg) {
    sErrMsg = DataEntry.cleanString(sErrMsg);
    if (sErrMsg == null) {
      return;
    }

    this.msErrMsg = (this.msErrMsg == null) ? "" : this.msErrMsg + "\n";
    this.msErrMsg += sErrMsg;
  }

  /**
   * Check if the AppRegsitry has an error message
   * @return boolean
   */
  public boolean hasError() {
    return (this.msErrMsg != null);
  }

  /**
   * <p>Called to initiate the application.  It clears prior error messages before
   * calls {@linkplain #onInitRegistry() onInitRegistry} to initiate the registry
   * and if no error occurred, it calls {@linkplain #initiateApp() initiateApp} to
   * execute the Application errors.</p>
   * <p>It traps any raises exception and assigns it as an error message.  It returns 
   * true if the registration process was successfully completed or false if an error 
   * had been recorded during this process. If an error occur it will call rollBackApp()
   * to undo the initiation process.</p>
   * @return true if the process was successful and false if an error was recorded.
   */
  public final boolean initiateApp() {
    boolean result = false;
    try {
      logger.log(Level.INFO, "Start {0}", this.getName());
      this.msErrMsg = null;      
      this.onInitRegistry();
      if (!this.hasError()) {
        this.onInitiateApp();
      }
    } catch (Exception pExp) {
      this.setErrMsg(pExp.getMessage());
    }

    result = (!this.hasError());
    if (this.hasError()) {
      logger.log(Level.INFO, "{0} Error:\n{1}", 
                                new Object[]{this.getName(), this.getErrMsg()});
      this.rollbackApp();
    } else {
      logger.log(Level.INFO, "Completed {0}", this.getName());
    }
    return result;
  }

  /**
   * Called after an error occurred to rollback the initiation of the application.
   * It calls the abstract onRollbackpp method and trap any raised exception but does
   * not assign it as an error message.
   */
  public final void rollbackApp() {
    try {
      logger.log(Level.INFO, "Start {0} Rollback", this.getName());
      this.onRollbackApp();
    } catch (Exception pExp) {
    } finally {
      logger.log(Level.INFO, "Completed {0} Rollback", this.getName());
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Must be implemented to add the inheritors custom initiation actions
   */
  protected abstract void onInitiateApp() throws Exception;

  /**
   * ABSTRACT: Must be implemented to add the inheritors custom initiation actions
   */
  protected abstract void onRollbackApp() throws Exception;
  // </editor-fold>
}
