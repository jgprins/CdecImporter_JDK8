package bubblewrap.core.selectors;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public abstract class MultiSelector<TItem extends Serializable> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = 
                                    Logger.getLogger(MultiSelector.class.getSimpleName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Selector's Caption.
   */
  private String selectorCaption;
  /**
   * Flag used by the FcasePage to manage the DropDown list state
   */
  private Boolean showDropDown;
  /**
   * Option that can be set to prevent update event from firing while the DropDwn is
   * visible (default = null|false)
   */
  private Boolean noChangedEventOnShow;
  /**
   * Placeholder for the cached list of all the available recordIds to select from
   */
  private ArrayList<String> allSelectIds;
  /**
   * Placeholder for the recordId's of the selected Items
   */
  private ArrayList<String> selectedIds;
  /**
   * Placeholder for the cached list selected records
   */
  private List<TItem> selectedItems;
  /**
   * A flag controlling the default selectAll state (default = True|null)
   */
  private Boolean defaultSelectAll;  
  /**
   * Transient updateCount to manage the isUpdating state
   */
  private transient int updateCount = 0;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Event Senders">
  /**
   * EventHdnler for sending a Selection Changed event.
   */
  public final EventHandler SelectionChanged;

  /**
   * Method called to fie the Selection Changed event.
   */
  protected void fireSelectionChanged() {
    if (this.selectedItems != null) {
      this.selectedItems.clear();
      this.selectedItems = null;
    }
    if ((!this.getNoChangedEventOnShow()) || (!this.getShowDropDown())) {
      this.SelectionChanged.fireEvent(this, new EventArgs());
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public MultiSelector() {
    super();
    this.SelectionChanged = new EventHandler();  
    this.selectedIds = null;
    this.selectedItems = null;
    this.selectorCaption = null;
    this.allSelectIds = null;
    this.defaultSelectAll = null;    
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.selectedIds != null) {
      this.selectedIds.clear();
      this.selectedIds = null;
    }
    if (this.allSelectIds != null) {
      this.allSelectIds.clear();
      this.allSelectIds = null;
    }
    if (this.selectedItems != null) {
      this.selectedItems.clear();
      this.selectedItems = null;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private/protected Methods">  
  /**
   * Called to set the base Selector's Caption and defaultSelectAll option - to 
   * select all the options by default.
   * @param caption the Selection List Caption if used with a Control
   * @param selectAll true to select all options be default
   */
  protected final void initBaseSelector(String caption, boolean selectAll) {    
    this.selectorCaption = DataEntry.cleanString(caption);
    this.defaultSelectAll = (selectAll)? selectAll: null;
  }
  
  /**
   * Called by getSelectOptions to build the Selection List - It initiates the list of
   * SelectItem to return and calls {@linkplain #onBuildSelectItemList(java.util.List, 
   * java.util.List) this.onBuildSelectItemList} to add the selected Options as 
   * SelectItems. It return an empty list if no items were added.
   * @param entList List if entities to select from
   * @return the selectItem list
   * @throws Exception 
   */
  protected final List<SelectItem> buildSelectItemList(List<TItem> selectOptions) 
          throws Exception {
    List<SelectItem> result = new ArrayList<>();
    /* If pRecordSet is not emty call onBuildSelectItemList to add the additional items */
    if ((selectOptions != null) && (!selectOptions.isEmpty())) {
      this.onBuildSelectItemList(result, selectOptions);
    }
    return result;
  }
  
  /**
   * Get whether the NoChangedEventOnShow flag is set. If set, all SelectionChanged events 
   * will postponed while this.showDropDown = true. In this case the SelectionChanged 
   * event will be fired when this.showDropDown is set to false - regardless of whether
   * the selection has changed.
   * @return the assigned value
   */
  protected final Boolean getNoChangedEventOnShow() {
    return ((this.noChangedEventOnShow != null) && (this.noChangedEventOnShow));
  }
  
  /**
   * <p>Set NoChangedEventOnShow flag. Typically set in the inheritor's constructor.</p>
   * <p>See {@linkplain #getNoChangedEventOnShow() getNoChangedEventOnShow} method for
   * details.</p> 
   * @param noChangedEventOnShow the new setting
   */
  protected final void setNoChangedEventOnShow(Boolean noChangedEventOnShow) {
    this.noChangedEventOnShow = noChangedEventOnShow;
  }
  
  /**
   * Check if the Selector should be default select all the records (default = true|null)
   * @return the assigned value
   */
  protected final boolean getDefaultSelectAll() {
    return ((this.defaultSelectAll == null) || (this.defaultSelectAll));
  }
  
  /**
   * Called to assign the new selections as the selector's selection. It first check if
   * the selection has changed, if if true initiate the new selectedItems list.
   * It fires the SelectionChanged event is the selection has changed.
   * @param newSelection the list of selected recordIds
   * @return true if the selection has changed.
   */
  @SuppressWarnings("unchecked")
  protected final void onAssignSelection(List<String> newSelection) {    
    ArrayList<String> priorSelection = 
               (this.selectedIds == null)? null: (ArrayList) this.selectedIds.clone();
    boolean hasChanged = (((priorSelection == null) && (newSelection != null)) ||
                      ((priorSelection != null) && (newSelection == null)) ||
                      ((priorSelection != null) && (newSelection != null) &&
                        (priorSelection.size() != newSelection.size())));
    if ((!hasChanged) && (priorSelection != null) && (newSelection != null)) {
      for (String recId : newSelection) {
        if (!priorSelection.contains(recId)) {
          hasChanged = true;
          break;
        }
      }
    }
    
    if (hasChanged) {
      try {
        this.beginUpdates();
        List<String> allIds = this.getAllRecordIds();
        if (this.selectedIds != null) {
          this.selectedIds.clear();
        }
        if ((newSelection != null) && (!newSelection.isEmpty()) && 
                (allIds != null) && (!allIds.isEmpty())) {
          if (this.selectedIds == null) {
            this.selectedIds = new ArrayList<>();
          }
          for (String recId : allIds) {
            if (newSelection.contains(recId)) {
              this.selectedIds.add(recId);
            }
          }
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.onAssignSelection Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      } finally {
        this.endUpdates();
      }
    }
  }
  
  /**
   * Called to get a list of all recordIds in the current recordSet
   * @return the cached list of Strings or an empty list if recordSet = null|empty
   */
  @SuppressWarnings("unchecked")
  protected final List<String> getAllRecordIds() {
    if (this.allSelectIds == null) {
      this.allSelectIds = new ArrayList<>();
      try {
        List<TItem> itemSet = this.onGetSelectionOptions();
        if ((itemSet != null) && (!itemSet.isEmpty())) {
          for (TItem item : itemSet) {
            String recId = this.toSelectId(item);
            if (recId != null) {
              this.allSelectIds.add(recId);
            }
          }
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.getAllRecordIds Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.allSelectIds;
  }
  
  /**
   * Call to sync the current selection with the selector's available selection options.
   * If fire the SelectionChanged if the current selection is not empty.
   */
  protected final void onSyncSelection() {
    if ((this.selectedItems == null) || (this.selectedItems.isEmpty())) {
      return;
    }
    try {
      this.beginUpdates();
      this.selectedItems = null;
      this.allSelectIds = null;
      List<String> allIds = this.getAllRecordIds();
      if ((allIds == null) || (allIds.isEmpty())) {
        this.clearAll();
      } else {
        List<String> newSelection = new ArrayList<>();
        for (String selectId : this.selectedIds) {
          if (allIds.contains(selectId)) {
            newSelection.add(selectId);
          }
        }

        this.onAssignSelection(newSelection);
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.syncSelection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.endUpdates();
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage the isUpdating state">
  /**
   * Check the Selector's isUpdating state. No events are fried while true
   * @return true if the Selector's updateCount &gt; 0
   */
  public final boolean isUpdating() {
    return (this.updateCount > 0);
  }
  
  /**
   * <p>Called to begin updating the Selector without firing events. This call MUST be
   * followed be a call to this.endUpdates(). These calls can be nested.</p>
   * <p>It increments the Selector's updateCount.</p>
   */
  public final void beginUpdates() {
    this.updateCount = (this.updateCount < 0)? 0: this.updateCount;
    this.updateCount++;
  }
  
  /**
   * <p>Called to end updating the Selector without firing events. This call should be
   * preceeded by a call to this.beginUpdates().</p>
   * <p>It decrements the Selector's updateCount and if the updated count = 0, it
   * fires the SelectionChanged event.</p>
   */
  public final void endUpdates() {
    this.updateCount = (this.updateCount < 0)? 1: this.updateCount;
    this.updateCount--;
    
    if (this.updateCount <= 0) {
      this.fireSelectionChanged();
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Selector's Control Caption
   * @return the assigned value
   */
  public final String getSelectorCaption() {
    return this.selectorCaption;
  }

  /**
   * Set the Selector's Control Caption
   * @param selectorCaption the new value
   */
  public final void setSelectorCaption(String selectorCaption) {
    this.selectorCaption = DataEntry.cleanString(selectorCaption);
  }

  /**
   * Get the ShowDropDown state (default=false)
   * @return the assigned value 
   */
  public final Boolean getShowDropDown() {
    return ((this.showDropDown != null) && (this.showDropDown));
  }

  /**
   * Set the ShowDropDown state. If the state change and this.noChangedEventOnShow = true,
   * it fires the SelectionChanged Event to force a update of listened.
   * @param showDropDown new state
   */
  public final void setShowDropDown(Boolean showDropDown) {
    Boolean newState = ((showDropDown == null) || (!showDropDown))? null: showDropDown;
    if (!DataEntry.isEq(newState, this.showDropDown)) {
      this.showDropDown = newState;
      if (this.getNoChangedEventOnShow()) {
        this.fireSelectionChanged();
      }
    }
  }
  
  /**
   * Get the display state of the DropDown Window
   * @return if this.showDropDown, 'block' else 'none'
   */
  public final String getDropDownDisplay() {
    return (this.getShowDropDown())? "block": "none";
  }
  
  /**
   * Get whether all records are selected
   * @return true if allRecordIds = null|empty || 
   * (allRecordIds.size = selectedItems.size)
   */
  public final Boolean getAllSelected() {
    List<String> allIds = this.getAllRecordIds();
    return ((allIds == null) || (allIds.isEmpty()) || 
            ((this.selectedItems != null)) && 
             (allIds.size() == this.selectedItems.size()));
  }
  
  /**
   * Set whether all record should be selected. If not this.allSelected call {@linkplain 
   * #selectAll() this.selectAll} to select all the records.
   */
  public final void setAllSelected(Boolean selectAll) {
    selectAll = ((selectAll != null) && (selectAll));
    if (selectAll) {
      if (!this.getAllSelected()) {
        this.selectAll();
      }
    } else {
      this.clearAll();
    }
  } 
    
  /**
   * Get the current selected SelectItem ID (only for used by FacePage)
   * @return a clone of the internal list of selected IDs
   */
  public final List<String> getSelection() {
    List<String> result = new ArrayList<>();
    if (this.selectedIds == null) {
      this.resetSelection();
    } else {
     result.addAll(this.selectedIds);
    }
    return result;
  }

  /**
   * Set the new selected SelectItem ID (only for used by FacePage)
   * @param selection the new list of selected items
   */
  @SuppressWarnings("unchecked")
  public final void setSelection(List<String> selection) {
    try {
      this.onAssignSelection(selection);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setSelection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * The method is called by the FacePage to get a list of SelectItems to display. It 
   * retrieve the list of record to display as follows:<ul>
   * <li>If a EntityListSearchClass is assigned, it will retrieve the Session's 
   * ManagementBean for this class and get the selected records</li>
   * <li>If a EntityListSearchClass is not assigned, it calls onGetRecordSet to
   * get a custom generated list of record. The onGetRecordSet must be overridden for
   * this case. The base method throws an UnsupportedOperationException.</li>
   * </ul>
   * <p>If a Comparator is assigned, the records will be sorted before the SelectItem
   * list is build. This method calls the getSelectItemList to add the Null and/or 
   * AddNew items before it calls onAddSelectItemList to add the additional records.
   * The base onAddSelectItemList method added a item for each record. This method can
   * be overridden to build a custom list (e.g., to group the items).</p>
   * @return a List of SelectItems or null is the recordSet is empty.
   */
  @SuppressWarnings("unchecked")
  public final List<SelectItem> getSelectOptions() {
    List<SelectItem> result = null;
    try {
      List<TItem> optionList = this.onGetSelectionOptions();
      /* Sort the record if not empty and mpComparator != null */
      if ((optionList != null) && (optionList.size() > 1)) {
        this.onSortSelectionOptions(optionList);
      }
      /* Call buildSelectItemList to build the selection list*/
      result = this.buildSelectItemList(optionList);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSelectOptions Error:\n {1}", 
                  new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get whether the list of Selection Options is empty
   * @return true if the recordset is null or empty
   */
  public final boolean isEmpty() {
    List<TItem> optionList = this.onGetSelectionOptions();
    return (optionList == null)? true: optionList.isEmpty();
  }
  /**
   * Get the Number of Selection Options.
   * @return the recordset size or 0 if the recordset = null.
   */
  public final int getSelectOptionCount() {
    List<TItem> optionList = this.onGetSelectionOptions();
    return (optionList == null)? 0: optionList.size();
  }

  /**
   * Called to add all record into the selectItems list. It fires the SelectionChanged
   * event if the selection list changed.
   */
  @SuppressWarnings("unchecked")
  public final void selectAll() {
    List<TItem> recordset = this.onGetSelectionOptions();
    if ((recordset == null) || (recordset.isEmpty())) {
      this.onAssignSelection(null);
    } else {
      List<String> newSelection = this.getAllRecordIds();
      this.onAssignSelection(newSelection);
    }
  }
  
  /**
   * Called to clear the current selection.It fires the SelectionChanged
   * event if the selection list changed.
   */
  public final void clearAll() {
    this.onAssignSelection(null);
  }
  
  /**
   * Called to reset the current selection to the default selection. if (defaultSelectAll)
   * call this.selectAll else call this.clearAll.
   */
  public final void resetSelection() {
    if (this.getDefaultSelectAll()) {
      this.selectAll();
    } else {
      this.clearAll();
    }
  }

  /**
   * Get the current list of selected Options
   * @return The list of selected Items represented 
   */
  public final List<TItem> getSelectItems() {
    if ((this.selectedItems == null) && (this.selectedIds != null)) {
      this.selectedItems = new ArrayList<>();
      List<TItem> selectOptions = null;
      if ((!this.selectedIds.isEmpty()) && 
              ((selectOptions = this.onGetSelectionOptions()) != null) && 
               (!selectOptions.isEmpty())) {
        for (TItem options : selectOptions) {
          String optionId = this.toSelectId(options);
          if ((optionId != null) && (this.selectedIds.contains(optionId))) {
            this.selectedItems.add(options);
          }
        }
      }
    }
    return this.selectedItems;
  }
  
  /**
   * Get whether the current Selection Set is empty - no items are selected
   * @return true if the selectionIds list is null|empty
   */
  public final boolean isEmptySelection() {
    return ((this.selectedIds == null) || (this.selectedIds.isEmpty()));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Overridable and Abstract Methods">
  /**
   * Get the Selected Option for a 
   * @param selectId the selected Item's selectId
   * @return The selectedItem represented by the selectedId
   */
  protected abstract TItem toSelectOption(String selectId);
  
  /**
   * Get the specified Selected Option's selectId
   * @param selectOption the specified selected Option
   * @return the Selected Option's selectId or null if not selectable
   */
  protected abstract String toSelectId(TItem selectOption);
  
  /**
   * CAN OVERRIDE: Called to add Items represented in options to <tt>selectOptions</tt>.
   * The base method add a SelectItem for each option by calling {@linkplain
   * #newSelectItem(java.io.Serializable) this.newSelectItem} to initiate the option.
   * <p><b>NOTE:</b> This method is called after adding the Null Items (if applicable).
   * It will only be called if selectOptions is not empty and after the items has been
   * sorted. If returning selectItems empty {@linkplain #getEmptyItem() this.emptyItem}
   * will be added as the only SelectItem</p>
   * <p><b>NOTE:</b> The SelectItem.id must be of  type String.</p>
   * @param selectItems List of SelectItems to update
   * @param selectOptions A list selection options
   * @throws Exception if an error occur.
   */
  protected final void onBuildSelectItemList(List<SelectItem> selectItems,
          List<TItem> selectOptions) throws Exception {
    if ((selectOptions != null) && (selectOptions != null)) {
      for (TItem option : selectOptions) {
        SelectItem item = this.newSelectItem(option);
        if (item != null) {
          selectItems.add(item);
        }
      }
    }
  }
  
  /**
   * <p>ABSTRACT: Called to initiated a SelectItem from to specified selection options.
   * </p>
   * @param selectOption of type TItem
   * @return the SelectItem or null to skip this item.
   */
  protected abstract SelectItem newSelectItem(TItem selectOption);
  
  /**
   * <p>ABSTRACT: Called to create a list of selection Options from which to build the
   * SelectItem list for sending to the facePage.</p>
   * @return
   */
  protected abstract List<TItem> onGetSelectionOptions();
  
  /**
   * <p>CAN OVERRIDE:: Called to sort a non-empty list of selection Options. The base
   * method does nothing </p>
   * @param selectOptions the list of options to sort
   */
  protected void onSortSelectionOptions(List<TItem> selectOptions){};
  //</editor-fold>  
}
