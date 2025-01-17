/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.core.enums;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.core.selectors.BwEnumSelector;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The abstract class defining a BwEnum
 * @author kprins
 */
public abstract class BwEnum<TEnum extends BwEnum<TEnum>> implements Serializable, 
        Comparable<TEnum> {
  
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
  public static <T extends BwEnum<T>> T valueOf(Class<? extends T> enumClass,
          String name) {
    T result = null;
    name = DataEntry.cleanString(name);
    if (name == null) {
      throw new NullPointerException("Name is unassigned.");
    }
    List<T> enumValues = BwEnum.getEnumValues(enumClass);
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
              + "] does not support Name[" + name + "].");
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
  public static <T extends BwEnum<T>> T valueOf(Class<? extends T> enumClass,
          int enumValue) {
    T result = null;
    List<T> enumValues = BwEnum.getEnumValues(enumClass);
    if ((enumValues == null) || (enumValues.isEmpty())) {
      throw new IllegalArgumentException("BwEnum.valueOf Error: \n"
              +  "Enum Class[" + enumClass.getSimpleName()
              + " has no declared Enum values");
    }
    for (T enumInst : enumValues) {
      if (enumInst.value == enumValue) {
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
   * Get the specified BwEnum class' assigned enum values (i.e., public static fields
   * that return instances of enumClass
   * @param <T> extends BwEnum
   * @param enumClass the WEnum Class
   * @return a list if enum values.
   */
  @SuppressWarnings("unchecked")
  public static <T extends BwEnum<T>> List<T> 
                                         getEnumValues(Class<? extends T> enumClass) {
    List<T> result = new ArrayList();
    if (enumClass == null) {
      throw new NullPointerException("The Enum Class is uanssigned.");
    }
    
    try{
      Class<BwEnum<T>> baseClass = 
                            ReflectionInfo.castAsSpecificGenericClass(BwEnum.class);
      Field[] publicFields = enumClass.getFields();
      if ((publicFields != null) && (publicFields.length > 0)) {
        for (Field field : publicFields) {
          if ((baseClass.isAssignableFrom(field.getType())) &&
                  (Modifier.isStatic(field.getModifiers()))) {
            Object enumObj = field.get(null);
            if (enumObj != null) {
              T enumVal = (T) enumObj;
              result.add(enumVal);
            }
          }
        }
      }
      if (result.size() > 1) {
        Collections.sort(result);
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException("BwEnum.getEnumValues Error:\n "
              + exp.getMessage());
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public final Fields">
  /**
   * The BwEnum's Name
   */
  public final String name;
  /**
   * The BwEnum's value
   */
  public final int value;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  protected BwEnum(String name, int value) {
    Class<? extends TEnum> thisClass = 
            ReflectionInfo.castAsSpecificGenericClass(this.getClass());
    List<TEnum> allValues = BwEnum.getEnumValues(thisClass);
    if (!allValues.isEmpty()) {
      for (TEnum enumValue : allValues) {
        if (enumValue.value == value) {
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
    return (zuper == BwEnum.class)? clazz : zuper;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implements Comparable">
  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Compare this.value with other.value</p>
   */
  @Override
  public int compareTo(TEnum other) {
    int result = 0;
    if (other == null) {
      result = -1;
    } else {
      result = Integer.compare(this.value, other.value);
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
      BwEnum otherFlag = (BwEnum) other;
      result = (this.value == otherFlag.value);
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode based on its value</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + this.value;
    return hash;
  }
  
  @Override
  protected Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException(this.getClass().getName()
            + " is not cloneable.");
  }
  //</editor-fold>
}
