package bubblewrap.http.controllers;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.http.request.HttpEvent;
import bubblewrap.http.request.HttpEventHandlingArgs;
import bubblewrap.http.request.IHttpRequestHandler;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Controller
@RequestMapping("/bwmbc")
public class ManagedBeanController extends MultiActionController {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger localLogger
          = Logger.getLogger(ManagedBeanController.class.getName());
  //</editor-fold>        
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ManagedBeanController() {
    super(); 
    WebApplicationContext webCtx = this.getWebApplicationContext();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * The AJAX Request handler for a specified ManagedBean (ID by its name) and a 
   * specified event (as supported by {@linkplain HttpEvent}). the managed bean must
   * implement the {@linkplain IHttpRequestHandler} interface and must be defined as
   * and Application bean in the <tt>applicationContext.xml</tt> or must be registered 
   * as and actionHandler class in the {@linkplain BwAppContext#registerActionHandler(
   * java.lang.Class, java.lang.Class, java.lang.String) BwAppContext} for baseClass =
   * IHttpRequestHandler.
   * @param beanName the bean name
   * @param event the request event
   * @param request the HttpServletRequest
   * @param response HttpServletResponse
   * @throws IOException 
   */
  @RequestMapping("/{beanName}/{event}")
  public void onEvent(@PathVariable("beanName") String beanName, 
                     @PathVariable("event") String event, 
                     HttpServletRequest request, HttpServletResponse response) 
                     throws IOException {
    if ((request == null) || (response == null)) {
      throw new IOException("The Request and/or Response is not accessible.");
    }
    
    try {
      beanName = DataEntry.cleanString(beanName);
      if (beanName == null) {
        throw new Exception("The Request's ManagedBean Name is undefined.");
      }
      
      IHttpRequestHandler reqHandler = SessionHelper.getRequestHandler(beanName);
      if (reqHandler == null) {
        throw new Exception("Unable to locate HttpRequestHandler[" + beanName + "].");
      }
      
      HttpEvent httpEvent = HttpEvent.fromEvent(event);
      HttpEventHandlingArgs args = 
                                new HttpEventHandlingArgs(httpEvent, request, response);
      reqHandler.onHttpEvent(args);
      if (!args.isHandled()) {
        response.getWriter().write("");
        response.getWriter().close();
      } else if (args.hasError()) {
        throw new Exception(args.getErrorMsg("ManagedBean[" 
                                                      + beanName + ".onEvent Error"));
      }
    } catch (Exception exp) {
      localLogger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      if (!response.isCommitted()) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                    exp.getMessage());
      }
    }
  }
  // </editor-fold>
}
