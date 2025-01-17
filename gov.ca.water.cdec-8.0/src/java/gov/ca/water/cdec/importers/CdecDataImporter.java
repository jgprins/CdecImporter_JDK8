package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.CdecSensorInfo;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.ca.water.cdec.entities.*;
import gov.ca.water.cdec.enums.DurationCodes;
import gov.ca.water.cdec.facades.*;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A singleton class for managing the importing of data from CDEC to a local CDEC 
 * database. This is an application level class the runs the import processes on separate
 * threads, which is not depended on a Web application session.
 * <p>It import the data once sensor at a time and run each sensor's import process on
 * a separate thread and can run up to 10 threads simultaneous. For more information
 * see documentation for the {@linkplain #configImporter(java.lang.Boolean, 
 * java.lang.Integer) configImporter(autostartExec, maxNumThreads}, {@linkplain 
 * #importTimeSeriesData(java.util.Date, java.util.Date, 
 * gov.ca.water.cdec.core.CdecSensorInfo...) importTimeSeriesData(startDt, endDt, 
 * sensorInfos)}, and {@linkplain #importTimeSeriesData(java.util.Date, java.util.Date,
 * gov.ca.water.cdec.core.CdecSensorTypes...) importTimeSeriesData(startDt, endDt, 
 * sensorInfos} methods.
 * <p>
 * <b>NOTE:</b> This import process should not be run if connected directly to the DWR's
 * CDEC database</p>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class CdecDataImporter implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecDataImporter.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="CdecDataImporter Singleton">
  private static CdecDataImporter INSTANCE = null;

  /**
   * Static method for accessing the Singleton
   * @return CdecDataImporterHolder.INSTANCE
   */
  public static CdecDataImporter getInstance() {
    if (CdecDataImporter.INSTANCE == null) {
      CdecDataImporter.INSTANCE = new CdecDataImporter();
    }
    return CdecDataImporter.INSTANCE;
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private StartNextExecThread Class">
  /**
   * A Runnable to fire the
   */
  private class StartNextExecThread implements Runnable {
    
    private Thread nextThread;
    private Thread priorThread;

    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public StartNextExecThread(Thread priorThread, Thread nextThread) {
      this.priorThread = priorThread;
      this.nextThread = nextThread;
    }
    // </editor-fold>

    @Override
    public void run() {
      if ((this.nextThread == null) && (this.priorThread == null)) {
        return;
      }
      
      String priorThreadId = null;
      try {
        if ((this.priorThread != null) && (this.priorThread.isAlive())) {
          this.priorThread.join(100);
        }
      } catch (InterruptedException ex) {
      } finally {
        if (this.nextThread != null) {
          this.nextThread.start();
        }
      }
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields/Methods">
  /**
   * The Url to request the SHEF formatted data
   */
  public static String CDECShefUrl = "https://cdec.water.ca.gov/cgi-progs/querySHEF";
  
  /**
   * Get the Sensor's ImportProcessor Name
   * @param sensor the sensor's SensorInfo
   * @return "Import Station[" + sensor.stationId + "].Sensor["
   * + sensor.sensorType.acronym + "]"
   */
  public static String getProcessName(CdecSensorInfo sensor) {
    if (sensor == null) {
      throw new NullPointerException("The SensorInfo is undefined");
    }
    return "Import Station[" + sensor.stationId + "].Sensor[" 
                            + sensor.sensorType.acronym + "]";
  }
//</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The EJB Injected reference to CdecEJBContext
   */
  CdecEJBContext cdecCtx;
  /**
   * Placeholder for the ImportProcess event logger
   */
  private CdecImportLogger importLogger;
  /**
   * Placeholder for the SensorDefFacade Entity's Local CDEC Facade
   */
  private SensorDefFacade sensorDefFacade;
  /**
   * Placeholder for the StationFacade Entity's Local CDEC Facade
   */
  private StationFacade stationFacade;
  /**
   * Placeholder for the SensorFacade Entity's Local CDEC Facade
   */
  private SensorFacade sensorFacade;
  /**
   * Placeholder for the DailyDataFacade Entity's Local CDEC Facade
   */
  private DailyDataFacade dailyFacade;
  /**
   * Placeholder for the MonthlyDataFacade Entity's Local CDEC Facade
   */
  private MonthlyDataFacade monthlyFacade;  
  /**
   * Placeholder for the EventDataFacade Entity's Local CDEC Facade
   */
  private EventDataFacade eventFacade;  
  /**
   * The Queue of Import Processes to execute.
   */
  private List<ImportProcessor> processingQueue;
  /**
   * A list of all executing Threads.
   */
  private LinkedHashMap<String, Thread> exectingThreads;
  /**
   * The time when the first process was completed
   */
  private Date startTime;
  /**
   * The time when the last process was completed
   */
  private Date endTime;
  /**
   * The Number of Import Processes submitted for processing
   */
  private Integer numProcesses;
  /**
   * The Number of Import Processes completed
   */
  private Integer numCompleted;
  /**
   * The Number of Import Processes that failed
   */
  private Integer numErrors;
  /**
   * The maximum Number of threads that can run at a given time (default = 1);
   */
  private Integer maxThreadCount;
  /**
   * The maximum Number retries allowed before a import process failed (default = 10);
   */
  private Integer maxTryCount;
  /**
   * Flag indicating whether the processing of import should start as they are added.
   * - no need to call startExecute (default = false|manual start)
   */
  private Boolean autoStartExec;
  /**
   * The last ImportDay that the Station and Sensor tables were synchronized.
   */
  private ImportDay sensorUpdateDay;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Event Handlers">
  /**
   * The EventHandler that fires the Execution Completed Event.
   */
  public final ImportEventHandler ExecutionCompleted;
  
  /**
   * Called to fire the Execution Completed Event.
   * @param args the event arguments
   */
  protected final void fireExecutionCompleted(ImportEventArgs args) {
    this.ExecutionCompleted.fireEvent(null, args);
  }
  
  /**
   * The EventHandler that fires the Status Changed Event - it is fired every time a
   * process execution is completed.
   */
  public final ImportEventHandler StatusChanged;
  
  /**
   * Called to fire the Status Changed Event.   *
   * @param eventInfo the event info
   */
  protected final void fireStatusChanged(ImportEventArgs eventInfo) {
    this.StatusChanged.fireEvent(this, eventInfo);
  }
//</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  private CdecDataImporter()  {
    super();  
    try {
      if (cdecCtx == null) {
        this.cdecCtx = CdecEJBContext.getInstance();
      }
      
      this.ExecutionCompleted = new ImportEventHandler();
      this.StatusChanged = new ImportEventHandler();
      
      this.sensorDefFacade = cdecCtx.getFacade(SensorDefFacade.class);
      if (this.sensorDefFacade == null) {
        throw new Exception("Unable to access the SensorDef Facade");
      }
      this.sensorFacade = cdecCtx.getFacade(SensorFacade.class);
      if (this.sensorFacade == null) {
        throw new Exception("Unable to access the Sensor Facade");
      }
      
      this.stationFacade = cdecCtx.getFacade(StationFacade.class);
      if (this.stationFacade == null) {
        throw new Exception("Unable to access the Station Facade");
      }
      
      this.dailyFacade = cdecCtx.getFacade(DailyDataFacade.class);
      if (this.dailyFacade == null) {
        throw new Exception("Unable to access the DailyData Facade");
      }
      
      this.monthlyFacade = cdecCtx.getFacade(MonthlyDataFacade.class);
      if (this.monthlyFacade == null) {
        throw new Exception("Unable to access the MonthlyData Facade");
      }
      
      this.eventFacade = cdecCtx.getFacade(EventDataFacade.class);
      if (this.eventFacade == null) {
        throw new Exception("Unable to access the EventDataFacade Facade");
      }
      
      this.processingQueue = new ArrayList<>();
      this.exectingThreads = new LinkedHashMap<>();
      this.resetProgress();
      this.autoStartExec = null;
      this.maxThreadCount = null;
      this.importLogger = new CdecImportLogger();
      this.sensorUpdateDay = null;
      this.doTrustToCertificates();
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".new Error:\n " + exp.getMessage());
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  private void doTrustToCertificates() throws Exception {
//    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
//    TrustManager[] trustAllCerts = new TrustManager[]{
//      new X509TrustManager() {
//        @Override
//        public X509Certificate[] getAcceptedIssuers() {
//          return null;
//        }
//
//        @Override
//        public void checkServerTrusted(X509Certificate[] certs, String authType) 
//                                                          throws CertificateException {
//        }
//
//        @Override
//        public void checkClientTrusted(X509Certificate[] certs, String authType) 
//                                                        throws CertificateException {
//        }
//      }
//    };
//
//    SSLContext sc = SSLContext.getInstance("SSL");
//    sc.init(null, trustAllCerts, new SecureRandom());
//    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
//    HostnameVerifier hv = new HostnameVerifier() {
//      @Override
//      public boolean verify(String urlHostName, SSLSession session) {
//        if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
//            System.out.println("Warning: URL host '" + urlHostName + 
//                    "' is different to SSLSession host '" + session.getPeerHost() + "'.");
//        }
//        return true;
//      }
//    };
//    HttpsURLConnection.setDefaultHostnameVerifier(hv);
  }
  /**
   * Called to import the CDEC DailyData for a specified  sensor
   * @param startDt the start date
   * @param endDt the end date
   * @param sensor the Sensor Information
   * @return the TimeSeriesMap with the imported data.
   */
  private boolean addImportProcess(CdecSensorInfo sensor, Date startDt, Date endDt) {
    boolean result = false;
    String procName = null;
    try {
      DurationCodes durCode = null;
      TimeSeriesImportProcessor processor = null;
      if ((sensor != null) && ((durCode = sensor.sensorType.getDurationCode()) != null) &&
              ((procName = CdecDataImporter.getProcessName(sensor)) != null)) {
        if (DurationCodes.D.equals(durCode)) {
          processor = new DailyDataImportProcessor(procName, sensor, startDt, endDt);
          if (processor == null) {
            throw new Exception("Initiating the DailyData ImportProcessor for '" +
                    procName + "' failed.");
          }
        } else if (DurationCodes.M.equals(durCode)) {
          processor = new MonthlyDataImportProcessor(procName, sensor, startDt, endDt);
          if (processor == null) {
            throw new Exception("Initiating the MonthlyData ImportProcessor for '" +
                    procName + "' failed.");
          }
        } else if (DurationCodes.E.equals(durCode)) {
          processor = new EventDataImportProcessor(procName, sensor, startDt, endDt);
          if (processor == null) {
            throw new Exception("Initiating the EventData ImportProcessor for '" +
                    procName + "' failed.");
          }
        } else {
          throw new Exception("An ImportProcessor for sensorType[" +
                    sensor.sensorType.acronym + "] is not currently supported.");
        }         
        result = this.addImportProcess(processor);
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.addImportProcess[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(),procName, exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to import the CDEC DailyData for a specified  sensor
   * @param startDt the start date
   * @param endDt the end date
   * @param sensor the Sensor Information
   * @return the TimeSeriesMap with the imported data.
   */
  private boolean addImportProcess(ImportProcessor processor) {
    boolean result = false;
    String procName = (processor != null)? processor.getProcessName(): "Unknown";
    try {
      if (processor != null) {          
        this.processingQueue.add(processor);
        this.numProcesses++;
        
        if (this.doAutoStartExec()) {
          this.onExcuteProcesses(null);
        }
        result = true;
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.addImportProcess[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(),procName, exp.getMessage()});
    }
    return result;
  }
    
  /**
   * Called by the eventHandler when the ImportProcess starts
   * @param processor the ImportProcessor that ended
   */
  private synchronized void startImportProcess(ImportProcessor processor) {
//    if (processor != null) {
//      String processName = processor.getProcessName();
//      
//    }
  }
      
  /**
   * Called by the eventHandler when the ImportProcess ended
   * @param processor the ImportProcessor that ended
   */
  private synchronized void endImportProcess(ImportProcessor processor) {
    if (processor == null) {
      return;
    }
    Thread execThread = null;
    String processId = processor.processId;
    String processName = processor.getProcessName();
    try {
      ImportStatus status = processor.getImportStatus();
      
      if (this.exectingThreads.containsKey(processId)) {
        execThread = this.exectingThreads.remove(processId);
        if ((this.endTime == null) && (this.processingQueue.isEmpty()) &&
                (this.exectingThreads.isEmpty())) {
          this.endTime = Calendar.getInstance().getTime();
        }
      }
      
      if (!ImportStatus.RETRY.equals(status)) {
        this.numCompleted++;
      }
      if (ImportStatus.ERROR.equals(status)) {
        this.numErrors++;
      }
      
      this.fireStatusChanged(new ImportEventArgs());
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.endImportProcess[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), processName, 
                exp.getMessage()});
    } finally {
      this.onExcuteProcesses(execThread);
    }
  }
        
  /**
   * Called by the eventHandler when the ImportProcess fire its ProcessRetry event.
   * If initiates a {@linkplain ImportProcessor#nextTryClone() nextTryClone} instance
   * of the processor, add it to the processingQueue and call onExecuteProcess.
   * @param processor the ImportProcessor that ended
   */
  private synchronized void retryImportProcess(ImportProcessor processor) {
    if (processor == null) {
      return;
    }
    String processName = processor.getProcessName();
    try {
      if (processor.getTryCount() >= this.getMaxTryCount()) {
        this.numCompleted++;
        throw new Exception("The Import Process retried out.");
      }
    
      ImportProcessor cloneProc = processor.nextTryClone();
      this.processingQueue.add(cloneProc);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.retryImportProcess[{1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), processName, 
                exp.getMessage()});
    }
  }
  
  /**
   * Called to launch up to this.maxNumThreads threads, adding the threads to 
   * this.exectingThreads and remove it from this.processingQueue. It also assign
   * this as the listener to the processor's ProcessStart and ProcessEnd events and
   * this.importLogger as an event listener to the processors LogMessage event.
   */
  private void onExcuteProcesses(Thread priorThread) {
    try {
      while ((this.exectingThreads.size() < this.getMaxNumThreads()) &&
              (!this.processingQueue.isEmpty())) {
        ImportProcessor processor = this.processingQueue.get(0);
        /**
         * Assign the ProcessStart eventHandler
         */
        processor.ProcessStart.add(new ImportEventDelegate(this) {
          
          @Override
          public void onEvent(Object sender, ImportEventArgs args) {
            CdecDataImporter listener = (CdecDataImporter) this.getListener();
            if ((listener != null) && 
                    (sender != null) && (sender instanceof ImportProcessor)) {
              ImportProcessor processor = (ImportProcessor) sender;
              listener.startImportProcess(processor);
            }
          }
        });
        
        /**
         * Assign the ProcessEnd eventHandler
         */
        processor.ProcessEnd.add(new ImportEventDelegate(this) {
          
          @Override
          public void onEvent(Object sender, ImportEventArgs args) {
            CdecDataImporter listener = (CdecDataImporter) this.getListener();
            if ((listener != null) && 
                    (sender != null) && (sender instanceof ImportProcessor)) {
              ImportProcessor processor = (ImportProcessor) sender;
              listener.endImportProcess(processor);
            }
          }
        });
        
        /**
         * Assign the ProcessRetry eventHandler
         */
        processor.ProcessRetry.add(new ImportEventDelegate(this) {
          
          @Override
          public void onEvent(Object sender, ImportEventArgs args) {
            CdecDataImporter listener = (CdecDataImporter) this.getListener();
            if ((listener != null) && 
                    (sender != null) && (sender instanceof ImportProcessor)) {
              ImportProcessor processor = (ImportProcessor) sender;
              listener.retryImportProcess(processor);
            }
          }
        });
        
        /**
         * Assign the importLogegr as an eventListener
         */
        this.importLogger.addAsLogEventListener(processor);
        
        if (processor instanceof StationImportProcessor) {
          StationImportProcessor staProcess = (StationImportProcessor) processor;
          staProcess.setFacade(this.stationFacade);
        } else if (processor instanceof SensorImportProcessor) {
          SensorImportProcessor senProcess = (SensorImportProcessor) processor;
          senProcess.setFacade(this.sensorFacade);
        } else if (processor instanceof SensorDefImportProcessor) {
          SensorDefImportProcessor senDefProcess = (SensorDefImportProcessor) processor;
          senDefProcess.setFacade(this.sensorDefFacade);
        } else if (processor instanceof TimeSeriesImportProcessor) {
          TimeSeriesImportProcessor timeProcessor = (TimeSeriesImportProcessor) processor;
          if (DurationCodes.D.equals(timeProcessor.sensorInfo.sensorType.getDurationCode())) {
            timeProcessor.initProcessor(this.dailyFacade);
          } else if (DurationCodes.M.equals(timeProcessor.sensorInfo.sensorType.getDurationCode())) {
            timeProcessor.initProcessor(this.monthlyFacade);
          } else if (DurationCodes.E.equals(timeProcessor.sensorInfo.sensorType.getDurationCode())) {
            timeProcessor.initProcessor(this.eventFacade);
          } else {
            throw new Exception("A CdecFacade for SensorType[" 
                    + timeProcessor.sensorInfo.sensorType.acronym + "] is not yet supported.");
          }
        } else {
          throw new Exception("A Processor[" 
                    + processor.getClass().getSimpleName() + "] is not yet supported.");
        }               
        
        Thread procThread = new Thread(processor, processor.processId);
        this.exectingThreads.put(processor.processId, procThread);
        this.processingQueue.remove(processor);
        
        if (this.startTime == null) {
          this.startTime = Calendar.getInstance().getTime();
        }
        
        StartNextExecThread startNext = new StartNextExecThread(priorThread, procThread);
        Thread startNextThread = new Thread(startNext);
        startNextThread.start();
        if (startNextThread.isAlive()) {
          startNextThread.join(100);
        }
        priorThread = null;
      }
      
      if ((priorThread != null) && (priorThread.isAlive())) {
        priorThread.join(100);
      }
      
      if (!this.isBusy()) {
        this.fireExecutionCompleted(new ImportEventArgs());
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onExcuteProcesses Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called internally to reset the progress indicators.
   */
  private void resetProgress() {
    this.numProcesses = 0;
    this.numCompleted = 0;
    this.numErrors = 0;
    this.startTime = null;
    this.endTime = null;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Error Message Handling">
  /**
   * Placeholder of an error message during execution
   */
  private String errorMsg;

  /**
   * Get whether an error has been reported
   * @return (this.errorMsg != null)
   */
  public boolean hasError() {
    return (this.errorMsg != null);
  }

  /**
   * Clear the current error message
   */
  public void clearError() {
    this.errorMsg = null;
  }

  /**
   * get the current Error Message
   * @return this.errorMsg (can be null)
   */
  public String getErrorMsg() {
    return this.errorMsg;
  }

  /**
   * Set an error message. If this.errMsg != null, the new error message will be appended
   * separated with a ";\n " delimiter. The call is ignored if the new message = ""|null.
   * @param errMsg new error message
   */
  protected void setErrorMsg(String errMsg) {
    errMsg = ImportUtils.cleanString(errMsg);
    if (errMsg != null) {
      if (this.errorMsg == null) {
        this.errorMsg = errMsg;
      } else {
        this.errorMsg += ";\n " + errMsg;
      }
    }
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether the autoStartExec flag is set (default=false)
   * @return the assigned value
   */
  public boolean doAutoStartExec() {
    return ((this.autoStartExec != null) && (this.autoStartExec));
  }
  
  /**
   * Get the maximum allowed Thread to execute at any given time (default = 1)
   * @return the assigned value
   */
  public int getMaxNumThreads() {
    return (this.maxThreadCount == null)? 1: this.maxThreadCount;
  }
  
  /**
   * Get the maximum of retries allowed before a process fails (default = 10).
   * Retries are only when the server connection failed.
   * @return the assigned value
   */
  public int getMaxTryCount() {
    return (this.maxTryCount == null)? 10: this.maxTryCount;
  }
  
  /**
   * Get the number of threads currently executing
   * @return this.exectingThreads.size
   */
  public int getNumExecutingThreads() {
    return (this.exectingThreads.size());
  }
  
  /**
   * Call to configure the data imported
   * @param autoStartExec set the autoStart (true=start automatically as adding
   * processes; false=start manually by calling startExecute) (default = false|null).
   * @param maxNumThreads the maximum number of threads (1..10) - default is 1.
   * Assume default if null| &le; 0; assume 10 if &gt; 10.
   * @param maxTryCount the maximum number of retries (on server connection failure) 
   * allowed before the process fails (default = 10).
   */
  public void configImporter(Boolean autoStartExec, Integer maxNumThreads, 
                                                    Integer maxTryCount) {
    this.autoStartExec = 
              ((autoStartExec == null) || (!autoStartExec))? null: autoStartExec;
    this.maxThreadCount = 
            ((maxNumThreads == null) || (maxNumThreads <= 0))? null: 
            ((maxNumThreads > 10)? 10: maxNumThreads);
    if ((maxTryCount != null) && (maxTryCount >= 0)) {
      this.maxTryCount = maxTryCount;
    }
  }
  
  /**
   * Overload 1: Called to initiates the importing process. It retrieves all sensors for
   * the specified <tt>sensorTypes</tt>, and call {@linkplain #addImportProcess(
   * gov.ca.water.cdec.core.CdecSensorInfo, java.util.Date, java.util.Date) 
   * this.addImportProcess} to initiate a {@link TimeSeriesImportProcessor} and add it to 
   * this.processingQueue. If {@linkplain #doAutoStartExec() this.doAutoStartExec} = true,
   * it will automatically launching the execution process. Otherwise, this call must
   * be followed by a call to {@linkplain #startExecute() this.startExecute} to launch 
   * the import process.
   * <p>
   * <b>NOTE:</b> The process will fail if a prior execution is in progress.
   * @param startDt the period of interest start date (inclusive)
   * @param endDt the period of interest end date (inclusive)
   * @param sensorTypes and array of sensor type for which to import data
   * @return (!this.hasError)
   */
  public boolean importTimeSeriesData(Date startDt, Date endDt,
                                                    CdecSensorTypes...sensorTypes) {
    this.clearError();
    try {
      if (this.isExecuting()) {
        throw new Exception("The Importer is busy with a prior data import request.");
      }
      this.resetProgress();
      
      /**
       * Check if the sendorTypes are defined.
       */
      if ((sensorTypes == null) || (sensorTypes.length == 0)) {
        throw new Exception("The SensorTypes for which to import the data from CDEC are "
                + "not specified.");
      }
      
      /**
       * Check if the start and end dates are defined.
       */
      if ((startDt == null) || (endDt == null)) {
        throw new Exception("Either or both the start and end dates are undefined.");
      }
      
      /**
       * Check the dates and switch if the the endDt is before the start date.
       */
      if (endDt.before(startDt)) {
        Date tmpDt = endDt;
        endDt = startDt;
        startDt = tmpDt;
      }
      
      if (this.sensorFacade == null) {
        throw new Exception("Unable to access the CDEC SensorFacade for retrieving the "
                + "information of the sensor's to import data for.");
      }
      
      for (CdecSensorTypes senType : sensorTypes) {        
        int sensorNum = senType.sensorNo;
        DurationCodes durCode = senType.getDurationCode();
        List<Sensor> sensorList = 
                            this.sensorFacade.getSensorsBySensorType(sensorNum, durCode);
        if ((sensorList == null) || (sensorList.isEmpty())) {
          continue;
        }
        
        logger.log(Level.INFO, "ImportTimeSeriesData: Start import for "
                + "SensorType[{0}]; Sensor Count = {1}",
              new Object[]{senType.acronym, sensorList.size()});
        Station senSta = null;
        SensorPK senPk = null; 
        CdecSensorInfo sensorinfo = null;
        int impCnt = 0;
        for (Sensor sensor : sensorList) {
          if ((senPk = sensor.getSensorPK()) == null) {
            logger.log(Level.WARNING, "ImportTimeSeriesData: Sensor[{0}]'s "
                + "Primary Key is not accessible",
              new Object[]{sensor.getSensorId()});
          
          } else if ((senSta = senPk.getStation()) == null) {
            logger.log(Level.WARNING, "ImportTimeSeriesData: Sensor[{0}]."
                + "Station[{1}] Error: Station Record is not accessible",
              new Object[]{sensor.getSensorId(),senPk.getStationId()});
          } else if ((sensorinfo = new CdecSensorInfo(sensor.getSensorId(), senType, 
                             senPk.getStationId(), senSta.getBasinNum())) == null) {
            logger.log(Level.WARNING, "ImportTimeSeriesData: Unable to initiate the "
                    + "sensorInfo for {0}",
              new Object[]{senPk.toString()});
          } else {  
            Date senEndDt = sensor.getEndDate();
            if ((senEndDt == null) || (!senEndDt.before(startDt))){
              this.addImportProcess(sensorinfo, startDt, endDt);
              impCnt++;
            }
          }
        }
        logger.log(Level.INFO, "ImportTimeSeriesData: Completed imports for "
                + "SensorType[{0}]; Sensor Count = {1}; Queued Count = {2}.",
              new Object[]{senType.acronym, sensorList.size(), impCnt});
      }      
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      logger.log(Level.WARNING, "{0}.importTimeSeriesData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }
  
  /**
   * Overload 1: Called to initiates the importing process. For each sensor specified in
   * <tt>sensorInfos</tt> call {@linkplain #addImportProcess(
   * gov.ca.water.cdec.core.CdecSensorInfo, java.util.Date, java.util.Date) 
   * this.addImportProcess} to initiate a {@link TimeSeriesImportProcessor} and add it to 
   * this.processingQueue. If {@linkplain #doAutoStartExec() this.doAutoStartExec} = true,
   * it will automatically launching the execution process. Otherwise, this call must
   * be followed by a call to {@linkplain #startExecute() this.startExecute} to launch 
   * the import process.
   * <p>
   * <b>NOTE:</b> The process will fail if a prior execution is in progress.
   * @param startDt the period of interest start date (inclusive)
   * @param endDt the period of interest end date (inclusive)
   * @param sensorInfos an array of one or more CdecSensorInfo 
   * @return (!this.hasError)
   */
  public boolean importTimeSeriesData(Date startDt, Date endDt,
                                                    CdecSensorInfo...sensorInfos) {
    this.clearError();
    try {
      if (this.isExecuting()) {
        throw new Exception("The Importer is busy with a prior data import request.");
      }
      
      this.resetProgress();
      
      /**
       * Check if the sendorTypes are defined.
       */
      if ((sensorInfos == null) || (sensorInfos.length == 0)) {
        throw new Exception("The SensorInfos for which to import the data from CDEC are "
                + "not specified.");
      }
      
      /**
       * Check if the start and end dates are defined.
       */
      if ((startDt == null) || (endDt == null)) {
        throw new Exception("Either or both the start and end dates are undefined.");
      }
      
      /**
       * Check the dates and switch if the the endDt is before the start date.
       */
      if (endDt.before(startDt)) {
        Date tmpDt = endDt;
        endDt = startDt;
        startDt = tmpDt;
      }     
      
      for (CdecSensorInfo sensorinfo : sensorInfos) {    
        this.addImportProcess(sensorinfo, startDt, endDt);
      }
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      logger.log(Level.WARNING, "{0}.importTimeSeriesData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }  
  
  /**
   * Called to import a period of record dataset for the specified sensorId
   * @param sensorId the SensorId
   * @return true if queued without any error.
   */
  public boolean importPeriodOfRecordData(Integer sensorId) {
    this.clearError();
    try {
      if (this.isExecuting()) {
        throw new Exception("The Importer is busy with a prior data import request.");
      }
      
      this.resetProgress();
      
      /**
       * Check if the sendorTypes are defined.
       */
      if (sensorId == null) {
        throw new Exception("The SensorId for which to import the data from CDEC are "
                + "not specified.");
      }
            
      if (this.sensorFacade == null) {
        throw new Exception("Unable to access the CDEC SensorFacade for retrieving the "
                + "information of the sensor's to import data for.");
      }
      
      Sensor sensor = this.sensorFacade.findBySensorId(sensorId);
      if (sensor == null) {
        throw new Exception("Unable to locate Sensor[" + sensorId + "]. Update the "
                + "local Sensor table and try again.");
      }
      
      Integer sensorNo = sensor.getSensorPK().getSensorNum();
      String durCode = sensor.getSensorPK().getDurCode();
      CdecSensorTypes senType = CdecSensorTypes.bySensorNoAndDurCode(sensorNo, durCode);
      SensorPK senPk = sensor.getSensorPK();
      Station senSta = senPk.getStation();
      CdecSensorInfo sensorinfo = new CdecSensorInfo(sensor.getSensorId(), senType, 
                                        senPk.getStationId(), senSta.getBasinNum());
      if (sensorinfo == null) {
        throw new Exception("Unable to initiate the sensorInfo for " +
                                                                  senPk.toString());
      }
      
      Date startDt = sensor.getStartDate();
      if (startDt == null) {
        startDt = new ImportDay(1900, 1, 1).asDate();
      }
      Date endDt = sensor.getEndDate();
      if (endDt == null) {
        endDt = ImportDay.toDay().asDate();
      }

      this.addImportProcess(sensorinfo, startDt, endDt);
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      logger.log(Level.WARNING, "{0}.importPeriodOfRecordData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }
  /**
   * Called to import a the latest sensor/station data
   * @return true if queued without any error.
   */
  public boolean importStationSensorData() {
    this.clearError();
    try {
      if (this.isExecuting()) {
        throw new Exception("The Importer is busy with a prior data import request.");
      }
      
      this.resetProgress();
      
      SensorDefImportProcessor sensorDefProcess = new SensorDefImportProcessor();
      if (sensorDefProcess == null) {
        throw new Exception("Initiating the Sensor Definition Processor failed");
      }
      
      StationImportProcessor staProcess = new StationImportProcessor();
      if (staProcess == null) {
        throw new Exception("Initiating the Station Import Processor failed");
      }
      SensorImportProcessor senProcess = new SensorImportProcessor();
      if (senProcess == null) {
        throw new Exception("Initiating the Sensor Import Processor failed");
      }
      
      this.addImportProcess(sensorDefProcess);      
      this.addImportProcess(staProcess);
      this.addImportProcess(senProcess);
    } catch (Exception exp) {
      this.setErrorMsg(exp.getMessage());
      logger.log(Level.WARNING, "{0}.importStationSensorData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }
  
  /**
   * Call to manually start executing the Data Import Processes. It calls {@linkplain 
   * #onExcuteProcesses() this.onExcuteProcesses} if (!{@linkplain #isExecuting() 
   * this.isExecuting}).
   */
  public void startExecute() {
    if (!this.isExecuting()) {
      this.onExcuteProcesses(null);
    }
  }
  
  /**
   * Called to stop the execution process by clearing the processing Queue and
   * interrupt all executing processes.
   */
  public void stopExecute() {
    if (!this.isBusy()) {
      return;
    }
    
    if (this.processingQueue != null) {
      this.processingQueue.clear();
    }
    
    if ((this.exectingThreads != null) && (!this.exectingThreads.isEmpty())) {
      List<String> processKeys = new ArrayList<>(this.exectingThreads.keySet());
      for (String processId : processKeys) {
        if (this.exectingThreads.containsKey(processId)) {
          Thread execThread = this.exectingThreads.remove(processId);
          if ((execThread != null) && (execThread.isAlive())) {
            try {
              execThread.interrupt();
            } catch (Exception exp) {}
          }
        }
      }
      this.exectingThreads.clear();
    }
    this.fireExecutionCompleted(new ImportEventArgs());
  }
  
  /**
   * Check if the Importer is currently executing import processes or have processes that
   * are not yet executed.
   * @return true if this.processingQueue or this.exectingThreads is not empty.
   */
  public boolean isBusy() {
    return ((!this.processingQueue.isEmpty()) || (!this.exectingThreads.isEmpty()));
  }
  
  /**
   * Check if the Importer is currently executing import processes
   * @return true if this.exectingThreads is not empty.
   */
  public boolean isExecuting() {
    return (!this.exectingThreads.isEmpty());
  }
  
  /**
   * Get the current percentage of submitted processes completed.
   * @return the current fraction or (0.0 if no process is running)
   */
  public double progress() {
    double result = 0.0;
    if (this.numProcesses > 0) {
      result = 1.0;
      if (this.numCompleted < this.numProcesses) {
        result = (1.0d * this.numCompleted)/(this.numProcesses);
      }
    }
    return result;
  }
  
  /**
   * Get the Number of processes added by the latest request
   * @return this.numProcesses
   */
  public int getNumProcesses() {
    return this.numProcesses;
  }
  
  /**
   * Get the Number of processes completed at this time
   * @return this.numCompleted (0 if numProcess = 0)
   */
  public int getNumCompleted() {
    return this.numCompleted;
  }
  
  /**
   * Get the Number of recorded import errors
   * @return this.numErrors
   */
  public int getNumImportErrors() {
    return this.numErrors;
  }
  
  
  /**
   * Get the processing time (in milliseconds) since the start of the execution process.
   * The timer stops at completion of the last process.
   * @return the processing time (in milliseconds) (0 if not yet started)
   */
  public long getProcessigTime() {
    long result = 0;
    if (this.startTime != null) {
      Date time2 = (this.endTime == null)? Calendar.getInstance().getTime(): this.endTime;
      result = (time2.getTime() - this.startTime.getTime());
    }
    return result;
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
