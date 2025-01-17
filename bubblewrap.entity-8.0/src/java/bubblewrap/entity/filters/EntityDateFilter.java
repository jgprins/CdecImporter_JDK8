package bubblewrap.entity.filters;

import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateRange;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.*;

/** 
 * A generic EntityFilter to retrieve Entity Bean Class[V] using a value type Date. 
 * This.isDistinct=false, because this filter will always return a unique result set.
 * @author kprins
 */
public class EntityDateFilter<TBean extends Serializable> extends EntityFilter<TBean> {

  // <editor-fold defaultstate="collapsed" desc="Protected Common Fields">
  private int conditionOptions = (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT);
  private int condition = EntityFilterEnums.EQUAL;
  private String fieldName;
  private DateTime filterValue;
  private List<DateTime> valueList;
  private DateRange valueRange;
  private Boolean distinct;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Protected Constructor for Cloning 
   */
  protected EntityDateFilter() {
    super();
    this.fieldName = null;
    this.filterValue = null;
    this.valueList = null;
    this.valueRange = null;
    this.distinct = null;
  }

  /**
   * Constructor 1: Specify the Filter's Field Name, the <tt>value</tt> and the
   * Entity Filter <tt>condition</tt>. value can be null. This constructor
   * only accept only <tt>condition</tt> EQUAL, NOTEQUAL, GREATER, GREATEROREQUAL,
   * LESS, LESSOREQUAL. Default = EQUAL.
   * @param fieldName The field name to filter on 
   * @param value the data value (can be null)
   * @param condition the {@linkplain EntityFilterEnums} filter conditions
   * @throws Exception
   */
  public EntityDateFilter(String fieldName, DateTime value, int condition) throws Exception {
    this(fieldName, value, condition, 
            (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT | 
                    EntityFilterEnums.GREATER | EntityFilterEnums.LESS));
  }
  
  /**
   * Constructor 2: Specify the Filter's Field Name, the <tt>value</tt> and the
   * Entity Filter <tt>condition</tt>. value can be null. This constructor
   * only accept only <tt>condition</tt> EQUAL, NOTEQUAL, GREATER, GREATEROREQUAL,
   * LESS, LESSOREQUAL. Default = EQUAL.
   * @param fieldName The field name to filter on 
   * @param valueRange a not null and not empty Date Range
   * @param inclusive true to filter BETWEEN valueRange.min and valueRange.max; false
   * to filter NOT BETWEEN valueRange.min and valueRange.max.
   * @throws Exception
   */
  public EntityDateFilter(String fieldName, DateRange valueRange, boolean inclusive) 
          throws Exception {
    this();    
    if ((this.fieldName = DataEntry.cleanString(fieldName)) == null) {
      throw new Exception("The Filter's FieldName cannot be unassigned");
    }
    if (((this.valueRange = valueRange) == null) || (this.valueRange.isEmpty())) {
      throw new Exception("The Filter's DateRange is unassigned or empty.");
    }
    
    this.conditionOptions = (EntityFilterEnums.BETWEEN | EntityFilterEnums.NOT);
    if (inclusive) {
      this.condition = EntityFilterEnums.BETWEEN;
    } else {
      this.condition = this.conditionOptions;
    }
    this.fieldName = fieldName.trim();
  }

  /**
   * Constructor 3: Specify the Filter's Field Name, and the Entity Filter 
   * <tt>condition</tt> without a preset value. The filter value is initially set to null 
   * and can be set using the {@linkplain #setValue(TValue) this.setValue}, {@linkplain 
   * #setValues(java.util.List) this.setValues}, or set
   * setValue methods. 
   * Calls Constructor 5 with value=null, conditionOptions=(EQUAL | NOTEQUAL,
   * IN | NOTIN | BETWEEN | NOTBETWEEN | GREATER | GREATEROREQUAL | LESS | LESSOREQUAL).
   * It excepts all EntityFilter Conditions  in <tt>conditionOptions</tt>. 
   * Default = EQUAL.
   * @param fieldname the filed name to filter on
   * @param condition the Entity Filter
   * @throws Exception
   */
  public EntityDateFilter(String fieldname, int condition) throws Exception {
    this(fieldname, null, condition, 
            (EntityFilterEnums.EQUAL | EntityFilterEnums.NOT |
             EntityFilterEnums.IN | EntityFilterEnums.BETWEEN | 
             EntityFilterEnums.GREATER | EntityFilterEnums.LESS));
  }

  /**
   * Constructor 4: Specify the Filter's Field Name, the eCond:EntityFilterEnums, and
   * and the eCondFilter (EntityFilterEnums) without a preset value. pValue is initially
   * set to null and can be set using the setValue methods. Default = EQUAL.
   * Calls Constructor 5 with pValue=null.
   * @param fieldName the filed name to filter on
   * @param condition the filter value (can be null)
   * @param conditionOptions a bitmap of the allowable filter conditions
   * @throws Exception
   */
  public EntityDateFilter(String fieldName, int condition, int conditionOptions)
                                                                    throws Exception {
    this(fieldName, null, condition, conditionOptions);
  }

