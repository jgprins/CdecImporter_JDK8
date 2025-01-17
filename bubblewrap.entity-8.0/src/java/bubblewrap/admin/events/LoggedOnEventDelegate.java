package bubblewrap.admin.events;

import bubblewrap.core.events.EventDelegateBase;

/**
 * An EventDelegateBase for a LoggedOnEventArgs EventArgs class.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class LoggedOnEventDelegate extends EventDelegateBase<LoggedOnEventArgs> {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public LoggedOnEventDelegate(Object listener) {
    super(listener);
  }
  //</editor-fold>
}
