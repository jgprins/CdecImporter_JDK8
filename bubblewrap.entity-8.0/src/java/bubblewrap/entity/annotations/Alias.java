package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To define a Field ALias based on the defined AliasField definition
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.FIELD,ElementType.METHOD})
public @interface Alias {
  /**
   * The alias field name
   * @return the assigned name
   */
  String alias();
  /**
   * (Optional) Flag stating whether this field is read only (default = false)
   * @return the assigned value
   */
  boolean readOnly() default false;
}
