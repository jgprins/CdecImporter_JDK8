package bubblewrap.core.reflection;

import bubblewrap.core.annotations.LookupMethod;
import bubblewrap.entity.context.EntityPath;
import bubblewrap.entity.context.FieldInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.persistence.Entity;

/**
 * ReflectionInfo is a utility class for extracting reflection information class 
 * definitions.
 * @author kprins
 * @version 1.00.002 (09/12/2016) modified getGetMethod and getSetMethod search all  
 * accessible methods - return methods can be public, protected, private, and/or static methods.
 */
public class ReflectionInfo {

  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * protected Static Logger object for logging errors, warnings, and info messages.
   */
  private static final Logger logger = Logger.getLogger("ReflectionInfo");
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Generic Class Access Methods">
  /**
   * <p>Call to cast a Generic Class to a Class&lt;? extend T&gt;. It is not possible to
   * assign a generic class to class variable. This will not work:</p>
   * {@code 
   *    Class<? extends List<T>> pClass = MyList<T>.class;
   * }
   * <p>Use this method to a cast pClass as a generic class as follows:</p>
   * {@code 
   *    Class<? extends List<T>> pClass = ReflectionInfo.castAsGenericClass(MyList.class);
   * }
   * @param <TBase> the base class in the generic reference
   * @param baseClass the class to cast (without it generic reference)
   * @return pClass cast as Class&lt;? extends TBase&gt;
   */
  @SuppressWarnings("unchecked")
  public static <TBase> Class<? extends TBase> castAsGenericClass(Class baseClass) {
    return (Class<? extends TBase>) ((Class) baseClass);
  }
  
  /**
   * <p>Call to cast a Generic Class to a Class&lt;TBase&gt;. It is not possible to
   * assign a generic class to class variable. This will not work:</p>
   * {@code 
   *    Class<List<T>> pClass = MyList<T>.class;
   * }
   * <p>Use this method to a cast pClass as a generic class as follows:</p>
   * {@code 
   *    Class<List<T>> pClass = ReflectionInfo.castAsSpecificGenericClass(MyList.class);
   * }
   * @param <TBase> the specific base class in the generic reference
   * @param baseClass the base class to cast (without it generic reference)
   * @return pClass cast as Class&lt;TBase&gt;
   */
  @SuppressWarnings("unchecked")
  public static <TBase> Class<TBase> castAsSpecificGenericClass(Class baseClass) {
    return (Class<TBase>) ((Class) baseClass);
  }
    
