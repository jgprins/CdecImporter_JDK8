
package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.PrecipAvg;
import gov.ca.water.cdec.entities.PrecipAvgPK;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * <p>A Facade for Entity[PrecipAvg]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class PrecipAvgFacade extends CdecBaseFacade<PrecipAvg> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public PrecipAvgFacade() {
    super(PrecipAvg.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the Annual Average Precip for all station. It uses the
   * NamedQuery[PrecipAvg.wyAvgs].
   * @return a hashMap(key=stationId, value=avgPcp)
   */
  public HashMap<String,Double> getWyPcpAvgs() {
    HashMap<String,Double> result = null;
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("PrecipAvg.wyAvgs");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[PrecipAvg.wyAvgs]");
      }
      List<PrecipAvg> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[PrecipAvg.wyAvgs] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (PrecipAvg pcpAvg : qryResult) {
        PrecipAvgPK pcpPK = pcpAvg.getPrecipAvgPK();
        result.put(pcpPK.getStationId(), pcpAvg.getAvgPrecip());
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getWyPcpAvgs getWyPcpAvgs:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Average Precip for from the start of the WaterYear (month=10) to
   * the current month (e.g., for a May 1 forecast it will be <tt>curMonth</tt> = 4 
   * (Apr)).
   * @return a hashMap(key=stationId, value=avgPcpToDt)
   */
  public HashMap<String,Double> getWyPcpAvgsToDate(Integer curMonth) {
    HashMap<String,Double> result = null;
    try {
      if ((curMonth == null) || (curMonth < 1) || (curMonth > 12)) {
        throw new Exception("Invalid Current Month[" + curMonth 
                + "]. Expected a value in Range[1..12].");
      }
      
      List<Integer> incMonths = new ArrayList<>();
      if (curMonth >= 10) {
        for (int i = 10; i<= curMonth; i++) {
          incMonths.add(i);
        }
      } else {        
        for (int i = 1; i <= curMonth; i++) {
          incMonths.add(i);
        }
        for (int i = 10; i<= 12; i++) {
          incMonths.add(i);
        }
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("PrecipAvg.wyAvgsToDate");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[PrecipAvg.wyAvgsToDate]");
      }
      
      qry.setParameter("inclMonths", incMonths);
      
      List<Object[]> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[PrecipAvg.wyAvgsToDate] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      String stationId;
      Double precipAvg;
      for (Object[] pcpAvg : qryResult) {
        if ((pcpAvg != null) && (pcpAvg.length >= 2) &&
                  ((stationId = (String) pcpAvg[0]) != null) &&
                  ((precipAvg = (Double) pcpAvg[1]) != null)) {
          result.put(stationId, precipAvg);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getWyPcpAvgsToDate getWyPcpAvgs:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Called to load the Average Precip all station's in <tt>stationIds</tt>.
   * @param stationIds the array of stationIds for which to load all monthly values.
   * @return a hashMap(key=stationId, value=Double[]) with indices 0..12, where idx=0 is
   * annual average.
   */
  public HashMap<String,Double[]> getPcpAvgByStationIds(String...stationIds) {
    HashMap<String,Double[]> result = null;
    try {
      if ((stationIds == null) || (stationIds.length == 0)) {
        throw new Exception("The Array of Station ID cannot be empty.");
      }
      
      List<String> staIdList = Arrays.asList(stationIds);
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("PrecipAvg.byStationIds");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[PrecipAvg.byStationIds]");
      }
      
      qry.setParameter("stationIds", staIdList);
      
      List<PrecipAvg> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[PrecipAvg.byStationIds] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      String stationId;
      int monthNo;
      Double precipAvg;
      PrecipAvgPK pk = null;
      for (PrecipAvg pcpAvg : qryResult) {
        if ((pcpAvg == null) || ((precipAvg = pcpAvg.getAvgPrecip()) == null) ||
            ((pk = pcpAvg.getPrecipAvgPK()) == null)) {
          continue;
        }
        
        stationId = pk.getStationId();
        monthNo = pk.getObsMonth();
        Double[] avgArr = null;
        if (result.containsKey(stationId)) {
          avgArr = result.get(stationId);
        } else {
          avgArr = new Double[13];
          result.put(stationId, avgArr);
        }
        avgArr[monthNo] = precipAvg;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getPcpAvgByStationIds:\n {1}",
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
