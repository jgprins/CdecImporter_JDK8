package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityNumberFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;

/**
 * SearchIntegerOptionFilter is a SearchFilter that allows the user make a selection from
 * a predefined list of selection options. It returns an EntityFilter that filters records
 * based on the selection option values.
 * @author kprins 
 */
public class SearchIntegerOptionFilter<TBean extends Serializable> 
                                           extends SearchOptionsFilter<TBean, Integer> {
  // <editor-fold defaultstate="collapsed" desc="Constructor">

  /**
   * Public Constructor  
   */
  public SearchIntegerOptionFilter(String searchId, boolean allowMultiSelect) {
    super(searchId,allowMultiSelect);        
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Proteced Methods">
  /**
   * Add a EntityNumberFilter<T,Integer> to the existing or new list of filters. 
   * Ignore the call is sField=null|"". Exceptions are logged.
   * @param fieldName String
   * @param bDoCase Boolean (default=false)
   */
  public void addEntityFilter(String fieldName) {
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        
        EntityNumberFilter<TBean,Integer> pFilter =
                new EntityNumberFilter<>(fieldName, EntityFilterEnums.EQUAL);
        if (pFilter == null) {
          throw new Exception("Initiating Field[" + fieldName + "]'s Filter failed.");
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
   * {@inheritDoc }
   * <p>OVERRIDE: Return a list of integers that represents to user selection values.
   * Return null if no values are found.</p>
   */
  @Override
  protected List<Integer> parsedSearchValues() {
    List<Integer> result = null;
    try {
      List<SelectItem> selections = this.getSelections();
      if ((selections != null) && (!selections.isEmpty())) {
        for (SelectItem item : selections) {
          Object pValue = (item.getValue());
          Integer intVal = null;
          try {
            intVal = Integer.parseInt(pValue.toString());
          } catch (NumberFormatException exp) {
            intVal = null;
          }
          if (intVal != null) {
            if (result == null) {
              result = new ArrayList<>();
            }
            result.add(intVal);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parsedSearchValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>  
}
