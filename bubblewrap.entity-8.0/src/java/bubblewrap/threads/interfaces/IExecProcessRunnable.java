package bubblewrap.threads.interfaces;

/**
 * A Runnable extended to include an IExecProcess interface and having a {@linkplain 
 * #clearAll() clearAll}
 * @author kprins
 */
public interface IExecProcessRunnable extends Runnable, IExecProcess{  
  /**
   * ABSTRACT: Called to clear all the ExecProcess settings
   */
  void clearAll();
}
