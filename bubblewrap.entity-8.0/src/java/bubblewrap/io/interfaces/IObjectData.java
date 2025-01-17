package bubblewrap.io.interfaces;

import bubblewrap.io.ObjectData;
import java.io.Serializable;

/**
 * <p>This interface can be used for classes where changing/customized classes have to 
 * serialized to a database. This should be used in liu of the standard Serializable
 * interface to serialize the object. </p>
 * <p><b>NOTE:</b> A class that implements the interface requires a parameterless public 
 * constructor.
 * @author kprins
 */
public interface IObjectData extends Serializable {
  
  /**
   * Called to Serialize the Object's properties to a HashMap.
   * @return HashMap<String, Serializable>
   */
  public ObjectData serializeObject();
  
  /**
   * Called to deserialize the properties from a HashMap
   * @param pData ObjectData
   */
  public void deserializeObject(ObjectData pData);
}
