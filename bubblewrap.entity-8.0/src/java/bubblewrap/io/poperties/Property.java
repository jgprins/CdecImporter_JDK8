package bubblewrap.io.poperties;

import bubblewrap.io.*;
import bubblewrap.core.events.*;
import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.entity.annotations.FieldValidation;
import bubblewrap.io.converters.DataConverter;
import bubblewrap.io.converters.FieldValueConverter;
import bubblewrap.io.interfaces.IParamItem;
import bubblewrap.io.poperties.annotation.PropertyDef;
import bubblewrap.io.validators.InputValidator;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.NotSupportedException;
import org.json.JSONObject;

/**
 * A Generic Class for Property key-value pair that support dynamic casting of the 
 * value if the casting is supported
 * @author kprins
 */
public abstract class Property<TValue extends Serializable> implements IParamItem {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(Property.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * A Static method for initiating a new Property instance from a {@linkplain
   * PropertyDef} annotation.
   * @param annotation the PropertyDef annotation
   * @return the Property instance
   * @throws NullPointerException
   * @throws IllegalArgumentException
   */
  public static Property initProperty(PropertyDef annotation) {
    Property result = null;
    try {
      if (annotation == null) {
        throw new NullPointerException("The Annotation is unassigned.");
      }
      
      Class<?>[] parTypes = new Class<?>[]{PropertyDef.class};
      Object[] args = new Object[]{annotation};
      Constructor construct = null;
      Class<? extends Property> propClass = annotation.propClass();
      if (propClass == null) {
        throw new NullPointerException("The Annotation's Property class is undefined");
      } else if ((construct = propClass.getConstructor(parTypes)) == null) {
        throw new IllegalArgumentException("Property Class["
                + propClass.getSimpleName() + "] does nopt support an Annotaion "
                + "Constructor." );
      }
      
      result = (Property) construct.newInstance(args);
      if (result.hasError()) {
        throw new IllegalArgumentException(result.getErrorMsg());
      }
    } catch (NullPointerException exp) {
      throw new NullPointerException(Property.class.getSimpleName()
              + ".initParam Error:\n " + exp.getMessage());
    } catch (NoSuchMethodException | SecurityException | IllegalArgumentException |
            InstantiationException | IllegalAccessException |
            InvocationTargetException exp) {
      throw new IllegalArgumentException(Property.class.getSimpleName()
              + ".initParam Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Property Key
   */
  private String key;
  /**
   * Placeholder for the Property Value
   */
  private TValue value;
  /**
   * Set to true if the value has changed using the setValue method
   */
  private Boolean dirty;
  /**
   * Placeholder for a Field Value Validator 
   */
  private InputValidator<TValue> validator;
  /**
   * Placeholder for a Field Value Converter 
   */
  private FieldValueConverter<TValue> converter;
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
  /**
   * Private Constructor
   */
  private Property() {
    this.ValueChanged = new EventHandler();
    this.key = null;
    this.value = null;
    this.converter = null;
    this.validator = null;
    this.dirty = null;
    this.errorMsg = null;
  }
  
  /**
   * The Annotation based constructor called by the static {@link 
   * #initProperty(bubblewrap.io.poperties.annotation.PropertyDef) Property.initProperty} 
   * method
   * @param annotation the property definition annotation
   */
  protected Property(PropertyDef annotation) {
    this();
    this.key = DataEntry.cleanString(annotation.key());
    if (this.key == null) {
      throw new NullPointerException("The Property Key cannot be unassigned");
    }
    
    this.value =  this.parse(annotation.value());
    this.dirty = null;
    this.initValidator(annotation.validation());
    this.initConverter(annotation.converter());
  }
  
  /**
   * Default Constructor with a parameter key and value
   * @param key the parameter key (required)
   * @param value the parameter value (can be null)
   */
  public Property(String key, TValue value) {
    this();
    key = DataEntry.cleanString(key);
    if (key == null) {
      throw new NullPointerException("The Property Key cannot be unassigned");
    }
    
    this.key = key;
    this.value = value;
    this.dirty = null;
  }
  
  /**
   * Called by the constructor to initiate the Property's InputValidator
   * @param annot the assigned FieldValidation annotation
   */
  private void initValidator(FieldValidation annot) {
    Class<? extends InputValidator> validatorClass = null;
    if ((annot == null) && ((validatorClass = annot.validator()) != null) &&
            (!validatorClass.equals(InputValidator.Void.class))) {
      try {
        this.validator = validatorClass.newInstance();
        if (this.validator == null) {
          throw new Exception("Initiating the " + validatorClass.getSimpleName() 
                  + " validator failed.");
        }
        
        this.validator.setOptions(annot.options());
        if (this.validator.hasError()) {
          throw new Exception(this.validator.getErrorMsg());
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.initValidator Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  
  /**
   * Called by the constructor to initiate the Property's InputValidator
   * @param annot the assigned FieldValidation annotation
   */
  private void initConverter(FieldConverter annot) {
    Class<? extends FieldValueConverter> converterClass = null;
    if ((annot == null) && ((converterClass = annot.converter()) != null) &&
            (!converterClass.equals(FieldValueConverter.Void.class))) {
      try {
        this.converter = converterClass.newInstance();
        if (this.converter == null) {
          throw new Exception("Initiating the " + converterClass.getSimpleName() 
                  + " converter failed.");
        }
        
        this.converter.setOptions(annot.options());
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.initValidator Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Error Message Handling">
  /**
   * Placeholder of an error message during execution
   */
  private String errorMsg;

  /**
   * Get whether an error has been reported
   * @return (this.errorMsg != null)
   */
  public boolean hasError() {
    return (this.errorMsg != null);
  }

  /**
   * Clear the current error message
   */
  public void clearError() {
    this.errorMsg = null;
  }

  /**
   * get the current Error Message
   * @return this.errorMsg (can be null)
   */
  public String getErrorMsg() {
    return this.errorMsg;
  }

  /**
   * Set an error message. If this.errMsg != null, the new error message will be appended
   * separated with a ";\n " delimiter. The call is ignored if the new message = ""|null.
   * @param errMsg new error message
   */
  protected void setErrorMsg(String errMsg) {
    errMsg = DataEntry.cleanString(errMsg);
    if (errMsg != null) {
      if (this.errorMsg == null) {
        this.errorMsg = errMsg;
      } else {
        this.errorMsg += ";\n " + errMsg;
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Propertys">
  /**
   * Get the Property.key
   * @return the parameter key - not null
   */
  public String getKey() {
    return this.key;
  }
  
  /**
   * Get the currently assigned Property.value
   * @return the unassigned value
   */
  public TValue getValue() {
    return this.value;
  }
  
  /**
   * Set the casted value of Property.value
   * @param newValue extends TValue
   * @return the new value to assign
   */
  @SuppressWarnings("unchecked")
  public void setValue(TValue newValue) {
    this.clearError();
    if ((!DataEntry.isEq(newValue, this.value)) && (this.isValidInput(newValue))) {
      this.value = newValue;
      this.dirty = true;
      this.fireValueChanged();
    }
  }
  
  /**
   * Get the casted value of Property.value
   * @return the casted value or null if unassigned.
   */
  @SuppressWarnings("unchecked")
  public String getStringValue() {
    String result = null;
    if (this.value != null) {
      if (this.converter != null) {
        result = this.converter.toStringValue(this.value);
      } else if (this.validator != null) {
        result = this.validator.toString(this.value);
      } else {
        result = this.value.toString();
      }
    }
    return result;
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
        Class valclass = this.value.getClass();
        if (valclass.equals(toClass)) {
          result = (TNum) this.value;
        } else if (Number.class.isAssignableFrom(valclass)) {
          Number numVal = (Number) this.value;
          result = DataConverter.convertTo(numVal, toClass);
        } else {
          String strVal = this.value.toString();
          result = DataConverter.toValue(strVal, toClass);
        }
      }
    } catch (Exception noTrapExp) {
      result = null;
    }
    return (result == null)? defValue: result;
  }
  
  /**
   * Get the casted value of Property.value and if undefined or invalid, return the 
   * assigned pDefault. Use the DataConverter.toBoolean passing in the value as string.
   * @param defValue a default boolean value to return
   * @return the parameter value casted as a boolean value or pDefault if unassigned.
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
   * Check is key matches Property.key
   * @param key the key to compare
   * @return true if the keys match (not case sensitive)
   */
  public boolean isKey(String key) {
    return ((this.key != null) && (DataEntry.isEq(this.key, key, true)));
  }
  
  /**
   * Check if the Property.value is unassigned
   * @return true=value is unassigned.
   */
  public boolean isNull() {
    return (this.value == null);
  }
  
  /**
   * Check is the Property.value can be casted to valClass
   * @param <T> extends Serializable
   * @param valClass a Class of type T
   * @return true if the value can be casted or isNull.
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> boolean isValueType(Class<T> valClass) {
    boolean result = (valClass != null);
    try {
      if ((result)  && (this.value != null)) {
        result = valClass.isInstance(this.value);
      }
    } catch (Exception pExp) {
      result = false;
    }
    return result;
  }
  
  /**
   * Check is Property.value is equal to pValue.
   * @param <T> extends Number
   * @param otherValue the value to compare
   * @return return true if both are null or is equal.
   */
  @SuppressWarnings("unchecked")
  public <T extends TValue> boolean isValue(T otherValue) {
    boolean result = ((otherValue == null) && (this.value == null));
    if (!result) {
      TValue other = (TValue) otherValue;
      result = DataEntry.isEq(this.value, other);
    }
    return result;
  }
  
  /**
   * Called parse a string to this property value (type TValue). If the property has and
   * assigned converter, it will call {@linkplain FieldValueConverter#toFieldValue(
   * java.lang.Object) this.converter.toFieldValue(strVal)} to handle the conversion.
   * Otherwise it calls {@linkplain #onParse(java.lang.String) this.onParse} to process
   * the conversion.
   * @param strVal the specified string value
   * @return the parsed value
   * @throws IllegalArgumentException is the value cannot be converted
   */
  protected final TValue parse(String strVal) throws IllegalArgumentException {
    TValue result = null;
    try {
      strVal = DataEntry.cleanString(strVal);
      if (strVal != null) {
        if (this.converter != null) {
          result = this.converter.toFieldValue(strVal);
        } else {
          result = this.onParse(strVal);
        }
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".parse Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called by the {@linkplain #Property(bubblewrap.core.annotations.Param) annotation 
   * constructor} to convert the parameter from a string value
   * @param strVal the specified string value
   * @return the parsed value
   * @throws IllegalArgumentException is the value cannot be converted
   */
  protected TValue onParse(String strVal) throws Exception {
    throw new NotSupportedException("The Property's onParse method is not "
            + "implemented for the generic class.");
  }
  
  /**
   * Called to validate the <tt>input</tt> is valid before it assigned as this.value.
   * if (this.validator != null) call {@linkplain InputValidator#isValidInput(
   * java.lang.Object) this.validator.isValidInput} to validate the input value. 
   * Otherwise, call {@linkplain #onValidateInput(java.io.Serializable) 
   * this.onValidateInput} to handle the validation.
   * <p>
   * All errors are trapped and assigned as this.errorMsg.
   * @param input the new input to validate
   * @return (!this.hasError())
   */
  protected final boolean isValidInput(TValue input) {
    try {
      if (this.validator != null) {
        if (!this.validator.isValidInput(input)) {
          throw new Exception(this.validator.getErrorMsg());
        }
      } else {
        this.onValidateInput(input);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.validateInput Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #isValidInput(java.io.Serializable) 
   * this.isValidInput} if this.validator is unassigned, to custom handle input 
   * validation. If input is not valid this method should throw an exception with
   * and error message.
   * @param input the new input to validate
   * @throws Exception is <tt>input</tt> is not valid.
   */
  protected void onValidateInput(TValue input) throws Exception {    
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public JSON Serialization Methods">
  /**
   * CAN OVERRIDE: Called to assign this property's value to a JSONObject that will be
   * serialized to storage devise.
   * <p>
   * The base method set output.put(this.key, this.value). It throws and exception if
   * this.key = null and skip the assignment if this.value = null.
   * @param output the output JSONObject
   */
  public void toJSON(JSONObject output) {
    if (output != null) {
      if (this.key == null) {
        throw new NullPointerException("The Property's Key is undefined");
      }
      
      if (this.value != null) {
        output.put(this.key, this.value);
      }
    }
  }

  /**
   * CAN OVERRIDE: Called to retrieve this property's value from a JSONObject that had
   * been serialized to storage devise.
   * <p>
   * The base method retrieve this.key's value as a string value form input and call
   * {@linkplain #parse(java.lang.String) this.parse} to convert the string to this
   * property's value type. This.value is set to null before the the assignment. The
   * process is skipped if this.key = null or (!input.has(this.key)).
   * @param input the input JSONObject
   */
  public void fromJSON(JSONObject input) {
    if (input != null) {
      if ((this.key != null) && (input.has(this.key))) {
        this.value = null;
        String strVal = DataEntry.cleanString(input.optString(this.key, ""));
        this.value = this.parse(strVal);
      }
    }
  }
  // </editor-fold>

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
   * OVERRIDE: Set this.dirty = null.</p>
   */
  @Override
  public void resetOnSaved() {
    this.dirty = null;
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
