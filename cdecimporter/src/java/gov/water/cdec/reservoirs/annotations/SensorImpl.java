package gov.water.cdec.reservoirs.annotations;

import bubblewrap.io.DataEntry;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.ca.water.cdec.enums.DurationCodes;
import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * An implementation of the {@linkplain SensorDef} annotation
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class SensorImpl implements Serializable, SensorDef{

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * See {@linkplain SensorDef#stationId()}
   */
  private String stationId;
  /**
   * See {@linkplain SensorDef#sensorType()}
   */
  private CdecSensorTypes sensorType;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SensorImpl(SensorDef annot) {
    super();  
    if (annot == null) {
      throw new NullPointerException("The SensorDef annotation is undefined");
    }
    if ((this.stationId = DataEntry.cleanString(annot.stationId())) == null) {
      throw new NullPointerException("The SensorDef's StationId is undefined");
    }
    if ((this.sensorType = annot.sensorType()) == null)  {
      throw new NullPointerException("The SensorDef's SensorType is undefined or invalid");
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Duration Code of the assigned Sensor Type
   * @return this.sensorType.durationCode
   */
  public DurationCodes getDurationCode() {
    return this.sensorType().getDurationCode();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="SensorDef Overrides">
  /**
   * See {@linkplain SensorDef#stationId()}
   */
  @Override
  public String stationId() {
    return this.stationId;
  }
  /**
   * See {@linkplain SensorDef#sensorType()}
   */
  @Override
  public CdecSensorTypes sensorType() {
    return this.sensorType;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return SensorDef.class;
  }
  // </editor-fold>
}
