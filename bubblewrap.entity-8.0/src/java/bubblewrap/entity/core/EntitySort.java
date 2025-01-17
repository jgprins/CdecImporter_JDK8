package bubblewrap.entity.core;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.Objects;

/**
 * <p>A Wrapper of Entity Sort settings (Field(s) to sort on the Sort Order). The 
 * EntitySort definitions are used by the {@linkplain EntityComparator} to sort 
 * entities in a list.</p>
 * <p>The EntitySort is designed to handles multi-field sorts as well as nested sorts.
 * In order to support these features the EntitySort can be setup to sort on one or 
 * more field with the same sort order and with a "sortIdField" that are different from
 * the actual sort fields. Example, a EntitySort[id="fullName"] could be setup to sort 
 * on fields {"lastName", "firstName"}
 * </p><p>
 * An EntitySort can also have subSorts, which another EntitySort to use if this main
 * sort cannot determine the sort order (i.e., {@linkplain EntityComparator#compare(
 * java.io.Serializable, java.io.Serializable) EntityComparator.compare(obj1, obj2)} = 0
 * </p>
 * <p>The simplest EntitySort is one that sort on a single field, in which case the
 * sortField is also the sortIdField.</p>
 * @author kprins
 */
public class EntitySort implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Sort Field
   */
  private String sortIdField;
  /**
   * The Array of field to sort on. This fields must have at an associate getMethod
   */
  private String[] sortFields;
  /**
   * The Sort order (true=Ascending; false=Descending) (Default=true)
   */
  private Boolean sortAsc;
  /**
   * Reference to an SubSort field
   */
  private EntitySort subSort;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public EntitySort(String sortField, boolean sortAsc) {
    sortField = DataEntry.cleanString(sortField);
    if (sortField == null) {
      throw new NullPointerException("The Sort Field cannot be unassigned");
    }
    
    this.sortIdField = sortField;
    this.sortFields = new String[]{sortField};
    this.sortAsc = (sortAsc)? null: sortAsc;
  }
  /**
   * Public Constructor  
   */
  public EntitySort(String sortIdField, boolean sortAsc, String...sortFields) {
    sortIdField = DataEntry.cleanString(sortIdField);
    if (sortIdField == null) {
      throw new NullPointerException("The SortId Field cannot be unassigned");
    }
    if ((sortFields == null) || (sortFields.length == 0)) {
      throw new NullPointerException("The Sort Fields cannot be unassigned or empty");
    }
    
    this.sortIdField = sortIdField;
    this.sortFields = sortFields;
    this.sortAsc = (sortAsc)? null: sortAsc;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the EntitySort's Sort ID Field Name
   * @return the assigned value
   */
  public String getSortIdField() {
    return this.sortIdField;
  }
  
  /**
   * Get the array of field to sort on in the order that they should be processed.
   * @return an array of one or more Entity field names.
   */
  public String[] getSortFields() {
    return this.sortFields;
  }
  
  /**
   * Get the sort order
   * @return (true=Ascending; false=Descending) (default=Ascending)
   */
  public boolean getSortAsc() {
    return (this.sortAsc == null)? true: this.sortAsc;
  }
  
  /**
   * If fieldName = this.sortIdField, return this.sortAsc else if (this.subSort != 
   * null), return this.subSort.getSortAsc(fieldName), else return true.
   * @param sortIdField the SortIdField to search for
   * @return the sort order of located or true if none is found.
   */
  public boolean getSortAsc(String sortIdField) {
    boolean result = true;
    sortIdField = DataEntry.cleanString(sortIdField);
    if (sortIdField != null) {
      if (this.isField(sortIdField)) {
        result = this.getSortAsc();
      } else if (this.subSort != null) {
        result = this.subSort.getSortAsc(sortIdField);
      }
    }
    return result;
  }
  
  /**
   * Set the sort order if this SortField
   * @param sortAsc (true=Ascending; false=Descending)
   */
  public void setSortAsc(boolean sortAsc) {
    this.sortAsc = (sortAsc)? null: sortAsc;
  }
    
  /**
   * If this.sortId = sortIdField, set this Sort Order, else if (this.subSort != null)
   * call the subSort's setSortAsc(sortIdField,sortAsc), else ignore the call. Also
   * ignore the call is fieldName = ""|null.
   * @param sortAsc (true=Ascending; false=Descending)
   */
  public void setSortAsc(String sortIdField, boolean sortAsc) {
    sortIdField = DataEntry.cleanString(sortIdField);
    if (sortIdField == null) {
      return;
    }
    if (this.isField(sortIdField)) {
      this.sortAsc = (sortAsc)? null: sortAsc;
    } else if (this.subSort != null) {
      this.subSort.setSortAsc(sortIdField, sortAsc);
    }
  }
  
  /**
   * Get this EntotySort's Sub-Sort(s)
   * @return return the assigned sub-sort (or null)
   */
  public EntitySort getSubSort() {
    return this.subSort;
  }
  
  /**
   * If this.subSort is null, initiate this sub-Sort, else if this.subSort.sortId = 
   * sortId, call this.subSort's reverseOrder method, else call this.subSort's 
   * addSubSort method to add the the field as a sub-sort of this.sub-Sort.
   * @param sortIdField field to sort on
   * @param sortAsc true for ascending; false for descending sort order
   */
  public void addSubSort(String sortIdField, boolean sortAsc) {
    sortIdField = DataEntry.cleanString(sortIdField);
    if (sortIdField == null) {
      return;
    }
    
    if (this.subSort == null) {
      this.subSort = new EntitySort(sortIdField, sortAsc);
    } else if (this.subSort.isField(sortIdField)) {
      this.subSort.reverseSortOrder();
    } else {
      this.subSort.addSubSort(sortIdField, sortAsc);
    }
  }
  
  /**
   * If this.subSort is null, assign <tt>entitySort</tt> as this.subSort, else if 
   * this.subSort.equals(entitySort), call this.subSort's reverseOrder method, else 
   * call this.subSort's addSubSort method to add the entitySort as a sub-sort of 
   * this.subSort.
   * @param entitySort the entity sort to add.
   */
  public void addSubSort(EntitySort entitySort) {    
    if (entitySort == null) {
      return;
    }
        
    if (this.subSort == null) {
      this.subSort = entitySort;
    } else if (this.subSort.equals(entitySort)) {
      this.subSort.reverseSortOrder();
    } else {
      this.subSort.addSubSort(entitySort);
    }
  }
  
  /**
   * If this Entity's subSort.sortIdField = sortIdField, clear the subSort and all its 
   * sub-sorts, else call this.subSort's removeSubSort method. Ignored is sortIdField = 
   * ""|null or this.subSort = null.
   * @param sortIdField the field to search for.
   */
  public void removeSubSort(String sortIdField) {
    sortIdField = DataEntry.cleanString(sortIdField);
    if ((sortIdField == null) || (this.subSort == null)) {
      return;
    }
    
    if (this.subSort.isField(sortIdField)) {
      this.subSort = null;
    } else {
      this.subSort.removeSubSort(sortIdField);
    }
  }
  
  /**
   * Clear the Sub-Sort and all its sub-sorts
   */
  public void clearSubSort() {
    this.subSort = null;
  }
  
  /**
   * Reverse the current Sort Order. 
   */
  public void reverseSortOrder() {
    this.setSortAsc((!this.getSortAsc()));
  }
  
  /**
   * Reverse the current Sort Order or its subSort - the one which sortIdField matches 
   * the specified Sort ID field 
   * @param sortIdField the name of the field who's sort order must be reversed.
   */
  public void reverseSortOrder(String sortIdField) {
    if (this.isField(sortIdField)) {
      this.setSortAsc((!this.getSortAsc()));
    } else if (this.subSort != null) {
      this.reverseSortOrder(sortIdField);
    }
  }
  
  /**
   * Get whether sField is this EntitySort's Field
   * @param sortIdField the field name to evaluate
   * @return true if it is a match (no case sensitive).
   */
  public boolean isField(String sortIdField) {
    return (DataEntry.isEq(this.sortIdField, sortIdField, true));
  }
  
  /**
   * Get whether sField is this EntitySort's Field
   * @param sortIdField the field name to evaluate
   * @return true if it is a match (no case sensitive).
   */
  public boolean hasField(String sortIdField) {    
    return ((DataEntry.isEq(this.sortIdField, sortIdField, true)) ||
            ((this.subSort != null) && this.subSort.hasField(sortIdField)));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Create a deep clone of this instance (including clones of its 
   * sub-sorts)</p>
   */
  @Override
  @SuppressWarnings("CloneDoesntCallSuperClone")
  public EntitySort clone()  {
    EntitySort result = 
                new EntitySort(this.sortIdField, this.getSortAsc(), this.sortFields);
    if (this.subSort != null) {
      result.subSort = this.subSort.clone();
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode based on this.sortIdField</p>
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + Objects.hashCode(this.sortIdField);
    return hash;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return  true if obj != null and an instance of EntitySort and 
   * this.sortFiedlId = obj.sortFieldId.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EntitySort));
    if (result) {
      EntitySort entSort = (EntitySort) obj;
      result = (this.sortIdField.equals(entSort.sortIdField));
    }
    return result;
  }
  //</editor-fold>
}
