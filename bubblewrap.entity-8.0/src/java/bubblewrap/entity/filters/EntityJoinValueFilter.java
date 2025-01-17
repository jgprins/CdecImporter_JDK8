package bubblewrap.entity.filters;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

/**
 * <p>This is an EntityValueFilter designed to join two related entities (i.e., with a
 * defined Parent-Child relationship) with a join to allow filtering a Bean Entity
 * (TBean) for a filter value of the Joined Entity (TJoin). For Example: Filter in
 * pChild.parent.disabled=true|false.</p>
 * <p>The EntityJoinValueFilter apply the join and used an nested 
 * EntityValueFilter<TJoin, TValue> to assign the Filter's Predicate.</p>
 * <p><b>NOTE:</b> EntityJoinValueFilter can be nested, which apply the 
 * EntityJoinValueFilter's assigned EntityValueFilter can be another 
 * EntityJoinValueFilter. However, the filter is on one field of type TValue. If the
 * nested filter is a GroupFilter (or any other EntityFilter) use the 
 * {@linkplain EntityJoinFilter} instead.</p>
 * @author kprins
 */
public class EntityJoinValueFilter<TBean extends Serializable, 
        TJoin extends Serializable, TValue> extends EntityValueFilter<TBean, TValue> {
  
  //<editor-fold defaultstate="collapsed" desc="Protected Common Fields">
  /**
   * Placeholder for the nested Join EntiyValueFilter.
   */
  private EntityValueFilter<TJoin,TValue> mpEntityFilter;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor for cloning
   */
  protected EntityJoinValueFilter() {
    super();    
    this.mpEntityFilter = null;
  }
  
  /**
   * Public Constructor with Join Field and an nested EntityValueFilter<TJoin,TValue>
   * @param sFieldname the TBean's joint field of type TJoin
   * @param pEntFilter the nested EntityValueFilter to apply the joined filter 
   * condition to.
   * @throws Exception 
   */
  public EntityJoinValueFilter(String sFieldName, 
                        EntityValueFilter<TJoin,TValue> pEntFilter) throws Exception {
    super();
    sFieldName = DataEntry.cleanString(sFieldName);
    if (sFieldName == null) {
      throw new Exception("The Field to apply the join on cannot be unassigned.");
    }
    if (pEntFilter == null) {
      throw new Exception("The EntityJoinFilter's nested Filter cannot be unassigned.");
    }
    this.fieldName = sFieldName;
    this.mpEntityFilter = pEntFilter;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="ValueFilter Properties">
  /**
   * Get the Join Field Name
   * @return String
   */
  public EntityValueFilter<TJoin,TValue> getJoinFilter() {
    return this.mpEntityFilter;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="EntityValueFilter Overrrides">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return true f the Join Fields and the nested 
   * EntityFilter is assigned and the nested EntityFilter.isSet.</p>
   */
  @Override
  public boolean isSet() {
    return ((this.fieldName != null) && (this.mpEntityFilter != null) && 
            this.mpEntityFilter.isSet());
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Returns false if the the nested EntityFilter is 
   * unassigned. Otherwise, it returns the nested EntityFilter.isDistinct setting.</p>
   */
  @Override
  public boolean isDistinct() {
    return (this.mpEntityFilter == null)? false: this.mpEntityFilter.isDistinct();
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Assign pValue to the nested EntityValueFilter.value</p>
   */
  @Override
  public void setValue(TValue pValue) {
    if (this.mpEntityFilter != null) {
      this.mpEntityFilter.setValue(pValue);
    }
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Return the value of the nested EntityValueFilter</p>
   */
  @Override
  public TValue getValue() {
    return (this.mpEntityFilter == null)? null: this.mpEntityFilter.getValue();
  }

  /**
   * {@inheritDoc} <p>OVERRIDE: Construct a Join on this.fieldname and calling the
   * nested EntityValueLink's getPredicate overload 2, passing the Join as the From 
   * parameter. It return the result of that call.</p>
   */
  @Override
  public <Tx extends Serializable, Ty extends Serializable> Predicate 
              getPredicate(From<Tx, Ty> fromEnt, CriteriaBuilder pCb) throws Exception {
    if ((!this.isSet()) || (fromEnt == null) || (pCb == null)) {
      return null;
    }
    
    Expression<TJoin> pPath = fromEnt.get(this.fieldName);
    if (pPath == null) {
      throw new Exception("Unable to locate Join Field[" + this.fieldName
                  + "] in Root[" + fromEnt.getJavaType().getSimpleName() + "].");
    }
    
    Join<TBean,TJoin> pJoin = null;
    try{
      pJoin = fromEnt.join(this.fieldName);
      if (pJoin == null) {
        throw new Exception("The join on Field[" + this.fieldName
                    + "] in Root[" + fromEnt.getJavaType().getSimpleName() + "] failed.");
      }                                  
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getPredicate.join[Field=" + this.fieldName
                    + "] Error:\n " + exp.getMessage());
    }
    
    return this.mpEntityFilter.getPredicate(pJoin, pCb);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Assign All properties - except the Filter value.
   * Ignore if pTarget=null or (!instanceof EntityJoinValueFilter)</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void AssignTo(EntityFilter pTarget) {
    super.AssignTo(pTarget);
    if ((pTarget != null) && (pTarget instanceof EntityJoinValueFilter)) {
      EntityJoinValueFilter pFilter = (EntityJoinValueFilter) pTarget;
      try {
        pFilter.mpEntityFilter = (EntityValueFilter) this.mpEntityFilter.clone();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.AssignTo Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
  }
  //</editor-fold>
}
