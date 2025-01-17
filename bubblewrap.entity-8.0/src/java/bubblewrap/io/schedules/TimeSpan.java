package bubblewrap.io.schedules;

import bubblewrap.io.DataEntry;
import bubblewrap.io.schedules.enums.Interval;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>A TimeSpan is used to define time as a number of intervals of a specified
 * {@linkplain TimeUnit}. The definition of a TimeSpan is subjected to the following 
 * constraints:</p><ol>
 *  <li><b>(TimeUnit!=null):</b> - The TimeSpan's TimeUnit must be defined.</li>
 *  <li><b>Min(TimeUnit)=MILLISECONDS:</b> - DateTimes are managed in MilliSeconds - 
 *    thus at this point there seems to be no need for time intervals in units of less 
 *    than milliseconds.</li>
 * </ol>
 * @author kprins
 */
@XmlRootElement()
public class TimeSpan implements Serializable, Comparable<TimeSpan> {  
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The the number of Interval Tick in units of this.interval.timeUnit (Default=0)
   */
  @XmlElement
  private Long tickCount;
  /**
   * The timeSpan's start interval tickIndex (default = 0)
   */
  @XmlElement
  private Integer startTick;
  /**
   * The TimeStep's Interval (Default = NONE) this span is measured in milliSeconds
   */
  @XmlAttribute
  private Interval interval;
  //</editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - for serialization only
   */
  public TimeSpan() {
    super();    
    this.tickCount = null;
    this.interval = Interval.NONE;
    this.startTick = null;
  }
  
  /**
   * Constructor with a Interval and tickCount (assuming startTick = 0).
   * @param interval the Interval for calculating the timeSpan per Interval tick
   * @param tickCount the number of interval ticks (must &gt; 0)
   */
  public TimeSpan(Interval interval, Long tickCount) {
    this();
    this.setTimeSpan(interval, tickCount, 0);
  }
  
  /**
   * Constructor with a Interval and tickCount, and startTick.
   * @param interval the Interval for calculating the timeSpan per Interval tick
   * @param tickCount the number of interval ticks (must &gt; 0)
   * @param startTick the TimeSpan's start tick index for the <tt>interval</tt> 
   * (default = null|0)
   */
  public TimeSpan(Interval interval, Long tickCount, Integer startTick) {
    this();
    this.setTimeSpan(interval, tickCount, startTick);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Call to set the Interval and tickCount, and startTick.
   * @param interval the Interval for calculating the timeSpan per Interval tick
   * @param tickCount the number of interval ticks (must &gt; 0)
   * @param startTick the TimeSpan's start tick index for the <tt>interval</tt> (must 
   * &gt; 0)
   */
  private void setTimeSpan(Interval interval, Long tickCount, Integer startTick) {
    if ((interval == null) || (Interval.NONE.equals(interval))) {
      throw new NullPointerException("The TimeSpan's interval cannot be unassigned or"
              + "NONE.");
    }
    this.interval = interval;
    this.tickCount = ((tickCount == null) || (tickCount <= 0))? null: tickCount;
    this.startTick = ((startTick == null) || (startTick <= 0))? null: startTick;
  }
  
  /**
   * Get the TimeStep's Interval
   * @return this.interval.timeUnit
   */
  public final Interval getInterval() {
    return (this.interval == null)? Interval.NONE: this.interval;
  }
  
  /**
   * Get the TimeStep's number of Interval Ticks
   * @return the assigned Time in this.TimeUnit  (default = 0)
   */
  public final long getTickCount() {
    return (this.tickCount == null)? 0: this.tickCount;
  }
  
  /**
   * Get the TimeStep's Interval
   * @return this.startTick (default = 0)
   */
  public final int getStartTick() {
    return (this.startTick == null)? 0: this.startTick;
  }
  
  /**
   * Get the TimeStep's Time in the TimeStep's TimeUnits
   * @return the assigned Time in this.TimeUnit  (default = 0)
   */
  public final long getSpan() {
    return (this.tickCount == null)? 0: this.tickCount;
  }
  
  /**
   * Get the TimeStep's Time in the TimeStep's TimeUnits
   * @return the assigned Time in this.TimeUnit  (default = 0)
   */
  public final TimeUnit getTimeUnit() {
    Interval intval = this.getInterval();
    return intval.timeUnit;
  }
  
  /**
   * Get the TimeStep's Span in the MilliSeconds
   * @return the time span in MilliSeconds
   */
  public final Long getMilliSeconds() {
    return TimeUnit.MILLISECONDS.convert(this.getSpan(), this.getTimeUnit());
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} 
   * <p>OVERRIDE: Return "TimeSpan[ int=" + this.interval.label 
                                         + "; span="+ this.span.toString() + " ]".</p>
   */
  @Override
  public String toString() {
    String result = "TimeSpan[ int=" + this.interval.label 
                                         + "; span="+ this.tickCount.toString() + " ]";
    return result;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return a new TimeInterval instance with the same Interval 
   * tickCount, and startTick settings.</p>
   */
  @Override
  public TimeSpan clone()  {
    return new TimeSpan(this.interval, this.tickCount, this.startTick);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return true if obj!=null and instanceof TimeStep and
   * its interval and span match this instance's values.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof TimeSpan));
    if (result) {
      TimeSpan other = (TimeSpan) obj;
      result = ((DataEntry.isEq(this.interval,other.interval)) &&
                (DataEntry.isEq(this.tickCount,other.tickCount)) &&
                (DataEntry.isEq(this.startTick,other.startTick)));
    }
    return result;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return a HashCode using this instance's interval,
   * tickCount and startTick values</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + Objects.hashCode(this.tickCount);
    hash = 59 * hash + Objects.hashCode(this.startTick);
    hash = 59 * hash + Objects.hashCode(this.interval);
    return hash;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc} <p>OVERRIDE: Compare this instance's MilliSecond with that of
   * other. Return 0 if both are unassigned, -1 of this.milliSeconds=null, 1 if
   * this or other.milliseconds is unassigned, or the result of {@linkplain
   * Long#compareTo(java.lang.Long) comparing this.milliSeconds} to
   * other.milliSeconds.</p>
   */
  @Override
  public int compareTo(TimeSpan other) {
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
