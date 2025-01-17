package bubblewrap.io.datetime;

import bubblewrap.io.schedules.enums.Interval;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 * @param <TValue>
 */
public class DateMap<TValue> extends LinkedHashMap<DateMapKey, TValue>{

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Interval used in Date Comparisons
   */
  public final Interval interval;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public DateMap(Interval interval) {
    super();
    if ((this.interval = interval) == null) {
      throw new NullPointerException("The DateMap's Interval for comparing DateTime "
              + "values cannot be unassigned");
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Get the <tt>key</tt> converted to a DateMapKey for searching on a Map key. It
   * supports the following cases:<ul>
   * <li>key is instance of DateMapKey: return key</li>
   * <li>key is instance of DateTime: return new DateMapKey(key, this.interval), and </li>
   * <li>key is instance of Date: return new DateMapKey(new DateTime(key), this.interval)
   * </li>
   * </ul>
   * @param key the input
   * @return the converted DateMapKey or null if not supported.
   */
  private DateMapKey getDateKey(Object key) {
    DateMapKey result = null;
    if (key != null) {
      if (key instanceof DateMapKey) {
        result = (DateMapKey) key;
      } else if (key instanceof DateTime) {
        DateTime dt = (DateTime) key;
        result = new DateMapKey(dt, this.interval);
      } else if (key instanceof Date) {
        DateTime dt = new DateTime((Date) key);
        result = new DateMapKey(dt, this.interval);
      }
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="LinkHashMap Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }

  @Override
  public TValue put(DateMapKey key, TValue value) {
    if (key == null) {
      throw new NullPointerException("The Map Entry's Key is unassigned.");
    }
    TValue result = null;
    if (value == null) {
      if (this.containsKey(key)) {
        result = this.remove(key);
      }
    } else {
      result =  super.put(key, value);
    }
    return result;
  }

  /**
   * Set a value for a specified <tt>date</tt>. It initiates a DateMapKey using the
   * DateMap's <tt>interval</tt> and calls the super method passing in the key and value.
   * if (value = null) and an existing value exists, the key-value pair will be removed.
   * @param date the date for which to set the <tt>value</tt> (cannot be null)
   * @param value the date's value.
   * @return the previously set value for the date
   * @throws NullPointerException is date or value is null
   */
  public TValue put(DateTime date, TValue value) {
    if (date == null) {
      throw new NullPointerException("The Map Entry's Key is unassigned.");
    }
    DateMapKey key = new DateMapKey(date, this.interval);
    return this.put(key, value);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if <tt>map</tt> != null|empty, call {@linkplain
   * #put(bubblewrap.io.datetime.DateTime, java.util.List) this.put(key,value)} for
   * each key-&gt;value pair.</p>
   */
  @Override
  public void putAll(Map<? extends DateMapKey, ? extends TValue> map) {
    if ((map != null) && (!map.isEmpty())) {
      TValue value = null;
      for (DateMapKey key : map.keySet()) {
        value = map.get(key);
        this.put(key, value);
      }
    }
  }

  /**
   * Assign all the values of <tt>map</tt> with key as DateTime values, by calling
   * {@linkplain #put(bubblewrap.io.datetime.DateTime, java.lang.Object)
   * this.put(date,value)} for each value.</p>
   * @param map
   */
  public void putAllByDateTime(Map<DateTime, TValue> map) {
    if ((map != null) && (!map.isEmpty())) {
      TValue value = null;
      for (DateTime key : map.keySet()) {
        value = map.get(key);
        this.put(key, value);
      }
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert mapKey = {@linkplain #getDateKey(java.lang.Object) 
   * this.getDateKey(key)}, then check is the Map contains a mapKey and if true 
   * return the assigned value. Otherwise, return null without throwing an exception.
   * <p>
   * <b>NOTE:</b> key can be of type DateMapKey, DateTime, or Date</p>
   */
  @Override
  public TValue get(Object key) {
    TValue result = null;
    DateMapKey mapKey = this.getDateKey(key);
    if ((mapKey != null) && (super.containsKey(mapKey))) {
      result = super.get(key);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert mapKey = {@linkplain #getDateKey(java.lang.Object) 
   * this.getDateKey(key)} and return super.containsKey(mapKey)</p>
   * <p><b>NOTE:</b> key can be of type DateMapKey, DateTime, or Date</p>
   */
  @Override
  public boolean containsKey(Object key) {
    boolean result = false;
    DateMapKey mapKey = this.getDateKey(key);
    if (mapKey != null) {
      result = super.containsKey(mapKey);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Convert mapKey = {@linkplain #getDateKey(java.lang.Object) 
   * this.getDateKey(key)} and return super.remove(mapKey)</p>
   * <p><b>NOTE:</b> key can be of type DateMapKey, DateTime, or Date</p>
   */
  @Override
  public TValue remove(Object key) {
    TValue result = null;
    DateMapKey mapKey = this.getDateKey(key);
    if ((mapKey != null) && (super.containsKey(mapKey))) {
      result = super.remove(mapKey);
    }
    return result;
  }
  // </editor-fold>
}
