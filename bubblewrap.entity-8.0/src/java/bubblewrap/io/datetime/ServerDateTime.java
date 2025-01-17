package bubblewrap.io.datetime;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class extends the DateTime class by supporting the server's default TimeZone.
 * @author kprins
 * @see DateTime
 */
public class ServerDateTime extends DateTime {

// <editor-fold defaultstate="collapsed" desc="Static methods">
  /**
   * This uses a calendar that should take into account the server's local time and
   * generate an appropriate UTC DateTime.  Beware that the "Server" may not have
   * the same configuration as the local user, so be cautious when using this option.
   * @return DateTime
   */
  public static DateTime getNow() {
    Calendar cal = Calendar.getInstance();
    return new ServerDateTime(cal.getTimeInMillis());
  }
  // </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Default Constructor
   */
  public ServerDateTime() {
    super();
  }

  /**
   * Constructor with specified MilliSceonds
   * @param lMilliSeconds long
   */
  public ServerDateTime(long lMilliSeconds) {
    super(lMilliSeconds);
  }

  /**
   * Constructor with specified Calendar
   * @param pCalendar Calendar
   */
  public ServerDateTime(Calendar pCalendar) {
    super(pCalendar);
  }

  /**
   * Constructor with specified Date
   * @param pDate Date
   */
  public ServerDateTime(Date pDate) {
    super(pDate);
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="DateTime Overrides">
  /**
   * OVERRIDE: get the ServerDateTime's TimeZoen as the Default TiemZone
   * @return TimeZone
   */
  @Override
  public TimeZone getTimeZone() {
    return TimeZone.getDefault();
  }
// </editor-fold>
}
