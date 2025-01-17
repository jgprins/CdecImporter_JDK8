package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.TimeSeriesMap;
import gov.ca.water.cdec.core.TimeStepKey;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeSeriesFacade<TBean extends Serializable, 
                                    TStepKey extends TimeStepKey<TStepKey>,
                                    TMap extends TimeSeriesMap<TBean, TStepKey>>
                                  extends CdecBaseFacade<TBean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public TimeSeriesFacade(Class<TBean> entityClass) {
    super(entityClass);  
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Sync Methods">
  /**
   * Called to do a merge of the records in <tt>mergeMap</tt> with the records in the
   * local database. If the local database contains a record for the primary key, but the
   * obsValues differ, then mergeMap record is used to update the local record. If the
   * record does not exist in local database, it will be inserted. Both the insert and
   * update is done in a batch transaction.
   * @param mergeMap the TimeSeriesMap containing the data to merge
   * @throws Exception if the mergeMap's dataRange is empty or retrieving the local data
   * failed.
   */
  public synchronized void mergeRecords(TMap mergeMap) throws Exception {
    if ((mergeMap == null) || (mergeMap.isEmpty())) {
      return;
    }
    
    TimeSeriesMap.DateRange dtRange = mergeMap.getDateRange();
    if (dtRange.isEmpty()) {
      throw new NullPointerException("The MergeMap's DateRange is empty.");
    }
    
    TMap localData = this.getSensorDataMap(mergeMap.sensorId, dtRange.getStartDate(),
            dtRange.getEndDate());
    if (localData == null) {
      throw new Exception("Retrieving the local data failed. See server log for more "
              + "details.");
    }
    
    TMap updateMap = (TMap) mergeMap.cloneInstance();
    TMap insertMap = (TMap) mergeMap.cloneInstance();
    TBean localRec = null;
    for (TBean importRec : mergeMap.getRecords()) {
      Date recDt = mergeMap.getRecordActualDt(importRec);
      if ((localRec = localData.getData(recDt)) == null) {
        insertMap.add(importRec);
      } else {
        boolean addUpdate = false;
        Double impVal = mergeMap.getRecordObsValue(importRec);
        Double locVal = mergeMap.getRecordObsValue(localRec);
        if (((impVal != null) && (locVal == null)) ||
            ((impVal == null) && (locVal != null)) ||
            ((locVal != null) && (impVal != null) && (!impVal.equals(locVal)))) {
          addUpdate = true;
        } 
        Date impObsDt = mergeMap.getRecordObsDt(importRec);
        Date locObsDt = mergeMap.getRecordObsDt(localRec);
        if (((impObsDt != null) && (locObsDt == null)) ||
          ((impObsDt == null) && (locObsDt != null)) ||
          ((impObsDt != null) && (locObsDt != null) && (!impObsDt.equals(locObsDt)))) {
          updateMap.add(importRec);
        }
        
        if (addUpdate) {
          String dataFlag = mergeMap.getRecordDataFlag(importRec);
          updateMap.setRecordObsData(localRec, impObsDt, impVal, dataFlag);
          updateMap.add(localRec);
        }
      }
    }
    
    if (!updateMap.isEmpty()) {
      this.updateRecords(updateMap);
    }
    
    if (!insertMap.isEmpty()) {
      this.insertRecords(insertMap);
    }
  }
  
  /**
   * Called to update multiple records within a transaction - this process is ThreadSave
   * @param updateMap the TimeSeriesMap containing the records to update.
   * @throws Exception
   */
  public synchronized void updateRecords(TMap updateMap) throws Exception {
    if ((updateMap == null) || (updateMap.isEmpty())) {
      return;
    }
    
    try {
      this.beginTransaction();
      for (TBean TBean : updateMap.getRecords()) {
        this.edit(TBean);
      }
    } catch (Exception exp) {
      this.rollbackTransaction();
      throw new Exception(this.getClass().getSimpleName()
              + ".updateRecord Error:\n " + exp.getMessage());
    } finally {
      this.commitTransaction();
    }
  }
  
  /**
   * Called to insert multiple records within a transaction - this process is ThreadSave
   * @param insertMap the TimeSeriesMap containing the records to insert.
   * @throws Exception
   */
  public synchronized void insertRecords(TMap insertMap) throws Exception {
    if ((insertMap == null) || (insertMap.isEmpty())) {
      return;
    }
    
    try {
      this.beginTransaction();
      for (TBean TBean : insertMap.getRecords()) {
        this.create(TBean);
      }
    } catch (Exception exp) {
      this.rollbackTransaction();
      throw new Exception(this.getClass().getSimpleName()
              + ".insertRecords Error:\n " + exp.getMessage());
    } finally {
      this.commitTransaction();
    }
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Asbtract Methods">
  /**
   * ABSTRACT: Call to load if TimeSeriesMap of type TMap containing the TimeSeries
   * Records for the given time period for a single sensor.
   * @param sensorId the sensor 's sensorId
   * @param startDt the start date
   * @param endDt the end date
   * @return the TimeSeriesMap with the query results
   */
  public abstract TMap getSensorDataMap(int sensorId, Date startDt, Date endDt);
//</editor-fold>
  
}
