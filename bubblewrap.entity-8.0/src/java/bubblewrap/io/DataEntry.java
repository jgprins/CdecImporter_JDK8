package bubblewrap.io;

import bubblewrap.entity.annotations.FieldConverter;
import bubblewrap.entity.annotations.FieldValidation;
import bubblewrap.io.converters.FieldValueConverter;
import bubblewrap.io.datetime.DateTime;
import bubblewrap.io.enums.StringMatch;
import bubblewrap.io.enums.UrlProtocolEnums;
import bubblewrap.io.schedules.enums.Interval;
import bubblewrap.io.validators.PasswordValidator;
import bubblewrap.io.validators.UrlValidator;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A static utility class with common methods used during data management (e.g., value
 * comparing, converting, and validation). It also includes methods for generating
 * unique ID's and encrypted passwords.
 * @author kprins
 */
public class DataEntry {

  //<editor-fold defaultstate="collapsed" desc="Static Public Fields">
  /**
   * The String representing a 'hard' space.
   */
  public static final String nbsp = "&nbsp;";
  public static final String DefaultDelimiters = "[,;&|/ (\\)(\n)(\n\r)]";

  /**
   * Text Trim Enums
   */
  public static final int TEXT_TRIMLEFT = 1;
  public static final int TEXT_TRIMRIGHT = 2;
  public static final int TEXT_TRIMALL = 3;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Static Error Logger for the Facade Class
   */
  private static final Logger logger = Logger.getLogger(DataEntry.class.getSimpleName());
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="String Conversions/Test">
  /**
   * Check if sInsTr string is an empty string (""|null).
   * @param inStr the string to check
   * @return true if cleanString(sInstr) == null
   */
  public static boolean isEmpty(String inStr) {
    return (DataEntry.cleanString(inStr) == null);
  }

  /**
   * Returns a trimmed string converted to uppercase or null if the string is empty.
   * @param inStr the input String (can be null)
   * @return the clean uppercase String (null if "" |null)
   */
  public static String cleanUpString(String inStr) {
    String result = ((inStr == null) || (inStr.trim().equals(""))) ? null
                            : inStr.trim();
    return (result == null)? null: result.toUpperCase();
  }

  /**
   * Returns a trimmed string converted to lowercase or null if the string is empty.
   * @param inStr the input String (can be null)
   * @return the cleaned lowercase String (null if "" |null)
   */
  public static String cleanLoString(String inStr) {
    String result = ((inStr == null) || (inStr.trim().equals(""))) ? null
                            : inStr.trim();
    return (result == null)? null: result.toLowerCase();
  }

  /**
   * OVERLOAD 1: Returns a trimmed string or null if the string is empty.
   * @param inStr the input String (can be null)
   * @return the cleaned String (null if "" |null)
   */
  public static String cleanString(String inStr) {
    return ((inStr == null) || (inStr.trim().equals(""))) ? null
            : inStr.trim();
  }

  /**
   * OVERLOAD 2: Returns a trimmed string or null if the string is empty.
   * If the length of the trimmed exceeds maxLen (for maxLen > 0), it will
   * return the first maxLen of the trimmed string.  Use this call for preparing
   * limited length string for assigning to the database records.
   * @param inStr the input String (can be null)
   * @param maxLen int
   * @return String
   */
  public static String cleanString(String inStr, int maxLen) {
    String result = ((inStr == null) || (inStr.trim().equals(""))) ? null
            : inStr.trim();
    if ((result != null) && (maxLen > 0) && (result.length() > maxLen)) {
      result = result.substring(0, maxLen);
    }
    return result;
  }
  /**
   * OVERLOAD 3: Clean the string by trimming it according to trimType(TEXT_TRIMLEFT |
   * TEXT_TRIMRIGHT | TEXT_TRIMALL) and truncate the string to maxLen if
   * (maxLen>0) and the string's length exceeds maxLen.  Use this call for
   * preparing limited length string for assigning to the database records and
   * preserving either leading or trailing blanks.
   * @param inStr the input String
   * @param maxLen the maximum sting length
   * @param trimType the Text Trim Type
   * @return the cleaned string null if the string is empty.
   */
  public static String cleanString(String inStr, int maxLen, int trimType) {
    String result = null;
    if (inStr != null) {
      switch (trimType) {
        case DataEntry.TEXT_TRIMLEFT:
          result = inStr + "_x";
          result = result.trim();
          if (result.equals("_x")) {
            result = null;
          } else {
            result = result.substring(0, (result.length()-2));
          }
          break;
        case DataEntry.TEXT_TRIMRIGHT:
          result = "x_" + inStr;
          result = result.trim();
          if (result.equals("x_")) {
            result = null;
          } else {
            result = result.substring(2);
          }
          break;
        default:
          result = inStr.trim();
          break;
      }
    }

    if ((result != null) && (maxLen > 0) && (result.length() > maxLen)) {
      result = result.substring(0, maxLen);
    }
    return result;
  }

  /**
   * OVERLOAD 4: Called to "clean" sValue (i.e., removing trailing blanks and delimiters
   * as defined by delimiters.
   * @param inStr String
   * @param delimiters String
   * @return String
   */
  public static String cleanString(String inStr, String delimiters) {
    String result = DataEntry.cleanString(inStr);
    delimiters = DataEntry.cleanString(delimiters);
    if (delimiters != null) {
      while (result != null) {
        String sLast = result.substring(result.length()-1);
        if (sLast.matches(delimiters)) {
          result = DataEntry.cleanString(result.substring(0, result.length()-1));
        } else {
          break;
        }
      }
    }
    return result;
  }


  /**
   * OVERLOAD 7: Returns a trimmed, un-quoted (if <tt>removeQuotes</tt> = true) string
   * or null if the string is empty. If remove any " or ' quotes - only if the string
   * starts and ends with " or '.
   * @param insStr the input string
   * @param removeQuotes true to remove quotes
   * @return the trimmed string or null if result = ""|null
   */
  public static String cleanString(String inStr, boolean removeQuotes) {
    String result = null;
    inStr = DataEntry.cleanString(inStr);
    if ((inStr != null) && (removeQuotes)) {
      if ((inStr.startsWith("\"")) || (inStr.endsWith("\""))) {
        result = inStr.replace("\"", "");
      } else if ((inStr.startsWith("'")) || (inStr.endsWith("'"))) {
        result = inStr.replace("'", "");
      } else {
        result = inStr;
      }
    }
    return result;
  }

  /**
   * Split the input string into a list of strings using the standard line break
   * characters ("\n" or "\n\r") as delimiters to split the string.
   * @param inputStr the input string to parse into sub-strings
   * @return the list of strings or an empty list if the input string is empty.
   */
  public static List<String> splitLines(String inputStr) {
    List<String> result = new ArrayList<>();
    inputStr = DataEntry.cleanString(inputStr);
    if (inputStr != null) {
      if (inputStr.contains("\n\r")) {
        inputStr = inputStr.replace("\n\r", "\n");
      }
      String delimiters = "[\n]";
      Scanner strScanner = new Scanner(inputStr);
      strScanner.useDelimiter(delimiters);
      while (strScanner.hasNext()) {
        String nextStr = DataEntry.cleanString(strScanner.next());
        if (nextStr != null) {
          result.add(nextStr);
        } else {
          result.add("");
        }
      }
    }
    return result;
  }

  /**
   * Split the input string into a list of substring using the specified Delimiter to
   * split the string.
   * <p><b>Note:</b>Do not put the delimiters inside "[..]" brackets.</p>
   * @param inputStr the input string to parse into sub-strings
   * @param delimiters the delimiters to use in splitting the string into a string list
   * - multiple delimiters can specified (e.g., ", /;").
   * @return the list if sub-string or an empty list if the input string is empty.
   */
  public static List<String> splitString(String inputStr, String delimiters) {
    List<String> result = new ArrayList<>();
    inputStr = DataEntry.cleanString(inputStr);
    if (inputStr != null) {
      delimiters = ((delimiters == null) || (delimiters.equals(""))) ?
              DataEntry.DefaultDelimiters: delimiters;
      if ((delimiters.contains("\\")) && (inputStr.contains("\\"))) {
        inputStr = inputStr.replace("\\", "/");
        if (!delimiters.contains("/")) {
          delimiters += "/";
        }
      }
      List<String> lines = DataEntry.splitLines(inputStr);
      String splitStr = null;
      if (!lines.isEmpty()) {
        for (String lineStr : lines) {
          if (lineStr.length() > 0) {
            splitStr = (splitStr == null)? "": splitStr + ";";
            splitStr += lineStr;
          }
        }
        if (!delimiters.contains(";")) {
          delimiters += ";";
        }
      }

      if (!delimiters.contains(";")) {
        if (delimiters.startsWith("[")) {
          delimiters = delimiters.replaceFirst("[", "");
        }
        delimiters = ";" +delimiters;
      }
      if (!delimiters.startsWith("[")) {
        delimiters = "[" + delimiters;
      }
      if (!delimiters.endsWith("]")) {
        delimiters += "]";
      }
      String charStr = splitStr.substring(0, 1);
      if ((!charStr.equals("[")) && (!charStr.equals("]")) &&
                                (delimiters.contains(charStr))) {
        result.add("");
      }
      Scanner strScanner = new Scanner(splitStr);
      strScanner.useDelimiter(delimiters);
      while (strScanner.hasNext()) {
        String nextStr = DataEntry.cleanString(strScanner.next());
        if (nextStr != null) {
          result.add(nextStr);
        } else {
          result.add("");
        }
      }
      charStr = splitStr.substring(splitStr.length()-1);
      if ((!charStr.equals("[")) && (!charStr.equals("]")) &&
                                (delimiters.contains(charStr))) {
        result.add("");
      }
    }
    return result;
  }

