package bubblewrap.entity.searchfilters;

import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.enums.StringSearchOptions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.faces.model.SelectItem;

/**
 *
 * @author kprins
 */
public abstract class SearchOptionsFilter<V extends Serializable, Y> 
                                  extends SearchFilter<V, Y> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Filter's unique list of select options
   */
  private List<SelectItem> selectOptions;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public SearchOptionsFilter(String sSearchId, boolean bMultiSelect) {
    super(sSearchId,
          ((bMultiSelect)? EntitySearchTypes.MULTISELECT: EntitySearchTypes.SINGLESELECT));    
    this.setStringSearchOption(StringSearchOptions.WHOLE);
    this.selectOptions = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Protected/Public Methods">
  /**
   * Get the Filter's SelectOptions
   * @param options a list of {@linkplain SelectItem}
   */
  protected void setSelectOptions(List<SelectItem> options) {
    this.selectOptions = options;
  }
  
  /**
   * Get a list of the assigned SelectOptions
   * @return a list of {@linkplain SelectItem}
   */
  public List<SelectItem> getSelectOptions() {
    return this.selectOptions;
  }
  
  /**
   * Get of selected SelectOptions that are selected by the user.  This list should be
   * user in liu of this.getSearchValues in implementing method parsedSearchValues.
   * @return List<SelectItem>
   */
  protected List<SelectItem> getSelections() {
    List<SelectItem> result = new ArrayList<>();
    try {
      List<String> pSearchValues = this.getSearchValues();
      if ((pSearchValues != null) && (!pSearchValues.isEmpty())) {
        for (String sValue : pSearchValues) {
          if (sValue == null) {
            continue;
          }
          for (SelectItem pOption : this.selectOptions) {
            Object pOptValue = (pOption == null)? null: pOption.getValue();
            String sOptValue = (pOptValue == null)? null: pOptValue.toString();
            if ((sOptValue != null) && (sValue.equalsIgnoreCase(sOptValue))) {
              result.add(pOption);
              break;
            }
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSelections Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true if the Filter's Selection Options are set.</p>
   */
  @Override
  protected boolean onHasFilter() {
    return ((this.selectOptions != null) && (!this.selectOptions.isEmpty()));
  }
  //</editor-fold>    
}
