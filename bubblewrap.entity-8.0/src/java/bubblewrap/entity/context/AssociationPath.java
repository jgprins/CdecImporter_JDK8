package bubblewrap.entity.context;

import bubblewrap.core.reflection.ReflectionInfo;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.Objects;

/**
 * The foreignKey association of two entities as define by their {@linkplain 
 * EntityPaths}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class AssociationPath implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Parent's EntityPath
   */
  public final Class parentClass;
  /**
   * The Child's EntityPath
   */
  public final Class childClass;
  /**
   * The association's ForeignKey Type
   */
  public final AssociationType fkType;
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public AssociationPath(EntityPath parentPath, EntityPath childPath, 
                                                              AssociationType fkType) {
    if (parentPath == null) {
      throw new NullPointerException("The Association's Parent Path is undefined.");
    }
    if (childPath == null) {
      throw new NullPointerException("The Association's Child Path is undefined.");
    }
    if ((fkType == null) || (AssociationType.None.equals(fkType))){
      throw new NullPointerException("The Association's ForeignKey Type is undefined.");
    }
    this.parentClass = parentPath.entityClass;
    this.childClass = childPath.entityClass;
    this.fkType = fkType;
  }
  
  /**
   * Public Constructor
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public AssociationPath(Class parentClass, Class childClass, AssociationType fkType) {
    if (parentClass == null) {
      throw new NullPointerException("The Association's Parent Class is undefined.");
    } else if (!ReflectionInfo.isEntity(parentClass)) {
      throw new IllegalArgumentException("The Association's Parent Class[" +
              parentClass.getSimpleName() + "] is not an entity class.");
    }
    if (childClass == null) {
      throw new NullPointerException("The Association's Child Class is undefined.");
    } else if (!ReflectionInfo.isEntity(childClass)) {
      throw new IllegalArgumentException("The Association's Child Class[" +
              childClass.getSimpleName() + "] is not an entity class.");
    }
    if ((fkType == null) || (AssociationType.None.equals(fkType))){
      throw new NullPointerException("The Association's ForeignKey Type is undefined.");
    }
    this.parentClass = parentClass;
    this.childClass = childClass;
    this.fkType = fkType;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Check whether <tt>entityPath</tt> match the AssciationPath's parentClass
   * @param entityPath the path to match
   * @return ((entityPath != null) & (this.parentClass.equals(entityPath.entityClass)))
   */
  public boolean isParent(EntityPath entityPath) {
    return ((entityPath != null) & (this.parentClass.equals(entityPath.entityClass)));
  }
  
  /**
   * Check whether <tt>entityClass</tt> match the AssciationPath's parentClass
   * @param entityClass the class to match
   * @return ((entityPath != null) & (this.parentClass.equals(entityPath)))
   */
  public boolean isParent(Class entityClass) {
    return ((entityClass != null) & (this.parentClass.equals(entityClass)));
  }
  
  /**
   * Check whether <tt>entityPath</tt> match the AssciationPath's childClass
   * @param entityPath the path to match
   * @return ((entityPath != null) & (this.childClass.equals(entityPath.entityClass)))
   */
  public boolean isChild(EntityPath entityPath) {
    return ((entityPath != null) & (this.childClass.equals(entityPath.entityClass)));
  }
  
  /**
   * Check whether <tt>entityClass</tt> match the AssciationPath's childClass
   * @param entityClass the class to match
   * @return ((entityPath != null) & (this.childClass.equals(entityClass)))
   */
  public boolean isChild(Class entityClass) {
    return ((entityClass != null) & (this.childClass.equals(entityClass)));
  }  
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: returns "Association[parent = " + this.parentClass.toString() 
 + "; child = " + this.childClass.toString() + "; type = " + this.fkType.name() 
 + "]"
   */
  @Override
  public String toString() {
    return "Association[parent = " + this.parentClass.toString() + "; child = " 
            + this.childClass.toString() + "; type = " + this.fkType.name() + "]";
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of AssociationPath.
   * Otherwise, return true if the following match: parent and child class fields.</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof AssociationPath));
    if (result) {
      AssociationPath trgObj = (AssociationPath) obj;
      result = ((DataEntry.isEq(this.parentClass,trgObj.parentClass))
              && (DataEntry.isEq(this.childClass,trgObj.childClass)));
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode on all fields</p>
   */
  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + Objects.hashCode(this.parentClass);
    hash = 97 * hash + Objects.hashCode(this.childClass);
    hash = 97 * hash + Objects.hashCode(this.fkType);
    return hash;
  }
  //</editor-fold>
}