  /**
   * The delimiter used to concatenate an array of Key-Value Pair Strings (KVP-strings).
   */
  public final static String KVPDelimiter = "||";
  /**
   * Called to concatenate an array of Key-Value Pair Strings (KVP-strings) into a single
   * string using a {@linkplain #KVPDelimiter KVPDelimiter} to separate the KVPs. if
   * <tt>kpvArray</tt> contains only one pair, the KVP-string will be returned without
   * any KVPDelimiters.
   * @param kpvArray the array of KVP-strings to concatenate
   * @return the concatenated string or "" if kpvArray = null|empty.
   */
  public static String concatKeyValuePairs(String...kpvArray) {
    String result = null;
    if ((kpvArray != null) && (kpvArray.length > 0)) {
      int pairCnt = 0;
      for (String kpvStr : kpvArray) {
        if ((kpvStr = DataEntry.cleanString(kpvStr)) != null) {
          pairCnt++;
          if (result == null) {
            result = kpvStr;
          } else {
            result += DataEntry.KVPDelimiter + kpvStr;
          }
        }
      }

      if (pairCnt > 1) {
        result = DataEntry.KVPDelimiter + result + DataEntry.KVPDelimiter;
      }
    }
    return (result == null)? "": result;
  }

  /**
   * Called to parse a concatenated set of Key-Value Pair Strings (KVP-strings) to an
   * array of KVP-strings assuming the KVP-strings are separated using a {@linkplain
   * #KVPDelimiter KVPDelimiter}. If <tt>concatKPVs</tt> does not contains any
   * KVPDelimiters it is assumed to be a single KVP-string.
   * @param concatKPVs the concatenated KVP-strings to parse.
   * @return an array of KVP-strings - can be empty if concatKPVs does not contain any
   * KVP-strings
   */
  public static String[] parseKeyValuePairs(String concatKPVs) {
    List<String> pvpList = new ArrayList<>();
    if ((concatKPVs = DataEntry.cleanString(concatKPVs)) != null) {
      if (concatKPVs.startsWith(DataEntry.KVPDelimiter)) {
        concatKPVs = DataEntry.cleanString(concatKPVs.substring(2));
      }
      if (concatKPVs.equals(DataEntry.KVPDelimiter)) {
        concatKPVs = null;
      }

      if (concatKPVs != null) {
        if (concatKPVs.contains(DataEntry.KVPDelimiter)) {
          int startPos = 0;
          int endPos = -1;
          String kpvStr = null;
          while ((endPos = concatKPVs.indexOf(DataEntry.KVPDelimiter, startPos)) > 0) {
            if ((kpvStr = DataEntry.cleanString(
                                  concatKPVs.substring(startPos, endPos))) != null) {
              pvpList.add(kpvStr);
            }
            startPos = endPos+2;
            if (startPos >= concatKPVs.length()-1) {
              break;
            }
          }
          if ((startPos < concatKPVs.length()-1) &&
              ((kpvStr = DataEntry.cleanString(concatKPVs.substring(startPos))) != null)){
            pvpList.add(kpvStr);
          }
        } else {
          pvpList.add(concatKPVs);
        }
      }
    }

    String[] result = null;
    if (pvpList.isEmpty()) {
      result = new String[]{};
    } else {
      result = new String[pvpList.size()];
      pvpList.toArray(result);
    }
    return result;
  }

  /**
   * Called to parse a array of Key-Value Pair Strings (KVP-strings) to a JSONObject
   * containing the Key-Value pairs.
   * <p>KVP-strings are used to assigned property/option settings to class (e.g., the
   * {@linkplain FieldValueConverter} or {@linkplain InputValidator} classes) using
   * annotation (e.g., {@linkplain FieldValidation} or {@linkplain FieldConverter}
   * annotations) or a string stored in a database record that contains
   * one or more KVP-strings. In the case of annotation, the options are defined as an
   * array of KVP-strings (e.g., options={"key1=asdasd","key2=20",...}). In the case of
   * set of settings stored a database field the array of KVP-strings is concatted using
   * a {{@linkplain #KVPDelimiter KVPDelimiter}} (e.g. "||key1=asdasd||key2=20||...||").
   * These concatted string can be build or parsed to and from KVP-strings arrays using
   * the {@linkplain #concatKeyValuePairs concatKeyValuePairs} and {@linkplain
   * #parseKeyValuePairs(java.lang.String) parseKeyValuePairs} respectively.
   * <p>
   * The (KVP-string) formats are as follows:<ul>
   * <li><b>Single Key-Value pair:</b> input = "key=value". The <tt>value</tt>can be
   * quote with single (') or double (") quotes, but quotes should only be used to
   * preserve leading or training spaces.
   * <p><b>Note:</b>Do not put the Key-Value Pair inside "[..]" or "{..}" brackets.</p>
   * </li>
   * <li><b>Key-Value Array:</b> - If the value contains and array of values (e.g., a
   * list of discrete values), the input should be formatted as "key=[value1,value2,..]".
   * The list of values will be converted to a JSONArray containing the delimited values.
   * </li>
   * <li><b>An Array of Values:</b>- if the input string is surrounded by "[..]"
   * or "{..}" brackets and the inner string DOES NOT contains "=",  or is the input
   * string does not contain a "=", it is assume that string is a "," array of values.
   * The string will be parsed as a JSONArray, and will be assign to the resulting
   * returned JSONObject under the key="keyvalues".</li>
   * </ul>
   * <b>NOTES:</b> <ol>
   * <li>All keys are converted to lowercase. Value stings are trimmed unless it
   * is quoted. Start and end quotes are removed from quoted stings.
   * </li>
   * <li>
   * If the <tt>kvpArray</tt> contains only one value and the value contains a
   * KVPDelimiter, the parseKeyValuePairs will be calls to parse the KVP-strings into
   * an array if KVP-strings before it is processed,
   * </li>
   * </ol>
   * @param kvpArray the array of input KVP-strings to parse.
   * @return the JSONObject with parsed value (can be empty if input is empty.
   */
  public static JSONObject splitKeyValuePairs(String...kvpArray) {
    JSONObject result = new JSONObject();
    String firstVal = null;
    if ((kvpArray != null) && (kvpArray.length == 1) &&
               ((firstVal = kvpArray[0]) != null) &&
              ((firstVal.contains(DataEntry.KVPDelimiter)))) {
      kvpArray = parseKeyValuePairs(firstVal);
    }

    if ((kvpArray != null) && (kvpArray.length > 0)) {
      int pos = -1;
      for (String keyPair : kvpArray) {
        JSONArray valArr = null;
        if (((keyPair.startsWith("[")) && (keyPair.endsWith("]"))) ||
                    ((keyPair.startsWith("{")) && (keyPair.endsWith("}")))) {
          keyPair = keyPair.substring(1, keyPair.length()-1);
          if (keyPair.indexOf("=") >= 1) {
            List<String> subValues = DataEntry.splitString(keyPair, ",");
            if ((subValues != null) && (!subValues.isEmpty())) {
              String[] subArray = new String[subValues.size()];
              subValues.toArray(subArray);
              JSONObject subObject = DataEntry.splitKeyValuePairs(subArray);
              if ((subObject != null) && (subObject.length() > 0)) {
                for (String key : JSONObject.getNames(subObject)) {
                  result.put(key, subObject.get(key));
                }
              }
            }
          } else {
            if (((valArr = DataEntry.parseKeyValueArray(keyPair)) != null) &&
                    (valArr.length() > 0)) {
              result.put("keyvalues",valArr);
            }
          }
        } else if (((keyPair = DataEntry.cleanString(keyPair)) != null) &&
                ((pos = keyPair.indexOf("=")) >= 1) &&
                (pos < keyPair.length())) {
          String key = DataEntry.cleanLoString(keyPair.substring(0, pos));
          String value = DataEntry.cleanString(keyPair.substring(pos+1));
          if ((key == null) || (value == null)) {
            continue;
          }

          if (((value.startsWith("\"")) && (value.endsWith("\""))) ||
              ((value.startsWith("'")) && (value.endsWith("\'")))) {
            value = value.substring(1, value.length()-1);
            result.put(key, value);
          } else if (((value.startsWith("[")) && (value.endsWith("]"))) ||
                    ((value.startsWith("{")) && (value.endsWith("}")))) {
            value = value.substring(1, value.length()-1);
            if (value.indexOf("=") >= 1) {
              List<String> subValues = DataEntry.splitString(keyPair, ",");
              if ((subValues != null) && (!subValues.isEmpty())) {
                String[] subArray = new String[subValues.size()];
                subValues.toArray(subArray);
                JSONObject subObject = DataEntry.splitKeyValuePairs(subArray);
                if ((subObject != null) && (subObject.length() > 0)) {
                  result.put(key,subObject);
                }
              }
            } else {
              if (((valArr = DataEntry.parseKeyValueArray(value)) != null) &&
                    (valArr.length() > 0)) {
                result.put(key,valArr);
              }
            }
          } else {
            result.put(key,value);
          }
        } else {
          if (((valArr = DataEntry.parseKeyValueArray(keyPair)) != null) &&
                    (valArr.length() > 0)) {
            result.put("keyvalues",valArr);
          }
        }
      }
    }
    return result;
  }

