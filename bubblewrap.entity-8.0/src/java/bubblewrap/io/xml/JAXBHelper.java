package bubblewrap.io.xml;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * <p>The JAXBHelper class provides methods to serialize and deserialize any Well-formed
 * XML object, any generic list with Well-formed XML items, or any generic Map with 
 * well-formed XML Key and Value classes. It provides two serialization options:
 * </p><ul>
 *  <li><b>To/From XML String:</b> using the {@linkplain #getXMLContent(
 *    javax.xml.namespace.QName, java.lang.Object, boolean) getXMLContent} and one of 
 *    the three fromXMLContent overrides to serialize and deserialize the object to
 *    and from a XML string.</li>
 *  <li><b>To/From XML File:</b> using the {@linkplain #toXMLFile(
 *    javax.xml.namespace.QName, java.lang.Object, boolean) toXMLFile} and one of 
 *    the three fromXMLFile overrides to serialize and deserialize the object to
 *    and from a XML file.</li>
 * </ul>
 * <p><b>Well-formed XML Root Objects:</b> JAXB XML serialization requires that the 
 * root class of the XML content, or the elements of list, or the keys and values of 
 * maps be "well-formed". This implies a class level {@linkplain XmlRootElement} 
 * annotation and {@linkplain XmlAttribute} or {@linkplain XmlElement} annotation on 
 * methods or fields as applicable.</p>
 * <p>If the root element of the XML content is a generic list (e.g., type 
 * List&lt;MyClass&gt;) it will automatically be wrapped be in a {@linkplain 
 * JaxbListWrapper}.  Similarly, a generic Map (e.g., HashMap&lt;myKey,myValue&gt;) will
 * be wrapper in a {@linkplain JaxbMapWrapper}. - to pre-processing is needed.  However,
 * on deserialization the classes of the list item or map key and value must be 
 * declared.</p> 
 * @author kprins
 */
public class JAXBHelper {

  //<editor-fold defaultstate="collapsed" desc="Private Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  private static final Logger logger = Logger.getLogger(JAXBHelper.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Static Methods">
  /**
   * Called to retrieve the nested error message
   * @param except the caught exception
   * @return the error message
   */
  private static String getErrorMsg(Exception except) {
    String result = null;
    Throwable cause = except;
    while (cause != null) {
      String msg = DataEntry.cleanString(cause.toString());
      if (msg != null) {
        if (result == null) {
          result = msg;
        } else {
          result += "\n\t" + msg;
        }
      }
      if (cause instanceof UnmarshalException) {
        UnmarshalException linkExp = (UnmarshalException) cause;
        cause = linkExp.getLinkedException();
      } else {
        cause = cause.getCause();
      }
    }
    return result;
  }
  
