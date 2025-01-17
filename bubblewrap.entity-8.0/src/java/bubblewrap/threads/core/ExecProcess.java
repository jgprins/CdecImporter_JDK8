package bubblewrap.threads.core;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.threads.interfaces.IExecProcess;
import bubblewrap.threads.interfaces.IExecProcessRunnable;
import bubblewrap.threads.interfaces.IProcessLogger;
import java.util.Objects;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract that should be extended to provide custom execution functionality. The
 * base method implements IMyRunable, which adds a static logger and two properties:
 * a) {@linkplain #getProcessName() ProcessName}; and b) {@linkplain #getProcessId()  
 * ProcessId}.
 * @author kprins
 */
public abstract class ExecProcess implements IExecProcessRunnable, Delayed, IProcessLogger {

  //<editor-fold defaultstate="collapsed" desc="Protected Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(ExecProcess.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Get the Process's Key (for using in a HashMap and for comparison)
   * @param pProcess the process of interest
   * @return the processId as a lowercase string or null if undefined.
   */
  public static String getProcessKey(IExecProcess pProcess) {
    String sResult =
            (pProcess == null)? null: DataEntry.cleanString(pProcess.getProcessId());
    return (sResult == null)? null: sResult.toLowerCase();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * The {@linkplain TimeUnit TimeUnits} in which the delay is measured.
   * [{@linkplain TimeUnit#MILLISECONDS}]
   */
  protected static final TimeUnit DelayTimeUnit = TimeUnit.MILLISECONDS;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Field for the Process's unique processId
   */
  private String processId;
  /**
   * Field for the Process's processName
   */
  private String processName;
  /**
   * A Flag set through the constructor to control whether this process action should 
   * be entered in the process log (Default=true)
   */
  private Boolean doProcessLog;
  /**
   * Placeholder for the internal ExecProcess Logger Reference.
   * (Optional)
   */
  private ExecProcessLogger processLogger;
  /**
   * A Placeholder for Future Schedule Task in which the ExecProcess is wrapped.
   * (Optional reference - only used by the {@linkplain ThreadQueue}).
   */
  private ExecProcessFuture<?> processFuture;
  /**
   * The Placeholder for a ExecProcessSchedule to manage the Tasks Scheduling
   */
  private ExecProcessSchedule processSchedule;
          
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a Process name (required)
   * @param processName the process name
   * <p><b>NOTE:</b> A unique ProcesssId is generated for each ExecProcess instance</p>
   */
  public ExecProcess(String processName) {
    processName = DataEntry.cleanString(processName);
    if (processName == null) {
      throw new NullPointerException("The ExecProcess' name cannot be unassigned");
    }
    
    this.processName = processName;
    this.processFuture = null;
    this.processLogger = null;
    this.processSchedule = null;
    this.doProcessLog = null;
    try {
      this.processId = DataEntry.newUniqueId();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.New Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Public constructor sith a ProcessName and a DoProcessLog flag settings
   * <p><b>NOTE:</b> A unique ProcesssId is generated for each ExecProcess instance</p>
   * @param processName the process name
   * @param doProcessLog true to add actions to the processLog (Default=true)
   */
  public ExecProcess(String processName, Boolean doProcessLog) {
    this(processName);
    this.doProcessLog = ((doProcessLog == null) || (doProcessLog))? null: doProcessLog;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="IProcessLogger Override">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call to send to log to the ExecProcess Logger (if assigned) or to the 
   * server. If (!this.doProcessLog), no log of Level.INFO or less will be logged.</p>
   */
  @Override
  public void log(Level logLevel, String logMsg) { 
    if (this.processLogger != null) {
      this.processLogger.log(this, logLevel, logMsg);
    }
    if (logLevel.intValue() > Level.INFO.intValue()) {
      logger.log(logLevel, "{0} Error: \n\t{1}",
                  new Object[]{this.getClass().getSimpleName(), logMsg});
    } 
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call to send to log to the ExecProcess Logger (if assigned) or to the 
   * server. If (!this.doProcessLog), no log of Level.INFO or less will be logged.</p>
   */
  @Override
  public void log(Level logLevel, String logMsg, Object[] args) {
    if (this.processLogger != null) {
      this.processLogger.log(this, logLevel, logMsg, args);
    }
    if (logLevel.intValue() > Level.INFO.intValue()) {
      logger.log(logLevel, logMsg, args);
    }
  }
  
  /**
   * Called by inheritor to send an ExecutionException with a message and cause back 
   * to the Future Schedule Task wrapper OR, if the Future Schedule Task is not 
   * assigned,  to log the error message. This call is ignored if sErrMsg = ""|null and 
   * pCause=null. It also stop the Process' ExecProcessSchedule to prevent re-execution of
   * the process.
   * @param errMsg an descriptive error message (or null)
   * @param cause the cause of the error (or null).
   */
  protected final void onExecutionFailed(String errMsg, Throwable cause) {
    errMsg = DataEntry.cleanString(errMsg);
    if ((errMsg == null) && (cause == null)) {
      return;
    }
    
    if (this.processFuture != null) {
      ExecutionException pError = (errMsg == null)? new ExecutionException(cause):
                                          new ExecutionException(errMsg, cause);
      try {
        this.processFuture.setExecutionError(pError);
      } catch (Exception pExp) {}
    } else {
      if (errMsg == null) {
        errMsg = cause.getMessage();
      } else if (cause != null) {
        errMsg += "\n\t" + cause.getMessage();
      }
      this.log(Level.SEVERE, errMsg);
    }
    
    if (this.processSchedule != null) {
      this.processSchedule.stop();
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Thread Management Methods">
  /**
   * Get whether this Process should write to the process log (set through the 
   * constructor)
   * @return true if the process action should be logged.
   */
  public boolean doProcessLog() {
    return ((this.doProcessLog == null) || (this.doProcessLog));
  }
  
  /**
   * Set the Process Logger (assigned by the thread on initiation). 
   * Skipped if (!this.doProcessLog);
   * @param processLogger the logger or null to reset.
   */
  public final void setProcessLogger(ExecProcessLogger processLogger) {
    if (this.doProcessLog()) {
      this.processLogger = processLogger;
    }
  }
  
  /**
   * Set the Process's ScheduledFutureTask wrapper (Used by the {@linkplain 
   * ExecProcessScheduledFuture} to assign it self to the task before a the task execute
   * and clear it reference after execution has been completed.
   * @param processFuture the reference to the ScheduledFutureTask or null to clear the 
   * reference
   */
  public final void setFutureTask(ExecProcessFuture processFuture) {
    this.processFuture = processFuture;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Properties">
  /**
   * Return the unique processId
   * @return a 32-char unique Id.
   */
  @Override
  public String getProcessId() {
    return this.processId;
  }
  
  /**
   * Get the Process's Name
   * @return the assigned name
   */
  @Override
  public String getProcessName() {
    return this.processName;
  }
  
  /**
   * Check if sProcessId matches this.processId - comparison is non-case sensitive.
   * @param processId the processId to match
   * @return true if it is a match.
   */
  @Override
  public boolean isProcessId(String processId) {
    return (DataEntry.isEq(this.processId, processId, true));
  }
  //</editor-fold>  

  //<editor-fold defaultstate="collapsed" desc="Implement Delayed/Manage Scheduling">
  /**
   * Internally called to get the process' ExecProcess Schedule. I no schedule was 
   * assigned, a default one-time action schedule will be initiated with no delay and 
   * assign the ExecProcess references to the Schedule.
   * Otherwise, it will return the previously assigned schedule.
   * @return the Process ExecProcess Schedule 
   */
  protected ExecProcessSchedule getSchedule() {
    if (this.processSchedule == null) {
      this.processSchedule = new ExecProcessSchedule();
      this.processSchedule.setProcess(this.processName, this.processId);
    }
    return this.processSchedule;
  }
  
  /**
   * Get the Scheduler's isDone State
   * @return true if the process scheduled actions are completed or the Scheduling has
   * been stopped
   */
  public boolean isDone() {
    ExecProcessSchedule schedule = this.getSchedule();
    return ((schedule != null) && (schedule.isDone()));
  }
  
  /**
   * Get the Process's ExecProcess Schedule's isPeriodic state.
   * @return true if the process has periodic scheduled actions
   */
  public boolean isPeriodic() {
    ExecProcessSchedule schedule = this.getSchedule();
    return ((schedule != null) && (schedule.doPeriodic()));
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: If this.startTime is set and it is after the current
   * {@linkplain DateTime#getNow(java.util.TimeZone) DateTime.now}, return the delay in
   * the specified units. Otherwise, return zero.</p>
   */
  @Override
  public long getDelay(TimeUnit timeUnit) {
    long result = 0;
    try {
      ExecProcessSchedule schedule = this.getSchedule();
      if (schedule != null) {
        result = schedule.getDelay(timeUnit);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getDelay Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }

  /**
   * Called to update the Schedule. if this is a One-Time ExecProcess Schedule, this 
   * scheduling will be stop (unless bRetry =true and the schedule supports retries).
   * Otherwise, it will reschedule the task. It calls {@link ExecProcessSchedule#reschedule(boolean) ExecProcessSchedule.updateSchedule} to update
   * the schedule.
   * @param bRetry true if a retry should be scheduled.
   */
  public final void updateSchedule(boolean bRetry) {
    ExecProcessSchedule schedule = this.getSchedule();
    if ((schedule != null) && 
        ((schedule.doPeriodic()) || ((bRetry) && (schedule.doRetry())))) {
      schedule.reschedule(bRetry);
    }
  }
  
  /**
   * Called to stop the ExecProcess' scheduling - due to an error or for any other 
   * reason. Once stopped it can be manually restarted again. It calls {@linkplain 
   * ExecProcessSchedule#stop() ExecProcessSchedule.stop} to stop the schedule.
   */
  public final void stopSchedule() {
    ExecProcessSchedule schedule = this.getSchedule();
    if (schedule != null) {
      schedule.stop();
    }
  }
   
  /**
   * Called to stop the ExecProcess' scheduling - due to an error or for any other 
   * reason. Once stopped it can be manually restarted again. It calls {@linkplain 
   * ExecProcessSchedule#stop() ExecProcessSchedule.stop} to stop the schedule.
   */
  public final void restartSchedule() {
    ExecProcessSchedule schedule = this.getSchedule();
    if (schedule != null) {
      schedule.restart();
    }
  }
 
  /**
   * Get the DateTime when the process is Scheduled to run.
   * @return the Process ActiomSchedule's ScehduledTime.
   */
  public final DateTime getScheduledTime() {
    ExecProcessSchedule schedule = this.getSchedule();
    return (schedule == null)? DateTime.getNow(null): schedule.getScheduledTime();
  }
  
  /**
   * Set the ExecProcess' Scheduled Time - It initiates a one-Time ExecProcess Schedule
   * and set the schedule's specified start Time.
   * @param startTime the start time - set to null for no delay.
   */
  public final void setSchedule(DateTime startTime) {
    ExecProcessSchedule schedule = new ExecProcessSchedule();    
    if (schedule != null) {
      schedule.stop();
      schedule.start(startTime);
    }
  }
   
  /**
   * Set the ExecProcess' Scheduled Time delay - It initiates a one-Time ExecProcess Schedule
   * and set the schedule's specified delay.
   * @param delayTime the start delay time - >= 0.
   * @param timeUnit the unit of the delay time
   */
  public final void setSchedule(long delayTime, TimeUnit timeUnit) {
    ExecProcessSchedule schedule = new ExecProcessSchedule();    
    if (schedule != null) {
      schedule.stop();
      schedule.start(delayTime, timeUnit);
    }
  }
   
  /**
   * Set the ExecProcess' Scheduled Time delay - It initiates a one-Time ExecProcess 
   * Schedule and set the schedule's specified Start Time and delay after the StartTime.
   * @param startTime the start time - set to null for using teh current DateTiem as 
   * the start time.
   * @param delayTime the start delay time - >= 0.
   * @param timeUnit the unit of the delay time
   */
  public final void setSchedule(DateTime startTime, long delayTime, TimeUnit timeUnit) {
    ExecProcessSchedule schedule = new ExecProcessSchedule();    
    if (schedule != null) {
      schedule.stop();
      schedule.start(startTime, delayTime, timeUnit);
    }
  }

  /**
   * Assign a predefined schedule to the ExecProcess. Typically used when assigning 
   * Periodic (repeating) action scheduling. If schedule=null, any previous scheduling
   * will be reset. If assigned, assign the ExecProcess references to the Schedule.
   * @param schedule the new action schedule
   */
  public final void setSchedule(ExecProcessSchedule schedule) {
    if ((this.processSchedule = schedule) != null) {
      this.processSchedule.setProcess(this.processName, this.processId);
    }    
  } 
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Get the Delay (in MilliSeconds) of this ExecProcess
   * and compare it to the Delay of the pDelayes process. If pDelayes=null,
   * assume a zero delay.</p>
   */
  @Override
  public int compareTo(Delayed delay) {
    long delayTime = (delay == null)? 0: delay.getDelay(TimeUnit.NANOSECONDS);
    long diff = this.getDelay(TimeUnit.NANOSECONDS) - delayTime;
    return (diff == 0) ? 0 : ((diff < 0) ? -1 : 1); 
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "ExecProcess[" + this.msProcessName + "]"</p>
   */
  @Override
  public String toString() {
    return "Process[" + this.processName + "]";
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Validate that (obj instanceof IExecProcess) and
   * (obj.processId=this.processId) (case insensitive)</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean bResult = ((obj != null) && (obj instanceof IExecProcess));
    if (bResult) {
      IExecProcess pProcess = (IExecProcess) obj;
      bResult = DataEntry.isEq(this.processId, pProcess.getProcessId(), true);
    }
    return bResult;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return a HashCode using the ProcessId</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 19 * hash + Objects.hashCode(this.processId);
    return hash;
  }
  //</editor-fold>
}
