package bubblewrap.entity.context;

/**
 * An Enum that defines the ForeignKey relationship type for a {@linkplain 
 * ForeignKey}.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public enum AssociationType {
  None,
  OneToOne,
  OneToMany,
  ManyToMany;
  
  /**
   * Get theAssocoationTypee by a field annotation class. OneToOne = OneToOne;
   * OneToMany | ManyToOne = OneToMany; ManyToMany = ManyToMany.
   * @param annotClass annotation class that defines the ForeignKey relationship type.
   * @return OneToOne = OneToOne;  OneToMany | ManyToOne = OneToMany; 
   * ManyToMany = ManyToMany.
   */
  public AssociationType byAnnotClass(Class annotClass) {
    AssociationType result = None;
    if (annotClass != null) {
      if (javax.persistence.OneToOne.class.equals(annotClass)) {
        result = OneToOne;
      } else if ((javax.persistence.OneToMany.class.equals(annotClass)) ||
                 (javax.persistence.ManyToOne.class.equals(annotClass))){
        result = OneToMany;
      } else if (javax.persistence.ManyToMany.class.equals(annotClass)) {
        result = ManyToMany;
      } 
    }
    return result;
  }
}
