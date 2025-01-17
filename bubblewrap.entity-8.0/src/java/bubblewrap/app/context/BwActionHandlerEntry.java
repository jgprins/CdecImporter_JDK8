package bubblewrap.app.context;

import java.util.HashMap;

/**
 * A HashMap class for registering a actionId-actionHandler pair used by the {@linkplain 
 * BwActionHandlerRegistry} 
 * @param <TBase>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwActionHandlerEntry<TBase> extends HashMap<String, Class<? extends TBase>> {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">

  /**
   * Public Constructor
   */
  public BwActionHandlerEntry() {
    super();
  }
  //</editor-fold>

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Convert sKey a lower case string before calling the supr method.</p>
   */
  @Override
  public Class<? extends TBase> put(String key, Class<? extends TBase> value) {
    key = key.toLowerCase();
    return super.put(key, value);
  }

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Convert pKey to a lowercase string before calling the super method.
   * </p>
   */
  @Override
  public Class<? extends TBase> get(Object key) {
    String sKey = (String) key;
    sKey = sKey.toLowerCase();
    return super.get(sKey);
  }

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Convert pKey to a lowercase string before calling the super method.
   * </p>
   */
  @Override
  public boolean containsKey(Object key) {
    String sKey = (String) key;
    sKey = sKey.toLowerCase();
    return super.containsKey(key);
  }
}