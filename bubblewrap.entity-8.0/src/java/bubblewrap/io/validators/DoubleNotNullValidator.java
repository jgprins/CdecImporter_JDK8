package bubblewrap.io.validators;

/**
 * An Double Input Validator that does not allow Null values.
 * @author kprins
 */
public class DoubleNotNullValidator extends DoubleValidator {

  /**
   * Parameterless Constructor - set allowNul=false;
   */
  public DoubleNotNullValidator() {
    super();
    super.initValidator(false);
  }
}
