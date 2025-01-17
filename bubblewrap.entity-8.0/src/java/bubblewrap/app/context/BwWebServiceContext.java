package bubblewrap.app.context;

import bubblewrap.io.DataEntry;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwWebServiceContext implements Serializable {    
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
          Logger.getLogger(BwWebServiceContext.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Singleton Methods">
  /**
   * Placeholder for the Static MyAppContext instance.
   */
  private static BwWebServiceContext ctxInstance = null;
  
  /**
   * Get a reference to the Singleton BwWebServiceContext. If not initiated, it will
   * be initiated using the current session's FacesContext. If the latter is not
   * accessible, it will return null.
   * @return
   */
  public static BwWebServiceContext doLookUp() {
    if (BwWebServiceContext.ctxInstance != null) {
      try {
        FacesContext faceCtx = FacesContext.getCurrentInstance();
        if (faceCtx != null) {
          BwWebServiceContext.ctxInstance = new BwWebServiceContext(faceCtx);
        }
      } catch (Exception pExp) {
        BwWebServiceContext.ctxInstance = null;
        logger.log(Level.WARNING, "{0}.initWebAppContext Error:\n {1}",
                new Object[]{"BwWebServiceContext", pExp.getMessage()});
      }
    }
    return BwWebServiceContext.ctxInstance;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fileds">
  /**
   * Placeholder for the Web Server Info (Name and version).
   */
  private String msServerInfo;
  /**
   * Placeholder for the Web Server's Major Version.
   */
  private int miVersionMajor;
  /**
   * Placeholder for the Web Server's Minor Version.
   */
  private int miVersionMinor;
  /**
   * Placeholder for the Web Application's ProjectStage (set in web.xml).
   */
  private String msProjectStage;
  /**
   * Placeholder for the Web Application's context path (can be null).
   */
  private String msContextPath;
  /**
   * Placeholder for the Web Application's Servlet path (typically 'faces').
   */
  private String msServletPath;
  /**
   * Placeholder for the Web Application's Host Url (e.g.. "http://www.myapp.com").
   */
  private String msHost;
  /**
   * File System Path to the Application's resource directory.
   */
  private String msResourcePath;
  //</editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with FacesContext reference - The FacesContext and its
   * ExternalContext, ServletContext, and HttpServletRequest references must exist.
   * @exception Exception - all errors are trapped and logged.
   */
  private BwWebServiceContext(FacesContext facesCtx) {
    try {
      if (facesCtx == null) {
        throw new Exception("The FacesContext cannot be unassigned.");
      }
      ExternalContext pExtCtx = facesCtx.getExternalContext();
      if (pExtCtx == null) {
        throw new Exception("Unable to access the FacesContext's "
                + "ExternalContext");
      }
      ServletContext pServlet = (ServletContext) pExtCtx.getContext();
      if (pServlet == null) {
        throw new Exception("Unable to access the FacesContext's "
                + "ServletContext");
      }
      HttpServletRequest pRequest = (HttpServletRequest) pExtCtx.getRequest();
      if (pRequest == null) {
        throw new Exception("Unable to access the FacesContext's "
                + "HttpServletRequest");
      }

      this.msServerInfo = pServlet.getServerInfo();
      this.miVersionMajor = pServlet.getMajorVersion();
      this.miVersionMinor = pServlet.getMinorVersion();
      this.msContextPath = DataEntry.cleanString(pServlet.getContextPath());
      this.msResourcePath = pServlet.getRealPath("/");
      this.msProjectStage = null;
      Enumeration<String> pNames = pServlet.getInitParameterNames();
      while (pNames.hasMoreElements()) {
        String sName = pNames.nextElement();
        if ((sName != null) && (sName.endsWith("PROJECT_STAGE"))) {
          this.msProjectStage = pServlet.getInitParameter(sName);
        }
      }

      this.msServletPath = DataEntry.cleanString(pRequest.getServletPath());

      Map pHeaderMap = pExtCtx.getRequestHeaderMap();
      String sHost = DataEntry.cleanString((String) pHeaderMap.get("host"));
      if (sHost != null) {
        int iPos = sHost.indexOf("://");
        if (iPos >= 0) {
          sHost = sHost.substring(iPos+3);
        }
        if (sHost.endsWith("/")) {
          if (sHost.equals("/")) {
            sHost = null;
          } else {
            sHost = sHost.substring(0,sHost.length()-2);
          }
        }
        sHost = DataEntry.cleanString(sHost);
      }

      if (sHost == null) {
        throw new Exception("Unabel to extract the Application's Host URL.");
      }
      this.msHost = "http://" + sHost;

      if (this.msContextPath != null) {
        if (!this.msContextPath.startsWith("/")) {
          this.msContextPath = "/" + this.msContextPath;
        }

        if (this.msContextPath.endsWith("/")) {
          if (this.msContextPath.equals("/")) {
            this.msContextPath = null;
          } else {
            this.msContextPath = 
                          this.msContextPath.substring(0,msContextPath.length()-2);
          }
        }          
      }
      if (this.msServletPath != null) {
        if (!this.msServletPath.startsWith("/")) {
          this.msServletPath = "/" + this.msServletPath;
        }
        if (this.msServletPath.endsWith("/")) {
          if (this.msServletPath.equals("/")) {
            this.msServletPath = null;
          } else {
            this.msServletPath = 
                          this.msServletPath.substring(0,msServletPath.length()-2);
          }
        } 
      }        
    } catch (Exception pExp) {
      throw new NullPointerException(this.getClass().getSimpleName()
              + ".new Error:\n\t " + pExp.getMessage());
    }
  }
  // </editor-fold>
    
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Web Server's Information (i.e. name and version)
   * @return the assigned value
   */
  public String getServerInfo() {
    return this.msServerInfo;
  }
  
  /**
   * Get the Web Server's major version
   * @return the assigned value
   */
  public int getMajorVersion() {
    return this.miVersionMajor;
  }
  
  /**
   * Get the Web Server's Minor version
   * @return the assigned value
   */
  public int getMinorVersion(){
    return this.miVersionMinor;
  }
  
  /**
   * Get the Application's Project Stage as assigned to the web.xml.
   * @return the assigned value (can be null|"".
   */
  public String getProjectStage() {
    return this.msProjectStage;
  }
  
  /**
   * Get the file system path for the relative resource path
   * @param sRelativePath a relative resource path (can be null to get the
   * ResourcePath).
   * @return the file system path
   * @exception Exception - all errors are trapped and logged.
   */
  public File getRealPath(String sRelativePath) {
    File pResult = null;
    try {
      sRelativePath = DataEntry.cleanString(sRelativePath);
      if (sRelativePath == null) {
        pResult = new File(this.msResourcePath);
      } else {
        pResult = new File(this.msResourcePath, sRelativePath);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  
  /**
   * Get the Web Application's request's contextPath (can be null)
   * @return the assigned value
   */
  public String getContextPath(){
    return this.msContextPath;
  }
  
  /**
   * Get the Web Application's request's servletPath (can be null, typically "faces")
   * @return the assigned value
   */
  public String getServletPath(){
    return this.msServletPath;
  }
  
  /**
   * Get the Relative URI for the specified relative resource path. It adds the
   * relative path to the contextPath/servletPath.
   * @param sRelativePath the specified relative path (if null assume "/").
   * @return the build path.
   * @exception Exception - all errors are trapped and logged.
   */
  public String getResourceUri(String sRelativePath) {
    String sResult = null;
    try {
      String sResUri = (this.msContextPath == null)? "": this.msContextPath;
      sResUri += (this.msServletPath== null)? "": this.msServletPath;
      
      sRelativePath = DataEntry.cleanString(sRelativePath);
      if (sRelativePath == null) {
        sRelativePath = "";
      } else {
        sRelativePath = sRelativePath.replace("\\", "/");
      }
      
      if (!sRelativePath.startsWith("/")) {
        sResUri += "/";
      }
      sResult = sResUri + sRelativePath;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return sResult;
  }
  
  /**
   * Get the full URL path for the relative resource path. It append the relative
   * path to this.hostURI/contextPath/servletPath.
   * @param sRelativePath the specified relative path (if null assume "/").
   * @return the build path.
   * @exception Exception - all errors are trapped and logged.
   */
  public String getFullURL(String sRelativePath) {
    String sResult = null;
    try {
      String sResUri = this.getResourceUri(sRelativePath);
      sResult = this.msHost + sResUri;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return sResult;
  }
  
  /**
   * Get the Application's Host URL
   * @return the assigned value (e.g., "http://www.myapp.com")
   */
  public String getHostURL() {
    return this.msHost;
  }
  
  /**
   * Get the Application's base URL.
   * @return this.hostUrl/this.contextPath or null if an error occurred.
   * @exception Exception - all errors are trapped and logged.
   */
  public URL getWebAppURL() {
    URL pResult = null;
    try {
      String sUrl = this.msHost;
      if (this.msContextPath != null) {
        sUrl += this.msContextPath;
      }
      pResult = new URL(sUrl);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getWebAppURL Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>  
}
