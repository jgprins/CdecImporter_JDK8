package bubblewrap.entity.annotations;

import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.AccessFlags;
import bubblewrap.navigation.enums.AppTasks;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Class Level Annotation for Settings the Entity's Access Validation and Navigation
 * parameters. Set doValidation=true|false is access validation is required or not.
 * Set appTask, and subTask for both access validation or navigation purposes.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface AccessValidation {  
  /**
   * Get/Set whether the entity required user access validation (default=false)
   * @return the assigned (or default) value
   */
  boolean doValidation() default false;
  /**
   * Get/Set the Access Validation/Navigation Application Task (default="CNTMNG")
   * @return the assigned value
   */
  AppTasks appTask() default AppTasks.CNTMNG;
  /**
   * Get/Set the Access Validation/Navigation Subtask (default=null)
   * @return the assigned value
   */
  String subTask();  
  /**
   * Get/Set the EntityWrapper Class to which this access validation settings apply 
   * (default=Void.class - all EntityWrapper classes).
   * @return the assigned class
   */
  Class<? extends EntityWrapper> wrapperClass() default EntityWrapper.Void.class;
  /**
   * Get/Set the Entity's Access Flag (type {@link AccessFlags})
   * @return the assigned access flag 
   * (default={} - full access)
   */
  AccessFlags[] accessFlags() default {};
}
