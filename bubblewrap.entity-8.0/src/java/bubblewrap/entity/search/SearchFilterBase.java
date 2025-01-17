package bubblewrap.entity.search;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventHandler;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.filters.EntityFilter;
import java.io.Serializable;

/**
 * The Generic Base class of all SearchFilter with an Entity class reference, but no
 * reference to the SearchValue type. Note. The SearchValue is always passes in as 
 * String and it is up to the Custom SearchFilter to parse this sting into a search 
 * value.
 * @author kprins
 */
public abstract class SearchFilterBase<TBean extends Serializable> 
                                                  implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Event Senders">
  /**
   * EventHdnler for sending a Search Changed event.
   */
  public final EventHandler SearchChanged;
  /**
   * Method called to fie the Search Changed event.
   */
  protected void fireSearchChanged() {
    this.SearchChanged.fireEvent(this, new EventArgs());
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected SearchFilterBase() {
    this.SearchChanged = new EventHandler();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * Get the SearchFilter's SearchId
   * @return String
   */
  public abstract String getSearchId();
  
  /**
   * Get the SearchFilters Entity Search Type
   * @return the assigned {@linkplain EntitySearchTypes}
   */
  public abstract EntitySearchTypes getSearchType();
  
  /**
   * Check whether the Search Filter has EntityFilter(s)
   * @return boolean
   */
  public abstract boolean hasFilter();
  
  /**
   * Check whether the SearchFilter's searchValue is set (not null)
   * @return boolean
   */
  public abstract boolean isSet();
  
  /**
   * Return true if this filter is for filtering columns in the DataGrid
   * @return boolean
   */
  public abstract boolean isColumnFilter();
  
  /**
   * Get the searchValue
   * @return String
   */
  public abstract String getSearchValue();
  
  /**
   * Set the searchValue
   * @param pValue String
   */
  public abstract void setSearchValue(String pValue);
  
  /**
   * Clear the searchValue
   */
  public abstract void clearFilter();
  
  /**
   * Get a updated filter with the searchValue set
   * @return EntityFilter<V>
   */
  public abstract EntityFilter<TBean> getFilter();
  //</editor-fold>
}
