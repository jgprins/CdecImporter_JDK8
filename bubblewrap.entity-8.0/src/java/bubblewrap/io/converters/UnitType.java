package bubblewrap.io.converters;

/**
 * Definition of 
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum UnitType {
  Distance(UnitConverter.meter),
  Area(UnitConverter.m2),
  Volume(UnitConverter.liter),
  Flow(UnitConverter.m3s);
  
  //<editor-fold defaultstate="collapsed" desc="Enum Constructor">
  /**
   * The UnitType base Metric unit
   */
  public final UnitConverter metricUnit;
  /**
   * Constructor
   * @param metricUnit
   */
  private UnitType(UnitConverter metricUnit) {
    this.metricUnit = metricUnit;
  }
//</editor-fold>
}
