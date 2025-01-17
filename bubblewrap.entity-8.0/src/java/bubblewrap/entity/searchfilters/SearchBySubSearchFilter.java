package bubblewrap.entity.searchfilters;

import bubblewrap.core.events.EventArgs;
import bubblewrap.core.events.EventDelegate;
import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.core.EntityList;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.filters.EntityEmptyFilter;
import bubblewrap.entity.filters.EntityFilter;
import bubblewrap.entity.filters.EntityGroupFilter;
import bubblewrap.entity.filters.EntityValueFilter;
import bubblewrap.entity.search.EntityListSearch;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * <p>This an abstract extension of the SearchCustomFilter class to search for the 
 * matching child record of class<TBean> based on a list parent entities of 
 * Class<TSearchBean>) using a the resulting recordset from Sub-RecordSearch. This 
 * filter returns a EntityGroupFilter<TBean> containing the list of 
 * EntityValueFilter<TBean,TSearcBean> to find the matching records.</p>
 * <p>The filter's SearchValue is a boolean flag to turn the Filter On|Off. It also
 * has an optional defaultSearchValue setting, which apply if the searchValue is not
 * explicitly set.</p>
 * <p>The filter isSet state is determined by (a) its searchValue=true (the Filter is
 * turned ON) and b) the subSearch's isSet state. </p>
 * <p>If (!isSet), the getFilter returns null, otherwise it get the SubSearch's 
 * recordset to build and return a EntityGroup files. If the SubSearch's recordset is
 * empty, it returns a EntityEmptyFilter</p>
 * <p>This filter is the listener to any changes to the Sub-RecordSearch and will 
 * propagate this onChangeEvent up the filter chain.</p>
 * <p><b>NOTE:</b> The clear filter does not clear the subsearch it only resets the
 * searchValue. This implies that after clearing the filter the defaultSearchValue will
 * determine the isSet state if the latter is assigned.</p>
 * <p><b>NOTE:</b> Any changes to the Subsearch will automatically be reflected in the
 * owner recordSearch.</p>
 * @author kprins
 */
