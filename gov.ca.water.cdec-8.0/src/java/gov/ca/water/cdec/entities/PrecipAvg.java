package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * An Entity mapping the CDEC.PRECIP_AVG table
 * @author kprins
 */
@Entity
@Table(name = "PRECIP_AVG")
@NamedQueries({
  @NamedQuery(name = "PrecipAvg.findAll", query = "SELECT p FROM PrecipAvg p"),
  @NamedQuery(name = "PrecipAvg.wyAvgs",
        query = "SELECT p FROM PrecipAvg p "
        + "WHERE (p.precipAvgPK.amonth = 0)"),
  @NamedQuery(name = "PrecipAvg.wyAvgsToDate",
        query = "SELECT p.precipAvgPK.stationId, SUM(p.avgPrecip) FROM PrecipAvg p "
        + "WHERE (p.precipAvgPK.amonth IN :inclMonths) "
        + "GROUP BY p.precipAvgPK.stationId"),
  @NamedQuery(name = "PrecipAvg.byStationIds",
        query = "SELECT p FROM PrecipAvg p "
        + "WHERE (p.precipAvgPK.stationId in :stationIds)")})
public class PrecipAvg implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  protected PrecipAvgPK precipAvgPK;
  @Column(name = "AVERAGE")
  private Double avgPrecip;

  public PrecipAvg() {
  }

  public PrecipAvg(PrecipAvgPK precipAvgPK) {
    this.precipAvgPK = precipAvgPK;
  }

  public PrecipAvg(String stationId, int amonth) {
    this.precipAvgPK = new PrecipAvgPK(stationId, amonth);
  }

  public PrecipAvgPK getPrecipAvgPK() {
    return precipAvgPK;
  }

//  public void setPrecipAvgPK(PrecipAvgPK precipAvgPK) {
//    this.precipAvgPK = precipAvgPK;
//  }

  public Double getAvgPrecip() {
    return avgPrecip;
  }

//  public void setAverage(Double average) {
//    this.average = average;
//  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (precipAvgPK != null ? precipAvgPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof PrecipAvg))) {
      return false;
    }
    PrecipAvg other = (PrecipAvg) object;
    if ((this.precipAvgPK == null && other.precipAvgPK != null) || 
            (this.precipAvgPK != null && !this.precipAvgPK.equals(other.precipAvgPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.PrecipAvg[" + precipAvgPK + " ]";
  }
  
}
