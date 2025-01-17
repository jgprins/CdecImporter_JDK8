package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An Entity mapping the primary key for the CDEC.PRECIP_AVG table
 * @author kprins
 */
@Embeddable
public class PrecipAvgPK implements Serializable {
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Basic(optional = false)
  @NotNull
  @Column(name = "AMONTH")
  private int amonth;

  public PrecipAvgPK() {
  }

  public PrecipAvgPK(String stationId, int amonth) {
    this.stationId = stationId;
    this.amonth = amonth;
  }

  public String getStationId() {
    return stationId;
  }

//  public void setStationId(String stationId) {
//    this.stationId = stationId;
//  }

  public int getObsMonth() {
    return amonth;
  }

//  public void setAmonth(int amonth) {
//    this.amonth = amonth;
//  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    hash += (int) amonth;
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof PrecipAvgPK))) {
      return false;
    }
    PrecipAvgPK other = (PrecipAvgPK) object;
    if ((this.stationId == null && other.stationId != null) || 
            (this.stationId != null && !this.stationId.equals(other.stationId))) {
      return false;
    }
    if (this.amonth != other.amonth) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "stationId=" + stationId + ", month=" + amonth;
  }
  
}
