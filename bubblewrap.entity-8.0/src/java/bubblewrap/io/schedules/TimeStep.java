package bubblewrap.io.schedules;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAttribute;
import org.json.JSONObject;

/**
 * A TimeStep represents the point in time of a {@linkplain TimeSeries} or a
 * {@linkplain ScheduleStep}, which store the properties of a {@linkplain DateTime}
 * as using five(5) parameters: <ul>
 * <li>{@linkplain #getTimeUnit() TimeUnit} ({@linkplain TimeUnit}): - the unit in which
 *  the TimeStep's time is recorded</li>
 * <li>{@linkplain #getTime() Time} (long): - the  number of time intervals in
 *  TimeStep's TimeUnits</li>
 * <li>{@linkplain #getShiftTimeUnit() ShiftTimeUnit} ({@linkplain TimeUnit}): - the unit 
 *  in which the TimeStep's ShiftTime is recorded. OPTIONAL - only required if shiftTime
 * is defined</li>
 * <li>{@linkplain #getShiftTime() shiftTime} (long): - this number of 
 * Shift TimeUnits that should be added to this.time to calculate this.dateTime. 
 * OPTIONAL - default = 0.</li>
 * <li>{@linkplain #getTimeZone() TimeZone} ({@linkplain TimeZone}): - the TimeZone of
 *  to generate DateTime in a specific TimeZone</li>
 * </ul>
 * <p>
 * <b>NOTE:</b> A TiemStep's Time is ALWAYS specified in UTC time. It is converted to
 * the local TimeZone when calling the {@linkplain #getDateTime() getDateTime} method
 * </p>
 * @author kprins
 */
//@XmlType(propOrder={"time","timeUnit","timeZone"})
public final class TimeStep implements Serializable, Comparable<TimeStep> {
  
  /**
   * Static Constructor from a JSONObejct containing the previously saved properties of 
   * the TiemSTep
   * @param jsonObj the input JSONObejct
   * @return the deserialized TimeStep instance
   * @throws IOException if <tt>jsonObj</tt> is invalid or the process failed.
   */
  public final static TimeStep fromJson(JSONObject jsonObj) throws IOException {
    TimeStep result = null;
    try {
      if ((jsonObj == null) || (jsonObj.length() == 0)) {
        throw new IOException("The input jsonObj is unassigned or empty");
      }
      
      result = new TimeStep();
      result.fromJSON(jsonObj);
    } catch (Exception exp) {
      throw new IOException(TimeStep.class.getSimpleName()
              + ".fromJson Error:\n " + exp.getMessage());
    }
    return result;
  }

  //<editor-fold defaultstate="collapsed" desc="Static Logger">

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(TimeStep.class.getName());
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The TimeStep's Time in number of this.timeUnit (Default=0)
   */
  @XmlAttribute
  private Long time;
  /**
   * The TimeStep's TimeUnit (Default = Milliseconds)
   */
  @XmlAttribute
  private TimeUnit timeUnit;
  /**
   * The TimeStep's TimeZone (Default={@linkplain TimeZone#getDefault() }
   */
  private TimeZone timeZone;
  /**
   * The TimeStep's Shift Time in number of this.shiftUnit (Default=0)
   */
  @XmlAttribute
  private Long shiftTime;
  /**
   * The TimeStep's Shift Time's TimeUnit (Default = this.timeUnit)
   */
  @XmlAttribute
  private TimeUnit shiftTimeUnit;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  private TimeStep() {
    super();
    this.resetTimeStep();
  }

