package bubblewrap.io.validators;

/**
 * An IntegerOptionsValidator that does not allow Null values.
 * @author kprins
 */
public class IntegerOptionsNotNullValidator extends IntegerOptionsValidator {

  /**
   * Parameterless Constructor (set mbAllwoNull=false)
   */
  public IntegerOptionsNotNullValidator() {
    super();
    this.initAllowNull(false);
  }
}
