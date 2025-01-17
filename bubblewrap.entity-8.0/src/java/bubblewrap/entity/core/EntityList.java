package bubblewrap.entity.core;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * A ArrayList containing EntityWrappers of a generic Class&lt;TWrapper&gt;
 * @author kprins
 */
public class EntityList<TWrapper extends EntityWrapper> extends ArrayList<TWrapper>{
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(EntityList.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  
  /**
   * Public Constructor
   */
  public EntityList() {
    super();
  }
  // </editor-fold>
}
