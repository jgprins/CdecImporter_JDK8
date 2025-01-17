package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.StationFacade;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An Entity mapping the CDEC.SNOW_COURSE_INFO table
 * @author kprins
 */
@Entity 
@Table(name = "SNOW_COURSE_INFO")
@NamedQueries({
  @NamedQuery(name = "SnowCourseInfo.findAll", query = "SELECT s FROM SnowCourseInfo s"),
  @NamedQuery(name = "SnowCourseInfo.findAllActive", 
              query = "SELECT s FROM SnowCourseInfo s WHERE s.yearAband IS NULL"),
  @NamedQuery(name = "SnowCourseInfo.findActiveByStation", 
              query = "SELECT s FROM SnowCourseInfo s "
                      + "WHERE ((s.yearAband IS NULL) AND (s.stationId = :stationId))")
})
public class SnowCourseInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "COURSE_NUM")
  private Integer courseNum;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
  @Column(name = "APR1_AVG")
  private Double apr1Avg;
  @Size(max = 5)
  @Column(name = "MEASURED")
  private String measured;
  @Column(name = "POINTS")
  private Integer points;
  @Column(name = "WILDAREA")
  private Character wildarea;
  @Size(max = 9)
  @Column(name = "ASPECT")
  private String aspect;
  @Size(max = 80)
  @Column(name = "EXPOSURE")
  private String exposure;
  @Column(name = "YEAR_ESTAB")
  private Integer yearEstab;
  @Column(name = "YEAR_ABAND")
  private Integer yearAband;
  @Column(name = "MEAS_AGENCY")
  private Integer measAgency;
  @Column(name = "ENABLE_AVG")
  private Character enableAvg;
  @Column(name = "START_YR_AVG")
  private Integer startYrAvg;
  @Column(name = "END_YR_AVG")
  private Integer endYrAvg;
  @Size(max = 1000)
  @Column(name = "STA_DIRECTIONS")
  private String staDirections;

  public SnowCourseInfo() {
  }

  public SnowCourseInfo(Integer courseNum) {
    this.courseNum = courseNum;
  }

  public SnowCourseInfo(Integer courseNum, String stationId) {
    this.courseNum = courseNum;
    this.stationId = stationId;
  }

  public Integer getCourseNum() {
    return courseNum;
  }

//  public void setCourseNum(Integer courseNum) {
//    this.courseNum = courseNum;
//  }

  public String getStationId() {
    return stationId;
  }

//  public void setStationId(String stationId) {
//    this.stationId = stationId;
//  }

  public Double getApr1Avg() {
    return apr1Avg;
  }

//  public void setApr1Avg(Double apr1Avg) {
//    this.apr1Avg = apr1Avg;
//  }

  public String getMeasured() {
    return measured;
  }

//  public void setMeasured(String measured) {
//    this.measured = measured;
//  }

  public Integer getPoints() {
    return points;
  }

//  public void setPoints(Integer points) {
//    this.points = points;
//  }

  public Character getWildarea() {
    return wildarea;
  }

//  public void setWildarea(Character wildarea) {
//    this.wildarea = wildarea;
//  }

  public String getAspect() {
    return aspect;
  }

//  public void setAspect(String aspect) {
//    this.aspect = aspect;
//  }

  public String getExposure() {
    return exposure;
  }

//  public void setExposure(String exposure) {
//    this.exposure = exposure;
//  }

  public Integer getYearEstab() {
    return yearEstab;
  }

//  public void setYearEstab(Integer yearEstab) {
//    this.yearEstab = yearEstab;
//  }

  public Integer getYearAband() {
    return yearAband;
  }

//  public void setYearAband(Integer yearAband) {
//    this.yearAband = yearAband;
//  }

  public Integer getMeasAgency() {
    return measAgency;
  }

//  public void setMeasAgency(Integer measAgency) {
//    this.measAgency = measAgency;
//  }

  public Character getEnableAvg() {
    return enableAvg;
  }

//  public void setEnableAvg(Character enableAvg) {
//    this.enableAvg = enableAvg;
//  }

  public Integer getStartYrAvg() {
    return startYrAvg;
  }

//  public void setStartYrAvg(Integer startYrAvg) {
//    this.startYrAvg = startYrAvg;
//  }

  public Integer getEndYrAvg() {
    return endYrAvg;
  }

//  public void setEndYrAvg(Integer endYrAvg) {
//    this.endYrAvg = endYrAvg;
//  }

  public String getStaDirections() {
    return staDirections;
  }

//  public void setStaDirections(String staDirections) {
//    this.staDirections = staDirections;
//  }
 
  @Override 
  public int hashCode() {
    int hash = 0;
    hash += (courseNum != null ? courseNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof SnowCourseInfo))) {
      return false;
    }
    SnowCourseInfo other = (SnowCourseInfo) object;
    if ((this.courseNum == null && other.courseNum != null) || 
            (this.courseNum != null && !this.courseNum.equals(other.courseNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.SnowCourseInfo[courseNum=" + courseNum + " ]";
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
    if ((this.station == null) && ((stationId = this.getStationId()) != null)) {
      StationFacade facade = CdecBaseFacade.getFacade(StationFacade.class);
      if (facade != null) {
        this.station = facade.find(stationId);
      }
    }
    return this.station;
  }
//</editor-fold>
}
