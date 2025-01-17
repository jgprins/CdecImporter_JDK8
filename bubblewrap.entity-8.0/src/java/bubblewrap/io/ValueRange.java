package bubblewrap.io;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.interfaces.BwComparator;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import org.json.JSONObject;

/**
 * Generic definition of a dynamic Value Range. The range can be initiated as an empty
 * range (i.e., this.min = this.max = null) and then be grown be calling {@linkplain 
 * #grow this.grow} and passing in one or more values.
 * @author kprins
 */
public abstract class ValueRange<TValue extends Comparable<TValue>> 
                                                                implements Serializable {
    
  //<editor-fold defaultstate="collapsed" desc="JSON Serializer Implementation">
  // <editor-fold defaultstate="collapsed" desc="JSON Public Serilization Methods">
  /**
   * Called to serialize this instance of ValueRange to <tt>jsonObj</tt>.
   * The assigned field values are as follows:<ul>
   * <li><b>:</b> </li>
   * </ul>
   * <p>
   * After serializing this instance's field values it calls {@linkplain
   * #onToJSON(org.json.JSONObject) this.onToJSON(jsonObj)} to handle the serialization
   * of inherited classes.
   * @param jsonObj the JSONObject to the assigned field values to
   * @throws IOException if the process failed.
   */
  
  public final void toJSON(JSONObject jsonObj) throws IOException {
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unassigned.");
      }
      
      if (this.comparator != null) {
        if (!JsonSerializer.isSerializable(this.comparator.getClass())) {
          throw new Exception("The Comparator Class[" 
                  + this.comparator.getClass().getSimpleName() 
                  + "] is not JSON Serializable");
        }
        jsonObj.putOpt("comparator", JsonSerializer.serialize(this.comparator));
      }
      
      /* Call onFromJSON to handle inherited serialization */
      this.onToJSON(jsonObj);
    } catch (Exception exp) {
      throw new IOException(ValueRange.class.getSimpleName()
              + ".toJson Error:\n " + exp.getMessage(), exp);
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #toJSON(org.json.JSONObject)
   * this.toJSON(fromJSON)} to serialize the field values of inherited classes.
   * The base method does nothing.
   * <p>
   * <b>NOTE:</b> Inheritors must serialize the minimum and maximum
   * values of the range</p>
   * @param jsonObj the JSONObject to assigned the instance's field values to.
   * @throws IOException if the process failed.
   * */
  protected void onToJSON(JSONObject jsonObj) throws IOException {
  }

  /**
   * Called to deserialize this properties of this instance of ValueRange
   * from a JSONObject. The instance properties were as set in the {@linkplain
   * #toJSON(org.json.JSONObject) toJSON} method.
   * <p>
   * After serializing this instance's field values it calls {@linkplain
   * #onFromJSON(org.json.JSONObject) this.onFromJSON(jsonObj)} to handle the
   * deserialization of inherited classes.
   * @param jsonObj the JSONObject containing the instance's field values.
   * @throws IOException if the process failed.
   */
  public final void fromJSON(JSONObject jsonObj) throws IOException {
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unassigned.");
      }
      
      JSONObject compObj = jsonObj.optJSONObject("comparator");
      if (compObj != null) {
        this.comparator = JsonSerializer.deserialize(compObj);
      }

      /* Call onFromJSON to hanle inherited deserialization */
      this.onFromJSON(jsonObj);
    } catch (Exception exp) {
      throw new IOException(ValueRange.class.getSimpleName()
              + ".fromJSON Error:\n " + exp.getMessage(), exp);
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #fromJSON(org.json.JSONObject)
   * this.fromJSON(fromJSON)} to deserialize the field values of inherited classes.
   * The base method does nothing.
   * <b>NOTE:</b> Inheritors must deserialize the minimum and maximum
   * values of the range</p>
   * @param jsonObj the JSONObject containing the instance's field values.
   * @throws IOException if the process failed.
   * */
  protected void onFromJSON(JSONObject jsonObj) throws IOException {
  }
  // </editor-fold>
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Final Fields">
  /**
   * The ranges minium value
   */
  private TValue min;
  /**
   * The ranges maximum value
   */
  private TValue max;
  /**
   * An optional Comparator
   */
  private BwComparator<TValue> comparator;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * EventHandler for sending a Range Changed event.
   */
  public final EventHandler RangeChanged;

  /**
   * Method called to fie the Range Changed event.
   */
  protected void fireRangeChanged() {
    this.RangeChanged.fireEvent(this, new EventArgs());
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public construct an empty valueRange
   */
  protected ValueRange()  {
    this.RangeChanged = new EventHandler();
    this.min = null;
    this.max = null;
    this.comparator = null;
  }
  
  /**
   * Public construct with an optional comparator and an empty valueRange
   */
  protected ValueRange(BwComparator<TValue> comparator)  {
    this();
    this.comparator = comparator;
  }
  
  /**
   * Public Constructor with a set of values
   */
  protected ValueRange(TValue...values) {
    this();
    this.grow(values);
  }
  
  /**
   * Public Constructor with an optional comparator and a set of values
   */
  protected ValueRange(BwComparator<TValue> comparator, TValue...values) {
    this(comparator);
    this.grow(values);
  }
  
  /**
   * Public Constructor with a set of values
   */
  protected ValueRange(Collection<TValue> values) {
    this();
    this.grow(values);
  }
  
  /**
   * Public Constructor with an optional comparator and a set of values
   */
  protected ValueRange(BwComparator<TValue> comparator, Collection<TValue> values) {
    this(comparator);
    this.grow(values);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method before calling this.RangeChanged.clear()</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.RangeChanged.clear();
  }
  
  /**
   * Call to reset the Range's Min/max values
   */
  public void reset() {
    if (!this.isEmpty()) {
      this.max = null;
      this.min = null;
      this.fireRangeChanged();
    }
  }
  
  /**
   * A protected method for JSON deserialization to assign the ValueRange's min and max 
   * values. 
   * @param min the minimum value (can be null)
   * @param max the maximum value (can be null)
   */
  protected void setRange(TValue min, TValue max) {
    this.max = max;
    this.min = min;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Method">
  /**
   * Called to compare two values. If this.comparator != null return the result of
   * this.comparator.compare(val1, val2). Else this.isValidValue(val1) return the
   * result of val1.compareTo(val2), else if this.isValidValue(val2), return -1.
   * @param val1 the first (base) value
   * @param val2 the second value to compare to val1
   * @return the comparison result.
   */
  private int compare(TValue val1, TValue val2) {
    int result = 0;
    if (this.comparator != null) {
      result = this.comparator.compare(val1, val2);
    } else if (this.isValidValue(val1)) {
      result = val1.compareTo(val2);
    } else if (this.isValidValue(val2)) {
      result = -1 ;
    }
    return result;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Public Method">
  /**
   * FINAL: Get the Entity Bean's Class
   * @return the generically assigned Entity Bean class
   */
  @SuppressWarnings("unchecked")
  public final Class<TValue> getValueClass() {
    Class myClass = this.getClass();
    return ReflectionInfo.getGenericClass(ValueRange.class, myClass, 0);
  }
  
  /**
   * Get the Minimum value
   * @return the maximum value (null if this.isEmpty)
   */
  public final TValue getMin() {
    return this.min;
  }
  
  /**
   * Get the Maximum value
   * @return the maximum value (null if this.isEmpty)
   */
  public final TValue getMax() {
    return this.max;
  }
  
  /**
   * Grow the range by the defined set of <tt>values</tt> (i.e., one or more values).
   * The process will be skipped if no values are passed in (i.e., <tt>values</tt> = 
   * null|empty) and individual values will be ignored if {@linkplain 
   * #isValidValue(java.lang.Comparable) this.isValidValue(value)} = false.
   * <p>If the range changed, it will fire the {@linkplain #RangeChanged 
   * this.RangeChanged} event.
   * <p>
   * <b>NOTE:</b> If {@linkplain #isEmpty() this.isEmpty} and only one valid value is
   * passed in then the range is set as this.min = this.max = value.</p>
   * @param values the values to expand to range to.
   */
  public final void grow(TValue...values) {
    if ((values == null) || (values.length == 0)) {
      return;
    }
    
    boolean changed = false;
    for (TValue value : values) {
      if (!this.isValidValue(value)) {
        continue;
      }
      
      if (this.isEmpty()) {
        this.max = value;
        this.min = value;
        changed = true;
      } else if (this.compare(this.min, value) > 0) {
        this.min = value;
        changed = true;
      } else if (this.compare(this.max, value) < 0) {
        this.max = value;
        changed = true;
      }
    }
    
    if (changed) {
      this.fireRangeChanged();
    }
  }
  
  /**
   * Grow the range by the defined set of <tt>values</tt> (i.e., one or more values).
   * The process will be skipped if no values are passed in (i.e., <tt>values</tt> = 
   * null|empty) and individual values will be ignored if {@linkplain 
   * #isValidValue(java.lang.Comparable) this.isValidValue(value)} = false.
   * <p>If the range changed, it will fire the {@linkplain #RangeChanged 
   * this.RangeChanged} event.
   * <p>
   * <b>NOTE:</b> If {@linkplain #isEmpty() this.isEmpty} and only one valid value is
   * passed in then the range is set as this.min = this.max = value.</p>
   * @param values the values to expand to range to.
   */
  public final void grow(Collection<TValue> values) {
    if ((values == null) || (values.isEmpty())) {
      return;
    }
    
    boolean changed = false;
    for (TValue value : values) {
      if (!this.isValidValue(value)) {
        continue;
      }
      
      if (this.isEmpty()) {
        this.max = value;
        this.min = value;
        changed = true;
      } else if (this.compare(this.min, value) > 0) {
        this.min = value;
        changed = true;
      } else if (this.compare(this.max, value) < 0) {
        this.max = value;
        changed = true;
      }
    }
    
    if (changed) {
      this.fireRangeChanged();
    }
  }
  
  /**
   * Check if the range is empty
   * @return if this.min or this.max is null.
   */
  public final boolean isEmpty() {
    return ((this.min == null) || (this.max == null));
  }
  
  /**
   * Check whether value falls within the defined range. If (!{@linkplain 
   * #isValidValue(java.lang.Comparable) this.isValidValue(valeu)}) or {@linkplain 
   * #isEmpty() thisisEmpty.}, it returns false. Otherwise, it will return true if 
   * <tt>value</tt> falls within the define range. It uses the TValue type's compareTo 
   * to determine whether the value is in- or outside the range.
   * @param value the value to validate
   * @return true if in the defined range or false if value is null.
   */
  public final boolean inRange(TValue value) {
    boolean result = false;
    if ((this.isValidValue(value)) && (!this.isEmpty())) {
      result = ((this.compare(this.min, value) <= 0) && 
                                                  (this.compare(this.max, value) >= 0));
    }
    return result;
  }
  
  /**
   * Assign the min/max setting of this range to the target range
   * @param target the range to update.
   */
  public void assignTo(ValueRange<TValue> target) {
    if (target != null) {
      target.max = this.max;
      target.min = this.min;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * Check whether <tt>value</tt> is a valid range value (i.e., not null, NaN, out of 
   * valid range).
   * @return true is valid
   */
  protected abstract boolean isValidValue(TValue value);
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "Range[min=this.min; ma:this.max]"</p>
   */
  @Override
  public String toString() {
    if (this.isEmpty()) {
      return "Range[]";
    } else {
      return "Range[min:" + this.min +"; max:" + this.max + "]";
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return (obj != null) && (instance of ValueRange) and this.min and this.max
   * matches obj.min and obj.max, respectively.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof ValueRange));
    if (result) {
      ValueRange other = (ValueRange) obj;
      result = ((DataEntry.isEq(this.min, other.min)) &&
                (DataEntry.isEq(this.max, other.max)));
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a hashCode using this.min and this.max values.</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 89 * hash + Objects.hashCode(this.min);
    hash = 89 * hash + Objects.hashCode(this.max);
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a clone of this instance.
   * @throws IllegalArgumentException if the instance initiation failed</p>
   */
  @Override
  public ValueRange<TValue> clone() {
    ValueRange<TValue> result = null;
    try {
      result = this.getClass().newInstance();
      result.min = this.min;
      result.max = this.max;
      if (this.comparator != null) {
        result.comparator = this.comparator;
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".clone Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
}
