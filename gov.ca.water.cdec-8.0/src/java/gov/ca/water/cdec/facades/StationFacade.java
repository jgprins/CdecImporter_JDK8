package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.entities.Station;
import static gov.ca.water.cdec.facades.CdecBaseFacade.logger;
import gov.ca.water.cdec.importers.ImportUtils;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>
 * A Facade for Entity[Station]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom
 * queries:</p><ul>
 * <li></li>
 * </ul>
 *
 * @author kprins
 */
@Stateless
public class StationFacade extends CdecBaseFacade<Station> {

  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public StationFacade() {
    super(Station.class);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Placeholder for the EntityManager
   */
  public Station getSensorData(String stationId) {
    Station result = null;
    try {
      if (stationId == null) {
        throw new Exception("The Station's Id is undefined.");
      }

      EntityManager entityManager = this.getEntityManager();
      result = entityManager.find(Station.class, stationId);
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
        
  /**
   * Get a list of Station records filtered on station.basinNum using the 
   * NamedQuery[Station.findByBasinNum]
   * @param basinNum the BasinNum to to search for
   * @return the list of Station or null | empty if not found
   */
  public synchronized List<Station> getStationsByBasinNum(Integer basinNum) {
    List<Station> result = null;
    try {
      if ((basinNum != null) && (basinNum > 0)) {
        EntityManager myEm = this.getEntityManager();
        Query qry = myEm.createNamedQuery("Station.findByBasinNum");
        if (qry == null) {
          throw new Exception("Unable to access NamedQuery[Station.findByBasinNum]");
        }

        /* Asign the Query Parameters */
        qry.setParameter("basinNum", basinNum);

        result = qry.getResultList();
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getStationsByBasinNum Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
        
  /**
   * Get a list of Station records filtered on station.stationId or station.stationName
   * using a partial <tt>keyword</tt> search using the NamedQuery[Station.findByKeyword] 
   * <p>
   * <b>NOTE:</b> If Key does not start or end with the wildcard character "%" it
   * will be added to the start and end of the keyWord.</p>
   * @param keyword the search keyword - ignored is null|"".
   * @return the list of Station or null | empty if not found
   */
  public synchronized List<Station> getStationsByKeyword(String keyword) {
    List<Station> result = null;
    try {
      if ((keyword = ImportUtils.cleanString(keyword)) != null) {
        if ((!keyword.startsWith("%")) || (!keyword.endsWith("%"))) {
          keyword = "%" + keyword + "%";
        }
        
        EntityManager myEm = this.getEntityManager();
        Query qry = myEm.createNamedQuery("Station.findByKeyword");
        if (qry == null) {
          throw new Exception("Unable to access NamedQuery[Station.findByKeyword]");
        }

        /* Asign the Query Parameters */
        qry.setParameter("keyword", keyword);

        result = qry.getResultList();
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getStationsByKeyword Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="expanded" desc="Implement CdecBaseFacade">  
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
