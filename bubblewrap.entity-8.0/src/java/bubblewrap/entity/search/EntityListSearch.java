package bubblewrap.entity.search;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.enums.*;
import bubblewrap.core.events.*;
import bubblewrap.core.reflection.EnumInfo;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.context.EntityContext;
import bubblewrap.entity.context.PuEntityManager;
import bubblewrap.entity.core.*;
import bubblewrap.entity.filters.*;
import bubblewrap.entity.interfaces.IEntityFilter;
import bubblewrap.entity.searchfilters.*;
import bubblewrap.io.*;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.persistence.Query;

/**
 * <p>An EntityListSearch are used by an EntityFormInfo or any other class to 
 * dynamically search for a result recordset based on the defined set of search 
 * criteria. It supports three types of Filters:</p><ul>
 *  <li><b>A Static Filter:</b> A StaticFilter is a custom filter that is by default on
 *  and applies to any selection unless its is manual turned off. This filter isSet 
 *  state if ignored when checking the RecrodSearch's isSet state.</li>
 *  <li><b>ForeignKey Filter:</b> If the Entity[TWrapper] has a set of ForeignKey 
 *  constraints (as defined in its EntityFacade), these ForeignKey filters will apply if 
 *  set. The EntityListSearch's result set will automatically be updated if any of these
 *  ForeignKey's are set/unset.</li>
 *  <li><b>SearchFilters:</b> Each EntityListSearch is designed with a set of 
 *  SearchFilters with a unique searchId, which applies to any record field and could 
 *  include SearchBySubSearchFilters. The latter returns a EntityGroupFilter based on 
 *  the results of a specified Sub-EntityListSearch.</li>
 * </ul>
 * <p>When the search criteria change, the result recordset (accessible through the
 * getRecordSet method) will be updated. The new result set will be resolved as follows:
 * </p><ul>
 *  <li>A EntityGroupFilter will be generated that includes the sub-EntityFilters of
 *    all the set Static and SearchFilters. If none is set, the EntityGroupFilter will
 *    be set to null.</li> 
 *  <li>If any set SeachFiler returns an EntityEmptyFilter, the result recordset will 
 *    be empty.</li>
 *  <li>ELSE, it calls the Entity's FacadeHelp.getAllByFilter to return the resulting 
 *    recordset, passing in the pFilter=EnityGroupFilter, bDoFK=this.doFKFilter, 
 *    bDoActive=this.activeOnly and bIsSystem=false.</li>
 * </ul> * 
 * <p><b>Notes:</b><ul>
 * <li>Custom EntityListSearch classes must be defined for each EntityView that requires
 * the support a EntityListSearch</li>
 * <li>The Custom EntityListSearchs classes must be defined as ManagementBeans with
 * SessionScope</li>
 * <li>For the EntityView to support a EntityList Search, is recordsetSearchClass must
 * be set in the constructor.</li>
 * <li>When using SearchBySubSearchFilter SearchFilter's the design of the sub-search
 * filter must set its isSet=false if the sub-search returns all values or common values
 * to prevent the generation of a too complex EntityGroupFilter.</li>
 * </ul>
 * @see SearchFilter
 * @see SearchStaticFilter
 * @see SearchBySubSearchFilter
 * @author kprins 
 */
