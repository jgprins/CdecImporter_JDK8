package bubblewrap.io.validators;

import bubblewrap.http.session.HttpUtils;
import bubblewrap.core.reflection.EnumInfo;
import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.UrlProtocolEnums;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A String Validator for validating URLs
 * @author kprins
 */
public class UrlValidator extends StringValidator {
  
  /**
   * Placeholder for the Validator's valid protocols.
   */
  private List<Integer> protocolOptions = null;
  private Integer defaultProtocol = null;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public UrlValidator() {
    super();
  }
  
  /**
   * Set more than one valid protocol with one that is the default protocol.
   * @param protocolOptions an array of Protocol Options
   * @param defaultProtocol the default protocol
   */
  public void setUrlProtocols(int[] protocolOptions, int defaultProtocol) {
    try {
      if ((protocolOptions == null) || (protocolOptions.length == 0)) {
        throw new NullPointerException("The Proptocol Options cannot be unassigned");
      }
      
      this.protocolOptions = new ArrayList<>();
      for (int eEnum:protocolOptions) {
        if (!EnumInfo.isValidEnumOption(UrlProtocolEnums.class, eEnum)) {
            throw new NullPointerException("Protocol[" +Integer.toString(eEnum) + 
              "] is not a valid HttpProtocolEnums option.");
        }    
        this.protocolOptions.add(eEnum);
      }
      
      if (!this.protocolOptions.contains(defaultProtocol)) {
        throw new NullPointerException("Default Protocol[" + 
                Integer.toString(defaultProtocol) + 
              "] is not included interface the supported Protocols.");
      }
      this.defaultProtocol = defaultProtocol;
    } catch (Exception pExp) {
      this.protocolOptions = null;
      this.defaultProtocol = null;
      logger.log(Level.WARNING, "{0}.setUrlProtocols Error:\n {1}", 
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Set only one valid protocol which will also be the default protocol the default 
   * protocol.
   * @param protocol the supported protocol
   */
  public void setUrlProtocols(int protocol) {
    if (!EnumInfo.isValidEnumOption(UrlProtocolEnums.class, protocol)) {
      throw new NullPointerException("Protocol[" +Integer.toString(protocol) + 
              "] is not a valid HttpProtocolEnums option.");
    }
    this.protocolOptions = new ArrayList<>();
    this.protocolOptions.add(protocol);
    this.defaultProtocol = protocol;
  }
// </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to check whether the Validator supports the Protocol[eProtocol]
   * @param eProtocol int (UrlProtocolEnums)
   * @return boolean
   */
  public boolean isSupported(int eProtocol) {
    return ((this.protocolOptions != null) && (this.protocolOptions.contains(eProtocol)));
  }
  
  /**
   * if the default protocol is not assigned, return 0 (an invalid UrlProtocolEnums
   * value)
   * @return int
   */
  public int getDefaultProtocol() {
    return (this.defaultProtocol == null)? 0: this.defaultProtocol;
  }  
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="String Validator Overrides">
  /**
   * First check if it is a valid string. If true, try to see if there is a regular 
   * expression match for any of the supported protocols.
   * @param input
   * @return 
   */
  @Override
  protected boolean onIsValidInput(String input) {
    if (super.onIsValidInput(input)) {
      try {
        input = DataEntry.cleanString(input);
        boolean match = false;
        if (input != null) {
          input = input.toLowerCase();
          boolean withPrefix = (input.indexOf("://") > 0);
          for (Integer protocol : this.protocolOptions) {
            String suffix = input;
            if (withPrefix) {
              String prefix = UrlProtocolEnums.getProtocol(protocol) + "://";
              if (input.startsWith(prefix)) {
                suffix = input.replace(prefix, "");
              }
            }
            String regExp = UrlProtocolEnums.getRegEx(protocol, false);
            match = suffix.matches(regExp);
            if (match) {
              break;
            }
          }
        }
        if (!match) {
          throw new Exception("You entered an invalid URL. No matching protocol could "
                  + "be found.");
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.isValidInput Error:\n {1}", 
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        this.setErrorMsg(exp.getMessage());
      }
    }
    return (!this.hasError());
  }
  
  /**
   * Return the trimmed version sParValue. 
   * @param parValue String
   * @return String
   */
  @Override
  public String toValue(String parValue) {
    return DataEntry.cleanString(parValue);
  }
  
  /**
   * Return the trimmed version pInput. If pInput does not have an assigned 
   * protocol prefix, assign the default protocol prefix. (e.g. http://)
   * @param input String
   * @return String
   */
  @Override
  public String toString(String input) {
    String result = DataEntry.cleanString(input);
    if ((result != null) && (!input.contains("://")) && 
                                                      (this.defaultProtocol != null)) {
      String prefix = UrlProtocolEnums.getProtocol(this.defaultProtocol);
      if (prefix != null) {
        result = prefix + "://" +result;
      }      
    }
    return result;
  }
  
  /**
   * Convert an input String to a FTP Url String based on the RFC1738 specs. 
   * If sUserName and/or sPassward is null|"", it is ignored. Similar if sRemoteFile is
   * null|"" it will be ignored. Set iPort=-1 to use the default port.
   * @param sHost String 
   * @param iPort int
   * @param sUserName String
   * @param sPassword String
   * @param sRemotePath
   * @return URL
   * @throws IOException
   */
  public URL toFTPUrl(String sHost, int iPort, String sUserName, String sPassword,
                    String sRemoteFile) throws IOException {
    URL pResult = null;
    try {
      sHost = DataEntry.cleanString(sHost);
      sUserName = HttpUtils.encodeString(sUserName);
      sPassword = HttpUtils.encodeString(sPassword);
      sRemoteFile = DataEntry.cleanString(sRemoteFile);
      if (sHost != null) {
        String sProtocol = UrlProtocolEnums.getProtocol(UrlProtocolEnums.FTP);
        if (sProtocol == null) {
          throw new Exception("Unable to access the FTP Protocol's Prefix.");
        }
        
        //sProtocol = sProtocol +"://"; 
        int iPos = sHost.indexOf("://");
        if (iPos > 0) {
          sHost = sHost.substring(iPos+3);
        }
        /* Start Building the Url */
        String sHostUrl = "";
        /* Add Username and password */
        if (!sUserName.equals("")) {
          sHostUrl += sUserName;
          if (!sPassword.equals("")) {
            sHostUrl += ":" + sPassword;
          }
          sHostUrl += "@";
        }
        /* Add the host */
        sHostUrl += sHost;
        /* Add the Remote File Name/Path */
        if (sRemoteFile != null) {
          sRemoteFile = sRemoteFile.replace("\\", "/");
          if (!sRemoteFile.startsWith("/")) {
            sRemoteFile = "/" + sRemoteFile;
          }
          //sHostUrl += sRemoteFile;
        }
        pResult = new URL(sProtocol, sHostUrl, iPort, sRemoteFile);
      }
    } catch (Exception pExp) {
      throw new IOException(this.getClass().getSimpleName()
              + ".toFTPUrl Error:\n " + pExp.getMessage());
    }
    return pResult;
  }
  //</editor-fold>
}
