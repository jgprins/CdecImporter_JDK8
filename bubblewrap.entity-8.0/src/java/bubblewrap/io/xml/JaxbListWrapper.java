package bubblewrap.io.xml;

import java.util.List;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import bubblewrap.core.reflection.ReflectionInfo;

/**
 * <p>The is a JAXB Serialization Wrapper for a generic List&lt;TItem&gt;. This wrapper
 * can be used by {@linkplain JAXBHelper} to serialize any generic list, provided that 
 * the class[TItem] has an assigned {@linkplain XmlRootElement} annotation.</p>
 * @author kprins
 */
public class JaxbListWrapper<TItem> {
  
  /**
   * Placeholder fro the wrapped list
   */
  @XmlAnyElement(lax=true)
  private List<TItem> list;

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor (for XML Serialization)
   */
  public JaxbListWrapper() {
    this.list = null;
  }
  
  /**
   * Public Constructor to create the wrapper
   * @param wrapList the list to wrap.
   */
  public JaxbListWrapper(List<TItem> wrapList){
    this.list = wrapList;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Wrapped List Item's Generic Item Class
   * @return the class if the list item or n
   */
  @XmlTransient
  public Class<?> getItemClass() {
    Class<?> result = 
              ReflectionInfo.getGenericClass(JaxbListWrapper.class, this.getClass(), 0);
    
    if ((result != null) && (!this.isEmpty())) {
      result = ReflectionInfo.getGenericListItemClass(this.list);
    }
    return result;
  }
  
  /**
   * Get whether the wrapped list is empty
   * @return true if the wrapped list is unassigned or empty.
   */
  @XmlTransient
  public boolean isEmpty() {
    return ((this.list == null) || (this.list.isEmpty()));
  }
  
  /**
   * Get the wrapped list
   * @return a reference to the assigned list
   */
  public List<TItem> getList() {
    return list;
  }
  
  /**
   * set the wrapped list
   * @param wrapList the new list to wrap.
   */
  protected void setList(List<TItem> wrapList) {
    this.list = wrapList;
  }
  //</editor-fold>
}
