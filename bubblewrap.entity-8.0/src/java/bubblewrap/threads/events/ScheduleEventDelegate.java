package bubblewrap.threads.events;

import bubblewrap.core.events.EventDelegateBase;

/**
 * A EventDelegate to assign to an {@linkplain ScheduleEventHandler} passing  
 * ScheduleEventArgs
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class ScheduleEventDelegate extends EventDelegateBase<ScheduleEventArgs> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ScheduleEventDelegate(Object listener) {
    super(listener);  
  }
  // </editor-fold>
}
