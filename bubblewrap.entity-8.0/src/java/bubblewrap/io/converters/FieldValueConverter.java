package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.enums.BwOnKeyPress;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * The base class used to convert a Bean Value Type to a Wrapper Value Type or vise 
 * versa. Typically when the EntityWrapper value is cast or converted to a different
 * value type as field in the Entity Bean. The converters is assigned as a Field 
 * Annotation 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class FieldValueConverter<TValue> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(FieldValueConverter.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="Void Class">
  /**
   * A Void implementation of FieldValueConverter
   */
  public static class Void extends FieldValueConverter {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public Void() {
      super();
    }
    //</editor-fold>
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Always return the input value.</p>
     */
    @Override
    public Object toFieldValue(Object inputValue) {
      return inputValue;
    }

    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Always return false</p>
     */
    @Override
    public boolean isValidInput(Object value) {
      return false;
    }

    @Override
    public String toStringValue(Object value) {
      return null;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected FieldValueConverter() {
    super();
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Overridable Methods">
  /**
   * This method is called to assign a set of pre-defined Key-Value option settings.
   * See {@linkplain DataEntry#splitKeyValuePairs(java.lang.String...) 
   * DataEntry.splitKeyValuePairs} for details on the <tt>options</tt> format.
   * <p>This method converts the assigned options to a JSONObject with the assigned 
   * settings and then called {@linkplain #onSetOptions(org.json.JSONObject) 
   * this.onSetOptions} to allow for custom processing of these assigned parameters.
   * @param options and array of options as required by the inheritor
   */
  public final void setOptions(String... options) {
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

  //<editor-fold defaultstate="collapsed" desc="Public Overriddable Methods">
  /**
   * CAN OVERRIDE: The javaScript to assign to a Text Input control's 
   * onkeypress event to prevent users from entering invalid characters in 
   * the field. Inheritors can us the {@linkplain BwOnKeyPress} enums to get
   * the standard in bw.core.js scripts for handling the event.
   * <p>
   * The base method returns null.
   * @return the input control's onkeypress script.
   */
  public String onKeypress() {
    return null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * <p>ABSTRACT: Called to convert the specified inputValue to a Field Value of type 
   * TOut.
   * @param inputValue the input value to convert - can be null or any valid input
   * @return the converted field value of type TValue
   */
  public abstract TValue toFieldValue(Object value);
  
  /**
   * <p>ABSTRACT: Called to convert the specified input <tt>value</tt> to the Field Value 
   * string format.
   * @param inputValue the input value to convert - can be null.
   * @return the string format for value
   */
  public abstract String toStringValue(Object value);
  
  /**
   * Called to check is the specified value is a valid input that can be converted by 
   * the 
   * @param value the value to assigned
   * @return true if value is a valid input
   */
  public abstract boolean isValidInput(Object value);
  //</editor-fold>
}