public abstract class SearchBySubSearchFilter<TBean extends Serializable, 
                                 TSearchBean extends Serializable> 
                                 extends SearchCustomFilter<TBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The base EnityFilter to be used for generating the resulting search EntityFilter.
   */
  private EntityValueFilter<TBean,TSearchBean> entityFilter;
  /**
   * Placeholder for this Filter's Sub-RecordSearch.
   */
  private EntityListSearch subSearch;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a searchId for the filter, the EntityListSearchClass of the
   * sub-RecordSearch and a searchId for the subRecordSearch.
   * @param sSearchId the Search Filter ID
   * @param pSubSearchClass the EntityListSearch Class to use for returning the records
   * to search on.
   * @param sFilterField the TBean.field to search on.
   */
  @SuppressWarnings("unchecked")
  protected SearchBySubSearchFilter(String sSearchId, String sFilterField,
                Class<? extends EntityListSearch> pSubSearchClass) {
    super(sSearchId);    
    try {      
      this.entityFilter = null;
      this.subSearch = null;
      sFilterField = DataEntry.cleanString(sFilterField);
      if (pSubSearchClass == null) {
        throw new Exception("The Filter's SubSearch class cannot be unassigned");
      }
      
      if (sFilterField == null) {
        throw new Exception("The Filter's EnityFilter Field cannot be unassigned");
      }
      

      Class pEntClass = 
            ReflectionInfo.getGenericClass(EntityListSearch.class, pSubSearchClass, 1);

      Class pValueClass = this.getValueClass();
      if ((pEntClass == null) || (!pEntClass.equals(pValueClass))) {
        throw new Exception("Sub-RecordSearch[" + pSubSearchClass.getSimpleName() 
                + "]'s Entity class does not match SearchFilter[" 
                + this.getClass().getSimpleName() + "]'s Value Class.");
      }
      
      this.entityFilter = 
                        new EntityValueFilter<>(sFilterField, EntityFilterEnums.EQUAL);
      
      this.subSearch = SessionHelper.getManagedBean(pSubSearchClass);
      if (this.subSearch == null) {
        throw new Exception("Accessing the Session's EntityListSearch[" 
                + pSubSearchClass.getSimpleName() + "] failed.");
      }
      this.subSearch.SearchChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          SearchBySubSearchFilter listener = this.getListener();
          if (listener != null) {
            try {
              listener.fireSearchChanged();        
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      });
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.new Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw new NullPointerException("Initiating Class[" + 
              this.getClass().getSimpleName() + "] see server log for details");
    }    
  }
  
  /**
   * Public Constructor with a searchId for the filter, the EntityListSearchClass of the
   * sub-RecordSearch and a searchId for the subRecordSearch.
   * @param sSearchId the Search Filter ID
   * @param pSubSearch the EntityListSearch to use for returning the records
   * to search on.
   * @param sFilterField the TBean.field to search on.
   */
  protected SearchBySubSearchFilter(String sSearchId, String sFilterField,
                EntityListSearch pSubSearch) {
    super(sSearchId);    
    try {      
      this.entityFilter = null;
      this.subSearch = null;
      sFilterField = DataEntry.cleanString(sFilterField);
      
      if (pSubSearch == null) {
        throw new Exception("The Filter's SubSearch cannot be unassigned");
      }
      if (sFilterField == null) {
        throw new Exception("The Filter's EnityFilter Field cannot be unassigned");
      }

      @SuppressWarnings("unchecked")
      Class pEntClass = pSubSearch.getEntityClass();
      Class pValueClass = this.getValueClass();
      if ((pEntClass == null) || (!pEntClass.equals(pValueClass))) {
        throw new Exception("Sub-RecordSearch[" + pSubSearch.getClass().getSimpleName() 
                + "]'s Entity class does not match SearchFilter[" 
                + this.getClass().getSimpleName() + "]'s Value Class.");
      }
      
      this.entityFilter = 
                        new EntityValueFilter<>(sFilterField, EntityFilterEnums.EQUAL);
      this.subSearch = pSubSearch;
      this.subSearch.SearchChanged.add(new EventDelegate(this) {
        
        @Override
        public void onEvent(Object sender, EventArgs eventInfo) {
          SearchBySubSearchFilter listener = this.getListener();
          if (listener != null) {
            try {
              listener.fireSearchChanged();        
            } catch (Exception exp) {
              logger.log(Level.WARNING, "{0}.onEvent Error:\n {1}",
                      new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
            }
          }
        }
      });
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.new Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw new NullPointerException("Initiating Class[" + 
              this.getClass().getSimpleName() + "] see server log for details");
    }    
  }

  /**
   * OVERRIDE: Call super method before removing this as a listener of the SubSearch if
   * the latter is defined.
   * @throws Throwable 
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (this.subSearch != null) {
      this.subSearch.SearchChanged.remove(this);
      this.subSearch = null;
    }
    this.entityFilter = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public/Protected Methods">
  /**
   * Get the Filter's related Class (i.e CLass<TSearchBean>)
   * @return Class<TSearchBean>
   */
  @SuppressWarnings("unchecked")
  public final Class<TSearchBean> getValueClass() {
    return (Class<TSearchBean>)
                      ReflectionInfo.getGenericClass(SearchBySubSearchFilter.class, 
                      this.getClass(), 1);
  }

  /**
   * Return the Filter ValueClass' SimpleName.
   * @return this.valueClass.getSimpleName
   */
  public final String getValueClassName() {
    Class pClass = this.getValueClass();
    return pClass.getSimpleName();
  }
  
  /**
   * Get a reference to the internal SubSearch reference. if undefined retrieve the
   * ManagedBean SubSearch using the assigned SubSearch Class
   * @return EntityListSearch to be used in retrieving the TSearchBean records.
   */
  public final EntityListSearch getSubRecordSearch() {
    return this.subSearch;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Overrides">
  /**
   * Return true if the SubSearchFilter has an assigned SubSearch and an EntityFilter
   * @return boolean
   */
  @Override
  public boolean hasFilter() {
    return ((this.subSearch != null) && (this.entityFilter != null));
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super.isSet and if true, check is the
   * subRecordSearch.isSet.</p>
   */
  @Override
  public boolean isSet() {
    boolean bResult = super.isSet();
    if (bResult) {
      EntityListSearch pSubSrch = this.getSubRecordSearch();
      bResult = ((pSubSrch != null) && (pSubSrch.isSet()));
    }
    return bResult;
  }
  
  /**
   * <p>Called by onGetFilter to get the sub-search's recordset as a TSearchBean list.
   * Errors are logged. The list will be empty if no records are found.</p>
   * @return return the SubSearch's recordset or null if (!isSet)
   */
  @SuppressWarnings("unchecked")
  protected List<TSearchBean> getSubSearchValues() {
    List<TSearchBean> pResult = null;
    try {
      if (this.isSet()) {
        EntityListSearch pSubSrch = this.getSubRecordSearch();
        EntityList<EntityWrapper> pRecset = 
                                 ((pSubSrch == null) || (pSubSrch.isEmpty()))?
                                 null: pSubSrch.getEntityList();
        if ((pRecset != null) && (!pRecset.isEmpty())) {
          for (EntityWrapper pEntAjax : pRecset) {
            TSearchBean pBean = (TSearchBean) pEntAjax.getEntity();
            if (pBean != null) {
              if (pResult == null) {
                pResult = new ArrayList<>();
              }
              pResult.add(pBean);
            }
          }
        } else {
          pResult = new ArrayList<>();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parsedSearchValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }

  /**
   * OVERRIDE: Call getSubSearchValues to get a list of TSearchBeans to use in building
   * the EntityFilter to return. if (!isSet) return null, if the list of TSearchBeans is
   * empty, return an EntityEmptyFilter, if there is only one TSearchBeans in the list,
   * return a single EntityValueFilter. Otherwise, build a GroupFilter with an
   * EntityValueFilter for each TSearchBean.
   * @return the EntityFilter to filter on the subSearch.recordset
   */
  @Override
  @SuppressWarnings("unchecked")
  protected EntityFilter<TBean> onGetFilter() {
    EntityFilter<TBean> pResult = null;
    try {
      if (this.isSet()) {
        List<TSearchBean> pValues = this.getSubSearchValues();
        if ((pValues != null) && (!pValues.isEmpty())) {
          if (pValues.size() == 1) {
            TSearchBean pValue = pValues.get(0);
            if (pValue != null) {
              EntityValueFilter<TBean,TSearchBean> pFilter = 
                     (EntityValueFilter<TBean,TSearchBean>) this.entityFilter.clone();
              pFilter.setValue(pValue);
              pFilter.setValues(null);
              pResult = pFilter;
            }
          } else if ((this.entityFilter.getCondition() == EntityFilterEnums.IN) ||
                  (this.entityFilter.getCondition() == EntityFilterEnums.NOTIN)) {
            EntityValueFilter<TBean,TSearchBean> pFilter = 
                     (EntityValueFilter<TBean,TSearchBean>) this.entityFilter.clone();
            pFilter.setValue(null);
            pFilter.setValues(pValues);
            pResult = pFilter;
          } else {
            EntityGroupFilter<TBean> pRecFilter = new EntityGroupFilter<>(false);
            for (TSearchBean pValue : pValues) {
              if (pValue != null) {
                EntityValueFilter<TBean,TSearchBean> pValueFilter =
                     (EntityValueFilter<TBean,TSearchBean>) this.entityFilter.clone();
                pValueFilter.setValue(pValue);
                pValueFilter.setValues(null);
                pRecFilter.addFilter(pValueFilter);
              }
            }
            pResult = pRecFilter;
          }
        } 
        
        if (pResult == null) {
          pResult = new EntityEmptyFilter<>();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Override">
  /**
   * OVERRIDE: Return "<className>[Value=" + this.getSearchValue() + "]";
   * @return a string representation of the filter
   */
  @Override
  public String toString() {
    String sResult = super.toString();
    if (this.subSearch != null) {
      sResult += "; SubSearch=" + this.subSearch.toString();
    }
    return sResult;
  }
  //</editor-fold>
}
