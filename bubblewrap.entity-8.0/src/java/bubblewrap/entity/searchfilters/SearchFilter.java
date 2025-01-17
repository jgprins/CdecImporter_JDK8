package bubblewrap.entity.searchfilters;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.enums.StringSearchOptions;
import bubblewrap.entity.filters.EntityEmptyFilter;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.entity.filters.EntityGroupFilter;
import bubblewrap.entity.filters.EntityValueFilter;
import bubblewrap.entity.search.SearchFilterBase;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This abstract class implements the ISearchFilters&lt;TBean&gt; and extends it by 
 * adding a generic search of TBean using search value of type TValue. It getFilter 
 * method is called to generate the EntityFilter&lt;TBean&gt; for selecting the TBean 
 * records that complies to the Filter settings.  The SearchFilter can comprise of one
 * or more EntityFilter filters on different TBean fields, which can be combined using
 * an AND or OR clause.  The assigned value will be assigned as a string to all sub
 * EntityFilters. The parseSearchValues is an abstract method that must be overridden 
 * by inheritors to parse the searchValue into a list&lt;TValue&gt; which will be used 
 * by the getFilter method to build a EntityGroupFilter&lt;TBean&gt; that contains all 
 * the EntityFilter to search for.</p> 
 * <p>The SearchFilter supports an onChange event, which is fired when the filters 
 * SearchValue changes. Use the addEventListener and removeEventListener method to
 * assign and/or remove listeners to this event.</p>
 * @see RecordsetSearch
 * @author kprins
 */
