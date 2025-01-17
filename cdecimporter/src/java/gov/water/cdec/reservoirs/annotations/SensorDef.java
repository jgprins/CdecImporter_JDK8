
package gov.water.cdec.reservoirs.annotations;

import gov.ca.water.cdec.core.CdecSensorTypes;
import java.lang.annotation.*;

/**
 * An Annotation to define a CDEC Sensor
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SensorDef {
  /**
   * The Sensor's CDEC ID
   * @return the assigned ID
   */
  String stationId();
  /**
   * The Sensor's CDEC SensorType 
   * @return the assigned SensorNo
   */
  CdecSensorTypes sensorType();
}
