package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "RIVER_BASIN")
@NamedQueries({
  @NamedQuery(name = "RiverBasin.findAll", query = "SELECT r FROM RiverBasin r"),
  @NamedQuery(name = "RiverBasin.findAllSorted", 
          query = "SELECT r FROM RiverBasin r ORDER BY r.basinNum"),
  @NamedQuery(name = "RiverBasin.findByHydroNums", 
      query = "SELECT r FROM RiverBasin r where (r.hydroNum in :hydroNums) "
              + "ORDER BY r.basinNum")})
public class RiverBasin implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "BASIN_NUM")
  private Integer basinNum;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "BASIN_NAME")
  private String basinName;
  @Column(name = "HYDRO_NUM")
  private Short hydroNum;

  public RiverBasin() {
  }

  public RiverBasin(Integer basinNum) {
    this.basinNum = basinNum;
  }

  public RiverBasin(Integer basinNum, String basinName) {
    this.basinNum = basinNum;
    this.basinName = basinName;
  }

  public Integer getBasinNum() {
    return basinNum;
  }

  public void setBasinNum(Integer basinNum) {
    this.basinNum = basinNum;
  }

  public String getBasinName() {
    return basinName;
  }

  public void setBasinName(String basinName) {
    this.basinName = basinName;
  }

  public Short getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Short hydroNum) {
    this.hydroNum = hydroNum;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (basinNum != null ? basinNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof RiverBasin)) {
      return false;
    }
    RiverBasin other = (RiverBasin) object;
    if ((this.basinNum == null && other.basinNum != null) || 
            (this.basinNum != null && !this.basinNum.equals(other.basinNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RiverBasin[basinNum=" + basinNum + " ]";
  }
  
}
