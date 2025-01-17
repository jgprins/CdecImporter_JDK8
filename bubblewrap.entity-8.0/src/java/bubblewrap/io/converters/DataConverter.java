package bubblewrap.io.converters;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import bubblewrap.io.validators.BoolValidator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kprins
 */
public class DataConverter {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(DataConverter.class.getName());

  //</editor-fold>
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
     *
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
     *
     * @param factor
     */
    private RoundCeiling(double factor) {
      this.factor = factor;
    }
    //</editor-fold>
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Value Rounding">
  /**
   * Return a rounded Long value of the input string value. Convert the string value to a
   * double and the Math.round() to get the rounded Long value
   *
   * @param strVal the string value to convert and rounded
   * @return the rounded value as a Long
   */
  public static Long toRoundedValue(String strVal) {
    Long result = null;
    if (strVal != null) {
      strVal = DataEntry.cleanNumericString(strVal);
      Double dblVal = Double.parseDouble(strVal);
      result = (dblVal == null) ? null : Math.round(dblVal);
    }
    return result;
  }

  /**
   * Round a Input Long values
   *
   * @param inVal the input value (can be null)
   * @param round the specified round options
   * @param ceiling the required {@linkplain RoundCeiling}
   * @return the rounded value or null if inVal = null.
   */
  public static Long roundTo(Long inVal, Round round, RoundCeiling ceiling) {
    Long result = null;
    if (inVal != null) {
      result = DataConverter.roundTo(inVal.doubleValue(), round, ceiling);
    }
    return result;
  }

