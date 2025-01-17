package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.StationFacade;
import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author rmanning
 */
@Entity
@Table(name = "SNOW_SENSOR_INFO")
@NamedQueries({
  @NamedQuery(name = "SnowSensorInfo.findAll", query = "SELECT s FROM SnowSensorInfo s"),
  @NamedQuery(name = "SnowSensorInfo.findByStationIds", 
      query = "SELECT s FROM SnowSensorInfo s WHERE (s.stationId IN :stationIds)")})
public class SnowSensorInfo implements Serializable {
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "SNOW_SENS_NUM", nullable = false)
  private Integer snowSensNum;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID", nullable = false, length = 3)
  private String stationId;
  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
  @Column(name = "APR1_AVG", precision = 7, scale = 2)
  private Double apr1Avg;
  @Size(max = 5)
  @Column(length = 5)
  private String scheduled;
  @Size(max = 10)
  @Column(name = "OTHER_SENSORS", length = 10)
  private String otherSensors;
  @Size(max = 8)
  @Column(name = "PRECIP_GAGE_TYPE", length = 8)
  private String precipGageType;
  private String wildarea;
  @Size(max = 9)
  @Column(length = 9)
  private String aspect;
  @Size(max = 80)
  @Column(length = 80)
  private String exposure;
  @Column(name = "YEAR_ESTAB")
  private Integer yearEstab;
  @Column(name = "YEAR_ABAND")
  private Integer yearAband;
  @Column(name = "GROUND_TRUTH_AGENCY")
  private Integer groundTruthAgency;
  @Column(name = "OPERATOR_AGENCY")
  private Integer operatorAgency;
  @Column(name = "MAINTENANCE_AGENCY")
  private Integer maintenanceAgency;

  public SnowSensorInfo() {
  }
  
  public SnowSensorInfo(Integer snowSensNum) {
    this.snowSensNum = snowSensNum;
  }

  public Integer getSnowSensNum() {
    return snowSensNum;
  }

  public void setSnowSensNum(Integer snowSensNum) {
    this.snowSensNum = snowSensNum;
  }
  
  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    if (((this.stationId == null) && (stationId != null)) ||
            ((this.stationId != null) && (!this.stationId.equals(stationId)))) {
      this.stationId = stationId;
      this.snoStation = null;
    }
  }
  
  public Double getApr1Avg() {
    return apr1Avg;
  }

  public void setApr1Avg(Double apr1Avg) {
    this.apr1Avg = apr1Avg;
  }

  public String getScheduled() {
    return scheduled;
  }

  public void setScheduled(String scheduled) {
    this.scheduled = scheduled;
  }

  public String getOtherSensors() {
    return otherSensors;
  }

  public void setOtherSensors(String otherSensors) {
    this.otherSensors = otherSensors;
  }

  public String getPrecipGageType() {
    return precipGageType;
  }

  public void setPrecipGageType(String precipGageType) {
    this.precipGageType = precipGageType;
  }

  public String getWildarea() {
    return wildarea;
  }

  public void setWildarea(String wildarea) {
    this.wildarea = wildarea;
  }

  public String getAspect() {
    return aspect;
  }

  public void setAspect(String aspect) {
    this.aspect = aspect;
  }

  public String getExposure() {
    return exposure;
  }

  public void setExposure(String exposure) {
    this.exposure = exposure;
  }

  public Integer getYearEstab() {
    return yearEstab;
  }

  public void setYearEstab(Integer yearEstab) {
    this.yearEstab = yearEstab;
  }

  public Integer getYearAband() {
    return yearAband;
  }

  public void setYearAband(Integer yearAband) {
    this.yearAband = yearAband;
  }

  public Integer getGroundTruthAgency() {
    return groundTruthAgency;
  }

  public void setGroundTruthAgency(Integer groundTruthAgency) {
    this.groundTruthAgency = groundTruthAgency;
  }

  public Integer getOperatorAgency() {
    return operatorAgency;
  }

  public void setOperatorAgency(Integer operatorAgency) {
    this.operatorAgency = operatorAgency;
  }

  public Integer getMaintenanceAgency() {
    return maintenanceAgency;
  }

  public void setMaintenanceAgency(Integer maintenanceAgency) {
    this.maintenanceAgency = maintenanceAgency;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (snowSensNum != null ? snowSensNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof SnowSensorInfo)) {
      return false;
    }
    SnowSensorInfo other = (SnowSensorInfo) object;
    if ((this.snowSensNum == null && other.snowSensNum != null) || (this.snowSensNum != null && !this.snowSensNum.equals(other.snowSensNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "SnowSensorInfo[ snowSensNum=" + snowSensNum + " ]";
  }
  
  //<editor-fold defaultstate="collapsed" desc="Transient/Cached Properties">
  @Transient
  private Station snoStation;
  
  /**
   * The Sensor's Cached Station Reference
   * @return the cached Station reference or null if undefined.
   */
  public Station getSnoStation() {
    if ((this.snoStation == null) && (this.stationId != null)) {
      StationFacade facade = CdecBaseFacade.getFacade(StationFacade.class);
      if (facade != null) {
        this.snoStation = facade.find(this.stationId);
      }
    }
    return this.snoStation;
  }
//</editor-fold>
}
