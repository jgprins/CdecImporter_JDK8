package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "HYDROLOGIC_AREA")
@XmlRootElement
@NamedQueries({
  @NamedQuery(name = "HydrologicArea.findAll", 
              query = "SELECT h FROM HydrologicArea h ORDER BY h.hydroNum")})
public class HydrologicArea implements Serializable {
  private static final long serialVersionUID = 1L;
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;
  @Size(max = 25)
  @Column(name = "HYDRO_AREA_NAME")
  private String hydroAreaName;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public HydrologicArea() {
    super();  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public Integer getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }

  public String getHydroAreaName() {
    return hydroAreaName;
  }

  public void setHydroAreaName(String hydroAreaName) {
    this.hydroAreaName = hydroAreaName;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (hydroNum != null ? hydroNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof HydrologicArea)) {
      return false;
    }
    HydrologicArea other = (HydrologicArea) object;
    if ((this.hydroNum == null && other.hydroNum != null) || (this.hydroNum != null && !this.hydroNum.equals(other.hydroNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "HydrologicArea[Num=" + hydroNum + "; Name" + hydroAreaName + " ]";
  }  
  // </editor-fold>
}
