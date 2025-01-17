package bubblewrap.core.selectors;

/**
 * A simple SelectObject wrapping an String selectable object
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class StringSelectObject extends SelectObject<String>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public StringSelectObject(String value) {
    super(value);  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="SelectObject Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (this.isNullValue())? "null": this.getValue())</p>
   */
  @Override
  protected String onGetSelectId() {
    return (this.isNullValue())? "null": this.getValue();
  }
  // </editor-fold>
}
