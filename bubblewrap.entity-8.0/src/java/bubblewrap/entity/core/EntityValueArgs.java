package bubblewrap.entity.core;

import bubblewrap.core.events.RequestArgs;
import bubblewrap.entity.context.FieldInfo;

/**
 * The Request Argument use in the validation of a Entity Field Value. It 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EntityValueArgs extends RequestArgs<Boolean> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Entity's field name
   */
  private FieldInfo fieldInfo;
  /**
   * Placeholder for the Input value to be validated
   */
  private Object inValue;
  /**
   * Placeholder for the curInput value
   */
  private Object curValue;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public EntityValueArgs(FieldInfo fieldInfo, Object inValue, 
          Object curValue) {
    if (fieldInfo == null) {
      throw new NullPointerException("The EntityValueArgs' Field Info cannot be "
              + "unassigned.");
    }
    this.fieldInfo = fieldInfo;
    this.inValue = inValue;
    this.curValue = curValue;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Entity's field Info
   * @return the assigned value
   */
  public FieldInfo getFieldInfo() {
    return this.fieldInfo;
  }
    
  /**
   * Get the Current Value of the field
   * @return the assigned value (can be null)
   */
  public Object getCurValue() {
    return this.curValue;
  }
  
  /**
   * Get the new Input Value for the field
   * @return the assigned value (can be null)
   */
  public Object getInValue() {
    return this.inValue;
  }
  
  /**
   * Set the this.result=true and isHandled=true. The inValue stay unchanged.
   */
  public void setIsValid() {
    this.setResult(true);
  }
  
  /**
   * Get the this.result=true and isHandled=true and update the inValue with a "cleaned" 
   * value for the field. 
   * @param inValue the "cleaned" and validated value
   */
  public void setIsValid(Object inValue) {
    this.inValue = inValue;
    this.setResult(true);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "Args[Field=???; CurValue=???; InValue=???]"</p>
   */
  @Override
  public String toString() {
    return "Args[Field=" + this.fieldInfo + "; CurValue=" + this.curValue
            + "; InValue=" + this.inValue + "]";
  }
  //</editor-fold>
}
