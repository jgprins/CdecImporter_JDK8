package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.StationFacade;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * An Entity mapping the CDEC.SENSOR table
 * @author kprins
 */
@Entity
@Table(name = "SENSOR")
@NamedQueries({
  @NamedQuery(name = "Sensor.findAll", query = "SELECT s FROM Sensor s"),
  @NamedQuery(name = "Sensor.findStationSensor", query = "SELECT s FROM Sensor s "
        + "WHERE ((s.sensorPK.stationId = :stationId) AND "
        + "(s.sensorPK.sensorNum = :sensorNum) AND "
        + "(s.sensorPK.durCode = :durCode))"),
  @NamedQuery(name = "Sensor.findBySensorType", query = "SELECT s FROM Sensor s "
        + "WHERE ((s.sensorPK.sensorNum = :sensorNum) AND "
        + "(s.sensorPK.durCode = :durCode))"),
  @NamedQuery(name = "Sensor.sensorInfo",
          query = "SELECT m FROM Sensor m "
          + "WHERE ((m.sensorPK.stationId in :stationIds) "
          + "AND (m.sensorPK.sensorNum = :sensorNum) "
          + "AND (m.sensorPK.durCode = :durCode))"),
  @NamedQuery(name = "Sensor.findBySensorId", query = "SELECT s FROM Sensor s "
        + "WHERE (s.sensorId = :sensorId)"),
  @NamedQuery(name = "Sensor.findByStationId", query = "SELECT s FROM Sensor s "
        + "WHERE (s.sensorPK.stationId = :stationId) "
          + "ORDER BY s.sensorPK.sensorNum, s.sensorPK.durCode")})
public class Sensor implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  protected SensorPK sensorPK;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_ID")
  private int sensorId;
  @Column(name = "START_DATE")
  @Temporal(TemporalType.DATE)
  private Date startDate;
  @Column(name = "END_DATE")
  @Temporal(TemporalType.DATE)
  private Date endDate;
  @Column(name = "RANGE_MIN")
  private Double rangeMin;
  @Column(name = "RANGE_MAX")
  private Double rangeMax;
  @Column(name = "COLLECT_NUM")
  private Integer collectNum;
  
  public Sensor() {
  }

  public Sensor(SensorPK sensorPK) {
    this.sensorPK = sensorPK;
  }

  public Sensor(SensorPK sensorPK, int sensorId) {
    this.sensorPK = sensorPK;
    this.sensorId = sensorId;
  }

  public Sensor(String stationId, short sensorNum, String durCode) {
    this.sensorPK = new SensorPK(stationId, sensorNum, durCode);
  }

  public SensorPK getSensorPK() {
    return sensorPK;
  }

  public void setSensorPK(SensorPK sensorPK) {
    this.sensorPK = sensorPK;
  }

  public int getSensorId() {
    return sensorId;
  }
  
  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public Double getRangeMin() {
    return rangeMin;
  }

  public void setRangeMin(Double rangeMin) {
    this.rangeMin = rangeMin;
  }

  public Double getRangeMax() {
    return rangeMax;
  }

  public void setRangeMax(Double rangeMax) {
    this.rangeMax = rangeMax;
  }

  public Integer getCollectNum() {
    return collectNum;
  }

  public void setCollectNum(Integer collectNum) {
    this.collectNum = collectNum;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (sensorPK != null ? sensorPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof Sensor))) {
      return false;
    }
    Sensor other = (Sensor) object;
    if ((this.sensorPK == null && other.sensorPK != null) || 
            (this.sensorPK != null && !this.sensorPK.equals(other.sensorPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.Sensor[PK:" + sensorPK + " ]";
  }

  //<editor-fold defaultstate="collapsed" desc="Transient/Cached Properties">
  @Transient
  private Station station;
  
  /**
   * The Sensor's Cached Station Reference
   * @return the cached Station reference or null if undefined.
   */
  public Station getStation() {
    String stationId = null;
    if ((this.station == null) && (this.sensorPK != null) &&
            ((stationId = this.sensorPK.getStationId()) != null)) {
      StationFacade facade = CdecBaseFacade.getFacade(StationFacade.class);
      if (facade != null) {
        this.station = facade.find(stationId);
      }
    }
    return this.station;
  }
//</editor-fold>
}
