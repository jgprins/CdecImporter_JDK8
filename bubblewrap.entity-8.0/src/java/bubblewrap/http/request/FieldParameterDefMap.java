package bubblewrap.http.request;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.http.request.annotation.HttpParameter;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class FieldParameterDefMap implements Serializable, Iterable<FieldParameterDef> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(FieldParameterDefMap.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The class for which the parameters are defined
   */
  public final Class clazz;
  /**
   * A map of the field's parameter names (lowercase) and its FieldParameterDef
   */
  private HashMap<String, FieldParameterDef> fieldMap;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public FieldParameterDefMap(Class clazz) {
    super();  
    this.fieldMap = new HashMap<>();
    if (clazz == null) {
      throw new NullPointerException("The FieldMap's Class is udnefined");
    }
    this.clazz = clazz;
    this.initClass();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Called by the constructor to retrieve all the FieldParameterDefs for the GET-Method
   * with assigned {@linkplain HttpParameter} annotation assignments
   */
  private void initClass() {
    if (this.clazz == null) {
      return;
    }
    
    try {
      HttpParameter annot = null;
      List<String> methodNames = new ArrayList<>();
      /* Process the Public Method to search for declared and inherited GET-method */
      for (Method method : this.clazz.getMethods()) {
        if ((Modifier.isStatic(method.getModifiers())) ||
            (Modifier.isAbstract(method.getModifiers())) || 
            (Void.class.equals(method.getReturnType()))) {
          return;
        }
        
        methodNames.add(method.getName());
        if ((annot = method.getAnnotation(HttpParameter.class)) == null) {
          continue;
        }
        
        this.initFieldParamDef(method, annot);
      }
      
      /* Process the Declared Method to search for Declared non-public GET-methods */
      for (Method method : this.clazz.getDeclaredMethods()) {
        if ((Modifier.isStatic(method.getModifiers())) || 
            (Modifier.isAbstract(method.getModifiers())) || 
            (Modifier.isPublic(method.getModifiers())) || 
            (Void.class.equals(method.getReturnType())) ||
            (methodNames.contains(method.getName()))) {
          continue;
        }        
        
        methodNames.add(method.getName());
        if ((annot = method.getAnnotation(HttpParameter.class)) == null) {
          continue;
        }
        
        this.initFieldParamDef(method, annot);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initClass Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called by {@linkplain #initClass() this.initClass} to initiate the FieldParameterDef
   * for the specified GET-Method
   * @param getMethod
   * @param annot 
   */
  private void initFieldParamDef(Method getMethod, HttpParameter annot) {
    if ((getMethod == null) || (annot == null)) {
      return;
    }
    
    Method setMethod = null;
    if (!annot.readOnly()) {
      String setMethodName = DataEntry.cleanString(annot.set());
      if (setMethodName != null) {
        try {
          setMethod =  this.clazz.getMethod(setMethodName, getMethod.getReturnType());
        } catch (Exception exp) {
          setMethod = null;
          logger.log(Level.WARNING, "{0}.initFieldParamDef.getSET-Method Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
        
        if (setMethod == null) {
          try {
            setMethod =  
                  this.clazz.getDeclaredMethod(setMethodName, getMethod.getReturnType());
          } catch (Exception exp) {
            setMethod = null;
            logger.log(Level.WARNING, "{0}.initFieldParamDef.getSET-Method Error:\n {1}",
                    new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
          }
        }
        
        if ((setMethod == null) || 
           ((Modifier.isStatic(setMethod.getModifiers())) || 
            (Modifier.isAbstract(setMethod.getModifiers())))) {
          setMethod = null;
          logger.log(Level.WARNING, "{0}.initFieldParamDef.getSET-Method Error:\n "
                  + "SET-Method[{1}] could not be located or is a static or abstract "
                  + "method.",
                    new Object[]{this.getClass().getSimpleName(), setMethodName});
        }
      } else {
        String fieldName = ReflectionInfo.getFieldname(getMethod.getName());
        setMethodName = ReflectionInfo.getSetMethodName(fieldName);
        try {
          setMethod =  this.clazz.getMethod(setMethodName, getMethod.getReturnType());
        } catch (Exception exp) {
        }
        if (setMethod == null) {
          try {
            setMethod = 
                  this.clazz.getDeclaredMethod(setMethodName, getMethod.getReturnType());
          } catch (Exception exp) {
          }
        }
        if ((setMethod != null) && 
            ((Modifier.isStatic(setMethod.getModifiers())) || 
            (Modifier.isAbstract(setMethod.getModifiers())))) {
          setMethod = null;
        }
      }
    }  
    
    try {
      FieldParameterDef fieldDef = new FieldParameterDef(getMethod, setMethod, annot);
      String key = DataEntry.cleanLoString(fieldDef.paramName);
      if (this.fieldMap.containsKey(key)) {
        throw new Exception("Duplicate HHTTParameter[" + fieldDef.paramName + "].");
      }
      this.put(key, fieldDef);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initFieldParamDef Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to add a FieldParameterDef to the
   * @param key the parameter key (the value's paramName)
   * @param value the FieldParameterDef (null to remove)
   */
  private void put(String key, FieldParameterDef value) {
    if ((key = DataEntry.cleanLoString(value.paramName)) == null) {
      return;
    }
    if (value == null) {
      if (this.fieldMap.containsKey(key)) {
        this.fieldMap.remove(key);
      }
    } else {
      this.fieldMap.put(key, value);
    }
  }
  
  /**
   * Called to clear the Class
   */
  private void clear() {
    this.fieldMap.clear();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Size if map
   * @return this.fieldMap.size
   */
  public int size() {
    return this.fieldMap.size();
  }

  /**
   * Get whether the fieldMap is empty
   * @return this.fieldMap.isEmpty
   */
  public boolean isEmpty() {
    return this.fieldMap.isEmpty();
  }

  /**
   * Get the FieldParameterDef for a specified parameter name
   * @param paramName the name to search for
   * @return the FieldParameterDef or null if not found
   */
  public FieldParameterDef get(String paramName) {
    FieldParameterDef result = null;
    if (((paramName = DataEntry.cleanLoString(paramName)) != null) && 
            (this.fieldMap.containsKey(paramName))) {
      result= this.fieldMap.get(paramName);
    }
    return result;
  }

  /**
   * Check if the map contains a FieldParameterDef for a specified parameter name
   * @param paramName the name to search for
   * @return true is found
   */
  public boolean containsKey(String paramName) {
    return (((paramName = DataEntry.cleanLoString(paramName)) != null) && 
            (this.fieldMap.containsKey(paramName)));
  }
  
  /**
   * Retrieve a Map[parameterName, parameterValue] that represents all the mapped 
   * parameter values of the specified instance of this.clazz.
   * @param instance
   * @return the retrieved map. is empty if instance = null, instance.class != this.clazz
   * or this.isEmpty.
   */
  public synchronized Map<String, String> getValueMap(Object instance) {
    HashMap<String, String> result = new HashMap<>();
    if ((instance != null) && (this.clazz.equals(instance.getClass())) && 
            (!this.isEmpty())) {
      for (FieldParameterDef fieldDef : this) {
        try {
          String fieldVal = fieldDef.getFieldValue(instance);
          result.put(fieldDef.paramName, (fieldVal == null)? "": fieldVal);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.getValueMap.getParameter[{1}].value Error:\n {2}",
                  new Object[]{this.getClass().getSimpleName(),fieldDef.paramName,
                    exp.getMessage()});
        }
      }
    }
    return result;
  }
  
  /**
   * Called to assign the parameter values (e.g., from a web-form HttpRequest) to the
   * fields of the specified instance using the FieldParameterDefs for the instance's
   * class.
   * <p>
   * <b>NOTE:</b> All errors (including validation and conversion errors) are trapped
   * and assigned to this.errorMsg, which can be retrieved if this method returns false.
   * </p>
   * @param instance the instance  to update
   * @param paramMap the map of parameter->values
   * @return (!this.hasError()).
   */
  public synchronized boolean setValueMap(Object instance, 
                                                      Map<String, String> paramMap) {
    this.clearError();
    try {
      if ((instance != null) && (this.clazz.equals(instance.getClass())) && 
          (!this.isEmpty()) && (paramMap != null) && (!paramMap.isEmpty())) {
        FieldParameterDef fieldDef = null;
        for (String paramName : paramMap.keySet()) {
          if ((fieldDef = this.get(paramName)) != null) {
            try {
              String parValue = paramMap.get(paramName);
              fieldDef.setFieldValue(instance, parValue);
            } catch (Exception exp) {
              this.setErrorMsg(exp.getMessage());
            }
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return (!this.hasError());
  }
  
  /**
   * Called to assign the parameter values (e.g., from a web-form HttpRequest) to the
   * fields of the specified instance using the FieldParameterDefs for the instance's
   * class.
   * <p>
   * <b>NOTE:</b> All errors (including validation and conversion errors) are trapped
   * and assigned to this.errorMsg, which can be retrieved if this method returns false.
   * </p>
   * @param instance the instance  to update
   * @param paramMap the map of parameter->values
   * @return (!this.hasError()).
   */
  public synchronized void setValueMap(HttpRequestArgs args) {
    try {
      if ((args !=null) && (args.instance != null) && 
          (this.clazz.equals(args.instance.getClass())) && 
          (!this.isEmpty())) {
        for (FieldParameterDef fieldDef : this) {
          if (!args.hasParamValue(fieldDef.paramName)) {
            continue;
          }
          try {
            String parVal = args.getParamValue(fieldDef.paramName);
            fieldDef.setFieldValue(args.instance, parVal);
          } catch (Exception exp) {
            args.setErrorMsg(exp.getMessage());
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.setValueMap Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Error Messaeg Handling">
  /**
   * Placeholder of an error message during execution
   */
  private String errorMsg;

  /**
   * Get whether an error has been reported
   * @return (this.errorMsg != null)
   */
  public boolean hasError() {
    return (this.errorMsg != null);
  }

  /**
   * Clear the current error message
   */
  public void clearError() {
    this.errorMsg = null;
  }

  /**
   * get the current Error Message
   * @return this.errorMsg (can be null)
   */
  public String getErrorMsg() {
    return this.errorMsg;
  }

  /**
   * Set an error message. If this.errMsg != null, the new error message will be appended
   * separated with a ";\n " delimiter. The call is ignored if the new message = ""|null.
   * @param errMsg new error message
   */
  protected void setErrorMsg(String errMsg) {
    errMsg = DataEntry.cleanString(errMsg);
    if (errMsg != null) {
      if (this.errorMsg == null) {
        this.errorMsg = errMsg;
      } else {
        this.errorMsg += ";\n " + errMsg;
      }
    }
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Iterator">
  @Override
  public Iterator<FieldParameterDef> iterator() {
    return this.fieldMap.values().iterator();
  }
  // </editor-fold>
}
