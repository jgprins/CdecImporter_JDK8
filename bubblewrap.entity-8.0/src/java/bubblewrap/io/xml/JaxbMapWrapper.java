package bubblewrap.io.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import bubblewrap.core.reflection.ReflectionInfo;

/**
 * <p>The JaxbMapWrapper is used by the {@linkplain JAXBHelper} to serialize non-JAXB
 * compliant Maps. These maps can be generic but must be strong-typed (i.e., all keys
 * and values must be of the same type). The format of the serialized XML is as
 * follows:</p>
 * <code style="font-size:smaller;">
 * &lt;?xml version="1.0" encoding="UTF-8" standalone="yes"?&gt;<br/>
 * &lt;Seconds&gt;<br/>
 * &nbsp;&nbsp;&lt;entry&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;key xsi:type="xs:string" 
 *  xmlns:xs="http://www.w3.org/2001/XMLSchema"<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;object1&lt;/key&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;value xsi:type="secondClass" <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 *  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;name&gt;..&lt;/name&gt; <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;   ....<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;<br/>
 * &nbsp;&nbsp;&lt;/entry&gt;<br/>
 * &nbsp;&nbsp;&lt;entry&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;key xsi:type="xs:string" 
 *  xmlns:xs="http://www.w3.org/2001/XMLSchema"<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; 
 *  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;object2&lt;/key&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;value xsi:type="secondClass" <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;  &lt;name&gt;..&lt;/name&gt;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;  ....<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/value&gt;&lt;br/&gt;<br/>
 * &nbsp;&nbsp;&lt;/entry&gt;&lt;br/&gt;<br/>
 * &lt;/Seconds&gt;<br/><br/>
 * </code>
 * @author kprins
 */
public class JaxbMapWrapper<TKey,TValue> {
  
  //<editor-fold defaultstate="collapsed" desc="Static JaxbMapEntry Class">
  /**
   * A Static Class for the XML Serializing of the Map Entries.
   */
  public static class JaxbMapEntry<TKey,TValue> {
    //@XmlAttribute
    public TKey key;
    //@XmlAnyElement(lax=true)
    public TValue value;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * Placeholder fro the wrapped Map
   */
  private Map<TKey,TValue> map;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor (for XML Serialization)
   */
  public JaxbMapWrapper() {
    this.map = null;
  }
  
  /**
   * Public Constructor to create the wrapper
   * @param wrapMap the Map to wrap.
   */
  public JaxbMapWrapper(Map<TKey,TValue> wrapMap){
    this.map = wrapMap;
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Wrapped Map's Generic Map Entry (Key-Value) Classes
   * @return the map entry's classes, which can be undefined if it cannot be resolved.
   */
  @XmlTransient
  @SuppressWarnings("unchecked")
  public ReflectionInfo.MapEntryClasses<TKey,TValue> getEntryClasses() {
    ReflectionInfo.MapEntryClasses<TKey,TValue> result = null;
    Class<?> keyClass 
            = ReflectionInfo.getGenericClass(JaxbMapWrapper.class, this.getClass(), 0);
    if (keyClass != null) {
      Class<?> valueClass 
            = ReflectionInfo.getGenericClass(JaxbMapWrapper.class, this.getClass(), 1);
      if (valueClass != null) {
        result = new ReflectionInfo.MapEntryClasses<>();
        result.keyClass = (Class<TKey>) keyClass;
        result.valueClass = (Class<TValue>) keyClass;
      }
    }
    
    if ((result == null) && (!this.isEmpty())) {
      result = ReflectionInfo.getGenericMapClasses(this.map);
    }
    
    if (result == null) {
      result = new ReflectionInfo.MapEntryClasses<>();
        
    }
    return result;
  }
  
  /**
   * Get a reference to the Wrapper's assigned Map
   * @return the assigned map or null is unassigned.
   */
  @XmlTransient
  public Map<TKey, TValue> getMap() {
    return this.map;
  }
  
  /**
   * Get whether the wrapped list is empty
   * @return true if the wrapped list is unassigned or empty.
   */
  @XmlTransient
  public boolean isEmpty() {
    return ((this.map == null) || (this.map.isEmpty()));
  }
  
  /**
   * Get the wrapped map as a list of JaxbMapEntries
   * @return the JaxbMapEntry list
   */
  @XmlElement(name="entry", type=JaxbMapWrapper.JaxbMapEntry.class)
  public List<JaxbMapEntry<TKey, TValue>> getMapEntries() {
    List<JaxbMapEntry<TKey, TValue>> result = new ArrayList<>();
    if ((this.map != null) && (!this.map.isEmpty())) {
      for (Map.Entry<TKey, TValue> entry : this.map.entrySet()) {
        JaxbMapEntry<TKey, TValue> mapEntry = new JaxbMapEntry<>();
        mapEntry.key = entry.getKey();
        mapEntry.value = entry.getValue();
        result.add(mapEntry);
      }
    }
    return result;
   }
  
  /**
   * Assign the wrapped map's entries
   * @param mapEntries the new list to JaxbMapEntries.
   */
  protected void setMapEntries(List<JaxbMapEntry<TKey, TValue>> mapEntries) {
    this.map = null;
    if ((mapEntries != null) && (!mapEntries.isEmpty())) {
      this.map = new HashMap<>();
      for (JaxbMapEntry<TKey, TValue> mapEntry : mapEntries) {
        if (mapEntry != null) {
          this.map.put(mapEntry.key, mapEntry.value);
        }
      }
    }
  }
  //</editor-fold>
}
