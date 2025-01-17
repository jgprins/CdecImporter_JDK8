package bubblewrap.core.events;

/**
 * An eventHandler to notify listener that a specified field's value has changed.  
 * It uses the {@linkplain AccessValidationEventArgs} to send the information to 
 * listeners.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class AccessValidationEventHandler 
                        extends EventHandlerBase<AccessValidationEventArgs> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public AccessValidationEventHandler() {
    super();    
  }
  // </editor-fold>

}