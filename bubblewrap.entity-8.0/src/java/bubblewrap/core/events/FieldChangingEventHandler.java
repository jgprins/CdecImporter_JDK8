package bubblewrap.core.events;

/**
 * An EventHandler to notify listener that a specified field's value is about to changed
 * and allow the listener to cancel the change by canceling the event.  
 * It uses the {@linkplain FieldChangingEventArgs} to send the information to listeners
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class FieldChangingEventHandler extends EventHandlerBase<FieldChangingEventArgs> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public FieldChangingEventHandler() {
    super();    
  }
  // </editor-fold>

}
