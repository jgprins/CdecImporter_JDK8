package bubblewrap.io.validators;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlTransient;
import org.json.JSONObject;

/**
 * Abstract InputValidator class with a generic parameter V representing the class
 * of the input value.  This class and its inheritors are used to convert String
 * field values to value of Class<V> or convert V values to String for storing in the
 * database.  Typically used by Parameter or property Classes.
 * @author kprins
 */
public abstract class InputValidator<TValue extends Serializable> {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Private Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(InputValidator.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Static Methods/Fields">
  /**
   * Validate the the <tt>className</tt> represents a InputValidator with a
   * parameterless constructor.
   * @param className the class name to validate
   * @return boolean
   */
  @SuppressWarnings("unchecked")
  public static boolean isValidClass(String className)
          throws Exception {
    boolean result = false;
    className = ((className == null) || (className.trim().equals("")))
            ? null : className.trim();
    if (className == null) {
      throw new Exception("InputValidator Class is undefined");
    }

    Class validatorClass = Class.forName(className);
    if (validatorClass == null) {
      throw new Exception("Unable to locate InputValidator Class["
              + className + "]");
    }

    if (!InputValidator.class.isAssignableFrom(validatorClass)) {
      throw new Exception("Class[" + className
              + "] does not inherit from InputValidator");
    }

    // Check the parameterless constructor
    Class[] pTypes = null;
    try {
      Constructor constuctor = validatorClass.getConstructor(pTypes);
      if (constuctor == null) {
        throw new Exception();
      }
    } catch (Exception pExp) {
      throw new Exception("Class[" + className
              + "] does not support a parameterless constructor");
    }
    result = true;
    return result;
  }

  /**
   * Initiate a new instance of the InputValidator.  sClassName is the class
   * name of the InputValidator.
   * @param className String
   * @return InputValidator
   */
  @SuppressWarnings("unchecked")
  public static InputValidator initValidator(String className) {
    InputValidator result = null;
    try {
      className = ((className == null) || (className.trim().equals("")))
              ? null : className.trim();
      if (className == null) {
        throw new Exception("InputValidator Class is undefined");
      }

      Class validatorClass = Class.forName(className);
      if (validatorClass == null) {
        throw new Exception("Unable to locate InputValidator Class["
                + className + "]");
      }
      result = InputValidator.initValidator(validatorClass);
    } catch (Exception pEx) {
      logger.log(Level.SEVERE, "InputValidator.initValidator Error:\n {0}", 
              pEx.getMessage());
    }
    return result;
  }

  /**
   * Initiate a new instance of InputValidator. pClass is the
   * InputValidator class.
   * @return InputValidator
   */
  public static InputValidator initValidator(Class<? extends InputValidator> 
                                                                   validatorClass) {
    InputValidator result = null;
    try {
      if (validatorClass == null) {
        throw new Exception("InputValidator Class is undefined");
      }

      // Get the Class's Parameterless constructor
      result = (InputValidator) validatorClass.newInstance();
    } catch (Exception pEx) {
      logger.log(Level.SEVERE, "InputValidator.initValidator Error:\n {0}", 
              pEx.getMessage());
      result = null;
    }
    return result;
  } 
  
  /**
   * Check if the specified <tt>validatorClass</tt> is to validator values of type
   * <tt>valueClass</tt>.
   * @param validatorClass the validator class to evaluate
   * @param the value class to test for.
   * @return true is the validator's Generic TVal class matches valeuClass
   */
  public static boolean isForValueClass(Class<? extends InputValidator> validatorClass,
          Class valueClass) {
    boolean result = false;
    try {
      if (validatorClass == null) {
        throw new Exception("InputValidator Class is undefined");
      }
      if (valueClass == null) {
        throw new Exception("InputValidator's Value Class is undefined");
      }
      
      Class genericClass =
                ReflectionInfo.getGenericClass(InputValidator.class, validatorClass, 0);

      // Get the Class's Parameterless constructor
      result = ((genericClass != null) && (genericClass.equals(valueClass)));
    } catch (Exception pEx) {
      logger.log(Level.SEVERE, "InputValidator.isForValueClass Error:\n {0}", 
              pEx.getMessage());
      result = false;
    }
    return result;
  }
  // </editor-fold>
      
  //<editor-fold defaultstate="collapsed" desc="A Void EntityWrapper Class">
  /**
   * A Void class - not constructible
   */
  public class Void extends InputValidator<String> {
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public Void() {
      super();
      throw new IllegalArgumentException("A Void class is not constructable.");
    }
    //</editor-fold>

    @Override
    public boolean onIsValidInput(String pInput) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toValue(String sParValue) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Message">
  /**
   * Internal placeholder for a Error Message during Validation.
   */
  @XmlTransient
  private String errorMsg = null;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Protected Constructor">
  /**
   * Protected Constructor
   */
  protected InputValidator() {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public/Protected/Abstract methods">
  /**
   * FINAL: Get the Validator's Value Class
   * @return Class<V>
   */
  @SuppressWarnings("unchecked")
  public final Class<TValue> getValueClass() {
    Class<? extends InputValidator> thisClass = this.getClass();
    return (Class<TValue>) 
                    ReflectionInfo.getGenericClass(InputValidator.class, thisClass, 0);
  }

  /**
   * Check whether <tt>checkClass</tt> matches this.valueClass. Return false is either 
   * checkClass or this.valueClass is undefined.
   * @param checkClass The class to validate
   * @return boolean
   */
  public final boolean isValueClass(Class checkClass) {
    Class<TValue> valueClass = this.getValueClass();
    return ((checkClass != null) && (valueClass != null) && 
                                                      (valueClass.equals(checkClass)));
  }

  /**
   * Clear the Validation Error Message
   * @return String
   */
  public void clearErrorMsg() {
    this.errorMsg = null;
  }

  /**
   * Set the Validation Error Message (not appended)
   * @return String
   */
  public void setErrorMsg(String errMsg) {
    this.errorMsg = DataEntry.cleanString(errMsg);
  }

  /**
   * Get the Assigned Validation Error Message
   * @return String
   */
  public String getErrorMsg() {
    return this.errorMsg;
  }
  
  public boolean hasError() {
    return (this.errorMsg != null);
  }
  
  /**
   * Check if a null input value is allowed.
   * @return true if allowed.
   */
  public boolean doAllowNull() {
    return true;
  }
  
  /**
   * Called to convert the specified input to a value of type TValue. If input!=null,
   * check if the input is a String and attempt to convert it to TValue by calling the
   * this.toValue. Otherwise attempt to convert the input to this.valueClass and
   * assign a conversion error message if the casting failed.
   * @param input the input value to convert
   * @return null if input is null or the casting is invalid. Otherwise, return the
   * casted value.
   */
  public TValue castAsValue(Object input) {
    TValue result = null;
    this.clearErrorMsg();
    if (input != null) {
      Class<TValue> valClass = this.getValueClass();
       try {
        if ((!valClass.equals(String.class)) 
                && (input instanceof String)) {
          result = this.toValue((String) input);
          if (result == null) {
            throw new Exception();
          }
        } else {
          result = valClass.cast(input);
        }
      } catch (Exception exp) {
        result = null;
        this.setErrorMsg("Unable to cast input[" + input.toString() 
                + "] as a value of Type[" + valClass.getSimpleName() + "].");
      }
    }
    return result;
  }
          
  /**
   * Called to validate that the specified input value is a valid of type TValue.  
   * @param input of type Serializable
   * @return boolean
   */
  public boolean isValidInput(Object input) {
    this.clearErrorMsg();
    if (input == null) {
      if (!this.doAllowNull()) {
        this.setErrorMsg("Null Value is not allowed.");
      }
    } else {
      TValue inValue = this.castAsValue(input);
      if (!this.hasError()) {
        try {
          if (!this.onIsValidInput(inValue)) {
            if (!this.hasError()) {
              throw new Exception();
            }
          }
        } catch (Exception pExp) {
          this.setErrorMsg("Input value[" + inValue.toString() + "] is not allowed.");
        }
      }
    }
    return (!this.hasError());
  }
  
  /**
   * Called by isValidInput after the value was successfully casts to Type TValue to
   * validate the number comply to custom criteria. The base method returns true.
   * <p><b>NOTE:</b> Assign a custom error message to notify the user why the input
   * value is invalid.</p>
   * @param input not null input of type TValue to validate
   * @return true if valid, false (with an custom error message) if invalid.
   */
  protected boolean onIsValidInput(TValue input) {
    return true;
  }

  /**
   * ABSTRACT: Convert the string Value to a input Value of type TValue
   * @param strVal input string
   * @return the converted value
   */
  public abstract TValue toValue(String strVal);

  /**
   * Return the string value if of an input of type TValue
   * @param input input value
   * @return return input.ToString() or null if input=null.
   */
  public String toString(TValue input) {
    return DataEntry.cleanString((input == null) ? null : input.toString());
  }

  /**
   * Return the value of the selection if hasSelectOptions, otherwise return null.  Calls
   * onGetSelectedValue to convert sSelection to a value. Return null if no match is
   * found or an error occurred.Errors are logged.
   * @param selectId the ID of selected option
   * @return the matching value of type TValue
   */
  public final TValue getSelectedValue(String selectId) {
    TValue result = null;
    if (this.hasSelectOptions()) {
      try {
        result = this.onGetSelectedValue(selectId);
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getSelectedValue Error:\n {1}", 
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
        result = null;
      }
    }
    return result;
  }

  /**
   * Return the Selection List if hasSelectOptions. Return null otherwise. Return an empty
   * list if an error is thrown. Errors are logged.
   * @return List<SelectItem>
   */
  public final List<SelectItem> getSelectOptions() {
    List<SelectItem> result = null;
    if (this.hasSelectOptions()) {
      try {
        result = this.onGetSelectOptions();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getSelectOptions Error:\n {1}", 
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
    if (result == null) {
      result = new ArrayList<>();
    }
    return result;
  }

  /**
   * TO BE OVERRIDE to return true if this Parameter has defined options.
   * The base method returns false.
   * @return boolean
   */
  public boolean hasSelectOptions() {
    return false;
  }

  /**
   * TO BE OVERRIDE if this Parameter has defined options. Base method throws
   * UnsupportedOperationException exception
   * @return List<SelectItem>
   * @throws Exception
   */
  protected List<SelectItem> onGetSelectOptions() throws Exception {
    throw new UnsupportedOperationException("This method is not supported for " + 
            this.getClass().getSimpleName());
  }

  /**
   * TO BE OVERRIDE if this Parameter has defined options. Must return a valid value
   * for type TVale based on the selected value.
   * <p>The base method return this.toValue(selectId)
   * @param selectId the ID of selected option
   * @return the selected value or null if no match has been found
   * @throws Exception
   */
  protected TValue onGetSelectedValue(String selectId) throws Exception {
    return this.toValue(selectId);
  }

  /**
   * This method is called to assign a set of pre-defined Key-Value option settings.
   * See {@linkplain DataEntry#splitKeyValuePairs(java.lang.String...) 
   * DataEntry.splitKeyValuePairs} for details on the <tt>options</tt> format.
   * <p>This method converts the assigned options to a JSONObject with the assigned 
   * settings and then called {@linkplain #onSetOptions(org.json.JSONObject) 
   * this.onSetOptions} to allow for custom processing of these assigned parameters.
   * @param options and array of options as required by the inheritor
   */
  public final void setOptions(String...options) {
    try {
      JSONObject optionObj = null;
      if ((options != null) && (options.length > 0) &&
              ((optionObj = DataEntry.splitKeyValuePairs(options)) != null) &&
              (optionObj.length() > 0)) {
        this.onSetOptions(optionObj);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setOptions Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * CAN OVERRIDE: This method can be overridden by inheritors to allow the assignment
   * of some value conversion parameters (e.g. the format of the numeric converter).
   * <b>The base method does nothing.</b>
   * <p>
   * <b>NOTE:</b> the <tt>options</tt> can be single values, a JSONArray of values
   * (e.g., a discreet value list) or a JSONObject with a set of sub Key-Value pairs</p>
   * @param options a JSONObject containing the parsed input options.
   */
  protected void onSetOptions(JSONObject options) {}

  /**
   * Implemented by OptionsValidator to return the Options as a delimited string of
   * values based on the key-value pair format as defined by {@linkplain 
   * DataEntry#splitKeyValuePairs(java.lang.String...) DataEntry.splitKeyValuePairs} 
   * <p><b>The base method returns null</b>.
   * @return the formatted string of key-value pairs.
   */
  public String getOptions() {
    return null;
  }
  // </editor-fold>
}