  /**
   * A private method called by {@linkplain #splitKeyValuePairs(java.lang.String...)
   * splitKeyValuePairs} to the <tt>input</tt> string to a JSONArray and the check that
   * each value is parse to by calling splitKeyValuePairs
   * @param input the input string to parse
   * @return a JSONArray of values.
   */
  private static JSONArray parseKeyValueArray(String input) {
    JSONArray result = null;
    if ((input = DataEntry.cleanString(input)) != null) {
      input = input.replace("{", "[");
      input = input.replace("}", "]");
      if ((input.startsWith("[")) && (input.endsWith("]"))) {
        input = input.substring(1, input.length()-1);
      }

      input = "[" + input + "]";
      try {
        result = new JSONArray(input);
      } catch (Exception exp) {
        result = null;
      }
    }
    return (result == null)? new JSONArray(): result;
  }

  /**
   * Concatenate the stings in the array using the specified delimiter.
   * <p><b>Note:</b>If a {@linkplain DataEntry#cleanString(java.lang.String) cleaned}
   * delimiter if null, the skipBlanks flags is automatically set to true.</p>
   * @param delimiter the delimiter to used (default=" " if null)
   * @param skipBlanks if true, blank or null values in the list will be ignored,
   * Otherwise, a empty string ("") will inserted between two delimiters
   * @param strArgs the string to Concatenate.
   * @return the concatenated string or an empty string ("") is the input strArgs is
   * null or empty or contain only null or empty strings.
   */
  public static String concatString(String delimiter, boolean skipBlanks,
          String...strArgs)  {
    String result = null;
    if ((strArgs != null) && (strArgs.length > 0)) {
      delimiter = (delimiter == null)? " ": delimiter;
      skipBlanks = (DataEntry.cleanString(delimiter) == null)? true: skipBlanks;
      for (String inStr : strArgs) {
        inStr = DataEntry.cleanString(inStr);
        if (inStr == null) {
          if (skipBlanks) {
            continue;
          } else {
            inStr = "";
          }
        }

        if (result == null) {
          result = inStr;
        } else {
          result += (delimiter + inStr);
        }
      }
    }
    return result;
  }

  /**
   * Concatenate the stings in the array using the specified delimiter.
   * <p><b>Note:</b>If a {@linkplain DataEntry#cleanString(java.lang.String) cleaned}
   * delimiter if null, the skipBlanks flags is automatically set to true.</p>
   * @param delimiter the delimiter to used (default=" " if null)
   * @param skipBlanks if true, blank or null values in the list will be ignored,
   * Otherwise, a empty string ("") will inserted between two delimiters
   * @param strArgs the list of strings to Concatenate.
   * @return the concatenated string or an empty string ("") is the input strArgs is
   * null or empty or contain only null or empty strings.
   */
  public static String concatString(String delimiter, boolean skipBlanks,
                                                                List<String> strArgs)  {
    String result = null;
    if ((strArgs != null) && (!strArgs.isEmpty())) {
      delimiter = (delimiter == null)? " ": delimiter;
      skipBlanks = (DataEntry.cleanString(delimiter) == null)? true: skipBlanks;
      for (String inStr : strArgs) {
        inStr = DataEntry.cleanString(inStr);
        if (inStr == null) {
          if (skipBlanks) {
            continue;
          } else {
            inStr = "";
          }
        }

        if (result == null) {
          result = inStr;
        } else {
          result += (delimiter + inStr);
        }
      }
    }
    return result;
  }

  /**
   * Return the Proper Case of the String.
   * @param inStr String
   * @return String
   */
  public static String toProper(String inStr) {
    String result = null;
    inStr = (inStr == null)? null: inStr.trim().toLowerCase();
    if ((inStr != null) && (inStr.length() > 0)) {
      List<String> pWords = DataEntry.splitString(inStr, " ");
      if (pWords.size() > 0) {
        for (String sSubStr: pWords) {
          sSubStr = sSubStr.trim();
          if (sSubStr.equals("")) {
            continue;
          }

          String sCh1 = sSubStr.substring(0, 1);
          String sCh2 = sCh1.toUpperCase();
          sSubStr = sSubStr.replaceFirst(sCh1, sCh2);
          if (result == null) {
            result = sSubStr;
          } else {
            result += (" " + sSubStr);
          }
        }
      }
    }
    return result;
  }

