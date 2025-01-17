package bubblewrap.io;

import bubblewrap.io.interfaces.IObjectData;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class extends the HaspMap class by implementing IObjectData. This extended
 * object serialize each Value by calling BlobSerlizer if the value implements the
 * IObjectData interface.
 * @author kprins
 */
public class ObjectDataMap<K extends Object, V extends Object> extends HashMap<K, V> 
                                                            implements IObjectData {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
                                        Logger.getLogger(ObjectDataMap.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Public Constructor  
   */
  public ObjectDataMap() {
    super();        
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Internal KeyPair Class defintion">
  private class KeyPair<K extends Object,V extends Object> implements Serializable {
    
    //<editor-fold defaultstate="collapsed" desc="Private Fields">
    private K mpKey;
    private Object mpValue;
    private Boolean mbSerialized;
    //</editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Constructor">
    /**
     * Public Constructor - Initiate the Key and Value of the KeyPair. if pValue!=null
     * and an instance if IObjectData, then serialize the value and assign its
     * serialized byte[] as the KeyPair value (set mbSerilized=true)
     */
    private KeyPair(K pKey, V pValue) throws Exception {
      this.mpKey = pKey;
      
      this.mpValue = null;
      if ((pValue != null) && (IObjectData.class.isInstance(pValue))) {
        IObjectData pSerialObj = (IObjectData) pValue;
        this.mpValue = BlobSerializer.toByteArray(pSerialObj);
        this.mbSerialized = (this.mpValue != null);
      } else {
        this.mpValue = pValue;
        this.mbSerialized = false;
      }
    }
    // </editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="KeyPair Properties">
    /**
     * Get the Key of the KeyPair
     * @return K
     */
    private K getKey() {
      return this.mpKey;
    }
    
    /**
     * Get the Deserialized value of the KeyPair. Return null if an error occur. No
     * exceptions are thrown.
     * @return V
     */
    @SuppressWarnings("unchecked")
    private V getValue()  {
      V pResult = null;
      try {
        if (this.mpValue != null) {
          if (this.mbSerialized) {
            byte[] pData = (byte[]) this.mpValue;
            pResult = (V) BlobSerializer.fromByteArray(pData);
          } else {
            pResult = (V) this.mpValue;
          }
        }
      } catch (Exception pExp) {
      }
      return pResult;
    }
    //</editor-fold>
  }
  //</editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="IObjectData Implementation">
  /**
   * Serialize each key-Value pair as a KeyPair and add to the ObejctData. Return
   * and empty ObjectData if the HashMap is empty
   * @return ObjectData
   */
  @Override
  public ObjectData serializeObject() {
    ObjectData pObjectData = null;
    try {
      pObjectData = new ObjectData(this);
      if (!this.isEmpty()) {
        Integer iCnt = 0;
        for (K pKey : this.keySet()) {
          V pValue = this.get(pKey);
          KeyPair<K,V> pKeyPair = new KeyPair<K,V>(pKey, pValue);
          iCnt++;
          pObjectData.put("keyPair" + iCnt.toString(), pKeyPair);
        }
        pObjectData.put("keyCount", iCnt);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.serializeObject Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pObjectData;
  }
  
  /**
   * Deserialize the HashMap's KeyPairs and assigned as key->Values
   * @param pData ObjectData
   */
  @Override
  public void deserializeObject(ObjectData pData) {
    if ((pData != null) && (!pData.isEmpty())) {
      Integer iCnt = pData.getCasted("keyCount", 0);
      if (iCnt == 0) {
        return;
      }
      
      for (Integer iKey = 0; iKey < iCnt; iKey++) {
        KeyPair<K,V> pKeyPair = pData.getCasted("keyPair" + iKey.toString(), null);
        if (pKeyPair != null) {
          K pKey = pKeyPair.getKey();
          V pValue = pKeyPair.getValue();
          if ((pKey != null) && (pValue != null)) {
            this.put(pKey, pValue);
          }
        }
      }
    }
  }
  //</editor-fold>
}
