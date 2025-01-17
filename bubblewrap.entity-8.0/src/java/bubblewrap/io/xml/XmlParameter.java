package bubblewrap.io.xml;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import java.io.Serializable;
import java.util.Objects;
import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.InputValidator;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * An base XMLParameter class - use to manage and serialize a Key-Value pair with the 
 * value a XML serializable type (e.g., string, boolean, integer, etc.).  The Parameter
 * has an optional {@linkplain InputValidator} whose class and settings are serialized 
 * with the parameter value. This call is used  with the {@linkplain XmlParameterMap}.
 * @author kprins
 */
public class XmlParameter implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Parameter's Key
   */
  @XmlAttribute(name="key")
  private String key;
  /**
   * Placeholder for the Parameter's Key
   */
  @XmlTransient
  private Serializable value;
  /**
   * Placeholder for the Parameter's input validator
   */
  //@XmlAttribute(name="validator")
  @XmlElement(name="validator")
  private InputValidatorWrapper validator;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * EventHandler for sending a Value Changed event.
   */
  @XmlTransient
  public final EventHandler ValueChanged;
  /**
   * Method called to fie the Value Changed event.
   */
  protected void fireValueChanged() {
    this.ValueChanged.fireEvent(this, new EventArgs());
  }
//</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public XmlParameter() {
    super();    
    this.ValueChanged = new EventHandler();
    this.key = null;
    this.value = null;
    this.validator = null;
  }
  
  /**
   * Public Constructor with a defined Parameter Key
   */
  public XmlParameter(String paramKey) {
    this();    
    this.setKey(paramKey);
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call super method before calling this.ValueChanged.clear()</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.ValueChanged.clear();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Parameter's Key
   * @return the assigned value.
   * @exception NullPointerException if the key is not set.
   */
  public final String getKey() {
    if (this.key == null) {
      throw new NullPointerException("The Parameter's Key is not set");
    }
    return this.key;
  }
  
  /**
   * Set the Parameter's Key (cannot be null)
   */
  protected final void setKey(String key) {
    key = DataEntry.cleanString(key);
    if (key == null) {
      throw new NullPointerException("The Parameter Key cannot be undefined.");
    }
    this.key = key.toUpperCase();
  }
  
  /**
   * Get whether the Parameter support a validator
   * @return true if a validator is assigned.
   */
  protected boolean hasValidator() {
    return (this.validator != null);
  }
  
  /**
   * Get the Parameter's Input Validator
   * @return return the assigned validator or null is not assigned.
   */
  @XmlTransient
  public InputValidator getValidator() {
    return (this.validator == null)? null: this.validator.getValidator();
  }
  
  /**
   * Set the Parameter's Input Validator
   * @param validator 
   */
  public void setValidator(InputValidator validator) {
    if (validator == null) {
      this.validator = null;
    } else {
      this.validator = new InputValidatorWrapper(validator);
    }
  }
    
  /**
   * <p>Called by the serializer to convert the parameter value to a string value that
   * will be saved in the XML file.</p>
   * <p>If hasValidator, call the validator's ToString(value) method. Otherwise
   * return this.value.toString (or null if the value is unassigned).
   * @return the parameter value as a string (can be null)
   */
  @XmlElement(name="value")
  @SuppressWarnings("unchecked")
  public String getAsString() {
    String result = null;
    if (this.value != null) {
      InputValidator processor = null;
      if ((this.hasValidator()) && ((processor = this.getValidator()) != null)) {
        result = processor.toString(this.value);
      } else {
        result = this.value.toString();
      }
    }
    return result;
  }
  
  /**
   * Call by the XML deserializer to convert the specified string value to the parameter
   * value.
   * @param strVal the previously saved value
   */
  public void setAsString(String strVal) {
    strVal = DataEntry.cleanString(strVal);
    InputValidator processor = null;
    if (strVal == null) {
      this.value = null;
    } else if ((this.hasValidator()) && ((processor = this.getValidator()) != null)) {
      this.value = processor.toValue(strVal);
    } else {
      this.value = strVal;
    }
  }
          
  /**
   * Called to the currently value
   * @return return the currently assigned value (can be null)
   */
  @XmlTransient
  public Serializable getValue() {
    return this.value;
  }
  
  /**
   * Called by the ParameterMap to set the value. This is called only if IsValid(value) 
   * = true and IsNew(value) = true. If a validator is assigned, called the validator's
   * castAsValue to cast the specified value to the Validator's valueClass, otherwise
   * store the value unconverted.
   * @param value the new value.
   */
  public void setValue(Serializable value) {
    Serializable newValue = null;
    if (value != null) {
      InputValidator processor = null;
      if ((this.hasValidator()) && ((processor = this.getValidator()) != null)) {
        newValue = processor.castAsValue(value);
      } else {
        newValue = value;
      }
    }
    if (!DataEntry.isEq(newValue, this.value)) {
      this.value = newValue;
      this.fireValueChanged();
    }
  }
  
  /**
   * Call to check if the proposed value is a valid value for this parameter. Returning
   * false will trigger an exception with this.valdiationError as the message.
   * Return true if no validator is assigned
   * @param value the proposed value 
   * @return true if proposed value is valid.
   */
  public boolean isValid(Serializable value) {
    boolean result = true;
    InputValidator processor = null;
    if ((this.hasValidator()) && ((processor = this.getValidator()) != null)) {
      result = processor.isValidInput(value);
    }
    return result;
  }
  
  /**
   * Get the Validator's Error Message after isValid return false.
   * @return the validator's current error message or null if no validator is assigned.
   */
  public String getValidationError() {
    String result = null;
    InputValidator processor = null;
    if ((this.hasValidator()) && ((processor = this.getValidator()) != null)) {
      result = processor.getErrorMsg();
    }
    return result;
  }
  
  /**
   * Call to reset the parameter value. The base method rest the value to null.
   */
  public void resetValue() {
    this.value = null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "Parameter[" + this.paramKey + "]"</p>
   */
  @Override
  public String toString() {
    return "Parameter[" + this.key + "]";
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return true if the specified object is an instance of
   * XmlParameter and this.Key matches the object Key.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof XmlParameter));
    if (result) {
      XmlParameter trgParam = (XmlParameter) obj;
      result = (DataEntry.isEq(this.key, trgParam.key, true));
    }
    return result;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return hash Code for the Parameter.Key</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 47 * hash + Objects.hashCode(this.key);
    return hash;
  }
  //</editor-fold>
}
