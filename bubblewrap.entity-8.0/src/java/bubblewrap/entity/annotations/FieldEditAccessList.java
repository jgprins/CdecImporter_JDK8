package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Class Level Annotation for assigning the multiple FieldEditAccess per field or 
 * method
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface FieldEditAccessList {    
  /**
   * Get/Set the set of FieldEditAccess
   * (default=null)
   */
   public FieldEditAccess[] value();
}
