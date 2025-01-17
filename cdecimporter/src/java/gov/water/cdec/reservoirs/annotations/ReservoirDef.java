package gov.water.cdec.reservoirs.annotations;

import java.lang.annotation.*;

/**
 * An Annotation that define a Reservoirs Parameters
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ReservoirDef {
  /**
   * The reservoir ID (typically the CDEC ID)
   * @return the assigned ID
   */
  String id();
  /**
   * The reservoir's display name 
   * @return the assigned name
   */
  String name();
  /**
   * The reservoir's full capacity
   * @return the assigned capacity
   */
  double capacity();
  /**
   * The reservoir's Storage Sensor Definition
   * @return the assigned info
   */
  SensorDef sensorStor();
  /**
   * The reservoir's Inflow Sensor Definition
   * @return the assigned info
   */
  SensorDef sensorQin();
  /**
   * The reservoir's FNF Inflow Sensor Definition
   * @return the assigned info
   */
  SensorDef sensorFnf();
  /**
   * The reservoir's Release/Outflow Sensor Definition
   * @return the assigned info
   */
  SensorDef sensorQout();
  /**
   * The reservoir's TOC Sensor Definition
   * @return the assigned info
   */
  SensorDef sensorToc();
  /**
   * The reservoir's WS Forecast Identifier 
   * @return the assigned identifier
   */
  String wsFcastId();  
}
