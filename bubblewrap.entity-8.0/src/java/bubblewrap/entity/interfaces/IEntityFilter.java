package bubblewrap.entity.interfaces;

import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Join;

/**
 * Interface for EntityFilter. Defining the base methods that should be supported by
 * all EntityFilters
 * @author kprins
 */
public interface IEntityFilter<TBean extends Serializable> extends Serializable {
  /**
   * ABSTRACT: Return true if the Filter must return a distinct (unique) set of results.
   * @return boolean
   */
  public boolean isDistinct();
  /**
   * ABSTRACT: Return true if the Filter is set
   * @return boolean
   */
  public boolean isSet();
  /**
   * ABSTRACT: returns true if this is a value filter
   * @return boolean
   */
  public boolean isValueFilter();
  /**
   * ABSTRACT: Called by the FacadeHelper or other objects to initiate the filter
   * instance.
   * @param root Root<V>
   * @param criteria CriteriaBuilder
   * @param query CriteriaQuery
   * @throws Exception
   */
  public void setFilter(Root<TBean> root, CriteriaBuilder criteria,
          CriteriaQuery query) throws Exception;
  /**
   * ABSTRACT: Overload 1: Get the predicate to call to apply the filter based on an 
   * assigned {@linkplain Root} implementation of interface {@linkplain From} 
   * @param root the Root from which to retrieve the Filter Expression/Path
   * @param criteria the CriteriaBuilder to use for generating the Predicate
   * @return the Predicate for the filter condition or null if none is set.
   * @throws Exception
   */
  public Predicate getPredicate(Root<TBean> root, CriteriaBuilder criteria) 
                                                                      throws Exception;
  
  /**
   * ABSTRACT: Overload 2: Get the predicate to call to apply the filter based on an 
   * assigned {@linkplain From}, which can be a  {@linkplain Root} or {@linkplain Join}.
   * @param <Tx> The return root class
   * @param <Ty> The local filter root class.
   * @param fromRoot the Root or Join from which to retrieve the Filter Expression/Path
   * @param criteria the CriteriaBuilder to use for generating the Predicate
   * @return the Predicate for the filter condition or null if none is set.
   * @throws Exception
   */
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
         getPredicate(From<Tx, Ty> fromRoot, CriteriaBuilder criteria) throws Exception;
}
