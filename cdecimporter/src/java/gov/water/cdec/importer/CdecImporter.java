package gov.water.cdec.importer;

import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.wateryr.WyConverter;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.ca.water.cdec.importers.*;
import gov.water.cdec.importer.controllers.CdecImportController;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A CDEC Data Importer Utility Class called by the {@linkplain CdecImportController} to
 * launch the requested Data Import process.
 * @author J.G. "Koos" Prins, D.Eng. PE. 
 */
public class CdecImporter implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecImporter.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="Static Constants">
  /**
   * The default number of Daily record to import at a given time
   */
  public static final int DefaultImportDays = 60;
  /**
   * The default number of Monthly record to import at a given time
   */
  public static final int DefaultImportMonth = 6;
  /**
   * The default maximum number of import Thread
   */
  public static final int MaxImportThread = 5;
  /**
   * The default maximum number of Download Connection Tries
   */
  public static final int MaxConnectTries = 5;
  /**
   * The Sensors for importing daily data = {{@linkplain CdecSensorTypes#DailyPrecip}, 
   * {@linkplain CdecSensorTypes#DailyPcpAdj}, {@linkplain CdecSensorTypes#DailySnow},
   * {@linkplain CdecSensorTypes#DailyFNF}, {@linkplain CdecSensorTypes#DailyResIn}, 
   * {@linkplain CdecSensorTypes#DailyResOut},{@linkplain CdecSensorTypes#DailyTOC},
   * {@linkplain CdecSensorTypes#DailyResStorage}}.
   */
  public static final CdecSensorTypes[] DailySensors = new CdecSensorTypes[] {
    CdecSensorTypes.DailyPrecip, CdecSensorTypes.DailyPcpAdj, CdecSensorTypes.DailySnow, 
    CdecSensorTypes.DailyFNF, CdecSensorTypes.DailyResIn, CdecSensorTypes.DailyResOut,
    CdecSensorTypes.DailyTOC, CdecSensorTypes.DailyResStorage
  };
  /**
   * The Sensors for importing monthly data = {{@linkplain CdecSensorTypes#MonthlyPrecip}, 
   * {@linkplain CdecSensorTypes#MonthlyPcpAdj}, {@linkplain CdecSensorTypes#MonthlySnow},
   * {@linkplain CdecSensorTypes#MonthlyResStorage}, 
   * {@linkplain CdecSensorTypes#MonthlyFNF}, {@linkplain CdecSensorTypes#LakeLevel}}.
   */
  public static final CdecSensorTypes[] MonthlySensors = new CdecSensorTypes[] {
    CdecSensorTypes.MonthlyPrecip, CdecSensorTypes.MonthlyPcpAdj, 
    CdecSensorTypes.MonthlySnow, 
    CdecSensorTypes.MonthlyResStorage, CdecSensorTypes.MonthlyFNF, 
    CdecSensorTypes.LakeLevel
  };
//</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Static Singleton Access">
  /**
   * Placeholder for the Singleton Instance
   */
  private static CdecImporter _INSTANCE;
  
  /**
   * Get the CdecImporter's Singleton Instance
   */
  public static CdecImporter getInstance() {
    if (CdecImporter._INSTANCE == null) {
      CdecImporter newInst = new CdecImporter();
      newInst.initImporter();
      if (newInst.getImporter() != null) {
        CdecImporter._INSTANCE = newInst;
      }
    }
    return CdecImporter._INSTANCE;
  }
