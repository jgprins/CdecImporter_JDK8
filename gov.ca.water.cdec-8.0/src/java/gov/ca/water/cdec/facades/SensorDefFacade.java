package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.SensorDef;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * <p>A Facade for Entity[SensorDef]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class SensorDefFacade extends CdecBaseFacade<SensorDef> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public SensorDefFacade() {
    super(SensorDef.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="expanded" desc="Implment CdecBaseFacade">  
//  /**
//   * {@inheritDoc}
//   * <p>OVERRIDE: Return Em[gov.ca.water.cdecPU]</p>
//   */
//  @Override
//  protected EntityManager getEntityManager() {
//    if (this.em == null) {
//      throw new NullPointerException(this.getClass().getSimpleName() +
//              ".entityManager is not accessible.");
//    }
//    return this.em;
//  }
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return Em[gov.ca.water.cdecPU]</p>
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
