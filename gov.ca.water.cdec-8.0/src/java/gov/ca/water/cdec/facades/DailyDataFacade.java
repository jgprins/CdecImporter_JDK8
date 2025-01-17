package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[DailyData]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class DailyDataFacade extends TimeSeriesFacade<DailyData, DateKey, DailyDataMap> {
   
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the EntityManager
   */
  @PersistenceContext(unitName = CdecEJBContext.PU_CDEC)
  private EntityManager em;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor
   */
  public DailyDataFacade() {
    super(DailyData.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Called to retrieve the DailyData for a specified sensor between two dates.
   * <p>
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the list of DailyData values or null if an error occurs or no values are 
   * found.
   */
  public List<DailyData> getSensorData(int sensorId, Date startDt, Date endDt) {
    List<DailyData> result = null;
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
      Query qry = myEm.createNamedQuery("DailyData.bySensorAndDate");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.bySensorAndDate]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      
      result = qry.getResultList();
      result = (result == null)? new ArrayList<DailyData>(): result;
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the DailyData for a specified sensor between two dates and
   * assign it to a hashMap with the Actual Dates as the HashMap key.
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the HashMap of DailyData values, an empty map if no data was retrieved, or 
   * null if an error occurs.
   */
  @Override
  public DailyDataMap getSensorDataMap(int sensorId, Date startDt, Date endDt) {
    DailyDataMap result = null;
    try {
      List<DailyData> qryResult = this.getSensorData(sensorId, startDt, endDt);
      if (qryResult != null)  {
        result = new DailyDataMap(sensorId);
        if (!qryResult.isEmpty()) {
          for (DailyData dailyData : qryResult) {
            result.add(dailyData);
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
   * Called to retrieve a Monthly (first of the Month) Snow Sensor readings for a 
   * specified WaterYear for a range of WyMonths between October and the specified
   * Current Month.
   * @param waterYear the specified WaterYear
   * @param curMonth the Current Month
   * @param sensorIds the sensors for which to retrieve the data
   * @return the result set - can be empty is an error occurred.
   */
  public MonthlyWyData getMonthlySnowSensorData(Integer waterYear, Integer curMonth, 
                                                            List<Integer> sensorIds) {
    MonthlyWyData result =  new MonthlyWyData(true);
    try {
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds list is undefined or empty.");
      }
      
      Calendar dtCal = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);      
      DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
      dtFormat.setTimeZone(dtCal.getTimeZone());
      
      int[] months = new int[] {10,11,12,1,2,3,4,5,6,7,8,9};
      List<Date> monthDates = new ArrayList<>();
      Date monDt = null;
      for (int iMon = 0; iMon < 12; iMon++) {
        int wyMonth = months[iMon];
        int dtMon = (wyMonth == 12)? 1: wyMonth+1;
        Integer dtYr = (wyMonth >= 10)? waterYear-1: waterYear;
        dtCal.set(dtYr, wyMonth-1, 1, 0, 0, 0);
        if ((monDt = dtCal.getTime()) == null) {
          throw new Exception("Calender[ yr=" + dtYr + ", month=" + (wyMonth-1) 
                             + ", day=1; ...] is invalid");
        }
        monthDates.add(monDt);
        if (wyMonth == curMonth+1) {
          break;
        }
      }      
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("DailyData.monthlySnoData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.monthlySnoData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("monthlyDates", monthDates);
      qry.setParameter("sensorIds", sensorIds);
      
      List<DailyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[DailyData.monthlySnoData] "
                + "is empty. Query SQL = " + qry.toString());
      }
      
      for (DailyData dailyData : qryResult) {
        if (dailyData != null) {
          MonthlyData monthlyData = new MonthlyData(dailyData);
          result.add(monthlyData);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getMonthlySnowSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
//SELECT s.SENSOR_ID, MIN(s.ACTUAL_DATE) as START_DATE, SUM(s.VALUE) as OBS_VALUE 
//FROM DAILY_DATA s
//WHERE (s.SENSOR_ID in (9594, 9616)) AND 
//  (s.ACTUAL_DATE BETWEEN TO_DATE('01/01/2013','MM/dd/yyyy') AND 
//      TO_DATE('01/31/2013','MM/dd/yyyy'))
//GROUP BY s.SENSOR_ID;
  @SuppressWarnings("unchecked")
  public MonthlyWyData getMonthlyPcpSensorData(Integer waterYear, Integer curMonth, 
                                                            List<Integer> sensorIds) {
    MonthlyWyData result = new MonthlyWyData();
    try {
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("The SensorIds list is undefined or empty.");
      }
      
      Calendar dtCal = Calendar.getInstance(CdecBaseFacade.CdecTimeZone);      
      DateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd");
      dtFormat.setTimeZone(dtCal.getTimeZone());
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("DailyData.monthlyPcpData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.monthlyPcpData]");
      }
      
      int[] months = new int[] {10,11,12,1,2,3,4,5,6,7,8,9};
      for (int iMon = 0; iMon < 12; iMon++) {
        int wyMonth = months[iMon];
        
        Integer dtYr = (wyMonth >= 10)? waterYear-1: waterYear;
        dtCal.set(dtYr, wyMonth-1, 1, 0, 0, 0);
        Date startDt = dtCal.getTime();
        dtCal.set(dtYr, wyMonth, 1, 0, 0, 0);
        dtCal.add(Calendar.DAY_OF_MONTH, -1);
        Date endDt = dtCal.getTime();
        
        /* Asign the Query Parameters */
        qry.setParameter("sensorIds", sensorIds);
        qry.setParameter("startDt", startDt, TemporalType.TIMESTAMP);
        qry.setParameter("endDt", endDt, TemporalType.TIMESTAMP);
                
        List<Object[]> qryResult = qry.getResultList();
        if ((qryResult == null) || (qryResult.isEmpty())) {
          throw new Exception("The result list from NamedQuery[MonthlyData.wyData] is "
                  + "empty. Query SQL = " + qry.toString());
        }

        for (Object[] dailyData : qryResult) {
          Integer sensorId = null;
          Date actualDate = null;          
          if ((dailyData != null) && (dailyData.length >= 3) &&
                  ((sensorId = (Integer) dailyData[0]) != null) &&
                  ((actualDate = (Date) dailyData[1]) != null)) {
            Double obsValue = (Double) dailyData[2];
            MonthlyData monthlyData = new MonthlyData(sensorId, actualDate, obsValue);
            result.add(monthlyData);
          }
        }
        
        if (wyMonth == curMonth) {
          break;
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getMonthlySnowSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the B120 SnwoWater Equivalent records for a specified list if 
   * sensors for the specified Forecast Date. The result a HashMap with key = sensorId
   * and value=DailyDataMap containing three records for the ForecastDt, prevDay
   * (ForecastDt - 1day), and (priorWeek) forecastDt - 7 days.
   * @param fcastDt the Forecast date (required)
   * @param prevDay the day before Forecast date (required)
   * @param priorWeek 7-days before the Forecast date (required)
   * @param sensorIds the List Sensor ID (must a non-empty list)
   * @return the HashMap of DailyData values or null if an error occurs or no values are 
   * found.
   */
  public HashMap<Integer,DailyDataMap> getB120SweData(Date fcastDt, Date prevDay,
                                              Date priorWeek, List<Integer> sensorIds) {
    HashMap<Integer,DailyDataMap> result = null;
    try {
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("SensorIds are undefined.");
      }
      
      if ((fcastDt == null) || (prevDay == null) || (priorWeek == null)) {
        throw new Exception("The Forecast, Prior Day, or Prior Week Date is undefined.");
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("DailyData.b120SweData");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.b120SweData]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorIds", sensorIds);
      qry.setParameter("curDate", fcastDt, TemporalType.DATE);
      qry.setParameter("priorDayDate", prevDay, TemporalType.DATE);
      qry.setParameter("priorWeekDate", priorWeek, TemporalType.DATE);
      
      List<DailyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[DailyData.b120SweData] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (DailyData dailyData : qryResult) {
        DailyDataPK primKey = dailyData.getDailyDataPK();
        Integer sensorId = primKey.getSensorId();
        if (result.containsKey(sensorId)) {
          DailyDataMap dataMap = result.get(sensorId);
          dataMap.add(dailyData);
        } else {
          DailyDataMap dataMap = new DailyDataMap(sensorId);
          dataMap.add(dailyData);
          result.put(sensorId, dataMap);
        }
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the Daily Data records for a specified list if sensors for the a
   * period between <tt>startDt</tt> and <tt>endDt</tt>. The result a HashMap with 
   * key = sensorId and value=DailyDataMap. The date range is inclusive.
   * @param startDt the period's Start date (required and inclusive)
   * @param prevDay the period's End date (required and inclusive)
   * @param sensorIds the List Sensor ID (must a non-empty list)
   * @return the HashMap of DailyData values or null if an error occurs or no values are 
   * found.
   */
  public HashMap<Integer, DailyDataMap> getDailyData(Date startDt, Date endDt,
                                                    List<Integer> sensorIds) {
    HashMap<Integer,DailyDataMap> result = null;
    try {
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("SensorIds are undefined.");
      }
      
      if ((endDt == null) || (startDt == null)) {
        throw new Exception("The search period's Start and/or End Date is undefined.");
      } else if (startDt.after(endDt)) {
        throw new Exception("The Start Date[" + startDt.toString() + "] is after the "
                + "End Date[" + endDt.toString() + "].");
      } 
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("DailyData.bySensorListAndDate");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.bySensorListAndDate]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorIds", sensorIds);
      qry.setParameter("startDt", startDt, TemporalType.DATE);
      qry.setParameter("endDt", endDt, TemporalType.DATE);
      
      logger.log(Level.INFO, "{0}.getSensorData SQL:\n {1}",
              new Object[]{this.getClass().getSimpleName(), qry.toString()});
      
      List<DailyData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from "
                + "NamedQuery[DailyData.bySensorListAndDate] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (DailyData dailyData : qryResult) {
        DailyDataPK primKey = dailyData.getDailyDataPK();
        Integer sensorId = primKey.getSensorId();
        if (result.containsKey(sensorId)) {
          DailyDataMap dataMap = result.get(sensorId);
          dataMap.add(dailyData);
        } else {
          DailyDataMap dataMap = new DailyDataMap(sensorId);
          dataMap.add(dailyData);
          result.put(sensorId, dataMap);
        }
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the Daily Data records by its Primary Key settings :
   * <tt>actualDt</tt> and <tt>sensorId</tt>. 
   * @param actualDt the actual record date (required)
   * @param sensorId the Sensor ID (required)
   * @return the DailyData record if found, else return null. 
   * @throws NullPointerException if <tt>actualDt</tt> or <tt>sensorId</tt>
   */
  public DailyData getDailyData(Date actualDt, Integer sensorId) {
    DailyData result = null;
    if (actualDt == null) {
      throw new NullPointerException("The Query's 'actualDt' is undefined.");
    }
    if (sensorId == null) {
      throw new NullPointerException("The Query's 'sensorId' is undefined.");
    }
    try {      
      EntityManager myEm = this.getEntityManager();
      
      DailyDataPK primKey = new DailyDataPK(sensorId, actualDt);
      result = myEm.find(DailyData.class, primKey);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getDailyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  /**
    * Get the Period-of-Record date range for the specified SensorId.
   * All errors are trapped and logged.
   * @param sensorId the senor ID - must be defined
   * @return the valid range or null if no result is found.
   */
  public DateKeyRange getPorDateRange(Integer sensorId) {
    DateKeyRange result = null;
    try {
      if ((sensorId == null)) {
        throw new Exception("SensorId is undefined.");
      }
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("DailyData.PORDates");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[DailyData.PORDates]");
      }
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      
      logger.log(Level.INFO, "{0}.getSensorData SQL:\n {1}",
              new Object[]{this.getClass().getSimpleName(), qry.toString()});
      
      List<Object[]> qryResult = qry.getResultList();
      Object[] objArr = null;
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[DailyData.PORDates] is "
                + "empty. Query SQL = " + qry.toString());
      } else if ((qryResult.size() > 1) || ((objArr = qryResult.get(0)) == null) ||
                (objArr.length != 2)) {
        throw new Exception("The result list from NamedQuery[DailyData.PORDates] is "
                + "invalid - expected a single array with two values. "
                + "Query SQL = " + qry.toString());
      }
            
      Date dt1 = null;
      Date dt2 = null;
      if ((objArr[0] == null) || (!(objArr[0] instanceof Date)) || 
              ((dt1 = (Date) objArr[0]) == null)) {
        throw new Exception("The Query Result from NamedQuery[DailyData.PORDates]'s "
                + "minimum date is undefined or invalid.");
      } else if ((objArr[1] == null) || (!(objArr[1] instanceof Date)) || 
              ((dt2 = (Date) objArr[1]) == null)) {
        throw new Exception("The Query Result from NamedQuery[DailyData.PORDates]'s "
                + "maximum date is undefined or invalid.");
      }
      result = new DateKeyRange(dt1, dt2);    
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getPorDates Error:\n {1}",
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
