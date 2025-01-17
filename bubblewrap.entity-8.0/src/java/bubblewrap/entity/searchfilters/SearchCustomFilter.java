package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.entity.search.SearchFilterBase;
import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.BoolValidator;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This abstract class implements the ISearchFilters and extends it by adding the base
 * functionally for defining a fixed EntityGroupFilter that will be returned if the 
 * filter searchValue=true. Otherwise it will return null.</p>
 * <p>It supports Event[CHANGED], which is fired when the filters SearchValue changes or 
 * conditions in the custom search settings has changes (see SearchBySubSearcFilter).</p>
 * <p><b>NOTE:</b> This.searchType=Boolean, which implies that searchValue can only be 
 * true|false. The default behavior of the SearchCustomFilter is that the isSet is 
 * controlled by through the searchValue. This implies that Filter's isSet state can be 
 * turned on/off using the searchValue. if the filter is cleared, the isSet state is 
 * controlled by the defaultSearchValue. </p>
 * @see RecordsetSearch
 * @see SearchBySubSearchFilter
 * @author kprins
 */
public abstract class SearchCustomFilter<TBean extends Serializable> 
                                                    extends SearchFilterBase<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(SearchCustomFilter.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The SearchFilter's unique ID - assigned via the constructor
   */
  private String searchId;
  /**
   * A flag stating whether this is column filter for filtering content in a DataGrid.
   */
  private Boolean isColumnFilter;
  /**
   * The SearchFilter's SearchType - assigned via the constructor
   */
  private EntitySearchTypes searchType;
  /**
   * Placeholder for the SearchFilter's Default Search Setting. Default=null|false;
   */
  private Boolean defaultValue;
  /**
   * Placeholder for the SearchFilter's user entered Search Option (True|False)
   */
  private Boolean searchValue;
  /**
   * The raw value entered by the user
   */
  private String searchInput;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protected constructor with the SearchId - must be defined.
   * @param sSearcId String
   */
  protected SearchCustomFilter(String sSearcId) {
    sSearcId = DataEntry.cleanString(sSearcId);
    if (sSearcId == null) {
      throw new NullPointerException("The SearchFilter's ID cannot be unassigned.");
    }
    this.searchId = sSearcId;
    this.searchType = EntitySearchTypes.BOOLEAN;
    this.searchValue = null;
    this.defaultValue = null;
    this.isColumnFilter = false;
    this.searchInput = null;
  }

  /**
   * OVERRIDE: Release the local resources before calling the super method.
   * @throws Throwable 
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilterBase Implementation">
  /**
   * Get the SearchFilter's Unique ID
   * @return String
   */
  @Override
  public final String getSearchId() {
    return this.searchId;
  }
  
  /**
   * Get whether this filter represents a Column Filter that can be used to filter 
   * content in a DataGrid
   * @return boolean
   */
  @Override
  public final boolean isColumnFilter() {
    return ((this.isColumnFilter != null) && (this.isColumnFilter));
  }
  
  /**
   * {@inheritDoc }
   */
  @Override
  public final EntitySearchTypes getSearchType() {
    return this.searchType;
  }
  
  /**
   * Get whether the search is set (i.e., this.hasFilter && this.currentSearchValue).
   * <p><b>NOTE:</b> This.searchType=Boolean, which implies that searchValue can only be 
   * true|false. The default behavior of the SearchCustomFilter is that the isSet is 
   * controlled by through the searchValue. This implies that Filter's isSet state can 
   * be turned on/off using the searchValue. if the filter is cleared, the isSet state 
   * is controlled by the defaultSearchValue. </p>
   * @return true if the filter has a set EntityFilter
   */
  @Override
  public boolean isSet() {
    return ((this.hasFilter())  && (this.getCurrentSearchValue()));
  }
  
  /**
   * Get the Filter's user-entered Search Value.
   * <p><b>NOTE:</b> The SearchFilter's SearchValue is the value entered by the user
   * could contain leading or trailing spaces.</p>
   * @return String
   */
  @Override
  public final String getSearchValue() {
    return this.searchInput;
  }
  
  /**
   * Set the Filter's user-entered Search Value.
   * <p><b>NOTE:</b> The SearchFilter's SearchValue is the value entered by the user
   * could contain leading or trailing spaces.</p>
   * @param sValue String
   */
  @Override
  public void setSearchValue(String sValue) {
    sValue = DataEntry.cleanString(sValue);
    BoolValidator pValidator = new BoolValidator();
    Boolean bValue = (sValue == null)? null: pValidator.toValue(sValue);
    if (!DataEntry.isEq(this.searchValue, bValue)) {
      this.searchValue = bValue;
      this.fireSearchChanged();
    }
  }
  
  /**
   * Called to reset the filter
   */
  @Override
  public void clearFilter() {
    try {
      this.searchValue = null;
      this.searchInput = null;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.fireSearchChanged();
    }
  }
  
  /**
   * <p>Get the EntityFilter for the SeachFilter. It calls onGetFilter and trap and log
   * all errors.</p>
   * <p><b>NOTE:</b> This method should return a null value if the SearchFilter's
   * hasFiletr=false.</p>
   * @return IEntityFilter<V>
   */
  @Override
  @SuppressWarnings("unchecked")
  public final EntityFilter<TBean> getFilter() {
    EntityFilter<TBean> pResult = null;
    try {
      if (this.hasFilter()) {
        pResult = this.onGetFilter();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Public/Abstract Methods">
  /**
   * Set whether this filter represents a Column Filter that can be used to filter 
   * content in a DataGrid (Default= false)
   * @param bSet Boolean
   */
  protected void setIsColumnFilter(Boolean bSet) {
    this.isColumnFilter = bSet;
  }
  
  /**
   * Set the Search's Default Search Value. If set to true, then the filter is set by 
   * default. (Default=false)
   * @param bValue If (true), then the filter is set by default.
   */
  public void setDefaultSearchValue(boolean bValue) {
    this.defaultValue = (!bValue)? null: bValue;
  }
  
  /**
   * Set the Search's Default Search Value. If set to true, then the filter is set by 
   * default. (Default=false)
   * @param bValue If (true), then the filter is set by default.
   */
  public boolean getDefaultSearchValue() {
    return ((this.defaultValue != null) && (this.defaultValue));
  }
  
  /**
   * Get the current filter's SearchValue value
   * @return (this.searchValue=null)? this.defaultSearchValue: this.searchValue
   */
  public boolean getCurrentSearchValue() {
    return (this.searchValue == null)? this.getDefaultSearchValue():
                                                        this.searchValue;
  }
   
  /**
   * ABSTRACT: called be getFilter to return the custom filter. It is only called when 
   * the isSet (i.e., the seachValue=true).
   * @return EntityFilter<V>
   */
  protected abstract EntityFilter<TBean> onGetFilter();
  //</editor-fold>   
  
  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * OVERRIDE: Return "<className>[Value=" + this.getSearchValue() + "]";
   * @return a string representation of the filter
   */
  @Override
  public String toString() {
    String sSet = Boolean.toString(this.isSet());
    return this.getClass().getSimpleName() + "[isSet=" + sSet + "]";
  }
  //</editor-fold>
}
