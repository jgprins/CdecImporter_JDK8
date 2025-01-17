package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Embeddable
public class FnfAvgPK implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Basic(optional = false)
  @NotNull
  private Integer amonth;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FnfAvgPK() {
    super();  
  }
  /**
   * Public Constructor  with a stationId and aMonth reference
   */
  public FnfAvgPK(String stationId, Integer amonth) {
    this.stationId = stationId;
    this.amonth = amonth;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public Integer getAmonth() {
    return amonth;
  }

  public void setAmonth(Integer amonth) {
    this.amonth = amonth;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    hash += (int) amonth;
    return hash;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof FnfAvgPK)) {
      return false;
    }
    FnfAvgPK other = (FnfAvgPK) object;
    if ((this.stationId == null && other.stationId != null) || (this.stationId != null && !this.stationId.equals(other.stationId))) {
      return false;
    }
    if (this.amonth != other.amonth) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "FnfAvgPK[ stationId=" + stationId + ", amonth=" + amonth + " ]";
  }
  // </editor-fold>
}
