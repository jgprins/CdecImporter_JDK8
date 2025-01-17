package bubblewrap.io.wateryr;

import bubblewrap.io.IntegerRange;
import bubblewrap.io.datetime.DateTime;
import java.util.TimeZone;

/**
 * A no-year specific Water Year "date" - defined by its WyMonth (0..11, with 0 = Oct) and
 * a monthDay (1..28/30/31) depending on the month. See {@linkplain #getDayRange(int)}
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class WyMonthDay {

  // <editor-fold defaultstate="collapsed" desc="Public Static WY Months">
  public static final int OCT = 0;
  public static final int NOV = 1;
  public static final int DEC = 2;
  public static final int JAN = 3;
  public static final int FEB = 4;
  public static final int MAR = 5;
  public static final int APR = 6;
  public static final int MAY = 7;
  public static final int JUN = 8;
  public static final int JUL = 9;
  public static final int AUG = 10;
  public static final int SEP = 11;
  public static final int NEXT_OCT = 12;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the default (for any Water Year Date range for the specified wyMonth
   *
   * @param wyMonth Water Year Month [0..11] with 0 = October
   * @return the Day range [1..28/30/31] depending on the wyMonth
   */
  public static IntegerRange getDayRange(int wyMonth) {
    IntegerRange result = null;
    switch (wyMonth) {
      case 0: //Oct
      case 2: //Dec
      case 3: //Jan
      case 5: //Mar
      case 7: //May
      case 9: //Jul
      case 10: //Aug
        result = new IntegerRange(1, 31);
        break;
      case 1: //Nov
      case 6: //Apr
      case 8: //Jun
      case 11: //Sep
        result = new IntegerRange(1, 30);
        break;
      case 12: // Oct for next Water Year - only the first day
        result = new IntegerRange(1, 1);
        break;
      case 4: //Feb
        result = new IntegerRange(1, 28);
        break;
      default:
        throw new AssertionError("Month index[" + wyMonth + "] out of Range[0..11].");
    }
    return result;
  }

  /**
   * Static Constructor
   *
   * @param wyMonth the Water Year Month range{0..11} with 0 = October
   * @param monthDay the Water Year Month-monthDay range{1..30/31/28} based on the wyMonth
   * @return a new WyMonthDay instance
   */
  public WyMonthDay init(int wyMonth, int monthDay) {
    return new WyMonthDay(wyMonth, monthDay);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Water Year Month range{0..11} with 0 = October
   */
  public final int wyMonth;
  /**
   * The Water Year Month-monthDay range{1..30/31/28} based on the wyMonth
   */
  public final int monthDay;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   *
   * @param wyMonth the Water Year Month range{0..11} with 0 = October
   * @param monthDay the Water Year Month-Day (a 1-base day index in the
   * range{1..30/31/28(29)} based on the wyMonth).
   */
  public WyMonthDay(int wyMonth, int monthDay) {
    IntegerRange dayRng = WyMonthDay.getDayRange(wyMonth);
    if (!dayRng.inRange(monthDay)) {
      throw new IllegalArgumentException("Day[" + monthDay + "] for WY Month[" + wyMonth
              + "] is out of " + dayRng.toString());
    }
    this.monthDay = monthDay;
    this.wyMonth = wyMonth;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Date for this WyMonthDay
   *
   * @param waterYr the Water year for which to generate the
   * @param timeZone (Optional) time zone - default = "PST")
   * @return the date (at midnight) for the given WyMonthDay
   */
  public final DateTime toDate(int waterYr, TimeZone timeZone) {
    int month = (this.wyMonth < 3) ? this.wyMonth + 9 : this.wyMonth - 3;
    int year = (this.wyMonth < 3) ? waterYr - 1 : waterYr;
    timeZone = (timeZone == null) ? WyConverter.PstTimeZone : timeZone;
    DateTime result = new DateTime(year, month, this.monthDay, timeZone);
    return result;
  }

  /**
   * Get the Water Year Day for this WyMonthDay (i.e., this index of this date in the
   * water year - considering leap years.
   *
   * @param waterYr the Water year for which to generate the
   * @param timeZone (Optional) time zone - default = "PST")
   * @return the water year monthDay index (0 = Oct 1).
   */
  public final int toWyDay(int waterYr, TimeZone timeZone) {
    DateTime thisDt = this.toDate(waterYr, timeZone);
    return WyConverter.toWyDay(thisDt);
  }

  /**
   * Gets the previous water year month.
   *
   * @return
   */
  public WyMonthDay previous() {
    WyMonthDay result = (this.wyMonth == 0)
            ? (this.init(wyMonth - 1, monthDay)) : this.init(11, monthDay);
    return result;
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: "WyMonthDay[ month = " + this.wyMonth + "; monthDay = " + this.monthDay +
   * "]"</p>
   */
  @Override
  public String toString() {
    return "WyMonthDay[ month = " + this.wyMonth + "; day = " + this.monthDay + "]";
  }
  // </editor-fold>

}
