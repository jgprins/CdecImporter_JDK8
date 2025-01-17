package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign to an Entity field or method to designate the caption (i.e., 
 * a user friendly field name) to used in for reporting validation errors.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface FieldCaption {
  /**
   * Get/Set the Field's Caption (the field name is used is unassigned)
   * @return the assigned value 
   */
  String caption(); 
}
