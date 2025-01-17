package bubblewrap.core.selectors;

/**
 * A simple SelectObject wrapping an integer selectable object
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class IntSelectObject extends SelectObject<Integer>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public IntSelectObject(Integer value) {
    super(value);  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="SelectObject Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (this.isNullValue())? "null": this.getValue().toString()</p>
   */
  @Override
  protected String onGetSelectId() {
    return (this.isNullValue())? "null": this.getValue().toString();
  }
  // </editor-fold>
}
