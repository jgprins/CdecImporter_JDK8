package bubblewrap.entity.core;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import bubblewrap.io.datetime.DateTime;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>A Comparator that can be used in the abstract {@linkplain 
 * Collections#sort(java.util.List)  Collections.sort} method to sort a list of Entity 
 * records according to the assigned {@linkplain EntitySort} for the generically 
 * assigned TBean. It sort the record by comparing the result from the defined 
 * EntitySort.field and the EntitySort.sortAsc. The TBean class must support a 
 * GET-Method for EntitySort.field.
 * @author kprins
 */
public class EntityComparator<TBean extends Serializable> 
                                           implements Comparator<TBean>, Serializable {
  //<editor-fold defaultstate="collapsed" desc="Private Static Compare Types">
  private static final int ASSTRING = 0;
  private static final int ASBYTE = 1;
  private static final int ASSHORT = 2;
  private static final int ASINT = 3;
  private static final int ASLONG = 4;
  private static final int ASFLOAT = 5;
  private static final int ASDOUBLE = 6;
  private static final int ASDATE = 7;
  private static final int ASDATETIME = 8;
  private static final int ASENUM = 9;
  private static final int ASCOMPARABLE = 10;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
                                  Logger.getLogger(EntityComparator.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Internal SortFieldDef class">
  /**
   * A Class for storing the sort definitions for multiple sort fields
   */
  private static class SortDef implements Serializable {
    /**
     * Placeholder for a sort method name
     */
    protected final String methodName;
    /**
     * Placeholder for a sort order (true=ASC |false=DESC)
     */
    protected Boolean sortAsc;
    /**
     * Placeholder for a sort method - initiated on first use
     */
    protected Method method;
    /**
     * Placeholder for a sort compare type (ASSTRING | ASLONG |ASDOUBLE)
     * (Default=ASSTRING)
     */
    protected Integer compareType;
    /**
    * Placeholder for a flag that is set is initiating the method failed.
    * (Default=false)
    */
    protected Boolean hasError;
    /**
    * Placeholder for a flag that is set is initiating the method failed.
    * (Default=false)
    */
    protected SortDef subSort;
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    protected SortDef(String methodName) {
      this.methodName = methodName;
      this.sortAsc = true;
      this.method = null;
      this.compareType = EntityComparator.ASSTRING;
      this.hasError = false;
      this.subSort = null;
    }
    //</editor-fold>
    
    /**
     * Called to check is the SortDef with a matching methodName exist in the sort 
     * stack. 
     * @param methodName the method name to search for.
     * @return true if a match was found 
     */
    public boolean isMethod(String methodName) {
      boolean result = (this.methodName.equals(methodName));
      if ((!result) && (this.subSort != null)) {
        result = this.subSort.isMethod(methodName);
      }
      return result;
    }
    
    /**
     * Called to add the subSort to the end of the sort stack. If this.subSort = null
     * set this.subSort=subSort, else call this.subSort.addSubSort(subSort)
     * @param subSort the sub-sortDef to add. Skipped is null.
     */
    public void addSubSort(SortDef subSort) {
      if (subSort == null) {
        return;
      }
      
      if (this.subSort == null) {
        this.subSort = subSort;
      } else {
        this.subSort.addSubSort(subSort);
      }
    }
  }
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Comparator's root SortDef.
   */
  private SortDef rootSortDef;  
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * A Protected Parameterless constructor with no sortField assignments
   */
  protected EntityComparator() {
    this.rootSortDef = null;
  }
  
  /**
   * Constructor for Comparators using sort fields as defined by the specified
   * <tt>entutySort</tt>. It calls the {@linkplain #initComparator(
   * bubblewrap.entity.core.EntitySort) this.initComparator(entitySort)} to initiate
   * the sortFields
   * @param entitySort the EnitySort with sorting specifications
   */
  public EntityComparator(EntitySort entitySort) {
    this();
    this.initComparator(entitySort);
  }
  
  /**
   * Method called by all constructors to initiate the Comparator
   * @param entitySort 
   */
  protected final void initComparator(EntitySort entitySort) {
    if (entitySort == null) {
      throw new NullPointerException("The EntitySort cannot be unassigned");
    }
    this.addEntitySort(entitySort);
  }
  
  /**
   * Called to add a entitySort to the comparator. If <tt>entitySort</tt> has a 
   * sub-sort, the method will be re-called to add the sub-sort to.
   * @param entitySort the EntitySort to add.
   */
  protected void addEntitySort(EntitySort entitySort) {
    String sortId = null;
    if ((entitySort == null) || ((sortId = entitySort.getSortIdField()) == null)) {
      return;
    }
    String[] fieldArr = entitySort.getSortFields();
    if ((fieldArr == null) || (fieldArr.length == 0)) {
      throw new NullPointerException("The EntitySort[" + sortId + "].sortFields is "
              + "unassigned or empty");
    }
    for (String fieldName : fieldArr) {
      this.addSortField(fieldName, entitySort.getSortAsc());
    }
    EntitySort subSort = entitySort.getSubSort();
    if (subSort != null) {
      this.addEntitySort(subSort);
    }
  }
  
  /**
   * Initiate a new sortField for the fieldName. 
   * @param fieldName
   * @param sortAsc 
   */
  private void addSortField(String fieldName, boolean sortAsc) {
    fieldName = DataEntry.cleanString(fieldName);
    if (fieldName == null) {
      return;
    }
    
    String methodName = ReflectionInfo.getGetMethodName(fieldName);
    try {
      if ((this.rootSortDef != null) && (this.rootSortDef.isMethod(methodName))) {
        return;
      }
      
      SortDef sortDef = new SortDef(methodName);
      sortDef.sortAsc = sortAsc; 
      sortDef.method = null;
      sortDef.compareType = EntityComparator.ASSTRING;
      if (this.rootSortDef == null) {
        this.rootSortDef = sortDef;
      } else {
        this.rootSortDef.addSubSort(sortDef);
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.addSortField Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Protected method to set the Sort Order after the Comparator is constructed.
   * It set the SortOrder of this.rootSortDef. Ignored is the rootSortDef = null.
   * @param sortAsc true=ASC and false=DESC
   */
  protected void setSortAsc(boolean sortAsc) {
    if (this.rootSortDef != null) {
      this.rootSortDef.sortAsc = sortAsc;
    }
  } 
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Methods">  
  /**
   * Get the Method Values and Compare the values as Long Values using this.mpMethod
   * @param obj1 First TBean instance
   * @param obj2 Second TBean instance
   * @return the resolved sort order
   * @throws Exception
   */
  private int compareAsNumber(SortDef sortField, TBean obj1, TBean obj2) 
          throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
      
    try {
      Object[] args = null;
      Object value1 = sortField.method.invoke(obj1, args);
      Object value2 = sortField.method.invoke(obj2, args);
      if ((value1 == null) && (value2 == null)) {
        result = 0;
      } else if (value1 == null) {
          result = 1;
      } else if (value2 == null) {
        result = -1;
      } else {
        switch (sortField.compareType) {
          case EntityComparator.ASBYTE:
            result = 
               Byte.compare(((Byte) value1).byteValue(), 
                              ((Byte) value2).byteValue());
            break;
          case EntityComparator.ASSHORT:
            result = Short.compare(((Short) value1).shortValue(), 
                                     ((Short) value2).shortValue());
            break;
          case EntityComparator.ASINT:
            result = Integer.compare(((Integer) value1).intValue(), 
                                       ((Integer) value2).intValue());
            break;
          case EntityComparator.ASLONG:
            result = Long.compare(((Long) value1).longValue(), 
                                    ((Long) value2).longValue());
            break;
          case EntityComparator.ASFLOAT:
            result = Float.compare(((Float) value1).floatValue(), 
                                     ((Float) value2).floatValue());
            break;
          case EntityComparator.ASDOUBLE:
            result = Double.compare(((Double) value1).doubleValue(), 
                                      ((Double) value2).doubleValue());
            break;
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsNumbar Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Get the Method Values and Compare the values as String Values using this.mpMethod
   * @param pObj1 First TBean instance
   * @param obj2 Second TBean instance
   * @return int
   * @throws Exception
   */
  private int compareAsString(SortDef sortField, TBean obj1, TBean obj2) 
            throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
    
    try {
      Object[] args = null;
      Object value1 = (Object) sortField.method.invoke(obj1, args);
      Object value2 = (Object) sortField.method.invoke(obj2, args);
      
      String sVal1 = (value1 == null)? null: DataEntry.cleanString(value1.toString());
      String sVal2 = (value2 == null)? null: DataEntry.cleanString(value2.toString());
      
      if ((sVal1 != null) && (sVal2 != null)) {
        if (sVal1 == null) {
          result = 1;
        } else if (sVal2 == null) {
          result = -1;
        } else {
          result = sVal1.compareToIgnoreCase(sVal2);
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsString Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Compare the field value of obj1 and objs2 as Dates
   * @param sortField the sort field
   * @param obj1 the first object
   * @param obj2 the first object
   * @return the compare result
   * @throws Exception 
   */
  private int compareAsDate(SortDef sortField, TBean obj1, TBean obj2) 
            throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
    
    try {
      Object[] args = null;
      Date value1 = (Date) sortField.method.invoke(obj1, args);
      Date value2 = (Date) sortField.method.invoke(obj2, args);
      
      if ((value1 != null) && (value2 != null)) {
        if (value1 == null) {
          result = 1;
        } else if (value2 == null) {
          result = -1;
        } else if (value1.before(value2)) {
          result = -1;
        } else if (value1.after(value2)) {
          result = 1;
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsDate Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Compare the field value of obj1 and objs2 as DateTime value
   * @param sortField the sort field
   * @param obj1 the first object
   * @param obj2 the first object
   * @return the compare result
   * @throws Exception 
   */
  private int compareAsDateTime(SortDef sortField, TBean obj1, TBean obj2) 
            throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
    
    try {
      Object[] args = null;
      DateTime value1 = (DateTime) sortField.method.invoke(obj1, args);
      DateTime value2 = (DateTime) sortField.method.invoke(obj2, args);
      
      if ((value1 != null) && (value2 != null)) {
        if (value1 == null) {
          result = 1;
        } else if (value2 == null) {
          result = -1;
        } else if (value1.isBefore(value2)) {
          result = -1;
        } else if (value1.isAfter(value2)) {
          result = 1;
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsDateTime Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Compare the field value of obj1 and objs2 as DateTime value
   * @param sortField the sort field
   * @param obj1 the first object
   * @param obj2 the first object
   * @return the compare result
   * @throws Exception 
   */
  private int compareAsEnum(SortDef sortField, TBean obj1, TBean obj2) 
            throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
    
    try {
      Object[] args = null;
      Enum value1 = (Enum) sortField.method.invoke(obj1, args);
      Enum value2 = (Enum) sortField.method.invoke(obj2, args);

      Integer intVal1 = (value1 == null)? null: value1.ordinal();
      Integer intVal2 = (value2 == null)? null: value2.ordinal();
      
      if ((intVal1 != null) && (intVal2 != null)) {
        if (intVal1 == null) {
          result = 1;
        } else if (intVal2 == null) {
          result = -1;
        } else {
          result = intVal1.compareTo(intVal2);
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsDateTime Error:\n " + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Compare the field value of obj1 and objs2 as DateTime value
   * @param sortField the sort field
   * @param obj1 the first object
   * @param obj2 the first object
   * @return the compare result
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  private int compareAsComparable(SortDef sortField, TBean obj1, TBean obj2) 
            throws Exception {
    int result = 0;
    if ((sortField.method == null) && (!sortField.hasError)) {
      return result;
    }
    
    try {
      Object[] args = null;
      Comparable value1 = (Comparable) sortField.method.invoke(obj1, args);
      Comparable value2 = (Comparable) sortField.method.invoke(obj2, args);
      
      if ((value1 != null) && (value2 != null)) {
        if (value1 == null) {
          result = 1;
        } else if (value2 == null) {
          result = -1;
        } else {
          result = value1.compareTo(value2);
        }
      }
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".compareAsDateTime Error:\n " + pExp.getMessage());
    }
    return result;
  }
    
  /**
   * Called the first time the Comparator is used to initiate the method and 
   * CompareType to use in the sorting. Skipped if beanClass or sortDef = null or
   * if sortDef.method is assigned or sortDef.hasError=true.
   * @param beanClass the class of the objects to sort
   * @param sortDef the sorthDef to update.
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  private void onInitMethod(Class beanClass, SortDef sortDef) throws Exception {
    if ((beanClass == null) || (sortDef == null) || (sortDef.method != null) ||
            (sortDef.hasError)) {
      return;
    }
    sortDef.method = null;
    sortDef.compareType = EntityComparator.ASSTRING;
    sortDef.hasError = false;
    if (beanClass == null) {
      throw new Exception("The EntityClass is undetermined");
    }
    
    Class<?>[] pParms = null;
    try {
      sortDef.method = beanClass.getMethod(sortDef.methodName, pParms);
    } catch (NoSuchMethodException | SecurityException pExp) {
      sortDef.method = null;
      throw new NullPointerException("Class[" + beanClass.getSimpleName() +
              "] does not support method[" + sortDef.methodName + "]");
    }
    
    if (sortDef.method == null) {
      sortDef.hasError = true;
      return;
    }
    
    try {
      Class returnClass = sortDef.method.getReturnType();
      if (returnClass.isEnum()) {
        sortDef.compareType = EntityComparator.ASENUM;
      } else if (Comparable.class.isAssignableFrom(returnClass)) {
        sortDef.compareType = EntityComparator.ASCOMPARABLE;
      } else if ((returnClass.equals(byte.class)) || (returnClass.equals(Byte.class))) {
        sortDef.compareType = EntityComparator.ASBYTE;
      } else if ((returnClass.equals(short.class)) || 
                                                  (returnClass.equals(Short.class))) {
        sortDef.compareType = EntityComparator.ASSHORT;
      } else if ((returnClass.equals(int.class)) || 
                                                (returnClass.equals(Integer.class))) {
        sortDef.compareType = EntityComparator.ASINT;
      } else if ((returnClass.equals(long.class)) || 
                                                  (returnClass.equals(Long.class))) {
        sortDef.compareType = EntityComparator.ASLONG;
      } else if ((returnClass.equals(float.class)) || 
                                                  (returnClass.equals(Float.class))) {
        sortDef.compareType = EntityComparator.ASFLOAT;
      } else  if ((returnClass.equals(double.class)) || 
                                                  (returnClass.equals(Double.class))) {
        sortDef.compareType = EntityComparator.ASDOUBLE;
      } else  if (returnClass.equals(Date.class)) {
        sortDef.compareType = EntityComparator.ASDATE;
      } else  if (returnClass.equals(DateTime.class)) {
        sortDef.compareType = EntityComparator.ASDATETIME;
      } 
    } catch (Exception pExp) {
      throw new Exception(this.getClass().getSimpleName()
              + ".onInitMethod Error:\n " + pExp.getMessage());
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Comparator Override">
  /**
   * OVERRIDE: Compare the two Entities based on the specified methods and the method's
   * return types.  Sort it as Float, Integer, or String Values. The latter is the
   * default for return types that cannot be casted as floats or integers.
   * @param pObj1 TBean one 
   * @param obj2 TBean two
   * @return the compare.  1:pObj1 is null or greater than pObj2; 0:equal or both are 
   * null; -1:pObj1 =null or less than pObj2. Reverse order if (!EntitySort.sortAsc).
   */
  @Override
  @SuppressWarnings("unchecked")
  public int compare(TBean obj1, TBean obj2) {
    int result = 0;
    if ((obj1 == null) && (obj2 == null)) {
        result = 0;
    } else if (obj1 == null) {
      result = 1;
    } else if (obj2 == null) {
      result = -1;
    } else if (this.rootSortDef != null) {
      SortDef sortDef = this.rootSortDef;
      while (sortDef != null) {
        try {
          if ((sortDef.method == null) && (!sortDef.hasError)) {
            this.onInitMethod(obj1.getClass(), sortDef);
          }

          if (!sortDef.hasError) {
            if (sortDef.compareType == EntityComparator.ASSTRING) {
              result = this.compareAsString(sortDef, obj1, obj2);
            } else if (sortDef.compareType == EntityComparator.ASENUM) {
              result = this.compareAsEnum(sortDef, obj1, obj2);
            } else if (sortDef.compareType == EntityComparator.ASCOMPARABLE) {
              result = this.compareAsComparable(sortDef, obj1, obj2);
            } else if (sortDef.compareType == EntityComparator.ASDATE) {
              result = this.compareAsDate(sortDef, obj1, obj2);
            } else if (sortDef.compareType == EntityComparator.ASDATETIME) {
              result = this.compareAsDateTime(sortDef, obj1, obj2);
            } else{
              result = this.compareAsNumber(sortDef, obj1, obj2);
            }

            if (result != 0) {
              if (!sortDef.sortAsc) {
                result = -1*result;
              }
              break;
            }
          } 
        } catch (Exception pExp) {
          throw new NullPointerException("Class[" + obj1.getClass().getSimpleName()
                  + "].SortMethod[" + sortDef.methodName
                  + "] Error:\n" + pExp.getMessage());
        }
        sortDef = sortDef.subSort;
      }        
    }
    return result;
  }
  //</editor-fold>
}
