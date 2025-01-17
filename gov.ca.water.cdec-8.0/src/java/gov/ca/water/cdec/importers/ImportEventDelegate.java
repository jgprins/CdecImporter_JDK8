package gov.ca.water.cdec.importers;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class ImportEventDelegate implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private Object listener;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ImportEventDelegate(Object listener) {
    super();  
    this.listener = listener;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get a reference to the Listener
   * @return 
   */
  public Object getListener() {
    return this.listener;
  }

  /**
   * Set the Listener reference to null.
   */
  public void resetListener() {
    this.listener = null;
  }
  
  /**
   * ABSTRACT: Called to process the event.
   * @param sender the sender 
   * @param args the arguments passed to the listener
   */
  public abstract void onEvent(Object sender, ImportEventArgs args);
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return true if obj != null and ((obj = this.listener) or ((obj
   * instance of ImportEventDelegate) and (obj.listener = this.listener)))</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof ImportEventDelegate));
    if (result) {
      ImportEventDelegate other = (ImportEventDelegate) obj;
      result = (this.listener == other.listener);
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return a HashCode on this.listener</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.listener);
    return hash;
  }
  //</editor-fold>
}
