package bubblewrap.http.request;

import bubblewrap.core.events.CancelRequestArgs;
import bubblewrap.io.DataEntry;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject; 

/**
 * A CancelRequestArgs containing reference to the HttpServletRequest to process and
 * the Object instance to update
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class HttpEventHandlingArgs extends CancelRequestArgs {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(HttpEventHandlingArgs.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Instance to Update
   */
  public final HttpEvent event;
  /**
   * The HttpRequest to handle
   */
  private final HttpServletRequest request;
  /**
   * The HttpRequest to handle
   */
  private final HttpServletResponse response;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public HttpEventHandlingArgs(HttpEvent event, HttpServletRequest request, HttpServletResponse response) {
    super(); 
    this.request = request;
    this.response = response;
    this.event = event;
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
  
  // <editor-fold defaultstate="collapsed" desc="Public Response Methods">
  /**
   * Call to send a JSON response to the client. It set stringResponse =
   * (jsonResponse == null)? null: jsonResponse.toString(), and then calls {@linkplain
   * #setResponse(java.lang.String) this.setResponse(stringResponse)}
   * @param jsonResponse the response (can be null)
   */
  public final void setResponse(JSONObject jsonResponse) {
    String stringResponse = (jsonResponse == null) ? null : jsonResponse.toString();
    this.setResponse(stringResponse);
  }

  /**
   * Call to send a response to the client. Once done, this.isHandled will be set.
   * This call is ignored if this.isHandled or this.hasError or the HttpResponse
   * is already committed.
   * <p>
   * <b>NOTE:</b> Once called, the response will be immediately send to the client</p>
   * @param stringResponse the response
   */
  public final void setResponse(String stringResponse) {
    if ((this.isHandled()) || (this.hasError()) || (this.response.isCommitted())) {
      this.setResult(true);
      return;
    }
    
    PrintWriter writer = null;
    try {
      if ((writer = this.response.getWriter()) != null) {
        writer.write(stringResponse);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setResponse Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.setResult(true);
      if (writer != null) {
        writer.close();;
      }
    }
  }

  /**
   * Called to send redirect response to the client.
   * @param redirectURL the redirect page's URL
   */
  public final void sendRedirect(String redirectURL) {
    if ((this.isHandled()) || (this.hasError())) {
      return;
    }
    
    PrintWriter writer = null;
    try {
      redirectURL = DataEntry.cleanString(redirectURL);
      if ((redirectURL == null) && ((writer = this.response.getWriter()) != null)) {
        writer.write("");
      } else {
        this.response.sendRedirect(redirectURL);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setResponse Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    } finally {
      this.setResult(true);
      if (writer != null) {
        writer.close();
      }
    }
  }
  // </editor-fold>
}
