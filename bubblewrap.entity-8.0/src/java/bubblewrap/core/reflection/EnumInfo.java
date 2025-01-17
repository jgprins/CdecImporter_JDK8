package bubblewrap.core.reflection;

import bubblewrap.core.sort.SelectItemComparer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.model.SelectItem;
import javax.naming.NamingException;

/**
 * This is a static class that supports static utility functions for handling enums,
 * where the latter is defined as static integer values of a class (e.g. ActionEnums or
 * ActionTarget). Enums can either be defined as a set discreetly unique values (see
 * ActionTarget) or bitmap value (see ActionEnums).
 * @see ActionTarget
 * @see ActionEnums
 * @author kprins
 */
public class EnumInfo {

  /**
   * Static Logger for Class
   */
  private static final Logger logger = Logger.getLogger(EnumInfo.class.getSimpleName());

  /**
   * Return a List of Field with 'public static int' fields from pClass.
   * @param pClass Class<?>
   * @return List<Field>
   */
  public static List<Field> getEnumFields(Class<?> pClass) {
    List<Field> pList = new ArrayList<Field>();
    try {
      Field[] pAllFields = (pClass != null) ? pClass.getFields() : null;
      if ((pAllFields != null) && (pAllFields.length > 0)) {
        for (Field pFld : pAllFields) {
          int iMod = pFld.getModifiers();
          if ((Modifier.isPublic(iMod)) && (Modifier.isStatic(iMod))
                  && (pFld.getType() == int.class)) {
            pList.add(pFld);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.getEnumFields Error\n{0}", pExp.getMessage());
      pList = new ArrayList<Field>();
    }
    return pList;
  }

  /**
   * Return a list integers representing the set Enum values.
   * @param pClass Class<?>
   * @return List<Integer>
   */
  public static List<Integer> getEnumValues(Class<?> pClass) {
    List<Integer> pValues = new ArrayList<Integer>();
    try {
      List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
      if ((pStatFields != null) && (!pStatFields.isEmpty())) {
        Integer iVal = 0;
        for (Field pFld : pStatFields) {
          try {
            iVal = pFld.getInt(null);
            pValues.add(iVal);
          } catch (Exception pInExp) {
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.getEnumValues Error\n{0}", pExp.getMessage());
      pValues = new ArrayList<Integer>();
    }

    return pValues;
  }

  /**
   * Return a list Strings representing the set Enum names.
   * @param pClass Class<?>
   * @return List<String>
   */
  public static List<String> getEnumNames(Class<?> pClass) {
    List<String> pValues = new ArrayList<String>();
    try {
      List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
      if ((pStatFields != null) && (!pStatFields.isEmpty())) {
        for (Field pFld : pStatFields) {
          try {
            pValues.add(pFld.getName());
          } catch (Exception pInExp) {
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.getEnumNames Error\n{0}", pExp.getMessage());
      pValues = new ArrayList<String>();
    }

    return pValues;
  }

  /**
   * Return a list of SelectItem representing the "Public Static int" values
   * of pClass.  If bAddBlank, add a the first line as "Blank Line" with value=0.
   * Set the Blank Line label=sBlankValue or "-- Select value --" as default.
   * if (pFromEnums) is set, it will return only the field values that match
   * the enums in the array pFromEnums, otherwise all values will be returned.
   * if (pExcEnums) is set, it will exclude any options that match the values
   * in pExcEnums.
   * @param pClass Class<?>
   * @param bAddBlank Boolean
   * @param sBlankValue String
   * @param pFromEnums int[]
   * @param pExcEnums int[]
   * @return List<SelectItem>
   */
  public static List<SelectItem> getAsOptions(Class<?> pClass,
          Boolean bAddBlank, String sBlankValue, 
          int[] pFromEnums, int[] pExcEnums) {
    List<SelectItem> pList = new ArrayList<SelectItem>();
    bAddBlank = ((bAddBlank != null) && (bAddBlank));
    if (bAddBlank) {
      sBlankValue = ((sBlankValue == null) && (sBlankValue.trim().equals("")))
              ? "-- Select Value --" : sBlankValue.trim();
      pList.add(new SelectItem("0", sBlankValue));
    }

    boolean bDoFrom = ((pFromEnums != null) && (pFromEnums.length > 0));
    boolean bDoExc = ((pExcEnums != null) && (pExcEnums.length > 0));
    try {
      List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
      if ((pStatFields != null) && (!pStatFields.isEmpty())) {
        Integer iVal = 0;
        String sName = null;
        for (Field pFld : pStatFields) {
          try {
            iVal = pFld.getInt(null);
            if ((bDoFrom) && (Arrays.binarySearch(pFromEnums, iVal) < 0)) {
              continue;
            }
            if ((bDoExc) && (Arrays.binarySearch(pExcEnums, iVal) >= 0)) {
              continue;
            }
            
            sName = pFld.getName();
            pList.add(new SelectItem(iVal.toString(), sName));
          } catch (Exception pInExp) {
          }
        }
      }

      if (((bAddBlank) && (pList.size() > 2))
              || ((!bAddBlank) && (pList.size() > 1))) {
        SelectItemComparer pComparer = new SelectItemComparer();
        pComparer.setCompareValuesAsBits(true);
        Collections.sort(pList, pComparer);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.getAsOptions Error\n{0}", pExp.getMessage());
      pList = new ArrayList<SelectItem>();
    }

    return pList;
  }

  /**
   * Get the EnumName (the static field name) of the eEnum value.
   * @param enumClass Class
   * @param searchValue int
   * @return String
   */
  public static String getEnumName(Class<?> enumClass, int searchValue) {
    String result = null;
    try {
      List<Field> staticFields = EnumInfo.getEnumFields(enumClass);
      if ((staticFields != null) && (!staticFields.isEmpty())) {
        Integer enumValue = 0;
        for (Field field : staticFields) {
          enumValue = field.getInt(null);
          if (enumValue == searchValue) {
            result = field.getName();
            break;
         }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "EnumInfo.getEnumName Error\n{0}", exp.getMessage());
      result = null;
    }
    return result;
  }

  /**
   * Get the Enum Value for a specified Static Enum Field Name. Throw an
   * exception if the pClass or sEnumName is undefined or the Enum Field
   * could not be located or the Field Type is not an integer.
   * @param pClass Class<?>
   * @param sEnumName String
   * @return int
   * @throws NamingException
   */
  public static int getEnumValue(Class<?> pClass, String sEnumName)
          throws NamingException {
    int eEnum = 0;
    boolean bHasValue = false;
    if (pClass == null) {
      throw new NamingException("The Enum Class is undefined");
    }
    String sClassName = pClass.getSimpleName();

    sEnumName = ((sEnumName == null) || (sEnumName.trim().equals(""))) ? null
            : sEnumName.trim();
    if (sEnumName == null) {
      throw new NamingException("The Enum Value String is undefined");
    }

    List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
    if ((pStatFields != null) && (!pStatFields.isEmpty())) {
      for (Field pFld : pStatFields) {
        if (sEnumName.equalsIgnoreCase(pFld.getName())) {
          try {
            eEnum = pFld.getInt(null);
            bHasValue = true;
            break;
          } catch (Exception pExp) {
            throw new NamingException(pExp.getMessage());
          }
        }
      }
    }

    if (!bHasValue) {
      throw new NamingException("Unable to locate value for " + sClassName
              + "[" + sEnumName + "].");
    }
    return eEnum;
  }

  /**
   * Return the EnumFilter for a bit-wise enum (i.e. an enum value with all
   * options set). Exceptions are Logged as Warnings
   * @param pClass Class
   * @return int
   */
  public static  int getEnumFilter(Class<?> pClass) {
    int eFilter = 0;
    try {
      List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
      if ((pStatFields != null) && (!pStatFields.isEmpty())) {
        Integer eVal = 0;
        for (Field pFld : pStatFields) {
          eVal = pFld.getInt(null);
          if (eVal != 0) {
            eFilter = (eFilter | eVal);
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.getEnumFilter Error:\n{0}", 
              pExp.getMessage());
      eFilter = 0;
    }
    return eFilter;
  }

  /**
   * Return true if eEnum if a matching enum is defined in pClass.
   * Exception are Logged as Warnings
   * @param pClass Class
   * @param eEnum int
   * @return boolean
   */
  public static boolean isValidEnumOption(Class<?> pClass, int eEnum) {
    boolean bIsValid = false;
    try {
      List<Field> pStatFields = EnumInfo.getEnumFields(pClass);
      if ((pStatFields != null) && (!pStatFields.isEmpty())) {
        Integer eVal = 0;
        for (Field pFld : pStatFields) {
          eVal = pFld.getInt(null);
          if (eVal == eEnum) {
            bIsValid = true;
            break;
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.isValidEnumOption Error:\n{0}", 
              pExp.getMessage());
      bIsValid = false;
    }
    return bIsValid;
  }

  /**
   * Return true if eEnum is a valid setting for the enums defined in the bit-
   * enum pClass. Exception are Logged as Warnings. If (eEnum=0) it will return true if
   * isValidEnumOption return true (i.e., if the BitEnum include a 0 option).
   * @param pClass Class
   * @param eEnum int
   * @return boolean
   */
  public static  boolean isValidBitEnum(Class<?> pClass, int eEnum) {
    boolean bIsValid = false;
    try {
      int eFilter = EnumInfo.getEnumFilter(pClass);
      if (eEnum == 0) {
        bIsValid = ((eFilter == 0) || (EnumInfo.isValidEnumOption(pClass, eEnum)));
      } else {
        bIsValid = ((eFilter & eEnum) == eEnum);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "EnumInfo.isValidBitEnum Error:\n{0}", 
              pExp.getMessage());
      bIsValid = false;
    }
    return bIsValid;
  }

  /**
   * Set the Enum flag in eOptions and return the updated eOptions
   * @param eOptions the current value 
   * @param eEnum the 
   * @return int
   */
  public static int setEnum(int eOptions, int eEnum) {
    return (eOptions | eEnum);
  }

  /**
   * Unset (turn off) the Enum flag in eOptions and return the updated eOptions
   * @param eOptions int
   * @param eEnum int
   * @return int
   */
  public static int unsetEnum(int eOptions, int eEnum) {
    int eFlag = (eOptions & eEnum);
    int eResult = 0;
    if (eFlag != 0) {
      eResult = (eOptions ^ eFlag);
    }
    return eResult;
  }

  /**
   * Return true if both eOptions and eEnum is zero (not set) or the bits represented
   * by eEnum are set in eOptions.  This intended o be used on bitmap Enums only.
   * @param eOptions int
   * @param eEnum int
   * @return boolean.
   */
  public static boolean isSet(int eOptions, int eEnum) {
    return (((eOptions == 0) && (eEnum == 0)) ||
            ((eEnum != 0) && ((eOptions & eEnum) == eEnum)));
  }
}
