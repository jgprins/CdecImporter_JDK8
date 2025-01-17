package bubblewrap.io.params;

import bubblewrap.core.annotations.Param;
import bubblewrap.core.annotations.ParamImpl;
import bubblewrap.io.ObjectData;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * A base extension of the generic ParameterMapBase
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class ParameterMap extends ParameterMapBase<ParameterMap> {
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Convert an array of Parameter Annotation to a ParameterMap containing the
   * {@linkplain Parameter Parameters} as the Map values.
   * @param annotArr the annotation array
   * @return a ParameterMap instance (empty if <tt>annotArr</tt> = null|empty.
   */
  public static ParameterMap fromAnnotation(Param[] annotArr) {
    ParameterMap result = new ParameterMap();
    try {
      if ((annotArr != null) && (annotArr.length > 0)) {
        for (Param annot : annotArr) {
          Parameter<?> par = Parameter.fromAnnotation(annot);
          if (par != null) {
            result.put(par.getKey(), par);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.fromAnnotation Error:\n {1}",
              new Object[]{ParameterMap.class.getSimpleName(), exp.getMessage()});
    }
    
    return result;
  }
  
  /**
   * Convert ParameterMap to a list of {@linkplain ParamImpl} Annotation. The list
   * can be converted to a Param[] by calling {@linkplain 
   * ParamImpl#toArray(java.util.List)}
   * @param paramMap the ParameterMap to convert
   * @return a list of ParamImpl (empty if <tt>paramMap</tt> = null|empty).
   */
  public static List<ParamImpl> toAnnotation(ParameterMap paramMap) {
    List<ParamImpl> result = new ArrayList<>();
    try {
      if ((paramMap != null) && (!paramMap.isEmpty())) {
        for (String key : paramMap.getKeys()) {
          Object parVal = paramMap.get(key, null);
          ParamImpl param = ParamImpl.toParamImpl(key, parVal);
          if (param != null) {
            result.add(param);
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.toAnnotation Error:\n {1}",
              new Object[]{ParameterMap.class.getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Singleton Void reference">
  /**
   * Placeholder for the static singleton void instance
   */
  private static Void voidInstance = null;
  /**
   * Get a reference to the singleton Void instance
   * @return the singleton Void instance
   */
  public static Void asVoid() {
    if (ParameterMap.voidInstance == null) {
      ParameterMap.voidInstance = new Void();
    }
    return ParameterMap.voidInstance;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public static Void class">
  public static class Void extends ParameterMap {
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    private Void() {
      super();
    }
    //</editor-fold>
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: return true of obj=this</p>
     */
    @Override
    public boolean equals(Object obj) {
      return ((obj != null) && (obj == this));
    }
    
    /**
     * {@inheritDoc }
     * <p>OVERRIDE: Return 0</p>
     */
    @Override
    public int hashCode() {
      return 0;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public ParameterMap() {
    super();
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get whether this is a reference to the singleton Void instance
   * @return true if ParameterMap.asVoid().equals(this)
   */
  public final boolean isVoid() {
    return (ParameterMap.asVoid().equals(this));
  }
  //</editor-fold>
}
