package bubblewrap.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Class Level Annotation for Settings the Properties of a class used by the 
 * {@linkplain ClassSelector}.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface ClassInfo {  
  /**
   * Get/Set a Label for displaying the Class as a SelectItem
   * @return 
   */
  String classLabel();
}