  /**
   * <p>Called to Serialize a JAXB Enabled object (i.e. with an assigned {@linkplain 
   * XmlRootElement} annotation. If the QName is not assigned it will be initiated as
   * the Object class' XmlRootElement.name assignment or "root". It also build a
   * Class array to add to the JAXB Context's class list during the initiation of the
   * context. It then initiates the Context and use a marshaller to serialize the 
   * Object and all its sub-element to the output result.</p>
   * <p><b>NOTE:</b> If a JaxbListWrapper wrapper is used, the Class[TItem] must have an
   * assigned XmlRootElement annotation.</p>
   * @param <TObj> the type of root class to serialize
   * @param qName the name of the root element
   * @param xmlObject the instance of TObj to serialize
   * @param elemClasses (optional) the class add to the JAXB Context.
   * @param outResult the output Result to populate.
   * @exception JAXBException is the marshal process fails.
   */
  @SuppressWarnings("unchecked")
  private static <TObj> void marshal(QName qName, TObj xmlObject, 
  Class[] elemClasses, Result outResult, boolean doFormatted)
          throws JAXBException {
    if ((xmlObject == null) || (outResult == null)) {
      return;
    }      
    /**
     * Build a list of class to add to the JaxbContext 
     */
    List<Class> ctxClassList = null;
    if (elemClasses != null) {
      ctxClassList = new ArrayList<>(Arrays.asList(elemClasses));
    } else {
      ctxClassList = new ArrayList<>();
    }
    
    if (xmlObject instanceof JaxbListWrapper) {
      JaxbListWrapper jabxList = (JaxbListWrapper) xmlObject;
      if (jabxList.isEmpty()) {
        return;
      }
      Class listItemClass = jabxList.getItemClass();
      if ((listItemClass != null) && (ctxClassList.indexOf(listItemClass) < 0)) {
        ctxClassList.add(listItemClass);
      }
    } else if (xmlObject instanceof JaxbMapWrapper) {
      JaxbMapWrapper jabxMap = (JaxbMapWrapper) xmlObject;
      if (jabxMap.isEmpty()) {
        return;
      }
      ReflectionInfo.MapEntryClasses entryClasses = jabxMap.getEntryClasses();
      if ((entryClasses != null) && (entryClasses.isDefined())) {
        if (ctxClassList.indexOf(entryClasses.keyClass) < 0) {
          ctxClassList.add(entryClasses.keyClass);
        }
        if (ctxClassList.indexOf(entryClasses.valueClass) < 0) {
          ctxClassList.add(entryClasses.valueClass);
        }
      }
    }

    Class xmlClass = xmlObject.getClass();
    if (ctxClassList.indexOf(xmlClass) < 0) {
      ctxClassList.add(0, xmlClass);
    }
    
    Class[] ctxClassArr = new Class[]{};
    ctxClassArr = ctxClassList.toArray(ctxClassArr);

    /*
     * Get the Root Element's name (impClass.XmlRootElement.name) or "root"
     */
    if (qName == null) {
      XmlRootElement annot = 
                        (XmlRootElement) xmlClass.getAnnotation(XmlRootElement.class);
      boolean isRoot = (annot != null);
      String name = (isRoot)? DataEntry.cleanString(annot.name()): null;
      name = (name == null)? xmlClass.getSimpleName() : name;
      if (name != null) {
        qName = QName.valueOf(name);
      }
    }

    JAXBElement<TObj> jabxElem = new JAXBElement(qName, xmlClass, xmlObject);
    JAXBContext context = null;
    try {
      context = JAXBContext.newInstance(ctxClassArr);
    } catch (NullPointerException nullExp) {
      throw new JAXBException("Unable to resolve the JAXBContext for Classses[" 
              + Arrays.toString(ctxClassArr) + "].");
    }
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, doFormatted);
    marshaller.marshal(jabxElem, outResult);  
  }
  
  /**
   * <p>Called to Serialize a strong-Typed list to the specified Result in XML format. 
   * If the list does not have an assigned {@linkplain XmlRootElement}
   * annotation, the list will be wrapped in a {@linkplain JaxbListWrapper} and the 
   * latter will be used to serialize the list of class[TItem].</p>
   * <p><b>NOTE:</b> If a JaxbListWrapper wrapper is used, the Class[TItem] must have an
   * assigned XmlRootElement annotation.</p>
   * @param <TItem> the list item class
   * @param qName the name of the root element
   * @param xmlList the list to serialize
   * @param outResult the output Result to populate.
   * @exception JAXBException is the marshal process fails.
   */
  @SuppressWarnings("unchecked")
  private static <TItem> void marshalList(QName qName, List<TItem> xmlList,
  Result outResult, boolean doFormatted) throws JAXBException {
    if ((xmlList == null) || (xmlList.isEmpty())) {
      return;
    }
    
    Class<List<TItem>> xmlClass = (Class<List<TItem>>) xmlList.getClass();
    List<Class> ctxClassList = new ArrayList<>();
    Class elemClass = ReflectionInfo.getGenericListItemClass(xmlList);
    if (elemClass == null) {
      throw new JAXBException("Unabel to extract the List Item's class.");
    }
    ctxClassList.add(elemClass);
    
    XmlRootElement annot = 
                      (XmlRootElement) xmlClass.getAnnotation(XmlRootElement.class);
    Object xmlObject = null;
    if (annot != null) {
      xmlObject = xmlList;
      ctxClassList.add(xmlClass);
    } else {
      JaxbListWrapper<TItem> jabxList = new JaxbListWrapper<>(xmlList);
      xmlObject = jabxList;  
      Class<? extends JaxbListWrapper<TItem>> listClass = 
                               ReflectionInfo.castAsGenericClass(JaxbListWrapper.class);
      ctxClassList.add(listClass);
    }
    Class[] ctxClassArr = new Class[]{};
    ctxClassArr = ctxClassList.toArray(ctxClassArr);
    
    JAXBHelper.marshal(qName, xmlObject, ctxClassArr, outResult, doFormatted);
  }
  
  /**
   * <p>Called to Serialize a strong-Typed list to the specified Result in XML format. 
   * If the list does not have an assigned {@linkplain XmlRootElement}
   * annotation, the list will be wrapped in a {@linkplain JaxbListWrapper} and the latter will
   * be used to serialize the list of class[TItem].</p>
   * <p><b>NOTE:</b> If a JaxbListWrapper wrapper is used, the Class[TItem] must have an
   * assigned XmlRootElement annotation.</p>
   * @param <TItem> the list item class
   * @param qName the name of the root element
   * @param xmlList the list to serialize
   * @param pElemClass (optional) the class of list item.
   * @param outResult the output Result to populate.
   * @exception JAXBException is the marshal process fails.
   */
  @SuppressWarnings("unchecked")
  private static <TKey, TValue> void marshalMap(QName qName, Map<TKey, TValue> xmlMap,
  Result outResult, boolean doFormatted) throws JAXBException {
    if ((xmlMap == null) || (xmlMap.isEmpty())) {
      return;
    }
    List<Class> ctxClassList = new ArrayList<>();
    ReflectionInfo.MapEntryClasses entryClasses = 
                                            ReflectionInfo.getGenericMapClasses(xmlMap);
    if ((entryClasses == null) || (!entryClasses.isDefined())) {
      throw new JAXBException("Unable to extract the Map's Key and/or Value classses.");
    }
    
    ctxClassList.add(entryClasses.keyClass);
    ctxClassList.add(entryClasses.valueClass);
        
    Class xmlClass = xmlMap.getClass();
    XmlRootElement annot = 
                      (XmlRootElement) xmlClass.getAnnotation(XmlRootElement.class);
    Object xmlObject = null;
    if (annot != null) {
      xmlObject = xmlMap;
      ctxClassList.add(xmlClass);
    } else {
      JaxbMapWrapper<TKey, TValue> jabxMap = new JaxbMapWrapper<>(xmlMap);
      xmlObject = jabxMap;  
      Class<? extends JaxbMapWrapper<TKey, TValue>> mapClass = 
                                ReflectionInfo.castAsGenericClass(JaxbMapWrapper.class);
      Class<? extends JaxbMapWrapper.JaxbMapEntry<TKey, TValue>> mapKeyClass = 
                  ReflectionInfo.castAsGenericClass(JaxbMapWrapper.JaxbMapEntry.class);
      ctxClassList.add(mapClass);
      ctxClassList.add(mapKeyClass);
    }
    Class[] ctxClassArr = new Class[]{};
    ctxClassArr = ctxClassList.toArray(ctxClassArr);
    
    JAXBHelper.marshal(qName, xmlObject, ctxClassArr, outResult, doFormatted);
  }
  
  /**
   * <p>Called to Serialize a JAXB enabled Object (i.e., the object must 
   * have an assigned {@linkplain XmlRootElement} annotation. It calls {@linkplain 
   * #marshal(javax.xml.namespace.QName, java.lang.Object, java.lang.Class[], 
   * javax.xml.transform.Result) marshal} and pass a {@linkplain StreamResult 
   * StreamResult(StringWriter)} as the result.</p>
   * <p><b>NOTE:</b> The marshalling of the content is handled as follows:></p><ul>
   *  <li>If (xmlObject is a List), {@linkplain #marshalList(javax.xml.namespace.QName,
   *   java.util.List, javax.xml.transform.Result) marshallList} is called.</li>
   *  <li>If (xmlObject is a Map), {@linkplain #marshalMap(javax.xml.namespace.QName, 
   *    java.util.Map, javax.xml.transform.Result) marshallMap} is called.</li>
   *  <li>Else {@linkplain #marshal(javax.xml.namespace.QName, java.lang.Object, 
   *    java.lang.Class[], javax.xml.transform.Result) marshal} is called directly.</li>
   * </ul> 
   * @param <TObj> the class of the object to serialize.
   * @param qName the name of the root element
   * @param xmlObject the list to serialize
   * @param doFormatted true to get a formatted XML Content.
   * @return the serialized XML string or null if the list is unassigned or empty or if
   * an error occurred.
   * @exception Exception when the marshal process fails- errors are logged too.
   */
  @SuppressWarnings("unchecked")
  public static <TObj> String getXMLContent(QName qName, TObj xmlObject,
  boolean doFormatted) throws Exception {
    String result = null;  
    try {
      if (xmlObject != null) {
        if (xmlObject instanceof List) {
          List xmlList = (List) xmlObject;
          if ((xmlList != null) && (!xmlList.isEmpty())) {
            StringWriter strWriter = new StringWriter();
            StreamResult strResult = new StreamResult(strWriter);
            JAXBHelper.marshalList(qName, xmlList, strResult, doFormatted);
            result = strWriter.toString();
          }
        }else if (xmlObject instanceof Map) {
          Map xmlMap = (Map) xmlObject;
          if ((xmlMap != null) && (!xmlMap.isEmpty())) {
            StringWriter strWriter = new StringWriter();
            StreamResult strResult = new StreamResult(strWriter);
            JAXBHelper.marshalMap(qName, xmlMap, strResult, doFormatted);
            result = strWriter.toString();
          }
        } else {
          StringWriter strWriter = new StringWriter();
          StreamResult strResult = new StreamResult(strWriter);
          JAXBHelper.marshal(qName, xmlObject, null, strResult, doFormatted);
          result = strWriter.toString();
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.getXMLContent Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(exp)});
      throw exp;
    }
    return result;
  }
  
  /**
   * <p>Called to Serialize a JAXB enabled Object (i.e., the object must 
   * have an assigned {@linkplain XmlRootElement} annotation. It calls {@linkplain 
   * #marshal(javax.xml.namespace.QName, java.lang.Object, java.lang.Class[], 
   * javax.xml.transform.Result) marshal} and pass a {@linkplain StreamResult 
   * StreamResult(pFile)} as the result.</p>
   * <p><b>NOTE:</b> It tries to delete the File if it exist before writing a new file.
   * If access to the file is denied the process will be aborted.</p>
   * <p><b>NOTE:</b> The marshalling of the content is handled as follows:></p><ul>
   *  <li>If (xmlObject is a List), {@linkplain #marshalList(javax.xml.namespace.QName,
   *   java.util.List, javax.xml.transform.Result) marshallList} is called.</li>
   *  <li>If (xmlObject is a Map), {@linkplain #marshalMap(javax.xml.namespace.QName, 
   *    java.util.Map, javax.xml.transform.Result) marshallMap} is called.</li>
   *  <li>Else {@linkplain #marshal(javax.xml.namespace.QName, java.lang.Object, 
   *    java.lang.Class[], javax.xml.transform.Result) marshal} is called directly.</li>
   * </ul> 
   * <p><b>NOTE:</b> XML output to file is always formatted.</p>
   * @param <TObj> the class of the object to serialize.
   * @param qName the name of the root element
   * @param xmlObject the list to serialize
   * @param pFile the file to write to.
   * @exception Exception when the marshal process fails- errors are logged too.
   */
  @SuppressWarnings("unchecked")
  public static <TObj> boolean toXMLFile(QName qName, TObj xmlObject, File pFile) 
            throws Exception {
    boolean result = false;  
    try {
      if (pFile == null) {
        throw new Exception("The destination XML File cannot be udnefined.");
      }
      
      if (pFile.exists()) {
        try {
          pFile.delete();
        } catch (Exception pExp) {
        }
        if (pFile.exists()) {
          throw new Exception("Unable to delete the existing file.");
        }
      }
      
      if (xmlObject != null) {
        if (xmlObject instanceof List) {
          List xmlList = (List) xmlObject;
          if ((xmlList != null) && (!xmlList.isEmpty())) {
            StreamResult outResult = new StreamResult(pFile);
            JAXBHelper.marshalList(qName, xmlList, outResult, true);
          }
        } else if (xmlObject instanceof Map) {
          Map xmlMap = (Map) xmlObject;
          if ((xmlMap != null) && (!xmlMap.isEmpty())) {
            StreamResult outResult = new StreamResult(pFile);
            JAXBHelper.marshalMap(qName, xmlMap, outResult, true);
          }
        } else {
          StreamResult outResult = new StreamResult(pFile);
          JAXBHelper.marshal(qName, xmlObject, null, outResult, true);
        }
        result = pFile.exists();
      }
    } catch (Exception exp) {
      Throwable cause = (exp.getMessage() == null)? exp.getCause(): exp;
      logger.log(Level.WARNING, "{0}.toXMLFile Error:\n {1}",
              new Object[]{"JAXBHelper", cause.getMessage()});
      throw exp;
    }
    return result;
  }
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="Deserialization Method">
  /**
   * Called to deserialize the XML Source to an instance of type TObj. I first initiates
   * a Class array for adding to the JaxbContext. It also check if the RootClass is a
   * list without an XmlRootElement annotation. if true, it will use the JaxbListWrapper class
   * as the root class. It then un-marshal the source content and retrieve the 
   * resulting instance of type TObj.
   * @param <TObj> the type of the instance to return.
   * @param xmlSource the XML source to deserialize
   * @param xmlRootClass the XML source's root class.
   * @param elemClass the list element class if the root class is a lit.
   * @return the deserialize instance of TObj or null is XML Source is empty or if 
   * an error occurred.
   * @throws JAXBException if the unmarshal process fails.
   */
  @SuppressWarnings("unchecked")
  private static <TObj> TObj unmarshal(Source xmlSource, Class<TObj> xmlRootClass, 
                             Class[] elemClasses) throws JAXBException {
    TObj result = null;
    if ((xmlSource != null) && (xmlRootClass != null)) {        
      /**
      * Build a list of class to add to the JaxbContext 
      */
      List<Class> ctxClassList = null;
      if (elemClasses != null) {
        ctxClassList = new ArrayList(Arrays.asList(elemClasses));
      } else {
        ctxClassList = new ArrayList();;
      }
      
      ctxClassList.add(xmlRootClass);
      
      Class xmlSrcClass = xmlRootClass;
      if (List.class.isAssignableFrom(xmlRootClass)) {
        XmlRootElement annot = 
                     (XmlRootElement) xmlRootClass.getAnnotation(XmlRootElement.class);
        if (annot == null) {
          xmlSrcClass = JaxbListWrapper.class;
          ctxClassList.add(xmlSrcClass);
        } 
      } else if (Map.class.isAssignableFrom(xmlRootClass)) {
        XmlRootElement annot = 
                     (XmlRootElement) xmlRootClass.getAnnotation(XmlRootElement.class);
        if (annot == null) {
          xmlSrcClass = JaxbMapWrapper.class;
          ctxClassList.add(xmlSrcClass);
        } 
      }
      Class[] ctxClassArr = new Class[]{};
      ctxClassArr = ctxClassList.toArray(ctxClassArr);
      
      /**
      * Initiate the JaxbContext  and unmarshal the XML Source.
      */
      JAXBContext context = JAXBContext.newInstance(ctxClassArr);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      Object jaxbObj = unmarshaller.unmarshal(xmlSource, xmlSrcClass);

      /**
       * Extract the original serialized object
       */
      if (jaxbObj != null) {
        Object xmlObj = null;
        if (jaxbObj instanceof JAXBElement) {
          xmlObj = JAXBIntrospector.getValue(jaxbObj);
        }

        if ((!xmlRootClass.equals(JaxbListWrapper.class)) 
                && (xmlObj instanceof JaxbListWrapper)) {
          JaxbListWrapper jaxbList = (JaxbListWrapper) xmlObj;
          xmlObj = jaxbList.getList();
        } else if ((!xmlRootClass.equals(JaxbMapWrapper.class)) 
                && (xmlObj instanceof JaxbMapWrapper)) {
          JaxbMapWrapper jaxbMap = (JaxbMapWrapper) xmlObj;
          xmlObj = jaxbMap.getMap();
        }

        if ((xmlObj != null) && (xmlRootClass.isAssignableFrom(xmlObj.getClass()))) {
          result = (TObj) xmlObj;
        }
      }        
    }    
    return result;
  }
  
  /**
   * <p>Overload 1: Called to deserialize the specified XML content to new instance of
   * the specified Root Element Class. It calls {@linkplain #unmarshal(
   * javax.xml.transform.Source, java.lang.Class, java.lang.Class) unmarshal} passing 
   * in {@linkplain StreamSource StreamSource(StringReader(xmlContent))}.</p>
   * <p><b>NOTE:</b> This method should not be used to deserialize List or Maps. User 
   * Overload 2 and 3 instead. </p>
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param xmlContent the source XML content.
   * @param xmlRootClass the root element class of type TObj - no List or Map classes.
   * @return the deserialized instance of type TObj or null if then is null|""
   * @throws Exception if any error occur.
   */
  @SuppressWarnings("unchecked")
  public static <TObj> TObj parseXMLContext(String xmlContent, Class<TObj> xmlRootClass)
                                                                    throws Exception {
    TObj result = null;
    try {
      xmlContent = DataEntry.cleanString(xmlContent);
      if (xmlContent != null) {
        if (xmlRootClass == null) {
          throw new Exception("The Root Class to deserialize cannot be unassigned.");
        } 
        
        if (List.class.isAssignableFrom(xmlRootClass)) {
          throw new Exception("Invalid method overload. Use Overload 2 to deserialize "
                  + "Lists.");
        }
        if (Map.class.isAssignableFrom(xmlRootClass)) {
          throw new Exception("Invalid method overload. Use Overload 3 to deserialize "
                  + "Maps.");
        }
      
        StringReader strReader = new StringReader(xmlContent);
        StreamSource xmlSource = new StreamSource(strReader);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, null);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parseXMLContext Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }
  
  /**
   * <p>Overload 2: Called to deserialize the specified XML content to new instance of
   * the specified List Root Element Class. It first retrieve the List's elemClass (if 
   * not defined) and then calls {@linkplain #unmarshal(javax.xml.transform.Source,
   * java.lang.Class, java.lang.Class) unmarshal} passing in {@linkplain StreamSource
   * StreamSource(StringReader(xmlContent))}.</p>
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param xmlContent the source XML content.
   * @param xmlRootClass the root element class of type TObj
   * @param elemClass if xmlRootClass is a List, specify the List Element Class. 
   * Otherwise it can be null.
   * @return the deserialized instance of type TObj or null if then is null|""
   * @throws Exception if any error occur.
   */
  @SuppressWarnings("unchecked")
  public static <TItem> List<TItem> parseXMLContext(String xmlContent, 
            Class<? extends List<TItem>> xmlRootClass, Class<TItem> elemClass) 
            throws Exception {
    List<TItem> result = null;
    try {
      xmlContent = DataEntry.cleanString(xmlContent);
      if (xmlContent != null) {
        if (xmlRootClass == null) {
          throw new Exception("The List Root Class to deserialize cannot be "
                  + "unassigned.");
        } else if (elemClass == null) {
          elemClass = ReflectionInfo.getGenericListItemClass(xmlRootClass);
          if (elemClass == null) {
            throw new Exception("The Element Class for the Generic Root List Class is "
                    + "required for deserializing the list.");
          }
        }
      
        StringReader strReader = new StringReader(xmlContent);
        StreamSource xmlSource = new StreamSource(strReader);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, new Class[]{elemClass});
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parseXMLContext Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }
  
  /**
   * Overload 3: Called to deserialize the specified XML content to new instance of the 
   * specified Map Root Element Class. It first retrieve the Map's entry Classes (if 
   * not defined) and then calls {@linkplain #unmarshal(javax.xml.transform.Source, 
   * java.lang.Class, java.lang.Class) unmarshal} passing in {@linkplain StreamSource
   * StreamSource(StringReader(xmlContent))}.
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param xmlContent the source XML content.
   * @param xmlRootClass the root element class of type TObj
   * @param keyClass if xmlRootClass is a List, specify the Map Key Class. 
   * @param valueClass if xmlRootClass is a List, specify the Map Value Class. 
   * Otherwise it can be null.
   * @return the deserialized instance of type TObj or null if then is null|""
   * @throws Exception if any error occur.
   */
  @SuppressWarnings("unchecked")
  public static <TKey,TMap> Map<TKey,TMap> parseXMLContext(String xmlContent, 
                      Class<? extends Map<TKey,TMap>> xmlRootClass, Class keyClass, 
                      Class valueClass) throws Exception {
    Map<TKey,TMap> result = null;
    try {
      xmlContent = DataEntry.cleanString(xmlContent);
      if (xmlContent != null) {
        Class[] elemClasses = null;
        if (xmlRootClass == null) {
          throw new Exception("The Map Root Class to deserialize cannot be "
                  + "unassigned.");
        } else if ((keyClass == null) || (valueClass == null)) {
          ReflectionInfo.MapEntryClasses entryClasses =
                                    ReflectionInfo.getGenericMapClasses(xmlRootClass);
          if ((entryClasses == null) || (!entryClasses.isDefined())) {
            throw new Exception("The Map Entry Classes (i.e., the key and value "
                    + "classes for the Generic Root Map Class are "
                    + "required for deserializing the Map.");
          }
          
          elemClasses = new Class[]{entryClasses.keyClass, entryClasses.valueClass};          
        } else {
          elemClasses = new Class[]{keyClass, valueClass};
        }
      
        StringReader strReader = new StringReader(xmlContent);
        StreamSource xmlSource = new StreamSource(strReader);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, elemClasses);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parseXMLContext Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }

  /**
   * <p>Overload 1: Called to deserialize the specified XML File's content to new
   * instance of the specified Root Element Class. It calls {@linkplain #unmarshal(
   * javax.xml.transform.Source, java.lang.Class, java.lang.Class) unmarshal} passing
   * in {@linkplain StreamSource StreamSource(pFile)}.</p>
   * <p><b>NOTE:</b> This method should not be used to deserialize List or Maps. User
   * Overload 2 and 3 instead. </p>
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param pXmlFile the source XML file.
   * @param xmlRootClass the root element class of type TObj
   * Otherwise it can be null.
   * @return the deserialized instance of type TObj of null if the file is no defined
   * or it does not exist.
   * @throws Exception if any error occur.
   */
  public static <TObj> TObj fromXMLFile(File pXmlFile, Class<TObj> xmlRootClass)
          throws Exception {
    TObj result = null;
    try {
      if ((pXmlFile != null) && (pXmlFile.exists())) {
        if (xmlRootClass == null) {
          throw new Exception("The Root Class to deserialize cannot be unassigned.");
        }
      
        if (List.class.isAssignableFrom(xmlRootClass)) {
          throw new Exception("Invalid method overload. Use Overload 2 to deserialize "
                  + "Lists.");
        }
        
        if (Map.class.isAssignableFrom(xmlRootClass)) {
          throw new Exception("Invalid method overload. Use Overload 3 to deserialize "
                  + "Maps.");
        }
        
        StreamSource xmlSource = new StreamSource(pXmlFile);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, null);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.fromXMLFile Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }
  
  /**
   * <p>Overload 2: Called to deserialize the specified XML File's content to a new
   * instance the specified List Root Element Class. It first retrieve the List's
   * elemClass (if not defined) and then calls {@linkplain #unmarshal(
   * javax.xml.transform.Source,java.lang.Class, java.lang.Class) unmarshal}
   * passing in {@linkplain StreamSource StreamSource(pFile)}.</p>
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param pXmlFile the source XML file.
   * @param xmlRootClass the root element class of type TObj
   * @param elemClass if xmlRootClass is a List, specify the List Element Class.
   * Otherwise it can be null.
   * @return the deserialized instance of type TObj or null if then is null|""
   * @throws Exception if any error occur.
   */
  @SuppressWarnings("unchecked")
  public static <TItem> List<TItem> fromXMLFile(File pXmlFile,
  Class<? extends List<TItem>> xmlRootClass, Class<TItem> elemClass)
          throws Exception {
    List<TItem> result = null;
    try {
      if ((pXmlFile != null) && (pXmlFile.exists())) {
        if (xmlRootClass == null) {
          throw new Exception("The List Root Class to deserialize cannot be "
                  + "unassigned.");
        } else if (elemClass == null) {
          elemClass = ReflectionInfo.getGenericListItemClass(xmlRootClass);
          if (elemClass == null) {
            throw new Exception("The Element Class for the Generic Root List Class is "
                    + "required for deserializing the list.");
          }
        }
        
        StreamSource xmlSource = new StreamSource(pXmlFile);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, new Class[]{elemClass});
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }
  
  /**
   * <p>Overload 3: Called to deserialize the specified XML File's content to a new
   * instance the specified Map Root Element Class. It first retrieve the Map's entry
   * Classes (if not defined) and then calls {@linkplain #unmarshal(
   * javax.xml.transform.Source, java.lang.Class, java.lang.Class) unmarshal} passing in
   * {@linkplain StreamSource StreamSource(pFile)}.</p>
   * @param <TObj> an JAXB enabled class or a generic list class.
   * @param pXmlFile the source XML file.
   * @param xmlRootClass the root element class of type TObj
   * @param keyClass if xmlRootClass is a List, specify the Map Key Class.
   * @param valueClass if xmlRootClass is a List, specify the Map Value Class.
   * Otherwise it can be null.
   * @return the deserialized instance of type TObj or null if then is null|""
   * @throws Exception if any error occur.
   */
  @SuppressWarnings("unchecked")
  public static <TKey,TMap> Map<TKey,TMap> fromXMLFile(File pXmlFile,
  Class<? extends Map<TKey,TMap>> xmlRootClass, Class keyClass,
  Class valueClass) throws Exception {
    Map<TKey,TMap> result = null;
    try {
      if ((pXmlFile != null) && (pXmlFile.exists())) {
        Class[] elemClasses = null;
        if (xmlRootClass == null) {
          throw new Exception("The Map Root Class to deserialize cannot be "
                  + "unassigned.");
        } else if ((keyClass == null) || (valueClass == null)) {
          ReflectionInfo.MapEntryClasses entryClasses =
                  ReflectionInfo.getGenericMapClasses(xmlRootClass);
          if ((entryClasses == null) || (!entryClasses.isDefined())) {
            throw new Exception("The Map Entry Classes (i.e., the key and value "
                    + "classes for the Generic Root Map Class are "
                    + "required for deserializing the Map.");
          }
          
          elemClasses = new Class[]{entryClasses.keyClass, entryClasses.valueClass};
        } else {
          elemClasses = new Class[]{keyClass, valueClass};
        }
        
        StreamSource xmlSource = new StreamSource(pXmlFile);
        result = JAXBHelper.unmarshal(xmlSource, xmlRootClass, elemClasses);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.method Error:\n {1}",
              new Object[]{"JAXBHelper", JAXBHelper.getErrorMsg(pExp)});
      throw pExp;
    }
    return result;
  }
  //</editor-fold> 
}
