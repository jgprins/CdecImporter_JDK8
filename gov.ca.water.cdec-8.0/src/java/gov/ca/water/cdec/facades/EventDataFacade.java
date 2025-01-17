package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.EventData;
import gov.ca.water.cdec.entities.EventDataPK;
import gov.ca.water.cdec.enums.EventStep;
import gov.ca.water.cdec.importers.ImportUtils;
import java.util.*;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * <p>A Facade for Entity[EventData]. Above the basic access provided through
 * CdecBaseFacade, this Facade also support the following custom queries:</p><ul>
 * <li></li>
 * </ul>  
 * @author kprins
 */
@Stateless
public class EventDataFacade extends TimeSeriesFacade<EventData, EventStepKey, EventDataMap> {
   
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
  public EventDataFacade() {
    super(EventData.class);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Query Methods">
  /**
   * Called to retrieve the EventData for a specified sensor between two dates.
   * <p>
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0)
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the list of EventData values or null if an error occurs or no values are 
   * found.
   */
  public List<EventData> getSensorData(int sensorId, Date startDt, Date endDt) {
    List<EventData> result = null;
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
      Query qry = myEm.createNamedQuery("EventData.bySensorAndDateRange");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[EventData.bySensorAndDateRange]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      qry.setParameter("startDt", startDt, TemporalType.TIMESTAMP);
      qry.setParameter("endDt", endDt, TemporalType.TIMESTAMP);
      
      result = qry.getResultList();
      result = (result == null)? new ArrayList<EventData>(): result;
    } catch (Exception exp) {
      result = null;
      logger.log(Level.WARNING, "{0}.getSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to retrieve the EventData (for EventStep.MINUTES) for a specified sensor
   * between two dates and assign it to a hashMap with the Actual Dates as the HashMap key.
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param timeStep the EventStep for rounding the Map TimeStepKey's Date-Time data.
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return the HashMap of EventData values, an empty map if no data was retrieved, or 
   * null if an error occurs.
   */
  public EventDataMap getSensorDataMap(EventStep timeStep, int sensorId, 
                                                            Date startDt, Date endDt) {
    EventDataMap result = null;
    timeStep = (timeStep == null)? EventStep.MINUTES: timeStep;
    try {
      List<EventData> qryResult = this.getSensorData(sensorId, startDt, endDt);
      if (qryResult != null)  {
        result = new EventDataMap(timeStep, sensorId);
        if (!qryResult.isEmpty()) {
          for (EventData eventData : qryResult) {
            result.add(eventData);
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
   * Called to retrieve the EventData (for EventStep.MINUTES) for a specified sensor
   * between two dates and assign it to a hashMap with the Actual Dates as the HashMap key.
   * <b>NOTE:</b> This method return an empty list if no results were found and null
   * if an error occur. All errors are trapped and logged,</p>
   * @param sensorId the Sensor ID (must by &ge; 0
   * @param startDt the start date (required)
   * @param endDt the end date (required)
   * @return {@linkplain #getSensorDataMap(gov.ca.water.cdec.enums.EventStep, 
   * int, java.util.Date, java.util.Date) 
   * this.getSensorDataMap(EventStep.MINUTES, sensorId, startDt, endDt)}
   */
  @Override
  public EventDataMap getSensorDataMap(int sensorId, Date startDt, Date endDt) {    
    return this.getSensorDataMap(EventStep.MINUTES, sensorId, startDt, endDt);
  }
    
  /**
   * Called to retrieve the Daily Data records for a specified list if sensors for the a
   * period between <tt>startDt</tt> and <tt>endDt</tt>. The result a HashMap with 
   * key = sensorId and value=EventDataMap(timeStep). The date range is inclusive.
   * @param timeStep the EventStep for rounding the Map TimeStepKey's Date-Time data.
   * @param startDt the period's Start date (required and inclusive)
   * @param prevDay the period's End date (required and inclusive)
   * @param sensorIds the List Sensor ID (must a non-empty list)
   * @return the HashMap of EventData(timeStep) values or null if an error occurs or
   * no values are found.
   */
  public HashMap<Integer, EventDataMap> getEventData(EventStep timeStep, 
                                                    Date startDt, Date endDt,
                                                    List<Integer> sensorIds) {
    timeStep = (timeStep == null)? EventStep.MINUTES: timeStep;
    HashMap<Integer,EventDataMap> result = null;
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
      Query qry = myEm.createNamedQuery("EventData.bySensorListAndDateRange");
      if (qry == null) {
        throw new Exception("Unable to access "
                                  + "NamedQuery[EventData.bySensorListAndDateRange]");
      }
      
      /* Asign the Query Parameters */
      qry.setParameter("sensorIds", sensorIds);
      qry.setParameter("startDt", startDt, TemporalType.TIMESTAMP);
      qry.setParameter("endDt", endDt, TemporalType.TIMESTAMP);
      
      logger.log(Level.INFO, "{0}.getSensorData SQL:\n {1}",
              new Object[]{this.getClass().getSimpleName(), qry.toString()});
      
      List<EventData> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from "
                + "NamedQuery[EventData.bySensorListAndDateRange] is "
                + "empty. Query SQL = " + qry.toString());
      }
      
      result = new HashMap<>();
      for (EventData eventData : qryResult) {
        EventDataPK primKey = eventData.getEventDataPK();
        Integer sensorId = primKey.getSensorId();
        if (result.containsKey(sensorId)) {
          EventDataMap dataMap = result.get(sensorId);
          dataMap.add(eventData);
        } else {
          EventDataMap dataMap = new EventDataMap(timeStep,sensorId);
          dataMap.add(eventData);
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
    * Get the Period-of-Record date range for the specified SensorId.
   * All errors are trapped and logged.
   * @param sensorId the sensor ID - must be defined
   * @return the valid range or null if no result is found.
   */
  public DateKeyRange getPorDateRange(Integer sensorId) {
    DateKeyRange result = null;
    try {
      if ((sensorId == null)) {
        throw new Exception("SensorId is undefined.");
      }
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("EventData.PORDates");
      if (qry == null) {
        throw new Exception("Unable to access NamedQuery[EventData.PORDates]");
      }
      /* Asign the Query Parameters */
      qry.setParameter("sensorId", sensorId);
      
//      logger.log(Level.INFO, "{0}.getPorDateRange SQL:\n {1}",
//              new Object[]{this.getClass().getSimpleName(), qry.toString()});
      
      List<Object[]> qryResult = qry.getResultList();
      Object[] objArr = null;
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from NamedQuery[EventData.PORDates] is "
                + "empty. Query SQL = " + qry.toString());
      } else if ((qryResult.size() > 1) || ((objArr = qryResult.get(0)) == null) ||
                (objArr.length != 2)) {
        throw new Exception("The result list from NamedQuery[EventData.PORDates] is "
                + "invalid - expected a single array with two values. "
                + "Query SQL = " + qry.toString());
      }
            
      Date dt1 = null;
      Date dt2 = null;
      if ((objArr[0] == null) || (!(objArr[0] instanceof Date)) || 
              ((dt1 = (Date) objArr[0]) == null)) {
        throw new Exception("The Query Result from NamedQuery[EventData.PORDates]'s "
                + "minimum date is undefined or invalid.");
      } else if ((objArr[1] == null) || (!(objArr[1] instanceof Date)) || 
              ((dt2 = (Date) objArr[1]) == null)) {
        throw new Exception("The Query Result from NamedQuery[EventData.PORDates]'s "
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
  
  //<editor-fold defaultstate="expanded" desc="Public Submit Methods"> 
  public void submitObsValuesByDate(EventStepKey stepKey, 
                                    HashMap<Integer, ObservedValue> obsValueBySensor) {
    if ((stepKey == null)||
        (obsValueBySensor == null) || (obsValueBySensor.isEmpty())) {
      return;
    }
    
    try {
      List<Integer> sensorIds = new ArrayList<>(obsValueBySensor.keySet());
      if ((sensorIds == null) || (sensorIds.isEmpty())) {
        throw new Exception("Initiating the SensorId list failed.");
      }
      
      Date dtTime = stepKey.getDate();
      if (dtTime == null) {
        throw new Exception("The StepKey's date is invalid or undefined.");
      }
            
      HashMap<EventDataPK, ObservedValue> srcMap = new HashMap<>();
      EventDataPK primKey = null;
      ObservedValue obsVal = null;
      for (Integer sensorId : sensorIds) {
        if ((obsValueBySensor.containsKey(sensorId)) &&
            ((obsVal = obsValueBySensor.get(sensorId)) != null) &&
            ((primKey = new EventDataPK(stepKey, sensorId)) != null)) {
          srcMap.put(primKey, obsVal);
        }
      }
      
      EntityManager myEm = this.getEntityManager();
      Query qry = myEm.createNamedQuery("EventData.bySensorListAndDate");
      if (qry == null) {
        throw new Exception("Unable to access "
                                  + "NamedQuery[EventData.bySensorListAndDate]");
      }
            
      /* Asign the Query Parameters */
      qry.setParameter("sensorIds", sensorIds);
      qry.setParameter("dateTime", dtTime, TemporalType.TIMESTAMP);
            
      HashMap<EventDataPK, EventData> trgMap = new HashMap<>();      
      List<EventData> qryResult = qry.getResultList();
      if ((qryResult != null) && (!qryResult.isEmpty())) {
        for (EventData record : qryResult) {
          trgMap.put(record.getEventDataPK(), record);
        }
      }
      
      this.merge(srcMap, trgMap, 
                      new EntityMergeDelegate<EventDataPK, ObservedValue, EventData>() {
        /**
         * {@inheritDoc}
         * <p>
         * OVERRIDE: Update <tt>trgBean if the obsValues don't match</tt></p>
         */
        @Override
        public boolean updateMerge(ObservedValue srcValue, EventData trgBean) throws Exception {
          boolean result = false;
          Double newVal = (srcValue == null)? null: srcValue.obsValue;
          Double curVal = (trgBean == null)? null: trgBean.getObsValue();
          if ((srcValue != null) && (trgBean != null) && 
              (!ImportUtils.isEq(curVal, newVal))) {
            trgBean.setObsValue(newVal);
            trgBean.setDataFlag("r");
            result = true;
          }
          return result;
        }

        @Override
        public EventData newMerge(EventDataPK srcKey, ObservedValue srcValue) 
                                                                      throws Exception {
          EventData result = null;
          if ((srcKey != null) && (srcValue != null)) {
            result = new EventData(srcKey);
            result.setObsValue(srcValue.obsValue);
            if (srcValue.dataFlag != null) {
              result.setDataFlag(srcValue.dataFlag);
            }
          }
          return result;
        }
      });
      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.submitObsValuesByDate Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="expanded" desc="Implement CdecBaseFacade">  
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
