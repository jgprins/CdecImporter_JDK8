package bubblewrap.io.datetime;

import bubblewrap.io.interfaces.BwComparator;
import bubblewrap.io.JsonSerializer;
import bubblewrap.io.schedules.enums.Interval;
import java.io.IOException;
import org.json.JSONObject;

/**
 * A DateTime Comparator that uses an specified {@linkplain Interval} for comparing the
 * equality of two dates.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DateTimeComparator extends BwComparator<DateTime>{
  
  //<editor-fold defaultstate="collapsed" desc="JSON Serializer Implementation">
  //<editor-fold defaultstate="collapsed" desc="Public Static JSON Serializer">
  /**
   * A Custom JSON Serializer for DateTimeComparator to and from a JSONObject using a custom
   * JsonSerializer.
   */
  public static JsonSerializer<DateTimeComparator> jsonSerializer
    = new JsonSerializer<DateTimeComparator>(DateTimeComparator.class) {
      /**
       * This implementation check the validity of <tt>obj</tt> and <tt>jsonObj</tt>
       * and call {@linkplain #toJSON(org.json.JSONObject) obj.toJSON(jsonObj)} to
       * serialize the <tt>obj</tt>'s field values to <tt>jsonObj</tt>.
       * @param obj the instance to serialize
       * @param jsonObj the output JSONObject to which the field values are assigned.
       * @throws IOException obj or jsonObj = null or a serialization error occurred.
       */
      @Override
      protected void onToJson(DateTimeComparator obj, JSONObject jsonObj) throws IOException {
        try {
          if (obj == null) {
            throw new IOException("The object to serialize is unassigned.");
          }
          if (jsonObj == null) {
            throw new Exception("The output JSONObject is unassigned.");
          }

          obj.toJSON(jsonObj);
        } catch (Exception exp) {
          throw new IOException(DateTimeComparator.class.getSimpleName()
                  + ".onToJson Error:\n " + exp.getMessage(), exp);
        }
      }

      /**
       * This implementation initiates a DateTimeComparator instance and call its
       * {@linkplain #fromJSON(org.json.JSONObject)  this.fromJSON(jsonObj)} method
       * to deserialize the instance's field values.
       * @param jsonObj the input JSONObject that contains the field values.
       * @return the new instance
       * @throws IOException if josnObj = null | empty or a serialization error
       * occurred.
       */
      @Override
      protected DateTimeComparator onFromJson(JSONObject jsonObj) throws IOException {
        DateTimeComparator result = null;
        try {
          if ((jsonObj == null) || (jsonObj.length() == 0)) {
            throw new Exception("The input JSONObject is unassigned or empty.");
          }

          result = new DateTimeComparator();
          result.fromJSON(jsonObj);
        } catch (Exception exp) {
          throw new IOException(DateTimeComparator.class.getSimpleName()
                  + ".onFromJson Error:\n " + exp.getMessage(), exp);
        }
        return result;
      }
    };
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Parameterless Constructor">
  /**
   * Protected Parameterless Constructor for JSON deserialization
   */
  protected DateTimeComparator() {
    super();
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="JSON Public Serilization Methods">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: The following field values are serialized to the <tt>jsonObj</tt>:<ul>
   * <li><b>"interval":</b> = this.compareInterval.value</li>
   * <li><b>"sortAsc":</b> = this.getSortArc()</li>
   * </ul>
   */
  @Override
  protected void onToJSON(JSONObject jsonObj) throws IOException {
    Integer intVal = this.compareInterval.value;
    jsonObj.put("interval", intVal);
    jsonObj.put("sortAsc", this.getSortArc());
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: This override deserialize the field values serialized in {@linkplain
   * #onToJSON(org.json.JSONObject) this.onToJSON(jsonObj)}:
   */
  @Override
  protected void onFromJSON(JSONObject jsonObj) throws IOException {
    Integer intVal = jsonObj.optInt("interval",0);
    this.compareInterval = Interval.fromValue(intVal);
    if ((this.compareInterval == null) || (Interval.NONE.equals(this.compareInterval))) {
      this.compareInterval = Interval.MILLISECONDS;
    }
    
    this.sortAsc = jsonObj.optBoolean("sortAsc",true);
    if (this.sortAsc) {
      this.sortAsc = null;
    }
  }
  // </editor-fold>
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The assigned Interval to use in the DateTime Comparison (default = null; compare
   * milliseconds)
   */
  private Interval compareInterval;
  /**
   * The sort order flag (default = null|true = Ascending)
   */
  private Boolean sortAsc;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public DateTimeComparator(Interval compareInterval) {
    super();  
    this.sortAsc = null;
    this.compareInterval = 
                      (compareInterval == null)? Interval.MILLISECONDS: compareInterval;
  }
  /**
   * Public Constructor with a comparator and a sort order.
   */
  public DateTimeComparator(Interval compareInterval, boolean sortAsc) {
    this(compareInterval);
    this.sortAsc = (sortAsc)? null: false;
  }

  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Comparator's DateTime Compare {@linkplain Interval}
   * @return this.compareInterval
   */
  public final Interval getCompareInterval() {
    return this.compareInterval;
  }

  /**
   * Get the comparator's sort order (default = true)
   * @return ((this.sortAsc == null) || (this.sortAsc))
   */
  public final boolean getSortArc() {
    return ((this.sortAsc == null) || (this.sortAsc));
  }

  // </editor-fold>
  // <editor-fold defaultstate="collapsed" desc="Comparator Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return "DateTimeComparator( interval=" + this.compareInterval.label + ")";
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Compare two date considering this.compareInterval. It handles the 
   * following cases:<ul>
   * <li>date1 = null and date2 = null; returns 0</li>
   * <li>date2 = null; returns 1</li>
   * <li>date1 = null; returns -1</li>
   * <li>date1.equals(date2, this.compareInterval); return 0</li>
   * <li>date1.isAfter(date2); returns 1</li>
   * <li>date1.isBefore(date2); return -1</li>
   * </ul>
   * if (!this.sortArc), the order will be switched.
   */
  @Override
  public int compare(DateTime date1, DateTime date2) {
    int result = 0;
    if (date1 != null) {
      if (date2 == null) {
        result = 1;
      } else {
        if (date1.equals(date2, this.compareInterval)) {
          result = 0;
        } else if (date1.isAfter(date2, this.compareInterval)) {
          result = 1;
        } else {
          result = -1;
        }
      }
    } else if (date2 != null) {
      result = -1;
    }
    
    if ((result != 0) && (!this.getSortArc())) {
      result = -1 * result;
    }
    return result;
  }
  // </editor-fold>
}
