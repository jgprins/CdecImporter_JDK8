package gov.ca.water.cdec.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * An Entity mapping the CDEC.DAILY_DATA table
 * @author kprins
 */
@Entity
@Table(name = "DAILY_DATA")
@NamedQueries({
  @NamedQuery(name = "DailyData.findAll", query = "SELECT d FROM DailyData d"),
  @NamedQuery(name = "DailyData.bySensorAndDate",
        query = "SELECT d FROM DailyData d WHERE "
        + "((d.dailyDataPK.sensorId = :sensorId) AND "
        + "(d.dailyDataPK.actualDate BETWEEN :startDt AND :endDt))"),
  @NamedQuery(name = "DailyData.bySensorListAndDate",
        query = "SELECT d FROM DailyData d WHERE "
        + "((d.dailyDataPK.sensorId IN :sensorIds) AND "
        + "(d.dailyDataPK.actualDate BETWEEN :startDt AND :endDt))"),
  @NamedQuery(name = "DailyData.monthlySnoData",
        query = "SELECT d FROM DailyData d "
        + "WHERE ((d.dailyDataPK.actualDate IN :monthlyDates) "
        + "AND (d.dailyDataPK.sensorId IN :sensorIds)) "
        + "ORDER BY d.dailyDataPK.sensorId, d.dailyDataPK.actualDate"),
  @NamedQuery(name = "DailyData.monthlyPcpData",
        query = "SELECT d.dailyDataPK.sensorId, MIN(d.dailyDataPK.actualDate), "
        + "SUM(d.obsValue) FROM DailyData d "
        + "WHERE ((d.dailyDataPK.sensorId IN :sensorIds) "
        + "AND (d.dailyDataPK.actualDate BETWEEN :startDt AND :endDt)) "
        + "GROUP BY d.dailyDataPK.sensorId "
        + "ORDER BY d.dailyDataPK.sensorId"),
  @NamedQuery(name = "DailyData.b120SweData",
        query = "SELECT d FROM DailyData d "
        + "WHERE ((d.dailyDataPK.sensorId IN :sensorIds) AND "
        + "((d.dailyDataPK.actualDate = :curDate) OR "
        + "(d.dailyDataPK.actualDate = :priorDayDate) OR "
        + "(d.dailyDataPK.actualDate = :priorWeekDate))) "
        + "ORDER BY d.dailyDataPK.sensorId, d.dailyDataPK.actualDate"),
  @NamedQuery(name = "DailyData.PORDates",
        query = "SELECT MIN(d.dailyDataPK.actualDate), MAX(d.dailyDataPK.actualDate)"
        + " FROM DailyData d WHERE (d.dailyDataPK.sensorId = :sensorId)")})
public class DailyData implements Serializable {
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  protected DailyDataPK dailyDataPK;
  @Column(name = "OBS_DATE")
  @Temporal(TemporalType.DATE)
  private Date obsDate;
  @Column(name = "VALUE")
  private Double obsValue;
  @Column(name = "DATA_FLAG")
  private String dataFlag;

  public DailyData() {
  }

  public DailyData(DailyDataPK dailyDataPK) {
    this.dailyDataPK = dailyDataPK;
  }

  public DailyData(int sensorId, Date actualDate) {
    this.dailyDataPK = new DailyDataPK(sensorId, actualDate);
  }

  public DailyDataPK getDailyDataPK() {
    return dailyDataPK;
  }

  public DailyDataPK getPrimaryKey() {
    return dailyDataPK;
  }

  public void setPrimaryKey(DailyDataPK dailyDataPK) {
    this.dailyDataPK = dailyDataPK;
  }

  public Date getObsDate() {
    return obsDate;
  }

  public void setObsDate(Date obsDate) {
    this.obsDate = obsDate;
  }

  public Double getObsValue() {
    return obsValue;
  }

  public void setObsValue(Double value) {
    this.obsValue = value;
  }

  public String getDataFlag() {
    return dataFlag;
  }

  public void setDataFlag(String dataFlag) {
    this.dataFlag = dataFlag;
  }

  @Override
  public int hashCode() {
    int hash = 0;
    hash += (dailyDataPK != null ? dailyDataPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof DailyData))) {
      return false;
    }
    DailyData other = (DailyData) object;
    if ((this.dailyDataPK == null && other.dailyDataPK != null) || 
            (this.dailyDataPK != null && !this.dailyDataPK.equals(other.dailyDataPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.DailyData[PK:" + dailyDataPK + " ]";
  }
  
}
