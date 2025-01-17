package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.EventData;
import gov.ca.water.cdec.enums.EventStep;
import java.util.Date;

/**
 * A Data Importer from the DWR CDEC database to a local database.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class EventDataImportProcessor extends TimeSeriesImportProcessor<EventData, 
                                       EventStepKey, EventDataMap, JsonEventDataParser> {
 
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  EventDataImportProcessor(String processName, CdecSensorInfo sensorInfo,
                             Date startDt, Date endDt) {
    super(processName,sensorInfo,startDt,endDt); 
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object CdecImportProcessor">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: return a new DailyDataMap instance</p>
   */
  @Override
  protected EventDataMap onNewTimeSeriesMap() {
    if (this.sensorInfo == null) {
      throw new NullPointerException("The Processoor's SensorInfo is undefined.");
    }
    return new EventDataMap(EventStep.MINUTES, this.sensorInfo.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new JsonDailyDataParser instance</p>
   */
  @Override
  protected JsonEventDataParser onInitDataParser(){
    return new JsonEventDataParser();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a new clone of this DailyDataImportProcessor</p>
   */
  @Override
  public EventDataImportProcessor nextTryClone() {
    EventDataImportProcessor result = new EventDataImportProcessor(this.processName, 
                                            this.sensorInfo, this.startDt, this.endDt);
    result.setTryCount(this.getTryCount() + 1);
    return result;
  }
  // </editor-fold>
}
