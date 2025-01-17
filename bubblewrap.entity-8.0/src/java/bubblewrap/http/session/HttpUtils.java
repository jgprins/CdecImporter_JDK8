package bubblewrap.http.session;

import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.files.FileManager;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * A Utility Class with static method for managing HTTP en- & decoding
 * @author kprins
 */
public class HttpUtils {
  /**
   * Static Error Logger for the Facade Class
   */
  private static final Logger logger = 
                                      Logger.getLogger(HttpUtils.class.getSimpleName());

  /**
   * Convert pDate to a URL formatted String using the sFormat to convert pDate to a
   * String and the replace " " with "+"; "/" or "-" with "%2F"; and ":" with "%3A".
   * @param pDate DateTime
   * @param sFormat String
   * @return String
   * @throws Exception
   */
  public static String encodeDateTime(DateTime pDate, String sFormat)
          throws Exception {
    String result = "";
    try {
      result = (pDate == null)? null: pDate.toLocaleString(sFormat);
      if (result != null) {
        result = result.replace("\\","/");
        result = URLEncoder.encode(result, "UTF-8");
      } else {
        result = "";
      }
    } catch (Exception exp) {
      result = "";
      throw new Exception("HttpUtils.encodeDateTime Error:\n " + exp.getMessage());
    }
    return result;
  }

