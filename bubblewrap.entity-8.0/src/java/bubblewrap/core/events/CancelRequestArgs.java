package bubblewrap.core.events;

/**
 * Extends RequestArgs to get a Cancel Request with a Boolean request result, which is
 * set to true is the process must be canceled. The Cancel flag is set when a Cancel
 * message is assigned. getResult = true if canceled and false if not.
 * NOTE: isHandled is always false.
 * @author kprins
 */
public class CancelRequestArgs extends RequestArgs<Boolean> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public CancelRequestArgs() {
    super();
  }
  
  /**
   * Public Constructor
   */
  public CancelRequestArgs(boolean bMultiErrors) {
    super(bMultiErrors);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override RequestArgs">
  @Override
  protected Boolean getOnErrorResult() {
    return true;
  }
  
  @Override
  protected Boolean getUnhandledResult() {
    return false;
  }
  // </editor-fold>
}
