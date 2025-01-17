package bubblewrap.io;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A static String Utility Class that supports string conversion processing (e.g. 
 * substituting replacement tags with values in a HashMap).
 * @author kprins
 */
public class StringConverter {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger =
                                     Logger.getLogger(StringConverter.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Repalcement Tag Methods">
  /**
   * Search and replace an occurrences of pReplaceTags' Keys in sInsStr. Replacement
   * tags are identified if they are in self enclosing tags (e.g., <MyParam/> where the
   * Key in pReplaceTags is "MyParam"). Null values will be replaced with "-".
   * @param sInStr String
   * @param pReplacements HashMap<String,Object>
   * @return String
   */
  public static String replaceTags(String sInStr, HashMap<String,Object> pReplacements){
    String sResult = sInStr;
    try {
      String sStartStr = DataEntry.cleanString(sInStr);
      String sEndStr = null;
      if ((sStartStr != null) && (sStartStr.contains("/>"))
              && (pReplacements != null) && (!pReplacements.isEmpty())) {
        for (String sKey : pReplacements.keySet()) {
          String sTag = "<" + sKey + "/>";
          Object pValue = pReplacements.get(sKey);
          String sValStr = (pValue == null)? "-": pValue.toString();
          sEndStr = sStartStr.replace(sTag, sValStr);
          if (!sEndStr.contains("/>")) {
            break;
          }
          sStartStr = sEndStr;
        }
        sResult = sStartStr;
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "StringConverter.replaceTags Error:\n {0}",
              pExp.getMessage());
    }
    return sResult;
  }
  
  /**
   * Return a Replacement Tag for use in a Message based on a known Parameter Key.
   * It returns "" if sKey is empty. It returns the tag in the format "<" + sKey +"/>".
   * @param sKey String
   * @return String
   */
  public static String getReplacementTag(String sKey) {
    sKey = DataEntry.cleanString(sKey);
    return (sKey == null)? "": "<" + sKey +"/>";
  }
  
  /**
   * Check is sInStr contain replacement tags. Return true if the string contains a
   * "/>" substring.
   * @param sInStr String
   * @return boolean
   */
  public static boolean hasReplacementTags(String sInStr) {
    return ((sInStr != null) && (sInStr.contains("/>")));
  }
  //</editor-fold>
}
