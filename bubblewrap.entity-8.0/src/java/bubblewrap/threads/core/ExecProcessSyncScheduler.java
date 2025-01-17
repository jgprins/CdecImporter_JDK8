package bubblewrap.threads.core;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.reflection.ReflectionInfo;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import bubblewrap.threads.interfaces.IExecProcess;
import bubblewrap.threads.enums.ThreadExecStatus;
import bubblewrap.io.datetime.DateTime;

/**
 * <p>This implementation of ExecProcessScheduler provides a synchronized execution of
 * {@linkplain ExecProcess ExecProcesses} using only one execution thread and a
 * {@linkplain BlockingQueue} to manage to sequential execution of queued processes. 
 * </p>
 * <p>The Scheduler's {@link ExecProcessSyncExecutor} extends {@link ThreadPoolExecutor}
 * and allow the assignment of a custom BlockingQueue for handling the queued tasks.
 * If unassigned, it will use a {@linkplain LinkedBlockingQueue} - A FIFO queue.</p>
 * <p>The ExecProcessSyncScheduler does not "re-schedule" ExcProcesses, even if the 
 * process is setup with a periodic run schedule. Each queued ExecProcess is run only 
 * once.</p>
 * <p>The ExecProcessSyncScheduler supports a Post Execute Tasks, an ExecProcess that
 * can be run repeatedly as scheduled by the designated{@linkplain 
 * PostExecuteEventHandler}. The execution of the Post Execute Tasks will be 
 * synchronized with the queued ExecProcesses to prevent concurrency lock downs.</p>
 * @see ExecProcessAsyncScheduler
 * @author kprins
 */
public class ExecProcessSyncScheduler extends ExecProcessScheduler {

  //<editor-fold defaultstate="collapsed" desc="Class[ExecProcessSyncExecutor]">
  /**
   * <p>Private ThreadPoolExecutor class to be used as the Scheduler's ExecProcess
   * Executor service. It allows a assignment of a custom {@linkplain BlockingQueue}
   * for handling the ExecProcesss that are waiting execution.</p>
   * <p>It uses the {@linkplain SyncSchedulerThreadFactory} to initiate {@linkplain 
   * SyncSchedulerThread} instance for the Executor.</p>
   * <p><b>NOTE:</b> This class will never handles Callable. Only Runnable of type
   * {@linkplain ExecProcess}.</p>
   */
  private class ExecProcessSyncExecutor extends ThreadPoolExecutor {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessSyncScheduler scheduler;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ExecProcessSyncExecutor(ExecProcessSyncScheduler scheduler,
            BlockingQueue<Runnable> queue) {
      super(1, 1, 10, TimeUnit.MINUTES, queue, 
                                      new SyncSchedulerThreadFactory(scheduler));
      this.scheduler = scheduler;
    }
    // </editor-fold>
    
    /**
     * {@inheritDoc} <p>OVERRIDE: Call the super method to initiates the RunnableFuture
     * instance. Then if (runnable instanceof ExecProcess), wrap the RunnableFuture in
     * a ExecProcessFuture instance and return the ExecProcessFuture. Else return the
     * RunnableFuture</p>
     */
    @Override
    protected <TResult> RunnableFuture<TResult> newTaskFor(Runnable runnable, 
                                                                          TResult result) {
      RunnableFuture<TResult> future = super.newTaskFor(runnable, result);
      if ((runnable != null) && (runnable instanceof ExecProcess)) {
        ExecProcess pExecProc = (ExecProcess) runnable;
        return new ExecProcessFuture<>(pExecProc, future, result);
      } else {
        return future;
      }
    }
    
