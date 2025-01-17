package bubblewrap.core.enums;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;

/**
 * An Set of BwEnum of a specified class {@linkplain #elementType}. New values can added
 * or removed from the set.
 * @author kprins
 */
public class EnumFlagSet<TEnum extends Enum<TEnum>> extends AbstractSet<TEnum>
                                                    implements Cloneable, Serializable {
 
  //<editor-fold defaultstate="collapsed" desc="Static Methods">
  /**
   * Creates an empty BwEnum set with the specified element type.   
   * @param elementType the class object of the element type for this BwEnumset
   * @throws NullPointerException if <tt>elementType</tt> is null
   * @throws ClassCastException if the elemtType has no assigned enum values
   */
  public static <E extends Enum<E>> EnumFlagSet<E> noneOf(Class<E> elementType) {
    EnumFlagSet<E> result = null;
    if (elementType == null) {
      throw new NullPointerException("The BwElement Class cannot be unassigned.");
    }
    result = new EnumFlagSet<>(elementType);
    return result;
  }
  
  /**
   * Creates an BwEnum set containing all of the elements in the specified element type.
   *
   * @param elementType the class object of the element type for this BwEnum set
   * @throws NullPointerException if <tt>elementType</tt> is null
   * @throws ClassCastException if the elemtType has no assigned enum values
   */
  public static <E extends Enum<E>> EnumFlagSet<E> allOf(Class<E> elementType) {
    EnumFlagSet<E> result = noneOf(elementType);
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
  public static <E extends Enum<E>> EnumFlagSet<E> copyOf(EnumFlagSet<E> srcSet) {
    return srcSet.clone();
  }
  
  /**
   * Creates a BwEnum set initially containing the elements as specified by the the
   * bitmap flag 
   * @param elementType the class object of the element type for this BwEnum set
   * @param e bitmap flag
   * @throws NullPointerException if <tt>elementType</tt> is null
   * @return an enum set initially containing the specified element
   */
  public static <E extends Enum<E>> EnumFlagSet<E> 
                                        of(Class<E> elementType, IntFlag e) {
    if (elementType == null) {
      throw new NullPointerException("The BwElement Class cannot be unassigned.");
    }
    return new EnumFlagSet<>(elementType, e);
  }
  
  /**
   * Creates a BwEnum set initially containing the specified element.
   * @param e the element that this set is to contain initially
   * @throws NullPointerException if <tt>e</tt> is null
   * @return an enum set initially containing the specified element
   */
  public static <E extends Enum<E>> EnumFlagSet<E> of(E e) {
    EnumFlagSet<E> result = noneOf(e.getDeclaringClass());
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
  public static <E extends Enum<E>> EnumFlagSet<E> of(E...enumArr) {
    EnumFlagSet<E> result = null;
    if ((enumArr != null) && (enumArr.length >= 0)) {
      for (E enumVal : enumArr) {
        if (enumVal == null) {
          continue;
        }
        if (result == null) {
          result = EnumFlagSet.noneOf(enumVal.getDeclaringClass());
        }
        result.add(enumVal);
      }
    }
    if (result == null) {
      throw new NullPointerException("The Array of enums cannot be unassigned "
              + "or empty.");
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
  public static <E extends Enum<E>> EnumFlagSet<E> of(Collection<E> enumCol) {
    EnumFlagSet<E> result = null;
    if ((enumCol != null) && (!enumCol.isEmpty())) {
      for (E enumVal : enumCol) {
        if (enumVal == null) {
          continue;
        }
        if (result == null) {
          result = EnumFlagSet.noneOf(enumVal.getDeclaringClass());
        }
        result.add(enumVal);
      }
    }
    if (result == null) {
      throw new NullPointerException("The Collection of enums cannot be unassigned "
              + "or empty.");
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The class of all the elements of this set.
   */
  public final Class<TEnum> elementType;
  /**
   * All of the values comprising T. (Cached for performance.)
   */
  private final EnumSet<TEnum> allValues;
  /**
   * The Values added to the set
   */
  private EnumSet<TEnum> setValues;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Protector Constructor   
   * @param elementType the element class
   */
  protected EnumFlagSet(Class<TEnum> elementType) {
    this.elementType = elementType;
    if (!IEnumFlag.class.isAssignableFrom(elementType)) {
      throw new ClassCastException("Invalid Enum Type. Expected an Enum that "
              + "implements an IEnumFlag interface.");
    }
    this.allValues = EnumSet.allOf(elementType);
    if ((this.allValues == null) || (this.allValues.isEmpty())) {
      throw new ClassCastException(elementType + " is not a valid Enum.");
    }
    this.setValues = EnumSet.noneOf(elementType);
  }
  
  /**
   * A Protector Constructor with element as defined by the specified bitmap
   * @param elementType the element class
   * @param bitMap The BitMap flag the include all the set values.
   */
  private EnumFlagSet(Class<TEnum> elementType, IntFlag bitMap) {
    this(elementType);
    this.setAsBitmap(bitMap);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Complements the contents of the set Values
   */
  public final void complement() {
    for (TEnum enumVal : this.allValues) {
      if (this.setValues.contains(enumVal)) {
        this.setValues.remove(enumVal);
      } else {
        this.setValues.add(enumVal);
      }
    }
  }
  
  /**
   * Get the EnumFlagSet's Bitmap value. 
   * @return 0 if empty or a bitmap mask of all set values
   */
  public final IntFlag getAsBitmap() {
    IntFlag result = IntFlag.ZERO;
    if (!this.isEmpty()) {
      for (TEnum enumVal : this.setValues) {
        IEnumFlag enumFlag = (IEnumFlag) enumVal;
        result = result.or(enumFlag.getValue());
      }
    }
    return result;
  }
  
  /**
   * Set the Set's value using the bitmap of the set values.  If bitmap=null or the 
   * BwEnumFlag does not support a zero-value, return and empty set. if 
   * the BwEnumFlag supports 0 add the enum. Otherwise, add all
   * BwEnumFlag value where {@linkplain BwEnumFlag#isSet(int) isSet(bitmap)}=true,
   * excluding an enum where enum.value=0.
   * @param bitmap a bitmap of all set values.
   */
  public final void setAsBitmap(IntFlag bitmap) {
    this.clear();
    if (bitmap != null) {
      if (IntFlag.ZERO.equals(bitmap)) {
        for (TEnum enumVal : this.allValues) {
          IEnumFlag enumFlag = (IEnumFlag) enumVal;
          if (IntFlag.ZERO.equals(enumFlag.getValue())) {
            this.add(enumVal);
          }
        }
      } else {
        for (TEnum enumVal : this.allValues) {
          IEnumFlag enumFlag = (IEnumFlag) enumVal;
          IntFlag intFlag = (enumFlag != null)? enumFlag.getValue(): null;
          if ((intFlag != null) && (!IntFlag.ZERO.equals(intFlag)) && 
                  (bitmap.isMatch(intFlag))) {
            this.add(enumVal);
          }
        }
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
  public final Iterator<TEnum> iterator() {
    return this.setValues.iterator();
  }
  
  /**
   * Get the Size of the Set
   * @return the size of the set values
   */
  @Override
  public final int size() {
    return this.setValues.size();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Add enumVal to the set values</p>
   * @throws  IllegalArgumentException if the Enum values is not a declared Enum Value.
   */
  @Override
  public final boolean add(TEnum enumVal) {
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
  public final boolean addAll(Collection<? extends TEnum> enumCol) {
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
  public final boolean addAll(TEnum...enumArr) {
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
  protected final void addAll() {
    this.addAll(this.allValues);
  }  

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true if other!=null and of type TEnum and contained in the
   * Set's values.</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public final boolean contains(Object other) {
    boolean result = ((other != null) && (this.elementType.isInstance(other)));
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
  public final boolean containsAll(Collection<?> c) {
    return this.setValues.containsAll(c);
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Clear the set values</p>
   */
  @Override
  public final void clear() {
    this.setValues.clear();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true is the set Value list is empty</p>
   */
  @Override
  public final boolean isEmpty() {
    return this.setValues.isEmpty();
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return an array of set values</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public final TEnum[] toArray() {
    return DataEntry.toArray(this.elementType, this.setValues);
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return an array of set values</p>
   */
  @Override
  public final <T> T[] toArray(T[] a) {
    return this.setValues.toArray(a);
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a shallow clone of this BwEnumSet</p>
   */
  @Override
  public EnumFlagSet<TEnum> clone() {
    EnumFlagSet<TEnum> result = null;
    try {
      result = new EnumFlagSet<>(this.elementType);
      result.addAll(this.setValues);
    } catch (Exception exp) {
      throw new AssertionError(exp);
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return true obj != null, and instacneOf BwEnumFlagSet, has
   * matching elementTypes, and bitmaps</p>
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EnumFlagSet));
    if (result) {
      EnumFlagSet other = (EnumFlagSet) obj;
      result = (this.elementType.equals(other.elementType));
      if (result) {
        IntFlag otherBit = other.getAsBitmap();
        IntFlag thisBit = this.getAsBitmap();
        result = DataEntry.isEq(otherBit, thisBit);
      }
    }
    return result;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return  a HashCode using the ElementType and set Values</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 59 * hash + Objects.hashCode(this.elementType);
    hash = 59 * hash + Objects.hashCode(this.setValues);
    return hash;
  }
  
  
  @Override
  public String toString() {
    return super.toString();
  }
  //</editor-fold>
}
