package bubblewrap.core.selectors;

import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.Objects;

/**
 * A base class for the a Selectable object used by {@linkplain TableSelector} and 
 * {@linkplain MapSelector} to complex selected item types (e.g., {@linkplain 
 * EntityWrapper EntityWrapper}) as selected items for table editors or simple selection
 * menu's or list.
 * @author Charlie Lay (Modified by k.prins)
 */
public abstract class SelectObject<TValue extends Serializable> implements Serializable {
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">  
  /**
   * The select object item id
   */
  private TValue value;
  /**
   * The SelectObject's IsDisabled state - this setting will be transfered
   * to the SelectItem
   */
  private Boolean disabled;
  /**
   * The SelectObject's doEscape state (default = false) - this setting will be transfered
   * to the SelectItem
   */
  private Boolean escape;
  /**
   * The Item's ReadOnly state
   */
  private Boolean readOnly;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a unique selectable value that is wrapped by the SelectObject
   * @param value the unique selectable value (can be null if this represents the null 
   * value)
   */
  public SelectObject(TValue value) {
    super();  
    this.value = value;
    this.disabled = null;
    this.readOnly = null;
    this.escape = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * Called by inheritor's to set the Select Object's states
   * @param readOnly true if this.value is readOnly (cannot be edited) 
 (default = false)
   * @param disabled true if the SelectObject is not selectable (default = false)
   * @param escape true if the Label of the SelectObject must be escaped before rendering
   * (default = false).
   */
  protected void setState(Boolean readOnly, Boolean disabled, Boolean escape) {
    this.readOnly = ((readOnly == null) || (!readOnly))? null: readOnly;
    this.disabled = ((disabled == null) || (!disabled))? null: disabled;
    this.escape = ((escape == null) || (!escape))? null: escape;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the TValue associated with this Select Object
   * @return the assigned item (it can be null)
   */
  public final TValue getValue() {
    return this.value;
  }  
  
  /**
   * CAN OVERRIDE: Get the select option label. (default = this.item.toString() or
   * "null" if this.item = null)
   * @return an optional label for the item
   */
  public String getLabel() {
    return (this.value == null)? "null": this.value.toString();
  }
    
  /**
   * Get the disable state.
   * @return true if the SelectObejct cannot be accessed/selected
   */
  public final Boolean isNullValue() {
    return (this.value == null);
  }
    
  /**
   * Get the disable state.
   * @return true if the SelectObejct cannot be accessed/selected
   */
  public final Boolean isDisabled() {
    return ((this.disabled != null) && (this.disabled));
  }
  
  /**
   * Get the SelectItem's disabled state.
   * @return true if this.readOnly is disabled (not selectable)
   */
  public final Boolean isReadOnly() {
    return ((this.readOnly != null) && (this.readOnly));
  }
  
  /**
   * Get the SelectItem's escape setting.
   * @return true if this.value's label must be escaped when rendered.
   */
  public final Boolean isEscape() {
    return ((this.escape != null) && (this.escape));
  }
  
  /**
   * Called to get a String value that uniquely represents this item in the
   * {@linkplain TableSelector#getSelectOptions() TableSelecto.selectOptions}. It
   * calls {@linkplain #onGetSelectId() this.onGetSelectId} to get the selectId
   * @return this.onGetSelectId or "null" if this.onGetSelectId = null.
   * @throws IllegalArgumentException if the request failed
   */
  public final String getSelectId() {
    String result = null;
    try {
      result = this.onGetSelectId();
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getSelectId Error:\n " + exp.getMessage());
    }
    return (result == null)? "null": result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Called by {@linkplain #getSelectId() this.getSelectId} to get a String 
   * value that uniquely represents this item in the
   * {@linkplain TableSelector#getSelectOptions() TableSelecto.selectOptions}
   * @return a non-null unique identifier
   */
  protected abstract String onGetSelectId();
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "SelectObject[id=" + this.selectId + "; item=" + this.getLabel() + "]"</p>
   */
  @Override
  public String toString() {
    return "SelectObject[id=" + this.getSelectId() + "; item=" + this.getLabel() + "]";
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return true of obj != null and instance of SelectObject and the
   * selectId's match.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof SelectObject));
    if (result) {
      SelectObject other = (SelectObject) obj;
      result = DataEntry.isEq(this.getSelectId(), other.getSelectId(), true);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p> OVERRIDE: Return a HashCode from this.value</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 67 * hash + Objects.hashCode(this.value);
    return hash;
  }
  // </editor-fold>
}
