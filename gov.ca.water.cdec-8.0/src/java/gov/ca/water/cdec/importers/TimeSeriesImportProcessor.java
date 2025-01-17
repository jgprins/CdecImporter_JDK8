package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.enums.DurationCodes;
import gov.ca.water.cdec.facades.TimeSeriesFacade;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeSeriesImportProcessor<TEntity extends Serializable, 
                          TStepKey extends TimeStepKey<TStepKey>,
                          TMap extends TimeSeriesMap<TEntity, TStepKey>,
                          TParser extends TimeSeriesDataParser<TEntity, TStepKey, TMap>> 
                          extends ImportProcessor<TEntity> {
  
  // <editor-fold defaultstate="collapsed" desc="Private/Protected Fields">
  /**
   * Placeholder for ProcessName
   */
  protected final CdecSensorInfo sensorInfo;
  /**
   * Placeholder for Import Start Start 
   */
  protected final Date startDt;
  /**
   * Placeholder for Import Start End 
   */
  protected final Date endDt;
  /**
   * Placeholder for the Downloader's TimeSeriesDataParser
   */
  private TParser dataParser;
  /**
   * The Query for loading the local Data
   */
  private TimeSeriesFacade<TEntity, TStepKey, TMap> localFacade;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TimeSeriesImportProcessor(String processName, CdecSensorInfo sensorInfo,
                             Date startDt, Date endDt) {
    super(processName);  
    try {            
      if (sensorInfo == null) {
        throw new NullPointerException("The SensorInfo cannot be undefined");
      }
      if (startDt == null) {
        throw new NullPointerException("The Start Date cannot be undefined");
      }
      if (endDt == null) {
        throw new NullPointerException("The End Date cannot be undefined");
      }
      
      this.localFacade = null;
      this.sensorInfo = sensorInfo;
      this.startDt = startDt;
      this.endDt = endDt;
      this.dataParser = null;
    } catch (Exception exp) {
      throw new NullPointerException(this.getClass().getSimpleName()
              + ".new Error:\n " + exp.getMessage());
    }
  } 
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   *  Called to initiate a new TimeSeriesMap instance - called {@linkplain
   * #onNewTimeSeriesMap() this.onNewTimeSeriesMap}, which implementation should
   * return a new TimeSeriesMap instance of the generic Entity type
   * @return new TimeSeriesMap instance.
   * @throws Exception if the process fails.
   */
  protected final TMap newTimeSeriesMap() throws Exception {
    TMap result = null;
    try {
      result = this.onNewTimeSeriesMap();
      if (result == null) {
        throw new Exception("Initiating a new TimeSeriesMap failed.");
      }
    } catch (Exception exp) {
      
      logger.log(Level.WARNING, "{0}.newTimeSeriesMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   *  Called to initiate the processor's TimeSeriesDataParser instance - called 
   * {@linkplain #onInitDataParser() this.onInitDataParser}, which implementation 
   * should return a new TimeSeriesDataParser instance of the generic Entity type
   * @return new TimeSeriesDataParser instance.
   * @throws Exception if the process fails.
   */
  protected final TParser getDataParser() throws Exception {
    if (this.dataParser == null) {
      try {
        this.dataParser = this.onInitDataParser();
        if (this.dataParser == null) {
          throw new Exception("Initiating the processor's TimeSeriesDataParser failed.");
        }
      } catch (Exception exp) {

        logger.log(Level.WARNING, "{0}.getDataParser Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.dataParser;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Protected Abstact Methods">
  /**
   * ABSTRACT: Called to initiate a new TimeSeriesMap instance
   * @return new
   */
  protected abstract TMap onNewTimeSeriesMap();
  
  /**
   * ABSTRACT: Called to initiate a the Processor's DataPareser
   * @return a new instance of the applicable parser
   */
  protected abstract TParser onInitDataParser();
  
  /**
   * Called by inheritor to assign the processor's Query for loading local data
   * @param localFacade the local data persistent Facade
   */
  public void initProcessor(TimeSeriesFacade<TEntity, TStepKey, TMap> localFacade) {
    this.localFacade = localFacade;
  }  
  //</editor-fold>
   
  //<editor-fold defaultstate="collapsed" desc="Override ImportProcessor">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Reset this.localFacade</p>
   */
  @Override
  protected void onResetProcessor() {
    this.localFacade = null;
  }
    
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Initiate the URL for the connecting to the external web-based data 
   * source and import the timeseries data. <p>
   * Example URL: http://cdec4gov.water.ca.gov/preciptemp/req/DailyDataServlet?
   * Start=2014-01-01&End=2014-02-01&SensorNums=45&Stations='FOL'</p>
   */
  @Override
  protected void onInitImportUrl(ImportArgs args) {
    try {
      DurationCodes durCode = this.sensorInfo.sensorType.getDurationCode();
      String urlStr = durCode.importUrl;
      if (urlStr == null) {
        throw new Exception("DurationCodes[" + durCode.toString() +"]'s Import Url is "
                + "undefined or not supported.");
      }
      
      Integer sensorNo = this.sensorInfo.sensorType.sensorNo;
      String format = "yyyy-MM-dd";
      switch (durCode) {
        case H:
        case E:
          format += "'T'hh:mm:ss";
          break;
        case M:
          format = "yyyy-MM";
          break;
      }
      
      String startDtStr = ImportUtils.dateToString(this.startDt, format, null);
      String endDtStr =  ImportUtils.dateToString(this.endDt, format, null);
      
      urlStr = ImportUtils.appendToUrl(urlStr, "Start", startDtStr);
      urlStr = ImportUtils.appendToUrl(urlStr, "End", endDtStr);
      urlStr = ImportUtils.appendToUrl(urlStr, "SensorNums", sensorNo.toString());
      urlStr = ImportUtils.appendToUrl(urlStr, "Stations", this.sensorInfo.stationId);
       //       "'" +this.sensorInfo.stationId + "'");
      //urlStr = ImportUtils.appendToUrl(urlStr, "dur_code", durCode.name());
      
      URL importUrl = new URL(urlStr);
      if (importUrl == null) {
        throw new Exception("The CDEC Import Url[" + urlStr + "] is invalid.");
      }
      
      args.setParameter(ImportKeys.IMPORT_URL, importUrl);
    } catch (Exception exp) {
      String errMsg = "onInitImportUrl Error:\n " + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Parse the imported data string into the TimeSeries Entities. It calls the
   * {@linkplain JsonDataParser#parseData(java.lang.String, 
   * gov.ca.water.cdec.core.TimeSeriesMap,
   * gov.ca.water.cdec.core.CdecSensorInfo) JsonDataParser.parseData} method to handle
   * the parsing. If the tiemSeriesMap returns empty (no valid data were loaded), it
   * calls args.setNotFound to set the args.importStatus=NOT_FOUND.</p>
   */
  @Override
  protected final void onParseImportData(ImportArgs args)  {
    try {
      TParser parser = null;
      String dataStr = args.getParameter(ImportKeys.IMPORT_STR, null);
      if (((dataStr = ImportUtils.cleanString(dataStr)) != null) &&
           (!"[]".equals(dataStr))) {
        if ((parser = this.getDataParser()) == null) {
          throw new Exception("The Downloader does not support a Data Parser or "
                  + "initiating the Data Parser failed.");
        }
        
        TMap timeSeriesMap = this.newTimeSeriesMap();
        if (timeSeriesMap == null) {
          throw new Exception("Initiating a New TimeSeriesMap failed.");
        }
        
        if (!parser.parseData(dataStr, timeSeriesMap, this.sensorInfo)) {
          throw new Exception(parser.getParseError());
        }
        
        if (timeSeriesMap.isEmpty()) {
          args.setNotFound();
        } else {
          args.setParameter(ImportKeys.IMPORT_DATA, timeSeriesMap);
        }
      } else {
        args.setNotFound();
      }
    } catch (Exception exp) {
      String errMsg = "onParseImportData Error:\n" + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Get the importMap from <tt>args[IMPORT_DATA]</tt> and call {@linkplain 
   * TimeSeriesFacade#mergeRecords(gov.ca.water.cdec.core.TimeSeriesMap) 
   * this.localFacade.mergeRecords(importMap)}</p>
   */
  @Override
  protected void onMergeData(ImportArgs args) {
    try {
      TMap importMap = args.getParameter(ImportKeys.IMPORT_DATA, null);
      if ((importMap == null) ||(importMap.isEmpty())) {
        throw new Exception("The imported Data Map is no longer accessible.");
      }
      
      if (this.localFacade == null) {
        throw new Exception("The Processor's Local Entity Facade is not accessible.");
      }
      this.localFacade.mergeRecords(importMap);
    } catch (Exception exp) {
      String errMsg = "onMergeData Error:\n " + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }
  //</editor-fold>
}
