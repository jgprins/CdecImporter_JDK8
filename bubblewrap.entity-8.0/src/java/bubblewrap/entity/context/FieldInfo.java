package bubblewrap.entity.context;

import bubblewrap.core.enums.PrimaryKeyType;
import bubblewrap.entity.annotations.Alias;
import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.entity.annotations.FieldEditAccess;
import bubblewrap.entity.annotations.FieldEditAccessList;
import bubblewrap.entity.annotations.NoMethods;
import bubblewrap.entity.annotations.PrimaryKey;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.FieldEditFlags;
import bubblewrap.io.converters.FieldValueConverter;
import bubblewrap.entity.validators.FieldContraintTypes;
import bubblewrap.entity.validators.FieldValidator;
import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.InputValidator;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Id;

/**
 * A Wrapper for a Field's base reflection properties *
 * @author kprins
 * @version 1.00.001 (11/01/16) the NotNullable constraint form manually assigned Primary
 * Key fields - see {@linkplain #initFieldValidator() this.initFieldValidator}.
 */
public class FieldInfo implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger = Logger.getLogger(FieldInfo.class.getName());
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The name of entity field
   */
  public final EntityPath entityPath;
  /**
   * The entity's field (can be null if this.isDelegate)
   */
  public final Field field;
  /**
   * The field's associated getMthod (always non-null)
   */
  public final Method getMethod;
  /**
   * The field's associated setMthod (can be null if readOnly)
   */
  public final Method setMethod;
  /**
   * The Field's return Type
   */
  public final Class<?> returnType;
  /**
   *  The Field alias (if defined or null if not)
   */
  public final String alias;
  /**
   * The Field's Input validator - lazy initiated
   */
  public final FieldValidator validator;
  /**
   * The Field's Input Value COnverter - lazy initiated
   */
  public final FieldValueConverter valueConverter;
  /**
   * A Flag indicating whether this is the Entity's Primary Key
   */
  public final Boolean isPrimaryKey;
  /**
   * If this is a PriamryKey, then this will be set and be meaningful
   */
  private PrimaryKeyType primaryKeyType;
  /**
   * The Field (or Get-methods) assigned Annotations;
   */
  private HashMap<Class<? extends Annotation>, Annotation> annotationMap;
  /**
   * Placeholder for the Field's {@linkplain FieldEditFlags}
   */
  private HashMap<Class<? extends EntityWrapper>, FieldEditAccessInfo> fieldEditMap;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * FieldInfo constructor with a defined EntityPath, and GET-Method and optional
   * assignments of field's SET-Method and Field reference.
   * <p>
   * The constructor build a HasMap of Annotation's by class, which include all the
   * annotation assigned to either the Field or the GET-Method. It also initiates
   * the field's alias (is an {@linkplain Alias} annotation is assigned, its value
   * Validator, and its input ValueConverter.
   * <p><b>Note:</b>If the Alias annotation's readOnly=true, the specified SET-Method
   * will be ignored.</p>
   * @param entityPath the field EntityPath (i.e., EntityClass and fieldName) (required)
   * @param getMethod the field's GET-Method (required - if ((field==null) && (the
   * field has not an assigned NoMethod annotation).
   * @param setMethod (Optional) the field's SET-method (the field is readOnly is not
   * defined).
   * @param field (Optional) the entities field .
   */
  public FieldInfo(EntityPath entityPath, Method getMethod, Method setMethod,
          Field field) {
    if (entityPath == null) {
      throw new NullPointerException("The Field EntityPath cannot be unassigned.");
    }

    boolean reqMethod = true;
    if (field != null) {
      this.buildAnnotMap(field.getDeclaredAnnotations());
      reqMethod = this.hasMethods();

      if ((!reqMethod) && ((getMethod != null) || (setMethod != null))) {
        throw new IllegalArgumentException("The Field does not support a GET- or "
                + "SET-Method, but a GET- and/or SET-Method is specified.");
      }
    }

    if ((reqMethod) && (getMethod == null)) {
      throw new NullPointerException("The Field required at least a GET-Method, but "
              + "the GET-Method is unassigned.");
    }

    if ((getMethod != null) && (void.class.equals(getMethod.getReturnType()))) {
      throw new IllegalArgumentException("The Field's GET-method is invalid for it is "
              + "a method method with no return type.");
    }

    if (getMethod != null) {
      this.buildAnnotMap(getMethod.getDeclaredAnnotations());
    }

    if ((setMethod != null) && (!void.class.equals(setMethod.getReturnType()))) {
      throw new IllegalArgumentException("The Field's SET-method is invalid for it is "
              + "a method with a return type.");
    }

    this.entityPath = entityPath;
    this.field = field;
    this.getMethod = getMethod;
    this.returnType = (getMethod == null)? field.getType(): getMethod.getReturnType();

    boolean isNullable = true;
    this.primaryKeyType = null;
    this.isPrimaryKey = ((field != null) &&
                        ((field.isAnnotationPresent(PrimaryKey.class))
                                  || (field.isAnnotationPresent(Id.class))));
    if (this.isPrimaryKey)  {
      PrimaryKey pkAnnot = field.getAnnotation(PrimaryKey.class);
      if (pkAnnot != null) {
        this.primaryKeyType = pkAnnot.type();
      }

      if (this.primaryKeyType == null) {
        this.primaryKeyType =
                    PrimaryKeyType.getDefaultPrimaryKeyType(this.returnType);
      }

      /**
       * The record can be nullable on submit if it is an auto-assigned recordId.
       * @since v1.00.001 - remove the following line - making all PrimaryKey's nullable.
       * isNullable = PrimaryKeyType.isAutoRecorded(this.primaryKeyType);
       */
    }

    Alias aliasAnnot = this.getAnnotation(Alias.class);
    String fldAlias = null;
    if (aliasAnnot != null) {
      fldAlias = DataEntry.cleanString(aliasAnnot.alias());
      if (aliasAnnot.readOnly()) {
        setMethod = null;
      }
    }

    this.alias = fldAlias;
    this.setMethod = setMethod;
    this.validator = ((this.isPrimaryKey) && (isNullable))? null:
                              this.initFieldValidator();
    this.valueConverter = this.initValueConverter();
    this.initFieldEditMap();
  }

  /**
   * Called by the constructors to build a Map of Annotations assigned to the field or
   * its associated getMethod.
   * @param annotArr
   */
  private void buildAnnotMap(Annotation...annotArr) {
    if ((annotArr == null) || (annotArr.length == 0)) {
      return;
    }

    if (this.annotationMap == null) {
      this.annotationMap = new HashMap<>();
    }

    for (Annotation annot : annotArr) {
      Class<? extends Annotation> annotClass = annot.annotationType();
      if (this.annotationMap.containsKey(annotClass)) {
        throw new IllegalArgumentException("Field[" + this.entityPath + "] contains "
                + "duplicate annotation[" + annotClass.getSimpleName()
                + "] assignments.");
      }

      this.annotationMap.put(annotClass, annot);
    }
  }

  /**
   * Called by the constructor to initiate this.fieldEditMap based on the assigned
   * {@linkplain FieldEditAccess} and/or {@linkplain FieldEditAccessList} annotations.
   */
  private void initFieldEditMap() {
    this.fieldEditMap = new HashMap<>();
    FieldEditAccess editAnnot = this.getAnnotation(FieldEditAccess.class);
    if (editAnnot != null) {
      this.initAccessInfo(editAnnot);
    }
    FieldEditAccessList annotArr = this.getAnnotation(FieldEditAccessList.class);
    if (annotArr != null) {
      this.initAccessInfo(annotArr.value());
    }
  }

  /**
   * Called to initiate the Field Edit Access Information - if applicable
   * @param editAnnot the assigned FieldEditAccess
   */
  private void initAccessInfo(FieldEditAccess...annotArr) {
    if ((annotArr != null) && (annotArr.length > 0)) {
      for (FieldEditAccess accessAnnot : annotArr) {
        try {
          FieldEditAccessInfo editInfo = new FieldEditAccessInfo(accessAnnot);
          if (editInfo == null) {
            continue;
          }

          if (this.fieldEditMap.containsKey(editInfo.wrapperClass)) {
            throw new IllegalArgumentException("Duplicate FieldEditAccess for "
                + "EntityWrapper[" + editInfo.wrapperClass + "].");
          }
          this.fieldEditMap.put(editInfo.wrapperClass, editInfo);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}[{1}].Initiate FieldEditAccessInfo "
                  + "Error:\n {3}", new Object[]{this.getClass().getSimpleName(),
                    this.entityPath, exp.getMessage()});
        }
      }
    }
  }

  /**
   * Called by the constructor to initiate the Field validator
   * @return
   */
  private FieldValidator initFieldValidator() {
    FieldValidator result = null;
    if ((this.annotationMap != null) && (!this.annotationMap.isEmpty())) {
      try {
        result = FieldValidator.init(this);
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}[{1}].initFieldValidator Error:\n {2}",
                new Object[]{this.getClass().getSimpleName(), this.entityPath,
                  pExp.getMessage()});
      }
    }
    return result;
  }

  /**
   * Get the Field Validator - initiate the validator if no yet initiated.
   * @return the validator or null if the process failed.
   */
  private FieldValueConverter initValueConverter() {
    FieldValueConverter result = null;
    FieldConverter annot = this.getAnnotation(FieldConverter.class);
    if (annot!= null) {
      try {
        Class<? extends FieldValueConverter> convClass = annot.converter();
        if ((convClass != null) && (!FieldValueConverter.Void.class.equals(convClass))){
          result = convClass.newInstance();
          if (result == null) {
            throw new Exception("Initiating the FieldValueConverter Type["
                    + convClass.getSimpleName() + "] failed.");
          }

          result.setOptions(annot.options());
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}[{1}].getValueConverter Error:\n {2}",
                new Object[]{this.getClass().getSimpleName(), this.entityPath,
                  exp.getMessage()});
      }
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the PrimaryKey's Type - only meaningful if this.isPriamryKey
   * @return PrimaryKey's Type or else null.
   */
  public final PrimaryKeyType getPrimaryKeyType () {
    return this.primaryKeyType;
  }

  /**
   * Check if this field has Set-Method name - if not, it is a readOnly field.
   * @return (this.setMethod != null).
   */
  public final boolean hasSetMethod() {
    return (this.setMethod != null);
  }

  /**
   * Check if this field has get-/set-methods
   * @return true if no {@linkplain NoMethods} annotation is assigned.
   */
  public final boolean hasMethods() {
    NoMethods annot = this.getAnnotation(NoMethods.class);
    return (annot == null);
  }

  /**
   * Get whether this is a Delegate field (i.e., a field that has no entity/bean
   * field, but is a field to with at least a get-method to get the value of
   * delegate property).
   * @return true if (this.field == null).
   */
  public final boolean isDelegate() {
    return (this.field == null);
  }

  /**
   * Get whether this field's value is not-nullable (required).
   * @return ((this.validator != null) and
            (this.validator.hasConstrait(FieldContraintTypes.NotNull)))
   */
  public final boolean isNotNull() {
    return ((this.validator != null) &&
            (this.validator.hasConstrait(FieldContraintTypes.NotNull)));
  }

  /**
   * Get the field/Get-method's assigned Annotations
   * @return an array of annotations - the array is empty if no annotations is assigned.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public final Annotation[] getAnnotations() {
    Annotation[] result = new Annotation[]{};
    Collection annotList = this.annotationMap.values();
    if ((annotList != null) && (!annotList.isEmpty())) {
      annotList.toArray(result);
    }
    return result;
  }

  /**
   * Check if this Field as assigned Annotation of type annotClasss.
   * @param <TAnnot> the required case type
   * @param annotClass the Annotation class to search for.
   * @return the annotation (can be null if not found)
   */
  public final <TAnnot extends Annotation> boolean hasAnnotation(
                                                            Class<TAnnot> annotClass) {
    return ((annotClass != null) && (this.annotationMap != null) &&
            (this.annotationMap.containsKey(annotClass)));
  }

  /**
   * Return the field's assigned annotation of type annotClasss.
   * @param <TAnnot> the required case type
   * @param annotClass the Annotation class to search for.
   * @return the annotation (can be null if not found)
   */
  @SuppressWarnings("unchecked")
  public final <TAnnot extends Annotation> TAnnot getAnnotation(
                                                            Class<TAnnot> annotClass) {
    TAnnot result = null;
    if ((annotClass != null) && (this.annotationMap != null) &&
            (this.annotationMap.containsKey(annotClass))) {
      result = (TAnnot) this.annotationMap.get(annotClass);
    }
    return result;
  }

  /**
   * Get the FieldEditAccessInfo for the specified EntityWrapper class
   *
   * @param wrapperClass the specified EntityWrapper class
   * @return the FieldEditAccessInfo if found or {@linkplain #getDefaultEditAccessInfo()
   * this.defaultEditAccessInfo} if not found.
   */
  public FieldEditAccessInfo getEditAccessInfo(Class<? extends EntityWrapper>
          wrapperClass) {
    FieldEditAccessInfo result = null;
    if ((wrapperClass != null) && (this.fieldEditMap.containsKey(wrapperClass))) {
      result = this.fieldEditMap.get(wrapperClass);
    }
    if (result == null) {
      result = this.getDefaultEditAccessInfo();
    }
    return result;
  }

  /**
   * Get the FieldEditAccessInfo for the EntityWrapper.Void.class
   *
   * @return the default FieldEditAccessInfo
   */
  public FieldEditAccessInfo getDefaultEditAccessInfo() {
    FieldEditAccessInfo result = null;
    Class<? extends EntityWrapper> wrapperClass = EntityWrapper.Void.class;
    if ((wrapperClass != null) && (this.fieldEditMap.containsKey(wrapperClass))) {
      result = this.fieldEditMap.get(wrapperClass);
    }
    if (result == null) {
      result = new FieldEditAccessInfo();
    }
    return result;
  }

  /**
   * Check if this field is editable based on it FieldEditAccess settings for the
   * specified EntityWrapper class and the records <tt>isSystemItem</tt> setting. It
   * locates {@linkplain #getEditAccessInfo(java.lang.Class) this.editAccessInfo} for
   * wrapperClass and calls its {@linkplain FieldEditAccessInfo#allowEdits(
   * ava.lang.Boolean) allowEdits(isSystemItem)} to determine if the field can be
   * edited.
   * @param wrapperClass the specified EntityWrapper class
   * @param isSystemItem
   * @return true if (this.setMethodName != null) and this.editAccessInfo.allowEdits.
   */
  public boolean allowEdits(Class<? extends EntityWrapper> wrapperClass,
          Boolean isSystemItem) {
    boolean result = this.hasSetMethod();
    if (result) {
      FieldEditAccessInfo editInfo = this.getEditAccessInfo(wrapperClass);
      if (editInfo != null) {
        result = editInfo.allowEdits(isSystemItem);
      }
    }
    return result;
  }

  /**
   * Get the FieldValidator's InputValidator - is assigned as a InputMask or a
   * FieldValdation constraint.
   *
   * @return the validator or null if the process failed.
   */
  public InputValidator getInputValidator() {
    InputValidator result = null;
    if (this.validator != null) {
      result = this.validator.getInputValidator();
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  public String toHtmlString() {
    String result = "<b>Field Info[" + this.entityPath.toString() + "]:</b> <ul>";
    result += "<li>Get-Method = " + ((this.getMethod == null)? "-":
            this.getMethod.getName()) + "</li>";
    result += "<li>Set-Method = " + ((this.setMethod == null)? "-":
            this.setMethod.getName()) + "</li>";
    result += "<li>ReturnType = " + this.returnType.getSimpleName()
            + "</li>";
    result += "<li>isDelegate = " + this.isDelegate() + "</li>";
    for (FieldEditAccessInfo editAccess : this.fieldEditMap.values()) {
      result += "<li>" + editAccess.toHtmlString() + "</li>";
    }
    result += "</ul></li>";
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "Field[Name=??; Type=??; ReadOnly=??]"</p>
   */
  @Override
  public String toString() {
    String result = "Field[Path=" + this.entityPath.toString() + "]:\n";
    result += "\t-Type = " + this.returnType.getSimpleName() + "\n";
    result += this.fieldEditMap.toString();
    return result;
  }
  //</editor-fold>
}
