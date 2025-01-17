package bubblewrap.io.xml;

import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.InputValidator;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * A Wrapper for Serializing the InputValidator assigned to the {@linkplain 
 * XmlParameter}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class InputValidatorWrapper implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
          Logger.getLogger(InputValidatorWrapper.class.getSimpleName());
  //</editor-fold> 
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A String containing the full InputValidtaor Class
   */
  @XmlElement(name="validatorClass")
  private String validatorClass;
  /**
   * A String containing the Validator Options/parameters
   */
  @XmlElement(name="validatorOptions")
  private String validatorOptions;
  
  /**
   * A cached reference to the InputValidator
   */
  @XmlTransient
  private InputValidator validator;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public InputValidatorWrapper() {
    super();
    this.validatorClass = null;
    this.validatorOptions = null;
    this.validator = null;
  }
  
  /**
   * Constructor with a defined Validator Class and Options
   * @param validatorClass the validator class
   * @param validatorOptions the validation to assign to the class (optional, can be 
   * null | "")
   */
  public InputValidatorWrapper(Class<? extends InputValidator> validatorClass, 
                                                          String validatorOptions) {
    this();
    if (validatorClass == null) {
      throw new NullPointerException("The Validator Class cannot be unassigned");
    }
    this.validatorClass = validatorClass.getName();
    this.validatorOptions = DataEntry.cleanString(validatorOptions);
  }
  
  /**
   * Constructor with a defined Validator
   * @param validator the validator from which to derive the Validator Class and options
   */
  public InputValidatorWrapper(InputValidator validator) {
    this();
    if (validator == null) {
      throw new NullPointerException("The Validator cannot be unassigned");
    }
    this.validatorClass = validator.getClass().getName();
    this.validatorOptions = DataEntry.cleanString(validator.getOptions());
    this.validator = validator;
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
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Validator Class name
   * @return the assigned class name
   */
  public String getValidatorClass() {
    return validatorClass;
  }

  /**
   * Get the Validator Options
   * @return the assigned options (can be null)
   */
  public String getValidatorOptions() {
    return validatorOptions;
  }
  
  /**
   * Can an instance of the Input Validator initiated using the assigned class name
   * and validator options. Errors are trapped and logged.
   * @return the new instance or null if the process failed.
   */  
  public InputValidator getValidator() {
    if ((this.validator == null) && (this.validatorClass != null)) {
      try {
        if (this.validatorClass == null) {
          throw new Exception("The Validator Class is undefined.");
        }

        Class instClass = Class.forName(this.validatorClass);
        if (instClass == null) {
          throw new Exception("Unable to resolve Class[" + this.validatorClass + "].");
        }

        this.validator = (InputValidator) instClass.newInstance();
        if ((this.validator != null) && (this.validatorOptions != null)) {
          this.validator.setOptions(this.validatorOptions);
        } 
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getValidator Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
    return this.validator;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrids">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    String result = "InputValidator[ class = " + this.validatorClass;
    if (this.validatorOptions != null) {
      result += "; options = " + this.validatorOptions;
    }
    result += "]";
    return result;
  }
  //</editor-fold>
}
