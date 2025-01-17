package bubblewrap.entity.searchfilters;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.filters.EntityEmptyFilter;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.entity.filters.EntityGroupFilter;
import bubblewrap.entity.search.SearchFilterBase;
import static bubblewrap.entity.searchfilters.SearchCustomFilter.logger;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A SearchGroupFilter is a implementation of SearchFilterBase to group multiple by either
 * and AND (i.e. Exclusive) or OR (i.e., Inclusive) filter constraint. It also supports
 * a Distinct filter constraint setting.</p>
 * <p>The SearchGroupFilter is a onChanged event sender as well as the listener of 
 * onChanged event of all its sub-filters.</p>
 * @author kprins
 */
public class SearchGroupFilter<TBean extends Serializable> 
                                       extends SearchFilterBase<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(SearchFilter.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the sub-SearchFilters
   */
  private List<SearchFilterBase<TBean>> subFilters;
  /**
   * The SearchFilter's unique ID - assigned via the constructor
   */
  private String searchId;
  /**
   * A flag stating whether this is column filter for filtering content in a DataGrid.
   */
  private Boolean columnFilter;
  /**
   * The SearchFilter's SearchType - assigned via the constructor
   */
  private EntitySearchTypes searchType;
  /**
   * Placeholder for the SearchFilter's Search Value
   */
  private String searchValue;
  /**
   * Filter Join Conditions (default=true)
   */
  private Boolean joinByAnd;
  /**
   * Filter District Filter Conditions (default=true)
   */
  private Boolean distinct;
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a SearchId a SearchType, and Join Condition and a District
   * flag setting.
   * @param sSearchId String
   * @param eSearchType the EntitySearchTypes
   * @param bAndJoin boolean
   * @param bDistinct boolean
   */
  public SearchGroupFilter(String sSearchId, EntitySearchTypes eSearchType,
                          boolean bAndJoin, boolean bDistinct) {
    sSearchId = DataEntry.cleanString(sSearchId);
    if (sSearchId == null) {
      throw new NullPointerException("The SearchFilter's ID cannot be unassigned.");
    }
    this.searchId = sSearchId;
    this.columnFilter = false;
    this.searchType = eSearchType;
    this.subFilters = null;
    this.searchValue = null;
    this.joinByAnd = bAndJoin;
    this.distinct = ((!joinByAnd) && (bDistinct));
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if ((this.subFilters != null) && (!this.subFilters.isEmpty())) {
      for (SearchFilterBase<TBean> subFilter : subFilters) {
        subFilter.SearchChanged.remove(this);
      }
    }
    this.subFilters = null;
  }
  
  
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Add a sub-filter to the group
   * NOTE: All Sub-filters should be designed to process the same search value
   * @param subFilter SearchFilterBase<V>
   */
  public void addSubFilter(SearchFilterBase<TBean> subFilter) {
    if (subFilter != null) {
      if (this.subFilters == null) {
        this.subFilters = new ArrayList<>();
      }
      this.subFilters.add(subFilter);
      subFilter.SearchChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          SearchGroupFilter listener = this.getListener();
          if (listener != null) {
            try {
              listener.fireSearchChanged();        
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
   * Test to see whether the current group filter contains the specific sub-filter.
   * @param subFilter The SearchFilterBase<V> to test for.
   * @return boolean true if the filter was in the list.
   */
  public boolean containsSubFilter(SearchFilterBase<TBean> subFilter)
  {
    if(this.subFilters == null){
      return false;
    }
    return subFilters.contains(subFilter);
  }
  
  /**
   * Remove a sub-filter from the group.
   * @param subFilter The SearchFilterBase<V> to remove.
   */
  public void removeSubFilter(SearchFilterBase<TBean> subFilter)
  {
    if(this.subFilters == null) {
      return;
    }
    subFilters.remove(subFilter);
    subFilter.SearchChanged.remove(this);
  }
  
  /**
   * Set whether this filter represents a Column Filter that can be used to filter
   * content in a DataGrid (Default= false)
   * @param bSet Boolean
   */
  public void setIsColumnFilter(Boolean bSet) {
    this.columnFilter = bSet;
  }
  
  /**
   * Set whether SearchFilter's sub-filters should be joint by AND (bAnd=true) or OR
   * (nAnd=false). Default = AND|true. If bAnd=false, bDistrict will set to either
   * true|false to return  all or only distinct records.
   * @param bAnd Boolean
   */
  public final void setFilterJoinOption(boolean bAnd, boolean bDistinct) {
    this.joinByAnd = bAnd;
    this.distinct = ((!bAnd) && (bDistinct));
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilterBase Implementation">
  /**
   * Return the GroupFilter's SearchId
   * @return String
   */
  @Override
  public String getSearchId() {
    return this.searchId;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  public EntitySearchTypes getSearchType() {
    return this.searchType;
  }
  
  /**
   * Return true if the GroupFilter has assigned SubFilter's
   * @return boolean
   */
  @Override
  public boolean hasFilter() {
    return ((this.subFilters != null) && (!this.subFilters.isEmpty()));
  }
  
  /**
   * Return if this.hasFilter and at least one sub-filter is set.
   * @return boolean
   */
  @Override
  public boolean isSet() {
    boolean bResult = false;
    try {
      if (this.hasFilter()) {
        for (SearchFilterBase<TBean> pFilter : this.subFilters) {
          if ((pFilter != null) && (pFilter.isSet())) {
            bResult = true;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isSet Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return bResult;
  }
  
  /**
   * Get whether this filter represents a column filter (default=false)
   * @return boolean
   */
  @Override
  public boolean isColumnFilter() {
    return this.columnFilter;
  }
  
  /**
   * Get the last assigned SearchValue
   * @return String
   */
  @Override
  public String getSearchValue() {
    return this.searchValue;
  }
  
  /**
   * Update this.searchValue and assigned the value to all sub-filters
   * @param pValue String
   */
  @Override
  public void setSearchValue(String pValue) {
    this.searchValue = pValue;
    try {
      if (this.hasFilter()) {
        for (SearchFilterBase<TBean> pFilter : this.subFilters) {
          if (pFilter != null) {
            this.onSetSearchValue(pFilter, pValue);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSearchValue Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * <p>CAN OVERRIDE: Called by
   * {@linkplain #setSearchValue(java.lang.String) setSearchValue} to assign the assigned
   * Search value to the sub-filter. The base method call the sub-filter's setSearchValue
   * method.</p>
   * <p>Inheritors can override this method to handle a custom search value assignment for
   * one or more of the sub-filters.</p>
   * @param filter the sub-filter to assign the search value to.
   * @param srchValue the search value
   */
  protected void onSetSearchValue(SearchFilterBase<TBean> filter, String srchValue) {
    filter.setSearchValue(srchValue);
  }
  
  /**
   * Called to reset the sub-filters' searchValue
   */
  @Override
  public void clearFilter() {
    this.searchValue = null;
    if (this.hasFilter()) {
      for (SearchFilterBase<TBean> pFilter : this.subFilters) {
        if (pFilter != null) {
          pFilter.clearFilter();
        }
      }
    }
  }
  
  /**
   * <p>Return a EntityFilter that include a combination of this GroupFiletr's set filters.
   * It might be a EnityValueFilter (if only one filter is set) or a EntityGroupFilter
   * if more than one sub-filter is set or null if !this.isSet. </p>
   * <p>If any of the subFilters returns a EntityEmptyFilter, this method ignores all 
   * other and return the value if the sub-filter join Option is set to bAND=true. 
   * Otherwise, the empty filter will be ignored.</p>
   * @return EntityFilter<V>
   */
  @Override
  @SuppressWarnings("unchecked")
  public EntityFilter<TBean> getFilter() {
    EntityFilter<TBean> pResult = null;
    try {
      if ((this.subFilters != null) && (!this.subFilters.isEmpty())) {
        List<EntityFilter<TBean>> pEntFilters = new ArrayList<>();
        for (SearchFilterBase<TBean> pFilter : this.subFilters) {
          if ((pFilter == null) || (!pFilter.isSet())) {
            continue;
          }
          
          EntityFilter<TBean> pEntFilter = pFilter.getFilter();
          if (pEntFilter != null) {
            if (pEntFilter instanceof EntityEmptyFilter) {
              if (this.joinByAnd == true) {
                pResult = pEntFilter;
                break;
              }
            } else if (pEntFilter.isSet()) {
              pEntFilters.add(pEntFilter);
            }
          }
        }
        
        if ((pResult == null) && (!pEntFilters.isEmpty())) {
          if (pEntFilters.size() == 1) {
            pResult = pEntFilters.get(0);
          } else {
            EntityGroupFilter<TBean> pGrpFilter =
                    new EntityGroupFilter<>(this.joinByAnd, this.distinct);
            for (EntityFilter<TBean> pEntFilter : pEntFilters) {
              if (pEntFilter != null) {
                pGrpFilter.addFilter(pEntFilter);
              }
            }
            pResult = pGrpFilter;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>
}
