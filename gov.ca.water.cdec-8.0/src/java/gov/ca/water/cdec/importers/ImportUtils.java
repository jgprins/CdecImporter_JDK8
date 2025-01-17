package gov.ca.water.cdec.importers;

import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.convert.ConverterException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ImportUtils {   
  
  //<editor-fold defaultstate="collapsed" desc="DataConverter's Round Enums">
  /**
   * DataConverter Round Options
   */
  public enum Round {
    ONE(1.0d),
    FIVE(5.0d),
    TEN(10.0d),
    HUNDRED(100.0d),
    THOUSAND(1000.0d);
    
    //<editor-fold defaultstate="collapsed" desc="RoundOption Instance Definition">
    /**
     * Public final factor to use in the rounding process.
     */
    public final double factor;
    /**
     * Private Constructor
     * @param factor
     */
    private Round(double factor) {
      this.factor = factor;
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="DataConverter's RoundCeiling Enums">
  /**
   * DataConverter Round Options
   */
  public enum RoundCeiling {
    UP(0.5d),
    NEAREST(0.0d),
    DOWN(-0.5d);
    
    //<editor-fold defaultstate="collapsed" desc="RoundOption Instance Definition">
    /**
     * Public final factor to use in the rounding process.
     */
    public final double factor;
    /**
     * Private Constructor
     * @param factor
     */
    private RoundCeiling(double factor) {
      this.factor = factor;
    }
    //</editor-fold>
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ImportUtils.class.getName());

  //</editor-fold>        
  
  /**
   * Check if <tt>newVal</tt> matches <tt>curVal</tt>. First clean <tt>newVal</tt> and 
   * <tt>curVal</tt> by calling the {@linkplain #cleanString(java.lang.String) 
   * cleanString} method and then compare them - ignoring case is ignoreCase.
   * Return true if both strings are empty or match.
   * @param curVal the current value
   * @param newVal the new input value
   * @param ignoreCase if true, do a case insensitive comparison
   * @return boolean
   */
  public static boolean isEq(String curVal, String newVal,
          boolean ignoreCase) {
    return (((curVal = ImportUtils.cleanString(curVal)) == null) ? 
            ((newVal = ImportUtils.cleanString(newVal)) == null)
            : ((ignoreCase)? curVal.equalsIgnoreCase(newVal):
               curVal.equals(newVal)));
  }
  /**
   * Check if newVal matches curVal.
   * Return true if both values are null or match.
   * @param curVal the current/base Integer Value
   * @param newVal the new Integer value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Integer curVal, Integer newVal) {
    return (curVal == null) ? (newVal == null) : curVal.equals(newVal);
  }
  
  /**
   * Check if curVal matches newVal.
   * Return true if both values are null or match.
   * @param curVal the current/base Long Value
   * @param newVal the new Long value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Long curVal, Long newVal) {
    return (curVal == null) ? (newVal == null) : curVal.equals(newVal);
  }
  
  /**
   * Check if newVal matches curVal.
   * Return true if both values are null or match.
   * @param curVal the current/base Short Value
   * @param newVal the new Short value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Short curVal, Short newVal) {
    return (curVal == null) ? (newVal == null) : curVal.equals(newVal);
  }
  
  /**
   * Check if newVal matches curVal.
   * Return true if both values are null or match.
   * @param dCurValue Double
   * @param dNewValue Double
   * @return boolean
   */
  public static boolean isEq(Double dCurValue, Double dNewValue) {
    return (dCurValue == null) ? (dNewValue == null) : dCurValue.equals(dNewValue);
  }
  
  /**
   * Check if <tt>newValue</tt> matches <tt>curValue</tt> - rounded to <tt>decimals</tt>
   * to the specified RoundCeiling (<tt>ceiling</tt>).
   * Return true if both values are null or match.
   * @param curVal the base value to compare to
   * @param newVal the other value to compare
   * @param decimals the number of decimal places to round to (no rounding if &lt; 0)
   * @param ceiling the RoundCeiling (use NEAREST if undefined).
   * @return true if the values match
   */
  public static boolean isEq(Double curVal, Double newVal, Round decimals, 
                            RoundCeiling ceiling) {
    boolean result = false;
    if ((decimals == null) || (curVal == null) || (newVal == null)){
      result = ImportUtils.isEq(curVal, newVal);
    } else {
      Long curValRnd = ImportUtils.roundTo(curVal, decimals, ceiling);
      Long newValRnd = ImportUtils.roundTo(newVal, decimals, ceiling);
      result = (curValRnd == null) ? (newValRnd == null) : curValRnd.equals(newValRnd);
    }
    return result;
  }
  
  /**
   * Check if newVal matches curVal.
   * Return true if both values are null or match.
   * @param curVal current Boolean value
   * @param newVal new Boolean value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Boolean curVal, Boolean newVal) {
    return (curVal == null) ? (newVal == null) : curVal.equals(newVal);
  }
  
  /**
   * Check if pNewValue matches pCurValue.
   * Return true if both values are null or match.
   * @param curVal Object
   * @param newVal Object
   * @return boolean
   */
  public static boolean isEq(Object curVal, Object newVal) {
    return (curVal == null) ? (newVal == null) : curVal.equals(newVal);
  }
  
  //<editor-fold defaultstate="collapsed" desc="Static String Methods">
  /**
   * Returns a trimmed string or null if the string is empty.
   * @param inputStr
   * @return String
   */
  public static String cleanString(String inputStr) {
    return ((inputStr == null) || (inputStr.trim().equals(""))) ? null
            : inputStr.trim();
  }
  //</editor-fold>
      
  //<editor-fold defaultstate="collapsed" desc="Number Converters">
  /**
   * Convert the String Value to specified pToClass (extends Number). Return null if
   * sValue is empty or unassigned. Throw a ConverterException if an error occur or
   * the pToClass is undefined or not supported.
   * @param <TValue> the Number Type to convert to
   * @param valStr The input string value (can be null)
   * @param toClass The Number class of the return value
   * @return the converted Number or null is sValue=null|""
   */
  @SuppressWarnings("unchecked")
  public static <TValue extends Number> TValue toValue(String valStr,
                                                       Class<TValue> toClass) {
    TValue result = null;
    try {
      if (((valStr = ImportUtils.getCleanNumber(valStr)) != null) &&
              (!valStr.equals("."))){
        if (toClass == null) {
          throw new Exception("The returned Value Class is undefined.");
        } else if (toClass.equals(Byte.class)) {
          Long lngValue = ImportUtils.toRoundedValue(valStr);
          if (lngValue != null) {
            if ((lngValue > Byte.MAX_VALUE)  || (lngValue < Byte.MIN_VALUE)) {
              throw new Exception("Value[" + lngValue.toString() + "] exceeds the valid "
                      + "Byte value range");
            }
            Byte intValue = Byte.parseByte(lngValue.toString());
            result = (TValue) intValue;
          }
        } else if (toClass.equals(Short.class)) {
          Long lngValue = ImportUtils.toRoundedValue(valStr);
          if (lngValue != null) {
            if ((lngValue > Short.MAX_VALUE)  || (lngValue < Short.MIN_VALUE)) {
              throw new Exception("Value[" + lngValue.toString() + "] exceeds the valid "
                      + "Short value range");
            }
            Short intValue = Short.parseShort(lngValue.toString());
            result = (TValue) intValue;
          }
        } else if (toClass.equals(Integer.class)) {
          Long lngValue = ImportUtils.toRoundedValue(valStr);
          if (lngValue != null) {
            if ((lngValue > Integer.MAX_VALUE)  || (lngValue < Integer.MIN_VALUE)) {
              throw new Exception("Value[" + lngValue.toString() + "] exceeds the valid "
                      + "Integer value range");
            }
            Integer intValue = Integer.parseInt(lngValue.toString());
            result = (TValue) intValue;
          }
        } else if (toClass.equals(Long.class)) {
          Long intValue = ImportUtils.toRoundedValue(valStr);
          result = (TValue) intValue;
        } else if (toClass.equals(Double.class)) {
          Double dblValue = Double.parseDouble(valStr);
          result = (TValue) dblValue;
        } else if (toClass.equals(Float.class)) {
          Float fltValue = Float.parseFloat(valStr);
          result = (TValue) fltValue;
        } else {
          throw new Exception("The returned Value Class["+toClass.getSimpleName()+
                  "] is not supported.");
        }
      }
    } catch (Exception pExp) {
      throw new ConverterException("DataConverter.toValue Error:\n "
              + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Return a rounded Long value of the input string value. Convert the string value to
   * a double and the Math.round() to get the rounded Long value
   * @param valStr the string value to convert and rounded
   * @return Long
   */
  public static Long toRoundedValue(String valStr) {
    Long result = null;
    if ((valStr = ImportUtils.cleanString(valStr)) != null) {
      Double dblVal = Double.parseDouble(valStr);
      result = (dblVal == null)? null: Math.round(dblVal);
    }
    return result;
  }
  
  /**
   * Round a Input Long values
   * @param inVal the input value (can be null)
   * @param round the specified round options
   * @param ceiling the required {@linkplain RoundCeiling}
   * @return the rounded value or null if inVal = null.
   */
  public static Long roundTo(Long inVal, Round round, RoundCeiling ceiling){
    Long result = null;
    if (inVal == null) {
      result = ImportUtils.roundTo(inVal.doubleValue(), round, ceiling);
    }
    return result;
  }
  
  /**
   * Return a rounded Long value of the input Double value.
   * @param inVal the input value (can be null)
   * @param round the specified round options
   * @param ceiling the required {@linkplain RoundCeiling}
   * @return the rounded value or null if inVal = null|NaN.
   */
  public static Long roundTo(Double inVal, Round round, RoundCeiling ceiling) {
    Long result = null;
    if ((inVal != null) && (!inVal.isNaN())) {
      try {
        round = (round == null)? round.ONE: round;
        ceiling = (ceiling == null)? ceiling.NEAREST: ceiling;
        Long rndVal = Math.round((inVal / round.factor) + ceiling.factor);
        Double rndDbl = (rndVal * round.factor);
        result = rndDbl.longValue();
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.roundTo Error:\n {1}",
                new Object[]{ImportUtils.class.getSimpleName(), exp.getMessage()});
      }
    }
    return result;
  }
  
  /**
   * Called to strip the <tt>valStr</tt> from any white spaces or digit characters
   * @param valStr the input value string
   * @return the cleaned numeric string or null if the string is empty||null
   */  
  public static String getDataFlag(String valStr) {
    String result = ImportUtils.cleanString(valStr);
    if (result != null) {
      String replaceEx = "[0-9.\\s\\+-]";
      result = result.replaceAll(replaceEx, "");
    }
    return ((result == null) || (result.equals("")))? null: result;
  }
  
  /**
   * Called to strip the <tt>valStr</tt> from any white spaces or non-digit characters
   * @param valStr the input value string
   * @return the cleaned numeric string or null if the string is empty||null
   */
  public static String getCleanNumber(String valStr) {
    String result = ImportUtils.cleanString(valStr);
    if (result != null) {
      String replaceEx = "[^-.\\d]";
      result = result.replaceAll(replaceEx, "");
    }
    return ((result == null) || (result.equals("")) || (result.equals(".")) ||
             (result.equals("-")) || (result.equals("-.")) || 
             (result.equals(".-")))? null: result;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="JSON Conversion Methods">
  /**
   * Convert the JSONarray to a list of JSONObject (assuming the array's content is of
   * type JSONObject)
   * @param jsonArr the input array
   * @return the list of JSONObjects can be empty - not null.
   */
  public static List<JSONObject> jsonArrToObjectList(JSONArray jsonArr) {
    List<JSONObject> result = new ArrayList<>();
    if ((jsonArr != null) && (jsonArr.length() > 0)) {
      JSONObject jsonObj = null;
      for (int item = 0; item < jsonArr.length(); item++) {
        if ((jsonObj = jsonArr.getJSONObject(item)) != null) {
          result.add(jsonObj);
        }
      }
    }
    return result;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="HTTP URL Builder">
  /**
   * Append to the sUrl (assuming it is a valid Url, the sPar and sValue in the format
   * sUrl?sPar=sValue. if (sValue=null|Empty), set sValue="". if (sUrl=null|Empty) or
   * (sPar=null|Empty) return null. If sUrl already contain a "?", and it is not the
   * last character add a "&" (i.e. sUrl&sPar=sValue), otherwise append a "?". Call
   * ImportUtils.encodeString(sValue) to encode the string before assigning it to the
   * Url.
   * @param curUrl String
   * @param parName String
   * @param parValue String
   * @return String
   * @throws Exception
   */
  protected static String appendToUrl(String curUrl, String parName, String parValue)
          throws Exception {
    String result = curUrl;
    try {
      parName = ImportUtils.cleanString(parName);
      parValue = ImportUtils.cleanString(parValue);
      if ((result != null) && (parName != null)) {
        String sEncVal = (parValue == null)? "" : ImportUtils.encodeString(parValue);
        String sAppend = parName + "=" + sEncVal;
        
        if (result.contains("?")) {
          if (!curUrl.endsWith("?")) {
            result += "&";
          }
        } else {
          result += "?";
        }
        result += sAppend;
      }
    } catch (Exception exp) {
      throw new Exception(ImportUtils.class.getSimpleName() 
              + ".appendToUrl Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Date Formatter">
  /**
   * Return a Date/Time Formatted string for this instance's date, Locale,
   * and TimeZone. sFormat specified the DateTime format (e.g., "MM/dd/yyyy").
   * @param inDate  the date input valie
   * @param dtFormat String (valid DateFormat format)
   * @return String
   */
  public static String dateToString(Date inDate, String dtFormat, TimeZone timeZone) {
    String result = null;
    try {
      if (inDate != null) {
        dtFormat = ImportUtils.cleanString(dtFormat);
        if (dtFormat == null) {
          dtFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
        }
        timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
        
        DateFormat drFormatter = new SimpleDateFormat(dtFormat);
        drFormatter.setTimeZone(TimeZone.getDefault());
        result = drFormatter.format(inDate);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toLocaleString Error:\n {1}",
              new Object[]{ImportUtils.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Creates a new instance of the DateTime class from the specified pTimeZone datetime
   * string formated as specified by sFormat.
   * @param dateTimeStr String
   * @param dtFormat String
   * @param timeZone TimeZone
   * @return DateTime
   * @throws ParseException
   */
  public static Date dateFromString(String dateTimeStr, String dtFormat,
                                          TimeZone timeZone) throws Exception {
    Date result = null;
    try {
      dateTimeStr = ImportUtils.cleanString(dateTimeStr);
      if (dateTimeStr != null) {
        dateTimeStr = dateTimeStr.replace("T", " ");
        timeZone = (timeZone == null)? TimeZone.getDefault(): timeZone;
        SimpleDateFormat dtFormatter = new SimpleDateFormat(dtFormat);
        dtFormatter.setTimeZone(timeZone);
        result = dtFormatter.parse(dateTimeStr);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.FromString Error:\n {1}", 
              new Object[]{ImportUtils.class.getSimpleName(), pExp.getMessage()});
      throw new Exception(ImportUtils.class.getSimpleName()
              + ".dateFromString Error:\n " + pExp.getMessage());
    }
    return result;
  }
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="URL Encoding/Decoding">
  /**
   * Convert pDate to a URL formatted String using the sFormat to convert pDate to a
   * String and the replace " " with "+"; "/" or "-" with "%2F"; and ":" with "%3A".
   * @param dateTimeIn DateTime
   * @param dtFormat the expected Date-Time format
   * @param timeZone the expected TimeZone
   * @return String
   * @throws Exception
   */
  public static String encodeDateTime(Date dateTimeIn, String dtFormat, TimeZone timeZone)
          throws Exception {
    String result = "";
    try {
      result = ImportUtils.dateToString(dateTimeIn, dtFormat, null);
      if (result != null) {
        result = result.replace("\\","/");
        result = URLEncoder.encode(result, "UTF-8");
      } else {
        result = "";
      }
    } catch (Exception exp) {
      result = "";
      throw new Exception(ImportUtils.class.getSimpleName() 
              + ".encodeDateTime Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Convert an Encoded (UTF-8) DateTime string to a DateTime object. Return null if the
   * string is empty or the conversion failed.
   * @param inputStr String
   * @param dtFormat the expected Date-Time format
   * @param timeZone the expected TimeZone
   * @return the parsed Date
   * @throws Exception
   */
  public static Date decodeDateTime(String inputStr, String dtFormat, TimeZone timeZone)
          throws Exception {
    Date result = null;
    try {
      inputStr = ImportUtils.cleanString(inputStr);
      if (inputStr != null) {
        String dateStr = ImportUtils.decodeString(inputStr);
        if ((dateStr != null) && (!dateStr.equals(""))) {
          result = ImportUtils.dateFromString(dateStr, dtFormat, timeZone);
        }
      }
    } catch (Exception exp) {
      throw new Exception(ImportUtils.class.getSimpleName()
              + ".encodeDateTime Error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Returns an encoded string using the "UTF-8" character encoding. Return "" if
   * sInstr = ""|null. Throws and exception if an error occurred
   * @param inputStr String
   * @return String
   * @throws Exception
   */
  public static String encodeString(String inputStr) throws Exception {
    return ImportUtils.encodeString(inputStr, null);
  }
  
  /**
   * Returns an encoded string using the specified character encoding encodeCharSet.
   * if encodeCharSet=null|"", use "UTF-8". Return "" if sInstr = ""|null.
   * Throws and exception if an error occurred
   * @param inputStr String
   * @return String
   * @throws Exception
   */
  public static String encodeString(String inputStr, String encodeCharSet)
          throws Exception {
    String result = "";
    try {
      inputStr = ImportUtils.cleanString(inputStr);
      if (inputStr != null) {
        encodeCharSet = ImportUtils.cleanString(encodeCharSet);
        if (encodeCharSet == null) {
          encodeCharSet = "UTF-8";
        }
        result = URLEncoder.encode(inputStr, encodeCharSet);
      }
    } catch (Exception exp) {
      throw new Exception(ImportUtils.class.getSimpleName()
              + ".encodeString error:\n " + exp.getMessage());
    }
    return result;
  }
  
  /**
   * Returns an encoded string using the "UTF-8" character encoding. Return "" if
   * sInstr = ""|null. Throws and exception if an error occurred
   * @param inputStr String
   * @return String
   * @throws Exception
   */
  public static String decodeString(String inputStr) throws Exception {
    return ImportUtils.decodeString(inputStr, null);
  }
  
  /**
   * Returns an encoded string using the specified character encoding encodeCharSet.
   * if encodeCharSet=null|"", use "UTF-8". Return "" if sInstr = ""|null.
   * Throws and exception if an error occurred
   * @param inputStr String
   * @return String
   * @throws Exception
   */
  public static String decodeString(String inputStr, String encodeCharSet)
          throws Exception {
    String result = "";
    try {
      inputStr = ImportUtils.cleanString(inputStr);
      if (inputStr != null) {
        encodeCharSet = ImportUtils.cleanString(encodeCharSet);
        if (encodeCharSet == null) {
          encodeCharSet = "UTF-8";
        }
        result = URLDecoder.decode(inputStr, encodeCharSet);
      }
    } catch (Exception exp) {
      throw new Exception(ImportUtils.class.getSimpleName()
              + ".decodeString Error:\n " + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Unique IDs">
  /**
   * Generate a new random hash Salt value (32 characters)
   * @return
   */
  public static String newHashSalt() {
    Random pRand = new Random();
    byte[] pSalt = new byte[12];
    pRand.nextBytes(pSalt);
    BigInteger hash = new BigInteger(1, pSalt);
    String sSalt = hash.toString(16);
    while (sSalt.length() < 32) {
      sSalt = "0" + sSalt;
    }
    return sSalt;
  }
  
  /**
   * Generate a new unique record ID starting with today's date and a random hash Salt
   * The return value is a 32-character string.
   * @return String
   */
  public static String newUniqueID() {
    String baseStr = null;
    String saltStr = null;
    Date todayDt = Calendar.getInstance().getTime();
    baseStr = todayDt.toString();
    saltStr = ImportUtils.newHashSalt();
    return ImportUtils.newUniqueId(baseStr, saltStr);
  }
  
  /**
   * Generate a new unique record ID starting with sBaseStr and using sSalt. The return
   * value a 32-character string.
   * @param baseStr String
   * @param saltStr String
   * @return String
   * @throws IllegalArgumentException is an error occur
   */
  public static String newUniqueId(String baseStr, String saltStr) {
    String result = null;
    try {
      baseStr = ImportUtils.cleanString(baseStr);
      if (baseStr == null) {
        throw new Exception("The UniqueId's Base String "
                + "cannot be null");
      }
      
      saltStr = ImportUtils.cleanString(saltStr);
      byte[] byteArr = null;
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.reset();
      if (saltStr == null) {
        digest.update(baseStr.getBytes());
        byteArr = digest.digest();
      } else {
        digest.update(saltStr.getBytes());
        byteArr = digest.digest(baseStr.getBytes());
      }
      BigInteger hash = new BigInteger(1, byteArr);
      result = hash.toString(16);
      while (result.length() < 32) {
        result = "0" + result;
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("ImportUtils.newUniqueID Error: \n" +
              exp.getMessage());
    }
    return result;
  }
  
  /**
   * Generate a new UniqueID with no salt and without throwing an exception. Instead
   * exceptions are logged.
   * baseStr sBaseStr String
   * @return String
   */
  public static String newUniqueId(String baseStr) {
    String result = null;
    try {
      result = ImportUtils.newUniqueId(baseStr, null);
    } catch (IllegalArgumentException exp) {
      logger.log(Level.WARNING,
              "ImportUtils.newUniqueId Error:\n {0}", exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
}
