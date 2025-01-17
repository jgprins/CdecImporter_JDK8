package gov.water.cdec.reservoirs.annotations;

import bubblewrap.io.DataEntry;
import gov.water.cdec.reservoirs.ReservoirSensor;
import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * An implementation of the {@linkplain ReservoirDef} annotation
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirImpl implements Serializable, ReservoirDef{

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * See {@linkplain ReservoirDef#id()}
   */
  private String id;
  /**
   * See {@linkplain ReservoirDef#name()}
   */
  private String name;
  /**
   * See {@linkplain ReservoirDef#capacity()}
   */
  private Double capacity;
  /**
   * See {@linkplain ReservoirDef#sensorStor()}
   */
  private SensorImpl sensorStor;
  /**
   * See {@linkplain ReservoirDef#sensorQin()}
   */
  private SensorImpl sensorQin;
  /**
   * See {@linkplain ReservoirDef#sensorFnf()}
   */
  private SensorImpl sensorFnf;
  /**
   * See {@linkplain ReservoirDef#sensorQout()}
   */
  private SensorImpl sensorQout;
  /**
   * See {@linkplain ReservoirDef#sensorToc()}
   */
  private SensorImpl sensorToc;
  /**
   * See {@linkplain ReservoirDef#wsFcastId()}
   */
  private String wsFcastId; 
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ReservoirImpl(ReservoirDef annot) {
    super();   
    if (annot == null) {
      throw new NullPointerException("The ReservoirDef annotation is undefined");
    }
    if ((this.id = DataEntry.cleanUpString(annot.id())) == null) {
      throw new NullPointerException("The ReservoirDef's reservoir Id is undefined");
    }
    if ((this.name = DataEntry.cleanString(annot.name())) == null) {
      throw new NullPointerException("The ReservoirDef's reservoir Name is undefined");
    }
    if (((this.capacity = annot.capacity()) == null) || (this.capacity.isNaN())) {
      throw new NullPointerException("The ReservoirDef's reservoir capacity is undefined");
    }
    if ((this.sensorStor = new SensorImpl(annot.sensorStor())) == null) {
      throw new NullPointerException("The ReservoirDef's Storage Sensor is undefined");
    }
    if ((this.sensorQin = new SensorImpl(annot.sensorQin())) == null) {
      throw new NullPointerException("The ReservoirDef's Inflow Sensor is undefined");
    }
    if ((this.sensorFnf = new SensorImpl(annot.sensorFnf())) == null) {
      throw new NullPointerException("The ReservoirDef's FNF Sensor is undefined");
    }
    if ((this.sensorQout = new SensorImpl(annot.sensorQout())) == null) {
      throw new NullPointerException("The ReservoirDef's Outflow Sensor is undefined");
    }
    if ((this.sensorToc = new SensorImpl(annot.sensorToc())) == null) {
      throw new NullPointerException("The ReservoirDef's TOC Sensor is undefined");
    }
    
    this.wsFcastId = DataEntry.cleanString(annot.wsFcastId());
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Check if the Reservoir has a designated WSForecat ID
   * @return (this.wsFcastId != null)
   */
  public boolean hasWsFcast() {
    return (this.wsFcastId != null);
  }
  
  /**
   * Get the SensorImpl associated with the specified ReservoirSensor
   * @param sensor the ReservoirSensor
   * @return the SensorImpl
   */
  public SensorImpl getSensorDef(ReservoirSensor sensor) {
    SensorImpl result = null;
    switch (sensor) {
      case QIN:
        result = this.sensorQin;
        break;
      case QOUT:
        result = this.sensorQout;
        break;
      case FNF:
        result = this.sensorFnf;
        break;
      case STOR:
        result = this.sensorStor;
        break;
      case TOC:
        result = this.sensorToc;
        break;
    }
    return result;
  }
   // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="ReservoirDef Overrides">
  /**
   * See {@linkplain ReservoirDef#id()}
   */
  @Override
  public String id() {
    return this.id;
  }
  /**
   * See {@linkplain ReservoirDef#name()}
   */
  @Override
  public String name() {
    return this.name;
  }
  /**
   * See {@linkplain ReservoirDef#capacity()}
   */
  @Override
  public double capacity() {
    return this.capacity;
  }
  /**
   * See {@linkplain ReservoirDef#sensorStor()}
   */
  @Override
  public SensorDef sensorStor() {
    return this.sensorStor;
  }
  /**
   * See {@linkplain ReservoirDef#sensorQin()}
   */
  @Override
  public SensorDef sensorQin() {
    return this.sensorQin;
  }
  /**
   * See {@linkplain ReservoirDef#sensorFnf()}
   */
  @Override
  public SensorDef sensorFnf() {
    return this.sensorFnf;
  }
  /**
   * See {@linkplain ReservoirDef#sensorQout()}
   */
  @Override
  public SensorDef sensorQout() {
    return this.sensorQout;
  }
  /**
   * See {@linkplain ReservoirDef#sensorToc()}
   */
  @Override
  public SensorDef sensorToc() {
    return this.sensorToc;
  }
  /**
   * See {@linkplain ReservoirDef#wsFcastId()} - can be ""
   */
  @Override
  public String wsFcastId() {
    return (this.wsFcastId == null)? "": this.wsFcastId;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return ReservoirDef.class;
  }
  // </editor-fold>
}
