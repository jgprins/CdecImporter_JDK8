package bubblewrap.io.schedules;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.enums.Interval;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.json.JSONObject;

/**
 * <p>A TimeInterval is used by a TimeSeries to calculate the discreet time intervals
 * of its time Steps. The TimeInterval is defined by an {@linkplain Interval} which
 * defines the time tick if the Interval in a set {@linkplain TimeUnit} and
 * {@linkplain Interval#getTickTime(java.lang.Integer) tickTime} by tickIndices.
 * The TimeInterval also include a <tt>tickCount</tt> parameter (default = 1) specifying
 * how many Interval Time ticks should be added for each TimeInterval tick (e.g., for
 * a 6-hourly TimeInetrval, use {@linkplain Interval#HOURS} with tickCount=6).
 * <p>It also supports an optional TimeShift - defined by its shiftTime and shiftTimeUnit
 * - that will be added to each {@linkplain TimeStep} as a Time Shift (i.e., a time
 * offset from the Interval's Tick Time). Example, for a Daily TimeInterval at 8:00am,
 * use a {@linkplain Interval#DAYS} with tickCount=1, shiftTime=6, shiftUnit={@linkplain
 * TimeUnit#HOURS}. The Interval's tick time is for zeroHours every day. The Time shift
 * definition will add 8-hours to the Interval's tick time to make it 8:00 am every day.
 * @author kprins
 */
@XmlRootElement()
public class TimeInterval implements Serializable, Comparable<TimeInterval> {

  // <editor-fold defaultstate="collapsed" desc="Public Static JSON Serialization Methods">
  /**
   * Called to serialize the TimeStep to a JSONObject.
   * @param jsonObj the JSONObject to serialize to
   */
  public final static void toJSON(TimeInterval timeInterval, JSONObject jsonObj) {
    try {
      if (jsonObj == null) {
        throw new NullPointerException("The output JSON object is undefined.");
      }
      if ((timeInterval != null) &&
              (timeInterval.interval != null) && (timeInterval.tickCount != null)) {
        jsonObj.put("interval", timeInterval.interval.toString());
        jsonObj.put("tickcount", timeInterval.tickCount);

        if (timeInterval.shiftTime != null) {
          jsonObj.put("shifttime", timeInterval.shiftTime);
          jsonObj.put("shifttimeunit", timeInterval.getShiftTimeUnit().toString());
        }
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(TimeInterval.class.getSimpleName()
              + ".toJSON Error:\n " + exp.getMessage());
    }
  }

  /**
   * Called to de-serialize the TimeStep from a JSONObject
   * @param jsonObj the JSONObject to de-serialize from
   * @return a new instance or null if the parsing failed.
   */
  public final static TimeInterval fromJSON(JSONObject jsonObj) {
    TimeInterval result = null;
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSON object is undefined.");
      }
      TimeInterval newInst = new TimeInterval();

      String enumName = DataEntry.cleanUpString(jsonObj.optString("interval",null));
      if ((enumName == null) ||
                        ((newInst.interval = Interval.valueOf(enumName)) == null)) {
        throw new Exception("The TimeInterval's interval is undefined");
      }
      newInst.tickCount = jsonObj.optLong("tickcount",-1l);
      if (newInst.tickCount <= 0) {
        throw new Exception("The TimeInterval's TickCount is undefined");
      }

      long shftTime = jsonObj.optLong("shifttime", 0);
      if (shftTime > 0) {
        newInst.shiftTime = shftTime;
        if (((enumName =
                DataEntry.cleanUpString(jsonObj.optString("shiftunit",null))) == null) ||
            ((newInst.shiftTimeUnit = TimeUnit.valueOf(enumName)) == null))
        newInst.shiftTime = null;
      }
      result = newInst;
    } catch (Exception exp) {
      throw new IllegalArgumentException(TimeInterval.class.getSimpleName()
              + ".fromJSON Error:\n " + exp.getMessage());
    }
    return result;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The the number of Interval ticks per TimeInterval (Default=1)
   */
  @XmlElement
  private Long tickCount;
  /**
   * The TimeInterval's Interval (Default = NONE) this span is measured in milliSeconds
   */
  @XmlAttribute
  private Interval interval;
  /**
   * The TimeInterval's Shift Time in number of this.shiftUnit (Default=0)
   */
  @XmlAttribute
  private Long shiftTime;
  /**
   * The TimeInterval's Shift Time's TimeUnit (Default = this.timeUnit)
   */
  @XmlAttribute
  private TimeUnit shiftTimeUnit;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - for serialization only
   */
  public TimeInterval() {
    super();
    this.tickCount = null;
    this.interval = null;
    this.shiftTime = null;
    this.shiftTimeUnit = null;
  }

