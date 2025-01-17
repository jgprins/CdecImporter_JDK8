package bubblewrap.core.selectors;

import bubblewrap.io.DataEntry;
import java.util.Arrays;
import java.util.List;
import javax.faces.model.SelectItem;
/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EnumSelector<TItem extends Enum<TItem>> extends Selector<TItem> {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Field">
  /**
   * The Selector's Enum Class.
   */
  public final Class<TItem> enumClass;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * The currently selected Options
   */
  private TItem selectedItem;
  /**
   * The cashed list of options.
   */
  private List<TItem> valuesOf;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor with a EnumCalue reference
   * @param enumClass the enum class.
   */
  public EnumSelector(Class<TItem> enumClass) {
    super();
    this.enumClass = enumClass;
    if (enumClass == null) {
      throw new NullPointerException("The EnumSelector's EnumClass cannot be "
              + "unassigned.");
    }
    this.selectedItem = null;
    this.valuesOf = null;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.selectedItem = null;
    this.valuesOf = null;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Call to validate that the current selection has a valid recordId - i.e. it
   * represents a selectable record.
   * @return the current recordId or null if no matching selectable record was found.
   */
  protected final TItem getSelectedValue(String selectId) {
    TItem result = null;
    int ordVal = -1;
    if ((selectId != null) && ((ordVal = DataEntry.parseInt(selectId, -1)) >= 0)) {
      List<TItem> allSelectItems = this.onGetSelectionItems();
      if ((allSelectItems != null) && (!allSelectItems.isEmpty())) {
        for (TItem selectItem : allSelectItems) {
          if (this.onIsSelectedValue(selectItem, ordVal)) {
            result = selectItem;
            break;
          }
        }
      }
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Set the currently selected Item. Call {@linkplain #setSelectId(java.lang.String)
   * this.selectId} with the selectId=selectItme.ordinal or null if selectedItme=null.
   * @param selectedItem the new selectedItem
   */
  public void setSelectedItem(TItem selectedItem) {
    String ordStr =
            (selectedItem == null)? null: Integer.toString(selectedItem.ordinal());
    this.setSelectId(ordStr);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override Selector">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  public TItem getSelectedItem () {
    return this.selectedItem;
  }
  
  /**
   * CAN OVERRIDE: called by {@linkplain #getSelectedValue(java.lang.String) 
   * this.getSelectedValue(selectId)}, after converting the selectId to ordVal (integer)
   * to test whether a <tt>selectItem</tt> in {@linkplain #onGetSelectionItems() 
   * this.selectionItems} matches the new selected <tt>ordVal</tt> 
   * @param selectItem the selection item to match
   * @param ordVal the ordinal value to match
   * @return the base method return
   * ((selectItem != null) && (selectItem.ordinal() == ordVal))
   */
  protected boolean onIsSelectedValue(TItem selectItem, int ordVal) {
    return ((selectItem != null) && (selectItem.ordinal() == ordVal));
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return new SelectItem(selectOption.ordinal(), selectOption.name())
   * </p>
   */
  @Override
  protected SelectItem newSelectOption(TItem selectOption) {
    SelectItem result = null;
    if (selectOption != null) {
      result = new SelectItem(selectOption.ordinal(), selectOption.name());
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Check if the selectId (assumed to be the enum.ordinal value) can be 
   * converted to an enum (calling {@linkplain #getSelectedValue(java.lang.String) 
   * this.getSelectedValue(selectId)}) and if successful, return selectId, else return
   * null.</p>
   */
  @Override
  protected final String onChangeSelection(String selectId) {
    String result = selectId;
    try {
      this.selectedItem = this.getSelectedValue(selectId);
      if (this.selectedItem == null) {
        if (!this.isNull()) {
          result = null;
        }
      }
    } catch (Exception exp) {
      result = null;
      this.selectedItem = null;
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Override and make final to return a cached list of all enum options.
   * To initiate the list it calls {@linkplain #onGetValuesOf() this.onGetValuesOf()}
   * </p>
   */
  @Override
  protected final List<TItem> onGetSelectionItems() {
    TItem[] enumArr = null;
    if ((this.valuesOf == null) && (this.enumClass != null) &&
            ((enumArr = this.enumClass.getEnumConstants()) != null) &&
            (enumArr.length > 0)) {
      this.valuesOf = Arrays.asList(enumArr);
    }
    return this.valuesOf;
  }
  //</editor-fold>
}
