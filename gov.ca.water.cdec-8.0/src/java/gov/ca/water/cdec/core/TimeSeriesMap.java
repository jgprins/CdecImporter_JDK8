package gov.ca.water.cdec.core;

import java.io.Serializable;
import java.util.*;

/**
 * A Base Class for all TimeSeriesMap use in reading Event, Daily, or Monthly Values into
 * a TimeSeries
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeSeriesMap<TEntity extends Serializable, 
                        TStepKey extends TimeStepKey<TStepKey>> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static DateRange Class">
  /**
   * A class to define the TimeSeries DateRange
   */
  public static class DateRange {
    private Date startDate;
    private Date endDate;
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    private DateRange() {
      this.startDate = null;
      this.endDate = null;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Private methods">
    /**
     * Called to add a new Date
     * @param newDate
     */
    private void addDate(Date newDate) {
      if (newDate == null) {
        return;
      }
      
      if (this.startDate == null) {
        this.startDate = newDate;
        this.endDate = newDate;
      } else if (newDate.before(this.startDate)) {
        this.startDate = newDate;
      } else if (newDate.after(this.endDate)) {
        this.endDate = newDate;
      }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Check if the DateRange is empty
     * @return ((this.startDate == null) || (this.endDate == null))
     */
    public boolean isEmpty() {
      return ((this.startDate == null) || (this.endDate == null));
    }
    
    /**
     * Get the Range's Start Date
     * @return this.startDate (null if this.isEmpty)
     */
    public Date getStartDate() {
      return this.startDate;
    }
    
    /**
     * Get the Range's End Date
     * @return this.endDate (null if this.isEmpty)
     */
    public Date getEndDate() {
      return this.endDate;
    }
    //</editor-fold>
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private/Public Fields">
  /**
   * Public final field containing the SensorId
   */
  public final int sensorId;
  /**
   * Placeholder for the Daily data map
   */
  protected LinkedHashMap<TStepKey, TEntity> dataMap;
  /**
   * Placeholder for the TimeSeriesMap dateRange
   */
  private DateRange dateRange;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor with sensorId reference
   * @param sensorId 
   */
  public TimeSeriesMap(int sensorId) {
    this.sensorId = sensorId;
    this.dataMap = new LinkedHashMap<>();
    this.dateRange = new DateRange();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  public DateRange getDateRange() {
    return this.dateRange;
  }
  
  /**
   * Return a Set of DateKey's in the dataMap (in the order entered).
   * @return this.dataMap.keySet()
   */
  public Set<TStepKey> getDateKeys() {
    return this.dataMap.keySet();
  }
  /**
   * Return a Collection of TimeSeries records
   * @return this.dataMap.values()
   */
  public Collection<TEntity> getRecords() {
    return this.dataMap.values();
  }
  
  /**
   * Add a new record to the TiemStepMap. If a record for the date already exist, it  
   * will be overridden.
   * @param record the new record to add. Ignored if null.
   * @exception if the record.dailyDataPK.sensorId does not match this.sensorId.
   */
  public void add(TEntity record) {
    if (record == null) {  
      return;
    }
    if (this.getRecordSensorId(record) != this.sensorId) {
      throw new IllegalArgumentException("The record's SensorId does not match the "
              + this.getClass().getSimpleName() + " TimeSeris Map" );
    }
    Date actDt = this.getRecordActualDt(record);
    if (actDt == null) {
      throw new IllegalArgumentException("The record's actual date is unassigned.");
    }
    TStepKey mapKey = this.toDateKey(actDt);
    if (mapKey == null) {
      throw new IllegalArgumentException("Intiating a DtaKey from Date[" 
              + actDt.toString() + "] failed.");
    }
    this.dataMap.put(mapKey, record);
    this.dateRange.addDate(actDt);
  }

  /**
   * Get the TimeSeries Record for the matching Date.
   * @param TEntity the entity type
   * @param actualDt the date to search for
   * @return the record or null if not found.
   */
  public TEntity getData(Date actualDt) {
    TEntity result = null;
    if (actualDt != null) {
      TStepKey mapKey = this.toDateKey(actualDt);
      if ((mapKey != null) && (this.dataMap.containsKey(mapKey))) {
        result = this.dataMap.get(mapKey);
      }
    }
    return result;
  }
  
  /**
   * Check if the TimeSeries Map is empty
   * @return (this.dataMap = null | empty)
   */
  public boolean isEmpty() {
    return ((this.dataMap == null) || (this.dataMap.isEmpty()));
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Abstract Methods">
  /**
   * ABSTRACT: Called to initiate a new DateKey for the specified actual Date
   * @param rawDate the record's raw Date
   * @return the new record instance
   */
  public abstract TStepKey toDateKey(Date rawDate);
  /**
   * ABSTRACT: Called to initiate a new Record for the specified actual Date
   * @param actualDt the record's actual Date
   * @return the new record instance
   */
  public abstract TEntity newRecord(Date actualDt);
  
  /**
   * ABSTRACT: Called to retrieve the sensorID from the inherited entity
   * @param record the record to retrieve the data from
   * @return record.sensorId or null if record or the date is null
   */
  public abstract int getRecordSensorId(TEntity record);
  
  /**
   * ABSTRACT: Called to retrieve the Actual Date from the inherited entity
   * @param record the record to retrieve the data from
   * @return record.actualDt or null if record or the date is null
   */
  public abstract Date getRecordActualDt(TEntity record);
  
  /**
   * ABSTRACT: Called to retrieve the Observed Date from the inherited entity
   * @param record the record to retrieve the data from
   * @return record.obsDate or null if record or the date is null
   */
  public abstract Date getRecordObsDt(TEntity record);
  
  /**
   * ABSTRACT: Called to retrieve the Observed Value from the inherited entity
   * @param record the record to retrieve the data from
   * @return record.obsValue or null if record or the value is null
   */
  public abstract Double getRecordObsValue(TEntity record);
  
  /**
   * ABSTRACT: Called to retrieve the DataFlag Value from the inherited entity
   * @param record the record to retrieve the data from
   * @return record.dataFalg or null if record or the value is null
   */  
  public abstract String getRecordDataFlag(TEntity record);
  
  /**
   * ABSTRACT: Called to assign the observed data to the inherited entity
   * Set dataFlag = "" if dataFlag = null.
   * @param record the record to update - ignore if null
   * @param obsDate the observed date (can be null)
   * @param obsValue the observed value (can be null)
   * @param dataFlag  the data flag (can be null)
   */
  public abstract void setRecordObsData(TEntity record, Date obsDate, Double obsValue,
                                                                      String dataFlag);
  
  /**
   * Called to get a Clone instance of this TimeSeriesMap
   * @return new instance
   */
  public abstract TimeSeriesMap<TEntity, TStepKey> cloneInstance();
//</editor-fold>
}
