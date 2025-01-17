package bubblewrap.core.annotations;

import bubblewrap.io.DataEntry;
import bubblewrap.io.params.*;
import java.lang.annotation.*;

/**
 * A Parameter Annotation
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.ANNOTATION_TYPE})
public @interface Param {
  /**
   * The Parameter Key (required). Parameter keys should not contain any white spaces,
   * but can contain "_", ".", "-". All white spaces will be replaced with "_" 
   * characters - see {@linkplain DataEntry#toParamKey(java.lang.String)
   * DataEntry.toParamKey} for more information.
   * @return the assigned Key
   */
  String key();
  /**
   * The Parameter Value (Default = "").
   * @return the assigned value
   */
  String value() default "";
  /**
   * The Parameter Class to use in converting the specified string value to a casted
   * parameter value. Default = StringParameter.class.
   * @return the assign class
   */  
  Class<? extends Parameter> paramClass() default StringParameter.class;
  /**
   * (Optional) The format string to be used in formatting the Parameter Value 
   * (default = "").
   * @return the assigned value
   */
  String format() default "";
}
