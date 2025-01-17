package bubblewrap.io.converters;

/**
 * 
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum UnitConverter {
  
  meter(UnitType.Distance, 1.0d),
  micron(UnitType.Distance, 0.000001d),
  millimeter(UnitType.Distance, 0.001d),
  centimeter(UnitType.Distance, 0.01d),
  kilometer(UnitType.Distance, 1000d),
  inch(UnitType.Distance, 0.025400d),
  feet(UnitType.Distance, 0.304800d),
  yard(UnitType.Distance, feet.toMetric*3.0d),
  mile(UnitType.Distance, feet.toMetric*5280.0d),
  
  /**
   * Square meter (base metric unit) 
   */
  m2(UnitType.Area, 1.0d),
  /**
   * Square millimeter 
   */
  mm2(UnitType.Area, millimeter.toMetric * millimeter.toMetric),
  cm2(UnitType.Area, centimeter.toMetric * centimeter.toMetric),
  km2(UnitType.Area, kilometer.toMetric * kilometer.toMetric),
  hectar(UnitType.Area, 10000.0d),
  inch2(UnitType.Area, inch.toMetric * inch.toMetric),
  ft2(UnitType.Area, feet.toMetric * feet.toMetric),
  yrd2(UnitType.Area, yard.toMetric * yard.toMetric),
  mile2(UnitType.Area, mile.toMetric * mile.toMetric),
  acres(UnitType.Area, 4046.86d),
  
  liter(UnitType.Volume, 1.0d),
  m3(UnitType.Volume, 1000.0d),
  ml(UnitType.Volume, 0.001d),
  cc(UnitType.Volume, 0.001d),
  cl(UnitType.Volume, 0.01d),
  KL(UnitType.Volume, 1000.0d),
  ML(UnitType.Volume, 1000000.0d),
  inch3(UnitType.Volume, 0.016387d),
  ft3(UnitType.Volume, 28.316847d),
  yrd3(UnitType.Volume, 764.554860d),
  gal(UnitType.Volume, 3.785412d),
  gal_impl(UnitType.Volume, 4.546090),
  ac_ft(UnitType.Volume, acres.toMetric * feet.toMetric * m3.toMetric),
  taf(UnitType.Volume, ac_ft.toMetric * 1000.0d),
  
  m3s(UnitType.Flow, 1.0d),
  liter_sec(UnitType.Flow, 0.001d),
  liter_min(UnitType.Flow, liter_sec.toMetric / 60.0d),
  liter_hr(UnitType.Flow, liter_min.toMetric / 60.0d),
  liter_day(UnitType.Flow, liter_hr.toMetric / 24.0d),
  MLD(UnitType.Flow, liter_day.toMetric * 1000000.0d),
  cfs(UnitType.Flow, ft3.toMetric * liter_sec.toMetric),
  gpd(UnitType.Flow, gal.toMetric * liter_day.toMetric),
  gpm(UnitType.Flow, gal.toMetric * liter_min.toMetric),
  mgd(UnitType.Flow, gpd.toMetric * 1000000.0d),
  gpd_impl(UnitType.Flow, gal_impl.toMetric * liter_day.toMetric),
  gpm_impl(UnitType.Flow, gal_impl.toMetric * liter_min.toMetric),
  mgd_impl(UnitType.Flow, gpd_impl.toMetric * 1000000.0d),
  acft_day(UnitType.Flow, ac_ft.toMetric * liter_day.toMetric);
  // <editor-fold defaultstate="collapsed" desc="Public Enum Value">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public 
   */
  public final UnitType unitType;
  /**
   * Define this unit in its metric standard unit (e.g., 1 foot = 0.3048 meter)
   */
  protected final double toMetric;
  /**
   * Public Constructor  
   */
  private UnitConverter(UnitType unitType, double toMetric) {
    this.unitType = unitType;
    this.toMetric = toMetric;
  }
  
  /**
   * Called to convert this Unit to the specified <tt>toUnit</tt>. It converts the value
   * to its metric base unit (e.g., meters for distance) and the convert the value from
   * metric to the required <tt>toUnit</tt>.
   * @param toUnit to target unit to convert to
   * @param value the value to convert
   * @return the converted value or null if value = null | NaN
   * @throws NullPointerException if <tt>toUnit is undefined</tt>
   * @throws IllegalArgumentException if this.unitType does not match toUnit.unitType.
   */
  public final Double to(UnitConverter toUnit, Double value) {
    Double result = null;
    if ((value != null) && (!value.isNaN())) {      
      if (toUnit == null) {
        throw new NullPointerException("The To Unit cannot be undefined.");
      }
      
      if (!this.unitType.equals(toUnit.unitType)) {
        throw new IllegalArgumentException("Incompatible Unit Type[" + toUnit.unitType 
                + "], exepected a Unit Type[" + this.unitType + "].");
      }
      result = (value * this.toMetric)/(toUnit.toMetric);
    }
    return result;
  }
  // </editor-fold>

}
