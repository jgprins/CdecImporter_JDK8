package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.importers.ImportUtils;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.persistence.*;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.persistence.internal.jpa.transaction.JTATransactionWrapper;
import org.springframework.web.context.request.*;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Singleton()
public class CdecEJBContext implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(CdecEJBContext.class.getName());
  //</editor-fold>        

  //<editor-fold defaultstate="collapsed" desc="CdecEJBContext Singleton">
  /**
   * Placeholder for the singleton reference of the CdecEJBContext class
   */
  private static CdecEJBContext INSTANCE = null;

  /**
   * Static method for accessing the Singleton
   * @return CdecEJBContextHolder.INSTANCE
   */
  public static CdecEJBContext getInstance() {
    if (CdecEJBContext.INSTANCE == null) {
      CdecEJBContext newInst = new CdecEJBContext();
      newInst.initManager();
      //CdecEJBContext.INSTANCE =  newInst;
    }
    return CdecEJBContext.INSTANCE;
  }

  /**
   * Call ONLY form a TestUnit to initiate CdecEJBContext singleton instance. At runtime
   * the singleton instance is initiated when the application starts.
   */
  public static void initSingleton() {
    if (CdecEJBContext.INSTANCE != null) {
      return;
    }
    CdecEJBContext newInst = new CdecEJBContext();
    newInst.initManager();
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Fields">
  /**
   * The CDEC Persistent Unit's JDNI
   */
  public static final String PU_CDEC = "gov.ca.water.cdecPU";
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * EntityManager Injection
   */
  @PersistenceContext(unitName = "gov.ca.water.cdecPU")
  private EntityManager entityManager;
  /**
   * Map of Facade's
   */
  private HashMap<Class<? extends CdecBaseFacade>, CdecBaseFacade> facadeMap;
  /**
   * The Application Urls's ContextPath
   */
  private String contextPath;
  /**
   * The Application Urls's resourcePath
   */
  private String resourcePath;
  /**
   * The Application's Project Stage
   */
  private String projectStage;
  /**
   * The Application's Urls's servletPath
   */
  private String servletPath;
  /**
   * The Application's Host Url
   */
  private String hostUrl;
  /**
   * Flag indicating if the JTA transactions is used (default = null|true)
   */
  private Boolean useJTA;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Private Constructor for Singleton
   */
  public CdecEJBContext() {
    super();
  }

  /**
   * Call after constructing the Class to initiate the services.
   */
  @PostConstruct
  protected void initManager() {
    try {
      CdecEJBContext.INSTANCE = this;
      logger.log(Level.INFO, "{0}.initManager @ {1}",
        new Object[]{this.getClass().getSimpleName(), Calendar.getInstance().getTime()});
      if (this.getEntityManager() == null) {
        throw new Exception("EntityManager Injection Failed.");
      }

      this.facadeMap = new HashMap<>();
      this.initWebContext();
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.initManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }

  /**
   * Called before Destroying the instance.
   * It set DataImportScheduler.mpAppInstance = null.
   */
  @PreDestroy
  protected void shutdownManager() {
    try {
      logger.log(Level.INFO, "{0}.shutdownManager @ {1}",
        new Object[]{this.getClass().getSimpleName(), Calendar.getInstance().getTime()});
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.shutdownManager Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  /**
   * Called to initiate the Application's WebContext
   */
  private void initWebContext() {
    try {
      HttpServletRequest request = null;
      FacesContext facesCtx = null;
      RequestAttributes reqAttrs = RequestContextHolder.getRequestAttributes();
      if ((reqAttrs != null) &&
          (reqAttrs instanceof ServletRequestAttributes)) {
        if ((request = ((ServletRequestAttributes) reqAttrs).getRequest()) == null) {
          throw new NullPointerException("The HttpRequest is not accessible.");
        }
      } else {
        try {
          if ((facesCtx = FacesContext.getCurrentInstance()) != null) {
            ExternalContext extCtx = facesCtx.getExternalContext();
            if ((extCtx == null) ||
                    ((request = (HttpServletRequest) extCtx.getRequest()) == null) ) {
              throw new NullPointerException("The HttpRequest is not accessible.");
            }
          }
        } catch (Exception exp) {
          throw exp;
        }
      }
      
      if (request != null) {
        this.initWebContext(request);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initWebContext Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  /**
   * Called by a controller to initiate the WebContext from a known request -
   * skipped if already initiated
   * @param request the HTTP Request
   */
  public void initWebContext(HttpServletRequest request) {
    if ((this.hostUrl != null) || (request == null)) {
      return;
    }
    try{
      ServletContext servletCtx = (ServletContext) request.getServletContext();
      if (servletCtx == null) {
        throw new Exception("Unable to access the FacesContext's "
                + "ServletContext");
      }
      
      this.contextPath = ImportUtils.cleanString(request.getContextPath());
      this.resourcePath = servletCtx.getRealPath("/");
      this.projectStage = null;
      Enumeration<String> paramNames = servletCtx.getInitParameterNames();
      while (paramNames.hasMoreElements()) {
        String name =  paramNames.nextElement();
        if ((name != null) && (name.endsWith("PROJECT_STAGE"))) {
          this.projectStage = servletCtx.getInitParameter(name);
        }
      }

      this.servletPath = ImportUtils.cleanString(request.getServletPath());

      //Map headerMap = extCtx.getRequestHeaderMap();
      String host = ImportUtils.cleanString((String) request.getHeader("host"));
      if (host != null) {
        int iPos = host.indexOf("://");
        if (iPos >= 0) {
          host = host.substring(iPos + 3);
        }
        if (host.endsWith("/")) {
          if (host.equals("/")) {
            host = null;
          } else {
            host = host.substring(0, host.length() - 2);
          }
        }
        host = ImportUtils.cleanString(host);
      }

      if (host == null) {
        throw new Exception("Unable to extract the Application's Host URL.");
      }
      
      if ((host.startsWith("localhost")) || (!request.isSecure())) {
        this.hostUrl = "http://" + host;
      } else {
        this.hostUrl = "https://" + host;
      }

      if (this.contextPath != null) {
        if (!this.contextPath.startsWith("/")) {
          this.contextPath = "/" + this.contextPath;
        }

        if (this.contextPath.endsWith("/")) {
          if (this.contextPath.equals("/")) {
            this.contextPath = null;
          } else {
            this.contextPath
                    = this.contextPath.substring(0, contextPath.length() - 2);
          }
        }
      }
      if (this.servletPath != null) {
        if (!this.servletPath.startsWith("/")) {
          this.servletPath = "/" + this.servletPath;
        }
        if (this.servletPath.endsWith("/")) {
          if (this.servletPath.equals("/")) {
            this.servletPath = null;
          } else {
            this.servletPath
                    = this.servletPath.substring(0, servletPath.length() - 2);
          }
        }
      }
    } catch (Exception exp) {
      this.resetWebContext();
      logger.log(Level.WARNING, "{0}.initWebContext Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  /**
   * Called to to reset the WebContext if the initiation Failed.
   */
  private void resetWebContext() {
    this.contextPath = null;
    this.servletPath = null;
    this.hostUrl = null;
    this.projectStage = null;
    this.resourcePath = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return Em[gov.ca.water.cdecPU]</p>
   */
  public EntityManager getEntityManager() {
    EntityManagerFactory factory = null;
    this.useJTA = null;
    if ((this.entityManager == null) && ((factory
            = Persistence.createEntityManagerFactory(PU_CDEC)) != null)) {
      if ((this.entityManager = factory.createEntityManager()) == null) {
        throw new NullPointerException(this.getClass().getSimpleName()
                + ".entityManager is not accessible.");
      }
      try {
        EntityTransaction entityTx = this.entityManager.getTransaction();
        if ((entityTx == null) || (!(entityTx instanceof JTATransactionWrapper))) {
          this.useJTA = false;
        }
      } catch (Exception exp) {
        this.useJTA = null;
      }
    }
    return this.entityManager;
  }

  /**
   * Called by to retrieve the Facade by its class.
   * @param <TBean> the Entity Bean class
   * @param <TFacade> extends CdecBaseFacade
   * @param facadeClass the specified CdecBaseFacade class
   * @return the facade.
   */
  public <TBean extends Serializable, TFacade extends CdecBaseFacade<TBean>> TFacade
          getFacade(Class<TFacade> facadeClass) {
    TFacade result = null;
    try {
      if (this.facadeMap == null) {
        throw new Exception("The FacadeManager was not fully initiated.");
      }

      if (this.facadeMap.containsKey(facadeClass)) {
        result = (TFacade) this.facadeMap.get(facadeClass);
      } else {
        try {
          result = (TFacade) InitialContext.doLookup("java:module/"
                  + facadeClass.getSimpleName());
        } catch (Exception inExp) {
          logger.log(Level.INFO, "CdecBaseFacade.lookup[java:module/{0}] failed because:"
                  + "\n{1}", new Object[]{facadeClass.getSimpleName(), inExp.getMessage()});
        }

        if (result == null) {
          EntityManager em = this.getEntityManager();
          try {
            result = facadeClass.newInstance();
            if (result == null) {
              throw new Exception("Intiating Facade[" + facadeClass.getSimpleName()
                      + "] failed.");
            }

            result.setEntityManager(em);
          } catch (Exception inExp) {
            result = null;
            String errMsg = (inExp == null) ? null : inExp.getMessage();
            errMsg = ((errMsg == null) || ("".equals(errMsg.trim())))
                    ? "Intiating Facade[" + facadeClass.getSimpleName() 
                    + "] failed." : errMsg;
            throw new Exception(errMsg);
          }
        }
        
        result.setUseJTA(this.useJTA);
        
        if (result != null) {
          this.facadeMap.put(facadeClass, result);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getFacade Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
   
  /**
   * Get the flag indicating if the persistent unit use JTA transaction
   * (default = null|true)
   * @return ((this.useJTA == null) || (this.useJTA))
   */
  public boolean doUseJTA() {
    return ((this.useJTA == null) || (this.useJTA));
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Web Context Methods">
  /**
   * Get the Application's Project Stage as assigned to the web.xml.
   * @return the assigned value (can be null|"".
   */
  public String getProjectStage() {
    return this.projectStage;
  }
  
  /**
   * Get the file system path for the relative resource path
   * @param sRelativePath a relative resource path (can be null to get the
   * ResourcePath).
   * @return the file system path
   * @exception Exception - all errors are trapped and logged.
   */
  public File getRealPath(String sRelativePath) {
    File result = null;
    try {
      sRelativePath = ImportUtils.cleanString(sRelativePath);
      if (sRelativePath == null) {
        result = new File(this.resourcePath);
      } else {
        result = new File(this.resourcePath, sRelativePath);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the Web Application's request's contextPath (can be null)
   * @return the assigned value
   */
  public String getContextPath(){
    return this.contextPath;
  }
  
  /**
   * Get the Web Application's request's servletPath (can be null, typically "faces")
   * @return the assigned value
   */
  public String getServletPath(){
    return this.servletPath;
  }
  
  /**
   * Get the Relative URI for the specified relative resource path. It adds the
   * relative path to the contextPath/servletPath.
   * @param relativePath the specified relative path (if null assume "/").
   * @return the build path.
   * @exception Exception - all errors are trapped and logged.
   */
  public String getResourceUri(String relativePath) {
    String result = null;
    try {
      String resUri = (this.contextPath == null)? "": this.contextPath;
      resUri += (this.servletPath== null)? "": this.servletPath;
      
      relativePath = ImportUtils.cleanString(relativePath);
      if (relativePath == null) {
        relativePath = "";
      } else {
        relativePath = relativePath.replace("\\", "/");
      }
      
      if (!relativePath.startsWith("/")) {
        resUri += "/";
      }
      result = resUri + relativePath;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the full URL path for the relative resource path. It append the relative
   * path to this.hostURI/contextPath/servletPath.
   * @param relativePath the specified relative path (if null assume "/").
   * @return the build path.
   * @exception Exception - all errors are trapped and logged.
   */
  public String getFullURL(String relativePath) {
    String result = null;
    try {
      String sResUri = this.getResourceUri(relativePath);
      result = this.hostUrl + sResUri;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRealPath Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get the Application's Host URL
   * @return the assigned value (e.g., "http://www.myapp.com")
   */
  public String getHostURL() {
    return this.hostUrl;
  }
  
  /**
   * Get the Application's base URL.
   * @return this.hostUrl/this.contextPath or null if an error occurred.
   * @exception Exception - all errors are trapped and logged.
   */
  public URL getWebAppURL() {
    URL pResult = null;
    try {
      String sUrl = this.hostUrl;
      if (this.contextPath != null) {
        sUrl += this.contextPath;
      }
      pResult = new URL(sUrl);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getWebAppURL Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
//</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
}
