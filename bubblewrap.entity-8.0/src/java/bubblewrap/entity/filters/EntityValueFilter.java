package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/** 
 * A generic EntityFilter to retrieve Entity Bean Class[V] using a value of
 * Class[Y extends Object]. 
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @author kprins
 */
public class EntityValueFilter<TBean extends Serializable, TValue extends Object> 
        extends EntityFilter<TBean> {

  // <editor-fold defaultstate="collapsed" desc="Protected Common Fields">
  protected int conditionOptions = (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT);
  protected int condition = EntityFilterEnums.EQUAL;
  protected String fieldName = null;
  protected TValue filterValue = null;
  protected List<TValue> valueList;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Protected Constructor for Cloning
   */
  protected EntityValueFilter() {
    super();
    this.fieldName = null;
    this.filterValue = null;
    this.valueList = null;
  }

  /**
   * Constructor 1: Specify the Filter's Field Name, the pValue and the
   * eCond:EntityFilterEnums. pValue can be null Excepts only Condition EQUAL,
   * and NOTEQUAL. Default = EQUAL
   * Calls Constructor 4 with eCond=(EQUAL|NOTEQUAL).
   * @param sFieldname String
   * @param pValue Y
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityValueFilter(String sFieldname, TValue pValue, int eCond) throws Exception {
    this(sFieldname, pValue, eCond, 
            (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT));
  }

  /**
   * Constructor 3: Specify the Filter's Field Name, and the eCond:EntityFilterEnums
   * without a preset value. pValue is initially set to null and can be set using the
   * setValue methods. Excepts only Condition EQUAL and NOTEQUAL. Default = EQUAL
   * Calls Constructor 4 with pValue=null, eCond=(EQUAL|NOTEQUAL), and bDistinct=true.
   * @param sFieldname String
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityValueFilter(String sFieldname, int eCond) throws Exception {
    this(sFieldname, null, eCond, 
            (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT
            | EntityFilterEnums.IN ));
  }

  /**
   * Constructor 3: Specify the Filter's Field Name, the eCond:EntityFilterEnums, and
   * and the eCondFilter (EntityFilterEnums) without a preset value. pValue is initially
   * set to null and can be set using the setValue methods. Default = EQUAL.
   * Calls Constructor 4 with pValue=null.
   * @param sFieldname String
   * @param eCond EntityFilterEnums
   * @throws Exception
   */
  public EntityValueFilter(String sFieldname, int eCond, int eCondFilter)
          throws Exception {
    this(sFieldname, null, eCond, eCondFilter);
  }

  /**
   * Constructor 4: Protected Constructor called by inheritors:
   * Specify the Filter's Field Name, the pValue, the eCond:EntityFilterEnums and
   * bDistinct. pValue can be null Excepts only Condition specified by eCondFilter.
   * Default = EQUAL.
   * @param fieldName String
   * @param value Y
   * @param filterCondition EntityFilterEnums
   * @param eCondFilter EntityFilterEnums
   * @param bDistinct boolean;
   * @throws Exception
   */
  protected EntityValueFilter(String fieldName, TValue value, int filterCondition,
          int eCondFilter) throws Exception {
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new Exception("The Filter's FieldName cannot be unassigned");
    }
    this.conditionOptions = eCondFilter;
    this.fieldName = fieldName.trim();
    this.filterValue = value;
    filterCondition = (filterCondition & this.conditionOptions);
    if (filterCondition != 0) {
      this.condition = filterCondition;
    }
  }
// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="ValueFilter Properties">
  /**
   * Get the Filter Conditions
   * @return EntityFilterEnums
   */
  public int getCondition() {
    return this.condition;
  }

  /**
   * Get the Filter's Supported Condition Filter (i.e. of all the supported conditions)
   * @return EntityFilterEnums
   */
  public int getConditionFilter() {
    return this.conditionOptions;
  }

  /**
   * Get the Filter Field Name
   * @return String
   */
  public String getFieldname() {
    return this.fieldName;
  }

  /**
   * Get the Filter Value (can be null for some filter classes)
   * @return Y
   */
  public TValue getValue() {
    return this.filterValue;
  }

  /**
   * Set the Filter Value (can be null for some filter classes)
   * @param value Y
   */
  public void setValue(TValue value) {
    this.filterValue = value;
  }
  
  /**
   * Get the assigned list of filter values (to be sued with an IN clause)
   * @return the assigned list (or null)
   */
  public List<TValue> getValues() {
    return this.valueList;
  }

  /**
   * Set the Filter Value (can be null for some filter classes)
   * @param pValue the list of values to filter on. Set to null if empty.
   */
  public void setValues(List<TValue> values) {
    this.valueList = ((values == null) || (values.isEmpty()))? null: values;
  }
// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="IEntityFilter Implementation">
  /**
   * Return false, because ValueFiletrs will always return a unique result set.
   * @return boolean
   */
  @Override
  public boolean isDistinct() {
    return false;
  }
  /**
   * Always return true - this is a value filter
   * @return boolean
   */
  @Override
  public final boolean isValueFilter() {
    return true;
  }

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
   * <p>IMPLEMENTATION: CAN OVERRIDE: Called by {@linkplain EntityJoinFilter} or this 
   * instance's {@linkplain #getPredicate(javax.persistence.criteria.Root, 
   * javax.persistence.criteria.CriteriaBuilder) Overload1} to retrieve the Predicate
   * for the set filter conditions.</p> 
   * <p>The base method set the filter conditions for the generic value type (TValue) 
   * and the EQUAL|NOT filter conditions. If (this.value=null), it returns a isNull or 
   * isNotNull Predicate, depending on the filter Condition.</p>
   * @param <Tx> The return root class
   * @param <Ty> The local filter root class.
   * @param fromRoot the Root or Join from which to retrieve the Filter Expression/Path
   * @param criteriaBuilder the CriteriaBuilder to use for generating the Predicate
   * @return the Predicate for the filter condition or null if none is set.
   * @throws Exception 
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> 
            Predicate getPredicate(From<Tx,Ty> fromRoot, CriteriaBuilder criteriaBuilder) 
            throws Exception {  
    if ((!this.isSet()) || (fromRoot == null) || (criteriaBuilder == null)) {
      return null;
    }
    
    Predicate pResult = null;    
    Expression<TValue> pPath = fromRoot.get(this.fieldName);    
    if (pPath != null) {
      if ((this.filterValue == null) && (this.valueList == null)) {
        if ((this.condition & EntityFilterEnums.NOTEQUAL) == EntityFilterEnums.NOTEQUAL) {
          pResult = criteriaBuilder.isNotNull(pPath);
        } else {
          pResult = criteriaBuilder.isNull(pPath);
        }
      } else if (this.filterValue != null) {
        if (this.condition == EntityFilterEnums.NOTEQUAL) {
          pResult = criteriaBuilder.notEqual(pPath, this.filterValue);
        } else if (this.condition == EntityFilterEnums.EQUAL) {
          pResult = criteriaBuilder.equal(pPath, this.filterValue);
        } else if (this.condition == EntityFilterEnums.IN) {
          pResult = pPath.in(this.filterValue);
        } else if (this.condition == EntityFilterEnums.NOTIN) {
          pResult = criteriaBuilder.not(pPath.in(this.filterValue));
        } else {
          throw new Exception("Not currently supported");
        }
      } else if (this.valueList != null) {
        if (this.condition == EntityFilterEnums.IN) {
          pResult = pPath.in(this.valueList);
        } else if (this.condition == EntityFilterEnums.NOTIN) {
          pResult = criteriaBuilder.not(pPath.in(this.valueList));
        }
      }
    }
    return pResult;
  }


  /**
   * {@inheritDoc} <p>OVERRIDE: Assign All properties - except the Filter value.
   * Ignore if pTarget=null or (!instanceof EntityValueFilter)</p>
   */
  @Override
  protected void AssignTo(EntityFilter pTarget) {
    if ((pTarget != null) && (pTarget instanceof EntityValueFilter)) {
      EntityValueFilter pFilter = (EntityValueFilter) pTarget;
      pFilter.condition = this.condition;
      pFilter.conditionOptions = this.conditionOptions;
      pFilter.fieldName = this.fieldName;
    }
  }
// </editor-fold>
}
