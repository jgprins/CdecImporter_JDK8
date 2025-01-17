package bubblewrap.entity.core;

import bubblewrap.entity.enums.WrapperCompareTypes;
import java.util.Collections;
import java.util.Comparator;

/**
 * <p>A {@linkplain EntityComparator} that can be used in the abstract Collections.sort 
 * method to sort a list of EntityWrapper records. It sort the record by comparing the 
 * result form the defined GET-method of the generically specified EntityWrapper. 
 * The GET-Method is specified by the following eSortBy enum values:</p><ul>
 * <li><b>BYRECID:</b> using the default Method[getRecordID]: SortField=recordID</li>
 * <li><b>BYNAME:</b> using the default Method[getRecordName]: SortField=recordName</li>
 * <li><b>BYDISPLAYIDX:</b> using the default Method[getDisplayIdx]: 
 *    SortField=displayIdx</li>
 * </ul>
 * <p>The SortField and SortOrder can also be specified using an {@linkplain EntitySort}
 * definition.</p>
 * @see Collections#sort(java.util.List, java.util.Comparator) 
 * @author kprins
 */
public class EntityWrapperComparator<TWrapper extends EntityWrapper> 
                                                    extends EntityComparator<TWrapper> {  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Constructor for Comparators using standard methods
   * @param compareType
   */
  public EntityWrapperComparator(WrapperCompareTypes compareType) {
    super();
    String sFieldName = null;
    if (compareType == WrapperCompareTypes.BYRECID) {
        sFieldName = "recordID";
    } else if (compareType == WrapperCompareTypes.BYNAME) {
        sFieldName = "recordName";
    } else if (compareType == WrapperCompareTypes.BYDISPLAYIDX) {
        sFieldName = "displayIdx";
    } else {
      throw new AssertionError("Invalid SortBy["+compareType+"]");
    }
    
    EntitySort entitySort = new EntitySort(sFieldName, true);
    this.initComparator(entitySort);
  }
  
  /**
   * Constructor using a {@linkplain EntitySort}
   * @param entitySort to set the Sort Field and Sort Order
   */
  public EntityWrapperComparator(EntitySort entitySort) {
    super(entitySort);
  }
  //</editor-fold>  
 }
