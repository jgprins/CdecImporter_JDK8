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
@NamedQueries({
  @NamedQuery(name = "Agency.findAll", query = "SELECT a FROM Agency a")})
public class Agency implements Serializable {
  private static final long serialVersionUID = 1L;
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "AGENCY_NUM", nullable = false)
  private Integer agencyNum;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 60)
  @Column(name = "AGENCY_NAME", nullable = false, length = 60)
  private String agencyName;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 15)
  @Column(name = "AGENCY_NAME_SHORT", nullable = false, length = 15)
  private String agencyNameShort;
  @Column(name = "AGENCY_GROUP1_NUM")
  private Integer agencyGroup1Num;
  @Column(name = "AGENCY_GROUP2_NUM")
  private Integer agencyGroup2Num;
  @Column(name = "AGENCY_TYPE_NUM")
  private Integer agencyTypeNum;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public Agency() {
    super();  
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Getters/Setters">
  public Agency(Integer agencyNum) {
    this.agencyNum = agencyNum;
  }
  
  public Agency(Integer agencyNum, String agencyName, String agencyNameShort) {
    this.agencyNum = agencyNum;
    this.agencyName = agencyName;
    this.agencyNameShort = agencyNameShort;
  }
  
  public Integer getAgencyNum() {
    return agencyNum;
  }
  
  public void setAgencyNum(Integer agencyNum) {
    this.agencyNum = agencyNum;
  }
  
  public String getAgencyName() {
    return agencyName;
  }
  
  public void setAgencyName(String agencyName) {
    this.agencyName = agencyName;
  }
  
  public String getAgencyNameShort() {
    return agencyNameShort;
  }
  
  public void setAgencyNameShort(String agencyNameShort) {
    this.agencyNameShort = agencyNameShort;
  }
  
  public Integer getAgencyGroup1Num() {
    return agencyGroup1Num;
  }
  
  public void setAgencyGroup1Num(Integer agencyGroup1Num) {
    this.agencyGroup1Num = agencyGroup1Num;
  }
  
  public Integer getAgencyGroup2Num() {
    return agencyGroup2Num;
  }
  
  public void setAgencyGroup2Num(Integer agencyGroup2Num) {
    this.agencyGroup2Num = agencyGroup2Num;
  }
  
  public Integer getAgencyTypeNum() {
    return agencyTypeNum;
  }
  
  public void setAgencyTypeNum(Integer agencyTypeNum) {
    this.agencyTypeNum = agencyTypeNum;
  }
  
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (agencyNum != null ? agencyNum.hashCode() : 0);
    return hash;
  }
//</editor-fold>
  
  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof Agency)) {
      return false;
    }
    Agency other = (Agency) object;
    if ((this.agencyNum == null && other.agencyNum != null) || (this.agencyNum != null && !this.agencyNum.equals(other.agencyNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "gov.ca.water.cdec.entities.Agency[ agencyNum=" + agencyNum + " ]";
  }
}
