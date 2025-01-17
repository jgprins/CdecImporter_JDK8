package bubblewrap.entity.filters;

import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import bubblewrap.io.DataEntry;

/**
 * <p>This is an EntityFilter designed to join two related entities (i.e., with a
 * defined Parent-Child relationship) with a join to allow filtering a Bean Entity
 * (TBean) for a filter condition set be a nested EntityFilter. For Example: Filter in
 * ((pChild.parent.disabled=false) && (pChild.parent.hidden = false)).</p>
 * <p>The EntityJoinFilter apply the join and used an nested EntityFilter<TJoin> to 
 * assign the Filter's Predicate.</p>
 * <p><b>NOTE:</b> EntityJoinFilter can be nested, which apply the EntityJoinFilter's 
 * assigned EntityFilter can be another EntityJoinFilter.  If the objective of the 
 * EntityJoinFilter is to filter on one join field it might be simpler to use the 
 * {@linkplain EntityJoinValueFilter} instead.</p>
 * @author kprins
 */
public class EntityJoinFilter<TBean extends Serializable,  TJoin extends Serializable> 
                                                          extends EntityFilter<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Protected Common Fields">
  /**
   * Placeholder for the Join Fieldname.
   */
  private String fieldName;
  /**
   * Placeholder for the nested Join EntiyFilter.
   */
  private EntityFilter<TJoin> mpEntityFilter;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor for cloning
   */
  protected EntityJoinFilter() {
    super();    
    this.mpEntityFilter = null;
  }
  
  /**
   * Public Constructor with Join Field and an nested EntityFilter<TJoin>
   * @param sFieldname the TBean's joint field of type TJoin
   * @param pEntFilter the nested EntityFilter to apply the joined filter 
   * condition to.
   * @throws Exception 
   */
  public EntityJoinFilter(String sFieldName, EntityFilter<TJoin> pEntFilter) 
                                                                    throws Exception {
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
   * @return the assigned value
   */
  public String getFieldname() {
    return this.fieldName;
  }
  
  /**
   * Get the Join's nested EntityFilter
   * @return String
   */
  public EntityFilter<TJoin> getJoinFilter() {
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
   * {@inheritDoc} <p>OVERRIDE: Always return false (A JoinFilter has no assigned value)
   * </p>
   */
  @Override
  public boolean isValueFilter() {
    return false;
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
    
    Expression<TJoin> fldPath = fromEnt.get(this.fieldName);
    if (fldPath == null) {
      throw new Exception("Unable to locate Join Field[" + this.fieldName
                  + "] in Root[" + fromEnt.getJavaType().getSimpleName() + "].");
    } 
    
    Join<TBean,TJoin> join = null;
    try{
      join = fromEnt.join(this.fieldName);
      if (join == null) {
        throw new Exception("The join on Field[" + this.fieldName
                    + "] in Root[" + fromEnt.getJavaType().getSimpleName() + "] failed.");
      }                                  
    } catch (Exception exp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".getPredicate.join[Field=" + this.fieldName
                    + "] Error:\n " + exp.getMessage());
    }
    
    return this.mpEntityFilter.getPredicate(join, pCb);
  }
  
  /**
   * {@inheritDoc} <p>OVERRIDE: Assign All properties - except the Filter value.
   * Ignore if pTarget=null or (!instanceof EntityJoinValueFilter)</p>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected void AssignTo(EntityFilter pTarget) {
    if ((pTarget != null) && (pTarget instanceof EntityJoinFilter)) {
      EntityJoinFilter pFilter = (EntityJoinFilter) pTarget;
      try {
        pFilter.fieldName = this.fieldName;
        pFilter.mpEntityFilter = (EntityValueFilter) this.mpEntityFilter.clone();
      } catch (Exception pExp) {
        logger.log(Level.WARNING, "{0}.AssignTo Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      }
    }
  }
  //</editor-fold>
}
