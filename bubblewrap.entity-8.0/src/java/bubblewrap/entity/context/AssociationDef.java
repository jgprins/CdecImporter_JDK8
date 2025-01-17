package bubblewrap.entity.context;

import bubblewrap.entity.annotations.Association;
import java.io.Serializable;
import java.util.EnumSet;
import javax.persistence.*;

/**
 * Wrapper for the {@linkplain OneToOne}, {@linkplain OneToMany}, 
 * {@linkplain ManyToOne}, and {@linkplain ManyToMany} annotations as assigned to a
 * entity field's {@linkplain FieldInfo}. It also support to use the {@linkplain 
 * Association} annotation to designate the OwnerParent and indicate whether the
 * EntityWrapper(s) in the association should be cached by its parent or child.
 * <p><b>Note:</b> A child can have only one owner-parent - the parent through which is
 * loaded and deleted and which maintains the master list of child entity beans. If the
 * child entity is refreshed, it is done either through owner parent (i.e., if the 
 * parent association's cascade setting includes {@linkplain CascadeType#REFRESH 
 * REFRESH}) or should be synchronized with the parent child collection.</p>
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class AssociationDef<TBean extends Serializable> implements Serializable {
  
  /**
   * Get an EnumSet of type CascadeType with all CascadeTypes in <tt>cascadeArr</tt>.
   * If the array include {@linkplain CascadeType#ALL} it returns {@linkplain 
   * #getCascadeSetAll() getCascadeSetAll}.
   * @param cascadeArr the array of CascadeTypes
   * @return the EnumSet (can be empty, but always not null).
   */
  public static EnumSet<CascadeType> getCascadeSet(CascadeType[] cascadeArr) {
    EnumSet<CascadeType> result = EnumSet.noneOf(CascadeType.class);    
    if ((cascadeArr != null) && (cascadeArr.length > 0)) {
      for (CascadeType type : cascadeArr) {
        if (!CascadeType.ALL.equals(type)) {
          result = AssociationDef.getCascadeSetAll();
          break;
        } else {
          result.add(type);
        }
      }
    }
    return result;
  }
  
  /**
   * Get an EnumSet of type CascadeType with all CascadeTypes except type {@linkplain 
   * CascadeType#ALL}
   * @return the CascadeType EnumSet
   */
  public static EnumSet<CascadeType> getCascadeSetAll() {
    EnumSet<CascadeType> result = EnumSet.noneOf(CascadeType.class);    
    for (CascadeType type : CascadeType.values()) {
      if (!CascadeType.ALL.equals(type)) {
        result.add(type);
      }
    }
    return result;
  }
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The EntityPath containing the association's targetEntity's class and the 
   * targetEntity's associated field
   */
  public final EntityPath<TBean> targetPath;
  /**
   * The association's cascade type settings
   */
  public final EnumSet<CascadeType> cascade;
  /**
   * The flag indicating if the mappedBy field is nullable.
   */
  public final boolean isNullable;
  /**
   * The ForeignKey Association  Type
   */
  public final AssociationType type;
  /**
   * A flag set if this is the parent in the child-parent association
   */
  public final boolean isParent;  
  /**
   * A flag indicating whether EntityWrapper of the Entity should be cached by it 
   * parent or child.
   */
  public final boolean cache; 
  /**
   * The association's ForeignKey Type
   */
  private Boolean ownerParent;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor for a OneToMany Association - called by a Child field in a
   * one-to-many Association to set the child association as seen from its parent.
   * This.isParent = false, because this defines the parent's
   * association with a child
   * @param annot this parent field's OneToMany annotation.
   * @param targetPath the target entityPath of the associated child entity
   * @param cache (optional) set flag if the child wrappers should be cached 
   * (can be null).
   */
  public AssociationDef(OneToMany annot, EntityPath<TBean> targetPath,
          Boolean cache, Boolean byPrimaryKey) {
    super();
    if (annot == null) {
      throw new NullPointerException("The Association OneToMany Annotation cannot be "
              + "undefined");
    }
    if (targetPath == null) {
      throw new NullPointerException("The OneToMany Association's target EntityPath "
              + "cannot be undefined");
    }
    
    this.targetPath = targetPath;
    this.cascade = AssociationDef.getCascadeSet(annot.cascade());
    this.isNullable = true;
    this.type = AssociationType.OneToMany;
    this.isParent = false;
    this.cache = (cache == null)? false: cache;
    this.ownerParent = null;
  }
  
  /**
   * Public Constructor for a ManyToOne Association - called by a Child field in a
   * one-to-many Association to set the parent association as seen from the child.
   * @param annot this parent field's ManyToOne annotation.
   * @param targetPath the target entityPath of the associated parent
   * @param cache (optional) set flag is the parent wrapper should be cached by its
   * children (can be null).
   * @param ownerParent (optional) true if this parent is the child's owner-parent. 
   * (can be null).
   */
  public AssociationDef(ManyToOne annot, EntityPath<TBean> targetPath,
          Boolean cache, Boolean ownerParent, Boolean byPrimaryKey) {
    super();
    if (annot == null) {
      throw new NullPointerException("The ManyToOne Annotation cannot be undefined");
    }
    if (targetPath == null) {
      throw new NullPointerException("The ManyToOne Association's Target EntityPath "
              + "cannot be undefined.");
    }
    
    this.targetPath = targetPath;
    this.cascade = AssociationDef.getCascadeSet(annot.cascade());
    this.isNullable = true;
    this.type = AssociationType.OneToMany;
    this.isParent = true;
    this.cache = (cache == null)? false: cache;
    this.ownerParent = ((ownerParent == null) || (!ownerParent))? null: ownerParent;
  }
  
  /**
   * Public Constructor for a OneToOne Association - called by a Child field in a
   * one-to-one Association to set the child or parent association as seen from the
   * parent or child, respectively.
   * @param annot this source field's OneToOne annotation.
   * @param targetPath the target entityPath of the associated parent/child
   * @param cache (optional) set flag is the parent/child wrapper should be cached 
   * (can be null).
   * @param isParent (optional) true to indicate the source (not the target) entity is 
   * the parent entity in the  association (default = null|false).
   * @param ownerParent (optional) true if this parent is the child's owner-parent. 
   * (can be null) - ignored is isParent = null|false.
   */
  public AssociationDef(OneToOne annot, EntityPath<TBean> targetPath,
          Boolean cache, Boolean isParent, Boolean ownerParent) {
    super();
    if (annot == null) {
      throw new NullPointerException("The source field's OneToOne Annotation cannot "
              + "be undefined");
    }
    if (targetPath == null) {
      throw new NullPointerException("The ManyToOne Association's Target EntityPath "
              + "cannot be undefined.");
    }
    
    this.targetPath = targetPath;
    this.cascade = AssociationDef.getCascadeSet(annot.cascade());
    this.isNullable = true;
    this.type = AssociationType.OneToMany;
    this.isParent = (isParent == null)? false: isParent;
    this.cache = (cache == null)? false: cache;
    this.ownerParent = ((!this.isParent) || 
            (ownerParent == null) || (!ownerParent))? null: ownerParent;
  }
  
  /**
   * Public Constructor for a ManyToMany Association - called by a Child field in a
   * ManyToMany Association to set the child or parent association as seen from the
   * parent or child, respectively. 
   * <p><b>Note:</b>this.byPrimaryKey = false</p>
   * @param annot this source field's ManyToOne annotation.
   * @param targetEntity the target entityPath of the associated parent/child
   * @param cache (optional) set flag is the parent/child wrapper should be cached 
   * (can be null).
   * @param isParent (optional) true to indicate the source (not the target) entity is 
   * the parent entity in the  association (default = null|false).
   * @param ownerParent (optional) true if this parent is the child's owner-parent. 
   * (can be null) - ignored is isParent = null|false.
   */
  public AssociationDef(ManyToMany annot, EntityPath<TBean> targetPath,
          Boolean cache, Boolean isParent, Boolean ownerParent) {
    super();
    if (annot == null) {
      throw new NullPointerException("The source field's ManyToMany Annotation cannot "
              + "be undefined");
    }
    if (targetPath == null) {
      throw new NullPointerException("The ManyToMany Association's Target EntityPath "
              + "cannot be undefined.");
    }
    
    this.targetPath = targetPath;
    this.cascade = AssociationDef.getCascadeSet(annot.cascade());
    this.isNullable = true;
    this.type = AssociationType.OneToMany;
    this.isParent = (isParent == null)? false: isParent;
    this.cache = (cache == null)? false: cache;
    this.ownerParent = ((!this.isParent) || 
            (ownerParent == null) || (!ownerParent))? null: ownerParent;
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

  //<editor-fold defaultstate="collapsed" desc="Private Methods">  
  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Called to set a Parent Association as the Child entity's OwnerParent. Ignored
   * if (!this.isParent). Note only one parent can be a child OwnerParent
   */
  public void setAsOwnerParent() {
    if (this.isParent) {
      this.ownerParent = true; 
    }
  }
  
  /**
   * Get whether this parent is the child's OwnerParent
   * @return the assigned setting
   */
  public boolean isOwnerParent() {
    return ((this.ownerParent != null) && (this.ownerParent));
  }
    
  /**
   * Get the targetPath's FieldInfo
   * @return this.targetPath.getFieldInfo()
   */
  public FieldInfo getTargetFieldInfo() {
    return this.targetPath.getFieldInfo();
  }
    
  /**
   * Get the targetPath's FieldInfo.returnType
   * @return this.targetPath.fieldInfo.retyurnType or Void.class if the fieldInfo is
   * not accessible.
   */
  public Class getTargetReturnType() {
    FieldInfo targetField = this.getTargetFieldInfo();
    return (targetField == null)? Void.class: targetField.returnType;
  }
  
  /**
   * Get whether this association's target field's returnType is the target
   * @param sourceType the class of the matching source
   * @return true if this.targetPath.fieldInfo.returnType matches <tt>sourceType</tt>
   */
  public boolean isTargetReturnType(Class sourceType) {
    Class returnType = this.getTargetReturnType();
    return ((sourceType != null) && (sourceType.equals(returnType)));
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  @Override
  public String toString() {
    return super.toString();
  }
  //</editor-fold>
}
