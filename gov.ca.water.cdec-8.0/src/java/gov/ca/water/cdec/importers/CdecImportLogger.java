package gov.ca.water.cdec.importers;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Log manager for handling the logging of message send by the importer processes.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class CdecImportLogger {
  
  //<editor-fold defaultstate="collapsed" desc="Private ImportLoggerDelegate class">
  /**
   * An ImportEventDelegate to post the log message when an event is fired.
   */
  private class ImportLoggerDelegate extends ImportEventDelegate {
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public ImportLoggerDelegate(CdecImportLogger listener) {
      super(listener);
    }
    // </editor-fold>
    
    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(Object sender, ImportEventArgs args) {
      CdecImportLogger listener = (CdecImportLogger) this.getListener();
      if ((listener != null) && (sender != null) && (args.hasMessage())) {
        listener.log(args.getLogLevel(), sender.toString(), args.getLogMessage());
      }
    }
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecImportLogger.class.getName());
  //</editor-fold>        
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public CdecImportLogger() {
    super();  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Add the Logger as an event listener to the <tt>processor</tt>
   * @param processor the Import Processor reference - ignored if null.
   */
  public void addAsLogEventListener(ImportProcessor processor) {
    if (processor != null)  {
      processor.LogMessage.add(new ImportLoggerDelegate(this));
    }
  }
  
  /**
   * Post the Log Message
   * @param logLevel the logLevel
   * @param process the process name
   * @param message the message
   */
  public void log(Level logLevel, String process, String message) {
    logger.log(logLevel, "{0}: {1}", new Object[]{process, message});
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
