package gov.ca.water.cdec.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Embeddable
public class HydroMonthlySwcAvgPK implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Basic(optional = false)
  @NotNull
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;
  @Basic(optional = false)
  @NotNull
  private Integer rmonth;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public HydroMonthlySwcAvgPK() {
    super();  
  }
  
  public HydroMonthlySwcAvgPK(Integer hydroNum, Integer rmonth) {
    this.hydroNum = hydroNum;
    this.rmonth = rmonth;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public Integer getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }

  public Integer getRmonth() {
    return rmonth;
  }

  public void setRmonth(Integer rmonth) {
    this.rmonth = rmonth;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (int) hydroNum;
    hash += (int) rmonth;
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof HydroMonthlySwcAvgPK)) {
      return false;
    }
    HydroMonthlySwcAvgPK other = (HydroMonthlySwcAvgPK) object;
    if (!Objects.equals(this.hydroNum, other.hydroNum)) {
      return false;
    }
    if (!Objects.equals(this.rmonth, other.rmonth)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HydroMonthlySwcAvgPK[ hydroNum=" + hydroNum + ", rmonth=" + rmonth + " ]";
  }
  // </editor-fold>
}
