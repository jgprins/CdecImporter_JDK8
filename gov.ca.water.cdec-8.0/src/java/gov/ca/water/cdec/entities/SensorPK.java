package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.enums.DurationCodes;
import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.StationFacade;
import gov.ca.water.cdec.importers.ImportUtils;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An Entity mapping the CDEC.SENSOR table
 * @author rmanning
 */
@Embeddable
public class SensorPK implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(SensorPK.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_NUM")
  private int sensorNum;
  @Basic(optional = false)
  @NotNull
  @Column(name = "DUR_CODE")
  private String durCode;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Constructors">
  public SensorPK() {
    this.station = null;
    this.durationCode = null;
  }
  
  public SensorPK(String stationId, int sensorNum, String durCode) {
    this.stationId = stationId;
    this.sensorNum = sensorNum;
    this.durCode = durCode;
    this.station = null;
    this.durationCode = null;
  }
  
  public SensorPK(String stationId, int sensorNum, DurationCodes durationCode) {
    this.stationId = stationId;
    this.sensorNum = sensorNum;
    this.durCode = (durationCode == null) ? null : durationCode.name;
    this.station = null;
    this.durationCode = null;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Getters and Setters">

  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = ImportUtils.cleanString(stationId);
    this.station = null;
  }

  public int getSensorNum() {
    return sensorNum;
  }

  public void setSensorNum(int sensorNum) {
    this.sensorNum = sensorNum;
  }

  public String getDurCode() {
    return durCode;
  }

  public void setDurCode(String durCode) {
    this.durCode = ImportUtils.cleanString(durCode);
    this.durationCode = null;
  }
// </editor-fold>
  
// <editor-fold defaultstate="collapsed" desc="Object Overrides">
  
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    hash += (int) sensorNum;
    hash += (durCode != null ? durCode.hashCode() : 0);
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof SensorPK));
    if (result) {
      SensorPK other = (SensorPK) obj;
      result = ((ImportUtils.isEq(this.stationId, other.stationId, true)) &&
                (this.sensorNum == other.sensorNum) &&
                (ImportUtils.isEq(this.durCode, other.durCode, true)));
    }
    return result;
  }
  
  @Override
  public String toString() {
    return "stationId=" + stationId + ", sensorNum=" + sensorNum
            + ", durCode=" + durCode;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Transient|Custom Methods">
  /**
   * Transient placeholder for the Sensor's referenced Station
   */
  @Transient
  private transient Station station = null;

  /**
   * Get the Station referenced by this Sensor
   *
   * @return the Station reference or null if not found or the StationFacade could not be
   * accessed.
   */
  public Station getStation() {
    if ((this.station == null) && (this.stationId != null)) {
      StationFacade facade = CdecBaseFacade.getFacade(StationFacade.class);
      if (facade != null) {
        this.station = facade.find(this.stationId);
      }
    }
    return this.station;
  }
  /**
   * Transient placeholder for the Sensor's DurationCodes reference
   */
  @Transient
  private transient DurationCodes durationCode = null;

  /**
   * Get the Sensor's DurationCodes reference
   *
   * @return the DurationCode reference or null assigned or is invalid
   */
  public DurationCodes getDurationCode() {
    String code = null;
    if ((this.durationCode == null) && 
       ((code = ImportUtils.cleanString(this.durCode)) != null)) {
      try {
        this.durationCode = DurationCodes.fromChar(code);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.getDurationCode Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.durationCode;
  }
  // </editor-fold>
}
