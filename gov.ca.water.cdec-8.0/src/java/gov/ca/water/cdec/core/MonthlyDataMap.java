package gov.ca.water.cdec.core;

import gov.ca.water.cdec.entities.MonthlyData;
import gov.ca.water.cdec.entities.MonthlyDataPK;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class MonthlyDataMap extends TimeSeriesMap<MonthlyData, DateKey> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public MonthlyDataMap(int sensorId) {
    super(sensorId); 
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements TimeSeriesMap">   
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (rawDate == null)? null: new DateKey(rawDate)</p>
   */
  @Override
  public DateKey toDateKey(Date rawDate) {
    return (rawDate == null)? null: new DateKey(rawDate);
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Initiates the MonthlyDataPK(this.sensorId, actualDt) and use the primaryKey
   * to initiate the MonthlyData instance.</p>
   */
  @Override
  public MonthlyData newRecord(Date actualDt) {
    MonthlyData result = null;
    if (actualDt == null) {
      throw new NullPointerException("The MonthlyData record's actual date is unassigned.");
    } 
    
    MonthlyDataPK recPk = new MonthlyDataPK(this.sensorId, actualDt);
    result = new MonthlyData(recPk);
    if (result == null) {
      throw new NullPointerException("Initiating the new MonthlyData record failed.");
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new MonthlyDataMap instance </p>
   */
  @Override
  public MonthlyDataMap cloneInstance() {
    return new MonthlyDataMap(this.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.monthlyDataPk.sensorId</p>
   */
  @Override
  public int getRecordSensorId(MonthlyData record) {
    MonthlyDataPK pKey = (record == null)? null: record.getMonthlyDataPK();
    return (pKey == null)? 0: pKey.getSensorId();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.monthlyDataPk.actualDate</p>
   */
  @Override
  public Date getRecordActualDt(MonthlyData record) {
    MonthlyDataPK pKey = (record == null)? null: record.getMonthlyDataPK();
    return (pKey == null)? null: pKey.getActualDate();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.obsDate</p>
   */
  @Override
  public Date getRecordObsDt(MonthlyData record) {
    return (record == null)? null: record.getObsDate();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.obsValue</p>
   */
  @Override
  public Double getRecordObsValue(MonthlyData record) {
    return (record == null)? null: record.getObsValue();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.dataFlag</p>
   */
  @Override
  public String getRecordDataFlag(MonthlyData record) { 
    return (record == null)? null: record.getDataFlag();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Update the MonthlyData's obsDate, obsValue, and dataFlag fields</p>
   */
  @Override
  public void setRecordObsData(MonthlyData record, Date obsDate, Double obsValue, String dataFlag) {
    if (record != null) {
      record.setObsDate(obsDate);
      record.setObsValue(obsValue);
      record.setDataFlag(dataFlag);
    }
  }
//</editor-fold>
}
