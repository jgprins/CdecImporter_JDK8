package gov.ca.water.cdec.entities;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Entity
@Table(name = "EVENT_DATA")
@NamedQueries({
  @NamedQuery(name = "EventData.findAll", query = "SELECT e FROM EventData e"),
  @NamedQuery(name = "EventData.bySensorAndDateRange",
        query = "SELECT e FROM EventData e WHERE "
        + "((e.eventDataPK.sensorId = :sensorId) AND "
        + "(e.eventDataPK.dateTime BETWEEN :startDt AND :endDt))"),
  @NamedQuery(name = "EventData.bySensorListAndDate",
        query = "SELECT e FROM EventData e WHERE "
        + "((e.eventDataPK.sensorId IN :sensorIds) AND "
        + "(e.eventDataPK.dateTime = :dateTime))"),
  @NamedQuery(name = "EventData.bySensorListAndDateRange",
        query = "SELECT e FROM EventData e WHERE "
        + "((e.eventDataPK.sensorId IN :sensorIds) AND "
        + "(e.eventDataPK.dateTime BETWEEN :startDt AND :endDt))"),
  @NamedQuery(name = "EventData.PORDates",
        query = "SELECT MIN(e.eventDataPK.dateTime), MAX(e.eventDataPK.dateTime)"
        + " FROM EventData e WHERE (e.eventDataPK.sensorId = :sensorId)")
})
public class EventData implements Serializable {
  private static final long serialVersionUID = 1L;

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  @EmbeddedId
  protected EventDataPK eventDataPK;
  @Size(max = 1)
  @Column(name = "DATA_FLAG")
  private String dataFlag;
  @Column(name = "VALUE")
  private Double value; /* Changed to obsValue since 'value' is a reserved word */
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EventData() {
    super();  
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  public EventData(EventDataPK eventDataPK) {
    this.eventDataPK = eventDataPK;
  }

  public EventData(Date dateTime, int sensorId) {
    this.eventDataPK = new EventDataPK(dateTime, sensorId);
  }

  public EventDataPK getEventDataPK() {
    return eventDataPK;
  }

  public void setEventDataPK(EventDataPK eventDataPK) {
    this.eventDataPK = eventDataPK;
  }

  public String getDataFlag() {
    return dataFlag;
  }

  public void setDataFlag(String dataFlag) {
    this.dataFlag = dataFlag;
  }

  public Double getObsValue() {
    return value;
  }

  public void setObsValue(Double obsValue) {
    this.value = obsValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public int hashCode() {
    int hash = 0;
    hash += (eventDataPK != null ? eventDataPK.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object object) {
    // TODO: Warning - this method won't work in the case the id fields are not set
    if (!(object instanceof EventData)) {
      return false;
    }
    EventData other = (EventData) object;
    if ((this.eventDataPK == null && other.eventDataPK != null) || 
        (this.eventDataPK != null && !this.eventDataPK.equals(other.eventDataPK))) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "EventData[ eventDataPK=" + eventDataPK + " ]";
  }
  // </editor-fold>
}
