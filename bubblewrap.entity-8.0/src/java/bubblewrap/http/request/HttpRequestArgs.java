package bubblewrap.http.request;

import bubblewrap.core.events.CancelRequestArgs;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 * A CancelRequestArgs containing reference to the HttpServletRequest to process and
 * the Object instance to update
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class HttpRequestArgs extends CancelRequestArgs {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Instance to Update
   */
  public final Object instance;
  /**
   * The HttpRequest to handle
   */
  private final HttpServletRequest request;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public HttpRequestArgs(HttpServletRequest request, Object instance) {
    super(); 
    this.request = request;
    this.instance = instance;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get whether the HTTPRequest submitted parameters include a parameter for the 
   * specified parameter name. The search is case sensitive. the parameter names must 
   * match.
   * @param parName the specified parameter name
   * @return true if the request != null and contains a parameter names <tt>parName</tt>
   */
  public boolean hasParamValue(String parName) {
    return (this.request != null) ? 
            this.request.getParameterMap().containsKey(parName) : false;
  }
  
  /**
   * Get the HTTPRequest submitted parameter value for the specified parameter name.
   * The search is case sensitive. the parameter names must match.
   * <p>
   * <b>NOTE:</b> If this is multivalued parameter only the first value is returned.</p>
   * @param parName the specified parameter name
   * @return {@linkplain HttpServletRequest#getParameter(java.lang.String)
   * this.request.getParameter(parName)} or null if this.request=null or this.request
   * does not contain the parameter
   */
  public String getParamValue(String parName) {
    return (this.request != null) ? this.request.getParameter(parName) : null;
  }

  /**
   * Get the HTTPRequest submitted parameter value (as an array of string) for the
   * specified parameter name. The search is case sensitive. the parameter names must
   * match.
   * <p>
   * @param parName the specified parameter name
   * @return {@linkplain HttpServletRequest#getParameterValues(java.lang.String)
   * this.request.getParameterValues(parName)} or null if this.request=null or
   * this.request does not contain the parameter
   */
  public String[] getParamValues(String parName) {
    return (this.request != null) ? this.request.getParameterValues(parName) : null;
  }
  // </editor-fold>
}
