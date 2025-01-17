package bubblewrap.core.events;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;

/**
 * <p>This is the abstract EventHandler class - a delegate assign to any other class for
 * managing the sending of a specific event (e.g., a Changed or Selected event).</p>
 * <p>An EventHandler is defined a a public final field and is initiated in the sender 
 * classes constructor and cleared in its finalize method. Example:</p>
 * <code> 
 *   //<br/>
 *   // The public final EventHandler for firing the Selection Changed Event<br/>
 *   //<br/>
 *   public final EventHandler selectionChanged;<br/>
 *   // A private method to fire the event<br/>
 *   private fireSelectionChanged() {<br/>
 *   &nbsp;&nbsp;this.selectionChanged.fireEvent(this,new EventArgs());<br/>
 *   }<br/>  
 * <br/>
 *   // Constructor <br/>
 *   public MySenderClass() { <br/>
 *   &nbsp;this.selectionChanged = new EventHandler(); <br/>
 *   } <br/>
 *  <br/>
 *   protected void finalize() {<br/>
 *   &nbsp;super.finalize();<br/>
 *   &nbsp;this.selectionChanged.clear():<br/>
 *   }<br/>  
 * </code>
 * <p>On the Listener side, the listener class must assign itself as a listener of the
 * event and remove itself as a listener when it is finalize or when it no longer want
 * to listener to the event. It also need a EventMethod to handle the event. Example:
 * </p><code>
 *   // Private field as placeholder for the sender reference<br/> 
 *   private MySenderClass sender;<br/> 
 *   //Constructor<br/> 
 *   public MyListenerClass() {<br/> 
 *   &nbsp;this.sender = null<br/> 
 *   }<br/> 
 *   // Finalize the Listener class<br/> 
 *   protected void finalize {<br/>
 *   &nbsp;super.finalize();<br/>
 *   &nbsp;if (this.sender != null) {<br/>
 *   &nbsp;&nbsp;this.sender.selectionChanged.remove(this);<br/> 
 *   &nbsp;}<br/> 
 *   }<br/>  
 *   // A Method to initaite the Sender instance<br/>
 *   private void initSender() {<br/> 
 *   &nbsp;this.sender = new MySenderClass();<br/> 
 *   &nbsp;this.sender.selectionChanged.add(this,"senderSelectionChanged"}<br/> 
 *   }<br/>
 *   // A EventMethod for handlign the event<br/>
 *   public final void senderSelectionChanged(Object sender, EventArgs args) {<br/>
 *   &nbsp;if (sender ==  this.sender) {<br/>
 *   &nbsp;&nbsp;this.handleSelectionChanged();<br/>
 *   &nbsp;}<br/>
 *   }<br/>
 * </code> 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class EventHandlerBase<TArgs extends EventArgs>
                                                        implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Method/Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = 
          Logger.getLogger(EventHandlerBase.class.getName());
  
  /**
   * Called to get the EventArgs class used by the EventHandlerDelegate
   * @return the generically referenced class
   */
  @SuppressWarnings("unchecked")
  public static <TEvent extends EventArgs> Class<TEvent>
          getEventInfoClass(Class<EventHandlerBase<TEvent>> handlerClass) {
    Class<TEvent> result = null;
    result = (Class<TEvent>)
         ReflectionInfo.getGenericClass(EventHandlerBase.class, handlerClass, 0);
    if (result == null) {
      result = (Class<TEvent>) EventArgs.class;
    }
    return result;
  }
      
  /**
   * Called to get the listener's PUBLIC Event Method (i.e., a method with parameter types
   * {Object, eventInfoClass} - e.g., onChildChange(Object Sender, EventArgs eventInfo))
   * @param listener this listener
   * @param methodName the event method name
   * @param eventInfoClass the EventHandler's EventArgs class
   * @return the method.
   * @exception IllegalArgumentException if any of the input parameters are unassigned or
   * if the method is not supported be the listener (i.e., the method and it parameter
   * types must be correctly defined).
   */
  @SuppressWarnings("unchecked")
  public static Method getEventMethod(Object listener, String methodName,
          Class<? extends EventArgs> eventInfoClass) {
    Method result = null;
    try {
      if (listener == null) {
        throw new Exception("The Listener instance reference is unassigned.");
      }
      methodName = DataEntry.cleanString(methodName);
      if (methodName == null) {
        throw new Exception("The Event Method's name is undefined.");
      }
      
      if (eventInfoClass == null) {
        throw new Exception("The eventhandler's EventInfo class is undefined.");
      }
      
      Class[] parTypes = new Class[]{Object.class, eventInfoClass};
      Class declaringClass = listener.getClass();
      while ((declaringClass != null) && (!Object.class.equals(declaringClass))) {
        try {
          result = declaringClass.getMethod(methodName, parTypes);
        } catch (Exception inExp) {
          result = null;
        }
        if (result != null) {
          break;
        }
        declaringClass = declaringClass.getSuperclass();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "ReflectionInfo.getEventMethod Error:\n {0}",
              exp.getMessage());
      throw new IllegalArgumentException(exp.getMessage());
    }
    return result;
  }  
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Class MethodEventDelegate">
  /**
   * A private class for storing the Listener Info
   */
  private class MethodEventDelegate extends EventDelegateBase<TArgs> {
    
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Placeholder for the Delegate's method
     */
    private Method method;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public MethodEventDelegate(Object listener, Method method) {
      super(listener);
      this.method = method;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Object Overrides">
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: Return {this.listener.class.simpleName}.{this.method.name}</p>
     */
    @Override
    public String toString() {
      Object listener = this.getListener();
      return listener.getClass().getSimpleName() + "."
              + this.method.getName();
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Override EventDelegateBase">
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: Invoke this.Method passing in sender, and eventInfo. Trap and log any
     * errors</p>
     */
    @Override
    public void onEvent(Object sender, TArgs eventInfo) {
      try {
        Object listener = this.getListener();
        if ((this.method != null) && ((listener = this.getListener()) != null)) {
          Object[] args = new Object[]{sender, eventInfo};
          this.method.invoke(listener, args);
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.fireEvent Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    //</editor-fold>
  }
  //</editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A List for maintaining the EventDelegateBase
   */
  private List<EventDelegateBase<TArgs>> delegates;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructore/Destructor">
  /**
   * Public constructor with a reference to the EventSende owner.
   * @param pOwner
   */
  protected EventHandlerBase() {
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
      for (EventDelegateBase<TArgs> delegate : delegates) {
        delegate.resetListener();
      }
      this.delegates.clear();
      this.delegates = null;
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * FINAL: Get the Entity's Class
   * @return Class<V>
   */
  @SuppressWarnings("unchecked")
  public final Class<TArgs> getEventArgsClass() {
    Class thisClasss = this.getClass();
    Class<TArgs> result = EventHandlerBase.getEventInfoClass(thisClasss);
    result = 
           ReflectionInfo.getGenericClass(EventHandlerBase.class, thisClasss, 0);
    if (result == null) {
      result = (Class<TArgs>) EventArgs.class;
    }
    return result;
  }
 
  /**
   * Add a new event EventDelegateBase for handling the event. It will verify the 
   * EventDelegateBase has not already be added. 
   * @param delegate the EventDelegateBase that will process the event if fired.
   */
  public synchronized final void add(EventDelegateBase<TArgs> delegate) {
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
  public synchronized void remove(Object listener) {
    if ((listener != null) && (this.delegates != null) && (!this.delegates.isEmpty())) {
      for (EventDelegateBase<TArgs> delegate : delegates) {
        if (delegate.equals(listener)) {
          this.delegates.remove(delegate);
          break;
        }
      }
    }
  }
  
  /**
   * Called to clear the eventHandler - call its finalize method/
   */
  public synchronized void clear() {
    try {
      this.finalize();
    } catch(Throwable e) {}
  }

  /**
   * <p>Called by the owner of the event Handler to fire the event (eEvent) notifying 
   * all the event listeners by calling the listener's assigned event method and passing the
   * assign eventInfo to the listener. It will call the listeners in the order they were
   * added to to the list and terminate the process if the eventInfo return handled 
   * (i.e., its isHandled state is set).</p>
   * <p><b>NOTE:</b> The process is skipped id the sender=null </p>
   * NOTE: ignored if pSender or not the Delegates owner or e = null.
   * @param pSender IEventSender
   * @param eventArgs the EventArgs to pass to the listener
   */
   public synchronized void fireEvent(Object sender, TArgs eventArgs) {
    if ((sender == null) || (eventArgs == null)
            || (this.delegates == null) || (this.delegates.isEmpty())) {
      return;
    }
    
    List<EventDelegateBase<TArgs>> delegateList = new ArrayList<>(this.delegates);    
    for (EventDelegateBase<TArgs> delegate : delegateList) {
      try {
        delegate.onEvent(sender, eventArgs);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.fireEvent Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
      
      if (eventArgs.isHandled()) {
        break;
      }
    }
  }
  // </editor-fold>
}
