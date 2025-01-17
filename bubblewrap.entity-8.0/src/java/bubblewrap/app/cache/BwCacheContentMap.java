package bubblewrap.app.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Map object used by {@linkplain BwAppCache} to manage cached content internally.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class BwCacheContentMap extends HashMap<BwCacheKey, BwCacheContent> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public BwCacheContentMap() {
    super();  
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Methods">
  /**
   * Get the <tt>key</tt> converted to a DateMapKey for searching on a Map key. It
   * supports the following cases:<ul>
   * <li>key is instance of BwCacheKey: return key; and</li>
   * <li>key is instance of Serializable: return new BwCacheKey(key), and </li>
   * </li>
   * </ul>
   * @param key the input
   * @return the converted BwCacheKey or null if not supported.
   */
  private BwCacheKey getCacheKey(Object key) {
    BwCacheKey result = null;
    if (key != null) {
      if (key instanceof BwCacheKey) {
        result = (BwCacheKey) key;
      } else if (key instanceof Serializable) {
        Serializable inst = (Serializable) key;
        result = new BwCacheKey(inst);
      }
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to check each cache content item in the BwCacheContentMap and is expired, 
   * remove the expired content from the map.
   * @return this.isEmpty()
   */
  protected boolean executeGC() {
    if (!this.isEmpty()) {
      List<BwCacheContent> contentList = new ArrayList<>(this.values());
      for (BwCacheContent content : contentList) {
        if (content.isExpired()) {
          if (this.containsKey(content.cacheKey)) {
            super.remove(content.cacheKey);
          }
        }
      }
    }
    return this.isEmpty();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override HashMap">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: If the content is found call its {@linkplain BwCacheContent#resetExpired()
   * resetExpired} method - to postpone the removal of the content from the cache.</p>
   * <p>
   * <b>NOTE:</b> The input key is converted to a BwCacheKey and can be either a instance
   * of BwCacheKey or and instance of Serializable</p>
   */
  @Override
  public BwCacheContent get(Object key) {
    BwCacheContent result = null;
    BwCacheKey cacheKey = this.getCacheKey(key);    
    if ((cacheKey != null) && (super.containsKey(cacheKey)) && 
                                              ((result = super.get(cacheKey)) != null)) {
      result.resetExpired();
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <b>NOTE:</b> The input key is converted to a BwCacheKey and can be either a instance
   * of BwCacheKey or and instance of Serializable</p></p>
   */
  @Override
  public boolean containsKey(Object key) {
    boolean result = false;
    BwCacheKey cacheKey = this.getCacheKey(key);    
    if (cacheKey != null) {
      result = super.containsKey(cacheKey);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * <b>NOTE:</b> The input key is converted to a BwCacheKey and can be either a instance
   * of BwCacheKey or and instance of Serializable</p></p>
   */
  @Override
  public BwCacheContent remove(Object key) {
    BwCacheContent result = null;
    BwCacheKey cacheKey = this.getCacheKey(key);    
    if ((cacheKey != null) && (super.containsKey(cacheKey))) {
      result = super.remove(cacheKey);
    }
    return result;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: "BwCacheContentMap[" + this.size() + "]"</p>
   */
  @Override
  public String toString() {
    return "BwCacheMap[" + this.size() + "]";
  }
  // </editor-fold>
}
