package bubblewrap.entity.annotations;

import bubblewrap.io.validators.InputValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The FieldValidation attribute can be assigned to an Entity Field to as a Validation
 * constraint. Specify the {@linkplain InputValidator} class and the validation options
 * to validate for (e.g., a range of values, or defined options). For default String
 * input validation use {@linkplain FieldInputMask} instead.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD})
public @interface FieldValidation {
  
  /**
   * The Input Validator class (must inherit from {@linkplain InputValidator}
   * @return the assigned class or null if unassigned
   */
  Class<? extends InputValidator> validator() default InputValidator.Void.class;
    
  /**
   * Get the (Optional) Validator class' options (to assign on initiation)
   * @return the assign value - default = {}.
   */
  String[] options() default {};
}
