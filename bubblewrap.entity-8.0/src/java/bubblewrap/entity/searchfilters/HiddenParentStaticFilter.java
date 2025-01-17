package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityBoolFilter;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.entity.filters.EntityJoinValueFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * A SearchStaticFilter on a Parent Entity[TParent]'s Field[hidden]. If set it filters 
 * on Field[hidden]=false. Otherwise, the filter is ignored.
 * @see #onGetFilter() 
 * @author kprins
 */
public class HiddenParentStaticFilter<TBean extends Serializable, 
                  TParent extends Serializable> extends SearchStaticFilter<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Child-Parent join field.
   */
  private String msJoinField;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public HiddenParentStaticFilter(String sJoinField) {
    super(); 
    sJoinField = DataEntry.cleanString(sJoinField);
    if (sJoinField == null) {
      throw new NullPointerException("The Joidn Field name cannot be unassigned.");
    }
    this.msJoinField = sJoinField;
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
   * {@inheritDoc} <p>OVERRIDE: Return a EntityJoinValueFilter on the 
   * JoinField[this.msJoinField} and EntityBoolFilter(field=hidden,value=false) 
   * assuming the if TBean has a support this msJoinField and the TParent support 
   * Field[hidden] (i.e., it is a Declared Field of the class).</p> 
   * <p>If the currentSeachValue=true (the default), only non-hidden records will be 
   * filtered. Otherwise, this method returns null to ignore the hidden filter.
   * </p>
   */
  @Override
  protected EntityFilter<TBean> onGetFilter() {
    EntityJoinValueFilter<TBean,TParent,Boolean> bResult = null;
    try {
      if (this.getCurrentSearchValue()) {
        EntityBoolFilter<TParent> pFilter = 
                      new EntityBoolFilter<>("hidden",false,EntityFilterEnums.EQUAL);
        bResult = new EntityJoinValueFilter<>(this.msJoinField, pFilter);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return bResult;
  }
  //</editor-fold>
}
