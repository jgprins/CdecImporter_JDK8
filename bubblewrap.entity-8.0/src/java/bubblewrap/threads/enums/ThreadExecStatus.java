package bubblewrap.threads.enums;

/**
 * An enum class for managing the Thread Execution Status of a {@linkplain ExecProcess}
 * running in a {@linkplain ExecProcessThread}
 * @author kprins
 */
public enum ThreadExecStatus {
  /**
   * The default - the process has not yet been started.
   */
  NOTSTARTED(0,"Not Started"),
  /**
   * The scheduler is preparing for execution of processes.
   */
  STARTING(1,"Starting..."),
  /**
   * The execution process is executing.
   */
  EXECUTING(2,"Executing"),
  /**
   * The scheduler process is stopping the current execution of processes.
   */
  STOPPING(3,"Shutting Down..."),
  /**
   * The execution process was paused on request.
   */
  PAUSED(4,"Paused"),
  /**
   * The execution process was canceled on request.
   */
  CANCELED(5,"Canceled"),
  /**
   * The execution process was canceled on request.
   */
  FAILED(6,"Failed"),
  /**
   * The execution process was successfully completed.
   */
  COMPLETED(7,"COMPLETED");
        
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
  private ThreadExecStatus(int value, String label) {
    this.label = label;
    this.value = value;
  }
  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the ThreadExecState associated with <tt>value</tt>
   * @param value the ThreadExecState.value to search for
   * @return the matching ThreadExecState or NOTSTARTED if not found.
   */
  public static ThreadExecStatus fromValue(int value) {
    ThreadExecStatus result = ThreadExecStatus.NOTSTARTED;
    for (ThreadExecStatus enumVal : ThreadExecStatus.values()) {
      if (enumVal.value == value) {
        result = enumVal;
        break;
      }
    }
    return result;
  }
  // </editor-fold>
}
