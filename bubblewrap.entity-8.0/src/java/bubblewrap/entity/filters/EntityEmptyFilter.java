package bubblewrap.entity.filters;

import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * An Extension of EntityFilter that represents an Empty filter. This is primarily users
 * be the SearchFilter and RecordsetSearch classes to distinguished between the results
 * from an unset sub-search and a empty search result.
 * @see RecordsetSearch#refreshSearch() 
 * @see SearchFilter#getFilter() 
 * @author kprins
 */
public class EntityEmptyFilter<V  extends Serializable> extends EntityFilter<V> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public EntityEmptyFilter() {
    super();  
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="IEntityFilter Implementation">
  /**
   * OVERRIDE: Return false
   * @return boolean
   */
  @Override
  public boolean isDistinct() {
    return false;
  }
  
  /**
   * OVERRIDE: return true
   * @return boolean
   */
  @Override
  public boolean isSet() {
    return true;
  }
  
  /**
   * OVERRIDE: Return true
   * @return boolean
   */
  @Override
  public boolean isValueFilter() {
    return true;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Always return null</p>
   */
  @Override
  public Predicate getPredicate(Root pRoot, CriteriaBuilder pCb) throws Exception {
    return null;
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Always return null</p>
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
              getPredicate(From<Tx, Ty> pForm, CriteriaBuilder pCb) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  /**
   * OVERRIDE: Does nothing
   * @param pTarget
   */
  @Override
  protected void AssignTo(EntityFilter pTarget) {
  }
  //</editor-fold>

}
