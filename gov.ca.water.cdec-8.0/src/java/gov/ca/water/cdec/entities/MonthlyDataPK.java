package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.core.DateKey;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * An Entity mapping the primary key for the CDEC.MONTHLY_DATA table
 * @author kprins
 */
@Embeddable
public class MonthlyDataPK implements Serializable  {
  // <editor-fold defaultstate="collapsed" desc="Private Fields">

  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_ID")
  private int sensorId;
  @Basic(optional = false)
  @NotNull
  @Column(name = "ACTUAL_DATE")
  @Temporal(TemporalType.DATE)
  private Date actualDate;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Constructors">
  public MonthlyDataPK() {
  }
  
  public MonthlyDataPK(int sensorId, Date actualDate) {
    this.sensorId = sensorId;
    this.actualDate = actualDate;
    this.dateKey = null;
  }
  
  public MonthlyDataPK(int sensorId, DateKey dateKey) {
    this.sensorId = sensorId;
    this.actualDate = (dateKey == null)? null: dateKey.getDate();
    this.dateKey = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public int getSensorId() {
    return sensorId;
  }
  
  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }
  
  public Date getActualDate() {
    DateKey eventKey = this.getDateKey();
    return (eventKey == null) ? null : eventKey.getDate();
  }
  
  public void setActualDate(Date actualDate) {
    this.actualDate = actualDate;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Object">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (int) sensorId;
    hash += (actualDate != null ? actualDate.hashCode() : 0);
    return hash;
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof MonthlyDataPK));
    if (result) {
      MonthlyDataPK other = (MonthlyDataPK) obj;
      DateKey thisStep = this.getDateKey();
      DateKey objStep = other.getDateKey();
      result = ((this.sensorId == other.sensorId) &&
                (thisStep != null) && (objStep != null) &&
                (thisStep.equals(objStep)));
    }
    return result;
  }
  
  @Override
  public String toString() {
    return "sensorId=" + sensorId + ", actualDate=" + actualDate;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Transient/Custom Methods">
  /**
   * Transient placeholder for the PK's DateKey.
   */
  @Transient
  private DateKey dateKey;
  /**
   * Get the lazy initiated and cached) PK's DateKey.
   * @return this.dateKey
   */
  public DateKey getDateKey() {
    if ((this.dateKey == null) && (this.actualDate != null)) {
      this.dateKey = new DateKey(this.actualDate);
    }
    return this.dateKey;
  }
  // </editor-fold>  
}
