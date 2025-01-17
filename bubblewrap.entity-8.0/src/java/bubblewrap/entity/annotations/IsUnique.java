package bubblewrap.entity.annotations;

import bubblewrap.core.enums.FacadeFilterEnums;
import bubblewrap.entity.core.EntityWrapper;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign to an Entity field's IsUnique flag. If the inParentOnly=true,
 * the instance value has to be unique in the instance's associated with its parent
 * entity. Otherwise, it must be unique in the recordset.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface IsUnique {
  /**
   * Get/Set the annotation's is Unique in Parent Only flag. (default=false)
   * @return the assigned value
   */
  boolean inParentOnly() default false;
  
  /**
   * Get/Set the FilterOptions (type {@linkplain FacadeFilterEnums}) (default = 0|None)
   * @return the assigned value.
   */
  int filterOptions() default 0;
  
  /**
   * Get/Set the Parent EnityWrapper Class (required if inParentOnly=true) 
   * (default=Void.class).
   * <p><b>Note:</b>The parentClass must be a registered entity (i.e., if must have a
   * registered EntityContext)</p>
   * @return the assigned class
   */
  Class<? extends EntityWrapper> parentClass() default EntityWrapper.Void.class;
}
