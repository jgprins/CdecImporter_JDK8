package bubblewrap.threads.core;

import java.util.concurrent.Delayed;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.TimeUnit;
import bubblewrap.threads.interfaces.IExecProcess;

/**
 * A Wrapper for the Outer RunnableScheduledFuture task and an {@linkplain ExecProcess} 
 * to extend to RunnableScheduledFuture as an {@linkplain IExecProcess} with a 
 * ProcessId and ProcessName. Used by {@linkplain ThreadQueue} to track processes.
 * @author kprins
 */
public class ExecProcessScheduledFuture<TResult> extends ExecProcessFuture<TResult> 
                               implements RunnableScheduledFuture<TResult> {
    
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessScheduledFuture(ExecProcess pRunnable, 
                              RunnableScheduledFuture<TResult> pTask, TResult pResult) {
    super(pRunnable, pTask, pResult);
  }
  // </editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="Implements RunnableScheduledFuture">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return false - all rescheduling is handled by Thread
   * </p>
   */
  @Override
  public boolean isPeriodic() {
    return false;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return this.outerTask.getDelay or 0 if unassigned
   * </p>
   */
  @Override
  public long getDelay(TimeUnit timeUnit) {
    RunnableScheduledFuture<TResult> outerTask = this.getOuterTask();
    return (outerTask == null)? 0: outerTask.getDelay(timeUnit);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return this.outerTask.compareTo or 0 if unassigned
   * </p>
   */
  @Override
  public int compareTo(Delayed other) {
    RunnableScheduledFuture<TResult> outerTask = this.getOuterTask();
    return (outerTask == null)? 0: outerTask.compareTo(other);
  }
  //</editor-fold>
}
