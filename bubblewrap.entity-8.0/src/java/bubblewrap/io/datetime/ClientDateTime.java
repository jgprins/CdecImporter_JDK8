package bubblewrap.io.datetime;

import bubblewrap.http.session.SessionHelper;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class extends the DateTime class by supporting a specified TimeZone and using
 * HTTPRequest's Locale for formating the toLocaleString datetime.
 * @author kprins
 * @see DateTime
 */
public class ClientDateTime extends DateTime {

// <editor-fold defaultstate="collapsed" desc="Static methods">
  /**
   * This uses a calendar that should take into account the server's local time and
   * generate an appropriate UTC DateTime.  Beware that the "Server" may not have
   * the same configuration as the local user, so be cautious when using this option.
   * @return DateTime
   */
  public static DateTime getNow() {
    Calendar cal = Calendar.getInstance();
    return new ClientDateTime(cal.getTimeInMillis());
  }
  /**
   * This uses a calendar that should take into account the server's local time and
   * generate an appropriate UTC DateTime.  Beware that the "Server" may not have
   * the same configuration as the local user, so be cautious when using this option.
   * @return DateTime
   */
  public static DateTime getNow(TimeZone pTimeZone) {
    Calendar cal = Calendar.getInstance();
    return new ClientDateTime(cal.getTimeInMillis());
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placehodler for the Client TimeZoneID - if Undefined, use TimeZone.getDefault()
   */
  private String msTimeZoneId = null;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Default Constructor
   */
  public ClientDateTime() {
    super();
  }

  /**
   * Constructor for existign time for a specifie TimeZone
   * @param pTimeZone TimeZone
   */
  public ClientDateTime(TimeZone pTimeZone) {
    super();
    this.msTimeZoneId = (pTimeZone == null)? null: pTimeZone.getID();
  }

  /**
   * Constructor with specified MilliSceonds
   * @param lMilliSeconds long
   */
  public ClientDateTime(long lMilliSeconds) {
    super(lMilliSeconds);
  }

  /**
   * Constructor with specified MilliSceonds and TiemZone
   * @param lMilliSeconds long
   * @param pTimeZone TimeZone
   */
  public ClientDateTime(long lMilliSeconds, TimeZone pTimeZone) {
    super(lMilliSeconds);
    this.msTimeZoneId = (pTimeZone == null)? null: pTimeZone.getID();
  }

  /**
   * Constructor with specified Calendar - using the Calendar's Time Zone
   * @param pCalendar Calendar
   */
  public ClientDateTime(Calendar pCalendar) {
    super(pCalendar);
    TimeZone pTimeZone = pCalendar.getTimeZone();
    this.msTimeZoneId = (pTimeZone == null)? null: pTimeZone.getID();
  }

  /**
   * Constructor with specified Date and TimeZone
   * @param pDate Date
   */
  public ClientDateTime(Date pDate, TimeZone pTimeZone) {
    super(pDate);
    this.msTimeZoneId = (pTimeZone == null)? null: pTimeZone.getID();
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="DateTime Overrides">
  /**
   * OVERRIDE: Get the ClientDateTime's TimeZone. If the TiemZone is not set it returns
   * the Default TimeZone
   * @return TimeZone
   */
  @Override
  public TimeZone getTimeZone() {
    TimeZone pTimeZone = null;
    if (this.msTimeZoneId != null) {
      pTimeZone  =TimeZone.getTimeZone(this.msTimeZoneId);
    }
    return (pTimeZone == null)? TimeZone.getDefault(): pTimeZone;
  }

  /**
   * OVERRIDE: Return the HttpReuest's Locale or if undefined, return the default locale.
   * @return Locale
   */
  @Override
  public Locale getLocale() {
    Locale pLocale = SessionHelper.getRequestLocale();
    return (pLocale == null)? Locale.getDefault(): pLocale;
  }
// </editor-fold>
}
