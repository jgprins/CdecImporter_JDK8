package bubblewrap.entity.annotations;

import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.FieldEditFlags;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign to an Entity field to designate the field as the Entity's
 * securityLevel field. It has a readOnly property indicating that only a Get-Method is
 * supported. It can be assigned to a Field or Method. Must be an Integer field.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface FieldEditAccess {
  /**
   * Get/Set the EntityWrapper Class to which this access validation settings apply 
   * (default=Void.class - all EntityWrapper classes).
   * @return the assigned class
   */
  Class<? extends EntityWrapper> wrapperClass() default EntityWrapper.Void.class;
  /**
   * Get/Set a the entity property's readOnly state (default=false)
   * @return the assigned (or default) value
   */
  FieldEditFlags[] editFlags() default {FieldEditFlags.NoConstaints};
}
