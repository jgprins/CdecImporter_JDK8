package bubblewrap.http.session;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A List Iterator for passing or managing a set of RequestParam
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
 public class RequestParamSet implements Serializable, Iterable<RequestParam> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * An internal list for managing the assigned RequestParams
   */
  private List<RequestParam> paramSet;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with an array of parameters
   * @param params array of parameters can be null or empty
   */
  public RequestParamSet() {
    super();  
    this.paramSet = new ArrayList<>();
  }
  
  /**
   * Public Constructor with an array of parameters
   * @param params array of parameters can be null or empty
   */
  public RequestParamSet(RequestParam...params) {
    this();  
    this.addParams(params);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to add an array of parameters to the set
   * @param params array of parameters - ignored is null or empty
   */
  public final void addParams(RequestParam...params) {    
    if ((params != null) && (params.length > 0)) {
      this.paramSet.addAll(Arrays.asList(params));
    }
  }
  
  /**
   * Get the Parameter value for the specified <tt>parKey</tt>.
   * @param parKey the key to search for
   * @return the assigned value or null if not find or the value = "".
   */
  public String getValue(String parKey) {
    String result = null;
    if ((!this.isEmpty()) && ((parKey = DataEntry.cleanString(parKey)) != null)) {
      for (RequestParam param : this) {
        if ((DataEntry.isEq(parKey, param.parKey, true))) {
          result = DataEntry.cleanString(param.parValue);
        }
      }
    }
    return result;
  }
  
  /**
   * Get whether the set is empty.
   * @return this.paramSet.isEmpty()
   */
  public boolean isEmpty() {
    return this.paramSet.isEmpty();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Iterator Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: this.paramSet.iterator()</p>
   */
  @Override
  public Iterator<RequestParam> iterator() {
    return this.paramSet.iterator();
  }
  // </editor-fold>
}
