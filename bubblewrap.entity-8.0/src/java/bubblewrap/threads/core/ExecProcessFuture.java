package bubblewrap.threads.core;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import bubblewrap.io.DataEntry;
import bubblewrap.threads.interfaces.IExecProcess;

/**
 * A Wrapper for the Outer RunnableScheduledFuture task and an {@linkplain ExecProcess} 
 * to extend to RunnableScheduledFuture as an {@linkplain IExecProcess} with a 
 * ProcessId and ProcessName. Used by {@linkplain ThreadQueue} to track processes.
 * @author kprins
 */
public class ExecProcessFuture<TResult> extends FutureTask<TResult> 
                                                                implements IExecProcess {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Outer RunnableFuture reference
   */
  private RunnableFuture<TResult> outerTask;
  /**
   * Placeholder for the Executable Process Reference
   */
  private ExecProcess execProc;
  /**
   * Placeholder for a Execution exception that is assigned to the Scheduled Future
   * task by the ExecProcess it is for as a wrapper.
   */
  private ExecutionException executeError;
  /**
   * A Placeholder for assigning the Executing task reference during execution.
   */
  private Thread execThread;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessFuture(ExecProcess execProc, 
                                  RunnableFuture<TResult> outerTask, TResult result) {
    super(execProc, result);
    if (execProc == null) {
      throw new NullPointerException("The Runnable cannot be uanssigned.");
    }        
    if (outerTask == null) {
      throw new NullPointerException("The Runnable Future Task cannot be unassigned.");
    }
    this.execProc = execProc;
    this.outerTask = outerTask;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Properties">
  /**
   * Get a Reference to this ScheduledTask's Runnable
   * @return the assigned ExecProcess
   */
  public ExecProcess getRunnable() {
    return this.execProc;
  }
  
  /**
   * Get whether the Task's Runnable require process logging
   * @return true if this.runnable=null or this.runnable.doProcessLog.
   */
  public boolean doProcessLog() {
    return ((this.execProc == null) || (this.execProc.doProcessLog()));
  }
  
  /**
   * Called to assign the ExecProcessLogger to the Future's Runnable (ExcProcess).
   * If this.runnable!=null, it calls the runnable's {@linkplain 
   * ExecProcess#setProcessLogger(bubblewrap.core.threads.ExecProcessLogger) 
   * setProcessLogger} method.
   * @param pLogger the logger reference or null to clear the logger reference
   */
  public void setProcessLog(ExecProcessLogger pLogger) {
    if (this.execProc != null) {
      this.execProc.setProcessLogger(pLogger);
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage the Thread Reference">
  /**
   * Set of clear the reference to the Thread  under which this Task is running.
   * @param pThread the thread reference or null to clear the reference.
   */
  public void setThread(Thread pThread) {
    this.execThread = pThread;
  }
  
  /**
   * Get the Thread under which this Task is running.
   * @return the assign thread (only accessible during execution)
   */
  public Thread getThread() {
    return this.execThread;
  }
  
  /**
   * Get a casted reference of the OuterTask
   * @param <TFuture> extends RunnableFuture<V>
   * @return the assigned OuterTask
   */
  @SuppressWarnings("unchecked")
  public final <TFuture extends RunnableFuture<TResult>> TFuture getOuterTask() {
    TFuture pResult = (TFuture) this.outerTask;
    return pResult; 
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="ExecutionException Manager.">
  /**
   * A method called by the Scheduled Future Task's ExecProcess.{@linkplain
   * ExecProcess#onExecutionFailed(java.lang.String, java.lang.Throwable)
   * onExecutionFailed} method report the Execution Error to the Wrapper. This execution
   * exception information is used by {@linkplain ThreadQueue} to report the error and
   * to determine the cause for stopping the ExecProcess.
   * @param pExecExp a ExecutionException with the cause of the failure
   */
  public final void setExecutionError(ExecutionException pExecExp) {
    this.executeError = pExecExp;
  }
  
  /**
   * Get the ExecutionException assigned by the ExecProcess.{@linkplain
   * ExecProcess#onExecutionFailed(java.lang.String, java.lang.Throwable)
   * onExecutionFailed} method. Typically called by {@linkplain
   * ThreadQueue#onAfterExecute(bubblewrap.core.interfaces.IExecProcess, java.lang.Throwable)
   * ThreadQueue.onAfterExecute} to determine whether the execution of the process was
   * successful.
   * @return the currently assigned ExecutionException
   */
  public final ExecutionException getExecutionError() {
    return this.executeError;
  }
  
  /**
   * Check whether the Wrapped ExecProcess stop due to a Execution Error.
   * @return true if an ExecutionException is assigned.
   */
  public final boolean hasExectionError() {
    return (this.executeError != null);
  } 
  //</editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="Implementing IExecProcess">
  /**
   * {@inheritDoc} <p>OVERRIDE: Get the Runnable's ProcessId (or null if unassigned)</p>
   */
  @Override
  public String getProcessId() {
    return (this.execProc == null)? null: this.execProc.getProcessId();
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Get the Runnable's ProcessName (or null if unassigned)
   * </p>
   */
  @Override
  public String getProcessName() {
    return (this.execProc == null)? null: this.execProc.getProcessName();
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: return Runnable's isProcessId result (or false if 
   * unassigned)</p>
   */
  @Override
  public boolean isProcessId(String sProcessId) {
    return (this.execProc == null)? false: this.execProc.isProcessId(sProcessId);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements RunnableScheduledFuture">  
  /**
   * {@inheritDoc} <p>OVERRIDE: Call this.outerTask.run or skip if the wrapped 
   * ExecProcess or the OuterTask is unassigned. It assign itself to the ExecProcess 
   * before calling this.outerTask.run and remove its reference from ExecProcess after
   * the run is completed or terminated.</p>
   * <p>Once executed, it checks the ExecProcess's isDone and delay. If !isDone and the
   * delay=0, it calls {@linkplain ExecProcess#updateSchedule(boolean) 
   * ExecProcess.updateSchedule} to reschedule that task. It rechecks the isDone and
   * delay and if (!isDone) and (delay=0), it calls {@linkplain 
   * ExecProcess#stopSchedule() ExecProcess.stopSchedule} to prevent the process from
   * being scheduled in an infinite loop.</p>
   */
  @Override
  public void run() {
    if ((this.execProc == null) || (this.outerTask == null)) {
      return;
    }
    
    try {
      this.execProc.setFutureTask(this);
      if (this.outerTask != null) {
        this.outerTask.run();
      }
    } finally {
      this.execProc.setFutureTask(null);
    }
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return this.outerTask.cancel or false if unassigned
   * </p>
   */
  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return (this.outerTask == null)? false:
            this.outerTask.cancel(mayInterruptIfRunning);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return this.outerTask.isCancelled or true if unassigned
   * </p>
   */
  @Override
  public boolean isCancelled() {
    return (this.outerTask == null)? true: this.outerTask.isCancelled();
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return this.outerTask.isDone or true if unassigned
   * </p>
   */
  @Override
  public boolean isDone() {
    return (this.outerTask == null)? true: this.outerTask.isDone();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "ExecProcess[" + this.msProcessName + "]"</p>
   */
  @Override
  public String toString() {
    return "Process[" + this.getProcessName() + "]";
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Validate that (obj instanceof IExecProcess) and
   * (obj.processId=this.processId) (case insensitive)</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof IExecProcess));
    if (result) {
      IExecProcess pProcess = (IExecProcess) obj;
      result = DataEntry.isEq(this.getProcessId(), pProcess.getProcessId(), true);
    }
    return result;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return a HashCode using the ProcessId</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 19 * hash + Objects.hashCode(this.getProcessId());
    return hash;
  }
  //</editor-fold>
}
