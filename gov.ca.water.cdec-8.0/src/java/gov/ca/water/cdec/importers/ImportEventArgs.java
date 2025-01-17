package gov.ca.water.cdec.importers;

import java.util.logging.Level;

/**
 * Event Arguments to pass with the firing of the CDEC Import Processing event.
 * It supports a log message.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ImportEventArgs {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * LogLevel for logging the message
   */
  private Level logLevel;
  /**
   * The message 
   */
  private String logMessage;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  - without a message
   */
  public ImportEventArgs() {
    this(null, null);
  }
  
  /**
   * Public Constructor  - without a log message
   * @param logMessage the message to pass on (can be null)
   */
  public ImportEventArgs(Level logLevel, String logMessage) {
    super();  
    this.logMessage = logMessage;
    this.logLevel = (logLevel == null)? Level.OFF: logLevel;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Log Message 
   * @return the assigned message (can be null)
   */
  public String getLogMessage() {
    return this.logMessage;
  }
  
  /**
   * Get the Log Level 
   * @return the assigned Level (Default = OFF)
   */
  public Level getLogLevel() {
    return this.logLevel;
  }
  
  /**
   * Get whether the EventArgs contains a log message
   * @return ((this.logMessage != null) && (this.logLevel !=  Level.OFF))
   */
  public boolean hasMessage() {
    return ((this.logMessage != null) && (this.logLevel !=  Level.OFF));
  }
  // </editor-fold>
}
