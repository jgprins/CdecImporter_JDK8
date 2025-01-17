package bubblewrap.io.poperties.annotation;

import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.entity.annotations.FieldValidation;
import bubblewrap.io.poperties.Property;
import bubblewrap.io.poperties.StringProperty;
import java.lang.annotation.*;

/**
 * An annotation defining to define a {@linkplain Property} of type propClass
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value={ElementType.TYPE})
public @interface PropertyDef {
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
   * The Property Class to use in converting the specified string value to a casted
   * parameter value. Default = StringParameter.class.
   * @return the assign class
   */  
  Class<? extends Property> propClass() default StringProperty.class;
  /**
   * The Property's FieldValidation. The Validator must be in support of the 
   * <tt>propClass</tt>. (default = FieldValidation.validator=VOID) 
   * @return this assigned FieldValidation annotation
   */
  FieldValidation validation() default @FieldValidation;
  /**
   * The Property's Field Value Converter. The Converter must be in support of the 
   * <tt>propClass</tt>. (default = FieldConverter.converter=VOID)
   * @return this assigned FieldConverter annotation
   */
  FieldConverter converter() default @FieldConverter;
}
