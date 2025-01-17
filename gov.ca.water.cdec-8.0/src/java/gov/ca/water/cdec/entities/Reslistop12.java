package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "RESLISTOP12")
@NamedQueries({
  @NamedQuery(name = "Reslistop12.findAll", query = "SELECT r FROM Reslistop12 r")})
public class Reslistop12 implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_ID")
  private int sensorId;
  @Basic(optional = false)
  @NotNull
  private Integer ord;
  @Size(max = 60)
  @Column(name = "DRAIN_AREA")
  private String drainArea;
  @Basic(optional = false)
  @NotNull
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;
  @Size(max = 60)
  @Column(name = "DRAIN_SUB_AREA")
  private String drainSubArea;
  @Size(max = 60)
  @Column(name = "RIV_BASIN")
  private String rivBasin;
  @Size(max = 60)
  @Column(name = "LOCAL_BASIN")
  private String localBasin;
  @Size(max = 60)
  private String reservoir;
  @Id
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Size(max = 60)
  private String operator;
  @Column(name = "ST_CODE")
  @Size(max = 1)
  private String stCode;
  @Size(max = 5)
  private String notes;
  @Column(name = "ST_TYPE")
  @Size(max = 1)
  private String stType;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public Reslistop12() {
    super();  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public int getSensorId() {
    return sensorId;
  }

  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }

  public Integer getOrd() {
    return ord;
  }

  public void setOrd(Integer ord) {
    this.ord = ord;
  }

  public String getDrainArea() {
    return drainArea;
  }

  public void setDrainArea(String drainArea) {
    this.drainArea = drainArea;
  }

  public Integer getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }

  public String getDrainSubArea() {
    return drainSubArea;
  }

  public void setDrainSubArea(String drainSubArea) {
    this.drainSubArea = drainSubArea;
  }

  public String getRivBasin() {
    return rivBasin;
  }

  public void setRivBasin(String rivBasin) {
    this.rivBasin = rivBasin;
  }

  public String getLocalBasin() {
    return localBasin;
  }

  public void setLocalBasin(String localBasin) {
    this.localBasin = localBasin;
  }

  public String getReservoir() {
    return reservoir;
  }

  public void setReservoir(String reservoir) {
    this.reservoir = reservoir;
  }

  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public String getOperator() {
    return operator;
  }

  public void setOperator(String operator) {
    this.operator = operator;
  }

  public String getStCode() {
    return stCode;
  }

  public void setStCode(String stCode) {
    this.stCode = stCode;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getStType() {
    return stType;
  }

  public void setStType(String stType) {
    this.stType = stType;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Reslistop12)) {
      return false;
    }
    Reslistop12 other = (Reslistop12) object;
    if ((this.stationId == null && other.stationId != null) || 
            (this.stationId != null && !this.stationId.equals(other.stationId))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Reslistop12[ stationId=" + stationId + " ]";
  }
  // </editor-fold>
}
