package bubblewrap.io;

import bubblewrap.core.enums.BwEnumFlag;
import bubblewrap.core.enums.BwEnumFlagSet;
import bubblewrap.core.enums.IntFlag;
import bubblewrap.io.interfaces.IObjectData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Wrapper for serializing a BwEnumFlagSet 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwEnumFlagSetSerializer implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                Logger.getLogger(BwEnumFlagSetSerializer.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Placeholder for the Enum's Value
   */
  private IntFlag bitMap;
  /**
   * Placeholder for the EnumSet's Declaring Class
   */
  private Class<? extends BwEnumFlagSet> enumSetClass;
  /**
   * Placeholder for the EnumSet's ElementType Class
   */
  private Class<? extends BwEnumFlag> enumClass;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwEnumFlagSetSerializer() {
    this.bitMap = null;
    this.enumSetClass = null;
    this.enumClass = null;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Assign the BwEnumFlagSet to serialize
   * @param <TEnum> extends BwEnumFlag
   * @param enumValue the enum value
   */
  @SuppressWarnings("unchecked")
  public final <TSet extends BwEnumFlagSet<TEnum>, TEnum extends BwEnumFlag<TEnum>> 
                                      void setEnumSet(TSet enumSet) {
    if (enumSet == null) {
      this.bitMap = null;
      this.enumClass = null;
      this.enumSetClass = null;
    } else {
      this.bitMap = enumSet.getAsBitmap();
      this.enumSetClass = (Class<? extends BwEnumFlagSet>) enumSet.getClass();
      this.enumClass = enumSet.elementType;
    }
  }
  
  /**
   * Retrieve the deserialized the BwEnumFlagSet
   * @param <TSet> extends BwEnumFlagSet
   * @param <TEnum> extends BwEnumFlag
   * @return the new instance with the deserialized settings
   */
  @SuppressWarnings("unchecked")
  public final <TSet extends BwEnumFlagSet<TEnum>, TEnum extends BwEnumFlag<TEnum>> 
          TSet getEnumSet() {
    TSet result = null;
    try{
      if ((this.enumClass != null) && 
              (this.enumSetClass != null)) {
        if (BwEnumFlagSet.class.equals(this.enumSetClass)) {
          result = (TSet) BwEnumFlagSet.noneOf(this.enumClass);
        } else {
          result = (TSet) BwEnumFlagSet.instanceOf(this.enumSetClass);
        }
      }
      if (this.bitMap != null) {
        result.setAsBitmap(this.bitMap);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getEnumSet Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement IObjectData">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Serialize this.enumName and this.enumClass.name</p>
   */
  @Override
  public ObjectData serializeObject() {
    ObjectData pResult = null;
    try {
      pResult = new ObjectData(this);
      pResult.put("enumValue", this.bitMap);
      pResult.put("enumClassName",
              (this.enumClass == null)? null: this.enumClass.getName());
      pResult.put("enumSetClassName",
              (this.enumSetClass == null)? null: this.enumSetClass.getName());
    } catch (Exception pExp) {
      pResult = null;
      logger.log(Level.WARNING, "{0}.serializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Deserialize thisenumValue and enumClassName and initiate
   * this.enumClass.</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public void deserializeObject(ObjectData pData) {
    try {
      this.bitMap = pData.getCasted("enumValue", null);
      String className = pData.getCasted("enumClassName", null);
      className = DataEntry.cleanString(className);
      if (className != null) {
        try {
          this.enumClass = (Class<? extends BwEnumFlag>) Class.forName(className);
        } catch(Exception inExp) {
          this.enumClass = null;
          throw new Exception("Unabel to create class from Name[" + className + "]");
        }
      }
      className = pData.getCasted("enumSetClassName", null);
      className = DataEntry.cleanString(className);
      if (className != null) {
        try {
          this.enumSetClass = (Class<? extends BwEnumFlagSet>) Class.forName(className);
        } catch(Exception inExp) {
          this.enumSetClass = null;
          throw new Exception("Unabel to create class from Name[" + className + "]");
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>  
}
