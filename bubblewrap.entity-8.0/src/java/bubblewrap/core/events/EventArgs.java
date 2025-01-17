package bubblewrap.core.events;

import java.io.Serializable;

/**
 * A EventArgs is the package of information send to listeners when an event is fired.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EventArgs implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The EventArgs's isHandled state flag
   */
  private Boolean handled;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public EventArgs() {
    super();    
    this.handled = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Properties">
  /**
   * Get the EventArgs's isHandled state (default = false)
   * @return the current state
   */
  public boolean isHandled() {
    return ((this.handled != null) && (this.handled));
  }
  
  /**
   * Called by inheritors to set the IsHandled State - is can only be turned on
   */
  public void setHandled() {
    this.handled = true;
  }
  //</editor-fold>
}
