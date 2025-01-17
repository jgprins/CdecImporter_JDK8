package bubblewrap.threads.core;

import java.util.logging.Level;
import java.util.logging.Logger;
import bubblewrap.threads.interfaces.IExecProcess;
import bubblewrap.threads.interfaces.IExecProcessRunnable;

/**
 *
 * @author kprins
 */
public class ExecProcessThread extends Thread {
  
  //<editor-fold defaultstate="collapsed" desc="Static Log Reference">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ExecProcessThread.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Private reference to the runnable process
   */
  private IExecProcess mpProcess = null;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor with a IMyRunnable process reference. Call super(pProcess, 
   * pProcess.processName).
   * @param pProcess the reference process - cannot be null.
   */
  public ExecProcessThread(IExecProcessRunnable pProcess) {
    super((Runnable) pProcess, 
                  ((pProcess == null)? "Unknown Process": pProcess.getProcessName()));
    if (pProcess == null) {
      throw new NullPointerException("The Runnable Process cannot be unassigned|null.");
    }
    this.mpProcess = pProcess;
    this.setPriority(Thread.MAX_PRIORITY);
  }  
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Process Deligates">
  /**
   * Get whether the executing process's processId matches sProcessId
   * @param sProcessId the processId to match
   * @return true if matching, false if this process is unassigned.
   */
  public boolean isProcessId(String sProcessId) {
    return (this.mpProcess == null)? false: this.mpProcess.isProcessId(sProcessId);
  }
  
  /**
   * Get the executing process' name (or null if undefined)
   * @return the executing process' name or 'Unknown Process' is no process is assigned.
   */
  public String getProcessName() {
    return (this.mpProcess == null)? "Unknown Process": this.mpProcess.getProcessName();
  }
  
  /**
   * Get the executing process' processId (or null if undefined)
   * @return the executing process' processId or this.getId if no process is assigned.
   */
  public String getProcessId() {
    return (this.mpProcess == null)? Long.toString(this.getId()): 
                                                      this.mpProcess.getProcessId();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Thread">
  /**
   * OVERRIDE: Call the super method within a try-catch. It also adds server log entry
   * before and after the start of the runnable.<br/>
   * {@inheritDoc }
   */
  @Override
  public void run() {
    try {
      logger.log(Level.INFO, "Thread[{0}] - Running . . . .", this.getProcessName());
      
      super.run();
      
      logger.log(Level.INFO, "Thread[{0}] - Completed", this.getProcessName());
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "Thread[{0}] - Running Error:\n {1}",
              new Object[]{this.getProcessName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
}
