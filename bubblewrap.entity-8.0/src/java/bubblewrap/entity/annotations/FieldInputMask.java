package bubblewrap.entity.annotations;

import bubblewrap.entity.validators.FieldValidator;
import bubblewrap.io.enums.InputMasks;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to assign a Entity Field's input mask for validation the user input.
 * See {@linkplain FieldValidator} for more details.
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value={ElementType.FIELD, ElementType.METHOD})
public @interface FieldInputMask {
  /**
   * Get/Set the Field's {@linkplain InputMasks} (default=null)
   * @return the assigned value
   */
  InputMasks mask();
}
