package bubblewrap.io.validators;

/**
 * StringNotNullValidator is a
 * @author kprins
 */
public class StringNotNullValidator extends StringValidator {

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor - call super and set allowNull=false
   */
  public StringNotNullValidator(){
    super();
    this.initAllowNull(false);
  }
  //</editor-fold>
}
