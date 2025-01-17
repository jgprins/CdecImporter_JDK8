package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONObject;

/**
 * A FieldValueConverter to convert DateTime to/from a DateTime String. See {@linkplain
 * #onSetOptions(org.json.JSONObject) onSetOptions} for details on the supported
 * conversion options.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class DateTimeConverter extends FieldValueConverter<DateTime> {
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The optional DateTime format that can be assigned as an option
   */
  private String format;
  /**
   * The optional TimeZone ID that can be assigned as an option
   */
  private String timeZoneId;
  // </editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public DateTimeConverter() {
    super();
    this.format = null;
    this.timeZoneId = null;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override FieldValueConverter">
  /**
   * Convert the specified value to a DateTime String value
   * @param value the input value
   * @return {@linkplain #toStringValue(java.lang.Object) 
   * this.toStringValue(value)}.
   */
  @SuppressWarnings("unchecked")
  @Override
  public DateTime toFieldValue(Object value) {    
    DateTime result = null;
    if (value != null) {
      if (value instanceof String) {
        String dtStr = DataEntry.cleanString((String) value);
        if (dtStr != null) {
          try {
            if (this.format == null) {
              result = DateTime.FromString(dtStr);
            } else {
              TimeZone timeZn = (this.timeZoneId == null) ? null : 
                                       TimeZone.getTimeZone(this.timeZoneId);
              result = DateTime.FromString(dtStr, this.format, timeZn);
            }
          } catch (Exception exp) {
            throw new IllegalArgumentException(exp.getMessage());
          }
        }
      } else if (value instanceof Date) {
        result = new DateTime((Date) value);
      } else if (value instanceof DateTime) {
        result = (DateTime) value;
      }
    }
    return result;
  }

  /**
   * Convert the specified <tt>value</tt> to a Date-Time String. The <tt>value</tt> can
   * be a string (in which case it is only pasted thru), a Date (which is converted to
   * a DateTime) or a DateTime. The dateTime value is converted to String using either
   * the {@linkplain DateTime#toString() toString} method (if this.format = null) or
   * {@linkplain DateTime#toLocaleString(java.lang.String) toLocaleString(this.format)}
   * method.
   * @param value the input value
   * @return the date-time string
   */
  @SuppressWarnings("unchecked")
  @Override
  public String toStringValue(Object value) {
    String result = null;
    if ((value != null)) {
      if (value instanceof String) {
        result = (String) value;
      } else if (value instanceof Date) {
        DateTime dtVal = new DateTime((Date) value);
        if (this.format != null) {
          result = dtVal.toLocaleString(this.format);
        } else {
          result = dtVal.toString();
        }
      } else if (value instanceof DateTime) {
        DateTime dtVal = (DateTime) value;
        if (this.format != null) {
          result = dtVal.toLocaleString(this.format);
        } else {
          result = dtVal.toString();
        }
      }
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: Return true if ((value == null) || (value instanceof Integer) ||
   * (value instanceof Date) || (value instanceof DateTime))</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return ((value == null) || (value instanceof String)
            || (value instanceof Date) || value instanceof DateTime);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Set this.format and this.timeZoneId assuming the options are defined as
   * {"format=...", "timezone=..."}.</p>
   */
  @Override
  protected void onSetOptions(JSONObject options) {
    this.format = null;
    this.timeZoneId = null;
    if ((options == null) || (options.length() == 0)) {
      return;
    }
    
    this.format = DataEntry.cleanString(options.optString("format", ""));
    this.timeZoneId = DataEntry.cleanString(options.optString("timezone", ""));
  }
  // </editor-fold>
}
