package bubblewrap.io;

import bubblewrap.core.reflection.ReflectionInfo;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.json.JSONObject;

/**
 * A Abstract class for serializing and deserializing unknown JSON serializing
 * capabilities (i.e., the caller has no reference of the specific class to serialize or
 * whether the class is JSON serializable).
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class JsonSerializer<THost extends Serializable> implements Serializable{

  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Call to deserialize an previously JSONObject serialized using the JsonSerializer.
   * If extracts the objClass from jsonObj[objclass] and the call {@linkplain
   * #getSerializer(java.lang.Class) getSerializer(objClass)} to get the class'
   * static JsonSerializer. If successfully retrieved, if call the serializer's
   * {@linkplain #fromJSONObject(org.json.JSONObject) fromJSONObject} method to
   * deserialize the instance of objClass
   * @param jsonObj the JSONObject containing the serialized properties
   * @return an instance of objClass
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  public final static <TObject extends Serializable> TObject
                                      deserialize(JSONObject jsonObj) throws IOException {
    TObject result = null;
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unasssigned");
      }

      Class<? extends Serializable> objClass = null;
      String className = DataEntry.cleanString(jsonObj.optString("objclass", null));
      if (className == null) {
        throw new Exception("The JSONObject does not contains the serialized 'objclass' "
                + "class name.");
      }

      Class inClass = Class.forName(className);
      if ((inClass == null) && (!Serializable.class.isAssignableFrom(inClass))) {
        throw new Exception("The JSONObject's serialized 'objclass' class name is "
                + "invalid or not assignable to the Base Class[Serializable].");
      }
      objClass = (Class<? extends Serializable>)inClass;

      JsonSerializer serializer = JsonSerializer.getSerializer(objClass);
      if (serializer == null) {
        throw new Exception("Class[" + objClass.getSimpleName()
                + "] does not support a static JsonSerializer");
      }

      result = (TObject) serializer.fromJSONObject(jsonObj);
    } catch (Exception exp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".deserialize Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Call to serialize <tt>obj</tt> to a JSONObject
   * @param <TClass>
   * @param obj
   * @return
   * @throws IOException
   */
  public static final <TClass extends Serializable> JSONObject serialize(TClass obj)
                                                                    throws IOException {
    JSONObject result = null;
    try {
      if (obj == null) {
        throw new Exception("The Object to serialized is unassigned.");
      }

      Class<? extends Serializable> objClass = obj.getClass();
      JsonSerializer serializer = JsonSerializer.getSerializer(objClass);
      if (serializer == null) {
        throw new Exception("Class[" + objClass.getSimpleName() + "] does not support "
                + "a JSON Serializer.");
      }

      result = serializer.toJSONObject(obj);
    } catch (Exception exp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".serialize Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * A static method for retrieving the <tt>objClass</tt>'s JsonSerializer - assigned as
   * a static field with a generically reference JsonSerializer.
   * @param objClass the class with the assigned JsonSerializer
   * @return the JsonSerializer
   * @throws IOException if <tt>objClass</tt> = null, the JsonSerializercannot be
   * retrieved.
   */
  public static <TClass extends Serializable> JsonSerializer<TClass>
                              getSerializer(Class<TClass> objClass) throws IOException {
    JsonSerializer<TClass> result = null;
    try {
      if (objClass == null) {
        throw new IOException("The Target Class has no been specified.");
      }

      Class<JsonSerializer<TClass>> fieldType =
                          ReflectionInfo.castAsSpecificGenericClass(JsonSerializer.class);

      for (Field field : objClass.getDeclaredFields()) {
        if ((Modifier.isStatic(field.getModifiers())) &&
            (Modifier.isPublic(field.getModifiers())) &&
            (field.getType().equals(fieldType))) {
          result = (JsonSerializer<TClass>) field.get(objClass);
          break;
        }
      }
    } catch (SecurityException | IllegalAccessException secExp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".getSerializer AccessError Error:\n " + secExp.getMessage(), secExp);
    } catch (IllegalArgumentException secExp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".getSerializer Argument Error:\n " + secExp.getMessage(), secExp);
    } catch (IOException exp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".getSerializer IOError Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Get whether the <tt>objClass</tt> is JSON serializable (i.e., whether the class
   * has an assigned public static {@linkplain JsonSerializer} field - a elegate to
   * handle the toJson and fromJson request.
   * @param objClass the class to assess
   * @return true if <tt>objClass</tt>'s static JsonSerializer is assigned.
   */
  public final static boolean isSerializable(Class<? extends Serializable> objClass) {
    boolean result = false;
    if (objClass != null) {
      try {
        result = (JsonSerializer.getSerializer(objClass) != null);
      } catch (Exception exp) {
      }
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The JsonSerializer's Host Class
   */
  public final Class<THost> hostClass;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public JsonSerializer(Class<THost> hostClass) {
    super();
    if ((this.hostClass = hostClass) == null) {
      throw new NullPointerException("The JsonSerializer's HostClass is unassigned.");
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Call to serialize <tt>obj</tt> - a JSON serializable object - to a JSON-formatted
   * string. If calls {@linkplain #toJSONObject(java.io.Serializable) toJSONObject(obj)}
   * and return the resulting JSONObject as a string.
   * @param obj the object to serialize
   * @return the JSON-formatted string.
   * @throws IOException if the serialization process failed.
   */
  public final String toJSONString(THost obj) throws IOException {
    JSONObject result = this.toJSONObject(obj);
    return (result == null)? null: result.toString();
  }

  /**
   * Call to serialize <tt>obj</tt> - a JSON serializable object - to a JSONObject. It
   * retrieves the obj.class' JsonDef annotation, locate the toJson Method, and invoke
   * the method to return the output JSONObject.
   * @param obj the object to serialize
   * @return the JSON-formatted string.
   * @throws IOException if the serialization process failed.
   */
  public final JSONObject toJSONObject(THost obj) throws IOException {
    JSONObject result = null;
    try {
      if (obj == null) {
        throw new IOException("The input object is unasssigned");
      }

      result = new JSONObject();
      this.onToJson(obj, result);

      result.put("objclass", this.hostClass.getName());
    } catch (Exception exp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".toJSONObject Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * ABSTRACT: Called by {@linkplain #toJSONObject(java.io.Serializable)
   * this.toJSONObject} to custom handle the serialization if <tt>obj</tt> to the
   * <tt>josnObj</tt>
   * @param obj the Object to serialize
   * @param jsonObj the JSONObject to which the properties must be assigned.
   * @throws IOException
   */
  protected abstract void onToJson(THost obj, JSONObject jsonObj) throws IOException;

  /**
   * Call to deserialize an instance that extends <tt>baseClass</tt> - a JSON serializable
   * object - from the <tt>jsonStr</tt> - a JSON-formatted string.
   * It initiates a JSONObject (jsonObj) from the <tt>jsonStr</tt> and calls
   * {@linkplain #fromJSONObject(org.json.JSONObject, java.lang.Class)
   * fromJSONObject(objClass, jsonObj)} and return the deserialized TObject result.
   * @param <TBase> The serialized object's base Class - must extends Serializable
   * @param baseClass the base class of the object to deserialize
   * @param jsonStr the JSON-formatted String
   * @return the deserialized instance of obsClass
   * @throws IOException if the serialization process failed.
   */
  public final THost fromJSONString(String jsonStr) throws IOException {
    if ((jsonStr = DataEntry.cleanString(jsonStr)) == null) {
      throw new IOException("The Input JSON String is null or empty");
    }
    JSONObject jsonObj = new JSONObject(jsonStr);
    return this.fromJSONObject(jsonObj);
  }

  /**
   * Call to deserialize an instance of <tt>objClass</tt> - a JSON serializable object -
   * from the <tt>jsonObj</tt>. It retrieves the obj.class' JsonDef annotation,
   * locate the fromJson Method, and invoke the method - passing in the <tt>josnObj</tt>
   * to return the deserialized objClass instance.
   * @param jsonObj the JSONObject containing the serialized properties of the objClass
   * instance.
   * @return the deserialized instance of obsClass
   * @throws IOException if the serialization process failed.
   */
  public final THost fromJSONObject(JSONObject jsonObj) throws IOException {
    THost result = null;
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unasssigned");
      }

      if ((result = this.onFromJson(jsonObj)) == null) {
        throw new Exception("The deserializing of the instance form JSON failed - it "
                + "returned null.");
      }
    } catch (Exception exp) {
      throw new IOException(JsonSerializer.class.getSimpleName()
              + ".toJSONObject Error:\n " + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * ASBSTRACT: Called by {@linkplain #fromJSONObject(org.json.JSONObject)
   * this.fromJSONObject} to custom handle the deserializing of an instance of type
   * THost from the properties in the jsonObj.
   * @param jsonObj the input JSONObject
   * @return the deserialized instance
   * @throws IOException if the serialization process failed.
   */
  protected abstract THost onFromJson(JSONObject jsonObj) throws IOException;
  // </editor-fold>
}
