package bubblewrap.threads.core;

import bubblewrap.threads.enums.ScheduleStatus;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import bubblewrap.threads.interfaces.IExecProcess;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeSchedule;
import bubblewrap.threads.events.ScheduleEventArgs;
import bubblewrap.threads.events.ScheduleEventHandler;

/**
 * <p>The ExecProcessSchedule can be used to schedule the one-time action or the Periodic 
 * (or repeating) Scheduling of an action at a {@linkplain TimeSchedule} starting a
 * specific DateTime or with a specified delay. In both types of schedules can be 
 * setup with a RetrySchedule, which allow the task to be rescheduled at shorter 
 * intervals for in case the process could not be completed at the scheduled time.</p>  
 * <p>The RetrySchedule does not impact the Periodic Scheduling of the process, neither
 * does a call to {@linkplain #executeNow()}.</p>
 * <p>The ExecProcessSchedule can be started with the current DateTime as the startDate, 
 * or with a delay from the current DateTime, at a given start DateTime, or at a given 
 * start DateTime with a delay. If not started before calling {@linkplain 
 * #getScheduledTime() this.scheduleTime} or {@link #reschedule(boolean) reschedule} 
 * or {@linkplain #executeNow() executeNow}, the Schedule will be 
 * started at the current DateTime with no delay.</p>
 * <p>Sporadic Processes (i.e., repeated processes without a periodic (fix) schedule, can
 * initiated as One-Time ExecProcess that are manually rescheduled by restarting the 
 * process schedule when it is needed.</p>
 * <p><b>NOTE:</b> To reset a ExecProcessSchedule start Time, or schedule start time, the
 * scheduling must first be stopped.</p>
 * @author kprins
 */
public final class ExecProcessSchedule implements IExecProcess {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ExecProcessSchedule.class.getName());
  //</editor-fold>
   
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The DateTime when the ExecProcess is scheduled to be executed. 
   */
  private DateTime scheduledTime;
  /**
   * The DateTime of the ExecProcess's periodical Schedule Execution. This DateTime is 
   * update when the ExecProcess was completed and must be rescheduled for it's next 
   * periodical Scheduled time.
   */
