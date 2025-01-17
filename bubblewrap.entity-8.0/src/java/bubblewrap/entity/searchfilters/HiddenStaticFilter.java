package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityBoolFilter;
import bubblewrap.entity.filters.EntityFilter;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * A SearchStaticFilter on Field[hidden]. If set it filters on Field[hidden]=false. 
 * Otherwise, the filter is ignored.
 * @author kprins
 */
public class HiddenStaticFilter<TBean extends Serializable> 
                                                    extends SearchStaticFilter<TBean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public HiddenStaticFilter() {
    super(); 
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="SearchStaticFilter Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return true</p>
   */
  @Override
  public boolean hasFilter() {
    return true;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Return a EntityFilter on the Field[hidden] if TBean
   * has a support this field (i.e., it is a Declared Field of the class). If the
   * currentSeachValue=true (the default), only non-hidden records will be filtered.
   * Otherwise, this method returns null to ignore the hidden filter.
   * </p>
   */
  @Override
  protected EntityFilter<TBean> onGetFilter() {
    EntityFilter<TBean> bResult = null;
    try {
      if (this.getCurrentSearchValue()) {
        bResult = new EntityBoolFilter<>("hidden",false,EntityFilterEnums.EQUAL);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return bResult;
  }
  //</editor-fold>
}
