package bubblewrap.io.datetime;

/**
 * Created on Nov 12, 2010 at 11:36:22 AM
 * @author Harold A. Dunsford Jr. Ph.D.
 * hdunsford@geiconsultants.com
 * This exception is a checked exception that can be thrown if a date
 * in terms of day, month and year are specified in a way that don't
 * correspond to a calendar date.
 */
public class InvalidDateException extends Exception {

  private final int year;
  private final int month;
  private final int day;
  private final int hour;
  private final int minute;
  private final double seconds;


  /**
   * Initializes a new instance of the InvalidDateException class.
   */
  public InvalidDateException(int year, int month, int day){
    super("The date specified by year: " + year + ", month: " + month
            + ", and day: " + day + " do not represent a valid calendar"
            + " date.  Month should range from 1 for Januar to 12 for"
            + " December, and day be the day of the month ranging from"
            + " 1 to a maximum of 31, depending on the month and leap"
            + " year.");
     this.year = year;
     this.month = month;
     this.day = day;
     this.hour = 0;
     this.minute =0;
     this.seconds = 0;
  }

  /**
   * Set the time.
   * @param hour
   * @param minute
   * @param seconds
   */
  public InvalidDateException(int hour, int minute, double seconds)
  {
     super("The time specified by hour: " + hour + ", minute: " + minute
            + ", and second: " + seconds + " do not represent a valid time.");
     this.year = 0;
     this.month = 0;
     this.day = 0;
     this.hour = hour;
     this.minute = minute;
     this.seconds = seconds;
  }

  /**
   * @return the year
   */
  public int getYear() {
    return year;
  }

  /**
   * @return the month
   */
  public int getMonth() {
    return month;
  }

  /**
   * @return the day
   */
  public int getDay() {
    return day;
  }

  /**
   * Gets the hour, if time is used.
   * @return Integer, the hour.
   */
  public int getHour(){
    return hour;
  }

  /**
   * Gets the minute, if time is used.
   * @return Integer, the minute.
   */
  public int getMinute(){
    return minute;
  }

  /**
   * Gets the double, if time is used.
   * @return Integer, the time.
   */
  public double getSeconds(){
    return seconds;
  }
}