//  private DateTime periodicTime;
  /**
   * The ExecProcessSchedules's for Periodic Scheduled    
   */
  private TimeSchedule periodicSchedule;
  /**
   * A Schedule Time Step to add if the ExecProcess Execution was unsuccessful at the 
   * Scheduled Time and has to be retried after a specified delay.
   */
  private TimeSchedule retrySchedule;
  /**
   * The TimeZone of the schedules (or null)
   */
  private TimeZone timeZone;
  /**
   * Placeholder for ProcessName
   */
  private String processName;
  /**
   * Placeholder for ProcessId
   */
  private String processId;
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Protected Event Senders">
  /**
   * The ScheduleEventHandler that fires the Schedule Started Event.
   */
  public final ScheduleEventHandler ScheduleStarted;

  /**
   * Called to fire the Schedule Started Event.
   */
  protected final void fireScheduleStarted() {
    this.ScheduleStarted.fireEvent(this, new ScheduleEventArgs(ScheduleStatus.STARTED));
  }
  
  /**
   * The ScheduleEventHandler that fires the Schedule Updated Event.
   */
  public final ScheduleEventHandler ScheduleUpdated;

  /**
   * Called to fire the Schedule Updated Event.
   * @param status the Completion Status - ignored if !IsUpdated
   */
  protected final void fireScheduleUpdated(ScheduleStatus status) {
    if (status.isUpdated()) {
      this.ScheduleUpdated.fireEvent(this, new ScheduleEventArgs(status));
    }
  }
  
  /**
   * The ScheduleEventHandler that fires the Schedule Completed Event.
   */
  public final ScheduleEventHandler ScheduleCompleted;

  /**
   * Called to fire the Schedule Completed Event.
   * @param status the Completion Status - ignored if !isDone
   */
  protected final void fireScheduleCompleted(ScheduleStatus status) {
    if (status.isDone()) {
      this.ScheduleCompleted.fireEvent(this, new ScheduleEventArgs(status));
    }
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - a One-Time ExecProcessSchedule with no Retry Scheduling options
   */
  public ExecProcessSchedule() {
    super(); 
    this.ScheduleStarted = new ScheduleEventHandler();
    this.ScheduleUpdated = new ScheduleEventHandler();
    this.ScheduleCompleted = new ScheduleEventHandler();
    
  //  this.periodicTime = null;
    this.periodicSchedule = null;
    this.retrySchedule = null;
    this.timeZone = null;
    this.scheduledTime = null;
  }
  
  /**
   * Public Constructor - a One-Time ExecProcessSchedule with a Retry Schedule
   * @param retrySchedule a ScheduleStep to set retry schedules on request and no retry 
   * if null.
   */
  public ExecProcessSchedule(TimeSchedule retrySchedule) {
    this();
    this.retrySchedule = retrySchedule;
    this.timeZone = (this.retrySchedule == null)? null: retrySchedule.getTimeZone();
  }
  
  /**
   * Public Constructor - a One-Time ExecProcessSchedule with a Retry Schedule. One of the 
   * schedules cannot be null;
   * @param periodicSchedule a TimeSchedule to set the period Schedule. If null, this
   * will be a One-Time ExecProcessSchedule
   * @param retrySchedule a TimeSchedule to set retry schedules on request and no retry 
   * if null.
   */
  public ExecProcessSchedule(TimeSchedule periodicSchedule, TimeSchedule retrySchedule) {
    this();
    if ((periodicSchedule == null) && (retrySchedule == null)) {
      throw new IllegalArgumentException("Either the Periodical or Retry Schedule must "
              + "assigned.");
    }
    if ((periodicSchedule != null) && (retrySchedule != null)) {
      if (periodicSchedule.compareIntervalTo(retrySchedule) <= 0) {
        throw new IllegalArgumentException("The Time Interval for the Retry " + 
                retrySchedule.toString() + "] cannot be equal to or longer that of "
                + "Periodic " + periodicSchedule.toString() + ".");
      } else if (!DataEntry.isEq(periodicSchedule.getTimeZone(), 
                                                          retrySchedule.getTimeZone())) {
        throw new IllegalArgumentException("The TimeZone of the Periodical and Retry "
                + "Schedules are not the same.");
      }
    }
    
    this.periodicSchedule = periodicSchedule;
    this.retrySchedule = retrySchedule;
    this.timeZone = (periodicSchedule != null)? periodicSchedule.getTimeZone():
                                                    retrySchedule.getTimeZone();
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Clear this.EventDelegate</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();    
    this.ScheduleStarted.clear();
    this.ScheduleUpdated.clear();
    this.ScheduleCompleted.clear();
    this.periodicSchedule = null;
    this.retrySchedule = null;
  //  this.periodicTime = null;
    this.scheduledTime = null;
  }  
    
  /**
   * Called to reset the StepCount of the Periodic and Retry ScheduleSteps. 
   * If (bOnlyRetry) the Periodic StepCount will not be reset.
   * @param onlyRetry true to reset only the Retry ScheduleSteps
   */
  protected void resetStepIndex(boolean onlyRetry) {
    if ((!onlyRetry) && (this.periodicSchedule != null)) {
      this.periodicSchedule.resetStepIndex();
    }
    if (this.retrySchedule != null) {
      this.retrySchedule.resetStepIndex();
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Properties">    
  /**
   * Get whether this periodic Scheduled has been started
   * @return ((this.doPeriodic()) || (this.doRetry()) || (this.scheduledTime!= null))
   */
  public boolean isStart() {
    return ((this.doPeriodic()) || (this.doRetry()) || (this.scheduledTime!= null));
  }
  /**
   * Get whether this periodic Scheduled has been stopped.
   * @return (((this.doPeriodic()) || (this.doRetry())) && (this.scheduledTime == null))
   */
  public boolean isDone() {
    boolean result = true;
    if ((this.doPeriodic()) || (this.doRetry())) {
      result = (this.scheduledTime == null);
    }
    return result;
  }
  
  /**
   * Get the ExecProcess's Scheduled Execution Time. If the ExecProcessSchedule has 
   * never been started, it will first call {@linkplain #start(bubblewrap.io.DateTime) 
   * this.start} with the StartDate = null. This will set the ScheduledTime = {@linkplain
   * DateTime#getNow(java.util.TimeZone) DateTime.Now}
   * @return a DateTime of the next Scheduled Execution Time (or null if (this.IsDone))
   */
  public DateTime getScheduledTime() {
    if ((this.hasPeriodic()) && (!this.doPeriodic())) {
      this.start(null);
    }
    return this.scheduledTime;
  }
  
  /**
   * Get the Scheduled Delay. If Stopped, it will throw an IllegalArgumentException
   * @param timeUnit the TiemUnit to convert the delay time to
   * @return the delay time or zero if no delay.
   * @exception an IllegalArgumentException will be thrown if the Schedule isDone.
   */
  public long getDelay(TimeUnit timeUnit) {
    long result = 0;    
    try {
      DateTime schedDt = this.getScheduledTime();
      if (schedDt != null) {        
        DateTime pNow = DateTime.getNow(null);
        result = schedDt.diff(pNow, timeUnit);
        result = (result <= 0)? 0: result;
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getDelay Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw new IllegalArgumentException("Illegal Argument in " 
              + this.getClass().getSimpleName() + ".getDelay", pExp);
    }
    return result;
  }
  
  /**
   * Get the Periodic Schedule of the ExecProcess
   * @return the assigned schedule (or null if (!isPeriodic)
   */
  public TimeSchedule getPeriodicSchedule() {
    return (this.doPeriodic())? this.periodicSchedule: null;
  }
  
  /**
   * Get the ExecProcesss Retry Schedule
   * @return the assigned schedule (or null if (!doRetry)
   */
  public TimeSchedule getRetrySchedule() {
    return this.retrySchedule;
  }
  
  /**
   * Get whether the ExecProcessSchedule has a Periodical Schedule 
   * @return (this.periodicSchedule != null)
   */
  public boolean hasPeriodic() {
    return (this.periodicSchedule != null);
  }
  
  /**
   * Get whether the Retry Schedule is active
   * @return true if a a Periodic Schedule is assigned and active.
   */
  public boolean doPeriodic() {
    return ((this.periodicSchedule != null) && (this.periodicSchedule.isActive()));
  }
  
  /**
   * Get whether the ExecProcessSchedule has a Retry Schedule 
   * @return (this.retrySchedule != null)
   */
  public boolean hasRetry() {
    return (this.retrySchedule != null);
  }
  
  /**
   * Get whether the Retry Schedule is active
   * @return true if a a Retry Schedule is assigned and Active.
   */
  public boolean doRetry() {
    return ((this.retrySchedule != null) && (this.retrySchedule.isActive()));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Schedule Management Methods">
  /**
   * <p>Call to force the scheduling of the action without a delay - regardless of
   * current periodic scheduled time. The latter will not be reset in this process and
   * after completion of the action a call to {@link #reschedule(boolean) updateSchedule} 
   * will reset the scheduled time to the periodic schedule time to
   * resume the regular schedule.</p>
   * <p><b>NOTE:</b> If not yet started, the {@linkplain #start(bubblewrap.io.DateTime,
   * long, java.util.concurrent.TimeUnit) start] method will be called with the
   * StartDateTime=Now and no delay.</p>
   * <p><b>NOTE:</b> If Previously Started, it will fire the ScheduleUpdated[RUNNOW] 
   * event.</p>
   */
  public void executeNow() {
    if ((this.hasPeriodic()) && (!this.doPeriodic())) {
      this.start(null);
    } else {
      this.scheduledTime = DateTime.getNow(this.timeZone);
      this.fireScheduleUpdated(ScheduleStatus.RUNNOW);
    }
  }
  
  /**
   * <p>Overload 1: Called to (re-)start a previously completed or stopped schedule.
   * It will have no effect on a active Schedule (!isDone). To force an immediate 
   * execution, call {@linkplain #executeNow() this.executeNow}.</p>
   * <p>It sets the Scheduled time for the next action to pStartDateTime. If the latter
   * is undefined, it will assumed the {@linkplain DateTime#getNow(java.util.TimeZone) 
   * current date-time}. If <tt>startDt</tt> is prior to the current date time, the 
   * action will have no delay.</p>
   * <p>Calls {@linkplain #start(bubblewrap.io.datetime.DateTime, long, 
   * java.util.concurrent.TimeUnit) Overload 3}, with no delay.</p>
   * @param startDt the date to start the schedule
   */
  public void start(DateTime startDt) {
    this.start(startDt, 0, TimeUnit.HOURS);
  }
  
  /**
   * <p>Overload 2:  Called to (re-)start a previously completed or stopped schedule 
   * after a delay from the {@linkplain DateTime#getNow(java.util.TimeZone) current
   * DateTime}. 
   * Calls {@linkplain #start(bubblewrap.io.DateTime, long, java.util.concurrent.TimeUnit) 
   * Overload 3}, with no startDate= null.</p>
   * @param startDelay the delay to add (ignored is less or equal to zero).
   * @param timeUnit the TimeUnit of delay
   */
  public void start(long startDelay, TimeUnit timeUnit) {
    this.start(null, startDelay, timeUnit);
  }
  
  /**
   * <p>Called to Start a previously completed or stopped periodical schedule. It will 
   * have no effect on a active Schedule (!isDone). To force an immediate execution, call
   * {@linkplain #executeNow() this.executeNow}.</p>
   * <p>It sets the Scheduled time for the next action to pStartDateTime and add a time
   * delay if specified. If pStartDateTime is undefined, it will assumed the
   * {@linkplain DateTime#getNow(java.util.TimeZone) current DateTime}. If specified
   * start DateTime is prior to the current DateTime, the action will have no delay.
   * </p>
   * <p><b>NOTES:</b></p><ul>
   *  <li>To reset the StartDateTime of an active periodic
   *    ExecProcessSchedule, the Schedule must be stopped manual (call {@linkplain #stop()}
   *    before calling any of the start Overloads.</li>
   *  <li>If not yet started, calling {@linkplain #getScheduledTime()} or any
   *    {@link #reschedule(boolean) updateSchedule} overloads, will first
   *    start the schedule setting the ScheduleTime to {@linkplain
   *    DateTime#getNow(java.util.TimeZone) DateTime.Now}.</li>
   *  <li>After starting the a Periodic schedule with a delayed start, the {@linkplain
   *   #executeNow()} method can be called to force a once of un-delayed scheduling
   *   of the action without affecting the set periodic schedule.</li>
   * </ul>
   * <p><b>NOTE:</b> The Start process will fire the Event[SCHEDULE_STARTED] if the
   * schedule has not been started before.</p>
   * @param startDt the Start DateTime
   * @param startDelay the delay to add to the Start DateTime
   * @param timeUnit the TimeUnit of delay
   */
  public void start(DateTime startDt, long startDelay, TimeUnit timeUnit) {
    try {
      if (this.scheduledTime == null) {
        DateTime schedDt = null;
        if (this.periodicSchedule == null) {
          schedDt = (startDt == null)? DateTime.getNow(this.timeZone):
                      startDt;
        } else {
          schedDt = this.periodicSchedule.getTimeStep(startDt);
        }
        
        if (startDelay > 0) {
          schedDt = schedDt.addTime(startDelay, timeUnit);
        }
        
        this.scheduledTime = schedDt;        
        /* Reset the Periodic- and RetrySteps' StepCount */
        this.resetStepIndex(false);        
        /* Fire the Start Schedule Event */
        this.fireScheduleStarted();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.Start Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * <p>Called to restart a previously stopped schedule. It will restart the schedule
   * as follows</p><ul>
   *  <li>If never been scheduled, call {@linkplain #start(bubblewrap.io.DateTime) start}
   *    with pStartDt=null (using the current date)</li>
   *  <li>If (!this.doPeriodic), set the ScheduledTime=DateTime.Now(this.TimeZone)</li>
   *  <li>If (this.doPeriodic), set the ScheduledTime={@linkplain 
   *    ScheduleStep#getStartStep(bubblewrap.io.DateTime, java.util.TimeZone) 
   *    this.periodicStep.getStartStep(this.scheduledTime,this.TimeZone)}</li>
   * </ul>
   * <p><b>NOTE:</b> The Restart process will fire the ScheduleStarted Event</p>
   * @return the new Scheduled Time
   */
  public DateTime restart() {
    if (!this.doPeriodic()) {
      if (this.scheduledTime == null) {
        this.start(null);
      } else {
        DateTime schedDt = DateTime.getNow(this.timeZone);
        if (this.periodicSchedule != null) {
          schedDt = this.periodicSchedule.getTimeStep(schedDt);
        }
        
        this.scheduledTime = schedDt;
        this.resetStepIndex(false);
        this.fireScheduleStarted();
      }
    } else {
      DateTime schedDt = DateTime.getNow(this.timeZone);
      this.scheduledTime = this.periodicSchedule.getTimeStep(schedDt);
      this.fireScheduleStarted();
    }
    return this.scheduledTime;
  }
  
  /**
   * Overload 1: Called to reschedule the action for the next periodical step. Call
   * {@link #reschedule(boolean) Overload 2} with retry=false.
   * @return the next scheduled date.
   */
  public DateTime reschedule() {
    return this.reschedule(false);
  }
  
  /**
   * <p>Overload 2: Called to reschedule the action. It processes the request as follows:
   * </p><ul>
   * <li><b>If this.isDone:</b> (i.e., the periodic schedule has been stopped) ship 
   *  process and return this.scheduledTime</li>
   * <li><b>If {@linkplain #hasPeriodic() this.hasPeriodic}:</b><ul>
   *  <li>If (!{@linkplain #doPeriodic() this.doPeriodic}): 
   *    start the process by calling {@linkplain #start(bubblewrap.io.datetime.DateTime) 
   *    this.start(null)}</li>
   *  <li><b>Else If (retry) and ({@linkplain TimeSchedule#incSteps() 
   *                this.retrySchedule.incSteps}):</b> - schedule the next retry step
   *   and fire the ScheduleUpdated[RETRIED] event.</li>
   *  <li><b>Else If ({@linkplain TimeSchedule#incSteps() 
   *                this.periodicSchedule.incSteps}):</b> - schedule the next periodic
   *   step and fire the ScheduleUpdated[PERIODIC] event.</li>
   *  <li><b>Else:</b> class {@linkplain #stop(bubblewrap.threads.events.ScheduleStatus) 
   *   this.stop(EXPIRED)}. Assuming the process is done. Only applicable if Periodical
   *   Schedule's {@linkplain TimeSchedule#getMaxSteps() maxSteps} is set.</li>
   * </ul></li>
   * <li><b>ELSE:</b><ul>
   *  <li>If not the schedule has not been started: start the process by calling 
   *    {@linkplain #start(bubblewrap.io.datetime.DateTime) this.start(null)}.</li>
   *  <li><b>Else If (retry) and ({@linkplain TimeSchedule#incSteps() 
   *                this.retrySchedule.incSteps}):</b> - schedule the next retry step
   *   and fire the ScheduleUpdated[RETRIED] event.</li>
   *  <li><b>Else:</b> class {@linkplain #stop(bubblewrap.threads.events.ScheduleStatus) 
   *   this.stop(COMPLETED)}. Assuming the process is done.
   * </ul></li>
   * </ul>
   * <p><b>NOTE:</b><tt>retry</tt> will be ignored if (!this.hasRetry) </p>
   * @param retry true if the retry schedule should be used.
   * @return the new Scheduled Time (can be null if stopped)
   */
  public DateTime reschedule(boolean retry) {
    try {
      retry = ((retry) && (this.hasRetry()));
      if (!this.isDone()) {
        if (this.hasPeriodic()) {
          if (!this.periodicSchedule.isActive()) {
            this.start(null);
          } else if ((retry) && (this.retrySchedule.incSteps())) {
            this.scheduledTime = this.retrySchedule.getNextStep();
            this.fireScheduleUpdated(ScheduleStatus.RETRIED);
          } else if (this.periodicSchedule.incSteps()) {
            this.scheduledTime = this.periodicSchedule.getNextStep();
            this.resetStepIndex(true);
            this.fireScheduleUpdated(ScheduleStatus.PERIODIC);
          } else {
            this.stop(ScheduleStatus.COMPLETED);
          }
        } else {
          if (this.scheduledTime == null) {
            this.start(null);
          } else if ((retry) && (this.retrySchedule.incSteps())) {
            this.scheduledTime = this.retrySchedule.getNextStep();
            this.fireScheduleUpdated(ScheduleStatus.RETRIED);
          } else {
            this.stop(ScheduleStatus.COMPLETED);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.updateSchedule Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      this.stop(ScheduleStatus.ERROR);
    }
    return this.scheduledTime;
  }
  
  
  /**
   * Called to manually stop the scheduled actions.
   * <p><b>NOTE:</b> The ScheduleStopped[STOPPED] will be fired if the Schedule had been
   * started.</p>
   */
  public void stop() {
    this.stop(ScheduleStatus.STOPPED);
  }
  
  /**
   * Called to stop the scheduled due to an Execution Error.
   * <p><b>NOTE:</b> The ScheduleStopped[ERROR] event will be fired if the Schedule had 
   * been started.</p>
   */
  public void stopOnError() {
    this.stop(ScheduleStatus.ERROR);
  }
  
  /**
   * Called to stop the scheduled actions and fire the Event[eEventExecProcess].
   */
  private void stop(ScheduleStatus status) {
    if (this.scheduledTime != null) {
      this.scheduledTime = null;
      this.fireScheduleCompleted(status);
    }
  }

  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a clone of this ExecProcessSchedule copying all its properties
   * except for this.scheduledTime and the instance's event handlers</p>
   */
  @Override
  public ExecProcessSchedule clone() {
    ExecProcessSchedule result = new ExecProcessSchedule();
    if (this.periodicSchedule != null) {
      result.periodicSchedule = this.periodicSchedule.clone();
    }
    if (this.retrySchedule != null) {
      result.retrySchedule = this.retrySchedule.clone();
    }
    result.timeZone = this.timeZone;
    result.processId = this.processId;
    result.processName = this.processName;
    return result;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements IExcProcess">
  /**
   * Called by the {@linkplain ExecProcess#setSchedule() ExecProcess.setSchedule} 
   * method to assign its reference to the schedule.  There reference are used by the
   * EventListners to identify the process.
   * @param processName the owner ExecProcess's ProcessName
   * @param processId the owner ExecProcess's ProcessId
   */
  public void setProcess(String processName, String processId) {
    this.processName = DataEntry.cleanString(processName);
    this.processId = DataEntry.cleanString(processId);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: return the Owner ExecProcess's ProcessID</p>
   */
  @Override
  public String getProcessId() {
    return this.processId;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: return the Owner ExecProcess's ProcessName</p>
   */
  @Override
  public String getProcessName() {
    return this.processName;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return true is this.processId is assigned and it
   * matches sProcessId (non case-sensitive comparison).</p>
   */
  @Override
  public boolean isProcessId(String sProcessId) {
    return ((this.processId != null)
            && (DataEntry.isEq(this.processId, sProcessId, true)));
  }
  //</editor-fold>
}
