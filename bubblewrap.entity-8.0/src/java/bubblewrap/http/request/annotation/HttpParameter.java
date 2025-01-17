package bubblewrap.http.request.annotation;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.io.converters.FieldValueConverter;
import bubblewrap.io.converters.DataConverter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The HttpParameter annotation is assigned to a Class' GET-Methods to retrieve the 
 * parameter values for a parameter map the of field values to send to a web-client
 * and to assigned updated field values received from a web-client to an instance of
 * the class.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpParameter {
  /**
   * (Optional) The Parameter field's parameter name (default = "", use the field name
   * @return the assigned name
   */
  String name() default "";
  /**
   * (Optional) The FieldValueConverter Class which should be used to convert the value.
   * (default=Void.class it will attempt to use the {@linkplain DataConverter#toValue(
   * java.lang.String, java.lang.Class) DataConverter.toValue} method to convert the
   * input string value to the field's value type).
   * @return the assigned class
   */
  FieldConverter converter() default @FieldConverter(); 
  
  /**
   * (Optional) The name of the SET-method. Only used if the SET-method cannot be
   * retrieved from the GET-Method using the ReflectionInfo utility's {@linkplain 
   * ReflectionInfo#getFieldname(java.lang.String) getFieldname} and {@linkplain
   * ReflectionInfo#getSetMethodName(java.lang.String) getSetMethodName} methods.
   * @return the assigned value (default = "" - use default SET-Method name)
   */
  String set() default "";
  
  /**
   * (Optional) A flag indicating that this is a readOnly value. Use to send information
   * to the client side, but ignored when submitted be a form
   */
  boolean readOnly() default false;
}
