package bubblewrap.io.validators;

/**
 * A String options Validator that does NOT allow null values.
 * @author kprins
 */
public class StringOptionsNotNullValidator extends StringOptionsValidator {

// <editor-fold defaultstate="collapsed" desc="Public Constructor">
  /**
   * Public Constructor - set allowNull=false
   */
  public StringOptionsNotNullValidator() {
    super();
    this.initAllowNull(false);
  }
// </editor-fold>
}
