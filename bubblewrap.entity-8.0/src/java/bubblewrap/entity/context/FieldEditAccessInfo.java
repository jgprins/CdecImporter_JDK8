package bubblewrap.entity.context;

import bubblewrap.admin.context.AdminContext;
import bubblewrap.entity.annotations.FieldEditAccess;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.FieldEditFlags;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * AWrapepr for a Field's FieldEditAccess settings maintained in {@linkplain FieldInfo}
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class FieldEditAccessInfo implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The EntityWrapper class to Edit Access constraints applied. If
   * EntityWrapper.Void.class it applies to all Wrapper Classes.
   */
  public final Class<? extends EntityWrapper> wrapperClass;
  /**
   * The Access Flag managing the field edit access to the entity field. 
   * @see FieldEditFlags for edit access options
   */
  public final EnumSet<FieldEditFlags> accessFlags;  
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor for a default FieldEditAccessInfo (i.e., wrapperClass = 
   * EntityWrapper.Void.class, accessFlags = EnumSet{FieldEditFlags.NoConstaints}
   */
  public FieldEditAccessInfo() {
    this.wrapperClass = EntityWrapper.Void.class;
    this.accessFlags = EnumSet.of(FieldEditFlags.NoConstaints);
  }

  /**
   * Constructor from a {@linkplain FieldEditAccess} annotation
   * @param accessAnnot 
   */
  public FieldEditAccessInfo(FieldEditAccess accessAnnot) {
    if (accessAnnot == null) {
      throw new NullPointerException("The FieldEditAccessInfo's FieldEditAccess "
              + "annotion reference cannot be unassigned.");
    }
    this.wrapperClass = (accessAnnot.wrapperClass() == null)? 
                              EntityWrapper.Void.class: accessAnnot.wrapperClass();
    this.accessFlags = EnumSet.noneOf(FieldEditFlags.class);
    FieldEditFlags[] flags = accessAnnot.editFlags();
    if ((flags != null) && (flags.length > 0)) {
      for (FieldEditFlags enumValue : flags) {
        if ((enumValue == null) || (FieldEditFlags.NoConstaints.equals(enumValue))) {
          continue;
        }
        if (FieldEditFlags.ReadOnly.equals(enumValue)) {
          this.accessFlags.clear();
          this.accessFlags.add(FieldEditFlags.ReadOnly);
          break;
        }
        
        if (!this.accessFlags.contains(enumValue)) {
          this.accessFlags.add(enumValue);
        }
      }
    } 
    if (this.accessFlags.isEmpty()) {
      this.accessFlags.add(FieldEditFlags.NoConstaints);
    }
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
   * Check is this field has any assigned Edit Constraint
   * @return try if (!this.accessFlag.isEmpty) and does not contain NoConstaints.
   */
  public boolean hasConstraints() {
    return ((!this.accessFlags.isEmpty()) &&
              (!this.accessFlags.contains(FieldEditFlags.NoConstaints)));
  }
  
  /**
   * Check is this field has readOnly access
   * @return try if (!this.accessFlag.isEmpty) and contains ReadOnly.
   */
  public boolean isReadOnly() {
    return ((!this.accessFlags.isEmpty()) &&
              (this.accessFlags.contains(FieldEditFlags.ReadOnly)));
  }
  
  /**
   * Check if this field is editable. If constraints are assign, return false if
   * (1) this.isReadOnly, (2) if FieldEditFlags.contains(NoIsSystem) and 
   * (isSystemItem=true), else (3) if FieldEditFlags.contains(AdminOnly) and the
   * {@linkplain AdminContext#isAdminLogon()} = true.
   * @param isSystemItem true if the record is a systemItem
   * @return true is the field can be edited; false if not.
   */
  public boolean allowEdits(Boolean isSystemItem) {
    boolean result = (!this.isReadOnly());
    if ((result) && (this.hasConstraints())) {
      if (this.accessFlags.contains(FieldEditFlags.NoIsSystem)) {
        result = ((isSystemItem == null) || (!isSystemItem));
      }
      
      if ((result) && (this.accessFlags.contains(FieldEditFlags.AdminOnly))) {
        AdminContext adminCtx = AdminContext.doLookup();
        result = adminCtx.isAdminLogon();
      }
    }
    return result;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrids">
  /**
   * Get the FieldEditAccess Settings as an HTML String.
   * @return teh formatted string
   */
  public String toHtmlString() {
    String result = "<b>FieldEditAccess[" + this.wrapperClass.getSimpleName() 
            + "]:</b><ul>";
    result += "<li>AccessFlags = " + this.accessFlags + "</li></ul>";
    return result;
  }
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return the FieldEditAccessInfo's settings as a string.</p>
   */
  @Override
  public String toString() {
    String result = this.getClass().getSimpleName() 
                + "[" + this.wrapperClass.getSimpleName() + "]:\n";
    result += "\t- AccessFlags = " + this.accessFlags;
    return result;
  }
  //</editor-fold>
}
