package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.*;
import bubblewrap.entity.filters.EntityNumberFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * The is a SearchFilter for searching an Integer Field or Integer Fields using one
 * or more EntityNumberFilter filters. Set the default stringSearchOption=WHOLE.
 * @author kprins
 */
public class SearchIntegerFilter<V extends Serializable> 
                                        extends SearchFilter<V,Integer> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor passing SearchFilter's searchId. 
   */
  public SearchIntegerFilter(String sSearchId) {
    super(sSearchId, EntitySearchTypes.TEXTINPUT);    
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
        
        EntityNumberFilter<V,Integer> pFilter =
                new EntityNumberFilter<V,Integer>(sField, EntityFilterEnums.EQUAL);
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
   * OVERRIDE: Get the searchValues and convert it to a list of integers ignoring and
   * null or invalid Integer numbers.
   * @return
   */
  @Override
  protected List<Integer> parsedSearchValues() {
    List<Integer> pResult = null;
    try {
      List<String> pSearchValues = this.getSearchValues();
      if ((pSearchValues != null) && (!pSearchValues.isEmpty())) {
        for (String sValue : pSearchValues) {
          if (sValue != null) {
            Integer iValue = null;
            try {
              iValue = Integer.parseInt(sValue);
            } catch (NumberFormatException pExp) {
              iValue = null;
            }
            if (iValue != null) {
              if (pResult == null) {
                pResult = new ArrayList<Integer>();
              }
              pResult.add(iValue);
            }
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parsedSearchValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>  
 }
