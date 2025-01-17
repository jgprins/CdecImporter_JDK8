package gov.ca.water.cdec.entities;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeSeriesPk implements Serializable {

  public abstract int getSensorId();

  public abstract void setSensorId(int sensorId);

  public abstract Date getActualDate();

  public abstract void setActualDate(Date actualDate);
}