    @Override
    protected void beforeExecute(Thread thread, Runnable runnable) {
      super.beforeExecute(thread, runnable);
      if ((runnable != null) && (runnable instanceof ExecProcessFuture<?>)) {
        ExecProcessFuture<?> task = (ExecProcessFuture<?>) runnable;
        task.setThread(thread);
        this.scheduler.onBeforeExecute(task);
      }
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it handle this
     * event as follows: </p><ul>
     *   <li>It cast runnable as a ExecProcessScheduledFuture (execTask)</li>
     *   <li>If (exp=null), it checks if (execTask.hasExectionError) and if true set
     *      exp=execTask.exectionError. Else if execTask.thread.isInterrupted, set exp =
     *      new InterruptedException(..).</li>
     *  <li>Clear execTask's Thread Reference.</li>
     *  <li>Call Scheduler's {@linkplain ExecProcessSyncScheduler#onAfterExecute(
     *    bubblewrap.core.threads.ExecProcessFuture, java.lang.Throwable)
     *    onAfterExecute} method.</li>
     *  <li>Call Scheduler's {@linkplain ExecProcessSyncScheduler#onPostExecute()
     *    onPostExecute} method to handle the PosteExecute Event.</li>
     * </ul>
     * <p><b>NOTE:</b> If the Scheduler is not accessible (which should never be that
     * case), the exception is send to the Server log. Otherwise, it is passed to the
     * Scheduler to handle.</p>
     */
    @Override
    protected void afterExecute(Runnable runnable, Throwable throwable) {
      super.afterExecute(runnable, throwable);
      ExecProcessFuture<?> task = null;
      if ((runnable != null) && (runnable instanceof ExecProcessFuture<?>)) {
        task = (ExecProcessFuture<?>) runnable;
      }
      
      /**
       * Attempt get get the cause of the Execution Termination if an exception was
       * thrown.
       */
      if ((throwable == null) && (task != null)) {
        if (task.hasExectionError()) {
          throwable = task.getExecutionError();
        } else {
          Thread pThread = task.getThread();
          if ((pThread != null) && (pThread.isInterrupted())) {
            throwable = 
                      new InterruptedException("The execution process was cancelled.");
          }
        }
      }
      
      if ((throwable != null) && (this.scheduler == null)) {
        logger.log(Level.WARNING, throwable.getMessage());
      }
      
      /** Clear the Task's Thread Reference and call this.mpScheduler.onAfterExecute
       * followed by this.mpScheduler.onPostExecute **/
      if (task != null) {
        task.setThread(null);
        if (this.scheduler != null) {
          this.scheduler.onAfterExecute(task, throwable);
          this.scheduler.onPostExecute();
        }
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Class[SyncSchedulerThreadFactory]">
  /**
   * The ThreadFactory for the return SyncSchedulerThread threads
   */
  protected class SyncSchedulerThreadFactory implements ThreadFactory {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessSyncScheduler mpScheduler;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public SyncSchedulerThreadFactory(ExecProcessSyncScheduler pScheduler) {
      super();
      this.mpScheduler = pScheduler;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Implement ThreadFactory">
    /**
     * {@inheritDoc} <p>IMPLEMENT: Return  new SyncSchedulerThread instance</p>
     */
    @Override
    public Thread newThread(Runnable runnable) {
      return new SyncSchedulerThread(this.mpScheduler, runnable);
      //</editor-fold>
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Class[SyncSchedulerThread]">
  /**
   * The SyncSchedulerThread for running the Synchronized ExecProcess. It checks if
   * the Scheduler's PostExecute Thread is running and join it before executing its
   * Runnable.
   */
  protected class SyncSchedulerThread extends Thread {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessSyncScheduler scheduler;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    protected SyncSchedulerThread(ExecProcessSyncScheduler scheduler,
            Runnable runnable) {
      super(runnable);
      this.scheduler = scheduler;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Thread Overrides">
    /**
     * {@inheritDoc} <p>OVERRIDE: Check if the Scheduler's PostExecute Thread is
     * assigned and not interrupted</p>
     */
    @Override
    public void run() {
      try {
        logger.log(Level.INFO, "SyncScheduler Thread Running....");
        super.run();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.run Error:\n {1}",
                new Object[]{"SyncScheduler Thread", exp.getMessage()});
      } finally {
        logger.log(Level.INFO, "SyncScheduler Thread Stopped");
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Class[PostExecuteThread]">
  /**
   * The PostExecuteThread for running the Post Execute Task. It checks if
   * the Scheduler's has an Executing Task and whether this task's Thread is running 
   * and join it before executing its Runnable.
   */
  protected class PostExecuteThread extends Thread {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessSyncScheduler scheduler;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    protected PostExecuteThread(ExecProcessSyncScheduler scheduler,
            PostExecProcess runnable) {
      super(runnable);
      this.scheduler = scheduler;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Thread Overrides">
    /**
     * {@inheritDoc} <p>OVERRIDE: Check if the Scheduler's PostExecute Thread is
     * assigned and not interrupted</p>
     */
    @Override
    public void run() {
      try {
        logger.log(Level.INFO, "SyncScheduler.PostExceute Thread Running....");
        super.run();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.run Error:\n {1}",
                new Object[]{"SyncScheduler.PostExceute", exp.getMessage()});
      }  finally {
        logger.log(Level.INFO, "SyncScheduler.PostExceute Thread Stopped");
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Class[PostExecProcess]">
  /**
   * A ExecProcessFuture Wrapper for the custom assigned Post Execute Task
   */
  private class PostExecProcess<V> extends ExecProcessFuture<V> {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessSyncScheduler scheduler;
    /**
     * Placeholder for the Custom Assigned Runnable;
     */
    private ExecProcess runnable;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public PostExecProcess(ExecProcessSyncScheduler pScheduler,
            ExecProcess runnable, V Result ) {
      super(runnable, new FutureTask<>(runnable, Result), Result);
      this.scheduler = pScheduler;
      this.runnable = runnable;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Override ExecProcessFuture">    
    @Override
    public void run() {
      ExecProcessLogger procLogger = null;
      String logMsg = null;
      try {
        if ((this.scheduler != null) && (this.runnable != null)) {
          boolean bDoLog = 
                      (this.runnable != null)? this.runnable.doProcessLog(): null;
          procLogger = (bDoLog)? this.scheduler.getProcessLogger(): null;
        }
        
        if (procLogger != null) {
          procLogger.log(this.runnable, Level.INFO, "Post Exec Task Started.");
        }          
        
        super.run();
        if (this.hasExectionError()) {
          Exception pExecErr = this.getExecutionError();
          if (pExecErr == null) {
            throw new Exception("Unknown Excution Error.");
          } else {
            throw pExecErr;
          }
        } 
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.method Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        Throwable expCause = (exp == null)? null: exp.getCause();
        logMsg = "Post Exec Task Error: \n\t" + exp.getMessage();
        if (expCause != null) {
          logMsg += "\n\t" + expCause.getMessage();
        }
      } catch (Throwable throwable) {
        logMsg = "Post Exec Task Error: \n\t" + throwable.getMessage();
      } finally {
        logMsg = (logMsg != null)? logMsg: "Post Exec Task Completed.";
        if (procLogger != null) {
          procLogger.log(this.runnable, Level.INFO, logMsg);
          procLogger.clearLog(this.runnable);
        } 
      }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Implement IExecProcess">
    /**
     * {@inheritDoc} <p>OVERRIDE: A Delegate for this.runnable.getPorcessId</p>
     */
    @Override
    public String getProcessId() {
      return (this.runnable == null)? null: this.runnable.getProcessId();
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: A Delegate for this.runnable.getProcessName</p>
     */
    @Override
    public String getProcessName() {
      return (this.runnable == null)? null: this.runnable.getProcessName();
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: A Delegate for this.runnable.isProcessId</p>
     */
    @Override
    public boolean isProcessId(String sProcessId) {
      return (this.runnable == null)? false: this.runnable.isProcessId(sProcessId);
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * The EventHandler that fires the Execution Stopped Event.
   */
  public final EventHandler ExecutionStopped;

  /**
   * Called to fire the Execution Stopped Event.
   */
  protected void fireExecutionStopped() {
    this.ExecutionStopped.fireEvent(this, new EventArgs());
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the BlockingQueue class to be used with the {@linkplain
   * ExecProcessSyncExecutor}
   */
  private Class<? extends BlockingQueue<Runnable>> queueClass;
  /**
   * Placeholder for the Scheduler's Executor
   */
  private ExecProcessSyncExecutor executor;
  /**
   * Placeholder for the Executing task.
   */
  private ExecProcessFuture<?> executingTask;
  /**
   * The assigned PostExecuteEventHandler that controlled the firing of the 
   * PostExecute Task event.
   */
  private PostExecuteEventHandler postExecHandler;
  /**
   * Placeholder for the ExecProcess to executed the PostExecuteEvent is fired.
   */
  private PostExecProcess<?> postExecTask;
  /**
   * Placeholder for the Thread Executing the Post Execute Task.
   */
  private PostExecuteThread postExecThread;
  /**
   * The Total number of Processes added for execution
   */
  private Integer processCount;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - using the default BlockingQueue class {@linkplain 
   * LinkedBlockingQueue}.
   */
  public ExecProcessSyncScheduler() {
    this(null);    
  }
  
  /**
   * Public Constructor - with a defined BlockingQueue class to assign to the
   * Executor.
   * <p><b>NOTE:</b> Call the {@linkplain ReflectionInfo#castAsGenericClass(
   * java.lang.Class) ReflectionInfo.castAsGenericClass} to cast the generic 
   * BlockingQueue classes as Class<? extends BlockingQueue<Runnable>></p>
   * @param queueClass the specified BlockingQueue or null to use the default 
   * {@linkplain LinkedBlockingQueue} class.
   */
  protected ExecProcessSyncScheduler(Class<? extends BlockingQueue<Runnable>> 
          queueClass){
    super();
    this.ExecutionStopped = new EventHandler();
    this.queueClass = queueClass;
    this.executor = null;
    this.executingTask = null;
    this.postExecHandler = null;
    this.postExecTask = null;
    this.postExecThread = null;
    this.processCount = 0;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: if this.isExceuting, call this.stopExecution(bNoew=true)
   * before disposing the Executor and calling the super method.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    if (this.isExecuting()) {
      this.stopExecutor(true);
    }
    this.ExecutionStopped.clear();
    this.executor = null;
    this.executingTask = null;
    this.postExecHandler = null;
    this.postExecTask = null;
    this.postExecThread = null;
    super.finalize();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected EventHandlers">
  /**
   * Handles the BeforeExecute Event. It checks if a previous ExceProcess is running and
   * if true, it joins the process' thread until the run ends. It assigns execTask as the
   * this.mpExecutingTask and if the task requires logging, add a "Start Execution"
   * log entry.
   * @param execTask the Task that is about to execute.
   */
  protected synchronized void onBeforeExecute(ExecProcessFuture<?> execTask) {
    if (execTask == null) {
      return;
    }
    Thread execThread = null;
    execThread = 
                (this.executingTask == null)? null: this.executingTask.getThread();
    if ((execThread != null) && (execThread.getState() !=  Thread.State.TERMINATED)) {
      try {
        execThread.join();
      } catch (InterruptedException ex) {}
    }
    
    this.executingTask = execTask;      
    if (execTask.doProcessLog()) {
      ExecProcessLogger procLog = this.getProcessLogger();
      if (procLog != null) {
        execTask.setProcessLog(procLog);
        procLog.log(execTask, Level.INFO, "Start Execution.");
      }
    }
  }
  
  /**
   * EventHandler called by the Executor when the execution of the Process has been 
   * completed - either successfully or not. It logs a message, which include the 
   * exception message if exp!=null, and close the Log for the process. Finally, it
   * sets this.mpExecutingTask=null.
   * @param execTask any iExecProcess reference
   * @param throwable a throwable or null is the process was successfully completed.
   */
  protected synchronized void onAfterExecute(ExecProcessFuture<?> execTask,
          Throwable throwable) {
    try {
      /** Clear execTask's Logger */
      if (execTask != null) {
        execTask.setProcessLog(null);
      }

      String errMsg = null;
      if (throwable != null) {
        errMsg = throwable.getMessage();
        Throwable errCause = 
             (throwable instanceof ExecutionException)? throwable.getCause(): null;
        if (errCause != null) {
          errMsg += ";\n\t" + errCause.getMessage();
        }
      }
      
      ExecProcessLogger procLogger = this.getProcessLogger();        
      if ((procLogger != null) && (execTask != null) && (execTask.doProcessLog())) {
        if (errMsg != null) {          
          procLogger.log(execTask, Level.WARNING, 
                                "Execution Stopped because: " + errMsg);
        } else {
          procLogger.log(execTask, Level.INFO, 
                                "Execution Successfully Completed."); 
        }
        procLogger.clearLog(execTask);
      } else if (errMsg != null) {
        String logMsg = (execTask == null)? "Process Execustion Stopped because:\n ": 
                "Process["+ execTask.getProcessName() + "] Stopped because:\n " ;
        logMsg += errMsg;
        logger.log(Level.WARNING, logMsg);
      } else if ((execTask != null) && (execTask.doProcessLog())) {
        String logMsg = (execTask == null)? "": "Process["+ execTask.getProcessName() 
                + "]: ";
        logMsg += "Execution Successfully Completed.";
        logger.log(Level.INFO, logMsg);
      }
    } catch (Exception pOuterExp) {
      logger.log(Level.WARNING, "{0}.onAfterExecute Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pOuterExp.getMessage()});
    } finally {
      this.executingTask = null;
    }
  }
  
  /**
   * <p>Called to execute Post Execute Process if assigned and due. This process is
   * called directly after handling the {@linkplain #onAfterExecute(
   * bubblewrap.core.threads.ExecProcessFuture, java.lang.Throwable) onAfterExecute} event.
   * </p>
   * <p>This process is skipped if:</p><ul>
   * <li>(!this.isExecutingTask) - a Executer has been stop or not started.</li>
   * <li>(this.mpExecutingTask != null) - a ExecProcess is running (should not be)</li>
   * <li>(this.mpPostExecTask == null) - no PostExecute Task is assigned</li>
   * <li>((this.mpPostExecThread != null) && (this.mpPostExecThread.getState() != 
   * Thread.State.TERMINATED))) - The PosetExecThread is already running. 
   * (should not be)</li>
   * <li>(!this.mpPostExecHandler.doEvent()) - the postExecute Event does not have to
   * run.</li>
   * </ul>
   * <p>Otherwise, it will clear this.mpPostExecThread, initiate a new {@linkplain 
   * PostExecuteThread} and start the thread. Finally, it calls the 
   * PostExecuteEventHandler's reset method.</p>
   */
  protected synchronized void onPostExecute() {
    this.postExecThread = ((this.postExecThread != null)
              && (this.postExecThread.getState() !=  Thread.State.TERMINATED))?
              this.postExecThread: null;
    try {
      if ((this.postExecThread == null) &&
          (this.postExecHandler != null) && (this.postExecHandler.doEvent()) &&
          (this.isExecuting()) && (this.postExecTask != null) &&
          (this.executingTask != null)) {

        this.postExecThread = new PostExecuteThread(this, this.postExecTask);
        this.postExecThread.start();  
        this.postExecHandler.reset();
        try {
          this.postExecThread.join();
        } catch (InterruptedException innerExp) {
          logger.log(Level.SEVERE, "Post Execute Event: process was interrupted");
        } finally {
          this.postExecThread = null;
        }
      }
    } finally {
            
    }
  }
  //</editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="Public Configuration Methods">
  /**
   * Called to set the Scheduler's optional Post Execute Task and the Post Execute
   * Event handler to fire the Post Execute Events.
   * @param execTask the ExecProcess to run on an Post Execute Event (can be null).
   * @param postExecHandler the to triggering the Post Execute Event - ignored if
   * execTask=null
   * @exception NullPointerException if execTask is assigned and pPostExecHandler=null.
   */
  public final void setPostExecuteTask(ExecProcess execTask,
          PostExecuteEventHandler postExecHandler) {
    if ((execTask != null) && (postExecHandler == null)) {
      throw new NullPointerException("The Post Execution Event Handler is unassigned.");
    }
    Void result = null;
    this.postExecTask = (execTask == null)? null: 
                                      new PostExecProcess<>(this, execTask, result);
    
    this.postExecHandler = (this.postExecTask == null)? null: postExecHandler;
    if (this.postExecHandler != null) {
      this.postExecHandler.setScheduler(this);
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements ExecProcessScheduler">
  /**
   * {@inheritDoc} <p>IMPLEMENT: this process retrieves the assigned {@linkplain 
   * BlockingQueue} class or used  {@linkplain LinkedBlockingQueue} class if unassigned. 
   * It initiates a new instance of the BlockingQueue class and use this instance to 
   * initiate a new {@linkplain ExecProcessSyncExecutor} as this Scheduler's Executor.
   * It the calls {@linkplain ExecProcessScheduler#startExecutor() super.startExecutor}
   * to start the ProcessLogger.
   * </p>
   * <p>This process is skipped is the Executor is already assigned and {@linkplain 
   * #isExecuting() isExecuting}.</p>
   * @exception IllegalArgumentException if the initiation of the BlockingQueue fails.
   */
  @Override
  @SuppressWarnings("unchecked")
  public void startExecutor() {
    if ((this.executor == null) || (this.executor.isShutdown())) {
      this.executor = null;
      BlockingQueue<Runnable> queue = null;
      try {
        Class<? extends BlockingQueue<Runnable>> queueClass = this.queueClass;
        if (queueClass == null) {
          queueClass = ReflectionInfo.castAsGenericClass(LinkedBlockingQueue.class);
        }
        try {
          queue = queueClass.newInstance();
        } catch (InstantiationException | IllegalAccessException exp) {
          throw new ExecutionException("Initiation an instance of Class[" 
                  + queueClass.getName() + "] failed", exp);
        }
      } catch (Exception exp) {
        throw new IllegalArgumentException(this.getClass().getSimpleName()
                + ".startExecutor Error:\n " + exp.getMessage(), exp);
      }
      
      this.executor = new ExecProcessSyncExecutor(this, queue);
      super.startExecutor();
    }
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Called to stop the Executor. If (bNow), it calls the 
   * {@linkplain ThreadPoolExecutor#shutdownNow() shutdownNow} method and log all 
   * process that was not completed. Otherwise, it calls the {@linkplain 
   * ThreadPoolExecutor#shutdown() shutdown} method, which will wait for all queued 
   * processes to complete before it terminates. It waits until the thread has 
   * terminated and then, if a PostExecute Task is assigned, run this task. Finally, it
   * clears the Executing task reference, and call the super method to stop the
   * ProcessLog Output Scheduler.</p>
   */
  @Override
  public void stopExecutor(boolean doNow) {
    if (this.executor != null) {
      if ((this.executor.isTerminating()) || this.executor.isTerminated()) {
        return;
      }
      
      try {
        this.executor.purge();
        List<Runnable> queued = null;
        if (doNow) {
          queued = this.executor.shutdownNow();
        } else {
          this.executor.shutdown();
        }

        while (this.executor.isTerminating()) {
          try {
            TimeUnit.MILLISECONDS.sleep(100);
          } catch (InterruptedException inExp) {}
        }
        /** For Testing **/
        logger.log(Level.INFO, "\"{0}.stopExecutor: Executer was terminated @ {1}.",
                  new Object[]{this.getClass().getSimpleName(), DateTime.getNow(null)});
        
        if ((queued != null) && (!queued.isEmpty())) {
          for (Runnable runnable : queued) {
            logger.log(Level.INFO, "{0}.stopExecutor: {1} was not executed.",
                new Object[]{this.getClass().getSimpleName(), runnable.toString()});
          }
        }          
        this.executor.purge();
        
        if (this.postExecTask != null) {
          this.postExecTask.run();
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.stopExecutor Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      } finally {
        this.executor = null;
        this.executingTask = null;
        
        super.stopExecutor(doNow);
      }
    }
  }
  
  /**
   * {@inheritDoc } 
   * <p>IMPLEMENT: Get whether the ExecProcessSyncScheduler's Executor is available.</p>
   */
  @Override
  public boolean isExecuting() {
    boolean result = ((this.executor != null) && (!this.executor.isShutdown()));
    return result;
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: This process starts the execution of the specified 
   * ExecProcess as follows:</p><ul>
   *  <li>If (execProcess.doProcessLog), it add a LogEntry[Submit to Execution Queue.]</li>
   *  <li>It calls the {@linkplain ThreadPoolExecutor#submit(java.lang.Runnable) 
   *    Executor.submit} to queue and/or start the ExecProcess.run.</li>
   *  <li>If this process fails, send a log entry to the server and the ProcessLog</li>
   * </ul>
   * <p><b>NOTE:</b> This process fails and return false if:</p><ul>
   *  <li>The Executor has not been started (this.isExecuting=false)</li>
   *  <li>execProcess=null</li>
   *  <li>The Executor.submit method failed.</li>
   * </ul>
   */
  @Override
  public boolean executeProcess(ExecProcess execProcess) {
    boolean result = false;
    try {
      if (!this.isExecuting()) {
        throw new ExecutionException("The Scheduler's Executor has not been started",
                null);        
      }
      
      if (execProcess != null) {
        ExecProcessLogger procLogger = this.getProcessLogger();
        if ((execProcess.doProcessLog()) && (procLogger != null)) {
          procLogger.log(execProcess, Level.INFO, "Submit to Execution Queue.");
        }
        
        Future<?> execTask = this.executor.submit(execProcess);
        if (execTask == null) {
          if ((execProcess.doProcessLog()) && (procLogger != null)) {
            procLogger.log(execProcess, Level.INFO, "Submit for Execution Failed.");
          }
          throw new Exception("Submit for Execution Failed.");
        }
        
        this.processCount++;
        result = true;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.executeProcess Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Check if execProcess is queued for execution. It check the ThreadPool's Queue for a
   * Matching Process.
   * @param process IExecProcess of interest
   * @return true if it is a Queued process
   */
  protected synchronized boolean isQueued(IExecProcess process) {
    boolean result = false;
    String procKey = ExecProcess.getProcessKey(process);
    if ((procKey != null) && (this.executor != null)) {
      BlockingQueue<Runnable> queue = this.executor.getQueue();
      if ((queue != null) && (!queue.isEmpty())) {
        for (Runnable runnable : queue) {
          if ((runnable != null) && (runnable instanceof IExecProcess)) {
            IExecProcess queuedProcess = (IExecProcess) runnable;
            if (procKey.equalsIgnoreCase(queuedProcess.getProcessId())) {
              result = true;
              break;
            }
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Check if execProcess is executing. It checks the Executing task is assigned and if
   * the ProcessIds match..
   * @param execProcess IExecProcess of interest
   * @return true if it is an executing process
   */
  protected synchronized boolean isExecuting(IExecProcess execProcess) {
    boolean result = false;
    if (this.executingTask != null) {
      String procKey = ExecProcess.getProcessKey(execProcess);
      String sExecKey = ExecProcess.getProcessKey(this.executingTask);
      result = ((procKey != null) && (sExecKey != null) && (sExecKey.equals(procKey)));
    }
    return result;
  }  
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Return the state based on execProcess's {@linkplain 
   * #isExecuting(bubblewrap.core.interfaces.IExecProcess) isExecuting} or {@linkplain 
   * #isQueued(bubblewrap.core.interfaces.IExecProcess) isQueued} states.</p>
   */
  @Override
  public ThreadExecStatus getExecProcessStatus(IExecProcess process) {
  ThreadExecStatus result = ThreadExecStatus.COMPLETED;
    if (process == null) {
      if (this.isExecuting(process)) {
        result = ThreadExecStatus.EXECUTING;
      } else if  (this.isQueued(process)) {
        result = ThreadExecStatus.NOTSTARTED;
      }
    }
    return result;
  }
  
  /**
   * Get the total number of processes submitted for execution. Only meaningful if this
   * is a manual execution where every process is only executed once.
   * <p>
   * <b>NOTE:</b> The number is incremented every time {@linkplain 
   * #executeProcess(bubblewrap.threads.core.ExecProcess) this.executeProcess} 
   * successfully submitted a process for execution.</p>
   * @return this.processCount
   */
  public int getProcessCount() {
    return this.processCount;
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Return the QueuedProcesses list size</p>
   */
  @Override
  public int getQueuedCount() {
    int result = 0;
    try {
      if (this.executor != null) {
        BlockingQueue<Runnable> queue = this.executor.getQueue();
        result = (queue == null)? 0: queue.size();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getQueuedCount Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Return 0 or 1 depending whether a process is executing
   * </p>
   */
  @Override
  public int getExecutingCount() {
    return (this.executingTask == null)? 0: 1;
  }
  //</editor-fold>
}
