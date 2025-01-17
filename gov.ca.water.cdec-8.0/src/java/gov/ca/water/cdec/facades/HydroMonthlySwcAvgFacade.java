
package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.HydroMonthlySwcAvg;
import gov.ca.water.cdec.entities.HydroMonthlySwcAvgPK;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <p>A Facade for Entity[HydroMonthlySwcAvg]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class HydroMonthlySwcAvgFacade extends CdecBaseFacade<HydroMonthlySwcAvg> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public HydroMonthlySwcAvgFacade() {
    super(HydroMonthlySwcAvg.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Monthly Average SWC-ratio (i.e., relative to Apr1) for all 
   * Hydrological Regions in California. 
   * @return a hashMap(key=hydroNum, value=linkedHashMap(key=aMonth, value=swcPcp))
   */
  public HashMap<Integer,LinkedHashMap<Integer, Double>> getSwcAvgs() {
    HashMap<Integer,LinkedHashMap<Integer, Double>> result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("HydroMonthlySwcAvg.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[HydroMonthlySwcAvg.findAll]");
      }
      List<HydroMonthlySwcAvg> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[HydroMonthlySwcAvg.findAll] "
                + "is empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      LinkedHashMap<Integer, Double> monthMap = null;
      Integer hydroNum = null;
      Integer month = null;
      Double swcAvg = null;
      for (HydroMonthlySwcAvg swcAvgRec : qryResult) {
        HydroMonthlySwcAvgPK swcAvgPK = swcAvgRec.getHydroMonthlySwcAvgPK();
        if ((swcAvgPK == null) || ((hydroNum = swcAvgPK.getHydroNum()) == null) ||
            ((month = swcAvgPK.getRmonth()) == null) ||
            ((swcAvg = swcAvgRec.getRavg()) == null)) {
          continue;
        }
        if ((!result.containsKey(hydroNum)) || 
                                          ((monthMap = result.get(hydroNum)) == null)) {
          monthMap = new LinkedHashMap<>();
          result.put(hydroNum, monthMap);
        }
        monthMap.put(month, swcAvg);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getWyPcpAvgs getSwcAvgs:\n {1}",
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
