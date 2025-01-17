package bubblewrap.mail;


import bubblewrap.admin.context.BwPrincipal;
import bubblewrap.io.DataEntry;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * A Static class with Email utility functions
 * @author kprins
 */
public class EmailFormatter {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
          Logger.getLogger(EmailFormatter.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Validate that the e-mail address format is correct. Use InternetAddress.parse() to
   * check if it is valid.
   * @param emailAddress String
   * @return boolean
   */
  public static boolean isValidAddress(String emailAddress) {
    boolean result = false;
    try {
      emailAddress = DataEntry.cleanString(emailAddress);
      if (emailAddress != null) {
        InternetAddress[] addressArr = InternetAddress.parse(emailAddress);
        result = ((addressArr != null) && (addressArr.length == 1));
      }
    } catch (Exception e) {
    }
    return result;
  }
  
  /**
   * Validate that sInput has a valid list of e-mail addresses.
   * Use InternetAddress.parse() to check if it is valid.
   * @param emailAddresses String
   * @return boolean
   */
  public static boolean isValidAddressList(String emailAddresses) {
    boolean result = false;
    try {
      emailAddresses = DataEntry.cleanString(emailAddresses);
      if (emailAddresses != null) {
        InternetAddress[] addressArr = InternetAddress.parse(emailAddresses);
        result = ((addressArr != null) && (addressArr.length > 0));
      }
    } catch (Exception e) {
    }
    return result;
  }
  
  /**
   * merge the Address list and return a combined list as an string list.
   * @param sList1list1String
   * @param list2 String
   * @return InternetAddress[]
   * @throws AddressException
   */
  public static InternetAddress[] mergeAddressLists(String list1, String list2)
          throws AddressException {
    list1 = DataEntry.cleanString(list1);
    list2 = DataEntry.cleanString(list2);
    InternetAddress[] addrList1 = (list1 == null)? null: InternetAddress.parse(list1);
    InternetAddress[] addrList2 = (list2 == null)? null: InternetAddress.parse(list2);
    InternetAddress[] mergedList = 
                                EmailFormatter.mergeAddressLists(addrList1, addrList2);
    return mergedList;
  }
  
  /**
   * Merge the two addrList1 arrays and return the result as an array.
   * @param addrList1 InternetAddress[]
   * @param addrList2 InternetAddress[]
   * @return InternetAddress[]
   * @throws AddressException
   */
  public static InternetAddress[] mergeAddressLists(InternetAddress[] addrList1,
          InternetAddress[] addrList2) throws AddressException {
    InternetAddress[] result = new InternetAddress[]{};
    if ((addrList1 != null) && (addrList1.length > 0)) {
      if ((addrList2 != null) && (addrList2.length > 0)) {
        List<InternetAddress> pMerge = new ArrayList<>();
        pMerge.addAll(Arrays.asList(addrList1));

        if ((addrList2 != null) && (addrList2.length > 0)) {
          for (InternetAddress pAddr: addrList2) {
            if (!pMerge.contains(pAddr)) {
              pMerge.add(pAddr);
            }
          }
        }
        result = pMerge.toArray(result);
      } else {
        result = addrList1;
      }
    } else if ((addrList2 != null) && (addrList2.length > 0)) {
      result = addrList2;
    }
    
    return result;
  }
  
  /**
   * Return a string list of email address for the user represented in pUsers. 
   * @param userList list of users
   * @return a string of addresses or null if pUsers is unassigned or empty.
   */
  public static String getAddressList(List<BwPrincipal> userList) {
    String result = null;
    try {
      if ((userList != null) && (!userList.isEmpty())) {
        if (userList.size() == 1) {
          BwPrincipal user = userList.get(0);
          InternetAddress pAddress = (user == null)? null: user.getEmailAddress();
          result = (pAddress == null)? null: pAddress.toString();
        } else {
          List<InternetAddress> addrList = new ArrayList<>();
          for (BwPrincipal user : userList) {
            InternetAddress address = (user == null)? null: user.getEmailAddress();
            if (address != null) {
              addrList.add(address);
            }
          }
          
          if (!addrList.isEmpty()) {
            InternetAddress[] addressArr = addrList.toArray(new InternetAddress[0]);
            result = InternetAddress.toString(addressArr);
          }
        }
      }
    } catch (UnsupportedEncodingException | AddressException exp) {
      logger.log(Level.WARNING, "{0}.getAddressList Error:\n {1}",
              new Object[]{"EmailFormatter", exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
}
