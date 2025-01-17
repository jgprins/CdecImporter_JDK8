package bubblewrap.core.selectors;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;

/**
 * A Selector with the selection options defined in LinkedHashMap with the selected 
 * Items as the Map's key, and the SelectItem's label as the Map's Value.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class MapSelector<TValue extends Serializable, 
                          TItem extends SelectObject<TValue>> extends Selector<TItem> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The LinkedHashMap containing the selected Options with the selectable TItem as the
   * Map's value, and the selectable TItem's selectId (as a string) as the Map's key .
   */
  private LinkedHashMap<String, TItem> selectionMap;
  /**
   * An internal flag set while this.selectionMap is been updated (versus reload)
   */
  private transient boolean updating = false;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public MapSelector() {
    super();  
    this.selectionMap = null;
    this.updating = false;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * A protected method that can be called by an inheritor to reset the SelectionMap. 
   * If (<tt>resetSelectId</tt>), it first calls 
   * {@linkplain #setSelectId(java.lang.String) this.setSelectId(null)} to reset the 
   * current selection before clearing this.selectionMap. ELSE, it capture the exiting
   * selectId, reset this.selectionMap, and the re-assigned the selectId.
   * <p>
   * <b>NOTE:</b> It calls {@link #onReloadSelectItems()} AFTER resetting 
   * this.selectionMap to allow custom handling of the request (e.g., force a reload of
   * a based search). All errors thrown by the call will be trapped and logged.</p>
   * @param resetSelectId true to reset the selectId to.
   */
  protected void resetSelectionMap(boolean resetSelectId) {
    String selectId = null;
    if (resetSelectId) {
      this.setSelectId(null);
    } else {
      selectId = this.getSelectId();
    }
    
    if (this.selectionMap != null) {
      this.selectionMap.clear();
      this.selectionMap = null;
    }
    
    try {
      this.onReloadSelectItems();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.resetSelectionMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    
    if (selectId != null) {
      this.setSelectId(selectId);
    }
  }
  
  /**
   * A protected method that can be called by a inheritor to update the selectable
   * items without changing this.selectionMap's keys or size. 
   * It only replace the items - which could result in an updated in the selectable 
   * display rendering.
   * <p>It calls {@linkplain #onReloadSelectItems() this.onReloadSelectItems} to trigger
   * an update of the base data if needed. It then call {@linkplain 
   * #onLoadSelectItems(java.util.List) this.onLoadSelectItems} to get a updated list of 
   * items and then update this.selectionMap's items with matching map keys. No new items
   * are added or old items removed.
   * <p>
   * <b>NOTE:</b> This will not trigger a SelectionChanged event - because the selectId 
   * does not change.
   */
  protected void updateSelectionMap() {
    if ((this.selectionMap == null) || (this.selectionMap.isEmpty())) {
      return;
    }
    try {
      this.updating = true;
      try {
        this.onReloadSelectItems();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.updateSelectionMap Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
      
      List<TItem> selectItems = new ArrayList<>();
      this.onLoadSelectItems(selectItems);
      if (!selectItems.isEmpty()) {
        String selectId = null;
        for (TItem item : selectItems) {
          if ((item != null) && 
              ((selectId = DataEntry.cleanString(item.getSelectId())) != null) &&
              (this.selectionMap.containsKey(selectId))) {
            this.selectionMap.put(selectId, item);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.updateSelectionMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.updating = false;
    }
  }
  
  /**
   * Get the Selector's current isUpdating status - only set while {@linkplain
   * #updateSelectionMap() this.updateSelectionMap} is executing.
   * @return 
   */
  protected boolean isUpdating() {
    return this.updating;
  }
  
  /**
   * Called to reset the current selection. It calls {@linkplain 
   * #setSelectId(java.lang.String) this.setSelectId(null)}.
   */
  public final void resetSelection() {    
    this.setSelectId(null);
  }
  
  /**
   * Get the LinkHashMap containing the Selector's Selection Items. If not yet initiated,
   * it calls {@linkplain #onLoadSelectItems(java.util.List) this.onLoadSelectItems} to
   * get a list of selectable items. It the add these items to a LinkedHashMap using
   * the item.selectId as the key and the item as the map value.
   * <p>All null items or items with empty|null selectIds will be ignored. To add a
   * blank/null item initiate a TItem with a null value and initiate the selectors
   * null items label by calling {@linkplain #setNullItem(java.lang.String) 
   * this.setNullItem(nullIdLabel)}. The selectId of the nullItem will be "null".
   * <p>
   * <b>NOTE:</b> Item's with duplicate keys will be overridden without a warning</p>
   * @return the cached Map (lazy initiated)
   */
  public LinkedHashMap<String, TItem> getSelectionMap() {
    if (this.selectionMap == null) {
      this.selectionMap = new LinkedHashMap<>();
      List<TItem> selectItems = new ArrayList<>();
      this.onLoadSelectItems(selectItems);
      if (!selectItems.isEmpty()) {
        String selectId = null;
        for (TItem item : selectItems) {
          if ((item != null) && 
                  ((selectId = DataEntry.cleanString(item.getSelectId())) != null)) {
            this.selectionMap.put(selectId, item);
          }
        }
      }
    }
    return this.selectionMap;
  }
  
  /**
   * Get a reference to the Selected Object's value.
   * @return {@linkplain #getSelectedItem() this.selectedItem}.{@link 
   * SelectObject#getValue() value}.
   */
  public final TValue getSelectedValue() {
    TItem item = this.getSelectedItem();
    return (item == null)? null: item.getValue();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Selector Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if (a TItem is selected, return the TValue of the selected item or null
   * if no item is selected.</p>
   */
  @Override
  public final TItem getSelectedItem() {
    TItem result = null;
    String selectId = this.getSelectId();
    if ((!this.isNull()) && 
            ((selectId = DataEntry.cleanString(this.getSelectId())) != null) &&
            (this.selectionMap != null) && (!this.selectionMap.isEmpty()) &&
            (this.selectionMap.containsKey(selectId))) {
      result = this.selectionMap.get(selectId);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: validate that the new <tt>selecteId</tt> is a valid key for 
   * this.selectionMap</p>
   */
  @Override
  protected String onChangeSelection(String selectId) {
    String result = DataEntry.cleanString(selectId);
    LinkedHashMap<String, TItem> optionMap = null;
    if ((result != null) && (((optionMap = this.getSelectionMap()) == null) ||
        (optionMap.isEmpty()) || (!optionMap.containsKey(result)))) {
      result = null;
    }    
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return result = new SelectItem(item.getSelectId, label)  with label = 
   * item.getLabel() or if item.isNullValue, label = this.nullItem if defined else
   * label = item.getLabel(). It also result.disabled = item.disabled and result.escape =
   * item.escape.
   * </p>
   */
  @Override
  protected final SelectItem newSelectOption(TItem item) {
    SelectItem result = null;
    if (item != null) {
      String selectId = item.getSelectId();
      String label = item.getLabel();
      if ((item.isNullValue()) && (this.getNullItem() != null)) {
        label = this.getNullItem();
      }
      
      result = new SelectItem(selectId, label);
      if (item.isDisabled()) {
        result.setDisabled(true);
      }
      if (item.isEscape()) {
        result.setEscape(false);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return new ArrayList(this.selectionMap.values) - list can be empty.
   * It calls {@linkplain #getSelectionMap() this.getSelectionMap} to retrieved the
   * cashed map of selection items.</p>
   */
  @Override
  protected List<TItem> onGetSelectionItems() {
    List<TItem> result = null;
    LinkedHashMap<String, TItem> itemMap = this.getSelectionMap();
    if ((itemMap == null) || (itemMap.isEmpty())) {
      result = new ArrayList<>();
    } else {
      result = new ArrayList<>(itemMap.values());
    }    
    return result;
  }
  // </editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Protected Abstract/Overridable Methods">
  /**
   * ABSTRACT: Called by {@linkplain #getSelectionMap() this.getSelectionMap} to lazy 
   * load the Selector's Map of selected items. The TItem's seelectId becomes keys of 
   * this.selectionMap and the TItems is assigned as the Map's value.
   * <p>
   * <b>NOTE:</b> The method will be called the first time this.getSelectionMap is 
   * called at start up or after {@linkplain #resetSelectionMap(boolean) 
   * this.resetSelectionMap} is called.</p>
   * @param selectItems and empty list to assigned the list items to - in the order of 
   * display.
   */
  protected abstract void onLoadSelectItems(List<TItem> selectItems);
  /**
   * CAN OVERRIDE: An overridable method called by {@linkplain #resetSelectionMap(boolean) 
   * this.resetSelectionMap} AFTER resetting this.selectMap or by {@linkplain 
   * #updateSelectionMap() this.updateSelectionMap} before calling {@linkplain 
   * #onLoadSelectItems(java.util.List) this.} to custom handle the request (e.g., force
   * a search to reload).
   * <p>
   * <b>NOTE:</b> All exceptions are trapped and logged, but will not break the process.
   * </p>
   */
  protected void onReloadSelectItems() throws Exception {} 
  // </editor-fold>
}
