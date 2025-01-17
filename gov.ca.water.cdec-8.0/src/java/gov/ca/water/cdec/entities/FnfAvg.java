package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "FNF_AVG")
@NamedQueries({
  @NamedQuery(name = "FnfAvg.findAll", query = "SELECT f FROM FnfAvg f")})
public class FnfAvg implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @EmbeddedId
  protected FnfAvgPK fnfAvgPK;
  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
  private Double average;
  @Column(name = "APR_HI")
  private Double aprHi;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FnfAvg() {
    super();  
  }
  public FnfAvg(FnfAvgPK fnfAvgPK) {
    this.fnfAvgPK = fnfAvgPK;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public FnfAvgPK getFnfAvgPK() {
    return fnfAvgPK;
  }

  public void setFnfAvgPK(FnfAvgPK fnfAvgPK) {
    this.fnfAvgPK = fnfAvgPK;
  }

  public Double getAverage() {
    return average;
  }

  public void setAverage(Double average) {
    this.average = average;
  }

  public Double getAprHi() {
    return aprHi;
  }

  public void setAprHi(Double aprHi) {
    this.aprHi = aprHi;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (fnfAvgPK != null ? fnfAvgPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof FnfAvg)) {
      return false;
    }
    FnfAvg other = (FnfAvg) object;
    if ((this.fnfAvgPK == null && other.fnfAvgPK != null) || 
            (this.fnfAvgPK != null && !this.fnfAvgPK.equals(other.fnfAvgPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "FnfAvg[ pk=" + fnfAvgPK + " ]";
  }
  // </editor-fold>  
}
