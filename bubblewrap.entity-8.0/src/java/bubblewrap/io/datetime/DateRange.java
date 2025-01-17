package bubblewrap.io.datetime;

import bubblewrap.io.DataEntry;
import bubblewrap.io.JsonSerializer;
import bubblewrap.io.ValueRange;
import java.io.IOException;
import org.json.JSONObject;

/**
 * A ValueRange for DateTimw values. This class is JSON serializable
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateRange extends ValueRange<DateTime>{

  //<editor-fold defaultstate="collapsed" desc="JSON Serializer Implementation">
  //<editor-fold defaultstate="collapsed" desc="Public Static JSON Serializer">
  /**
   * A Custom JSON Serializer for DateRange to and from a JSONObject using a custom
   * JsonSerializer.
   */
  public static JsonSerializer<DateRange> jsonSerializer
    = new JsonSerializer<DateRange>(DateRange.class) {
      /**
       * This implementation check the validity of <tt>obj</tt> and <tt>jsonObj</tt>
       * and call {@linkplain #toJSON(org.json.JSONObject) obj.toJSON(jsonObj)} to
       * serialize the <tt>obj</tt>'s field values to <tt>jsonObj</tt>.
       * @param obj the instance to serialize
       * @param jsonObj the output JSONObject to which the field values are assigned.
       * @throws IOException obj or jsonObj = null or a serialization error occurred.
       */
      @Override
      protected void onToJson(DateRange obj, JSONObject jsonObj) throws IOException {
        try {
          if (obj == null) {
            throw new IOException("The object to serialize is unassigned.");
          }
          if (jsonObj == null) {
            throw new Exception("The output JSONObject is unassigned.");
          }

          obj.toJSON(jsonObj);
        } catch (Exception exp) {
          throw new IOException(DateRange.class.getSimpleName()
                  + ".onToJson Error:\n " + exp.getMessage(), exp);
        }
      }

      /**
       * This implementation initiates a DateRange instance and call its
       * {@linkplain #fromJSON(org.json.JSONObject)  this.fromJSON(jsonObj)} method
       * to deserialize the instance's field values.
       * @param jsonObj the input JSONObject that contains the field values.
       * @return the new instance
       * @throws IOException if josnObj = null | empty or a serialization error
       * occurred.
       */
      @Override
      protected DateRange onFromJson(JSONObject jsonObj) throws IOException {
        DateRange result = null;
        try {
          if ((jsonObj == null) || (jsonObj.length() == 0)) {
            throw new Exception("The input JSONObject is unassigned or empty.");
          }

          result = new DateRange();
          result.fromJSON(jsonObj);
        } catch (Exception exp) {
          throw new IOException(DateRange.class.getSimpleName()
                  + ".onFromJson Error:\n " + exp.getMessage(), exp);
        }
        return result;
      }
    };
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Parameterless Constructor">
  /**
   * Public Parameterless Constructor for JSON deserialization and cloning
   */
  public DateRange() {
    super();
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="JSON Public Serilization Methods">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: The following field values are serialized to the <tt>jsonObj</tt>:<ul>
   * <li>"min"<b>:</b> = this.min.toString()</li>
   * <li>"max"<b>:</b> = this.max.toString()</li>
   * </ul>
   */
  @Override
  protected void onToJSON(JSONObject jsonObj) throws IOException {
    DateTime dt = this.getMin();
    if (dt != null) {
      String dtStr = dt.toString();
      jsonObj.put("min", dtStr);
    }
    dt = this.getMax();
    if (dt != null) {
      String dtStr = dt.toString();
      jsonObj.put("max", dtStr);
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: This override deserialize the field values serialized in {@linkplain
   * #onToJSON(org.json.JSONObject) this.onToJSON(jsonObj)}:
   */
  @Override
  protected void onFromJSON(JSONObject jsonObj) throws IOException {
    try {
      DateTime min = null;
      String dtStr = null;
      if ((dtStr = DataEntry.cleanString(jsonObj.optString("min",null))) != null) {
        min = DateTime.FromString(dtStr);
      }
      
      DateTime max = null;
      if ((dtStr = DataEntry.cleanString(jsonObj.optString("max",null))) != null) {
        max = DateTime.FromString(dtStr);
      }
      
      this.setRange(min, max);
    } catch (Exception exp) {
      throw new IOException(exp);
    }
  }
  // </editor-fold>
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Public Constructor with a custom comparator
   * @param comparator a DateTime Comparator
   */
  public DateRange(DateTimeComparator comparator) {
    super(comparator);  
  }
  
  /**
   * Public Constructor with a set of DateTime values
   * @param value the initial values to grow the range
   */
  public DateRange(DateTime...values) {
    super(values);
  }
  
  /**
   * Public Constructor with with a custom comparator and a set of DateTime values
   * @param comparator a DateTime Comparator
   * @param value the initial values to grow the range
   */
  public DateRange(DateTimeComparator comparator, DateTime...values) {
    super(comparator, values);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="ValueRange Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (value != null)</p>
   */
  @Override
  protected boolean isValidValue(DateTime value) {
    return (value != null);
  }
  // </editor-fold>
}
