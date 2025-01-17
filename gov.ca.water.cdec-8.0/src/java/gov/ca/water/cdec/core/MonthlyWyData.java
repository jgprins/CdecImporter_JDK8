package gov.ca.water.cdec.core;

import gov.ca.water.cdec.entities.MonthlyData;
import gov.ca.water.cdec.entities.MonthlyDataPK;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

/**
 * A Data Collection that stores the {@linkplain MonthlyData monthly WY-Data} by 
 * SensorId and wyMonth. If (isSnowData), the wyMonth is the month prior to the 
 * MonthlyData.actualData, because the actualDate represents the snow WC at the beginning
 * of the month. For Precip and FNF the data represents the accumulation or flow volume
 * during the month - even if the actual date is the first of the month.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class MonthlyWyData implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Internal Class for Monthly Sensor Data">
  protected class MonthlySensorData extends HashMap<Integer, MonthlyData> {
    
    //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
    /**
     * Public Field for Sensor ID
     */
    public final Integer sensorId;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public MonthlySensorData(Integer sensorId) {
      super();
      this.sensorId = sensorId;
    }

    // </editor-fold>
    
    /**
     * {@inheritDoc}
     * <p>OVERRIDE: Return a shallow clone of the MonthlySensorData</p>
     */
    @Override
    public MonthlySensorData clone() {
      MonthlySensorData result = new MonthlySensorData(this.sensorId);
      for (Integer wyMonth : this.keySet()) {
        MonthlyData data = this.get(wyMonth);
        result.put(wyMonth, data);
      }
      return result;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the MonthyData by sensor and month
   */
  private HashMap<Integer,MonthlySensorData> sensorData;
  /**
   * Placeholder for the isSnowData flag (default= null|false) - use in settings the
   * Data Month based on the MonthlyData.actual_date
   */
  private Boolean isSnoData;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public MonthlyWyData() {
    super();    
    this.sensorData = new HashMap<>();
    this.isSnoData = null;
  }
  
  /**
   * Public Constructor
   */
  public MonthlyWyData(Boolean isSnoData) {
    this();    
    this.isSnoData = ((isSnoData == null) || (!isSnoData))? null: isSnoData;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Called to add a MonthlyData record.
   * @param monthlyData the new record to add
   */
  public void add(MonthlyData monthlyData) {
    if (monthlyData == null) {
      return;
    }
    
    boolean isForSno = ((this.isSnoData != null) && (this.isSnoData));
    
    MonthlyDataPK primKey = monthlyData.getMonthlyDataPK();
    
    Integer sensorId = primKey.getSensorId();
    Calendar actDt = Calendar.getInstance();
    actDt.setTime(primKey.getActualDate());
    Integer month = actDt.get(Calendar.MONTH);
    if (isForSno) {
      month = (month == 0)? 12: month;
    } else {
      month = month+1;
    }
    
    MonthlySensorData dataMap = null;
    if (this.sensorData.containsKey(sensorId)) {
      dataMap = this.sensorData.get(sensorId);
    } else {
      dataMap = new MonthlySensorData(sensorId);
      this.sensorData.put(sensorId, dataMap);
    }    
    dataMap.put(month, monthlyData);
  }
  
  /**
   * Merge this and the other MonthlyWyData be adding the MonthlySensorData of the other 
   * to this MonthlyWyData. If the other MonthlyWyData contains data for a sensor in this
   * MonthlyWyData, the latter will be overridden.
   * @param other the other MonthlyWyData with this MonthlyWyData
   */
  public void merge(MonthlyWyData other) {
    if ((other == null) || (other.isEmpty())) {
      return;
    }
    
    for (Integer sensorId : other.sensorData.keySet()) {
      MonthlySensorData data = other.sensorData.get(sensorId);
      this.sensorData.put(sensorId, data.clone());
    }
  }
  
  /**
   * Get the MonthlyData for a specified SensorId and WyMonth
   * @param sensorId the sensor to search for
   * @param wyMonth the WaterYear month to search for.
   * @return the MonthlyData record or null if not found.
   */
  public MonthlyData get(Integer sensorId, Integer wyMonth) {
    MonthlyData result = null;
    if ((sensorId != null) && (wyMonth != null) &&
            (this.sensorData.containsKey(sensorId))) {
      MonthlySensorData dataMap = this.sensorData.get(sensorId);
      if ((dataMap != null) && (dataMap.containsKey(wyMonth))) {
        result = dataMap.get(wyMonth);
      }
    }
    return result;
  }
  
  /**
   * Get the Map of MonthlyData by Month for a specified SensorId
   * @param sensorId the sensor to search for
   * @return the HashMap[key=month, value=MonthlyData] or null if not found.
   */
  public HashMap<Integer, MonthlyData> get(Integer sensorId) {
    HashMap<Integer, MonthlyData> result = null;
    if ((sensorId != null) && (this.sensorData.containsKey(sensorId))) {
      result = this.sensorData.get(sensorId);
    }
    return result;
  }
  
  /**
   * Check if the MonthlyWyData is empty
   * @return true if MonthlyWyData contains no data.
   */
  public boolean isEmpty() {
    return this.sensorData.isEmpty();
  }
  
  /**
   * Check if the MonthlyWyData for a specified SensorId is empty (no records)
   * @param sensorId the sensor to search for
   * @return true if the MonthlyWyData contains no data for the sensor or the sensorId
   * is null.
   */
  public boolean isEmpty(Integer sensorId) {
    boolean result = true;
    if ((sensorId != null) && (this.sensorData.containsKey(sensorId))) {
      MonthlySensorData dataMap = this.sensorData.get(sensorId);
      result = dataMap.isEmpty();
    }
    return result;
  }
  
  /**
   * Called to clear the MonthlyWyData - delete the data of all sensors.
   */
  public void clear() {
    this.sensorData.clear();
  }
  //</editor-fold>
}
