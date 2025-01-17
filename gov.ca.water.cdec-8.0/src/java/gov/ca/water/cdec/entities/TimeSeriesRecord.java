package gov.ca.water.cdec.entities;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class TimeSeriesRecord<TPk extends TimeSeriesPk> implements Serializable {

  public abstract TPk getPrimaryKey();
  
  public abstract void setPrimaryKey(TPk newKey);

  public abstract Date getObsDate();

  public abstract void setObsDate(Date obsDate);

  public abstract Double getObsValue();

  public abstract void setObsValue(Double value);

  public abstract String getDataFlag();

  public abstract void setDataFlag(String dataFlag);
}
