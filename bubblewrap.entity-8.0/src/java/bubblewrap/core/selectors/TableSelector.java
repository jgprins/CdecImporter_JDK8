package bubblewrap.core.selectors;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import bubblewrap.io.IntegerRange;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;

/**
 * A Selector for Table displaying a list of {@linkplain SelectObject SelectObjects}.
 * The TableSelector supports as {@linkplain TablePageManager} for displaying the 
 * listed items in pages with a set number of items per page.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 * @version 1.00.000
 */
public abstract class TableSelector<TItem extends Serializable
              ,TOption extends SelectObject<TItem>> extends SelectorBase<TItem,TOption> { 

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The cashed map of options.
   */
  private LinkedHashMap<String,TOption> selectOptions;
  /**
   * Placeholder for the Selector's PageManager - to display the SelectOption by page
   * (i.e. within a index range).
   */
  private TablePageManager pageManager;
  /**
   * A Cached list item for the  currently Selected Page (only used if 
   * this.doPages = true)
   */
  private List<TOption> pageSelectOptions;
//  /**
//   * A Display Flag that will hide/ignore the Selected Item, displaying the Table 
//   * SelectOptions are unselected/readOnly records.
//   */
//  private Boolean hideSelection;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TableSelector() {
    super();  
    this.selectOptions = null;
    this.pageManager = null;
    this.pageSelectOptions = null;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call super method before releasing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.pageSelectOptions = null;
    this.selectOptions = null;
    if (this.pageManager != null) {
      this.pageManager.PageChanged.remove(this);
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Protected TablePageManager Methods">
  /**
   * Called from the inheritor's constructor to initiate the TablePageManager - if the
   * selector supports the pageManager
   * @param numItemsPerPage the number of items to display per page
   */
  protected final void initPageManager(Integer numItemsPerPage) {
    if (this.pageManager == null) {
      this.pageManager = new TablePageManager(numItemsPerPage);
      this.pageManager.PageChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs args) {
          TableSelector listener = this.getListener();
          if ((listener != null) && (listener.pageManager == sender)) {
            try {
              listener.pageSelectOptions = null;
              listener.fireSelectionChanged();
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      });
    } 
  }
  
  /**
   * Called to change the number of records to display per page. If this.pageManager is
   * not initiated, it calls {@linkplain #initPageManager(java.lang.Integer) 
   * this.initPageManager(numItemsPerPage)}, ELSE call {@linkplain 
   * TablePageManager#setItemsPerPage(java.lang.Integer)  
   * this.pageManager.setPageIndex(numItemsPerPage)}.
   * @param numItemsPerPage 
   */
  public final void setItemsPerPage(Integer numItemsPerPage) {
    if (this.pageManager == null) {
      this.initPageManager(numItemsPerPage);
    } else {
      this.pageManager.setPageIndex(numItemsPerPage);
    }
  }
  
  /**
   * Check is this selector support Pages (i.e., its TablePageManager is initiated)
   * @return (this.pageManager != null)
   */
  public final boolean doPages() {
    return (this.pageManager != null);
  }
  
  /**
   * Get a reference to the TabeLSelector's PageManager it it was initiated 
   * (this.doPages = true)
   * @return the reference to the cached pageManager
   */
  public TablePageManager getPageManager() {
    return this.pageManager;
  }
  
  /**
   * Called when this.selectionOptions has changed to sync this.pageManager's 
   * IndexRange with the <tt>optionList.size</tt>. It the IndexRange.max != 
   * (optionList.size()-1), it reset the IndexRange and the set the Range to 
   * [0..(optionList.size()-1)].
   * @param optionList the list of all available selection options
   */
  protected final void onUpdatePageManager(Collection<TOption> optionList) {
    if (this.pageManager != null) {
      try {
        IntegerRange idxRng = this.pageManager.getIndexRange();
        if ((idxRng.isEmpty()) || ((optionList.isEmpty()) && (idxRng.getMax() != 0)) ||
                ((!optionList.isEmpty()) && (idxRng.getMax() != (optionList.size()-1)))) {
          try {
            this.pageManager.beginUpdate();
            idxRng.reset();
            if (optionList.isEmpty()) {
              idxRng.grow(0);
            } else {
              idxRng.grow(0, optionList.size() - 1);
            }
          } finally {
            this.pageManager.endUpdate();
          }
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.onUpdatePageManger Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  
  /**
   * Called by {@linkplain #onGetSelectOptions() this.onGetSelectOptions} to return the
   * selected Page's Subset of Options.
   * @return the cached list if this.doPages, all this.selectOptions.values() if not, or
   * an empty list if this.selectOptions = null.
   */
  protected final Collection<TOption> onGetPageSelectionOptions() {
    Collection<TOption> result = null;
    if (this.selectOptions != null) {
      if (this.pageManager == null) {
        result = this.selectOptions.values();
      } else if (this.pageManager != null) {
        if (this.pageSelectOptions == null) {
          this.doValidateSelectId();
          try {
            this.beginChange();
            this.pageSelectOptions = new ArrayList<>();
            ArrayList<TOption> allOptions = new ArrayList<>(this.selectOptions.values());
            if (!allOptions.isEmpty()) {
              Integer loIdx = this.pageManager.getStartIndex();
              Integer hiIdx = this.pageManager.getEndIndex();
              if (DataEntry.isEq(loIdx, hiIdx)) {
                this.pageSelectOptions.add(allOptions.get(loIdx));
              } else {
                List<TOption> subList = allOptions.subList(loIdx, hiIdx);
                this.pageSelectOptions.addAll(subList);
              }
            }
          } catch (Exception exp) {
            logger.log(Level.WARNING, "{0}.onGetPageSelectionOptions Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          } finally {
            this.endChange();
          }
        }
        result = this.pageSelectOptions;
      }
    }
    return (result == null)? new ArrayList<TOption>(): result;
  }
  
  /**
   * Locate the list index of the selectItem in this.selectOptions and then set 
   * this.selectId = selectId. if (this.doPages), call this.pageManeger.gotoItem(itemIdx)
   * to change the selected page. Finally, call this.setSelectId to make the selected 
   * item switch.
   * @param selectId the new selected Item's ID
   * @return true if the selectId was set.
   */
  protected final boolean gotoPageItem(String selectId) {
    boolean result = false;
    if (((selectId = DataEntry.cleanString(selectId)) != null) &&
        (this.selectOptions != null) && (this.selectOptions.containsKey(selectId))) {
      if (this.doPages()) {
        int index = -1;    
        for (String key : this.selectOptions.keySet()) {
          index++;
          if (DataEntry.isEq(key, selectId, true)) {
            this.pageManager.gotoItem(index);
            result = true;
            break;
          }
        }
      } else {
        result = true;
      }
      
      if (result) {
        this.setSelectId(selectId);
      }
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Protected SelectOption Methods">
  /**
   * Called when the TableSelector's list of Select Items has changed. It resets
   * this.selectOptions to force a update in this.selectOptions. it then calls
   * {@linkplain #getSelectOptions() this.getSelectOptions} to re-initiate
   * this.selectOption and validate that this.selectId is still valid.
   */
  protected void onSelectItemsChanged() {
    if (this.pageSelectOptions != null) {
      this.pageSelectOptions.clear();
    }
    this.pageSelectOptions = null;
    this.resetSelectOptions();
  }

  /**
   * Called to get the SelectOption for a specified SelectId. If this.selectOptios = null
   * it will first initiate the selection list.
   * @param selectId the TOption.selectId to search for
   * @return the selected option or null if not found.
   */
  protected TOption getSelectOption(String selectId) {
    TOption result = null;
    Collection<TOption> optionList = null;
    if (((selectId = DataEntry.cleanLoString(selectId)) != null) && 
         ((optionList = this.getSelectOptions()) != null) && (!optionList.isEmpty())) {
      for (TOption option : optionList) {
        if (DataEntry.isEq(selectId, option.getSelectId(), true)) {
          result = option;
          break;
        }
      }
    }
    return result;
  }
  
//  /**
//   * Get the display flag to hide the selected SelectOption (i.e., displaying all 
//   * records as unselected read-only records). Default = false.
//   * @return true to hide the selection
//   */
//  public boolean doHideSelection() {
//    return ((this.hideSelection != null) && (this.hideSelection));
//  }
//  
//  /**
//   * Set the display flag to hide the selected SelectOption (i.e., displaying all 
//   * records as unselected read-only records). Default = false.
//   * @param hideSelection 
//   */
//  public void setHideSelection(boolean hideSelection) {
//    this.hideSelection = (!hideSelection)? null: true;
//  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * Get the TableSelector's related Item's Class
   * @return class of TItem
   */
  @SuppressWarnings("unchecked")
  public final Class<TItem> getItemClass() {
    return (Class<TItem>)
            ReflectionInfo.getGenericClass(TableSelector.class, this.getClass(), 0);
  }
  
  /**
   * Return the Item Class' SimpleName.
   * @return this.itemClass.simpleName
   */
  public final String getItemClassName() {
    Class result = this.getItemClass();
    return result.getSimpleName();
  }
  /**
   * Get the TableSelector's related Option's Class
   * @return class of TOption
   */
  @SuppressWarnings("unchecked")
  public final Class<TOption> getOptionClass() {
    return (Class<TOption>)
            ReflectionInfo.getGenericClass(TableSelector.class, this.getClass(), 1);
  }
  
  /**
   * Return the Option Class' SimpleName.
   * @return this.optionsClass.simpleName
   */
  public final String getItemOptionName() {
    Class result = this.getItemClass();
    return result.getSimpleName();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="expanded" desc="SelectorBase Overrides">
  /**
   * Get the Selected option using this.selectId and if found, return the option's
   * SelectItem.
   * @return the selected Option's SelectItem or null if not selected
   */
  @Override
  public TItem getSelectedItem() {
    TItem result = null; 
    String selectId = this.getSelectId();
    TOption option = null;
    if (selectId != null) {
      if ((option = this.getSelectOption(selectId)) != null) {
        result = option.getValue();
      } else {
        this.setSelectId(null);
        if (((selectId = this.getSelectId()) != null) &&
                ((((option = this.getSelectOption(selectId)) != null)))) {
          result = option.getValue();
        }
      }
    }
    return result;  
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Returns the for SelectId of the value in this.selectOptions</p>
   */
  @Override
  protected String onGetNullId() {
    String result = null;
    Collection<TOption> optionList = this.getSelectOptions();
    if ((optionList != null) && (!optionList.isEmpty())) {
      for (TOption option : optionList) {
        if ((result = DataEntry.cleanString(option.getSelectId())) != null) {
          break;
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Check if {@linkplain #getSelectOption(java.lang.String) 
   * this.getSelectOption(selectId)} != null. if true, return selectId, else 
   * {@linkplain #onGetNullId() this.onGetNullId}</p>
   */
  @Override
  protected String onChangeSelection(String selectId) {
    String result = null;
    try {
      TOption option = this.getSelectOption(selectId);
      if (option != null) {
        result = DataEntry.cleanString(option.getSelectId());
      } 
      
      if (result == null) {
        result = this.onGetNullId();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.onChangeSelection Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: if (this.selectOptions = null), build the new Selection Option Map.
   * Return this.selectOptions.values.</p>
   */
  @Override
  protected Collection<TOption> onGetSelectOptions() throws Exception {
    Collection<TOption> result = null;
    boolean newList = false;
    if (this.selectOptions == null) {
      try {
        this.beginChange();      
        newList = true;      
        this.selectOptions = new LinkedHashMap<>();
        try {
          List<TOption> optionList = new ArrayList<>();
          List<TItem> selectItems = this.onGetSelectionItems();
          this.onBuildSelectOptionList(optionList, selectItems);

          if (!optionList.isEmpty()) {
            for (TOption option : optionList) {
              String selectId = DataEntry.cleanLoString(option.getSelectId());
              if (selectId == null) {
                continue;
              } else if (this.selectOptions.containsKey(selectId)) {
                throw new Exception("Duplicate SelectId for " + option.toString());
              }

              if (selectId != null) {
                this.selectOptions.put(selectId, option);
              }
            }
          }
        } catch (Exception exp) {
          this.selectOptions.clear();
          logger.log(Level.WARNING, "{0}.onGetSelectOptions Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
        this.onUpdatePageManager(this.selectOptions.values());
      } finally {
        this.endChange();
      }
    }
    
    /**
     * Return either all the selectOptions or only the selected Page's selectOptions - if
     * this.doPages.
     */
    if (!this.doPages()) {
      result = this.selectOptions.values();
    } else {
      if (!newList) {
        this.onUpdatePageManager(this.selectOptions.values());
      }
      if (this.pageManager.getPageCount() == 0) {
        result = this.selectOptions.values();
      } else {
        result = this.onGetPageSelectionOptions();
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: For item in <tt>selectItem</tt> ignore the item if null or 
   * {@link #onIsValidSelectItem(java.io.Serializable) this.onIsValidTableOption(item)} = 
   * false. Otherwise initiate a new selectOption be calling {@linkplain 
   * #newSelectOption(java.io.Serializable) this.newSelectOption(item)} and add it to
   * the <tt>selectOptions</tt> if not Null.</p>
   */
  @Override
  protected void onBuildSelectOptionList(List<TOption> selectOptions, 
                                        List<TItem> selectItems) throws Exception {
    if ((selectItems == null) || (selectItems.isEmpty())) {
      return;
    }
    
    for (TItem item : selectItems) {
      if ((item == null) || (!this.onIsValidSelectItem(item))) {
        continue;
      }
      
      TOption selectOption = this.newSelectOption(item);
      if (selectOption != null) {
        selectOptions.add(selectOption);
      }
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Called by the implementation of {@link #onBuildSelectOptionList(
   * java.util.List, java.util.List) this.onBuildSelectItemList} to check if this
   * specified selectItem is a valid item to display in the Table.
   * @param selectItem the selectItem to evaluate (will not be null)
   * @return true if valid.
   */
  protected abstract boolean onIsValidSelectItem(TItem selectItem);
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Reset the cached select options
   */
  public final void resetSelectOptions(){
    if (this.selectOptions != null) {
      this.selectOptions.clear();
    }
    this.selectOptions = null;
  }
  
  /**
   * Called to goto a valid selectOption (not necessary a option on the current page).
   * If (this.doPages), it will move to the option's page and make it the selected item.
   * Otherwise, it will call {@linkplain #setSelectId(java.lang.String) this.setSelectId}
   * to move to the selected option.
   * @param option 
   */
  public final void gotoSelectOption(TOption option) {    
    Collection<TOption> optionCol = null;
    if ((option == null) || ((optionCol = this.getSelectOptions()) == null) ||
            (optionCol.isEmpty())) {
      return;
    }
    
    List<TOption> optionlist = new ArrayList<>(this.selectOptions.values());
    if (this.doPages()) {
      int optionIdx = optionlist.indexOf(option);
      if (optionIdx >= 0) {
        this.pageManager.gotoItem(optionIdx);
      }
    }
    this.setSelectId(option.getSelectId());
  }
  // </editor-fold>
}
