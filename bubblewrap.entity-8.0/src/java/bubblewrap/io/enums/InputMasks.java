package bubblewrap.io.enums;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.validators.PasswordValidator;
import bubblewrap.io.validators.*;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>This enum class is define the special input mask for an Entity Input Field (e.g. a
 * PhoneNo. Associated with each enum value is a StringInputValidator as follows:</p>
 * <ul>
 *   <li><b>{@linkplain #PhoneNo}:</b> = {@linkplain PhoneNoValidator}</li>
 *   <li><b>{@linkplain #ShortZipcode}:</b> = {@linkplain ShortZipCodeValidator}</li>
 *   <li><b>{@linkplain #LongZipcode}:</b> = {@linkplain LongZipCodeValidator}</li>
 *   <li><b>{@linkplain #Email}:</b> = {@linkplain EmailValidator}</li>
 *   <li><b>{@linkplain #WebUrl}:</b> = {@linkplain WebUrlValidator}</li>
 *   <li><b>{@linkplain #FtpUrl}:</b> = {@linkplain FtpUrlValidator}</li>
 *   <li><b>{@linkplain #Username}:</b> = {@linkplain UsernameValidator}</li>
 *   <li><b>{@linkplain #Password}:</b> = {@linkplain PasswordValidator}</li>
 * </ul>  
 * @author kprins
 */
public enum InputMasks {
  
  //<editor-fold defaultstate="collapsed" desc="Enum Values">
  /**
   * Standard phone number format "(999) 444-5555"
   */
  PhoneNo (PhoneNoValidator.class),
  /**
   * 5-digit size code "95630"
   */
  ShortZipcode (ShortZipCodeValidator.class),
  /**
   * 9-digit size code "95630-3210"
   */
  LongZipcode (LongZipCodeValidator.class),
  /**
   * Standard e-mail address
   */
  Email (EmailValidator.class),
  /**
   * Standard http(s) web site address
   */
  WebUrl (WebUrlValidator.class),
  /**
   * Standard ftp(s) web site address
   */
  FtpUrl (FtpUrlValidator.class),
  /**
   * For Validating entered Usernames
   */
  Username (UsernameValidator.class),
  /**
   * For validating Passwords - its uses and abstract PasswordValidator class which 
   * should be overridden for a specific application and registered as Class Delegate
   * to the {@linkplain BwAppContext}.
   */
  Password (PasswordValidator.class);  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Enum Class Definition">
  /**
   * The InputMask's validation RegExp
   */
  private final Class<? extends StringValidator> validatorClass;
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  private InputMasks(Class<? extends StringValidator> validatorClass) {
    this.validatorClass = validatorClass;
  }
  //</editor-fold>
  
  /**
   * Get the Validator for the specific InputMask. It calls {@linkplain 
   * BwAppContext#getDelegateClass(java.lang.Class) 
   * appCtx.getDelegateClass(this.validatorClass)} to retrieve any delegate class 
   * assigned for InputMask's base class (typically for {@linkplain PasswordValidator}).
   * @return an instance of the assigned validator class or an instance of {@linkplain
   * StringValidator} is the class is unassigned.
   */
  public StringValidator getValidator() {
    StringValidator result = null;
    if (this.validatorClass == null) {
      result = new StringValidator();
    } else {
      try {
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new NullPointerException("Unable to access the Application Context.");
        }
        
        Class<? extends StringValidator> delegateClass =
                                          appCtx.getDelegateClass(this.validatorClass);
        if (delegateClass != null) {
          if (Modifier.isAbstract(delegateClass.getModifiers())) {
            throw new Exception("The Validator Class[" + delegateClass.getSimpleName()
                    + "] is Abstract and cannot be initiated.");
          }
          result = delegateClass.newInstance();
        }
      } catch (Exception exp) {
        Logger.getLogger(InputMasks.class.getName()).log(Level.SEVERE, null, exp);
      }
    }
    return result;
  }
  //</editor-fold>
}
