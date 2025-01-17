package bubblewrap.http.session;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.http.request.IHttpRequestHandler;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import bubblewrap.io.DataEntry;
import javax.faces.bean.ManagedBean;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * This a a utility class with static method that support method the quick access of
 * session content of settings (e.g., accessing ManagedBeans)
 * @author kprins
 */
public class SessionHelper {

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(SessionHelper.class.getName());
  
  /**
   * Place holder for the application context cache.
   */
  private static ApplicationContext appContext;
  
  /**
   * Get cached application context instance
   * @return cached value
   */
  public static ApplicationContext getApplicationContext(){
    if (SessionHelper.appContext == null){
      HttpServletRequest request = SessionHelper.getHttpRequest();
      if (request != null){
        SessionHelper.appContext = 
        WebApplicationContextUtils.getWebApplicationContext(request.getServletContext());
      }
    }
    return SessionHelper.appContext;
  }
  
  /**
   * Check if the Session has a FacesContext
   * @return (FacesContext.getCurrentInstance() != null)
   */
  public static boolean hasFacesContext() {
    return (FacesContext.getCurrentInstance() != null);
  }
  
  /**
   * Called to check if a HttpRequest has been started.
   * @return true if the RequestContextHolder or FacesContext HttpRequest is accessible.
   */
  public static boolean hasHttpRequest() {
    boolean result = false;
    try {
      FacesContext facesCtx = null;
      ServletRequestAttributes reqAttrs = 
              (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (reqAttrs != null) {
        result = (reqAttrs.getRequest() != null);
      } else if ((facesCtx = FacesContext.getCurrentInstance()) != null) {
        ExternalContext extCtx = facesCtx.getExternalContext();
        if (extCtx != null) {
          result = (extCtx.getRequest() != null);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getHttpRequest Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to reference to the current HttpRequest
   * @return RequestContextHolder.getRequestAttributes().getRequest()
   * @throws NullPointerException is the request is not accessible
   */
  public static HttpServletRequest getHttpRequest() {
    HttpServletRequest result = null;
    try {
      FacesContext facesCtx = null;
      ServletRequestAttributes reqAttrs = 
              (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (reqAttrs != null) {
        if ((result = reqAttrs.getRequest()) == null) {
          throw new NullPointerException("The HttpRequest is not accessible.");
        }
      } else if ((facesCtx = FacesContext.getCurrentInstance()) != null) {
        ExternalContext extCtx = facesCtx.getExternalContext();
        if ((extCtx == null) ||
                ((result = (HttpServletRequest) extCtx.getRequest()) == null) ) {
          throw new NullPointerException("The HttpRequest is not accessible.");
        }
      } else {
        throw new NullPointerException("The Request Context is not accessible and "
                + "access to the HttpRequest could not be resolved.");
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getHttpRequest Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called to reference to the current HttpRequest's HttpSession
   * @return RequestContextHolder.getRequestAttributes().getRequest().getSession
   * @throws NullPointerException is the request or Session is not accessible
   */
  public static HttpSession getHttpSession() {
    HttpSession result = null;
    HttpServletRequest request = SessionHelper.getHttpRequest();
    if ((request == null) ||
            ((result = request.getSession()) == null)) {
        throw new NullPointerException("The HttpRequest and/or the HttpSession is "
                                      + "not accessible");
    }
    return result;
  }
  
  /**
   * Get the Key for registering the <tt>beanClass</tt> as a ManagedBean in the 
   * SessionMap. If check if <tt>beanClass</tt> as an assigned {@linkplain ManagedBean}
   * annotation and then retrieve the key = ManagedBean.name. If annotation is
   * unassigned or the manageBean name is not defined, return key = 
   * beanClass.simpleName.toLowercase.
   * @param <TBean> extending Serializable
   * @param beanClass the class of the ManagedBean 
   * @return 
   */
  private static <TBean extends Serializable> 
                            String getManagedBeanKey(Class<? extends TBean> beanClass) {
    String result = null;
    ManagedBean annot = null;
    if (beanClass != null) {
      if ((!beanClass.isAnnotationPresent(ManagedBean.class)) ||
              ((annot = beanClass.getAnnotation(ManagedBean.class)) == null) ||
              ((result = DataEntry.cleanString(annot.name())) ==null)) {
        result = beanClass.getSimpleName().toLowerCase();
      }
    }
    return result;
  }

  /**
   * Public Static method called to get the current applicationContext or session's 
   * instance of the ManagedBean of <tt>beanClass</tt>. It first check if the beanClass 
   * is accessible through the applicationContext and if not, then through the 
   * Session. 
   * <p>
   * If the instance of the ManagedBean cannot be found, it initiates a new instance of
   * the Ben and assigned it to the Session using its simpleClassName.loLowerCase.
   * The beanClass mast support a public parameterless constructor. 
   * <p>
   * If the beanClass has a PostConstruct, it will be called when the instance is 
   * created (once per session). If it has a PostActive method it will be called 
   * every time this method is called.
   */
  @SuppressWarnings({"unchecked", "unchecked"})
  public static <TBean extends Serializable> TBean 
                                    getManagedBean(Class<? extends TBean> beanClass) {
    TBean result = null;
    String sessionKey = null;
    try {
      Class<? extends TBean> delegateClass = beanClass;
      BwAppContext bwAppCtx = BwAppContext.doLookup();
      if (bwAppCtx != null) {
        delegateClass = bwAppCtx.getDelegateClass(beanClass);
      }
      
      sessionKey = SessionHelper.getManagedBeanKey(delegateClass);      
      Object obj = null;
      
      ApplicationContext appCtx = SessionHelper.getApplicationContext();
      if (appCtx != null) {
        try {
          obj = appCtx.getBean(sessionKey);
        } catch (Exception exp) {
  //        throw new Exception(this.getClass().getSimpleName()
  //                + ".methodName Error:\n " + exp.getMessage());
        }
        if (obj == null){
          try {
            obj = appCtx.getBean(delegateClass.getSimpleName());
          } catch (Exception exp) {
  //        throw new Exception(this.getClass().getSimpleName()
  //                + ".methodName Error:\n " + exp.getMessage());
          }
        }
      }
      
      HttpSession session = null;
      if ((obj == null) && ((session = SessionHelper.getHttpSession()) != null)) {
        obj = session.getAttribute(sessionKey);
        if ((obj == null) || (!delegateClass.isInstance(obj))) {
          // Get the Class's Parameterless constructor
          try {
            result = delegateClass.newInstance();
          } catch (Exception pEx1) {
            throw new Exception("Creating a new instance of class[" + sessionKey +
                    "] failed. Reason: " + pEx1.getMessage());
          }

          if (result == null) {
            throw new Exception("Creating a new instance of class["+sessionKey+
                    "] failed.");
          }

          Method postMethod = SessionHelper.getPostConstruct(delegateClass);
          if (postMethod != null) {
            Object[] args = null;
            postMethod.invoke(result, args);
          }
          
          session.setAttribute(sessionKey, result);
          logger.log(Level.INFO, "ManagementBean[{0}] was successfully initiated.", 
                  sessionKey);
        }
      }
      
      if ((obj != null) && (delegateClass.isInstance(obj))) {
        result = (TBean) obj;
      }
      
      //Call the View's PostActivate method if defined;
      Method postActivateMethod = SessionHelper.getPostActivate(delegateClass);
      if (postActivateMethod != null) {
        Object[] args = null;
        postActivateMethod.invoke(result, args);
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.SEVERE, "{0}.getManagedBean[{1}] Error: \n{2}", 
              new Object[]{SessionHelper.class.getSimpleName(), 
                                                      sessionKey, exp.getMessage()});
    }    
    return result;
  }

  /**
   * Public Static method called to get the current webApplication and or session's 
   * instance of the ManagedBean of <tt>beanName</tt>. It first check if bean is 
   * assigned/defined in the ApplicationContext and if not, it checks the Session.
   * It then validate that the retrieved bean is and instance of IHttpRequestHandler
   * before it is casted and return as the result.
   * <p>
   * <b>NOTE:</b> The IHttpRequestHandler cannot be initiated if not found</p>
   * @param beanName the bean to search for by name
   * @return the bean instance or null if not found.
   */
  @SuppressWarnings({"unchecked", "unchecked"})
  public static IHttpRequestHandler getRequestHandler(String beanName) {
    IHttpRequestHandler result = null;
    String sessionKey = null;
    try {
      beanName = DataEntry.cleanString(beanName);
      if (beanName == null) {
        throw new Exception("The RequestHandler's Name is undefined");
      }
      sessionKey = beanName.toLowerCase();  
      Object obj = null;
      
      ApplicationContext appCtx = SessionHelper.getApplicationContext();
      if (appCtx != null) {
        try {
          obj = SessionHelper.getApplicationContext().getBean(sessionKey);
        } catch (Exception exp) {
        }
        if (obj == null){
          try {
            obj = SessionHelper.getApplicationContext().getBean(beanName);
          } catch (Exception exp) {
          }
        }
        HttpSession session = null;
        if ((obj == null) && ((session = SessionHelper.getHttpSession()) != null)){
          try {
            obj = session.getAttribute(sessionKey);
          } catch (Exception exp) {
          }
        }
        
        BwAppContext bwAppCtx = null;
        Class<? extends IHttpRequestHandler> beanClass = null;
        if ((obj == null) && ((bwAppCtx = BwAppContext.doLookup()) != null) &&
           ((beanClass = bwAppCtx.getActionHandler(IHttpRequestHandler.class, beanName))
                != null)) {
          result = SessionHelper.getManagedBean(beanClass);
        }
      }
    } catch (Exception exp) {
      result = null;
      logger.log(Level.SEVERE, "{0}.getRequestHandler[{1}] Error: \n{2}", 
              new Object[]{SessionHelper.class.getSimpleName(), 
                                                      sessionKey, exp.getMessage()});
    }    
    return result;
  }
                                    
  /**
   * Get a Session Flag Setting - return false if the Session is not accessible
   * or the setting is not set. If not set, return bDefault 
   * (Default for bDefault = false).
   * @param sessionKey String
   * @param defaultFlag Boolean
   * @return Boolean
   */
  public static Boolean getSessionFlag(String sessionKey, Boolean defaultFlag) {
    Boolean result = null;
    defaultFlag = ((defaultFlag != null) && (defaultFlag));
      try {
      sessionKey = DataEntry.cleanString(sessionKey);
      if (sessionKey != null) {
        sessionKey = sessionKey.toLowerCase();
        HttpSession session = SessionHelper.getHttpSession();
        if (session == null) {
          throw new Exception("The HttpSession is not accessible");
        }
        
        Object obj = session.getAttribute(sessionKey);
        if ((obj != null) && (Boolean.class.equals(obj.getClass()))) {
          result = (Boolean) obj;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSessionFlag Error:\n {1}",
              new Object[]{"SessionHelper", pExp.getMessage()});    
    }
    return (result == null)? defaultFlag:  result;
  }
  
  /**
   * Set a Session Flag Setting. The Entry is removed id bSet=null.
   * @param sessionKey the session key
   * @param flag Boolean
   */
  @SuppressWarnings("unchecked")
  public static void setSessionFlag(String sessionKey, Boolean flag) {
    try {
      sessionKey = DataEntry.cleanString(sessionKey);
      if (sessionKey != null) {
        sessionKey = sessionKey.toLowerCase();
        HttpSession session = SessionHelper.getHttpSession();
        if (session == null) {
          throw new Exception("The HttpSession is not accessible");
        }
        
        session.setAttribute(sessionKey, flag);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSessionFlag Error:\n {1}",
              new Object[]{"SessionHelper", pExp.getMessage()});    
    }
  }
  
  /**
   * Get a Session String Setting - return null if the Session is not accessible
   * or the setting is not set. Default = null.
   * @param sessionKey String
   * @return String
   */
  public static String getSessionString(String sessionKey) {
    String result = null;
    try {
      sessionKey = DataEntry.cleanString(sessionKey);
      if (sessionKey != null) {
        sessionKey = sessionKey.toLowerCase();
        HttpSession session = SessionHelper.getHttpSession();
        if (session == null) {
          throw new Exception("The HttpSession is not accessible");
        }
        
        Object obj = session.getAttribute(sessionKey);
        if ((obj != null) && (String.class.equals(obj.getClass()))) {
          result = (String) obj;
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getSessionString Error:\n {1}",
              new Object[]{"SessionHelper", pExp.getMessage()});    
    }
    return result;
  }
  
  /**
   * Set a Session String Setting. Remove the value is sValue=""|null
   * @param sessionKey String
   * @param value String
   */
  @SuppressWarnings("unchecked")
  public static void setSessionString(String sessionKey, String value) {
    try {
      sessionKey = DataEntry.cleanString(sessionKey);
      if (sessionKey != null) {
        sessionKey = sessionKey.toLowerCase();
        HttpSession session = SessionHelper.getHttpSession();
        if (session == null) {
          throw new Exception("The HttpSession is not accessible");
        }
        
        value = DataEntry.cleanString(value);        
        session.setAttribute(sessionKey, value);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.setSessionString Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), pExp.getMessage()});    
    }
  }
  
  /**
   * Called to retrieve a Session Parameter set by an instance of the specified 
   * <tt>clazz</tt> for a specified <tt>paramKey</tt>. 
   * @param <TVal> extends Serializable
   * @param clazz the owner class
   * @param paramKey the parameter Key
   * @return return the assigned (and casted) value or null if the Session is not
   * accessible or the value is unassigned.
   */
  @SuppressWarnings("unchecked")
  public static <TVal extends Serializable> TVal getClassParameter(Class clazz, 
                                                                      String paramKey) {
    TVal result = null;
    try {
      paramKey = DataEntry.cleanString(paramKey);
      if ((clazz == null) && (paramKey == null)) {
        throw new Exception("The owner Class Reference or the paramater key is not "
                + "specified");
      }
      String sessionKey = clazz.getSimpleName()+ "." + paramKey;
      
      HttpSession session = SessionHelper.getHttpSession();
      if (session == null) {
        throw new Exception("The HttpSession is not accessible");
      }
      
      Object obj = session.getAttribute(sessionKey);
      if (obj != null) {
        result = (TVal) obj;
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getClassParameter Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called be an instance of the specified <tt>clazz</tt> to assign a Session 
   * Parameter value for the specified <tt>clazz</tt> for a specified <tt>paramKey</tt>. 
   * If the <tt>paramValue</tt> is unassigned and a prior value is assigned, the 
   * paramKey will be removed from the SessionMap.
   * @param <TVal> extends Serializable
   * @param clazz the owner class
   * @param paramKey the parameter Key
   * @param paramValue the new value to assign to the session map (can be null).
   */
  @SuppressWarnings("unchecked")
  public static <TVal extends Serializable> void setClassParameter(Class clazz, 
                                                  String paramKey, TVal paramValue) {
    try {
      paramKey = DataEntry.cleanString(paramKey);
      if ((clazz == null) && (paramKey == null)) {
        throw new Exception("The owner Class Reference or the paramater key is not "
                + "specified");
      }
      String sessionKey = clazz.getSimpleName()+ "." + paramKey;
      
      HttpSession session = SessionHelper.getHttpSession();
      if (session == null) {
        throw new Exception("The HttpSession is not accessible");
      }
              
      session.setAttribute(sessionKey, paramValue);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setClassParameter Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
  }
  
  
  /**
   * Called to retrieve a cached instance of TClass which was previously assigned as 
   * a Session Parameter using the clazz.getSimpleName as a parameter key. 
   * @param <TClass> extends Serializable
   * @param clazz the class of interest
   * @return return the assigned (and cached) instance or null if the Session is not
   * accessible or the value is unassigned.
   */
  @SuppressWarnings("unchecked")
  public static <TClass extends Serializable> TClass 
                                                  getCacheInstance(Class<TClass> clazz) {
    TClass result = null;
    try {
      String paramKey = null;
      if ((clazz == null) || 
                  ((paramKey = DataEntry.cleanLoString(clazz.getSimpleName())) == null)) {
        throw new Exception("The owner Class Reference or the paramater key is not "
                + "specified");
      }
      
      HttpSession session = SessionHelper.getHttpSession();
      if (session == null) {
        throw new Exception("The HttpSession is not accessible");
      }
      
      Object obj = session.getAttribute(paramKey);
      if ((obj != null) && (clazz.isInstance(obj.getClass()))) {
        result = (TClass) obj;
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getCacheInstance Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Called be assign an instance of the specified <tt>clazz</tt> as a Session 
   * Parameter value using the clazz.getSimpleName as a parameter key. Skipped is
   * <tt>instance</tt> = null.
   * @param <TClass> extends Serializable
   * @param clazz the class of interest
   * @param instance the instance to assign (must be not null)
   * @param paramValue the new value to assign to the session map (can be null).
   */
  @SuppressWarnings("unchecked")
  public static <TClass extends Serializable> void setCachedInstance(Class<TClass> clazz, 
                                                  TClass instance) {
    try {
      String paramKey = null;
      if ((clazz == null) || 
                  ((paramKey = DataEntry.cleanLoString(clazz.getSimpleName())) == null)) {
        throw new Exception("The owner Class Reference or the paramater key is not "
                + "specified");
      }
      
      HttpSession session = SessionHelper.getHttpSession();
      if (session == null) {
        throw new Exception("The HttpSession is not accessible");
      }
              
      session.setAttribute(paramKey, instance);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setCachedInstance Error:\n {1}",
              new Object[]{SessionHelper.class.getSimpleName(), exp.getMessage()});
    }
  }

  /**
   * Get the Classes Post Construct Method
   * @param pClass Class<? extends V>
   * @return Method
   */
  private static <V> Method getPostConstruct(Class<? extends V> pClass) {
    Method pPostMethod = null;
    Method[] pList = (pClass == null) ? null : pClass.getMethods();
    if ((pList != null) && (pList.length > 0)) {
      for (Method pMethod : pList) {
        if (pMethod.getAnnotation(PostConstruct.class) != null) {
          pPostMethod = pMethod;
        }
      }
    }
    return pPostMethod;
  }

  /**
   * Get the Classes Post Activate Method
   * @param pClass Class<? extends V>
   * @return Method
   */
  private static <V> Method getPostActivate(Class<? extends V> pClass) {
    Method pPostMethod = null;
    Method[] pList = (pClass == null) ? null : pClass.getMethods();
    if ((pList != null) && (pList.length > 0)) {
      for (Method pMethod : pList) {
        if (pMethod.getAnnotation(PostActivate.class) != null) {
          pPostMethod = pMethod;
        }
      }
    }
    return pPostMethod;
  }

  /**
   * Invalidate the current session and release all bound resources.
   */
  public static void invalidate() {
    try {
      HttpSession session = SessionHelper.getHttpSession();
      if (session != null) {
        session.invalidate();
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "SessionHelper.invalidate Error:\n {0}", 
              pExp.getMessage());
    }
  }
  
  /**
   * Get the Web Site's Host address (i.e. ExternalContext.RequestHeaderMap['host'])
   * @return String
   */
  public static String getWebAddress() {
    String result = null;
    try {
      HttpServletRequest request = SessionHelper.getHttpRequest();
      if (request != null) {
        result = request.getHeader("host");
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "SessionHelper.getWebAddress Error:\n {0}", 
              pExp.getMessage());
    }
    return result;
  }

  /**
   * Get the Web Site's Host URL (e.g., 'http://www.mysite.org/subweb')
   * @return the Sessions WebUrl
   */
  public static String getWebURL() {
    String result = null;
    try {
      HttpServletRequest request = SessionHelper.getHttpRequest();
      if (request != null) {
        result = request.getHeader("host");
        String ctxPath = DataEntry.cleanString(request.getContextPath());
        if (ctxPath == null) {
          ctxPath = "";
        } else if (!ctxPath.endsWith("/")) {
          ctxPath += "/";
        }
        String scheme = DataEntry.cleanString(request.getScheme());
        scheme = (scheme == null)? "http://" : scheme+"://";
        
        result = scheme + result + ctxPath;
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "SessionHelper.getWebURL Error:\n {0}", 
              pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Retrieve the SessionId from the HtppRequest.HttpSession.
   * @return the session ID or null if the HttpServletRequest is not accessible
   */
  public static String getSessionId() {
    String result = null;
    try {
      HttpServletRequest request = SessionHelper.getHttpRequest();
      if (request != null) {
        HttpSession curSession = request.getSession();
        result = curSession.getId();
      }
    } catch (Exception pExp) {
      result = null;
      logger.log(Level.WARNING, "SessionHelper.getSessionId Error:\n {0}", 
              pExp.getMessage());
    }
    return result;
  }

  /**
   * Return the Locale of the HTTP Request
   * @return the HttpServletRequest's Locale or null if the HttpServletRequest is not
   * accessible.
   */
  public static Locale getRequestLocale() {
    Locale result = null;
    HttpServletRequest httpReq = SessionHelper.getHttpRequest();
    if (httpReq != null) {
      result = httpReq.getLocale();
    }
    return result;
  }
}


