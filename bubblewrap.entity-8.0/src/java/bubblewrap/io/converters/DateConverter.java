package bubblewrap.io.converters;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONObject;

/**
 * A FieldValueConverter to convert Date value to/from strings.  See {@linkplain
 * #onSetOptions(org.json.JSONObject) onSetOptions} for details on the supported
 * conversion options.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class DateConverter extends FieldValueConverter<Date> {
 
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
  public DateConverter() {
    super();
    this.format = null;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override FieldValueConverter">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return the input <tt>value</tt> to Date value. If <tt>value</tt> is a
   * String, it is converted to a DateTime using the specified </p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public Date toFieldValue(Object value) {
    Date result = null;
    if ((value != null)) {
      if (value instanceof String) {
        String dtStr = DataEntry.cleanString((String) value);
        if (dtStr != null) {
          DateTime dtVal = null;
          try {
            if (this.format == null) {
              dtVal = DateTime.FromString(dtStr);
            } else {
              TimeZone timeZn
                      = (this.timeZoneId == null) ? null : TimeZone.getTimeZone(this.timeZoneId);
              dtVal = DateTime.FromString(dtStr, this.format, timeZn);
            }
          } catch (Exception exp) {
            throw new IllegalArgumentException(exp.getMessage());
          }
          
          result = (dtVal == null) ? null : dtVal.getAsDate();
        }
      } else if (value instanceof Date) {
        result = (Date) value;
      } else if (value instanceof DateTime) {
        DateTime dtVal = (DateTime) value;
        result = dtVal.getAsDate();
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
   * OVERRIDE: Return true if ((value == null) || (value instanceof Date) ||
   * (value instanceof DateTime))</p>
   */
  @Override
  public boolean isValidInput(Object value) {
    return ((value == null) || (value instanceof Date) || value instanceof DateTime);
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Set this.format and this.tiemZoneId assuming the options are defined as
   * ["format=...", "timezone=..."].</p>
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
