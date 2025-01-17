package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.facades.CdecBaseFacade;
import gov.ca.water.cdec.facades.PrecipSubareaFacade;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "PRECIP_GROUPS")
@NamedQueries({
  @NamedQuery(name = "PrecipGroup.findAll", 
              query = "SELECT p FROM PrecipGroup p WHERE (p.groupId='ALL')")})
public class PrecipGroup implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Column(name = "GROUP_ID")
  private String groupId;
  @Column(name = "SUBAREA_NUM")
  private Integer subareaNum;  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public PrecipGroup() {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public String getStationId() {
    return stationId;
  }

  public String getGroupId() {
    return groupId;
  }

  public Integer getSubareaNum() {
    return subareaNum;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">  
  @Override
  public int hashCode() {
    Integer hash = 0;
    hash += (stationId != null ? stationId.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof PrecipGroup))) {
      return false;
    }
    PrecipGroup other = (PrecipGroup) object;
    if ((this.stationId == null && other.stationId != null)
            || (this.stationId != null && !this.stationId.equals(other.stationId))) {
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
    return "PrecipGroup[GripId=" + this.groupId + "; StationId=" + this.stationId + "]";
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Transient Methods">
  @Transient
  private PrecipSubarea subarea = null;
  public PrecipSubarea getSubArea() {
    if ((this.subarea == null) && (this.subareaNum != null)) {
      PrecipSubareaFacade facade = CdecBaseFacade.getFacade(PrecipSubareaFacade.class);
      if (facade != null) {
        this.subarea = facade.find(this.subareaNum);
      }
    }
    return subarea;
  }
  //</editor-fold>
}
