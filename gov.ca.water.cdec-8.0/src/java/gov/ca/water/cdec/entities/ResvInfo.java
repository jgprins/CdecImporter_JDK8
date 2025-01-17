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
@Table(name = "RESV_INFO")
@NamedQueries({
  @NamedQuery(name = "ResvInfo.findAll", query = "SELECT r FROM ResvInfo r"),
  @NamedQuery(name = "ResvInfo.findByStationIds", 
              query = "SELECT s FROM ResvInfo s "
                    + "WHERE (s.stationId IN :stationIds)")})
public class ResvInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID", nullable = false, length = 3)
  private String stationId;
  @Size(max = 30)
  @Column(name = "DAM_NAME", length = 30)
  private String damName;
  @Size(max = 30)
  @Column(name = "LAKE_NAME", length = 30)
  private String lakeName;
  @Size(max = 30)
  @Column(name = "STREAM_NAME", length = 30)
  private String streamName;
  private Integer capacity;
  @Column(name = "YEAR_COMP")
  private Integer yearComp;
  @Column(name = "START_YR_FILL")
  private Integer startYrFill;
  @Column(name = "START_YR_AVG")
  private Integer startYrAvg;
  @Column(name = "END_YR_AVG")
  private Integer endYrAvg;
  @Column(name = "JAN_AVG")
  private Integer janAvg;
  @Column(name = "FEB_AVG")
  private Integer febAvg;
  @Column(name = "MAR_AVG")
  private Integer marAvg;
  @Column(name = "APR_AVG")
  private Integer aprAvg;
  @Column(name = "MAY_AVG")
  private Integer mayAvg;
  @Column(name = "JUN_AVG")
  private Integer junAvg;
  @Column(name = "JUL_AVG")
  private Integer julAvg;
  @Column(name = "AUG_AVG")
  private Integer augAvg;
  @Column(name = "SEP_AVG")
  private Integer sepAvg;
  @Column(name = "OCT_AVG")
  private Integer octAvg;
  @Column(name = "NOV_AVG")
  private Integer novAvg;
  @Column(name = "DEC_AVG")
  private Integer decAvg;
  
  public ResvInfo() {
  }

  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public String getDamName() {
    return damName;
  }

  public void setDamName(String damName) {
    this.damName = damName;
  }

  public String getLakeName() {
    return lakeName;
  }

  public void setLakeName(String lakeName) {
    this.lakeName = lakeName;
  }

  public String getStreamName() {
    return streamName;
  }

  public void setStreamName(String streamName) {
    this.streamName = streamName;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public Integer getYearComp() {
    return yearComp;
  }

  public void setYearComp(Integer yearComp) {
    this.yearComp = yearComp;
  }

  public Integer getStartYrFill() {
    return startYrFill;
  }

  public void setStartYrFill(Integer startYrFill) {
    this.startYrFill = startYrFill;
  }

  public Integer getStartYrAvg() {
    return startYrAvg;
  }

  public void setStartYrAvg(Integer startYrAvg) {
    this.startYrAvg = startYrAvg;
  }

  public Integer getEndYrAvg() {
    return endYrAvg;
  }

  public void setEndYrAvg(Integer endYrAvg) {
    this.endYrAvg = endYrAvg;
  }

  public Integer getJanAvg() {
    return janAvg;
  }

  public void setJanAvg(Integer janAvg) {
    this.janAvg = janAvg;
  }

  public Integer getFebAvg() {
    return febAvg;
  }

  public void setFebAvg(Integer febAvg) {
    this.febAvg = febAvg;
  }

  public Integer getMarAvg() {
    return marAvg;
  }

  public void setMarAvg(Integer marAvg) {
    this.marAvg = marAvg;
  }

  public Integer getAprAvg() {
    return aprAvg;
  }

  public void setAprAvg(Integer aprAvg) {
    this.aprAvg = aprAvg;
  }

  public Integer getMayAvg() {
    return mayAvg;
  }

  public void setMayAvg(Integer mayAvg) {
    this.mayAvg = mayAvg;
  }

  public Integer getJunAvg() {
    return junAvg;
  }

  public void setJunAvg(Integer junAvg) {
    this.junAvg = junAvg;
  }

  public Integer getJulAvg() {
    return julAvg;
  }

  public void setJulAvg(Integer julAvg) {
    this.julAvg = julAvg;
  }

  public Integer getAugAvg() {
    return augAvg;
  }

  public void setAugAvg(Integer augAvg) {
    this.augAvg = augAvg;
  }

  public Integer getSepAvg() {
    return sepAvg;
  }

  public void setSepAvg(Integer sepAvg) {
    this.sepAvg = sepAvg;
  }

  public Integer getOctAvg() {
    return octAvg;
  }

  public void setOctAvg(Integer octAvg) {
    this.octAvg = octAvg;
  }

  public Integer getNovAvg() {
    return novAvg;
  }

  public void setNovAvg(Integer novAvg) {
    this.novAvg = novAvg;
  }

  public Integer getDecAvg() {
    return decAvg;
  }

  public void setDecAvg(Integer decAvg) {
    this.decAvg = decAvg;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ResvInfo)) {
      return false;
    }
    ResvInfo other = (ResvInfo) object;
    if ((this.stationId == null && other.stationId != null) || (this.stationId != null && !this.stationId.equals(other.stationId))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ResvInfo[ stationId=" + stationId + " ]";
  }
}
