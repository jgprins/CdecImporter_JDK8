package bubblewrap.entity.search;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.core.selectors.MultiSelector;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;
import bubblewrap.entity.context.EntityContext;
import bubblewrap.entity.context.FieldInfo;
import bubblewrap.entity.context.PuEntityManager;
import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.core.EntityWrapperComparator;
import bubblewrap.http.session.SessionHelper;

/**
 * The EntityMultiSelector is a base class for supporting a multi-select List/DropDown 
 * FacePage controls or the bwcomp/multiSelectDropDown control to manage selection 
 * options, the display of selected items information. This selector allow the selection 
 * of zero to all records, width a default selection of either all or none.
 * @author kprins
 */
public abstract class EntityMultiSelector<TBean extends Serializable, 
                    TWrapper extends EntityWrapper<TBean>> 
                    extends MultiSelector<TWrapper> {
  
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
  private EntityWrapperComparator<TWrapper> entityComparator;
  /**
   * An actual search can be specified.  If this is used, it will take priority over
   * the normal class based search.  This allows for searches that need to be run
   * at the same time, but are fundamentally different from the main search.
   */
  private EntityListSearch entitySearch; 
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntityMultiSelector() {
    super();
    this.entityFacade = null;
    this.entityContext = null;
    this.entitySearch = null;  
    this.entityComparator = null;
  }

  /**
   * OVERRIDE: Remove self as eventListern of this.mpSearch. Clear local resources
   * before calling super method
   * @throws Throwable 
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.entitySearch != null) {
      this.entitySearch.SearchChanged.remove(this);
      this.entitySearch = null;
    }
    if (this.entityFacade != null) {
      this.entityFacade.PkFilterChanged.remove(this);
      this.entityFacade = null;
    }
    this.entityComparator = null;
    this.entityContext = null;
  }

  /**
   * Assign the searchClass (the EntityListSearch class to be used in retrieving the
   * Selection Option records) and the comparator (the EntityWrapperComparator for 
   * sorting the retrieved records). Both parameters are optional or can be assigned as null.
   * If (searchClass=null), this inheriting class must implement the onGetRecordset to
   * retrieve the records for building the Selection Options.
   * @param searchClass Class<? extends EntityListSearch>
   * @param comparator EntityWrapperComparator of type TAjax
   * @param caption the Selection List Caption if used with a Control
   * @param defaultSelectAll true if all records must be show selected by default.
   * @throws Exception 
   */
  protected final void onInitSelector(Class<? extends EntityListSearch> searchClass,
          EntityWrapperComparator<TWrapper> entComparator, String caption, 
          Boolean defaultSelectAll) throws Exception {
    this.initBaseSelector(caption, defaultSelectAll);
    this.entityComparator = entComparator;
    this.entitySearch = null;
    if (searchClass != null) {
      this.entitySearch = SessionHelper.getManagedBean(searchClass);
      if (this.entitySearch == null) {
        throw new NullPointerException("Unable to access the Session Instance of "
                + "SearchClass[" + searchClass.getSimpleName() + "].");
      }
      this.entitySearch.refreshSearch(true);
      this.entitySearch.SearchChanged.add(new EventDelegate(this) {
        
        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          EntityMultiSelector<TBean, TWrapper> listener = null;
          if ((sender != null) && ((listener = 
              (EntityMultiSelector<TBean, TWrapper>) this.getListener()) != null) &&
              (!listener.isUpdating())) {
            listener.onSyncSelection();
          }
        }
      });
    }
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
          ReflectionInfo.getGenericClass(EntityMultiSelector.class, this.getClass(), 0);
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
         ReflectionInfo.getGenericClass(EntityMultiSelector.class, this.getClass(), 1);
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
      if (this.entityFacade == null) {
        EntityContext<TBean> entCtx = this.getEntityContext();        
        Class<? extends PuEntityManager> puMngrClass = entCtx.getPuManagerClass();
        if (puMngrClass == null) {
          throw new Exception("Entity Class[" + this.getEntityClassName() 
                  + "]'s Persistent Unit Manager is not accessible accessible");
        }

        Class<TBean> entClass = this.getEntityClass();
        PuEntityManager puMngr = PuEntityManager.doLookup(puMngrClass);      
        this.entityFacade = (puMngr == null)? null: puMngr.getFacade(entClass);
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
  public final EntityListSearch getEntityListSearch() {
    return this.entitySearch;
  }
  
  /**
   * Get the Number of record's in the Selection Set.
   * @return the recordset size or 0 if the recordset = null.
   */
  public int getRecordCount() {
    List<TWrapper> recSet = this.getRecordset();
    return (recSet == null)? 0: recSet.size();
  }
  
  /**
   * Get the recordset to build the selection set form. If a EntityListSearch has been 
   * assigned use it to retrieve the record to add as selection options. Otherwise, 
   * call the onGetRecordSet for a custom retrieval of these records
   * @return list if records.
   */
  @SuppressWarnings("unchecked")
  protected final List<TWrapper> getRecordset() {
    List<TWrapper> pResult = null;
    try {
      if (this.entitySearch != null) {
        pResult = this.entitySearch.getEntityList();
      } else {
        pResult = this.onGetRecordset();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRecordset Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * CAN OVERRIDE: If no RecordsetSearh has been assigned, this method must be
   * overridden to retrieve the records to display as the Selection Options.
   * @return List<V>
   */
  @Override
  protected final String toSelectId(TWrapper selectOption) {
    String result = null;
    Serializable recordId = null;
    if ((selectOption != null) && (!selectOption.isNew()) &&
            ((recordId =  selectOption.getRecordId()) != null)) {
      result = recordId.toString();
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return the entity associated with the selectId or null if not found</p>
   */
  @Override
  protected TWrapper toSelectOption(String selectId) {
    TWrapper result = null;
    if (selectId != null) {
      List<TWrapper> entList = this.onGetSelectionOptions();
      if ((entList != null) && (!entList.isEmpty())) {
        for (TWrapper record : entList) {       
          String recordId = this.toSelectId(record);
          if ((recordId != null) && (recordId.equals(selectId))) {
            result = record;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a SelectItem with value = selectionOption.recordID and
   * label = selectionOption.recordName</p>
   */
  @Override
  protected SelectItem newSelectItem(TWrapper selectOption) {
    SelectItem result = null;
    if (selectOption != null) {
      result = new SelectItem(selectOption.getRecordId(), selectOption.getRecordName());
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
  protected final List<TWrapper> onGetSelectionOptions() {
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
   * <p>OVERRIDE: If this.entComaprator is assigned use the comparator to
   * sort the list.</p>
   */
  @Override
  protected void onSortSelectionOptions(List<TWrapper> selectOptions) {
    if ((this.entityComparator != null) && (selectOptions != null)) {
      Collections.sort(selectOptions, this.entityComparator);
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
  //</editor-fold>
}
