package bubblewrap.io.timeseries;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeInterval;
import bubblewrap.io.schedules.TimeStep;
import bubblewrap.io.schedules.enums.Interval;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.*;

/**
 * This is the base TimeSeries class which contains and maintains a set of {@linkplain
 * TimeStepValue TimeStepValues} between a {@linkplain TimeStep startStep} and a endStep
 * as a defined {@linkplain TimeInterval timeInterval}. The TimeSeries's start- and
 * endStep can be dynamically changed during runtime, but it timeInterval is uniquely
 * defined for an TimeSeries instance and can only be initiated via the Constructor.
 * <p><b>Defining the StartDate/EndDate:</b> The Start or End Date can be assigned via
 * the {@linkplain #TimeSeries(bubblewrap.io.timeseries.TimeInterval,
 * bubblewrap.io.datetime.DateTime, bubblewrap.io.datetime.DateTime) Constructor} or
 * during runtime by calling the {@linkplain #onSetTimeSeries(
 * bubblewrap.io.datetime.DateTime, bubblewrap.io.datetime.DateTime)
 * onSetTimeSeries(startDt,endDt)} method. The assigned Start Date will be converted to
 * Millisecond and then to the defined TimeStep units, which truncates the start and end
 * times to the lower value of the specified TimeUnits. For example, if the TimeUnits is
 * HOURS and Start Date = "2012/12/2012 12:15:44.9999" the start date after conversion
 * will be "2012/12/2012 12:00:00.0000". The TimeSeries' EndDate will be calculated to
 * include all timeStep - starting at the StartDt in the set TimeInterval up to (or
 * before) the specified endDate.</p>
 * @author kprins
 */
