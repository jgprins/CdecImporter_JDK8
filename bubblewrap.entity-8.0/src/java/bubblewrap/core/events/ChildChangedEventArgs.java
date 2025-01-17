package bubblewrap.core.events;

import java.io.Serializable;

/**
 * An EventArgs to notify the a field has changed value. Its supports a Field name,
 * oldValue and newValue properties. 
 * It is used by the {@linkplain FieldChangedEventHandler}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class ChildChangedEventArgs extends EventArgs {
  
  private Class<? extends Serializable> childClass;
  private Object childId;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ChildChangedEventArgs(Object childId, 
                                            Class<? extends Serializable> childClass) {
    super();    
    this.childId = childId;
    this.childClass = childClass;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get the name of the field that changed
   * @param TId any class
   * @return the child's recordId cast as TId
   */
  @SuppressWarnings("unchecked")
  public <TId> TId getChildId() {
    return (TId) this.childId;
  }
  
  /**
   * Check if the clazz matches this EventArgs's child (entity) Class
   * @param clazz the entity class to check against the assigned child class
   * @return true if it is a match.
   */
  public boolean isChildClass(Class<? extends Serializable> clazz) {
    return ((clazz != null) && (this.childClass != null) &&
            (this.childClass.equals(clazz)));
  }
  //</editor-fold>
}
