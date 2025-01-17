package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.FnfMainStreams;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>
 * A Facade for Entity[Resv_Info]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom
 * queries:</p><ul>
 * <li></li>
 * </ul>
 *
 * @author rmanning
 */
@Stateless
public class FnfMainStreamsFacade extends CdecBaseFacade<FnfMainStreams> {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public FnfMainStreamsFacade() {
    super(FnfMainStreams.class);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the all the Fnf MainStream info. It uses the
   * NamedQuery[FnfMainStreams.findAll] .
   * <p>
   * <b>NOTE:</b> All errors are trapped an logged and null value is
   * returned</p>
   * @return a HashMap[key=stationId, value=FnfMainStreams] or 
   * null if an error occurred or not found.
   */
  public HashMap<String, FnfMainStreams> getFnfMainStreams() {
    HashMap<String, FnfMainStreams> result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("FnfMainStreams.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[FnfMainStreams.findAll]");
      }

      List<FnfMainStreams> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[FnfMainStreams."
                + "findAll] is empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      String staKey = null;
      for (FnfMainStreams stream : qryResult) {
        if (((staKey = stream.getStationId()) != null) && 
                ((staKey = staKey.trim()).length() > 0)) {
          result.put(staKey.toUpperCase(), stream);                
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getFnfMainStreams Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
 //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement CdecBaseFacade">  
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
