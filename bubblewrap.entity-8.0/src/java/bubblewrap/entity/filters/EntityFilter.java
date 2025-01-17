package bubblewrap.entity.filters;

import bubblewrap.entity.interfaces.IEntityFilter;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;


/**
 * An Abstract implementation of IEntityFilter
 * @author kprins
 */
public abstract class EntityFilter<TBean  extends Serializable> implements 
                                                      IEntityFilter<TBean> {

  //<editor-fold defaultstate="collapsed" desc="Protected Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(EntityFilter.class.getName());
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  protected EntityFilter() {
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Implement IEntityFilter">
  /**
   * Called to assign the CriteriaQuery's where clause based on the defined
   * filter. if (any input is null or the filter is not set, it will return
   * null). If any of the sub-filters are not set, it will be ignored.
   * NOTE: (a) There is only one filter per query. Individual query  conditions can
   * be grouped if an 'AND' or 'OR' group filter. (b)
   * @param pEntClass Class
   * @param pCb CriteriaBuilder
   * @param pCq CriteriaQuery
   * @throws Exception
   */
  @Override
  @SuppressWarnings("unchecked")
  public void setFilter(Root pRoot, CriteriaBuilder pCb,
          CriteriaQuery pCq) throws Exception {
    if ((!this.isSet()) || (pRoot == null) || (pCb == null)
            || (pCq == null)) {
      return;
    }

    Predicate pPred = this.getPredicate(pRoot, pCb);
    if (pPred != null) {
      pCq.where(pPred);
    }
  }
  
  /**
   * {@inheritDoc } 
   * <p>IMPLEMENTATION: This overload cast the Root<TBean> as a Form<TBean, TBean> and 
   * call {@linkplain #getPredicate(javax.persistence.criteria.From, 
   * javax.persistence.criteria.CriteriaBuilder) Overload 2}. <u>This method should not 
   * be overridden</u>. It is only used when called directly from the FacadeHelper or 
   * a EntityGroupFilter when TBean is the root of the CriteriaBuilder</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public Predicate getPredicate(Root<TBean> pRoot, CriteriaBuilder pCb) 
                                                          throws Exception {
    if ((!this.isSet()) || (pRoot == null) || (pCb == null)) {
      return null;
    }
    
    From<TBean,TBean> fromRoot = pRoot;
    return this.getPredicate(fromRoot, pCb);
  }  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Cloning">
  /**
   * Generic Clone = requires a parameterless protected constructor
   * @return EntityFilter
   * @throws CloneNotSupportedException
   */
  @Override
  public EntityFilter clone() throws CloneNotSupportedException {
    EntityFilter result = null;
    try {
      result = this.getClass().newInstance();
      this.AssignTo(result);
    } catch (Exception pExp) {
      throw new CloneNotSupportedException(this.getClass().getSimpleName()
              + ".clone Error:\n " + pExp.getMessage());
    }
    return result;
  }

  /**
   * ABSTRACT: override to assign this instance field values to pTarget - used for
   * cloning.
   * @param pTarget
   */
  protected abstract void AssignTo(EntityFilter pTarget);
  // </editor-fold>
}