public class EntityListSearch<TBean extends Serializable, 
                      TWrapper extends EntityWrapper<TBean>> implements Serializable{
  
  //<editor-fold defaultstate="collapsed" desc="EntityNamedQuery Class">
  /**
   * A Class for handling namedQuery searches
   * @param <TBean> the entity bean calss
   */
  private class EntityNamedQuery<TBean extends Serializable> implements Serializable {
    // <editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * The Named Query to Execute
     */
    private String namedQuery;
    /**
     * Placeholder for the owner search reference
     */
    private EntityListSearch search;
  // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public EntityNamedQuery(EntityListSearch search, String namedQuery) {
      super();
      this.search = search;
      this.namedQuery = DataEntry.cleanString(namedQuery);
      if (this.namedQuery == null) {
        throw new NullPointerException("The NamedQuery's name is unassigned.");
      }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Called by the EntityListSearch to execute the custom NamedQuery. It calls the
     * search's {@linkplain #onAssignQueryParams(javax.persistence.Query)
     * onAssignQueryParams} method to update the NamedQuery's parameters and
     * {@linkplain #onAssignQueryResults(java.util.List) onAssignQueryResults} to update
     * the Search entityList.
     * @param facade
     */
    public void execQuery(EntityFacade<TBean> facade) {
      try {
        NamedQueryDelegate<TBean> delegate = new NamedQueryDelegate<TBean>(this) {
          @Override
          public void assignParameters(Query query) throws SQLException {
            try {
              EntityNamedQuery listener = this.getListener();
              if ((listener != null) && (listener.search != null)) {
                try {
                  listener.search.onAssignQueryParams(query);
                } catch (Exception exp) {
                  logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                          new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
                }
              }
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.assignParameters Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
          
          @Override
          public void loadQuery(List<TBean> rs) throws SQLException {
            try {
              EntityNamedQuery listener = this.getListener();
              if ((listener != null) && (listener.search != null)) {
                try {
                  listener.search.onAssignQueryResults(rs);
                } catch (Exception exp) {
                  logger.log(Level.WARNING, "{0}.loadQuery Error:\n {1}",
                          new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
                }
              }
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.assignParameters Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        };
        
        facade.executeQuery(this.namedQuery, delegate);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.execQuery Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    // </editor-fold>
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="EntityCustomQuery">
  /**
   * A Class for handling custom SQL queries
   * @param <TBean> the entity bean class
   */
  private class EntityCustomQuery<TBean> implements Serializable {
    // <editor-fold defaultstate="collapsed" desc="Private Fields">
    /**
     * The Named Query to Execute
     */
    private String querySql;
    /**
     * Placeholder for the owner search reference
     */
    private EntityListSearch search;
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor
     */
    public EntityCustomQuery(EntityListSearch search, String querySql) {
      super();
      this.search = search;
      this.querySql = DataEntry.cleanString(querySql);
      if (this.querySql == null) {
        throw new NullPointerException("The Custom Query's SQL statement is empty.");
      }
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Public Methods">
    /**
     * Called by the EntityListSearch to execute the custom NamedQuery. It calls the
     * search's {@linkplain #onAssignQueryParams(javax.persistence.Query)
     * onAssignQueryParams} method to update the NamedQuery's parameters and
     * {@linkplain #onAssignQueryResults(java.util.List) onAssignQueryResults} to update
     * the Search entityList.
     * @param facade the EntityFacade to use for the query
     */
    public void execQuery(EntityFacade facade) {
      try {
        SqlQueryDelegate delegate = new SqlQueryDelegate(this) {
          @Override
          public void assignParameters(Query query) throws SQLException {
            try {
              EntityCustomQuery listener = this.getListener();
              if ((listener != null) && (listener.search != null)) {
                try {
                  listener.search.onAssignQueryParams(query);
                } catch (Exception exp) {
                  logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                          new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
                }
              }
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.assignParameters Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
          
          @Override
          public void loadQuery(List<Object> rs) throws SQLException {
            try {
              EntityCustomQuery listener = this.getListener();
              if ((listener != null) && (listener.search != null)) {
                try {
                  listener.search.onAssignQueryResults(rs);
                } catch (Exception exp) {
                  logger.log(Level.WARNING, "{0}.loadQuery Error:\n {1}",
                          new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
                }
              }
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.assignParameters Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        };
        
        facade.executeQuery(this.querySql, delegate);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.execQuery Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    // </editor-fold>
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * The Static Logged for the class
   */
  protected static final Logger logger = 
                                    Logger.getLogger(EntityListSearch.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for an assigned NamedQuery
   */
  private EntityNamedQuery<TBean> namedQuery;
  /**
   * Placeholder for an assigned Custom SQL Query
   */
  private EntityCustomQuery<TBean> customQuery;
  /**
   * A flag controlling the SeachValue changing
   */
  private Integer changingCount;  
  /**
   * Placeholder for the EntityListSearch's list of filters
   */
  private HashMap<String,SearchFilterBase<TBean>> filterMap;
  /**
   * Placeholder for the EntityListSearch's Static Filter
   */
  private SearchStaticFilter<TBean> staticFilter;
  /**
   * Placeholder for the recordSet's DefaultSearchOptions (Default=NONE/null)
   */
  private Integer defaultSearchOptions;
  /**
   * Placeholder for the recordSet's current SearchOptions (Default=NONE/null)
   */
  private Integer searchOptions;
  /**
   * Placeholder for the Search's EnitySort - a static sort order applicable in absence
   * of a ColumnSort and when ColumnSort if turned off (default=null)
   */
  private EntitySort entitySort;
  /**
   * Placeholder of pre-registered (custom) EntitySort that are can be used as column
   * sorts for this search.
   */
  private HashMap<String, EntitySort> columnSortMap;
  /**
   * Placeholder for the Search's Column EntitySort - a dynamic sort used by ListViews 
   * to allowed users to click on columns to resort the list's content (default=null).
   * Only applicable if doColumnSort is turned on.
   */
  private EntitySort columnSort;
  /**
   * A flag controlling whether to column sort is applied to the result set.
   * (default=null|false)
   */
  private Boolean doColumnSort;
  /**
   * Placeholder for the EntityListSearch's resulting EntityList.
   */
  private EntityList<TWrapper> entityList;
  /**
   * The Maximum Number of results (return all results if 0 or null).
   */
  private Integer maxResults;
  /**
   * Placeholder for the Search's EntityFacade
   */
  private EntityFacade entityFacade;
  /**
   * Flag Controlling the search's Modified State
   */
  private Boolean isModified;
  //</editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="Event Sender">
  /**
   * The EventHandler for sending the Search Changed event.
   */
  public final EventHandler SearchChanged;
  /**
   * Fire the SaerchFilter's Event[CHANGED].
   */
  protected final void fireSearchChanged() {
    if (!this.isSearchChanging()) {
      this.SearchChanged.fireEvent(this, new EventArgs());
    }
  }
  
  /**
   * CAN OVERRDIE: 
   * Called by {@linkplain #fireSearchChanged() this.fireSearchChanged} before firing 
   * this.SearchChanged event. Only called when this.SeacHChanging = false or turn 
   * false.
   * <p>The Base Method does nothing.
   */
  protected void onSearchChanged() {}
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor 
   */
  public EntityListSearch() {
    this.SearchChanged = new EventHandler();
    this.isModified = true;
    this.maxResults = null;
    this.entityList = null;
    this.filterMap = null;  
    this.staticFilter = null;
    this.entitySort = null;
    this.columnSort = null;
    this.doColumnSort = null;
    this.entityFacade = null;
    this.defaultSearchOptions = null;
    this.searchOptions = null;
    this.columnSortMap = null;
    this.namedQuery = null;
    this.customQuery = null;
  }
  
  /**
   * OVERRIDE: Release the local resources before calling the super method.
   * @throws Throwable 
   */
  @Override
  protected void finalize() throws Throwable {
    this.SearchChanged.clear();
    if ((this.filterMap != null) && (!this.filterMap.isEmpty())) {
      for (SearchFilterBase<TBean> filter : this.filterMap.values()) {
        filter.SearchChanged.remove(this);
      }
    }
    if (this.staticFilter != null) {
      this.staticFilter.SearchChanged.remove(this);
    }
    this.removeFKeyEventListerners();
    
    this.filterMap = null;
    this.entityList = null;
    this.entityFacade = null;
    this.entitySort = null;
    this.columnSort = null;
    this.columnSortMap = null;
    this.namedQuery = null;
    this.customQuery = null;
    super.finalize();
  } 
  
  /**
   * Set the RecordSearch's default FacadeFilter Options. If any of the settings is
   * unassigned, it will be ignored. Set this.modified = true.
   * @param doActiveOnly new setting
   * @param doPrimaryKey new setting
   * @see FacadeFilterEnums
   */
  protected void setDefaultSearchOptions(boolean doActiveOnly, boolean doPrimaryKey) {
    Integer options = FacadeFilterEnums.setFilter(doActiveOnly, false, doPrimaryKey);
    if ((!DataEntry.isEq(this.defaultSearchOptions, options))) {
      this.defaultSearchOptions = options;
      this.setModified();
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Reflection Methods">  
  /**
   * Get the EntityLink's related EntityWrapper.EntityClass Class
   * @return Class<? extends Serializable>
   */
  @SuppressWarnings("unchecked")
  public final Class<TBean> getEntityClass() {
    return (Class<TBean>)
            ReflectionInfo.getGenericClass(EntityListSearch.class, this.getClass(), 0);
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
   * Get the EntityListSearch's related  EntityWrapper Class
   * @return Class<? extends EntityWrapper>
   */
  @SuppressWarnings("unchecked")
  public final Class<TWrapper> getEntityWrapperClass() {
    return (Class<TWrapper>)
            ReflectionInfo.getGenericClass(EntityListSearch.class, this.getClass(), 1);
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
   * Get the EntityClass' EntityContext
   * @return the EntityContext registered with BwAppContext
   */
  public final EntityContext<TBean> getEntityContext() {
    Class<TBean> entClass = this.getEntityClass();
    BwAppContext appCtx = BwAppContext.doLookup();
    if (appCtx == null) {
      throw new NullPointerException("Unable to access the Application's"
              + " BwAppContext.");
    }
    return appCtx.getEntityContext(entClass);
  }
  /**
   * Get the EntityListSearch's associated EntityFacade (via the EntityClass assignment).
   * Log errors if the EntityClass, Facade is not accessible.
   * @return the Search Entity's EntityFacade
   */
  public final EntityFacade getFacade() {
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
        
        this.addFKeyEventListerners();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFacade Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.entityFacade;
  }  
  
  /**
   * Called when the FacadeHelper is initiated to add this search as an EventLister to
   * this.facadeHelper.entityLink's ForeignKeys (as applicable) for an Action[CHANGED]
   * @throws Exception 
   */
  private void addFKeyEventListerners() throws Exception {
    EntityFacade facade = this.getFacade();
    if (facade != null) {
      facade.PkFilterChanged.add(new EventDelegate(this) {
        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          EntityListSearch listener = (EntityListSearch) this.getListener();
          if (listener != null) {
            listener.isModified = true;
            listener.fireSearchChanged();
          }
        }
      });
    }
  }
  
  /**
   * Called by finalize to remove this search as an EventLister from
   * this.facadeHelper.entityLink's ForeignKeys (as applicable) .
   * @throws Exception 
   */
  private void removeFKeyEventListerners() {
    try {
      EntityFacade facade = this.getFacade();
      if (facade != null) {
        facade.PkFilterChanged.remove(this);
      }
    } catch (Exception pExp) {
    }
  }
  
  /**
   * Check is the EntityList's EntityFacade has at least one set Parent ForeignKey
   * @return true if a Parent ForeignKey is set.
   */
  private boolean isForeignKeysSet() {
    boolean result = false;
    EntityFacade facade = this.getFacade();
    result = (facade == null)? false: facade.hasSetParentPkFilters();
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage Static Entity Sorts">
  /**
   * <p>Set the "static" sort Fields on which to sort the results and the field's
   * Sort Order. The "static" sort definition can can consist of multiple field defined
   * in an {@linkplain EntitySort} object. Each call to this method will add the new
   * field to the sort list or update the existing field's sort order.  It is typically
   * used by constructor to set the search's predefine content sort.</p>
   * <p>The field name must be a field in the entityBean (e.g. "displayIdx"). If the
   * filed is invalid, a error will be logged on the use of the EntitySort. The process
   * will be skipped if fieldName = ""|null.</p>
   * <p><b>NOTE:</b> This.sortResult is called if the EntitySort or its sort order
   * has changed.</p>
   * @see #setSort(bubblewrap.entity.core.EntitySort)
   * @param sSortField the field to sort on or null to reset the sort.
   * @param sortAsc the sort order or null to reverse the sort order of an existing
   * field.
   */
  public final void setSortField(String fieldName, Boolean sortAsc) {
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      if (this.entitySort != null) {
        this.entitySort = null;
        this.sortResult();
      }
    } else if ((this.entitySort != null) && (this.entitySort.isField(fieldName))) {
      if (sortAsc == null) {
        sortAsc = (!this.entitySort.getSortAsc());
        this.entitySort.setSortAsc(sortAsc);
        this.sortResult();
      } else if (this.entitySort.getSortAsc() != sortAsc) {
        this.entitySort.setSortAsc(sortAsc);
        this.sortResult();
      }
    } else {
      sortAsc = (sortAsc == null)? true: sortAsc;
      this.entitySort = new EntitySort(fieldName, sortAsc);
      this.sortResult();
    }
  }
  
  /**
   * Called to assign the EntitySort as the Search's 'static' sort. It will replace any
   * previous sort definition or reset the sort if entSort=null.
   * @param entSort the new content sort
   * @see #setSortField(java.lang.String, java.lang.Boolean)
   */
  public void setSort(EntitySort entSort) {
    this.entitySort = entSort;
    this.sortResult();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Manage Column Sorts">
  /**
   * Get whether the doColumnSort flag is set
   * @return the assigned value (default=null)
   */
  public Boolean getDoColumnSort() {
    return ((this.doColumnSort != null) && (this.doColumnSort));
  }
  
  /**
   * Set whether the ColumnSort applies. Set this.isModified=true if the value changed.
   * @param doSet true to turn the Column Sort on.
   */
  public void setDoColumnSort(Boolean doSet) {
    doSet = ((doSet == null) || (!doSet))? null: doSet;
    if (!DataEntry.isEq(doSet,this.columnSort)) {
      this.doColumnSort = doSet;
      this.sortResult();
    }
  }
  
  /**
   * Called from the constructor of an inheritor to assign a custom EntitySort as a
   * ColumnSort when a user click on the sort column.
   * @param colSort the custom colSort
   */
  public final void addCustomColumnSort(EntitySort colSort) {
    if (colSort == null) {
      return;
    }
    String sortId = colSort.getSortIdField();
    if (this.columnSortMap == null) {
      this.columnSortMap = new HashMap<>();
    }
    this.columnSortMap.put(sortId, colSort);
  }
  
  /**
   * Called to retrieve a clone of the the EntitySort with a matching sortIdField from
   * the pre-registered customColumn Sort EntitySorts
   * @param sortIdField the Sort ID field to search for
   * @return a clone of the EntitySort if found or null if not found.
   */
  protected final EntitySort getCustomColumnSort(String sortIdField) {
    EntitySort result = null;
    EntitySort colSort = null;
    if ((this.columnSortMap != null) && 
            (this.columnSortMap.containsKey(sortIdField)) && 
            ((colSort = this.columnSortMap.get(sortIdField)) != null)) {
      result = colSort.clone();
    }
    return result;
  }
  
  /**
   * Called to set the Column Search and the doColumnSearch. Typically called to restore
   * the colSOrt or to reset it (i.e., with colSort=null and doSort=null|false;
   * Set this.isModified=true;
   */
  public void setColumSort(EntitySort colSort, Boolean doSort) {
    this.columnSort = colSort;
    this.setDoColumnSort(doSort);
    this.sortResult();
  }
  
  /**
   * <p>Called by a FacePage ListView's Column when the user click on the Column Heading
   * to set or reverse the Column sort order. If the column is not in the ColumnSort,
   * it will be added, otherwise it the column's sort order will be reversed.</p>
   * <p><b>Note:</b> The call is ignored if sortIdField=""|null or doColumnSort=false
   * </p>
   * @param sortIdField the Entity Sort ID Field to sort on.
   */
  public final void clickSortField(String sortIdField) {
    sortIdField = DataEntry.cleanString(sortIdField);
    if ((sortIdField == null) || (!this.getDoColumnSort())) {
      return;
    }
    
    if ((this.columnSort != null) && (this.columnSort.hasField(sortIdField))) {
      this.columnSort.reverseSortOrder(sortIdField);
    } else {
      EntitySort newSort = this.getCustomColumnSort(sortIdField);
      if (newSort == null) {
        newSort = new EntitySort(sortIdField, true);
      }
      
      if (this.columnSort == null) {
        this.columnSort = newSort;
      } else {
        this.columnSort.addSubSort(newSort);
      }
    }
    this.sortResult();
  }
  
  /**
   * <p>Called by the FacePage Colum to get the Column's SortOrder StyleClass. It
   * returns on of three values:</p><ul>
   *  <li><b>wfColumnSortOff:</b> - if the EntitySort, sField=""|null, or
   *    (!EntitySort.isField(sField))</li>
   *   <li><b>wfColumnSortAsc:</b> - if the field is sorted Ascending order</li>
   *   <li><b>wfColumnSortDsc:</b> - if the field is sorted Descending order</li>
   * </ul>
   * @param fieldName the field to evaluate
   * @return the resulting styleClass
   */
  public final String getSortOrderClass(String fieldName) {
    String result = "wfColumnSortOff";
    fieldName = DataEntry.cleanString(fieldName);
    if ((this.getDoColumnSort()) &&
            (this.columnSort != null) && (fieldName != null)
            && (this.columnSort.hasField(fieldName))) {
      result = (this.columnSort.getSortAsc(fieldName))? "wfColumnSortAsc": 
                                                        "wfColumnSortDsc";
    }
    return result;
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Manager Search Options">
  /**
   * Set the RecordSearch's FacadeHelper's Filter Options. If any of the settings is
   * unassigned, it will be ignored. Set this.modified = true.
   * @param bActiveOnly new setting
   * @param bDoKFilter new setting
   * @see FacadeFilterEnums
   */
  public void setSearchOptions(boolean bActiveOnly, boolean bDoKFilter) {
    Integer srcOptions = FacadeFilterEnums.setFilter(bActiveOnly, false, bDoKFilter);
    if ((!DataEntry.isEq(this.searchOptions, srcOptions))) {
      this.searchOptions = srcOptions;
      this.setModified();
    }
  }
  
  /**
   * Call to reset the searchOptions and let the default settings (if defined) apply
   */
  public void resetSearchOptions() {
    if (this.searchOptions != null) {
      this.searchOptions = null;
      this.setModified();
    }
  }
  
  /**
   * Get whether the DoFKFilter search option is set
   * @return boolean
   */
  public boolean getDoFKFilter() {
    return (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.FKFILTER);
  }
  
  /**
   * If the SearchFilter's FacadeHelper.doFilter=bSet
   * @param setOption new setting
   */
  public void setDoFKFilter(boolean setOption) {
    boolean curSet = (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.FKFILTER);
    if (curSet != setOption) {
      boolean otherOption = (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.ENABLEDONLY);
      this.searchOptions = FacadeFilterEnums.setFilter(otherOption, false, setOption);
      this.setModified();
    }
  }
  
  /**
   * Get whether the ActiveOnly search option is set
   * @return boolean
   */
  public boolean getActiveOnly() {
    return (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.ENABLEDONLY);
  }
   
  /**
   * If the SearchFilter's FacadeHelper.ActiveOnly=bSet
   * @param bSet new setting
   */
  public void setActiveOnly(boolean bSet) {
    boolean bCurSet = (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.ENABLEDONLY);
    if (bCurSet != bSet) {
      boolean bOther = (this.searchOptions == null)? false: 
                    EnumInfo.isSet(this.searchOptions, FacadeFilterEnums.FKFILTER);
      this.searchOptions = FacadeFilterEnums.setFilter(bSet, false, bOther);
      this.setModified();
    }
  }
  
  /**
   * Called by the refreshSearch to get the FilterOptions to pass to the FacadeHelper
   * @return return the assigned searchOptions or the defaultOptions if the 
   * searchOptions were not set (default=NONE)
   */
  protected int getFilterOptions() {
    int result = (this.searchOptions == null)? 0: this.searchOptions;
    if ((this.searchOptions == null) && (this.defaultSearchOptions != null)) {
      result = this.defaultSearchOptions;
    }
    return result;
  }
  
  /**
   * Called to reset the PrimaryKey[parentClass]. Ignored if parentClass=null.
   * Exceptions are logged.
   * @param parentClass the parent class of the PrimaryKey
   */
  @SuppressWarnings("unchecked")
  public void resetParentPrimaryKey(Class<? extends Serializable> parentClass) {
    try {
      if (parentClass == null) {
        return;
      }
      
      this.beginSearchChange();
      EntityFacade facade = EntityWrapper.getFacadeByBean(parentClass);
      if (facade == null) {
        return;
      }

      facade.resetPkFilter();
      this.onResetParentPrimaryKey(parentClass);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.resetParentPrimaryKey Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endSearchChange();
    }
  }

  /**
   * CAN OVERRIDE: Called by resetParentPkFilters to handle the reset of 
 indirectly related foreign keys that might impact the record selection.
   * @param parentClass the parent class of the foreign key
   * @throws Exception
   */
  protected void onResetParentPrimaryKey(Class<? extends Serializable> parentClass) 
          throws Exception {
  } 
  
  /**
   * Return true if the search contains no SearchFilters or no static filter with 
   * assigned EntityFilters.
   * @return boolean
   */
  public final boolean isEmpty() {
    boolean result = true;
    if (this.staticFilter != null) {
      result = (!this.staticFilter.hasFilter());
    }
    if ((this.namedQuery != null) || (this.customQuery != null)) {
      result = true;
    }    
    if ((result) && (this.filterMap != null) && (!this.filterMap.isEmpty())) {
      for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
        if ((srcFilter != null) && (srcFilter.hasFilter())) {
          result = false;
        }
      }
    }
    return result;
  }
  
  /**
   * Check if this search has an assigned custom or named query
   * @return true if (this.namedQuery != null) || (this.customQuery != null)
   */
  public final boolean hasCustomQuery() {
    return (this.namedQuery != null) || (this.customQuery != null);
  }
  
  /**
   * Add a new SearchFilter to the Search's Filter collection.
   * @param srcFilter SearchFilterBase<V>
   */
  public final void addFilter(SearchFilterBase<TBean> srcFilter) throws Exception {
    if (srcFilter == null) {
      return;
    }
    
    if (this.hasCustomQuery()) {
      throw new Exception("Additional filters is ignored if the sarch using a Named- or "
              + "CustomQuery.");
    }
    
    try {      
      String searchId = DataEntry.cleanString(srcFilter.getSearchId());
      if (searchId == null) {
        throw new Exception("The SearchFilter's SearchId cannot by unassigned.");
      }
      
      if (!srcFilter.hasFilter()) {
        throw new Exception("The SearchFilter's EntityFilter(s) must be assigned.");
      }
      
      if (this.filterMap == null) {
        this.filterMap = new HashMap<>();
      }
      
      this.filterMap.put(searchId, srcFilter);
      srcFilter.SearchChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          EntityListSearch listener = this.getListener();
          if (listener != null) {
            try {
              listener.isModified = true;
              listener.fireSearchChanged();        
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      });
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.setModified();
    }
  }
  
  /**
   * removeRecord a previously added Search Filter
   * @param searchId String
   */
  public final void removeFilter(String searchId) {
    try {
      if ((!this.hasCustomQuery()) && 
              ((searchId = DataEntry.cleanString(searchId)) != null) && 
              (this.filterMap != null) && (this.filterMap.containsKey(searchId))) {
        SearchFilterBase<TBean> srcFilter = this.filterMap.get(searchId);
        if (srcFilter != null) {
          srcFilter.SearchChanged.remove(this);
        }
        srcFilter = null;
        this.filterMap.remove(searchId);
        this.setModified();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.removeFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Get a reference to SearchFilter[sSearchId]. return null if not found.
   * @param TFilter extends SearchFilterBase<V>
   * @param searchId String
   * @return TFilter
   */
  @SuppressWarnings("unchecked")
  public final <TFilter extends SearchFilterBase<TBean>> TFilter 
                                                          getFilter(String searchId) {
    TFilter result = null;
    try {
      if ((!this.hasCustomQuery()) && 
              ((searchId = DataEntry.cleanString(searchId)) != null) && 
              (this.filterMap != null) && (this.filterMap.containsKey(searchId))) {
        result = (TFilter) this.filterMap.get(searchId);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the Search's SearchFilter collection. Return null if undefined or empty or it
   * has no filter with an assigned EntityFilter.
   * @return List<ISearchFilter<V>>
   */
  public final List<SearchFilterBase<TBean>> getSearchFilters() {
    List<SearchFilterBase<TBean>> result = new ArrayList<>();
    try {
      if ((!this.hasCustomQuery()) && 
              (this.filterMap != null) && (!this.filterMap.isEmpty())) {
        for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
          if ((srcFilter != null) && (srcFilter.hasFilter())) {
            result.add(srcFilter);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSearchFilters Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * RemoveRecord all SearchFilters from the search
   */
  public void removeAllFilters() {
    if (this.filterMap != null) {
      if (!this.filterMap.isEmpty()) {
        for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
          if (srcFilter != null) {
            srcFilter.SearchChanged.remove(this);
          }
        }
      }
      this.filterMap.clear();
    }
    this.filterMap = null;
    this.entityList = null;
    this.namedQuery = null;
    this.customQuery = null;
    this.setModified();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Management methods">
  /**
   * Set the maximum number of records to return. If set to equal to 0, it returns all
   * matching records. Otherwise it returns up to iMaxResult values.
   * @param maxResults Integer
   */
  public final void setMaxResults(Integer maxResults) {
    Integer curVal = ((maxResults == null) || (maxResults <= 0))? null: maxResults;
    if (((this.maxResults == null) && (curVal != null)) || 
            ((this.maxResults != null) && (!this.maxResults.equals(curVal)))) {
      this.maxResults = curVal;
      this.setModified();
    } 
  }
  
  /**
   * Get the maximum number of records to return. return 0 is not set or unlimited.
   * @return Integer
   */
  public Integer getMaxResults() {
    return (this.maxResults == null)? 0: this.maxResults;
  }
  
  /**
   * Called internally to get the searchRange (i.e., the range of records to return).
   * Return null if !this.hasMaxResults.
   * @return search array or null is !this.hasResultLimit
   */
  protected final FilterRange getSearchRange() {
    FilterRange result = null;
    if (this.hasMaxResults()) {
      result = new FilterRange(0,this.maxResults.intValue());
    }
    return result;
  }
  
  /**
   * Reset any previously set recordLimit
   */
  public final void clearMaxResults() {
    this.maxResults = null;
    this.setModified();
  }
  
  /**
   * Return true if the maximum number of records to return is set.
   * @return boolean
   */
  public final boolean hasMaxResults() {
    return ((this.maxResults != null) && (this.maxResults > 0));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="StaticFilter methods">  
  /**
   * Add a Static Filter to the RecordSearch. It assign the recordSeaerch as an 
   * EventListener to the Filter.   
   * <p><b>NOTE:</b> A RecordSearch supports only one Static Filter. Thus, an 
   * IllegalArgumentException will be thrown if the filter is already assigned. 
   * It will also throw a NullPointerException is pFilter=null.</p>
   * @param srcFilter the recordSearch's static Filter.
   */
  public void addStaticFilter(SearchStaticFilter<TBean> srcFilter) {
    if (srcFilter == null) { 
      throw new NullPointerException("The new StaticFilter is unassigned.");
    } else if (this.staticFilter != null) {
      throw new IllegalArgumentException("The EntityListSearch's StaticFilter is "
              + "already assigned.");
    }
    
    this.staticFilter = srcFilter;
    this.staticFilter.SearchChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          EntityListSearch listener = this.getListener();
          if (listener != null) {
            try {
              listener.isModified = true;
              listener.fireSearchChanged();        
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      });
  }
  
  /**
   * Get whether the StaticFilter is set.
   * @return staiticFilter.currentSearchValue or false if the staiticFilter=null
   */
  public boolean isStaticFilterSet() {
    return ((this.staticFilter != null) 
            && (this.staticFilter.getCurrentSearchValue()));
  }
  
  /**
   * Set the StaticFilter's SearchValue="true"|"false" based on bIsOn.
   * @param isSetOn the new filter state
   */
  public void setStaticFilter(boolean isSetOn) {
    if (this.staticFilter != null) {
      String sValue = (isSetOn)? "true": "false";
      this.staticFilter.setSearchValue(sValue);
    }
  }
  
  /**
   * Set the StaticFilter to its default set state (as per design)
   */
  public void resetStaticFilter() {
    if (this.staticFilter != null) {
      this.staticFilter.clearFilter();
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Search Setting Management">
  /**
   * Gets the SearchValue of the SearchFilter[sSearchId].
   * @param searchId The string ID of the filter to find.
   * @return The String value of the filter.
   */
  @SuppressWarnings("unchecked")
  public String getSearchValue(String searchId) {
    String result = null;
    try {
      searchId = DataEntry.cleanString(searchId);
      SearchFilterBase<TBean> srcFilter = this.getFilter(searchId);
      if (srcFilter != null) {
        result = srcFilter.getSearchValue();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Set the SearchValue of a SearchFilter[sSearchId]. If pValue=null, call the filter's
   * clearFilter method. Otherwise, call its setSearchValue method.  Ignore the call
   * if sSearchId is unassigned or empty.
   * @param searchId the searchId of the search to update
   * @param srcValue a String search value
   */
  @SuppressWarnings("unchecked")
  public void setSearchValue(String searchId, String srcValue) {
    try {
      this.beginSearchChange();
      searchId = DataEntry.cleanString(searchId);
      String sValue = DataEntry.cleanString(srcValue);
      SearchFilterBase<TBean> srcFilter = this.getFilter(searchId);
      if (srcFilter != null) {
        if (sValue == null) {
          srcFilter.clearFilter();
        } else {
          srcFilter.setSearchValue(srcValue);
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endSearchChange();
    }
  }
  
  /**
   * Set the SearchValue of a SearchFilter[sSearcId]. If pValue=null, call the filter's
   * clearFilter method. Otherwise, call its setSearchValue method.  Ignore the call
   * if sSearchId is unassigned or empty.
   * @param searchId the searchId of the search to update
   * @param srcValue a Integer search value
   */
  @SuppressWarnings("unchecked")
  public void setSearchValue(String searchId ,Integer srcValue) {    
    try {
      this.beginSearchChange();      
      SearchFilterBase<TBean> srcFilter = this.getFilter(searchId);
      if (srcFilter != null) {
        if (srcValue == null) {
          srcFilter.clearFilter();
        } else {
          srcFilter.setSearchValue(srcValue.toString());
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endSearchChange();
    }
  }

  
  /**
   * Set the SearchValue of a SearchFilter[sSearcId]. If pValue=null, call the filter's
   * clearFilter method. Otherwise, call its setSearchValue method.  Ignore the call
   * if sSearchId is unassigned or empty.
   * @param searchId the searchId of the search to update
   * @param srcValue a boolean search value
   */
  @SuppressWarnings("unchecked")
  public void setSearchValue(String searchId, Boolean srcValue) {    
    try {
      this.beginSearchChange();
      SearchFilterBase<TBean> srcFilter = this.getFilter(searchId);
      if (srcFilter != null) {
        if (srcValue == null) {
          srcFilter.clearFilter();
        } else {
          srcFilter.setSearchValue(srcValue.toString());
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.endSearchChange();
    }
  }
 
  
  /**
   * Locate SearchFilter[sSearcId] and, if found, call its clearFilter method.
   * @param searchId String
   */
  public void clearFilter(String searchId) {
    try {
      this.beginSearchChange();
      SearchFilterBase<TBean> srcFilter = this.getFilter(searchId);
      if (srcFilter != null) {
        srcFilter.clearFilter();
      }
      searchId = DataEntry.cleanString(searchId);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }
  }
  
  /**
   * Clear all the SearchFilters in the RecordSearch by calling its clearFilter method. 
   * <p><b>NOTE:</b> This does not reset any ForeighKey Filters or the StaticFilter.
   * Neither does clear the SubSearch's references by the SearchFilters</p>
   * @see #clearAllSearch(boolean, boolean) 
   */
  public void clearSearch() {
    try {
      this.beginSearchChange();
      if (!this.isEmpty()) {
        for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
          if (srcFilter != null) {
            srcFilter.clearFilter();            
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearSearch Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }
  }
  
  /**
   * Clear all the filters in the RecordSearch by calling its clearFilter method. If
 (bResetFKs), calls this.clearPrimaryKeys to clear any ForeignKey settings0
 related to the RecordSearch. if (bResetSubSearch) it will identify any 
 SearchBySubSearchFilters and propagate this call to this filter's sub-RecordSearch.
 If the RecordSearch has a StaticFilter, it will call is clearFilter method to reset 
 it to its default setting
   */
  public void clearAllSearch(boolean resetFKs, boolean resetSubSearch) {
    try {
      this.beginSearchChange();
      if (resetFKs) {
        this.clearPrimaryKeys();
        this.searchOptions = null;
      }
      
      if (this.staticFilter != null) {
        this.staticFilter.clearFilter();
      }
      
      if (!this.isEmpty()) {
        for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
          if (srcFilter != null) {
            srcFilter.clearFilter();
            if ((resetSubSearch) && (srcFilter instanceof SearchBySubSearchFilter)) {
              SearchBySubSearchFilter subFilter = (SearchBySubSearchFilter) srcFilter;
              EntityListSearch subSrch = subFilter.getSubRecordSearch();
              if (subSrch != null) {
                subSrch.clearAllSearch(resetFKs, resetSubSearch);
              }
            }
          }
        }
      }      
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearAllSearch Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }
  }
  
  /**
   * Locate the TAjax class' EntityLink and call its resetEntityFilters to reset any 
   * ForeignKey Filter settings that might influence the ForeignKey filter 
   * result.
   */
  public void clearPrimaryKeys() {
    try {
      this.beginSearchChange();
      EntityFacade facade = this.getFacade();
      facade.resetParentPkFilters();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearPrimaryKeys Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }       
  }
  
  /**
   * Clear only the Content Search Filters (i.e., the non-ColumnFilters)
   */
  public void clearContentSearch() {
    try {
      this.beginSearchChange();
      if (!this.isEmpty()) {
        for (SearchFilterBase<TBean> srchFilter : this.filterMap.values()) {
          if ((srchFilter != null) && (!srchFilter.isColumnFilter())) {
            srchFilter.clearFilter();
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearContentSearch Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }
  }
  
  /**
   * Clear only this searches Column Search Filters 
   */
  public void clearColumnFilters() {
    try {
      this.beginSearchChange();
      if (!this.isEmpty()) {
        for (SearchFilterBase<TBean> pFilter : this.filterMap.values()) {
          if ((pFilter != null) && (pFilter.isColumnFilter())) {
            pFilter.clearFilter();
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearColumnFilters Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.isModified = true;
      this.endSearchChange();
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="EntityList/Keyword Search Options">
  /**
   * CAN OVERRIDE: This method and the get- and setSearchKeyword and the must be 
   * overridden if the EntityList supports a Keyword Search option - used by the 
   * EntityViewFormInfo and its associated List View form to allow users to do keyword 
   * search to narrow down the number of records displayed. The base method returns
   * false.
   * @return true if the EntityListSearch support keyword search (default=false). 
   */
  public boolean doSearchKeyword() {
    return false;
  }
  
  /**
   * CAN OVERRIDE: If {@linkplain #doSearchKeyword() doSearchKeyword} is set this
   * method must also be overridden to return the currently set Keyword Search value.
   * The base method always returns null.
   * @return the currently set Keyword Search value (default=null).
   */
  public String getSearchKeyword() {
    return null;
  }
  
  /**
   * CAN OVERRIDE: If {@linkplain #doSearchKeyword() doSearchKeyword} is set this
   * method must also be overridden to set the currently Keyword Search value.
   * The base method does nothing.
   * @param sValue the new Keyword Search value
   */
  public void setSearchKeyword(String sValue) {    
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="EntityList/Search Result Management">
  /**
   * Check whether any SearchFilter in the EntityListSearch is set. 
   * @return return true if at least one filter is set.
   */
  public boolean isSearchFiltersSet() {
    boolean result = false;
    try {
      if (!this.isEmpty()) {
        for (SearchFilterBase<TBean> srcFilter : this.filterMap.values()) {
          if ((srcFilter != null) && (srcFilter.isSet())) {
            result = true;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isSearchFiltersSet Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Check whether any SearchFilter is set or if DoFKFilters, if any ForeignKey Filter
   * is set. 
   * <p><b>NOTE:</b> This check exclude the StaticFilter, since it applies by default.
   * Call the isStaticFiltrSet to check its isSet state.</p>
   * @return ((this.isSearchFiltersSet()) || ((bDoFK) && (isForeignKeysSet()))).
   */
  public boolean isSet() {
    boolean result = false;
    try {
      int eOptions = this.getFilterOptions();
      boolean doPrimaryKey = (EnumInfo.isSet(eOptions, FacadeFilterEnums.FKFILTER));
      result = ((this.isSearchFiltersSet()) 
                                    || ((doPrimaryKey) && (this.isForeignKeysSet())));
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isSet Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the EntityListSearch's Modified state (i.e., a searchValue, ForeignKey setting,
   * of StaticFilter setting changed) and the result set does not reflect the change.
   * @return return true of the internal flag is set.
   */
  public final boolean isModified() {
    return ((this.isModified == null) || (this.isModified));
  }
  
  /**
   * Protected method to set the flag isModified=true
   */
  public final void setModified() {
    if ((this.isModified == null) || (!this.isModified)) {
      this.isModified = true;
      this.fireSearchChanged();
    }
  }
  
  /**
   * OVERLOAD 1: Call Overload 2 with bForce=false;
   */
  @SuppressWarnings({"unchecked", "unchecked"})
  public final void refreshSearch() {
    this.refreshSearch(false);
  }
  
  /**
   * OVERLOAD 2: refreshRecord the Search's result recordset if this.isModified or 
   * bForce=true. This process will reset the current recordset and if (this.isEmpty),
   * it will reset Modified and return without refreshing the recordset). Otherwise, it
   * builds a EnityFilter List as follows:<ul>
   *  <li>If the StaticFilter is assigned, it will call its getFilter method regardless
   *    of whether the filter isSet=true/false. If the EntityFilter is assigned 
   *    and not an EntityEmptFilter, it will be added to the list. If it is an 
   *    EntityEmptFilter, set flag bEmptyResultset=true.</li>
   *  <li>If (!bEmptyResultset), for each searchFilter with isSet=true, call getFilter 
   *    to get its EntityFilter. If the EntityFilter is assigned and not an 
   *    EntityEmptFilter, it will be added to the list. If it is an EntityEmptFilter, 
   *    set flag bEmptyResultset=true.</li>
   * </ul>
   * <p>If (!bEmptyResultset) and (EnityFilter List is not Empty), build a 
   * GroupEntityFilter joining sub-EntityFilters with AND. </p>
   * <p>Then if (!bEmptyResultset): initiates the FacadeHelper with the Search's Sort 
   * and SearchOptions (i.e., doFK=this.doFKFilter, doActive=this.ActiveOnly, 
   * bIsSystem=false), and call its findAllByFilter or findRangeByFilter - depending is 
   * sub-range of records are requested - to get resulting TBean list. If the records 
   * are returned, build a result RecordList.</p>
   * <p>if (bEmptyResultset) it recordset is empty.</p>
   * <p>Finally, set this.modified=false.</p> 
   * <p><b>NOTE:</b> Errors are logged and resulting recordset=null.</p>
   * @param forceRefresh true to force an update regardless whether the search is 
   * modified.
   */
  @SuppressWarnings({"unchecked", "unchecked"})
  public final void refreshSearch(boolean forceRefresh) {
    try {
      if ((!this.isModified()) && (!forceRefresh)) {
        return;
      }
      
      
      this.entityList = null;
//      if (this.isEmpty()) {
//        this.mbModified = false;
//        return;
//      }
      
      EntityFacade facade = this.getFacade();
      if (facade == null) {
        throw new Exception("Entity[" + this.getEntityClassName()
                + "]'s Facade is not accessible.");
      }
      
      if (this.namedQuery != null) {
        if (this.isQueryParamsSet()) {
          this.namedQuery.execQuery(facade);
        } else {
          this.isModified = false;      
          //this.fireSearchChanged();
        }
        return;
      } else if (this.customQuery != null) {
        if (this.isQueryParamsSet()) {
          this.customQuery.execQuery(facade);
        } else {
          this.isModified = false;      
          //this.fireSearchChanged();
        }
        return;
      }
            
      boolean noResults = false;
      List<IEntityFilter<TBean>> entFilters = new ArrayList<>();
      if (this.staticFilter != null) {
        IEntityFilter<TBean> entFilter = this.staticFilter.getFilter();
        if (entFilter != null) {
          if (entFilter instanceof EntityEmptyFilter) {
            entFilters.clear();
            noResults = true;
          } else if (entFilter.isSet()) {               
            entFilters.add(entFilter);
          }
        }
      }
      
      if ((!noResults) 
                      && (this.filterMap != null) && (!this.filterMap.isEmpty())) {
        for (SearchFilterBase<TBean> pFilter : this.filterMap.values()) {
          if ((pFilter == null) || (!pFilter.isSet())) {
            continue;
          }

          IEntityFilter<TBean> entFilter = pFilter.getFilter();
          if (entFilter != null) {
            if (entFilter instanceof EntityEmptyFilter) {
              entFilters.clear();
              noResults = true;
              break;
            } else if (entFilter.isSet()) {               
              entFilters.add(entFilter);
            }
          }
        }
      }
      
      IEntityFilter<TBean> entListFilter = null;
      if ((!noResults) && (!entFilters.isEmpty())) {
        if (entFilters.size() == 1) {
          entListFilter = entFilters.get(0);
        } else {
          EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(true, false);
          for (IEntityFilter<TBean> entFilter : entFilters) {
            groupFilter.addFilter(entFilter);
          }
          entListFilter = groupFilter;
        }
      }
      
      if (!noResults) {
        int eOptions = this.getFilterOptions();
        FilterRange range = this.getSearchRange();
        List<TBean> beanList = null; 
        EntitySort backupSort = facade.getEntitySort();
        try {
          /** Clear Sort - results will be sorted afterwards **/
          facade.setEntitySort(null);
          if (range == null) {
            beanList = facade.findAllByFilter(entListFilter,eOptions);
          } else {
            beanList = facade.findRangeByFilter(range, entListFilter,eOptions);
          }
        } finally {
          facade.setEntitySort(backupSort);
        }
        
        /** If the pBeanList is not empty - returns a unique recordList<T> **/
        if ((beanList != null) && (!beanList.isEmpty())) {
          @SuppressWarnings("unchecked")
          Class<TWrapper> wrapperClass = (Class<TWrapper>) this.getEntityWrapperClass();
          for (TBean entBean : beanList) {
            TWrapper entWrapper = 
                            (TWrapper) EntityWrapper.newFromBean(wrapperClass, entBean);
            if (entWrapper != null) {
              if (this.entityList == null) {
                this.entityList = new EntityList<>();
              }
              if (!this.entityList.contains(entWrapper)) {
                this.entityList.add(entWrapper);
              }
            }
          } 
          
          this.sortResult();
        }
      }
      
      if (this.entityList != null)  {
        this.onApplyCustomFilter(this.entityList);
      }
      this.isModified = false;
      
      //this.fireSearchChanged();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.refreshSearch Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Custom/NamedQuery Filter Methods">
  /**
   * Called to set the Custom NamedQuery for the search.
   * <p>
   * <b>NOTE:</b> The Search supports only one NamedQuery or Custom SQL Query. Once 
   * assigned all other query settings is removed and/or ignored. Only the result 
   * sort and custom filter is used.</p>
   * @param nameQuery 
   */
  protected void setNamedQuery(String nameQuery) {
    if (this.hasCustomQuery()) {
      throw new IllegalArgumentException("A EntiyListSearch supports only one "
              + "NamedQuery or Custom SQL Query.");
    }
    nameQuery = DataEntry.cleanString(nameQuery);
    if (nameQuery == null) {
      throw new NullPointerException("The name of the NamedQuery cannot be unassigned.");
    }
    
    this.namedQuery = new EntityNamedQuery<>(this,nameQuery);
    if (this.filterMap != null) {
      this.filterMap.clear();
      this.filterMap = null;
      this.staticFilter = null;
    }
  }
  
  /**
   * Called to set the Custom NamedQuery for the search.
   * <p>
   * <b>NOTE:</b> The Search supports only one NamedQuery or Custom SQL Query. Once 
   * assigned all other query settings is removed and/or ignored. Only the result 
   * sort and custom filter is used.</p>
   * @param nameQuery 
   */
  protected void setSQLQuery(String sqlQuery) {
    if (this.hasCustomQuery()) {
      throw new IllegalArgumentException("A EntiyListSearch supports only one "
              + "NamedQuery or Custom SQL Query.");
    }
    sqlQuery = DataEntry.cleanString(sqlQuery);
    if (sqlQuery == null) {
      throw new NullPointerException("The SQL for the CustomQuery cannot be unassigned.");
    }
    
    this.customQuery = new EntityCustomQuery<>(this,sqlQuery);
    if (this.filterMap != null) {
      this.filterMap.clear();
      this.filterMap = null;
      this.staticFilter = null;
    }
  }
  
  /**
   * CAN OVERRIDE: Called to verify that the {@linkplain EntityNamedQuery} or {@linkplain 
   * EntityCustomQuery} filter's Query Parameters are set. If this method returns false,
   * the search will return an empty result list.
   * <p>The base method always returns true
   * @return true to run the query; false to return an empty EntityList
   */
  protected boolean isQueryParamsSet(){
    return true;
  }
  
  /**
   * CAN OVERRIDE: Called by the {@linkplain EntityNamedQuery} or {@linkplain 
   * EntityCustomQuery} filters to set the Query filters before updating the query.
   * <p><b>The base method does nothing</b></p>
   * @param query the query to update.
   */
  protected void onAssignQueryParams(Query query) throws Exception {}
  
  /**
   * Called by the {@linkplain EntityNamedQuery} or {@linkplain EntityCustomQuery} filters
   * on loadQuery to update the Search's entityList. If the <tt>beanList</tt> != null |
   * empty, it initiate a EntityWrapper for each bean, then sort the list according to 
   * the search's search settings, and then call {@linkplain #onApplyCustomFilter(
   * bubblewrap.entity.core.EntityList) this.onApplyCustomFilter} to apply any 
   * after-query filtering.
   * <p>Finally, it sets this.modified = false.
   * @param beanList the of filtered beans
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  private void onAssignQueryResults(List<TBean> beanList) throws Exception {
    try {
      this.entityList = null;
      /** If the pBeanList is not empty - returns a unique recordList<T> **/
      if (beanList != null) {
        this.entityList = new EntityList<>();
        if (!beanList.isEmpty()) {
          Class<TWrapper> wrapperClass = (Class<TWrapper>) this.getEntityWrapperClass();
          for (TBean entBean : beanList) {
            TWrapper entWrapper = 
                            (TWrapper) EntityWrapper.newFromBean(wrapperClass, entBean);
            if (entWrapper != null) {
              if (this.entityList == null) {
                this.entityList = new EntityList<>();
              }
              if (!this.entityList.contains(entWrapper)) {
                this.entityList.add(entWrapper);
              }
            }
          }  
          this.sortResult();
        }
      }
      
      if ((this.entityList != null) && (!this.entityList.isEmpty())) {
        this.onApplyCustomFilter(this.entityList);
      }
      this.isModified = false;
      
      //this.fireSearchChanged();
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Post Procssing Results Methods">
  /**
   * <p>CAN OVERRIDE: Called after generating the update EntityList to allow inheritors 
   * to apply an additional custom filter - normally something that cannot be defined
   * directly to the bean - to the result recordset. The implementation of this method 
   * can add or remove entities from the EntityList.</p>
   * <p><b>NOTE:</b> This method is only called when this.IsModified = true and the 
   * entityList is not null. However, the entityList can be empty </p>
   * @param entList the updated EntityList
   */
  protected void onApplyCustomFilter(EntityList<TWrapper> entList) {    
  }
  
  /**
   * Called by refereshSearch or when  the sort conditions changed to sort the results
   * EntityList.
   */
  private void sortResult() {
    if ((this.entityList != null) && (this.entityList.size() > 1)) {
      if ((this.getDoColumnSort()) && (this.columnSort != null)) {
        EntityComparator<TWrapper> comparator = new EntityComparator<>(this.columnSort);
        Collections.sort(this.entityList, comparator);
      }  else if (this.entitySort != null) {
        EntityComparator<TWrapper> comparator = new EntityComparator<>(this.entitySort);
          Collections.sort(this.entityList, comparator);
      }
    }
  }
  
  /**
   * Get the current result set. If this.isModified, refresh the Search before
   * returning the results.
   * @return the resulting EntityList
   */
  public EntityList<TWrapper> getEntityList() {
    try {
      if (this.isModified()) {
        this.refreshSearch();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityList Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return this.entityList;
  }
  
  /**
   * Get the "custom-filtered" version of the current result set. It calls the base 
   * {@linkplain #getEntityList() this.getEntityList} to retrieve the current result set,
   * and the apply the <tt>filterDelegate</tt> to extract only the entities for which
   * {@linkplain  EntityFilterDelegate#include(bubblewrap.entity.core.EntityWrapper) 
   * filterDelegate.include} = true.
   * @param filterDelegate the custom filter
   * @return the custom filtered result EntityList
   */
  public EntityList<TWrapper> getEntityList(EntityFilterDelegate<TWrapper> filterDelegate) {
    EntityList<TWrapper> result = null;
    try {
      EntityList<TWrapper> curResult = this.getEntityList();
      if ((filterDelegate == null) || (curResult == null) || (curResult.isEmpty())) {
        result = curResult;
      } else {
        result = new EntityList<>();
        for (TWrapper entity : curResult) {
          if (filterDelegate.include(entity)) {
            result.add(entity);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getEntityList Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the number of records in the search recordset (i.e., after running the
   * refreshSearch if this.isModified).
   * @return the number of results
   */
  public int getEntityCount() {
    List<TWrapper> resultList = this.getEntityList();
    return (resultList == null)? 0: resultList.size();
  }
  
  /**
   * Return true if the search record is not empty (i.e., after running the
   * refreshSearch if this.isModified).
   * @return boolean
   */
  public boolean hasResults() {
    List<TWrapper> resultList = this.getEntityList();
    return ((resultList != null) && (!resultList.isEmpty()));
  }

  /**
   * Return a list of SelectItems(value=pRecord.recordID,label=pRecord.recordName)
   * that represents the values in the recordset.  If the recordsetSearch's Sort options
   * were set, the records will be display in the sorted order.  
   * If (bAddEmptyOption=True) the first options be a blank option, and the caption
   * will be set to sEmptyCaption or an empty string if not defined.
   * @param addEmptyOption boolean
   * @param emptyValue Object
   * @param emptyCaption String
   * @return List<SelectItem>
   */
  public List<SelectItem> getAsOptions(boolean addEmptyOption, Object emptyValue, 
          String emptyCaption) {
    List<SelectItem> result = new ArrayList<>();
    List<TWrapper> resultList = null;
    try {
      resultList = this.getEntityList();
      if (resultList != null) {
        if (addEmptyOption) {
          emptyCaption = (emptyCaption == null)? "" : emptyCaption;
          result.add(new SelectItem(emptyValue, emptyCaption));
        }
        if (!resultList.isEmpty()) {
          for (TWrapper entWrapper : resultList) {
            result.add(new SelectItem(entWrapper.getRecordId().toString(), 
                                      entWrapper.getRecordName()));
          }
        }
      }
    } catch (Exception pExp) {
      resultList = null;
      logger.log(Level.WARNING, "{0}.getAsOptions Error: \n{1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
   
  //<editor-fold defaultstate="collapsed" desc="Change Management Methods">
  /**
   * Call when starting a change in the search criteria. It increment ChangeCount which
   * will prevent the firing of the on ChangeEvent until all changes has been completed.
   * <p><b>NOTE:</b> Always call the beginSearchChange and endSearchChange in a pair 
   * using a Try-Finally brackets.</p>
   */
  public void beginSearchChange() {
    this.changingCount = ((this.changingCount == null) || (this.changingCount < 0))?
            0: this.changingCount;
    this.changingCount ++;
  }
  
  /**
   * Call after completing a change in the SearchValue. It decrement the internal 
   * ChangeCount and if zero, it fires the onChange event.
   */
  public void endSearchChange() {
    if (this.changingCount > 0) {
      this.changingCount --;
    }
    if (this.changingCount == 0) {
      this.fireSearchChanged();
    }
  }
  
  /**
   * Get the recordsetSearch's Search Change state
   * @return true if changes are processed
   */
  public boolean isSearchChanging() {
    return ((this.changingCount != null) && (this.changingCount > 0));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * OVERRIDE: Return "Search[this.entityClassName] - <i> of <n> filters set".
   * @return a string representative of the class.
   */
  @Override
  public String toString() {
    String result = null;
    int filterCnt = 0;
    if ((this.filterMap != null) && (!this.filterMap.isEmpty())) {
      for (SearchFilterBase<TBean> filter : this.filterMap.values()) {
        if (filter.isSet()) {
          filterCnt++;
        }
      }
      result = this.getClass().getSimpleName() + "[" + filterCnt + " of "
            + this.filterMap.size() + " filters set].";
    } else {
      result = this.getClass().getSimpleName() + "[" + filterCnt + " of "
            + filterCnt + " filters set].";
    }
    return result;
  }
  //</editor-fold>
}
