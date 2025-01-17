package bubblewrap.io;

import bubblewrap.core.enums.BwEnum;
import bubblewrap.core.enums.BwEnumFlag;
import bubblewrap.core.enums.BwEnumFlagSet;
import bubblewrap.core.enums.BwEnumSet;
import bubblewrap.io.interfaces.IObjectData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This is a helper class to standardize the serializing and deserializing of a object
 * to/from a byte array.
 * @author kprins
 */
public class BlobSerializer {
  /**
   * Serialize pObject to a byte array and throw an exception is the process fails.
   * Return null if pObject = null or the serialization returns an empty byte array. 
   * If pObject is an instance of IObjectData, the Object is serialized to an 
   * ObecjtDataMap and if the latter is not null or empty its is serialized to a 
   * byte array, which is returned by this method.
   * @param <V> <V extends Serializable>
   * @param inObject V
   * @return byte[]
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public static <V extends Serializable> byte[] toByteArray(V inObject) 
                                                                      throws Exception {
    byte[] result = null;
    try {
      if (inObject != null) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream outStream = new ObjectOutputStream(byteStream);
        if (inObject instanceof IObjectData) {
          IObjectData objData = (IObjectData) inObject;
          ObjectData serializedData = objData.serializeObject();
          if ((serializedData != null) && (!serializedData.isEmpty())) {
            outStream.writeObject(serializedData);
            outStream.close();
          }
        } else if (inObject instanceof BwEnumFlag) {
          BwEnumFlagSerializer serializer = new BwEnumFlagSerializer();
          BwEnumFlag enumObj = (BwEnumFlag) inObject;
          serializer.setEnum(enumObj);
          ObjectData serializedData = serializer.serializeObject();
          if ((serializedData != null) && (!serializedData.isEmpty())) {
            outStream.writeObject(serializedData);
            outStream.close();
          }
        } else if (inObject instanceof BwEnumFlagSet) {
          BwEnumFlagSetSerializer serializer = new BwEnumFlagSetSerializer();
          BwEnumFlagSet enumObj = (BwEnumFlagSet) inObject;
          serializer.setEnumSet(enumObj);
          ObjectData serializedData = serializer.serializeObject();
          if ((serializedData != null) && (!serializedData.isEmpty())) {
            outStream.writeObject(serializedData);
            outStream.close();
          }
        } else if (inObject instanceof BwEnum) {
          BwEnumSerializer serializer = new BwEnumSerializer();
          BwEnum enumObj = (BwEnum) inObject;
          serializer.setEnum(enumObj);
          ObjectData serializedData = serializer.serializeObject();
          if ((serializedData != null) && (!serializedData.isEmpty())) {
            outStream.writeObject(serializedData);
            outStream.close();
          }
        } else if (inObject instanceof BwEnumSet) {
          BwEnumSetSerializer serializer = new BwEnumSetSerializer();
          BwEnumSet enumObj = (BwEnumSet) inObject;
          serializer.setEnumSet(enumObj);
          ObjectData serializedData = serializer.serializeObject();
          if ((serializedData != null) && (!serializedData.isEmpty())) {
            outStream.writeObject(serializedData);
            outStream.close();
          }
        } else {
          if (Serializable.class.isInstance(inObject)) {
            outStream.writeObject(inObject);
            outStream.close();
          }
        }
        
        if (byteStream.size() > 0) {
          result = byteStream.toByteArray();
        }
      }
    } catch (Exception pExp) {
      throw new Exception("BlobSerializer.toByteArray Error:\n\r "+ pExp.toString());
    }
    return result;
  }

  /**
   * <p>Deserialize a previously serialized instance of Class<V>. Return null if pObjData
   * is null or empty or the deserialization of pObjData returns null. Throws an
   * exception if the deserialized instance is not of Class<V> or any deserialization
   * errors occur.</p>
   * <p>If the deserialized instance is an ObjectData, it will create an instance 
   * of the wrapper class and deserialize and return the object.</p> 
   * @param <V> <V extends Serializable>
   * @param dataBlob byte[]
   * @return V
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  public static <V extends Serializable> V fromByteArray(byte[] dataBlob) 
          throws Exception {
    V result = null;
    try {
      if ((dataBlob != null) && (dataBlob.length > 0)) {
        InputStream byteStream = new ByteArrayInputStream(dataBlob);
        ObjectInputStream inStream = new ObjectInputStream(byteStream);
        Object outObject = inStream.readObject();
        if (outObject != null) {
          if ((outObject instanceof ObjectData)) {
            ObjectData objData = (ObjectData) outObject;
            Object dataObject = objData.deserialize();
            if (dataObject instanceof BwEnumFlagSerializer) {
              BwEnumFlagSerializer serializer = (BwEnumFlagSerializer) dataObject;
              BwEnumFlag enumVal = serializer.getEnum();
              result = (V) enumVal;
            } else if (dataObject instanceof BwEnumFlagSetSerializer) {
              BwEnumFlagSetSerializer serializer = (BwEnumFlagSetSerializer) dataObject;
              BwEnumFlagSet enumVal = serializer.getEnumSet();
              result = (V) enumVal;
            } else if (dataObject instanceof BwEnumSerializer) {
              BwEnumSerializer serializer = (BwEnumSerializer) dataObject;
              BwEnum enumVal = serializer.getEnum();
              result = (V) enumVal;
            } else if (dataObject instanceof BwEnumSetSerializer) {
              BwEnumSetSerializer serializer = (BwEnumSetSerializer) dataObject;
              BwEnumSet enumVal = serializer.getEnumSet();
              result = (V) enumVal;
            } else {
              result = (V) objData.deserialize();
            }
          } else {
            result = (V) outObject;
          }
        }
      }      
    } catch (Exception pExp) {
      throw new Exception("BlobSerializer.fromByteArray Error:", pExp);
    }
    return result;
  }
}
