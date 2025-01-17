
package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.ResvAvg;
import gov.ca.water.cdec.entities.ResvAvgPK;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[FnfAvg]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class ResvAvgFacade extends CdecBaseFacade<ResvAvg> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public ResvAvgFacade() {
    super(ResvAvg.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Monthly Average Reservoir Storage for all listed Reservoir
   * Station. 
   * @return a hashMap(key=stationId, value=linkedHashMap(key=aMonth, value=resvAvgs))
   */
  public HashMap<String,LinkedHashMap<Integer, Double>> getResvAvgs() {
    HashMap<String,LinkedHashMap<Integer, Double>> result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("ResvAvg.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[ResvAvg.findAll]");
      }
      List<ResvAvg> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[ResvAvg.findAll] "
                + "is empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      LinkedHashMap<Integer, Double> monthMap = null;
      String stationId = null;
      Integer month = null;
      Double resvAvg = null;
      for (ResvAvg avgRec : qryResult) {
        ResvAvgPK avgPK = avgRec.getResvAvgPK();
        if ((avgPK == null) || 
            ((stationId = avgPK.getStationId()) == null) ||
            ((stationId = stationId.trim()).length() == 0)  ||  
            ((month = avgPK.getAmonth()) == null) ||
            ((resvAvg = avgRec.getAverage()) == null)) {
          continue;
        }
        if ((!result.containsKey(stationId)) || 
                                          ((monthMap = result.get(stationId)) == null)) {
          monthMap = new LinkedHashMap<>();
          result.put(stationId.toUpperCase(), monthMap);
        }
        monthMap.put(month, resvAvg);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getResvAvgs getSwcAvgs:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implment CdecBaseFacade">  
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
