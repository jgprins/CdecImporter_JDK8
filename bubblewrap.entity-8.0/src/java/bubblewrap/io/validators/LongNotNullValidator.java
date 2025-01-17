package bubblewrap.io.validators;

/**
 * An Long Input Validator that does not allow Null values.
 * @author kprins
 */
public class LongNotNullValidator extends LongValidator {

  /**
   * Parameterless Constructor - set allowNul=false;
   */
  public LongNotNullValidator() {
    super();
    this.initValidator(false);
  }
}
