package bubblewrap.core.interfaces;

import bubblewrap.entity.context.PuEntityManager;
import java.io.Serializable;
import java.util.List;

/**
 * An interface that identify the PackageLoader as an Entity PackageLoader. It has a
 * reference to the Entities 
 * @author kprins
 */
public interface IEntityLoader {
  /**
   * Get a reference to the Entities' PuEntityManager class
   * @return return the assigned class
   */
  public Class<? extends PuEntityManager> getEntityManagerClass();
  
  /**
   * Get a list of Entity Classes
   * @return a list of classes
   */
  public List<Class> getClasses();
}
