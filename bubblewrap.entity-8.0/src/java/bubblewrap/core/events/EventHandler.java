package bubblewrap.core.events;

/**
 * A Simple EventHandler using the base EventArgs to notify any listener that the event
 * occurred. Since it use the EventArgs class, it sends no additional information, nor
 * can the EventArgs be handled - it is always propagated to all listeners.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EventHandler extends EventHandlerBase<EventArgs> {  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public EventHandler() {
    super();    
  }
  // </editor-fold>
}
