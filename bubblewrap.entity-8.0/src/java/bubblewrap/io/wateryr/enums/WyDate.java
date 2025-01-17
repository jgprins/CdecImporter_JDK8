package bubblewrap.io.wateryr.enums;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.wateryr.WyConverter;
import bubblewrap.io.wateryr.WyMonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import javax.faces.model.SelectItem;

/**
 * A set of standard WY Dates (e.g. Oct 1, Jan 1, ...). Can by use to get the
 * WaterYear-Day and/or the the Date for a specified Water Year.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum WyDate {
  // <editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * Oct 1 (Start of Water Year - Default Option)
   */
  Oct1(0, "Oct 1", 9, 1),
  /**
   * Nov 1 (Start of Water Year - Default Option)
   */
  Nov1(1, "Nov 1", 10, 1),
  /**
   * Dec 1 (Start of Water Year - Default Option)
   */
  Dec1(2, "Dec 1", 11, 1),
  /**
   * Jan 1
   */
  Jan1(3, "Jan 1", 0, 1),
  /**
   * Feb 1
   */
  Feb1(4, "Feb 1", 1, 1),
  /**
   * Mar 1
   */
  Mar1(5, "Mar 1", 2, 1),
  /**
   * Apr 1
   */
  Apr1(6, "Apr 1", 3, 1),
  /**
   * May 1
   */
  May1(7, "May 1", 4, 1),
  /**
   * Jun 1
   */
  Jun1(8, "Jun 1", 5, 1),
  /**
   * Jul 1
   */
  Jul1(9, "Jul 1", 6, 1),
  /**
   * Aug 1
   */
  Aug1(10, "Aug 1", 7, 1),
  /**
   * Sep 1
   */
  Sep1(11, "Sep 1", 8, 1),

  /**
   * Sep 30 (end of WaterYear)
   */
  Sep30(12, "Sep 30", 8, 30);
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * A Defined enum value (not its ordinate)
   */
  public final int value;
  /**
   * A Display label for the enum option
   */
  public final String label;
  /**
   * The Date's Month (Jan=0...Dec=11)
   */
  public final int month;
  /**
   * The Day-of-the-month (1..31)
   */
  public final int day;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Private Constructor
   * @param value the option value
   * @param label the option label
   */
  private WyDate(int value, String label, int month, int day) {
    this.value = value;
    this.label = label;
    this.month = month;
    this.day = day;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the WyDate option's Water-Day (day since Oct 1 with Oct 1 = 0) for the
   * specified Water Year.
   * @param waterYr the Water Year
   * @return {@link WyConvert#toWyDay(int, int, int, java.util.TimeZone)
   * WyConverter.toWyDay(waterYr, this.month, this.day, WyConverter.PstTimeZone)}
   */
  public int getWyDay(int waterYr) {
    return WyConverter.toWyDay(waterYr, this.month, this.day, WyConverter.PstTimeZone);
  }

  /**
   * Get the WyDate option's Date (Local time with no time stamp) for the
   * specified Water Year. TimeZone = {@linkplain WyConverter#PstTimeZone}.
   * @param waterYr the Water Year
   * @return {@link WyConvert#fromWyDay(int, int, java.util.TimeZone)
   * WyConverter.fromWyDay(wyDay, waterYr, WyConverter.PstTimeZone)}
   */
  public DateTime getWyDate(int waterYr) {
    TimeZone timeZone = WyConverter.PstTimeZone;
    DateTime result = this.getWyDate(waterYr, timeZone);
    return result;
  }

  /**
   * Get the WyDate option's Date (Local time with no time stamp) for the
   * specified Water Year. TimeZone = {@linkplain WyConvert#PstTimeZone}.
   * @param waterYr the Water Year
   * @param timeZone timezone of the target date.
   * @return {@link WyConvert#fromWyDay(int, int, java.util.TimeZone)
   * WyConverter.fromWyDay(wyDay, waterYr, WyConverter.PstTimeZone)}
   */
  public DateTime getWyDate(int waterYr, TimeZone timeZone) {
    int wyDay = this.getWyDay(waterYr);
    DateTime fromWyDay = WyConverter.fromWyDay(wyDay, waterYr, TimeZone.getTimeZone("UTC"));
    DateTime result = DateTime.toLocalDate(DateTime.toTimeZoneDate(fromWyDay, timeZone));
    return result;
  }

  /**
   * Get the previous WyMonthDay.  For example, the previous to Sep1 is Aug1 and so on.
   * However, the previous to Oct1 is Sep30.
   * @return
   */
  public WyDate previous() {
    WyDate result;
    if (this.value == 0) {
      result = WyDate.fromValue(12);
    } else {
      result = WyDate.fromValue(this.value-1);
    }
    return result;
  }


  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the WyDate associated with <tt>value</tt>
   * @param value the WyDate.value to search for
   * @return the matching WyDate or NONE if not found.
   */
  public static WyDate fromValue(Integer value) {
    WyDate result = WyDate.Oct1;
    if (value != null) {
      for (WyDate enumVal : WyDate.values()) {
        if (enumVal.value == value) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Get the WyDate associated with <tt>wyDay</tt>
   * @param waterYr the Water Year to search for
   * @param wyDay the WyDate.value to search for
   * @return the matching StartEndDates or NONE if not found.
   */
  public static WyDate fromWyDay(Integer waterYr, Integer wyDay) {
    WyDate result = WyDate.Oct1;
    if ((waterYr != null) && (wyDay != null)) {
      for (WyDate enumVal : WyDate.values()) {
        if (DataEntry.isEq(enumVal.getWyDay(waterYr),wyDay)) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }

  /**
   * A utility method to generate a SelectItem list for the specified set of dates.
   * The list always start with SelectItem(null, "Select...")
   * @param dates the array of dates
   * @return the SelectItem list
   */
  public static List<SelectItem> getSelectItems(WyDate...dates) {
    List<SelectItem> result = new ArrayList<>();
    if ((dates != null) && (dates.length > 0)) {
      result.add(new SelectItem(null, "Select..."));
      for (WyDate chartDt : dates) {
        result.add(new SelectItem(chartDt.value, chartDt.label));
      }
    }
    return result;
  }
  // </editor-fold>


}
