package bubblewrap.core.selectors;

import bubblewrap.io.DataEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * A MultiSelector for a generic Enum class
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EnumSetSelector<TEnum extends Enum<TEnum>> extends MultiSelector<TEnum> {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Field">
  /**
   * The Selector's Enum Class.
   */
  public final Class<TEnum> enumClass;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * The cashed list of options.
   */
  private List<TEnum> valuesOf;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public EnumSetSelector(Class<TEnum> enumClass, String caption, boolean selectAll) {
    super();
    this.enumClass = enumClass;
    if (enumClass == null) {
      throw new NullPointerException("The EnumMultiSelector's EnumClass cannot be "
              + "unassigned.");
    }
    this.valuesOf = null;
    this.initBaseSelector(caption, selectAll);
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.valuesOf = null;
  }  
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the EnumSet that contains the selection Items
   * @return the EnumSet - the set is empty if no items are selected.
   */
  public final EnumSet<TEnum> getEnumSet() {
    EnumSet<TEnum> result = EnumSet.noneOf(this.enumClass);
    List<TEnum> selectItems = this.getSelectItems();
    if ((selectItems != null) && (!selectItems.isEmpty())) {
      for (TEnum item : selectItems) {
        result.add(item);
      }
    }
    return result;
  }
  
  /**
   * Assign the values in <tt>enumSet</tt> as the new selection
   * @param enumSet the new selected values (can be null to clear selection)
   */
  public final void setEnumSet(EnumSet<TEnum> enumSet) {
    List<String> selection = new ArrayList<>();
    if ((enumSet != null) && (!enumSet.isEmpty())) {
      for (TEnum enuVal : enumSet) {
        String selectId = this.toSelectId(enuVal);
        if (selectId != null) {
          selection.add(selectId);
        }
      }
    }
    this.setSelection(selection);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override MultiSelector">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Get the selected Enum for the selectedId - assumed to be the Enum's
   * ordinal value</p>
   */
  @Override
  protected TEnum toSelectOption(String selectId) {
    TEnum result = null;
    int ordVal = -1;
    if ((selectId != null) && ((ordVal = DataEntry.parseInt(selectId, -1)) >= 0)) {
      List<TEnum> allOptions = this.onGetSelectionOptions();
      if ((allOptions != null) && (!allOptions.isEmpty())) {
        for (TEnum options : allOptions) {
          if (this.onIsSelectedValue(options, ordVal)) {
            result = options;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * CAN OVERRIDE: called by {@linkplain #getSelectedValue(java.lang.String) 
   * this.getSelectedValue(selectId)}, after converting the selectId to ordVal (integer)
   * to test whether a <tt>selectItem</tt> in {@linkplain #onGetSelectionItems() 
   * this.selectionItems} matches the new selected <tt>ordVal</tt> 
   * @param options the selection item to match
   * @param ordVal the ordinal value to match
   * @return the base method return
   * ((options != null) && (options.ordinal() == ordVal))
   */
  protected boolean onIsSelectedValue(TEnum options, int ordVal) {
    return ((options != null) && (options.ordinal() == ordVal));
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return the selectionOption.ordinal as a string or null is
   * selectionOption = null.</p>
   */
  @Override
  protected String toSelectId(TEnum selectOption) {
    return (selectOption == null)? null: Integer.toString(selectOption.ordinal());
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return new SelectItem(selectOption.ordinal(), selectOption.name())
   * </p>
   */
  @Override
  protected SelectItem newSelectItem(TEnum selectOption) {
    SelectItem result = null;
    if (selectOption != null) {
      result = new SelectItem(selectOption.ordinal(), selectOption.name());
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
  protected List<TEnum> onGetSelectionOptions() {
    TEnum[] enumArr = null;
    if ((this.valuesOf == null) && (this.enumClass != null) &&
            ((enumArr = this.enumClass.getEnumConstants()) != null) &&
            (enumArr.length > 0)) {
      this.valuesOf = Arrays.asList(enumArr);
    }
    return this.valuesOf;
  }
  //</editor-fold>
}
