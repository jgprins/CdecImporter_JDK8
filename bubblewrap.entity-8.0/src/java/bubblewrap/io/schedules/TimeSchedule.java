package bubblewrap.io.schedules;

import java.io.Serializable;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import bubblewrap.io.datetime.DateTime;

/**
 * The ScheduleStep are used by the {@linkplain ActionSchedule} class for managing it
 * Periodic and Retry Schedules. The ScheduleStep manage schedules in discreet time 
 * steps as defined by its TimeUnit.
 */
public class TimeSchedule implements Serializable, Comparable<TimeSchedule> {
  
  /**
   * Get the TimeUnit if the for the ScheduleStep's StartDelay setting based on the
   * ScheduleStep's Step TimeUnit. 
   * @param pStepTimeUnit the ScheduleStep's Step TimeUnit
   * @return return one TimeUnit increment down from the Step TimeUnit (e.g. if
   * the Step TimeUnit=DAYS, it returns HOURS, or HOURS it returns MINUTES, etc.)
   */
  public static TimeUnit getDelayTimeUnit(TimeUnit pStepTimeUnit) {
    TimeUnit eResult = TimeUnit.MILLISECONDS;
    if (pStepTimeUnit == TimeUnit.DAYS) {
      eResult = TimeUnit.HOURS;
    } else if (pStepTimeUnit == TimeUnit.HOURS) {
      eResult = TimeUnit.MINUTES;
    } else if (pStepTimeUnit == TimeUnit.MINUTES) {
      eResult = TimeUnit.SECONDS;
    } else if (pStepTimeUnit == TimeUnit.SECONDS) {
      eResult = TimeUnit.MICROSECONDS;
    } else if (pStepTimeUnit == TimeUnit.MICROSECONDS) {
      eResult = TimeUnit.NANOSECONDS;
    } else {
      throw new IllegalArgumentException("Cannot have a Delay Time for a"
              + " NanoSecond ScheduleStep.");
    }
    return eResult;
  }

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Schedule's TimeInterval
   */
  private TimeInterval timeInterval;
  /**
   * The Maximum Number of Step allowed (default = null|0 - unlimited)
   */
  private Integer maxSteps;
  /**
   * The current stepIndex - only used if maxSteps is set.
   */
  private Integer stepIndex;
  /**
   * The Schedule's Current TimeStep
   */
  private TimeStep timeStep;
  /**
   * The Schedule's Time (default = the server's local timezone)
   */
  private TimeZone timeZone;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor with a timeInterval
   * @param timeInterval the TimeInterval to use for scheduling
   */
  public TimeSchedule(TimeInterval timeInterval) {
    if (timeInterval == null) {
      throw new IllegalArgumentException("The Schedule's TimeInetrval is undefined.");
    }
    this.timeInterval = timeInterval;
    this.maxSteps = null;
    this.stepIndex = null;
    this.timeZone = null;
    this.timeStep = null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Schedule's Time Interval
   * @return the assigned interval
   */
  public TimeInterval getTimeInterval() {
    return this.timeInterval;
  }

  /**
   * Get the {@linkplain TimeUnit} of the schedule interval's time step
   * @return this.timeInterval.timeUnit
   */
  public TimeUnit getTimeUnit() {
    return this.timeInterval.getTimeUnit();
  }
  
  /**
   * Get the Schedule's TimeZone
   * @return the assigned TimeZone.
   */
  public TimeZone getTimeZone() {
    return this.timeZone;
  }
  
  /**
   * Set the Schedule's TimeZone
   * @param timeZone the new time zone (if null, use local server time zone)
   * @throws IllegalArgumentException is this.isActive
   */
  public void setTimeZone(TimeZone timeZone) {
    if (this.timeStep != null) {
      throw new IllegalArgumentException("The Schedule's time zone cannot be changed "
              + "while a schedule is active. Reset the schedule before changing the "
              + "TimeZone.");
    }
    this.timeZone = timeZone;
  }
  
  /**
   * Get the Schedules Number of Steps are limited.
   * @return ((this.maxSteps != null) && (this.maxSteps > 0))
   */
  public boolean hasMaxSteps() {
    return ((this.maxSteps != null) && (this.maxSteps > 0));
  }
  
  /**
   * Set the Schedules Maximum Number of Steps.
   * @return the assigned value (null if unlimited)
   */
  public Integer getMaxSteps() {
    return this.maxSteps;
  }
  
  /**
   * Set the Schedules Maximum Number of Steps (null or maxSteps &le; 0 
   * @param maxSteps the maximum number of steps allowed (if null or &le; 0 this number
   * of steps are unlimited).
   * @throws IllegalArgumentException is this.isActive
   */
  public void setMaxSteps(Integer maxSteps) {
    if (this.timeStep != null) {
      throw new IllegalArgumentException("The Schedule's Maximum Step Count cannot be "
              + "changed while a schedule is active. Reset the schedule before changing "
              + "the Settings.");
    }
    this.maxSteps = ((maxSteps == null) || (maxSteps <= 0))? null: maxSteps;
  }
  
  /**
   * Get the current Schedule step's index
   * @return 0 if (!this.hasMaxSteps) or this.setpIndex.
   */
  public int getStepIndex() {
    int result = 0;
    if (this.hasMaxSteps()) {
      if (this.stepIndex == null) {
        this.stepIndex = 0;
      }
      result = this.stepIndex;
    }
    return result;
  }
    
  /**
   * Called by the Schedule user to increment the step before updating the schedule 
   * and to check if more steps are allowed. if (!this.hasMaxSteps), this
   * call will always return true (unlimited steps). Otherwise, it will return true
   * if the incremented this.stepIndex is less than this.maxStepCount.
   * @return (!this.hasMaxSteps) || (this.stepIndex &lt; this.maxSteps)
   */
  public boolean incSteps() {
    boolean result = true;
    if (this.maxSteps != null) {
      if (this.stepIndex == null) {
        this.stepIndex = 0;
      } else if (this.stepIndex < this.maxSteps) {
        this.stepIndex++;
      }
      result = (this.stepIndex < this.maxSteps);
    }
    return result;
  }
  
  /**
   * Reset the Schedule's Step Index to null|0.
   */
  public void resetStepIndex() {
    this.stepIndex = null;
  }  
  
  /**
   * Check if the schedule is active 
   * @return (this.timeStep != null)
   */
  public boolean isActive() {
    return (this.timeStep != null);
  }

  /**
   * <p>Get the ScheduleStep's current Schedule Date. if (!{@linkplain #isActive() 
   * this.isActive}, a the schedule will be started, return a date on or before now (i.e.
   * a schedule that has no delay).</p>
   * @return this.timeStep.dateTime.
   * @throws IllegalArgumentException if this.timeInterval = null, or initiating 
   * this.timeStep failed.
   */
  public DateTime getTimeStep() {
    TimeStep step = this.onGetCurrentStep(null);
    return timeStep.getDateTime();
  }

  /**
   * <p>Start (re-start) the schedule using the specified date. If ({@linkplain 
   * #isActive() this.isActive} call {@linkplain #restart() this.restart}, to reset the
   * current schedule timeStep and stepIndex. If <tt>startDt</tt>=null, use the current
   * date. The initiate this.timeStep = this.timeInterval.getTimeStep(startDt).</p>
   * @param startDt the date to start the schedule on
   * @return this.timeStep.dateTime.
   * @throws IllegalArgumentException if this.timeInterval = null, or initiating 
   * this.timeStep failed.
   */
  public DateTime getTimeStep(DateTime startDt) {
    if (this.isActive()) {
      this.restart();
    }
    
    TimeStep step = this.onGetCurrentStep(startDt);
    return timeStep.getDateTime();
  }
  
  /**
   * Called to get the Schedule's current timeStep.
   * If (this.timeStep = null), initiate the first timeStep as this.timeStep = 
   * this.timeInterval.getTimeStep(startDt). if <tt>startDt</tt> = null, set startDt =
   * DateTime.getNow(this.timeZone). 
   * <p>
   * <b>NOTE:</b> The currentTimeStep (if initiated) will be on or before 
   * <tt>startDt</tt>.</p>
   * @param startDt the date to start the schedule on
   * @return this.timeStep
   * @throws IllegalArgumentException if this.timeInterval = null, or initiating 
   * this.timeStep failed.
   */
  private TimeStep onGetCurrentStep(DateTime startDt) {
    if (this.timeStep == null) {
      if (this.timeInterval == null) {
        throw new IllegalArgumentException("The Schedule's TimeInterval is not defined.");
      }

      if (startDt == null) {
        startDt = DateTime.getNow(this.timeZone);
      }
      this.timeStep = this.timeInterval.getTimeStep(startDt);
      if (this.timeStep == null) {
        throw new IllegalArgumentException("Initiating the Schedule failed. See server "
                + "log for details.");
      }
    }
    return this.timeStep;
  }
  
  /**
   * Reset the current tiemStep and the setIndex.
   */
  public void restart() {
    this.timeStep = null;
    this.resetStepIndex();
  }

  /**
   * <p>Called to get the Schedule Steps Next Schedule Date.  It gets the curStep =
   * {@linkplain #onGetCurrentStep(bubblewrap.io.datetime.DateTime)
   * this.onGetCurrentStep(null)}, then gets the nextStep = {@linkplain 
   * TimeInterval#getNextStep(bubblewrap.io.schedules.TimeStep) 
   * this.timeInterval.getNextStep(curStep)}, and if successfully initiated, set 
   * this.timeStep = nextStep.
   * @return this.timeStep.getDateTime().
   * @throws IllegalArgumentException if this.timeInterval = null, or initiating 
   * the nextStep failed.
   */
  public DateTime getNextStep() {
    TimeStep curStep = this.onGetCurrentStep(null);
    TimeStep nextStep = this.timeInterval.getNextStep(curStep);
    if (nextStep == null) {
      throw new IllegalArgumentException("Initiating the Schedule's next scheduled step"
              + " failed. See server log for details.");
    }
    this.timeStep = nextStep;
    return this.timeStep.getDateTime();
  }
  
  /**
   * Get the delay until this schedule is ready to execute.
   * @return the delay in milliseconds, 0 when ready to execute, and Long.MAX_VALUE if 
   * (!this.isActive).
   */
  public long getDelay() {
    long result = Long.MAX_VALUE;
    if (this.timeStep != null) {
      long stepTime = this.timeStep.getMilliSeconds();
      DateTime now = DateTime.getNow(this.timeZone);
      long nowTime = now.getTotalMilliseconds();
      result = Math.max(0, (stepTime - nowTime));
    }
    return result;
  }
//  
//  /**
//   * Add increment the Date with this Time Step
//   * @param pCurDt the date to increment
//   * @return the new date (or null if (pCurDt=null)
//   */
//  public DateTime addToDate(DateTime pCurDt) {
//    return (pCurDt == null)? null:
//            pCurDt.addTime(this.getTimeStep(), this.timeUnit);
//  }
//
//  /**
//   * Increment a current Time (in the specified units by adding this TiemStep
//   * @param lTime the current time
//   * @param eUnits the TimeUnit of the current time
//   * @return the new time in the specified TimeUnit
//   */
//  public long addToTime(long lTime, TimeUnit eUnits) {
//    long lStep = this.getTimeStep();
//    return lTime + eUnits.convert(lStep, this.timeUnit);
//  }  
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "Schedule[" + this.timeInterval.toString +
   * "; MaxStep=" + this.maxSteps + "]"</p>
   */
  @Override
  public String toString() {
    String result = "Schedule[";
    result += (this.timeInterval == null)? "-": this.timeInterval.toString();
    result += "; MaxSteps=" + ((this.maxSteps == null)? "-": this.maxSteps.toString());
    result += "]";
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a clone of this instance with the same timeInteval, timeZone, 
   * and maxSteps settings.  The stepIndex and timeStep are not copied.</p>
   */
  @Override
  public TimeSchedule clone() {
    TimeSchedule result = new TimeSchedule(this.timeInterval.clone());
    result.maxSteps = this.maxSteps;
    result.timeZone = this.timeZone;
    return result;
  }

  /**
   * Return a clone of this instance with the same timeInteval, timeZone, maxSteps,
   * stepIndex, and timeStep settings. 
   * @return cloned instance
   */
  public TimeSchedule deepClone() {
    TimeSchedule result = new TimeSchedule(this.timeInterval.clone());
    result.maxSteps = this.maxSteps;
    result.timeZone = this.timeZone;
    result.stepIndex = this.stepIndex;
    result.timeStep = this.timeStep.clone();
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc} <p>OVERRIDE: Compare the delays of this and the other Schedule. 
   * If (other=null), return=-1</p>
   */
  @Override
  public int compareTo(TimeSchedule other){
    int result = 0;
    if (other == null) {
      result = -1;
    } else {
      long delay1 = this.getDelay();
      long delay2 = other.getDelay();
      if (delay1 > delay2) {
        result = 1;
      } else if (delay1 < delay2) {
        result = -1;
      }
    }
    return result;
  }
  
  /**
   * Compare this.timeInterval to other.timeInterval and return result
   * @param other the other scheduler
   * @return return -1 if other = null; 0 if the timeInterval of this and other = null;
   * 1 of this.timeInterval = null; -1 if other.timeInterval = null; else returns
   * this.timeInterval.compareTo(other.timeInterval).
   */
  public int compareIntervalTo(TimeSchedule other){
    int result = 0;
    TimeInterval thisInterval = this.getTimeInterval();
    TimeInterval otherInterval = (other == null)? null: other.getTimeInterval();
    if ((thisInterval != null) && (otherInterval != null)) {
      if (thisInterval == null) {
        result = 1;
      } else if (otherInterval == null) {
        result = -1;
      } else {        
        result = thisInterval.compareTo(otherInterval);
      }
    }
    return result;
  }
  //</editor-fold>
}
