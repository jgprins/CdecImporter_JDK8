package bubblewrap.io.predicates;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class UseDefaultPredicate<TValue extends Number>  
                                                              extends Predicate<TValue>{

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Default Value to Use
   */
  public final TValue defaultValue;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public UseDefaultPredicate(Object owner, TValue defaultValue) {
    super(owner); 
    this.defaultValue = defaultValue;
  }
  // </editor-fold>
}
