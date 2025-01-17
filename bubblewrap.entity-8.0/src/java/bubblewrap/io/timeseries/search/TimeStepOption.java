package bubblewrap.io.timeseries.search;

import bubblewrap.core.selectors.SelectObject;
import bubblewrap.io.timeseries.TimeStepValue;

/**
 * A SelectObject that wraps a TimeStepValue. On initiation is sets the SelectObject's
 * {@linkplain #setState(java.lang.Boolean, java.lang.Boolean) state} based on
 * the TimeStepValue's {@linkplain TimeStepValue#isReadOnly() isReadOnly} and
 * {@linkplain TimeStepValue#isDisabled() isDisabled} states, respectively.
 * <p>
 * By default the SelectObject's {@linkplain #getLabel() Label} is set as the 
 * TimeStepValue's {@linkplain TimeStepValue#getDateTime() dateTime} formatted as
 * a "MM/dd/yyyy" date string and its selectId as the TimeStepValue's {@linkplain 
 * TimeStepValue#getMilliSeconds() milliSeconds} as a string.
 * @author Charlie Lay/Koos Prins
 */
public class TimeStepOption<TItem extends TimeStepValue> extends SelectObject<TItem> {
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TimeStepOption(TItem item) {
    super(item);
    if (item == null) {
      this.setState(true, true, false);
    } else {
      this.setState(item.isReadOnly(), item.isDisabled(), false);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   * @return String of the selected item id
   */
  @Override
  protected String onGetSelectId() {
    return this.getValue().getMilliSeconds().toString();
  }
  
  /**
   * <p>Get the time step date string in the format of "MM/dd/yyyy"</p>
   * @return String the date string format of "MM/dd/yyyy"
   */
  @Override
  public String getLabel() {
    String result = null;
    if ((this.getValue() != null)
        && (this.getValue().getDateTime() != null)){
      result = this.getValue().getDateTime().toLocaleString("MM/dd/yyyy");
    }
    return result;
  }
  // </editor-fold>
}
