package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.ResvInfo;
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
public class ResvInfoFacade extends CdecBaseFacade<ResvInfo> {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public ResvInfoFacade() {
    super(ResvInfo.class);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Reservoir info for a specified Sensors. It uses the
   * NamedQuery[RESV_INFO.findByResName] .
   * <p>
   * <b>NOTE:</b> All errors are trapped an logged and null value is
   * returned</p>
   *
   * @param stationIds the list of station IDs for which to retrieve data. Return all 
   * records is stationIds = null|empty
   * @return the result - null if an error occurred or not found.
   */
  public HashMap<String, ResvInfo> getResvInfo(List<String> stationIds) {
    HashMap<String, ResvInfo> result = null;
    try {
      String nameQry = "ResvInfo.findByStationIds";
      if ((stationIds == null) || (stationIds.isEmpty())) {
        nameQry = "ResvInfo.findAll";
      }

      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery(nameQry);
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[" + nameQry + "]");
      }

      /* Asign the Query Parameters */
      if ((stationIds != null) && (!stationIds.isEmpty())) {
        qry.setParameter("stationIds", stationIds);
      }

      List<ResvInfo> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[" + nameQry  
                + "] is empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (ResvInfo resvInfo : qryResult) {
        result.put(resvInfo.getStationId(), resvInfo);                
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getResvInfo Error:\n {1}",
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
