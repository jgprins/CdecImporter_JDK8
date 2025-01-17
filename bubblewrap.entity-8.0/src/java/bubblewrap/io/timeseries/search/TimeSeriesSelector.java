package bubblewrap.io.timeseries.search;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.selectors.TableSelector;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.timeseries.TimeSeries;
import bubblewrap.io.timeseries.TimeStepValue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class extends TableSelector to render {@linkplain TimeSeries} table's using the
 * TimeSeries' {@linkplain TimeStepValue TimeStepValues} to build a selectable list of
 * {@linkplain TimeStepOption TimeStepOption}.
 * <p>
 * There are three option to initiate the TimeSeriesSelector. Inheritors can assign the
 * custom TimeSeries using the {@linkplain #onInitSelector(java.lang.Class) 
 * onInitSelector(timeSeriesClass)} method if the the timeseries is a Session ManagedBean,
 * or initiate an instance of the TimeSeries and assign it using the {@linkplain 
 * #onInitSelector(bubblewrap.io.timeseries.TimeSeries) onInitSelector(timeSeries)} 
 * method, or override the {@linkplain #onGetSelectionItems() onGetSelectionItems}
 * method to return the list of selectable TimeStepValues on the fly.
 * 
 * @author Charlie Lay
 */
public abstract class TimeSeriesSelector<TStep extends TimeStepValue,
                                  TSeries extends TimeSeries<TStep>> 
                                  extends TableSelector<TStep, TimeStepOption<TStep>> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The selector time series.
   */
  private TSeries timeSeries;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TimeSeriesSelector() {
    super();
    this.timeSeries = null;
  }
  // </editor-fold>  
  
  // <editor-fold defaultstate="collapsed" desc="Protected Seach Initiation Methods">
  /**
   * <p>
   * Assign the <tt>timeseriesClass</tt> (the TimeSeries class to be used in retrieving 
   * a Session ManagedBean for).</p>
   * <p>
   * <b>Note:</b> - it also adds this selector listener the
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param searchClass the {@linkplain TimeSeries} to retrieve a Session ManagedBean for.
   * @exception NullPointerException if the searchClass is unassigned or the ManagedBean
   * is not accessible.
   */
  protected final void onInitSelector(Class<TSeries> timeseriesClass) {
    this.timeSeries = null;
    if (timeseriesClass == null) {
      throw new NullPointerException("The TimeSeries Class cannot be unassigned.");
    }
    TSeries ts = SessionHelper.getManagedBean(timeseriesClass);
    if (ts == null) {
      throw new NullPointerException("Unable to access the Session Instance of "
              + "SearchClass[" + timeseriesClass.getSimpleName() + "].");
    }
    this.onInitSelector(ts);
  }

  /**
   * <p>
   * Assign the <tt>timeseries</tt> (the {@linkplain TimeSeries} to be used in 
   * retrieving the TimeStepValue to build the Selection Options)</p>
   * <p>
   * An exception will be thrown if (timeseries=null).</p>
   * <p>
   * <b>Note:</b> - it also adds this selector listener the
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param timeseries to {@linkplain TimeSeries} to select TimeStepValue from
   * @exception NullPointerException if TimeSeries unassigned.
   */
  protected final void onInitSelector(TSeries timeseries) {
    if (timeseries == null) {
      throw new NullPointerException("The EntitySearch reference cannot be unassigned.");
    }
    this.timeSeries = timeseries;
    this.timeSeries.TimeSeriesChanged.add(new EventDelegate(this) {
      
      @Override
      public void onEvent(Object sender, EventArgs args) {
        TimeSeriesSelector listener = this.getListener();
        if ((listener != null) && (listener.timeSeries == sender)) {
          try {
            String selectId = listener.getSelectId();
            listener.onSelectItemsChanged();
            listener.setSelectId(selectId);   
            /**
             * Only fire the SelectIonChanged event if the listener.selectId has not 
             * changed
             */
            if (DataEntry.isEq(selectId, listener.getSelectId(), true)) {
              listener.fireSelectionChanged();
            }
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }
      }
    });
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get A reference to the assigned timeSeries
   * @return reference to this.timeSeries (can be null if not initiated)
   */
  public TSeries getTimeSeries() {
    return this.timeSeries;
  }
  
  /**
   * Call to set the Selector's selectItem by its TimeStep date. If selectDt = null, or
   * no match is found, set this.selectId = null, else set this.selectId.
   * @param selectDt the date to search for.
   */
  public void setSelectedDate(DateTime selectDt) {
    try {
      if ((selectDt == null) || (this.timeSeries == null) || (this.timeSeries.isEmpty())){
        this.setSelectId(null);
        return;
      }
      
      TStep timeStep = null;
      TimeStepOption<TStep> option = null;
      if ((this.timeSeries.isInRange(selectDt)) &&
          ((timeStep = this.timeSeries.getTimeValue(selectDt)) != null) && 
          ((option = this.newSelectOption(timeStep)) != null)) {
        if (!this.gotoPageItem(option.getSelectId())) {
          this.setSelectId(null);
        }
      } else {
        this.setSelectId(null);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelectedDate Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="TableSelector Overrides">
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
   * <p>
   * OVERRIDE: Call super method before release the local resources and disconnect all
   * event listeners</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.timeSeries = null;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return new EntitySelectOption<>(selectItem)</p>
   */
  @Override
  protected TimeStepOption<TStep> newSelectOption(TStep item) {
    TimeStepOption<TStep> result = null;
    try {
      result = new TimeStepOption<>(item);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.newSelectOption Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } 
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @SuppressWarnings(value = "unchecked")
  @Override
  protected String onChangeSelection(String selectId) {
    String result = super.onChangeSelection(selectId);
    try {
      TStep item = null;
      TimeStepOption<TStep> option = this.getSelectOption(selectId);
      if (option != null) {
        item = option.getValue();        
      }
      this.onSelectionChanged(item);
    } catch (Exception exp) {
      result = null;
    } 
    return result;
  }
    
  /**
   * <p>Convert the <tt>timeSeries</tt> to a list for populating the faces component.
   * @return list if records.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected final List<TStep> onGetSelectionItems() {
    List<TStep> result = null;
    try {
      if (this.timeSeries != null){
        result = new ArrayList<>();
        for (TStep tstep : this.timeSeries) {
          if (this.onIsValidSelectItem(tstep)){
            result.add(tstep);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetSelectionItems Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return false if ((selectItem = null) | (selectItem.isNullValue) | 
   *                                                  (selectItem.isMissingValue)) - an 
   * EntityTableSelector does not support null or new records.</p>
   */
  @Override
  protected boolean onIsValidSelectItem(TStep selectItem) {
    return ((selectItem != null) 
            && (!selectItem.isNullValue())
            && (!selectItem.isMissingValue()));
  }
  
  /**
   * CAN OVERRIDE: Override this method when using a ChildSelector to update the
   * ChildSelector's filters/options with any change in the Parent's Selection.
   * the base method does nothing.
   * @param record the newly selected record
   * @throws Exception
   */
  protected void onSelectionChanged(TStep record) throws Exception {}
  // </editor-fold>
}
