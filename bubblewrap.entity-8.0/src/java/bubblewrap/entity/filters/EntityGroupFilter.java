package bubblewrap.entity.filters;

import bubblewrap.entity.interfaces.IEntityFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

/**
 * Implementation of the IEntityFilter that represents a grouping of one or many
 * sub-filters. These filters are join by either an AND or OR clause. If this filter
 * or any of its sub-filters are set to be distinct, it will return a distinct
 * result set. NOTE: if bAnd=true (Joining using a AND clause), this.isDistinct=false
 * unless a sub-filter is set to this.isDistinct=true.
 * @author kprins
 */
public class EntityGroupFilter<TBean extends Serializable> extends EntityFilter<TBean> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private boolean mbDistinct = true;
  private boolean mbAnd = true;
  private List<IEntityFilter> mpFilters = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protected Constructor for Cloning -
   * calls Constructor 3 with bAnd=true and bDistinct=true.
   */
  public EntityGroupFilter() {
    this(true,true);
  }

  /**
   * Constructor 2: Constructor with bAnd=true to bind filters with AND or bAnd=false to
   * bind filters with OR. Call Constructor 3 with bDistinct=true.
   * @param bAnd
   */
  public EntityGroupFilter(boolean bAnd) {
    this(bAnd,true);
  }

  /**
   * Constructor 3: Create a new GroupFilter with bAnd=true to bind filters with AND or
   * bAnd=false to bind filters with OR. this.isDistinct=true if (bAnd=false) and
   * (bDistinct=true). If this.isDistinct=true the results set will be
   * unique, otherwise it could contain return duplicates. (Not possible with and
   * AND joint clause.
   * @param bAnd boolean
   * @param bDistinct boolean
   */
  public EntityGroupFilter(boolean bAnd, boolean bDistinct) {
    super();
    this.mbAnd = bAnd;
    this.mpFilters = new ArrayList<>();
    this.mbDistinct = (bAnd)? false: bDistinct;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Add a additional filter to the group. If pFilter.isDistinct, the will set the
   * GroupFilter.isDistinct=true.
   * @param pFilter
   */
  public void addFilter(IEntityFilter<TBean> pFilter) {
    if (pFilter != null) {
      this.mpFilters.add(pFilter);
      if ((!this.mbDistinct) && (pFilter.isDistinct())) {
        this.mbDistinct = true;
      }
    }
  }

  /**
   * Get the number of filters added to the GroupFilter
   * @return int
   */
  public int size() {
    return (this.mpFilters == null) ? 0 : this.mpFilters.size();
  }

  /**
   * Return the Filter at iIndex. Throws an IndexOutOfBoundsException if the index is
   * not valid.
   * @param <V>
   * @param iIndex int
   * @return EntityFilter<V>
   */
  @SuppressWarnings("unchecked")
  public <V extends Serializable> EntityFilter<V> getFilter(int iIndex) {
    EntityFilter<V> pFilter = null;
    try {
      pFilter = (EntityFilter<V>) this.mpFilters.get(iIndex);
    } catch (Exception pExp) {
      throw new IndexOutOfBoundsException(this.getClass().getSimpleName()
              + ".getFilter Error:\n " + pExp.getMessage());
    }
    return pFilter;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="EntityFilter Overrides">
  /**
   * Return true if the Filter Definition is set
   * @return
   */
  @Override
  public boolean isSet() {
    return ((this.mpFilters != null) && (!this.mpFilters.isEmpty()));
  }

  /**
   * Return true whether the Filter must return a distinct set of results
   * (default = true)
   * @return boolean
   */
  @Override
  public boolean isDistinct() {
    return this.mbDistinct;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Always return false (Group Filter has no assigned value)
   * </p>
   */
  @Override
  public boolean isValueFilter() {
    return false;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return the Predicate to add to the query a list of all 
   * sub-filters linked by either AND or OR conditions.</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
              getPredicate(From<Tx, Ty> pForm, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (pForm == null) || (pCb == null)) {
      return null;
    }
    
    Predicate pResult = null;
    List<Predicate> pSubFilters = new ArrayList<>();
    for (IEntityFilter pFilter : this.mpFilters) {
      Predicate pPred = pFilter.getPredicate(pForm, pCb);
      if (pPred != null) {
        pSubFilters.add(pPred);
      }
    }

    if (!pSubFilters.isEmpty()) {
      if (pSubFilters.size() == 1) {
        pResult = pSubFilters.get(0);

      } else {
        Predicate[] pPredArr = pSubFilters.toArray(new Predicate[0]);
        if (this.mbAnd) {
          pResult = pCb.and(pPredArr);
        } else {
          pResult = pCb.or(pPredArr);
        }
      }
    }

    return pResult;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Clone the instance and deep clone each filter in the filterGroup.</p>
   * @param pTarget EntityFilter (of type EntityGroupFilter)
   */
  @Override
  protected void AssignTo(EntityFilter pTarget) {
    if ((pTarget != null) && (pTarget instanceof EntityGroupFilter)) {
      EntityGroupFilter pFilter = (EntityGroupFilter) pTarget;
      pFilter.mbDistinct = this.mbDistinct;
      pFilter.mbAnd = this.mbAnd;
      if ((this.mpFilters != null) && (!this.mpFilters.isEmpty())) {
        for (IEntityFilter pSubFilter: this.mpFilters) {
          if ((pSubFilter != null) && (pSubFilter instanceof EntityFilter)) {
            try {
              EntityFilter pNewFilter = ((EntityFilter) pSubFilter).clone();
              this.mpFilters.add(pNewFilter);
            } catch (CloneNotSupportedException pExp) {
              logger.log(Level.SEVERE, "{0} Error:\n\r {1}", 
                      new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
            }
          }
        }
      }
    }
  }
  // </editor-fold>
}
