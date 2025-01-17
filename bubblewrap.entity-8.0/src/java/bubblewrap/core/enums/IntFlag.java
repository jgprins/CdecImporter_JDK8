package bubblewrap.core.enums;

/**
 * An Unsigned 32-bit Integer for managing bitmap flag values. The valid range of the
 * IntFlag = [0x00000000..0x7FFFFFFF] with bit index [1..31]
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public final class IntFlag extends Number implements Comparable<IntFlag> {

  //<editor-fold defaultstate="collapsed" desc="Static Fields">
  /**
   * The Minimum value if an IntFlag (0x000000)
   */
  public static final IntFlag ZERO = new IntFlag(0);
  /**
   * The Minimum value if an IntFlag (0x000000)
   */
  public static final IntFlag MIN = new IntFlag(0);
  /**
   * The Minimum value if an IntFlag (0x7FFFFFFF)
   */
  public static final IntFlag MAX = new IntFlag(0x7FFFFFFF);
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static Constructor
   * @param value a long value
   * @return a new instance of value
   * @throws IllegalStateException is out of bounds[MIN..MAX]
   */
  public static IntFlag valueOf(int value) {
    if ((value < IntFlag.MIN.value) || (value > IntFlag.MAX.value)) {
      throw new IllegalStateException("Value[" + value + "] is out of the avlid "
              + "InFlag.Bounds[0.." + IntFlag.MAX.value + "].");
    }
    return new IntFlag(value);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The internal value of the IntFlag
   */
  private final int value;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Private Constructor
   * @param lngVal 
   */
  private IntFlag(int lngVal) {
    super();    
    this.value = lngVal;
  }  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods/Operations">
  /**
   * Bitwise And operation
   * @param other another IntFlag
   * @return return the bitwise operation result
   */
  public IntFlag and(IntFlag other) {
    if (other == null) {
      throw new NullPointerException("The other IntFlag number caanot be unassigned");
    }
    return new IntFlag(other.value & this.value);
  }
  
  /**
   * Bitwise Or operation
   * @param other another IntFlag
   * @return return the bitwise operation result
   */
  public IntFlag or(IntFlag other) {
    if (other == null) {
      throw new NullPointerException("The other IntFlag number caanot be unassigned");
    }
    return new IntFlag(other.value | this.value);
  }
  
  /**
   * Bitwise Xor operation
   * @param other another IntFlag
   * @return return the bitwise operation result
   */
  public IntFlag xor(IntFlag other) {
    if (other == null) {
      throw new NullPointerException("The other IntFlag number caanot be unassigned");
    }
    return new IntFlag(other.value ^ this.value);
  }
  
  /**
   * Bitwise Not (~operation on itself) operation
   * @param other another IntFlag
   * @return return the bitwise operation result
   */
  public IntFlag not() {
    int lngVal = (IntFlag.MAX.value & (~ this.value));
    return new IntFlag(lngVal);
  }
  
  
  /**
   * Check if the other flag's bits are set in this IntFlag
   * @param other another IntFlag
   * @return true if (other != null) and ((this.value & other.value) = other.value)
   */
  public boolean isMatch(IntFlag other) {
    boolean result = (other != null); 
    if (result) {
      if (other.value == 0) {
        result = (this.value == 0);
      } else {
        result = ((this.value & other.value) == other.value);
      }
    }
    return result;
  }
  
  /**
   * Check if the specified bit is set
   * @param bit bit index in the range[1..31]
   * @return return true if the bit is set or false if the bit index is out of bounds
   */
  public boolean isBitSet(int bit) {
    boolean result = ((bit > 0) && (bit <= 31));
    if (result) {
      long lngVal = (bit == 1)? 1: (0x1 << (bit-1));
      result = ((this.value & lngVal) == lngVal);
    }
    return result;
  }
  
  /**
   * Turn the specified bit on (if off)
   * @param bit bit index in the range[0..30]
   * @return return this value with the specified bit turned on or unchanged if the bit
   * index is out of bounds
   */
  public IntFlag setBit(int bit) {
    int result = this.value;
    if (((bit > 0) && (bit <= 31))) {
      int lngVal = (bit == 1)? 1: (0x1 << (bit-1));
      if ((this.value & lngVal) == 0l) {
        result = (this.value | lngVal);
      }
    }
    return new IntFlag(result);
  }
  
  /**
   * Turn the specified bit off (if set)
   * @param bit bit index in the range[1..31]
   * @return return this value with the specified bit turned off or unchanged if the bit
   * index is out of bounds
   */
  public IntFlag clearBit(int bit) {
    int result = this.value;
    if (((bit > 0) && (bit <= 31))) {
      int lngVal = (bit == 1)? 1: (0x1 << (bit-1));
      if ((this.value & lngVal) == lngVal) {
        result = (this.value ^ lngVal);
      }
    }
    return new IntFlag(result);
  }
  
  /**
   * Switch the setting of the specified bit
   * @param bit bit index in the range[1..31]
   * @return return this value with the specified bit switch or unchanged if the bit
   * index is out of bounds
   */
  public IntFlag switchBit(int bit) {
    int result = this.value;
    if (((bit > 0) && (bit <= 31))) {
      int lngVal = (bit == 1)? 1: (0x1 << (bit-1));
      if ((this.value & lngVal) == lngVal) {
        result = (this.value ^ lngVal);
      } else {
        result = (this.value | lngVal);
      }
    }
    return new IntFlag(result);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements Number">
  /**
   * {@inheritDoc }
   */
  @Override
  public int intValue() {
    return this.value;
  }
  
  /**
   * {@inheritDoc }
   */
  @Override
  public long longValue() {
    return this.value;
  }
  
  /**
   * {@inheritDoc }
   */
  @Override
  public float floatValue() {
    Integer result = this.value;
    return result.floatValue();
  }
  
  /**
   * {@inheritDoc }
   */
  @Override
  public double doubleValue() {
    Integer result = this.value;
    return result.doubleValue();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: </p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object obj) {
    boolean result = ((obj !=  null) && (obj instanceof IntFlag));
    if (result) {
      IntFlag other = (IntFlag) obj;
      result = (this.value == other.value);
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE:  Return a HashCode based on thius.value</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 17 * hash + (int) (this.value ^ (this.value >>> 32));
    return hash;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return this.value as a 8-digit hexadecimal string </p>
   */
  @Override
  public String toString() {
    return String.format("%0#8X", this.value);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return 1 of other=null, or
   * return Long.Compare(this.value, other.value)</p>
   */
  @Override
  public int compareTo(IntFlag other) {
    int result = 0;
    if (other == null) {
      result = 1;
    } else {
      result = Long.compare(this.value, other.value);
    }
    return result;
  }
  //</editor-fold>
}
