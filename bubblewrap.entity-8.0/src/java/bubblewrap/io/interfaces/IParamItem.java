package bubblewrap.io.interfaces;

import bubblewrap.core.events.EventDelegate;
import java.io.Serializable;


/**
 * The IParamItem and a ValueChanged Event Listener work as pair to establish a simple 
 * ValueChanged event handler mechanism.
 * @author kprins
 */
public interface IParamItem extends Serializable { 
  /**
   * A property to check the ParamItem's dirty state
   * @return true if the child's properties have changes.
   */
  boolean isDirty();
  
  /**
   * Called by the Listener after the saved the ParamItem's properties.
   */
  void resetOnSaved();
  
  /**
   * Called to assign a {@linkplain EventDelegate} to listen to the ValueChanged event
   * and handle the event when fired.
   * @param delegate the EventDelegate to handle the event
   */
  void setValueChangedListener(EventDelegate delegate);
  
  /**
   * Call to remove a ValueChanged listener.
   * @param listener the listener to remove
   */
  void removeValueChangedListener(Object listener);
}
