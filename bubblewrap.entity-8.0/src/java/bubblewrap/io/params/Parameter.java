package bubblewrap.io.params;

import bubblewrap.core.annotations.Param;
import bubblewrap.core.annotations.ParamImpl;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import javax.resource.NotSupportedException;

/**
 * A Generic Class for Parameter key-value pair that support dynamic casting of the 
 * value if the casting is supported
 * @author kprins
 */
public class Parameter<TValue extends Serializable> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * A Static method for initiating a new Parameter instance from a {@linkplain Param}
   * annotation.
   * @param annotation the Param annotation
   * @return the Parameter instance
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public static Parameter fromAnnotation(Param annotation) throws NullPointerException,
          IllegalArgumentException {
    Parameter result = null;
    try {
      if (annotation == null) {
        throw new NullPointerException("The Annotation is unassigned.");
      }
      
      Class<?>[] parTypes = new Class<?>[]{Param.class};
      Object[] args = new Object[]{annotation};
      Constructor construct = null;
      Class<? extends Parameter> parClass = annotation.paramClass();
      if (parClass == null) {
        throw new NullPointerException("The Annotation's Parameter class is undefined");
      } else if ((construct = parClass.getConstructor(parTypes)) == null) {
        throw new IllegalArgumentException("Parameter Class["
                + parClass.getSimpleName() + "] does not support an Annotaion "
                + "Constructor." );
      }
      
      result = (Parameter) construct.newInstance(args);
    } catch (NullPointerException exp) {
      throw new NullPointerException(Parameter.class.getSimpleName()
              + ".fromAnnotation Error:\n " + exp.getMessage());
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException |
            InstantiationException | IllegalAccessException |
            InvocationTargetException exp) {
      throw new IllegalArgumentException(Parameter.class.getSimpleName()
              + ".fromAnnotation Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public A Void Parameter class">
  /**
   * A Void Parameter is a placeholder for a null Value as an assigned parameter
   */
  public static class Void extends Parameter<Serializable> {
    /**
     * A Void Parameter to represents a null Value
     * @param key
     */
    private Void(String key) {
      super(key,null);
    }
    
    /**
     * {@inheritDoc }
     * <p>
     * OVERRIDE: Always return null</p>
     */
    @Override
    public <TCast extends Serializable> TCast getValue() {
      return null;
    }
    /**
     * {@inheritDoc }
     * <p>
     * OVERRIDE: Ignore set value</p>
     */
    @Override
    public <TCast extends Serializable> void setValue(TCast value) {
    }
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Parameter Key
   */
  private String key;
  /**
   * Placeholder for the Parameter Value
   */
  private TValue value;
  /**
   * (Optional) The Format String to use in formating the parameter value (default = 
   * null|"").
   */
  private String format;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * EventHandler for sending a Value Changed event.
   */
  public final EventHandler ValueChanged;

  /**
   * Method called to fie the Value Changed event.
   */
  protected void fireValueChanged() {
    this.ValueChanged.fireEvent(this, new EventArgs());
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  private Parameter() {
    this.key = null;
    this.value = null;
    this.format = null;
    this.ValueChanged = new EventHandler();
  }
  
  /**
   * The Annotation based constructor called by the static {@linkplain 
   * #fromAnnotation(bubblewrap.core.annotations.Param) Parameter.fromAnnotation} method
   * @param annotation the parameter annotation
   */
  protected Parameter(Param annotation) {
    this();
    this.key = DataEntry.toParamKey(annotation.key());
    if (this.key == null) {
      throw new NullPointerException("The Parameter Key cannot be unassigned");
    }
    
    this.format = DataEntry.cleanString(annotation.format());
    this.value =  this.parse(annotation.value());
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public Parameter(String key, TValue value) {
    this();
    key = DataEntry.toParamKey(key);
    if (key == null) {
      throw new NullPointerException("The Parameter Key cannot be unassigned");
    }
    
    this.key = key;
    this.value = value;
  }
    
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   * @param format the format string to format the parameter value (can be null)
   */
  public Parameter(String key, TValue parValue, String format) {
    this();
    key = DataEntry.toParamKey(key);
    if (key == null) {
      throw new NullPointerException("The Parameter Key cannot be unassigned");
    }
    
    this.key = key;
    this.format = DataEntry.cleanString(format);
    this.value = parValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * FINAL: Get the TemplateManager's generically assigned TemplateOutline Class
   * @return Class&lt;TOutline&gt;
   */
  @SuppressWarnings("unchecked")
  public final Class<TValue> getValueClass() {
    Class myClass = this.getClass();
    return ReflectionInfo.getGenericClass(Parameter.class, myClass, 0);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Parameters">
  /**
   * Get the Parameter.key
   * @return the parameter key - not null
   */
  public String getKey() {
    return this.key;
  }
  
  /**
   * Get the Parameter.key
   * @return the parameter key - not null
   */
  public String getFormat() {
    return this.format;
  }
  
  /**
   * Get the Parameter.key
   * @return the parameter key - not null
   */
  public void setFormat(String format) {
    this.format = DataEntry.cleanString(format);
  }
  
  /**
   * Get the casted value of Parameter.value
   * @param <TCast> extends TValue
   * @return the casted value or null if unassigned.
   */
  @SuppressWarnings("unchecked")
  public <TCast extends TValue> TCast getValue() {
    TCast result = null;
    try {
      if (this.value != null) {
        result = (TCast) this.value;
      }
    } catch (Exception exp) {
      result = null;
    }
    return result;
  }
  
  /**
   * Set the casted value of Parameter.value
   * @param <TCast> extends TValue
   * @return the new value to assign
   */
  @SuppressWarnings("unchecked")
  public <TCast extends TValue> void setValue(TCast value) {
    if (DataEntry.isEq(value, this.value)) {
      this.value = value;
      this.fireValueChanged();
    }
  }
  
  /**
   * Get the parameter value casted as a number of type toClass. If the value or 
   * toClass is undefined or invalid, return the assigned pDefault. 
   * @param <TNum> extends Number
   * @param toClass the Number sub-class to which to convert the parameter value to.
   * @param defValue a default value to return
   * @return the casted value or pDefault if unassigned.
   */
  @SuppressWarnings("unchecked")
  public <TNum extends Number> TNum getNumberValue(Class<TNum> toClass, TNum defValue){
    TNum result = null;
    try {
      if ((toClass != null) && (this.value != null)) {
        Class valClass = this.getValueClass();
        if (valClass.equals(toClass)) {
          result = (TNum) this.value;
        } else if (Number.class.isAssignableFrom(valClass)) {
          Number numVal = (Number) this.value;
          result = DataConverter.convertTo(numVal, toClass);
        } else {
          String strVal = this.value.toString();
          result = DataConverter.toValue(strVal, toClass);
        }
      }
    } catch (Exception exp) {
      result = null;
    }
    return (result == null)? defValue: result;
  }
  
  /**
   * Get the casted value of Parameter.value and if undefined or invalid, return the 
   * assigned pDefault. Use the DataConverter.toBoolean passing in the value as string.
   * @param defValue a default boolean value to return
   * @return the parameter value casted as a boolean value or defValue if unassigned.
   */
  @SuppressWarnings("unchecked")
  public Boolean getBoolValue(Boolean defValue) {
    Boolean result = null;
    try {
      if (this.value != null) {
        String strVal = this.value.toString();
        result = DataConverter.toBoolean(strVal);
      }
    } catch (Exception noTrapExp) {
      result = null;
    }
    return (result == null)? defValue: result;    
  }
  
  /**
   * Check is key matches Parameter.key
   * @param key the key to compare
   * @return true if the keys match (not case sensitive)
   */
  public boolean isKey(String key) {
    return ((this.key != null) && (this.key.equalsIgnoreCase(key)));
  }
  
  /**
   * Check if the Parameter.value is unassigned
   * @return true=value is unassigned.
   */
  public boolean isNull() {
    return (this.value == null);
  }
  
  /**
   * Check is the Parameter.value can be casted to valClass
   * @param <T> extends Serializable
   * @param inClass a Class of type T
   * @return true if the value can be casted or isNull.
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> boolean isValueType(Class<T> inClass) {
    boolean result = (inClass != null);
    try {
      Class<TValue> valClass = null;
      if ((result)  && ((valClass = this.getValueClass()) != null)) {
        result = inClass.isAssignableFrom(valClass);
      }
    } catch (Exception exp) {
      result = false;
    }
    return result;
  }
  
  /**
   * Check is Parameter.value is equal to pValue.
   * @param <T> extends Number
   * @param parValue the value to compare
   * @return return true if both are null or is equal.
   */
  @SuppressWarnings("unchecked")
  public <T extends Number> boolean isValue(T parValue) {
    boolean result = ((parValue == null) && (this.value == null));
    if((!result) && (parValue != null) && (this.value != null)) {
      T curValue = null;
      Class<T> toClass = (Class<T>) parValue.getClass();
      try{
        if (toClass.isInstance(this.value)) {
          curValue = (T) this.value;
        }
        if (curValue != null) {
          result = curValue.equals(parValue);
        }
      } catch(Exception exp) {
        result = false;
      }
    }
    return result;
  }
  
  /**
   * Get this Parameter as runtime Param annotation ({@linkplain ParamImpl})
   * @return new ParamImpl(this);
   * @throws IllegalArgumentException is the value cannot be converted
   */
  public final ParamImpl asParam() {
    ParamImpl result = null;
    try {
      result = new ParamImpl(this);
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".asParam Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called by the {@linkplain #Parameter(bubblewrap.core.annotations.Param) annotation 
   * constructor} to convert the parameter from a string value
   * @param strVal the specified string value
   * @return the value as a string (return = "" if null)
   * @throws IllegalArgumentException is the value cannot be converted
   */
  public final String asString() {
    String result = null;
    try {
      result = this.onAsString();
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".asString Error:\n " + exp.getMessage());
    }
    return (result == null)? "": result;
  }
  
  /**
   * Called by the {@linkplain #asString() this.asString} to convert the parameter's
   * value to a string value.
   * <p>The base method returns (this.value == null)? "": this.value.toString();
   * @return the value as a string
   * @throws IllegalArgumentException is the value cannot be converted
   */
  protected String onAsString() throws Exception {
    return (this.value == null)? "": this.value.toString();
  }
  
  /**
   * Called by the {@linkplain #Parameter(bubblewrap.core.annotations.Param) annotation 
   * constructor} to convert the parameter from a string value
   * @param strVal the specified string value
   * @return the parsed value
   * @throws IllegalArgumentException is the value cannot be converted
   */
  private TValue parse(String strVal) {
    TValue result = null;
    try {
      strVal = DataEntry.cleanString(strVal);
      if (strVal != null) {
        result = this.onParse(strVal);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".parse Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called by the {@linkplain #Parameter(bubblewrap.core.annotations.Param) annotation 
   * constructor} to convert the parameter from a string value
   * @param strVal the specified string value
   * @return the parsed value
   * @throws NotSupportedException if the value cannot be converted
   */
  protected TValue onParse(String strVal) throws Exception {
    throw new NotSupportedException("The Parameter's onParse method is not "
            + "implemented for the generic class.");
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * Return this.key + "=" + this.value.toString. If this.value=null, return 
   * this.key + "=". If this.value.class=String, put the value in quotes.
   * @return this instance properties as a sting
   */
  @Override
  public String toString() {
    String result = this.key + "=";
    if (this.value != null) {
      if (this.value.getClass().equals(String.class)) {
        result += "\"" + this.value.toString() + "\"";
      } else {
        result += this.value.toString();
      }
    }
    return super.toString();
  }
  //</editor-fold> 
}
