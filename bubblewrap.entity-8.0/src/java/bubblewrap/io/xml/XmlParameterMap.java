package bubblewrap.io.xml;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import bubblewrap.io.DataEntry;
import java.util.LinkedHashMap;
import javax.persistence.Transient;

/**
 *
 * @author kprins
 */
@XmlRootElement(name="ParameterMap")
public class XmlParameterMap implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
                                    Logger.getLogger(XmlParameterMap.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the HashTable that store the Key->Parameter Values pairs
   */
  @Transient
  private LinkedHashMap<String,XmlParameter> paramMap;
  /**
   * An internal count to prevent firing the change events while updating.
   */
  @Transient
  private transient int editCount = 0;
  //</editor-fold>
  
  /**
   * EventHandler for sending a Value Changed event.
   */
  @Transient
  public final EventHandler ValueChanged;
  /**
   * Method called to fie the Value Changed event.
   */
  protected void fireValueChanged() {
    if (this.editCount <= 0) {
      this.ValueChanged.fireEvent(this, new EventArgs());
    }
  }
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public XmlParameterMap() {
    super();    
    this.ValueChanged = new EventHandler();
    this.paramMap = new LinkedHashMap<>();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call super method before calling this.ValueChanged.clear()</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.ValueChanged.clear();
    this.clearParameters();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Called to trim the key and convert it to upper case.
   * @param key input key
   * @return cleaned key
   */
  private String cleanKey(String key) {
    key = DataEntry.cleanString(key);
    return (key == null)? null: key.toUpperCase();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * <p>Add a new Parameter to the Parameter Map. This call is ignored if the specified
   * parameter is null.</p>
   * <p><b>NOTE:</b> The parameter must be added before can parameter values can
   * be accessed through the putValue/getValue methods</p>
   * @param param the new parameter to added to the map
   * @throws Exception if the parameter already exists.
   */
  public void addParameter(XmlParameter param) throws Exception {
    try {
      if (param == null) {
        return;
      }
      
      String key = param.getKey();
      if (this.paramMap.containsKey(key)) {
        throw new Exception("Parameter[" + key + "] already exists.");
      }
      
      param.ValueChanged.add(new EventDelegate(this) {        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          XmlParameterMap listener = (XmlParameterMap) this.getListener();
          if (listener != null) {
            listener.fireValueChanged();
          }
        }
      });
      
      this.paramMap.put(key, param);
      this.fireValueChanged();
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".addParameter Error:\n " + pExp.getMessage());
    }
  }
  
  /**
   * Called to remove a previously added parameter. Skipped if the parameter is not
   * found.
   * @param key
   */
  public void removeParameter(String key) {
    key = this.cleanKey(key);
    if ((key != null) && (this.paramMap.containsKey(key))) {
      XmlParameter param = this.paramMap.get(key);
      if (param != null) {
        param.ValueChanged.remove(this);
      }
      this.paramMap.remove(key);
      this.fireValueChanged();
    }
  }
  
  /**
   * Called to remove all parameters
   */
  public void clearParameters() {
    if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
      for (XmlParameter param : this.paramMap.values()) {
        if (param != null) {
          param.ValueChanged.remove(this);
        }
      }
      this.paramMap.clear();
      this.fireValueChanged();
    }
  }
  
  /**
   * Call to reset all the parameters value to null.
   */
  public void resetParameters() {
    if (this.isEmpty()) {
      return;
    }
    
    for (XmlParameter param : this.paramMap.values()) {
      param.resetValue();
    }
    this.fireValueChanged();
  }
  
  /**
   * Get a the Map's Parameter Keys
   * @return a List of parameter Keys - the list is empty if this.isEmpty.
   */
  public List<String> getParameterKeys() {
    List<String> result = null;
    if (!this.isEmpty()) {
      result = new ArrayList<>(this.paramMap.keySet());
    }
    if (result == null) {
      result = new ArrayList<>();
    }
    return result;
  }
  
  /**
   * Get the Parameter associated with the specified Parameter Key
   * @param key the parameter Key
   * @return the XMLParameter or null if not found.
   */
  public XmlParameter getParameter(String key) {
    XmlParameter result = null;
    if (!this.isEmpty()) {
      key = this.cleanKey(key);
      if ((key != null) && (this.paramMap.containsKey(key))) {
        result = this.paramMap.get(key);
      }
    }
    return result;
  }
  
  /**
   * Get the number of entries in the ParameterMap
   * @return t0 or larger
   */
  public int size() {
    return this.paramMap.size();
  }
  
  /**
   * Check if the ParameterMap is empty (no parameters)
   * @return true if no parameters
   */
  public boolean isEmpty() {
    return this.paramMap.isEmpty();
  }
  
  /**
   * Check if the ParameterMap contains a parameter with the specified Key
   * @param key the parameter key to search for
   * @return true if the parameter exists.
   */
  public boolean containsKey(String key) {
    boolean result = false;
    key = this.cleanKey(key);
    if (key != null) {
      result = this.paramMap.containsKey(key);
    }
    return result;
  }
  
  /**
   * Called to assign a new value to for Parameter[key]. It locate the parameter and
   * call its isValid(value) method to validate that the proposed value is a valid
   * value for the parameters. It the calls the parameter's isNew(value) method and if
   * this method returns true, it will assign the values to the parameter, set the
   * map's isDistry state=true and fire the OncChanged event.
   * @param key the parameter key (must be a key of an existing parameter
   * @param value the new value to assign.
   * @exception NullPointerException if the key is not specified or the parameter has
   * not been added to the map.
   * @exception IllegalArgumentException if the value is not valid.
   */  
  public void setValue(String key, Serializable value) {
    key = this.cleanKey(key);
    if (key == null) {
      throw new NullPointerException("The Parameter Key cannot be unassigned.");
    }
    
    XmlParameter param = null;
    if (this.paramMap.containsKey(key)) {
      param = this.paramMap.get(key);
    }
    
    if (param == null) {
      throw new NullPointerException("The PararameterMap does not contain Parameter[" +
              key + "].");
    }
    
    if (!param.isValid(value)) {
      String errMsg = param.getValidationError();
      errMsg =(errMsg != null)? errMsg: "The value for Parameter[" +
              key + "] is invalid.";
      throw new IllegalArgumentException(errMsg);
    }
    
    param.setValue(value);
  }
  
  /**
   * Get Parameter[key]'s currently assigned value (or the default value). It locates
   * the parameter and call it getValue method to retrieve the value. It cast it TValue.
   * @param <TValue> extends Serializable
   * @param key the parameter key
   * @param defaultVal a default value to assign if the current value = null
   * @return the assigned value or the default value if the current value = null.
   */
  @SuppressWarnings("unchecked")
  public <TValue extends Serializable> TValue getValue(String key, TValue defaultVal) {
    TValue result = null;
    key = this.cleanKey(key);
    if (key == null) {
      throw new NullPointerException("The Parameter Key cannot be unassigned.");
    }
    
    XmlParameter param = null;
    if (this.paramMap.containsKey(key)) {
      param = this.paramMap.get(key);
    }
    
    if (param == null) {
      throw new NullPointerException("The PararameterMap does not contain Parameter[" +
              key + "].");
    }
    
    Serializable parVal = param.getValue();
    if (parVal == null) {
      result = defaultVal;
    } else {
      result = (TValue) parVal;
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Change Management">
  /**
   * Start the edit process by incrementing the internal editCount
   */
  public void beginEdits() {
    this.editCount = (this.editCount < 0)? 0: this.editCount;
    this.editCount++;
  }
  
  /**
   * End the edit process by decrementing the internal editCount. if (editCount=0), fire
   * the onChanged event.
   */
  public void endEdits() {
    if (this.editCount > 0) {
      this.editCount--;
    }
    if (this.editCount == 0) {
      this.fireValueChanged();
    }
  }  
  
  /**
   * Get this.isEditing state - indicating that a edit session is pending
   * @return (this.editCount > 0)
   */
  public boolean isEditing() {
    return (this.editCount > 0);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="XML Serializing">
  /**
   * Get the wrapped map as a list of VersionParamEntry
   * @return the VersionParamEntry list
   */
  @XmlElementWrapper(name="MapEntries")
  @XmlElement(name="parameter", type=XmlParameter.class)
  public List<XmlParameter> getMapEntries() {
    List<XmlParameter> result = new ArrayList<>();
    if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
      for (XmlParameter param : this.paramMap.values()) {
        result.add(param);
      }
    }
    return result;
  }
  
  /**
   * Assign the wrapped map's entries to Parameter map
   * @param mapEntries the new list to ParamMapEntry.
   */
  protected void setMapEntries(List<XmlParameter> mapEntries) {
    this.paramMap.clear();
    if ((mapEntries != null) && (!mapEntries.isEmpty())) {
      for (XmlParameter param : mapEntries) {
        String key = null;
        if ((param != null) && ((key = param.getKey()) != null)) {
          this.paramMap.put(key, param);
          
          param.ValueChanged.add(new EventDelegate(this) {        
            @Override
            public void onEvent(Object sender, EventArgs eventInfo) {
              XmlParameterMap listener = (XmlParameterMap) this.getListener();
              if (listener != null) {
                listener.fireValueChanged();
              }
            }
          });
        }
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    String result = "ParameterMap:";
    if (this.isEmpty()) {
      result += "\n - Empty";
    } else {
      for (XmlParameter param : this.paramMap.values()) {
        result += "\n - Param[key" + param.getKey()
                + "; value=" + param.getAsString()+ "]";
      }
    }
    return result;
  }
  //</editor-fold>
}
