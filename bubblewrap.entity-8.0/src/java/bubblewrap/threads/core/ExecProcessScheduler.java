package bubblewrap.threads.core;

import java.io.Serializable;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import bubblewrap.threads.interfaces.IExecProcess;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.threads.enums.ThreadExecStatus;

/**
 * <p>This is the base (abstract) class for providing a ExecProcessScheduler service to
 * execute multiple {@linkplain ExecProcess ExecProcesses} on separate threads.</p>
 * <p>All Thread related activities are track in a {@linkplain ExecProcessLogger}, which
 * maintain the ProcessLog for each executing ExecProcess. Once the process' execution
 * is completed, the log is closed and moved to the Archived Logs.  The process logger
 * can be configured to dump the log to the Server Log or a File by assigning a
 * {@linkplain ExecProcessLogHandler}.</p>
 * <p><b>NOTE:</b> The Process Logger functionality is supported by the based class.</p>
 * @see ExecProcessAsyncScheduler
 * @see ExecProcessSyncScheduler
 * @author kprins
 */
public abstract class ExecProcessScheduler implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
                              = Logger.getLogger(ExecProcessScheduler.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Class[ThreadQueueExecuter]">
  /**
   * <p>A custom ScheduledThreadPoolExecutor for executing the ExecProcesses 
   * asynchronized based on the assigned {@linkplain ActionSchedule}. It overrides the
   * following methods:</p><ul>
   *  <li>{@linkplain #decorateTask(java.lang.Runnable, 
   *    java.util.concurrent.RunnableScheduledFuture)}: to wrap the 
   *    RunnableScheduledFuture in a ExecProcessScheduledFuture.</li>
   *  <li>{@linkplain #beforeExecute(java.lang.Thread, java.lang.Runnable)}</li>
   *  <li>{@linkplain #afterExecute(java.lang.Runnable, java.lang.Throwable)}</li>
   * </ul>
   * <p><b>NOTE:</b> This Executer only handles ExecProcess Runnables - no Callables.
   * </p>
   */
  private class ExecProcessLoggerExecutor extends ScheduledThreadPoolExecutor {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Placeholder of the Active Thread (only on can run)
     */
    private Thread activeThread;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ExecProcessLoggerExecutor() {
      super(1);
      this.setMaximumPoolSize(1);
      this.activeThread = null;
    }
    // </editor-fold>
    
    /**
     * Check is the Executer is running and not in the process of being terminated
     * @return true if the activeThread is assigned
     */
    public boolean isExecuting() {
      return ((this.activeThread != null) 
              && ((!this.isTerminating()) || (this.isTerminated())));
    }
    
    //<editor-fold defaultstate="collapsed" desc="Override ScheduledThreadPoolExecutor">
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it wraps the 
     * RunnableScheduledFuture in a {@linkplain ExecProcessScheduledFuture} instance.
     * </p>
     */
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable pRunnable,
    RunnableScheduledFuture<V> pTask) {
      if (pRunnable == null) {
        throw new NullPointerException("The Runnable cannot be unassigned.");
      } else if (pRunnable instanceof ExecProcess) {
        ExecProcess pProcess = (ExecProcess) pRunnable;
        V pValue = null;
        return new ExecProcessScheduledFuture<>(pProcess, pTask, pValue);
      } else {
        return pTask;
      }
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it assign pThread
     * as the ExecProcess' Thread and Call the Scheduler's {@linkplain 
     * ExecProcessAsyncScheduler#onBeforeExecute(
     * bubblewrap.core.threads.ExecProcessScheduledFuture) onBeforeExecute} method.</p>
     */
    @Override
    protected void beforeExecute(Thread pThread, Runnable pRunnable) {
      super.beforeExecute(pThread, pRunnable);
      if ((pRunnable != null) && (pRunnable instanceof ExecProcessScheduledFuture<?>)) {
        ExecProcessScheduledFuture<?> pTask = (ExecProcessScheduledFuture<?>) pRunnable;
        pTask.setThread(pThread);
      }
      this.activeThread = pThread;
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it handle this 
     * event as follows: </p><ul>
     *   <li>It cast pRunnable as a ExecProcessScheduledFuture (pTask)</li>
     *   <li>If (pExp=null), it checks if (pTask.hasExectionError) and if true set 
     *      pExp=pTask.exectionError. Else if pTask.thread.isInterrupted, set pExp = 
     *      new InterruptedException(..).</li>
     *  <li>Clear pTask's Thread Reference.</li>
     *  <li>Call Scheduler's {@linkplain ExecProcessAsyncScheduler#onAfterExecute(
     *    bubblewrap.core.threads.ExecProcessScheduledFuture, java.lang.Throwable) 
     *    onAfterExecute} method.</li>
     *  <li>Call Scheduler's {@linkplain ExecProcessAsyncScheduler#onRescheduleTask(
     *    bubblewrap.core.threads.ExecProcessScheduledFuture) onRescheduleTask} method to
     *    reschedule the task if it is a periodic task.</li>
     * </ul>
     * <p><b>NOTE:</b> If the Scheduler is not accessible (which should never be that 
     * case), the exception is send to the Server log. Otherwise, it is passed to the
     * Scheduler to handle.</p>
     */
    @Override
    protected void afterExecute(Runnable pRunnable, Throwable pThrowable) {
      super.afterExecute(pRunnable, pThrowable);
      ExecProcessScheduledFuture<?> pTask = null;
      if ((pRunnable != null) && (pRunnable instanceof ExecProcessScheduledFuture<?>)) {
        pTask = (ExecProcessScheduledFuture<?>) pRunnable;
      }
      
      /**
       * Attempt get get the cause of the Execution Termination if an exception was
       * thrown.
       */
      if ((pThrowable == null) && (pTask != null)) {
        if (pTask.hasExectionError()) {
          pThrowable = pTask.getExecutionError();
        } else {
          Thread pThread = pTask.getThread();
          if ((pThread != null) && (pThread.isInterrupted())) {
            pThrowable =
                      new InterruptedException("The execution process was cancelled.");
          }
        }
      }
      
      if (pThrowable != null)  {
        logger.log(Level.WARNING, "{0}.Exection Error:\n{1}", 
                new Object[]{this.getClass().getSimpleName(), pThrowable.getMessage()});
      }
      
      /** Clear the Task's Thread Reference and call this.Queue.onAfterExecute **/
      if (pTask != null) {
        pTask.setThread(null);        
      }
      this.activeThread = null;
      
      /** Start Rescheduling Process **/
      /** Ignore Rescheduling if isTerminating or isTerminated **/
      ExecProcess pProcess = (pTask == null)? null: pTask.getRunnable();
      if ((pProcess == null) || (this.isTerminating())|| (this.isTerminated())) {
        return; 
      }
    
      /** Reschedule Process if not isDone**/
      try {
        if ((!pProcess.isDone()) && (pProcess.getDelay(TimeUnit.NANOSECONDS) == 0)) {
          pProcess.updateSchedule(false);
        }

        if (!pProcess.isDone()) { 
          long lDelay = pProcess.getDelay(TimeUnit.MILLISECONDS);
          this.schedule(pProcess, lDelay, TimeUnit.MILLISECONDS);
        }
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.rescheduleLogger Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A Ongoing Log of Thread Execution Activities.
   */
  private ExecProcessLogger processLogger;
  /**
   * The Executor for running logging schedules
   */
  private ExecProcessLoggerExecutor logExecutor;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessScheduler() {
    super();            
    this.processLogger = new ExecProcessLogger();
    this.logExecutor = null;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Call the super method and dump the log is not empty.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.stopLogExecutor();
    this.processLogger = null;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Managing the ProcessLog">
  /**
   * <p>Set the ExecProcessLogger - see {@linkplain
   * ExecProcessLogger#setLogger(java.lang.Integer, java.lang.Integer,
   * bubblewrap.core.threads.ExecProcessLogHandler) ExecProcessLogger.setLogger}.</p>
   * <p>If the ExecProcessLogger is assigned and isPeriodic, it will initiate the
   * ExecProcessLoggerExecutor and schedule the first Log handling.</p>
   * @param iMaxProcesses the maximum number of process to log (No limit is null or
   * less or equal to zero)
   * @param iMaxAge the maximum age of entries to keep in the Active Log (No limit is
   * null or less or equal to zero)
   * @param logHandler to process the log output to the server or a file (optional)
   */
  public final void setLogger(Integer iMaxProcesses, Integer iMaxAge,
          ExecProcessLogHandler logHandler) {
    this.processLogger.setLogger(iMaxProcesses, iMaxAge, logHandler);
  }
  
  /**
   * Called by the startExecutor method to start the ProcessLogger Output Scheduler.
   * The Scheduler will only be initiated if not already initiated, and the 
   * ProcessLogger has an assigned Log Output Handler with a periodic schedule.
   */
  protected void startLogExector() {
    if ((this.processLogger != null) && (this.logExecutor == null)) {
      ExecProcessLogHandler pHandler = this.processLogger.getLogOutputHandler();
      if ((pHandler != null) && (pHandler.isPeriodic())) {
        this.logExecutor = new ExecProcessLoggerExecutor();
        long lDelay = pHandler.getDelay(TimeUnit.MILLISECONDS);
        this.logExecutor.schedule(pHandler, lDelay, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  /**
   * <p>Call to stop the ProcessLogger Output Scheduler - If not started the call will 
   * be ignored and it will call this.dumpProcessLog to output the log. </p>
   * <p>Otherwise, if isExcuting - it will shutdown allowing the current log
   * output to complete. If not executing, it calls the Executors shutdownNow to
   * shutdown and ignore any scheduled output requests. It will call this.dumpProcessLog
   * to force a log output. it set this.mpLogExecutor = null</p>
   * <p>Finally, it will clear the ProcessLogger's content.</p>
   */
  protected void stopLogExecutor() {
    if (this.logExecutor != null) {
      boolean dumpLog = false;
      if (this.logExecutor.isExecuting()) {
        this.logExecutor.shutdown();
      } else {
        this.logExecutor.shutdownNow();
        dumpLog = true;
      }
      
      /* Wait for the Logger to shut down */
      while (this.logExecutor.isTerminating()) {
        try {
          TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException iExp) {
        }
      }
      
      /** For Testing **/
      logger.log(Level.INFO, "{0}.stopLogExecutor: LogExecuter was terminated @ {1}.",
                new Object[]{this.getClass().getSimpleName(), DateTime.getNow(null)});
      
      if (dumpLog) {
        this.dumpProcessLog();
      }
      this.logExecutor.purge();
      this.logExecutor = null;
    } else {
      this.dumpProcessLog();
    }
    
    if (this.processLogger != null) {
      this.processLogger.clearLog();
    }
  }
  
  /**
   * Get a reference to the Queue's Process Logger
   * @return this.mpThreadLog
   */
  public ExecProcessLogger getProcessLogger() {
    return this.processLogger;
  }
  
  /**
   * Call to dump the Processing Log. If the Processor Log has an assigned
   * LogOutputHandler, the LogOutputHandler will be run to process the request.
   * Otherwise, it calls the ProcessLogger's {@linkplain ExecProcessLogger#dumpLog()
   * dumpLog} method to handle the request.
   */
  public void dumpProcessLog() {
    if (this.processLogger != null) {
      ExecProcessLogHandler handler = this.processLogger.getLogOutputHandler();
      if (handler != null) {
        if ((this.logExecutor == null) || (!this.logExecutor.isExecuting())) {
          handler.run();
        }
      } else {
        this.processLogger.dumpLog();
      
        /* Set  to sleep to allow output of Log */
        try {
          TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException ex) {}
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract ExecProcessScheduler Methods">
  /**
   * <p>MUST OVERRIDE: Called to start the Executor. The base method starts the 
   * ProcessLogger's Output Scheduler to schedule the ProcessLogger's output.</p>
   * <p><b>NOTE:</b> Inheritors MUST call the super method AFTER initiating the
   * Scheduler's Executor</p>
   */
  public void startExecutor() {
    this.startLogExector();
  }
  
  /**
   * <p>MUST OVERRIDE: Called to shutdown the Executor. If (bNow), it should force a
   * shutdown without execution queued tasks. The base method stops the ProcessLogger's
   * Output Scheduler</p>
   * <p><b>NOTE:</b> Inheritors MUST call the super method AFTER terminating the
   * Scheduler's Executor.</p>
   * @param stopNow true to terminate Executer and leaving queued tasks un-executed.
   */
  public void stopExecutor(boolean stopNow) {
    this.stopLogExecutor();
  }
  
  /**
   * ABSTRACT: Get whether the Scheduler's Executor is available
   * @return true if the executed has been started and has not been shutdown.
   */
  public abstract boolean isExecuting();

  /**
   * ABSTRACT: Called to queue and execute a ExecProcess.
   * @param pProcess the ExecProcess to execute
   * @return true is the process is successfully queued and false if the process failed.
   */
  public abstract boolean executeProcess(ExecProcess pProcess);
  
  /**
   * ABSTRACT: Get the Execution State {@linkplain ThreadExecutionStates}) of the
   * Process.
   * @param process the process of interest
   * @return NOTSTARTED if queued; EXECUTING if executing; otherwise COMPLETED .
   */
  public abstract ThreadExecStatus getExecProcessStatus(IExecProcess process);
  
  /**
   * ABSTRACT: Get the Number of Processes queued for execution
   * @return the Queued Processes count
   */
  public abstract int getQueuedCount();
  
  /**
   * Get the Number of Threads executing concurrently
   * @return the Executing Thread count
   */
  public abstract int getExecutingCount();
  //</editor-fold>
}
