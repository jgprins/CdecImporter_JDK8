package bubblewrap.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import bubblewrap.core.reflection.EnumInfo;

/**
 * A Field Level Annotation for Settings the SelectItem Label of an Enum Field used
 * by {@linkplain EnumInfo#getAsOptions(java.lang.Class, java.lang.Boolean, 
 * java.lang.String, int[], int[]) EnumInfo.getAsOptions} to generate a SelectItem List
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD})
public @interface EnumLabel {  
  /**
   * Get/Set a Label for displaying the Class as a SelectItem
   * @return 
   */
  String label();
}
