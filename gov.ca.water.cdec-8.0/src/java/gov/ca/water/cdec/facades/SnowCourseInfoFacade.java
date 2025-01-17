package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.SnowCourseInfo;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[SnowCourseInfo]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class SnowCourseInfoFacade extends CdecBaseFacade<SnowCourseInfo> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public SnowCourseInfoFacade() {
    super(SnowCourseInfo.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Snow Course Schedule(Field[Measured]) for all snow course 
   * stations. It uses the NamedQuery[SnowCourseInfo.findAll].
   * @return a hashMap(key=stationId, value=measured)
   */
  public HashMap<String,String> getSnowSchedules() {
    HashMap<String,String> result = new HashMap<>();
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("SnowCourseInfo.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[SnowCourseInfo.findAll]");
      }
      List<SnowCourseInfo> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[SnowCourseInfo.findAll] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      for (SnowCourseInfo courseInfo : qryResult) {
        result.put(courseInfo.getStationId(), courseInfo.getMeasured());
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getSnowSchedules:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Snow Course Schedule(Field[Measured]) for all snow course 
   * stations. It uses the NamedQuery[SnowCourseInfo.findAll].
   * @return a hashMap(key=stationId, value=measured)
   */
  public HashMap<String,SnowCourseInfo> getActiveByStation() {
    HashMap<String,SnowCourseInfo> result = new HashMap<>();
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("SnowCourseInfo.findAllActive");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[SnowCourseInfo.findAllActive]");
      }
      List<SnowCourseInfo> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from "
                + "NamedQuery[SnowCourseInfo.findAllActive] is empty. Query SQL = " 
                + qry.toString());
      }
      
      String staId = null;
      for (SnowCourseInfo courseInfo : qryResult) {
        if (((staId = courseInfo.getStationId()) != null) &&
                (!staId.trim().equals(""))) {
          result.put(staId.toUpperCase(), courseInfo);
        }
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getActiveByStation:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the SnowCourseInfo for Station[cdecId = <tt>stationId</tt>]
   * @param stationId the CDEC Station ID to search for.
   * @return SnowCourseInfo or null if not found or record is no longer active.
   */
  public SnowCourseInfo getStationSnowCourseInfo(String stationId) {
    SnowCourseInfo result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("SnowCourseInfo.findActiveByStation");
      if (qry == null) {
        throw new Exception("Unable to access "
                + "NamedQuery[SnowCourseInfo.findActiveByStation]");
      }
      
      if ((stationId != null) && (!stationId.toUpperCase().trim().equals(""))) {      
        qry.setParameter("stationId", stationId);

        List<SnowCourseInfo> qryResult = qry.getResultList();
        if ((qryResult != null) && (qryResult.size() > 0)) {
          result = qryResult.get(0);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getActiveByStation:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implment CdecBaseFacade">  
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
