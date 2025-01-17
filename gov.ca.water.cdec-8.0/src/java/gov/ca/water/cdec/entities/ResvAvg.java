package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "RESV_AVG")
@NamedQueries({
  @NamedQuery(name = "ResvAvg.findAll", query = "SELECT r FROM ResvAvg r")})
public class ResvAvg implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @EmbeddedId
  protected ResvAvgPK resvAvgPK;
  private Double average;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ResvAvg() {
    super();  
  }

  public ResvAvg(ResvAvgPK resvAvgPK) {
    this.resvAvgPK = resvAvgPK;
  }

  public ResvAvg(String stationId, Integer amonth) {
    this.resvAvgPK = new ResvAvgPK(stationId, amonth);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public ResvAvgPK getResvAvgPK() {
    return resvAvgPK;
  }

  public void setResvAvgPK(ResvAvgPK resvAvgPK) {
    this.resvAvgPK = resvAvgPK;
  }

  public Double getAverage() {
    return average;
  }

  public void setAverage(Double average) {
    this.average = average;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (resvAvgPK != null ? resvAvgPK.hashCode() : 0);
    return hash;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof ResvAvg)) {
      return false;
    }
    ResvAvg other = (ResvAvg) object;
    if ((this.resvAvgPK == null && other.resvAvgPK != null) || 
            (this.resvAvgPK != null && !this.resvAvgPK.equals(other.resvAvgPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ResvAvg[ pK=" + resvAvgPK + " ]";
  }
  // </editor-fold>
}