@XmlRootElement(name="TimeSeries")
public abstract class TimeSeries<TValue extends TimeStepValue>
                                        implements Serializable, Iterable<TValue> {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
                                      Logger.getLogger(TimeSeries.class.getName());
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Time Series TimeValues
   */
  @XmlElementWrapper(name="TimeValues")
  @XmlElement(name="timeValue")
  private List<TValue> timeValues;
  /**
   * The Time Series Start DateTime
   */
  @XmlElement
  private TimeStep startStep;
  /**
   * The Time Series End DateTime
   */
  @XmlElement
  private TimeStep endStep;
  /**
   * The TimeStep Intervals
   */
  @XmlElement
  private TimeInterval timeInterval;
  /**
   * Placeholder for the TimeSeries TimeZone (Default=null|{@linkplain
   * TimeZone#getDefault()}
   */
  private TimeZone timeZone;
  /**
   * A Transient counter use for managing the TimeSeries' isUpdating state
   */
  @XmlTransient
  private transient int updatingCount = 0;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * The EventHandler that fires the TimeSeries Changed Event.
   */
  public final EventHandler TimeSeriesChanged;

  /**
   * Called to fire the TimeSeries Changed Event.
   */
  protected final void fireTimeSeriesChanged() {
    if (!this.isUpdating()) {
      this.TimeSeriesChanged.fireEvent(this, new EventArgs());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor for serializing
   */
  protected TimeSeries() {
    super();
    this.TimeSeriesChanged = new EventHandler();
    this.timeValues = null;
    this.startStep = null;
    this.endStep = null;
    this.timeInterval = null;
  }

  /**
   * Public Constructor with a defined Timestep interval and TimeUnit
   * @param interval the time interval (must be greater or equal to zero (must &gt; 0 -
   * set to one (1) if &le; 0)
   * @param timeUnit the TimeUnit (MILLISECONDS or greater)
   * @param shiftTime the time interval shift from the Interval time
   * (default = 0)
   * @param shiftTimeUnit the timeUnit of the shiftTiem (default = this.timeUnit)
   */
  protected TimeSeries(Interval interval, long tickCount,
                                                Long shiftTime, TimeUnit shiftTimeUnit) {
    this();
    this.onSetTimeInterval(interval, tickCount, shiftTime, shiftTimeUnit);
  }

  /**
   * Public Constructor with a defined TimeInterval for managing the TimeSeries'
   * TimeSteps.
   * @param timeInterval the TimeInterval for managing the TimeSeries' TimeSteps.
   */
  protected TimeSeries(TimeInterval timeInterval) {
    this();
    this.onSetTimeInterval(timeInterval);
  }

  /**
   * <p>Public Constructor with a defined TimeStep interval and TimeUnit and start
   * and/or end DateTime of the series. After initiating the TimeSeries' Interval ad
   * TimeUnit, it calls {@linkplain #onSetTimeSeries(myapp.io.DateTime,
   * myapp.io.DateTime) onSetTimeSeries} to initiate the TimeSeries for the specified
   * pStartDt and pEndDt.</p>
   * @param timeInterval the TimeInterval for managing the TimeSeries' TimeSteps.
   * @param startDt the Time series start Date/Time (can be null)
   * @param endDt the Time series end Date/Time (can be null)
   */
  protected TimeSeries(TimeInterval timeInterval, DateTime startDt, DateTime endDt) {
    this();
    this.onSetTimeInterval(timeInterval);
    this.onSetTimeSeries(startDt, endDt);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call supper method before calling {@linkplain #resetTimeSeries()
   * this.resetTimeSeries}</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.TimeSeriesChanged.clear();
    this.resetTimeSeries();
    this.timeInterval = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Methods for XML Serialization">
  /**
   * Get the TimeZoneId (for XML serializing)
   * @return the current timeZone's ID (or null if this.timeZone=null)
   */
  @XmlAttribute(name="timeZone")
  private String getTimeZoneId() {
    return (this.timeZone == null)? null: this.timeZone.getID();
  }
  /**
   * Get the TimeZoneId (for XML serializing). Initiate the time zone using the ID
   * @param zoneId the time zone ID (can be null)
   */
  private void setTimeZoneId(String zoneId) {
    zoneId = DataEntry.cleanString(zoneId);
    this.timeZone = (zoneId == null)? null: TimeZone.getTimeZone(zoneId);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Protected/Private Methods">
  /**
   * Called by the Constructor (or other Instance Initiation Methods ) to initiate the
   * the TimeSeries TimeStep Interval and TimeUnit.
   * @param interval the time interval (must be greater or equal to zero (must &gt; 0 -
   * set to one (1) if &le; 0)
   * @param timeUnit the TimeUnit (MILLISECONDS or greater)
   * @param shiftTime the time interval shift from the Interval time
   * (default = 0)
   * @param shiftTimeUnit the timeUnit of the shiftTiem (default = this.timeUnit)
   * @exception IllegalArgumentException if the Interval less or equal to zero or the
   * TimeUnit is undefined or smaller than MilliSeconds (the smallest TimeUnit for
   * managing DateTime values).
   */
  private void onSetTimeInterval(Interval interval, long tickCount,
                                                Long shiftTime, TimeUnit shiftTimeUnit) {
    TimeInterval newInterval =
                         new TimeInterval(interval, tickCount, shiftTime, shiftTimeUnit);
    if (newInterval == null) {
      throw new IllegalArgumentException("The TimeSeries' Interval is invalid.");
    }
    this.timeInterval = newInterval;
  }

  /**
   * Called by the Constructor (or other Instance Initiation Methods ) to initiate the
   * the TimeSeries TimeStep Interval.
   * @param pInterval a TiemInetrval
   * @exception NullPointerException if pInterval=null.
   */
  private void onSetTimeInterval(TimeInterval pInterval) {
    if (pInterval == null) {
      throw new NullPointerException("The TimeSeries' Interval cannot be unassigned");
    }
    this.timeInterval = pInterval;
  }

  /**
   * Set the TimeSeries TimeZone (Default = {@linkplain TimeZone#getDefault()})
   * @param pTimeZone the new TimeZone (can be null to use default).
   */
  protected void setTimeZone(TimeZone pTimeZone) {
    this.timeZone = pTimeZone;
  }

  /**
   * <p>Called by the Constructor to set the Start and End TimeSteps and to initiate the
   * TimeSeries if applicable. It handles the request as follows:</p><ul>
   * <li><b>If (pStartDt=null) and (pEndDt==null):</b> return unhandled.</li>
   * <li><b>If (pStartDt=null) and (pEndDt!=null):</b> set pStartDt=pEndDt; pEndDt=null
   * </li>
   * <li><b>If (pStartDt!=null) and (pEndDt!=null) and (pStartDt.isAfter(pEndDt):</b>
   *  switch the dates.</li>
   * <li><b>Initiate StartStep:</b> Call {@linkplain #setStartTime(myapp.io.DateTime)
   *    setStartTime} to set the TimeSteries Start TimeStep.</li>
   * <li>Set lEndStep = (pEndDt==null)? StartStep: Call {@linkplain #toTimeStep(
   *    myapp.io.DateTime) toTimeStep} to convert pEndDt to a TimeStep.
   *  <li>Call {@linkplain #onSetTimeSeries(long, long) onSetTimeSeries} to initiate
   *    the TimeSeries.</li>
   * </ul>
   * @param startDt the TimeSeries' Start DateTime (can be null).
   * @param endDt the TimeSeries' End DateTime (can be null).
   */
  protected final void onSetTimeSeries(DateTime startDt, DateTime endDt) {
    try {
      if ((startDt == null) && (endDt == null)) {
        return;
      }

      if (this.timeInterval == null) {
        throw new Exception("The TimeSeries cannot be initiated, because its "
                + "TimeInterval is not assigned.");
      }

      if ((startDt == null) && (endDt != null)) {
        startDt = endDt;
        endDt = null;
      } else if ((startDt != null) && (endDt != null) && (startDt.isAfter(endDt))) {
        DateTime tempDt = startDt;
        startDt = endDt;
        endDt = tempDt;
      }

      /**
       * Initiate the StartTime
      * Set the TimeSeries' StartTime. Ignored if the StartTime is already set or if
      * (dateTime=null). Else, calls the {@linkplain #toTimeStep(myapp.io.DateTime)
      * toTimeStep} method to convert the specified dateTime to TimeStep units.
       */
      
      TimeStep startStp = this.timeInterval.getTimeStep(startDt);
      if (startStp == null) {
        throw new Exception("Initiating the StartSetp form Date '" + startDt.toString()
                         + "' failed.");
      }

      /**
       * If endDt is not set, set lEndStep=StartStep, else convert endDt to a TimeStep
       * and call Overload 2 to initiate the series and set the EndStep.
       */
      TimeStep endStp = (endDt == null)? null: this.timeInterval.getTimeStep(endDt);
      if ((endDt == null) && (startStp != null)) {
        endStp = startStp.clone();
      }

      if ((!DataEntry.isEq(this.startStep, startStp)) ||
                                              (!DataEntry.isEq(this.endStep, endStp))) {
        this.onSetTimeSeries(startStp, endStp);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onSetTimeSeries Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }

  /**
   * <p>Called to initiate or expand the TimeSeries by adding new values to the series
   * start or end depending on the following:</p><ul>
   *  <li><b>if (lStartStep > lEndStep):</b> - switch the values.</li>
   *  <li><b>if the TimeSeries' StartStep is unassigned:</b> - set lStartStep as the
   *    TimeSeries' StartStep.</li>
   *  <li>Initiate bPrePend=(lStartStep is less than this.StartStep)</li>
   *  <li>Initiate bAppend and this.EndStep as follows:<ul>
   *    <li>If (lEndStep is greater than this.StartStep): set bAppend=true if
   *      this.EndStep=null or less than lEndStep</li>
   *    <li>If (pPrepend) and (this.EndStep=null): set pEndStep=this.StartStep and
   *      set bAppend=false.</li>
   *  </ul></li>
   *  <li><b>If (bPrepend or bAppend):</b> add new TimeValue to the start and end of
   *    the series for each time step.</li>
   *  <li><b>Finally:</b>If (!this.isUpdating) and (!this.values.isEmpty) and
   *    (bPrepend or bAppend) - Call {@linkplain #validate()) to sort and validate the
   *    the list to ensure that the values are defined and in an increasing Time order.
   *  </li>
   * </ul>
   * @param startStep the TimeSerie's new Start TimeStep
   * @param endStep the TimeSerie's new End TimeStep
   */
  protected final void onSetTimeSeries(TimeStep startStep, TimeStep endStep) {
    boolean doPrepend = false;
    boolean doAppend = false;
    if (((startStep == null) && (endStep == null))
              || (this.timeInterval == null)) {
      return;
    }

    try {
      if ((startStep == null) && (endStep != null)) {
        startStep = endStep;
        endStep = null;
      } else if ((startStep != null) && (endStep != null)
              && (startStep.getTime() > endStep.getTime())) {
        TimeStep tempStep = startStep;
        startStep = endStep;
        endStep = tempStep;
      }

      if ((this.startStep != null) && (this.startStep.isGreaterThan(endStep))) {
        this.resetTimeSeries();
      }

      if (this.startStep == null) {
        this.startStep = startStep;
      }

      doPrepend = (startStep.getTime() < this.startStep.getTime());
      if ((endStep != null) && (endStep.getTime() >= this.startStep.getTime())) {
        if (this.endStep == null) {
          this.endStep = this.startStep.clone();
          doAppend = true;
        } else {
          doAppend = (this.endStep.getTime() < endStep.getTime());
        }
      } else if ((doPrepend) && (this.endStep == null)) {
        this.endStep = this.startStep.clone();
      }

      if (((doPrepend) || (doAppend)) && (this.timeValues == null)) {
        this.timeValues = new ArrayList<>();
        TValue timeValue = this.onNewTimeValue(this.startStep);
        this.timeValues.add(timeValue);
        doAppend = (!this.startStep.equals(endStep));
      }

      if (doPrepend) {
        long startTime = startStep.getTime();
        while (this.startStep.getTime() > startTime) {
          TimeStep timeStep = this.timeInterval.getPriorStep(this.startStep);
          TValue timeValue = this.onNewTimeValue(timeStep);
          this.timeValues.add(0,timeValue);
          this.startStep = timeStep;
        }
      }

      if (doAppend) {
        long endTime = endStep.getTime();
        while (this.endStep.getTime() < endTime) {
          TimeStep timeStep = this.timeInterval.getNextStep(this.endStep);
          TValue timeValue = this.onNewTimeValue(timeStep);
          this.timeValues.add(timeValue);
          this.endStep = timeStep;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onSetTimeSeries Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.validate();
    }
  }

  /**
   * Called to check if <tt>timeStep</tt> is within the range of the current TiemSeries and
   * if not to grow the TimeSeries to include the timeStep. It determines if
   * <tt>timeStep</tt> if before this.startStep or after this.endStep and then calls
   * {@linkplain #onSetTimeSeries(bubblewrap.io.schedules.TimeStep,
   * bubblewrap.io.schedules.TimeStep) this.onSetTimeSeries} to resize the TimeSeries.
   * <p>It call this.startUpdating and this.endUpdating if the range change.
   * @param timeStep the timeStep
   */
  private void onGrowTimeSeries(TimeStep timeStep) {
    if ((timeStep == null) ||
        ((this.startStep != null) && (this.endStep != null) &&
         (this.startStep.isLessThan(timeStep)) &&
          (this.endStep.isGreaterThan(timeStep)))) {
      return;
    }

    if (this.startStep == null) {
      this.resetTimeSeries();
      this.onSetTimeSeries(timeStep, timeStep);
    } else if (timeStep.isLessThan(this.startStep)) {
      this.onSetTimeSeries(timeStep, this.endStep);
    } else if ((this.endStep == null) || (timeStep.isGreaterThan(this.endStep))) {
      this.onSetTimeSeries(this.startStep, timeStep);
    }
  }

  /**
   * <p>Convert dateTime to a TimeSeries TimeStep.
   * @param dateTime the DateTime to convert
   * @return this.timeInterval.getTimeStep(dateTime).
   * @exception IllegalArgumentException if dateTime or this.tiemInetrval is undefined.
   */
  protected final TimeStep toTimeStep(DateTime dateTime) {
    TimeStep result = null;
    if (dateTime == null) {
      throw new IllegalArgumentException("The DateTime cannot be unassigned.");
    }

    if (this.timeInterval == null) {
      throw new IllegalArgumentException("The TimeSeries' if not properly initiated; The "
              + "TimeInterval is unassigned.");
    }

    result = this.timeInterval.getTimeStep(dateTime);
    return result;
  }

  /**
   * <p>Convert timeStep to an equivalent TimeSeries TimeStep.
   * @param dateTime the DateTime to convert
   * @return this.toTimeStep(timeStep.getDateTime());.
   * @exception IllegalArgumentException if dateTime or this.tiemInetrval is undefined.
   */
  protected final TimeStep toTimeStep(TimeStep timeStep) {
    if (timeStep == null) {
      throw new IllegalArgumentException("The TiemStep cannot be unassigned.");
    }
    return this.toTimeStep(timeStep.getDateTime());
  }

  /**
   * Get whether the Index is in the range of the defined TimeValues.
   * @param lIndex the index of interest (can be negative)
   * @return true if this.value!=null and (!this.value.isEmpty) and (lIndex greater or
   * equal to Zero) and (lIndex less than this.values.size).
   */
  protected final boolean isIndexInRange(int lIndex) {
    boolean result = false;
    if ((this.timeValues != null) && (!this.timeValues.isEmpty())) {
      result = ((lIndex >= 0) && (lIndex < this.timeValues.size()));
    }
    return result;
  }

  /**
   * Called internally to locate the TValue within this.timeValues with a matching
   * timeStep.
   * <p>
   * <b>NOTE:</b> The <tt>timeStep</tt> must be a time step of this.interval. Call
   * {@linkplain #toTimeStep(bubblewrap.io.datetime.DateTime) this.toTimeStep(date)}
   * or {@linkplain #toTimeStep(bubblewrap.io.schedules.TimeStep)
   * this.toTimeStep(timeStep)} to convert a specified date or timeStep to a
   * this.interval TimeStep.</p>
   * @param timeStep the TimeSTep to match
   * @return returns -1 if not found or a value &ge; 0 if found.
   */
  private int onGetStepIndex(TimeStep timeStep) {
    int result = -1;
    if ((timeStep != null) && (!this.isEmpty())) {
      TimeStep step = null;
      for (int iStep = 0; iStep < this.timeValues.size(); iStep++) {
        TValue value = this.timeValues.get(iStep);
        if ((value != null) && ((step = value.getTimeStep()) != null) &&
                (step.equals(timeStep))) {
          result = iStep;
          break;
        }
      }
    }
    return result;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Managing the Updating State">
  /**
   * Get the TimeSeries' isUpdatingState
   * @return true if (this.updatingCount > 0)
   */
  public final boolean isUpdating() {
    return (this.updatingCount > 0);
  }

  /**
   * Call to start the Updating of the TimeSeries. It increments this.updatingCount with
   * each call. Must be called in pair with this.endUpdating.
   */
  public final void startUpdating() {
    this.updatingCount = (this.updatingCount < 0)? 0: this.updatingCount;
    this.updatingCount++;
  }

  /**
   * Call to end the Updating of the TimeSeries. It decrement this.updatingCount with
   * each call. When the count equals zero, it call {@linkplain #validate()} to sort and
   * validate the time series values. Finally, it calls {@linkplain #onEndUpdating()
   * this.onEndUpdating} to custom handle the completion of the update process.
   */
  public final void endUpdating() {
    if (this.updatingCount > 0) {
      this.updatingCount--;
    }
    if (this.updatingCount == 0) {
      this.validate();
      try {
        this.onEndUpdating();
      } catch (Exception exp) {
      }
      this.fireTimeSeriesChanged();
    }
  }

  /**
   * Called to sort and validate the TimeSeries Values. It check that each value in the
   * TimeSeries is initiated ad sort the values. It also removes any TimeStep value
   * before or after this.startStep and the.endStep, respectively.
   * It calls {@linkplain #onValidate()} to handle any custom validation of the series.
   * <p>This process is skipped while this.isUpdating
   * <p>
   * <b>NOTE:</b> This process is automatically call by {@linkplain #endUpdating()
   * this.endUpdating} when {@linkplain #isUpdating() this.isUpdating} turns false and
   * BEFORE the {@linkplain #TimeSeriesChanged TimeSeriesChanged} event is fired.</p>
   */
  public final void validate() {
    if (this.isUpdating()) {
      return;
    }else if ((this.startStep == null) || (this.timeInterval == null)) {
      this.endStep = null;
      return;
    }

    try {
      if (this.endStep == null) {
        this.onSetTimeSeries(this.startStep, this.startStep);
      }

      if ((this.timeValues == null) || (this.endStep == null)) {
        throw new Exception("Initiating the TimeSeries failed.");
      }

      if (this.timeValues.size() > 1) {
        Collections.sort(this.timeValues, new TimeSeriesComparator());
      }

      TimeStep timeStep = null;
      TValue timeValue = null;
      while ((!this.timeValues.isEmpty()) &&
              ((timeValue = this.timeValues.get(0)) == null) ||
              ((timeStep = timeValue.getTimeStep()) == null) ||
              (this.startStep.isGreaterThan(timeStep))) {
        this.timeValues.remove(0);
      }

      while ((!this.timeValues.isEmpty()) &&
              ((timeValue = this.timeValues.get(this.timeValues.size() - 1)) == null) ||
              ((timeStep = timeValue.getTimeStep()) == null) ||
              (this.endStep.isLessThan(timeStep))) {
        this.timeValues.remove(this.timeValues.size() - 1);
      }

      /* Call onValidate for custom handling of the request. */
      this.onValidate();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.validate Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the TimeSeries TimeZone (Default = {@linkplain TimeZone#getDefault()})
   * @return the assigned Value (can be null).
   */
  public TimeZone getTimeZone() {
    return timeZone;
  }

  /**
   * Convert the specified DateTime its TimeStep Equivalent. It first convert the
   * DateTime to a TimeStep by calling {@linkplain #toTimeStep(myapp.io.DateTime)
   * toTimeStep} and the convert the TimeStep back to a DateTime by calling {@linkplain
   * #toDateTime(long) toDateTime}.
   * @param dateTime the Date/Time of interest
   * @return A DateTime that represents a TimeSeries TimeStep (in the TimeSeries
   * TimeZone)
   */
  public DateTime toTimeStepDateTime(DateTime dateTime) {
    DateTime result = null;
    if (dateTime != null) {
      if (this.timeInterval != null) {
        throw new IllegalArgumentException("The TimeSeries' TimeInterval is not "
                + "assigned.");
      }
      TimeStep timeStep = this.timeInterval.getTimeStep(dateTime);
      result = timeStep.getDateTime();
    }
    return result;
  }

  /**
   * Get the TimeSeries Start TimeStep
   * @return the assigned TimeStep or null if undefined.
   */
  public TimeStep getStartStep() {
    return this.startStep;
  }

  /**
   * Get the TimeSeries StartTime. Convert the start TimeStep to a DateTime by calling
   * the {@linkplain #toDateTime(long) toDateTime} method.
   * @return return the StartTime or null if undefined.
   */
  @XmlTransient
  public DateTime getStartTime() {
    return (this.startStep == null)? null: this.startStep.getDateTime();
  }

  /**
   * Get the TimeSeries EndTime. Convert the End TimeStep to a DateTime by calling
   * the {@linkplain #toDateTime(long) toDateTime} method.
   * @return return the EndTime or null if undefined.
   */
  @XmlTransient
  public DateTime getEndTime() {
    return (this.endStep == null)? null: this.endStep.getDateTime();
  }

  /**
   * Get the TimeSeries End TimeStep
   * @return the assigned TimeStep or null if undefined.
   */
  public TimeStep getEndStep() {
    return this.endStep;
  }

  /**
   * Get the TimeSeries' TimeInterval
   * @return the assigned Interval
   */
  public TimeInterval getTimeInterval() {
    return this.timeInterval;
  }

  /**
   * Get the TimeSeries' TimeInterval's TimeUnit
   * @return the assigned this.timeInterval.timeUnit or null if this.timeInterval=null
   */
  @XmlTransient
  public TimeUnit getTimeUnit() {
    return (this.timeInterval == null)? null: this.timeInterval.getTimeUnit();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Manage TimeValues">
  /**
   * Overload 1: Get the TimeValue for the Specified DateTime. It calls {@linkplain
   * #getStepIndex(bubblewrap.io.datetime.DateTime) this.getStepIndex} to get the
   * Index of the TimeStep and then  retrieve the TimeValue using the Index.
   * @param dateTime the Date/Time value of interest
   * @return the matching TimeValue if in the TimeSeries Range or null is not in range.
   */
  public final TValue getTimeValue(DateTime dateTime) {
    TValue result = null;
    int index = -1;
    if ((dateTime != null) &&
                  ((index = this.getStepIndex(dateTime)) >= 0)) {
      result = this.timeValues.get(index);
    }
    return result;
  }

  /**
   * Overload 2: Get the TimeValue for the Specified TimeStep. It calls {@linkplain
   * #getStepIndex(bubblewrap.io.schedules.TimeStep) this.getStepIndex} to get the
   * Index of the TimeStep and then  retrieve the TimeValue using the Index.
   * @param timeStep the TimeStep of Interest
   * @return the matching TimeValue if in the TimeSeries Range or null is not in range.
   */
  public final TValue getTimeValue(TimeStep timeStep) {
    TValue result = null;
    int index = -1;
    if ((timeStep != null) &&
                  ((index = this.getStepIndex(timeStep)) >= 0)) {
      result = this.timeValues.get(index);
    }
    return result;
  }

  /**
   * Overload 1: Called to get the TimeValue index of the specified date. It calls
   * {@linkplain #toTimeStep(bubblewrap.io.datetime.DateTime) this.toTimeStep} to
   * convert it to a TimeStep of this.timeInterval and the calls {@linkplain
   * #onGetStepIndex(bubblewrap.io.schedules.TimeStep) this.onGetStepIndex} to locate
   * the index.
   * @param date the DateTime to search for
   * @return -1 if not found or &ge; 0 if found.
   */
  public final int getStepIndex(DateTime date) {
    int result = -1;
    TimeStep localStep = null;
    if ((date != null) && ((localStep = this.toTimeStep(date)) != null)){
      result = this.onGetStepIndex(localStep);
    }
    return result;
  }

  /**
   * Overload 2: Called to get the TimeValue index of the specified timeStep. It calls
   * {@linkplain #toTimeStep(bubblewrap.io.schedules.TimeStep) this.toTimeStep} to
   * convert it to a TimeStep of this.timeInterval and the calls {@linkplain
   * #onGetStepIndex(bubblewrap.io.schedules.TimeStep) this.onGetStepIndex} to locate
   * the index.
   * @param timeStep the TimeStep to search for
   * @return -1 if not found or &ge; 0 if found.
   */
  public final int getStepIndex(TimeStep timeStep) {
    int result = -1;
    TimeStep localStep = null;
    if ((timeStep != null) && ((localStep = this.toTimeStep(timeStep)) != null)){
      result = this.onGetStepIndex(localStep);
    }
    return result;
  }

  /**
   * Overload 2: Get the TimeValue for the Specified TimeValue Index. It retrieves the
   * TimeValue for the specified Index is iIndex is in Range[0..this.size-1].
   * @param index the TimeValue's Index
   * @return the TimeValue[iIndex] or null is not in range.
   */
  public final TValue getTimeValue(int index) {
    TValue result = null;
    if (this.isIndexInRange(index)) {
      result = this.timeValues.get(index);
    }
    return result;
  }

  /**
   * <p>Assign the values of the Source TimeStepValue to its matching TimeStepValue in
   * this TimeSeries instance. If the Source TimeStepValue's TimeStep is out of the
   * current TimeSeries range, it will add a new TimeStepValue for the Source's
   * TimeStep, which will expand the imeSeries rang and then assign the Source's values
   * to the new value. Otherwise, it will locate the matching in-range TimeStepValue
   * and if found, it will assign the Source's values to the matching value. It calls
   * the {@linkplain TimeStepValue#assignTo(myapp.io.timeseries.TimeValue)
   * TimeStepValue.assignTo} to transfer the TimeStepValue values.</p>
   * <p><b>NOTE:</b> This call will have no effect if the matching in-range
   * TimeStepValue could not be retrieved (i.e., this.getTimeValue(lTimeStep) returns
   * null) or is (sourceValue=null).</p>
   * @param sourceValue the Source TimeStepValue of type TValue
   */
  public final void assignTimeValue(TValue sourceValue) {
    if (sourceValue == null) {
      return;
    }
    try {
      this.startUpdating();
      DateTime srcDt = sourceValue.getDateTime();
      TimeStep trgStep = this.toTimeStep(srcDt);
      TValue trgValue = this.getTimeValue(trgStep);
      if (trgValue == null) {
        this.onGrowTimeSeries(trgStep);
        trgValue = this.getTimeValue(trgStep);
        if (trgValue == null) {
          throw new Exception("Unable to retrieve TimeStep[" + trgStep.toString()
                          + "] after growing the TimeSeries to include the step. "
                          + "See server log for protential errors.");
        }
        sourceValue.assignTo(trgValue);
      } else {
        sourceValue.assignTo(trgValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.assignTimeValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endUpdating();
    }
  }

  /**
   * Overload 1: Get a the current TimeValue or initiate a new TimeValue for the
   * specified DateTime. It first convert the DateTime to a TimeStep by calling
   * {@linkplain #toTimeStep(myapp.io.DateTime) toTimeStep}. It then call the
   * {@linkplain #newTimeValue(long) Overload 2} to initiate the return value.
   * @param dateTime the DateTime for the TimeValue
   * @return an existing TimeValue (if previously initiated) or a new TimeValue
   * instance after adding it to the series.
   */
  public final TValue newTimeValue(DateTime dateTime) throws Exception {
    TValue result = null;
    if (dateTime == null) {
      throw new Exception("The New TimeValue's DateTime cannot be "
              + "unassigned.");
    }
    TimeStep timeStep = this.toTimeStep(dateTime);
    result = this.getTimeValue(timeStep);
    if (result == null) {
      try {
        this.onGrowTimeSeries(timeStep);
        result = this.getTimeValue(timeStep);
        if (result == null) {
          throw new Exception("Unable to retrieve TimeStep[" + timeStep.toString()
                          + "] after growing the TimeSeries to include the step. "
                          + "See server log for protential errors.");
        }
      } catch (Exception exp) {
        throw new Exception(this.getClass().getSimpleName()
                + ".methodName Error:\n " + exp.getMessage());
      } finally {
        this.endUpdating();
      }
    }
    return result;
  }

  /**
   * Overload 1: Check if the DateTime is the Range of the TimeSeries. it calls
   * {@linkplain #toTimeStep(myapp.io.DateTime) toTimeStep} to convert the DateTime to
   * a TimeStep and call {@linkplain #isInRange(long) Overload 2} to check the range.
   * @param dateTime the DateTime to check.
   * @return true if TimeSeries index of the DateTime is in the TimeSeries Range.
   */
  public final boolean isInRange(DateTime dateTime) {
    TimeStep lTimeStep = this.toTimeStep(dateTime);
    return this.isInRange(lTimeStep);
  }

  /**
   * Overload 2: Check if the TimeStep is the Range of the TimeSeries. Get the
   * {@linkplain #getStepIndex(long) this.stepIndex} for lTimeStep) and check if
   * {@linkplain #isIndexInRange(long) Index is In Range}.
   * @param timeStep the TimeStep to check for.
   * @return true if index of the TimeStep is in the TimeSeries Range.
   */
  public final boolean isInRange(TimeStep timeStep) {
    return (this.getTimeValue(timeStep) != null);
  }

  /**
   * Get the TimeSeries isEmpty state.
   * @return true is the TimeSeries has no TimeValues
   */
  public final boolean isEmpty() {
    return ((this.timeValues == null) || (this.timeValues.isEmpty()));
  }

  /**
   * Get the Current Size (number of TimeSteps) in the TimeSeries.
   * @return the size of the Values list or 0 if the Values list is undefined.
   */
  public final int size() {
    return (this.timeValues == null)? 0: this.timeValues.size();
  }

  /**
   * Called to clear the TimeSeries values - without deleting the timeSteps. It called
   * each timeValue's {@linkplain TimeStepValue#.}. It also call {@link #onClearValues()} for
   * custom handling of the request.
   * <p>The this.isUpdating flag is set while clearing the values, and will be reset on
   * completing all resets (which could trigger a validation)
   */
  public final void clearValues() {
    if ((this.timeValues == null) || (this.timeValues.isEmpty())) {
      return;
    }
    try {
      this.startUpdating();

      for (TValue timeValue : this) {
        timeValue.reset();
      }

      this.onClearValues();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endUpdating();
    }
  }

  /**
   * Called by {@linkplain #assignTo(gov.ca.water.common.io.timeseries.TimeSeries)
   * this.assignTo} to reset all timeseries properties - clearing all TimeSetValues
   */
  protected final void resetTimeSeries() {
    try {
      this.startUpdating();
      if (this.timeValues != null) {
        this.timeValues.clear();
        this.timeValues = null;
      }
      this.startStep = null;
      this.endStep = null;
      this.onResetTimeSeries();
    } catch (Exception exp) {
    } finally {
      this.endUpdating();
    }
  }

  /**
   * ABSTRACT: Called by {@linkplain #resetTimeSeries() this.resetTimeSeries} to
   * reset all TimeSeries settings. The base method does nothing.
   */
  protected abstract void onResetTimeSeries();

  /**
   * <p>Overload 1:  Called to remove all TimeValue before a Specified TimeStep. It
   * converts <tt>timeStep</tt> to a date and call {@linkplain #removeBefore(
   * bubblewrap.io.datetime.DateTime) Overload 2}.
   * <p><b>NOTE:</b> If the timeseries range changes,
   * this.Start- and EndUpdating is called before and after the value  List has been
   * updated.</p>
   * @param timeStep the new StartStepTime of the TimeSeries.
   */
  public void removeBefore(TimeStep timeStep) {
    DateTime trgDt = null;
    TimeStep trgStep = null;
    if ((this.isEmpty()) || (timeStep == null) || (this.startStep == null) ||
            ((trgDt = timeStep.getDateTime()) == null)) {
      return;
    }

    this.removeBefore(trgDt);
  }

  /**
   * Overload 2: Call to remove all TimeStep Values before the specified date. It converts
   * <tt>dateTime</tt> to a timeStep (calling {@linkplain #toTimeStep(
   * bubblewrap.io.datetime.DateTime) this.toTimeStep} and if timeStep &lt;
   * this.startStep, if will set this.startStep = timeStep, and call {@linkplain
   * #validate() this.validate}, which will remove the steps before the new startStep.
   * <p>The call is ignored if <tt>dateTime</tt> = null, or this.isEmpty, or timeStep
   * &ge; this.startStep.
   * <p><b>NOTE:</b> If the timeseries range changes,
   * this.Start- and EndUpdating is called before and after the value List has been
   * updated.</p>
   * @param dateTime the DateTime of the new StartStepTime
   */
  public void removeBefore(DateTime dateTime) {
    TimeStep timeStep = this.toTimeStep(dateTime);
    if ((dateTime == null) || ((timeStep = this.toTimeStep(dateTime)) == null) ||
        (this.isEmpty()) || (this.startStep == null) ||
        (!timeStep.isLessThan(this.startStep))) {
      return;
    }

    this.startStep = timeStep;
    this.validate();
  }

  /**
   * <p>Overload 1: Called to remove all TimeValue after a Specified TimeStep. It
   * converts <tt>timeStep</tt> to a date and call {@linkplain #removeAfter(
   * bubblewrap.io.datetime.DateTime) Overload 2}.
   * <p><b>NOTE:</b> If the timeseries range changes,
   * this.Start- and EndUpdating is called before and after the value  List has been
   * updated.</p>
   * @param timeStep the new EndStepTime of the TimeSeries.
   */
  public final void removeAfter(TimeStep timeStep) {
    DateTime trgDt = null;
    TimeStep trgStep = null;
    if ((this.isEmpty()) || (timeStep == null) || (this.endStep == null) ||
            ((trgDt = timeStep.getDateTime()) == null)) {
      return;
    }

    this.removeAfter(trgDt);
  }

  /**
   * Overload 2: Call to remove all TiemStep Values after the specified date. It converts
   * <tt>dateTime</tt> to a timeStep (calling {@linkplain #toTimeStep(
   * bubblewrap.io.datetime.DateTime) this.toTimeStep} and if timeStep &gt; this.endStep,
   * if will set this.endStep = timeStep, and call {@linkplain #validate() this.validate},
   * which will remove the steps after the new endStep.
   * <p>The call is ignored if <tt>dateTime</tt> = null, or this.isEmpty, or timeStep
   * &le; this.endStep.
   * <p><b>NOTE:</b> If the timeseries range changes,
   * this.Start- and EndUpdating is called before and after the value  List has been
   * updated.</p>
   * @param dateTime the DateTime of the new end TimeStep
   */
  public void removeAfter(DateTime dateTime) {
    TimeStep timeStep = this.toTimeStep(dateTime);
    if ((dateTime == null) || ((timeStep = this.toTimeStep(dateTime)) == null) ||
        (this.isEmpty()) || (this.endStep == null) ||
        (!timeStep.isGreaterThan(this.endStep))) {
      return;
    }

    this.endStep = timeStep;
    this.validate();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * <p>MUST OVERRIDE FOR XML SERIALIZATION: The base method returns the currently
   * assigned timeValues. The override must call the base method and must add the
   * following XML Annotations:</p><ul>
   *  <li><b>@{@linkplain XmlElementWrapper}(name="MyTimeValues"):</b></li>
   *  <li><b>@{@linkplain XmlElement}(name="timeValue",type=MyTimeValue.class):</b></li>
   * </ul>
   * @return the TimeSeries' timeValues as an list.
   */
  protected List<TValue> getTimeValues() {
    return this.timeValues;
  }

  /**
   * <p>MUST OVERRIDE FOR XML SERIALIZATION: This method is called during XML
   * deserialization to assign the the deserialized timeValue to the timeSeries. The
   * timeValues are assigned between calls to {@linkplain #startUpdating()} and
   * {@linkplain #endUpdating()} to trigger a call to {@linkplain #validate()}</p>
   * <p><b>NOTE:</b> Inheritors must call the super method, but does not have to do
   * anything else.</p>
   * @param timeValues the deserialized TValue values.
   */
  protected void setTimeValues(List<TValue> timeValues) {
    try {
      this.startUpdating();
      this.timeValues = timeValues;
    } finally {
      this.endUpdating();
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #validate() this.validate} after sorting the
   * TimeSeries Values and add any missing value to allow custom validation of the
   * TimeSeries.
   * @throws Exception can be thrown and will be caught an logged.
   */
  protected void onValidate() throws Exception {
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #endUpdating() this.endUpdating} when the
   * updating of the timeseries is completed and after calling {@linkplain #validate()
   * this.validate} to validate the time series.
   * <p>The base method does nothing.
   * <b>NOTE:</b> This method is called while this.isUpdating = false.</p>
   * @throws Exception can be thrown and will be caught an logged.
   */
  protected void onEndUpdating() throws Exception {
  }

  /**
   * CAN OVERRIDE: Called by {@link #clearValues() this.clear} after clearing the Value
   * list. The base method does nothing.
   * <p>
   * <b>NOTE:</b> This method is called while this.isUpdating = true.</p>
   * @throws Exception can be thrown and will be caught an logged.
   */
  protected void onClearValues() throws Exception {
  }

  /**
   * ABSTRACT: Subclasses must override this method to initiate a new missing value
   * instance of the supported TimeValue.
   * @param timeStep the TimeValue's timestep
   * @return a new TimeValue instance
   */
  protected abstract TValue onNewTimeValue(TimeStep timeStep);
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="TimeSeries Cloning">
  /**
   * <p>Call to assign all properties and values of this instance to pTarget. The
   * process is ignored if pTarget= null</p>
   * <p><b>NOTE:</b> The TimeValue's are cloned during this process.</p>
   * @param <TSeries> extends TimeSeries<TValue>
   * @param target
   */
  @SuppressWarnings("unchecked")
  public <TSeries extends TimeSeries<TValue>> void assignTo(TSeries target) {
    try {
      target.resetTimeSeries();
      TimeSeries<TValue> timeSeries = target;
      timeSeries.timeInterval = this.timeInterval.clone();
      timeSeries.startStep = this.startStep.clone();
      timeSeries.endStep = this.endStep.clone();
      timeSeries.timeZone = this.timeZone;
      timeSeries.updatingCount = 0;
      if ((this.timeValues != null) && (!this.timeValues.isEmpty())) {
        timeSeries.timeValues = new ArrayList<>();
        for (TValue value : this.timeValues) {
          TValue clone = (TValue) ((value == null)? null: value.clone());
          if (clone != null) {
            timeSeries.timeValues.add(clone);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.assignTo Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: It creates and new instance of this class and call
   * {@linkplain #assignTo(myapp.io.timeseries.TimeSeries) this.assignTo) to initiate
   * the field values of the clone before it returns the clone.</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected TimeSeries<TValue> clone() throws CloneNotSupportedException {
    TimeSeries<TValue> result = null;
    try {
      try {
        result = this.getClass().newInstance();
      } catch (InstantiationException | IllegalAccessException pInExp) {
        throw new Exception("Cloning[" + this.getClass().getSimpleName()
                + "] failed.", pInExp);
      }
      this.assignTo(result);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clone Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw new CloneNotSupportedException(pExp.getMessage());
    }
    return result;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return the TiemSeries and its TimeValues as a String.
   * </p>
   */
  @Override
  public String toString() {
    String result = this.getClass().getSimpleName()
            + ":\ntimeInterval = " + this.timeInterval
            + "\nstartStep = " + this.startStep
            + "\nendStep = " + this.endStep
            + "\ntimeZone = " + this.getTimeZoneId();

    String subStr = this.onToString();
    if (subStr != null) {
      result += subStr;
    }

    if ((this.timeValues == null) || (this.timeValues.isEmpty())) {
      result += "\ntimeValues = empty";
    } else {
      result += "\ntimeValues:";
      int iCnt = 0;
      for (TValue value : this) {
        result += "\n- timeValues[" + iCnt + "] = " + value.toString();
        iCnt++;
      }
    }
    return result;
  }

  /**
   * CAN OVERRIDE: call by toString to append additional TimeSeries Field Values before
   * listing the timeValues. The base method return null.
   * @return a string listing the TimeSeries Properties
   */
  protected String onToString() {
    return null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Interator<TValue>">
  /**
   * <p>IMPLEMENT: Return this.mpValues' iterator. if this.mpValues=null, initiate an
   * empty list and return its iterator.</p>
   * @return the TimeSeries' TimeValue iterator. Can be empty.
   */
  @Override
  public Iterator<TValue> iterator() {
   List<TValue> valueList = this.timeValues;
    if (valueList == null) {
      valueList = new ArrayList<>();
    }
    return valueList.iterator();
  }
  //</editor-fold>

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + Objects.hashCode(this.timeValues);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TimeSeries<?> other = (TimeSeries<?>) obj;
    if (!Objects.equals(this.timeValues, other.timeValues)) {
      return false;
    }
    return true;
  }
}
