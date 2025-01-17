package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Class Level Annotation for assigning the multiple AccessValidationList
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface AccessValidationList {    
  /**
   * Get/Set the set of AccessValidation
   * (default=null)
   */
   public AccessValidation[] value();
}
