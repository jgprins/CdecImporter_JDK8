package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "HYDRO_MONTHLY_SWC_AVG")
@NamedQueries({
  @NamedQuery(name = "HydroMonthlySwcAvg.findAll", 
              query = "SELECT h FROM HydroMonthlySwcAvg h")})
public class HydroMonthlySwcAvg implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @EmbeddedId
  protected HydroMonthlySwcAvgPK hydroMonthlySwcAvgPK;
  private Double ravg;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public HydroMonthlySwcAvg() {
    super();  
  }

  public HydroMonthlySwcAvg(HydroMonthlySwcAvgPK hydroMonthlySwcAvgPK) {
    this.hydroMonthlySwcAvgPK = hydroMonthlySwcAvgPK;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public HydroMonthlySwcAvgPK getHydroMonthlySwcAvgPK() {
    return hydroMonthlySwcAvgPK;
  }

  public void setHydroMonthlySwcAvgPK(HydroMonthlySwcAvgPK hydroMonthlySwcAvgPK) {
    this.hydroMonthlySwcAvgPK = hydroMonthlySwcAvgPK;
  }

  public Double getRavg() {
    return ravg;
  }

  public void setRavg(Double ravg) {
    this.ravg = ravg;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (hydroMonthlySwcAvgPK != null ? hydroMonthlySwcAvgPK.hashCode() : 0);
    return hash;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof HydroMonthlySwcAvg)) {
      return false;
    }
    HydroMonthlySwcAvg other = (HydroMonthlySwcAvg) object;
    if ((this.hydroMonthlySwcAvgPK == null && other.hydroMonthlySwcAvgPK != null) || 
            (this.hydroMonthlySwcAvgPK != null && 
            !this.hydroMonthlySwcAvgPK.equals(other.hydroMonthlySwcAvgPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HydroMonthlySwcAvg[ pK=" + hydroMonthlySwcAvgPK + " ]";
  }
  // </editor-fold>
}
