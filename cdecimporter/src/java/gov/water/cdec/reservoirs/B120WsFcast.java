package gov.water.cdec.reservoirs;

import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;

/**
 * A Class for summarizing a B120 AJ Forecast
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class B120WsFcast implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Forecast Date
   */
  public final String resId;
  /**
   * The Forecast Date
   */
  public final DateTime fcastDt;
  /**
   * The Median Forecast
   */
  public final Double fcastMed;
  /**
   * The 90th-Percentile Forecast
   */
  public final Double fcast90;
  /**
   * The 10th-Percentile Forecast
   */
  public final Double fcast10;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public B120WsFcast(String resId, DateTime fcastDt, Double fcast90, Double fcastMed, 
                                                                      Double fcast10) {
    super();  
    this.resId = resId;
    this.fcastDt = fcastDt;
    this.fcast90 = fcast90;
    this.fcastMed = fcastMed;
    this.fcast10 = fcast10;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "B120WsFcast[ resId=" + this.resId + "; dt=" + 
 this.fcastDt.toLocaleString("MM/dd/yyyy") + "]"</p>
   */
  @Override
  public String toString() {
    return "B120WsFcastData[ resId=" + this.resId + "; dt=" 
            + this.fcastDt.toLocaleString("MM/dd/yyyy") + "]";
  }
  // </editor-fold>
}
