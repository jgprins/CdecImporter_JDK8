package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.enums.StringSearchOptions;
import bubblewrap.entity.filters.EntityBoolFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The is a SearchFilter<TBean,Boolean> for searching a Boolean Field or Boolean Fields 
 * using one or more EntityBooleanFilter filters. 
 * Set the default stringSearchOption=WHOLE.
 * @author kprins
 */
public class SearchBooleanFilter<TBean extends Serializable> 
                                                extends SearchFilter<TBean,Boolean> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor passing SearchFilter's searchId. 
   */
  public SearchBooleanFilter(String sSearchId) {
    super(sSearchId, EntitySearchTypes.BOOLEAN);    
    this.setStringSearchOption(StringSearchOptions.WHOLE);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Proteced Methods">
  /**
   * Add a EntityNumberFilter<T,Integer> to the existing or new list of filters. 
   * Ignore the call is sField=null|"". Exceptions are logged.
   * @param sField String
   * @param bDoCase Boolean (default=false)
   */
  public void addEntityFilter(String sField) {
    try {
      sField = DataEntry.cleanString(sField);
      if (sField != null) {
        
        EntityBoolFilter<TBean> pFilter =
                               new EntityBoolFilter<>(sField, EntityFilterEnums.EQUAL);
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
  
  //<editor-fold defaultstate="collapsed" desc="SearchValue Overrides">
  /**
   * OVERRIDE: Get the searchValues and convert it to a list of Boolean. If the 
   * searchValue is set, a single Boolean value will be added to list. The value is set
   * to true if searchValue="true"|"yes"|"1"|"-1". The string compare is case insensitive.
   * @return List<Boolean>
   */
  @Override
  protected List<Boolean> parsedSearchValues() {
    List<Boolean> pResult = null;
    try {
      String sValue = this.getCleanSearchValue();
      if (sValue != null) {
        Boolean bValue = ((sValue.equalsIgnoreCase("true")) 
                          || (sValue.equalsIgnoreCase("yes"))
                          || (sValue.equalsIgnoreCase("1"))
                          || (sValue.equalsIgnoreCase("-1")));
        pResult = new ArrayList<>();
        pResult.add(bValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parsedSearchValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>  
}
