package bubblewrap.admin.enums;

import bubblewrap.core.enums.IEnumFlag;
import bubblewrap.core.enums.IntFlag;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public enum TestEnum implements IEnumFlag {
  None(0x00001) {
    @Override
    public boolean canAccess(int otherBit) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IntFlag getValue() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  };
  
  public final int bitValue;
  
  private TestEnum(int bitValue) {
    this.bitValue = bitValue;
  } 
  
  public abstract boolean canAccess(int otherBit);
}
