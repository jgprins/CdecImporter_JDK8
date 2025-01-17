package bubblewrap.threads.events;

import bubblewrap.core.events.EventArgs;
import bubblewrap.threads.enums.ScheduleStatus;

/**
 * An EventArgs class for Schedule Management
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ScheduleEventArgs extends EventArgs {

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  public final ScheduleStatus status;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ScheduleEventArgs(ScheduleStatus status) {
    super();  
    this.status = status;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "ScheduleEventArgs[ status=" + this.status.label + " ]"</p>
   */
  @Override
  public String toString() {
    return "ScheduleEventArgs[ status=" + this.status.label + " ]";
  }
  // </editor-fold>
}
