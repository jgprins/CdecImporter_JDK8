package gov.ca.water.cdec.importers;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class ImportProcessor<TEntity extends Serializable> implements Runnable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(ImportProcessor.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Static Placeholder for the CdecVerifier (type HostnameVerifier) reference 
   */
  private static HostnameVerifier _cdecVerifier = null;
  /**
   * Get the Static CdecVerifier reference.
   * @return _cdecVerifier (LIAC)
   */
  private static HostnameVerifier CdecVerifier() {
    if (_cdecVerifier == null) {
      _cdecVerifier = new HostnameVerifier() {
          @Override
          public boolean verify(String string, SSLSession ssls) {
            return true;
          }
        };
    }
    return _cdecVerifier;
  }
  /**
   * Static Placeholder for the SocketFactory (type SSLSocketFactory) reference 
   */
  private static SSLSocketFactory _socketFactory = null;
  /**
   * Get the Static SocketFactory reference.
   * @return _socketFactory (LIAC)
   */
  private static SSLSocketFactory SocketFactory() {
    if (_socketFactory == null) {
      try {
        TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) 
                                                             throws CertificateException {
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) 
                                                            throws CertificateException {
            }
          }
        };
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
        _socketFactory = sc.getSocketFactory();
        
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.SocketFactory Error:\n {1}",
                new Object[]{ImportProcessor.class.getSimpleName(), exp.getMessage()});
      }
    }
    return _socketFactory;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Class ImportArgs">
  /**
   * <p>Protected CancelRequestArgs argument used by inheritors to respond to the
   * download request by setting the {@linkplain #getImportStatus() getImportStatus}
   * using one of the following methods:</p><ul>
   *  <li>{@linkplain #setCompleted() setCompleted} - when download is successfully
   *    completed</li>
   *  <li>{@linkplain #setNotFound() setNotFound} - when no or no new data were
   *    found and a download retry must be scheduled.</li>
   *  <li>{@linkplain #setErrorMsg(String) setErrorMsg} - when Connection or Download
   *    Failed.</li>
   * </ul>
   * <p>The Download Arguments also supports a ParameterMap for storing transient 
   * download setting (e.g. a the URL) for transferring from one process to another.</p>
   */
  public class ImportArgs implements Serializable {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * Placeholder for error message as the reason for the cancellation
     */
    private String errorMessage;
    /**
     * Placeholder for a ParameterMap for storing transient data associated with
     * a download.
     */
    private LinkedHashMap params;
    /**
     * Placeholder for the Import Status (default = {@linkplain ImportStatus#IMPORTING}
     */
    private ImportStatus importStatus;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    protected ImportArgs() {
      super();
      this.errorMessage = null;
      this.params = new LinkedHashMap();
      this.importStatus = ImportStatus.IMPORTING;
    }

    /**
     * {@inheritDoc} <p>OVERRIDE: Call super method before clearing and disposing the
     * Parameters.</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      if (this.params != null) {
        this.params.clear();
      }
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * <p>Get the ImportStatus. Initially {@linkplain ImportStatus#NOTSTARTED}.
     * @return the event's Download Status .
     */
    public ImportStatus getImportStatus() {
      return this.importStatus;
    }

    /**
     * Get the assigned error message
     * @return the assign message (or null)
     */
    public String getErrorMessage() {
      return errorMessage;
    }
    
    /**
     * Call to set the DownloadStatus={@linkplain DownloadStatusEnums#COMPLETED
     * COMPLETED} - after a successful download. It also set this.result=true.
     */
    public void setCompleted() {
      this.importStatus = ImportStatus.COMPLETED;
    }
    
    /**
     * <p>Call to set the ImportStatus={@linkplain ImportStatus#NOTFOUND
     * NOTFOUND}.</p>
     * <p>This should only be called if the connection and import was successful,
     * but no or no new data has been download. </p>
     */
    public void setNotFound() {
      this.importStatus = ImportStatus.NOTFOUND;
    }
    
    /**
     * <p>Call to set the ImportStatus={@linkplain ImportStatus#RETRY
     * RETRY}.</p>
     * <p>This should only be called if the connection and import was successful,
     * but no or no new data has been download. </p>
     */
    public void setRetry() {
      this.importStatus = ImportStatus.RETRY;
    }
    
    /**
     * Called to set the ImportStatus={@linkplain ImportStatus#ERROR
     * ERROR}. It also assigned the associates error message
     * @param errorMessage 
     */
    public void setErrorMsg(String errorMessage) {
      this.errorMessage = errorMessage;
      this.importStatus = ImportStatus.ERROR;
    }
    
    /**
     * Get a previously set Parameter value
     * @param <TValue> extends Serializable
     * @param paramKey the parameter key/name
     * @param defaultValue the default value if the parameter is not assigned
     * @return the set value or null if the parameter is undefined.
     */
    @SuppressWarnings("unchecked")
    public <TValue extends Serializable> TValue getParameter(String paramKey, 
                                                                   TValue defaultValue) {
      TValue result = null;
      try {
        if ((this.params != null) && (this.params.containsKey(paramKey))) {
          result = (TValue) this.params.get(paramKey);
        } 
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.getParameter Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
      return (result == null)? defaultValue: result;
    }
    
    /**
     * Set a Parameter Value. if the value already exist, it will be overridden with 
     * no warning.
     * @param <TValue> extends Serializable
     * @param paramKey the parameter key/name
     * @param paramValue the assigned value (can be null)
     */
    public <TValue extends Serializable> void setParameter(String paramKey, 
                                                           TValue paramValue) {
      try {
        this.params.put(paramKey, paramValue);
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.setParamater Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
    //</editor-fold>
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Protected Static Methods">
  /**
   * Append to the sUrl (assuming it is a valid Url, the sPar and sValue in the format
   * sUrl?sPar=sValue. if (sValue=null|Empty), set sValue="". if (sUrl=null|Empty) or
   * (sPar=null|Empty) return null. If sUrl already contain a "?", and it is not the
   * last character add a "&" (i.e. sUrl&sPar=sValue), otherwise append a "?". Call
   * HttpUtils.encodeString(sValue) to encode the string before assigning it to the
   * Url.
   * @param curUrl String
   * @param parname String
   * @param parValue String
   * @return String
   * @throws Exception
   */
  protected static String appendToUrl(String curUrl, String parname, String parValue) 
          throws Exception {
    String result = curUrl;
    try {
      parname = ImportUtils.cleanString(parname);
      parValue = ImportUtils.cleanString(parValue);
      if ((result != null) && (parname != null)) {
        String sEncVal = (parValue == null) ? "" : ImportUtils.encodeString(parValue);
        String sAppend = parname + "=" + sEncVal;
        if (result.contains("?")) {
          if (!curUrl.endsWith("?")) {
            result += "&";
          }
        } else {
          result += "?";
        }
        result += sAppend;
      }
    } catch (Exception pExp) {
      throw new Exception("ImportProcessor.appendToUrl Error:\n " + pExp.getMessage());
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for ProcessName
   */
  protected final String processId;
  /**
   * Placeholder for ProcessName
   */
  protected final String processName;
  /**
   * Placeholder for the Processors importStatus (initially NOTSTARTED )
   */
  protected ImportStatus importStatus;
  /**
   * The Number of re-tries to connect
   */
  protected Integer tryCount;
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Events">
  /**
   * The EventHandler that fires the LogMessageEvent.
   */
  public final ImportEventHandler LogMessage;
  /**
   * The EventHandler that fires the Process Start Event.
   */
  public final ImportEventHandler ProcessStart;
  /**
   * The EventHandler that fires the Process End Event.
   */
  public final ImportEventHandler ProcessEnd;
  /**
   * The EventHandler that fires the Process End Event.
   */
  public final ImportEventHandler ProcessRetry;
  /**
   * Called to fire the LegMessage  Event.  
   * @param logMsg the message to log
   */
  protected final void sendLogMessage(Level logLevel, String logMsg) {
    if (this.LogMessage.isEmpty()) {  
      logger.log(logLevel, "{0}: {1}", new Object[]{this.processName, logMsg});
    } else {
      this.LogMessage.fireEvent(this, new ImportEventArgs(logLevel, logMsg));
    }
  }

  /**
   * Called to fire the Process Start Event.
   *
   * @param eventInfo the event info
   */
  protected final void fireProcessStart() {
    this.ProcessStart.fireEvent(this, new ImportEventArgs());
    //this.sendLogMessage(Level.INFO, "Starting Import Process.");
  }

  /**
   * Called to fire the Process End Event.
   *
   * @param eventInfo the event info
   */
  protected final void fireProcessEnd() {
    this.ProcessEnd.fireEvent(this, new ImportEventArgs());
    //this.sendLogMessage(Level.INFO, "Completed Import Process.");
  }

  /**
   * Called to fire the Process End Event.
   *
   * @param eventInfo the event info
   */
  protected final void fireProcessRetry() {
    this.ProcessEnd.fireEvent(this, new ImportEventArgs());
    this.sendLogMessage(Level.INFO, "Retry Import Process.");
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor
   */
  public ImportProcessor(String processName) {
    super();
    try {
      if (processName == null) {
        throw new NullPointerException("The Process Name cannot be undefined");
      }
      
      this.LogMessage = new ImportEventHandler();
      this.ProcessStart = new ImportEventHandler();
      this.ProcessEnd = new ImportEventHandler();
      this.ProcessRetry = new ImportEventHandler();
      this.processId = ImportUtils.newUniqueID();
      this.processName = processName;
      this.importStatus = ImportStatus.NOTSTARTED;
      this.tryCount = 1;
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".methodName Error:\n " + exp.getMessage());
    }
  }
  

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method before releasing all event delegates assigned to
   * the public EventHandlers.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.resetProcessor();
  }
  
  /**
   * Called after completion of the execution to reset the processors event listeners
   * and facade reference.
   */
  private void resetProcessor() {
    this.LogMessage.clear();
    this.ProcessEnd.clear();
    this.ProcessRetry.clear();
    this.ProcessStart.clear();
    
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Processor's ProcessName
   * @return the assigned name
   */
  public String getProcessName() {
    return this.processName;
  }

  /**
   * Get the processors ImportStatus
   * @return the current assigned Status
   */
  public ImportStatus getImportStatus() {
    return this.importStatus;
  }

  /**
   * Get the Process current TryCount (number of process tries)
   * @return the assigned value (default = 1)
   */
  public Integer getTryCount() {
    return (this.tryCount == null) ? 1 : this.tryCount;
  }

  /**
   * Set the Process new TryCount (number of process tries)
   * @param newCount the new count (ignored if null)
   */
  protected void setTryCount(Integer newCount) {
    if (newCount != null) {
      this.tryCount = newCount;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public/Protected Abstract Methods">
  /**
   * CAN OVERRIDE: Called by {@linkplain #resetProcessor() this.resetProcessor} to allow
   * inheritors to reset resources after a successful or unsuccessful run.
   */
  protected void onResetProcessor() {}
  
  /**
   * Initiate a new Instance with the same properties as this process with an incremented
   * TryCount and no events listeners assigned.
   * @return a new cloned instance.
   */
  public abstract ImportProcessor<TEntity> nextTryClone();

  /**
   * ABSTRACT: Called  by the {@linkplain #run() run} method to initiate the URL for the
   * connecting to the external web-based data source and assign the URL to
   * args.parameter[IMPORT_URL].<p>
   * <p><b>NOTE:</b> Errors should be trapped and assigned as args.errorMsg.
   * @param args the ImportArgs initiated in {@linkplain #run() run}
   */
  protected abstract void onInitImportUrl(ImportArgs args);

  /**
   * <p>CAN OVERRIDE: Called by the {@linkplain #run() run} method to handle the
   * custom download of data of data and update the {@linkplain ImportArgs args}.</p>
   * <p><b>NOTE:</b> Errors should be trapped and assigned as args.errorMsg.
   * @param args the ImportArgs initiated in {@linkplain #run() run}
   */
  protected void onImportData(ImportArgs args) {
    try {
      System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
      System.setProperty ("jsse.enableSNIExtension", "false");
      HttpsURLConnection.setDefaultHostnameVerifier(ImportProcessor.CdecVerifier());
      HttpsURLConnection.setDefaultSSLSocketFactory(ImportProcessor.SocketFactory());
      
      URL urlAddr = args.getParameter(ImportKeys.IMPORT_URL, null);
      if (urlAddr == null) {
        throw new Exception("The Import URL is undefined or accessible.");
      }
      
      this.sendLogMessage(Level.FINE,"Import Url = " + urlAddr.toString());
      
      int loopCount = 0;
      while (loopCount <= 10) {
        loopCount++;
        URLConnection conn = null;
        String dataStr = null;
        int respondCode = 0;
        HttpsURLConnection sslConn = null;
        try {
          conn = urlAddr.openConnection();  
          if ((conn == null) || (!(conn instanceof HttpsURLConnection )) ||
              ((sslConn = (HttpsURLConnection) conn) == null)) {  
            throw new Exception("Open a conenction for URL[" + urlAddr.toString() 
                    + "] or is not a failed SSL Conenction. No reason was provided.");
          } 
          sslConn.setRequestMethod("GET");
          sslConn.connect();

          InputStream inStream = sslConn.getInputStream();
          if ((respondCode = sslConn.getResponseCode()) == HttpURLConnection.HTTP_OK) {
            int iRead = 0;
            byte[] readBuffer = new byte[4096];
            String subStr = null;

            /** Read the first line to validate that the data is correct **/
            iRead = inStream.read(readBuffer);
            if (iRead > 0) {
              subStr = new String(readBuffer, 0, iRead, "UTF-8");
              if ((subStr == null) || ((subStr.startsWith("<!DOCTYPE")))) {
                throw new Exception("No data found.");
              }
              dataStr = subStr;
            }

            if (dataStr != null) {
              while (true) {
                iRead = inStream.read(readBuffer);
                if (iRead <= 0) {
                  break;
                }
                subStr = new String(readBuffer, 0, iRead, "UTF-8");
                if (subStr != null) {
                  dataStr += subStr;
                }
              }
            }
            args.setParameter(ImportKeys.IMPORT_STR, dataStr);
            break;
          } else {
            this.sendLogMessage(Level.WARNING, "Connection[" + urlAddr.toString() 
                                        + "] failed; Response Code[" 
                                      + respondCode + ". Retry Connection");
          }
        } catch (IOException expIO) {
          String errMsg = "Connection[" + urlAddr.toString() 
                                        + "] failed; Error: " + expIO.getMessage() 
                                        + ".";
          if (respondCode == 500) {
            errMsg += " Retry Connection.";
            this.sendLogMessage(Level.WARNING, errMsg);
          } else {
            this.sendLogMessage(Level.WARNING, errMsg);
            args.setErrorMsg(errMsg);
          }
        } catch (NoClassDefFoundError expClass) {
          String errMsg = "Connection[" + urlAddr.toString() 
                                        + "] failed; Error: " + expClass.getMessage();
          this.sendLogMessage(Level.WARNING, errMsg);
          args.setErrorMsg(errMsg);   
        } catch (Exception expIn) {
          String errMsg = "Connection[" + urlAddr.toString() 
                                        + "] failed; Error: " + expIn.getMessage();
          this.sendLogMessage(Level.WARNING, errMsg);
          args.setErrorMsg(errMsg);          
        } finally {
          if (conn != null) {
            if (sslConn != null) {
              sslConn.disconnect();
            }
            conn = null;
          }
        }
        
        if (!args.getImportStatus().equals(ImportStatus.IMPORTING)) {
          break;
        }
      }
    } catch (Exception exp) {
      String errMsg = "onImportData Error:\n " + exp.getMessage();
      args.setErrorMsg(errMsg);
    }
  }

  /**
   * <p>ABSTRACT: Called by {@linkplain #run() this.run} to parse the imported data 
   * string to the format required by the merge process. If the parsing return an empty 
   * result (no valid data were loaded), it should calls args.setNotFound to set 
   * the args.importStatus=NOT_FOUND.</p>
   * <p><b>NOTE:</b> Errors should be trapped and assigned as args.errorMsg.
   * @param args the ImportArgs initiated in {@linkplain #run() run}
   */
  protected abstract void onParseImportData(ImportArgs args);

  /**
   * <p>ABSTRACT: Called by the {@linkplain #run() run} method to handle the
   * merging of the imported and local data. Missing records will be inserted and the
   * observed value of the import and locate records are compared and if not equal, the
   * local record will be replaced or updated with the imported record.</p>
   * <p><b>NOTE:</b> Errors should be trapped and assigned as args.errorMsg.
   * @param args the ImportArgs initiated in {@linkplain #run() run}
   */
  protected abstract void onMergeData(ImportArgs args);
  //</editor-fold>

  // <editor-fold defaultstate="expanded" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: This execute the following calls in sequence:<ul>
   * <li>It initiates the ImportArgs (args) for the process</li>
   * <li>It set this.importStatus=IMPORTING and fire the ProcessStart event.</li>
   * <li>It calls {@linkplain #onInitImportUrl(
   *  gov.ca.water.cdec.importers.ImportProcessor.ImportArgs) this.onInitImportUrl}.
   *  and raise the exception if args.status=ERROR</li>
   * <li>It calls {@linkplain #onImportData(
   *  gov.ca.water.cdec.importers.ImportProcessor.ImportArgs) this.onImportData}.
   *  and raise the exception if args.status=ERROR. If the args.status=RETRY, it
   *  set this.importStatus=RETRY and end the import process.</li>
   * <li>It calls {@linkplain #onParseImportData(
   *  gov.ca.water.cdec.importers.ImportProcessor.ImportArgs) this.onParseImportData}.
   *  and raise the exception if args.status=ERROR. If the args.status=NOTFOUND, it
   *  set this.importStatus=NOTFOUND and end the import process.</li>
   * <li>It calls {@linkplain #onLoadLocalData(
   *  gov.ca.water.cdec.importers.ImportProcessor.ImportArgs) this.onLoadLocalData}.
   *  and raise the exception if args.status=ERROR</li>
   * <li>It calls {@linkplain #onMergeData(
   *  gov.ca.water.cdec.importers.ImportProcessor.ImportArgs) this.onMergeData}.
   *  and raise the exception if args.status=ERROR</li>
   * <li>It sets this.importStatus=COMPLETED</li>
   * <li>FINALLY: It fires this.ProcessEnd event. before returning to the Thread.</li>
   * </ul>
   */
  @Override
  public void run() {
    ImportArgs args = new ImportArgs();
    try {
      this.importStatus = ImportStatus.IMPORTING;
      this.fireProcessStart();
      this.onInitImportUrl(args);
      if (ImportStatus.ERROR.equals(args.getImportStatus())) {
        throw new Exception(args.getErrorMessage());
      }
      this.onImportData(args);
      if (ImportStatus.ERROR.equals(args.getImportStatus())) {
        throw new Exception(args.getErrorMessage());
      } else if (ImportStatus.RETRY.equals(args.getImportStatus())) {
        this.importStatus = ImportStatus.RETRY;
        return;
      }
      this.onParseImportData(args);
      if (ImportStatus.ERROR.equals(args.getImportStatus())) {
        throw new Exception(args.getErrorMessage());
      } else if (ImportStatus.NOTFOUND.equals(args.getImportStatus())) {
        this.importStatus = ImportStatus.NOTFOUND;
        this.sendLogMessage(Level.INFO, "No import data found.");
        return;
      }
      this.onMergeData(args);
      if (ImportStatus.ERROR.equals(args.getImportStatus())) {
        throw new Exception(args.getErrorMessage());
      }
      this.importStatus = ImportStatus.COMPLETED;
    } catch (Exception exp) {
      this.importStatus = ImportStatus.ERROR;
      String errMsg = ((exp == null) || (exp.getMessage() == null)) ? 
                "Unknown" + ((exp == null) ? "Exception" : exp.getClass().getSimpleName())
                : exp.getMessage();
      this.sendLogMessage(Level.WARNING, errMsg);
    } finally {
      this.fireProcessEnd();
      this.resetProcessor();
    }
  }

  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return this.processName;
  }
  // </editor-fold>
}
