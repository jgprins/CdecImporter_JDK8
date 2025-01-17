package bubblewrap.io.timeseries;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.TimeStep;
import java.io.Serializable;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import org.json.JSONObject;

/**
 * An object for storing a single time step's value in a timeseries recordset.
 * @author kprins
 */
public abstract class TimeStepValue implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
                                    = Logger.getLogger(TimeStepValue.class.getName());
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder of the value's TimeStep in the owner TimeSeries' TimeUnits.
   */
  @XmlElement
  private TimeStep timeStep;
  /**
   * An delegate assigned by the timeSeries to fire when a TimeSeries Value Change
   */
  private EventDelegate valueChangedDelegate;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Parameterless Constructor for serialization. Set the Datetime and the value as null
   * (i.e {@linkplain #isNullValue()} = true.
   */
  public TimeStepValue() {
    this.timeStep = null;
  }

  /**
   * Constructor with a TimeStep and Value
   * @param timeStep the TimeStep (not null)
   */
  public TimeStepValue(TimeStep timeStep) {
    if ((this.timeStep = timeStep) == null) {
      throw new NullPointerException("The TimeValue's TimeStep cannot be unassigned");
    }
  }

  /**
   * Constructor with a TimeStep, Value, TimeUnit and TimeZone
   * @param time the TimeStep in the specified TimeUnits
   * @param timeUnit The TimeStep's TimeUnit.
   * @param timeZone the new TimeZone (can be null to use default).
   */
  public TimeStepValue(Long time, TimeUnit timeUnit, TimeZone timeZone) {
    if (time == null) {
      throw new NullPointerException("The TimeValue's Time cannot be unassigned");
    }
    if (timeUnit == null) {
      throw new NullPointerException("The TimeValue's TimeUnit cannot be unassigned");
    }

    this.timeStep = new TimeStep(time, timeUnit, timeZone);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * Get the TimeStep's TimeZone
   * @return the assigned TimeZone or null if using the Default TimeZone.
   */
  protected TimeZone getTimeZone() {
    return (this.timeStep == null)? null: this.timeStep.getTimeZone();
  }
  
  /**
   * Called to trigger a send a ValeuChanged call if this.valueChangedDelegate is assigned
   */
  protected void fireValueChanged() {
    if (this.valueChangedDelegate != null) {
      try {
        this.valueChangedDelegate.onEvent(this, new EventArgs());
      } catch (Exception exp) {
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the TimeStep's DateTime.
   * @return the {@linkplain TimeStep#getDateTime()} or null if the TimeStep=null.
   */
  public final DateTime getDateTime() {
    return (this.timeStep == null)? null: this.timeStep.getDateTime();
  }

  /**
   * Get the TimeStep in the MilliSeconds
   * @return the {@linkplain TimeStep#getMilliSeconds()} or null if the TimeStep=null.
   */
  public final Long getMilliSeconds() {
    return (this.timeStep == null)? null: this.timeStep.getMilliSeconds();
  }

  /**
   * Get the timestep's in the value's TimeUnit.
   * @return the assigned TimeStep
   */
  public final TimeStep getTimeStep() {
    return this.timeStep;
  }

  /**
   * Check is this timestep value was properly initiated
   * @return true if this.timeStep = null.
   */
  public boolean isNullValue() {
    return (this.timeStep == null);
  }

  /**
   * Called to reset the TimeStepValue's value(s). - not its timeStep setting.
   * It calls {@linkplain #onReset() this.onReset} between a try-except and ignore all
   * errors that are thrown.
   */
  public final void reset() {
    try {
      this.onReset();
    } catch (Exception exp) {
    }
  }
  
  /**
   * Set the TimeStep's ValueChange Delegate to which a call can be triggered by calling
   * {@linkplain #fireValueChanged() this.fireValueChanged}
   * @param valueChangedDelegate a EventDelegate - can be null.
   */
  public final void setValueChangedDelegate(EventDelegate valueChangedDelegate) {
    this.valueChangedDelegate = valueChangedDelegate;
  }

  /**
   * CAN OVERRIDE: Get the TimeStep's isDisabled state. The base method always returns
   * false.
   * @return the assigned state
   */
  public boolean isDisabled() {
    return false;
  }

  /**
   * CAN OVERRIDE: Get the TimeStep's isReadOnly state. The base method always returns
   * false.
   * @return the assigned state
   */
  public boolean isReadOnly() {
    return false;
  }

  /**
   * ABSTRACT: Called by {@linkplain #reset() this.reset} to custom handle the reset of
   * the TimeStepValue's value(s).
   * @throws Exception on error, which is trapped an ignored.
   */
  protected abstract void onReset() throws Exception;

  /**
   * Check if the record has a missing value.
   * @return true if (this.value=null)
   */
  public abstract boolean isMissingValue();

  /**
   * Get whether TimeStepValue's value(s) matches this instance's value(s)
   * @param <TValue> - extends TimeStepValue
   * @param target a target TimeValue to compare to.
   * @return true if the values match (but not necessarily the timeSteps.
   */
  public abstract <TValue extends TimeStepValue> boolean isValue(TValue target);
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public JSON Serializtion Methods">
  /**
   * Called to serialize the TimeStepValue to a JSONObject. It serializes
   * this.timeStep before calling {@linkplain #onToJSON(org.json.JSONObject)
   * this.onToJSON} to handle custom serialization the extended class properties.
   * @param jsonObj the JSONObject to serialize from
   */
  public final void toJSON(JSONObject jsonObj) {
    try {
      if (jsonObj == null) {
        throw new NullPointerException("The output JSON object is undefined.");
      }
      JSONObject newObj = new JSONObject();
      this.timeStep.toJSON(newObj);
      jsonObj.put("timestep", newObj);

      this.onToJSON(jsonObj);

    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".toJSON Error:\n " + exp.getMessage());
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #toJSON(org.json.JSONObject) this.toJSON}
   * to serialize to extended properties of the inherited class.
   * @param jsonObj the JSONObject to serialize to
   * @throws Exception is any error occurred.
   */
  protected abstract void onToJSON(JSONObject jsonObj) throws Exception;

  /**
   * Called to de-serialize the TimeStepValue from a JSONObject. It de-serialize
   * the saved TimeStep (savedStep). if (this.timeStep != null) (i.e., a pre-initiated
   * TimeStepValue) it will validate that the steps match and throw an exception if not.
   * Otherwise, it will set this.timeStep = savedStep.
   * <p>When this.timeStep is assigned or validated, it calls {@linkplain 
   * #onFromJSON(org.json.JSONObject) this.onFromJSON} to handle custom de-serialization 
   * of extended TimeStepValue.
   * @param jsonObj the JSONObject to serialize from
   */
  public final void fromJSON(JSONObject jsonObj) {
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSON object is undefined.");
      }
      JSONObject stepObj = jsonObj.optJSONObject("timestep");
      TimeStep savedStep = TimeStep.fromJson(stepObj);
      if (savedStep == null) {
        throw new Exception("Unable to deserialize the TiemStep Value's TimeStep.");
      }
      
      if (this.timeStep != null) {
        if (!this.timeStep.equals(savedStep)) {
          throw new Exception("The JSON input's timestep does not match this TimeStep.");
        }
      } else {
        this.timeStep = savedStep;
      }

      this.onFromJSON(jsonObj);

    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".fromJSON Error:\n " + exp.getMessage());
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #fromJSON(org.json.JSONObject) this.fromJSON}
   * to de-serialize to extended properties of the inherited class.
   * @param jsonObj the JSONObject to serialize from
   * @throws Exception is any error occurred.
   */
  protected abstract void onFromJSON(JSONObject jsonObj) throws Exception;
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Check if obj matches this instance by comparing the
   * TimeStep, TimeUnit, and values. Returns true if obj != null and a instance of
   * TimeValue and the TimeStep, TimeUnit, and Value match.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof TimeStepValue));
    if (result) {
      TimeStepValue other = (TimeStepValue) obj;
      result = ((this.timeStep != null) && (this.timeStep.equals(other.timeStep)));
    }
    return result;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return a hash code using the DateTime and value</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + Objects.hashCode(this.timeStep);
    return hash;
  }

  /**
   * CAN OVERRIDE: Called to assign the values of this instance to that of <tt>target</tt>.
   * Skipped if target is unassigned.
   * @param <TValue> extends TimeValue
   * @param target The target to assign to.
   */
  protected <TValue extends TimeStepValue> void assignTo(TValue target) {
    if (target != null) {
      TimeStepValue stepValue = target;
      stepValue.timeStep = this.timeStep.clone();
    }
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: It creates and new instance of this class and call
   * {@linkplain #assignTo(myapp.io.timeseries.TimeValue) this.assignTo) to initiate
   * the field values of the clone before it returns the clone.</p>
   */
  @Override
  public final TimeStepValue clone() throws CloneNotSupportedException {
    TimeStepValue result = null;
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
   * {@inheritDoc} <p>OVERRIDE: Return TimeValue[DateTime; Value]</p>
   */
  @Override
  public String toString() {
    String sResult = this.getClass().getSimpleName() +": timeStep[";
    sResult += (this.timeStep == null)? "-" : (this.timeStep.toString());
    sResult += "]";
    return sResult;
  }
  //</editor-fold>
}
