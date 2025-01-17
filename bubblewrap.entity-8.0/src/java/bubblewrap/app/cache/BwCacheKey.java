package bubblewrap.app.cache;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * A HashMap key for caching a specified class' content in the {@linkplain BwCacheMap}
 * map. The key can be IDed by a matching BwCacheKey or a matching recordId.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class BwCacheKey implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The placeholder for the Cached Content's unique record id.
   */
  private Serializable recordId;
  /**
   * The class of the recordId (extends Serializable)
   */
  private Class<? extends Serializable> recIdClass;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with only recordId reference - the content will be cached until 
   * the application shuts down.
   * <b>NOTE:</b> The recordId class should override the {@linkplain 
   * Object#equals(java.lang.Object) equals} method to ensure that the BwCacheKey
   * can be identify by a recordId</p>
   * @param recordId the unique record ID of the data to be cached.
   */
  public BwCacheKey(Serializable recordId) {
    super();
    if ((this.recordId = recordId) == null) {
      throw new NullPointerException("The BwCacheKey's recordId cannot be unassigned.");
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Call the super method before resetting this.recIdClass and this.recordId
   * </p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize(); 
    this.recIdClass = null;
    this.recordId = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * The CacheKey's unique recordId reference
   * @return the assigned recordId
   */
  public Serializable getRecordId() {
    return this.recordId;
  }
  
  /**
   * The CacheKey's unique recordId reference
   * @param otherClass
   * @return true if <tt>otherClass</tt> matched this.recIdClass
   */
  public boolean isRecordType(Class<? extends Serializable> otherClass) {
    return (DataEntry.isEq(this.recIdClass, otherClass));
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return a hashCode based on this.recordId</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + Objects.hashCode(this.recordId);
    return hash;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return true if obj != null, is Serializable, an instance of BwCacheKey
   * and the recordIds match, or obj.class = this.recordId.class and the this.recordId =
   * obj.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof Serializable));
    if (result) {
      Serializable serialObj = null;
      if (obj instanceof BwCacheKey) {
        BwCacheKey other = (BwCacheKey) obj;
        result = DataEntry.isEq(this.recordId, other.recordId);
      } else if (((serialObj = (Serializable) obj) != null) && 
                        (this.isRecordType(serialObj.getClass()))) {
        result = DataEntry.isEq(this.recordId, serialObj);
      } else {
        result = false;      
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "CacheKey[ recId=" + this.recordId + "; type="
   * + this.recIdClass.getSimpleName() + "]."</p>
   */
  @Override
  public String toString() {    
    return "CacheKey[ recId=" + this.recordId + "; type=" 
                                            + this.recIdClass.getSimpleName() + "].";
  }
  // </editor-fold>
}
