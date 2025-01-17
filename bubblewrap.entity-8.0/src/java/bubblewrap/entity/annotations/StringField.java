/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.constraints.Size;

/**
 * An Annotation to define a string Field's Size. If the min> 0, the field does not
 * accept empty strings. If max=0 the annotation will be ignored. The message will be
 * used to display an input validation error message .
 * <p><b>Note:</b> This annotation is not the same as the {@linkplain Size} annotation
 * which set the size limitations in bytes, and if the database support Unicode 
 * characters, each character can be more than one byte.</p>
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface StringField {
  /**
   * The Minimum String length (default=0)
   * @return assigned value
   */
  int min() default 0;
  /**
   * The maximum String length (default=255)
   * @return assigned value
   */
  int max() default 255;
  /**
   * 
   * @return 
   */
  String message() default "";
}
