package bubblewrap.core.enums;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * An Set of BwEnum of a specified class {@linkplain #elementType}. New values can added
 * or removed from the set.
 * @author kprins
 */
public class BwEnumSet<TEnum extends BwEnum<TEnum>> extends AbstractSet<TEnum>
    implements Cloneable, java.io.Serializable{
 
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * <p>Creates an new empty instance if BwEnumSet class that extends BwEnumSet.
   * </p> 
   * <p><b>Note:</b> The BwEnumSet class must support a public parameterless 
   * constructor.</p>
   * @param <TSet> extends BwEnumSet
   * @param <E> extends BwEnum
   * @param enumSetClass the class that extends BwEnumSet
   * @return a new empty instance of the class
   * @exception IllegalArgumentException if initiating the new instance fail because 
   * the specified class does not support a parameterless constructor or the constructor
   * threw an exception.
   */
  public static <TSet extends BwEnumSet<E>, E extends BwEnum<E>> TSet 
                                               instanceOf(Class<TSet> enumSetClass) {
    TSet result = null;
    if (enumSetClass == null) {
      throw new NullPointerException("The BwEnumSet Class cannot be unassigned.");
    }
    try{
      result = enumSetClass.newInstance();
    } catch (InstantiationException exp1) {
      throw new IllegalArgumentException("BwEnumSet.noneOf Error:\n " 
              + "BwEnumSet[" + enumSetClass.getSimpleName() + "] does not support "
              + "a parameterless constructor or the constructor thro an exception.");
    } catch (IllegalAccessException exp2) {
      throw new IllegalArgumentException("BwEnumSet.noneOf Error:\n " 
              +  exp2.getMessage());
    }
    return result;
  }
  
  /**
   * Creates an empty BwEnum set with the specified element type.
   *
   * @param elementType the class object of the element type for this BwEnumset
   * @throws NullPointerException if <tt>elementType</tt> is null
   * @throws ClassCastException if the elemtType has no assigned enum values
   */
  public static <E extends BwEnum<E>> BwEnumSet<E> 
                                                noneOf(Class<? extends E> elementType) {
    BwEnumSet<E> result = null;
    if (elementType == null) {
      throw new NullPointerException("The BwElement Class cannot be unassigned.");
    }
    result = new BwEnumSet<>(elementType);
    return result;
  }
  
  /**
   * Creates an BwEnum set containing all of the elements in the specified element type.
   *
   * @param elementType the class object of the element type for this BwEnum set
   * @throws NullPointerException if <tt>elementType</tt> is null
   * @throws ClassCastException if the elemtType has no assigned enum values
   */
  public static <E extends BwEnum<E>> BwEnumSet<E> 
                                                allOf(Class<? extends E> elementType) {
    BwEnumSet<E> result = noneOf(elementType);
    result.addAll();
    return result;
  }
  
  /**
   * Creates a BwEnum set with the same element type as the specified enum set,
   * initially containing the same elements (if any).
   *
   * @param s the BwEnum set from which to initialize this BwEnum set
   * @throws NullPointerException if <tt>s</tt> is null
   */
  public static <E extends BwEnum<E>> BwEnumSet<E> copyOf(BwEnumSet<E> srcSet) {
    return srcSet.clone();
  }
  
  /**
   * Creates a BwEnum set initially containing the specified element.
   * @param e the element that this set is to contain initially
   * @throws NullPointerException if <tt>e</tt> is null
   * @return an enum set initially containing the specified element
   */
  public static <E extends BwEnum<E>> BwEnumSet<E> of(E e) {
    BwEnumSet<E> result = noneOf(e.getDeclaringClass());
    result.add(e);
    return result;
  }
  
  /**
   * Creates an enum set initially containing the specified elements. This factory,
   * whose parameter list uses the varargs feature, may be used to create an enum set
   * initially containing an arbitrary number of elements, but it is likely to run
   * slower than the overloads that do not use varargs.
   * @param enumArr the elements the set is to contain initially
   * @throws NullPointerException if any of the specified elements are null, or if
   * <tt>enumArr</tt> is null
   * @return an enum set initially containing the specified elements
   */
  @SafeVarargs
  public static <E extends BwEnum<E>> BwEnumSet<E> of(E...enumArr) {
    if ((enumArr == null) || (enumArr.length == 0)) {
      throw new NullPointerException("The Array of enums cannot be unassigned "
              + "or empty.");
    }
    BwEnum<E> first = (BwEnum<E>)enumArr[0];
    BwEnumSet<E> result = BwEnumSet.noneOf(first.getDeclaringClass());
    for (E enumVal : enumArr) {
      result.add(enumVal);
    }
    return result;
  }
  
  /**
   * Creates a BwEnum set initially containing the specified elements.
   * @param enumCol the elements the set is to contain initially
   * @throws NullPointerException if any of the specified elements are null, or if
   * <tt>enumCol</tt> is null
   * @return an enum set initially containing the specified elements
   */
  public static <E extends BwEnum<E>> BwEnumSet<E> of(Collection<E> enumCol) {
    if ((enumCol == null) || (enumCol.isEmpty())) {
      throw new NullPointerException("The Collection of enums cannot be unassigned "
              + "or empty.");
    }
    BwEnumSet<E> result = null;
    for (E enumVal : enumCol) {
      if (result == null) {
        result = BwEnumSet.noneOf(enumVal.getDeclaringClass());
      }
      result.add(enumVal);
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The class of all the elements of this set.
   */
  public final Class<? extends TEnum> elementType;
  /**
   * All of the values comprising T. (Cached for performance.)
   */
  private final List<TEnum> allValues;
  /**
   * The Values added to the set
   */
  private List<TEnum> setValues;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Private Constructor
   *
   * @param elementType the element class
   * @param values all values supported by the BwEnum class
   */
  BwEnumSet(Class<? extends TEnum> elementType) {
    this.elementType = elementType;
    this.allValues = BwEnum.getEnumValues(elementType);
    if ((this.allValues == null) || (this.allValues.isEmpty())) {
      throw new ClassCastException(elementType + " is not a valid BwEnum.");
    }
    this.setValues = new ArrayList<>();
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Complements the contents of the set Values
   */
  public void complement() {
    for (TEnum enumVal : this.allValues) {
      if (this.setValues.contains(enumVal)) {
        this.setValues.remove(enumVal);
      } else {
        this.setValues.add(enumVal);
      }
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Implement/Override AbstractSet">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: get an iterator on the set values</p>
   */
  @Override
  public Iterator<TEnum> iterator() {
    return this.setValues.iterator();
  }
  
  /**
   * Get the Size of the Set
   * @return the size of the set values
   */
  @Override
  public int size() {
    return this.setValues.size();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Add enumVal to the set values</p>
   * @throws  IllegalArgumentException if the Enum values is not a declared Enum Value.
   */
  @Override
  public boolean add(TEnum enumVal) {
    if (enumVal == null) {
      return false;
    }
    
    if (!this.allValues.contains(enumVal)) {
      throw new IllegalArgumentException("Enum[" + enumVal.toString() 
              + "] is an invalid Class[" + this.elementType.getSimpleName() 
              + "] value");
    }
    return this.setValues.add(enumVal);
  }

  /**
   * Add a collection of values to the setValue - skip duplicates and null values
   * @param enumCol an array of values
   * @return true is all values was successfully added
   */
  @Override
  public boolean addAll(Collection<? extends TEnum> enumCol) {
    boolean result = true;
    if ((enumCol != null) && (!enumCol.isEmpty())) {
      for (TEnum enumVal : enumCol) {
        if (enumVal == null) {
          continue;
        }
        
        if (!this.allValues.contains(enumVal)) {
          throw new IllegalArgumentException("Enum[" + enumVal.toString() 
                  + "] is an invalid Class[" + this.elementType.getSimpleName() 
                  + "] value");
        }
        if (!this.setValues.contains(enumVal)) {
          result |= this.setValues.add(enumVal);
        }
      }
    }
    return result;
  }
  
  /**
   * Add an array of values to the setValue - skip duplicates
   * @param enumArr an array of values
   * @return true is all values was successfully added
   */
  @SuppressWarnings("unchecked")
  public boolean addAll(TEnum...enumArr) {
    boolean result = true;
    if ((enumArr != null) && (enumArr.length > 0)) {
      for (TEnum enumVal : enumArr) {
        if (!this.setValues.contains(enumVal)) {
          result |= this.setValues.add(enumVal);
        }
      }
    }
    return result;
  }
  
  /**
   * Adds all of the elements from the appropriate enum type to this enum set, which is
   * empty prior to the call.
   */
  protected void addAll() {
    this.addAll(this.allValues);
  }  

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true if other!=null and of type TEnum and contained in the
   * Set's values.</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean contains(Object other) {
    boolean result = ((other != null) && (this.elementType.equals(other.getClass())));
    if (result) {
      TEnum otherEnum = (TEnum) other;
      result = this.setValues.contains(otherEnum);
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Check if the set values contains the all values in the collection</p>
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    return this.setValues.containsAll(c);
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Clear the set values</p>
   */
  @Override
  public void clear() {
    this.setValues.clear();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true is the set Value list is empty</p>
   */
  @Override
  public boolean isEmpty() {
    return this.setValues.isEmpty();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return an array of set values</p>
   */
  @Override
  public Object[] toArray() {
    return this.setValues.toArray();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return an array of set values</p>
   */
  @Override
  public <T> T[] toArray(T[] a) {
    return this.setValues.toArray(a);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a shallow clone of this BwEnumSet</p>
   */
  @Override
  protected BwEnumSet<TEnum> clone() {
    BwEnumSet<TEnum> result = null;
    try {
      result = new BwEnumSet<>(this.elementType);
      result.addAll(this.setValues);
    } catch (Exception exp) {
      throw new AssertionError(exp);
    }
    return result;
  }
  //</editor-fold>
}
