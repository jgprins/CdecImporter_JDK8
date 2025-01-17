package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.MonthlyData; 
import java.util.Date;

/**
 * A Data Importer from the DWR CDEC database to a local database.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class MonthlyDataImportProcessor 
                extends TimeSeriesImportProcessor<MonthlyData, DateKey, MonthlyDataMap,
                                            JsonMonthlyDataParser>{
 
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  MonthlyDataImportProcessor(String processName, CdecSensorInfo sensorInfo,
                             Date startDt, Date endDt) {
    super(processName,sensorInfo,startDt,endDt); 
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object CdecImportProcessor">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return a new MonthlyDataMap instance</p>
   */
  @Override
  protected MonthlyDataMap onNewTimeSeriesMap() {
    if (this.sensorInfo == null) {
      throw new NullPointerException("The Processoor's SensorInfo is undefined.");
    }
    return new MonthlyDataMap(this.sensorInfo.sensorId);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a new JsonMonthlyDataParser instance</p>
   */
  @Override
  protected JsonMonthlyDataParser onInitDataParser() {
    return new JsonMonthlyDataParser();
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a new clone of this MonthlyDataImportProcessor</p>
   */
  @Override
  public MonthlyDataImportProcessor nextTryClone() {
    MonthlyDataImportProcessor result = new MonthlyDataImportProcessor(this.processName, 
                                            this.sensorInfo, this.startDt, this.endDt);
    result.setTryCount(this.getTryCount() + 1);
    return result;
  }
  // </editor-fold>
}
