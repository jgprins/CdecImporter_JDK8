package gov.ca.water.cdec.core;

import gov.ca.water.cdec.entities.DailyData;
import gov.ca.water.cdec.entities.DailyDataPK;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DailyDataMap extends TimeSeriesMap<DailyData, DateKey> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public DailyDataMap(int sensorId) {
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
   * OVERRIDE: Initiates the DailyDataPK(this.sensorId, actualDt) and use the primaryKey
   * to initiate the DailyData instance.</p>
   */
  @Override
  public DailyData newRecord(Date actualDt) {
    DailyData result = null;
    if (actualDt == null) {
      throw new NullPointerException("The DailyData record's actual date is unassigned.");
    } 
    
    DailyDataPK recPk = new DailyDataPK(this.sensorId, actualDt);
    result = new DailyData(recPk);
    if (result == null) {
      throw new NullPointerException("Initiating the new DailyData record failed.");
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new DailyDataMap instance </p>
   */
  @Override
  public DailyDataMap cloneInstance() {
    return new DailyDataMap(this.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.dailyDataPK.sensorId</p>
   */
  @Override
  public int getRecordSensorId(DailyData record) {
    DailyDataPK pKey = (record == null)? null: record.getDailyDataPK();
    return (pKey == null)? 0: pKey.getSensorId();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.dailyDataPK.actualDate</p>
   */
  @Override
  public Date getRecordActualDt(DailyData record) {
    DailyDataPK pKey = (record == null)? null: record.getDailyDataPK();
    return (pKey == null)? null: pKey.getActualDate();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.obsDate</p>
   */
  @Override
  public Date getRecordObsDt(DailyData record) {
    return (record == null)? null: record.getObsDate();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.obsValue</p>
   */
  @Override
  public Double getRecordObsValue(DailyData record) {
    return (record == null)? null: record.getObsValue();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.dataFlag</p>
   */
  @Override
  public String getRecordDataFlag(DailyData record) { 
    return (record == null)? null: record.getDataFlag();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Update the DailyData's obsDate, obsValue, and dataFlag fields</p>
   */
  @Override
  public void setRecordObsData(DailyData record, Date obsDate, Double obsValue, String dataFlag) {
    if (record != null) {
      record.setObsDate(obsDate);
      record.setObsValue(obsValue);
      record.setDataFlag(dataFlag);
    }
  }
//</editor-fold>
}