  /**
   * Constructor with a Interval and TimeUnit.
   *
   * @param tickCount the number if Interval ticks to add per TimeInterval tick (set to
   * 1 if &le; 0)
   */
  public TimeInterval(Interval interval, long tickCount) {
    this(interval, tickCount, null, null);
  }

  /**
   * Constructor with a Interval, a tickCount, a shiftTime, and a shiftTimeUnit.
   * Typically used when the
   * Interval is greater than 1 and the time series interval should start at some
   * offset from the TimeUnit's Zero Increment.
   * @param interval the time Interval (e.g., DAYS, MONTHS, etc.) - not NONE | null
   * @param tickCount the number if Interval ticks to add per TimeInterval tick (set to
   * 1 if &le; 0)
   * @param shiftTime the time interval shift from the Interval time (default = 0)
   * @param shiftTimeUnit the timeUnit of the shiftTime (must be defined is
   * <tt>shiftTime</tt> is set and not zero).
   */
  public TimeInterval(Interval interval, long tickCount, Long shiftTime,
          TimeUnit shiftTimeUnit) {
    this();
    if ((interval == null) || (DataEntry.isEq(Interval.NONE, interval))) {
      throw new IllegalArgumentException("The TimeInterval's TimeUnit cannot be "
              + "unassigned or NONE.");
    }
    if (tickCount <= 0) {
      throw new IllegalArgumentException("The TimeInterval's Tick Count cannot be less "
              + "or equal to zero.");
    }
    if ((this.shiftTime != null) && (this.shiftTime != 0)) {
      if (this.shiftTimeUnit == null) {
        throw new IllegalArgumentException("The TimeInterval's ShiftTime timeunit is "
                + "undefined.");
      }
    }
    this.interval = interval;
    this.tickCount = tickCount;
    this.shiftTime = shiftTime;
    this.shiftTimeUnit = shiftTimeUnit;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Properties">
  /**
   * Get the TimeStep's Interval
   * @return this.interval.timeUnit (default = {@linkplain Interval#NONE}
   */
  public final Interval getInterval() {
    return (this.interval == null) ? Interval.NONE : this.interval;
  }

  /**
   * Get the TimeStep's number of Interval Ticks
   * @return the assigned Time in this.TimeUnit (default = 0; never null)
   */
  public final long getTickCount() {
    return (this.tickCount == null) ? 0 : this.tickCount;
  }

  /**
   * Get the TimeStep's Time in the TimeStep's TimeUnits
   * @return this.interval.TimeUnit (never null)
   */
  public final TimeUnit getTimeUnit() {
    Interval intval = this.getInterval();
    return intval.timeUnit;
  }

  /**
   * Get the ShiftTime to add to the TimeStep's Interval Time
   * @return this.shiftTime or 0 if unassigned
   */
  public final long getShiftTime() {
    return (this.shiftTime == null) ? 0 : this.shiftTime;
  }

  /**
   * Get the ShiftTime's TimeUnit
   * @return this.shiftUnit or this.timeUnit is unassigned
   */
  public final TimeUnit getShiftTimeUnit() {
    return (this.shiftTimeUnit == null) ? this.getTimeUnit() : this.shiftTimeUnit;
  }
  // </editor-fold>


  /**
   * Called to get the TimeStep for a specified <tt>date</tt>. It calculates the
   * baseDt = date.addTime(-1*this.shiftTime, this.shiftTimeUnit, then
   * calls {@linkplain Interval#getTimeStep(bubblewrap.io.datetime.DateTime)
   * this.interval.getTimeStep(baseDt)} to get the new TimeStep, and finally transfers
   * this.shiftTime and this.shiftTimeUnit to the new TimeStep.
   * @param date the specified Date-Time
   * @return the new TimeStep.
   * @throws IllegalArgumentException is date=null or an error occurred.
   */
  public final TimeStep getTimeStep(DateTime date) {
    TimeStep result = null;
    try {
      if (date == null) {
        throw new Exception("The specified date cannot be unassigned.");
      }

      if (this.getShiftTime() != 0) {
        long shift = -1l * this.getShiftTime();
        date.addTime(shift, this.getShiftTimeUnit());
      }

      Interval intVal = this.getInterval();
      result = intVal.getTimeStep(date);
      if (this.getShiftTime() != 0) {
        result.setTimeShift(this.shiftTime, this.shiftTimeUnit);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getTimeStep Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Called to and the new TimeStep as defined by this TimeInetrval to the specified
   * current TimeStep. It calls {@linkplain
   * Interval#getNextStep(bubblewrap.io.schedules.TimeStep, long)
   * this.interval.getNextStep(curStep, this.tickCount)} to get the next TimeStep and
   * then transfers this.shiftTime and this.shiftTimeUnit to the new TimeStep.
   * @param curStep the specified Current TimeStep
   * @return the new TimeStep.
   * @throws IllegalArgumentException is date=null or an error occurred.
   */
  public final TimeStep getNextStep(TimeStep curStep) {
    TimeStep result = this.getNextStep(curStep, 1);
    return result;
  }

  /**
   * Called to and the new TimeStep as defined by this TimeInetrval to the specified
   * current TimeStep. It calls {@linkplain
   * Interval#getNextStep(bubblewrap.io.schedules.TimeStep, long)
   * this.interval.getNextStep(curStep, this.tickCount)} to get the next TimeStep and
   * then transfers this.shiftTime and this.shiftTimeUnit to the new TimeStep.
   * @param curStep the specified Current TimeStep
   * @param numSteps number of steps. 
   * @return the new TimeStep.
   * @throws IllegalArgumentException is date=null or an error occurred.
   */
  public final TimeStep getNextStep(TimeStep curStep, int numSteps) {
    TimeStep result = null;
    try {
      if (curStep == null) {
        throw new Exception("The Current TimeStep cannot be unassigned.");
      }
      Interval intVal = this.getInterval();
      result = intVal.getNextStep(curStep, this.getTickCount()*numSteps);
      result.setTimeShift(this.shiftTime, this.shiftTimeUnit);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getNextStep Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Called to and the new TimeStep as defined by this TimeInetrval to the specified
   * current TimeStep. It calls {@linkplain
   * Interval#getPriorStep(bubblewrap.io.schedules.TimeStep, long)
   * this.interval.getPriorStep(curStep, this.tickCount)} to get the prior TimeStep and
   * then transfer this.shiftTime and this.shiftTimeUnit to the new TimeStep.
   * @param curStep the specified Current TimeStep
   * @return the new TimeStep.
   * @throws IllegalArgumentException is date=null or an error occurred.
   */
  public final TimeStep getPriorStep(TimeStep curStep) {
    TimeStep result = null;
    try {
      if (curStep == null) {
        throw new Exception("The Current TimeStep cannot be unassigned.");
      }
      Interval intVal = this.getInterval();
      result = intVal.getPriorStep(curStep, this.getTickCount());
      result.setTimeShift(this.shiftTime, this.shiftTimeUnit);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getNextStep Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return "this.Time this.TimeUnit" .</p>
   */
  @Override
  public String toString() {
    String result = "TimeInterval[ timeUnit=" + this.getTimeUnit().toString()
            + "; tickCount=" + this.getTickCount();
    if ((this.getShiftTime() > 0)) {
      result += "; shiftTime=" + this.getShiftTime() + "; shiftUnit="
              + this.getShiftTimeUnit().toString();
    }
    result += " ]";
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new TimeInterval instance with the same Time and TimeUnit
   * settings.</p>
   */
  @Override
  public TimeInterval clone() {
    return new TimeInterval(this.getInterval(), this.getTickCount(),
            this.shiftTime, this.shiftTimeUnit);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return true if obj!=null and instance of TimeStep and its Interval,
   * tickCount, shiftTime and shiftTimeUnit match this instance's values.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean bResult = (super.equals(obj) && (obj instanceof TimeInterval));
    if (bResult) {
      TimeInterval other = (TimeInterval) obj;
      bResult = ((DataEntry.isEq(this.interval, other.interval))
              && (DataEntry.isEq(this.tickCount, other.tickCount))
              && (DataEntry.isEq(this.getShiftTime(), other.getShiftTime()))
              && (DataEntry.isEq(this.getShiftTimeUnit(), other.getShiftTimeUnit())));
    }
    return bResult;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a code based on this Interval, tickCount, shiftTime and
   * shiftTimeUnit</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 41 * hash + Objects.hashCode(this.tickCount);
    hash = 41 * hash + Objects.hashCode(this.interval);
    hash = 41 * hash + Objects.hashCode(this.shiftTime);
    hash = 41 * hash + Objects.hashCode(this.shiftTimeUnit);
    return hash;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Comparable">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Compare first this to other's intervals and is equal, compare their
   * tickCounts</p>
   */
  @Override
  public int compareTo(TimeInterval other) {
    int result = 0;
    Interval thisInt = this.getInterval();
    Interval otherInt = (other == null) ? Interval.NONE : other.getInterval();
    result = thisInt.compareTo(otherInt);
    if (result == 0) {
      Long thisTicks = this.getTickCount();
      Long otherTicks = (other == null) ? 0 : other.getTickCount();
      result = thisTicks.compareTo(otherTicks);
    }
    return result;
  }
  // </editor-fold>
}
