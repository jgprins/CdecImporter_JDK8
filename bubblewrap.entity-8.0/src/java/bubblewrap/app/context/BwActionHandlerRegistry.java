package bubblewrap.app.context;

import java.util.Collection;
import java.util.HashMap;

/**
 * An HashMap class for managing the ctionHandler registration in 
 * {@linkplain BwAppContext}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class BwActionHandlerRegistry extends HashMap<Class, BwActionHandlerEntry> {

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwActionHandlerRegistry() {
    super();
  }
  //</editor-fold>

  /**
   * Method to assign an actionHandler Class registry entry for actionId and baseClass
   * to the BwActionHandlerRegistry.   *
   * @param <TBase> the base class' generic reference
   * @param baseClass the registry class' base class
   * @param actionHandler the actionHandler class.
   * @param actionId the class' unique actionId
   */
  @SuppressWarnings("unchecked")
  public <TBase> void put(Class<TBase> baseClass,
          Class<? extends TBase> actionHandler, String actionId) {
    BwActionHandlerEntry<TBase> pClassReg = null;
    if (this.containsKey(baseClass)) {
      pClassReg = this.get(baseClass);
    }

    if (pClassReg == null) {
      pClassReg = new BwActionHandlerEntry<>();
    }

    pClassReg.put(actionId, actionHandler);
    this.put(baseClass, pClassReg);
  }

  /**
   * Check if the registry contains a Class regsitryId for the specified base class.   *
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @param actionId the regsitrId to check for.
   * @return true if a valid registry exists.
   */
  public <TBase> boolean contains(Class<TBase> baseClass, String actionId) {
    boolean bResult = false;
    BwActionHandlerEntry entry = null;
    if (this.containsKey(baseClass)) {
      entry = this.get(baseClass);
    }

    if (entry != null) {
      bResult = entry.containsKey(actionId);
    }
    return bResult;
  }

  /**
   * Get the registered class for the specified base class and resgitryId.
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @param actionId the regsitrId to check for.
   * @return the registered class or null if not found.
   */
  @SuppressWarnings("unchecked")
  public <TBase> Class<? extends TBase> get(Class<TBase> baseClass, String actionId) {
    Class<? extends TBase> result = null;
    BwActionHandlerEntry entry = null;
    if ((this.containsKey(baseClass)) && ((entry = this.get(baseClass)) != null)) {
      result = entry.get(actionId);
    }
    return result;
  }

  /**
   * Get a the classes that are registered for the specified base class.
   * @param <TBase> the base class' generic reference
   * @param baseClass the base class to check for
   * @return a collection of registered classes or null is none has been registered
   */
  @SuppressWarnings("unchecked")
  public <TBase> Collection<Class<? extends TBase>> getClasses(Class<TBase> baseClass) {
    Collection<Class<? extends TBase>> result = null;
    BwActionHandlerEntry entry = null;
    if (this.containsKey(baseClass)) {
      entry = this.get(baseClass);
    }

    if (entry != null) {
      result = entry.values();
    }

    return result;
  }
}
