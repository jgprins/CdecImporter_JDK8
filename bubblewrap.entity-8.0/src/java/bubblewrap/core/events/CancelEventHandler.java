package bubblewrap.core.events;

/**
 * A base Cancel request EventHandler used send a request to listener for permission to
 * proceed with process. It uses the {@linkplain CancelEventArgs}, which allows listeners
 * to cancel the request and provide a reason for the cancellation.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class CancelEventHandler extends EventHandlerBase<CancelEventArgs>{
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public CancelEventHandler() {
    super();    
  }
  // </editor-fold>
}
