package gov.ca.water.cdec.importers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Event Handler for managing the firing of CDEC Import Processing 
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ImportEventHandler implements Serializable {
    
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ImportEventHandler.class.getName());
  //</editor-fold>        
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A List for maintaining the EventDelegateBase
   */
  private List<ImportEventDelegate> delegates;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public constructor with a reference to the EventSende owner.
   * @param pOwner
   */
  protected ImportEventHandler() {
    this.delegates = null;
  }

  /**
   * OVERRIDE: dispose local resources when disposing
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.delegates != null) {
      for (ImportEventDelegate delegate : this.delegates) {
        delegate.resetListener();
      }
      this.delegates.clear();
      this.delegates = null;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected/public Method">
  
  /**
   * Add a new event EventDelegateBase for handling the event. It will verify the 
   * EventDelegateBase has not already be added. 
   * @param delegate the EventDelegateBase that will process the event if fired.
   */
  public final void add(ImportEventDelegate delegate) {
    if (delegate != null) {
      if (this.delegates == null) {
        this.delegates = new ArrayList<>();
        this.delegates.add(delegate);
      } else if (!this.delegates.contains(delegate)) {
        this.delegates.add(delegate);
      }
    }
  }

  /**
   * Remove the Listener for the EventHandler's EventListener's list. Ignored if the
   * listener is undefined.
   * @param listener the event listener to remove
   */
  public final void remove(Object listener) {
    if ((listener != null) && (this.delegates != null) && (!this.delegates.isEmpty())) {
      for (ImportEventDelegate delegate : delegates) {
        if (delegate.equals(listener)) {
          this.delegates.remove(delegate);
          break;
        }
      }
    }
  }
  
  /**
   * Check if <tt>listener</tt> is already an assigned listener to this EventHandler.
   * @param listener the listener to check for
   * @return true if already a listener
   */
  public final boolean isListener(Object listener) {
    boolean result = false;
    if ((listener != null) && (this.delegates != null) && (!this.delegates.isEmpty())) {
      for (ImportEventDelegate delegate : delegates) {
        if (delegate.equals(listener)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Check if the EventHandler is empty - has no assigned delegates
   * @return 
   */
  public boolean isEmpty() {
    return ((this.delegates == null) || (this.delegates.isEmpty()));
  }
  
  /**
   * Called to clear the eventHandler - call its finalize method/
   */
  public final void clear() {
    try {
      this.finalize();
    } catch(Throwable e) {}
  }

  /**
   * <p>Called by the owner of the event Handler to fire the event notifying all
   * the event listeners by calling the listeners' assigned event method and passing the
   * assign <tt>eventArgs</tt> to the listener. It will call the listeners in the order 
   * they were added to the list and terminate the process if the eventInfo return handled (i.e.,
   * its isHandled state is set).</p>
   * <p><b>NOTE:</b> The process is skipped id the sender=null </p>
   * @param sender the Event sender
   * @param eventArgs the ImportEventArgs to pass to the listener
   */
  public final synchronized void fireEvent(Object sender, ImportEventArgs eventArgs) {
    try {
      if ((sender == null) || (eventArgs == null)
              || (this.delegates == null) || (this.delegates.isEmpty())) {
        return;
      }

      for (ImportEventDelegate delegate : this.delegates) {
        try {
          delegate.onEvent(sender, eventArgs);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.fireEvent Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.fireEvent Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>
}
