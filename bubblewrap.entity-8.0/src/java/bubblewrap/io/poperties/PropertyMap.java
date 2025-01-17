package bubblewrap.io.poperties;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import bubblewrap.io.interfaces.IParamItem;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class PropertyMap implements Serializable, IParamItem {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(PropertyMap.class.getName());
  //</editor-fold>        
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The LinkedHashmap for managing the PropertyMap's entries.
   */
  private LinkedHashMap<String, Property> propertyMap;
  /**
   * Set to true if the value has changed using the setValue method
   */
  private Boolean dirty;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * EventHandler for sending a Value Changed event.
   */
  public final EventHandler ValueChanged;

  /**
   * Method called to fie the Value Changed event.
   */
  protected void fireValueChanged(Object sender) {
    this.ValueChanged.fireEvent(sender, new EventArgs());
    this.dirty = true;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public PropertyMap() {
    super();  
    this.ValueChanged = new EventHandler();
    this.propertyMap = new LinkedHashMap<>();
    this.dirty = null;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.ValueChanged.clear();
    this.propertyMap.clear();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public isInitiating State Management">
  /**
   * Transient counter for managing the isInitiating state
   */
  private transient int countInitiate = 0;

  /**
   * Get the current isInitiating state
   * @return true if (this.countInitiate > 0)
   */
  public final boolean isInitiating() {
    return (this.countInitiate > 0);
  }

  /**
   * Called to start the Initiate process. It increment this.countInitiate.
   * <p>
   * <b>NOTE:</b> Every call to beginInitiate must be followed by a call to {@linkplain
   * #endInitiate() this.endInitiate}.</p>
   */
  public final void beginInitiate() {
    this.countInitiate = (this.countInitiate < 0) ? 0 : this.countInitiate;
    this.countInitiate++;
  }

  /**
   * Called - after calling {@linkplain #beginInitiate() this.beginInitiate} - to decrement
   * this.countInitiate and to call {@linkplain #onInitiateEnds() this.onInitiateEnds} to
   * process the event.
   */
  public final void endInitiate() {
    if (this.countInitiate > 0) {
      this.countInitiate--;
      if (this.countInitiate == 0) {
        this.onInitiateEnds();
      }
    }
  }

  /**
   * Calls to execute any post-process after the PropertyMap's settings have changed
   * (e.g. fire an event).
   */
  protected void onInitiateEnds() {
    
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Property Management Methods">
  /**
   * get whether the map is empty
   * @return this.propertyMap.isEmpty
   */
  public boolean isEmpty() {
    return this.propertyMap.isEmpty();
  }

  /**
   * Get the number of Property Values in the PropertyMap
   * @return this.propertyMap.size
   */
  public int size() {
    return this.propertyMap.size();
  }

  /**
   * Get whether the PropertyMap contains the specified <tt>key</tt>
   * @param key the map key to search for (case-insensitive)
   * @return true is the key is found
   */
  public boolean containsProperty(String key) {
    key = DataEntry.cleanLoString(key);
    return ((key != null) && (this.propertyMap.containsKey(key)));
  }

  /**
   * Get the Property for the specified key
   * @param key the map key to search for (case-insensitive)
   * @return the property or null is not found
   */
  public Property getProperty(String key) {
    Property result = null;
    if (((key = DataEntry.cleanLoString(key)) != null) && 
                                            (this.propertyMap.containsKey(key))) {
      result = this.propertyMap.get(key);
    }
    return result;
  }

  /**
   * Called to add a new property to the PropertyMap.  I the property already exist, the
   * old property will be removed before the new property is added. Since the properties
   * is maintained in a {@linkplain LinkedHashMap} and key-value pairs are maintained in 
   * the order entered, the new key-value pair will be appended to the end of the list.
   * <p>When adding the new property, the parameterMap is add as a listener to the 
   * property's {@linkplain Property#ValueChanged ValueChanged} event.
   * <p>If (!this.isInitiating), the PropertyMap's {@linkplain #ValueChanged ValueChanged}
   * event will be fired.
   * @param property the new property to add
   */
  public void addProperty(Property property) {
    if (property == null) {
      return;
    }
    String key = DataEntry.cleanLoString(property.getKey());
    if (key == null) {
      throw new IllegalArgumentException("The Property's key is unassigned.");
    }
    Property former = null;
    if ((this.propertyMap.containsKey(key)) && 
              ((former = this.propertyMap.get(key)) != null)) {
      former.ValueChanged.remove(this);      
    }
    
    this.propertyMap.put(key, property);
    property.ValueChanged.add(new EventDelegate(this) {      
      @Override
      public void onEvent(Object sender, EventArgs args) {
        PropertyMap listener = this.getListener();
        if ((listener != null) && (sender != null)) {
          try {
            listener.fireValueChanged(sender);
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }
      }
    });
    if (!this.isInitiating()) {
      this.fireValueChanged(this);
    }
  }

  /**
   * Called to remove the Property associated with the specified <tt>key</tt> from the
   * PropertyMap. If the property is found and (!this.isInitiating), the PropertyMap's 
   * {@linkplain #ValueChanged ValueChanged} event will be fired.
   * @param key the map key to search for (case-insensitive)
   */
  public void remove(String key) {
    key = DataEntry.cleanLoString(key);
    if ((key != null) && (this.propertyMap.containsKey(key))) {
      Property prop = this.propertyMap.get(key);
      if (prop != null) {
        prop.ValueChanged.remove(this);
      }
      this.propertyMap.remove(key);
      if (!this.isInitiating()) {
        this.fireValueChanged(this);
      }
    }
  }

  /**
   * Called to clear all properties from the PropertyMap. If (!this.isInitiating), the 
   * PropertyMap's {@linkplain #ValueChanged ValueChanged} event will be fired.
   */
  public void clear() {
    if (!this.propertyMap.isEmpty()) {
      for (Property prop : this.propertyMap.values()) {
        prop.ValueChanged.remove(this);
      }
      this.propertyMap.clear();
      if (!this.isInitiating()) {
        this.fireValueChanged(this);
      }
    }
  }

  /**
   * Get a set of all keys of the PropertyMap
   * @return this.propertyMap.keySet
   */
  public Set<String> getPropertyKeys() {
    return this.propertyMap.keySet();
  }

  /**
   * Get a collection of all values in the PropertyMap
   * @return this.propertyMap.values
   */
  public Collection<Property> getProperties() {
    return this.propertyMap.values();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public JSON Serilization Methods">
  /**
   * Called to serialize the PropertyMap's Property Values to <tt>output</tt> -
   * a JSONObject
   * @param output to saved the property values to.
   */
  public void toJSON(JSONObject output) {
    if ((output != null) && (!this.isEmpty())) {
      for (Property prop : this.propertyMap.values()) {
        prop.toJSON(output);
      }
    }
  }

  /**
   * Called to deserialize the PropertyMap's saved Property Value from <tt>input</tt> -
   * a JSONObject
   * @param input the previously saved property values.
   */
  public void fromJSON(JSONObject input) {
    if ((input != null) && (!this.isEmpty())) {
      for (Property prop : this.propertyMap.values()) {
        prop.fromJSON(input);
      }
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override IParamItem">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return ((this.dirty != null) && (this.dirty))</p>
   */
  @Override
  public boolean isDirty() {
    return ((this.dirty != null) && (this.dirty));
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Set this.dirty = null and call resetOnSaved for each property in 
   * this.propertyMap</p>
   */
  @Override
  public void resetOnSaved() {
    if (!this.isDirty()) {
      return;
    }
    this.dirty = null;
    if (!this.isEmpty()) {
      for (Property prop : this.propertyMap.values()) {
        prop.resetOnSaved();
      }
    }
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: call this.ValueChanged.add(delegate)</p>
   */
  @Override
  public void setValueChangedListener(EventDelegate delegate) {
    this.ValueChanged.add(delegate);
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: call this.ValueChanged.remove(listener)</p>
   */
  @Override
  public void removeValueChangedListener(Object listener) {
    this.ValueChanged.remove(listener);
  }
  // </editor-fold>
  
  
}
