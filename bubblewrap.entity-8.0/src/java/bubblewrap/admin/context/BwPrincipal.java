package bubblewrap.admin.context;

import bubblewrap.admin.interfaces.IUserInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Objects;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * The BwPrincipal provide access to a user's Username, UserGroup,
 * UserId, and e-mail address. The logged-on user's BwPrincipal is only accessible 
 * via the AdminContext.
 * @author kprins
 */
public class BwPrincipal<TUser extends IUserInfo> 
                                        implements Principal, Serializable, IUserInfo {
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
// CONVERT: Move to UserInfoView
//  /**
//   * Call to lookup the User Info for a given UserId.
//   * @param sUserId the user recordId
//   * @return the user info as a BwPrincipal (can be null if not found).
//   * @throws Exception
//   */
//  public static BwPrincipal lookup(String sUserId) throws Exception {
//    UserInfoView pUserView = SessionHelper.getManagedBean(UserInfoView.class);
//    if (pUserView == null) {
//      throw new Exception("Unable to access the Session UserInfo View.");
//    }
//    return pUserView.getUserByName(sUserId);
//  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Principal's User Name
   */
  private String userName;
  /**
   * Placeholder for the Principal's UserId
   */
  private String userId;
  /**
   * Placeholder for the Principal's Full Name (First LastName)
   */
  private String fullname;
  /**
   * Placeholder for the Principal's E-Mail address
   */
  private String email;
  /**
   * Placeholder for the Principal's isLoggedIn status
   */
  private Boolean isLoggedIn;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor for non-logged-in Principals - call Constructor 2 with validator = null
   * @param userInfo the user record
   * @throws Exception 
   */
  public BwPrincipal(TUser userInfo) throws Exception {
    this(userInfo,null);
  }
  
  /**
   * Constructor for the logged-in Principal. If the validator is defined the user's
   * IsLoggedIn state will be resolved. Otherwise, the user's loggedIn state will be
   * false.
   * @param userInfo the user record
   * @param validator the UserAccessValidator to verify the user's logged-in status
   * @throws Exception 
   */
  public BwPrincipal(TUser userInfo, UserAccessValidator validator) 
                                                                    throws Exception {
    String userId = 
                 (userInfo == null)? null: DataEntry.cleanString(userInfo.getUserId());
    if (userInfo == null) {
      throw new Exception("The User record cannot be unassigned.");
    } else if (userId == null) {
      throw new Exception("The User's UserId cannot be unassigned. ");
    }
    
    this.userName = userInfo.getName();
    this.userId = userInfo.getUserId();
    this.fullname = userInfo.getFullName();
    this.email = userInfo.getEmail();
    this.isLoggedIn = null;
    this.isLoggedIn = ((validator != null) && (validator.isLoggedOnUser(userId)));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the user's userId
   * @return the Principal's userId
   */
  @Override
  public String getUserId() {
    return this.userId;
  }
  
  /**
   * Get the User's FullName 
   * @return the user's First and Last Names
   */
  @Override
  public String getFullName() {
    return this.fullname;
  }

  /**
   * Get the user's Email 
   * @return the assigned email as as a string
   */
  @Override
  public String getEmail() {
    return this.email;
  }
  
  /**
   * Get the user's Internet formatted email address 
   * @return the InternetAddress address or null if the principal's e-mail is unassigned
   * @throws UnsupportedEncodingException
   * @throws AddressException 
   */
  public InternetAddress getEmailAddress() throws 
                                        UnsupportedEncodingException, AddressException {
    InternetAddress result = null;
    String addrress = DataEntry.cleanString(this.getEmail());
    if (addrress != null) {
      String emailName = DataEntry.cleanString(this.getFullName());
      if (emailName != null) {
        result = new InternetAddress(addrress, emailName);
      } else {
        result = new InternetAddress(addrress);
      }
    }
    return result;
  }

  /**
   * Check is pUser matches the principal credentials.  It compares the UserId's
   * @param user a UserRecord the is an instance of IUserInfo
   * @return true if pUser.userId matches the Principal's userId
   */
  public boolean isUser(IUserInfo user) {
    boolean result = (user != null);
    String sRecId = (result) ? user.getUserId() : null;
    result = (sRecId == null) ? false : sRecId.equalsIgnoreCase(this.userId);
    return result;
  }

  /**
   * Check is sUserId matches the principal credentials.
   * @param sUserId userid UserId to validate
   * @return true if sUserId matches the Principal's userId
   */
  public boolean isUser(String userId) {
    boolean bIsMatch = (userId != null);
    bIsMatch = (userId == null) ? false : userId.equalsIgnoreCase(this.userId);
    return bIsMatch;
  }
  
  /**
   * Get whether this Principal represents the logged in user.
   * @return boolean
   */
  public boolean isLoggedIn() {
    return ((this.isLoggedIn != null) && (this.isLoggedIn));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Implements Principal">
  /**
   * {@inheritDoc }
   * @return the User's Username 
   */        
  @Override
  public String getName() {
    return this.userName;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return true is obs != null and instance of BwPrincipal and the userIds
   * match</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof BwPrincipal));
    if (result) {
      BwPrincipal other = (BwPrincipal) obj;
      result = DataEntry.isEq(this.userId, other.userId, true);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Return a hashCode on this.userId</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 47 * hash + Objects.hashCode(this.userId);
    return hash;
  }
  // </editor-fold>
}
