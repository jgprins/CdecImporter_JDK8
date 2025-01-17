package bubblewrap.io;

import bubblewrap.core.enums.BwEnum;
import bubblewrap.core.enums.BwEnumFlag;
import bubblewrap.core.enums.BwEnumFlagSet;
import bubblewrap.core.enums.BwEnumSet;
import bubblewrap.core.enums.IntFlag;
import bubblewrap.io.interfaces.IObjectData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Wrapper for serializing a BwEnumFlagSet 
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwEnumSetSerializer implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                Logger.getLogger(BwEnumSetSerializer.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Placeholder for the Enum's Value
   */
  private BwEnum[] enumArray;
  /**
   * Placeholder for the EnumSet's Declaring Class
   */
  private Class<? extends BwEnumSet> enumSetClass;
  /**
   * Placeholder for the EnumSet's ElementType Class
   */
  private Class<? extends BwEnum> enumClass;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwEnumSetSerializer() {
    this.enumArray = null;
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
  public final <TSet extends BwEnumSet<TEnum>, TEnum extends BwEnum<TEnum>> 
                                      void setEnumSet(TSet enumSet) {
    if (enumSet == null) {
      this.enumArray = null;
      this.enumClass = null;
      this.enumSetClass = null;
    } else {
      this.enumArray = new BwEnum[]{};
      if (!enumSet.isEmpty()) {
        this.enumArray = enumSet.toArray(this.enumArray);
      }
      this.enumSetClass = (Class<? extends BwEnumSet>) enumSet.getClass();
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
  public final <TSet extends BwEnumSet<TEnum>, TEnum extends BwEnum<TEnum>> 
          TSet getEnumSet() {
    TSet result = null;
    try{
      if ((this.enumClass != null) && 
              (this.enumSetClass != null)) {
        if (BwEnumSet.class.equals(this.enumSetClass)) {
          result = (TSet) BwEnumSet.noneOf(this.enumClass);
        } else {
          result = (TSet) BwEnumSet.instanceOf(this.enumSetClass);
        }
      }
      if ((this.enumArray != null) && (this.enumArray.length > 0)) {
        TEnum[] castArr = (TEnum[]) this.enumArray;
        result.addAll(castArr);
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
      pResult.put("enumArray", this.enumArray);
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
      this.enumArray = pData.getCasted("enumArray", null);
      String className = pData.getCasted("enumClassName", null);
      className = DataEntry.cleanString(className);
      if (className != null) {
        try {
          this.enumClass = (Class<? extends BwEnum>) Class.forName(className);
        } catch(Exception inExp) {
          this.enumClass = null;
          throw new Exception("Unabel to create class from Name[" + className + "]");
        }
      }
      className = pData.getCasted("enumSetClassName", null);
      className = DataEntry.cleanString(className);
      if (className != null) {
        try {
          this.enumSetClass = (Class<? extends BwEnumSet>) Class.forName(className);
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
