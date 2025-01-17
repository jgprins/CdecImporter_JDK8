package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.SearchIds;
import java.io.Serializable;

/**
 * The SearchStaticFilter extends the SearchCustomFilter by adding a parameterless
 * constructor, which call the super constructor with SearchId=SearchIds.StaticFilter.
 * This call is used to assign a static constructor to Entity- and RecordsetSearches.
 * <p><b>NOTE:</b> A SearchStaticFilter has two default setting defaultSearchValue=true 
 * and isColumnFilter=false. These settings cannot be changed. It is set by the 
 * constructor and the setDefaultSearchValue and setIsColumnFilter are overridden to 
 * prevent changes to these settings.</p>
 * @author kprins
 */
public abstract class SearchStaticFilter <TBean extends Serializable> 
                                                    extends SearchCustomFilter<TBean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  protected SearchStaticFilter() {
    super(SearchIds.StaticFilter);    
    super.setDefaultSearchValue(true);
    super.setIsColumnFilter(false);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="SearchCustomFilter Overrides">
  /**
   * <p>OVERRIDE: Does nothing - It prevents resetting the default value - it is set to
   * true by the constructor.</p>
   * @param bValue - no effect
   */
  @Override
  public final void setDefaultSearchValue(boolean bValue) {
  }  
  
  /**
   * <p>OVERRIDE: Does nothing - It prevents resetting the default value - it is set to
   * false by the constructor.</p>
   * @param bSet - no effect
   */
  @Override
  protected void setIsColumnFilter(Boolean bSet) {
  }
  //</editor-fold>
}
