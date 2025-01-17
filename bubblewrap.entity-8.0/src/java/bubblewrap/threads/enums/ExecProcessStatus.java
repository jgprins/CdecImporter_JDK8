package bubblewrap.threads.enums;

/**
 * A Status that can be return by a ExecProces
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum ExecProcessStatus {

  NOTSTARTED(0x0001, "Process Not Started"),
  PENDING(0x0002, "Processing Pending"),
  EXECUTING(0x0004, "Process is Executing . . ."),
  COMPLETED(0x0008, "Process Completed"),
  NOTFOUND(0x0018, "Data not Found, Retry"),
  FAILED(0x0028, "Processing Error"),
  STOPPED(0x0048, "Process Manually Stopped");
  
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
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor
   * @param value the option value
   * @param label the option label
   */
  private ExecProcessStatus(int value, String label) {
    this.label = label;
    this.value = value;
  }
  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the ExecProcessStatus associated with <tt>value</tt>
   * @param value the ExecProcessStatus.value to search for
   * @return the matching ExecProcessStatus or PENDING if not found.
   */
  public static ExecProcessStatus fromValue(int value) {
    ExecProcessStatus result = ExecProcessStatus.PENDING;
    for (ExecProcessStatus enumVal : ExecProcessStatus.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }
  // </editor-fold>
}
