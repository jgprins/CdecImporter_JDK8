package gov.ca.water.cdec.facades;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.importers.ImportUtils;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.springframework.transaction.annotation.Transactional;

/**
 * An Abstract Facade Class for used by all entities Facades
 * @author kprins
 */
public abstract class CdecBaseFacade<TBean extends Serializable> implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(CdecBaseFacade.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Static Field(s)">
  /**
   * Get the Default CDEC Time Zone (PST)
   */
  public static final TimeZone CdecTimeZone = TimeZone.getTimeZone("PST");
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Method">
  /**
   * Called to initiate or access a StateFul Facade that a shared within the session.
   * @param <T >the Entity bean class reference
   * @param <V> the CdecBaseFacade reference
   * @param facadeClass the facade class
   * @return the CdecBaseFacade of type V
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable, V extends CdecBaseFacade<T>> V 
                                                        getFacade(Class<V> facadeClass) {
    V result = null;
    String classKey = null;
    try {
      classKey = facadeClass.getSimpleName().toLowerCase();
//      FacesContext facesCtx = FacesContext.getCurrentInstance();
//      CdecEJBContext ejbCtx = null;
//      if (facesCtx != null) {
//        ExternalContext extCtx = facesCtx.getExternalContext();
//        Map sessionMap = extCtx.getSessionMap();
//        if (sessionMap.containsKey(classKey)) {
//          result = (V) sessionMap.get(classKey);
//        } else {
//          // Get the Class's Parameterless constructor
//          try {
//            result =
//                 (V) InitialContext.doLookup("java:module/"+facadeClass.getSimpleName());
////            if (result == null) {
////              throw new Exception("InitialContext.doLookup["+facadeClass.getSimpleName()+
////                      "] failed.");
////            }
//          } catch (Exception pEx1) {
//            logger.log(Level.INFO, "CdecBaseFacade.lookup[java:module/{0}] failed.\n"
//                    + "Reason: {1}", new Object[]{classKey, pEx1.getMessage()});
//          }
//          
//          if (result == null) {
//            try {
//              result = (V) facadeClass.newInstance();
//            } catch (Exception pEx1) {
//              logger.log(Level.INFO, "CdecBaseFacade.class[{0}].newInstance failed.\n"
//                    + "Reason: {1}", new Object[]{classKey, pEx1.getMessage()});
//            }
//          }
//          
//          if (result != null) {
//            sessionMap.put(classKey,result);
//          }
//        }
//      } else 
      CdecEJBContext ejbCtx = CdecEJBContext.getInstance();
      if (ejbCtx != null) {
        result = ejbCtx.getFacade(facadeClass);
      } else {
        try {
          result = (V) facadeClass.newInstance();
        } catch (Exception pEx1) {
          logger.log(Level.INFO, "CdecBaseFacade.class[{0}].newInstance failed.\n"
                + "Reason: {1}", new Object[]{classKey, pEx1.getMessage()});
        }
      }
    } catch (Exception pExp) {
      result = null;
      logger.log(Level.SEVERE, "SessionHelper.getFacade[{0}] Error: \n{1}",
              new Object[]{classKey, pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>  
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the facade's Entity Class reference
   */
  private Class<TBean> entityClass;
  /**
   * 
   */
  private transient EntityTransaction transaction;
  /**
   * Flag indicating if the persistent unit use JTA transaction
   * (default = null|true)
   */
  private Boolean useJTA;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor with a Entity Class reference
   * @param entityClass the Entity Class reference
   */
  public CdecBaseFacade(Class<TBean> entityClass) {
    this.entityClass = entityClass;
    this.useJTA = null;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Core Public Facade Methods">
  /**
   * Get the flag indicating if the persistent unit use JTA transaction
   * @return ((this.useJTA == null) || (this.useJTA))
   */
  public boolean doUseJTA() {
    return ((this.useJTA == null) || (this.useJTA));
  }
  
  /**
   * Set the flag indicating if the persistent unit use JTA transaction.
   * Default = true|null.
   * @param useJTA true|false|null
   */
  public void setUseJTA(Boolean useJTA) {
    this.useJTA = ((useJTA == null) || (useJTA))? null: useJTA;
  }
  
  /**
   * Rollback the EntityManager's current transaction - ignored if this.doUseJTA
   */
  public void beginTransaction() {
    EntityManager entMngr = null;
    if ((!this.doUseJTA()) && ((entMngr = this.getEntityManager()) != null)) {
      this.transaction = entMngr.getTransaction();
      if (this.transaction != null) {
         this.transaction.begin();
      }
    }
  }
  
  /**
   * Commit the EntityManager's current transaction - ignored if this.doUseJTA
   */
  public void commitTransaction() {
    EntityManager entMngr = null;
    if ((!this.doUseJTA()) && ((entMngr = this.getEntityManager()) != null)) {
      this.transaction = entMngr.getTransaction();
      if (this.transaction != null) {
         this.transaction.commit();
      }
      this.transaction = null;
    }
  }
  
  /**
   * Rollback the EntityManager's current transaction - ignored if this.doUseJTA
   */
  public void rollbackTransaction() {
    EntityManager entMngr = null;
    if ((!this.doUseJTA()) && ((entMngr = this.getEntityManager()) != null)) {
      this.transaction = entMngr.getTransaction();
      if (this.transaction != null) {
         this.transaction.rollback();
      }
      this.transaction = null;
    }
  }
  
  /**
   * Call to create a new entity in the underlying table
   * @param entity 
   */
  @Transactional
  public void create(TBean entity) throws Exception {
    try {
      if (entity == null) {
        throw new Exception("The Entity is null.");
      }
      
      EntityManager entMngr = this.getEntityManager();
      if (entMngr == null) {
        throw new Exception("The EntityManage for PU[" + CdecEJBContext.PU_CDEC 
                          + "] is not accessible.");
      }
      entMngr.persist(entity);
    } catch (Throwable exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".create Error:\n " + exp.getMessage());
    }
  }
  
  /**
   * Called to submit and updated Entity to the database
   * @param entity the entity to update
   */
  @Transactional
  public void edit(TBean entity) {
    if (entity == null) {
      try {
        EntityManager entMngr = this.getEntityManager();
        if (entMngr == null) {
          throw new Exception("The EntityManage for PU[" + CdecEJBContext.PU_CDEC 
                            + "] is not accessible.");
        }
        entMngr.merge(entity);
      } catch (Throwable exp) {
        throw new IllegalArgumentException(this.getClass().getSimpleName()
                + ".edit Error:\n " + exp.getMessage());
      }
    }
  }
  
  /**
   * Called to delete the specified entity from the database
   * @param entity the entity to update - ignored if null
   */
  @Transactional
  public void remove(TBean entity) {
    if (entity != null) {
      try {
        EntityManager entMngr = this.getEntityManager();
        if (entMngr == null) {
          throw new Exception("The EntityManage for PU[" + CdecEJBContext.PU_CDEC 
                            + "] is not accessible.");
        }
        entMngr.remove(entMngr.merge(entity));
      } catch (Throwable exp) {
        throw new IllegalArgumentException(this.getClass().getSimpleName()
                + ".edit Error:\n " + exp.getMessage());
      }
    }
  }
  
  /**
   * Called to retrieve a Entity using its primary Key
   * @param id the primary key value
   * @return the located record or null if id=null or not found.
   */
  @SuppressWarnings("unchecked")
  @Transactional
  public TBean find(Object id) {
    TBean result = null;
    EntityManager entMngr = null;
    if ((id != null) && ((entMngr = this.getEntityManager()) != null)) {
      result = (TBean) entMngr.find(this.entityClass, id);
    }
    return result;
  }
  
  /**
   * Find all the records in the underlying table
   * @return list of record (can be null|empty
   */
  @Transactional
  public List<TBean> findAll() {
    List<TBean> result = null;
    EntityManager entMngr = this.getEntityManager();
    CriteriaQuery cq = null;
    Query qry = null;
    if ((entMngr != null) &&
        ((cq = entMngr.getCriteriaBuilder().createQuery()) != null)) {
      cq.select(cq.from(this.entityClass));
      if ((qry = this.getEntityManager().createQuery(cq)) != null) {
        result = qry.getResultList();
      }
    }
    return result;
  }
    
  /**
   * Find a range of values the records in the underlying table
   * @param the range of records [firstRec..lastRec]
   * @return list of record (can be null|empty
   */
  @Transactional
  public List<TBean> findRange(int[] range) {
    List<TBean> result = null;
    EntityManager entMngr = this.getEntityManager();
    CriteriaQuery cq = null;
    Query qry = null;
    if ((entMngr != null) &&
        ((cq = entMngr.getCriteriaBuilder().createQuery()) != null)) {
      cq.select(cq.from(this.entityClass));
      if ((qry = this.getEntityManager().createQuery(cq)) != null) {
        qry.setMaxResults(range[1] - range[0]);
        qry.setFirstResult(range[0]);
        result = qry.getResultList();
      }
    }
    return result;
  }
  
  /**
   * Get the number of records in the underlying table.
   * @return the count (0 if empty)
   */
  @Transactional
  public int count() {
    int result = 0;
    EntityManager entMngr = this.getEntityManager();
    CriteriaQuery cq = null;
    Root<TBean> rt = null;
    Query qry = null;
    if ((entMngr != null) &&
        ((cq = entMngr.getCriteriaBuilder().createQuery()) != null) &&
        ((rt = cq.from(this.entityClass)) != null)) {
      cq.select(entMngr.getCriteriaBuilder().count(rt));
      if ((qry = this.getEntityManager().createQuery(cq)) != null) {
        result = ((Long) qry.getSingleResult()).intValue();
      }
    }
    return result;
  }
  
  /**
   * Called to create a NamedQuery by its name
   * @param qryName NamedQuery's name
   * @return the query or null if not found or the entityManager = null.
   */
  public Query getNamedQry(String qryName) {
    Query result = null;
    if (qryName != null) {
      if ((qryName = qryName.trim()).length() == 0) {
        qryName = null;
      }
    }
    if (qryName == null) { 
      throw new NullPointerException("The NamedQuery's name cannot be unassigned.");
    }
    EntityManager entMng = this.getEntityManager();   
    if (entMng == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    return entMng.createNamedQuery(qryName);
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Custom Query">
  /**
   * Called to execute a data load query with a custom defined SQL <tt>querySql</tt>.
   * The assigned <tt>delegate</tt> should be designed to process the query's returned
   * result set. 
   * <p>
   * <b>NOTE:</b> If {@linkplain #_DEBUG_ON} = true, all query or data processing errors
   * will be trapped and logged. Otherwise, all errors are ignored</p>
   * @param querySql the custom SQL string
   * @param delegate the delegate to process the queries result set.
   */
  @Transactional
  public void excuteQuery(String querySql, final SqlQueryDelegate delegate) {
    if (delegate == null) {
      throw new NullPointerException("The SqlQueryDelegate is not defined");
    }
    
    final String sql = ImportUtils.cleanString(querySql);
    if (sql == null) {
      throw new NullPointerException("The query's SQL statement cannot be empty.");
    }
    
    EntityManager entMng = this.getEntityManager();    
    if (entMng == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    
    Query qry = entMng.createQuery(sql);
    if (qry == null) {
      throw new IllegalArgumentException("Unable to initiate Query[" + sql + "].");
    }
    
    try {      
      /* Assign the Query Parameters */
      delegate.assignParameters(qry);
      
      /* Execute the Query */
      List<Object> qryResult = qry.getResultList();
      if ((qryResult == null) || (qryResult.isEmpty())) {
        throw new Exception("The result list from Query is empty. "
                + "Query SQL = " + qry.toString());
      }
      
      delegate.loadQuery(qryResult);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.excuteQuery Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called to execute a data load query with a custom defined SQL <tt>querySql</tt>.
   * The assigned <tt>delegate</tt> should be designed to process the query's returned
   * result set. 
   * @param <TResult> depends on the query. It can be type TBean is the entire record is
   * returned on a array of values if the results a subset if TBean or a joined view 
   * recordset.
   * @param namedQuery the name of the {@linkplain NamedQuery} (defined as annotation to
   * the Entity class of type TBean
   * @param delegate the delegate to process the queries result set.
   */
  @Transactional
  public <TResult extends Serializable> void excuteQuery(String namedQuery, 
                                          final NamedQueryDelegate<TResult> delegate) {
    if (delegate == null) {
      throw new NullPointerException("The NamedQueryDelegate is not defined");
    }
    
    if ((namedQuery = ImportUtils.cleanString(namedQuery)) == null) {
      throw new NullPointerException("The NamedQuery's name cannot be undefined");
    }
    
    EntityManager entMng = this.getEntityManager();    
    if (entMng == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    
    Query qry = entMng.createNamedQuery(namedQuery);
    if (qry == null) {
      throw new IllegalArgumentException("Unable to access NamedQuery[" 
                                                                    + namedQuery + "].");
    }
    
    try {
      /* Assign the Query Parameters */
      delegate.assignParameters(qry);
      /* Execute the Query */
      List<TResult> qryResult = qry.getResultList();
      if (qryResult == null) {
        throw new Exception("The result list from NamedQuery[" + namedQuery 
                + "] = null. Query SQL = " + qry.toString());
      }
      
      delegate.loadQuery(qryResult);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.excuteQuery Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Public Merge Methods">
  /**
   * This method is called during a merge process if target records that are not in the
   * source record set (<tt>srcMap</tt>) should be removed from the underlying table.
   * <p>
   * <b>NOTE:</b> Always call mergeRemove before call {@linkplain #merge(
   * java.util.HashMap, java.util.HashMap, gov.ca.water.cdec.core.EntityMergeDelegate) 
   * this.merge} to avoid unique constraint violations.</p>
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param trgMap the target map of records to update
   */
  public <TKey extends Serializable, TSrc> void mergeRemove(HashMap<TKey, TSrc> srcMap, 
                                                       HashMap<TKey, TBean> trgMap) {
    if ((srcMap == null) || (trgMap == null) || (trgMap.isEmpty())) {
      return;
    }
    EntityManager entMng = this.getEntityManager();   
    if (entMng == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    try {
      this.beginTransaction();
      List<TKey> allKeys = new ArrayList<>(trgMap.keySet());
      for (TKey trgKey : allKeys) {
        if ((trgKey != null) && (!srcMap.containsKey(trgKey))) {
          TBean bean = trgMap.get(trgKey);
          if (bean != null) {
            try {
              trgMap.remove(trgKey);
              this.remove(bean);
              logger.log(Level.INFO, "Remove Record {0}", trgKey.toString());
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.mergeRemove[key={1}] Error:\n {2}",
              new Object[]{this.getClass().getSimpleName(), trgKey.toString(),
                           exp.getMessage()});
            }
          }
        }
      }
      /* Flush EntityManager to commit changes */
      if ((!this.doUseJTA()) && (entMng != null) && (entMng.isJoinedToTransaction())) {
        entMng.flush();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.mergeRemove Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});      
      this.rollbackTransaction();
      throw new IllegalArgumentException(exp);
    } finally {
      this.commitTransaction();
    }
  }
  
  /**
   * This method is called to merge two sets of data: <tt>srcMap</tt> containing new
   * records not in the existing database and <tt>trgMap</tt> containing existing records
   * retrieved from the existing database.
   * <p>
   * All records in the srcMap that are not in the trgMap will be inserted as new
   * records, calling the <tt>delegate's</tt> {@linkplain EntityMergeDelegate#newMerge(
   * java.io.Serializable) newMerge} method to initiate the new record.
   * <p>
   * All records in the srcMap that are in the trgMap will be updates if the records
   * has changed. the <tt>delegate's</tt> {@linkplain EntityMergeDelegate#updateMerge(
   * java.io.Serializable, java.io.Serializable) updateMerge} method is called to
   * check if the record has changed and if true to update the target record and then
   * return true - to indicate that the update record must be submitted to the database.
   * <p>
   * <b>NOTE:</b> To prevent unique constraint violations, all record updates are 
   * completed before the new records are inserted.</p>
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param trgMap the target map of records to update
   * @param delegate the Merge Delegate to initiate new records or to update existing
   * records.
   * @throws IllegalArgumentException is the process failed.
   */
  public <TKey extends Serializable, TSrc> void
          merge(HashMap<TKey, TSrc> srcMap, HashMap<TKey, TBean> trgMap,
            EntityMergeDelegate<TKey, TSrc, TBean> delegate) {
    if ((srcMap == null) || (srcMap.isEmpty()) || (trgMap.isEmpty()) ||
        (delegate == null)) {
      return;
    }
    EntityManager entMng = this.getEntityManager();   
    if (entMng == null) {
      throw new NullPointerException("The Facade's EntityManager is not accessible.");
    }
    try {
      this.beginTransaction();
      List<TBean> insertList = new ArrayList<>();
      TKey key = null;
      TSrc source = null;
      TBean target = null;
      for (Map.Entry<TKey, TSrc> e : srcMap.entrySet()) {
        if (((key = e.getKey()) == null) || ((source  = e.getValue()) == null)) {
          continue;
        }
        
        if (trgMap.containsKey(key)) {
          if ((target = trgMap.get(key)) != null) {  
            try {
              if (delegate.updateMerge(source, target)) {
                this.edit(target);
              }
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.Merge.Update[key={1}] Error:\n {2}",
                      new Object[]{this.getClass().getSimpleName(), key.toString(),
                        exp.getMessage()});
            }
          }
        } else {
          if ((target = delegate.newMerge(key, source)) != null) {
            insertList.add(target);
          }
        }
      }   
      
      /* Flush EntityManager to synchronize changes */
      if ((!this.doUseJTA()) && (entMng != null) && (entMng.isJoinedToTransaction())) {
        entMng.flush();
      }
      
      if (!insertList.isEmpty()) {
        for (TBean bean : insertList) {
          if (bean != null) {
            try {            
              this.create(bean);
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.Merge.Insert[{1}] Error:\n {2}",
                      new Object[]{this.getClass().getSimpleName(), bean.toString(),
                        exp.getMessage()});
            }
          }
        }
      }
      
      /* Flush EntityManager to synchronize changes */
      if ((!this.doUseJTA()) && (entMng != null) && (entMng.isJoinedToTransaction())) {
        entMng.flush();
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.merge Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      this.rollbackTransaction();
      throw new IllegalArgumentException(exp);
    } finally {
      this.commitTransaction();
    }
  }
          
  /**
   * This method is called to merge a new set of record (<tt>srcMap</tt>) with all 
   * existing records of type TBean using a <tt>mapDelegate</tt> - to convert all 
   * existing records to a target map - and a <tt>mergeDelegate</tt> - to assist with
   * initiating new records or update the existing records. The process is skipped if 
   * <tt>srcMap</tt> = null|Empty.
   * <p>
   * It calls {@linkplain #findAll() this.findAll} to retrieve all existing records and
   * the use the <tt>mapDelegate</tt> to convert the existing records to a target Map.
   * It then calls {@linkplain #merge(java.util.HashMap, java.util.HashMap, 
   * gov.ca.water.cdec.core.EntityMergeDelegate) this.merge(srcMap, trgMap, 
   * mergeDelegate)} to handle the merging of the records.
   * <p>
   * The process will fail is generating the target Map fails. All errors are trapped and
   * logged.
   * @param <TKey> a common primary key to match the source and target records
   * @param <TSrc> the source class (does not have to be an entity of type TBean)
   * @param srcMap the source map of records
   * @param mapDelegate the delegate for converting the existing record to a target Map
   * @param mergeDelegate the Merge Delegate to initiate new records or to update existing
   * records.
   * @param removeMissing remove all records in the existing record set that are not in
   * the <tt>srcMap</tt>
   * @throws IllegalArgumentException is the process failed.
   */
  public <TKey extends Serializable, TSrc> void
        mergeAll(HashMap<TKey, TSrc> srcMap, MapperDelegate<TKey, TBean> mapDelegate,
        EntityMergeDelegate<TKey, TSrc, TBean> mergeDelegate, boolean removeMissing) {
    if ((srcMap == null) || (srcMap.isEmpty())) {
      return;
    }
    
    try {
      if (mapDelegate == null) {
        throw new Exception("The Mapper Delegate to convert the existing records to a "
                + "target map is not specified.");
      }  
      if (mergeDelegate == null) {
        throw new Exception("The Entity Merge Delegate to assist with merging the source "
                + "and target maps is not specified.");
      }  
      
      HashMap<TKey, TBean> trgMap = new HashMap<>();
      List<TBean> trgList = this.findAll();
      if ((trgList != null) && (!trgList.isEmpty())) {
        mapDelegate.toMap(trgList, trgMap);
      }
      
      if (removeMissing) {
        this.mergeRemove(srcMap, trgMap);
      }
      
      this.merge(srcMap, trgMap, mergeDelegate);      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.mergeAll Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      throw new IllegalArgumentException(exp);
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * Get the injected EJB Entity Manager to use by the facade
   * @return the Facade's EntityManager
   */
  protected abstract EntityManager getEntityManager();
  /**
   * ABSTRACT: Set the Facades EnityManage (called by the {@linkplain CdecEJBContext}
   * @param em 
   */
  protected abstract void setEntityManager(EntityManager em);
  //</editor-fold>
}
