package bubblewrap.entity.validators;

import bubblewrap.entity.annotations.FieldCaption;
import bubblewrap.entity.annotations.FieldInputMask;
import bubblewrap.entity.annotations.FieldValidation;
import bubblewrap.entity.annotations.IsUnique;
import bubblewrap.entity.annotations.StringField;
import bubblewrap.entity.context.FieldInfo;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.http.request.FieldParameterDef;
import bubblewrap.io.DataEntry;
import bubblewrap.io.IntegerRange;
import bubblewrap.io.enums.InputMasks;
import bubblewrap.io.validators.InputValidator;
import bubblewrap.io.validators.StringValidator;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;

/**
 * <p>A field validator class used by the EntityWrapper to validate user input. The
 * validator's settings are initiated from the following entity beans field
 * annotations:</p><ul>
 *  <li><b>@{@linkplain FieldCaption}:</b> - to assign a user friendlier Field Caption 
 *  to be used in validation messages.</li>
 *  <li><b>@{@linkplain NotNull}:</b> - if set the field value cannot be null. This
 *  annotation should be customized by assigning a validation message to it (e.g.,
 *  set @NotNull(message="Field MyField cannot be empty or unassigned.")</li>
 * <li><b>@{@linkplain StringField}:</b> - (applicable to String fields only) defining 
 *  the length of the allowable text string. This annotation should be customized by 
 *  assigning a validation message to it (e.g., @StringField(min=1,max=60, 
 *  message="Field MyField's length is limited to between 1..60 characters.")</li>
 * <li><b>@{@linkplain IsUnique}:</b> - assigned to identify the assign value must be 
 *  unique in the recordset of in all records associated with a foreignKey parent. The
 *  latter applies if setting @IsUnique(inParentOnly=true)</li>
 * <li><b>@{@linkplain FieldInputMask}:</b> - assigned a special {@linkplain InputMasks
 *  InputMask}to validate the user input (e.g., a Email mask).</li>
 * <li><b>@{@linkplain Column}:</b> - this annotation has the following properties
 *  that are used if the NotNull, Size, or IsUnique constraints are not set. These
 *  include:<ul>
 *     <li>nullable = false</li> - equivalent to the NotNull annotation
 *     <li>length &gt; 0</li> - equivalent to the Size annotation (applicable to string 
 *        fields only)
 *     <li>unique = true</li> - equivalent to the IsUnique annotation (without the 
 *        inParentOnly option).
 *  </ul></li>
 * </ul>
 * @author kprins
 */
