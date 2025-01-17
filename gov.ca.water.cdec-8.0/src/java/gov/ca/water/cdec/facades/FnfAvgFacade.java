
package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.FnfAvg;
import gov.ca.water.cdec.entities.FnfAvgPK;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <p>A Facade for Entity[FnfAvg]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class FnfAvgFacade extends CdecBaseFacade<FnfAvg> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public FnfAvgFacade() {
    super(FnfAvg.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Monthly Average FNF for all listed FNF Station. 
   * @return a hashMap(key=stationid, value=linkedHashMap(key=aMonth, value=fnfAvg))
   */
  public HashMap<String,LinkedHashMap<Integer, Double>> getFnfAvgs() {
    HashMap<String,LinkedHashMap<Integer, Double>> result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("FnfAvg.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[FnfAvg.findAll]");
      }
      List<FnfAvg> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[FnfAvg.findAll] "
                + "is empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      LinkedHashMap<Integer, Double> monthMap = null;
      String stationId = null;
      Integer month = null;
      Double fnfAvg = null;
      for (FnfAvg fnfAvgRec : qryResult) {
        FnfAvgPK fnfAvgPK = fnfAvgRec.getFnfAvgPK();
        if ((fnfAvgPK == null) || 
            ((stationId = fnfAvgPK.getStationId()) == null) ||
            ((stationId = stationId.trim()).length() == 0)  ||  
            ((month = fnfAvgPK.getAmonth()) == null) ||
            ((fnfAvg = fnfAvgRec.getAverage()) == null)) {
          continue;
        }
        if ((!result.containsKey(stationId)) || 
                                          ((monthMap = result.get(stationId)) == null)) {
          monthMap = new LinkedHashMap<>();
          result.put(stationId.toUpperCase(), monthMap);
        }
        monthMap.put(month, fnfAvg);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getFnfAvgs getSwcAvgs:\n {1}",
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
