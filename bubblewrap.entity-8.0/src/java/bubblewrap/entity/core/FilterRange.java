package bubblewrap.entity.core;

import java.io.Serializable;

/**
 * The Filter Range definition used by the facade to get a sub-set of values.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class FilterRange implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Private placeholder for the array that stores the two indices.
   */
  private int[] range;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public parameter Constructor initiating a 
   */
  public FilterRange() {
    this.range = new int[]{0,0};
  }
  
  /**
   * Constructor with a low Index and the Range size
   * @param loIdx the lowest (zero-base) element in array
   * @param size the number of element allowed in range
   */
  public FilterRange(int loIdx, int size) {
    this();
    this.range[0] = loIdx;
    this.range[1] = loIdx + ((size <= 0)? 0: size-1);
  }
  
  
  /**
   * Constructor with a Range size assuming the lowest index = 0.
   * @param size the number of element allowed in range
   */
  public FilterRange(int size) {
    this();
    this.range[0] = 0;
    this.range[1] = ((size <= 0)? 0: size-1);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the lowest element index
   * @return the assigned value
   */
  public int getLowIndex() {
    return this.range[0];
  }
  
  /**
   * Get the highest element index
   * @return the assigned value
   */
  public int getHighIndex() {
    return this.range[1];
  }
  
  /**
   * The number of element allowed in the range
   * @return (this.range[1] - this.range[0] + 1)
   */
  public int getSize() {
    return (this.range[1] - this.range[0] + 1);
  }
  
  /**
   * Increment both indices with this.size to get the next set of filtered elements
   */
  public void increment() {
    int size = this.getSize();
    this.range[0] = this.range[0] + size;
    this.range[1] = this.range[1] + size;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: "Range"  + this.range.toString()</p>
   */
  @Override
  public String toString() {
    return "Range"  + this.range.toString();
  }
  //</editor-fold>
}
