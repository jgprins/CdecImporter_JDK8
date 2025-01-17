package bubblewrap.threads.interfaces;

import java.io.Serializable;

/**
 * A interface to identify the runnable process
 * @author kprins
 */
public interface IExecProcess extends Serializable {
  
  /**
   * Get the Runnable processId
   * @return the unique processId
   */
  String getProcessId();
  
  /**
   * Get the Runnable process' Name
   * @return the unique processName
   */
  String getProcessName();
  
  /**
   * A Case-Insensitive comparison between sProcessId and this.processId.
   * @param sProcessId processId to compare
   * @return true if processId's match
   */
  boolean isProcessId(String sProcessId);
}