//</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private CdecDataImporter dataImporter;
  private List<CdecImportRequest> requestQueue;
  private List<CdecImportRequest> historyQueue;
  private CdecImportRequest executingRequest;
  // </editor-fold>
  

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  private CdecImporter() {
    super();  
  }
  /**
   * Call after constructing the Class to initiate the services.
   */
  //@PostConstruct
  protected void initImporter() {
    try {
      logger.log(Level.INFO, "{0}.initImporter @ {1}",
        new Object[]{this.getClass().getSimpleName(), Calendar.getInstance().getTime()});     
      this.dataImporter = null;
      this.requestQueue = new ArrayList<>();
      this.historyQueue = new ArrayList<>();
      this.executingRequest = null;     
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.initManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Called before Destroying the instance. 
   * It set DataImportScheduler.mpAppInstance = null.
   */
  //@PreDestroy
  protected void shutdownImporter() {
    try {
      logger.log(Level.INFO, "{0}.shutdownImporter @ {1}",
         new Object[]{this.getClass().getSimpleName(),Calendar.getInstance().getTime()});
      this.disposeImporter();
      this.requestQueue = null;
      this.executingRequest = null;
      this.historyQueue = null;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.shutdownManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method followed by this.shutdownImporter</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.shutdownImporter();
  }
  
  /**
   * Called by the constructor to initiate the Importer as a listener to the
   * DataImporter's StatusChanged and ExecutionCompleted events. These listeners call
   * {@linkplain #onUpdateExecProgress() this.onUpdateExecProgress} and {@linkplain 
   * #onExecutionCompleted() this.onExecutionCompleted}, respectively is the event is
   * fired.
   */
  private void initEventListeners(CdecDataImporter importer) {
    importer.StatusChanged.add(new ImportEventDelegate(this) {
      
      @SuppressWarnings("unchecked")
      @Override
      public void onEvent(Object sender, ImportEventArgs args) {
        CdecImporter listener = (CdecImporter) this.getListener();
        if (listener != null) {
          listener.onUpdateExecProgress();
        }
      }
    });
    
    importer.ExecutionCompleted.add(new ImportEventDelegate(this) {
      
      @SuppressWarnings("unchecked")
      @Override
      public void onEvent(Object sender, ImportEventArgs args) {
        CdecImporter listener = (CdecImporter) this.getListener();
        if (listener != null) {
          listener.onExecutionCompleted();
        }
      }
    });
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called to lazy initiate the CdecDataImporter
   * @return the cached reference
   */
  private CdecDataImporter getImporter() {
    if (this.dataImporter == null) {
      try {
        this.dataImporter = CdecDataImporter.getInstance();
        this.dataImporter.configImporter(true, CdecImporter.MaxImportThread,
                                            CdecImporter.MaxConnectTries);
        this.initEventListeners(this.dataImporter);
      } catch (Exception exp) {
        this.dataImporter = null;
        logger.log(Level.WARNING, "{0}.getImporter Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.dataImporter;
  }
  
  /**
   * Called to clear the event listing to the CdecDataImporter before disposing the
   * reference.
   */
  private void disposeImporter() {
    if (this.dataImporter != null) {
      this.dataImporter.ExecutionCompleted.remove(this);
      this.dataImporter.StatusChanged.remove(this);
    }
    this.dataImporter = null;
  }
  
  /**
   * Get the Default Daily Start date (i.e. {@linkplain #DefaultImportDays 
   * DefaultImportDays} before the specified endDat
   * @param endDate the period's endDate (if null, assume today)
   * @return the start Date
   */
  private Date getDefaultDailyStartDate(Date endDate) {
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    if ((endDate == null) || (endDate.after(today))) {
      endDate = today;
    }
    cal.setTime(endDate);

    int numDays = (-1 * CdecImporter.DefaultImportDays);
    cal.add(Calendar.HOUR, (numDays * 24));
    Date result = cal.getTime();
    return result;
  }
  
  /**
   * Get the Default Daily Start date (i.e. {@linkplain #DefaultImportMonth
   * DefaultImportMonth} before {@linkplain #getFirstOfMonthDate(java.util.Date) 
   * this.getFirstOfMonthDate(endDate)}
   * @param endDate the period's endDate (if null, assume today)
   * @return the start Date
   */
  private Date getDefaultMonthlyStartDate(Date endDate) {
    endDate = this.getFirstOfMonthDate(endDate);
    Calendar cal = Calendar.getInstance();
    cal.setTime(endDate);
    
    int numMonths = (-1 * CdecImporter.DefaultImportMonth);
    cal.add(Calendar.MONTH, numMonths);
    Date result = cal.getTime();
    return result;
  }
  
  /**
   * Get the Default Daily Start date (i.e. {@linkplain #DefaultImportDays 
   * DefaultImportDays} before the specified endDat
   * @param curDate the period's endDate (if null, assume today)
   * @return the start Date
   */
  private Date getFirstOfMonthDate(Date curDate) {
    Calendar cal = Calendar.getInstance();
    Date today = cal.getTime();
    if ((curDate == null) || (curDate.after(today))) {
      curDate = today;
    }
    cal.setTime(curDate);
    
    int year = cal.get(Calendar.YEAR);
    int month = cal.get(Calendar.MONTH);
    
    cal.set(year, month, 1, 0, 0, 0);
    Date result = cal.getTime();
    return result;
  }
  
  /**
   * Called by the this.dataImport.StatusChanged event listener. If 
   * this.executingRequest is accessible, it checked if this.dataImporter.isBusy. If not,
   * it calls {@linkplain #onExecutionCompleted() this.onExecutionCompleted} to handle
   * the event. Otherwise, it set this.executingRequest = (this.dataImporter.progress*100)
   */
  private void onUpdateExecProgress() {
    try {
      this.onExecute();
      
      CdecDataImporter importer = null;
      if ((importer = this.getImporter()) != null) {
        if (!importer.isBusy()) {
          this.onExecutionCompleted();
        } else {
          Long rndProgress = 0l;
          Double progress = importer.progress();
          if (progress != null) {
            rndProgress = Math.round((importer.progress() * 100.0d));
          }
          if(this.executingRequest != null) {
            this.executingRequest.setPercCompleted(rndProgress.intValue());
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onUpdateExecProgress Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called by the this.dataImport.ExecutionCompleted event listener. If 
   * this.executingRequest is accessible, its status will be set to Completed, it will be
   * added to the first item in History Queue and it sets this.executingRequest = null.
   * <b>Finally:</b> it calls {@linkplain #onExecute() this.onExecute} to launch the next
   * request is available.
   */
  private void onExecutionCompleted() {
    try {
      if (this.executingRequest != null) {
        this.executingRequest.completeExecution();
        if (!this.historyQueue.contains(this.executingRequest)) {
          this.historyQueue.add(0, this.executingRequest);
        }
        this.executingRequest = null;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onExecutionCompleted Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.onExecute();
    }
  }
 
  /**
   * Called to add the <tt>request</tt> to the queue and call {@linkplain #onExecute() 
   * this.onExecute} to start the execution process if this.dataImporter is available.
   * @param request 
   */
  private void submitRequest(CdecImportRequest request) {
    try {
      if ((request != null) && (request.isPending())) {
        this.requestQueue.add(request);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.submitRequest Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.onExecute();
    }
  }
  
  /**
   * Called to execute the next available ImprotRequest in the requestQueue. If the
   * request is completed or an error occurred it will be added to the History Queue
   * and another process will be started.
   */
  private void onExecute() {
    CdecDataImporter importer = null;
    if (((importer = this.getImporter()) == null) ||
              (importer.isBusy()) || (this.requestQueue.isEmpty())) {
      if (this.executingRequest == null) {
        
      }
      return;
    }
    
    try {
      //this.executingRequest = null;
      while ((this.executingRequest == null) &&
              (!importer.isBusy()) && (!this.requestQueue.isEmpty())) {
        CdecImportRequest request = this.requestQueue.get(0);
        this.requestQueue.remove(request);

        this.executingRequest = request;
        this.launchRequest(request);
        if ((request.isCompleted()) || (request.isFailed())) {
          if (!this.historyQueue.contains(request)) {
            this.historyQueue.add(0, request);
          }
          this.executingRequest = null;
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onExecute Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to launch the specified request
   * @param request the request to launch
   * @return true if the process was 
   */
  private void launchRequest(CdecImportRequest request) {
    try {
      if (!request.isPending()) {
        throw new Exception("The Import Request is no longer pending");
      }
      CdecDataImporter importer = null;
      if (((importer = this.getImporter()) == null) || (importer.isBusy())) {
        throw new Exception("Cannot launch request because the Data Importer is busy.");
      }
      
      request.startExecution();
      if (request instanceof TimeSeriesImportRequest) {
        TimeSeriesImportRequest timeRequest = (TimeSeriesImportRequest) request;
        if (!importer.importTimeSeriesData(timeRequest.startDate, timeRequest.endDate,
                                                      timeRequest.sensorTypes)) {
          throw new Exception(importer.getErrorMsg());
        } else if (!importer.isBusy()) {
          request.completeExecution();
        }
      } else if (request instanceof StationSensorImportRequest) {
        if (!importer.importStationSensorData()) {
          throw new Exception(importer.getErrorMsg());
        } else if (!importer.isBusy()) {
          request.completeExecution();
        }
      } else if (request instanceof PeriodOfRecordImportRequest) {
        PeriodOfRecordImportRequest porRequest = (PeriodOfRecordImportRequest) request;
        if (!importer.importPeriodOfRecordData(porRequest.sensorId)) {
          throw new Exception(importer.getErrorMsg());
        } else if (!importer.isBusy()) {
          request.completeExecution();
        }
      }
    } catch (Exception exp) {
      request.setError(exp.getMessage());
      logger.log(Level.WARNING, "{0}.launchRequest Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to clear the History Queue
   */
  public void resetHistory() {
    this.historyQueue.clear();
  }
  
  /**
   * Called to stop all execution - it clear the current requestQueue and
   * if this.dataImporter.isBusy, call its {@linkplain CdecDataImporter#stopExecute()
   * stopExecute} method.
   */
  public void stopExecute() {
    this.requestQueue.clear();
    CdecDataImporter importer = null;
    if (((importer = this.getImporter()) != null) && (importer.isBusy())) {
      importer.stopExecute();
    }
  }
  
  /**
   * Called by the service to start the import of all daily data between two dates.
   * @param startDate the start Date - set as {@linkplain #getDefaultDailyStartDate(
   * java.util.Date) this.defaultDailyStartDate} is unassigned.
   * @param endDate the end Date - set as today if unassigned.
   */
  public void importDailyData(Date startDate, Date endDate) {
    try {
      Calendar cal = Calendar.getInstance();
      Date today = cal.getTime();
      if ((endDate == null) || (endDate.after(today))) {
        endDate = today;
      }
      
      if ((startDate == null) || (!startDate.before(endDate))) {
        startDate = this.getDefaultDailyStartDate(endDate);
      }
      
      TimeSeriesImportRequest request = new TimeSeriesImportRequest("Import DailyData", startDate,
                                                    endDate, CdecImporter.DailySensors);
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importDailyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to the service to start the import of all daily data for the period 
   * <tt>numDays</tt> prior to the endDate to the endDate.
   * @param endDate the end Date - set as today if unassigned.
   * @param numDays the number of days {@linkplain #DefaultImportDays DefaultImportDays}
   */
  public void importDailyData(Date endDate, Integer numDays) {
    try {
      Calendar cal = Calendar.getInstance();
      Date today = cal.getTime();
      if ((endDate == null) || (endDate.after(today))) {
        endDate = today;
      }
      cal.setTime(endDate);

      numDays = ((numDays == null) || (numDays == 0))? CdecImporter.DefaultImportDays:
                numDays;
      numDays = (numDays > 0)? -1*numDays: numDays;
      cal.add(Calendar.HOUR, (numDays * 24));
      Date startDate = cal.getTime();
          
      TimeSeriesImportRequest request = new TimeSeriesImportRequest("Import DailyData",
                                    startDate, endDate, CdecImporter.DailySensors);
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importDailyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called by the service to start the import of all daily data between two dates.
   * @param startDate the start Date - set as {@linkplain #getDefaultMonthlyStartDate(
   * java.util.Date) this.defaultStartMonthlyDate} is unassigned.
   * @param endDate the end Date - convert to {@linkplain #getFirstOfMonthDate(
   * java.util.Date) this.getFirstOfMonthDate(endDate}.
   */
  public void importMonthlyData(Date startDate, Date endDate) {
    try {
      endDate = this.getFirstOfMonthDate(endDate);
      
      if ((startDate == null) || 
          ((startDate = this.getFirstOfMonthDate(startDate)) == null) ||
              (!startDate.before(endDate))) {
        startDate = this.getDefaultMonthlyStartDate(endDate);
      }
      
      TimeSeriesImportRequest request = new TimeSeriesImportRequest("Import MonthlyData", 
                                       startDate, endDate, CdecImporter.MonthlySensors);
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importMonthlyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to the service to start the import of all daily data for the period 
   * <tt>numDays</tt> prior to the endDate to the endDate.
   * @param endDate the end Date - set as today if unassigned.
   * @param numDays the number of days {@linkplain #DefaultImportDays DefaultImportDays}
   */
  public void importMonthlyData(Date endDate, Integer numMonths) {
    try {
      Calendar cal = Calendar.getInstance();
      endDate = this.getFirstOfMonthDate(endDate);
      cal.setTime(endDate);
      
      numMonths = ((numMonths == null) || (numMonths == 0))? 
                      CdecImporter.DefaultImportMonth: numMonths;
      numMonths = (numMonths > 0)? -1*numMonths: numMonths;
      cal.add(Calendar.MONTH, numMonths);
      Date startDate = cal.getTime();
          
      TimeSeriesImportRequest request = new TimeSeriesImportRequest("Import MonthlyData", 
                                        startDate, endDate, CdecImporter.MonthlySensors);
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importMonthlyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to the service to start the import of all B120 Forecast Data ending with
   * <tt>endYr</tt> (or this Water Year if endWy = null) going back <tt>numYrs</tt> 
   * (with numYrs = 1 if undefined).
   * @param endYr the end Water Year - set as current Water Year if unassigned.
   * @param numYrs the number of years - assume 1 year if undefined
   */
  public void importB120Data(Integer endYr, Integer numYrs) {
    try {
      Integer incYrs = ((numYrs != null) && (numYrs > 0))? (-1 * (numYrs + 1)): -1;
      Integer curWy = WyConverter.getWaterYear(DateTime.getNow(WyConverter.PstTimeZone));
      DateTime endDt = null;
      if ((endYr == null) || (endYr < 1900) || (endYr > curWy)) {        
        endDt = new DateTime(curWy, 9, 1, WyConverter.PstTimeZone);
      } else {
        endDt = new DateTime(endYr, 9, 1, WyConverter.PstTimeZone);
      }
      DateTime startDt = endDt.addYears(incYrs);          
      TimeSeriesImportRequest request = new TimeSeriesImportRequest("Import B120Data",
                                          startDt.getAsDate(), endDt.getAsDate(), 
                                          CdecSensorTypes.WsFcastSensors());
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importDailyData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to the service to start the import of all daily data for the period 
   * <tt>numDays</tt> prior to the endDate to the endDate.
   * @param endDate the end Date - set as today if unassigned.
   * @param numDays the number of days {@linkplain #DefaultImportDays DefaultImportDays}
   */
  public void importPorData(List<Integer> sensorIds) {
    try {  
      if ((sensorIds != null) && (!sensorIds.isEmpty())) {
        for (Integer sensorId : sensorIds) {
          PeriodOfRecordImportRequest request = new PeriodOfRecordImportRequest(sensorId);
          this.submitRequest(request);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importPorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to the service to start the import of all daily data for the period 
   * <tt>numDays</tt> prior to the endDate to the endDate.
   * @param endDate the end Date - set as today if unassigned.
   * @param numDays the number of days {@linkplain #DefaultImportDays DefaultImportDays}
   */
  public void importStationSensorData() {
    try {     
      StationSensorImportRequest request = new StationSensorImportRequest();
      this.submitRequest(request);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.importStationSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Get the Importer Status as a JSON String
   * @return a JSON string containing three elements: "request", "executing", "history".
   * The first and last elements contains list of {@linkplain 
   * CdecImportRequest#getAsJSON() CdecImportRquest JsonObjects}. The "executing" 
   * contains the CdecImportRquest JsonObjects for the currently executing request.
   */
  public String getImportStatus() {
    String result = null;
    JSONObject statusObj = new JSONObject();
    
//<editor-fold defaultstate="collapsed" desc="For Testing">
//    Calendar cal = Calendar.getInstance();
//    Date dt1 = cal.getTime();
//    cal.add(Calendar.HOUR, (-1*60*24));
//    Date dt2 = cal.getTime();
//
//    CdecImportRequest request = new CdecImportRequest("Import Daily Data",
//            dt2, dt1, CdecImporter.DailySensors);
//    JsonArray queue = new JsonArray();
//    JsonObject reqObj = request.getAsJSON();
//    queue.add(reqObj);
//    statusObj.add("requests", queue);
//
//    request.startExecution();
//    request.setPercCompleted(20);
//    reqObj = request.getAsJSON();
//    statusObj.add("executing", reqObj);
//
//    CdecImportRequest request2 = new CdecImportRequest("Import Daily Data",
//            dt2, dt1, CdecImporter.DailySensors);
//    request2.completeExecution();
//    request.setError("Import failed due to a problem.");
//    queue = new JsonArray();
//    reqObj = request.getAsJSON();
//    queue.add(reqObj);
//    reqObj = request2.getAsJSON();
//    statusObj.add("history", queue);
//</editor-fold>
    
    this.onUpdateExecProgress();
    
    if (((this.requestQueue != null)) && (!this.requestQueue.isEmpty())) {
      JSONArray queue = new JSONArray();
      for (CdecImportRequest request : this.requestQueue) {
        JSONObject reqObj = request.getAsJSON();
        queue.put(reqObj);
      }
      statusObj.put("requests", queue);
    }
    CdecImportRequest execReq = this.executingRequest;
    if (execReq != null) {
      JSONObject reqObj = execReq.getAsJSON();
      statusObj.put("executing", reqObj);
    }
    if (((this.historyQueue != null)) && (!this.historyQueue.isEmpty())) {
      JSONArray queue = new JSONArray();
      for (CdecImportRequest request : this.historyQueue) {
        JSONObject reqObj = request.getAsJSON();
        queue.put(reqObj);
      }
      statusObj.put("history", queue);
    }
    result = ImportUtils.cleanString(statusObj.toString());
    return (result == null)? "[]": result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
}
