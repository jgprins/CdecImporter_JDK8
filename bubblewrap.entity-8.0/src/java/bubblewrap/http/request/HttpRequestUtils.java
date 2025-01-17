package bubblewrap.http.request;

import bubblewrap.http.request.annotation.HttpParameter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Static Class
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public final class HttpRequestUtils {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(HttpRequestUtils.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Static Fields">
  /**
   * The static placeholder for the registry of the FieldParameterDefMap for 
   * class that supports or are used for get- and set- of field parameters.
   */
  private static HashMap<Class,FieldParameterDefMap> _paramDefRegistry = null;  
  // </editor-fold>

  /**
   * Get the FieldParameterDefMap (that contains all Field-Parameter mapping definitions
   * for the specified class). If not yet in the registry, the FieldParameterDefMap will
   * be initiated and added to teh application's registry
   * @param clazz the class to search for
   * @return the map or null if <tt>clazz</tt> = null.
   */
  public synchronized static FieldParameterDefMap getFieldParamDefMap(Class clazz) {
    FieldParameterDefMap result = null;
    try {
      if (clazz != null) {
        if ((_paramDefRegistry != null) && (_paramDefRegistry.containsKey(clazz))) {
          result = _paramDefRegistry.get(clazz);
        } else {
          result = new FieldParameterDefMap(clazz);
          if (result != null) {
            if (_paramDefRegistry == null) {
              _paramDefRegistry = new HashMap<>();
            }
            _paramDefRegistry.put(clazz, result);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getFieldParamDefMap Error:\n {1}",
              new Object[]{HttpRequestUtils.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Get a Map[parameter->value] for all field-parameter mapped values of <tt>instance
   * </tt>. 
   * <p>Field-parameter mapping is defined using the {@linkplain HttpParameter}
   * annotation in conjunction with other entity field annotation to define field
   * validation constraints)
   * @param instance the instance to map
   * @return the map of the parameter->value pairs
   */
  public synchronized static Map<String,String> getObjectValues(Object instance) {
    Map<String,String> result = null;
    try {
      FieldParameterDefMap fldParMap = null;
      if ((instance != null) && ((fldParMap = 
              HttpRequestUtils.getFieldParamDefMap(instance.getClass())) != null) &&
              (!fldParMap.isEmpty())) {
        result = fldParMap.getValueMap(instance);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getParameterMap Error:\n {1}",
              new Object[]{HttpRequestUtils.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Update the <tt>instance</tt> field value based on the settings of <tt>paramMap</tt>
   * - a Map[parameter->value] for field-parameter mapped values of <tt>instance</tt>. 
   * <p>Field-parameter mapping is defined using the {@linkplain HttpParameter}
   * annotation in conjunction with other entity field annotation to define field
   * validation constraints)
   * @param args the instance to map
   * @return the map of the parameter->value pairs
   */
  public synchronized static void setObjectValues(HttpRequestArgs args) {
    try {
      FieldParameterDefMap fldParMap = null;
      if ((args != null) && (args.instance != null) &&  
          ((fldParMap = 
              HttpRequestUtils.getFieldParamDefMap(args.instance.getClass())) != null) &&
          (!fldParMap.isEmpty())) {
        fldParMap.setValueMap(args);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getParameterMap Error:\n {1}",
              new Object[]{HttpRequestUtils.class.getSimpleName(), exp.getMessage()});
    }
  }
}
