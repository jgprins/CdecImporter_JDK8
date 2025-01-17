package gov.water.cdec.importer.beans;

import gov.ca.water.cdec.facades.CdecEJBContext;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A managed bean use by the facePage to access system information.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
//@EJB(name="cdechttpinfo")
public class CdecHttpInfo {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecHttpInfo.class.getName());
  //</editor-fold>        

  //<editor-fold defaultstate="collapsed" desc="CdecHttpInfo Singleton">
  /**
   * Static class for holding and initiating the CdecHttpInfo singleton in.
   */
  private static class CdecHttpInfoHolder {
    private static final CdecHttpInfo INSTANCE = new CdecHttpInfo();
  }

  /**
   * Static method for accessing the Singleton
   *
   * @return CdecHttpInfoHolder.INSTANCE
   */
  public static CdecHttpInfo getInstance() {
    return CdecHttpInfoHolder.INSTANCE;
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Get the URI for the WebService
   *
   * @return this.cdecCtx.hostURL + "/import"
   */
  public static String getWebServiceUri() {
    String result = null;
    try {
      CdecHttpInfo info = CdecHttpInfo.getInstance();
      if (info == null) {
        throw new Exception("Initiating the CdecHttpInfo singleton failed.");
      }
      
      if ((result = info.onGetWebServiceUri()) == null) {
        throw new Exception("Accessing the WebService URI failed.");
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getWebServiceUri Error:\n {1}",
              new Object[]{CdecHttpInfo.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields"> 
  @Autowired
  ServletContext context; 
  /**
   * Placeholder for the EJB injected CdecEJBContext 
   */
  private CdecEJBContext cdecCtx;
  /**
   * Placeholder for the WebServiceUri
   */
  private String webServiceUri;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public CdecHttpInfo() {
    super();  
    this.cdecCtx = null;
    this.webServiceUri = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the URI for the WebService
   * @return this.cdecCtx.hostURL + "/import"
   */
  private String onGetWebServiceUri() {
    URL webUrl = null;
    CdecEJBContext ctx = null;
    if ((this.webServiceUri == null) && ((ctx = this.getCdecCtx()) != null) &&
            ((webUrl = ctx.getWebAppURL()) != null)) {
      this.webServiceUri = webUrl.toString();
      if (!this.webServiceUri.endsWith("/")) {
        this.webServiceUri += "/";
      }
      this.webServiceUri += "import";
    }     
    return this.webServiceUri;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Get a reference to the CdecEJBContext Singleton
   *
   * @return this.cdecCtx - lazy initiated
   */
  private CdecEJBContext getCdecCtx() {
    if (this.cdecCtx == null) {
      this.cdecCtx = CdecEJBContext.getInstance();
    }
    return this.cdecCtx;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
}