public abstract class SearchFilter<TBean extends Serializable, TValue> 
                                                    extends SearchFilterBase<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(SearchFilter.class.getName());
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the SearchFilter sub-EntityFilters
   */
  private List<EntityFilter<TBean>> entityFilters;
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
   * Placeholder for the SearchFilter's Search Value
   */
  private String searchValue;
  /**
   * Placeholder for the SearchFilter's Default Search Value (i.e., the value to search
   * for if the user is no search value has been set - for Fix-SearchOptions that are
   * always set). Default=null;
   */
  private String defaultValue;
  /**
   * Placeholder for the SearchFilter's user entered Search Value (not cleaned)
   */
  private String enteredValue;
  /**
   * Filter Conditions (default=true)
   */
  private Boolean doAnd;
  /**
   * Placeholder for a set of delimiters use in parsing the SearchValue to a list of
   * Search Words. Default="\s+|\s*,\s*|\s*:\s*|\s*;\s*" (i.e. any number of space,
   * ',',':',';). If Null, the SearchValue will not by split into sub-search values.
   */
  private String delimiters;
  /**
   * Placeholder for assigning a StringSearchOption 
   * (type {@linkplain StringSearchOptions}) 
   * (Default={@linkplain StringSearchOptions#WHOLE})
   */
  private StringSearchOptions seachOptions;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protected constructor with the SearchId - must be defined.
   * @param searchId the Filter's search Identifier (could be from 
   * {@linkplain SearchIds}}
   * @param searchType an enum value of type {@linkplain EntitySearchTypes}
   */
  protected SearchFilter(String searchId, EntitySearchTypes searchType) {
    searchId = DataEntry.cleanString(searchId);
    if (searchId == null) {
      throw new NullPointerException("The SearchFilter's ID cannot be unassigned.");
    }    
    this.searchId = searchId;
    this.searchType = searchType;
    this.entityFilters = null;
    this.searchValue = null;
    this.defaultValue = null;
    this.enteredValue = null;
    this.delimiters = "\\s+|\\s*,\\s*|\\s*:\\s*|\\s*;\\s*";
    this.seachOptions = StringSearchOptions.WHOLE;
  }

  /**
   * OVERRIDE: Release the local resources before calling the super method.
   * @throws Throwable 
   */
  @Override
  protected void finalize() throws Throwable {
    this.entityFilters = null;
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
   * Get the SearchFilter's SearchType
   * @return the assigned {@linkplain EntitySearchTypes}
   */
  @Override
  public final EntitySearchTypes getSearchType() {
    return this.searchType;
  }
  
  /**
   * Get whether the SearchFilter has assigned EntityFilters
   * @return boolean
   */
  @Override
  public final boolean hasFilter() {
    return ((this.entityFilters != null) && (!this.entityFilters.isEmpty()));
  }
  
  /**
   * Get whether the filter has filters and the search value is set.
   * @return boolean
   */
  @Override
  public boolean isSet() {
    boolean hasF = this.hasFilter();
    String search = this.getCleanSearchValue();
    return ((this.hasFilter()) && (this.getCleanSearchValue() != null));
  }
  
  /**
   * Get the Filter's user-entered Search Value.
   * <p><b>NOTE:</b> The SearchFilter's SearchValue is the value entered by the user
   * could contain leading or trailing spaces.</p>
   * @return String
   */
  @Override
  public final String getSearchValue() {
    return this.enteredValue;
  }
  
  /**
   * Set the Filter's user-entered Search Value.
   * <p><b>NOTE:</b> The SearchFilter's SearchValue is the value entered by the user
   * could contain leading or trailing spaces.</p>
   * @param value the search value
   */
  @Override
  public final void setSearchValue(String value) {
    this.enteredValue = value;
    value = DataEntry.cleanString(value);
    if (!DataEntry.isEq(this.searchValue, value, true)) {
      this.searchValue = value;
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
      this.enteredValue = null;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.clearFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    } finally {
      this.fireSearchChanged();
    }
  }
  
  /**
   * <p>Get the EntityFilter for the SeachFilter. It retrieves a list of filter values 
   * of class<Y> by calling this.parsedSearchValues. and then build a single or group
   * EntityFilter from this.mpEntityFilters, cloning each filter and assigning it a
   * value in the list of filter values.</p>
   * <p><b>NOTE:</b> This method returns a null value if the EntityFilters are not set,
   * the SearchValue is not set, or an exception is thrown.  If the searchValue is set, 
   * but the parseSearchValues returns null or an empty list, it will return an 
   * EntityEmptyFilter. Indicating the this filter, although it isSet() will not 
   * returned values.</p>
   * @return IEntityFilter<V>
   */
  @Override
  @SuppressWarnings("unchecked")
  public final EntityFilter<TBean> getFilter() {
    EntityFilter<TBean> result = null;
    try {
      if ((this.hasFilter()) && (this.isSet())) {
        List<TValue> valuesList = this.parsedSearchValues();
        if ((valuesList != null) && (!valuesList.isEmpty())) {
          if (valuesList.size() == 1) {
            TValue value = valuesList.get(0);
            if (value != null) {
              if (this.entityFilters.size() == 1) {
                EntityFilter<TBean> entFilter = this.entityFilters.get(0);
                if ((entFilter != null) && (entFilter.isValueFilter())) {
                  @SuppressWarnings("unchecked")
                  EntityValueFilter<TBean,TValue> valueFilter =
                          (EntityValueFilter<TBean,TValue>) entFilter.clone();
                  valueFilter.setValue(value);
                  valueFilter.setValues(null);
                  result = valueFilter;
                }
              } else {
                Boolean bAnd = ((this.doAnd == null) || (this.doAnd));
                EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(bAnd);
                for (EntityFilter<TBean> entFilter : this.entityFilters) {
                  if ((entFilter != null) && (entFilter.isValueFilter())) {
                    @SuppressWarnings("unchecked")
                    EntityValueFilter<TBean,TValue> valueFilter =
                            (EntityValueFilter<TBean,TValue>) entFilter.clone();
                    valueFilter.setValue(value);
                    valueFilter.setValues(null);
                    groupFilter.addFilter(valueFilter);
                  }
                }
                result = groupFilter;
              }
            }
          } else {
            if (this.entityFilters.size() == 1) {
              EntityFilter<TBean> entFilter = this.entityFilters.get(0);
              if ((entFilter != null) && (entFilter.isValueFilter())) {
                EntityValueFilter<TBean,TValue> baseFilter =
                            (EntityValueFilter<TBean,TValue>) entFilter.clone();
                baseFilter.setValue(null);
                baseFilter.setValues(null);
                if (EntityFilterEnums.isIn(baseFilter.getCondition())) {
                  baseFilter.setValues(valuesList);
                  result = baseFilter;
                } else {
                  EntityGroupFilter<TBean> recFilter = new EntityGroupFilter<>(false);
                  for (TValue value : valuesList) {
                    if (value != null) {
                      EntityValueFilter<TBean,TValue> valueFilter =
                              (EntityValueFilter<TBean,TValue>) entFilter.clone();
                      valueFilter.setValue(value);
                      recFilter.addFilter(valueFilter);
                    }
                  }
                  result = recFilter;
                }
              }
              
            } else {
              Boolean bAnd = ((this.doAnd == null) || (this.doAnd));
              EntityGroupFilter<TBean> groupFilter = new EntityGroupFilter<>(bAnd);
              for (EntityFilter<TBean> entFilter : this.entityFilters) {
                if ((entFilter != null) && (entFilter.isValueFilter())) {
                  EntityValueFilter<TBean,TValue> baseFilter =
                            (EntityValueFilter<TBean,TValue>) entFilter.clone();
                  baseFilter.setValue(null);
                  baseFilter.setValues(null);
                  if (EntityFilterEnums.isIn(baseFilter.getCondition())) {
                    baseFilter.setValues(valuesList);
                    result = baseFilter;
                  } else {
                    EntityGroupFilter<TBean> recFilter = new EntityGroupFilter<>(false);
                      for (TValue value : valuesList) {
                        if (value != null) {
                        EntityValueFilter<TBean,TValue> valueFilter =
                                (EntityValueFilter<TBean,TValue>) entFilter.clone();
                          valueFilter.setValue(value);
                          recFilter.addFilter(valueFilter);
                      }
                    }
                    groupFilter.addFilter(recFilter);
                  }
                }
              }
              result = groupFilter;
            }
          }
        } 
        
        if (result == null) {
          result = new EntityEmptyFilter<>();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Public/Abstract Methods"> 
  /**
   * Get the Filter's related Class (i.e CLass<Y>
   * @return Class<Y>
   */
  @SuppressWarnings("unchecked")
  public final Class<TValue> getValueClass() {
    return (Class<TValue>) 
            ReflectionInfo.getGenericClass(SearchFilter.class, this.getClass(), 1);
  }

  /**
   * Return the Filter ValueClass' SimpleName.
   * @return String
   */
  public final String getValueClassName() {
    Class pClass = this.getValueClass();
    return pClass.getSimpleName();
  }
  
  /**
   * CAN OVERRIDE: Override to validate that the filter is fully defined.
   * The base method returns true.
   * @return boolean.
   */
  protected boolean onHasFilter() {
    return true;
  }
  
  /**
   * Add a new EntityValueFilter to the SearchFilter's List
   * @param pFilter EntityValueFilter<V,Y>
   */
  @SuppressWarnings("unchecked")
  public final void addEntityFilter(EntityValueFilter<TBean,TValue> pFilter) {
    if (pFilter == null) {
      return;
    }
    
    try {
      if (this.entityFilters == null) {
        this.entityFilters = new ArrayList<>();
      }
      pFilter.setValue(null);
      this.entityFilters.add(pFilter);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.addEntityFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Set whether this filter represents a Column Filter that can be used to filter 
   * content in a DataGrid (Default= false)
   * @param bSet Boolean
   */
  public void setIsColumnFilter(Boolean bSet) {
    this.isColumnFilter = bSet;
  }
 
  /**
   * Set whether the SearchFilter's sub-filters should be joined by AND (bAnd=true) or OR
   * (bAnd=false). Default = AND|true.
   * @param bAnd Boolean
   */
  public final void setFilterJoinOption(Boolean bAnd) {
    this.doAnd = ((bAnd == null) || (bAnd));
  }

  /**
   * Set the StringSearchOption for for the structuring of the search Value filters.
   * Default=WHOLE
   * @param searchOption the {@linkplain StringSearchOptions}
   */
  public final void setStringSearchOption(StringSearchOptions searchOption) {    
    this.seachOptions = searchOption;
  }
  
  /**
   * Get the StringSearchOption for for the structuring of the search Value filters.
   * (Default=WHOLE)
   * @return the assigned StringSearchOptions 
   */
  protected final StringSearchOptions getStringSearchOption() {
    return (this.seachOptions == null)? StringSearchOptions.WHOLE: this.seachOptions;
  }
 
  /**
   * Set the Delimiter(s) for parsing the input SearchValue. If set to null, the input
   * value will not be split into multiple search values using a set of delimiters.
   * Default="\\s+|\\s*,\\s*|\\s*:\\s*|\\s*;\\s*"
   * @param String
   */
  public final void setSearchValueDelimiters(String sDelimiter) {
    this.delimiters = DataEntry.cleanString(sDelimiter);
  }
  
  /**
   * Get  the Delimiter(s) for parsing the input SearchValue.
   * Default="\\s+|\\s*,\\s*|\\s*:\\s*|\\s*;\\s*"
   * @return int (StringSearchEnum)
   */
  protected final String getSearchValueDelimiters() {
    return this.delimiters;
  }
  
  /**
   * Set the Search's Default Search Value. if (!null), the filter is always set and the
   * default value will apply if not search Value is set.
   * @param sValue String
   */
  public void setDefaultSearchValue(String sValue) {
    this.defaultValue = DataEntry.cleanString(sValue);
  }
  
  /**
   * Check whether the search has a Default Value
   * @return boolean
   */
  public boolean hasDefaultSearchValue() {
    return (this.defaultValue != null);
  }
 
  /**
   * Get a list "prepared" search strings. This contains the sub-strings generated by
   * splitting the search string according to the set delimeters and prepared for
   * search based on the set StringSearchOptions (WHOLE|PARTIAL|SEGEMENTS).
   * This list of string list can be used by inheritors to prepare the 
   * parsedSearchValues that is used in generating the searchFilter's EntityFilter.
   * @return List<String>
   */
  protected final List<String> getSearchValues() {
    List<String> pResult = new ArrayList<>();
    try {
      String sSearchValue = this.getCleanSearchValue();
      if (sSearchValue != null) {
        String sDelimeters = this.getSearchValueDelimiters();
        Scanner pScanner = new Scanner(sSearchValue);
        pScanner.useDelimiter(sDelimeters);
        while (pScanner.hasNext()) {
          String sNext = DataEntry.cleanString(pScanner.next());
          if ((sNext != null) && (!sNext.matches("%|_"))) {
            StringSearchOptions srchOption = this.getStringSearchOption();
            if (srchOption == StringSearchOptions.SEGMENTS) {
                if (!sNext.substring(0).matches("%|_")) {
                  sNext = "%"+sNext;
                }
                if (!sNext.substring(sNext.length()-1).matches("%|_")) {
                  sNext = sNext+"%";
                }
            } else if (srchOption == StringSearchOptions.PARTIAL) {
                if (!sNext.substring(sNext.length()-1).matches("%|_")) {
                  sNext = sNext+"%";
                }
            }
            pResult.add(sNext);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSearchValues Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * <p>Get the "clean" searchValue (i.e., after removing trailing spaces and delimeters)
   * This string can be used by inheritors to prepare the parsedSearchValues that is 
   * used in generating the searchFilter's EntityFilter.</p>
   * <p><b>NOTE:</b> If the searchValue is not set, it returns the defaultSearchValue-
   * whether it is set or not.
   * </p>
   * @return String
   */
  protected final String getCleanSearchValue() {
    return (this.searchValue == null)? this.defaultValue: this.searchValue;
  }
  
  /**
   * Return a list of parsed values based on the SearchFilter's cleanSearchValue.
   * @return List<Y>
   */
  protected abstract List<TValue> parsedSearchValues();
  //</editor-fold>   
  
  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * OVERRIDE: Return "<className>[Value=" + this.getSearchValue() + "]";
   * @return a string representation of the filter
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[Value=" + this.getSearchValue() + "]";
  }
  //</editor-fold>
}