public class FieldValidator implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Private Class">
  /**
   * Private class for capturing the IsUnique Options
   */
  protected static class IsUniqueOptions {
    private int filterOptions = 0;
    private Class<? extends EntityWrapper> parentClass = null;
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public IsUniqueOptions() {
      
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                    Logger.getLogger(FieldValidator.class.getName());  
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * A static method to initiate the Field's Validator based on the specified
   * fieldInfo's Validation Annotations.
   * @param fieldInfo the owner fieldInfo
   * @return the new Field Validation instance.
   * @throws Exception if fiedlInfo=null of initiating the validator failed.
   */
  @SuppressWarnings("unchecked")
  public static FieldValidator init(FieldInfo fieldInfo) throws Exception {
    FieldValidator result = null;
    if ((fieldInfo == null) || (fieldInfo.entityPath == null)) {
      throw new NullPointerException("The FieldInfo reference or its EntityPath "
              + "is undefined.");
    }
    
    String fieldName = fieldInfo.entityPath.fieldName;
    try {      
      Class fieldType = fieldInfo.returnType;
      
      FieldCaption captionAnnot = fieldInfo.getAnnotation(FieldCaption.class);
      String fieldCaption
              = (captionAnnot == null) ? null : DataEntry.cleanString(captionAnnot.caption());
      
      FieldValidator validator = new FieldValidator(fieldName, fieldCaption, fieldType);      
      FieldConstaintSet constSet = validator.getConstraints();      
      FieldConstaintSet.Constraint constraint = null;      
      
      NotNull nullAnnot = fieldInfo.getAnnotation(NotNull.class);
      Basic basicAnnot = fieldInfo.getAnnotation(Basic.class);
      if ((nullAnnot != null) || ((basicAnnot != null) && (!basicAnnot.optional()))) {
        String msg = (nullAnnot == null) ? null : nullAnnot.message();
        if (msg == null) {
          msg = "This is a required field and can not be unassigned.";
        }
        constSet.add(FieldContraintTypes.NotNull, msg);        
      }      
      
      StringField sizeAnnot = fieldInfo.getAnnotation(StringField.class);
      if (sizeAnnot != null) {
        Integer min = sizeAnnot.min();
        Integer max = sizeAnnot.max();
        min = Math.min(min, max);
        if (max > 0) {
          IntegerRange sizeRng = new IntegerRange(min, max);
//          validator.setSizeRange(sizeRng);
          constraint = constSet.add(FieldContraintTypes.SizeRange, sizeAnnot.message());
          constraint.setOptions(sizeRng);
        }
      }      
      
      IsUnique uniqueAnnot = fieldInfo.getAnnotation(IsUnique.class);
      if (uniqueAnnot != null) {
        IsUniqueOptions options = new IsUniqueOptions();        
        if (uniqueAnnot.inParentOnly()) {
          Class parentClass = uniqueAnnot.parentClass();
          if ((parentClass.equals(EntityWrapper.Void.class))
                  || (!(EntityWrapper.class.isAssignableFrom(parentClass)))) {
            throw new Exception("The IsUnqiueInParent Constraint's Parent Class["
                    + parentClass.getSimpleName()
                    + "] is undefined or not a EntityWrapper class.");
          }
          constraint = constSet.add(FieldContraintTypes.UniqueInParent, "The "
                  + validator.getCaption() + " field's value must be unqiue in its "
                  + "parent's child recordset.");          
          options.filterOptions = uniqueAnnot.filterOptions();
          options.parentClass = (Class<? extends EntityWrapper>) parentClass;
        } else {
          constraint = constSet.add(FieldContraintTypes.Unique,
                  "The " + validator.getCaption()
                  + " field's value must be unqiue in the recordset.");          
          options.filterOptions = uniqueAnnot.filterOptions();
        }
        constraint.setOptions(options);
      }      
      
      FieldInputMask maskAnnot = fieldInfo.getAnnotation(FieldInputMask.class);
      if (maskAnnot != null) {
        constraint = constSet.add(FieldContraintTypes.InputMask, validator.getCaption()
                + " field's entered value is invalid.");
        constraint.setOptions(maskAnnot.mask());
      }
      
      FieldValidation validAnnot = fieldInfo.getAnnotation(FieldValidation.class);
      if (validAnnot != null) {
        Class validClass = validAnnot.validator();
        if ((validClass == null) || (InputValidator.Void.class.equals(validClass))
                || (!InputValidator.class.isAssignableFrom(validClass))) {
          throw new Exception("The FieldValidation annotation's Validator Class["
                  + ((validClass == null) ? "null" : validClass.getSimpleName())
                  + " ]is unassigend, Void, or not an InputValidator class.");
        }
        
        InputValidator fieldValidator = null;
        try {
          fieldValidator = (InputValidator) validClass.newInstance();
        } catch (Exception inExp) {
          inExp = null;
          fieldValidator = null;
        }
        
        if (fieldValidator == null) {
          throw new Exception("Initiating an instanne of FieldValidator["
                  + validClass.getSimpleName() + "] failed.");
        } else if (!fieldValidator.isValueClass(fieldType)) {
          throw new Exception("FieldValidator[" + validClass.getSimpleName()
                  + "] does not match Field Return Type["
                  + fieldType.getSimpleName() + "].");
        }
        String[] options = validAnnot.options();
        if ((options != null) && (options.length > 0)) {
          fieldValidator.setOptions(options);
        }
        
        constraint = constSet.add(FieldContraintTypes.FieldValidation,
                validator.getCaption() + " field's entered value is invalid.");
        constraint.setOptions(fieldValidator);
      }
      
      Column colAnnot = fieldInfo.getAnnotation(Column.class);
      if (colAnnot != null) {
        if ((!colAnnot.nullable())
                && (constSet.contains(FieldContraintTypes.NotNull))) {
          constSet.add(FieldContraintTypes.NotNull, "The " + validator.getCaption()
                  + " field's value cannot be empty/unassigned.");
        }
        
        if ((fieldType.equals(String.class)) &&
                (!constSet.contains(FieldContraintTypes.SizeRange)) &&
                (!fieldInfo.hasAnnotation(Lob.class))) {
          if (colAnnot.length() > 0) {
            Integer min = (constSet.contains(FieldContraintTypes.NotNull)) ? 1 : 0;
            Integer max = colAnnot.length();
            IntegerRange sizeRng = new IntegerRange(min, max);
            constraint = constSet.add(FieldContraintTypes.SizeRange, "The "
                    + validator.getCaption()
                    + " field's length cannot exceed " + max + " characters.");
            constraint.setOptions(sizeRng);
          }
        }        
        
        if ((colAnnot.unique()) && ((!constSet.contains(FieldContraintTypes.Unique))
                || (!constSet.contains(FieldContraintTypes.UniqueInParent)))) {
          constSet.add(FieldContraintTypes.Unique, "The " + validator.getCaption()
                  + " field's value must be unqiue in the recordset.");
        }
      }      
      result = validator;
    } catch (Exception pExp) {
      throw new Exception("FieldValidator.init[Field=" + fieldName + "] Error:\n "
              + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * A static method to initiate the Field's Validator based on the specified
   * fieldInfo's Validation Annotations.
   * @param fieldDef the owner fieldInfo
   * @return the new Field Validation instance.
   * @throws Exception if fiedlInfo=null of initiating the validator failed.
   */
  @SuppressWarnings("unchecked")
  public static FieldValidator init(FieldParameterDef fieldDef) throws Exception {
    FieldValidator result = null;
    if ((fieldDef == null) || (fieldDef.getMethod == null)) {
      throw new NullPointerException("The FieldParameter Definition reference or its "
              + "GET-method is undefined.");
    }
    
    String fieldName = fieldDef.paramName;
    try {      
      Class fieldType = fieldDef.getMethod.getReturnType();
      
      FieldCaption captionAnnot = fieldDef.getMethod.getAnnotation(FieldCaption.class);
      String fieldCaption
            = (captionAnnot == null) ? null : DataEntry.cleanString(captionAnnot.caption());
      
      FieldValidator validator = new FieldValidator(fieldName, fieldCaption, fieldType);      
      FieldConstaintSet constSet = validator.getConstraints();      
      FieldConstaintSet.Constraint constraint = null;      
      
      NotNull nullAnnot = fieldDef.getMethod.getAnnotation(NotNull.class);
      Basic basicAnnot = fieldDef.getMethod.getAnnotation(Basic.class);
      if ((nullAnnot != null) || ((basicAnnot != null) && (!basicAnnot.optional()))) {
        String msg = (nullAnnot == null) ? null : nullAnnot.message();
        if (msg == null) {
          msg = "This is a required field and can not ne unassigned.";
        }
        constSet.add(FieldContraintTypes.NotNull, msg);        
      }      
      
      StringField sizeAnnot = fieldDef.getMethod.getAnnotation(StringField.class);
      if (sizeAnnot != null) {
        Integer min = sizeAnnot.min();
        Integer max = sizeAnnot.max();
        min = Math.min(min, max);
        if (max > 0) {
          IntegerRange sizeRng = new IntegerRange(min, max);
//          validator.setSizeRange(sizeRng);
          constraint = constSet.add(FieldContraintTypes.SizeRange, sizeAnnot.message());
          constraint.setOptions(sizeRng);
        }
      }     
      
      FieldInputMask maskAnnot = fieldDef.getMethod.getAnnotation(FieldInputMask.class);
      if (maskAnnot != null) {
        constraint = constSet.add(FieldContraintTypes.InputMask, validator.getCaption()
                + " field's entered value is invalid.");
        constraint.setOptions(maskAnnot.mask());
      }
      
      FieldValidation validAnnot = 
                                fieldDef.getMethod.getAnnotation(FieldValidation.class);
      if (validAnnot != null) {
        Class validClass = validAnnot.validator();
        if ((validClass == null) || (InputValidator.Void.class.equals(validClass))
                || (!InputValidator.class.isAssignableFrom(validClass))) {
          throw new Exception("The FieldValidation annotation's Validator Class["
                  + ((validClass == null) ? "null" : validClass.getSimpleName())
                  + " ]is unassigend, Void, or not an InputValidator class.");
        }
        
        InputValidator fieldValidator = null;
        try {
          fieldValidator = (InputValidator) validClass.newInstance();
        } catch (Exception inExp) {
          inExp = null;
          fieldValidator = null;
        }
        
        if (fieldValidator == null) {
          throw new Exception("Initiating an instanne of FieldValidator["
                  + validClass.getSimpleName() + "] failed.");
        } else if (!fieldValidator.isValueClass(fieldType)) {
          throw new Exception("FieldValidator[" + validClass.getSimpleName()
                  + "] does not match Field Return Type["
                  + fieldType.getSimpleName() + "].");
        }
        String[] options = validAnnot.options();
        if ((options != null) && (options.length > 0)) {
          fieldValidator.setOptions(options);
        }
        
        constraint = constSet.add(FieldContraintTypes.FieldValidation,
                validator.getCaption() + " field's entered value is invalid.");
        constraint.setOptions(fieldValidator);
      }
      
      Column colAnnot = fieldDef.getMethod.getAnnotation(Column.class);
      if (colAnnot != null) {
        if ((!colAnnot.nullable())
                && (constSet.contains(FieldContraintTypes.NotNull))) {
          constSet.add(FieldContraintTypes.NotNull, "The " + validator.getCaption()
                  + " field's value cannot be empty/unassigned.");
        }
        
        if ((fieldType.equals(String.class))
                && (!constSet.contains(FieldContraintTypes.SizeRange))) {
          if (colAnnot.length() > 0) {
            Integer min = (constSet.contains(FieldContraintTypes.NotNull)) ? 1 : 0;
            Integer max = colAnnot.length();
            IntegerRange sizeRng = new IntegerRange(min, max);
            constraint = constSet.add(FieldContraintTypes.SizeRange, "The "
                    + validator.getCaption()
                    + " field's length cannot exceed " + max + " characters.");
            constraint.setOptions(sizeRng);
          }
        }        
        
        if ((colAnnot.unique()) && ((!constSet.contains(FieldContraintTypes.Unique))
                || (!constSet.contains(FieldContraintTypes.UniqueInParent)))) {
          constSet.add(FieldContraintTypes.Unique, "The " + validator.getCaption()
                  + " field's value must be unqiue in the recordset.");
        }
      }      
      result = validator;
    } catch (Exception pExp) {
      throw new Exception("FieldValidator.init[FieldParameter=" + fieldName 
                          + "] Error:\n " + pExp.getMessage());
    }
    return result;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Owner Field Name
   */
  private String fieldName;
  /**
   * Owner Field's user friendly caption (default=fieldName). Use the {@linkplain 
   * FieldCaption} annotation to assign the field's caption
   */
  private String caption;  
  /**
   * Field Return Type as retrieved from the Field Info
   */
  private Class returnType;
  /**
   * Set of field constraints as retrieve from assigned annotations
   */
  private FieldConstaintSet constraints;
  /**
   * The validation error Message
   */
  private String errorMsg;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected FieldValidator(String fieldName, String caption, Class returnType) {
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new NullPointerException("The Validator's Field Name cannot be unassigned");
    }
    if (returnType == null) {
      throw new NullPointerException("The Validator's Field Return Type cannot be "
              + "unassigned.");
    }
    caption = DataEntry.cleanString(caption);
    this.fieldName = fieldName;
    this.caption = (caption == null)? fieldName: caption;
    this.returnType = returnType;
    this.constraints = null;
    this.errorMsg = null;
  }
  
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the validator's Field's name
   * @return the assigned/supported field's name
   */
  public String getFieldName() {
    return this.fieldName;
  }
  
  /**
   * Get the validator's Field's caption
   * @return the assigned/supported field's caption
   */
  public String getCaption() {
    return this.caption;
  }
  
  /**
   * Get the validator's Field's return type
   * @return the class of the return type.
   */
  public Class getReturnType() {
    return this.returnType;
  }
  
  /**
   * GCall to get the FieldValidator's {@linkplain FieldConstaintSet} to update.
   * (Default is an empty set)
   * @return the internally initiated value 
   */
  public FieldConstaintSet getConstraints() {
    if (this.constraints == null) {
      this.constraints = new FieldConstaintSet();
    }
    return this.constraints;
  }
  
  /**
   * Get whether the specified constraint apply to the field.
   * @param type the {@linkplain FieldContraintTypes FieldContraintType} to test for
   * @return true if the FieldContraintType apply
   */
  public boolean hasConstrait(FieldContraintTypes type) {
    return ((this.constraints != null) && (this.constraints.contains(type)));
  }
  
  /**
   * Check if the Validator has an assigned validation error message
   * @return true is the message is assigned
   */
  public boolean hasError() {
    return (this.errorMsg != null);
  }
  
  /**
   * Get the current error message
   * @return the assigned value (Can be null)
   */
  public String getErrorMsg() {
    return this.errorMsg;
  }
  
  /**
   * Set/Append the new message
   * @param errMsg the new error message - skipped if null|"".
   */
  protected void setErrorMsg(String errMsg) {
    errMsg = DataEntry.cleanString(errMsg);
    if (errMsg != null) {
      if (this.errorMsg == null) {
        this.errorMsg = errMsg;
      } else {
        this.errorMsg = "\n" + errMsg;
      }
    }
  }
  
  /**
   * Called to clear the error message
   */
  public void clearErrorMsg() {
    this.errorMsg = null;
  }
      
  /**
   * Get the Validator's  InputValidator - if assigned as an InputMask or 
   * FieldValidation constraint.
   * @return the validator or null if unassigned.
   */
  public InputValidator getInputValidator() {
    InputValidator result = null;
    if (this.hasConstrait(FieldContraintTypes.InputMask)) {
      InputMasks inputMask = 
                    this.constraints.getOptions(FieldContraintTypes.InputMask);
      result = (inputMask == null)? null: inputMask.getValidator();          
    } else if (this.hasConstrait(FieldContraintTypes.FieldValidation)) {
      result = this.constraints.getOptions(FieldContraintTypes.FieldValidation);
    }
    return result;
  }
  
  /**
   * <p>Called by the EntityWrapper to validate the input of a field input or a field's
   * value in a record (i.e. forRecord=true). If invalid, the validation error message
   * is assign to this.errorMsg.</p>
   * <p><b>Note:</b> The NoNull constraint is only checked if forRecord = false.</p>
   * @param <TWrapper> extends EntityWrapper
   * @param wrapper the EntityWrapper instance
   * @param inValue the field value
   * @param forRecord true if this validation is to check the record level constraints.
   * @return true if the value is valid, false if this.hasError
   */
  @SuppressWarnings("unchecked")
  public <TWrapper extends EntityWrapper> boolean isValidInput(TWrapper wrapper, 
                                              Object inValue, boolean forRecord) {
    this.clearErrorMsg();
    try {
      if (this.returnType.equals(String.class)) {
        String inStr = DataEntry.cleanString((String) inValue);
        if (inStr == null) {
          if ((forRecord) && (this.hasConstrait(FieldContraintTypes.NotNull))) {
            throw new 
                   Exception(this.constraints.getMessage(FieldContraintTypes.NotNull));
          }
        } else {
          StringValidator validator = (StringValidator) this.getInputValidator();
          if (validator != null) {
            inStr = validator.toValue(inStr);
            if (!validator.isValidInput(inStr)) {
              throw new Exception(validator.getErrorMsg());
            }
          }
                    
          if (this.hasConstrait(FieldContraintTypes.SizeRange)) {
            IntegerRange sizeRange = 
                          this.constraints.getOptions(FieldContraintTypes.SizeRange);
            if ((sizeRange != null) && (!sizeRange.inRange(inStr.length()))) {
              throw new 
                  Exception(this.constraints.getMessage(FieldContraintTypes.SizeRange));
            }
          }
        }  
        inValue = inStr;
      } else {
        if (inValue == null) {
          if ((forRecord) && (this.hasConstrait(FieldContraintTypes.NotNull))) {
            throw new 
                   Exception(this.constraints.getMessage(FieldContraintTypes.NotNull));
          } 
        } else {
          InputValidator validator = this.getInputValidator();
          if ((validator != null) && (!validator.isValidInput(inValue))) {
            throw new Exception(validator.getErrorMsg());
          }
        }
      }
        
      if (inValue != null) {
        if ((this.hasConstrait(FieldContraintTypes.Unique)) && (wrapper != null)) {
          IsUniqueOptions options = 
                            this.constraints.getOptions(FieldContraintTypes.Unique);
          int filterOptions = (options == null)? 0: options.filterOptions;
          if (!wrapper.isUnique(this.fieldName, inValue, filterOptions)) {
            throw new Exception("The enter " + this.getCaption() + " value is not "
                    + "unqinue. Duplicates are not allowed.");
          }
        } else if ((this.hasConstrait(FieldContraintTypes.UniqueInParent)) && 
                (wrapper != null)) {
          IsUniqueOptions options = 
                       this.constraints.getOptions(FieldContraintTypes.UniqueInParent);
          int filterOptions = (options == null)? 0: options.filterOptions;
          Class<? extends EntityWrapper> parentClass = options.parentClass;

          if (!wrapper.isUniqueInParent(this.fieldName, inValue, parentClass, 
                  filterOptions)) {
            Class parentBeanClass = EntityWrapper.getEntityClass(parentClass);
            throw new Exception("The enter " + this.getCaption() + " value is not "
                    + "unqinue within parent Class["
                    + parentBeanClass.getSimpleName() +"]'s context. "
                    + "Duplicates are not allowed.");
          }
        }
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    }
    return (!this.hasError());
  }
  
  /**
   * <p>Called by the {@linkplain FieldParameterDef} to validate the input of a parameter 
   * value before it assigned the object field. If invalid, the validation error message
   * is assign to this.errorMsg.</p>
   * <p><b>Note:</b> The NoNull constraint is only checked if forRecord = false.</p>
   * @param <TWrapper> extends EntityWrapper
   * @param wrapper the EntityWrapper instance
   * @param inValue the field value
   * @param forRecord true if this validation is to check the record level constraints.
   * @return true if the value is valid, false if this.hasError
   */
  @SuppressWarnings("unchecked")
  public boolean isValidParamValue(Object inValue) {
    this.clearErrorMsg();
    try {
      if (this.returnType.equals(String.class)) {
        String inStr = DataEntry.cleanString((String) inValue);
        if (inStr == null) {
          if (this.hasConstrait(FieldContraintTypes.NotNull)) {
            throw new Exception(this.constraints.getMessage(FieldContraintTypes.NotNull));
          }
        } else {
          StringValidator validator = (StringValidator) this.getInputValidator();
          if (validator != null) {
            inStr = validator.toValue(inStr);
            if (!validator.isValidInput(inStr)) {
              throw new Exception(validator.getErrorMsg());
            }
          }
                    
          if (this.hasConstrait(FieldContraintTypes.SizeRange)) {
            IntegerRange sizeRange = 
                          this.constraints.getOptions(FieldContraintTypes.SizeRange);
            if ((sizeRange != null) && (!sizeRange.inRange(inStr.length()))) {
              throw new 
                  Exception(this.constraints.getMessage(FieldContraintTypes.SizeRange));
            }
          }
        }  
        inValue = inStr;
      } else {
        if (inValue == null) {
          if (this.hasConstrait(FieldContraintTypes.NotNull)) {
            throw new Exception(this.constraints.getMessage(FieldContraintTypes.NotNull));
          } 
        } else {
          InputValidator validator = this.getInputValidator();
          if ((validator != null) && (!validator.isValidInput(inValue))) {
            throw new Exception(validator.getErrorMsg());
          }
        }
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
    }
    return (!this.hasError());
  }
  //</editor-fold>
}