  /**
   * Return a rounded Long value of the input Double value.
   *
   * @param inVal the input value (can be null)
   * @param round the specified round options. (can be null) If {@literal null},
   * then this method uses {@literal  Round.ONE} as a default.
   * @param ceiling the required {@linkplain RoundCeiling}
   * @return the rounded value or null if inVal = null|NaN.
   */
  public static Long roundTo(Double inVal, Round round, RoundCeiling ceiling) {
    Long result = null;
    if ((inVal != null) && (!inVal.isNaN())) {
      round = (round == null) ? Round.ONE : round;
      if (ceilEqualsFloor(inVal, round)) {
        result = inVal.longValue();
      } else {
        try {
          ceiling = (ceiling == null) ? RoundCeiling.NEAREST : ceiling;
          Long rndVal = Math.round((inVal / round.factor) + ceiling.factor);
          Double rndDbl = (rndVal * round.factor);
          result = rndDbl.longValue();
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.roundTo Error:\n {1}",
                  new Object[]{DataConverter.class.getSimpleName(), exp.getMessage()});
        }
      }
    }
    return result;
  }

  /**
   * Checks whether a ceil and floor operation are equivalent after taking into
   * consideration a round factor.
   * For example, if <code> inVal = 16 </code> and <code> round = Round.FIVE </code>, then
   * <code> #ceilEqualsFloor</code>  returns <code> false</code>.  But if
   * <code> inVal = 15</code>, then  <code> #ceilEqualsFloor</code>  returns
   * <code> true</code>.
   *
   * @param inVal the input value (cannot be null)
   * @param round the specified round options
   * @see Round
   * @see #roundTo(Double, Round, RoundCeiling)
   */
  private static boolean ceilEqualsFloor(Double inVal, Round round) {
    double ceil = Math.ceil(inVal/round.factor)*round.factor;
    double floor = Math.floor(inVal/round.factor)*round.factor;
    return ceil == floor;
  }

  /**
   * Return a rounded Long value of the input Double value.
   *
   * @param inVal the input value (can be null)
   * @param decimal the specified decimal (0 = 1, 1 =0.1, 2 =0.01, etc)
   * @param ceiling the required {@linkplain RoundCeiling}
   * @return the rounded value or null if inVal = null|NaN or <tt>decimal</tt> &lt; 0
   */
  public static Double roundTo(Double inVal, int decimal, RoundCeiling ceiling) {
    Double result = null;
    if ((inVal != null) && (!inVal.isNaN()) && (decimal >= 0)) {
      try {
        double factor = Math.pow(10.0d, (1.0d * decimal));
        ceiling = (ceiling == null) ? ceiling.NEAREST : ceiling;
        Long rndVal = Math.round((inVal * factor) + ceiling.factor);
        result = ((1.0d * rndVal) / factor);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.roundTo Error:\n {1}",
                new Object[]{DataConverter.class.getSimpleName(), exp.getMessage()});
      }
    }
    return result;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Static Conversion Methods">
  /**
   * Convert a Number pFrom of Type[F] to Type[T] where type[T] is of class pToClass.
   * Return null if pFrom=null. Throw ConverterException error if conversion is not
   * possible
   *
   * @param <TFom> the Number type of the in value
   * @param <TTo> the Number type of the return value
   * @param pFrom The number to convert from
   * @param pToClass the class of the return value
   * @return the converted value or null if pFrom is null.
   */
  @SuppressWarnings("unchecked")
  public static <TFrom extends Number, TTo extends Number> TTo
          convertTo(TFrom pFrom, Class<TTo> pToClass) {
    TTo pResult = null;
    try {
      if (pFrom != null) {
        if (pToClass == null) {
          throw new Exception("The returned Value Class is undefined.");
        }

        if (pToClass.isInstance(pFrom)) {
          pResult = (TTo) pFrom;
        } else {
          String sValue = null;
          if ((pToClass.equals(Integer.class)) || (pToClass.equals(Byte.class))
                  || (pToClass.equals(Short.class)) || (pToClass.equals(Long.class))) {
            Double dValue = DataConverter.convertTo(pFrom, Double.class);
            Long iValue = Math.round(dValue);
            sValue = iValue.toString();
          } else {
            sValue = DataConverter.toString(pFrom);
          }

          if (sValue != null) {
            pResult = DataConverter.toValue(sValue, pToClass);
          }
        }
      }
    } catch (Exception pExp) {
      throw new IllegalArgumentException("DataConverter.convertTo Error:\n "
              + pExp.getMessage(), pExp);
    }
    return pResult;
  }

  /**
   * Convert the number pFrom to an unformatted string.
   *
   * @param <TFrom> the type of in value (extends Number)
   * @param pFrom the number value to convert to string
   * @return the resulting string
   */
  public static <TFrom extends Number> String toString(TFrom pFrom) {
    String sResult = null;
    try {
      if (pFrom != null) {
        sResult = String.valueOf(pFrom);
      }
    } catch (Exception pExp) {
      throw new IllegalArgumentException("DataConverter.convertTo Error:\n "
              + pExp.getMessage(), pExp);
    }
    return sResult;
  }

  /**
   * Convert the input strung to a boolean value using a BoolValidator.toValue method.
   *
   * @param strVal the input string
   * @return the returned boolean value or null if unassigned
   */
  public static Boolean toBoolean(String strVal) {
    Boolean result = null;
    try {
      strVal = DataEntry.cleanString(strVal);
      if (strVal != null) {
        BoolValidator validator = new BoolValidator();
        result = validator.toValue(strVal);
      }
    } catch (Exception pExp) {
      throw new IllegalArgumentException("DataConverter.toBoolean Error:\n "
              + pExp.getMessage(), pExp);
    }
    return result;
  }

  /**
   * Convert the String Value to specified pToClass (extends Number). Return null if
   * sValue is empty or unassigned. Throw a ConverterException if an error occur or the
   * pToClass is undefined or not supported.
   *
   * @param <TNum> the Number Type to convert to
   * @param strVal The input string value (can be null)
   * @param toClass The Number class of the return value
   * @return the converted Number or null is sValue=null|""
   */
  @SuppressWarnings("unchecked")
  public static final <TNum extends Number> TNum
          toValue(String strVal, Class<TNum> toClass) {
    TNum result = null;
    try {
      strVal = DataEntry.cleanNumericString(strVal);

      if (strVal != null) {
        if (toClass == null) {
          throw new Exception("The returned Value Class is undefined.");
        } else if (toClass.equals(Byte.class)) {
          Long lngVal = DataConverter.toRoundedValue(strVal);
          if (lngVal != null) {
            if ((lngVal > Byte.MAX_VALUE) || (lngVal < Byte.MIN_VALUE)) {
              throw new Exception("Value[" + lngVal.toString() + "] exceeds the valid "
                      + "Byte value range");
            }
            Byte iValue = Byte.parseByte(lngVal.toString());
            result = (TNum) iValue;
          }
        } else if (toClass.equals(Short.class)) {
          Long lngVal = DataConverter.toRoundedValue(strVal);
          if (lngVal != null) {
            if ((lngVal > Short.MAX_VALUE) || (lngVal < Short.MIN_VALUE)) {
              throw new Exception("Value[" + lngVal.toString() + "] exceeds the valid "
                      + "Short value range");
            }
            Short shtVal = Short.parseShort(lngVal.toString());
            result = (TNum) shtVal;
          }
        } else if (toClass.equals(Integer.class)) {
          Long lngVal = DataConverter.toRoundedValue(strVal);
          if (lngVal != null) {
            if ((lngVal > Integer.MAX_VALUE) || (lngVal < Integer.MIN_VALUE)) {
              throw new Exception("Value[" + lngVal.toString() + "] exceeds the valid "
                      + "Integer value range");
            }
            Integer intVal = Integer.parseInt(lngVal.toString());
            result = (TNum) intVal;
          }
        } else if (toClass.equals(Long.class)) {
          Long lngVal = DataConverter.toRoundedValue(strVal);
          result = (TNum) lngVal;
        } else if (toClass.equals(Double.class)) {
          strVal = DataEntry.cleanNumericString(strVal);
          Double dblVal = Double.parseDouble(strVal);
          result = (TNum) dblVal;
        } else if (toClass.equals(Float.class)) {
          strVal = DataEntry.cleanNumericString(strVal);
          Float fltVal = Float.parseFloat(strVal);
          result = (TNum) fltVal;
        } else {
          throw new Exception("The returned Value Class[" + toClass.getSimpleName()
                  + "] is not supported.");
        }
        boolean stop = true;
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("DataConverter.toValue Error:\n "
              + exp.getMessage(), exp);
    }
    return result;
  }

  /**
   * Convert the items from the srcList to the Target Item Class and return a new generic
   * list with item class TItem. TItem must be of type String or a Number.
   *
   * @param <TItem> must be of type String or a Number
   * @param srcList the Source List to convert
   * @param trgItemClass the target list item class (of type TItem).
   * @return the converted list or null if srcList=null.
   * @throws Exception if trgItemClass is unassigned or any other conversion error is
   * trapped.
   */
  @SuppressWarnings("unchecked")
  public static <TItem extends Serializable> List<TItem>
          convertListItem(List<? extends Serializable> srcList,
                  Class<TItem> trgItemClass)
          throws Exception {
    List<TItem> result = (srcList == null) ? null : new ArrayList<TItem>();
    try {
      if (trgItemClass == null) {
        throw new Exception("The Target List Item Class is unassigned.");
      }
      if ((srcList != null) && (!srcList.isEmpty())) {
        Class<? extends Serializable> srcItemClass
                = ReflectionInfo.getGenericListItemClass(srcList);
        if (trgItemClass.equals(srcItemClass)) {
          result = (List<TItem>) srcList;
        } else {
          Class<Number> numClass = (Number.class.isAssignableFrom(trgItemClass))
                  ? (Class<Number>) trgItemClass : null;

          for (Serializable secItem : srcList) {
            String strItem = (secItem == null) ? null
                    : DataEntry.cleanString(secItem.toString());
            if (strItem == null) {
              continue;
            }

            TItem trgItem = null;
            if (numClass != null) {
              trgItem = (TItem) DataConverter.toValue(strItem, numClass);
            } else {
              trgItem = (TItem) strItem;
            }

            if (trgItem != null) {
              result.add(trgItem);
            }
          }
        }
      }
    } catch (Exception exp) {
      throw new Exception("DataConverter.convertListItem Error:\n " + exp.getMessage());
    }
    return result;
  }
  // </editor-fold>
}
