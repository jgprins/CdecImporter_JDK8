package gov.ca.water.cdec.core;

import gov.ca.water.cdec.enums.DurationCodes;
 
/**
 * An Enum for the Sensor type used by the Water Supply Forecast, which include:<ul>
 * <li>{@linkplain #DailyPrecip} - Daily Precipitation</li>
 * <li>{@linkplain #MonthlyPrecip} - Monthly Precipitation</li>
 * <li>{@linkplain #DailySnow} - Daily Precipitation</li>
 * <li>{@linkplain #MonthlySnow} - Monthly Snow Water Content</li>
 * <li>{@linkplain #MonthlySnow} - Monthly Full Natural Inflow</li>
 * <li>{@linkplain #LakeLevel} - Monthly Lake Levels</li>
 * <li>{@linkplain #MonthlyResStorage} - Monthly Reservoir Storage</li>
 * <li>{@linkplain #DailyFNF} - Daily Reservoir FNF</li>
 * <li>{@linkplain #DailyResIn} - Daily Reservoir Inflow</li>
 * <li>{@linkplain #DailyResOut} - Daily Reservoir Outflow</li>
 * <li>{@linkplain #DailyTOC} - Daily Reservoir TOC</li>
 * <li>{@linkplain #DailyResStorage} - Daily Reservoir Storage</li>
 * <li>{@linkplain #AJ10} - AJ Forecast 10% Exceedence</li>
 * <li>{@linkplain #AJ50} - AJ Forecast 50% Exceedence</li>
 * <li>{@linkplain #AJ90} - AJ Forecast 90% Exceedence</li>
 * <li>{@linkplain #WY10} - WY Forecast 10% Exceedence</li>
 * <li>{@linkplain #WY50} - WY Forecast 50% Exceedence</li>
 * <li>{@linkplain #WY90} - WY Forecast 90% Exceedence</li>
 * </ul>
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum CdecSensorTypes {
  
  //<editor-fold defaultstate="collapsed" desc="Enum Values">
  /**
   * Daily Precipitation (value=0)
   */
  DailyPrecip(0,45,"D","DPCP", "Incremental Daily Precipitation"),
  /**
   * Monthly Precipitation (value=1)
   */
  MonthlyPrecip(1,2,"M","MPCP", "Accumulated Monthly Precipitation"),
  /**
   * Daily Snow Water Content (value=2)
   */
  DailySnow(2,82,"D","DSNO", "Daily Snow Water Content"),
  /**
   * Monthly Snow Water Content (value=3)
   */
  MonthlySnow(3,3,"M","MSNO", "Observed Snow Water Content"),
  /**
   * Daily Full Natural Inflow (value=4)
   */
  MonthlyFNF(4,65,"M","MFNF","Monthly Full Natural Flow"),  
  /**
   * Monthly Lake level (value=5)
   */
  LakeLevel(5,42,"M","MLL","Monthly Lake level"),  
  /**
   * Monthly Average Reservoir Storage (value=6)
   */
  MonthlyResStorage(6,15,"M", "MRSTO", "Reservoir Storage"),   
  /**
   * Daily Full Natural Inflow (value=6)
   */
  DailyFNF(7,8,"D","DFNF","Daily Full Natural Flow"),  
  /**
   * Daily Reservoir In Flow (value=8)
   */
  DailyResIn(8,76,"D","DRESIN","Daily Reservoir Inflow"),    
  /**
   * Daily Reservoir Out Flow  (value=9)
   */
  DailyResOut(9,23,"D","DRESOUT","Daily Reservoir Releases"),   
  /**
   * Daily Reservoir TOC (Top-of-Conservation) (value=10)
   */
  DailyTOC(10,94,"D","DTOC","Daily Reservoir TOC"),
  /**
   * Daily Daily Reservoir Storage (RSTOD) (value=11)
   */
  DailyResStorage(11,15,"D","DRSTO","Daily Reservoir Storage"),
  /**
   * Daily Adjusted Precipitation (value=12)
   */
  DailyPcpAdj(12,80,"D","DPCPADJ", "Incremental Adjusted Daily Precipitation"),
  /**
   * Monthly Adjusted Precipitation (value=13)
   */
  MonthlyPcpAdj(12,50,"M","MPCPADJ", "Accumulated Adjusted Monthly Precipitation"),
  /**
   * A-J 10% FORECAST EXCEEDENCE  (value=20) 
   */
  AJ10(20,260,"E", "AJ-10", "A-J 10% FORECAST EXCEEDENCE"),
  /**
   * A-J 50% FORECAST EXCEEDENCE  (value=21)
   */
  AJ50(21,261,"E", "AJ-50", "A-J 50% FORECAST EXCEEDENCE"),
  /**
   * A-J 90% FORECAST EXCEEDENCE  (value=22)
   */
  AJ90(22,262,"E", "AJ-90", "A-J 90% FORECAST EXCEEDENCE"),
  /**
   * WY 10% FORECAST EXCEEDENCE  (value=23)
   */
  WY10(23,263,"E", "WY-10", "WY 10% FORECAST EXCEEDENCE"),
  /**
   * WY 50% FORECAST EXCEEDENCE  (value=24)
   */
  WY50(24,264,"E", "WY-50", "WY 50% FORECAST EXCEEDENCE"),
  /**
   * WY 90% FORECAST EXCEEDENCE  (value=25)
   */
  WY90(25,265,"E", "WY-90", "WY 90% FORECAST EXCEEDENCE");
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * The CDEC Sensor Type Value (unique for each type)
   */
  public final int value;
  /**
   * The CDEC Sensor Number for the sensor type
   */
  public final int sensorNo;
  /**
   * The Sensor Acronym
   */
  public final String acronym;
  /**
   * The Sensor's Duration Code
   */
  public final String durCode;
  /**
   * The sensor Description
   */
  public final String description;
  
  /**
   * Private Constructor
   */
  private CdecSensorTypes(int value, int sensorNo, String durCode, 
                                                  String acronym, String description) {
    this.value = value;
    this.sensorNo = sensorNo;
    this.durCode = durCode;
    this.acronym = acronym;
    this.description = description;
  }
  
  /**
   * Get the Duration Code Enum for the Sensor Type's Duration
   * @return DurationCodes.fromChar(this.durCode)
   */
  public DurationCodes getDurationCode() {
    return DurationCodes.fromChar(this.durCode);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the CdecSensorTypes by its Ordinal value
   * @param enumValue the Type.value to search for
   * @return the matching CdecSensorTypes value or null if no match is found.
   */
  public static CdecSensorTypes valueOf(int enumValue) {
    CdecSensorTypes result = null;
    for (CdecSensorTypes type : CdecSensorTypes.values()) {
      if (enumValue == type.value) {
        result = type;
        break;
      }
    }
    return result;
  }
  
  /**
   * Get the CdecSensorTypes by its sensorNum and durCode
   * @param sensorNo the sensor number
   * @param durCode the duration code
   * @return the matching CdecSensorTypes value or null if no match is found.
   */
  public static CdecSensorTypes bySensorNoAndDurCode(Integer sensorNo, String durCode) {
    CdecSensorTypes result = null;
    durCode = ((durCode == null) || ("".equals((durCode = durCode.trim()))))? null:
                durCode.toUpperCase();
    if ((sensorNo != null) && (durCode != null)) {
      for (CdecSensorTypes type : CdecSensorTypes.values()) {
        if ((sensorNo.equals(type.sensorNo)) && (durCode.equals(type.durCode))) {
          result = type;
          break;
        }
      }
    }
    return result;
  }
  /**
   * Get an Array of CdecSensorTypes representing the WS Forecast sensors
   * @return {AJ10, AJ50, AJ90, WY10, WY50, WY90}
   */
  public static CdecSensorTypes[] WsFcastSensors() {
    return new CdecSensorTypes[]{CdecSensorTypes.AJ10, CdecSensorTypes.AJ50,
                                 CdecSensorTypes.AJ90, CdecSensorTypes.WY10,
                                 CdecSensorTypes.WY50, CdecSensorTypes.WY90};
  }
  //</editor-fold>
}
 