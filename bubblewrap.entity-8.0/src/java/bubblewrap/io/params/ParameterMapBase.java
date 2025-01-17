package bubblewrap.io.params;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import bubblewrap.io.ObjectData;
import bubblewrap.io.interfaces.IObjectData;
import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.enums.StringMatch;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>The ParameterMapBase is a Wrapper for a Parameter HashMap<String, Serializable> that
 * manages the put and set of parameters. It also support the substitution of 
 * Replacement Tags. It also implement IObjectData, which implementation saves the
 * Parameters, but not the MapOwenr reference. It has to be re-assigned after 
 * deserialization.</p>
 * <p><b>NOTE:</b> The ParameterMapBase changes are managed using a isDirty state (i.e., the
 * Mapped values has saved since the last save), an onChanged event send to the map's
 * assigned mpOwner, and a call back function that should be called by the owner after
 * the map has been successfully saved to reset the isDirty state.  To prevent multiple
 * save during initiation or multi-parameter updates, the beginEdit and endEdit can be 
 * called in pairs to increment and decrement the internal editCount. The onChanged 
 * event will only be fired if an endEdit is called, the editCount=0, and the map is
 * dirty.</p>
 * <p>The ParameterMapBase is also the ParameterMapOwner of any children of instance 
 * ParameterMapBase. Thus, any changes to these changes will be perpetuated to this 
 * Parameter's owner and a callback from the owner will be perpetuated up the tree.</p>
 * @see DataEntry#getReplacementTag(java.lang.String) 
 * @author kprins
 */ 
