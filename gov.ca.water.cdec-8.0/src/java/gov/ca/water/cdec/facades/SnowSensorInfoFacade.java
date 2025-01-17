package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.SnowSensorInfo;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 *
 * @author rmanning
 */
@Stateless
public class SnowSensorInfoFacade extends CdecBaseFacade<SnowSensorInfo> {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public SnowSensorInfoFacade() {
    super(SnowSensorInfo.class);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Reservoir info for a specified Sensors. It uses the
   * NamedQuery[SnowSensorInfo.findBySensorIds]. The result is a HashMap with key =
   * SnowSensorInfo.stationId and value = SnowSensorInfo.
   * <p>
   * <b>NOTE:</b> All errors are trapped an logged and null value is
   * returned</p>
   * @param sensorIds the a list of Sensor Ids to filter by
   * @return the result - null if an error occurred or not found.
   */
  public HashMap<String, SnowSensorInfo> getSensorInfoByStationIds(
                                                              List<String> stationId) {
    HashMap<String, SnowSensorInfo> result = null;
    try {
      if ((stationId == null) || (stationId.isEmpty())) {
        throw new Exception("The StationIds to query for are undefined.");
      }

      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("SnowSensorInfo.findByStationIds");
      if (qry == null) {
        throw new Exception("Unable to access "
                + "NamedQuery[SnowSensorInfo.findByStationIds]");
      }

      /* Asign the Query Parameters */
      qry.setParameter("stationIds", stationId);

      List<SnowSensorInfo> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from "
                + "NamedQuery[SnowSensorInfo.findByStationIds] is empty. "
                + "Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (SnowSensorInfo sensorInfo : qryResult) {
        result.put(sensorInfo.getStationId(), sensorInfo);                
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorInfoByStationIds Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
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
