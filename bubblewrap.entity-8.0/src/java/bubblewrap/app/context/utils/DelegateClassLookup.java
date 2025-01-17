package bubblewrap.app.context.utils;

import bubblewrap.app.context.BwAppContext;
import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Lookup Utility for looking up the delegate class for a baseClass. This utility can
 * use to lookup a static reference to a base class. Example:<br/>
 * <p>{@code 
 *  public final static Class<? extends MyBaseClass> DelegateClass = 
 *  DelegateClassLookup.doLookup(MyBaseClass.class, false);
 * }
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class DelegateClassLookup<TBaseClass> implements Serializable {
  
  /**
   * A Public Static Lookup function, which will initiate a DelegateClassLookup instance
   * and return the {@linkplain #getDelegateClass() delegate class}.
   * @param <TClass> the type of the base class
   * @param baseClass the base class for which to lookup the delegate
   * @param canBeBase the flag indicating that the delegate class can be the base class.
   * @return the delegate class.
   */
  public final static <TClass> Class<? extends TClass> doLookup(Class<TClass> baseClass, 
                                                           boolean canBeBase) {
    DelegateClassLookup<TClass> lookup = new DelegateClassLookup<>(baseClass, canBeBase);
    if (lookup == null) {
      throw new NullPointerException("The DelegateClassLookup could not be initiated.");
    }
    return lookup.getDelegateClass();
  }
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(DelegateClassLookup.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Base Class
   */
  public final Class<TBaseClass> baseClass;
  /**
   * The registered Delegate Class
   */
  private Class<? extends TBaseClass> delegateClass;
  /**
   * The registered Delegate Class can be the baseClass
   */
  private Boolean canBeBase;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor 
   * @param baseClass the base class for which to lookup the delegate
   * @param canBeBase the flag indicating that the delegate class can be the base class.
   */
  public DelegateClassLookup(Class<TBaseClass> baseClass, boolean canBeBase) {
    super();  
    if ((this.baseClass = baseClass) == null) {
      throw new IllegalArgumentException("The DelegateClass Lookup's baseClass cannot be "
              + "undefined.");
    }
    this.delegateClass = null;
    this.canBeBase = (!canBeBase)? null: true;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the registered delegate class for the specified baseClass. 
   * @return the cached this.delegateClass.
   * @throws NullPointerException if the delegate class is not accessible, 
   * (!this.canBeBaseClass) and (delegateClass = baseClass), or if (delegateClass is an
   * abstract class).
   */
  public final Class<? extends TBaseClass> getDelegateClass() {
    if (this.delegateClass == null) {
      Class<? extends TBaseClass> delClass = null;
      try {
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new Exception("The Application Context is not accessible.");
        }
        delClass = appCtx.getDelegateClass(this.baseClass);
        if (delClass == null) {
          throw new Exception("The delegate class for Class[" 
                  + this.baseClass.getSimpleName() + "] is not defined or accessible.");
        } if ((!this.canBeBaseClass()) && (delClass.equals(this.baseClass))) {
          throw new Exception("The delegate class for Class[" 
                  + this.baseClass.getSimpleName() + "] is not defined or accessible.");
        } else if (Modifier.isAbstract(delClass.getModifiers())) {
          throw new Exception("Class[" + delClass.getSimpleName() 
                  + "] - the delegate for Class[" + this.baseClass.getSimpleName() 
                  + "] is an abstract class.");
        }
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}[{1}].getDelegateClass Error:\n {2}",
                new Object[]{this.getClass().getSimpleName(), this.baseClass, 
                            exp.getMessage()});
        throw new NullPointerException(exp.getMessage());
      }
    }
    return this.delegateClass;
  }
  
  /**
   * Get whether the delegate class can be the base class.
   * @return true if the baseClass is allowed.
   */
  public boolean canBeBaseClass() {
    return ((this.canBeBase != null) && (this.canBeBase));
  }
  
  /**
   * Called to reset the cached this.delegate reference.
   */
  public final void reset() {
    this.delegateClass = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: this.getClass().getSimpleName() + "[ baseClass=" + 
   * this.baseClass.getSimpleName() + "]."</p>
   */
  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[ baseClass=" 
            + this.baseClass.getSimpleName() + "].";
  }
  // </editor-fold>
}
