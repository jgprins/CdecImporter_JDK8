package bubblewrap.io.datetime;

import bubblewrap.io.DataEntry;
import bubblewrap.io.schedules.enums.Interval;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * An ArrayList for managing DateTime values. The list maintains a DateRange of all
 * added values and allow item comparison using an {@linkplain Interval} for checking
 * for date equality.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateList extends ArrayList<DateTime> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The list's DateRange
   */
  private DateRange dateRange;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public DateList() {
    super();  
  }
  
  /**
   * Constructor from a list of dates
   * @param dates a collection of dates (the list is empty if null | empty)
   */
  public DateList(Collection<DateTime> dates) {
    this();
    if ((dates != null) && (!dates.isEmpty())) {
      this.addAll(dates);
    }
  }
  
  /**
   * Constructor from a list of dates
   * @param dates an array of dates (the list is empty if null | empty)
   */
  public DateList(DateTime...dates) {
    this();
    if ((dates != null) && (dates.length > 0)) {
      this.addAll(Arrays.asList(dates));
    }
  }
  
  /**
   * Constructor from a DateRange with values at the specified <tt>interval</tt>
   * @param dateRange a date range (the list is empty if null | empty).
   * @param interval the values interval
   * @throws IllegalArgumentException if interval = null | NONE
   */
  public DateList(DateRange dateRange, Interval interval) {
    this();
    if ((interval == null) || (Interval.NONE.equals(interval))) {
      throw new IllegalArgumentException("The Date Intervale cannot be unassigned or "
              + "NONE");
    }
    
    if ((dateRange != null) && (!dateRange.isEmpty())) {
      DateTime curDt = dateRange.getMin();
      while (dateRange.inRange(curDt)) {
        this.add(curDt);
        curDt = curDt.addTime(0l, interval.timeUnit);
      }
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return a copy of the list's date range
   * @return this.dateRange.clone()
   */
  public DateRange getDateRange() {
    return (DateRange) this.dateRange.clone();
  }
  
  /**
   * Call to sot the list for a specified <tt>interval</tt> and <tt>sortArc</tt> sort 
   * order. It initiates a DateTimeComparator and call {@linkplain #sort(
   * bubblewrap.io.datetime.DateTimeComparator) this.sort(comparator)}.
   * @param interval the date-time comparison interval
   * @param sortAsc the sort order (true = sort in ascending order).
   */
  public void sort(Interval interval, boolean sortAsc) {
    interval = (interval == null)? Interval.MILLISECONDS: interval;
    DateTimeComparator comparator = new DateTimeComparator(interval, sortAsc);
    this.sort(comparator);
  }
  
  /**
   * Called to sort the list using a <tt>comparator</tt>.
   * @param comparator the sort comparator. Using default (millisecond) sort if undefined.
   */
  public void sort(DateTimeComparator comparator) {
    if (this.size() > 1) {
      if (comparator == null) {
        Collections.sort(this);
      } else {
        Collections.sort(this, comparator);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="ArrayList Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method and if this list has changed, grow this.dateRange</p>
   */
  @Override
  public boolean addAll(int index, Collection<? extends DateTime> collection) {
    boolean result = super.addAll(index, collection); 
    if (result) {
      DateTime[] dates = DataEntry.toArray(DateTime.class, collection);
      this.dateRange.grow(dates);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method and if this list has changed, grow this.dateRange</p>
   */
  @Override
  public final boolean addAll(Collection<? extends DateTime> collection) {
    boolean result = super.addAll(collection); 
    if (result) {
      DateTime[] dates = DataEntry.toArray(DateTime.class, collection);
      this.dateRange.grow(dates);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call the super method and then call this.dateRange.grow(date)</p>
   */
  @Override
  public void add(int index, DateTime date) {
    super.add(index, date); 
    this.dateRange.grow(date);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call the super method and then call this.dateRange.grow(date)</p>
   */
  @Override
  public final boolean add(DateTime date) {
    boolean result = super.add(date); 
    if (result) {
      this.dateRange.grow(date);
    }
    return result;
  }

  /**
   * Remove all item form this list that matches the DateTime represented by <tt>obj</tt>.
   * if (obj = null | instance of DateTime), it casts date = (DateTime) obj and call
   * {@linkplain #removeAll(bubblewrap.io.schedules.enums.Interval,
   * bubblewrap.io.datetime.DateTime...) this.removeAll(NONE, date)}
   * @param obj the DateTime object to remove (can be null)
   * @return true if the list has changed
   */
  @Override
  public boolean remove(Object obj) {
    boolean result = false;
    DateTime date = null;
    if (obj == null) {
      result = this.removeAll(Interval.NONE, date);
    } else if (obj instanceof DateTime) {
      date = (DateTime) obj;
      result = this.removeAll(Interval.NONE, date);
    }
    return result;
  }
  
  /**
   * Remove all items from this list with matching items in <tt>dateArr</tt> using
   * the specified <tt>interval</tt> for the dateTime comparison.
   * @param interval the Interval for the dateTime comparison
   * @param dateArr the collection of DateTime items to remove
   * @return true if the list has changed. 
   */
  public boolean removeAll(Interval interval, DateTime...dateArr) {
    boolean result = false;
    if ((dateArr != null) && (dateArr.length > 0)) {
      result = this.removeAll(interval, Arrays.asList(dateArr));
    }
    return result;
  }
  
  /**
   * Remove all items from the list with matching items in <tt>collection</tt> using
   * the specified <tt>interval</tt> for the dateTime comparison.
   * <p>
   * <b>NOTE:</b> This.dateRange will be rebuilt.</p>
   * @param interval the Interval for the dateTime comparison
   * @param collection the collection of items to remove
   * @return true if the list has changed. 
   */
  public boolean removeAll(Interval interval, Collection<DateTime> collection) {
    boolean result = false;
    boolean rangeChanged = false;
    if ((collection != null) && (!collection.isEmpty())) {
      int index = -1;
      for (DateTime date : collection) {
        while ((index = this.indexOf(date, interval)) >= 0) {
          rangeChanged =  ((rangeChanged) || ((this.remove(index)) != null));
          result = true;
        }
      }
    }
    
    if (rangeChanged) {
      this.dateRange.reset();
      this.dateRange.grow(this);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Build a removeList of DateTime values from <tt>collection</tt> and call 
   * {@linkplain #removeAll(bubblewrap.io.schedules.enums.Interval, 
   * java.util.Collection) this.removeAll(Interval.NONE,removeList}</p>
   */
  @Override
  public boolean removeAll(Collection<?> collection) {  
    boolean result = false;
    if ((collection != null) && (!collection.isEmpty())) {
      List<DateTime> removeList = new ArrayList<>();
      DateTime date = null;
      for (Object obj : collection) {
        if (obj == null) {
          if (!removeList.contains(null)) {
            removeList.add(null);
          }
        } else if (obj instanceof DateTime) {
          date = (DateTime) obj;
          if (!removeList.contains(date)) {
            removeList.add(date);
          }
        }
      }
      result = this.removeAll(Interval.NONE, removeList);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method and this.dateRange.reset()</p>
   */
  @Override
  public void clear() {
    super.clear(); 
    this.dateRange.reset();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: cast <tt>obj</tt> as <tt>date</tt> of type DateTime and return
   * {@linkplain #indexOf(bubblewrap.io.datetime.DateTime, 
   * bubblewrap.io.schedules.enums.Interval) this.indexOf(date,null)}. Return -1 if
   * <tt>obj != null and not an instance of DateTime</tt></p>
   */
  @Override
  public int indexOf(Object obj) {
    int result = -1;
    if (!this.isEmpty()) {
      DateTime date = null; 
      if (obj == null) {
        result = this.indexOf(date, null);
      } else if (obj instanceof DateTime) {
        date = (DateTime) obj;
        result = this.indexOf(date, null);
      }
    }
    return result; 
  }
  
  /**
   * Get the first index of <tt>date</tt> using the specified <tt>interval</tt> to compare
   * list items with <tt>date</tt>
   * @param date the DateTime value to match (can be null to find null values)
   * @param interval the Interval to use in the comparison (assume milliseconds if 
   * unassigned or NONE).
   * @return the index of the first matching item.
   */
  public int indexOf(DateTime date, Interval interval) {
    int result = -1;
    if (!this.isEmpty()) {
      if (date == null) {
        for (int i = 0; i < this.size(); i++) {
          if (this.get(i) == null) {
            result = i;
            break;
          }
        }
      } else {
        DateTime item = null;
        for (int i = 0; i < this.size(); i++) {
          if (((item = this.get(i)) != null) && (DataEntry.isEq(date, item, interval))) {
            result = i;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: cast <tt>obj</tt> as <tt>date</tt> of type DateTime and return
   * {@linkplain #lastIndexOf(bubblewrap.io.datetime.DateTime, 
   * bubblewrap.io.schedules.enums.Interval) this.indexOf(date,null)}. Return -1 if
   * <tt>obj != null and not an instance of DateTime</tt></p>
   */
  @Override
  public int lastIndexOf(Object obj) {
    int result = -1;
    if (!this.isEmpty()) {
      DateTime date = null; 
      if (obj == null) {
        result = this.lastIndexOf(date, null);
      } else if (obj instanceof DateTime) {
        date = (DateTime) obj;
        result = this.lastIndexOf(date, null);
      }
    }
    return result; 
  }
  
  /**
   * Get the last index of <tt>date</tt> using the specified <tt>interval</tt> to compare
   * list items with <tt>date</tt>
   * @param date the DateTime value to match (can be null to find null values)
   * @param interval the Interval to use in the comparison (assume milliseconds if 
   * unassigned or NONE).
   * @return the index of the last matching item.
   */
  public int lastIndexOf(DateTime date, Interval interval) {
    int result = -1;
    if (!this.isEmpty()) {
      if (date == null) {
        for (int i = this.size()-1; i >= this.size(); i--) {
          if (this.get(i) == null) {
            result = i;
            break;
          }
        }
      } else {
        DateTime item = null;
        for (int i = this.size()-1; i >= this.size(); i--) {
          if (((item = this.get(i)) != null) && (DataEntry.isEq(date, item, interval))) {
            result = i;
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: returns (this.indexOf(obj) &ge; 0)
   */
  @Override
  public boolean contains(Object obj) {
    return (this.indexOf(obj) >= 0);
  }

  /**
   * Check if the list contains <tt>obj</tt>
   * @param interval the Interval to use in the comparison (assume milliseconds if 
   * unassigned or NONE).
   * @param obj the list item to search for (can be null)
   * @return (this.indexOf(date, interval) &ge; 0)
   */
  public boolean contains(Interval interval, DateTime date) {
    return (this.indexOf(date, interval) >= 0);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: returns true if collection != null | empty and all index of all items 
   * (i.e., is index = this.indexOf(obj)) &ge; 0.
   */
  @Override
  public boolean containsAll(Collection<?> collection) {
    boolean result = false;
    if ((collection != null) && (!collection.isEmpty())) {
      result = true;
      for (Object obj : collection) {
        if (this.indexOf(obj) < 0) {
          result = false;
          break;
        }
      }
    }
    return result; 
  }
  
  /**
   * Check if all items in <tt>collection</tt> has a matching item in this list, using
   * the <tt>interval</tt> for comparing the DateTime values
   * @param interval for comparing the DateTime values
   * @param collection the list if DateTime to check for.
   * @return true if collection != null | empty and all index of all items 
   * (i.e., is index = this.indexOf(obj)) &ge; 0. 
   */
  public boolean containsAll(Interval interval, Collection<DateTime> collection) {
    boolean result = false;
    if ((collection != null) && (!collection.isEmpty())) {
      result = true;
      for (DateTime date : collection) {
        if (this.indexOf(date, interval) < 0) {
          result = false;
          break;
        }
      }
    }
    return result; 
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a DateTime array of the items in this list.</p>
   */
  @Override
  public DateTime[] toArray() {   
    DateTime[] result = null;
    if (!this.isEmpty()) {
      result = DataEntry.toArray(DateTime.class, this);
    }
    return (result == null)? new DateTime[0]: result;
  }
  // </editor-fold>
}
