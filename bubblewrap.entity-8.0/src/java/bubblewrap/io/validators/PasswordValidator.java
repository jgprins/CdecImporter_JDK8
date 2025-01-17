package bubblewrap.io.validators;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.InputMasks;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An Abstract Validator for a user Password. 
 * @author kprins
 */
public abstract class PasswordValidator extends StringValidator {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Constructor">
  /**
   * Check if a valid PasswordValidator has been registered to the {@linkplain 
   * BwAppContext} as a delegate class for PasswordValidator.
   * @return 
   */
  public static boolean isRegistered() {
    boolean result = false;
    try {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new NullPointerException("Unable to access the Application Context.");
      }
      
      Class<? extends PasswordValidator> delegateClass =
              appCtx.getDelegateClass(PasswordValidator.class);
      result = ((delegateClass != null) &&
                (!Modifier.isAbstract(delegateClass.getModifiers())));
    } catch (Exception exp) {
      Logger.getLogger(PasswordValidator.class.getName()).log(Level.SEVERE, null, exp);
    }
    return result;
  }
  
  /**
   * Get a new instance of the registered PasswordValidator delegate (i.e., as
   * registered to the {@linkplain BwAppContext} as a delegate class for 
   * PasswordValidator.
   * <p>
   * All errors are trapped and logged.</p>
   * @return the validator instance (can be null)
   */
  public static <TValidator extends PasswordValidator> TValidator newInstance() {
    TValidator result = null;
    try {
      BwAppContext appCtx = BwAppContext.doLookup();
      if (appCtx == null) {
        throw new NullPointerException("Unable to access the Application Context.");
      }
      
      Class<? extends PasswordValidator> delegateClass =
              appCtx.getDelegateClass(PasswordValidator.class);
      if (delegateClass != null) {
        if (Modifier.isAbstract(delegateClass.getModifiers())) {
          throw new Exception("The Validator Class[" + delegateClass.getSimpleName()
                  + "] is Abstract and cannot be initiated.");
        }
        result = (TValidator) delegateClass.newInstance();
      }
    } catch (Exception exp) {
      Logger.getLogger(PasswordValidator.class.getName()).log(Level.SEVERE, null, exp);
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected PasswordValidator() {
    super();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Generate a Random Password consisting of 10 Characters. Call {@linkplain
   * #onGetRandomPassword() this.onGetRandomPassword} for custom implementations
   * @return the random password (can be null if not supported - see server log)
   */
  public final String getRandomPassword() {
    String result = null;
    try {
      result = this.onGetRandomPassword();
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.getRandomPassword Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    
    return result;
  }
  
  /**
   * Called to check if the two passwords matched. It cleans the passwords, and
   * return false if one or both are null|"". Otherwise, it calls {@linkplain 
   * #onIsEq(java.lang.String, java.lang.String) this.onIsEq} to check if it is a match.
   * @param savedPw the saved password.
   * @param inputPw the second password.
   * @param pwSalt the the string used as the salt of encrypted passwords
   * @return true if it is a match.
   */
  public final boolean isEq(String savedPw, String inputPw, String pwSalt) {
    boolean result = false;
    try {
      if (((savedPw = DataEntry.cleanString(savedPw)) != null) &&
              ((inputPw = DataEntry.cleanString(inputPw)) != null)) {
        result = onIsEq(savedPw, inputPw, pwSalt);
      }
    } catch (Exception exp) {
      logger.log(Level.SEVERE, "{0}.isEq Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Abstract Methods">
  /**
   * ABSTRACT: Check if the Password complies with the required password format
   * @param inputValue the entered password
   * @return boolean
   */
  protected abstract boolean onIsValidPassword(String inputValue);
  
  /**
   * ABSTRACT: Get a description to inform the user what is required format for a
   * valid password.
   * @return the password format description
   */
  public abstract String getPasswordFormat();
  
  /**
   * ABSTRACT: Called by {@linkplain #getRandomPassword() this.getRandomPassword} to
   * generate a random password that complies with the password format requirements.
   * @return the random password.
   */
  protected abstract String onGetRandomPassword();
  
  /**
   * ABSTRACT: Called by {@linkplain #isEq(java.lang.String, java.lang.String) 
   * this.isEq} to check if the two passwords matches.
   * <p>
   * <b>Note:</b>Both passwords will not be null|"" and were cleaned before the call.</p>
   * @param savedPw the saved password.
   * @param inputPw the second password.
   * @param pwSalt the the string used as the salt of encrypted passwords
   * @return true if the passwords match.
   */
  protected abstract boolean onIsEq(String savedPw, String inputPw, String pwSalt);
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="StringValidator Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Validate the input password based on the PassWord criteria defined
   * by {@linkplain #getPasswordFormat() this.passwordFormat}. The password
   * if validate by calling {@link #onIsValidInput(java.lang.String) 
   * this.onIsValidPassword(inpoutValue(}.</p>
   * @throws Exception if the validation failed. It pass a message that outlines the 
   * password format requirements.
   */
  @Override
  protected final boolean onIsValidInput(String inputValue) {
    inputValue = DataEntry.cleanString(inputValue);
    if ((super.onIsValidInput(inputValue)) && (inputValue != null) && 
        (!this.onIsValidPassword(inputValue))) {
      this.setErrorMsg("Invalid Password: " + this.getPasswordFormat());
    }
    return (!this.hasError());
  }
  
  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: return super.toString(input)</p>
   */
  @Override
  public String toString(String input) {
    return super.toString(input);
  }
  
  /**
   * {@inheritDoc }
   * <p>
   * OVERRIDE: return super.toValue(input)</p>
   */
  @Override
  public String toValue(String input) {
    return super.toValue(input);
  }
  //</editor-fold>
}
