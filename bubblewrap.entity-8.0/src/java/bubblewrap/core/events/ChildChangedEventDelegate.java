package bubblewrap.core.events;

/**
 * A EventDelgate for a ChildChanged Event 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class ChildChangedEventDelegate 
                                     extends EventDelegateBase<ChildChangedEventArgs>{
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public ChildChangedEventDelegate(Object listener) {
    super(listener);
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }  
  //</editor-fold>
}
