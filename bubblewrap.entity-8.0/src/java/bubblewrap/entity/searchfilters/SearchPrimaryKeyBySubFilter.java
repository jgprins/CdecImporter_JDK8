package bubblewrap.entity.searchfilters;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.entity.core.EntityList;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.EntityFilterEnums;
import bubblewrap.entity.enums.EntitySearchTypes;
import bubblewrap.entity.filters.EntityValueFilter;
import bubblewrap.entity.search.SearchFilterBase;
import bubblewrap.entity.search.EntityListSearch;
import bubblewrap.http.session.SessionHelper;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Given V is the entity to search like AppUser.  Y is the type of the primary
 * key like String.  E is the entity link like AppUserLink.
 * @author kprins
 */
public abstract class SearchPrimaryKeyBySubFilter<TBean extends Serializable, 
                                  TSearchBean extends Serializable> 
                                  extends SearchFilter<TBean,TSearchBean> {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for this Filter's Sub-RecordSearch Class.
   */
  private Class<? extends EntityListSearch> subSearchClass;
  /**
   * The searchId of the SubSearchFilter
   */
  private String subSearchId;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a searchId for the filter, the EntityListSearchClass of the
   * sub-RecordSearch and a searchId for the subRecordSearch.
   * @param sSearchId String
   * @param pSubClass Class<? extends EntityListSearch>
   * @param sSubSearchId String
   */
  protected SearchPrimaryKeyBySubFilter(String sSearchId, 
                  Class<? extends EntityListSearch> pSubClass, String sSubSearchId) {
    super(sSearchId,EntitySearchTypes.TEXTINPUT);    
    try {
      sSubSearchId = DataEntry.cleanString(sSubSearchId);
      if (pSubClass == null) {
        throw new Exception("The Filter's Sub-EntityListSearch class cannot be "
                + "unassigned");
      }
      if (sSubSearchId == null) {
        throw new Exception("The Filter's Sub-SearchId cannot be "
                + "unassigned");
      }

      @SuppressWarnings("unchecked")
      Class pEntClass = 
            ReflectionInfo.getGenericClass(EntityListSearch.class, pSubClass, 1);

      Class pValueClass = this.getValueClass();
      if ((pEntClass == null) || (!pEntClass.equals(pValueClass))) {
        throw new Exception("Sub-RecordSearch[" + pSubClass.getSimpleName() 
                + "]'s Entity class does not match SearchFilter[" 
                + this.getClass().getSimpleName() + "]'s Value Class.");
      }

      this.subSearchClass = pSubClass;
      EntityListSearch pSubSrch = this.getRecordSearch();
      if (pSubSrch == null) {
        throw new Exception("Sub-RecordSearch[" + pSubClass.getSimpleName() 
                      + "] is not accessible.");
      }
      
      SearchFilterBase srchFilter = pSubSrch.getFilter(sSubSearchId);
      if (srchFilter == null) {
        throw new Exception("Sub-RecordSearch[" + pSubClass.getSimpleName() 
                      + "] does not support SearcId[" + sSubSearchId +"]"); 
      } else if (!srchFilter.hasFilter()) {
        throw new Exception("Sub-RecordSearch[" + pSubClass.getSimpleName() 
                      + "] Filter[" + sSubSearchId +"] has no EntityFilter definition"); 
      }
      this.subSearchId = sSubSearchId;
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.new Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
      throw new NullPointerException("Initiating Class[" + 
              this.getClass().getSimpleName() + "] see server log for details");
    }
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public/Protected Methods">
  /**
   * Add a EntityValueFilter<V,Y> to the existing or new list of filters. 
   * Ignore the call is sField=null|"". Exceptions are logged.
   * @param fieldName String
   */
  public void addEntityFilter(String fieldName) {
    try {
      fieldName = DataEntry.cleanString(fieldName);
      if (fieldName != null) {
        
        EntityValueFilter<TBean,TSearchBean> pFilter =
                new EntityValueFilter<>(fieldName, EntityFilterEnums.EQUAL);
        if (pFilter == null) {
          throw new Exception("Initiating Field[" + fieldName + "]'s Filter failed.");
        }
        
        this.addEntityFilter(pFilter);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.addFilter Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
  }
  
  /**
   * Get a reference to the
   * @return EntityListSearch
   */
  protected final EntityListSearch getRecordSearch() {
    EntityListSearch pResult = null;
    try {
      if (this.subSearchClass != null) {
        pResult = SessionHelper.getManagedBean(this.subSearchClass);
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.getRecordSearch Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="SearchFilter Overrides">
  /**
   * Return true if the DataGrid has an assigned RecordSearch class
   * @return boolean
   */
  @Override
  public boolean onHasFilter() {
    return ((super.onHasFilter()) && (this.subSearchClass != null));
  }
  
  /**
   * <p>OVERRIDE: Assign this.cleanSearchValue to the SubEntityListSearch[SubSearchId]
   * (after clearing the SubEntityListSearch) and get its resulting recordset. If the 
   * recordset is not empty, assign the EntityBean of reach return record to the
   * result list.</p>
   * 
   * <p><b>NOTE:</b> If the searchValue is empty or an error occur, the method returns
   * null. If the recordset if empty (i.e., the searchValue returns no matching values),
   * this method returns an empty list.
   * @return List<Y>
   */
  @Override
  @SuppressWarnings("unchecked")
  protected List<TSearchBean> parsedSearchValues() {
    List<TSearchBean> result = null;
    try {
      String srchValue = this.getCleanSearchValue();
      if (srchValue != null) {
        EntityListSearch subSrch = this.getRecordSearch();
        if ((subSrch == null) || (subSrch.isEmpty())) {
          throw new Exception("SubSearch[" + this.subSearchClass.getSimpleName() + 
                  "] is not accessible or empty.");
        }
        
        subSrch.clearSearch();
        subSrch.setSearchValue(this.subSearchId, srchValue);

        EntityList<EntityWrapper> entList = subSrch.getEntityList();
        if ((entList != null) && (!entList.isEmpty())) {
          for (EntityWrapper entWrapper : entList) {
            @SuppressWarnings("unchecked")
            TSearchBean recId = (TSearchBean)entWrapper.getRecordId();
            if (recId != null) {
              if (result == null) {
                result = new ArrayList<>();
              }
              result.add(recId);
            }
          }
        } else {
          result = new ArrayList<>();
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.parsedSearchValues Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
  
}
