package bubblewrap.core.events;

import java.io.Serializable;
import java.util.Objects;

/**
 * The Abstract Class for all DeventDelgates
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class EventDelegateBase<TArgs extends EventArgs> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Field">
  /**
   * Placeholder for the EventDelgate's listener
   */
  private Object listener;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  protected EventDelegateBase(Object listener) {
    if (listener == null) {
      throw new NullPointerException("The EventDelgate's Listener cannot be "
              + "unassigned.");
    }
    this.listener = listener;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.listener = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected/public Method">
  /**
   * Called by the EventHanlder to reset the delegate's listener on remover from the
   * listeners list.
   */
  protected void resetListener() {
    this.listener = null;
  }

  /**
   * Called to get a reference to the delegate's listener
   * @return the assigned listener (null after being reset)
   */
  @SuppressWarnings("unchecked")
  public <TListener> TListener getListener() {
    return (TListener) this.listener;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return true if obj != null and ((obj = this.listener) or ((obj
   * instanceof ListenerInfo) and (obj.listener = this.listner)))</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    boolean result = (obj != null);
    if (result) {
      if (obj instanceof EventDelegateBase) {
        EventDelegateBase other = (EventDelegateBase) obj;
        result = (this.listener == other.listener);
      } else {
        result = (this.listener == obj);
      }
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
  
  //<editor-fold defaultstate="collapsed" desc="Abstract methods">
  /**
   * The method for handling the fired event.
   * @param sender the sender of the event
   * @param args the associated Event Information or arguments
   */
  public abstract void onEvent(Object sender, TArgs args);
  //</editor-fold>
}
