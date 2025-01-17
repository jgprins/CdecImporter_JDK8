
package bubblewrap.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Annotation used with Singleton EJBs to assign to a Static Method that can be 
 * call to access the singleton instance. The method should take no arguments
 * @author kprins
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD})
public @interface LookupMethod {  
}
