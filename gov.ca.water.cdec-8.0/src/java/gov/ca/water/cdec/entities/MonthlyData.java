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
 * An Entity mapping the CDEC.MONTHLY_DATA table
 * @author kprins
 */
@Entity
@Table(name = "MONTHLY_DATA")
@NamedQueries({
  @NamedQuery(name = "MonthlyData.findAll",
        query = "SELECT m FROM MonthlyData m"),
  @NamedQuery(name = "MonthlyData.sensorData",
        query = "SELECT m FROM MonthlyData m "
        + "WHERE ((m.monthlyDataPK.sensorId = :sensorId) "
        + "AND (m.monthlyDataPK.actualDate BETWEEN :startDt AND :endDt))"),
  @NamedQuery(name = "MonthlyData.wyData",
        query = "SELECT m FROM MonthlyData m "
        + "WHERE ((m.monthlyDataPK.actualDate BETWEEN :startDt AND :endDt) "
        + "AND (m.monthlyDataPK.sensorId IN :sensorIds)) "
        + "ORDER BY m.monthlyDataPK.sensorId, m.monthlyDataPK.actualDate"),
  @NamedQuery(name = "MonthlyData.wyToDateData",
        query = "SELECT m.monthlyDataPK.sensorId, SUM(m.obsValue) FROM MonthlyData m "
        + "WHERE ((m.monthlyDataPK.actualDate BETWEEN :startDt AND :endDt)  "
        + "AND (m.monthlyDataPK.sensorId IN :sensorIds)) "
        + "GROUP BY m.monthlyDataPK.sensorId"),
  @NamedQuery(name = "MonthlyData.dataByDate",
        query = "SELECT m FROM MonthlyData m "
        + "WHERE ((m.monthlyDataPK.sensorId in :sensorIds) "
        + "AND (m.monthlyDataPK.actualDate= :actualDt))")})
public class MonthlyData implements Serializable {
  
  private static final long serialVersionUID = 1L;
  @EmbeddedId
  protected MonthlyDataPK monthlyDataPK;
  @Column(name = "OBS_DATE")
  @Temporal(TemporalType.DATE)
  private Date obsDate;
  // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
  @Column(name = "VALUE")
  private Double obsValue;
  @Column(name = "DATA_FLAG")
  private String dataFlag;

  public MonthlyData() {
  }

  public MonthlyData(MonthlyDataPK monthlyDataPK) {
    this.monthlyDataPK = monthlyDataPK;
  }

  /**
   * Initiate a MonthyData record from a DailyDate Record
   * @param dailyData the DailyData record
   * @return a MonthlyData instance or null if the dailyData = null.
   */
  public MonthlyData(DailyData dailyData) {
    if (dailyData == null) {
      throw new NullPointerException("The DailyData record is unassigned.");
    }
    this.monthlyDataPK = new MonthlyDataPK(dailyData.dailyDataPK.getSensorId(),
            dailyData.dailyDataPK.getActualDate());
    this.obsDate = dailyData.getObsDate();
    this.obsValue = dailyData.getObsValue();
    this.dataFlag = dailyData.getDataFlag();
  }
  
  public MonthlyData(int sensorId, Date actualDate, Double obsValue) {
    this.monthlyDataPK = new MonthlyDataPK(sensorId, actualDate);
    this.obsDate = actualDate;
    this.obsValue = obsValue;
    this.dataFlag = null;
  }

  public MonthlyDataPK getMonthlyDataPK() {
    return monthlyDataPK;
  }

  public MonthlyDataPK getPrimaryKey() {
    return monthlyDataPK;
  }

   public void setPrimaryKey(MonthlyDataPK monthlyDataPK) {
    this.monthlyDataPK = monthlyDataPK;
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
    hash += (monthlyDataPK != null ? monthlyDataPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if ((object == null) || (!(object instanceof MonthlyData))) {
      return false;
    }
    MonthlyData other = (MonthlyData) object;
    if ((this.monthlyDataPK == null && other.monthlyDataPK != null) || 
        (this.monthlyDataPK != null && !this.monthlyDataPK.equals(other.monthlyDataPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "CDEC.MonthlyData[PK:" + monthlyDataPK + " ]";
  }
  
}
