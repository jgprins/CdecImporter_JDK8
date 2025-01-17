package bubblewrap.entity.search;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.core.selectors.Selector;
import bubblewrap.entity.context.EntityContext;
import bubblewrap.entity.context.FieldInfo;
import bubblewrap.entity.context.PuEntityManager;
import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.core.EntityWrapperComparator;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import bubblewrap.io.converters.DataConverter;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;

/**
 * The EntitySelector is a base class for supporting a List/DropDown FacePage control to
 * manage selection options, the display of selected item information, and the selection
 * option to add a new item.
 * @author kprins
 */
public abstract class EntitySelector<TBean extends Serializable, 
                    TWrapper extends EntityWrapper<TBean>> extends Selector<TWrapper> {
  
  //<editor-fold defaultstate="collapsed" desc="PrimaryKeyChangedDelegate Class">
  /**
   * The Event Delegate for listening the Facade PrimaryKey Changed events.
   * <p>(sender=this.entityFacade) and (!this.isChanging):</b> - get
   *    this.entityFacade.primaryKey. If not assign and is this.selectId != null|AddNew,
   *    set this.selectId=null, ELSE:</p> <ul>
   *    <li>if (this.record=null or (this.record.recordId!=PrimaryKey.recordId): set
   *      this.selectId = PrimaryKey.recordId
   *    </li>
   *    <li>Else If (this.record != primaryKey) (i.e. not the same instance): set
   *      this.record=PrimaryKey and call {@linkplain #fireSelectionChanged()}.</li>
   *   </ul>
   * <p>Set this.isChanging=true|false before and after changing the recrodId or
   * current record to match the facade's PrimaryKey.</p>
   */
  private class PrimaryKeyChangedDelegate extends EventDelegate {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public PrimaryKeyChangedDelegate(EntitySelector<TBean,TWrapper> listener) {
      super(listener);
    }
    //</editor-fold>
    
    @SuppressWarnings("unchecked")
    @Override
    public void onEvent(Object sender, EventArgs eventInfo) {
      EntitySelector<TBean,TWrapper> listener = null;
      if ((sender != null) && ((listener = 
              (EntitySelector<TBean,TWrapper>) this.getListener()) != null) &&
              (!listener.isChanging())) {
        TWrapper primKey = (TWrapper) listener.entityFacade.getPkFilter();
        if (primKey == null) {
// Removed because it reset a prior selection when a child was added to the selected item.
// and the primayKey was reset. Not sure why these lines are needed.          
//          if ((!listener.isNull()) && (!listener.isAddNew())) {
//            listener.setSelectId(null);
//          }
        } else if ((listener.curRecord == null) ||
                (!listener.curRecord.equals(primKey))) {
          listener.setSelectId(primKey.getRecordId().toString());
        } else if (listener.curRecord != primKey) {
          listener.curRecord = primKey;
          listener.fireSelectionChanged();
        }
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the generically reference EntityFacade
   */
  private EntityContext<TBean> entityContext;
  /**
   * Placeholder for the generically reference EntityFacade
   */
  private EntityFacade<TBean> entityFacade;
  /**
   * The placeholder of a Comparator that will be used to sort the retrieved records
   * before the Selection Option list is build.
   */
  private EntityWrapperComparator<TWrapper> entComparator;
  /**
   * An actual search can be specified.  If this is used, it will take priority over
   * the normal class based search.  This allows for searches that need to be run
   * at the same time, but are fundamentally different from the main search.
   */
  private EntityListSearch entitySearch;
  /**
   * Placeholder for the AddNewId - the Item Id for the "Add New" item.
   */
  private String addNewId;
  /**
   * Placeholder for the AddNew Item - the Item Caption for the AddNew item (if set).
   */
  private String addNewItem;
  /**
   * A transient reference to the current selected record.
   */
  private transient TWrapper curRecord;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntitySelector() {
    super();
    this.entityFacade = null;
    this.entityContext = null;
    this.entitySearch = null;
    this.addNewId = null;
    this.addNewItem = null;
    this.entComparator = null;
    this.curRecord = null;
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.curRecord = null;
    this.entComparator = null;
    if (this.entitySearch != null) {
      this.entitySearch.SearchChanged.remove(this);
      this.entitySearch = null;
    }
    if (this.entityFacade != null) {
      this.entityFacade.PkFilterChanged.remove(this);
      this.entityFacade = null;
    }
    this.entityContext = null;
  }
  //</editor-fold>
   
  /**
   * Set the Select Item Label for the Add New Item (e.g., "-- Add New Item --"). 
   * If set, it will be added to the SelectItem list as the second item following the 
   * "null" item - if supported or, otherwise, the first item.  If itemLabel is 
   * unassigned the default label "-- Add New Item --" will be used .
   * @param itemLabel the new "Add New" item selection
   */
  public final void doAddNewItem(String itemLabel) {
    this.addNewId = "AddNew";
    if (this.addNewId != null) {
      itemLabel = DataEntry.cleanString(itemLabel);
      this.addNewItem = (itemLabel == null) ? "Add New Item..." : itemLabel;
    }
  }


  /**
   *<p>Assign the <tt>searchClass</tt> (the EntityListSearch class to be used in retrieving the
   * Selection Option records) and the entComparator (the EntityWrapperComparator for
   * sorting the retrieved records). Both parameters are optional or can be assigned
   * as null.</p>
   * <p>An exception will be thrown if (searchClass=null).</p>
   * <p><b>Note:</b> - it also adds this selector listener the 
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param searchClass to search for selectable records
   * @param entComparator the Comparator for sorting the selectable record for display
   * in the selectItem list.
   * @exception NullPointerException if ManagedBean of the searchClass is not 
   * accessible.
   */
  protected final void onInitSelector(Class<? extends EntityListSearch<TBean,TWrapper>> 
        searchClass, EntityWrapperComparator<TWrapper> entComparator) {
    this.entitySearch = null;
    if (searchClass == null) {
      throw new NullPointerException("The EntitySearch Class cannot be unassigned.");
    }
    EntityListSearch<TBean,TWrapper> search = SessionHelper.getManagedBean(searchClass);
    if (search == null) {
      throw new NullPointerException("Unable to access the Session Instance of "
              + "SearchClass[" + searchClass.getSimpleName() + "].");
    }
    this.onInitSelector(search, entComparator);
  }
  
  /**
   *<p>Assign the <tt>search</tt> (the EntityListSearch to be used in retrieving the
   * Selection Option records) and the entComparator (the EntityWrapperComparator for
   * sorting the retrieved records). Both parameters are optional or can be assigned
   * as null.</p>
   * <p>An exception will be thrown if (search=null).</p>
   * <p><b>Note:</b> - it also adds this selector listener the 
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param search to search for selectable records
   * @param entComparator the Comparator for sorting the selectable record for display
   * in the selectItem list.
   * @exception NullPointerException if ManagedBean of the searchClass is not 
   * accessible.
   */
  protected final void onInitSelector(EntityListSearch<TBean,TWrapper> search,
                                        EntityWrapperComparator<TWrapper> entComparator) {
    if (search == null) {
      throw new NullPointerException("The EntitySearch reference cannot be unassigned.");
    }
    this.entComparator = entComparator;
    this.entitySearch = search;
    this.entitySearch.refreshSearch(true);
    this.entitySearch.SearchChanged.add(new EventDelegate(this) {
      @SuppressWarnings("unchecked")
      @Override
      public void onEvent(Object sender, EventArgs eventInfo) {
        EntitySelector<TBean, TWrapper> listener = null;
        if ((sender != null) && ((listener = this.getListener()) != null) &&
            (!listener.isChanging())) {
          Serializable recordId = listener.getRecordId();
          if (recordId != null) {
            listener.entitySearch.refreshSearch();
            listener.setRecordId(recordId);        
          }      
          listener.fireSelectionChanged();
        }
      }
    });
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * Get the EntitySelector's related EntityWrapper.EntityClass Class
   * @return class of TBean
   */
  @SuppressWarnings("unchecked")
  public final Class<TBean> getEntityClass() {
    return (Class<TBean>)
            ReflectionInfo.getGenericClass(EntitySelector.class, this.getClass(), 0);
  }
  
  /**
   * Return the Entity Class' SimpleName.
   * @return String
   */
  public final String getEntityClassName() {
    Class pClass = this.getEntityClass();
    return pClass.getSimpleName();
  }
  
  /**
   * Get the EntitySelector's related  EntityWrapper Class{@literal <TWrapper>}
   * @return class of TWrapper
   */
  @SuppressWarnings("unchecked")
  public final Class<TWrapper> getEntityWrapperClass() {
    return (Class<TWrapper>)
            ReflectionInfo.getGenericClass(EntitySelector.class, this.getClass(), 1);
  }
  
  /**
   * Return the EntityWrapper Class' SimpleName.
   * @return String
   */
  public final String getEntityWrapperClassName() {
    Class pClass = this.getEntityWrapperClass();
    return pClass.getSimpleName();
  }
  
  /**
   * Get the EntitySelector' EntityContext
   * @return the EntityContext registered with BwAppContext
   */
  public final EntityContext<TBean> getEntityContext() {
    try {
      if (this.entityContext == null) {
        Class<TBean> entClass = this.getEntityClass();
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new NullPointerException("Unable to access the Application's"
                  + " BwAppContext.");
        }
        this.entityContext = appCtx.getEntityContext(entClass);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityContext Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.entityContext;
  }
  
  /**
   * <p>Get the EntitySelector's associated EntityFacade (via the EntityClass
   * assignment). Log errors if the EntityClass, Facade is not accessible.</p>
   * <p><b>Note:</b> - it also adds this selector listener the 
   * {@link EntityFacade#PkFilterChanged} EventHandler.</p>
   * @return the EntitySelector's EntityFacade
   */
  public final EntityFacade<TBean> getFacade() {
    try {
      Class<TWrapper> wrapperClass = null;
      if ((this.entityFacade == null) && 
              ((wrapperClass = this.getEntityWrapperClass()) != null)) {             
        this.entityFacade = EntityWrapper.getFacadeByWrapper(wrapperClass);
        if (this.entityFacade == null) {
          throw new Exception("Entity[" + this.getEntityClassName()
                  + "]'s Facade is not accessible.");
        }
        this.entityFacade.PkFilterChanged.add(new PrimaryKeyChangedDelegate(this));
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFacadeHelper Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.entityFacade;
  }

  /**
   * Get the EntityView's Generic itemId class<T>
   * @return Class<? extends Serializable>
   */
  @SuppressWarnings("unchecked")
  protected final Class<? extends Serializable> getRecIdClass() {
    EntityContext<TBean> entCtx = this.getEntityContext();
    FieldInfo recIdInfo = (entCtx == null)? null: entCtx.getPrimaryKey();
    Class<? extends Serializable> result = (recIdInfo == null)? String.class: 
            (Class<? extends Serializable>) recIdInfo.returnType;
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Face Page Methods">
  /**
   * Check if the selector uses a recordset search to return selection options.
   * @return true if the internal search reference has been initiated.
   */
  public final boolean hasEntityListSearch() {
    return (this.entitySearch != null);
  }
  
  /**
   * Get a reference to the Selector's recordset search used in generating the selection 
   * options.
   * @return the internal search reference or null if not initiated. 
   */
  public final EntityListSearch<TBean,TWrapper> getEntityListSearch() {
    return this.entitySearch;
  }
  
  /**
   * <p>Call by {@linkplain #setSelectId(java.lang.String) setSelectId()} when the
   * Selection has changed to set the new SelectId. This process does the following
   * initiate and return result which is the new selectId. The process involve the
   * following:</p>
   * <ul>
   *  <li>initiate result=selectId</li>
   *  <li>if this.entityFacade is not accessible, result=null.</li>
   *  <li>Called {@linkplain #getRecordId()} to retrieve the new selected record's 
   *    recordId. If: <ul>
   *       <li>recordId=null or the TBean cannot be retrieve, set result=null</li>
   *       <li>if (recordId=this.entityFacade.primaryKey.recordId), set 
   *           this.curRecord = this.entityFacade.PrimaryKey</li>
   *       <li>else, initiate a new TWrapper instance as this.curRecord.</li>
   *    </ul>
   *  </li>
   *  <li>If this.curRecord could not be initiated, and if not (isNull and isAddNew)
   *    set result = null.</li>
   *  <li>If and exception is thrown, set result=null</li>
   * </ul>
   * @param selectId the new selectId (not yet set).
   * @return the final (updated) selectId.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected String onChangeSelection(String selectId) {
    String result = selectId;
    try {
      this.curRecord = null;
      EntityFacade<TBean> entFacade = this.getFacade();
      if ((entFacade != null) && (!this.isNull()) && (!this.isAddNew())) {
        Serializable recordId = this.onValidateRecordId(this.getRecordId());
        if (recordId == null) {
          result = null;
        } else if (entFacade.isPkFilterRecordId(recordId)) {
          this.curRecord = (TWrapper) entFacade.getPkFilter();
        } else {
          TBean entBean = entFacade.find(recordId);
          if (entBean != null) {
            this.curRecord = 
                    EntityWrapper.newFromBean(this.getEntityWrapperClass(), entBean);
          }
        }
      }
      
      if (this.curRecord == null) {
        if ((!this.isNull()) && (!this.isAddNew())) {
          result = null;        
        }
      }
      
      this.onSelectionChanged(this.curRecord);
    } catch (Exception exp) {
      result = null;
      this.curRecord = null;
    } 
    return result;
  }
  
  /**
   * Call to validate that the current selection has a valid recordId - i.e. it 
   * represents a selectable record.
   * @param newRecId the recordId to validate
   * @return the current recordId or null if no matching selectable record was found.
   */
  @SuppressWarnings("unchecked")
  protected final <TRecId extends Serializable> TRecId 
                                                  onValidateRecordId(TRecId newRecId) {
    TRecId result = null;
    if (newRecId != null) {
      List<TWrapper> entList = this.onGetSelectionItems();
      if ((entList != null) && (!entList.isEmpty())) {
        for (TWrapper record : entList) {       
          Serializable recordId = ((record == null)? null: record.getRecordId());
          if ((recordId != null) && (recordId.equals(newRecId))) {
            result = (TRecId) recordId;
            break;
          }
        }
      }
    }
    return result;
  }

  /**
   * Get the Identification Information of the Selected Item
   * @return the result of {@linkplain #onGetSelectedInfo(
   * bubblewrap.entity.core.EntityWrapper) onGetSelectedInfo()} for the select record or
   * null if no record is selected.
   */
  public final String getSelectItemInfo() {
    String result = null;
    try {
      TWrapper record = this.getSelectedItem();
      if (record != null) {
        result = this.onGetSelectedInfo(record);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSelectItemInfo Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }

  /**
   * Get a summary of details associated with the selected record.
   * @return the result of {@linkplain #onGetSelectedDetails(
   * bubblewrap.entity.core.EntityWrapper) onGetSelectedDetails()} for the select record
   *  or null if no record is selected.
   */
  public final String getSelectItemDetails() {
    String result = null;
    try {
      TWrapper record = this.getSelectedItem();
      if (record != null) {
        result = this.onGetSelectedDetails(record);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSelectItemDetails Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the currently selected record
   * @return V
   */
  @SuppressWarnings("unchecked")
  @Override
  public final TWrapper getSelectedItem() {
    return this.curRecord;
  }

  /**
   * Get the Selected EntityWrapper's recordId.
   * @return the current selected record's ID or null if isAddNew or isNull
   */
  @SuppressWarnings("unchecked")
  public final <TRecId extends Serializable> TRecId getRecordId() {
    TRecId result = null;
    if ((!this.isAddNew()) && (!this.isNull())) {
      Class recIdClass = this.getRecIdClass();
      String selectId = this.getSelectId();
      if (String.class.equals(recIdClass)) {
        result = (TRecId) selectId;
      } else {
        result = (TRecId) DataConverter.toValue(selectId, recIdClass);
      }
    }
    return result;
  }

  /**
   * Set (initiate) the selected record's recordId. It first call {@linkplain 
   * #onValidateRecordId(java.io.Serializable) onValidateRecordId()} to validate that
   * recordId represents a selectable record, before calling{@linkplain #setSelectId(
   * java.lang.String) setSelectId()) to set the new selection with selectId = 
   * recordId.toSting() or null. This call is ignored if recordId matches the current
   * recordId. Call this.clearSelection if the selection should be cleared.
   * @param recordId the new recordId (can be null)
   */
  @SuppressWarnings("unchecked")
  public final <TRecId extends Serializable> void setRecordId(TRecId recordId) {
    recordId = this.onValidateRecordId(recordId);
    Serializable curRecId = this.getRecordId();
    if (!DataEntry.isEq(curRecId, recordId)) {
      String newSelectId = (recordId == null) ? null : 
                                            DataEntry.cleanString(recordId.toString());
      this.setSelectId(newSelectId);
    }
  }
  
  /**
   * Return true if the AddNew ID has been initiated ant the current SelectItem ID
   * equals the AddNew ID.
   * @return boolean
   */
  public final boolean isAddNew() {
    return ((this.addNewId != null) && 
            (DataEntry.isEq(this.addNewId, this.getSelectId(), true)));
  }

  /**
   * Set the current Selected Value as AddNew (it will have no affect if the AddNew ID
   * has not been initiated).
   */
  public final void setAddNew() {
    if (this.addNewId != null) {
      this.setSelectId(this.addNewId);
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract/Overridable Methods">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a SelectItem with value = selectionOption.recordID and
   * label = selectionOption.recordName</p>
   */
  @Override
  protected SelectItem newSelectOption(TWrapper selectOption) {
    SelectItem result = null;
    if (selectOption != null) {
      result = new SelectItem(selectOption.getRecordId(), selectOption.getRecordName());
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: If this.entComaprator is assigned use the comparator to
   * sort the list.</p>
   */
  @Override
  protected void onSortSelectionItems(List<TWrapper> selectOptions) {
    if ((this.entComparator != null) && (selectOptions != null)) {
      Collections.sort(selectOptions, this.entComparator);
    }
  }
  
  /**
   * Get the recordset to build the selection set form. If a EntityListSearch has been 
   * assigned use it to retrieve the record to add as selection options. Otherwise, 
   * call the onGetRecordSet for a custom retrieval of these records
   * @return list if records.
   */
  @SuppressWarnings("unchecked")
  @Override
  protected final List<TWrapper> onGetSelectionItems() {
    List<TWrapper> result = null;
    try {
      if (this.entitySearch != null) {
        result = this.entitySearch.getEntityList();
      } else {
        result = this.onGetRecordset();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityList Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: If (this.addNewItem0 is defined, add a "Add New Item" selectItem 
   * before calling the base method.</p>
   */
  @Override
  protected void onBuildSelectOptionList(List<SelectItem> selectItems, List<TWrapper>
          selectOptions) throws Exception {
    if ((this.addNewId != null) && (this.addNewItem != null)) {
      selectItems.add(new SelectItem(this.addNewId, this.addNewItem));
    }
    super.onBuildSelectOptionList(selectItems, selectOptions); 
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: if childSelector!=null, get the selected record and call 
   * {@linkplain #onSelectionChanged(bubblewrap.entity.core.EntityWrapper) 
   * this.onSelectionChanged(record)}  </p>
   */
  @Override
  protected final void onSetChildSelector(Selector<?> childSelector) {
    if (childSelector == null) {
      return;
    }
    try {
      TWrapper record = this.getSelectedItem();
      this.onSelectionChanged(record);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.fireSelectionChanged Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * CAN OVERRIDE: If no RecordsetSearh has been assigned, this method must be
   * overridden to retrieve the records to display as the Selection Options.
   * @return List<V>
   */
  protected List<TWrapper> onGetRecordset() {
    throw new UnsupportedOperationException(this.getClass().getSimpleName()
            + " does support onGetRecordset.");
  }

  /**
   * ABSTRACT: Implement to return the Identification Information for the selected
   * record (e.g., "<b>" + pRecord.recordName + "<b><br/>" + pRecord.recordDesc).Return
   * null if undefined.
   * @param record the record for which to retrieve the information
   * @return String
   */
  protected abstract String onGetSelectedInfo(TWrapper record);

  /**
   * ABSTRACT: implement to return a summary of details for the selected record. Return
   * null if undefined.
   * @param record the record for which to retrieve the details
   * @return String
   */
  protected abstract String onGetSelectedDetails(TWrapper record);

  /**
   * CAN OVERRIDE: Override this method when using a ChildSelector to update the 
   * ChildSelector's filters/options with any change in the Parent's Selection.
   * the base method does nothing.
   * @param record the newly selected record
   * @throws Exception 
   */
  protected void onSelectionChanged(TWrapper record) throws Exception {
  }
  //</editor-fold>
}
