package bubblewrap.core.events;

/**
 * An EventArgs extension to handle the canceling of a Cancel Request Event.
 * Used by the {@linkplain CancelEventHandler} when firing an event.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class CancelEventArgs extends EventArgs {
  
  //<editor-fold defaultstate="collapsed" desc="private Fields">
  /**
   * Placeholder for the cancellation message
   */
  private String cancelMsg;
  private Boolean canceled;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public CancelEventArgs() {
    super();    
    this.cancelMsg = null;
    this.canceled = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Called to cancel the event's request. If not yet handled, the message will be
   * assigned and this.isHandled will be set.
   * @param cancelMsg the reason for the cancellation.
   */
  public void cancel(String cancelMsg) {
    if (!this.isHandled()) {
      this.cancelMsg = cancelMsg;
      this.canceled = true;
      this.setHandled();
    }
  }
  
  /**
   * Get the EventArgs's Canceled state.
   * @return ((this.canceled != null) && (this.canceled))
   */
  public final boolean isCanceled() {
    return ((this.canceled != null) && (this.canceled));
  }
  
  /**
   * Get the reason for the cancellation
   * @return the assigned message (or "Canceled for an unknown reason." if unassigned).
   */
  public final String getMessage() {
    return (this.cancelMsg == null)? "Canceled for an unknown reason.": this.cancelMsg;
  }
  //</editor-fold>  
}
