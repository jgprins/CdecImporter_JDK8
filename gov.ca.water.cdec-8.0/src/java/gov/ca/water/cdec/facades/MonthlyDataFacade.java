package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.*;
import static gov.ca.water.cdec.facades.CdecBaseFacade.logger;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[MonthlyData]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class MonthlyDataFacade extends 
                                 TimeSeriesFacade<MonthlyData, DateKey, MonthlyDataMap> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">  
  public MonthlyDataFacade() {
    super(MonthlyData.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  private HashMap<Integer, Sensor> getPcpSensors(List<String> stationIds)
          throws Exception {
    HashMap<Integer, Sensor> result = null;
    CdecSensorTypes sensorType = CdecSensorTypes.MonthlyPrecip;
    /**
     * Load the reservoir Information from Table[PrecipGroup]
     */
    SensorFacade sensorFacade = CdecBaseFacade.getFacade(SensorFacade.class);
    if (sensorFacade == null) {
      throw new Exception("Unable to access the SensorFacade.");
    }
    List<Sensor> pcpSensors = sensorFacade.getSensorsForStations(stationIds, 
                                              sensorType.durCode, sensorType.sensorNo);
    if ((pcpSensors == null) || (pcpSensors.isEmpty())) {
      throw new Exception("Extracting the Station's Monthly Precip Sensor Information "
              + "failed.");
    }
    
    result = new HashMap<>();
    for (Sensor sensor : pcpSensors) {
      result.put(sensor.getSensorId(), sensor);
    }
    return result;
  }
  
  /**
   * Get The Monthly Snow Sensor for the list of Station IDs
   * @param stationIds list of Station IDs
   * @return the list of snow sensors (see {@linkplain CdecSensorTypes#MonthlySnow})
   * @throws Exception 
   */
  private HashMap<Integer, Sensor> getSnoSensors(List<String> stationIds)
          throws Exception {
    HashMap<Integer, Sensor> result = null;
    CdecSensorTypes sensorType = CdecSensorTypes.MonthlySnow;
    /**
     * Load the reservoir Information from Table[PrecipGroup]
     */
    SensorFacade sensorFacade = CdecBaseFacade.getFacade(SensorFacade.class);
    if (sensorFacade == null) {
      throw new Exception("Unable to access the SensorFacade.");
    }
    List<Sensor> pcpSensors = sensorFacade.getSensorsForStations(stationIds, 
                                         sensorType.durCode, sensorType.sensorNo);
    if ((pcpSensors == null) || (pcpSensors.isEmpty())) {
      throw new Exception("Extracting the Station's Monthly Snow Sensor Information "
              + "failed.");
    }
    
    result = new HashMap<>();
    for (Sensor sensor : pcpSensors) {
      result.put(sensor.getSensorId(), sensor);
    }
    return result;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to retrieve the MonthlyData for a specified sensor between two dates
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the list of MonthlyData values or null if an error occurs or no values are 
   * found.
   */
  public List<MonthlyData> getSensorData(int sensorId, Date startDt, Date endDt) {
    List<MonthlyData> result = null;
    try {
      if (sensorId <= 0) {
        throw new Exception("SensorId[" + sensorId + "] is invalid.");
      }
      
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if (endDt.before(startDt)) {
        Date tmpDt = endDt;
        endDt = startDt;
        startDt = tmpDt;
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.sensorData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.sensorData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      
      result = qry.getResultList();
      result = (result == null)? new ArrayList<MonthlyData>(): result;
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the MonthlyData for a specified sensor between two dates and
   * assign it to a hashMap with the Actual Dates as the HashMap key.
   * <b>NOTE:</b> This method return an empty map if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the HashMap of MonthlyData values, an empty map if no data was retrieved, or 
   * null if an error occurs.
   */
  @Override
  public MonthlyDataMap getSensorDataMap(int sensorId, Date startDt, Date endDt) {
    MonthlyDataMap result = null;
    try {
      List<MonthlyData> qryResult = this.getSensorData(sensorId, startDt, endDt);
      if (qryResult != null) {
        result = new MonthlyDataMap(sensorId);
        if (!qryResult.isEmpty()) {
          for (MonthlyData monthlyData : qryResult) {
            result.add(monthlyData);
          }
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorDataMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Monthly PCP Data for a set of Sensors into a {@linkplain 
   * MonthlyWyData}. It uses the NamedQuery[MonthlyData.wyData] to retrieve the date by
   * setting the start date of the query as Oct 1. and the endDt as the first day of the 
   * specified current month (e.g. if curMonth=2 (Feb) the lastDt = Feb 1).
   * <p><b>NOTE:</b> All errors are trapped an logged and an empty HashMap is returned</p>
   * @param waterYear the Water Year
   * @param curMonth the specified current (calender) month [1..12]
   * @param stationIds the list of Precip StationIds
   * @return the result set - can be empty is an error occurred.
   */
  public HashMap<String, Double> getWyToDtPcpStaData(Integer waterYear, Integer curMonth, 
                                                              List<String> stationIds) {
    HashMap<String, Double> result = new HashMap<>();
    try {
      /** Oct 1, waterYear-1 **/
      Calendar wyStart = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      wyStart.set(waterYear-1, 9, 1, 0, 0, 0);
      Date startDt = wyStart.getTime();
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
      
      /** curMonth-1 1, waterYear-1 **/
      Calendar wyCur = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      Integer curYr = (curMonth >= 10)? waterYear-1: waterYear;
      wyCur.set(curYr, curMonth-1, 1, 0, 0, 0);
      Date endDt = wyCur.getTime();
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if ((stationIds == null) || (stationIds.isEmpty())) {
        throw new Exception("The Precip StationId list is undefined or empty.");
      }
      
      HashMap<Integer, Sensor> pcpSensors = this.getPcpSensors(stationIds);
      if ((pcpSensors == null) || (pcpSensors.isEmpty())) {
        throw new Exception("Extracting the Sensor Information for the Precip Stations "
                + "failed.");
      }
      
      List<Integer> sensorIds = new ArrayList<>(pcpSensors.keySet());
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.wyToDateData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.wyToDateData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      qry.setParameter("sensorIds", sensorIds);
      
      List<Object[]> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.wyToDateData] "
                + "is empty. Query SQL = " + qry.toString());
      }
      
      Integer sensorId;
      Double obsValue;
      Sensor sensor;
      String stationId;
      for (Object[] monthlyData : qryResult) {
        if ((monthlyData != null) && (monthlyData.length >= 2) &&
                  ((sensorId = (Integer) monthlyData[0]) != null) &&
                  ((obsValue = (Double) monthlyData[1]) != null) &&
                  (pcpSensors.containsKey(sensorId)) &&
                  ((sensor =  pcpSensors.get(sensorId)) != null) &&
                  ((stationId = sensor.getSensorPK().getStationId()) != null)) {
          Double curValue = (result.containsKey(stationId))? result.get(stationId): 0.0d;
          curValue += obsValue;
          result.put(stationId, curValue);
        }
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getWyToDtPcpData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Monthly PCP Data for a set of Station and a set Data into a 
   * HashMap with key=>stationId and value=>.MonthlyData It uses the 
   * NamedQuery[MonthlyData.dataByDate] to retrieve the Data for the first of the month.
   * (i.e., precip through the end last mon
   * <p><b>NOTE:</b> All errors are trapped an logged and an empty HashMap is returned</p>
   * @param waterYear the Water Year
   * @param curMonth the specified current month
   * @param stationIds the list of station ID to filter on
   * @return the result set - can be empty is an error occurred.
   */
  public HashMap<String,MonthlyData> getMonthPcpStaData(Integer waterYear, 
                                            Integer curMonth, List<String> stationIds) {
    HashMap<String,MonthlyData> result = new HashMap();
    try {
      Calendar wyCur = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      Integer curYr = (curMonth >= 10)? waterYear-1: waterYear;
      wyCur.set(curYr, curMonth-1, 1, 0, 0, 0);
      Date actualDt = wyCur.getTime();
      if (actualDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if ((stationIds == null) || (stationIds.isEmpty())) {
        throw new Exception("The Precip StationId list is undefined or empty.");
      }
      
      HashMap<Integer, Sensor> pcpSensors = this.getPcpSensors(stationIds);
      if ((pcpSensors == null) || (pcpSensors.isEmpty())) {
        throw new Exception("Extracting the Sensor Information for the Precip Stations "
                + "failed.");
      }
      
      List<Integer> sensorIds = new ArrayList<>(pcpSensors.keySet());
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.dataByDate");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.dataByDate]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("actualDt", actualDt, TemporalType.DATE);
      qry.setParameter("sensorIds", sensorIds);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.dataByDate] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      Integer sensorId = null;
      Sensor sonsorInfo = null;
      SensorPK sensorPk = null;
      String stationId = null;
      for (MonthlyData monthlyData : qryResult) {
        MonthlyDataPK pk = monthlyData.getPrimaryKey();
        if ((pk == null) || ((sensorId = pk.getSensorId()) == null) ||
                (!pcpSensors.containsKey(sensorId)) ||
                ((sonsorInfo = pcpSensors.get(sensorId)) == null) ||
                ((sensorPk = sonsorInfo.getSensorPK()) == null) ||
                ((stationId = sensorPk.getStationId()) == null)) {
          continue;
        }
        result.put(stationId, monthlyData);
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getMonthPcpStaData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Monthly PCP Data for a set of Sensors into a {@linkplain 
   * MonthlyWyData}. It uses the NamedQuery[MonthlyData.wyData] to retrieve the date by
   * setting the start date of the query as Oct 1. and the endDt as the first day of the 
   * specified current month (e.g. if curMonth=2 (Feb) the endDt = Feb 1).
   * <p><b>NOTE:</b> All errors are trapped an logged and an empty HashMap is returned</p>
   * @param waterYear the Water Year
   * @param curMonth the specified current month
   * @param sensorIds the list of sensor values to filter on
   * @return the result set - can be empty is an error occurred.
   */
  public MonthlyWyData getWyPcpData(Integer waterYear, Integer curMonth, 
                                                            List<Integer> sensorIds) {
    MonthlyWyData result = new MonthlyWyData();
    try {
      Calendar wyStart = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      wyStart.set(waterYear-1, 9, 1, 0, 0, 0);
      Date startDt = wyStart.getTime();
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
      
      Calendar wyCur = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      Integer curYr = (curMonth >= 10)? waterYear-1: waterYear;
      wyCur.set(curYr, curMonth-1, 1, 0, 0, 0);
      Date endDt = wyCur.getTime();
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds list is undefined or empty.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.wyData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.wyData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      qry.setParameter("sensorIds", sensorIds);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.wyData] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      for (MonthlyData monthlyData : qryResult) {
        result.add(monthlyData);
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getWyPcpData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
   /**
   * Called to load the Monthly MonthlyFNF Data for a set of Sensors into a {@linkplain 
   * MonthlyWyData}. It uses the NamedQuery[MonthlyData.wyData] to retrieve the date by
   * setting the start date of the query as Oct 1. and the endDt as the first day of the 
   * specified current month (e.g. if curMonth=2 (Feb) the lastDt = Feb 1).
   * <p><b>NOTE:</b> All errors are trapped an logged and an empty HashMap is returned</p>
   * @param waterYear the Water Year
   * @param curMonth the specified current month
   * @param sensorIds the list of sensor values to filter on
   * @return the result set - can be empty is an error occurred.
   */
  public MonthlyWyData getWyFnfData(Integer waterYear, Integer curMonth, 
                                                            List<Integer> sensorIds) {
    MonthlyWyData result = new MonthlyWyData();
    try {
      Calendar wyStart = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      wyStart.set(waterYear-1, 9, 1, 0, 0, 0);
      Date startDt = wyStart.getTime();
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
      
      Calendar wyCur = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      Integer curYr = (curMonth >= 10)? waterYear-1: waterYear;
      wyCur.set(curYr, curMonth-1, 1, 0, 0, 0);
      Date endDt = wyCur.getTime();
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds list is undefined or empty.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.wyData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.wyData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      qry.setParameter("sensorIds", sensorIds);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.wyData] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      for (MonthlyData monthlyData : qryResult) {
        result.add(monthlyData);
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getWyFnfData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
 
  /**
   * <p>Called to load the Monthly Snow Water Content Data for a set of Sensors into a
   * {@linkplain MonthlyWyData}. It uses the NamedQuery[MonthlyData.wyData] to retrieve
   * the date by setting the start date of the query as Nov 1. and the endDt as the first
   * day of the following the specified current month (e.g. if curMonth=2 (Feb) the 
   * lastDt = Mar 1).</p>
   * <p><b>NOTE:</b> All errors are trapped an logged and an empty HashMap is returned</p>
   * @param waterYear the Water Year
   * @param curMonth the specified current month. Range [1..12]
   * @param sensorIds the list of sensor values to filter on
   * @return the result set - can be empty is an error occurred.
   */
  public MonthlyWyData getWySnoData(Integer waterYear, Integer curMonth, 
                                                            List<Integer> sensorIds) {
    MonthlyWyData result = new MonthlyWyData(true);
    try {
      Calendar wyStart = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      wyStart.set(waterYear-1, 9, 1, 0, 0, 0);
      Date startDt = wyStart.getTime();
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
      
      Calendar wyCur = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);
      Integer curYr = (curMonth >= 10)? waterYear-1: waterYear;
      wyCur.set(curYr, curMonth, 1, 0, 0, 0);
      Date endDt = wyCur.getTime();
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds list is undefined or empty.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.wyData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.wyData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      qry.setParameter("sensorIds", sensorIds);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.wyData] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      for (MonthlyData monthlyData : qryResult) {
        result.add(monthlyData);
      }
    } catch (Exception exp) {
      result.clear();
      logger.log(Level.WARNING, "{0}.getWySnoData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Monthly Snow WC Data for a specified list of Stations and for
   * a specified data. It calls {@linkplain #getSnoSensors(java.util.List) 
   * this.getSnoSensors} to get the associated list of sensorIds for the list of 
   * stationIds. It then calls {@linkplain #getByDate(java.util.List, java.util.Date) 
   * this.getByDate} to retrieve the data.
   * <p><b>NOTE:</b> All errors are trapped an logged and null value is returned</p>
   * @param stationIds the list of StationId to filter on
   * @param actualDt the Actual Date to filter on
   * @return the result - null if an error occurred or not found.
   */
  public HashMap<String, Double> getSnoStasByDate(List<String> stationIds, Date actualDt){
    
    HashMap<String, Double> result = new HashMap<>();
    try {
      HashMap<Integer, Sensor> sensorMap = this.getSnoSensors(stationIds);
      if ((sensorMap == null) || (sensorMap.isEmpty())) {
        throw new Exception("Unable to the Monthly Snow Sensors for the specified list "
                + "of Stations");
      }
      List<Integer> sensorIds = new ArrayList<>(sensorMap.keySet());
      HashMap<Integer, Double> snowData = this.getByDate(sensorIds, actualDt);    
      Sensor sensor = null;
      String staId = null;
      Double obsVal = null;
      if ((snowData != null) && (!snowData.isEmpty())) {
        for (Integer sensorId : snowData.keySet()) {
          if (((obsVal = snowData.get(sensorId)) != null) &&
                  (sensorMap.containsKey(sensorId)) && 
                  ((sensor = sensorMap.get(sensorId)) != null) &&
                  ((staId = sensor.getSensorPK().getStationId()) != null)) {
            result.put(staId, obsVal);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to load the Monthly Data for a specified Sensors between two dates and 
   * calculate the sum of the observed values. 
   * It uses the NamedQuery[MonthlyData.sensorData] .
   * <p><b>NOTE:</b> All errors are trapped an logged and null value is returned</p>
   * @param sensorId the Sensor to filter on
   * @param startDt the Start Date in Date Range
   * @param endDt the End Date in Date Range
   * @return the result - null if an error occurred or not found.
   */
  public Double getSensorSum(Integer sensorId, Date startDt, Date endDt) {
    Double result = null;
    try {
      if (startDt == null) {
        throw new Exception("The Start Date is undefined.");
      }
            
      if (endDt == null) {
        throw new Exception("The End Date is undefined.");
      }
      
      if (sensorId == null) {
        throw new Exception("The Sensor's Id is undefined.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.sensorData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.sensorData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.sensorData] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = 0.0d;
      for (MonthlyData monthlyData : qryResult) {
        Double obsVal = monthlyData.getObsValue();
        if ((obsVal != null) && (!obsVal.equals(Double.NaN))) {
          result += obsVal;
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getWyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
   /**
   * Called to load the Monthly Data for a specified Sensors between two dates
   * and calculate the average of the observed values. 
   * It uses the NamedQuery[MonthlyData.sensorData] .
   * <p><b>NOTE:</b> All errors are trapped an logged and null value is returned</p>
   * @param sensorIds the list of Sensor to filter on
   * @param actualDt the Actual Date to filter on
   * @return the result - null if an error occurred or not found.
   */
  public HashMap<Integer, Double> getByDate(List<Integer> sensorIds, Date actualDt) {
    HashMap<Integer, Double> result = null;
    try {
      if (actualDt == null) {
        throw new Exception("The Actual Date is undefined.");
      }
            
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds are undefined.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("MonthlyData.dataByDate");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[MonthlyData.dataByDate]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorIds", sensorIds);
      qry.setParameter("actualDt", actualDt, TemporalType.DATE);
      
      List<MonthlyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[MonthlyData.dataByDate] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (MonthlyData monthlyData : qryResult) {
        MonthlyDataPK dataPk = monthlyData.getMonthlyDataPK();
        Double obsVal = monthlyData.getObsValue();
        if (obsVal != null) {
          result.put(dataPk.getSensorId(), obsVal);
        }
      }   
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getByDate Error:\n {1}",
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
