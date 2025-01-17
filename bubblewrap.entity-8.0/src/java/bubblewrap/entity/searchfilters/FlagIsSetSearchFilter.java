package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityBoolFilter;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * A {@linkplain SearchCustomFilter}[sSearchId] on Field[sFlagField] - Boolean Field. 
 * If it currentSearchValue=true, it filters on Field[sFlagField]=true. Otherwise, 
 * the filter is ignored. The deafaultSearchValue=false.
 * @author kprins
 */
public class FlagIsSetSearchFilter<TBean extends Serializable> 
                                                  extends SearchCustomFilter<TBean> {
  
  /**
   * Name of the Boolean Field to filter on
   */
  private String msFlagField;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public FlagIsSetSearchFilter(String sSearchId, String sFlagField) {
    super(sSearchId);    
    sFlagField = DataEntry.cleanString(sFlagField);
    if (sFlagField == null) {
      throw new NullPointerException("The Flag Field's name cannot be unassigned.");
    }
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
   * {@inheritDoc} <p>OVERRIDE: Return a EntityFilter on the Field[this.msFlagField] if 
   * TBean supports this field (i.e., it is a Declared Field of the class). If the
   * currentSeachValue=true (default=false), only records where this Flag is set will be
   * filtered. Otherwise, this method returns null to ignore the filter.
   * </p>
   */
  @Override
  protected EntityFilter<TBean> onGetFilter() {
    EntityFilter<TBean> pResult = null;
    try {
      if (this.getCurrentSearchValue()) {
        pResult = new EntityBoolFilter<>(this.msFlagField,true,EntityFilterEnums.EQUAL);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>  
}
