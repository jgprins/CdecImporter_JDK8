package bubblewrap.entity.search;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.core.selectors.TableSelector;
import bubblewrap.entity.context.EntityContext;
import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.core.EntityWrapperComparator;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

/**
 * An extension of the TableSelector to display a list of {@linkplain EntityWrapper
 * EntityWrappers} wrapped as a {@linkplain EntitySelectOption}.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 * @version 1.00.000
 */
public abstract class EntityTableSelector<TBean extends Serializable, 
                        TWrapper extends EntityWrapper<TBean>>
                           extends TableSelector<TWrapper, EntitySelectOption<TWrapper>> {
 
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
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
   * An actual search can be specified. If this is used, it will take priority over
   * the normal class based search. This allows for searches that need to be run
   * at the same time, but are fundamentally different from the main search.
   */
  private EntityListSearch<TBean, TWrapper> entitySearch;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityTableSelector() {
    super();  
    this.entityFacade = null;
    this.entitySearch = null;
    this.entComparator = null;
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
    this.entComparator = null;
    if (this.entitySearch != null) {
      this.entitySearch.SearchChanged.remove(this);
      this.entitySearch = null;
    }
    if (this.entityFacade != null) {
      this.entityFacade.PkFilterChanged.remove(this);
      this.entityFacade = null;
    }
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Protected Seach Initiation Methods">
  /**
   * <p>
   * Assign the <tt>searchClass</tt> (the EntityListSearch class to be used in retrieving 
   * the Selection Option records) and the entComparator (the EntityWrapperComparator for
   * sorting the retrieved records). Both parameters are optional or can be assigned
   * as null.</p>
   * <p>
   * An exception will be thrown if (searchClass=null).</p>
   * <p>
   * <b>Note:</b> - it also adds this selector listener the
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param searchClass to search for selectable records
   * @param entComparator the Comparator for sorting the selectable record for display
   * in the selectItem list.
   * @exception NullPointerException if ManagedBean of the searchClass is not
   * accessible.
   */
  protected final void onInitSelector(Class<? extends EntityListSearch<TBean, TWrapper>> 
                         searchClass, EntityWrapperComparator<TWrapper> entComparator) {
    this.entitySearch = null;
    if (searchClass == null) {
      throw new NullPointerException("The EntitySearch Class cannot be unassigned.");
    }
    EntityListSearch<TBean, TWrapper> search = SessionHelper.getManagedBean(searchClass);
    if (search == null) {
      throw new NullPointerException("Unable to access the Session Instance of "
              + "SearchClass[" + searchClass.getSimpleName() + "].");
    }
    this.onInitSelector(search, entComparator);
  }

  /**
   * <p>
   * Assign the <tt>search</tt> (the EntityListSearch to be used in retrieving the
   * Selection Option records) and the entComparator (the EntityWrapperComparator for
   * sorting the retrieved records). Both parameters are optional or can be assigned
   * as null.</p>
   * <p>
   * An exception will be thrown if (search=null).</p>
   * <p>
   * <b>Note:</b> - it also adds this selector listener the
   * {@linkplain EntityListSearch#SearchChanged} EventHandler.</p>
   * @param search to search for selectable records
   * @param entComparator the Comparator for sorting the selectable record for display
   * in the selectItem list.
   * @exception NullPointerException if ManagedBean of the searchClass is not
   * accessible.
   */
  protected final void onInitSelector(EntityListSearch<TBean, TWrapper> search,
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
        EntityTableSelector<TBean, TWrapper> listener = null;
        if ((sender != null) && ((listener = this.getListener()) != null)
                && (!listener.isChanging())) {
          String selectId = listener.getSelectId();
          listener.onSelectItemsChanged();
          listener.setSelectId(selectId);   
          /**
           * Only fire the SelectIonChanged event if the listener.selectId has not changed
           */
          if (DataEntry.isEq(selectId, listener.getSelectId(), true)) {
            listener.fireSelectionChanged();
          }
        }
      }
    });
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Reflection Methods">
  /**
   * Get the EntityTableSelector's related EntityWrapper.bean Class
   * @return class of TBean
   */
  @SuppressWarnings("unchecked")
  public final Class<TBean> getEntityClass() {
    return (Class<TBean>)
            ReflectionInfo.getGenericClass(EntityTableSelector.class, this.getClass(), 0);
  }
  
  /**
   * Return the Entity Class' SimpleName.
   * @return this.entityClass.simpleName
   */
  public final String getEntityClassName() {
    Class pClass = this.getEntityClass();
    return pClass.getSimpleName();
  }
  
  /**
   * <p>Get the EntityTableSelector's associated EntityFacade (via the EntityClass
   * assignment). Log errors if the EntityClass, Facade is not accessible.</p>
   * <p><b>Note:</b> - it also adds this selector listener the 
   * {@link EntityFacade#PkFilterChanged} EventHandler.</p>
   * @return the EntityTableSelector's EntityFacade
   */
  public final EntityFacade<TBean> getFacade() {
    try {
      Class<TWrapper> wrapperClass = null;
      if ((this.entityFacade == null) && 
              ((wrapperClass = this.getItemClass()) != null)) {             
        this.entityFacade = EntityWrapper.getFacadeByWrapper(wrapperClass);
        if (this.entityFacade == null) {
          throw new Exception("Entity[" + this.getEntityClassName()
                  + "]'s Facade is not accessible.");
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFacadeHelper Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.entityFacade;
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
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Selected EntityWrapper's recordId.
   * @return the current selected record's ID or null if isAddNew or isNull
   */
  @SuppressWarnings("unchecked")
  public final <TRecId extends Serializable> TRecId getRecordId() {
    TRecId result = null;
    EntitySelectOption<TWrapper> option = this.getSelectOption(this.getSelectId());
    TWrapper item = null;
    if ((option != null) && ((item = option.getValue()) != null)){
      result = item.getRecordId();
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
    Collection<EntitySelectOption<TWrapper>> selectOptions = null;
    String selectId = null;
    if ((recordId != null) && ((selectOptions = this.getSelectOptions()) != null) && 
            (!selectOptions.isEmpty())) {
      TWrapper item = null;
      for (EntitySelectOption<TWrapper> option : selectOptions) {
        if ((option != null) && ((item = option.getValue()) != null) &&
                (DataEntry.isEq(recordId, item.getRecordId()))) {
          selectId = option.getSelectId();
        }
      }
    }    
    this.setSelectId(selectId);
  }
  //</editor-fold>
      
  // <editor-fold defaultstate="collapsed" desc="Protected Overriable Methods">
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
   * CAN OVERRIDE: Override this method when using a ChildSelector to update the
   * ChildSelector's filters/options with any change in the Parent's Selection.
   * the base method does nothing.
   * @param record the newly selected record
   * @throws Exception
   */
  protected void onSelectionChanged(TWrapper record) throws Exception {
  }
  // </editor-fold>  
  
  // <editor-fold defaultstate="collapsed" desc="Override TableSelector">  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return new EntitySelectOption<>(selectItem)</p>
   */
  @Override
  protected EntitySelectOption<TWrapper> newSelectOption(TWrapper selectItem) {
    EntitySelectOption<TWrapper> result = null;
    try {
      result = new EntitySelectOption<>(selectItem);
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
      TWrapper item = null;
      EntitySelectOption<TWrapper> option = this.getSelectOption(selectId);
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
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return false if ((selectItem = null) | (selectItem.isNew)) - an 
   * EntityTableSelector does not support null or new records.</p>
   */
  @Override
  protected boolean onIsValidSelectItem(TWrapper selectItem) {
    return ((selectItem != null) && (!selectItem.isNew()));
  }
  // </editor-fold>
}