  /**
   * Constructor with a Time, TimeUnit, and TimeZone value.
   * @param time the time in the specified <tt>timeUnit</tt> (must be &ge; zero - will
   * be set to zero if negative).
   * @param timeUnit the TimeUnit (MILLISECONDS or greater)
   * @param timeZone the timezone reference (can be null to use the default).
   */
  public TimeStep(long time, TimeUnit timeUnit, TimeZone timeZone) {
    this();
    this.setTimeStep(time, timeUnit, timeZone);
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

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Reset all the TimeStpe settings
   */
  public final void resetTimeStep() {
    this.time = null;
    this.timeUnit = null;
    this.timeZone = null;
    this.shiftTime = null;
    this.shiftTimeUnit = null;    
  }
  
  /**
   * Call to set the TimeStep's Time, TimeUnit, and TimeZone value.
   * @param time the time in the specified <tt>timeUnit</tt> (must be &ge; zero - will
   * be set to zero if negative).
   * @param timeUnit the TimeUnit (MILLISECONDS or greater).
   * @param timeZone the timezone reference (can be null to use the default).
   */
  public final void setTimeStep(long time, TimeUnit timeUnit, TimeZone timeZone) {
//    this.time = (time < 0)? 0: time;
    this.time = time;
    if (timeUnit == null) {
      throw new NullPointerException("The Timestep's TimeUnit cannot be unassigned.");
    } else if (timeUnit.compareTo(TimeUnit.MILLISECONDS) < 0) {
      throw new IllegalArgumentException("TimeUnit[" + timeUnit.toString() + "] is "
              + "not supported. The Timestep's TimeUnit must by MILLISECONDS or "
              + "larger.");
    }
    this.timeUnit = timeUnit;
    this.timeZone = timeZone;
  }

  /**
   * Get the TimeStep's Time
   * @return the assigned Time in this.TimeUnit
   */
  public final Long getTime() {
    return this.time;
  }

  /**
   * Get the TimeStep's TimeUnit
   * @return
   */
  public final TimeUnit getTimeUnit() {
    return this.timeUnit;
  }

  /**
   * Get the TimeStep's TimeZone
   * @return get the assign TimeZone or {@linkplain TimeZone#getDefault() } if
   * unassigned.
   */
  public final TimeZone getTimeZone() {
    return this.timeZone;
  }

  /**
   * Get the TimeStep's DateTime. It use initiates a UTC Date using this.time and
   * this.timeUnits to calculate the date's time in milliseconds. The using the
   * {@linkplain DateTime#toTimeZoneDate(bubblewrap.io.datetime.DateTime,
   * java.util.TimeZone) DateTime.toTimeZoneDate} to convert the UTCDate to the
   * TimeStep's timeZone while maintaining its date stamp.
   * @return a DateTime in the specified (or Default) TimeZone
   */
  public final DateTime getDateTime() {
    Long milliSec = (TimeUnit.MILLISECONDS.equals(this.getTimeUnit()))? this.getTime():
                       TimeUnit.MILLISECONDS.convert(this.getTime(), this.getTimeUnit());
    DateTime utcDt = new DateTime(milliSec, DateTime.UTCTimeZone);
    DateTime result = DateTime.toTimeZoneDate(utcDt, this.getTimeZone());
    long delTime = this.getShiftTime();
    if (delTime != 0) {
      result = result.addTime(delTime, this.getShiftTimeUnit());
    }
    return result;
  }

  /**
   * Get the TimeStep's Time in the MilliSeconds for this.timeZone.
   * @return {@linkplain #getDateTime() this.dateTime}.totalMilliSeconds
   */
  public final Long getMilliSeconds() {
    DateTime localDt = this.getDateTime();
    return localDt.getTotalMilliseconds();
  }

  /**
   * OVERLOAD 1: A call {@linkplain #setTimeShift(long, java.util.concurrent.TimeUnit)
   * Overload 2} with shiftUnit = this.timeUnit.
   * <p>
   * <b>NOTE:</b> Typically, the </p>
   * @param shiftTime the time (in this.tiemUnits) to add to the Interval step time.
   */
  public final void setTimeShift(Long shiftTime) {
    this.setTimeShift(shiftTime, null);
  }

  /**
   * Add a Shift time for this TimeStep (i.e., an offset from the TimeStep's interval
   * timeStep). Example for a Daily Time step (which start at Zero Hour), a shift time
   * of (6, HOURS) will set the TimeStep's DateTime at 06:00h. The shift time is negative
   * the TimeStep's DateTime will be before TimeStep's interval timeStep's DateTime.
   * @param shiftTime this time interval to add (in the specified shiftUnit units)
   * @param shiftTimeUnit the TimeUit of the time interval.
   */
  public final void setTimeShift(Long shiftTime, TimeUnit shiftTimeUnit) {
    this.shiftTime = shiftTime;
    this.shiftTimeUnit = shiftTimeUnit;
  }

  /**
   * Get the ShiftTime to add to the TimeStep's Interval Time
   * @return this.shiftTime or 0 if unassigned
   */
  public final long getShiftTime() {
    return (this.shiftTime == null)? 0: this.shiftTime;
  }

  /**
   * Get the ShiftTime's TimeUnit
   * @return this.shiftUnit or this.timeUnit is unassigned
   */
  public final TimeUnit getShiftTimeUnit() {
    return (this.shiftTimeUnit == null)? this.getTimeUnit(): this.shiftTimeUnit;
  }

  /**
   * Get whether this TimeStep is greater than (later than) pTimeStep
   * @param timeStep the other TimeStep
   * @return return (this.compareTo(pTimeStep) &gt; 0)
   */
  public final boolean isGreaterThan(TimeStep timeStep) {
    return (this.compareTo(timeStep) > 0);
  }

  /**
   * Get whether this TimeStep is less than (earlier than) pTimeStep
   * @param timeStep the other TimeStep
   * @return return (this.compareTo(pTimeStep) &lt; 0)
   */
  public final boolean isLessThan(TimeStep timeStep) {
    return (this.compareTo(timeStep) < 0);
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public JSON Serialization Methods">
  /**
   * Called to serialize the TimeStep to a JSONObject.
   * @param jsonObj the JSONObject to serialize to
   */
  public final void toJSON(JSONObject jsonObj) {
    try {
      if (jsonObj == null) {
        throw new NullPointerException("The output JSON object is undefined.");
      }
      jsonObj.put("time", this.time);
      jsonObj.put("timeunit", this.timeUnit.toString());
      if (this.timeZone != null) {
        jsonObj.put("timezone", this.timeZone.getID());
      }

      if (this.shiftTime != null) {
        jsonObj.put("shifttime", this.shiftTime);
        jsonObj.put("shifttimeunit", this.getShiftTimeUnit().toString());
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".toJSON Error:\n " + exp.getMessage());
    }
  }

  /**
   * Called to de-serialize the TimeStep from a JSONObject
   * @param jsonObj the JSONObject to de-serialize from
   */
  private void fromJSON(JSONObject jsonObj) throws IOException {
    try {
      this.resetTimeStep();
      if (jsonObj == null) {
        throw new Exception("The input JSON object is undefined.");
      }
      this.time = jsonObj.optLong("time",-1l);
      if (jsonObj.isNull("time")) {
        throw new Exception("The TimeStep's time is undefined");
      } 
      String unitName = DataEntry.cleanUpString(jsonObj.optString("timeunit",null));
      this.timeUnit = TimeUnit.valueOf(unitName);
      if ((unitName == null) || ((this.timeUnit = TimeUnit.valueOf(unitName)) == null)) {
        throw new Exception("The TimeStep's timeunit is undefined");
      }      
      
      String znId = DataEntry.cleanString(jsonObj.optString("timezone",null));
      if (znId != null) {
        this.timeZone = TimeZone.getTimeZone(znId);
      }
      long shftTime = jsonObj.optLong("shifttime", 0);
      if (shftTime > 0) {
        this.shiftTime = shftTime;
        if (((unitName = 
             DataEntry.cleanUpString(jsonObj.optString("shifttimeunit",null))) == null) ||
            ((this.shiftTimeUnit = TimeUnit.valueOf(unitName)) == null))   
        this.shiftTime = null;
      }
    } catch (Exception exp) {
      this.resetTimeStep();
      throw new IOException(exp.getMessage(), exp);
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "this.Time this.TimeUnit (this.TimeZone.ID)" or
   * "this.Time this.TimeUnit" if the TiemZone is not specified.</p>
   */
  @Override
  public String toString() {
    String sResult = this.time.toString() + " " + this.timeUnit.toString();
    if (this.timeZone != null) {
      sResult += " (" + this.timeZone.getID() + ")";
    }
    return sResult;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return a new TimeStep instance with the same Time,
   * TimeUnit, and TimeZone settings.</p>
   */
  @Override
  public TimeStep clone()  {
    TimeStep result = new TimeStep(this.time, this.timeUnit, this.timeZone);
    result.setTimeShift(this.shiftTime, this.shiftTimeUnit);
    return result;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return true if obj!=null and instanceof TimeStep and
   * its Time and TimeUnit match this instance's values.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean bResult = ((obj != null) && (obj instanceof TimeStep));
    if (bResult) {
      TimeStep other = (TimeStep) obj;
      bResult = ((this.time.equals(other.time))
              && (this.timeUnit.equals(other.timeUnit)));
    }
    return bResult;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return a HashCode using this instance's Time and
   * TimeUnit values</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 41 * hash + Objects.hashCode(this.time);
    hash = 41 * hash + (this.timeUnit != null ? this.timeUnit.hashCode() : 0);
    return hash;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc} <p>Compare this instance's MilliSecond with that of
   * pTimeStep. Return 0 if both are unassigned, -1 of this.milliSeconds=null, 1 if
   * pTimeStep or pOther.milliseconds is unassigned, or the result of {@linkplain
   * Long#compareTo(java.lang.Long) comparing this.milliSeconds} to
   * pOther.milliSeconds.</p>
   */
  @Override
  public int compareTo(TimeStep other) {
    int result = 0;
    Long milSec1 = this.getMilliSeconds();
    Long milSec2 = (other == null)? null: other.getMilliSeconds();
    if ((milSec1 == null) && (milSec2 != null)) {
      result = -1;
    } else if ((milSec1 != null) && (milSec2 == null)) {
      result = 1;
    } else if ((milSec1 != null) && (milSec2 != null)) {
      result = milSec1.compareTo(milSec2);
    }
    return result;
  }
  //</editor-fold>
}
