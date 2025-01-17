package bubblewrap.threads.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.datetime.DateTimeSpan;
import bubblewrap.threads.interfaces.IExecProcess;
import java.io.Serializable;

/**
 * An Logger Class that handles the event logging of a Executable Processes. Used by the
 * {@linkplain ThreadQueue class} and the {@linkplain @ExecProcessThread} is assigned.
 * @author kprins
 */
public class ExecProcessLogger implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(ExecProcessLogger.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Source Enum">
  /**
   * The process log Source (ACTIVE | ARCHIVED)
   */
  public static enum Source {

    ACTIVE,
    ARCHIVED;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="LogEntry Class">
  /**
   * public Class for the LogEntry used by {@linkplain ProcessLog}
   */
  public class LogEntry implements Serializable {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * The Date/Time the message was logged.
     */
    private DateTime logTime;
    /**
     * The Log Level
     */
    private Level logLevel;
    /**
     * The Log Message
     */
    private String logMsg;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Constructor Initiate a new entry
     * @param logLevel the log Level
     * @param logMsg the log message
     */
    public LogEntry(Level logLevel, String logMsg) {
      this.logTime = DateTime.getNow(null);
      this.logLevel = logLevel;
      this.logMsg = logMsg;
    }
    
    /**
     * Parameterless Constructor for cloning
     */
    private LogEntry(){
      this.logTime = null;
      this.logLevel = null;
      this.logMsg = null;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Get the Date/Time of the Log Entry
     * @return the recorded DateTime
     */
    public DateTime getLogTime() {
      return this.logTime;
    }
    
    /**
     * Get the Entry's Log Level
     * @return the assigned Level
     */
    public Level getLevel() {
      return this.logLevel;
    }
    
    /**
     * Get the Log Message
     * @return the assigned Message
     */
    public String getMessage() {
      return this.logMsg;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Object Overrides">
    /**
     * {@inheritDoc} <p>OVERRIDE: Return the log entry is the format DateTime; Level;
     * Message.</p>
     */
    @Override
    public String toString() {
      return this.logTime.toLocaleString("MM/dd/yyyy hh:mm:ss") + "; "
              + this.logLevel.toString() + "; "
              + this.logMsg;
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: Return a deep clones LogEntry</p>
     */
    @Override
    protected LogEntry clone() {
      LogEntry clone = new LogEntry();
      clone.logTime = this.logTime.clone();
      clone.logLevel = this.logLevel;
      clone.logMsg = this.logMsg;
      return clone;
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="ProcessLog Class ">
  /**
   * Public embedded class for managing the logs by ExecProces 
   */
  protected class ProcessLog implements IExecProcess, Comparable<ProcessLog> {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * The ID of the process
     */
    private String processId;
    /**
     * The name of the Process
     */
    private String processName;
    /**
     * The Log Entries for the process
     */
    private List<LogEntry> logEntries;
    /**
     * The Date/Time the last log entry was posted
     */
    private DateTime lastLogTime;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor with a process reference
     */
    protected ProcessLog(IExecProcess process) {
      super();
      if (process == null) {
        throw new NullPointerException("The Process Log's process is unassigned.");
      }
      if ((this.processId = DataEntry.cleanString(process.getProcessId())) == null) {
        throw new NullPointerException("The Process Log's Process ID is undefined.");
      }
      if ((this.processName = DataEntry.cleanString(process.getProcessName())) == null) {
        throw new NullPointerException("The Process Log's Process Name is undefined.");
      }
      this.logEntries = null;
      this.lastLogTime = null;
    }
    
    /**
     * {@inheritDoc} <p>OVERRIDE: Call super method before disposing Log Entries.</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
      this.clearLog();
    }

    /**
     * Called to clear the Log ENtries.
     */
    public void clearLog() {
      if (this.logEntries != null) {
        this.logEntries.clear();
      }
      this.logEntries = null;
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Implement IExecProcess">
    /**
     * Get the Process's ID
     * @return return the assigned ID
     */
    @Override
    public String getProcessId() {
      return this.processId;
    }
    
    /**
     * Get the Process's Name
     * @return return the assigned Name
     */
    @Override
    public String getProcessName() {
      return this.processName;
    }

    /**
     * {@inheritDoc} 
     */
    @Override
    public boolean isProcessId(String sProcessId) {
      return DataEntry.isEq(this.processId, sProcessId, true);
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Get the number of full days since the las log entry was posted
     * @return the age in days.
     */
    public int getAge() {
      DateTime now = DateTime.getNow(null);
      DateTimeSpan timeSpan =  now.subtract(this.lastLogTime);
      return (timeSpan == null)? 0: timeSpan.getFullDays();
    }
   
    /**
     * Add a new Log Entry. Skip is this.isClosed or logMsg = null|"".
     * logLevel logLevel the Log Level
     * @param logMsg the message to log.
     */
    public void addLog(Level logLevel, String logMsg) {
      logMsg = DataEntry.cleanString(logMsg);
      if (logMsg == null) {
        return;
      }
      
      if (this.logEntries == null) {
        this.logEntries = new ArrayList<>();
      }
      
      LogEntry logEntry = new LogEntry(logLevel, logMsg);
      this.logEntries.add(logEntry);
      this.lastLogTime = logEntry.getLogTime();
    }
    
    /**
     * A Private add method to add a clone of pEntry to the LogEntries list. Called
     * from AssignTo
     * logEntry pEntry 
     */
    private void addLog(LogEntry logEntry) {
      LogEntry clone = (logEntry == null)? null: logEntry.clone();
      if (clone == null) {
        return;
      }
      
      if (this.logEntries == null) {
        this.logEntries = new ArrayList<>();
      }
      
      this.logEntries.add(clone);
      if ((this.lastLogTime == null) 
                        || (clone.getLogTime().isAfter(this.lastLogTime))) {
        this.lastLogTime = clone.getLogTime();
      }
    }
    
    /**
     * Check if the ProcessLog is empty
     * @return true is the log has no log entries
     */
    public boolean isEmpty() {
      return ((this.logEntries == null) || (this.logEntries.isEmpty()));
    }
    
    /**
     * Write the ProcessLog's full message to the Server Log. It calls this.getLog to
     * get the Log message.
     */
    public void dumprocLog() {
      String sLog = this.getLog();
      logger.log(Level.INFO, sLog);
    }
    
    /**
     * Get the Log Entries as a multi-line sting
     * @return the log entries
     */
    public String getLog() {
      String result = this.toString() + ":\n\r";
      if (this.isEmpty()) {
        result += "\tNo log entries\n\r";
      } else {
        for (LogEntry logEntry : this.logEntries) {
          result += "\t" + logEntry.toString() + "\n\r";
        }
      }
      result += "\t---- End Log -----\n\r";
      return result;
    }
    
    /**
     * Get the Process Log as an HTML formatted string
     * @return the formatted string
     */
    public String getHtmlLog() {
      String result = "<div class='bwgProcessLog collapsed'>\n"
              + "  <span class='caption' onclick='return bw.toggleProcLog(this);'>" 
              + this.processName + ":</span>\n  <ul>\n";
      if (this.isEmpty()) {
        result += "    <li>No log entries</li>\n";
      } else {
        for (LogEntry logEntry : this.logEntries) {
          result += "    <li>" + logEntry.toString() + "</li>\n";
        }
      }
      result += "  </ul>\n</div>\n";
      return result;
    }
    
    /**
     * Transfer the this instances LogEntries to pTargettarget
     * @param target the ProcessLog to transfer the entries to
     */
    protected void AssignTo(ProcessLog target) {
      try {
        if ((target == null) 
                    && (this.logEntries == null) || (this.logEntries.isEmpty())) {
          return;
        }
        
        for (LogEntry logEntry : this.logEntries) {
          target.addLog(logEntry);
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.AssignTo Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Object Overrides">
    /**
     * {@inheritDoc} <p>OVERRIDE: Return "ProcessLog[id=" + this.processId + ";
     * process=" + this.processName + "]"</p>
     */
    @Override
    public String toString() {
      return "ProcessLog[id=" + this.processId + "; process=" + this.processName + "]";
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Override Comparable">
    /**
     * {@inheritDoc}
     * <p>
     * OVERRIDE: Compare by processName</p>
     */
    @Override
    public int compareTo(ProcessLog other) {
      int result = 0;
      String otherName  = (other == null)? null: other.getProcessName();
      if (otherName == null) {
        result = 1;
      } else {
        result = this.processName.compareTo(otherName);
      }
      return result;
    }
    // </editor-fold>
  }
  //</editor-fold>
  
  /**
   * The ActiveProcessLog extends the base ProcessLog to add an Initiated Time and a
   * Close State. When the ActiveProcessLog is closed, its log entries will 
   * automatically be transferred to the ArchiveLogs and it will be remove from the 
   * ActiveLogs list.
   */
  protected class ActiveProcessLog extends ProcessLog {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * The Date/Time the log was initiated.
     */
    private DateTime initTime;
    /**
     * Flag stating whether the Log is closed.
     */
    private Boolean closed;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    protected ActiveProcessLog(IExecProcess process) {
      super(process);            
      this.initTime = DateTime.getNow(null);
      this.closed = null;
      this.addLog(Level.INFO, "--- Process Logging Started ---");
    }
    // </editor-fold>
     
    //<editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Get the Date/Time when the ProcessLog was initiated
     * @return the assigned DateTime
     */
    public DateTime getInitiatedTime() {
      return this.initTime;
    }
    
    /**
     * Call to close the log - Once closed no log entries can be added. The content
     * of the log is automatically copied to the ArchiveLogs. However, this log is
     * not cleared or removed from the ActiveLog.
     */
    public void closeLog() {
      if (!this.isClosed()) {
        this.addLog(Level.INFO, "--- Process Logging Closed ---");
        this.closed = true;
        ProcessLog pArchiveLog = getArchivedLog(this);
        if (pArchiveLog != null) {
          this.AssignTo(pArchiveLog);
        }
      }
    }
    
    /**
     * Get the ProcessLog siCLosed State
     * @return true if closed.
     */
    public boolean isClosed() {
      return ((this.closed != null) && (this.closed));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="ProcessLog Overrrides">
    /**
     * {@inheritDoc} <p>OVERRIDE: Skip the process if this.isClosed</p>
     */
    @Override
    public final void addLog(Level logLevel, String logMsg) {
      if (!this.isClosed()) {
        super.addLog(logLevel, logMsg);
      }
    }
    //</editor-fold>
  }
  
  //<editor-fold defaultstate="collapsed" desc="ProgressLogComparator Class">
  /**
   * A Comparator used to sort the Logs by age before cleaning it.
   */
  private class ProcessLogComparator implements Comparator<ProcessLog>, Serializable {
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ProcessLogComparator() {
      super();
    }
    // </editor-fold>
    
    /**
     * {@inheritDoc} <p>OVERRIDE: Compare the two ProcessLogs by Age assuming age-1 if
     * undefined</p>
     */
    @Override
    public int compare(ProcessLog log1, ProcessLog log2) {
      Integer age1 = (log1 == null)? -1: log1.getAge();
      Integer age2 = (log2 == null)? -1: log2.getAge();
      return age1.compareTo(age2);
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A 
   */
  private HashMap<String,ProcessLog> archivedLogs;
  /**
   * Placeholder for the processes
   */
  private HashMap<String,ActiveProcessLog> activeLogs;
  /**
   * Placeholder for the maximum number of processes to log a a given time (ignored if
   * not set)
   */
  private Integer maxProcesses;
  /**
   * Placeholder for the maximum number of days before the log is deleted (ignored if
   * not set)
   */
  private Integer maxAge;
  /**
   * A placeholder for an optional ExecProcessLogHandler for handling the output of the
   * log to the server or a file.
   */
  private ExecProcessLogHandler loggerHandler;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ExecProcessLogger() {
    super();    
    this.archivedLogs = null;
    this.activeLogs = null;
    this.maxProcesses = null;
    this.maxAge = null;
    this.loggerHandler = null;
  }
  
  /**
   * Set the Logger's limits - to prevent too large logs and the ExecProcessLogHandler
   * to manage the log output to the server or to file . If (pOutputHandler != null), it
   * will assign itself as the pOutputHandler.logger.
   * @param maxProcesses the maximum number of process to log (No limit is null or 
   * less or equal to zero)
   * @param maxAge the maximum age of entries to keep in the Active Log (No limit is 
   * null or less or equal to zero) 
   * @param outputHandler to process the log output to the server or a file (optional)
   */
  public void setLogger(Integer maxProcesses, Integer maxAge, 
                                                ExecProcessLogHandler outputHandler) {
    this.maxProcesses = ((maxProcesses == null) || (maxProcesses <= 0))? null:
                              maxProcesses;
    this.maxAge = ((maxAge == null) || (maxAge <= 0))? null: maxAge;
    this.loggerHandler = outputHandler;
    if (this.loggerHandler != null) {
      this.loggerHandler.setLogger(this);
    }
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Clear the Output Handler reference and its assignment
   * to the handler.</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.loggerHandler != null) {
      this.loggerHandler.setLogger(null);
      this.loggerHandler = null;
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * Get the ProcessLog for the ExecProcess
   * @param process the ExecProcess to locate log for.
   * @return the ExecProcess or null if not found
   */
  protected ProcessLog getArchivedLog(IExecProcess process) {
    ProcessLog result = null;
    if (process == null) {
      throw new NullPointerException("The Process is uanssigned.");
    }
    
    String procKey = ExecProcess.getProcessKey(process);
    if (procKey == null) {
      throw new NullPointerException(process.toString() +"'s ProcessId is undefined.");
    }
    
    if ((this.archivedLogs != null) && (this.archivedLogs.containsKey(procKey))) {
      result = this.archivedLogs.get(procKey);
    }
    
    if (result == null) {
      result = new ProcessLog(process);
      if (this.archivedLogs == null) {
        this.archivedLogs = new HashMap<>();
      }
      this.archivedLogs.put(procKey, result);
    }
    return result;
  }
  
  /**
   * Get the ActiveProcessLog for the ExecProcess
   * @param process the ExecProcess to locate log for.
   * @return the ExecProcess or null if not found
   */
  protected ActiveProcessLog getActiveLog(IExecProcess process) {
    ActiveProcessLog result = null;
    if (process == null) {
      throw new NullPointerException("The Process is uanssigned.");
    }
    
    String procKey = ExecProcess.getProcessKey(process);
    if (procKey == null) {
      throw new NullPointerException(process.toString() +"'s ProcessId is undefined.");
    }
    
    if ((this.activeLogs != null) && (this.activeLogs.containsKey(procKey))) {
      result = this.activeLogs.get(procKey);
    }
    
    if (result == null) {
      result = new ActiveProcessLog(process);
      if (this.activeLogs == null) {
        this.activeLogs = new HashMap<>();
      }
      this.activeLogs.put(procKey, result);
    }
    return result;
  }
  
  /**
   * Check if an ActiveLog for the process exists.
   * @param process the IExecProces of interest
   * @return true if a matching entry can be found.
   */
  protected synchronized boolean hasActiveProcessLog(IExecProcess process) {
    String procKey = ExecProcess.getProcessKey(process);
    return ((procKey != null) 
              && (this.activeLogs != null) && (this.activeLogs.containsKey(procKey)));
  }
  
  /**
   * Get a List of ActiveProcessLogs sorted by age 
   * @return a sorted list or null if no logs are available.
   */
  private synchronized List<ActiveProcessLog> getActiveLogs() {
    List<ActiveProcessLog> result = null;
    if ((this.activeLogs != null) && (!this.activeLogs.isEmpty())) {
      result = new ArrayList<>(this.activeLogs.values());
      Comparator<ProcessLog> pComparator = new ProcessLogComparator();
      Collections.sort(result, pComparator);
    }
    return result;
  }
  
  /**
   * Called after adding a new log entry to clear the existing log - removing expired
   * logs (if age limit is set) or the oldest log to fit into the MaxProcesses limit.
   */
  private synchronized void cleanLog() {    
    try {
      /** Sort the Logs by age **/
      List<ActiveProcessLog> activeLogs = this.getActiveLogs();
      if ((activeLogs == null) || (activeLogs.isEmpty())) {
        return;
      }
      
      /** Remove logs that exceeds the age limit - if set **/
      if ((this.maxAge != null) && (this.maxAge > 0)) {
        for (ActiveProcessLog procLog : activeLogs) {
          String procKey = ExecProcess.getProcessKey(procLog);
          if (!procLog.isClosed()) {
            procLog.closeLog();
          }
          procLog.clearLog();
          procLog = null;
          
          if ((procKey != null) && (this.activeLogs.containsKey(procKey))) {
            this.activeLogs.remove(procKey);
          }
        }
      }
      
      if ((this.maxProcesses == null) || (this.maxProcesses <= 0) 
              || (this.activeLogs == null) 
              || (this.activeLogs.size() <= this.maxProcesses)) {
        return;
      }
      
      activeLogs = this.getActiveLogs();
      if ((activeLogs == null) || (activeLogs.size() <= this.maxProcesses)) {
        return;
      }
      
      /** Remove access logs is exceeding MaxcProcesses (if set) - starting at the
       * oldest log **/
      int idx = 0;
      while ((idx < activeLogs.size()) 
              && (this.activeLogs.size() > this.maxProcesses)) {
        ActiveProcessLog procLog = activeLogs.get(idx);
        String procKey = ExecProcess.getProcessKey(procLog);
        if (!procLog.isClosed()) {
          procLog.closeLog();
        }
        procLog.clearLog();
        procLog = null;
        
        if ((procKey != null) && (this.activeLogs.containsKey(procKey))) {
          this.activeLogs.remove(procKey);
        }
        idx++;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.cleanLog Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * Get whether the logger has a log output handler.
   * @return true if the handler is assigned.
   */
  public boolean hasLogOutputHandler() {
    return (this.loggerHandler != null);
  }
  
  /**
   * Get the ProcessLogger's ExecProcessLogHandler
   * @return the assigned handler or null.
   */
  public ExecProcessLogHandler getLogOutputHandler() {
    return this.loggerHandler;
  }
  
  /**
   * Called to enter a new log entry for the specified executable process. This call
   * will be ignored if the process' log is closed or the message is undefined.
   * @param process the calling Executable Process
   * @param logLevel thelogMsglog level
   * @param logMsg the message to log
   */
  public synchronized void log(IExecProcess process, Level logLevel, String logMsg) {
    if ((process == null) && ((logMsg = DataEntry.cleanString(logMsg)) == null)) {
      return;
    }
    try {
      ActiveProcessLog procLog = this.getActiveLog(process);
      if ((procLog != null) && (!procLog.isClosed())) {
        procLog.addLog(logLevel, logMsg);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.log Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } 
  }
  
  /**
   * Convert logMsg and pArgs to string and call Overload 1.
   * @param process the calling Executable Process
   * @param logLevel the log level
   * @param message the message to log
   * @param args Argument to format logMsg
   */
  public synchronized void log(IExecProcess process, Level logLevel, String message, 
          Object[] args){
    try {
      String logMsg = (args == null)? message: String.format(message, args);
      this.log(process, logLevel, logMsg);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.log Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to close the Process' Log - no further entries can be added to this
   * process's log.
   * @param process the calling Executable Process
   */
  public synchronized void closeLog(IExecProcess process){
    if ((process == null) || (!this.hasActiveProcessLog(process))) {
      return;
    }
    
    ActiveProcessLog procLog = this.getActiveLog(process);
    if (procLog != null) {
      procLog.closeLog();
    }
    
    this.cleanLog();
  }
  
  /**
   * Locate and remove the ActiveProcessLog for the specified Process. Before removing 
   * the Log, call is closeLog method if not yet closed and call clearLog to remove
   * all log entries. The call is skipped is the log no longer exists.
   * @param process the process of interest
   */
  public synchronized void clearLog(IExecProcess process) {
    String procKey = ExecProcess.getProcessKey(process);
    if ((procKey != null)
            && (this.activeLogs != null) && (this.activeLogs.containsKey(procKey))) {
      ActiveProcessLog procLog = this.activeLogs.get(procKey);
      if (procLog != null) {
        if (!procLog.isClosed()) {
          procLog.closeLog();
        }
        procLog.clearLog();
        procLog = null;
      }
      this.activeLogs.remove(procKey);   
      this.cleanLog();
    }
  }
  
  /**
   * Called to clear all logs (active and archived) and release all resources.
   */
  public synchronized void clearLog() {
    if ((this.activeLogs != null) && (!this.activeLogs.isEmpty())) {
      for (ActiveProcessLog procLog : this.activeLogs.values()) {
        procLog.clearLog();
      }
      this.activeLogs.clear();
      this.activeLogs = null;
    }
    if ((this.archivedLogs != null) && (!this.archivedLogs.isEmpty())) {
      for (ProcessLog procLog : this.archivedLogs.values()) {
        procLog.clearLog();
      }
      this.archivedLogs.clear();
      this.archivedLogs = null;
    }
  }
  
  /**
   * Get whether the Logger is Empty
   * @return true if both the Active and Archived Logs are empty,
   */
  public final boolean isEmpty() {
    return (((this.activeLogs == null) || (this.archivedLogs.isEmpty()))
            && ((this.archivedLogs == null) || (this.archivedLogs.isEmpty())));
  }
  
  /**
   * Dump the Entire Log to the Server Log. Waits a 100 millisecond to output log.
   */
  public synchronized void dumpLog() {
    String logMsg = this.getLog();
    logger.log(Level.INFO, logMsg);
    try {
      TimeUnit.MILLISECONDS.sleep(100);
    } catch (InterruptedException ex) {}
  }
  
  /**
   * Get the Log as String (multi-line)
   * @return the log as a string
   */
  public synchronized String getLog() {
    String result = "";
    if ((this.activeLogs == null) || (this.activeLogs.isEmpty())) {
      result += "\n-- The Active Process Log is Empty.\n";
    } else {
      result += "\n---------------------------- Start Active Process Log "
              + "----------------------------\n\r";
      for (ActiveProcessLog procLog : this.activeLogs.values()) {
        result += procLog.getLog();
      }
      
      result += "----------------------------- End Active Process Log "
              + "-----------------------------\n\r";
    }
    if ((this.archivedLogs == null) || (this.archivedLogs.isEmpty())) {
      result += "\n-- The Archived Process Log is Empty.\n";
    } else {
      result += "\n---------------------------- Start Archived Process Log "
              + "----------------------------\n\r";
      for (ProcessLog procLog : this.archivedLogs.values()) {
        result += procLog.getLog();
      }
      
      result += "----------------------------- End Archived Process Log "
              + "-----------------------------\n\r";
    }
    return result;
  }
  
  /**
   * Get the Log as an HTML formatted String.
   * @return the log as an HTML string
   */
  public synchronized String getHtmlLog() {    
    Source[] sources = null;
    return this.getHtmlLog(false,sources);
  }
  
  /**
   * Get the Log as an HTML formatted String.
   * @param forPrint true if the log is for print
   * @param logSources the log sources to report (assumed {Source.ACTIVE, Source.ARCHIVED}
   * if null | empty)
   * @return the log as an HTML string
   */
  public synchronized String getHtmlLog(boolean forPrint, Source...logSources) {
    if ((logSources == null) || (logSources.length == 0)) {
      logSources = new Source[]{Source.ACTIVE, Source.ARCHIVED};
    }
    
    String result = (forPrint)? "<div class='bwgProcessPanel forPrint'>\n": 
            "<div class='bwgProcessPanel'>\n";
    if (DataEntry.inArray(Source.ACTIVE, logSources)) {
      if ((this.activeLogs == null) || (this.activeLogs.isEmpty())) {
        result += "<p>The Active Process Log is Empty.</p>\n";
      } else {
        result += "<h3>The Active Process Log</h3>\n";
        List<ActiveProcessLog> logList = new ArrayList<>(this.activeLogs.values());
        Collections.sort(logList);
        for (ActiveProcessLog procLog : logList) {
          result += procLog.getHtmlLog();
        }
      }
    }
    
    if (DataEntry.inArray(Source.ARCHIVED, logSources)) {
      if ((this.archivedLogs == null) || (this.archivedLogs.isEmpty())) {
        result += "<p>The Archived Process Log is Empty.</p>\n";
      } else {
        List<ProcessLog> logList = new ArrayList<>(this.archivedLogs.values());
        Collections.sort(logList);
        result += "<h3>The Archived Process Log</h3>\n";
        for (ProcessLog procLog : logList) {
          result += procLog.getHtmlLog();
        }
      }
    }
    result += "</div>\n";
    return result;
  }
  //</editor-fold>
}
