package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.DailyData;
import java.util.Date;

/**
 * A Data Importer from the DWR CDEC database to a local database.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DailyDataImportProcessor extends TimeSeriesImportProcessor<DailyData, 
                                            DateKey, DailyDataMap, JsonDailyDataParser> {
 
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  DailyDataImportProcessor(String processName, CdecSensorInfo sensorInfo,
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
  protected DailyDataMap onNewTimeSeriesMap() {
    if (this.sensorInfo == null) {
      throw new NullPointerException("The Processoor's SensorInfo is undefined.");
    }
    return new DailyDataMap(this.sensorInfo.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new JsonDailyDataParser instance</p>
   */
  @Override
  protected JsonDailyDataParser onInitDataParser(){
    return new JsonDailyDataParser();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a new clone of this DailyDataImportProcessor</p>
   */
  @Override
  public DailyDataImportProcessor nextTryClone() {
    DailyDataImportProcessor result = new DailyDataImportProcessor(this.processName, 
                                            this.sensorInfo, this.startDt, this.endDt);
    result.setTryCount(this.getTryCount() + 1);
    return result;
  }
  // </editor-fold>
}
