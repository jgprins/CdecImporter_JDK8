package bubblewrap.entity.annotations;

import bubblewrap.entity.context.EntityContext;
import bubblewrap.entity.context.FieldInfo;
import bubblewrap.entity.core.EntityFacade;
import bubblewrap.io.converters.FieldValueConverter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Entity Bean field Annotation fro specifying a {@linkplain FieldValueConverter} to
 * be used to convert a specified value (typically the EntityWrapper or a cached or
 * a delegate property for a Entity Bean field) to the actual field value.
 * <p>The Annotation is read by when initiating the Bean {@linkplain EntityContext} and
 * is a assign as a property of the field's {@linkplain FieldInfo}.
 * <p>One application where a FieldValueConverter is used is by the EntityFacade's
 * {@linkplain EntityFacade#findAllByField(java.lang.String, java.lang.Object, int) 
 * findAllByField} method. It retrieves the specified field FieldInfo to a) check 
 * whether this valid, non-delegate field of the entity, and b) to check if the 
 * field has a specified FieldValueConverter, If the converter is defined, it will use 
 * it to check if the specified value can be converted and if true, it call the 
 * converter to convert the value to a field value and use the value in defining the 
 * EntityFilter.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD})
public @interface FieldConverter { 
  /**
   * Get/Set the FieldValueConverter Class which should be used to convert the value.
   * Exception if converter=Void (default=Void.class).
   * @return the assigned class
   */
  Class<? extends FieldValueConverter> converter() 
          default FieldValueConverter.Void.class; 
  
  /**
   * (Optional) Get/Set the parameter that will be assigned to the convert when the 
   * converter instance is initiated (e.g. a numeric format string)
   * @return the assigned options (default = {}).
   */
  String[] options() default {};
}
