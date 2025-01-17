package bubblewrap.entity.search;

import bubblewrap.core.selectors.SelectObject;
import bubblewrap.entity.core.EntityWrapper;

/**
 * A SelectObject that wraps a {@linkplain EntityWrapper}. On initiation is sets the 
 * SelectObject's {@linkplain #setState(java.lang.Boolean, java.lang.Boolean) state} 
 * based on the EntityWrapper's {@linkplain EntityWrapper#allowEdits() allowEdits} and
 * {@linkplain EntityWrapper#getDisabled() getDisabled} states, respectively.
 * <p>
 * By default the SelectObject's {@linkplain #getLabel() Label} is set as the 
 * EntityWrapper's {@linkplain EntityWrapper#getRecordName() recordName} and its selectId
 * as EntityWrapper's {@linkplain EntityWrapper#getRecordId() recordId} as a string.
 * @author Charlie Lay
 */
public class EntitySelectOption<TItem extends EntityWrapper> extends SelectObject<TItem> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntitySelectOption(TItem item) {
    super(item);
    if (item == null) {
      this.setState(true, true, false);
    } else {
      this.setState((!item.allowEdits()), item.getDisabled(), false);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override SelectObject">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (this.selectItem == null)? "null": this.selectItem.recordName</p>
   */
  @Override
  public String getLabel() {
    TItem item = this.getValue();
    return (item == null)? "null": item.getRecordName();
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return "null" if this.selectItem = null; "new" if this.selectItem.isNew;
   * else this.selectItem.recordId.toString</p>
   */
  @Override
  protected String onGetSelectId() {
    String result = null;
    TItem item = this.getValue();
    if (item == null) {
      result = "null";
    } else if (item.isNew()) {
      result = "new";
    } else {
      result = item.getRecordId().toString();
    }
    return result;
  }
  // </editor-fold>
}
