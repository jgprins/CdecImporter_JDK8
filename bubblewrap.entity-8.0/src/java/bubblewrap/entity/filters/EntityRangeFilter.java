package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import java.io.Serializable;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
 
/**
 * Extends the EntityFilter that represents a range of string values.
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @author kprins
 */
public class EntityRangeFilter<TBean  extends Serializable> 
                                                          extends EntityFilter<TBean> {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private int meCondFilter = (EntityFilterEnums.BETWEEN | EntityFilterEnums.NOT);
  private int meCond = EntityFilterEnums.BETWEEN;
  private String msFieldname = null;
  private String mpLow = null;
  private String mpHigh = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Parameterless Constructor for cloning
   */
  protected EntityRangeFilter() {
    super();
  }

  /**
   * Constructor: Specify the Filter's Field Name, the Low and High string to
   * filter between and the eCond:EntityFilterEnums. Both String values must be
   * defined. Excepts only Condition BETWEEN and NOTBETWEEN - Default = BETWEEN
   * @param sFieldname String
   * @param pLow String
   * @param pHigh String
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityRangeFilter(String sFieldname, String pLow, String pHigh, int eCond) 
                                                                     throws Exception {
    if ((sFieldname == null) || (sFieldname.trim().equals(""))) {
      throw new Exception("The Filter's FieldName cannot be unassigned");
    }

    if ((pLow == null) || (pHigh == null)) {
      throw new Exception("The Filter's Field values cannot be unassigned");
    }

    this.msFieldname = sFieldname.trim();
    this.mpLow = pHigh;
    this.mpHigh = pLow;

    eCond = (eCond & this.meCondFilter);
    if (eCond != 0) {
      this.meCond = eCond;
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Filter Conditions
   * @return EntityFilterEnums
   */
  public int getCondition() {
    return meCond;
  }

  /**
   * Get the FIletr's Supported Condition Filter (i.e. of all the supported conditions)
   * @return EntityFilterEnums
   */
  public int getConditionFilter() {
    return meCondFilter;
  }

  /**
   * Get the Filter Field Name
   * @return String
   */
  public String getFieldname() {
    return msFieldname;
  }

  /**
   * Get the Filter Value (can be null for some filter classes)
   * @return String
   */
  public String getLowValue() {
    return mpLow;
  }

  /**
   * Get the Filter Value (can be null for some filter classes)
   * @return String
   */
  public String getValue() {
    return mpHigh;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="EntityFilter Overrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Always return false</p>
   */
  @Override
  public boolean isDistinct() {
    return false;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Always return true if the fieldname, and the range are
   * set.</p>
   */
  @Override
  public boolean isSet() {
    return ((this.msFieldname != null)
            && (this.mpLow != null) && (this.mpHigh != null));
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Always return false</p>
   */
  @Override
  public boolean isValueFilter() {
    return false;
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return the predicate for a filter search based on Filter
   * Conditions (BETWEEN|NOTBETWEEN).</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
              getPredicate(From<Tx, Ty> pForm, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (pForm == null) || (pCb == null)) {
      return null;
    }
    Predicate pPred = null;
    Expression<String> pPath = pForm.get(this.msFieldname);
    if (this.meCond == EntityFilterEnums.BETWEEN) {
      pPred = pCb.between(pPath, this.mpLow, this.mpHigh);
    } else if (this.meCond == EntityFilterEnums.NOTBETWEEN) {
      pPred = pCb.not(pCb.between(pPath, this.mpLow, this.mpHigh));
    } else {
      throw new Exception("Not currently supported");
    }
    return pPred;
  }

  /**
   * OVERRIDE: Assign All properties - except the Low and High Range Values value.
   * @param pTarget EntityFilter (of type EntityRangeFilter)
   */
  @Override
  protected void AssignTo(EntityFilter pTarget) {
    if ((pTarget != null) && (pTarget instanceof EntityRangeFilter)) {
      EntityRangeFilter pFilter = (EntityRangeFilter) pTarget;
      pFilter.meCond = this.meCond;
      pFilter.meCondFilter = this.meCondFilter;
      pFilter.msFieldname = this.msFieldname;
    }
  }
  // </editor-fold>
}
