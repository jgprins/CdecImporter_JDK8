package bubblewrap.entity.context;

import java.io.Serializable;
import java.util.Objects;

/**
 * An class that capture the ForeignKey relationship settings between two classes
 * (a parent and child class)
 * @author kprins
 */
public class ForeignKey implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Publci Final Fields">
  /**
   * The class of the parent bean
   */
  public final AssociationPath associationPath;
  /**
   * The class of the Parent Association Definition
   */
  public final AssociationDef parentAssociation;
  /**
   * The class of the Child Association Definition
   */
  public final AssociationDef childAssociation;
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A Flag indicating the parent Wrapper is the child's Owner Parent (i.e., the 
   * child wrapper entity should be sync-ed with the matching entity in the parent
   * child collection).
   */
  private Boolean ownerParent;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">  
  /**
   * Public Constructor with a defined parent-child Association Definition of the same
   * ForeignKey Type
   * @param parentAssoc the parent association definitions
   * @param childAssoc the child association definitions
   */
  public ForeignKey(AssociationDef parentAssoc, AssociationDef childAssoc) {
   
    if (parentAssoc == null) {
      throw new NullPointerException("The ForeingKey Constraint's Parent Association "
              + "cannot be unassigned.");
    }
    if (childAssoc == null) {
      throw new NullPointerException("The ForeingKey Constraint's Parent Association "
              + "cannot be unassigned.");
    }
    
    if (!parentAssoc.type.equals(childAssoc.type)) {
      throw new IllegalArgumentException("The Parent ForeignKeyType[" 
              + parentAssoc.type + "] does not macth the Chiild ForeignKeyType[" 
              + childAssoc.type + "].");
    }
    
    this.associationPath = new AssociationPath(parentAssoc.targetPath.entityClass,
            childAssoc.targetPath.entityClass, parentAssoc.type);
    this.parentAssociation = parentAssoc;
    this.childAssociation = childAssoc;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * Get the parent class
   * @return the assigned class
   */
  public EntityPath getParentPath() {
    return this.parentAssociation.targetPath;
  }
  
  /**
   * Get the child class
   * @return the assigned class
   */
  public EntityPath getChildPath() {
    return this.childAssociation.targetPath;
  }
  
  /**
   * Get whether this is a parent-child relational is optional (Default=false)
   * @return true if optional 
   */
  public boolean isNullable() {
    return this.childAssociation.isNullable;
  }
    
  /**
   * Get whether this parent wrapper is the child wrapper's owner (i.e., the 
   * child wrapper entity should be sync-ed with the matching entity in the parent
   * child collection). 
   * @see #setOwnerParent(boolean) setOwnerParent for constraints
   * @return true if optional 
   */
  public boolean isOwnerParent() {
    return (parentAssociation.isOwnerParent());
  }
  
  /**
   * Called to set this.isOwnerParent. It calls {@linkplain 
   * AssocationDef#setAsOwnerParent()  this.parentAssociation.setAsOwnerParent()}/p>
   * @param isOwnerParent true to set the parent in the association as the OwnerParent.
   */
  public void setOwnerParent(boolean isOwnerParent) {
    this.parentAssociation.setAsOwnerParent();
  }
    
  /**
   * Check if the specified EntityPath matched is the parentAssociation.enityPath
   * @param beanClass class of interest
   * @return true the class matches the parent class
   */
  public boolean isParent(EntityPath entityPath) {
    return this.parentAssociation.targetPath.equals(entityPath);
  }
    
  /**
   * Check if the specified class is the association's parent class
   * @param beanClass class of interest
   * @return true the class matches the parent class
   */
  public boolean isParent(Class parentClass) {
    return this.associationPath.isParent(parentClass);
  }
  
  /**
   * Check if the specified class matches the childAssociation.enityPath
   * @param beanClass class of interest
   * @return true the class matches the child class
   */
  public boolean isChild(EntityPath entityPath) {
    return this.childAssociation.targetPath.equals(entityPath);
  }
    
  /**
   * Check if the specified class is the association's child class
   * @param childClass class of interest
   * @return true the class matches the child class
   */
  public boolean isChild(Class childClass) {
    return this.associationPath.isChild(childClass);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of ForeignKey.
   *  Otherwise, return true if the following match: 
   * (this.associationPath.equals(trgObj.associationPath))</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof ForeignKey));
    if (result) {
      ForeignKey trgObj = (ForeignKey) obj;
      result = (this.associationPath.equals(trgObj.associationPath));
    }
    return result;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 79 * hash + Objects.hashCode(this.associationPath);
    return hash;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return "ForeignKey[ path = " + 
 this.associationPath.toString() + "]";</p>
   */
  @Override
  public String toString() {
    String result = "ForeignKeyConstraint[ path = " + this.associationPath.toString()
                   + "]";
    return result;
  }
  //</editor-fold>
}
