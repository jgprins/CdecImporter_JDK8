package bubblewrap.http.request;

import bubblewrap.http.controllers.ManagedBeanController;
import java.io.Serializable;

/**
 * An interface that must be implemented by any ManagedBean that can be used with the
 * {@link ManagedBeanController}. It {@linkplain #onHttpEvent(
 * bubblewrap.http.request.HttpEventHandlingArgs) onHttpEvent} method must be implemented
 * to handle the request for the specified event.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public interface IHttpRequestHandler extends Serializable {
  
  /**
   * Called by the {@link ManagedBeanController} (or simulator controllers) to handle
   * the submitted HTTPReuest event.
   * @param args the arguments passed from the client
   */
  public void onHttpEvent(HttpEventHandlingArgs args);  
}
