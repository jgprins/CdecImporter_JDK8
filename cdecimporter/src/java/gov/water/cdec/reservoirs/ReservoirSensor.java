package gov.water.cdec.reservoirs;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum ReservoirSensor {
  
  // <editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * The Inflow Sensor
   */// <editor-fold defaultstate="expanded" desc="Enum Values">
  /**
   * The Inflow Sensor
   */
  QIN(0, "qin", 1.0),
  /**
   * The FNF Sensor
   */
  FNF(1, "fnf", 1.0),
  /**
   * The Outflow/Release Sensor
   */
 QOUT(2, "qout", 1.0),
  /**
   * The Storage Volume Sensor
   */
 STOR(3, "stor", 0.001),
  /**
   * The Storage Volume Sensor
   */
 TOC(4, "toc", 0.001);
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * A Defined enum value (not its ordinate)
   */
  public final int value;
  /**
   * A Display field for the enum option
   */
  public final String field;
  /**
   * The Factor with which to scale the observed value
   */
  public final double scaleFactor;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor
   * @param value the option value
   * @param field the option JSON field
   * @param scaleFactor the Factor with which to scale the observed value
   */
  private ReservoirSensor(int value, String field, double scaleFactor) {
    this.field = field;
    this.value = value;
    this.scaleFactor = scaleFactor;
  }
  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the ReservoirSensor associated with <tt>value</tt>
   * @param value the ReservoirSensor.value to search for
   * @return the matching ReservoirSensor or null if not found.
   */
  public static ReservoirSensor fromValue(Integer value) {
    ReservoirSensor result = null;
    if (value != null) {
      for (ReservoirSensor enumVal : ReservoirSensor.values()) {
        if (enumVal.value == value) {
          result = enumVal;
          break;
        }
      }
    }
    return result;
  }
  // </editor-fold>

}
