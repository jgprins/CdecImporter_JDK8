package bubblewrap.entity.validators;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author kprins
 */
public class FieldConstaintSet implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="private class Constraint">
  /**
   * Public class containing the Constraint attributes
   */
  public class Constraint implements Serializable {
    public final FieldContraintTypes constraintType;
    public final String message;
    private Object options;
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public Constraint(FieldContraintTypes constraintType, String message) {
      this.constraintType = constraintType;
      this.message = message;
    }
    //</editor-fold>
    
    /**
     * Set additional options associated with the constraint
     * @param <TObj> type object
     * @param options the constraints evaluation options
     */
    public <TObj> void setOptions(TObj options) {
      this.options = options;
    }
    
    /**
     * Get the additional options associated with the  constraint.
     * @param <TObj> of type object
     * @return the assigned value
     */
    @SuppressWarnings("unchecked")
    public <TObj> TObj getOptions() {
      TObj result = (TObj) this.options;
      return result;
    }
  }
  //</editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Private HashMap containing the assigned Constraints.
   */
  private HashMap<FieldContraintTypes, Constraint> constraints;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public FieldConstaintSet() {
    this.constraints = new HashMap<>();            
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  public boolean isEmpty() {
    return this.constraints.isEmpty();
  }
  
  public boolean contains(FieldContraintTypes key) {
    return this.constraints.containsKey(key);
  }
  
  /**
   * Add a new constraint with a Validation message. If the constraint already exist,
   * update the message and return the existing constraint. Otherwise, initiate a new
   * Constraint, assign it to the internal HashMap, and return the instance.
   * @param constraintType the constraint's {@linkplain FieldContraintTypes FieldContraintType}
   * @param message the constraint's validation message
   * @return the existing or new instance.
   */
  public Constraint add(FieldContraintTypes type, String message) {
    Constraint result = null;
    if (type != null) {
      if (this.constraints.containsKey(type)) {
        result = this.constraints.get(type);
      } else {
        result = new Constraint(type, message);
        this.constraints.put(type, result);
      }
    }
    return result;
  }
  
  /**
   * Get error message for the specified constraint constraintType
   * @param constraintType the field constraint constraintType
   * @return the assigned value or null if not supported.
   */
  public String getMessage(FieldContraintTypes type) {
    String result = null;
    if (this.constraints.containsKey(type)) {
      result = this.constraints.get(type).message;
    }
    return result;
  }  
  
  /**
   * Get the validation options associated with the specified constraintType
   * @param <TObj> type object
   * @param type the constraintType
   * @return the assigned options of null if not supported.
   */
  public <TObj> TObj getOptions(FieldContraintTypes type) {
    TObj result = null;
    if (this.constraints.containsKey(type)) {
      result = this.constraints.get(type).getOptions();
    }
    return result;
  }
  //</editor-fold>
}
