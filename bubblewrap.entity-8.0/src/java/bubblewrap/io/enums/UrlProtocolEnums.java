package bubblewrap.io.enums;

import bubblewrap.core.reflection.EnumInfo;

/**
 * UrlProtocolEnums - the HttpProtocols used in resolving a URL String
 * @author kprins
 */
public class UrlProtocolEnums {
  
  //<editor-fold defaultstate="collapsed" desc="comment">
  public static final int HTTP  = 1; //"http://";
  public static final int HTTPS = 2; //"https://";
  public static final int FTP   = 3; //"ftp://";
  public static final int FILE  = 4; //"file://";
  public static final int EMAIL = 5; //"mail://";
  //</editor-fold>
  
  public static String getProtocol(int eEnum) {
    String sResult = null;
    switch (eEnum) {
      case UrlProtocolEnums.HTTP:
        sResult = "http";
        break;
      case UrlProtocolEnums.HTTPS:
        sResult = "https";
        break;
      case UrlProtocolEnums.FTP:
        sResult = "ftp";
        break;
      case UrlProtocolEnums.FILE:
        sResult = "file";
        break;
      case UrlProtocolEnums.EMAIL:
        sResult = "mailto";
        break;
    }
    return sResult;
  }
  
  /**
   * Get the Regular Expression to find a match for the protocol
   * @param eProtocol int
   * @return Regexp
   * @throws Exception 
   */
  public static String getRegEx(int eProtocol, boolean bWithPrefix) throws Exception {
    String pResult = null;
    
    
    if (!EnumInfo.isValidEnumOption(UrlProtocolEnums.class, eProtocol)) {
      throw new NullPointerException("Protocol[" +Integer.toString(eProtocol) + 
              "] is not a valid HttpProtocolEnums option.");
    }
    
    String sSuffix = null;
    String sPrefix = null;
    switch (eProtocol) {
      case UrlProtocolEnums.HTTP:
      case UrlProtocolEnums.HTTPS:
      case UrlProtocolEnums.FTP:
        sSuffix = "[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*" 
                     + "[\\w\\-\\@?^=%&amp;/~\\+#])?";
        if (bWithPrefix) {
          sPrefix = UrlProtocolEnums.getProtocol(eProtocol);
          pResult = "^(" + sPrefix + ":\\/\\/)+" + sSuffix ;
        } else {
          pResult = sSuffix;
        }
        break;      
       case UrlProtocolEnums.FILE:
        sSuffix = "^((\\\\[a-zA-Z0-9-]+\\[a-zA-Z0-9`~!@#$%^&(){}'._-]+([ ]+"
                + "[a-zA-Z0-9`~!@#$%^&(){}'._-]+)*)|([a-zA-Z]:))(\\[^ \\/:*"
                + "?\"\"<>|]+([ ]+[^ \\/:*?\"\"<>|]+)*)*\\?$";
        if (bWithPrefix) {
          sPrefix = UrlProtocolEnums.getProtocol(eProtocol);
          pResult = "(" + sPrefix + "):\\/\\/" + sSuffix;
        } else {
          pResult = sSuffix;
        }
        break; 
     case UrlProtocolEnums.EMAIL:
        sSuffix = "(^[a-z]([a-z0-9_.]*)@([a-z]([a-z0-9_.]*))";
        sSuffix += "([.][a-z]{3})$)|(^[a-z]([a-z0-9_.]*)@([a-z]([a-z0-9_.]*))";
        sSuffix += "(.[a-z]{3})(.[a-z]{2})*$)";
        if (bWithPrefix) {
          sPrefix = UrlProtocolEnums.getProtocol(eProtocol);
          pResult = "(" + sPrefix + "):\\/\\/" + sSuffix;
        } else {
          pResult = sSuffix;
        }
        break; 
    }
    return pResult;
  }
}
