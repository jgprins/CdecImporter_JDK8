package bubblewrap.io.predicates;

import java.io.Serializable;

/**
 * A generic predicate for test a condition based on the field values/properties of a
 * target and the values, conditions, or settings of the Predicate Owner.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class Predicate<TTarget extends Serializable> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private Object owner;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public Predicate(Object owner) {
    super();
    if (owner == null) {
      throw new NullPointerException("The reference to the Predicate's owner cannot "
              + "be unassigned.");
    }
    this.owner = owner;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the reference to the Predicate's owner cased as type TOwner
   * @param <TOwner>
   * @return the Predicate's owner
   */
  public <TOwner> TOwner getOwner() {
    return (TOwner) this.owner;
  } 
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Called to apply a test on <tt>target</tt>, which return true if the test
   * pass or false is not.
   * @param target the target object to test
   * @return true if test pass.
   */
  public abstract boolean isTrue(TTarget target);
  // </editor-fold>
}
