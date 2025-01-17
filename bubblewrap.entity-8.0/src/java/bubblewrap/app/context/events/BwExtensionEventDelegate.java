package bubblewrap.app.context.events;

import bubblewrap.core.events.EventDelegateBase;

/**
 * A Delegate for handling {@linkplain BwExtensionEventHandler} events
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class BwExtensionEventDelegate 
                                          extends EventDelegateBase<BwExtensionEventArgs>{
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public BwExtensionEventDelegate(Object listener) {
    super(listener);  
  }
  // </editor-fold>

}
