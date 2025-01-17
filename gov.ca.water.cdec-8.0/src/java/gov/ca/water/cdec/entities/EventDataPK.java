package gov.ca.water.cdec.entities;

import gov.ca.water.cdec.core.EventStepKey;
import gov.ca.water.cdec.enums.EventStep;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Embeddable
public class EventDataPK implements Serializable {
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @Basic(optional = false)
  @NotNull
  @Column(name = "DATE_TIME")
  @Temporal(TemporalType.TIMESTAMP)
  private Date dateTime;
  @Basic(optional = false)
  @NotNull
  @Column(name = "SENSOR_ID")
  private int sensorId;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EventDataPK() {
    super();  
  }
  
  public EventDataPK(Date dateTime, int sensorId) {
    this.dateTime = dateTime;
    this.sensorId = sensorId;
    this.dateKey =  null;
  }
  
  public EventDataPK(EventStepKey dateKey, int sensorId) {
    this.dateTime = (dateKey == null)? null: dateKey.getDate();
    this.sensorId = sensorId;
    this.dateKey =  null;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: this.dateKey.date or null if this.dateTime = null;</p>
   */
  public Date getDateTime() {
    EventStepKey eventKey = this.getDateKey();
    return (eventKey == null)? null: eventKey.getDate();
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
    this.dateKey =  null;
  }

  public int getSensorId() {
    return sensorId;
  }

  public void setSensorId(int sensorId) {
    this.sensorId = sensorId;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (dateTime != null ? dateTime.hashCode() : 0);
    hash += (int) sensorId;
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EventDataPK));
    if (result) {
      EventDataPK other = (EventDataPK) obj;
      EventStepKey thisStep = this.getDateKey();
      EventStepKey objStep = other.getDateKey();
      result = ((this.sensorId == other.sensorId) &&
                (thisStep != null) && (objStep != null) &&
                 (thisStep.equals(objStep)));
    }
    return result;
  }

  @Override
  public String toString() {
    return "CDEC.EventDataPK[ actualDate=" + dateTime + ", sensorId=" + sensorId + " ]";
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Transient/Custom Methods">
  /**
   * Transient placeholder for the PK's EventStepKey.
   */
  @Transient
  private EventStepKey dateKey;

  /**
   * Get the lazy initiated and cached) PK's EventStepKey.
   * @return this.dateKey
   */
  public EventStepKey getDateKey() {
    if ((this.dateKey == null) && (this.dateTime != null)) {
      this.dateKey = new EventStepKey(this.dateTime, EventStep.MINUTES);
    }
    return this.dateKey;
  }
  // </editor-fold>
}
