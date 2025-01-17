package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

/**
 * Extends EntityValueFilter that represents a single NUMERIC value.
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @author kprins
 */
public class EntityNumberFilter<V extends Serializable, TValue extends Number> 
                                                      extends EntityValueFilter<V,TValue> {

// <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Parameterless Constructor for cloning
   */
  protected EntityNumberFilter() {
    super();
  }

  /**
   * Constructor 1: Specify the Filter's Field Name and eCond:EntityFilterEnums. The
   * Filter value is initially set to null and can be set using the setValue method.
   * Excepts only Condition EQUAL, NOTEQUAL, GREATER, GREATEROREQUAL, LESS, LESSOREQUAL,
   * IN, or NOTIN.  Default = EQUAL. 
   * Calls Constructor 2 with pValue=null
   * @param sFieldname String
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityNumberFilter(String sFieldname, int eCond) throws Exception {
    this(sFieldname, null, eCond);
  }

  /**
   * Constructor2: Specify the Filter's Field Name, the pValue (type Y) and the
   * eCond:EntityFilterEnums. pValue can be null Excepts only Condition EQUAL,
   * NOTEQUAL, GREATER, GREATEROREQUAL, LESS, LESSOREQUAL, IN, or NOTIN. 
   * Default = EQUAL.    
   * @param pValue Y
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityNumberFilter(String sFieldname, TValue pValue, int eCond) 
          throws Exception {
    super(sFieldname, pValue, eCond, (EntityFilterEnums.EQUAL
            | EntityFilterEnums.GREATER | EntityFilterEnums.LESS | EntityFilterEnums.IN
            | EntityFilterEnums.NOT));
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="EntityFilter Overrides">
  /**
   * Return true if the Filter Definition is set
   * @return
   */
  @Override
  public boolean isSet() {
    return (this.fieldName != null);
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return the Predicate to add to the query based on the 
   * sFieldName, pValue, and eCond set in constructor for.</p><ul>
   * <li>
   * If this.value=null & this.values=null, return a isNull or isNotNull predicate,
   *  according  to the the set (EQUAL | NOTEQUAL) Filter Condition.</li>
   * <li> If this.value!=null, support Filter Conditions: 
   * (EQUAL | NOTEQUAL |GREATER | GREATEROREQUAL | LESS | LESSOREQUAL | IN | NOTIN). </li>
   * <li> If this.values!=null, support Filter Conditions: 
   * (IN | NOTIN). </li>
   * </ul> 
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
                getPredicate(From<Tx, Ty> pForm, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (pForm == null) || (pCb == null)) {
      return null;
    }
    Predicate pResult = null;
    Expression<TValue> pPath = pForm.get(this.fieldName);
    if ((this.filterValue == null) && (this.valueList == null)) {
      if ((this.condition & EntityFilterEnums.NOTEQUAL) == EntityFilterEnums.NOTEQUAL) {
        pResult = pCb.isNotNull(pPath);
      } else {
        pResult = pCb.isNull(pPath);
      }
    } else if (this.filterValue != null) {
      if (this.condition == EntityFilterEnums.EQUAL) {
        pResult = pCb.equal(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.NOTEQUAL) {
        pResult = pCb.notEqual(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.GREATER) {
        pResult = pCb.gt(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.GREATEROREQUAL) {
        pResult = pCb.ge(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.LESS) {
        pResult = pCb.lt(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.LESSOREQUAL) {
        pResult = pCb.le(pPath, this.filterValue);
      } else if (this.condition == EntityFilterEnums.IN) {
        pResult = pPath.in(this.filterValue);
      } else if (this.condition == EntityFilterEnums.NOTIN) {
        pResult = pCb.not(pPath.in(this.filterValue));
    } else {
        throw new Exception("Not currently supported");
      }
    } else if (this.valueList != null) {
      if (this.condition == EntityFilterEnums.IN) {
        pResult = pPath.in(this.valueList);
      } else if (this.condition == EntityFilterEnums.NOTIN) {
        pResult = pCb.not(pPath.in(this.valueList));
      } else {
        throw new Exception("Not currently supported");
      }
    }
    return pResult;
  }
// </editor-fold>
}
