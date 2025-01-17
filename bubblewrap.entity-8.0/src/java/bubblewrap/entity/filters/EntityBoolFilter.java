package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Extends EntityValueFilter that represents a single BOOLEAN value.
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @see #getPredicate(javax.persistence.criteria.Root, 
 * javax.persistence.criteria.CriteriaBuilder) 
 * @author kprins
 */
public class EntityBoolFilter<V extends Serializable> 
                                              extends EntityValueFilter<V,Boolean> {

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Parameterless Constructor for cloning
   */
  protected EntityBoolFilter() {
    super();
  }

  /**
   * Constructor: Specify the Filter's Field Name and the eCond:EntityFilterEnums. It
   * calls the next overload with pValue=false. Excepts only Condition EQUAL,
   * and NOTEQUAL. This.isDistinct=false, because this filter does not
   * support partial filtering.
   * @see #getPredicate(javax.persistence.criteria.Root, 
   * javax.persistence.criteria.CriteriaBuilder) 
   * @param sFieldname String
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityBoolFilter(String sFieldname, int eCond) throws Exception {
    this(sFieldname, Boolean.FALSE, eCond); 
  }

  /**
   * Constructor: Specify the Filter's Field Name, the pValue and the
   * eCond:EntityFilterEnums. pValue can be True|False. Excepts only Condition EQUAL,
   * and NOTEQUAL. Default = EQUAL. This.isDistinct=false, because this filter does not
   * support partial filtering.
   * @see #getPredicate(javax.persistence.criteria.Root, 
   * javax.persistence.criteria.CriteriaBuilder) 
   * @param sFieldname String
   * @param pValue boolean
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityBoolFilter(String sFieldname, boolean pValue, int eCond) 
            throws Exception {
    super(sFieldname, pValue, eCond, 
            (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT));
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
   * {@inheritDoc }
   * <p>OVERRIDE: Return the Predicate to add to the query based on the sFieldName, 
   * pValue, and eCond set in constructor</p>
   * <p><b>NOTE:</b> If the filter field is not a required field, the field can have 
   * three possible values True|False|null. The filter's assigned value are used as the
   * default value, which is paired with. This give us the following cases:</p><ul>
   * <li>if this.value=false and eCond=EQUAL the filter= "WHERE (isNull(Field) or 
   * (Field=false))" and for eCond=NOTEQUAL the filter="WHERE (Field=true)".</li> 
   * <li>If this.value=true and eCond=EQUAL the filter= "WHERE (isNull(Field) or 
   * (Field=true))" and for eCond=NOTEQUAL the filter="WHERE (Field=false)".</li>
   * </ul> 
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
               getPredicate(From<Tx, Ty> pForm, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (pForm == null) || (pCb == null)) {
      return null;
    }
    
    Predicate pResult = null;
    Expression<Boolean> pPath = pForm.get(this.fieldName);
    Boolean pValue = ((this.filterValue != null) && (this.filterValue));
    Boolean bIsTrue = true;
    if (this.condition == EntityFilterEnums.EQUAL) {
      bIsTrue = pValue;
    } else if (this.condition == EntityFilterEnums.NOTEQUAL) {
      bIsTrue = (!pValue);
    } else {
      throw new Exception("Filter Condition["+Integer.toString(this.condition) 
              + "] is not supported");
    }
    
    if (bIsTrue) {
      if (pValue) {
        pResult = pCb.or(pCb.isNull(pPath), pCb.isTrue(pPath));
      } else {
        pResult = pCb.isTrue(pPath);
      }
    } else {
      if (pValue) {
        pResult = pCb.or(pCb.isNull(pPath), pCb.isFalse(pPath));
      } else {
        pResult = pCb.isFalse(pPath);
      }
    }
    return pResult;
  }
  // </editor-fold>
}
