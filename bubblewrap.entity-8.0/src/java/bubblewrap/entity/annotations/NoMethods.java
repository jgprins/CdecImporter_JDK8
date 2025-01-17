package bubblewrap.entity.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *<p>An Field Annotation assigned to Entity fields to indicate that this field has no
 * get or set-method. Typically, because it is indirectly assigned.</p>
 * <p><b>Note:</b>This Annotation is used by the EntityContext class to check before
 * logging a "No GET-Method" ContextError</p>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME) 
@Target(value=ElementType.FIELD)
public @interface NoMethods {  
}
