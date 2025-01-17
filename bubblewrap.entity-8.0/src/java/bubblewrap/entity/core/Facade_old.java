package bubblewrap.entity.core;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;

/**
 * The abstract class for all Facade classes. have a reference to the FacadeHelper
 * and include the Task-Action Access Validation event handling.
 * @author kprins
 */
public class Facade_old implements Serializable {
//  
//  // <editor-fold defaultstate="collapsed" desc="Static Field">
//  /**
//   * Static Error Logger for the Facade Class
//   */
//  protected static final Logger logger = Logger.getLogger(Facade.class.getSimpleName());
//// </editor-fold>
//
//  // <editor-fold defaultstate="collapsed" desc="Constructor">
//  /**
//   * Protected Constructor
//   */
//  protected Facade() {
//  }
//  // </editor-fold>
//
//  // <editor-fold defaultstate="collapsed" desc="Public methods">
//  /**
//   * Create/Insert the record after checking its recordId - Log any errors
//   * @param pEntity a Serializable instance of the supported class
//   * @throws Exception if the process fails
//   */
//  @SuppressWarnings("unchecked")
//  public boolean create(Serializable pEntity) throws Exception {
//    boolean bResult = false;
//    try {
//      if (pEntity == null) {
//        throw new Exception("The New Record cannot be unassigned.");
//      }
//      
//      if (!this.getHelper().getEntityClass().equals(pEntity.getClass())) {
//        throw new Exception("The New Record is not of Class[" 
//                                      + this.getHelper().getEntityClassName() + "].");
//      }
//      
//      this.getHelper().initRecordId(pEntity);
//      EntityManager pEm = this.getEntitymanager();
//      pEm.persist(pEntity);
//      bResult = true;
//    } catch (Exception pExp) {
//      logger.log(Level.SEVERE, "{0}.create Error:\n {1}", 
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
//    return bResult;
//  }
//
//  /**
//   * Save edits to the persistent data source
//   * @param pEntity a Serializable instance of the supported class
//   * @return the saved Serializable instance
//   * @throws Exception if the process fails
//   */
//  public Serializable edit(Serializable pEntity) throws Exception {
//    Serializable pResult = pEntity;
//    try {
//      if (pEntity == null) {
//        throw new Exception("The New Record cannot be unassigned.");
//      }
//      
//      if (!this.getHelper().getEntityClass().equals(pEntity.getClass())) {
//        throw new Exception("The New Record is not of Class[" 
//                                      + this.getHelper().getEntityClassName() + "].");
//      }
//      
//      EntityManager pEm = this.getEntitymanager();
//      pResult = pEm.merge(pEntity);
//      
//    } catch (Exception pExp) {
//      logger.log(Level.SEVERE, "{0}.edit Error:\n {1}", 
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
//    return pResult;
//  }
//
//  /**
//   * Remove the record from the persistent data source
//   * @param pEntity a Serializable instance of the supported class
//   * @throws Exception if the process fails
//   */
//  public void remove(Serializable pEntity) throws Exception {
//    try {
//      if (pEntity == null) {
//        throw new Exception("The New Record cannot be unassigned.");
//      }
//      
//      if (!this.getHelper().getEntityClass().equals(pEntity.getClass())) {
//        throw new Exception("The New Record is not of Class[" 
//                                      + this.getHelper().getEntityClassName() + "].");
//      }
//      EntityManager pEm = this.getEntitymanager();
//      pEm.remove(pEm.merge(pEntity));
//    } catch (Exception pExp) {
//      logger.log(Level.SEVERE, "{0}.edit Error:\n {1}", 
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
//  }
//
//  /**
//   * Refresh the Entity from the Database.
//   * @param pEntity a Serializable instance of the supported class
//   * @return the new retrieved Serializable instance
//   * @throws Exception if the process fails
//   */
//  public Serializable refresh(Serializable pEntity, Object pRecId) throws Exception {
//    Serializable pResult = pEntity;
//    try {
//      if (pEntity == null) {
//        throw new Exception("The New Record cannot be unassigned.");
//      }
//      
//      if (!this.getHelper().getEntityClass().equals(pEntity.getClass())) {
//        throw new Exception("The New Record is not of Class[" 
//                                      + this.getHelper().getEntityClassName() + "].");
//      }
//      
//      if (pEntity != null) {
//        EntityManager pEm = this.getEntitymanager();
//        pEm.detach(pEntity);
//      }
//
//      if (pRecId != null) {
//        FacadeHelper pHelper = this.getHelper();
//        pResult = pHelper.find(pRecId);
//      }
//    } catch (Exception pExp) {
//      logger.log(Level.SEVERE, "{0}.edit Error:\n {1}", 
//              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
//      throw pExp;
//    }
//    return pResult;
//  }
//
//  /**
//   * Flush the EntityManager
//   */
//  public void flush() {
//    EntityManager pEm = this.getEntitymanager();
//    pEm.flush();
//  }
//  // </editor-fold>
//
//  // <editor-fold defaultstate="collapsed" desc="Abstract Methods">
//  /**
//   * Abstract: Return reference to the Facade's Facade Helper
//   * @return IFacadeHelper
//   */
//  public abstract FacadeHelper getHelper();
//
//  /**
//   * ABSTRACT: return the Facade's EnityManager.
//   * @return
//   */
//  public abstract EntityManager getEntitymanager();
//  // </editor-fold>
}
