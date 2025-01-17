package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.enums.StringSearchOptions;
import bubblewrap.entity.filters.EntityStringFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

/**
 * The is a SearchFilter<TBean, String> for searching a String Field or String Fields 
 * using one or more EntityStringFilter filters. 
 * Set the default stringSearchOption=SEGEMENTS.
 * @author kprins
 */
public class SearchStringFilter<TBean extends Serializable> 
                                                  extends SearchFilter<TBean, String> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor passing SearchFilter's searchId. 
   * Set the default stringSearchOption=SEGEMENTS.
   */
  public SearchStringFilter(String sSearchId) {
    super(sSearchId, EntitySearchTypes.TEXTINPUT);        
    this.setStringSearchOption(StringSearchOptions.SEGMENTS);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public/Protected Methods">
  /**
   * Add a EntityStringFilter<T> to the existing or new list of filters. 
   * Ignore the call is sField=null|"". Exceptions are logged.
   * @param sField String
   * @param bDoCase Boolean (default=false)
   */
  public void addEntityFilter(String sField, Boolean bDoCase) {
    try {
      sField = DataEntry.cleanString(sField);
      if (sField != null) {
        
        EntityStringFilter<TBean> pFilter =
                new EntityStringFilter<>(sField, EntityFilterEnums.LIKE, bDoCase);
        if (pFilter == null) {
          throw new Exception("Initiating Field[" + sField + "]'s Filter failed.");
        }
        
        this.addEntityFilter(pFilter);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.addFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Overrides">
  /**
   * OVERRIDE: Return this.getSearchValues
   * @return List<String>
   */
  @Override
  public List<String> parsedSearchValues() {
    return this.getSearchValues();
  }
  //</editor-fold>
}
