package bubblewrap.io;

import bubblewrap.io.interfaces.IObjectData;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kprins
 */
public class ObjectData implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(ObjectData.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Serialized Object's Class Name
   */
  private String msObjectClass;
  /**
   * Placeholder for the Serialized data of the Object's Fields
   */
  private HashMap<String, byte[]> mpFieldBlobs;
  /**
   * Placeholder for a transient ReportString - generated instead of the mpFieldBlobs if
   * mbForReport is set
   */
  private transient String msReport;
  /**
   * Placeholder for a ForReport flag
   */
  private Boolean mbForReport;
  //</editor-fold>
          
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a Object reference. The constructor calls setObjectClass to
   * initiate the internal object class reference. 
   * Call overload 2 with bForReport=false.
   */
  public <T extends IObjectData> ObjectData(T pObject) throws Exception {
    this(pObject,false);
  }
  
  /**
   * Public Constructor with a Object reference. The constructor calls setObjectClass to
   * initiate the internal object class reference. iIf (bForReport) the values will be
   * serialize to report String instead of the HashMap.
   * @param <T> <T extends IObjectData>
   * @param pObject T
   * @param bForReport boolean
   * @throws Exception 
   */
  public <T extends IObjectData> ObjectData(T pObject, boolean bForReport) 
          throws Exception {
    super();     
    this.msObjectClass = null;
    this.msReport = null;
    this.mbForReport = bForReport;
    this.setObjectClass(pObject);
  }
  //</editor-fold>
          
  // <editor-fold defaultstate="collapsed" desc="Private Methods">  
  /**
   * Called to set the internal ObjectClass name after checking that the class support
   * a parameterless constructor
   * @param <T extends IObjectData>
   * @param pObject T
   * @throws Exception 
   */
  private <T extends IObjectData> void setObjectClass(T pObject) throws Exception {
    if (pObject == null) {
      throw new NullPointerException("The Object reference cannot be unassigned");
    }
    
    @SuppressWarnings("unchecked")
    Class<T> pClass = (Class<T>) pObject.getClass();
    try {
      Class[] pTypes = null;
      Constructor pConst = pClass.getConstructor(pTypes);
      if (pConst == null) {
        throw new Exception("");
      }
    } catch (Exception pExp) {
      throw new Exception("IObejctData Class[" + pClass.getSimpleName() + "] does not "
              + "support a parameterless instructor, which is needed for "
              + "deserialization.");
    }
    
    this.msObjectClass = pClass.getName();
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get a new instance of the Map's Object Class. Throws Exception if this process 
   * fails because the class is undefined or it instance initiation failed.
   * @param <T extends IObjectData>
   * @return T
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public <T extends IObjectData> T newObjectInst() throws Exception {
    if ((this.mbForReport != null) && (this.mbForReport)) {
      throw new Exception("Cannot create a new Object Instance from when the Object "
              + "was serialized ror reporting purposes.");
    }
    
    T pResult = null;
    if (this.msObjectClass == null) {
      throw new Exception("The ObjectDataMap's Object Class has not been initiated.");
    }

    @SuppressWarnings("unchecked")
    Class pClass = Class.forName(this.msObjectClass);
    if (pClass == null) {
      throw new Exception("Unable to initiate Class[" + this.msObjectClass + "].");
    }

    Object pObject = pClass.newInstance();
    if (pObject == null) {
      throw new Exception("Initiating a new instance of Class[" + this.msObjectClass 
              + "] failed.");
    } 
    pResult = (T) pObject;
    return pResult;
  }
  
  /**
   * Called to initiate and deserialize the serialized class. It calls newObjectInst to
   * create the new instance and the call its deserializeObject() method to deserialize
   * itself using this ObjectData instance's serialize settings.
   * @param <T extends IObjectData>
   * @return T
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public <T extends IObjectData> T deserialize() throws Exception {
    if ((this.mbForReport != null) && (this.mbForReport)) {
      throw new Exception("Cannot deserialize the properties of the Object when it "
              + "was serialized for Rrporting purposes.");
    }
    
    T pResult = (T) this.newObjectInst();
    if (pResult != null) {
      pResult.deserializeObject(this);
    }
    return pResult;
  }
  
  /**
   * Return true if the ObjectData has no field values.
   * @return boolean
   */
  public boolean isEmpty() {
    return ((this.mpFieldBlobs == null) || (this.mpFieldBlobs.isEmpty()));
  }
  
  /**
   * <p>Attempt to serialize pValue by calling BlobSerializer.toByteArray(pValue) and
   * assign the result as the field value in the internal FieldBlob HashMap.  If pValue
   * or the serialized result is null, save the field blob as an empty array. It throws
   * an exception if sField is empty and any serialization error occurred.</p>
   * <p><b>NOTE:</b> The Field names are case sensitive to be consistent with other 
   * java reflection features. However, the passed field names are stripped from any 
   * leading and trailing spaces.</p>
   * @param <T extends Serializable>
   * @param sField String
   * @param pValue T
   * @throws Exception 
   */
  public <T extends Serializable> void put(String sField, T pValue) throws Exception {
    try {
      sField = DataEntry.cleanString(sField);
      if (sField == null) {
        throw new Exception("The Object Field name cannot by empty.");
      }
      
      if ((this.mbForReport != null) && (this.mbForReport)) {
        String sValue = (pValue == null)? "null": pValue.toString();
        this.msReport = (this.msReport == null)? "" : this.msReport;
        this.msReport += "<li>" + sField + " = " +sValue + "</li>";
      } else {
        byte[] pBlob = null;
        if (pValue != null) {
          pBlob = BlobSerializer.toByteArray(pValue);
        }      

        if (pBlob == null) {
          pBlob = new byte[]{};
        }

        if (this.mpFieldBlobs == null) {
          this.mpFieldBlobs = new HashMap<String, byte[]>();
        }

        this.mpFieldBlobs.put(sField, pBlob);
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
                                            + ".put Error:\n " + pExp.getMessage());
    }
  }
  
  /**
   * <p>Return the assigned value casted to T. If the field was serialized, it first 
   * deserialized the fields saved Blob. If the save blob is an empty byte array, the
   * field has not been serialized, or the deserialization failed. The saved field
   * value is set to null. In this case the returned value will be set to pDefault.
   * All errors are logged.</p>
   * <p><b>NOTE:</b> The Field names are case sensitive to be consistent with other 
   * java reflection features. However, the passed field names are stripped from any 
   * leading and trailing spaces.</p>
   * @param <T extends Serializable>
   * @param sField String
   * @param pDefault T
   * @return T
   */
  @SuppressWarnings("unchecked")
  public <T extends Serializable> T getCasted(String sField, T pDefault) {
    T pValue = null;
    try {
      if ((this.mbForReport != null) && (this.mbForReport)) {
        throw new Exception("Cannot get field values when the Object was serialized "
                + "for reporting purposes.");
      }
      
      sField = DataEntry.cleanString(sField);
      if ((sField != null) && (!this.isEmpty()) 
                                      && (this.mpFieldBlobs.containsKey(sField))) {
        byte[] pBlob = this.mpFieldBlobs.get(sField);
        if ((pBlob != null) && (pBlob.length > 0)) {
          pValue = (T) BlobSerializer.fromByteArray(pBlob);
        }
      }
    } catch (Exception pExp) {
      pValue = null;
      logger.log(Level.WARNING, "{0}.getCasted Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return (pValue == null)? pDefault: pValue;
  }
  
  /**
   * Get the generated Object Report in the format "<b>" + sCaption + ":</b><ul>" +
   * "<li>sField = sFieldValue</li>....</ul>". if (sCaption=null), set sCaption = 
   * this.ObjectClass.
   * @param sCaption String
   * @return String.
   */
  public String getObjectReport(String sCaption) {
    sCaption = DataEntry.cleanString(sCaption);
    if (sCaption == null) {
      sCaption = this.msObjectClass;
    }
    String sResult = "<b>" + sCaption + ":</b><ul>";
    if (this.msReport == null) {
      sResult += "<li>Empty</li>";
    } else {
      sResult += this.msReport;
    }
    sResult += "</ul>";
    return sResult;
  }
  //</editor-fold>
}
