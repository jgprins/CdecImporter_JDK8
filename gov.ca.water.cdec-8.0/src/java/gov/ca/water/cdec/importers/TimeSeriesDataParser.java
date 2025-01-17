package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import java.io.Serializable;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The TimeSeriesDataParser Class is employed by the {@linkplain DataDownloader} to 
 * parse the downloaded data string to a TimeSeries.
 * @author kprins
 */
public abstract class TimeSeriesDataParser<TEntity extends Serializable,
                                    TStepKey extends TimeStepKey<TStepKey>,
                                    TMap extends TimeSeriesMap<TEntity, TStepKey>>
                                    implements Serializable { 

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = 
                                  Logger.getLogger(TimeSeriesDataParser.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Fields">
  /**
   * The SHEDDataParser's Default TimeZone [TimeZone.getTimeZone("PST")].
   */
  public static TimeZone defaultTimeZone = TimeZone.getTimeZone("PST");
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for a LoadError - an error encountered during the TimeSeries values
   * initiation/load/parse process.
   */
  private String parseError;
  /**
   * Placeholder for the TimeZone of the SHEF Data. (Default={@linkplain 
   * #defaultTimeZone})
   */
  private TimeZone timeZone;
  //</editor-fold>  
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  protected TimeSeriesDataParser() {
    super();  
    this.parseError = null;
    this.timeZone = null;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Set the Parser's TimeZone Setting
   * @param pTimeZone the TimeZone of the downloaded data (can be null to accept
   * the default={@linkplain #defaultTimeZone})
   */
  public void setTimeZone(TimeZone pTimeZone) {
    this.timeZone = pTimeZone;
  }
  
  /**
   * Get the Parser's TimeZone Setting
   * @return the assigned TimeZone or {@linkplain #defaultTimeZone}) if unassigned.
   */
  public TimeZone getTimeZone() {
    return (this.timeZone == null)? TimeSeriesDataParser.defaultTimeZone: this.timeZone;
  }

  /**
   * Get whether an previous attempt generates a Parse error - this will prevent future
   * attempts to read the same data source that course errors.
   * @return boolean
   */
  public final boolean hasParseError() {
    return (this.parseError != null);
  }
  
  /**
   * Get the Parse Error
   * @return the assigned error or null if no error was reported..
   */
  public final String getParseError() {
    return this.parseError;
  }
  
  /**
   * <p>Assign a new pass error message. The error message will be appended to any
   * previously assigned messages. The message is ignored if empty|null. The
   * parse error will automatically be logged with a SEVERE log level.</p>
   * <p><b>NOTE:</b> Assigning a parse error will cause the download process to stop,
   * which could terminate the scheduling of additional downloads. To record a parse
   * warning, call the {@linkplain #log(java.util.logging.Level, java.lang.String) log}
   * method directly.</p>
   * @param errMsg a new error message
   */
  protected final void setParseError(String errMsg) {
    errMsg = ImportUtils.cleanString(errMsg);
    if (errMsg != null) {
      if (this.parseError == null) {
        this.parseError = "Parse Errors:";
      } 
      this.parseError += "\n\t- " + errMsg;
    }
  }
  
  /**
   * Clear any assigned pass error messages
   */
  public final void clearParseError() {
    this.parseError = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * <p>Called by to update the data record form a data string downloaded from an
   * external source. It is assumed that prior to this call the data record
   * has been reset. </p>
   * <p>It resets the parseError before calling {@linkplain #onParseData(java.lang.String,
   * gov.ca.water.cdec.core.TimeSeriesMap, gov.ca.water.cdec.core.CdecSensorInfo)  
   * this.onParseData} method. The latter will only be called if the data string is not
   * empty, timeSeriesMap, and the sensorInfo are assigned.</p>
   * <p>Errors are trapped and assigned as ParseErrors, which could terminate the
   * download process The record NOT will be cleared if an error occur during the
   * parsing.</p>
   * @param dataStr the Text string containing the Data is in the supported format
   * @param timeSeriesMap the The TiemSeriesMap to update
   * @param sensorInfo the CDEC Information on the sensor
   */
  public final boolean parseData(String dataStr, TMap timeSeriesMap, 
                                                           CdecSensorInfo sensorInfo) {
    this.clearParseError();
    /** Skip the process if the record or data staring is unassigned **/
    dataStr = ImportUtils.cleanString(dataStr);
    if ((timeSeriesMap != null) || (dataStr != null)) {
      try {
        this.onParseData(dataStr, timeSeriesMap, sensorInfo); 
      } catch (Exception exp) {
        this.setParseError(exp.getMessage());
      }
    }
    return (!this.hasParseError());
  }
//</editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">  
  /**
   * <p>ABSTRACT: Must be implemented to read the data from the <tt>dataStr</tt>. Called 
   * by the {@linkplain #parseData(java.lang.String, gov.ca.water.cdec.core.TimeSeriesMap,
   * gov.ca.water.cdec.core.CdecSensorInfo) this.parseData} method to handle the custom 
   * parsing of the input Data String into a list of records that must be added to 
   * the timeSeriesMap.</p>
   * <p><b>NOTE:</b> Non-fatal parsing errors or warnings should be assigned as 
   * {@linkplain #setParseError(java.lang.String) parseErrors}.</p>
   * @param dataStr the data string to parse
   * @return a list of record Strings
   * @throws Exception if parsing fails
   */
  protected abstract void onParseData(String dataStr, TMap timeSeriesMap, 
                                           CdecSensorInfo sensorInfo) throws Exception;
  // </editor-fold>
}
