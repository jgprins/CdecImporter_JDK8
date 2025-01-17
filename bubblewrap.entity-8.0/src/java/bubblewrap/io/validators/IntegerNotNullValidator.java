package bubblewrap.io.validators;

/**
 * An Integer Input Validator that does not allow Null values.
 * @author kprins
 */
public class IntegerNotNullValidator extends IntegerValidator {

  /**
   * Parameterless Constructor - set allowNul=false;
   */
  public IntegerNotNullValidator() {
    super();
    this.initValidator(false);
  }
}
