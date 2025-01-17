package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.enums.SearchIds;
import bubblewrap.entity.filters.EntityBoolFilter;
import bubblewrap.entity.filters.EntityFilter;
import java.io.Serializable;
import java.util.logging.Level;
/**
 * A {@linkplain SearchCustomFilter}[SearchIds.HideSystemItem] on Field[systemItem]. 
 * If it currentSearchValue=true, it filters on Field[systemItem]=false. Otherwise, 
 * the filter is ignored. The deafaultSearchValue=false.
 * @author kprins
 */
public class HideSystemItemStaticFilter<TBean extends Serializable> 
                                                  extends SearchStaticFilter<TBean> {
  // <editor-fold defaultstate="collapsed" desc="Constructor">

  /**
   * Public Constructor
   */
  public HideSystemItemStaticFilter() {
    super();    
    this.setDefaultSearchValue(false);
    this.setIsColumnFilter(false);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="SearchCustomFilter Override">
  @Override
  public boolean hasFilter() {
    return true;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return a EntityFilter on the Field[systemItem] if TBean
   * has a support this field (i.e., it is a Declared Field of the class). If the
   * currentSeachValue=true (default=false), only non-systemItem records will be
   * filtered. Otherwise, this method returns null to ignore the filter.
   * </p>
   */
  @Override
  protected EntityFilter<TBean> onGetFilter() {
    EntityFilter<TBean> result = null;
    try {
      if (this.getCurrentSearchValue()) {
        result = new EntityBoolFilter<>("systemItem",false,EntityFilterEnums.EQUAL);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>  
}
