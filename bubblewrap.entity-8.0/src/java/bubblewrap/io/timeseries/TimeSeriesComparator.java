package bubblewrap.io.timeseries;

import bubblewrap.io.schedules.TimeStep;
import java.util.Comparator;

/**
 * A Comparator for sorting {@linkplain TimeStepValue} entities by their 
 * {@linkplain TimeStep#getMilliSeconds() TimeStep.milliSeconds}
 * @author kprins
 */
public class TimeSeriesComparator implements Comparator<TimeStepValue> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Flag controlling the sort Order (default=true|null)
   */
  private Boolean mbAscending;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public TimeSeriesComparator() {
    super();    
    this.mbAscending = null;
  }
  
  /**
   * Public Constructor
   */
  public TimeSeriesComparator(boolean bAscending) {
    this();    
    this.mbAscending = (bAscending)? null: bAscending;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Comparator isAscendign sort order setting.
   * @return true if sorting in ascending order.
   */
  public boolean isAscending() {
    return ((this.mbAscending == null) || (this.mbAscending));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement Comparator">
  /**
   * {@inheritDoc} <p>IMPLEMENT: return 0 if both values are null. Return -1 if
   * pValue1=null, or 1 if pValue2=null. Otherwise, get the values TiemStep in
   * MilliSeconds and return the 1, 0, or -1 if the Timestep for pValue1 is greater
   * than, equal to, or less than that of pValue2, respectively. if the comparison is 
   * not equal (0) and (!this.isAscending), switch the returned value form positive to
   * negative and vise versa.
   * </p>
   */
  @Override
  public int compare(TimeStepValue step1, TimeStepValue step2) {
    int result = 0;
    if ((step1 != null) && (step2 != null)) {
      Long milliSec1 = null;
      Long milliSec2 = null;
      if ((step1 == null) || ((milliSec1 = step1.getMilliSeconds()) == null)) {
        result = -1;
      } else if ((step2 == null) || ((milliSec2 = step2.getMilliSeconds()) == null)){
        result = 1;
      } else {
        result = milliSec1.compareTo(milliSec2);
      }
    }
    
    if ((result != 0) && (!this.isAscending())) {
      result = -1*result;
    }
    
    return result;
  }
  //</editor-fold>
}
