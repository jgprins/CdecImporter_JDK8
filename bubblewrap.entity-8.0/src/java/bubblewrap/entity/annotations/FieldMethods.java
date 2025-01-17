package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *<p>An Field Annotation assigned to Entity fields to set the names of the field's
 * get or set-methods. Only used in cases where the methods does not match the default
 * get- and set-method convention (i.e., with field="myField", the Get="getMyField" and
 * the Set="setMyField").</p>
 * <p><b>Note:</b>In the EntityContext registration the fields set be this annotation
 * will be used instead of the default field to present the field Get- and Set-method.
 * Assigning the Set-method is optional (make the field read only), but an unassigned
 * Get-Method name will cause a EntityContext Registration error.</p>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value=ElementType.FIELD)
public @interface FieldMethods {  
  /**
   * Get/Set the Field's Get-Method's Name. Must be assigned.
   * @return the assigned value 
   */
  String get();
  /**
   * (Optional) Get/Set the Field's Set-Method's Name.
   * @return the assigned value 
   */
  String set() default "";
}
