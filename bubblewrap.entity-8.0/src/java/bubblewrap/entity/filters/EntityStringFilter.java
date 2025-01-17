package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

/**
 * Extends EntityValueFilter that represents a single STRING value
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @author kprins
 */
public class EntityStringFilter<V extends Serializable> 
                                  extends EntityValueFilter<V, String> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for flag controlling whether searches is case sensitive
   * (mbCheckCase=true) or not.
   */
  private boolean mbCheckCase = false;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Parameterless Constructor for cloning
   */
  protected EntityStringFilter() {
    super();
  }

  /**
   * Constructor: Specify the Filter's Field Name, the pValue and the
   * eCond:EntityFilterEnums. pValue can be null. Excepts only Condition EQUAL,
   * NOTEQUAL, LIKE, NOTLIKE. Default = EQUAL. Search is assumed to be Case-insensitive.
   * Calls Constructor 4 with pDoCase=false.
   * @param sFieldname String
   * @param pValue String
   * @param eCond int (EntityFilterEnums)
   * @throws Exception
   */
  public EntityStringFilter(String sFieldname, String pValue, int eCond)
          throws Exception {
    this(sFieldname, pValue, eCond, false);
  }

  /**
   * Constructor 2: Specify the Filter's Field Name, and the eCond:EntityFilterEnums.
   * pValue is initially set to be null and can be set using the setValue method.
   * Search is assumed to be Case-insensitive.
   * Excepts only Condition EQUAL, NOTEQUAL, LIKE, NOTLIKE. Default = EQUAL
   * Calls Constructor 4 with pValue=null and pDoCase=false.
   * @param sFieldname String
   * @param eCond int (EntityFilterEnums)
   * @throws Exception
   */
  public EntityStringFilter(String sFieldname, int eCond) throws Exception {
    this(sFieldname, null, eCond, false);
  }

  /**
   * Constructor 3: Specify the Filter's Field Name, the eCond:EntityFilterEnums, and the
   * bDoCase flag. pValue is initially set to be null and can be set using the setValue
   * method. Excepts only Condition EQUAL, NOTEQUAL, LIKE, NOTLIKE. Default = EQUAL.
   * Calls Constructor 4 with pValue=null.
   * @param sFieldname String
   * @param eCond int (EntityFilterEnums)
   * @param bDoCase Boolean
   * @throws Exception
   */
  public EntityStringFilter(String sFieldname, int eCond, Boolean bDoCase)
          throws Exception {
    this(sFieldname, null, eCond, bDoCase);
  }

  /**
   * Constructor 4: Specify the Filter's Field Name, the pValue, the eCond:
   * EntityFilterEnums and a Flag specifying whether the searc is case sensiive or not.
   * pValue can be null. It Excepts only eCond[EQUAL, NOTEQUAL, LIKE, NOTLIKE].
   * Default = EQUAL. Default bDoChase=false - (i.e., case in-sensitive querying).
   * @param sFieldname String
   * @param pValue String
   * @param eCond int (EntityFilterEnums)
   * @param bDoCase Boolean
   * @throws Exception
   */
  public EntityStringFilter(String sFieldname, String pValue, int eCond,
          Boolean bDoCase) throws Exception {
    super(sFieldname, pValue, eCond, (EntityFilterEnums.EQUAL | EntityFilterEnums.LIKE
           | EntityFilterEnums.NOT | EntityFilterEnums.LESS | EntityFilterEnums.GREATER
            | EntityFilterEnums.IN ));
    this.mbCheckCase = ((bDoCase != null) && (bDoCase));
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
   * {@inheritDoc} <p>OVERRIDE: Return the predicate for a filter search based on Filter
   * Conditions (EQUAL|NOTEQUAL|LIKE|NOTLIKE). If this.value=null, return a isNull or
   * isNotNull predicate, according to the the set Filter Condition.</p>
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
              getPredicate(From<Tx, Ty> fromRoot, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (fromRoot == null) || (pCb == null)) {
      return null;
    }
    Expression<String> pPath = fromRoot.get(this.fieldName);
    if (!this.mbCheckCase) {
      pPath = pCb.lower(pPath);
      pPath = pCb.trim(pPath);
    }

    Predicate pResult = null;
    if ((this.filterValue == null) && (this.valueList == null)) {
      if ((this.condition & EntityFilterEnums.EQUAL) == EntityFilterEnums.EQUAL) {
        pResult = pCb.isNull(pPath);
      } else {
        pResult = pCb.isNotNull(pPath);
      }
    } else if (this.filterValue != null){
      String sValue = (this.mbCheckCase)? this.filterValue: this.filterValue.toLowerCase();
      if (this.condition == EntityFilterEnums.EQUAL) {
        pResult = pCb.equal(pPath, sValue);
      } else if (this.condition == EntityFilterEnums.NOTEQUAL) {
        pResult = pCb.notEqual(pPath, sValue);
      } else if (this.condition == EntityFilterEnums.LIKE) {
        pResult = pCb.like(pPath, sValue);
      } else if (this.condition == EntityFilterEnums.NOTLIKE) {
        pResult = pCb.notLike(pPath, sValue);
      } else if (this.condition == EntityFilterEnums.IN) {
        pResult = pPath.in(sValue);
      } else if (this.condition == EntityFilterEnums.NOTIN) {
        pResult = pCb.not(pPath.in(sValue));
      } else {
        throw new Exception("Not currently supported");
      }
    } else {
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

  /**
   * OVERRIDE: Assign All properties - except the Filter value.
   * @param pTarget EntityFilter (of type EntityStringFilter)
   */
  @Override
  protected void AssignTo(EntityFilter pTarget) {
    super.AssignTo(pTarget);
    if ((pTarget != null) && (pTarget instanceof EntityStringFilter)) {
      EntityStringFilter pFilter = (EntityStringFilter) pTarget;
      pFilter.mbCheckCase = this.mbCheckCase;
    }
  }
  // </editor-fold>
}
