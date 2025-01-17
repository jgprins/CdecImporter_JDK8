package bubblewrap.entity.search;

import bubblewrap.entity.core.EntityWrapper;
import java.io.Serializable;

/**
 * An Abstract Delegate class for handling custom filter of EntityList search results.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class EntityFilterDelegate<TWrapper extends EntityWrapper> 
                                                                implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Owner/listener of the delegate
   */
  private final Object listener;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityFilterDelegate(Object listener) {
    super(); 
    this.listener = listener;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
  public abstract boolean include(TWrapper entity);
  // </editor-fold>
}
