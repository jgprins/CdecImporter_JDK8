package bubblewrap.http.session;

import bubblewrap.io.DataEntry;
import java.io.Serializable;

/**
 * A class for defining a HTTP Request key-value parameter pair
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class RequestParam implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The assigned Parameter Key (not empty)
   */
  public final String parKey;
  /**
   * The assigned Parameter Value (can be empty string)
   */
  public final String parValue;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public RequestParam(String parKey, String parValue) {
    super();  
    if ((this.parKey = DataEntry.cleanString(parKey)) == null) {
      throw new NullPointerException("The Parameter Key cannot be null or an empty "
              + "string");
    }
    this.parValue = ((parValue = DataEntry.cleanString(parValue)) == null)? "": parValue;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: return"RequestParam[ key=" + this.parKey + "; value=" + this.parValue
   * + "]" </p>
   */
  @Override
  public String toString() {
    return "RequestParam[ key=" + this.parKey + "; value=" + this.parValue + "]";
  }
  // </editor-fold>
}
