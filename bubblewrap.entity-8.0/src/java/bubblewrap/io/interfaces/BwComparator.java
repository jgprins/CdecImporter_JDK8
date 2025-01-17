package bubblewrap.io.interfaces;

import bubblewrap.io.JsonSerializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import org.json.JSONObject;

/**
 * The BwComparator implements both Comparator and Serializable to have a serializable
 * Comparator. It extends the signature of Comparator by adding a protected parameterless
 * constructor, and the to- and fromJSOn serialization method that makes inheritors of 
 * BwComparator JSON serializable..
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class BwComparator<T> implements Comparator<T>, Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="JSON Serializer Implementation">
  // <editor-fold defaultstate="collapsed" desc="Protected Parameterless Constructor">
  /**
   * Protected Parameterless Constructor for JSON deserialization
   */
  protected BwComparator() {
    super();
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="JSON Public Serilization Methods">
  /**
   * Called to serialize this instance of BwComparator to <tt>jsonObj</tt>.
   * The assigned field values are as follows:<ul>
   * <li><b>:</b> </li>
   * </ul>
   * <p>
   * After serializing this instance's field values it calls {@linkplain
   * #onToJSON(org.json.JSONObject) this.onToJSON(jsonObj)} to handle the serialization
   * of inherited classes.
   * @param jsonObj the JSONObject to the assigned field values to
   * @throws IOException if the process failed.
   */
  public final void toJSON(JSONObject jsonObj) throws IOException {
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unassigned.");
      }

      /* Call onFromJSON to handle inherited serialization */
      this.onToJSON(jsonObj);
    } catch (Exception exp) {
      throw new IOException(BwComparator.class.getSimpleName()
              + ".toJson Error:\n " + exp.getMessage(), exp);
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #toJSON(org.json.JSONObject)
   * this.toJSON(fromJSON)} to serialize the field values of inherited classes.
   * The base method does nothing.
   * @param jsonObj the JSONObject to assigned the instance's field values to.
   * @throws IOException if the process failed.
   * */
  protected void onToJSON(JSONObject jsonObj) throws IOException {
  }

  /**
   * Called to deserialize this properties of this instance of BwComparator
   * from a JSONObject. The instance properties were as set in the {@linkplain
   * #toJSON(org.json.JSONObject) toJSON} method.
   * <p>
   * After serializing this instance's field values it calls {@linkplain
   * #onFromJSON(org.json.JSONObject) this.onFromJSON(jsonObj)} to handle the
   * deserialization of inherited classes.
   * @param jsonObj the JSONObject containing the instance's field values.
   * @throws IOException if the process failed.
   */
  public final void fromJSON(JSONObject jsonObj) throws IOException {
    try {
      if (jsonObj == null) {
        throw new Exception("The input JSONObject is unassigned.");
      }
      /* Call onFromJSON to hanle inherited deserialization */
      this.onFromJSON(jsonObj);
    } catch (Exception exp) {
      throw new IOException(BwComparator.class.getSimpleName()
              + ".fromJSON Error:\n " + exp.getMessage(), exp);
    }
  }

  /**
   * CAN OVERRIDE: Called by {@linkplain #fromJSON(org.json.JSONObject)
   * this.fromJSON(fromJSON)} to deserialize the field values of inherited classes.
   * The base method does nothing.
   * @param jsonObj the JSONObject containing the instance's field values.
   * @throws IOException if the process failed.
   * */
  protected void onFromJSON(JSONObject jsonObj) throws IOException {
  }
  // </editor-fold>
  // </editor-fold>
}
