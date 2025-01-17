package bubblewrap.io;

import bubblewrap.core.enums.BwEnum;
import bubblewrap.core.enums.BwEnumFlag;
import bubblewrap.core.enums.IntFlag;
import bubblewrap.io.interfaces.IObjectData;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Wrapper for serializing a BwEnum enum value
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwEnumSerializer implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                Logger.getLogger(BwEnumSerializer.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Placeholder for the Enum's Value
   */
  private Integer enumValue;
  /**
   * Placeholder for the Enum's Declaring Class
   */
  private Class<? extends BwEnum> enumClass;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwEnumSerializer() {
    this.enumValue = null;
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
   * Assign the BwEnum to serialize
   * @param <TEnum> extends BwEnum
   * @param enumValue the enum value
   */
  @SuppressWarnings("unchecked")
  public final <TEnum extends BwEnum<TEnum>> void setEnum(TEnum enumValue) {
    if (enumValue == null) {
      this.enumValue = null;
      this.enumClass = null;
    } else {
      this.enumValue = enumValue.value;
      this.enumClass = (Class<? extends BwEnum>) enumValue.getClass();
    }
  }
  
  /**
   * Retrieve the deserialized the BwEnumFlagSet
   * @param <TEnum> extends BwEnumFlag
   * @return the deserialized instance
   */
  @SuppressWarnings("unchecked")
  public final <TEnum extends BwEnum<TEnum>> TEnum getEnum() {
    TEnum result = null;
    try{
      if ((this.enumValue != null) && (this.enumClass != null)) {
        result = (TEnum) BwEnum.valueOf(this.enumClass, this.enumValue.intValue());
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getEnum Error:\n " + exp.getMessage());
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
      pResult.put("enumValue", this.enumValue);
      pResult.put("enumClassName",
              (this.enumClass == null)? null: this.enumClass.getName());
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
      this.enumValue = pData.getCasted("enumValue", null);
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
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.deserializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  //</editor-fold>  
}
