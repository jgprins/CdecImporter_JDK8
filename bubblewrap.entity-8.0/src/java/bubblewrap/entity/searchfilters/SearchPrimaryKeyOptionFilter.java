package bubblewrap.entity.searchfilters;

import bubblewrap.entity.core.EntityFacade;
import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityValueFilter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;

/**
 *
 * @author kprins
 */
public class SearchPrimaryKeyOptionFilter<TBean extends Serializable, 
            TSearchBean extends Serializable> 
            extends SearchOptionsFilter<TBean, TSearchBean> {
  
  /**
   * Placeholder for facade used in locating the selected entities.
   */
  private final EntityFacade entityFacade;

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SearchPrimaryKeyOptionFilter(String searchId, EntityFacade facade, 
                                                            boolean allowMultiSelect) {
    super(searchId, allowMultiSelect);    
    if (facade == null) {
      throw new NullPointerException("The Entity SearchFilter's Facade is unassigned.");
    } else {
      Class pValueClass = this.getValueClass();
      if ((pValueClass != null) && (!pValueClass.equals(facade.getEntityClass()))) {
        throw new NullPointerException("Invalid Facade. Facade[" 
                + facade.getClass().getSimpleName() +"] does not support EntityClass["
                + pValueClass.getSimpleName() + "].");
      }
    }
    this.entityFacade = facade;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Add a EntityValueFilter<V,Y> to the existing or new list of filters.
   * Ignore the call is sField=null|"". Exceptions are logged.
   * @param fieldName String
   */
  public void addEntityFilter(String fieldName) {
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        
        EntityValueFilter<TBean,TSearchBean> pFilter =
                            new EntityValueFilter<>(fieldName, EntityFilterEnums.EQUAL);
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
   * <p>OVERRIDE: Use the Filter's set Facade to locate the Entity Bean using the user
   * selection's SelectionItem.value. Return null is no value are available.</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected List<TSearchBean> parsedSearchValues() {
    List<TSearchBean> pResult = null;
    try {
      List<SelectItem> pSelection = this.getSelections();
      if ((this.entityFacade != null) &&
              (pSelection != null) && (!pSelection.isEmpty())) {
        for (SelectItem pItem : pSelection) {
          Object pValue = (pItem.getValue());
          if (pValue == null) {
            continue;
          }
          
          TSearchBean pBean = null;
          try {
            pBean = (TSearchBean) this.entityFacade.find(pValue);
          } catch (NumberFormatException pExp) {
            pBean = null;
          }
          
          if (pBean != null) {
            if (pResult == null) {
              pResult = new ArrayList<>();
            }
            pResult.add(pBean);
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
