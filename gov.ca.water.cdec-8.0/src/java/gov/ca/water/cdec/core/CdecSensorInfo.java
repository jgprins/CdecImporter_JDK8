package gov.ca.water.cdec.core;

import java.io.Serializable;

/**
 * A Class for capturing the essential information of a sensor
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class CdecSensorInfo implements Serializable {
 
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Sensor's CdecSensor Type
   */
  public final CdecSensorTypes sensorType;
  /**
   * The Station specific CDEC SensorId
   */
  public final int sensorId;
  /**
   * The Sensor's associated CDEC StationId
   */
  public final String stationId;
  /**
   * The Sensor's associated CDEC Basin Number
   */
  public final int basinNum;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public CdecSensorInfo(int sensorId, CdecSensorTypes sensorType, String stationId, 
                                                                  int basinNum) {
    stationId = (stationId == null)? null: stationId.trim();
    this.sensorType = sensorType;
    this.sensorId = sensorId;
    this.stationId = stationId;
    this.basinNum = basinNum;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: return true if obj != null, and instance of WsSensorinfo and the
   * sensorId's match.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof CdecSensorInfo));
    if (result) {
      CdecSensorInfo other = (CdecSensorInfo) obj;
      result = (other.sensorId == this.sensorId);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return a HashCode based on the sensorId</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.sensorId;
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return this.sensorType.acronym + "[" + this.sensorId + "]";</p>
   */
  @Override
  public String toString() {
    String result = (this.stationId == null)? "": (this.stationId + ".");
    result += this.sensorType.acronym + "[" + this.sensorId + "]";
    return result;
  }
  //</editor-fold>
}
