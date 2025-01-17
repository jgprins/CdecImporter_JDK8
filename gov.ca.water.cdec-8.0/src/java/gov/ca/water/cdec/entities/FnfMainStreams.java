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
@Table(name = "FNF_MAIN_STREAMS")
@NamedQueries({
  @NamedQuery(name = "FnfMainStreams.findAll", query = "SELECT f FROM FnfMainStreams f")})
public class FnfMainStreams implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "STA_ORD")
  private Integer staOrd;
  @Size(max = 3)
  @Column(name = "STATION_ID")
  private String stationId;
  @Size(max = 50)
  @Column(name = "STREAM_NAME")
  private String streamName;
  @Size(max = 25)
  @Column(name = "STREAM_NAME_SHORT")
  private String streamNameShort;
  @Column(name = "HYDRO_NUM")
  private Integer hydroNum;
  @Size(max = 1)
  private String asterisk;
  @Size(max = 5)
  private String note1;
  @Size(max = 5)
  private String note2;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FnfMainStreams() {
    super();  
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public Integer getStaOrd() {
    return staOrd;
  }

  public void setStaOrd(Integer staOrd) {
    this.staOrd = staOrd;
  }

  public String getStationId() {
    return stationId;
  }

  public void setStationId(String stationId) {
    this.stationId = stationId;
  }

  public String getStreamName() {
    return streamName;
  }

  public void setStreamName(String streamName) {
    this.streamName = streamName;
  }

  public String getStreamNameShort() {
    return streamNameShort;
  }

  public void setStreamNameShort(String streamNameShort) {
    this.streamNameShort = streamNameShort;
  }

  public Integer getHydroNum() {
    return hydroNum;
  }

  public void setHydroNum(Integer hydroNum) {
    this.hydroNum = hydroNum;
  }

  public String getAsterisk() {
    return asterisk;
  }

  public void setAsterisk(String asterisk) {
    this.asterisk = asterisk;
  }

  public String getNote1() {
    return note1;
  }

  public void setNote1(String note1) {
    this.note1 = note1;
  }

  public String getNote2() {
    return note2;
  }

  public void setNote2(String note2) {
    this.note2 = note2;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (staOrd != null ? staOrd.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof FnfMainStreams)) {
      return false;
    }
    FnfMainStreams other = (FnfMainStreams) object;
    if ((this.staOrd == null && other.staOrd != null) || 
            (this.staOrd != null && !this.staOrd.equals(other.staOrd))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "FnfMainStreams[ staOrd=" + staOrd + " ]";
  }
  // </editor-fold>
}
