package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.RiverBasin;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>
 * A Facade for Entity[RiverBasin]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom
 * queries:</p><ul>
 * <li></li>
 * </ul>
 *
 * @author kprins
 */
@Stateless
public class RiverBasinFacade extends CdecBaseFacade<RiverBasin> {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public RiverBasinFacade() {
    super(RiverBasin.class);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to load the River Basin Names by Basin Numbers. It uses the
   * NamedQuery[RiverBasin.findAll].
   * @return a hashMap(key=basinNum, value=basinName)
   * @return a hashMap(key=basinNum, value=basinName)
   */
  public HashMap<Integer, String> getBasinNames() {
    HashMap<Integer,String> result = new HashMap<>();
    try {
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("RiverBasin.findAll");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[RiverBasin.findAll]");
      }
      List<RiverBasin> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[RiverBasin."
                + "findAll] is empty. Query SQL = " + qry.toString());
      }

      for (RiverBasin basin : qryResult) {
        result.put(basin.getBasinNum(), basin.getBasinName());
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getBasinNames getWyAvgs:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  /**
   * Called to load the RiverBasin by Basin Numbers for the specified list of hydroNums
   * If hydroNums = null|empty return all basins. It uses the
   * NamedQuery[RiverBasin.findAll].
   * @return a hashMap(key=basinNum, value=RiverBasin)
   */
  public LinkedHashMap<Integer, RiverBasin> getBasinsByHydroNum(Integer...hydroNums) {
    LinkedHashMap<Integer,RiverBasin> result = new LinkedHashMap<>();
    try {
      EntityManager myEm = this.getEntityManager();
      String qryName = null;
      List<Integer> numList = null;
      if ((hydroNums == null) || (hydroNums.length == 0) ||
          ((numList = Arrays.asList(hydroNums)) == null) || (numList.isEmpty())) {
        qryName = "RiverBasin.findAll";
      } else {
        qryName = "RiverBasin.findByHydroNums";
      }
      Query qry = myEm.createNamedQuery(qryName);
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[" + qryName + "]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("hydroNums", numList);
      
      List<RiverBasin> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[" + qryName 
                + "] is empty. Query SQL = " + qry.toString());
      }

      for (RiverBasin basin : qryResult) {
        result.put(basin.getBasinNum(), basin);
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getBasinsByHydroNum getWyAvgs:\n {1}",
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
