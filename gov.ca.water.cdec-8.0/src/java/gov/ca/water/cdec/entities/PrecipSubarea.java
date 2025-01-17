package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.HydrologicAreaFacade;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "PRECIP_SUBAREA")
@NamedQueries({
  @NamedQuery(name = "PrecipSubarea.findAll", 
              query = "SELECT p FROM PrecipGroup p")})
public class PrecipSubarea implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "SUBAREA_NUM")
  private Integer subareaNum;
  @Column(name = "SUBAREA_NAME")
  private String subareaName;
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public PrecipSubarea() {
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public Integer getSubareaNum() {
    return subareaNum;
  }

  public void setSubareaNum(Integer subareaNum) {
    this.subareaNum = subareaNum;
  }

  public String getSubareaName() {
    return subareaName;
  }

  public void setSubareaName(String subareaName) {
    this.subareaName = subareaName;
  }

  public Integer getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    Integer hash = 0;
    hash += (subareaNum != null ? subareaNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof PrecipSubarea))) {
      return false;
    }
    PrecipSubarea other = (PrecipSubarea) object;
    if ((this.subareaNum == null && other.subareaNum != null)
            || (this.subareaNum != null && !this.subareaNum.equals(other.subareaNum))) {
      return false;
    }
    return true;
  }

  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return "PrecipSubarea[" + this.subareaNum + "]";
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Transient Methods">
  @Transient
  private HydrologicArea hydroArea = null;
  public HydrologicArea getHydrologicArea() {
    if ((this.hydroArea == null) && (this.hydroNum != null)) {
      HydrologicAreaFacade facade = CdecBaseFacade.getFacade(HydrologicAreaFacade.class);
      if (facade != null) {
        this.hydroArea = facade.find(this.hydroNum);
      }
    }
    return hydroArea;
  }
  //</editor-fold>
}