  /**
   * Convert the input <tt>inputStr</tt> to a Tag ID (i.e., a DOC Element's ID). It
   * convert the input string to upper case and replace all ":" with "" and {".", " ",
   * "-"} with "_".
   * @param inputStr input string
   * @return converted Tag ID or null if the string is empty or only consist of ":".
   */
  public static String toTagId(String inputStr) {
    String result = DataEntry.cleanUpString(inputStr);
    if (result != null) {
      result = result.replace(":", "");
      result = result.replace(".", "_");
      result = result.replace(" ", "_");
      result = result.replace("-", "_");
      result = DataEntry.cleanString(result);
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="String Tests">
  /**
   * A Placeholder for the Application's Parameter Path Delimiter (default = ".").
   */
  private static String _ParamPathDelimeter = ".";
  
  /**
   * Get the Application's Parameter Path Delimiter (default = ".")
   * @return the assigned delimiter
   */
  public static String getParamPathDelimeter() {
    return DataEntry._ParamPathDelimeter;
  }
  
  /**
   * Set the Application's Parameter Path Delimiter (default = ".")
   * @param delimeter the custom delimiter (set to "." if null|""|" ")
   */
  public static void setParamPathDelimeter(String delimeter) {
    DataEntry._ParamPathDelimeter = 
        ((delimeter = DataEntry.cleanString(delimeter)) == null)?  ".": delimeter;
  }

  /**
   * Convert the input <tt>inputStr</tt> to a Parameter Key. It DOES NOT convert the
   * input case, but replace all ":" with "" and " " | {@linkplain 
   * #getParamPathDelimeter() DataEntry.paramPathDelimeter} with "_". All leading and
   * trailing white spaces and DataEntry.paramPathDelimeter are removed.
   * <p>
   * <b>NOTE:</b> DataEntry.ParamPathDelimeter cannot be used in a parameter name, 
   * because it will interfere with the building and parsing of Parameter Paths</p>
   * @param inputStr input string
   * @return converted parameter  or null if the string is empty or only consist of ":".
   */
  public static String toParamKey(String inputStr) {
    String del = DataEntry.getParamPathDelimeter();
    String result = DataEntry.stripDelimiter(inputStr, del);
    if (result != null) {
      result = result.replace(":", "");
      result = result.replace(" ", "_");
      result = result.replace(del, "_");
    }
    return result;
  }
  
  /**
   * OVERLOAD 1: Build a parameter path using <tt>baseParPath</tt> - a base parameter 
   * path - and a list of <tt>childKeys</tt>. Convert the <tt>childKeys</tt> list to a 
   * childKeys and call {@linkplain #buildParamPath(java.lang.String, java.lang.String...) 
   * OVERLOAD 2}.
   * array and call
   * @param basePath the base parameter path to amend the childKeys to
   * @param childKeys the list of child/sub parameter key to amend to the basePath
   * @return the resulting parameter path
   */
  public static String buildParamPath(String basePath, List<String> childKeys) {
    String result = null;
    if ((childKeys != null) && (!childKeys.isEmpty())) {
      String[] keyArr = new String[0];
      childKeys.toArray(keyArr);
      result = DataEntry.buildParamPath(basePath, keyArr);
    }
    return result;
  }

  /**
   * OVERLOAD 2: Build a parameter path using <tt>baseParPath</tt> - a base parameter 
   * path - and an array of <tt>childKeys</tt> - the child parameter key using the 
   * {@linkplain #ParamPathDelimeter DataEntry.ParamPathDelimeter} as the delimiter 
   * (default = "."). It handles the following special cases.<ul>
   * <li>IF (basePath = null|empty), return a path build from childKeys</li>
   * <li>IF (childKeys = null|empty), return baseParPath</li>
   * <li>IF ((baseParPath = null|empty) and (childKeys = null|empty), return null.</li>
   * <li>ELSE returns baseParPath with childKeys using the path delimiter</li>
   * </ul>
   * <b>NOTE:</b> Both <tt>baseParPath</tt> and each sub paramKey is cleaned by calling
   * {@linkplain #stripDelimiter(java.lang.String) DataEntry.stripDelimiter} before being 
   * processed. 
   * Before passing the <tt>childKeys</tt> each key validity should be checked using the 
   * {@linkplain #toParamKey(java.lang.String) DataEntry.toParamKey method}</p>
   * @param basePath the base parameter path to amend the childKeys to
   * @param childKeys an array of the child parameter keys in the build order
   * @return the resulting parameter path
   */
  public static String buildParamPath(String basePath, String...childKeys) {
    String del = DataEntry.getParamPathDelimeter();
    String result = DataEntry.stripDelimiter(basePath,del);
    if ((childKeys != null) && (childKeys.length > 0)) {
      for (String parKey : childKeys) {
        if ((parKey = DataEntry.cleanString(parKey)) == null) {
          continue;
        }
        
        if (result == null) {
          result = parKey;
        } else {
          result += del + parKey;
        }
      }
    }
    return result;
  }
  
  /**
   * Called to strip the <tt>delimiter</tt> from the start and end of <tt>input</tt>.
   * <p>
   * <b>NOTE:</b> By default it removes all leading and trailing white characters</p>
   * @param input the input string
   * @param delimiter the delimiter (ignored if null | "")
   * @return the cleaned string
   */
  public static String stripDelimiter(String input, String delimiter) {
    String result = DataEntry.cleanString(input);
    if ((result != null) && (delimiter != null) && (delimiter.length() > 0)) {
      while ((result != null) && (result.startsWith(delimiter))) {
        if (result.length() == 1) {
          result = null;
        } else {
          result = DataEntry.cleanString(result.substring(1));
        }
      }
    
      while ((result != null) && (result.endsWith(delimiter))) {
        if (result.length() == 1) {
          result = null;
        } else {
          result = result.substring(0, result.length()-1);
        }
      }
    }
    return result;
  }
    
  /**
   * Get the First Parameter Key in the <tt>paramPath</tt> assuming {@linkplain 
   * #ParamPathDelimeter DataEntry.ParamPathDelimeter} was used as the path delimiter. 
   * If the <tt>paramPath</tt> starts with a ".", it will first be removed. 
   * It handles the following special cases:<ul>
   * <li>if (paramPath = null|empty), return null</li>
   * <li>if (paramPath does not contain a delimiter), return paramPath</li>
   * <li>ELSE return the substring starting at the begin of paramPath to the first
   * delimiter position</li>
   * </ul>
   * @param paramPath
   * @return the first parameter's key
   */
  public static List<String> parseParamPath(String paramPath) {
    List<String> result = null;
    if ((paramPath = DataEntry.cleanString(paramPath)) != null) {
      String delimiter = DataEntry.getParamPathDelimeter();
      result = DataEntry.splitString(paramPath, delimiter);
    }
    return result;
  }
  
  /**
   * Get the First Parameter Key in the <tt>paramPath</tt> assuming a "." path delimiter. 
   * If the <tt>paramPath</tt> starts with a ".", it will first be removed. 
   * It handles the following special cases:<ul>
   * <li>if (paramPath = null|empty), return null</li>
   * <li>if (paramPath does not contain a delimiter), return paramPath</li>
   * <li>ELSE return the substring starting at the begin of paramPath to the first
   * delimiter position</li>
   * </ul>
   * @param paramPath
   * @return 
   */
  public static String getFirstParamKey(String paramPath) {
    String result = null;
    List<String> pathList = DataEntry.parseParamPath(paramPath);
    if (pathList != null) {
      int index = 0;
      while ((pathList.size() >= (index + 1)) && 
                                              ((result = pathList.get(index)) == null)) {
        index++;
      }
    }
    return result;
  }
  
  /**
   * Get the First Parameter Key in the <tt>paramPath</tt> assuming a "." path delimiter. 
   * If the <tt>paramPath</tt> starts with a ".", it will first be removed. 
   * It handles the following special cases:<ul>
   * <li>if (paramPath = null|empty), return null</li>
   * <li>if (paramPath does not contain a delimiter), return paramPath</li>
   * <li>ELSE return the substring starting at the begin of paramPath to the first
   * delimiter position</li>
   * </ul>
   * @param paramPath
   * @return 
   */
  public static String getLastParamKey(String paramPath) {
    String result = null;
    List<String> pathList = DataEntry.parseParamPath(paramPath);
    if (pathList != null) {
      int index = pathList.size()-1;
      while ((pathList.size() >= (index + 1)) && 
                                            ((result = pathList.get(index)) == null)) {
        index--;
      }
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="String Tests">
  /**
   * Check if newValue matches curValue. First clean newValue by calling the
   * cleanString method and then compare them - ignoring case is bIngnoreCase.
   * Return true if both strings are empty or match.
   * Note: sCurvalue is not "cleaned"
   * @param curValue the current value
   * @param newValue the new input value
   * @param ignoreCase if true, do a case insensitive comparison
   * @return boolean
   */
  public static boolean isEq(String curValue, String newValue,
          boolean ignoreCase) {
    newValue = DataEntry.cleanString(newValue);
    return ((curValue == null) ? (newValue == null)
            : ((ignoreCase)? curValue.equalsIgnoreCase(newValue):
            curValue.equals(newValue)));
  }

  /**
   * Check if newValue matches curValue. If (!skipTrim), first clean newValue by
   * calling the cleanString method and then compare them - ignoring case if
   * bIngnoreCase=true. Return true if both strings are empty or match.
   * Note: sCurvalue is not "cleaned"
   * @param curValue the current value
   * @param newValue the new input value
   * @param ignoreCase if true, do a case insensitive comparison
   * @param skipTrim if true, newValue is not trimmed to remove leading and/or
   * trailing spaces.
   * @return boolean
   */
  public static boolean isEq(String curValue, String newValue,
          boolean ignoreCase, boolean skipTrim) {
    newValue = (skipTrim)? newValue: DataEntry.cleanString(newValue);
    return ((curValue == null) ? (newValue == null)
            : ((ignoreCase)? curValue.equalsIgnoreCase(newValue):
            curValue.equals(newValue)));
  }

  /**
   * Check if newValue matches curValue. First clean newValue by calling the
   * cleanString with maxLen set method and then compare them -
   * ignoring case is bIngnorCase.Return true if both strings are empty or match.
   * Note: sCurvalue is not "cleaned"
   * @param curValue String
   * @param newValue String
   * @param ignoreCase boolean
   * @param maxLen the maximum length
   * @return boolean
   */
  public static boolean isEq(String curValue, String newValue,
          boolean ignoreCase, int maxLen) {
    newValue = DataEntry.cleanString(newValue, maxLen);
    return ((curValue == null) ? (newValue == null)
            : ((ignoreCase)? curValue.equalsIgnoreCase(newValue):
            curValue.equals(newValue)));
  }

  /**
   * Check if <tt>phrase</tt> is a partial match for <tt>target</tt> based on the
   * specified <tt>matchType</tt>. If (<tt>ignoreCase</tt>), thus matching is case
   * insensitive.
   * @param target the target string
   * @param phrase the partial phrase to match
   * @param matchType the Match Type (assumed FULL if unassigned)
   * @param ignoreCase true to ignore case
   * @return true if a match.
   */
  public static boolean isMatch(String target, String phrase, StringMatch matchType,
                                                                  boolean ignoreCase){
    boolean result = false;
    String str1 = DataEntry.cleanString(target);
    String str2 = DataEntry.cleanString(phrase);
    if ((str1 != null) && (str2 != null)) {
      if (ignoreCase) {
        str1 = str1.toLowerCase();
        str2 = str2.toLowerCase();
      }
      matchType = (matchType == null)? StringMatch.FULL: matchType;
      switch (matchType) {
        case FULL:
          result = str1.equals(str2);
          break;
        case ENDWITH:
          result = str1.endsWith(str2);
          break;
        case STARTWITH:
          result = str1.startsWith(str2);
          break;
        case CONTAINS:
          result = str1.contains(str2);
          break;
      }
    }
    return result;
  }

  /**
   * Check if a value (strValue) is equal to a value in the array strArray of possible
   * values. Return false if strValue = ""|null or if strArray = null|empty.
   * Matchings is not case sensitive.
   * @param strValue string to check
   * @param strArray array of string to match
   * @return true if a match is found.
   */
  public static boolean inStringArray(String strValue, String...strArray) {
    boolean result = false;
    try {
      if (((strValue = DataEntry.cleanString(strValue)) != null) &&
                                      ((strArray != null) && (strArray.length > 0))) {
        for (String item : strArray) {
          if (strValue.equalsIgnoreCase(item)) {
            result = true;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.inStringArray Error:\n {1}",
              new Object[]{DataEntry.class.getSimpleName(), pExp.getMessage()});
    }
    return result;
  }

  /**
   * Check if a value (strValue) is equal to a value in the array strArray of possible
   * values. Return false if strValue = ""|null or if strArray = null|empty.
   * Matchings is not case sensitive.
   * @param item string to check
   * @param itemArray array of string to match
   * @return true if a match is found.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public static <TItem> boolean inArray(TItem item, TItem...itemArray) {
    boolean result = false;
    try {
      if ((item != null) && (itemArray != null) && (itemArray.length > 0)) {
        for (TItem arrItem : itemArray) {
          if (item.equals(arrItem)) {
            result = true;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.inArray Error:\n {1}",
              new Object[]{DataEntry.class.getSimpleName(), pExp.getMessage()});
    }
    return result;
  }


  /**
   * Check if the items in <tt>arr1</tt> matches the items in <tt>arr2</tt> - not
   * necessary in the same order.
   * @param <TItem> the array item type
   * @param arr1 array 1
   * @param arr2 array 2
   * @return true if all items match or both arrays are empty, false is arr1 and/or
   * arr2 is null or arr1.length != arr2.length
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public static <TItem> boolean isEq(TItem[] arr1, TItem...arr2) {
    boolean result = ((arr1 != null) && (arr2 != null) && (arr1.length == arr2.length));
    if (result) {
      for (TItem item1 : arr1) {
        if (!DataEntry.inArray(item1, arr2)) {
          result = false;
          break;
        }
      }
    }
    return result;
  }

  /**
   * Convert the <tt>itemArray</tt> to an ArrayList.
   * <p><b>Note:</b> 'null' items are skipped</p>
   * @param <TItem> the list item type
   * @param itemArray the array of items to add
   * @return the list of items or an empty list of itemArray = null|Empty.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public static <TItem> List<TItem> toList(TItem...itemArray) {
    List<TItem> result = new ArrayList<>();
    try {
      if ((itemArray != null) && (itemArray.length > 0)) {
        for (TItem item : itemArray) {
          if (item != null) {
            result.add(item);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toList Error:\n {1}",
              new Object[]{DataEntry.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Converts a List containing Items of type <tt>TListItem</tt>, which extends or
   * implement type TItem to an array of type TItem.
   * @param <TItem>
   * @param <TListItem> extends TItem
   * @param itemClass the class of type TItem
   * @param list list of TListItems
   * @return an array if type TItem. The array is empty if List = null|empty
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public static <TItem, TListItem extends TItem> TItem[]
                          toArray(Class<TItem> itemClass, Collection<TListItem> list) {
    int size = (list == null)? 0: list.size();
    TItem[] result = (TItem[]) Array.newInstance(itemClass, size);
    if (size > 0) {
      int idx = 0;
      for (TListItem item : list) {
        result[idx] = (TItem) item;
        idx++;
      }
    }
    return result;
  }
  

  /**
   * Converts a List containing Items of type <tt>TListItem</tt>, which extends or
   * implement type TItem to an array of type TItem. It handles the following cases:<ul>
   * <li>IF (srcArr = null | empty), return baseArr (even if empty | null)</li>
   * <li>IF (baseArr = null | empty), return srcArr</li>
   * <li>Else, initiate a new array with size baseArr.length + srcArr.length and
   * fill is with first the baseArr elements and then the srcArr elements.</li>
   * </ul>
   * @param <TItem> the item type of the arrays
   * @param baseArr the base array to which the <tt>srcArr</tt> will be appended
   * @param srcArr the array of TITems to append to the base array.
   * @return an array if type TItem. The array is empty if List = null|empty if both
   * baseArr and srcArr = null| empty.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public static <TItem> TItem[] appendToArray(TItem[] baseArr, TItem...srcArr) {
    TItem[] result = null;
    if ((srcArr == null) || (srcArr.length == 0)) {
      result = baseArr;
    } else if ((baseArr == null) || (baseArr.length == 0)) {
      result = srcArr;
    } else {
      int size = baseArr.length + srcArr.length;
      Class<TItem> compClass = (Class<TItem>) baseArr.getClass().getComponentType();
      result = (TItem[]) Array.newInstance(compClass, size);
      if (size > 0) {
        int idx = 0;
        int baseSize = baseArr.length;
        for (int i = 0; i < size; i++) {
          if (i < baseSize) {
            result[i] = baseArr[i];
          } else {
            result[i] = srcArr[i - baseSize];
          }
        }
      }
    }
    return result;
  }

  /**
   * Called to left Pad the sInstr with sPadStr until the sInt.Length is >= iLength.
   * if (sInstr = null), set sInstr = "" before padding. if sPadStr = empty or null,
   * set sPadStr = " ".
   * @param inStr String
   * @param sPadStr String
   * @param iLength int
   * @return String
   */
  public static String leftPadString(String inStr, String sPadStr, int iLength) {
    String result = (inStr == null) ? "" : inStr;
    sPadStr = ((sPadStr == null) || (sPadStr.equals(""))) ? " " : sPadStr;
    while (result.length() < iLength) {
      result = sPadStr + result;
    }
    return result;
  }

  /**
   * Trim all leading and ending spaces and if the input string is not empty strip out
   * any non-numeric characters
   * @param inStr
   * @param bIsDecimal
   * @return String
   */
  public static String cleanNumericString(String inStr) {
    String result = null;
    inStr = DataEntry.cleanString(inStr);
    if (inStr != null) {
      String sRegExp = "^[\\D]*[ ]*g$";
      inStr = inStr.replaceAll(sRegExp, "");
      result = inStr.replaceAll(",", "");
      result = DataEntry.cleanString(result);
    }
    return result;
  }

  /**
   * Format a numeric string by adding a delimiter (e.g., sDelimiter="-" - not an empty
   * string, but it could be a space or spaces) at the character spacing defined in
   * pSpacing after inStr is trimmed from any leading or ending spaces. For example
   * formating sInstr="0000000000" using pSpacing={3,3,4} using sDelimiter="-" will
   * return "000-000-0000". If sInstr.length exceeds the sum of pSpacing, the trailing
   * characters will be ignored.  if sInstr.length less than the sum of pSpacing, the
   * String will be right padded with "0". The trimmed sInstr will be returned if
   * (pSpacing=null|empty) or (sDelimiter=null|"").
   * @param inStr String
   * @param bIsDecimal
   * @return String
   */
  public static String formatNumericString(String inStr, String sDelimiter,
            int[] pSpacings) {
    String result = null;
    inStr = DataEntry.cleanString(inStr);
    if ((sDelimiter == null) || (sDelimiter.equals("")) ||
            (pSpacings == null) || (pSpacings.length == 0)) {
      result = inStr;
    } else if (inStr != null) {
      int iSumChars = 0;
      for (int iChars : pSpacings) {
        iSumChars += iChars;
      }

      if (inStr.length() < iSumChars) {
        inStr = DataEntry.rightPadString(inStr, "0", iSumChars);
      }

      int iLast = 0;
      List<String> pSubStrings = new ArrayList<>();
      for (int iChars : pSpacings) {
        if (iChars <= 0) {
          continue;
        }
        int iNext = iLast + iChars;
        if (iNext >= (inStr.length()-1)) {
          pSubStrings.add(inStr.substring(iLast));
          iLast = inStr.length();
          break;
        } else {
          pSubStrings.add(inStr.substring(iLast, iNext));
          iLast = iNext;
        }
      }

      if (pSubStrings.size() <= 1) {
        result = inStr;
      } else {
        for (String sSubStr : pSubStrings) {
          if (result == null) {
            result = sSubStr;
          } else {
            result += sDelimiter;
            result += sSubStr;
          }
        }
      }
    }
    return result;
  }

  /**
   * Called to Pad right the sInstr with sPadStr until the sInt.Length is >= iLength.
   * if (sInstr = null), set sInstr = "" before padding. if sPadStr = empty or null,
   * set sPadStr = " ".
   * @param inStr String
   * @param sPadStr String
   * @param iLength int
   * @return String
   */
  public static String rightPadString(String inStr, String sPadStr, int iLength) {
    String result = (inStr == null) ? "" : inStr;
    sPadStr = ((sPadStr == null) || (sPadStr.equals(""))) ? " " : sPadStr;
    while (result.length() < iLength) {
      result += sPadStr;
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Number/Boolean Test/Conversions">
  /**
   * Return a "clean" integer value or null if the value is unassigned or invalid.
   * if iLBound or iUBound (i.e., the Lower or upper value range boundaries) are set
   * and iVal falls outside the boundaries return a null value.
   * @param iVal Integer
   * @param iLBound Integer
   * @param iUBound Integer
   * @return Integer
   */
  public static Integer cleanInteger(Integer iVal, Integer iLBound, Integer iUBound) {
    Integer iResult = iVal;
    if (iVal != null) {
      if ((iLBound != null) &&  (iVal < iLBound)) {
        iResult = null;
      } else if ((iUBound != null) && (iVal > iUBound)) {
        iResult = null;
      }
    }
    return iResult;
  }

  /**
   * Check if iNewValue matches iCurValue.
   * Return true if both values are null or match.
   * @param iCurValue the current/base Integer Value
   * @param iNewValue the new Integer value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Integer iCurValue, Integer iNewValue) {
    return (iCurValue == null) ? (iNewValue == null) : iCurValue.equals(iNewValue);
  }

  /**
   * Check if iNewValue  complies to range set limits and matches iCurValue.
   * Call cleanInteger method on the iNewValue before comparing the two values.
   * Return true if both values are null or match.
   * @param iCurValue the current/base Integer Value
   * @param iNewValue the new Integer value
   * @param ignoreCase boolean
   * @return true if both is null or equal and in bounds
   */
  public static boolean isEq(Integer iCurValue, Integer iNewValue,
          Integer iLBound, Integer iUBound) {
    iNewValue = DataEntry.cleanInteger(iNewValue, iLBound, iUBound);
    return (iCurValue == null) ? (iNewValue == null): iCurValue.equals(iNewValue);
  }

  /**
   * Check if iNewValue matches iCurValue.
   * Return true if both values are null or match.
   * @param iCurValue the current/base Short Value
   * @param iNewValue the new Short value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Short iCurValue, Short iNewValue) {
    return (iCurValue == null) ? (iNewValue == null) : iCurValue.equals(iNewValue);
  }

  /**
   * Check if iNewValue matches iCurValue.
   * Return true if both values are null or match.
   * @param dCurValue Double
   * @param dNewValue Double
   * @return boolean
   */
  public static boolean isEq(Double dCurValue, Double dNewValue) {
    return (dCurValue == null) ? (dNewValue == null) : dCurValue.equals(dNewValue);
  }

  /**
   * Check if bNewValue matches bCurValue.
   * Return true if both values are null or match.
   * @param dCurValue current Boolean value
   * @param dNewValue new Boolean value
   * @return true if both is null or equal.
   */
  public static boolean isEq(Boolean dCurValue, Boolean dNewValue) {
    return (dCurValue == null) ? (dNewValue == null) : dCurValue.equals(dNewValue);
  }

  /**
   * Check if <tt>curDate</tt> matches <tt>otherDate</tt> for the specified 
   * <tt>interval</tt> using for curDate.timeZone as the common TimeZone if 
   * <tt>convertTimeZone</tt> = true.
   * Return true if both values are null or match.
   * @param curDate the base date
   * @param otherDate the other date
   * @param interval the Date Interval to base the comparison on.
   * @param convertTimeZone true to convert <tt>other.timeZone</tt> to this.timeZone
   * before doing the comparison.
   * @return true if both values are null or equal
   */
  public static boolean isEq(DateTime curDate, DateTime otherDate, Interval interval,
                                                              boolean convertTimeZone) {
    return (curDate == null)? (otherDate == null) : 
                                   curDate.equals(otherDate, interval, convertTimeZone);
  }

  /**
   * Check if <tt>curDate</tt> matches <tt>otherDate</tt> for the specified 
   * <tt>interval</tt> - the comparison ignores the TimeZone except if Interval = 
   * MILLISCECONDS.
   * Return true if both values are null or match.
   * @param curDate the base date
   * @param otherDate the other date
   * @param interval the Date Interval to base the comparison on.
   * @return true if both values are null or match.
   */
  public static boolean isEq(DateTime curDate, DateTime otherDate, Interval interval) {
    return (curDate == null)? (otherDate == null) : curDate.equals(otherDate, interval);
  }

  /**
   * Check if pNewValue matches pCurValue.
   * Return true if both values are null or match.
   * @param curValue Object
   * @param otherValue Object
   * @return boolean
   */
  public static boolean isEq(Object curValue, Object otherValue) {
    return (curValue == null)? (otherValue == null) : curValue.equals(otherValue);
  }

 /**
   * Return the Parsed integer value represented by sInval. If it contains a period (.)
   * it will return the integer value of the string left of the period. It will remove
   * all "," separators from the string. If the string is empty or the conversion fails
   * it returns iDefault.
   * @param inputStr String
   * @param defaultVal int
   * @return int
   */
  public static int parseInt(String inputStr, int defaultVal) {
    Integer result = null;
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        int iIdx = inputStr.indexOf(".");
        if (iIdx > 0) {
          inputStr = inputStr.substring(0,iIdx);
        }
        inputStr = inputStr.replaceAll(",", "");
        if (!inputStr.equals("")) {
          result = Integer.parseInt(inputStr);
        }
      }
    } catch (Exception exp) {
      result = null;
    }
    return (result == null)? defaultVal: result;
  }

 /**
   * Return the Parsed long value represented by inputStr. If it contains a period (.)
   * it will return the integer value of the string left of the period. It will remove
   * all "," separators from the string. If the string is empty or the conversion fails
   * it returns defaultVal.
   * @param inputStr input String to parse
   * @param defaultVal a default number to return is not a valid number
   * @return the parsed long value (or the defaultVal - is empty)
   */
  public static long parseLong(String inputStr, long defaultVal) {
    Long result = null;
    try {
      inputStr = DataEntry.cleanString(inputStr);
      if (inputStr != null) {
        int iIdx = inputStr.indexOf(".");
        if (iIdx > 0) {
          inputStr = inputStr.substring(0,iIdx);
        }
        inputStr = inputStr.replaceAll(",", "");
        if (!inputStr.equals("")) {
          result = Long.parseLong(inputStr);
        }
      }
    } catch (Exception exp) {
      result = null;
    }
    return (result == null)? defaultVal: result;
  }

  /**
   * Return the Parsed double value represented by sInval. It will remove
   * all "," separators from the string. If the string is empty or the conversion fails
   * it returns dDefault.
   * @param inputStr String
   * @param defaultVal double
   * @return double
   */
  public static double parseDouble(String inputStr, double defaultVal) {
    Double result = null;
    try {
      inputStr = DataEntry.cleanNumericString(inputStr);
      if (inputStr != null) {
        if (!inputStr.equals("")) {
          result = Double.parseDouble(inputStr);
        }
      }
    } catch (Exception exp) {
      result = null;
    }
    return (result == null)? defaultVal: result;
  }

  /**
   * Return iNumber as an alpha character String consisting of upper case values A..Z.
   * Valid iNumber values are greater than 0.  Returns "" if iNumber is zero, negative
   * or null. For numbers greater than 25, a "X" will prepend for the number of times
   * the number is larger the 25. Example: iNumber=26 returns "XA", and iNumber=51
   * returns "XXA".
   * @param iNumber Integer
   * @return String
   */
  public static String toAlpha(Integer iNumber) {
    String sAlpha = "";
    if ((iNumber != null) && (iNumber > 0)) {
      Integer iBase = 64;
      Integer iNumChars = 26;
      Integer iRemain = iNumber;
      while (iRemain > 0) {
        if (iRemain >= iNumChars) {
          sAlpha += "X";
        } else {
          char pChar = (char)(iBase+iRemain);
          sAlpha += String.valueOf(pChar);
        }
        iRemain -= iNumChars;
      }
    }
    return sAlpha;
  }

  /**
   * Return a List with the non-null elements of pArray.
   * @param <T>
   * @param pArray T[]
   * @return List<T>
   */
  public static <T> List<T> newAsList(T[] pArray) {
    List<T> pResults = new ArrayList<>();
    if ((pArray != null) && (pArray.length > 0)) {
      for (T pObj : pArray) {
        if (pObj != null) {
          pResults.add(pObj);
        }
      }
    }
    return pResults;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Number Formating">
  /**
   * Get a Double value formatted to the specified format
   * @param value the double value (can be null)
   * @param decimals the number of decimal places Range >= 0
   * @param showPlus true to show the plus sign in font the the number
   * @param prefix the prefix to add to a non-null value
   * @param suffix the suffix to add to a non-null value
   * @param nullValStr the string to return is value=null.
   * @return the formatted string.
   */
  public static String getDoubleFormat(Double value, int decimals, boolean showPlus,
          String prefix, String suffix, String nullValStr) {
    String result = nullValStr;
    if ((value != null) && (!value.equals(Double.NaN))) {
      String formatStr = "%1$";
      if (showPlus) {
        formatStr += "+";
      }
      decimals = (decimals >= 0)? decimals: 0;
      formatStr += "." + decimals + "f";
      result = String.format(formatStr, value);
      if (prefix != null) {
        result = prefix + result;
      }
      if (suffix != null) {
        result += suffix;
      }
    }
    return result;
  }
  /**
   * Get a Double value formatted to the specified format
   * @param value the double value (can be null)
   * @param decimals the number of decimal places Range >= 0
   * @param thousandSeparator true to add thousand separators
   * @param showPlus true to show the plus sign in font the the number
   * @param prefix the prefix to add to a non-null value
   * @param suffix the suffix to add to a non-null value
   * @param nullValStr the string to return is value=null.
   * @return the formatted string.
   */
  public static String getDoubleFormat(Double value, int decimals,
          boolean thousandSeparator, boolean showPlus,
          String prefix, String suffix, String nullValStr) {
    String result = nullValStr;
    if ((value != null) && (!value.equals(Double.NaN))) {
      String formatStr = "%1$";
      if (showPlus) {
        formatStr += "+";
      }
      if (thousandSeparator) {
        formatStr += ",";
      }
      decimals = (decimals >= 0)? decimals: 0;
      formatStr += "." + decimals + "f";
      result = String.format(formatStr, value);
      if (prefix != null) {
        result = prefix + result;
      }
      if (suffix != null) {
        result += suffix;
      }
    }
    return result;
  }
  /**
   * Get a Long value formatted to the specified format
   * @param value the Long value (can be null)
   * @param thousandSeparator true to add thousand separators
   * @param showPlus true to show the plus sign in font the the number
   * @param prefix the prefix to add to a non-null value
   * @param suffix the suffix to add to a non-null value
   * @param nullValStr the string to return is value=null.
   * @return the formatted string or the nullValStr if value=null.
   */
  public static String getLongFormat(Long value, boolean thousandSeparator,
          boolean showPlus, String prefix, String suffix, String nullValStr) {
    String result = nullValStr;
    if (value != null) {
      String formatStr = "%1$";
      if (showPlus) {
        formatStr += "+";
      }
      if (thousandSeparator) {
        formatStr += ",";
      }
      formatStr += "d";
      result = String.format(formatStr, value);
      if (prefix != null) {
        result = prefix + result;
      }
      if (suffix != null) {
        result += suffix;
      }
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Input Validations">
  /**
   * Check is the inputValue complies with the set format of a parameter name
   * consisting of only alphanumeric characters or underscore and starting with
   * a letter. No limit in the length.
   * @param inputValue
   * @return boolean
   */
  public static boolean isValidParameter(String inputValue) {
    boolean result = false;
    String sRegExp = "^[a-zA-Z][a-zA-Z0-9_]*$";
    inputValue = DataEntry.cleanString(inputValue);
    result = (inputValue == null)? false : inputValue.matches(sRegExp);
    return result;
  }

  /**
   * Validate that the e-mail entry format is correct. Use InternetAddress.parse() to
   * check if it is valid.
   * @param inputValue String
   * @return boolean
   */
  public static boolean isValidEMail(String inputValue) {
    boolean bIsValid = false;
    try {
      inputValue = DataEntry.cleanString(inputValue);
      if (inputValue != null) {
        String sRegExp = "(^[a-z]([a-z0-9_.-]*)@([a-z]([a-z0-9_.-]*))"
                + "([.][a-z]{3})$)|"
                + "(^[a-z]([a-z0-9_.-]*)@([a-z]([a-z0-9_.-]*))"
                + "(.[a-z]{3})(.[a-z]{2})*$)";
        bIsValid = inputValue.matches(sRegExp);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "DataEntry.isValidEMail Error:\n {1}",
              new Object[]{pExp.getMessage()});
    }
    return bIsValid;
  }

  /**
   * Check is the username complies with the set format consisting of 5-16
   * characters, and only alphanumeric characters or underscore. The username
   * must start with a letter.
   * @param inputValue
   * @return boolean
   */
  public static boolean isValidUsername(String inputValue) {
    boolean bIsValid = false;
    String sRegExp = "^[a-zA-Z][a-zA-Z0-9_]{4,15}$";
    bIsValid = ((inputValue == null) || (inputValue.equals(""))) ? false
            : inputValue.matches(sRegExp);
    return bIsValid;
  }

  /**
   * The Hint that explains the format of a valid username. (i.e. {@value}).
   */
  public final static String UsernameHint = "A valid username has 5-16 alpha-numeric "
          + "characters or underscores. The first character must be an alpha value."
          + "No spaces are allowed. NOTE: The username is case-sensitive.";

  /**
   * Check if the inputValue is a valid 10-digit telephone number. Call
   * getTelNoasNumber to strip all non-numeric characters and return true
   * if the length = 10 and the first digit is not 0.
   * @param inputValue String
   * @return boolean
   */
  public static boolean isValidTelNo(String inputValue) {
    boolean bIsValid = false;

    String sNumber = DataEntry.getTelNoasNumber(inputValue);
    bIsValid = (sNumber == null) ? false
            : ((sNumber.length() == 10) && (!sNumber.startsWith("0")));
    return bIsValid;
  }

  /**
   * Strip the any spaces, brackets, dashes, etc. and return only the digits
   * of the inputValue telephone number
   * @param inputValue String
   * @return String
   */
  public static String getTelNoasNumber(String inputValue) {
    String sNumber = "";
    if ((inputValue != null) && (!inputValue.trim().equals(""))) {
      sNumber = inputValue.replaceAll("[ ()-./]*", "");
    }
    return DataEntry.cleanString(sNumber.trim());
  }

  /**
   * Convert inputValue to the standard "(000) 000-0000" format. return an empty
   * string is the number is invalid
   * @param inputValue String
   * @return String
   */
  public static String getTelNoAsString(String inputValue) {
    String sOutStr = "";
    String sNumber = getTelNoasNumber(inputValue);
    if ((sNumber != null) && (sNumber.length() == 10)
            && (!sNumber.startsWith("0"))) {
      sOutStr = "(" + sNumber.substring(0, 3) + ")";
      sOutStr += " " + sNumber.substring(3, 6) + "-";
      sOutStr += sNumber.substring(6);
    }
    return DataEntry.cleanString(sOutStr);
  }

  /**
   * Check if the inputValue is a valid web site Address. It initiates a UrlValidator
   * and if inputValue is not empty, call the validator's isValidInput method to
   * validate the inputValue.
   * @param inputValue the input to validate
   * @return false if inputValue=""|null or UrlValidator.isValidInput(inputValue)
   */
  public static boolean isValidWebAddress(String inputValue) {
    boolean bResult = false;
    try {
      inputValue = DataEntry.cleanString(inputValue);
      if (inputValue != null) {
        int[] pProtocols = new int[]{UrlProtocolEnums.HTTP, UrlProtocolEnums.HTTPS};
        UrlValidator pValidator = new UrlValidator();
        pValidator.setUrlProtocols(pProtocols, UrlProtocolEnums.HTTP);

        bResult = pValidator.isValidInput(inputValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isValidWebAddress Error:\n {1}",
              new Object[]{"DataEntry", pExp.getMessage()});
    }
    return bResult;
  }

  /**
   * Clean the inputValue and return a fully resolved web site address. For example:
   * inputValue="www.mysite.com" will return "http://www.mysite.com"
   * @param inputValue the input to clean
   * @return a fully resolved web site address or null if inputValue is invalid.
   */
  public static String cleanWebAddress(String inputValue) {
    String result = null;
    try {
      inputValue = DataEntry.cleanString(inputValue);
      if (inputValue != null) {
        int[] pProtocols = new int[]{UrlProtocolEnums.HTTP, UrlProtocolEnums.HTTPS};
        UrlValidator pValidator = new UrlValidator();
        pValidator.setUrlProtocols(pProtocols, UrlProtocolEnums.HTTP);

        result = pValidator.toString(inputValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getCleanWebAddress Error:\n {1}",
              new Object[]{"DataEntry", pExp.getMessage()});
    }
    return result;
  }

  /**
   * Check if the inputValue is a valid ftp site Address. It initiates a UrlValidator
   * and if inputValue is not empty, call the validator's isValidInput method to
   * validate the inputValue.
   * @param inputValue the input to validate
   * @return false if inputValue=""|null or UrlValidator.isValidInput(inputValue)
   */
  public static boolean isValidFtpAddress(String inputValue) {
    boolean bResult = false;
    try {
      inputValue = DataEntry.cleanString(inputValue);
      if (inputValue != null) {
        int[] pProtocols = new int[]{ UrlProtocolEnums.FTP};
        UrlValidator pValidator = new UrlValidator();
        pValidator.setUrlProtocols(pProtocols, UrlProtocolEnums.FTP);

        bResult = pValidator.isValidInput(inputValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.isValidFtpAddress Error:\n {1}",
              new Object[]{"DataEntry", pExp.getMessage()});
    }
    return bResult;
  }

  /**
   * Clean the inputValue and return a fully resolved ftp site address. For example:
   * inputValue="ftp.mysite.com" will return "ftp://ftp.mysite.com"
   * @param inputValue the input to clean
   * @return a fully resolved ftp site address or null if inputValue is invalid.
   */
  public static String cleanFtpAddress(String inputValue) {
    String result = null;
    try {
      inputValue = DataEntry.cleanString(inputValue);
      if (inputValue != null) {
        int[] pProtocols = new int[]{UrlProtocolEnums.FTP};
        UrlValidator pValidator = new UrlValidator();
        pValidator.setUrlProtocols(pProtocols, UrlProtocolEnums.FTP);

        result = pValidator.toString(inputValue);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getCleanWebAddress Error:\n {1}",
              new Object[]{"DataEntry", pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Password Validation/Conversions">
  /**
   * Check whether the two un-encrypted passwords match.
   * @param savedPw the first password.
   * @param password2 the first password.
   * @return true if it is a match.
   */
  public static boolean isPasswordEq(String savedPw, String inputPw) {
    boolean result = false;
    savedPw = DataEntry.cleanString(savedPw);
    inputPw = DataEntry.cleanString(inputPw);
    PasswordValidator validator = null;
    if ((savedPw != null) && (inputPw != null) &&
        (PasswordValidator.isRegistered()) &&
        ((validator = PasswordValidator.newInstance()) != null) &&
        (validator.isValidInput(inputPw))) {
      result = validator.isEq(savedPw, inputPw, null);
    }
    return result;
  }

  /**
   * Check whether the encrypted encrypted passwords match and raw entered enter, using
   * the specified encryption salt.
   * @param encryptedPw the saved (encrypted) password.
   * @param inputPw the raw un-encrypted password to validate.
   * @param salt the salt string to use
   * @return true if it is a match.
   */
  public static boolean isPasswordEq(String encryptedPw, String inputPw, String salt) {
    boolean result = false;
    encryptedPw = DataEntry.cleanString(encryptedPw);
    inputPw = DataEntry.cleanString(inputPw);
    PasswordValidator validator = null;
    if ((encryptedPw != null) && (inputPw != null) &&
        (PasswordValidator.isRegistered()) &&
        ((validator = PasswordValidator.newInstance()) != null) &&
        (validator.isValidInput(inputPw))) {
      result = validator.isEq(encryptedPw, inputPw, salt);
    }
    return result;
  }

  /**
   * Generate a Random Password consistent with the password requirements.
   * Initiate the application's {@linkplain PasswordValidator} and call its {@linkplain
   * PasswordValidator#getRandomPassword() getRandomPassword} method.
   * @return the random password or "password"
   */
  public static String getRandomPassword() {
    String result = null;
    PasswordValidator validator = null;
    if ((PasswordValidator.isRegistered()) &&
            ((validator = PasswordValidator.newInstance()) != null)) {
      result = validator.getRandomPassword();
    }
    return (result == null)? "password": result;
  }

  /**
   * Encrypts a password using a specified Salt string (Optional) and return a
   * 32-character hexadecimal string.
   * @param password String
   * @param salt String
   * @return String
   * @throws Exception
   */
  public static String hashPassword(String password, String salt) {
    String result = null;
    try {
      password = DataEntry.cleanString(password);
      if (password == null) {
        throw new Exception("The Password is empty or undefined");
      }
      byte[] byteArr = null;
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.reset();
      salt = DataEntry.cleanString(salt);
      if (salt == null) {
        digest.update(password.getBytes());
        byteArr = digest.digest();
      } else {
        digest.update(salt.getBytes());
        byteArr = digest.digest(password.getBytes());
      }
      BigInteger hash = new BigInteger(1, byteArr);
      result = hash.toString(16);
      while (result.length() < 32) {
        result = "0" + result;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.hashPassword Error:\n {1}",
              new Object[]{DataEntry.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Unique IDs">
  /**
   * Generate a new random hash Salt value (32 characters)
   * @return a random 32-character hash Salt
   */
  public static String newHashSalt() {
    Random random = new Random();
    byte[] byteArr = new byte[12];
    random.nextBytes(byteArr);
    BigInteger hash = new BigInteger(1, byteArr);
    String result = hash.toString(16);
    while (result.length() < 32) {
      result = "0" + result;
    }
    return result;
  }

  /**
   * Generate a new unique record ID starting with today's date and a random hash Salt
   * The return value is a 32-character string.
   * @return the unique ID or null is an error occurred
   */
  public static String newUniqueId() {
    String baseStr = null;
    String hashSalt = null;
    DateTime today = DateTime.getNow(null);
    baseStr = today.toString();
    hashSalt = DataEntry.newHashSalt();
    return DataEntry.newUniqueId(baseStr, hashSalt);
  }

  /**
   * Generate a new unique record ID starting with sBaseStr and using sSalt. The return
   * value a 32-character string.
   * @param baseStr a Base string used .
   * @param hashSalt a Hash Salt string (can be null to use the default salt Hash
   * @return the unique ID or null is an error occurred
   */
  public static String newUniqueId(String baseStr, String hashSalt) {
    String result = null;
    try {
      baseStr = DataEntry.cleanString(baseStr);
      if (baseStr == null) {
        throw new Exception("The UniqueId's Base String cannot be null");
      }

      hashSalt = DataEntry.cleanString(hashSalt);
      byte[] byteArr = null;
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.reset();
      if (hashSalt == null) {
        digest.update(baseStr.getBytes());
        byteArr = digest.digest();
      } else {
        digest.update(hashSalt.getBytes());
        byteArr = digest.digest(baseStr.getBytes());
      }
      BigInteger hash = new BigInteger(1, byteArr);
      result = hash.toString(16);
      while (result.length() < 32) {
        result = "0" + result;
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.newUniqueId Error:\n {1}",
              new Object[]{DataEntry.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }

  /**
   * Generate a new UniqueID with no salt. Exceptions are trapped and logged.
   * @param baseStr a Base string used with a default HashSalt.
   * @return the unique ID or null is an error occurred.
   */
  public static String newUniqueId(String baseStr) {
    return DataEntry.newUniqueId(baseStr, null);
  }


  /**
   * Generate a new UniqueID for a class, which is always the same.
   * It uses <tt>baseStr</tt> = clazz.simpleName and <tt>hashSalt</tt> =
   * clazz.package.name
   * @param clazz the class for which to generate an unique ID (always the same).
   * @return DataEntry.newUniqueId(baseStr, hashSalt).
   * @exception NullPointerException if <tt>clazz</tt> = null
   */
  public static String classUniqueId(Class clazz) {
    if (clazz == null) {
      throw new NullPointerException("The Class definition is unassigned.");
    }
    String baseStr = clazz.getSimpleName();
    String hashSalt = clazz.getPackage().getName();
    return DataEntry.newUniqueId(baseStr, hashSalt);
  }
  //</editor-fold>


  /**
   *
   * @param percentile
   * @param d
   * @param d0
   * @return
   */
  public static boolean inRange(double percentile, double d, double d0, boolean inclusive) {
    return d <= percentile && percentile <= d0;
  }
}
