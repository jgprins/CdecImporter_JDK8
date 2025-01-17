package bubblewrap.entity.core;

import java.io.Serializable;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class EntityActionHandler<TBean extends Serializable, 
                            TWrapper extends EntityWrapper<TBean>> {
  
  private TWrapper entity;
  private String EntityKey;
  private int actionId;    
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected EntityActionHandler() {
    super();
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
