package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign to an Entity field to designate the field as the Entity's
 * record name. It has a readOnly property indicating that only a Get-Method is
 * supported. It can be assigned to a Field or Method. Must be an String field.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface AccessCodeField {
  /**
   * Get/Set a the entity property's readOnly state (default=false)
   * @return the assigned (or default) value
   */
  boolean readOnly() default false;
}