  /**
   * Convert an Encoded (UTF-8) DateTime string to a DateTime object. Return null if the
   * string is empty or the conversion failed.
   * @param inputStr String
   * @return DateTime
   * @throws Exception
   */
  public static DateTime decodeDateTime(String inputStr) {
    DateTime result = null;
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        String sDateStr = HttpUtils.decodeString(inputStr);
        if ((sDateStr != null) && (!sDateStr.equals(""))) {
          result = DateTime.FromString(sDateStr);
        }
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("HttpUtils.encodeDateTime Error:\n " + 
              exp.getMessage());
    }
    return result;
  }
  
  /**
   * Returns an encoded string using the "UTF-8" character encoding. Return "" if 
   * sInstr = ""|null. Throws and exception if an error occurred.   * 
   * <p><b>Note:</b>The <tt>inputStr</tt> is not cleaned - leading and trailing white
   * spaces are not removed - the preserved intentional leading or trailing spaces.</p>
   * @param inputStr String
   * @return the encoded string or "" if inputStr = null|"";
   * @throws IllegalArgumentException if an error occurs 
   */
  public static String encodeString(String inputStr) {
    return HttpUtils.encodeString(inputStr, null);
  }
  
  /**
   * Returns an encoded string using the specified character encoding sEncodeCharset.
   * if sEncodeCharset=null|"", use "UTF-8". Return "" if sInstr = ""|null. 
   * Throws and exception if an error occurred.
   * <p><b>Note:</b>The <tt>inputStr</tt> is not cleaned - leading and trailing white
   * spaces are not removed - the preserved intentional leading or trailing spaces.</p>
   * @param inputStr String
   * @param charSet the character set to use for the encoding (default = "UTF-8")
   * @return the encoded string or "" if inputStr = null|"";
   * @throws IllegalArgumentException if an error occurs 
   */
  public static String encodeString(String inputStr, String charSet) {
    String result = "";
    try {      
      if (inputStr != null) {
        charSet = DataEntry.cleanString(charSet);
        if (charSet == null) {
          charSet = "UTF-8";
        }
        result = URLEncoder.encode(inputStr, charSet);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("HttpUtils.encodeString error:\n " + 
              exp.getMessage());
    }
    return result;
  }
  
  /**
   * Returns an encoded string using the "UTF-8" character encoding. Return "" if 
   * sInstr = ""|null. Throws and exception if an error occurred.
   * <p><b>Note:</b>The <tt>inputStr</tt> is cleaned - leading and trailing white
   * spaces are removed - the prevent un-intentional leading or trailing spaces.</p>
   * @param inputStr String
   * @return the decoded string or "" if inputStr = null|"";
   * @throws Exception 
   */
  public static String decodeString(String inputStr) {
    return HttpUtils.decodeString(inputStr, null);
  }
  
  /**
   * Returns an encoded string using the specified character encoding sEncodeCharset.
   * if sEncodeCharset=null|"", use "UTF-8". Return "" if sInstr = ""|null. 
   * Throws and exception if an error occurred.
   * <p><b>Note:</b>The <tt>inputStr</tt> is cleaned - leading and trailing white
   * spaces are removed - the prevent un-intentional leading or trailing spaces.</p>
   * @param inputStr String
   * @return the decoded string or "" if inputStr = null|"";
   * @throws IllegalArgumentException when an error occurred 
   */
  public static String decodeString(String inputStr, String sEncodeCharset)  {
    String result = "";
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        sEncodeCharset = DataEntry.cleanString(sEncodeCharset);
        if (sEncodeCharset == null) {
          sEncodeCharset = "UTF-8";
        }
        result = URLDecoder.decode(inputStr, sEncodeCharset);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("HttpUtils.decodeString Error:\n " + 
              exp.getMessage());
    }
    return result;
  }
  
  /**
   * If (sExt != null), check if pathInfo has an extension to the 
   * base PathInfo (i.e., with any assigned parameters). If no Extension is assigned, 
   * append the sDefaultExt to the base PathInfo.
   * @param pathInfo String
   * @param sExt String
   * @return String
   * @throws Exception 
   */
  public static String setPathExtension(String pathInfo, String sExt) 
          throws Exception {
    String result = null;
    try {
      sExt = DataEntry.cleanString(sExt);
      pathInfo = DataEntry.cleanString(pathInfo);
      if ((pathInfo != null) && (sExt != null)) {
        String sPars = null;
        String sBasePath = pathInfo;
        if (pathInfo.contains("?")) {
          int iPos = pathInfo.indexOf("?");
          sBasePath = pathInfo.substring(0, iPos);
          sPars = pathInfo.substring(iPos);
        }
        
        if (FileManager.getFileExtension(sBasePath) == null) {
          if (sExt.startsWith(".")) {
            sBasePath += sExt;
          } else {
            sBasePath += ("." + sExt);
          }
        }
        
        pathInfo = sBasePath;
        if (sPars != null) {
          pathInfo = pathInfo + sPars;
        } 
      }
      
      result = pathInfo;
    } catch (Exception exp) {
      throw new Exception("HttpUtils.setPathExtension Error:\n " + exp.getMessage());
    }
    return result;
  } 
  
  /**
   * OVERLOAD 1: Call HttpUtils.setPathExtension to validate that the extension is set
   * and use <tt>defaultExt</tt> if not set, before calling Overload 2 to build the full Url.
   * @param pathInfo String
   * @param defaultExt String
   * @return String
   * @throws Exception 
   */
  public static String buildRequestUrl(String pathInfo, String defaultExt) 
          throws Exception {
       
    HttpServletRequest request = null;
    return HttpUtils.buildRequestUrl(request, pathInfo, defaultExt);
  }  
  
  /**
   * OVERLOAD 2: Call HttpUtils.setPathExtension to validate that the extension is set
   * and use <tt>defaultExt</tt> if not set, before calling {@linkplain 
   * HttpUtils#buildRequestUrl(javax.servlet.http.HttpServletRequest, java.lang.String) 
   * Overload 4} to build the request URL.
   * @param request the current HttpServletRequest (can be null)
   * @param pathInfo the relative URI with a file extension
   * @param defaultExt the default extension to add if not yet added.
   * @return the expanded URI
   * @throws Exception on error
   */
  public static String buildRequestUrl(HttpServletRequest request, String pathInfo, 
                                                     String defaultExt) throws Exception {
    try {      
      pathInfo = HttpUtils.setPathExtension(pathInfo, defaultExt);
    } catch (Exception exp) {
      throw new Exception("HttpUtils.buildRequestUrl Error:\n " + exp.getMessage());
    }
    return HttpUtils.buildRequestUrl(request, pathInfo);
  } 
  
  /**
   * OVERLOAD 3: Call {@linkplain 
   * HttpUtils#buildRequestUrl(javax.servlet.http.HttpServletRequest, java.lang.String) 
   * Overload 4} with request=null to build the request URL.
   * @param pathInfo the relative URI with a file extension
   * @return the expanded URI
   * @throws Exception on error
   */
  public static String buildRequestUrl(String pathInfo) throws Exception {    
    HttpServletRequest request = null;
    return HttpUtils.buildRequestUrl(request, pathInfo);
  }
  
  /**
   * OVERLOAD 4: Append pathInfo to the HttpServletRequest's contextPath/serveletPath. 
   * Return the pathInfo unchanged if the HttpServletRequest information cannot be 
   * accessed.
   * @param pathInfo the relative URI with a file extension
   * @param request the current HttpServletRequest (can be null)
   * @return the expanded URI
   * @throws Exception on error
   */
  public static String buildRequestUrl(HttpServletRequest request, String pathInfo) 
                                                                        throws Exception {
    String result = null;
    try {
      String input = pathInfo;
      pathInfo = FileManager.cleanPath(pathInfo, "/");
      String serverPath = null;
      request = (request == null)? SessionHelper.getHttpRequest(): request;
      if ((pathInfo != null) && (request != null)) {
        if (((serverPath = DataEntry.cleanString(request.getContextPath())) != null) &&
                (!serverPath.equals("/"))) {
          result = serverPath;
        } else {
          result = "";
          serverPath = null;
        }
        
        String servletPath = DataEntry.cleanString(request.getServletPath());        
        if (!pathInfo.startsWith("/")) {
          pathInfo = "/" + pathInfo;
        }
        
        if ((serverPath != null) && (pathInfo.startsWith(result))) {
          result = pathInfo;
        } else if (FileManager.getFileExtension(servletPath) == null) {
          if ((serverPath == null) || (!pathInfo.startsWith(serverPath))) {
            servletPath += pathInfo;
          }
          result += servletPath;
        } else if ((serverPath == null) || (!pathInfo.startsWith(serverPath))) {
          result += pathInfo;
        }
        logger.log(Level.INFO, "serverPath={0}; servletPath={1}; pathInfo={2}; result={3}", 
                          new Object[]{serverPath, servletPath, input, result});
      }      
    } catch (Exception exp) {
      throw new Exception("HttpUtils.buildRequestUrl Error:\n " + exp.getMessage());
    }
    return (result == null)? pathInfo: result;
  }
  
  /**
   * Overload 1: Call {@linkplain #buildFullUrl(javax.servlet.http.HttpServletRequest, 
   * java.lang.String, java.lang.String) Overload 2} with request=null.
   * @param pathInfo the path to the page to display
   * @param defaultExt the default extension to add if not yet added.
   * @return the full HTTP request URL
   * @throws Exception 
   */
  public static String buildFullUrl(String pathInfo, String defaultExt) throws Exception { 
    HttpServletRequest request = null;
    return HttpUtils.buildFullUrl(request, pathInfo, defaultExt);    
  } 
  
  /**
   * Overload 2: Call {@linkplain #setPathExtension(java.lang.String, java.lang.String) 
   * setPathExtension(pathInfo, defaultExt} to validate that the path extension has been 
   * added before calling {@linkplain #buildFullUrl(javax.servlet.http.HttpServletRequest,
   * java.lang.String) Overload 4} to expand the URL.
   * @param pathInfo the path to the page to display (with extension added)
   * @param request the current HttpServletRequest (can be null)
   * @param defaultExt the default extension to add if not yet added.
   * @return the full HTTP request URL
   * @throws Exception 
   */
  public static String buildFullUrl(HttpServletRequest request, String pathInfo,
                                                    String defaultExt) throws Exception {
    String result = null;
    try {      
      pathInfo = HttpUtils.setPathExtension(pathInfo, defaultExt);
    } catch (Exception exp) {
      throw new Exception("HttpUtils.buildFullUrl Error:\n " + exp.getMessage());
    }
    return HttpUtils.buildFullUrl(request, pathInfo);
  }
  
  /**
   * Overload 3: Call {@linkplain #buildFullUrl(javax.servlet.http.HttpServletRequest,
   * java.lang.String) Overload 4} with request=null.
   * @param pathInfo the path to the page to display
   * @return the full HTTP request URL
   * @throws Exception 
   */
  public static String buildFullUrl(String pathInfo) throws Exception { 
    HttpServletRequest request = null;
    return HttpUtils.buildFullUrl(request, pathInfo);    
  } 
  
  /**
   * Overload 1: Append pathInfo to the HttpServletRequest's 
   * serverUrl/contextPath/servletPath, serverUrl if the base server Url (e.g., 
   * "http://www.mysite.com"). If request = null, set request = {@linkplain 
   * SessionHelper#getHttpRequest()} and throw an exception if the latter returns null.
   * @param pathInfo the path to the page to display (with extension added)
   * @param request the current HttpServletRequest (can be null)
   * @return the full HTTP request URL
   * @throws Exception 
   */
  public static String buildFullUrl(HttpServletRequest request, String pathInfo) 
                                                                       throws Exception {
    String result = null;
    try {
      pathInfo = FileManager.cleanPath(pathInfo, "/");
      request = (request == null)? SessionHelper.getHttpRequest(): request;
      String ctxUrl = null;
      if ((pathInfo != null) && (request != null) &&
              ((ctxUrl = HttpUtils.buildRequestUrl(request, pathInfo)) != null) &&
              ((result = request.getHeader("Host")) != null)) {
        if (!ctxUrl.startsWith("/")) {
          result += "/";
        }
        result += ctxUrl;
        String scheme = DataEntry.cleanString(request.getScheme());
        if (scheme == null) {
          scheme = "http";
        }
        result = scheme + "://" + result;
      }      
    } catch (Exception exp) {
      throw new Exception(HttpUtils.class.getSimpleName() 
                                      + ".buildFullUrl Error:\n " + exp.getMessage());
    }
    return (result == null)? pathInfo: result;
  }
  
  /**
   * Get the HttpServletRequest's serverUrl (e.g., "http://www.mysite.com")
   * @return the Server URL
   * @throws Exception 
   */
  public static String getServerUrl() throws Exception {
    String result = null;
    try {
      HttpServletRequest request = SessionHelper.getHttpRequest();
      if ((request != null) &&
              ((result = request.getHeader("Host")) != null)) {
        String scheme = DataEntry.cleanString(request.getScheme());
        if (scheme == null) {
          scheme = "http";
        }
        result = scheme + "://" + result;
      }      
    } catch (Exception exp) {
      throw new Exception(HttpUtils.class.getSimpleName() 
                                      + ".getServerUrl Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Append to the sUrl (assuming it is a valid Url, the sPar and sValue in the format
   * sUrl?sPar=sValue. if (sValue=null|Empty), set sValue="". if (sUrl=null|Empty) or 
   * (sPar=null|Empty) return null. If sUrl already contain a "?", and it is not the 
   * last character add a "&" (i.e. sUrl&sPar=sValue), otherwise append a "?". Call
   * HttpUtils.encodeString(sValue) to encode the string before assigning it to the
   * Url.
   * @param baseUrl String
   * @param par String
   * @param value String
   * @return String
   * @throws Exception 
   */
  public static String appendToUrl(String baseUrl, String par, String value) 
                                                                      throws Exception {
    String result = DataEntry.cleanString(baseUrl);
    try {
      par = DataEntry.cleanString(par);
      value = DataEntry.cleanString(value);
      if ((result != null) && (par != null)) {
        String encodedVal = (value == null)? "" : HttpUtils.encodeString(value);
        String paramStr = par + "=" + encodedVal;
        
        if (result.contains("?")) {
          if (!baseUrl.endsWith("?")) {
            result += "&";
          }
        } else {
          result += "?";
        }
        result += paramStr;
      }      
    } catch (Exception exp) {
      throw new Exception("HttpUtils.appendToUrl Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Append to the baseUrl (assuming it is a valid Url, a set of RquestParams in the 
   * format baseUrl?par=value. If baseUrl already contain a "?", and it is not the 
   * last character add a "&" (i.e. baseUrl&par=value), otherwise append a "?". Call
   * value = HttpUtils.encodeString(param.parValue) to encode the string before 
   * assigning it to theUrl.
   * @param baseUrl the URL to append the parameters to
   * @param paramSet a set of RequestParams
   * @return the appended URL
   * @throws Exception 
   */
  public static String appendToUrl(String baseUrl, RequestParamSet paramSet) 
                                                                      throws Exception {
    String result = DataEntry.cleanString(baseUrl);
    try {
      if ((paramSet != null) && (!paramSet.isEmpty())) {
        String paramStr = null;
        for (RequestParam param : paramSet) {
          String value = HttpUtils.encodeString(param.parValue);
          if (paramStr == null) {
            paramStr = param.parKey + "=" + value;
          } else {
            paramStr += "&" + param.parKey + "=" + value;
          }
        }
      if ((result != null) && (paramStr != null)) {        
        if (result.contains("?")) {
          if (!baseUrl.endsWith("?")) {
            result += "&";
          }
        } else {
          result += "?";
        }
        result += paramStr;
      }  
      }
    } catch (Exception exp) {
      throw new Exception("HttpUtils.appendToUrl Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Extract the Query Parameter string form the specified <tt>inputUrl</tt>.
   * @param inputUrl the URL to parse
   * @return the query string or null if no parameters are assigned. 
   */
  public static String getQueryString(String inputUrl) {
    String result = null;
    int pos = -1;
    if (((inputUrl = DataEntry.cleanString(inputUrl)) != null) &&
            ((pos = inputUrl.indexOf("?")) >= 0) && 
            ((pos < (inputUrl.length()-1)))) {
      result = inputUrl.substring(pos+1);
    }
    return result;
  }
  
  /**
   * Called to extract and parse the Query Parameter form the specified <tt>inputUrl</tt>
   * and assign it to a RequestParamSet.
   * @param inputUrl the URL to parse
   * @return the request parameter set (can be empty never null
   */
  public static RequestParamSet parseQueryString(String inputUrl) {
    RequestParamSet result = new RequestParamSet();
    String qyrStr = HttpUtils.getQueryString(inputUrl);
    List<String> parSets = DataEntry.splitString(qyrStr, "&");
    if ((qyrStr != null) && ((parSets = DataEntry.splitString(qyrStr, "&")) != null) &&
            (!parSets.isEmpty())) {
      List<String> parPair = null;
      RequestParam param = null;
      for (String qryPar : parSets) {
        if (((parPair = DataEntry.splitString(qryPar, "=")) != null) &&
                (!parPair.isEmpty())) {
          String key = parPair.get(0);
          String value = "";
          if (parPair.size() > 1) {
            value = parPair.get(1);
          }
          if ((param = new RequestParam(key, value)) != null) {
            result.addParams(param);
          }
         
        }
      }
    }
    return result;
  }
}
