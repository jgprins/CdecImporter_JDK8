package bubblewrap.threads.interfaces;

import bubblewrap.threads.core.ExecProcess;
import bubblewrap.threads.core.ExecProcessLogger;
import java.util.logging.Level;

/**
 * An interface, typically assigned to {@linkplain ExecProcess}, to give other process
 * to have enter process logs without having access to any other features of the owner
 * class.
 * @author kprins
 */
public interface IProcessLogger {
  
  /**
   * Call to send to log to the underlying {@linkplain ExecProcessLogger} (if assigned) 
   * or to the server log (if not).
   * @param logLevel the log {@linkplain Level}
   * @param logMsg the message
   */
  public void log(Level logLevel, String logMsg);
  
  /**
   * <p>Call to send to log to the underlying {@linkplain ExecProcessLogger} (if
   * assigned) or to the server log (if not). The reference of each argument in the args
   * array should reference by its index in the logMsg</p> 
   * <p>Example:<br/>
   * &nbsp&nbsp&nbsp log{Level.Info, "{0} Errors:\n{1}", <br/>
   * &nbsp&nbsp&nbsp new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
   * </p>
   * @param logLevel the log Level
   * @param logMsg the message
   * @param args the arguments to format the Message
   */
  public void log(Level logLevel, String logMsg, Object[] args);
}
