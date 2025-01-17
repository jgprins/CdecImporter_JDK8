package bubblewrap.threads.core;

import java.util.logging.Level;

/**
 * <p>An ExecProcessLogHandler for writing the Log to the Server Log and clear the 
 * log in the ExecprocessLogger if ({@linkplain #doClearLog() this.doClearLog}).</p>
 * @author kprins
 */
public class ExecProcessLogToServerHandler extends ExecProcessLogHandler {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessLogToServerHandler(String sProcessName) {
    super(sProcessName);        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements Runnable">
  /**
   * {@inheritDoc} <p>OVERRIDE: Call the assigned ExecProcessLogger's {@linkplain
   * ExecProcessLogger#dumpLog() dumpLog} method and if (this.doClearLog), call the
   * logger's clearLog method.</p>
   */
  @Override
  public void run() {
    try {
      ExecProcessLogger pLogger = this.getLogger();
      if (pLogger == null) {
        throw new Exception("The ExcProcess Logger is not assigned.");
      }
      
      pLogger.dumpLog();
      if (this.doClearLog()) {
        pLogger.clearLog();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.run Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      this.stopSchedule();
    }
  }
  //</editor-fold>  
}