public abstract class ParameterMapBase<TMap extends ParameterMapBase<TMap>> 
                                                     implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
                                     Logger.getLogger(ParameterMapBase.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * An internally generate mapID to uniquely identify this set of data
   */
  protected String mapId;
  /**
   * Placeholder for the Serialized data of the Object's Fields
   */
  private HashMap<String, Serializable> paramMap;  
  /**
   * An internal count to prevent firing the change events while updating.
   */
  private transient int editCount = 0;
  /**
   * FLag stating the Map's dirty state
   */
  private transient boolean dirty = false;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Event Sender">
  /**
   * EventHandler for sending a Value Changed event.
   */
  public final EventHandler ValueChanged;
  /**
   * Method called to fie the Value Changed event. The event is not fired while 
   * this.isEditing
   */
  protected void fireValueChanged() {
    if (!this.isEditing()) {
      this.ValueChanged.fireEvent(this, new EventArgs());
    }
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Parameterless Constructor  - call this(pOwner=null)
   */
  protected ParameterMapBase() {
    super(); 
    this.ValueChanged = new EventHandler();
    this.paramMap = null;
    try {
      this.mapId = DataEntry.newUniqueId();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.new.initiate MapId Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }  

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Called super method before call this.ValueChanged.clear() to remove
   * any listeners and the call this.clear to dispose the paramMap and remove this
   * instance as the listener of any ITreeItems</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();    
    this.ValueChanged.clear();
    this.clear();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * Get the unique mapId of this dataset
   * @return String
   */
  public final String getMapID() {
    return this.mapId;
  }
  
  /**
   * Check if the map is empty.
   * @return boolean
   */
  public boolean isEmpty() {
    return ((this.paramMap == null) || (this.paramMap.isEmpty()));
  }
  
  /**
   * Get the number of parameters in the map.
   * @return boolean
   */
  public int size() {
    return (this.paramMap == null)? 0 : this.paramMap.size();
  }
  
  /**
   * Return true if the Map contains the key.
   * @param parKey String
   * @return boolean
   */
  public boolean containsKey(String parKey) {
    parKey = DataEntry.cleanString(parKey);
    return ((parKey == null) || (this.paramMap == null))? false: 
                                    this.paramMap.containsKey(parKey);
  }
  
  /**
   * Get the first Parameter Key that matched the <tt>partialKey</tt> for the specified
   * matching criteria.
   * @param partialKey the partial key phrase
   * @param matchType the specified match type (default = FULL)
   * @param ignoreCase true to make match case insensitive
   * @return the first matching key or null if no match is found.
   */
  public String findKey(String partialKey, StringMatch matchType, boolean ignoreCase) {
    String result = null;
    if ((partialKey = DataEntry.cleanString(partialKey)) != null) {
      for (String parKey : this.getKeys()) {
        if (DataEntry.isMatch(parKey, partialKey, matchType, ignoreCase)) {
          result = parKey;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * <p>Save the Parameter[parKey]. If (newValue=null), it will remove the Key if it 
   * exists. if (value != null), the value will be added if it does not exist or 
   * replaced other otherwise. It fires this.ValueChanged event if the parameter value
   * has changed.</p>
   * <p><b>NOTE:</b> The Parameter Keys are case sensitive to be consistent with other
   * java reflection features. However, the passed field names are stripped from any
   * leading and trailing spaces.</p>
   * @param <T extends Serializable>
   * @param paramKey String
   * @param newValue T
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> void put(String paramKey, T newValue) 
                                                                     throws Exception {
    try {
      paramKey = DataEntry.cleanString(paramKey);
      if (paramKey == null) {
        throw new Exception("The Parameter Key cannot by uanssigned");
      }
      
      if (newValue == null) {
        if ((this.paramMap != null) && (this.paramMap.containsKey(paramKey))) {
          Serializable curValue = this.paramMap.get(paramKey);
          if (curValue instanceof Parameter) {
            Parameter parItem = (Parameter) curValue;
            parItem.ValueChanged.remove(this);
          }
          this.paramMap.remove(paramKey);
          this.dirty = true;
          this.fireValueChanged();
        }
      } else {
        Class<T> valClass = (Class<T>) newValue.getClass();
        if ((this.paramMap != null) && (this.paramMap.containsKey(paramKey))) {           
          Serializable curValue = this.paramMap.get(paramKey);
          if ((curValue != null) && (curValue instanceof Parameter)) {
            Parameter curPar = (Parameter) curValue;
            if (valClass.equals(curValue.getClass())) {
              Parameter newPar = (Parameter) newValue;
              curPar.setValue(newPar.getValue());
            } else if (valClass.equals(curPar.getValueClass())) {
              curPar.setValue(newValue);
            } else {
              curPar.ValueChanged.remove(this);  
              this.paramMap.put(paramKey, newValue);
              this.dirty = true;   
              
              if (newValue instanceof Parameter) {
                Parameter newPar = (Parameter) newValue;
                newPar.ValueChanged.add(new ValueChangedDelegate(this));
              }
              
              this.fireValueChanged();
            }
          } else if (!DataEntry.isEq(curValue, newValue)) {
            this.paramMap.put(paramKey, newValue);
            this.dirty = true;
            
            if (newValue instanceof Parameter) {
              Parameter newPar = (Parameter) newValue;
              newPar.ValueChanged.add(new ValueChangedDelegate(this));
            }
            this.fireValueChanged();
          }
        } else {
          if (this.paramMap == null) {
            this.paramMap = new HashMap<>();
          }
          
          this.paramMap.put(paramKey, newValue);
          this.dirty = true;
          
          if (newValue instanceof Parameter) {
            Parameter parItem = (Parameter) newValue;
            parItem.ValueChanged.add(new ValueChangedDelegate(this));
          }
          this.fireValueChanged();
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".put Error:\n " + pExp.getMessage());
    }
  }
  
  /**
   * <p>Return the assigned value casted to T. If value of the parKey is a Parameter, 
   * it will return the parameter's value cast as type T. 
   * All errors are logged and the defautlValue is returned.</p>
   * <p><b>NOTE:</b> The parKey are case sensitive to be consistent with other
   * java reflection features. However, the passed field names are stripped from any
   * leading and trailing spaces.</p>
   * <p>
   * <b>Note:</b>Use theTo access the parameter</p>
   * @param <T> extends Serializable
   * @param parKey the parameter
   * @param defaultValue T
   * @return the current value or defaultValue if the value could not be found or is 
   * unassigned
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T get(String parKey, T defaultValue) {
    T result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if ((parValue != null) && (!Parameter.Void.class.equals(parValue.getClass()))) {
          if (parValue instanceof Parameter) {
            Parameter par = (Parameter) parValue;
            result = (T) par.getValue();
          } else {
            result = (T) parValue;
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.get Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Check if the parameter[parKey] is assigned and an instance of Parameter.
   * @param parKey the case sensitive parameter Key to search for.
   * @return true is the parameter exist and it is a match
   */  
  public boolean isParameter(String parKey) {
    return this.isParameter(parKey, null);
  }
  
  /**
   * Check if the parameter[parKey] is assigned and an instance of Parameter and 
   * assignable from paramClass.
   * @param parKey the case sensitive parameter Key to search for.
   * @return true is the parameter exist and it is a match
   */  
  public boolean isParameter(String parKey, Class<? extends Parameter> paramClass) {
    Serializable parValue = null;
    paramClass = (paramClass == null)? Parameter.class: paramClass;
    return (((parKey = DataEntry.cleanString(parKey)) != null) && (!this.isEmpty()) &&
            (this.paramMap.containsKey(parKey)) &&
            ((parValue = this.paramMap.get(parKey)) != null) &&
            (paramClass.isAssignableFrom(parValue.getClass())));
  }
  
  /**
   * Get the Parameter value for the specified parKey - if found and the assigned 
   * parameter value if of type Parameter
   * @param parKey the case sensitive parameter Key to search for.
   * @return the Parameter or null is unassigned or not a Parameter value.
   */
  public Parameter getParameter(String parKey) {
    Parameter result = null;
    Serializable parValue = null;
    if (((parKey = DataEntry.cleanString(parKey)) != null) && (!this.isEmpty()) &&
            (this.paramMap.containsKey(parKey)) &&
            ((parValue = this.paramMap.get(parKey)) != null) &&
            (parValue instanceof Parameter)) {
      result = (Parameter) parValue;
    }
    return result;
  }
  
  /**
   * Get the Parameter Value as a String
   * @param parKey the case sensitive parameter Key to search for.
   * @param defaultValue the default value (if the parameter value is not found or null)
   * @return the assigned value or the default value.
   */
  public String getAsString(String parKey, String defaultValue) {
    String result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if (parValue != null) {
          if (parValue instanceof Parameter) {
            Parameter par = (Parameter) parValue;
            parValue = par.getValue();
          } 
          result = parValue.toString();
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getAsString Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Parameter Value as an Integer value
   * @param parKey the parameter Key
   * @param defaultValue the default value (if the parameter value is not found or null)
   * @return the assigned value or the default value.
   */  
  public Integer getAsInt(String parKey, Integer defaultValue) {
    Integer result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if ((parValue != null) && (parValue instanceof Parameter)) {
          if (parValue instanceof NumericParameter) {
            NumericParameter par = (NumericParameter) parValue;
            parValue = par.getNumberValue(Integer.class, null);
          } else {
            Parameter par = (Parameter) parValue;
            parValue = par.getValue();
          }
        }
      
        if (parValue != null) {
          if (parValue instanceof Integer) {
            result = (Integer) parValue;
          } else if ((parValue instanceof Number) || (parValue instanceof String)) {
            String strValue = parValue.toString();
            result = DataConverter.toValue(strValue, Integer.class);
          } else if (parValue instanceof Boolean) {
            result = ((Boolean) parValue)? 1: 0;
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getAsInt Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Parameter Value as a Long value
   * @param parKey the parameter Key
   * @param defaultValue the default value (if the parameter value is not found or null)
   * @return the assigned value or the default value.
   */  
  public Long getAsLong(String parKey, Long defaultValue) {
    Long result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if ((parValue != null) && (parValue instanceof Parameter)) {
          if (parValue instanceof NumericParameter) {
            NumericParameter par = (NumericParameter) parValue;
            parValue = par.getNumberValue(Long.class, null);
          } else {
            Parameter par = (Parameter) parValue;
            parValue = par.getValue();
          }
        }
      
        if (parValue != null) {
          if (parValue instanceof Long) {
            result = (Long) parValue;   
          } else if ((parValue instanceof Number) || (parValue instanceof String)) {
            String strValue = parValue.toString();
            result = DataConverter.toValue(strValue, Long.class);
          } else if (parValue instanceof Boolean) {
            result = ((Boolean) parValue)? 1l: 0l;
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getAsLong Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Parameter Value as a Double value
   * @param parKey the parameter Key
   * @param defaultValue the default value (if the parameter value is not found or null)
   * @return the assigned value or the default value.
   */  
  public Double getAsDouble(String parKey, Double defaultValue) {
    Double result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if ((parValue != null) && (parValue instanceof Parameter)) {
          if (parValue instanceof NumericParameter) {
            NumericParameter par = (NumericParameter) parValue;
            parValue = par.getNumberValue(Double.class, null);
          } else {
            Parameter par = (Parameter) parValue;
            parValue = par.getValue();
          }
        }
      
        if (parValue != null) {
          if (parValue instanceof Double) {
            result = (Double) parValue;
          } else if ((parValue instanceof Number) || (parValue instanceof String)) {
            String strValue = parValue.toString();
            result = DataConverter.toValue(strValue, Double.class);
          } else if (parValue instanceof Boolean) {
            result = ((Boolean) parValue)? 1.0d: 0.0d;
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getAsDouble Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Get the Parameter Value as a Boolean value
   * @param parKey the parameter Key
   * @param defaultValue the default value (if the parameter value is not found or null)
   * @return the assigned value or the default value.
   */  
  public Boolean getAsBoolean(String parKey, Boolean defaultValue) {
    Boolean result = null;
    try {
      parKey = DataEntry.cleanString(parKey);
      if ((parKey != null) && (!this.isEmpty())
              && (this.paramMap.containsKey(parKey))) {
        Serializable parValue = this.paramMap.get(parKey);
        if ((parValue != null) && (parValue instanceof Parameter)) {
          if (parValue instanceof NumericParameter) {
            NumericParameter par = (NumericParameter) parValue;
            parValue = par.getBoolValue(null);
          } else {
            Parameter par = (Parameter) parValue;
            parValue = par.getValue();
          }
        }
      
        if (parValue != null) {
          if (parValue instanceof Boolean) {
            result = (Boolean) parValue;
          } else if ((parValue instanceof Number) || (parValue instanceof String)) {
            String strValue = parValue.toString();
            result = DataConverter.toBoolean(strValue);
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getAsBoolean Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? defaultValue: result;
  }
  
  /**
   * Clear all Parameter and fire the onChanged event.
   */
  public void clear() {
    if (this.paramMap != null) {
      if (!this.paramMap.isEmpty()) {
        for (Serializable child : this.paramMap.values()) {
          if (child instanceof Parameter) {
            Parameter parItem = (Parameter) child;
            parItem.ValueChanged.remove(this);
          }
        }
      }
      
      this.paramMap.clear();
      this.paramMap = null;
      this.dirty = true;
      this.fireValueChanged();
    }
  }
  
  /**
   * Get a set of the ParameterMapBase's keys
   * @return a set of the keys or an empty set if no parameters are assigned
   */
  public Set<String> getKeys() {
    Set<String> result = null;
    if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
      result = this.paramMap.keySet();
    }
    return (result == null)? new HashSet<String>(): result;
  }
  
  /**
   * Get a collection of the ParameterMapBase's values
   * @return a collection of Serializable objects or an empty collection if no 
   * parameters are assigned.
   */
  public Collection<Serializable> getValues() {
    Collection<Serializable> result = null;
    if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
      result = this.paramMap.values();
    }
    return (result == null)? new ArrayList<Serializable>(): result;
  }
  
  /**
   * <p>Search and replace an occurrences of this.Parameter Keys in sInsStr. Replacement 
   * tags are identified if they are in self enclosing tags (e.g., <MyParam/> where the
   * Key in this.Parameter Key "MyParam"). Null values will be replaced with "". The
   * original string is returned if an error occurred and the error is logged.</p> 
   * <p>If sPrefix!= null, its will be appended to this Map's key to form a combined
   * Replacement Tag (e.g., tag="<MyPrefix.MyParam/>"). Also if this Map's parameter 
   * value if of type ParameterMapBase, it will call this Map.replaceParameterTags method 
   * passing this parameter comboKey (e.g., "MyPrefix.MyParam" as the prefix.</p>
   * @param inputStr String
   * @return String
   */
  public String replaceParameterTags(String inputStr, String prefix) {
    String result = inputStr;
    try {
      prefix = DataEntry.cleanString(prefix);
      String startStr = DataEntry.cleanString(inputStr);
      String endStr = null; 
      if ((startStr != null) && (startStr.contains("/>"))) {
        if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
          for (String parKey : this.paramMap.keySet()) {
            Serializable parValue = this.paramMap.get(parKey);
            if (parValue == null) {
              continue;
            }
            
            String comboKey = (prefix == null)? parKey: prefix + "." +parKey;
            String tag = "<" + comboKey + "/>";
            if (parValue instanceof ParameterMapBase) {
              ParameterMapBase childMap = (ParameterMapBase) parValue;
              childMap.replaceParameterTags(startStr, comboKey);
            } else {
              String strValue = (parValue == null)? "": parValue.toString();
              endStr = startStr.replace(tag, strValue);
              startStr = endStr;
              if (!endStr.contains("/>")) {
                break;
              }
            }
          }
        }        
        result = startStr;
      }      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.replaceParameterTags Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Generates a Html Report of the ParameterMapBase's assigned values in the format:
   * "<b>" + sCaption + "</b><ul>" + "<li>" + parKey + " = " + pValue.toString() + "</li>"
   * + ... "</ul>". If pValue is an instance of ParameterMapBase, its getAsHtml(parKey) is
   * called instead of toString().
   * caption sCaption String
   * @return String
   */
  public final String getAsHtml(String caption) {
    String result = null;
    try {
      caption = DataEntry.cleanString(caption);
      caption = (caption == null)? "ParameterMap Elements:": caption;
      result = "<b>" + caption + "</b><ul>";
      try {
        result += "<li>MapId = " + this.mapId + "</li>";
        
        if ((this.paramMap == null) && (this.paramMap.isEmpty())) {
          result += "<li>-- Empty --</li>";
        } else {
          for (String parKey : this.paramMap.keySet()) {
            Serializable pValue = this.paramMap.get(parKey);
            String sValue = "null";
            if (pValue != null) {
              if (pValue instanceof ParameterMapBase) {
                ParameterMapBase pChild = (ParameterMapBase) pValue;
                sValue = pChild.getAsHtml(parKey);
              } else {
                sValue = pValue.toString();
              }
            } 
            result += "<li>" + parKey + " = " + sValue + "</li>";
          }
        }
      } finally {
        result += "</ul>";
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getAsHtml Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      result += "<b>Report Error:</b> <i>" + pExp.getMessage() + "</i>";
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Clone Support">
  /**
   * Assign all parameters in this instance to pTarget as well as this instance's mapId.
   * If pTarget has an assigned MapOwner, the onChange event will only be fired after 
   * all parameters are assigned.
   * @param pTarget ParameterMapBase
   */
  public void assignTo(TMap target) {
    try {
      if ((target != null) && (this.size() > 0)) {
        try {
          target.beginEdits();
          for (String parKey : this.paramMap.keySet()) {
            Serializable curVal = this.paramMap.get(parKey);
            target.put(parKey, curVal);
          }
          target.mapId = this.mapId;
        } finally {
          target.endEdits();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.assignTo Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Get a clone of this instance. Not the MapOwner reference is not transfer.
   * @return
   */
  @SuppressWarnings("unchecked")
  @Override
  public TMap clone() {
    TMap result = null;
    try {
      result = (TMap) this.getClass().newInstance();
      this.assignTo(result);      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clone Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
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
   * Check if the ParameterMapBase is an editing state
   * @return true if this.editCount > 0.
   */
  protected boolean isEditing() {
    return (this.editCount > 0);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * OVERRIDE: Compare whether obj has the same MapId or both are unassigned. 
   * @param obj the parameterMap to evaluate for equality
   * @return true if the mapId's match and neither this or obj is dirty.
   */
  @Override
  public boolean equals(Object obj) {
    boolean bResult = ((obj != null) && (obj instanceof ParameterMapBase));
    if (bResult) {
      ParameterMapBase pMap = (ParameterMapBase) obj;
      bResult = (((this.mapId == null) && (pMap.mapId == null))
              || ((this.mapId != null) 
               && (this.mapId.equalsIgnoreCase(pMap.mapId))));      
    }
    return bResult;
  }
  
  /**
   * OVERRIDE: return a HashCode based on the MapId.
   * @return
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + (this.mapId != null ? this.mapId.hashCode() : 0);
    return hash;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement IObjectData">
  /**
   * OVERRIDE: Serialize the ParameterMapBase's parameters HashMap to ObjectData[params]
   * @return ObjectData
   */
  @Override
  public ObjectData serializeObject() {
    ObjectData result = null;
    try {
      result = new ObjectData(this);
      result.put("mapId", this.mapId);
      result.put("params", this.paramMap);
    } catch (Exception pExp) {
      result = null;
      logger.log(Level.WARNING, "{0}.serializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * OVERRIDE: Deserialize the ParameterMapBase. Set mpParam=null if undefined.
   * @param pData ObjectData
   */
  @Override
  public void deserializeObject(ObjectData pData) {
    try {
      this.paramMap = pData.getCasted("params", null);
      this.mapId = pData.getCasted("mapId", null);
      if (this.mapId == null) {
        this.mapId = DataEntry.newUniqueId();
      }
      
      /**
       * Assign itself as the event listener to any children that are of instance
       * ITreeItem.
       */
      if ((this.paramMap != null) && (!this.paramMap.isEmpty())) {
        for (Serializable pValue : this.paramMap.values()) {
          if ((pValue != null) && (pValue instanceof Parameter)) {
            Parameter paramItem = (Parameter) pValue;
            paramItem.ValueChanged.add(new ValueChangedDelegate(this));
          }
        }
      }      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="ValueChangedDelegate Class">
  /**
   * An EventDelegate for handling IParamItem's ValueChanged Events
   */
  private class ValueChangedDelegate extends EventDelegate {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public ValueChangedDelegate(ParameterMapBase listener) {
      super(listener);
    }
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Call the super method before disposing local resources</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
    //</editor-fold>
    
    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(Object sender, EventArgs eventInfo) {
      ParameterMapBase listener = (ParameterMapBase) this.getListener();
      if (listener != null) {
        listener.dirty = true;
        listener.fireValueChanged();
      }
    }
  }
//</editor-fold>
}
