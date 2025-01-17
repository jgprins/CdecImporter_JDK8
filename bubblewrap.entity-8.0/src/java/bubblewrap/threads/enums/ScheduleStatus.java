package bubblewrap.threads.enums;

/**
 * <p>EventArgs specific to Schedule EventHandling. The {@linkplain 
 * #isScheduleEvent(int) Schedule Specific events} are as follows:</p><ul>
 *  <li><b>{@linkplain #SCHEDULE_STARTED}:</b> - fired after the schedule has been 
 *    started</li>
 *  <li><b>{@linkplain #UPDATED_PERIODIC}:</b> - fired after updating the Periodic 
 *    Schedule</li>
 *  <li><b>{@linkplain #UPDATED_RETRIED}:</b> - fired after updating the Retry 
 *    Schedule</li>
 *  <li><b>{@linkplain #UPDATED_RUNNOW}:</b> - fired after scheduling a ExecuteNow 
 *    request</li>
 *  <li><b>{@linkplain #ISDONE_COMPLETED}:</b> - when execution is successfully 
 *    Completed</li>
 *  <li><b>{@linkplain #ISDONE_STOPPED}:</b> - when execution is manually stopped</li>
 *  <li><b>{@linkplain #ISDONE_EXPIRED}:</b> - when execution is stopped because the 
 *    Schedule expired</li>
 *  <li><b>{@linkplain #ISDONE_ERROR}:</b> - when execution is stopped due to an error.
 *  </li>
 * </ul>
 * @author kprins
 */
public enum ScheduleStatus {
  //<editor-fold defaultstate="expanded" desc="EnumValues">
  /**
   * No Scheduler is assigned
   */
  NONE(0x0000,"Not Scheduled"),
  /**
   * The Scheduler started and the start time is set
   */
  STARTED(0x0001,"Schedule Started"),
  /**
   * A new periodic schedule time has been set
   */
  PERIODIC(0x0011,"Set Periodic Schedule"),
  /**
   * A new retry schedule time has been set
   */
  RETRIED(0x0021,"Set Retry Schedule"),
  /**
   * A new run-now schedule has been set
   */
  RUNNOW(0x0041,"Set Run-Now Schedule"),
  /**
   * All scheduled events were scheduled and successfully completed (i.e., only if the
   * Schedule Steps were limited)
   */
  COMPLETED(0x0104,"Schedule Completed"),
  /**
   * The Schedule was interrupted/manually stopped
   */
  STOPPED(0x0204,"Manually Stopped Execution"),
  /**
   * The Scheduler was interrupted due to a execution/scheduling error.
   */
  ERROR(0x0804,"Execution Failed");
    //</editor-fold>
//  //<editor-fold defaultstate="collapsed" desc="EnumValues">
//  private static final int SCHEDULE      = 0x0001000;
//  private static final int STARTED             = 0x00000001;
//  //UPDATED is inherited from Base Calass
//  //private static final int UPDATED            = 0x00000002;
//  private static final int PERIODIC            = 0x00000010;
//  private static final int RETRIED             = 0x00000020;
//  private static final int RUNNOW              = 0x00000040;
//  private static final int ISDONE              = 0x00000004;
//  private static final int COMPLETED           = 0x00000004;
//  private static final int STOPPED             = 0x00000100;
//  private static final int EXPIRED             = 0x00000200;
//  private static final int ONERROR             = 0x00000400;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  public final String label;
  public final int value;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor  
   * @param value the option value
   * @param label the option label
   */
  private ScheduleStatus(int value, String label) {
    this.label = label;
    this.value = value;
  }
  // </editor-fold>
  
  /**
   * Check if the status represents a Updated Scheduled
   * @return ((this.value > 0) && ((this.value & 0x0001) == 0x0001))
   */
  public boolean isUpdated() {
    return ((this.value > 0) && ((this.value & 0x0001) == 0x0001));
  }
  
  /**
   * Check if the status represents a Scheduled that is executing.
   * @return this.equals(STARTED)
   */
  public boolean isExecuting() {
    return (this.equals(STARTED));
  }
  
  /**
   * Check if the status represents a Scheduled that execution stopped.
   * @return ((this.value > 0) && ((this.value & 0x0004) == 0x0004))
   */
  public boolean isDone() {
    return ((this.value > 0) && ((this.value & 0x0004) == 0x0004));
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the getClass() associated with <tt>value</tt>
   * @param value the getClass().value to search for
   * @return the matching getClass() or NONE if not found.
   */
  public static ScheduleStatus fromValue(int value) {
    ScheduleStatus result = ScheduleStatus.NONE;
    for (ScheduleStatus enumVal : ScheduleStatus.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }
  // </editor-fold>
}
