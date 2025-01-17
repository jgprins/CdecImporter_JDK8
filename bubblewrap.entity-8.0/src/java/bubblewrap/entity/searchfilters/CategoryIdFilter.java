package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.SearchIds;
import java.io.Serializable;

/**
 * A SearchIntegerFilter on the Field[categoryId] with a searchId={@linkplain 
 * SearchIds#Category SearchIds.Category}
 * @author kprins
 */
public class CategoryIdFilter<TBean extends Serializable> 
                                                  extends SearchIntegerFilter<TBean> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public CategoryIdFilter() {
    super(SearchIds.Category); 
    this.addEntityFilter("categoryId");
    this.isColumnFilter();
    this.setDefaultSearchValue(null);
  }
  // </editor-fold>
}
