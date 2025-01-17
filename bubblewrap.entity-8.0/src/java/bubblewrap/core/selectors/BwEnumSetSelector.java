package bubblewrap.core.selectors;

import bubblewrap.core.enums.BwEnum;
import bubblewrap.core.enums.BwEnumSet;
import bubblewrap.io.DataEntry;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * A MultiSelector for a generic BwEnum class
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwEnumSetSelector<TEnum extends BwEnum<TEnum>> 
                                                        extends MultiSelector<TEnum> {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Field">
  /**
   * The Selector's Enum Class.
   */
  public final Class<? extends TEnum> enumClass;
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
  public BwEnumSetSelector(Class<? extends TEnum> enumClass) {
    super();
    this.enumClass = enumClass;
    if (enumClass == null) {
      throw new NullPointerException("The BwEnumMultiSelector's enumClass cannot be "
              + "unassigned.");
    }
    this.valuesOf = null;
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
   * @return the EnumSet - the set is empty is no items are selected.
   */
  public BwEnumSet<TEnum> getEnumSet() {
    BwEnumSet<TEnum> result = BwEnumSet.noneOf(this.enumClass);
    List<TEnum> selectItems = this.getSelectItems();
    if ((selectItems != null) && (!selectItems.isEmpty())) {
      for (TEnum item : selectItems) {
        result.add(item);
      }
    }
    return result;
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
          if (options.value == ordVal) {
            result = options;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return the selectionOption.ordinal as a string or null is
   * selectionOption = null.</p>
   */
  @Override
  protected String toSelectId(TEnum selectOption) {
    return (selectOption == null)? null: Integer.toString(selectOption.value);
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
      result = new SelectItem(selectOption.value, selectOption.name);
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
    if ((this.valuesOf == null) && (this.enumClass != null)) {
      this.valuesOf = BwEnum.getEnumValues(this.enumClass);
    }
    return this.valuesOf;
  }
  //</editor-fold>
}
