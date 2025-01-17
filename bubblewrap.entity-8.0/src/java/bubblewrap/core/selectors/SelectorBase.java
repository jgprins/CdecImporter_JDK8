package bubblewrap.core.selectors;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.events.EventHandler;
import static bubblewrap.core.selectors.Selector.logger;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Base Class for all selector's with a generic select item class (TItem) and
 * and generic Select Option (TOption).
 * @author Charlie Lay.K and J.G. "Koos" Prins, D.Eng., P.E.
 */
public abstract class SelectorBase<TItem extends Serializable
                                ,TOption extends Serializable> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(Selector.class.getSimpleName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="ChildSelectionChangedDelegate Class">
  /**
   * A Delegate Class for assigning to the Child Selector's SelectionChanged event.
   */
  private class ChildSelectionChangedDelegate extends EventDelegate {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public ChildSelectionChangedDelegate(SelectorBase listener) {
      super(listener);
    }
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Call the super method before disposing local resources</p>
     */
    @Override
    protected void finalize() throws Throwable {
      super.finalize();
    }
    //</editor-fold>
    
    @SuppressWarnings("unchecked")
    @Override    
    public void onEvent(Object sender, EventArgs eventInfo) {
      SelectorBase listener = this.getListener();
      if ((listener != null) && (listener.childSelector == sender)) {
        try {
          listener.fireSelectionChanged();
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the selected Item's Id
   */
  private String selectId;
  /**
   * Placeholder for the isOptional flag - control whether the NullItem is displayed
   * when the Selection List is empty.
   */
  private Boolean optional;
  /**
   * Placeholder for default Option that could be sued to initiate the selectedItem
   */
  private TItem defaultOption;
  /**
   * Placeholder for a Sub/Child Selector which depends in this selector's selection
   * Override onSelectionChanged to update the ChildSelector Search or Filter settings.
   */
  private Selector<?> childSelector;
  /**
   * A Transient Flag that can be set by calling this.doValidateSelectId, which will
   * force the validation of the current selectId with the first getSelectId call
   */
  private transient boolean validateSelectId = false;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Event(s)">
  /**
   * EventHandler for sending a Selection Changed event.
   */
  public final EventHandler SelectionChanged;
  
  /**
   * Method called to fie the Selection Changed event.  
   */
  protected void fireSelectionChanged() {
    if (!this.isChanging()) {  
      this.SelectionChanged.fireEvent(this, new EventArgs());
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor(s)">
  /**
   * Protected Parameterless Constructor
   */
  protected SelectorBase() {
    super();  
    this.SelectionChanged = new EventHandler();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.SelectionChanged.clear();
    this.selectId = null;
    if (this.childSelector != null) {
      this.childSelector.SelectionChanged.remove(this);
      this.childSelector = null;
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private/protected Methods">
  /**
   * Called by {@linkplain #setSelectId(java.lang.String) this.setSelectId} to handle
   * the selectId change process. If (!this.isChanging), set this.isChanging=true
   * and set this.selectId = selectId before calling
   * {@linkplain #onChangeSelection(java.lang.String) this.onChangeSelection} to custom
   * handle the change in selection and set return seelctId as the final this.selectId.
   * Finally, reset this.isChanging (if previously set) and call
   * {@linkplain #fireSelectionChanged() this.fireSelectionChanged()} to fire the
   * SelectionChanged event if the selection has changed.
   * @param selectId the new selection Item's Id.
   */
  protected final void changeSelection(String selectId) {
    try {
      this.beginChange();
      this.selectId = selectId;
      this.selectId = this.onChangeSelection(selectId);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.changeSelection Error:\n {1}", 
                        new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endChange();
    }
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #isNull() this.isNull} to check if 
   * this.selectId represents a null-selection. The base method always returns false.
   * @return true if isNull
   */
  protected boolean onIsNullItem(String selectId) {
    return false;
  }
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #getSelectId() this.getSelectId} to get the 
   * null item's selectId. The base method returns null.
   * @return the null item's selectId
   */
  protected String onGetNullId() {
    return null;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public isChanging State Management">
  /**
   * Transient counter for managing the isChanging state
   */
  private transient int countChange = 0;

  /**
   * Get the current isChanging state
   * @return true if (this.countChange > 0)
   */
  public final boolean isChanging() {
    return (this.countChange > 0);
  }

  /**
   * Called to start the Change process. It increment this.countChange.
   * <p>
   * <b>NOTE:</b> Every call to beginChange must be followed by a call to {@linkplain
   * #endChange() this.endChange}.</p>
   */
  public final void beginChange() {
    this.countChange = (this.countChange < 0) ? 0 : this.countChange;
    this.countChange++;
  }

  /**
   * Called - after calling {@linkplain #beginChange() this.beginChange} - to decrement
   * this.countChange and to call {@linkplain #onChangeEnds() this.onChangeEnds} to
   * process the event.
   */
  public final void endChange() {
    if (this.countChange > 0) {
      this.countChange--;
      if (this.countChange == 0) {
        this.onChangeEnds();
      }
    }
  }

  /**
   * Calls to execute any post-process after the SelectorBase's settings have changed
   * (e.g. fire an event). Calls {@linkplain #fireSelectionChanged() 
   * this.fireSelectionChanged}
   */
  protected void onChangeEnds() {
    this.fireSelectionChanged();
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Set the selector's defaultOption (default=null)
   * @param defaultOption the new default value
   */
  public final void setDefaultOption(TItem defaultOption) {
    this.defaultOption = defaultOption;
  }

  /**
   * Get the selector's defaultOption
   * @return the assigned value (default=null)
   */
  public final TItem getDefaultOption() {
    return this.defaultOption;
  }

  /**
   * Return true if the NullId  has been initiated and the current SelectItem ID
   * equals the NullId or if the this.SelectId=null .
   * @return boolean
   */
  public final boolean isNull() {
    return ((this.selectId == null) || (this.onIsNullItem(this.selectId)));    
  }
  
  /**
   * Call to set this.validateSelectId, which will force the validation of the selectId 
   * on the next call to {@linkplain #getSelectId() this.getSelectId}
   */
  public final void doValidateSelectId() {
    this.validateSelectId = true;
  }

  /**
   * Get the current selected SelectItem ID (only for used by FacePage). The
   * selectId can be {@linkplain #nullId this.nullId}, {@linkplain #addNewId
   * this.addNewId}, or the selected record's {@linkplain #getRecordId() recordId}.
   * @return the previously set selectId (
   */
  public final String getSelectId() {
    if (this.validateSelectId) {
      try{
        if (this.selectId != null) {
          String validId = this.onChangeSelection(this.selectId);
          if (!DataEntry.isEq(validId, this.selectId,true)) {
            this.selectId = validId;
            this.fireSelectionChanged();
          }
        }
      } finally {
        this.validateSelectId = false;
      }
    }
    return (this.selectId == null) ? this.onGetNullId() : this.selectId;
  }

  /**
   * <p>Set the new selected SelectItem ID (only for used by FacePage controls). If the
   * selectId is equal to {@linkplain #onIsNullItem(java.lang.String)  
   * this.onIsNullItem(selectId)} it is set to null.</p>
   * <p>If the selection has changed, {@linkplain #changeSelection(java.lang.String)  
   * this.changeSelection(selectId)} is called.</p>
   * @param pSelectId String
   */
  public final void setSelectId(String selectId) {
    selectId = DataEntry.cleanString(selectId);
    if ((selectId != null) && (this.onIsNullItem(selectId))) {
      selectId = null;
    }
    
    if (!DataEntry.isEq(this.selectId, selectId, true)) {
      this.changeSelection(selectId);
    }
  }

  /**
   * Call to clear the selection - i.e., set this.selectId=null.
   */
  public final void clearSelection() {
    this.setSelectId(null);
  }

  /**
   * Get a reference to this Selector's ChildSelector - return null if unassigned
   * @param <TChild> extends Selector
   * @return the child selector or null if unassigned.
   */
  @SuppressWarnings(value = "unchecked")
  public final boolean hasChildSelector() {
    return (this.childSelector != null);
  }

  /**
   * Get a reference to this Selector's ChildSelector - return null if unassigned
   * @param <TChild> extends Selector
   * @return the child selector or null if unassigned.
   */
  @SuppressWarnings(value = "unchecked")
  public final <TChild extends Selector> TChild getChildSelector() {
    TChild result = (this.childSelector == null)? null: (TChild) this.childSelector;
    return result;
  }

  /**
   * Assign a Child/SubSelector to the Selector. Add this selector as a listener to the
   * Child Selector's SelectionChanged event using a {@linkplain
   * ChildSelectionChangedDelegate}.
   * @param childSelector to assign or null to clear the child selectors
   */
  public final void setChildSelector(Selector<?> childSelector) {
    if (this.childSelector != null) {
      this.childSelector.SelectionChanged.remove(this);
    }
    this.childSelector = childSelector;
    this.onSetChildSelector(childSelector);
    if (this.childSelector != null) {
      this.childSelector.SelectionChanged.add(new ChildSelectionChangedDelegate(this));
    }
    this.fireSelectionChanged();
  }

  /**
   * The method is called by the FacePage to get a list of SelectItems to display. It
   * retrieve the list of record to display as follows:<ul>
   * <li>It calls {@link #onGetSelectionItems() this.onGetSelectionOptions} 
   * to get a list of selection Options from which to build the selectItem list.</li>
   * <li>If the Selection Options list is not empty, it calls {@link 
   * #onSortSelectionItems(java.util.List) this.onSortSelectionOptions} to sort the
   * list.</li>
   * <li>It calls {@link #buildSelectOptions this.buildSelectItemList} to 
   * generate the list of SelectItems to return.</li>
   * </ul>
   * <p>All Errors are trapped and logged.</p>
   * @return the list of SelectItems of null if an error occurred.
   */
  public final Collection<TOption> getSelectOptions() {
    Collection<TOption> result = null;
    try {
      result = this.onGetSelectOptions();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getSelectOptions Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (result == null)? new ArrayList<TOption>(): result;
  }
    
  /**
   * Called by {@linkplain #getSelectOptions() this.getSelectOptions} to build the List 
   * of selectOptions - It initiates the list of Select Options to return and after 
   * it added the Null Item (if set), it calls 
   * {@link #onBuildSelectOptionList(java.util.List, java.util.List) 
   * this.onBuildSelectItemList} to add the selected Options as SelectItems.
   * It return an empty list if no items were added.
   * @param entList List if entities to select from
   * @return the select option list
   * @throws Exception 
   */
  protected final List<TOption> buildSelectOptions(List<TItem> selectOptions) 
          throws Exception {
    List<TOption> result = new ArrayList<>();

    /* If pRecordSet is not empty or if isOptional and setNullItem, add the null item */
    if (((selectOptions != null) && (!selectOptions.isEmpty())) || (this.isOptional())) {
      this.onAddNullSelectOption(result);
    }

    /* If pRecordSet is not emty call onBuildSelectItemList to add the additional items */
    if ((selectOptions != null) && (!selectOptions.isEmpty())) {
      this.onBuildSelectOptionList(result, selectOptions);
    }
    
    /* If the selection list is still empty, call this.onAddEmptySelectOption to add
     * an emptyItem (if so required).
     */
    if (result.isEmpty()) {
      this.onAddEmptySelectOption(result);
    }
    return result;
  }

  /**
   * Return true if the Null Item is displayed if the Selection Options is empty
   * (i.e., providing that the NullItem is set)
   * @return true if optional is set..
   */
  public final boolean isOptional() {
    return (this.optional != null) && (this.optional);
  }

  /**
   * Return true if the Null Item is displayed if the Selection Options is empty
   * (i.e., providing that the NullItem is set)
   * @return true if optional is set..
   */
  protected final void setOptional(Boolean isOptional) {
    this.optional = ((isOptional == null) || (!isOptional))? null: this.optional;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Overridable and Abstract Methods">
  /**
   * ABSTRACT: Get the current SelectedItem
   * @return The selectedItem represented by the selectedId
   */
  public abstract TItem getSelectedItem();
    
  /**
   * ABSTRACT: Called by {@linkplain #getSelectOptions() this.getSelectOptions} to 
   * initiate the list of Selection Options (type TOption) to return to the FacePage
   * @return the list of option
   * @throws Exception when the process fails. 
   */
  protected abstract Collection<TOption> onGetSelectOptions() throws Exception;
  
  /**
   * ABSTRACT: Called to add SelectItems represented in options to <tt>selectOptions</tt>.
   * The should initiate the SelectItem for each selectOption by calling {@link 
   * #newSelectOption(java.io.Serializable) this.newSelectItem} to initiate the option.
   * <p><b>NOTE:</b> This method is called after adding the Null Items (if applicable).
   * It will only be called if selectOptions is not empty and after the items has been
   * sorted. If returning selectItems empty {@linkplain #getEmptyItem() this.emptyItem}
   * will be added as the only SelectItem</p>
   * @param  selectOptions List of Select Options of type TOption
   * @param  selectItems A list selectable items of type TItem
   * @throws Exception if an error occur.
   */
  protected abstract void onBuildSelectOptionList(List<TOption> selectOptions, 
                                            List<TItem> selectItems) throws Exception;
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #buildSelectOptions(java.util.List) 
   * this.buildSelectOptions} to add the first option to the list (i.e., if the selector
   * supports a "null" select option). The base method does nothing.
   * @param emptyOptions the empty Option list to update.
   */
  protected void onAddNullSelectOption(List<TOption> selectOptions) {}
  
  /**
   * CAN OVERRIDE: Called by {@linkplain #getSelectOptions() this.getSelectOptions} if
   * {@link #onBuildSelectOptionList(java.util.List, java.util.List) this.onBuildSelectItemList} returns an empty list to allow implementor to add 
   * an item indicating that the list is empty. The base method does nothing.
   * @param emptyOptions the empty Option list to update.
   */
  protected void onAddEmptySelectOption(List<TOption> emptyOptions) {}
  
  /**
   * <p>ABSTRACT: Called to initiated a SelectItem from to specified selection options.
   * </p>
   * @param selectItem of type TItem
   * @return the SelectItem or null to skip this item.
   */
  protected abstract TOption newSelectOption(TItem selectItem);

  /**
   * <p>ABSTRACT: Called to create a list of selection Items from which to build the
   * SelectOption list for sending to the facePage.</p>
   * @return a list of items (can be null)
   */
  protected abstract List<TItem> onGetSelectionItems();

  /**
   * <p>CAN OVERRIDE:: Called to sort a non-empty list of selection Items. The base
   * method does nothing </p>
   * @param selectItems the list of options to sort (will not be null)
   */
  protected void onSortSelectionItems(List<TItem> selectItems) {}

  /**
   * <p>CAN OVERRIDE: Can be after the selector has been initiated to set the default
   * selection (as applicable). The base method does nothing.</p>
   */
  public void initDefaultSelection() {}

  /**
   * ABSTRACT: <p>Call by {@linkplain #changeSelection(java.lang.String) 
   * this.changeSelection()} to set the new SelectId. Inheritors must validate the
   * <tt>selectId</tt> and return either the validated <tt>selectId</tt> or an 
   * alternate valid selectId.  The returned value will be come the new selectId</p>
   * <p>This method is also called by {@linkplain #getSelectId() this.getSelectId} if
   * this.validateSelectId = true, validate that the current selectId is a valid value.
   * If not it will be replaced with the returned value and a SelectionChanged event will
   * be fired.
   * <p>
   * <b>NOTE:</b> Inheritors should not fire the SelectionChanged event or use the
   * {@linkplain #beginChange() beginChange} - {@linkplain #endChange() endChange} block
   * </p>
   * @param selectId the submitted new selectId (not yet set).
   * @return the updated selectId.
   */
  @SuppressWarnings(value = "unchecked")
  protected abstract String onChangeSelection(String selectId);

  /**
   * <p>CAN OVERRIDE: Called by {@linkplain #setChildSelector(
   * bubblewrap.core.selectors.Selector) this.setChildSelector} after the child
   * has been assigned a this selector's child selector, but before this selector is
   * added as a listener to the child selector's SelectionChanged event.</p>
   * <b>The Base method does nothing</b>
   * @param childSelector the new child selector.
   */
  protected void onSetChildSelector(Selector<?> childSelector) {
  }
  //</editor-fold>
}
