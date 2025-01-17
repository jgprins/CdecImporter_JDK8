package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.Agency;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Stateless
public class AgencyFacade extends CdecBaseFacade<Agency>{

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  private String Test;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public AgencyFacade() {
    super(Agency.class);  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implment CdecBaseFacade">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: if (this.em = null), set this.em = 
   * CdecEJBContext.getInstance().entityManager. Return this.em.</p>
   * @throws NullPointerException if this.entityManager = null.
   */
  @Override
  protected EntityManager getEntityManager() {
    CdecEJBContext ejbCtx = null;
    if (this.em == null) {
      if ((ejbCtx = CdecEJBContext.getInstance()) != null) {
        this.em = ejbCtx.getEntityManager();
      }
    }
    if (this.em == null) {
      throw new NullPointerException(this.getClass().getSimpleName() +
              ".entityManager is not accessible.");
    }
    return this.em;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Assign em to this.em if the latter is unassigned</p>
   */
  @Override
  protected void setEntityManager(EntityManager em) {
    if (this.em == null) {
      this.em = em;
    }
  }
  //</editor-fold>

  
}
