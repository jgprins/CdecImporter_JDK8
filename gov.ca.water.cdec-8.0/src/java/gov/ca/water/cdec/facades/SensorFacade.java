package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.CdecSensorInfo;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.ca.water.cdec.entities.*;
import gov.ca.water.cdec.enums.DurationCodes;
import static gov.ca.water.cdec.facades.CdecBaseFacade.logger;
import gov.ca.water.cdec.importers.ImportUtils;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[Sensor]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class SensorFacade extends CdecBaseFacade<Sensor> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  public SensorFacade() {
    super(Sensor.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Custom Queries">
  /**
   * Get the Sensor for the unique sensorId
   * @param sensorId the SensorId to search for
   * @return the sensor or null if not found
   * @throws IllegalArgumentException is the input parameters are invalid or if multiple 
   * records are returned
   */
  public synchronized Sensor findBySensorId(Integer sensorId) {
    Sensor result = null;
    try {      
      if ((sensorId == null) || (sensorId < 0)) {
        throw new Exception("SensorId[" + sensorId + "] is invalid.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("Sensor.findBySensorId");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[Sensor.findBySensorId]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      
      List<Sensor> qryResult = qry.getResultList();
      if ((qryResult != null) && (!qryResult.isEmpty())) {
        if (qryResult.size() > 1) {
          throw new Exception("SensorId[" + sensorId + "] has multiple Sensors.");
        }
        result = qryResult.get(0);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".findBySensorId Error:\n " + exp.getMessage());
    }
    return result;
  }
  /**
   * Get the Sensor for the unique StationId, SensorNum and Duration Codes primary key
   * @param sationId the CDEC StationId
   * @param sensorNum the SensorNum to search for
   * @param durCode the Duration Code to search for
   * @return the sensor record or null if not found.
   * @throws IllegalArgumentException is the input parameters are invalid or if multiple 
   * records are returned
   */
  public synchronized Sensor getStationSensor(String stationId, Integer sensorNum, 
                              DurationCodes durCode)  {
    Sensor result = null;
    try {
      if ((stationId == null) || (((stationId.trim()).length()) == 0)) {
        throw new Exception("The Sensor's StationId is undefined.");
      }
      
      if ((sensorNum == null) || (sensorNum < 0)) {
        throw new Exception("SensorNum[" + sensorNum + "] is invalid.");
      }
      
      if (durCode == null) {
        throw new Exception("The Sensor's DurationCode is undefined");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("Sensor.findStationSensor");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[Sensor.findStationSensor]");
      }
      
      String code = durCode.toString();
      /* Asign the Query Parameters */
      qry.setParameter("stationId", stationId);
      qry.setParameter("sensorNum", sensorNum);
      qry.setParameter("durCode", code);
      
      List<Sensor> qryResult = qry.getResultList();
      if ((qryResult != null) && (!qryResult.isEmpty())) {
        if (qryResult.size() > 1) {
          throw new Exception("Station[" + stationId + "] has multiple Sensors of "
                  + "Type[" + sensorNum + "} and Duration[" + durCode + "].");
        }
        result = qryResult.get(0);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getStationSensor Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Return a list of Sensors with matching SensorNum and Duration Codes
   * @param sensorNum the SensorNum to search for
   * @param durCode the Duration Code to search for
   * @return the resulting list of sensors.
   * @throws Exception
   */
  public synchronized List<Sensor> getSensorsBySensorType(Integer sensorNum, DurationCodes durCode)
          throws Exception {
    List<Sensor> result = null;
    try {
      if ((sensorNum == null) || (sensorNum < 0)) {
        throw new Exception("SensorNum[" + sensorNum + "] is invalid.");
      }
      
      if (durCode == null) {
        throw new Exception("The Sensor's DurationCode is undefined");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("Sensor.findBySensorType");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[Sensor.findBySensorType]");
      }
      
      String code = durCode.toString();
      /* Asign the Query Parameters */
      qry.setParameter("sensorNum", sensorNum);
      qry.setParameter("durCode", code);
      
      result = qry.getResultList();
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getSensorsBySensorType Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Get a list of Sensor records for the specified stationIds, durationCode and
   * sensorNum.
   * @param stationIds List of StationIds to return sensor info for
   * @param durCode Duration code (H,D,M,Y...).
   * @param sensorNum Sensor Number (denoting sensor type).
   * @return the SensorPK or null if not found
   */
  public synchronized List<Sensor> getSensorsForStations(List<String> stationIds, String durCode,
                                                             int sensorNum) {
    List<Sensor> result = null;
    try {
      if ((stationIds == null) || (stationIds.isEmpty())) {
        throw new Exception("The Station Ids are undefined.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("Sensor.sensorInfo");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[Sensor.sensorInfo]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("stationIds", stationIds);
      qry.setParameter("durCode", durCode);
      qry.setParameter("sensorNum", sensorNum);
      
      result = qry.getResultList();
      if ((result == null) || (result.isEmpty())) {
        throw new Exception("The result list from NamedQuery[Sensor.sensorInfo] is "
                + "empty. Query SQL = " + qry.toString());
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorsForStations Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Get a list of CdecSensorInfo definitions for the specified sensorType and stationIds 
   * and SesnoroType. If <tt>stationIds</tt> = null|empty it will return all the sensors
   * for the specified <tt>sensorType</tt>. It return null if sensorType = null or
   * and error occurred and an empty list if no matches has been found.
   * @param sensorType the SensorType to get the SesnorInfo for.
   * @param stationIds List of StationIds to return sensor info for
   * @return the SensorPK or null if not found
   */
  public synchronized List<CdecSensorInfo> getSensorInfos(CdecSensorTypes sensorType, 
                                                              String...stationIds) {
    List<CdecSensorInfo> result = null;
    try {
      if (sensorType != null) {
        List<Sensor> sensorList = null;
        if ((stationIds == null) || (stationIds.length == 0)) {
          sensorList = this.getSensorsBySensorType(sensorType.sensorNo, 
                                                          sensorType.getDurationCode());
        } else {
          List<String> staList = Arrays.asList(stationIds);
          sensorList = this.getSensorsForStations(staList, sensorType.durCode, 
                                                          sensorType.sensorNo);
        }
        
        if (sensorList != null) {
          result = new ArrayList<>();
          if (!sensorList.isEmpty()) {
            for (Sensor sensor : sensorList) {              
              SensorPK senPk = sensor.getSensorPK();
              Station senSta = senPk.getStation();
              CdecSensorInfo sensorinfo = new CdecSensorInfo(sensor.getSensorId(), 
                                sensorType, senPk.getStationId(), senSta.getBasinNum());
              if (sensorinfo == null) {
                throw new Exception("Unable to initiate the sensorInfo for " 
                                              + senPk.toString());
              }
              
              result.add(sensorinfo);
            }
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorsForStations Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
      
  /**
   * Get a list of Sensor records for the Station[stationId] 
   * @param stationId the StationId to to search for
   * @return the list of sensors or null | empty if not found
   */
  public synchronized List<Sensor> getSensorsByStation(String stationId) {
    List<Sensor> result = null;
    try {
      if ((stationId = ImportUtils.cleanString(stationId)) != null) {
        EntityManager myEm = this.getEntityManager();
        Query qry = myEm.createNamedQuery("Sensor.findByStationId");
        if (qry == null) {
          throw new Exception("Unable to access NamedQuery[Sensor.findByStationId]");
        }

        /* Asign the Query Parameters */
        qry.setParameter("stationId", stationId);

        result = qry.getResultList();
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorsForStations Error:\n {1}",
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
