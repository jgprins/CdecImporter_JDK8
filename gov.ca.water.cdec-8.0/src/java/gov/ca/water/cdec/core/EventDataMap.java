package gov.ca.water.cdec.core;

import gov.ca.water.cdec.entities.EventData;
import gov.ca.water.cdec.entities.EventDataPK;
import gov.ca.water.cdec.enums.EventStep;
import java.util.Date;

/**
 * An TimeSeriesMap for storing EventData. 
 * <p>
 * <b>NOTE:</b> It takes a <tt>eventStep</tt> parameter in its constructor, which 
 * specified how the {@linkplain EventStepKey EventStepKey's} Time data is rounded.</p>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class EventDataMap extends TimeSeriesMap<EventData, EventStepKey> {
  
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Map's EvenStep setting - initiate via the constructor
   */
  public final EventStep eventStep;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public EventDataMap(EventStep eventStep, int sensorId) {
    super(sensorId);
    if ((this.eventStep = eventStep) == null) {
      throw new NullPointerException("The EventDataMap's EventStep cannot be unassigned");
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements TimeSeriesMap">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return (rawDate == null)? null: new EventStepKey(rawDate, this.eventStep);
   * </p>
   */
  @Override
  public EventStepKey toDateKey(Date rawDate) {
    return (rawDate == null)? null: new EventStepKey(rawDate, this.eventStep);
  }  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Initiates the EventDataPK(this.sensorId, actualDt) and use the primaryKey
   * to initiate the EventData instance.</p>
   */
  @Override
  public EventData newRecord(Date dateTime) {
    EventData result = null;
    if (dateTime == null) {
      throw new NullPointerException("The EventData record's actual date is unassigned.");
    } 
    
    EventDataPK recPk = new EventDataPK(dateTime, this.sensorId);
    result = new EventData(recPk);
    if (result == null) {
      throw new NullPointerException("Initiating the new EventData record failed.");
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new EventDataMap instance </p>
   */
  @Override
  public EventDataMap cloneInstance() {
    return new EventDataMap( this.eventStep, this.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.eventDataPK.sensorId</p>
   */
  @Override
  public int getRecordSensorId(EventData record) {
    EventDataPK pKey = (record == null)? null: record.getEventDataPK();
    return (pKey == null)? 0: pKey.getSensorId();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.eventDataPK.actualDate</p>
   */
  @Override
  public Date getRecordActualDt(EventData record) {
    EventDataPK pKey = (record == null)? null: record.getEventDataPK();
    return (pKey == null)? null: pKey.getDateTime();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.obsDate</p>
   */
  @Override
  public Date getRecordObsDt(EventData record) {
    return this.getRecordActualDt(record);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.value</p>
   */
  @Override
  public Double getRecordObsValue(EventData record) {
    return (record == null)? null: record.getObsValue();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return record.dataFlag</p>
   */
  @Override
  public String getRecordDataFlag(EventData record) { 
    return (record == null)? null: record.getDataFlag();
  }
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Update the EventData's obsDate, obsValue, and dataFlag fields</p>
   * note: obsDate is ignored
   */
  @Override
  public void setRecordObsData(EventData record, Date obsDate, 
                                                    Double obsValue, String dataFlag) {
    if (record != null) {
      record.setObsValue(obsValue);
      record.setDataFlag(dataFlag);
    }
  }
//</editor-fold>
}
