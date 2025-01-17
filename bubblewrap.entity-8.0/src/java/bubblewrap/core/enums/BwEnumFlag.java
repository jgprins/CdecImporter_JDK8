package bubblewrap.core.enums;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An abstract class for defining a set of enum value with the constant of type
 * {@linkplain IntFlag} - a 32-bit bitmap value.
 * @author kprins
 */
public abstract class BwEnumFlag<TEnum extends BwEnumFlag<TEnum>> 
                  implements Serializable, Comparable<TEnum>{
  
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Static Method to convert a string to an BwEnum of class enumClass
   * @param <T>
   * @param enumClass the EnumClass
   * @param name the Enum Value's name
   * @return the enum value
   * @exception IllegalArgumentException if the class has no enum values or the
   * enum[name] is not supported
   */
  public static <T extends BwEnumFlag<T>> T valueOf(Class<? extends T> enumClass,
          String name) {
    T result = null;
    name = DataEntry.cleanString(name);
    if (name == null) {
      throw new NullPointerException("Name is unassigned.");
    }
    List<T> enumValues = BwEnumFlag.getEnumValues(enumClass);
    if ((enumValues == null) || (enumValues.isEmpty())) {
      throw new IllegalArgumentException("BwEnum.valueOf Error: \n"
              +  "Enum Class[" + enumClass.getSimpleName()
              + " has no declared Enum values");
    }
    for (T enumVal : enumValues) {
      if (DataEntry.isEq(name, enumVal.name, true)) {
        result = enumVal;
        break;
      }
    }
    if (result == null) {
      throw new IllegalArgumentException("BwEnum.valueOf Error: \n"
              +  "Enum Class[" + enumClass.getSimpleName()
              + " does not support Name[" + name + "].");
    }
    return result;
  }
  
  /**
   * Static Method to convert a string to an BwEnum of class enumClass
   * @param <T>
   * @param enumClass the EnumClass
   * @param enumValue the Enum's value
   * @return the enum value
   * @exception IllegalArgumentException if the class has no enum values or the
   * enum[name] is not supported
   */
  public static <T extends BwEnumFlag<T>> T valueOf(Class<? extends T> enumClass,
          IntFlag enumValue) {
    T result = null;
    List<T> enumValues = BwEnumFlag.getEnumValues(enumClass);
    if ((enumValues == null) || (enumValues.isEmpty())) {
      throw new IllegalArgumentException("BwEnum.valueOf Error: \n"
              +  "Enum Class[" + enumClass.getSimpleName()
              + " has no declared Enum values");
    }
    
    for (T enumInst : enumValues) {
      if (enumInst.value.equals(enumValue)) {
        result = enumInst;
        break;
      }
    }
    
    if (result == null) {
      throw new IllegalArgumentException("BwEnum.valueOf Error: \n"
              +  "Enum Class[" + enumClass.getSimpleName()
              + " does not support value[" + enumValue + "].");
    }
    return result;
  }
  
  /**
   * Get the specified BwEnum class' assigned public enum values (i.e., public static 
   * fields that return instances of enumClass
   * @param <T> extends BwEnum
   * @param enumClass the WEnum Class
   * @return a list if enum values.
   */
  @SuppressWarnings("unchecked")
  public static <T extends BwEnumFlag<T>> List<T> 
                                           getEnumValues(Class<? extends T> enumClass) {
    List<T> result = new ArrayList();
    if (enumClass == null) {
      throw new NullPointerException("The Enum Class is uanssigned.");
    }
    
    try {
      Class<BwEnumFlag<T>> baseClass = 
                            ReflectionInfo.castAsSpecificGenericClass(BwEnumFlag.class);
      Field[] publicFields = enumClass.getFields();
      if ((publicFields != null) && (publicFields.length > 0)) {
        for (Field field : publicFields) {
          if ((baseClass.isAssignableFrom(field.getType())) &&
                  (Modifier.isStatic(field.getModifiers()))) {
            Object enumObj = field.get(null);
            if (enumObj != null) {
              T enumVal = (T) enumObj;
              if (Modifier.isPublic(enumVal.getClass().getModifiers())) {
                result.add(enumVal);
              }
            }
          }
        }
      }
      Collections.sort(result);
    } catch (Exception pExp) {
      throw new IllegalArgumentException("BwEnum.getEnumValues Error:\n "
              + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Enum value of type <tt>enumClass</tt> as set in the specified
   * <tt>bitMap</tt>. It calls {@linkplain #getEnumValues(java.lang.Class) 
   * getEnumValues(enumClass)} to retrieve all possible values and the use the
   * {@linkplain IntFlag#isMatch(bubblewrap.core.enums.IntFlag)} method to filter the
   * matching Enum Values.
   * @param <T> extends BwEnum
   * @param enumClass the WEnum Class
   * @param bitMap the bitmap value that represent the set of BwEnumFlag values
   * @return a list if enum values - an empty list is returned if no matches are found.
   */
  @SuppressWarnings("unchecked")
  public static <T extends BwEnumFlag<T>> List<T> 
                    getMappedEnumValues(Class<? extends T> enumClass, IntFlag bitMap) {
    List<T> result = new ArrayList();
    if (enumClass == null) {
      throw new NullPointerException("The Enum Class is uanssigned.");
    }
    
    try {
      List<T> allValues = BwEnumFlag.getEnumValues(enumClass);
      if ((allValues == null) || (allValues.isEmpty())) {
        throw new Exception("Class[" + enumClass.getClass().getSimpleName() 
                + "] is invalid. It does not have any assigned Enum values.");
      }
      
      for (T enumVal : allValues) {
        if ((enumVal.value.isMatch(bitMap))) {
          result.add(enumVal);
        }
      }
    } catch (Exception pExp) {
      throw new IllegalArgumentException("BwEnum.getMappedEnumValues Error:\n "
              + pExp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The BwEnumFlag's Name
   */
  public final String name;
  /**
   * The BwEnumFlag's value - type {@linkplain IntFlag}
   */
  public final IntFlag value;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected BwEnumFlag(String name, IntFlag value) {
    Class<? extends TEnum> thisClass = 
            ReflectionInfo.castAsSpecificGenericClass(this.getClass());
    List<TEnum> allValues = BwEnumFlag.getEnumValues(thisClass);
    if (!allValues.isEmpty()) {
      for (TEnum enumValue : allValues) {
        if (enumValue.equals(value)) {
          throw new IllegalArgumentException("Duplicate Enum.Value[" + value + "].");
        }
        if (DataEntry.isEq(name, enumValue.name, true)) {
          throw new IllegalArgumentException("Duplicate Enum[" + name + "].");
        }
      }
    }
    
    this.name = DataEntry.cleanString(name);
    this.value = value;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get this class' Declaring Class
   * @return the declaring class.
   */
  @SuppressWarnings("unchecked")
  public final Class<TEnum> getDeclaringClass() {
    Class clazz = getClass();
    Class zuper = clazz.getSuperclass();
    return (zuper == BwEnumFlag.class) ? clazz : zuper;
  }
  
  /**
   * Check if this Bit Flag value is set in the bitmap value.
   * @param bitmap the value to test
   * @return true if (bitmap=this.value=0) || ((bitmap & this.value)=this.value)
   */
  public boolean isSet(IntFlag bitmap) {
    return (((IntFlag.ZERO.equals(bitmap)) && (IntFlag.ZERO.equals(this.value))) ||
            (this.value.equals(bitmap.and(this.value))));
  }
  
  /**
   * Check if this Bit Flag value is set in the <tt>intVal</tt> value.
   * @param intVal the integer representing the value's bitmap settings
   * @return {@linkplain #isSet(bubblewrap.core.enums.IntFlag) 
   * this.isSet(IntFlag.valueOf(intVal))}.
   */
  public boolean isSet(int intVal) {
    return this.isSet(IntFlag.valueOf(intVal));
  }
  
  /**
   * Check if this Enum's flag value includes the <tt>other</tt> enum's flag value
   * @param other the enum to test for
   * @return true if neither this or other is NONE, and 
   * (this.flagValue & other.flagValue) == other.flagValue.
   */
  public boolean include(TEnum other) {
    boolean result = false;
    if ((other != null) && (!IntFlag.ZERO.equals(this.value)) && 
            (!IntFlag.ZERO.equals(other.value))) {
      result = other.value.equals(this.value.and(other.value));
    }
    return result;
  }
  
  /**
   * Check if this Enum's flag value excludes the other enum's flag value
   * @param other the enum to test for
   * @return true if either this or other is NONE, or other=null or 
   * (this.flagValue & other.flagValue) == 0.
   */
  public boolean exclude(TEnum other) {
    boolean result = (other == null);
    if (!result)  {
      if (IntFlag.ZERO.equals(other.value)) {
        result = (!IntFlag.ZERO.equals(this.value));
      } else {
        result = (this.value.and(other.value) != other.value);
      }
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements Comparable">
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Compare this.value with other.value - after converting it to a 
   * representative Double value</p>
   */
  @Override
  public int compareTo(TEnum other) {
    int result = 0;
    if (other == null) {
      result = -1;
    } else {
      result = this.value.compareTo(other.value);
    }
    return result;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return this.name</p>
   */
  @Override
  public String toString() {
    return this.name;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return (this.class = other.class) and (this.value = other.value)</p>
   */
  @Override
  public boolean equals(Object other) {
    boolean result = ((other != null) && (this.getClass().equals(other.getClass())));
    if ((result) && (!(result = (this == other)))) {
      BwEnumFlag otherFlag = (BwEnumFlag) other;
      result = ((this.value != null) && (this.value.equals(otherFlag.value)));
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return this.value.hashCode</p>
   */
  @Override
  public int hashCode() {
    return this.value.hashCode();
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException(this.getClass().getName()
            + " is not cloneable.");
  }
  //</editor-fold>  
}
