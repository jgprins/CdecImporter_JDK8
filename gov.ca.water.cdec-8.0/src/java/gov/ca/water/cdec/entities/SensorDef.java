package gov.ca.water.cdec.entities;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * An Entity mapping the CDEC.SENSOR_DEF table
 * @author kprins
 */
@Entity
@Table(name = "SENSOR_DEF")
@NamedQueries({
  @NamedQuery(name = "SensorDef.findAll", query = "SELECT s FROM SensorDef s")})
public class SensorDef implements Serializable {
  private static final long serialVersionUID = 1L;
  @Id
  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_NUM")
  private Integer sensorNum;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 2)
  @Column(name = "SHEF_PE")
  private String shefPe;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SHEF_DUR")
  private String shefDur;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SHEF_TYPE")
  private String shefType;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SHEF_SOURCE")
  private String shefSource;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SHEF_EXTREMA")
  private String shefExtrema;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SHEF_PROB")
  private String shefProb;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 8)
  @Column(name = "SENS_SHORT_NAME")
  private String sensShortName;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 30)
  @Column(name = "SENS_LONG_NAME")
  private String sensLongName;
  @Basic(optional = false)
  @NotNull
  @Size(min = 1, max = 6)
  @Column(name = "SENS_UNITS")
  private String sensUnits;
  @Basic(optional = false)
  @NotNull
  @Column(name = "FMT_TYPE")
  private String fmtType;
  @Column(name = "SENS_ORDER")
  private Integer sensOrder;

  public SensorDef() {
  }

  public SensorDef(Integer sensorNum) {
    this.sensorNum = sensorNum;
  }

//  public SensorDef(Integer sensorNum, String shefPe, String shefDur, String shefType,
//    String shefSource, String shefExtrema, String shefProb, String sensShortName, 
//    String sensLongName, String sensUnits, String fmtType) {
//    this.sensorNum = sensorNum;
//    this.shefPe = shefPe;
//    this.shefDur = shefDur;
//    this.shefType = shefType;
//    this.shefSource = shefSource;
//    this.shefExtrema = shefExtrema;
//    this.shefProb = shefProb;
//    this.sensShortName = sensShortName;
//    this.sensLongName = sensLongName;
//    this.sensUnits = sensUnits;
//    this.fmtType = fmtType;
//  }

  public Integer getSensorNum() {
    return sensorNum;
  }

//  public void setSensorNum(Integer sensorNum) {
//    this.sensorNum = sensorNum;
//  }

  public String getShefPe() {
    return shefPe;
  }

  public void setShefPe(String shefPe) {
    this.shefPe = shefPe;
  }

  public String getShefDur() {
    return shefDur;
  }

  public void setShefDur(String shefDur) {
    this.shefDur = shefDur;
  }

  public String getShefType() {
    return shefType;
  }

  public void setShefType(String shefType) {
    this.shefType = shefType;
  }

  public String getShefSource() {
    return shefSource;
  }

  public void setShefSource(String shefSource) {
    this.shefSource = shefSource;
  }

  public String getShefExtrema() {
    return shefExtrema;
  }

  public void setShefExtrema(String shefExtrema) {
    this.shefExtrema = shefExtrema;
  }

  public String getShefProb() {
    return shefProb;
  }

  public void setShefProb(String shefProb) {
    this.shefProb = shefProb;
  }

  public String getSensShortName() {
    return sensShortName;
  }

  public void setSensShortName(String sensShortName) {
    this.sensShortName = sensShortName;
  }

  public String getSensLongName() {
    return sensLongName;
  }

  public void setSensLongName(String sensLongName) {
    this.sensLongName = sensLongName;
  }

  public String getSensUnits() {
    return sensUnits;
  }

  public void setSensUnits(String sensUnits) {
    this.sensUnits = sensUnits;
  }

  public String getFmtType() {
    return fmtType;
  }

  public void setFmtType(String fmtType) {
    this.fmtType = fmtType;
  }

  public Integer getSensOrder() {
    return sensOrder;
  }

  public void setSensOrder(Integer sensOrder) {
    this.sensOrder = sensOrder;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (sensorNum != null ? sensorNum.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof SensorDef))) {
      return false;
    }
    SensorDef other = (SensorDef) object;
    if ((this.sensorNum == null && other.sensorNum != null) || 
            (this.sensorNum != null && !this.sensorNum.equals(other.sensorNum))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.SensorDef[ sensorNum=" + sensorNum + " ]";
  }
  
}
