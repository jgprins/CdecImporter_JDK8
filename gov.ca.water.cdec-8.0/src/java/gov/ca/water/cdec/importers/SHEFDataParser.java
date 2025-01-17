package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

/**
 * A TimeSeriesDataParser for Parsing TimeSeries Data from SHEF-format text stings. It currently 
 * only supports SHEF.A formats.
 * @author kprins
 */
public class SHEFDataParser<TEntity extends Serializable,
                                  TStepKey extends TimeStepKey<TStepKey>,
                                  TMap extends TimeSeriesMap<TEntity, TStepKey>> 
                                  extends TimeSeriesDataParser<TEntity, TStepKey, TMap> {
  
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the SHEFDataParser's DateFormat
   */
  private ImportDataFormats dataFormat;
  /**
   * Placeholder for TimeZone of the downloaded data (optional - can be null)
   */
  private TimeZone timeZone;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor 
   * @param eDataFormat the Parser's Data Format
   */
  public SHEFDataParser(ImportDataFormats dataFormat) {
    this(dataFormat,null);
  }
  
  /**
   * Overload 2:Public Constructor with a DataFormat and TimeZone reference
   * @param eDataFormat the Parser's Data Format
   * @param timeZone the TimeZone of the downloaded data (can be null to accept
   * the default={@linkplain #defaultTimeZone})
   */
  public SHEFDataParser(ImportDataFormats dataFormat, TimeZone timeZone) {
    super();    
    if (((dataFormat == null) || (dataFormat.equals(ImportDataFormats.NONE)))) {
      throw new NullPointerException("The Import DataFormat is undefined or NONE");
    }      
    this.dataFormat = dataFormat;
    this.timeZone = timeZone;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="TimeSeriesDataParser Override">
  @Override
  protected void onParseData(String dataStr, TMap timeSeriesMap, 
                                        CdecSensorInfo sensorInfo) throws Exception {
    try {      
      String formatId = this.dataFormat.formatId; 
      TimeZone timeZn = this.getTimeZone();        

      String[] shefLines = (dataStr == null) ? null : dataStr.split("\n");
      if ((shefLines != null) && (shefLines.length > 0)) {
        for (String recordStr : shefLines) {
          recordStr = ImportUtils.cleanString(recordStr);
          if ((recordStr != null) && (!recordStr.startsWith(formatId))) {
            if (this.dataFormat.equals(ImportDataFormats.CDEC_SHEF_A)) {
              this.onParseDataA(recordStr, timeSeriesMap, timeZn);
            } else if (this.dataFormat == ImportDataFormats.CDEC_SHEF_B) {
              this.onParseDataB(recordStr, timeSeriesMap, timeZn);
            } else if (this.dataFormat == ImportDataFormats.CDEC_SHEF_E) {
              this.onParseDataE(recordStr, timeSeriesMap, timeZn);
            }
          }
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".onParseRecord Error:\n " + pExp.getMessage());
    }
  }  

  /**
   * Parse the Record from sShefLine. The Method returns the Date-Time if the Shef
   * Record has a non-missing value.
   * @param recordStr String
   * @throws Exception
   */
  private void onParseDataA(String recordStr, TMap timeSeriesMap, TimeZone timeZn) throws Exception {
    Date actualDt = null;
    Double obsValue = null;
    String flag = null;

    try {
      String[] recValues = recordStr.split(" ");
      if (recValues.length != 7) {
        throw new Exception("Line[" + recordStr 
                                          + "]'s is not a valid Type-A SHEF Format.");
      }

      String sDate = ImportUtils.cleanString(recValues[2]);
      if (sDate.length() != 8) {
        throw new Exception("Date[" + sDate 
                                          + "]'s is not a valid DateFormat[yyyyMMdd].");
      }

      String sTime = ImportUtils.cleanString(recValues[4]);
      sTime = ((sTime == null) || (sTime.length() < 4)) ? sTime
              : sTime.substring(sTime.length() - 4);
      if ((sTime == null) || (sTime.length() != 4)) {
        throw new Exception("Time[" + sTime + "]'s is not a valid TimeFormat[hhmm].");
      }

      sDate += " " + sTime.substring(0, 2);
      sDate += ":" + sTime.substring(2);
      actualDt = ImportUtils.dateFromString(sDate, "yyyyMMdd HH:mm", timeZn);

      String strVal = ImportUtils.cleanString(recValues[6]);
      if (strVal == null) {
        flag = "m";
      } else {
        flag = ImportUtils.getDataFlag(strVal);
        obsValue = ImportUtils.toValue(strVal, Double.class);
        if ((obsValue != null) && (obsValue <= -9990.0d)) {
          obsValue = null;
          flag = "m";
        }
      }
    } catch (Exception pExp) {
      actualDt = null;
    }

    if (actualDt != null) {
      TEntity record = timeSeriesMap.newRecord(actualDt);
      timeSeriesMap.setRecordObsData(record, actualDt, obsValue, flag);
      timeSeriesMap.add(record);
    }
  }

  private void onParseDataB(String recordStr, TMap timeSeriesMap, TimeZone timeZn) throws Exception {
  }

  private void onParseDataE(String recordStr, TMap timeSeriesMap, TimeZone timeZn) throws Exception  {
  }
  //</editor-fold>

  
}
