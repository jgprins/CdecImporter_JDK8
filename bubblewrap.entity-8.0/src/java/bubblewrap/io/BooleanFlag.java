package bubblewrap.io;

/**
 * A Boolean flag class for managing Throttle settings - e.g. used in the
 * bwcomp:ajaxThrottleButton component
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BooleanFlag {
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * The Flag Value (default = this.default)
   */
  private Boolean flag;
  /**
   * The default flag value (default = null|false)
   */
  private Boolean defaultFlag;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BooleanFlag() {
    super();
    this.flag = null;
    this.defaultFlag = null;
  }
  /**
   * Public Constructor
   */
  public BooleanFlag(Boolean defaultFlag) {
    this();
    this.defaultFlag = ((defaultFlag == null) || (!defaultFlag))? null: defaultFlag;
    this.reset();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Flag State (default = false)
   * @return the current setting
   */
  public Boolean getFlag() {
    return ((this.flag != null) && (this.flag));
  }
  
  /**
   * Set the Flag State
   * @param flag the new setting
   */
  public void setFlag(Boolean flag) {
    this.flag = ((flag == null) || (!flag))? null: flag;
  }
  
  /**
   * Get the Default Flag (default = false) - set during initiation
   * @return get the assigned value
   */
  public Boolean getDefaultFlag() {
    return ((this.defaultFlag != null) && (this.defaultFlag));
  }
  
  /**
   * Reset this.flag = this.defaultFalg
   */
  public void reset() {
    this.setFlag(this.getDefaultFlag());
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a clone of this instance</p>
   */
  @Override
  protected BooleanFlag clone() {
    BooleanFlag result = new BooleanFlag(this.defaultFlag);
    result.flag = this.flag;
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "Flag=" + this.flag</p>
   */
  @Override
  public String toString() {
    return "Flag=" + this.getFlag();
  }
  //</editor-fold>
}