  /**
   * Return the Generic Argument class for a Class with one or more generic arguments.
   * @param baseClass the base (generic) class
   * @param childClass the child class for which to get the generic information
   * @param argIdx the index of the generic parameter
   * @return the child class' generic class assignment for Type Argument[ardIdx]
   * @exception Exception - errors are trapped and logged.
   */
  @SuppressWarnings("unchecked")
  public static <T> Class<?> getGenericClass(Class<T> baseClass,
          Class<? extends T> childClass, int argIdx) {
    Class result = null;
    try {
      if (baseClass == null) {
        throw new Exception("The Base Class is undefined");
      }
      
      if (childClass == null) {
        throw new Exception("The Parent Class is undefined");
      }
      
      List<Class<?>> typeArgs = ReflectionInfo.getTypeArguments(baseClass, childClass);
      if ((typeArgs != null) && (argIdx < typeArgs.size())) {
        result = typeArgs.get(argIdx);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "ReflectionInfo.getGenericClass Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
    
  /**
   * Overload 1. Get the generic casting of the List's Item. It call {@linkplain 
   * #getGenericListItemClass(java.lang.Class) Overlaod 2} and if the class cannot be
   * retrieve it checks the list content and returns the class of the first non-null 
   * list item.
   * @param list the list whose class is to be explore
   * @return the list item class or null if listClass is a generic class and the list 
   * is empty or the list reference is null.
   * @exception Exception - errors are trapped and logged.
   */
  @SuppressWarnings("unchecked")
  public static <TItem> Class<TItem> 
                      getGenericListItemClass(List<TItem> list) {
    Class<TItem> result = null;
    try {
      if (list != null) {
        Class<List<TItem>> listClass = (Class<List<TItem>>) list.getClass();
        result = ReflectionInfo.getGenericListItemClass(listClass);
        if ((result == null) && (!list.isEmpty())) {
          for (TItem item : list) {
            if (item != null) {
              result = (Class<TItem>) item.getClass();
              break;
            }
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "ReflectionInfo.getGenericListItemClass Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
    
  /**
   * Overload 2. Get the generic casting of the List's Item.
   * @param listClass the list class to explore
   * @return the list item class or null if listClass is a generic class
   * @exception Exception - errors are trapped and logged.
   */
  @SuppressWarnings("unchecked")
  public static <TItem> Class<TItem> 
                      getGenericListItemClass(Class<? extends List<TItem>> listClass) {
    Class<TItem> result = null;
    try {
      if (listClass == null) {
        throw new Exception("The List Class is undefined");
      }
      Class baseClass = List.class;
      
      List<Class<?>> typeArgs = ReflectionInfo.getTypeArguments(baseClass, listClass);
      if ((typeArgs != null) && (!typeArgs.isEmpty())) {
        result = (Class<TItem>) typeArgs.get(0);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "ReflectionInfo.getGenericListItemClass Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
  
  //<editor-fold defaultstate="collapsed" desc="Static Class MapEntryClasses ">
  /**
   * A Static Class for returning the Map entry's key-value classes
   */
  public static class MapEntryClasses<TKey,TValue> {
    /**
     * Placeholder for the Map's Key class
     */
    public Class<TKey >keyClass;
    /**
     * Placeholder for the Map's Value class
     */
    public Class<TValue> valueClass;
    
    /**
     * Check if both classes are defined.
     * @return true id the key and value class are assigned.
     */
    public boolean isDefined() {
      return ((this.keyClass != null) && (this.valueClass != null));
    }
  }
  //</editor-fold>
  
  /**
   * Get the generic casting of the Map's Key.
   * @param map the Map whose class is to be explored
   * @return the {@linkplain MapEntryClasses} with the extracted key and value classes
   * or null if Map class is a generic class
   * @exception Exception - errors are trapped and logged.
   */
  @SuppressWarnings("unchecked")
  public static <TKey, TValue> MapEntryClasses<TKey,TValue> 
                                          getGenericMapClasses(Map<TKey,TValue> map) {
    MapEntryClasses<TKey,TValue> result = new MapEntryClasses<>();
    try {
      if (map == null) {
        throw new Exception("The Map is undefined");
      }
      
      Class mapClass = map.getClass();
      MapEntryClasses<TKey,TValue> classes
                                      = ReflectionInfo.getGenericMapClasses(mapClass);
      if ((classes == null) || (!classes.isDefined())) { 
        if (!map.isEmpty()) {
          for (Map.Entry<TKey, TValue> entry : map.entrySet()) {
            if ((entry != null) 
                            && (entry.getKey() != null) && (entry.getValue() != null)) {
              result.keyClass = (Class<TKey>) entry.getKey().getClass();
              result.valueClass = (Class<TValue>) entry.getValue().getClass();
              break;
            }
          }
        }
      } else {
        result = classes;
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "ReflectionInfo.getGenericMapClasses Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  } 
  
  /**
   * Get the generic casting of the Map's Key.
   * @param mapClass the Map class to explore
   * @return the Map key class or null if Map class is a generic class
   * @exception Exception - errors are trapped and logged.
   */
  @SuppressWarnings("unchecked")
  public static <TKey, TValue> MapEntryClasses<TKey,TValue> 
                    getGenericMapClasses(Class<? extends Map<TKey,TValue>> mapClass) {
    MapEntryClasses<TKey,TValue> result = new MapEntryClasses<>();
    try {
      if (mapClass == null) {
        throw new Exception("The List Class is undefined");
      }
      Class baseClass = Map.class;
      
      List<Class<?>> typeArgs = ReflectionInfo.getTypeArguments(baseClass, mapClass);
      if ((typeArgs != null) && (typeArgs.size() >= 2)) {
        result.keyClass = (Class<TKey>) typeArgs.get(0);
        result.valueClass = (Class<TValue>) typeArgs.get(1);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "ReflectionInfo.getGenericMapClasses Error:\n {0}",
              pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the underlying class for a type, or null if the type is a variable type.
   * @param classType the type to explore
   * @return the class the underlying class or null if the class cannot be resolved.
   */
  public static Class<?> getTypeClass(Type classType) {
    if (classType instanceof Class) {
      return (Class) classType;
    } else if (classType instanceof ParameterizedType) {
      return ReflectionInfo.getTypeClass(((ParameterizedType) classType).getRawType());
    } else if (classType instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) classType).getGenericComponentType();
      Class<?> pCompClass = ReflectionInfo.getTypeClass(componentType);
      if (pCompClass != null ) {
        return Array.newInstance(pCompClass, 0).getClass();
      } else {
        return null;
      }
    } else {
      return null;
    }
  }
  
  /**
   * Get the actual type arguments a child class has used to extend a generic base class.
   * @param baseClass the base class
   * @param childClass the child class
   * @return a list of the raw classes for the actual type arguments.
   */
  public static <T> List<Class<?>> getTypeArguments(Class<T> baseClass,
          Class<? extends T> childClass) {
    Map<Type, Type> resolvedTypes = new HashMap<>();
    Type classType = childClass;
    // start walking up the inheritance hierarchy until we hit baseClass
    if ((!childClass.equals(baseClass)) && (baseClass.isAssignableFrom(childClass))) {
      while (true) {
        if (classType instanceof Class) {
          // there is no useful information for us in raw types, so just keep going.
          Type nextType = ((Class) classType).getGenericSuperclass();
          if ((nextType == null) 
                  || (ReflectionInfo.getTypeClass(nextType).equals(Object.class))) {
            break;
          }
          classType = nextType;
        } else {
          ParameterizedType paramType = (ParameterizedType) classType;
          Class<?> rawClass = (Class) paramType.getRawType();

          Type[] actualArgs = paramType.getActualTypeArguments();
          TypeVariable<?>[] typeParams = rawClass.getTypeParameters();
          for (int i = 0; i < actualArgs.length; i++) {
            resolvedTypes.put(typeParams[i], actualArgs[i]);
          }

          if (ReflectionInfo.getTypeClass(classType).equals(baseClass)) {
            break;
          }

          if (!rawClass.equals(baseClass)) {
            Type nextType = rawClass.getGenericSuperclass();
            if ((nextType == null) 
                    || (ReflectionInfo.getTypeClass(nextType).equals(Object.class))) {
              break;
            }
            classType = nextType;
          } else {
            break;
          }
        }
      }
    }
    
    // finally, for each actual type argument provided to baseClass, determine (if possible)
    // the raw class for that type argument.
    List<Class<?>> typeArgsAsClasses = new ArrayList<>();
    if (!resolvedTypes.isEmpty()) {
      Type[] actualTypeArgs;
      if (classType instanceof Class) {
        actualTypeArgs = ((Class) classType).getTypeParameters();
      } else {
        actualTypeArgs = ((ParameterizedType) classType).getActualTypeArguments();
      }

      // resolve types by chasing down type variables.
      for (Type actualType: actualTypeArgs) {
        while (resolvedTypes.containsKey(actualType)) {
          actualType = resolvedTypes.get(actualType);
        }
        Class actualClass = ReflectionInfo.getTypeClass(actualType);
        typeArgsAsClasses.add(actualClass);
      }
    }
    return typeArgsAsClasses;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Entity Class Reflection Methods">
  /**
   * Check if this bean is a persistent entity bean.
   * @param beanClass
   * @return true if its supports the {@linkplain Entity} annotation.
   */
  public static boolean isEntity(Class<? extends Serializable> beanClass) {
    boolean result = (beanClass != null);
    if (result) {
      result = (beanClass.getAnnotation(Entity.class) != null);
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Entity Field Reflection Methods">
  /**
   * <p>Locate the Bean Class' Field[fieldName] and extract the base field information.
   * It first looks for a declared field with a matching name (case insensitive). If no
   * field was located, its get the field's Get-Method. It retrieves the field's return
   * type and assigned Annotation from either the field or the Get-Method. It also
   * retrieve the field's Set-Method - if defined.</p>
   *  <p>This process fails if the beanClasss or fieldName is undefined, or the field or
   * its Get-Method cannot located.</p>
   * @param beanClass the bean class that contains the field
   * @param fieldName the name of the field to search for.
   * @return the field's FieldInfo
   * @throws NoSuchFieldException if the field or Get-Method cannot be located
   * @throws NoSuchMethodException if the Get-Method cannot be located.
   */
  public static FieldInfo getFieldInfo(Class<? extends Serializable> beanClass,
          String fieldName) throws NoSuchFieldException, NoSuchMethodException {
    FieldInfo result = null;
    
    if (beanClass == null) {
      throw new NullPointerException("The Bean Class cannot be unassigned.");
    }
    
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new NullPointerException("The Field Name cannot by unassigned.");
    }
    
    boolean hasField = false;
    Class returnType = null;
    Method getMethod = null;
    Method setMethod = null;
    Field mappedBy = null;
    Annotation[] annotArr = null;
    for (Field field : beanClass.getDeclaredFields()) {
      if (DataEntry.isEq(fieldName, field.getName(), true)) {
        mappedBy = field;
        hasField = true;
        returnType = field.getType();
      }
    }
    
    if ((getMethod = ReflectionInfo.getGetMethod(beanClass, fieldName)) == null) {
      throw new NoSuchFieldException("Unable to locate Get-Method for Field[" 
              + fieldName + "] in Bean Class[" + beanClass.getClass().getName() + "].");
    } else if ((!Modifier.isPublic(getMethod.getModifiers())) ||
               (Modifier.isStatic(getMethod.getModifiers()))) {
      throw new NoSuchFieldException("The Get-Method for Field[" 
              + fieldName + "] in Bean Class[" + beanClass.getClass().getName() 
              + "] is not a public field or it is a static field.");
    } else if (!hasField) {
      hasField = true;
      returnType = getMethod.getReturnType();
    }
    
    setMethod = ReflectionInfo.getSetMethod(beanClass, fieldName);
    if ((setMethod != null) && (!Modifier.isPublic(setMethod.getModifiers())) ||
               (Modifier.isStatic(setMethod.getModifiers()))) {
      setMethod = null;
    }
    
    if (!hasField) {
      throw new NoSuchMethodException("Unable to locate Field[" + fieldName + "] in "
              + "Bean Class[" + beanClass.getClass().getName() + "].");
    }
    
    EntityPath entityPath = new EntityPath<>(beanClass, fieldName);
    result = new FieldInfo(entityPath, getMethod, setMethod, mappedBy);
    return result;
  }
  
  /**
   * Private Method called by the getGetMethodName and getSetMethodName methods.
   * @param fieldName the field name
   * @param namePrefix the field name prefix (e.g. "get")
   * @return the method name
   */
  private static String getMethodName(String fieldName, String namePrefix) {
    String result = null;
    if ((fieldName != null) && (!fieldName.trim().equals(""))) {
      fieldName = fieldName.trim();
      String firstChar = fieldName.substring(0, 1);
      String upperChar = firstChar.toUpperCase();
      result = fieldName.replaceFirst(firstChar, upperChar);
      if ((namePrefix != null) && (!namePrefix.trim().equals(""))) {
        result = namePrefix.trim() + result;
      }
    }
    return result;
  }
  
  /**
   * Get the get Method for sField as "get"+(sField with the First Character
   * capitalized). Example: "myField" return "getMyField"
   * @param fieldName
   * @return String
   */
  public static String getGetMethodName(String fieldName) {
    return ReflectionInfo.getMethodName(fieldName, "get");
  }
  
  /**
   * Get the get Method for sField as "set"+(sField with the First Character
   * capitalized). Example: "myField" return "setMyField"
   * @param fieldName
   * @return String
   */
  public static String getSetMethodName(String fieldName) {
    return ReflectionInfo.getMethodName(fieldName, "set");
  }
  
  /**
   * Extract the FieldName from the method name. It assumes that the method name starts
   * with "get", "set", "has", "is", or "do".
   * @param methodName the method name to process
   * @return the field name with first character in lower case. Return null if the
   * fieldName does not follows the above patterns.
   */
  public static String getFieldname(String methodName) {
    String result = null;
    methodName = DataEntry.cleanString(methodName);
    String fieldName = null;
    if ((!methodName.startsWith("_")) && (!methodName.equalsIgnoreCase("hashCode"))) {
      if ((methodName.startsWith("get")) || (methodName.startsWith("set")) ||
          (methodName.startsWith("has"))) {
        if (!methodName.equalsIgnoreCase("hashCode")) {
          fieldName = methodName.substring(3);
        }
      } else if ((methodName.startsWith("is")) || (methodName.startsWith("do"))) {
        fieldName = methodName.substring(2);
      }
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        String firstChar = fieldName.substring(0,1).toLowerCase();
        result = firstChar + fieldName.substring(1);
      }
    }
    return result;
  }
  
  /**
   * Overload 1: Check if a Read-And-Write Field[sField] is supported by pEntClass.
   * Call Overload 2 with bReadOnly=false.
   * @param beanClass a Serializable class
   * @param fieldName the Field's Name
   * @return true if the field's GET- and SET-methods are supported.
   */
  public static boolean hasField(Class<? extends Serializable> beanClass,
          String fieldName) {
    return ReflectionInfo.hasField(beanClass, fieldName, false);
  }
  
  /**
   * Overload 2: Check if sField is a valid field for Bean pClass. If bReadOnly=true,
   * it only check if a get-Method exist. Otherwise, it will check whether both a Get-
   * and Set-method exists. It returns false if either pClass or sField is undefined.
   * @param beanClass a Serializable class
   * @param fieldName the Field's Name
   * @param readOnly true if the field is readOnly| false=A read and write field
   * @return true if the field GET- (and SET-) method(s) exist.
   */
  public static boolean hasField(Class<? extends Serializable> beanClass,
          String fieldName, boolean readOnly) {
    boolean result = false;
    fieldName = DataEntry.cleanString(fieldName);
    if ((beanClass != null) && (fieldName != null)) {
      String getMethodName = ReflectionInfo.getGetMethodName(fieldName);
      result = ReflectionInfo.hasMethod(beanClass, getMethodName);
      if ((result) && (!readOnly)) {
        String setMethodName = ReflectionInfo.getSetMethodName(fieldName);
        result = ReflectionInfo.hasMethod(beanClass, setMethodName);
      }
    }
    return result;
  }
  //</editor-fold>
 
  //<editor-fold defaultstate="collapsed" desc="Entity Method Reflection Methods">
  /**
   * Call to get a Singleton Class' static instance lookup method - as indicated by the
   * {@linkplain LookupMethod} annotation.
   * @param reqClass a singleton class
   * @return the static method or null if the class is not a singleton or the lookup 
   * method is not found or non-static.
   */
  @SuppressWarnings("unchecked")
  public static Method getLookupMethod(Class reqClass) {
    Method result = null;
    if ((reqClass != null) && (reqClass.isAnnotationPresent(Singleton.class))) {
      for (Method method : reqClass.getMethods()) {
        int filter = (Modifier.PUBLIC | Modifier.STATIC);
        if ((method.isAnnotationPresent(LookupMethod.class))
                && ((method.getModifiers() & filter) == filter)
                && (reqClass.equals(method.getReturnType())))   {
          result = method;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Check if Method[sMethod] is supported by the specified class.
   * @param beanClass a Class that extends Serializable
   * @param methodName the method name (case in-sensitive search)
   * @return true if the method is supported
   */
  public static boolean hasMethod(Class<? extends Serializable> beanClass, 
                                                                 String methodName) {
    boolean result = false;
    methodName = DataEntry.cleanString(methodName);
    if ((beanClass != null) && (methodName != null)) {
      for (Method method : beanClass.getMethods()) {
        if (method.getName().equalsIgnoreCase(methodName)) {
          result = true;
          break;
        }
      }
    }
    return result;
  }
  
  /**
   * Get the class' Method based on a method name only.
   * @param beanClass a Class that extends Serializable
   * @param methodName the method name (case in-sensitive search)
   * @return the Entity Class' Method or null if not supported
   */
  public static Method getMethod(Class<? extends Serializable> beanClass, 
                                                    String methodName){
    Method result = null;
    if (beanClass == null) {
      throw new NullPointerException("Class reference is unassigned");
    }

    methodName = DataEntry.cleanString(methodName);
    if (methodName == null) {
      throw new NullPointerException("The field name is unassigned");
    }

    for (Method method : beanClass.getMethods()) {
      if (method.getName().equalsIgnoreCase(methodName)) {
        result = method;
        break;
      }
    }
    return result;
  }
  
  /**
   * @since 1.00.002
   * Get a class' GET-method for a specified field. If search a Get-Method (i.e., 
   * return of !Void and taking no parameters) that ends with the field name and a 
   * has a prefix of "get" or if the return type is boolean it also accept "is" or "has". 
   * The field name search is not case sensitive.
   * <p>
   * <b>NOTE:</b> Check the returned field's Modifiers - the field could be static, 
   * protected, private, etc.</p>
   * @param beanClass a Class that extends Serializable
   * @param fieldName the method name (case in-sensitive search)
   * @return the Class' GET-method or null if not supported.
   * @exception NullPointerException if the class or the field name is not defined.
   */
  public static Method getGetMethod(Class<? extends Serializable> beanClass, 
                                                  String fieldName) {
    Method result = null;
    if (beanClass == null) {
      throw new NullPointerException("Class reference is unassigned");
    }
    
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new NullPointerException("The field name is unassigned");
    }
    fieldName = fieldName.toLowerCase();    
    if ((result = ReflectionInfo.getGetMethod(fieldName, false, beanClass.getMethods())) 
            == null) {
      result = ReflectionInfo.getGetMethod(fieldName, true, 
                                                          beanClass.getDeclaredMethods());
    }
    return result;
  }
  
  /**
   * @since 1.00.002
   * Get the Matching get-Method from the array of <tt>methods</tt> ignoring any public
   * public field if <tt>noPublic</tt> = true.
   * @param fieldName the field name to match
   * @param noPublic true ti ignore public field.
   * @param methods the array of methods to search
   * @return the matching get-Method
   */
  private static Method getGetMethod(String fieldName, boolean noPublic, 
                                                                      Method[] methods) {    
    Method result = null;
    if ((fieldName != null) && (methods != null) && (methods.length > 0)) {
      Class returnType = null;
      Class[] parTypes = null;
      String methodName = null;
      for (Method method : methods) {        
        if (((noPublic) && (Modifier.isPublic(method.getModifiers()))) ||
            ((returnType = method.getReturnType()) == null) || 
            (returnType.equals(void.class)) ||
            (((parTypes = method.getParameterTypes()) != null) && 
                (parTypes.length > 0))) {
          continue;
        }
        methodName = method.getName();
        if (methodName.toLowerCase().endsWith(fieldName)) {
          String prefix = methodName.substring(0, 
                                                methodName.length() - fieldName.length());
          if ((returnType.equals(Boolean.class)) || (returnType.equals(boolean.class))) {
            if (DataEntry.inStringArray(prefix.toLowerCase(), "get", "is", "has")) {
              result = method;
              break;
            }
          } else if ("get".equalsIgnoreCase(prefix)) {
            result = method;
              break;
          }
        }
      }
      
    }
    return result;
  }
  
  /**
   * @since 1.00.002
   * Get a class' SET-method for a specified field. If search a Set-Method (i.e., 
   * return Void) that ends with the field name and a has a prefix of "set". 
   * The field name search is not case sensitive.
   * <p>
   * <b>NOTE:</b> Check the returned field's Modifiers - the field could be static, 
   * protected, private, etc.</p>
   * @param beanClass a Class that extends Serializable
   * @param fieldName the method name (case in-sensitive search)
   * @return the Class' SET-method or null if not supported.
   * @exception NullPointerException if the class or the field name is not defined.
   */
  public static Method getSetMethod(Class<? extends Serializable> beanClass, 
                                                  String fieldName) {
    Method result = null;
    if (beanClass == null) {
      throw new NullPointerException("Class reference is unassigned");
    }
    
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new NullPointerException("The field name is unassigned");
    }
    fieldName = fieldName.toLowerCase();    
    if ((result = ReflectionInfo.getSetMethod(fieldName, false, beanClass.getMethods())) 
            == null) {
      result = ReflectionInfo.getSetMethod(fieldName, true, 
                                                          beanClass.getDeclaredMethods());
    }
    return result;
  }
  
  /**
   * @since 1.00.002
   * Get the Matching set-Method from the array of <tt>methods</tt> ignoring any public
   * public field if <tt>noPublic</tt> = true.
   * @param fieldName the field name to match
   * @param noPublic true ti ignore public field.
   * @param methods the array of methods to search
   * @return the matching set-Method
   */
  private static Method getSetMethod(String fieldName, boolean noPublic, 
                                                                      Method[] methods) {    
    Method result = null;
    if ((fieldName != null) && (methods != null) && (methods.length > 0)) {
      Class returnType = null;
      Class[] parTypes = null;
      String methodName = null;
      for (Method method : methods) {
        if (((noPublic) && (Modifier.isPublic(method.getModifiers()))) ||
            ((returnType = method.getReturnType()) == null) || 
            (!returnType.equals(void.class)) ||
            ((parTypes = method.getParameterTypes()) == null) || 
             (parTypes.length == 0)) {
          continue;
        }
        methodName = method.getName();
        if (methodName.toLowerCase().endsWith(fieldName)) {
          String prefix = methodName.substring(0, methodName.length() - fieldName.length());
          if ("set".equalsIgnoreCase(prefix)) {
            result = method;
            break;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Check whether the Bean Class supports a Field with return type Collection that
   * starts with the name of the Foreign Key Class' simpleName (not case-sensitive) and 
   * return the matching field's name, otherwise return null. Example, if 
   * foreignKeyClass="appTask" the collection field name will be "appTaskCollection".
   * @param beanClass the EntityBean Class
   * @param foreignKeyClass this class.simpleName (e.g. "appTask")
   * @return the name of the Collection Field
   */
  public static String getChildCollectionField(Class<? extends Serializable> beanClass, 
                                                           String foreignKeyClass){
    String result = null;
    try {
      foreignKeyClass = DataEntry.cleanString(foreignKeyClass);
      if ((beanClass != null) && (foreignKeyClass != null)) {
        String namePreffix = foreignKeyClass.toLowerCase();
        for (Field pField : beanClass.getDeclaredFields()) {
          if ((pField.getType() == Collection.class)
              && (pField.getName().toLowerCase().startsWith(namePreffix))) {
            result = pField.getName();
            break;
          }
        }
      }
    } catch (Exception pExp) {
      result = null;
    }
    return result;
  }
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="Entity Get-Set Value Methods">
  /**
   * <p>Get an object's Field[sField]'s value. Throw an exception if the object does not
   * support the field (i.e.c {@linkplain #getGetMethod(java.lang.Class, 
   * java.lang.String) returns null). </p>
   * <p><b>NOTE:</b> The return value of Field with Primitive return types will always
   * be the object version of the Primitive type (e.g. type 'int' is returned as an
   * Integer object.</p>
   * @param <TBean> extends Serializable
   * @param <TValue> any class
   * @param entityBean a Serializable instance of the entity bean
   * @param fieldName the field name (a GET-Method for this field must be supported)
   * @return the Method result or null if unassigned. The return type will be cast to
   * the generic reference TObject.
   * @throws Exception if any of the input is unassigned or if the method is not
   * supported or retrieving or casting the returned value failed.
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TValue> TValue
                   getFieldValue(TBean entityBean, String fieldName) throws Exception {
    TValue result = null;        
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new Exception("The Field name is undefined");
    }
    
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    if (entityClass == null) {
      throw new Exception("Entity Class is not accessable");
    }
    
    Method pMethod = ReflectionInfo.getGetMethod(entityClass,fieldName);
    if (pMethod == null) {
      throw new Exception("The GET-method for '" + entityClass.getSimpleName()
              + "." + fieldName + "' is not supported");
    }
    
    Object[] pArg = null;
    Object pValue = pMethod.invoke(entityBean, pArg);
    result = (pValue == null)? null: (TValue) pValue;
    return result;
  }
  
  /**
   * Retrieve the FieldValue using the GET-Method name specified by the EntityFieldInfo.
   * @param <TBean> extends Serializable
   * @param <TValue> the field value type
   * @param entityBean the entityBean
   * @param fieldInfo the field EntityFieldInfo
   * @return the assigned value (can be null)
   * @throws Exception is the EntityFieldInfo or Entity is unassigned
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TValue> TValue
                   getFieldValue(TBean entityBean, FieldInfo fieldInfo) 
                  throws Exception { 
    TValue result = null;
    if (fieldInfo == null) {
      throw new Exception("The EntityFieldInfo is undefined");
    }
    String fieldName = fieldInfo.entityPath.fieldName;
    
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    if (entityClass == null) {
      throw new Exception("Entity Class is not accessable");
    }
    
    Method method = fieldInfo.getMethod;
    if (method == null) {
      throw new Exception("The GET-method for field " 
              + entityClass.getSimpleName() + "." + fieldName + " is not supported");
    }
    
    Object[] pArg = null;
    Object pValue = method.invoke(entityBean, pArg);
    result = (pValue == null)? null: (TValue) pValue;
    return result;
  }
  
  /**
   * Set pEntity.Field[sField]'s value. Throw an exception if the field does not
   * support a SET-method or the field does not exist in the entity or pEnity is not an
   * instance of pClass.
   * @param pClass a Serializable Class
   * @param entityBean a Serializable instance of pClass
   * @param fieldName the field name (a GET-Method for this field must be supported)
   * @param value the field's new value (can be null is null is allowed).
   * @throws Exception if the field does not support a SET-method or the field does not
   * exist in the entity or pEntity is not an instance of pClass or assigning the value
   * to the Entity's field failed.
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TValue> void 
     setFieldValue(TBean entityBean, String fieldName, TValue value) throws Exception {
    if (entityBean == null) {
      throw new Exception("The Entity Instance if undefined");
    }
    
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new Exception("The Field name is undefined");
    }
    
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    if (entityClass == null) {
      throw new Exception("Entity Class is not accessable");
    }
    
    Method setMethod = ReflectionInfo.getSetMethod(entityClass,fieldName);
    if (setMethod == null) {
      throw new Exception("The SET-method for '" + entityClass.getSimpleName()
              + "." + fieldName + "' is not supported");
    }
    
    try {
      Object[] args = {value};
      Object result = setMethod.invoke(entityBean, args);
    } catch (IllegalAccessException | IllegalArgumentException 
            | InvocationTargetException exp) {
      throw new Exception("Setting Field '" + entityClass.getSimpleName()
              + "." + fieldName + "' failed because:" + exp.getMessage());
    }
  }
  
  /**
   * Get entityBean's child/foreignKey collection. Throw an exception if the field does 
   * not have a Get method for the collection field (i.e., as defined by {@linkplain 
   * #getChildCollectionField(java.lang.Class, java.lang.String) 
   * getChildCollectionField}.
   * @param <TBean> parent class extends Serializable
   * @param <TChild> child class extends Serializable
   * @param entityBean the EntityBean
   * @param childClass this child class.
   * @return a collection of TChild
   * @throws Exception if the input is unassigned or the field or GET-Method is not 
   * supported.
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TChild extends Serializable> 
          Collection<TChild> getChildCollection(TBean entityBean, 
          Class<TChild> childClass) throws Exception {
    Collection<TChild> result = null;
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    if (childClass == null) {
      throw new Exception("The Child/ForeignKey Class reference is unassigned.");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    String childClassName = childClass.getSimpleName();

    String fieldName = 
                  ReflectionInfo.getChildCollectionField(entityClass, childClassName);
    if (fieldName == null) {
      throw new Exception("Entity[" + entityClass.getSimpleName()
              + "] does not support a collection Field[" 
              + childClassName + "Collection]");
    }

    Class<?>[] params = null;
    Method method = ReflectionInfo.getGetMethod(entityClass, fieldName);
    if (method == null) {
      throw new Exception("Method '" + entityClass.getSimpleName()
              + "." + fieldName + "' is not supported");
    }

    Object[] args = null;
    result = (Collection<TChild>) method.invoke(entityBean, args);
    return result;
  }

  /**
   * Set entityBean's child/foreignKey collection. Throw an exception if the field does 
   * not have a Get method for the collection field (i.e., as defined by {@linkplain 
   * #getChildCollectionField(java.lang.Class, java.lang.String) 
   * getChildCollectionField}.
   * @param <TBean> parent class extends Serializable
   * @param <TChild> child class extends Serializable
   * @param entityBean the parent instance
   * @param childClass the child class
   * @param childCol the Collection to assign to the parent instance
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static <TBean extends Serializable, TChild extends Serializable> void 
          setChildCollection(TBean entityBean, Class<TChild> childClass, 
          Collection<TChild> childCol) throws Exception {
    if (entityBean == null) {
      throw new Exception("The Entity Instance is undefined");
    }
    
    if (childClass == null) {
      throw new Exception("The Child/ForeignKey Class reference is unassigned.");
    }
    
    Class<TBean> entityClass = (Class<TBean>) entityBean.getClass();
    String childClassName = childClass.getSimpleName();

    String fieldName = 
                  ReflectionInfo.getChildCollectionField(entityClass, childClassName);
    if (fieldName == null) {
      throw new Exception("Entity[" + entityClass.getSimpleName()
              + "] does not support a collection Field[" 
              + childClassName + "Collection]");
    }
    ReflectionInfo.setFieldValue(entityBean, fieldName, childCol);
  }
  //</editor-fold>
}
