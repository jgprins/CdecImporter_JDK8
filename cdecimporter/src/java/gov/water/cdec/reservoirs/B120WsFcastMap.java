package gov.water.cdec.reservoirs;

import bubblewrap.io.datetime.DateRange;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.datetime.DateTimeComparator;
import bubblewrap.io.schedules.enums.Interval;
import gov.ca.water.cdec.core.DateKey;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A Map for maintaining a Reservoir's published B-120 Water Supply forecasts with each
 * forecast defined by a {@link B120WsFcast} record
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class B120WsFcastMap implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Map for maintaining a Reservoir published forecasts
   */
  private LinkedHashMap<DateKey, B120WsFcast> dataMap;
  /**
   * The DateRange for which Forecast are available
   */
  private DateRange dateRange;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public B120WsFcastMap() {
    super();  
    this.dataMap = new LinkedHashMap<>();
    this.dateRange = new DateRange(new DateTimeComparator(Interval.DAYS));
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.dataMap.clear();
  }
  // </editor-fold>
  
  private DateKey toKey(DateTime fcastDt) {
    return (fcastDt == null)? null: new DateKey(fcastDt.getAsDate());
  }
  
  //private List<DateKey> 

  // <editor-fold defaultstate="collapsed" desc="Public Map Delegate Methods">
  /**
   * Add the B120WsFcast to the map. Ignored if <tt>wsFcast</tt> = null or its fcastDt is
   * unassigned or the map already contains a forecast for this date.
   * @param wsFcast the forecast to add.
   */
  public void add(B120WsFcast wsFcast) {
    DateKey key = null;
    if ((wsFcast == null) || ((key = this.toKey(wsFcast.fcastDt)) == null) ||
        (this.dataMap.containsKey(key))) {
      return;
    }
    this.dataMap.put(key, wsFcast);
    this.dateRange.grow(wsFcast.fcastDt);
  }
  
  /**
   * Get the B120WsFcast entry for the specified date
   * @param fcastDt the forecast date
   * @return the forecast record if found or else null.
   */
  public B120WsFcast get(DateTime fcastDt) {
    B120WsFcast result = null;
    DateKey key = this.toKey(fcastDt);
    if ((key != null) && (this.dataMap.containsKey(key))) {
      result = this.dataMap.get(key);
    }
    return result;
  }
  
  /**
   * Get the B120WsFcast entry before or on specified date. If <tt>fcastDt</tt> == null,
   * return the latest forecast.
   * @param fcastDt the forecast date
   * @return the forecast record if found or else null.
   */
  public B120WsFcast getLastFcast(DateTime fcastDt) {
    B120WsFcast result = null;
    DateKey key = null;
    if ((this.dateRange != null) || (!this.dateRange.isEmpty())) {
      if ((fcastDt == null) || 
                            (!fcastDt.isBefore(this.dateRange.getMax(),Interval.DAYS))) {
        key = this.toKey(this.dateRange.getMax());
      } else if (!fcastDt.isBefore(this.dateRange.getMin(), Interval.DAYS)) {
        key = this.toKey(fcastDt);
      }
    } else {
      key = this.toKey(fcastDt);
    }
    
    if ((key != null) && (!this.isEmpty())) {
      if (this.dataMap.containsKey(key)) {
        result = this.dataMap.get(key);
      } else {
        List<DateKey> keyList = new ArrayList<>(this.dataMap.keySet());
        Collections.sort(keyList);
        DateKey listKey = null;
        for (int iKey = (keyList.size()-1); iKey >= 0; iKey--) {
          listKey = keyList.get(iKey);
          if ((listKey != null) && (!listKey.isAfter(key))) {
            result = this.dataMap.get(listKey);
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Get the Map contains a B120WsFcast entry for the specified date
   * @param fcastDt the forecast date
   * @return true is a match were found
   */
  public boolean contains(DateTime fcastDt) {
    DateKey key = this.toKey(fcastDt);
    return ((key != null) && (this.dataMap.containsKey(key)));
  }

  /**
   * Remove stored forecasts.
   */
  public void clear() {
    this.dataMap.clear();
  }

  /**
   * Get whether the map is empty
   * @return this.dataMap.isEmpty()
   */
  public boolean isEmpty() {
    return this.dataMap.isEmpty();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
}
