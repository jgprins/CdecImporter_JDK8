package bubblewrap.core.events;

import bubblewrap.io.DataEntry;

/**
 * A CancelEventArgs to notify the a field is about to changed value and to allow 
 * listeners to cancel request. Its supports a Field name, oldValue and newValue 
 * properties. 
 * It is used by the {@linkplain FieldChangingEventHandler}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class FieldChangingEventArgs extends CancelEventArgs {
  
  /**
   * The name of the field that will be changed
   */
  private String fieldName;
  /**
   * The old/current value
   */
  private Object oldValue;
  /**
   * The proposed/new value
   */
  private Object newValue;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public FieldChangingEventArgs(String fieldName, Object oldValue, Object newValue) {
    super();    
    this.fieldName = DataEntry.cleanString(fieldName);
    this.oldValue = oldValue;
    this.newValue = newValue;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get the name of the field that changed
   * @return
   */
  public String getFieldName() {
    return this.fieldName;
  }
  
  /**
   * Check if the otherName matches this EventInfo's assigned FieldName
   * @param otherName the name to check against this.fieldName
   * @return true if it is a match.
   */
  public boolean isField(String otherName) {
    return ((this.fieldName != null) &&
            (DataEntry.isEq(this.fieldName, otherName, true)));
  }
  
  /**
   * Get the field's old value
   * @return the assigned old value (can be null)
   */
  public Object getOldValue() {
    return this.oldValue;
  }
  
  /**
   * Get the field's new value
   * @return the assigned new value (can be null)
   */
  public Object getNewValue() {
    return this.newValue;
  }
  //</editor-fold>
}
