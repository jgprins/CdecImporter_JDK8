package bubblewrap.threads.core;

/**
 * An abstract ExecProcess for handling the writing of the ProcessLog to the server or 
 * to a file. This ExecProcess does not record its own actions to the managing executors
 * Process Log.
 * @author kprins
 */
public abstract class ExecProcessLogHandler extends ExecProcess {
  
  //<editor-fold defaultstate="collapsed" desc="comment">
  /**
   * Private Reference to the ProcessLog to handle.
   */
  private ExecProcessLogger mpProcessLog;
  /**
   * Flag controlling whether Log should be cleared after handling the log.
   * (Default=false)
   */
  private Boolean mbClearLog;
  //</editor-fold>
  

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessLogHandler(String sProcessName) {
    super(sProcessName,false);  
    this.mpProcessLog = null;
    this.mbClearLog = null;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.mpProcessLog = null;
  }
  
  /**
   * Call to set the ProceessLog to handle during run.
   * @param pProcessLog the ExecProcessLogger to process.
   */
  protected void setLogger(ExecProcessLogger pProcessLog) {
    this.mpProcessLog = pProcessLog;
  }
  
  /**
   * Get the ExecProcessLogger to process
   * @return the assigned value or null if unassigned.
   */
  protected ExecProcessLogger getLogger() {
    return this.mpProcessLog;
  }
  
  /**
   * Set the Flag to clear the Log after successful execution of this process.
   * (Default= false)
   * @param bClearLog true to clear the log 
   */
  protected void setClearLog(boolean bClearLog) {
    this.mbClearLog = (!bClearLog)? null: bClearLog;
  }
  
  /**
   * Get whether the log should be clear after a successful completion of this action.
   * (Default= false).
   * @return true to clear the log
   */
  protected boolean doClearLog() {
    return ((this.mbClearLog != null) && (this.mbClearLog));
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements Runnable">
  /**
   * {@inheritDoc} <p>OVERRIDE: </p>
   */
  @Override
  public void clearAll() {
    this.mpProcessLog = null;
  }
  //</editor-fold>  
}