  /**
   * Constructor 5: Protected Constructor called by inheritors:
   * Specify the Filter's Field Name, the <tt>value</tt>, the Entity Filter 
   * <tt>condition</tt>. The <tt>value</tt> can be null and it excepts only a
   * <tt>condition</tt> included by eCondFilter.
   * Default = EQUAL.
   * @param fieldName the filed name to filter on
   * @param value the filter value (can be null)
   * @param condition the Entity Filter
   * @param conditionOptions a bitmap of the allowable filter conditions
   * @param bDistinct boolean;
   * @throws Exception
   */
  protected EntityDateFilter(String fieldName, DateTime value, int condition,
          int conditionOptions) throws Exception {
    this();
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      throw new Exception("The Filter's FieldName cannot be unassigned");
    }
    this.conditionOptions = conditionOptions;
    this.fieldName = fieldName.trim();
    this.filterValue = value;
    condition = (condition & this.conditionOptions);
    if (condition != 0) {
      this.condition = condition;
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
  public int getConditionOptions() {
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
   * @return the assigned Date
   */
  public DateTime getValue() {
    return this.filterValue;
  }

  /**
   * Set the Filter Value (can be null for some filter classes)
   * @param value the Date to filter on
   */
  public void setValue(DateTime value) {
    if (value == null) {
      this.filterValue = null;
    } else if ((this.valueRange != null) || (this.valueList != null)) {
      throw new IllegalArgumentException("You cannot set the value after setting a "
              + "list of filter values or a date range.");
    } else {
      this.filterValue = value;
    }
  }
  
  /**
   * Get the assigned list of filter values (to be used with an IN | NOTIN condition)
   * @return the assigned list (or null)
   */
  public List<DateTime> getValues() {
    return this.valueList;
  }

  /**
   * Set the Filter Value (can be null for some filter classes)
   * @param values the list of values to filter on. Set to null if empty.
   */
  public void setValues(List<DateTime> values) {
    if ((values == null) || (values.isEmpty())) {
      this.valueList = null;
    } else if ((this.valueRange != null) || (this.filterValue != null)) {
      throw new IllegalArgumentException("You cannot set the value list after setting a "
              + "filter value or a date range.");
    } else {
      this.valueList = values;
    }
  }
  
  /**
   * Get the assigned list of filter values (to be used with an IN | NOTIN condition)
   * @return the assigned list (or null)
   */
  public DateRange getValueRange() {
    return this.valueRange;
  }

  /**
   * Set the Filter Value (can be null for some filter classes)
   * @param values the list of values to filter on. Set to null if empty.
   */
  public void setValueRange(DateRange valueRange) {
    if ((valueRange == null) || (valueRange.isEmpty())) {
      this.valueRange = null;
    } else if ((this.valueList != null) || (this.filterValue != null)) {
      throw new IllegalArgumentException("You cannot set the value range after setting a "
              + "filter value or a list of values.");
    } else {
      this.valueRange = valueRange;
    }
  }
// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="IEntityFilter Implementation">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Always return false</p>
   */
  @Override
  public boolean isDistinct() {
    return false;
  }
  
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE:  Always return true - this is a value filter</p>
   */
  @Override
  public final boolean isValueFilter() {
    return true;
  }

  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return (this.fieldName != null)</p>
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
    
    Predicate result = null;    
    Expression<Date> expPath = fromRoot.get(this.fieldName);    
    if (expPath != null) {
      if ((this.filterValue == null) && (this.valueList == null) && 
                                                          (this.valueRange == null)) {
        if ((this.condition & EntityFilterEnums.NOTEQUAL) == EntityFilterEnums.NOTEQUAL) {
          result = criteriaBuilder.isNotNull(expPath);
        } else {
          result = criteriaBuilder.isNull(expPath);
        }
      } else if (this.filterValue != null) {
        Date filterDate = this.filterValue.getAsDate();
        if (this.condition == EntityFilterEnums.NOTEQUAL) {
          result = criteriaBuilder.notEqual(expPath, filterDate);
        } else if (this.condition == EntityFilterEnums.EQUAL) {
          result = criteriaBuilder.equal(expPath, filterDate);
        } else if (this.condition == EntityFilterEnums.GREATEROREQUAL) {
          result = criteriaBuilder.greaterThanOrEqualTo(expPath, filterDate);
        } else if (this.condition == EntityFilterEnums.GREATER) {
          result = criteriaBuilder.greaterThan(expPath, filterDate);
        } else if (this.condition == EntityFilterEnums.LESSOREQUAL) {
          result = criteriaBuilder.lessThanOrEqualTo(expPath, filterDate);
        } else if (this.condition == EntityFilterEnums.LESS) {
          result = criteriaBuilder.lessThan(expPath, filterDate);
        } else {
          throw new Exception("Not currently supported");
        }
      } else if (this.valueList != null) {
        if (this.condition == EntityFilterEnums.IN) {
          result = expPath.in(this.valueList);
        } else if (this.condition == EntityFilterEnums.NOTIN) {
          result = criteriaBuilder.not(expPath.in(this.valueList));
        }
      } else if ((this.valueRange != null) && (!this.valueRange.isEmpty())) {
        Date minDt = this.valueRange.getMin().getAsDate();
        Date maxDt = this.valueRange.getMax().getAsDate();
        if (this.condition == EntityFilterEnums.BETWEEN) {
          result = criteriaBuilder.between(expPath, minDt, maxDt);
        } else if (this.condition == EntityFilterEnums.NOTIN) {
          result = criteriaBuilder.not(criteriaBuilder.between(expPath, minDt, maxDt));
        }
      }
    }
    return result;
  }


  /**
   * {@inheritDoc} <p>OVERRIDE: Assign All properties - except the Filter value.
   * Ignore if target=null or (!instanceof EntityDateFilter)</p>
   */
  @Override
  protected void AssignTo(EntityFilter target) {
    if ((target != null) && (target instanceof EntityDateFilter)) {
      EntityDateFilter filter = (EntityDateFilter) target;
      filter.condition = this.condition;
      filter.conditionOptions = this.conditionOptions;
      filter.fieldName = this.fieldName;
      filter.filterValue = this.filterValue;
      filter.valueList = this.valueList;
      filter.valueRange = this.valueRange;
    }
  }
  // </editor-fold>
}
