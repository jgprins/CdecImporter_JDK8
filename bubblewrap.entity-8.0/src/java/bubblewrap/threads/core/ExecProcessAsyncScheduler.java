package bubblewrap.threads.core;

import bubblewrap.threads.enums.ThreadExecStatus;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import bubblewrap.threads.interfaces.IExecProcess;

/**
 * <p>An ExecProcessScheduler that handles the asynchronized execution of multiple 
 * {@linkplain ExecProcess ExcProcesses} each on its own thread. It uses an private
 * {@linkplain ExecProcessAsyncExecutor} class to manage the thread queue, scheduling,
 * and execution of the processes.</p>
 * <p>Each ExecProcess has an assigned {@linkplain ActionSchedule} that defines it 
 * place in the Execution Queue. The ExecProcess can be scheduled for a immediate 
 * execution, a specified start time, a delayed start, or a execution schedule at a
 * periodic interval.</p>
 * 
 * <p><b>NOTE:</b> The ExecProcessAsyncScheduler allows the repeated execution of the 
 * same process. However, the process cannot be executed while a prior instance of the
 * process is executing. Its second execution thread will be delayed until the first has
 * been completed.</p>
 * @author kprins
 */
public class ExecProcessAsyncScheduler extends ExecProcessScheduler {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * The default maximum threads pool size. [{@value}]
   */
  protected static final int MaxPoolSize = 4;
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
  private class ExecProcessAsyncExecutor extends ScheduledThreadPoolExecutor {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Private reference to the Associated ExecProcessAsyncScheduler instance;
     */
    private ExecProcessAsyncScheduler scheduler;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ExecProcessAsyncExecutor(ExecProcessAsyncScheduler pScheduler, 
                                                                int iCorePoolSize) {
      super(iCorePoolSize);
      this.scheduler = pScheduler;
    }
    // </editor-fold>
        
