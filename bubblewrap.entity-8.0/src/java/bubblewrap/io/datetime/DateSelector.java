package bubblewrap.io.datetime;

import bubblewrap.core.events.EventHandler;
import bubblewrap.core.events.EventArgs;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.TimeZone;

/**
 * A Date Selector bean that allow user's to select a date within a Date range 
 * {@linkplain #getMinDate() this.minDate} and {@linkplain #getMaxDate() this.maxDate}.
 * If no date is selected {@linkplain #getDefaultDate() this.defaultDate} will be shown 
 * as the selected (focus date).
 * <p>
 * <b>NOTE:</b> All dates are converted to zero-hour dates of the selector's assigned 
 * (or default - UTC timezone).
 * </p>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateSelector implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Minimum Selectable Date 
   */
  private DateTime minDate;
  private DateTime maxDate;
  private DateTime selectedDate;
  private DateTime defaultDate;
  private TimeZone timeZone;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public events">
  /**
   * The EventHandler that fires the Date Changed Event.
   */
  public final EventHandler DateChanged;
  
  /**
   * Called to fire the Date Changed Event.
   *
   * @param eventInfo the event info
   */
  protected final void fireDateChanged() {
    this.DateChanged.fireEvent(this, new EventArgs());
  }
//</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public DateSelector() {
    super();  
    this.DateChanged = new EventHandler();
    this.minDate = null;
    this.maxDate = null;
    this.selectedDate = null;
    this.timeZone = null;
  }
  
  /**
   * Public Constructor  with a dedicated TimeZone
   */
  public DateSelector(TimeZone timeZone) {
    this();
    this.timeZone = timeZone;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method before clearing this.DateChanged listeners</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.DateChanged.clear();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Set the Minimum and maximum Selection ranges
   * @param minDate the minimum date (can be null is no lower limit)
   * @param maxDate the maximum date (can be null is no upper limit)
   */
  public void setDateRange(DateTime minDate, DateTime maxDate) {
    this.setDateRange(minDate, maxDate, null);
  }
  
  /**
   * Set the Minimum and maximum Selection ranges
   * @param minDate the minimum date (can be null is no lower limit)
   * @param maxDate the maximum date (can be null is no upper limit)
   * @param defaultDate the default date if none is selected (can be null to use today,
   * the minDate, or the maxDate - see {@linkplain #getDefaultDate() this.defaultDate}
   * for more info.
   */
  public void setDateRange(DateTime minDate, DateTime maxDate, DateTime defaultDate) {
    minDate = (minDate == null)? null: 
                        DateTime.toZeroHourInTimeZone(minDate, this.getTimeZone());
    maxDate = (maxDate == null)? null: 
                        DateTime.toZeroHourInTimeZone(maxDate, this.getTimeZone());
    if ((minDate != null) && (maxDate != null) && (minDate.isAfter(maxDate))) {
      DateTime tmpDt = minDate;
      minDate = maxDate;
      maxDate = tmpDt;
    }
    this.minDate = minDate;
    this.maxDate = maxDate;
    if (defaultDate != null) {
      defaultDate = DateTime.toZeroHourInTimeZone(defaultDate, this.getTimeZone());
      if ((this.minDate != null) && (defaultDate.isBefore(this.minDate))) {
        defaultDate = this.minDate;
      }
      if ((this.maxDate != null) && (defaultDate.isAfter(this.maxDate))) {
        defaultDate = this.maxDate;
      }
      this.defaultDate = defaultDate;
    }
  }
  
  /**
   * Get the Selector's TimeZone (default = UTC/Zulu Time)
   * @return (this.timeZone == null)? DateTime.UTCTimeZone: this.timeZone
   */
  public TimeZone getTimeZone() {
    return (this.timeZone == null)? DateTime.UTCTimeZone: this.timeZone;
  }

  /**
   * Get the Minimum Date
   * @return the assigned date (can be null)
   */
  public DateTime getMinDate() {
    return this.minDate;
  }

  /**
   * Get the Maximum Date
   * @return the assigned date (can be null)
   */
  public DateTime getMaxDate() {
    return this.maxDate;
  }

  /**
   * Get the Selected Date
   * @return the assigned date (can be null)
   */
  public DateTime getSelectedDate() {
    return this.selectedDate;
  }

  /**
   * Get the Default Date - if undefined it will be initiated as today or this.minDate or
   * this.maxDate depending whether today is before this.minDate or after this.maxDate,
   * respectively.
   * <p>
   * <b>NOTE:</b> The default date can  be initiated using the {@linkplain 
   * #setDateRange(bubblewrap.io.datetime.DateTime, bubblewrap.io.datetime.DateTime,
   * bubblewrap.io.datetime.DateTime) this.setDateRange} method.</p>
   * @return the assigned date (will not be null)
   */
  public DateTime getDefaultDate() {
    if (this.defaultDate == null) {
      this.defaultDate = DateTime.zeroHourToday(this.getTimeZone());
      if ((this.minDate != null) && (this.defaultDate.isBefore(this.minDate))) {
        this.defaultDate = this.minDate;
      } 
      if ((this.maxDate != null) && (this.defaultDate.isAfter(this.maxDate))) {
        this.defaultDate = this.maxDate;
      }
    }
    return this.defaultDate;
  }

  /**
   * Set the Selected Date. If <tt>selectedDate</tt> is out of the Date range, it 
   * is set to null. If this.selectedDate change, it fires the DateChanged event
   * @param selectedDate the new selected date
   */
  public void setSelectedDate(DateTime selectedDate) {
    selectedDate = DateTime.toZeroHourInTimeZone(selectedDate, this.getTimeZone());
    if (selectedDate != null) {
      if ((this.minDate != null) && (selectedDate.isBefore(this.minDate))) {
        selectedDate = null;
      }
    }
    if (selectedDate != null) {
      if ((this.maxDate != null) && (selectedDate.isAfter(this.maxDate))) {
        selectedDate = null;
      }
    }
    if (!DataEntry.isEq(this.selectedDate, selectedDate)) {
      this.selectedDate = selectedDate;
      this.fireDateChanged();
    }
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
