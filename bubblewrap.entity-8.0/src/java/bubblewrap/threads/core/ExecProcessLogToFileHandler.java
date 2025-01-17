package bubblewrap.threads.core;

import bubblewrap.http.session.SessionHelper;
import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.files.FileManager;

/**
 * <p>An ExecProcessLogHandler for writing the a file at a specified location  and clear 
 * the log in the ExecprocessLogger if ({@linkplain #doClearLog() this.doClearLog}).</p>
 * <p></p>
 * @author kprins
 */
public class ExecProcessLogToFileHandler extends ExecProcessLogHandler {  
  /**
   * Placeholder for an assigned Output Path
   */
  private File path;
  private String filePrefix;
  private Boolean asHtmlFile;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessLogToFileHandler(String sProcessName) {
    super(sProcessName);    
    this.path = null;
    this.filePrefix = null;
    this.asHtmlFile = null;
    this.setClearLog(true);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * <p>Set the Log Output Path, a prefix for the log filename, and the flag specifying
   * the output format (HTML or Text).</p>
   * <p>If (pPath!=null), it will get the absolute path, check if the path exists, and
   * it not, it will attempt to create the path. If this process failed, it will log 
   * the error and set the path=null (see {@linkplain #getPath()} for more details).</p>
   * <p>If (sFilePrefix!=""|null), it will convert sFilePrefix to a Unix-formatted 
   * filename.</p>
   * @param path the log file output path (can be null to use default path)
   * @param filePrefix the log filename prefix (can be null to use default prefix)
   * @param asHtmlFile true=HTML format; false=text format (default=false).
   */
  public void setLogFile(File path, String filePrefix, boolean asHtmlFile) {
    this.path = null;
    if (path != null) {
      try {
        this.path = path.getAbsoluteFile();
        if (!this.path.exists()) {
          this.path.mkdirs();
        }
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.setLogFile.path Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
        this.path = null;
      }
    }
    
    this.filePrefix = DataEntry.cleanString(filePrefix);
    if (this.filePrefix != null) {
      FileManager.toUnixFilename(this.filePrefix);
    }
    this.asHtmlFile = (!asHtmlFile)? null: asHtmlFile;
  }
  
  /**
   * Get the File Format setting (default=false|Text File)
   * @return true=HTML File; false=Text File.
   */
  public boolean getAsHtmlFile() {
    return ((this.asHtmlFile != null) && (this.asHtmlFile));
  }
  
  /**
   * Get the FilePrefix. With the first call, if the prefix is unassigned, set it as 
   * this.preocessName and call the {@linkplain FileManager#toUnixFilename(
   * java.lang.String) FileManager.toUnixFilename} method to convert the processName to
   * a valid filename.
   * @return return the file prefix.
   */
  protected String getFilePrefix() {
    if (this.filePrefix == null) {
      this.filePrefix = this.getProcessName();
      this.filePrefix = FileManager.toUnixFilename(this.filePrefix);
    }
    return this.filePrefix;
  }
  
  /**
   * Get the Log Output Path. With the first call, if the Path is unassigned, initiate
   * the paths as a {@linkplain FileManager#getRealResourcePath(java.lang.String) 
   * ResourcePath}("logs/" + this.filePrefix + "/") ResourcePath} if the FaceContext 
   * is accessible or a {@linkplain FileManager#getTempDirectory() TempDirectory()} 
   * with a sub-directory "/javaapp/logs/" + this.filePrefix + "/". Check if this path
   * exists, and if not, create the path.
   * @return the log output path
   */
  public File getPath() {
    try {
      if ((this.path == null) || (!this.path.isDirectory())) {
        String sPrefix = this.getFilePrefix();
        File tempDir = null;
        if (SessionHelper.hasFacesContext()) {
          tempDir = FileManager.getRealResourcePath("logs/" + sPrefix + "/");
        } else {
          File baseDir = FileManager.getTempDirectory();
          tempDir = new File(baseDir, "/javaapp/logs/" + sPrefix + "/");
        }
        
        this.path = tempDir.getAbsoluteFile();
      }
      
      if (!this.path.exists()) {
        this.path.mkdirs();
      }
    } catch (Exception pExp) {
      this.path = null;
      logger.log(Level.WARNING, "{0}.getPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.path;
  }
  
  /**
   * Get the full LogFile path with Date Stamp appended to the File Prefix. The file
   * extension will be set according this.ssHtmlFile flag.
   * @return the full path of the current log file
   */
  protected File getLogFile() {
    File pResult = null;
    try {
      Integer iCnt = 0;
      DateTime pNow = DateTime.getNow(null);
      String sDtStamp = pNow.toLocaleString("YYYY_MM_dd_HHmmss");
      while (true) {
        String sFilename = this.getFilePrefix()+"_"+sDtStamp;
        if (iCnt > 0) {
          sFilename += "_" + iCnt.toString();
        }
        if (this.getAsHtmlFile()) {
          sFilename += ".html";
        } else {
          sFilename += ".log";
        }
        iCnt++;
        
        File pPath = this.getPath();
        File pFilePath = new File(pPath,sFilename);
        if (!pFilePath.exists()) {
          pResult = pFilePath;
          break;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getLogFile Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * Get the HTML Log Content as the body of an HTML file with no style settings.
   * @param sHtmlLog the HTML File Body content (in HTML Format as generated by 
   * {@linkplain ExecProcessLogger#getHtmlLog()})
   * @return the HTML formatted file as a string.
   */
  protected String getHtmlLogContent(String sHtmlLog) {
    String sResult = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" "
      + "\"http://www.w3.org/TR/html4/loose.dtd\">\n" 
      + "<html>\n" 
      + "  <head>\n" 
      + "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n"
      + "    <title>" + this.getProcessName() + " Log</title>\n" 
      + "  </head>\n" 
      + "  <body>\n" 
      + sHtmlLog
      + "  </body>\n" 
      + "</html>"; 
    return sResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements Runnable">
  /**
   * {@inheritDoc} <p>OVERRIDE: Call the assigned ExecProcessLogger's {@linkplain
   * ExecProcessLogger#dumpLog() getLog} or {@linkplain ExecProcessLogger#dumpLog() 
   * getHtmlLog} method to get the current log dump. I f(this.doClearLog), the log will
   * be cleared.</p>   * 
   * <p>if (this.asHtmlFile), call {@linkplain #getHtmlLogContent(java.lang.String) 
   * this.getHtmlLogContent}, to get the HTML with the log as the content. This method
   * can be overridden to present the log in a custom format.</p>
   * <p>Call {@linkplain #getLogFile() this.getLogFile} to get the path to the log 
   * output file and write the log content to the file</p>
   * <p><b>NOTE:</b> If this process generates and exception, the error will be logged
   * to the server and the scheduling of future task will be stopped.</p>
   */
  @Override
  public void run() {
    File pFile = null;
    try {
      ExecProcessLogger pLogger = this.getLogger();
      if (pLogger == null) {
        throw new Exception("The ExcProcess Logger is not assigned.");
      }
      
      if (!pLogger.isEmpty()) {
        pFile = this.getLogFile();
        if (pFile == null) {
          throw new Exception("Unable to initiate the Log Filename.");
        }

        String sLog = null;
        if (this.getAsHtmlFile()) {
          sLog = pLogger.getHtmlLog();
          sLog = this.getHtmlLogContent(sLog);
        } else {
          sLog = pLogger.getLog();
        }
      
        if (DataEntry.cleanString(sLog) == null) {
          sLog = "The Process Log is Empty or a error occurred when generating the log.";
        }

        try (PrintWriter pWriter = new PrintWriter(pFile)) {
          pWriter.print(sLog);
          pWriter.close();
        }
        
        logger.log(Level.INFO, "Save Log to File[{0}] @ {1}",
                new Object[]{pFile.getPath(),DateTime.getNow(null)});
      }
      
      if (this.doClearLog()) {
        pLogger.clearLog();
      }      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.run Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      this.stopSchedule();
    }
  }
  //</editor-fold>  
}