    //<editor-fold defaultstate="collapsed" desc="Override ScheduledThreadPoolExecutor">
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it wraps the 
     * RunnableScheduledFuture in a {@linkplain ExecProcessScheduledFuture} instance.
     * </p>
     */
    @Override
    protected <V> RunnableScheduledFuture<V> decorateTask(Runnable pRunnable,
    RunnableScheduledFuture<V> execTask) {
      if (pRunnable == null) {
        throw new NullPointerException("The Runnable cannot be unassigned.");
      } else if (pRunnable instanceof ExecProcess) {
        ExecProcess execProcess = (ExecProcess) pRunnable;
        V pValue = null;
        return new ExecProcessScheduledFuture<>(execProcess, execTask, pValue);
      } else {
        return execTask;
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
        ExecProcessScheduledFuture<?> execTask = (ExecProcessScheduledFuture<?>) pRunnable;
        execTask.setThread(pThread);
        this.scheduler.onBeforeExecute(execTask);
      }
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: If the Runnable is an ExecProcess, it handle this 
     * event as follows: </p><ul>
     *   <li>It cast pRunnable as a ExecProcessScheduledFuture (execTask)</li>
     *   <li>If (pExp=null), it checks if (execTask.hasExectionError) and if true set 
     *      pExp=execTask.exectionError. Else if execTask.thread.isInterrupted, set pExp = 
     *      new InterruptedException(..).</li>
     *  <li>Clear execTask's Thread Reference.</li>
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
      ExecProcessScheduledFuture<?> execTask = null;
      if ((pRunnable != null) && (pRunnable instanceof ExecProcessScheduledFuture<?>)) {
        execTask = (ExecProcessScheduledFuture<?>) pRunnable;
      }
      
      /**
       * Attempt get get the cause of the Execution Termination if an exception was
       * thrown.
       */
      if ((pThrowable == null) && (execTask != null)) {
        if (execTask.hasExectionError()) {
          pThrowable = execTask.getExecutionError();
        } else {
          Thread pThread = execTask.getThread();
          if ((pThread != null) && (pThread.isInterrupted())) {
            pThrowable = new InterruptedException("The execution process was cancelled.");
          }
        }
      }
      
      if ((pThrowable != null) && (this.scheduler == null)) {
        logger.log(Level.WARNING, pThrowable.getMessage());
      }
      
      /** Clear the Task's Thread Reference and call this.Queue.onAfterExecute **/
      if (execTask != null) {
        execTask.setThread(null);
        if (this.scheduler != null) {
          this.scheduler.onAfterExecute(execTask, pThrowable);
          
          this.scheduler.onRescheduleTask(execTask);
        }
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the ThreadQueueExecuter which handles the scheduling and the
   * execution of the scheduled tasks.
   */
  private ExecProcessAsyncExecutor executor;
  /**
   * List of Threads Queued for Execution
   */
  private HashMap<String,ExecProcessScheduledFuture<?>> executingTasks;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * A Parameterless Constructor
   */
  public ExecProcessAsyncScheduler() {
    super();    
    this.executor = null;
    this.executingTasks = new HashMap<>();
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
    this.executor = null;
    super.finalize();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * <p>Called by the Executer before a Scheduled Future Task to add this task to 
   * this.executingTask List. However, if the same task is already executing, the
   * scheduled task must wait for the prior task to finish executing by joining the 
   * executing task's thread, and then wait until the Thread is removed from the 
   * ExecutingTask list.</p>
   * <p>If the Thread is free or not currently in the ExecutingTask list, it will be
   * added to the list. Also any prior Process Logs for the process will be cleared.</p>
   * <p><b>NOTE:</b> If (execTask.doPorcessLog), it assign this.ProcessLogger reference 
   * to the task and it enter a "Start Execution." log entry.</p>
   * @param execTask the new ExecProcessScheduledFuture Task to start
   */
  protected synchronized void onBeforeExecute(ExecProcessScheduledFuture<?> execTask) {
    if (execTask == null) {
      return;
    }
    
    String sKey = ExecProcess.getProcessKey(execTask);
    if (sKey != null) {
      if (this.executingTasks.containsKey(sKey)) {
        ExecProcessScheduledFuture<?> pPriorTask = this.executingTasks.get(sKey);
        Thread pThread = pPriorTask.getThread();
        if ((!pPriorTask.isDone()) 
            && (pThread != null) && (pThread.getState() !=  Thread.State.TERMINATED)) {
          try {
            pThread.join();
          } catch (InterruptedException ex) {}
          
          while (this.executingTasks.containsKey(sKey)) {
            try {
              TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ex) {}
          }
          
          this.executingTasks.put(sKey, execTask);          
        } else {
          this.executingTasks.put(sKey, execTask);
        }
      } else {
        this.executingTasks.put(sKey, execTask);
      }
      
      if (execTask.doProcessLog()) {
        ExecProcessLogger procLogger = this.getProcessLogger();
        if (procLogger != null) {
          execTask.setProcessLog(procLogger);
          procLogger.log(execTask, Level.INFO, "Start Execution.");
        }        
      }
    }
  }
  
  /**
   * <p>EventHandler called by the Executor when the execution of the Process has been
   * completed - either successfully or not. It logs a message, which include the
   * exception message if pExp!=null, and close the Log for the process. Finally, it
   * removes the process from the ExistingTasks list.</p>
   * <p><b>NOTE:</b> It also clears execTask's ProcessLogger reference - if it was 
   * assigned.</p>
   * @param execTask any iExecProcess reference
   * @param pThrowable a throwable or null is the process was successfully completed.
   */
  protected synchronized void onAfterExecute(ExecProcessScheduledFuture<?> execTask, 
          Throwable pThrowable) {
    try {
      /** Clear execTask's Logger */
      if (execTask != null) {
        execTask.setProcessLog(null);
      }
      
      String sErrMsg = null;
      if (pThrowable != null) {
        sErrMsg = pThrowable.getMessage();
        Throwable pCause = 
                (pThrowable instanceof ExecutionException)? pThrowable.getCause(): null;
        if (pCause != null) {
          sErrMsg += ";\n\t" + pCause.getMessage();
        }
      }
      
      ExecProcessLogger procLogger = this.getProcessLogger();        
      if ((procLogger != null) && (execTask != null) && (execTask.doProcessLog())) {
        if (sErrMsg != null) {          
          procLogger.log(execTask, Level.WARNING, 
                                "Execution Stopped because: " + sErrMsg);
        } else {
          procLogger.log(execTask, Level.INFO, 
                                "Execution Successfully Completed."); 
        }
        procLogger.clearLog(execTask);
      } else if (sErrMsg != null) {
        String sMsg = (execTask == null)? "Process Execustion Stopped because:\n ": 
                "Process["+ execTask.getProcessName() + "] Stopped because:\n " ;
        sMsg += sErrMsg;
        logger.log(Level.WARNING, sMsg);
      } else if ((execTask != null) && (execTask.doProcessLog())) {
        String sMsg = (execTask == null)? "": "Process["+ execTask.getProcessName() 
                + "]: ";
        sMsg += "Execution Successfully Completed.";
        logger.log(Level.INFO, sMsg);
      }
    } catch (Exception pOuterExp) {
      logger.log(Level.WARNING, "{0}.onAfterExecute Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pOuterExp.getMessage()});
    } finally {
      String sKey = (execTask == null)? null: ExecProcess.getProcessKey(execTask);
      if ((sKey != null) && (this.executingTasks.containsKey(sKey))) {
        this.executingTasks.remove(sKey);
      }
    }
  }
  
  /**
   * <p>Called by {@linkplain ExecProcessAsyncExecutor#afterExecute(java.lang.Runnable,
   * java.lang.Throwable) ThreadQueueExecutor.afterExecute} to process the rescheduling
   * if execTask's ExecProcess (execProcess). This is handled as follows:</p><ul>
   *  <li><b>If The Executor isTerminating or isTerminated</b> - don't reschedule the 
   *    execProcess</li>
   *  <li><b>If ((!execProcess.isDone) and (execProcess.delay()=0))</b> - the process has not
   *    been rescheduled for a Retry. Thus, call the {@linkplain 
   *    ExecProcess#updateSchedule(boolean) ExecProcess.updateSchedule(bRetry=false)}
   *    to reschedule the next task. If this process' schedule is not periodic, the
   *    task will be stopped (execProcess.isDone=true). Otherwise, the process will be
   *    ready for the next scheduled run.</li>
   *  <li>After the prior call if (!execProcess.isDone)</li> - reschedule the task by
   *    calling {@linkplain #executeProcess(bubblewrap.core.threads.ExecProcess) 
   *    executeProcess}.
   * </ul>
   * <p><b>NOTE:</b> Retry Scheduling is handled by the custom ExecProcess and based on
   * a specific retry requirement (e.g., a dependent task or dataset is not available).
   * This method only handles the rescheduling of periodic tasks.</p>
   * @param execTask 
   */
  protected synchronized void onRescheduleTask(ExecProcessScheduledFuture<?> execTask) {
    ExecProcess execProcess = (execTask == null)? null: execTask.getRunnable();
    if ((execProcess == null) || (this.executor == null) 
            || (this.executor.isTerminating())|| (this.executor.isTerminated())) {
      return; 
    }
    
    try {
      if ((!execProcess.isDone()) && (execProcess.getDelay(TimeUnit.NANOSECONDS) == 0)) {
        execProcess.updateSchedule(false);
      }
      
      String sMsg = "Process Execution scheduling has been stopped.";
      if (!execProcess.isDone()) { 
        this.executeProcess(execProcess);
        sMsg = "Process is re-submitted to the Execution Queue.";
      }
      
      /** Clear execTask's Logger */
      if ((execTask != null) && (execTask.doProcessLog())) {
        ExecProcessLogger procLogger = this.getProcessLogger();
        if (procLogger != null) {
          procLogger.log(execTask, Level.INFO, sMsg);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onRescheduleTask Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
    
  /**
   * Check if execProcess is queued for execution. It check the ThreadPool's Queue for a
   * Matching Process.
   * @param execProcess IExecProcess of interest
   * @return true if it is a Queued process
   */
  protected synchronized boolean isQueued(IExecProcess execProcess) {
    boolean result = false;
    String sKey = ExecProcess.getProcessKey(execProcess);
    if ((sKey != null) && (this.executor != null)) {
      BlockingQueue<Runnable> pQueue = this.executor.getQueue();
      if ((pQueue != null) && (!pQueue.isEmpty())) {
        for (Runnable pRunnable : pQueue) {
          if ((pRunnable != null) && (pRunnable instanceof IExecProcess)) {
            IExecProcess pQueuedProcess = (IExecProcess) pRunnable;
            if (sKey.equalsIgnoreCase(pQueuedProcess.getProcessId())) {
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
   * Check if execProcess is executing. It checks the Executing task List.
   * @param execProcess IExecProcess of interest
   * @return true if it is an executing process
   */
  protected synchronized boolean isExecuting(IExecProcess execProcess) {
    String sKey = ExecProcess.getProcessKey(execProcess);
    return ((sKey != null) && (this.executingTasks != null) 
            && (this.executingTasks.containsKey(sKey)));
  }  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Executor Management">
  /**
   * {@inheritDoc} <p>IMPLEMENT: Overload 1: Call {@linkplain 
   * #startExecutor(java.lang.Integer, java.lang.Integer) Overload 2} with undefined
   * Core Pool Size and Maximum Pool Size.</p>
   */
  @Override
  public void startExecutor() {
    this.startExecutor(null, null);
  }
  
  /**
   * Overload 2:Called to start the ThreadPoolExecutor - this call is ignored if the 
   * ExecProcessAsyncScheduler already has a ThreadPoolExecutor and the Executor has 
   * not been shutdown. When the Executor is (re)started and the ProcessLogger supports 
   * a Log Output Handler with a Periodic Schedule, it starts the execution of the 
   * output handler.
   * @param iCorePoolSize the size of the ThreadPool's core size (set to {@linkplain 
   * #MaxPoolSize}) is null or less than 1).
   * @param iMaxPoolsize the maximum size of the Thread Pool (set to iCorePoolSize if
   * null or less than iCorePoolSize).
   */
  public void startExecutor(Integer iCorePoolSize, Integer iMaxPoolsize) {
    if ((this.executor == null) || (this.executor.isShutdown())) {
      iCorePoolSize = ((iCorePoolSize == null) || (iCorePoolSize < 1))? 
                    ExecProcessAsyncScheduler.MaxPoolSize : iCorePoolSize;
      iMaxPoolsize = ((iMaxPoolsize == null) || (iMaxPoolsize < iCorePoolSize))? 
                    iCorePoolSize: iMaxPoolsize;
      this.executor = new ExecProcessAsyncExecutor(this, iCorePoolSize);
      this.executor.setMaximumPoolSize(iMaxPoolsize);
      this.executor.setRemoveOnCancelPolicy(true);
    }
    super.startExecutor();
  }
  
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Called to stop the Executor. If (bNow), it calls the {@linkplain
   * ThreadPoolExecutor#shutdownNow() shutdownNow} method and log all process that was
   * not completed. Otherwise, it calls the {@linkplain ThreadPoolExecutor#shutdown()
   * shutdown} method, which will wait for all queued processes to complete before it
   * terminates. It waits until the thread has terminated and then clear the Executing
   * Tasks list and call the super method to stop the ProcessLog Output Scheduler.</p>
   */
  @Override
  public void stopExecutor(boolean stopNow) {
    if (this.executor != null) {
      try {
        if (!this.executor.isShutdown()) {
          List<Runnable> pQueued = null;
          if (stopNow) {
            pQueued = this.executor.shutdownNow();
          } else {
            this.executor.shutdown();
          }

          while (this.executor.isTerminating()) {
            try {
              TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException pIExp) {
            }
            this.executor.purge();
          }

          if ((pQueued != null) && (!pQueued.isEmpty())) {
            for (Runnable pRunnable : pQueued) {
              logger.log(Level.INFO, "{0}.stopExecutor: {1} was not executed.",
                  new Object[]{this.getClass().getSimpleName(), pRunnable.toString()});
            }
          }
        }
        this.executor.purge();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.stopExecutor Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      } finally {
        this.executor = null;
        this.executingTasks.clear();
        
        super.stopExecutor(stopNow);
      }
    }
  }
  
  /**
   * {@inheritDoc } 
   * <p>IMPLEMENT: Get whether the ExecProcessAsyncScheduler's Executor is available.
   * </p>
   */
  @Override
  public boolean isExecuting() {
    return ((this.executor != null) && (!this.executor.isShutdown()));
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * {@inheritDoc } 
   * <p>IMPLEMENT: The process is added to the {@linkplain ExecProcessAsyncExecutor 
   * Executor}'s queue and Process log is started for Process and logging a "Queued" 
   * message that include the scheduled start time if it is a delayed execution. 
   * Return false if (pProces=null)</p>
   * @exception ExecutionException if the executor has not been started.
   */
  @Override
  public boolean executeProcess(ExecProcess execProcess) {
    boolean result = false;
    try {
      if (this.executor == null) {
        throw new ExecutionException("The Scheduler's Executor has not been started", 
                null);        
      }
      if (execProcess != null) {
        Future<?> execTask = null;
        TimeUnit eUnit = ExecProcess.DelayTimeUnit;
        long lDelay = execProcess.getDelay(eUnit);
        if (lDelay > 0) {          
          execTask = 
                 this.executor.schedule(execProcess, execProcess.getDelay(eUnit), eUnit);
        } else {
          execTask = this.executor.submit(execProcess);
        }
        
        ExecProcessLogger procLogger = this.getProcessLogger();
        if ((execProcess.doProcessLog()) && (procLogger != null)) {
          if (lDelay > 0) {
            procLogger.log(execProcess, Level.INFO, "Queued. "
                    + "Scheduled for " 
                    + execProcess.getScheduledTime().toString() + ".");
          } else {
            procLogger.log(execProcess, Level.INFO, "Queued. "
                    + "Execute with no delay");
          }
        }
        result = true;
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.executeProcess Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * <p>Called to stop a Process currently queued or executing. If Queued (not executing
   * it will remove the item from the queue. It will the calls the process' {@linkplain
   * ExecProcess#stopSchedule()} method to stop the executing task - and prevent it from
   * being rescheduled. It will then wait until the task's execution is completed (i.e.
   * if it is executing) for up to 60 seconds.</p>
   * <p>If (!this.isExcuting) or (this.getExecutingState(execProcess)=COMPLETED), it 
   * will still call the processes stopSchedule method.</p>
   * <p><b>NOTE:</b> Processes manually stopped can be restarted by calling the process'
   * {@linkplain ExecProcess#restartSchedule()} method and then adding it scheduler by
   * calling the {@linkplain #executeProcess(bubblewrap.core.threads.ExecProcess) 
   * executeProcess} method.</p>
   * @param execProcess the process to stop.
   * @return true if successful or false if an error occurred
   * @exception Exception is logged.
   */
  public boolean stopProcess(ExecProcess execProcess) {
    boolean result = true;
    try {
      if (execProcess != null) {
        ThreadExecStatus state = null;
        if ((this.isExecuting()) && 
                ((state = this.getExecProcessStatus(execProcess)) !=null) &&
                (ThreadExecStatus.COMPLETED.equals(state))) {
          if (this.isQueued(execProcess)) {
            BlockingQueue<Runnable> pQueue = this.executor.getQueue();
            pQueue.remove(execProcess);
          }
          execProcess.stopSchedule();

          int iCnt = 0;
          while ((iCnt < 60) && (this.isExecuting(execProcess))) {
            Thread.sleep(100l);
            iCnt++;
          }
        } else {
          execProcess.stopSchedule();
        }
      }
    } catch (Exception pExp) {
      result = false;
      logger.log(Level.WARNING, "{0}.stoexecProcess Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * {@inheritDoc } 
   * <p>IMPLEMENT: Return the state based on execProcess's {@linkplain 
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
   * {@inheritDoc } <p>IMPLEMENT: Return the QueuedProcesses list size</p>
   */
  @Override
  public int getQueuedCount() {
    int result = 0;
    if (this.executor != null) {
      BlockingQueue<Runnable> pQueue = this.executor.getQueue();
      result = (pQueue == null)? 0: pQueue.size();
    }
    return result;
  }
  
  /**
   * {@inheritDoc } <p>IMPLEMENT: Return the ExectingThreads list size</p>
   */
  @Override
  public int getExecutingCount() {
    return this.executingTasks.size();
  }
  
  //</editor-fold>  
}
