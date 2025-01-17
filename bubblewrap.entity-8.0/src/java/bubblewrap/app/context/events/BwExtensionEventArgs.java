package bubblewrap.app.context.events;

import bubblewrap.app.context.BwAppExtension;
import bubblewrap.core.events.EventArgs;

/**
 * An EventArgs for the {@linkplain BwExtensionEventHandler} 
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class BwExtensionEventArgs extends EventArgs {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private BwAppExtension extension;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public BwExtensionEventArgs(BwAppExtension extension) {
    super();  
    if (extension == null) {
      throw new NullPointerException("The Bw Extension Reference cannot be null");
    }
    this.extension = extension;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Check if the assigned extension is an instance of the specified <tt>extClass</tt>
   * @param extClass the class to test for
   * @return true if the extension is assigned and is an instance of <tt>extClass</tt>
   */
  public boolean isExtensionClass(Class<? extends BwAppExtension> extClass) {
    return ((extClass != null) && (this.extension != null) &&
            (extClass.isInstance(this.extension)));
  }
  
  /**
   * Get the assigned Extension case as a <tt>TExt</tt>
   * @param <TExt> extends {@linkplain BwAppExtension}
   * @return the assigned extension 
   */
  public <TExt extends BwAppExtension> TExt getExtension() {
    TExt result = (this.extension == null)? null: (TExt) this.extension;
    return result;
  }
  // </editor-fold>
}
