package bubblewrap.core.selectors;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
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
public abstract class Selector<TItem extends Serializable> extends 
                                                      SelectorBase<TItem, SelectItem>  {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = 
                                    Logger.getLogger(Selector.class.getSimpleName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the selected Item's Id
   */
  private String selectId;
  /**
   * Placeholder for the NullId - the Item Id for the "not selected" item.
   */
  private String nullId;
  /**
   * Placeholder for the Null Item - the Item Caption for the Null item (if set).
   */
  private String nullItem;
  /**
   * Placeholder for the Empty Item - the null item when the list is empty.
   * (Default="-- No Selection Options --")
   */
  private String emptyItemLabel;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public Selector() {
    super();
    this.emptyItemLabel = null;
    this.nullId = null;
    this.nullItem = null;
    this.selectId = null;
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
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private/protected Methods">  
  /**
   * Get the default empty list item (i.e., if there are no selection options).
   * @return a selectItem for the empty list using the assigned {@linkplain
   * #setEmptyItemLabel(java.lang.String) this.emptyItemLabel} or 
   * "-- No Selection Options --" if unassigned.
   */
  protected final SelectItem getEmptyItem() {
    String label = (this.emptyItemLabel == null)? "-- No Selection Options --":
                            this.emptyItemLabel;
    return new SelectItem("null", label);
  }
  
  /**
   * Get whether sSelectID is null or equal to the defined NullId
   * @param selectId the value to test
   * @return true if null or equal to the NullId.
   */
  protected final boolean isNullSelectId(String selectId) {
    selectId = DataEntry.cleanString(selectId);
    return (selectId == null) || 
            ((this.nullId != null) && (this.nullId.equals(selectId)));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the display label of the Selected Option (type {@linkplain SelectItem})
   * @return this.newSelectOption(this.selectedItem).label of null if this.selectedItem =
   * null.
   */
  public final String getSelectOptionLabel() {
    SelectItem option = null;
    TItem item = this.getSelectedItem();
    if (item != null) {
      option = this.newSelectOption(item);
    }
    return (option == null)? null: option.getLabel();
  }
  
  /**
   * Overrides the default text to display is there are no items to select from
   * @param label the empty item's label (Default= "-- No Selection Options --")
   */
  public void setEmptyItemLabel(String label) {
    this.emptyItemLabel = DataEntry.cleanString(label);
  }
  

  /**
   * Overload1. Set the null item's text label. Call 
   * {@link #setNullItem(java.lang.String, java.lang.Boolean)
   * this.setNullItem(itemLabel, false)}.
   * @param itemLabel the new null item selection
   */
  public final void setNullItem(String itemLabel) {
    this.setNullItem(itemLabel, false);
  }

  /**
   * <p>Set the Null ID and Not Selected Item's Label (e.g., null, "Select..."). It will
   * be added to the SelectItem list as the first item. It set this.nullId="null", and
   * if the itemLabel=""|null, it will use the default Label "Select...".</p>
   * <p>The isOptional flag controls whether a not-null selection is allowed (true) or
   * not (false) - default=false.</p>
   * @param itemLabel the new null item selection
   * @param optional If selecting an not-null value is optional
   */
  public final void setNullItem(String itemLabel, Boolean optional) {
    this.nullId = "null";
    itemLabel = DataEntry.cleanString(itemLabel);
    this.nullItem = (itemLabel == null) ? "Select..." : itemLabel;
    this.setOptional(optional);
  }
  
  /**
   * Get the null/No Selected SelectItem's label if assigned.
   * @return this.nullItem can be null if not assigned.
   */
  protected final String getNullItem() {
    return this.nullItem;
  }

  /**
   * {@inheritDoc}
   * <p>OVERRIDE: Return true if ((this.nullId != null) && 
   * (this.nullId.equals(this.selectId)))</p>
   */
  @Override
  protected boolean onIsNullItem(String selectId) {
    return ((this.nullId != null) && (this.nullId.equals(selectId)));
  }

  /**
   * {@inheritDoc}
   * <p>OVERRIDE: returns this.nullId (default = null)</p>
   */
  @Override
  protected String onGetNullId() {
    return this.nullId;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Overridable and Abstract Methods"> 
  
  /**
   * The method is called by {@linkplain #methodthis.}
   * retrieve the list of record to display as follows:<ul>
   * <li>It calls {@link #onGetSelectionItems() this.onGetSelectionOptions} 
   * to get a list of selection Options from which to build the selectItem list.</li>
   * <li>If the Selection Options list is not empty, it calls {@link #onSortSelectionItems(java.util.List) this.onSortSelectionOptions} to sort the
   * list.</li>
   * <li>It calls {@link #buildSelectOptions this.buildSelectItemList} to 
   * generate the list of SelectItems to return.</li>
   * </ul>
   * <p>All Errors are trapped and logged.</p>
   * @return the list of SelectItems of null if an error occurred.
   */
  @SuppressWarnings(value = "unchecked")
  @Override
  public final List<SelectItem> onGetSelectOptions() throws Exception {
    List<SelectItem> result = null;
    List<TItem> entList = this.onGetSelectionItems();
    /* Sort the record if not empty and mpComparator != null */
    if ((entList != null) && (entList.size() > 1)) {
      this.onSortSelectionItems(entList);
    }
    /* Call buildSelectItemList to build the selection list*/
    result = this.buildSelectOptions(entList);
    if (result == null) {
      throw new Exception("The BuildSelectionOptions failed and returned null.");
    }
    return result;
  }
  
  /**
   * CAN OVERRIDE: Called to add Items represented in options to <tt>selectOptions</tt>.
   * The base method add a SelectItem for each option by calling {@link #newSelectOption(java.io.Serializable) this.newSelectItem} to initiate the option.
   * <p><b>NOTE:</b> This method is called after adding the Null Items (if applicable).
   * It will only be called if selectOptions is not empty and after the items has been
   * sorted. If returning selectItems empty {@linkplain #getEmptyItem() this.emptyItem}
   * will be added as the only SelectItem</p>
   * <p><b>NOTE:</b> The SelectItem.id must be of  type String.</p>
   * @param selectItems List of SelectItems to update
   * @param selectOptions A list selection options
   * @throws Exception if an error occur.
   */
  @Override
  protected void onBuildSelectOptionList(List<SelectItem> selectItems,
          List<TItem> selectOptions) throws Exception {
    if ((selectOptions != null) && (selectOptions != null)) {
      for (TItem option : selectOptions) {
        SelectItem item = this.newSelectOption(option);
        if (item != null) {
          selectItems.add(item);
        }
      }
    }
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Assign the Null Item if defined.</p>
   */
  @Override
  protected void onAddNullSelectOption(List<SelectItem> selectOptions) {
    if ((selectOptions != null) && (this.nullItem != null)) {
      selectOptions.add(new SelectItem(this.nullId, this.nullItem));
    }
  }
  //</editor-fold>
}
