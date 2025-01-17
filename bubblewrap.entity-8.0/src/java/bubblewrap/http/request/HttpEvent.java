package bubblewrap.http.request;

import bubblewrap.io.DataEntry;

/**
 * An Enum defining all the supported HTTPRequest handling Events
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum HttpEvent {
  CLICK("onclick", "click");
  
  //<editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Finale Fields">
  public final String tagAttribute;
  public final String event;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Constructor">
  private HttpEvent(String tagAttribute, String event) {
    this.tagAttribute = tagAttribute;
    this.event = event;
  }
  // </editor-fold>
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Called to find the HttpEvent that matches the <tt>event</tt> name.
   * @param event
   * @return the matching HttpEvent or throws an exception if no match is found.
   * @throws NullPointerException if event = ""|null
   * @throws IllegalArgumentException if no match is found.
   */
  public static HttpEvent fromEvent(String event) {
    HttpEvent result = null;
    event = DataEntry.cleanLoString(event);
    if (event == null) {
      throw new NullPointerException("The event name is unasigned.");
    }
    
    for (HttpEvent enumVal : HttpEvent.values()) {
      if ((event.equals(enumVal.event))) {
        result = enumVal;
        break;
      }
    }
    
    if (result == null) {
      throw new IllegalArgumentException("HttpEvent[" + event + "] is not supported.");
    }
    return result;
  }
  // </editor-fold>
}
