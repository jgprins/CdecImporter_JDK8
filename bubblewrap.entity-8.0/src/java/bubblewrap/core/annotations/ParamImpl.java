package bubblewrap.core.annotations;

import bubblewrap.http.session.HttpUtils;
import bubblewrap.io.*;
import bubblewrap.io.params.*;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class ParamImpl implements Serializable, Param {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
          Logger.getLogger(ParamImpl.class.getSimpleName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Convert an array of {@linkplain Param} annotations to a list of ParamImpls
   * @param paramArr the array of {@linkplain Param}
   * @return the resulting list (can be empty (not null)
   */
  public static List<ParamImpl> fromAnnotation(Param... paramArr) {
    List<ParamImpl> result = new ArrayList<>();
    if ((paramArr != null) && (paramArr.length > 0)) {
      ParamImpl annotImpl = null;
      for (Param param : paramArr) {
        if ((param != null) && ((annotImpl = new ParamImpl(param)) != null)) {
          result.add(annotImpl);
        }
      }
    }
    return result;
  }
  
  /**
   * Convert a list of ParamImpl to an array of {@linkplain Param} annotations
   * @param annotList the input list
   * @return the Param array. Can be empty if annotList = null|empty.
   */
  public static Param[] toArray(List<ParamImpl> annotList) {
    Param[] result = new Param[0];
    if ((annotList != null) && (!annotList.isEmpty())) {
      result = annotList.toArray(result);
    }
    return result;
  }
  
  /**
   * Convert the key-value pair to ParamImpl. It handles this request as follows:<ul>
   * <li>(value = null), return ParamImpl(key, null, null)</li>
   * <li>(value instanceof Parameter), return ParamImpl(key, value.getValueToString, 
   *        value.class)</li>
   * <li>(value instanceof Number), return ParamImpl(key, value.toString(), 
   * {numParamClass}) where numParamClass is the Parameter Class for the value.class. 
   * Supports Byte, Double, Float, Integer, Long, and Short values.</li>
   * <li>(value instanceof Boolean), return ParamImpl(key, value.toString(), 
   * BooleanParameter.class). </li>
   * <li>(value instanceof DateTime), return ParamImpl(key, value.toString(), 
   * DateTimeParameter.class). </li>
   * <li>Else throw not supported exception</li>
   * </ul>
   * <p>
   * <b>NOTE:</b> return null is key = null|""</p>
   * @param key the parameter key 
   * @param value the value
   * @return the ParamImpl or null if not supported
   */
  @SuppressWarnings("unchecked")
  public static ParamImpl toParamImpl(String key, Object value) {
    ParamImpl result = null;
    try {
      if ((key = DataEntry.cleanString(key)) != null) {
        if (value == null) {
          result = new ParamImpl(key, null, null, null);
        } else if (value instanceof Parameter) {
          Parameter param = (Parameter) value;
          result = param.asParam();
        } else if (value instanceof Number) {
          Class<? extends Number> valClass = (Class<? extends Number>) value.getClass();
          if (valClass.equals(Byte.class)) {
            result = new ParamImpl(key, value.toString(), ByteParameter.class, null);
          } else if (valClass.equals(Short.class)) {
            result = new ParamImpl(key, value.toString(), ShortParameter.class, null);
          } else if (valClass.equals(Integer.class)) {
            result = new ParamImpl(key, value.toString(), IntegerParameter.class, null);
          } else if (valClass.equals(Long.class)) {
            result = new ParamImpl(key, value.toString(), LongParameter.class, null);
          } else if (valClass.equals(Float.class)) {
            result = new ParamImpl(key, value.toString(), FloatParameter.class, null);
          } else if (valClass.equals(Double.class)) {
            result = new ParamImpl(key, value.toString(), DoubleParameter.class, null);
          } else {
            throw new Exception("Value Class[" + valClass.getSimpleName()
                    + "] is not supported");
          }
        } else if (value instanceof Boolean) {
          result = new ParamImpl(key, value.toString(), BooleanParameter.class, null);
        } else if (value instanceof DateTime) {
          result = new ParamImpl(key, value.toString(), DateTimeParameter.class, null);
        } else if (value instanceof String) {
          result = new ParamImpl(key, value.toString(), StringParameter.class, null);
        } else {
          throw new Exception("Parameter Value Class[" + value.getClass().getSimpleName()
                  + "] cannot be wrapped in a ParamImpl.");
        }
      }
    } catch (Exception exp) {
      throw new IllegalArgumentException(ParamImpl.class.getSimpleName() 
              +  ".toParamImpl Error:\n" + exp.getMessage());
    }
    return result;
  }  
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Parameter Key (required)
   * @return the assigned Key
   */
  @XmlAttribute(name = "key")
  private String key;
  /**
   * The Parameter Value (Default = "") - encoded for serialization
   * @return the assigned value
   */
  @XmlElement(name = "value")
  private String value;
  /**
   * The Parameter Class to use in converting the specified string value to a casted
   * parameter value. Default = StringParameter.class.
   * @return the assign class
   */  
  @XmlTransient
  private Class<? extends Parameter> paramClass;
  /**
   * (Optional) The Format String to use in formating the parameter value (default = 
   * null|"").
   */
  @XmlElement(name = "format")
  private String format;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public ParamImpl() {
    super();
    this.key = null;
    this.value = null;
    this.paramClass = null;
    this.format = null;
  }
  
  /**
   * Public Constructor from a Param annotation 
   * @param annot the annotation - cannot be null
   */
  public ParamImpl(Param annot) {
    this();
    if (annot == null) {
      throw new IllegalArgumentException("The Param Annotation is unassigned.");
    }
    if ((this.key = DataEntry.toParamKey(annot.key())) == null) {
      throw new IllegalArgumentException("The Param Annotation's key is unassigned.");
    }
    this.value = HttpUtils.encodeString(annot.value());
    this.paramClass = (Class<? extends Parameter<?>>) annot.paramClass();
    this.format = DataEntry.cleanString(HttpUtils.encodeString(annot.format()));
  }
  
  /**
   * Public Constructor from a Parameter 
   * @param parameter the parameter - cannot be null
   */
  public ParamImpl(Parameter parameter) {
    this();
    if (parameter == null) {
      throw new IllegalArgumentException("The Parameter is unassigned.");
    }
    if ((this.key = DataEntry.toParamKey(parameter.getKey())) == null) {
      throw new IllegalArgumentException("The Parameter's key is unassigned.");
    }
    this.value = HttpUtils.encodeString(parameter.asString());
    this.paramClass = (Class<? extends Parameter>) parameter.getClass();
    this.format = DataEntry.cleanString(HttpUtils.encodeString(parameter.getFormat()));
  }
  
  /**
   * Public Constructor from a Parameter 
   * @param parameter the parameter - cannot be null
   */
  public ParamImpl(String key, String value, Class<? extends Parameter> paramClass, 
          String format) {
    super();
    if ((this.key = DataEntry.toParamKey(key)) == null) {
      throw new IllegalArgumentException("The Parameter's key is unassigned.");
    }
    this.value = HttpUtils.encodeString(value);
    this.paramClass = (paramClass == null)? StringParameter.class: paramClass;
    this.format = DataEntry.cleanString(HttpUtils.encodeString(format));
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of FormFormatDef.
   * Otherwise, return true if the following match: this.id = obj.id.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof Param));
    if (result) {
      Param trgObj = (Param) obj;
      result = (DataEntry.isEq(this.key(), trgObj.key(),true));
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode based on this.id</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 37 * hash + Objects.hashCode(this.key);
    return hash;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return "Param[ key=" + this.key() + "]"</p>
   */
  @Override
  public String toString() {
    return "Param[ key=" + this.key() + "]";
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Convert this Annotation to a parameter - call {@link 
   * Parameter#fromAnnotation(bubblewrap.core.annotations.Param)}.
   * @return Parameter.fromAnnotation(this)
   */
  @SuppressWarnings("unchecked")
  public final <TParam extends Parameter> TParam getParameter() {
    return (TParam) Parameter.fromAnnotation(this);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="For XML Serialization">
  /**
   * FOR XML: Get the parameter class' name for serialization
   * @return this.paramClass' name
   */
  @XmlElement(name = "paramClass")
  protected String getParamClassName() {
    Class<? extends Parameter> parClass = this.paramClass();
    return parClass.getName();
  }
  
  /**
   * FOR XML: Set the Parameter's class name - convert to class/
   * @param className the serialized class name
   */
  protected void setParamClassName(String className) {
    this.paramClass = null;
    if ((className = DataEntry.cleanString(className)) != null) {
      try {
        this.paramClass = (Class<? extends Parameter<?>>) Class.forName(className);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.setParamClassName Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Param Overrides">
  /**
   * The Parameter Key (required). Parameter keys should not contain any white spaces,
   * but can contain "_", ".", "-". All white spaces will be replaced with "_" 
   * characters - see {@linkplain DataEntry#toParamKey(java.lang.String)
   * DataEntry.toParamKey} for more information.
   * @return the assigned Key
   */
  @Override
  public String key() {
    return this.key;
  }
  /**
   * The Parameter Value (Default = "")
   * @return the assigned value
   */
  @Override
  public String value() {
    return (this.value == null)? "": HttpUtils.decodeString(this.value);
  }
  
  /**
   * The Parameter Class to use in converting the specified string value to a casted
   * parameter value. Default = StringParameter.class.
   * @return the assign class
   */  
  @Override
  public Class<? extends Parameter> paramClass() {
    return (this.paramClass == null)? StringParameter.class: this.paramClass;
  }
  /**
   * (Optional) The format string to be used in formatting the Parameter Value 
   * (default = "").
   * @return the assigned value
   */
  @Override
  public String format() {
    return (this.format == null)? "": HttpUtils.decodeString(this.format);
  }
  
  @Override
  public Class<? extends Annotation> annotationType() {
    return Param.class;
  }
  //</editor-fold>
}
