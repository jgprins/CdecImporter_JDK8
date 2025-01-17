package bubblewrap.io.poperties.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A Annotation for assigning one or more {@linkplain PropertyDef} to a class
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface PropertyDefs {
  /**
   * Array of PropertyDefs (default ={})
   * @return assigned values
   */
  PropertyDef[] value() default {};
}
