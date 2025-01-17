package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An Entity mapping the CDEC.STATION table
 *
 * @author kprins
 */
@Entity
@Table(name = "STATION")
@NamedQueries({
  @NamedQuery(name = "Station.findAll", query = "SELECT s FROM Station s"),
  @NamedQuery(name = "Station.findByBasinNum", 
          query = "SELECT s FROM Station s WHERE (s.basinNum = :basinNum)"),
  @NamedQuery(name = "Station.findByKeyword", 
          query = "SELECT s FROM Station s "
                + "WHERE ((s.stationId like :keyword) OR (s.stationName like :keyword))")
})
public class Station implements Serializable {

  private static final long serialVersionUID = 1L;
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Id
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 40)
  @Column(name = "STATION_NAME")
  private String stationName;
  @Basic(optional = false)
  @NotNull
  @Column(name = "ELEVATION")
  private Integer elevation;
  @Basic(optional = false)
  @NotNull
  @Column(name = "LATITUDE")
  private Double latitude;
  @Basic(optional = false)
  @NotNull
  @Column(name = "LONGITUDE")
  private Double longitude;
  @Size(max = 30)
  @Column(name = "NEARBY_CITY")
  private String nearbyCity;
  @Basic(optional = false)
  @NotNull
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;
  @Basic(optional = false)
  @NotNull
  @Column(name = "BASIN_NUM")
  private Integer basinNum;
  @Basic(optional = false)
  @NotNull
  @Column(name = "COUNTY_NUM")
  private Integer countyNum;
  @Column(name = "OWNER")
  private Integer owner;
  @Column(name = "MAINTENANCE")
  private Integer maIntegerenance;
  @Basic(optional = false)
  @NotNull
  @Column(name = "OPERATOR")
  private Integer operator;
  @Basic(optional = false)
  @NotNull
  @Column(name = "MAP_NUMBER")
  private Integer mapNumber;
  @Basic(optional = false)
  @NotNull
  @Column(name = "COLLECT_NUM")
  private Integer collectNum;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Constructors">
  public Station() {
  }
  
  public Station(String stationId) {
    this.stationId = stationId;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public String getStationId() {
    return stationId;
  }
  
  public void setStationId(String stationId) {
    this.stationId = stationId;
  }
  
  public String getStationName() {
    return stationName;
  }
  
  public void setStationName(String stationName) {
    this.stationName = stationName;
  }
  
  public Integer getElevation() {
    return elevation;
  }
  
  public void setElevation(Integer elevation) {
    this.elevation = elevation;
  }
  
  public Double getLatitude() {
    return latitude;
  }
  
  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }
  
  public Double getLongitude() {
    return longitude;
  }
  
  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }
  
  public String getNearbyCity() {
    return nearbyCity;
  }
  
  public void setNearbyCity(String nearbyCity) {
    this.nearbyCity = nearbyCity;
  }
  
  public Integer getHydroNum() {
    return hydroNum;
  }
  
  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }
  
  public Integer getBasinNum() {
    return basinNum;
  }
  
  public void setBasinNum(Integer basinNum) {
    this.basinNum = basinNum;
  }
  
  public Integer getCountyNum() {
    return countyNum;
  }
  
  public void setCountyNum(Integer countyNum) {
    this.countyNum = countyNum;
  }
  
  public Integer getOwner() {
    return owner;
  }
  
  public void setOwner(Integer owner) {
    this.owner = owner;
  }
  
  public Integer getMaIntegerenance() {
    return maIntegerenance;
  }
  
  public void setMaIntegerenance(Integer maIntegerenance) {
    this.maIntegerenance = maIntegerenance;
  }
  
  public Integer getOperator() {
    return operator;
  }
  
  public void setOperator(Integer operator) {
    this.operator = operator;
  }
  
  public Integer getMapNumber() {
    return mapNumber;
  }
  
  public void setMapNumber(Integer mapNumber) {
    this.mapNumber = mapNumber;
  }
  
  public Integer getCollectNum() {
    return collectNum;
  }
  
  public void setCollectNum(Integer collectNum) {
    this.collectNum = collectNum;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Object">
  @Override
  public int hashCode() {
    Integer hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    return hash;
  }
  
  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof Station))) {
      return false;
    }
    Station other = (Station) object;
    if ((this.stationId == null && other.stationId != null)
            || (this.stationId != null && !this.stationId.equals(other.stationId))) {
      return false;
    }
    return true;
  }
  
  @Override
  public String toString() {
    return "CDEC.Station[ stationId=" + stationId + " ]";
  }
  // </editor-fold>

}
