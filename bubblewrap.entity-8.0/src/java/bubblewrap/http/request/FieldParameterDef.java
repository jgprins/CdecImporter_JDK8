package bubblewrap.http.request;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.entity.annotations.FieldInputMask;
import bubblewrap.io.converters.FieldValueConverter;
import bubblewrap.entity.validators.FieldValidator;
import bubblewrap.http.request.annotation.HttpParameter;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class FieldParameterDef implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(FieldParameterDef.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Field's getMethod
   */
  public final Method getMethod;
  /**
   * The Field setMod (null is readOnly)
   */
  public final Method setMethod;
  /**
   * The Field value Type
   */
  public final Class<?> fieldType;
  /**
   * The Field's ParameterName
   */
  public final String paramName;
  /**
   * (Optional) The Field Value Converter - default user {@linkplain DataConverter}
   */
  private FieldValueConverter converter;
  /**
   * (Optional) The Field Input validator assigned using a {@linkplain FieldValidator}
   * or {@linkplain FieldInputMask} annotation
   */
  private FieldValidator validator;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FieldParameterDef(Method getMethod, Method setMethod, HttpParameter annot) {
    super();  
    this.converter = null;
    this.validator = null;
    
    if (getMethod == null) {
      throw new NullPointerException("The FieldParameter's GET-method cannot be "
              + "unassigned.");
    }
    
    if (annot == null) {
      throw new NullPointerException("The FieldParameter HttpParameter annotaion is "
              + "unasssigned.");
    }
    boolean readOnly = ((annot.readOnly()) || (setMethod == null));
    this.getMethod = getMethod;
    this.fieldType = getMethod.getReturnType();
    
    setMethod = (readOnly)? null: setMethod;
    this.setMethod = setMethod;
    String name = DataEntry.cleanString(annot.name());
    if (name == null) {
      name = ReflectionInfo.getFieldname(getMethod.getName());
    }
    this.paramName = name;
    FieldConverter convertAnnot = annot.converter();
    if (convertAnnot != null) {
      this.initConverter(convertAnnot);
    }
    this.initValidator();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called by the constructor to initiate the input converter - to translate the input
   * string to a fieldValue. Set this.converter = null if <tt>converterClass</tt> = null 
   * or FieldValueConverter.Void.
   * @param converterClass the specified FieldValueConverter class
   */
  private void initConverter(FieldConverter convertAnnot) {
    Class<? extends FieldValueConverter> converterClass = null;
    if ((convertAnnot == null) || 
        ((converterClass = convertAnnot.converter()) == null) ||
        (FieldValueConverter.Void.class.equals(converterClass))) {
      return;
    } 
    
    try {
      this.converter = converterClass.newInstance();
      if (this.converter == null) {
        throw new Exception("Unable to initaite a Field Converter Type[" 
                + converterClass.getSimpleName() + "].");
      }
      
      this.converter.setOptions(convertAnnot.options());
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initConverter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called by the constructor to initiate the input converter - to translate the input
   * string to a fieldValue. Set this.converter = null if <tt>converterClass</tt> = null 
   * or FieldValueConverter.Void.
   * @param converterClass the specified FieldValueConverter class
   */
  private void initValidator() {    
    try {  
      this.validator = FieldValidator.init(this);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initConverter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether this Field is Read Only
   * @return (this.setMethod == null)
   */
  public boolean isReadOnly() {
    return (this.setMethod == null);
  }
  
  /**
   * Get the mapped Field value as string to assign to a parameter map. The field's value
   * is retrieved by calling the GET-Method's {@linkplain Method#invoke(
   * java.lang.Object, java.lang.Object...) invoke} method.
   * <p>If a FieldValueConverter is assigned, the converter's {@linkplain 
   * FieldValueConverter#toStringValue(java.lang.Object) toStringValue} method is used to 
   * convert the value to a String. Otherwise, if the field value != null, it return
   * fieldValue.toString().
   * @param instance the instance from which to retrieve the value
   * @return the value as a string or null if instance = null.
   * @throws Exception if a value-to-String conversion error occurs.
   */
  public String getFieldValue(Object instance) throws Exception {
    String result = null;
    try {
      if (instance != null) {
        Object[] args = null;
        Object fieldVal = this.getMethod.invoke(instance, args);
        if (this.converter != null) {
          result = this.converter.toStringValue(fieldVal);
        } else if (fieldVal != null) {
          result = fieldVal.toString();
        }
      }
    } catch (Exception exp) {
      throw new Exception("Get Field[" + this.paramName 
                                       + "] Value Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Called to assign the new parameter value to the object's mapped field. If a 
   * FieldConverter was defined, it will use the field converter to convert the input 
   * string to field value type. Otherwise, it will use the {@linkplain DataConverter}
   * to convert String, Boolean, and Number values. 
   * <p>If a validator is assigned, the validators {@linkplain 
   * FieldValidator#isValidParamValue(java.lang.Object) isValidParamValue} will be
   * called to validate the converted input value.
   * <p>Finally, it will use the SET-Method's {@linkplain Method#invoke(
   * java.lang.Object, java.lang.Object...) invoke} method to assign the field value.
   * @param instance the instance to be updated
   * @param newValue the new parameter value to assign
   * @throws Exception if the input value cannot be converted to the field type or if
   * a validator is assigned and the input does not comply with the validator's 
   * constraints or if the SET-method's invoke throws an exception.
   */
  public void setFieldValue(Object instance, String newValue) throws Exception {
    if (this.isReadOnly()) {
      return;
    }
    try {
      
      Object inputVal = null;
      if (this.converter != null) {
        inputVal = this.converter.toFieldValue(newValue);
      } else {
        if (String.class.equals(this.fieldType)) {
          inputVal = DataEntry.cleanString(newValue);
        } else if (Boolean.class.equals(this.fieldType)) {
          inputVal = DataConverter.toBoolean(newValue);
        } else if (Number.class.isAssignableFrom(this.fieldType)) {
          Class<? extends Number> numClass = (Class<? extends Number>) this.fieldType;
          Number numVal = DataConverter.toValue(newValue, numClass);
          inputVal = numVal;
        } else {
          throw new Exception("Unable to covert String value to Field Type["
                                               + this.fieldType.getSimpleName() + "].");
        }
      }
      
      if ((this.validator != null) && (!this.validator.isValidParamValue(inputVal))) {
        throw new Exception(this.validator.getErrorMsg());
      }
      
      Object[] args = {inputVal};
      this.setMethod.invoke(instance, args);
    } catch (Exception exp) {
      throw new Exception("Set Field[" + this.paramName 
                                       + "] Value Error:\n " + exp.getMessage());
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
    return "FieldParameterDef[ param = " + this.paramName + "]";
  }
  // </editor-fold>
}
