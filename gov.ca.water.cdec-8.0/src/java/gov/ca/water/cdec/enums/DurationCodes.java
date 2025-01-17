package gov.ca.water.cdec.enums;

/**
 * An Enum defining the CDEC Sensor Durations, which include:<ul>
 * <li>{@linkplain #H} - Hourly;
 *  importUrl: https://cdec.water.ca.gov/preciptemp/req/HourlyDataServlet</li>
 * http://cdec.water.ca.gov/preciptemp/req/HourlyDataServlet?Start=2012-03-01T00:00:00&End=2014-02-01T00:00:00&SensorNums=30&Stations=%27AGW%27
 * <li>{@linkplain #D} - Daily;
 *  importUrl: https://cdec.water.ca.gov/preciptemp/req/DailyDataServlet</li>
 * <li>{@linkplain #M} - Monthly;
 *  importUrl: https://cdec.water.ca.gov/preciptemp/req/MonthlyDataServlet</li>
 * <li>{@linkplain #E} - Event;
 *  importUrl: "https://cdec.water.ca.gov/preciptemp/req/EventDataServlet"</li>
 * http://cdec.water.ca.gov/preciptemp/req/EventDataServlet?Start=2000-01-01T00:00:00&End=2020-07-01T00:00:00&SensorNums=260&Stations=%27KGF%27
 * </ul>
 * Also see https://cdec.water.ca.gov/preciptemp/testLinks.jsp for more help.
 * @author J.G. "Koos" Prins, D.Eng. PE.  
 */
public enum DurationCodes {
  H("Hourly", "https://cdec.water.ca.gov/preciptemp/req/HourlyDataServlet"),
  D("Daily", "https://cdec.water.ca.gov/preciptemp/req/DailyDataServlet"),
  M("Monthly", "https://cdec.water.ca.gov/preciptemp/req/MonthlyDataServlet"),
  E("Event", "https://cdec.water.ca.gov/preciptemp/req/EventDataServlet");          
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  public final String name;
  public final String importUrl;
  /**
   * Public Constructor
   */
  private DurationCodes(String name, String importUrl) {
    this.name = name;
    this.importUrl = importUrl;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static methods">
  /**
   * Get the DurationCodes for a specified string.
   * @param durCode the string representative of the DurationCodes - either the character
   * (e.g. "H") or the DurationCodes.name (e.g. "Hourly")
   * @return the associated DurationCodes
   * @exception IllegalArgumentException if the input string is invalid or null|""
   */
  public static DurationCodes fromChar(String durCode) {
    DurationCodes result = null;
    durCode = ((durCode==null) || (durCode.trim().equals("")))? null: durCode.trim();
    if (durCode != null) {
      durCode = durCode.toUpperCase();
      if ((durCode.equals("H")) || (durCode.equals("HOURLY"))) {
        result = DurationCodes.H;
      } else if ((durCode.equals("D")) || (durCode.equals("DAILY"))) {
        result = DurationCodes.D;
      } else if ((durCode.equals("M")) || (durCode.equals("MONTHLY"))) {
        result = DurationCodes.M;
      } else if ((durCode.equals("E")) || (durCode.equals("EVENT"))) {
        result = DurationCodes.E;
      }
    }
    if (result == null) {
      throw new IllegalArgumentException("DurCode[" + durCode + "] is not supported.");
    }
    return result;
  }
  //</editor-fold>
}
