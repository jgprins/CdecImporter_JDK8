package bubblewrap.entity.context;

import bubblewrap.entity.annotations.AccessValidation;
import bubblewrap.entity.core.EntityWrapper;
import bubblewrap.entity.enums.AccessFlags;
import bubblewrap.io.DataEntry;
import bubblewrap.navigation.enums.AppTasks;
import java.io.Serializable;
import java.util.EnumSet;
import java.util.Objects;

/**
 * A class that contains the declared user access validation requirements specific to a
 * Entity Bean and one a specified EntityWrapper class (or to all EntityWrappers is 
 * EntityWrapepr.Void class). The user's access to the wrapper content will be 
 * determined by the {@linkplain NavigationContext#getActiveNavigationTarget() 
 * actNavCtx=NavigationContext.activeNavigationContext} and the applicable 
 * EntityValidation settings filter by the EntityWrapper class and the 
 * actNavCtx.appTask and actNavCtx.subTask.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EntityValidation implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The EntityWrapper class to Access Validation constraints applied. If
   * EntityWrapper.Void.class it applies to all Wrapper Classes.
   */
  public final Class<? extends EntityWrapper> wrapperClass;
  /**
   * The Entity's AppTask (for navigation and access validation purposes), 
   * (Default={@linkplain AppTasks#CNTMNG})
   */
  public final AppTasks appTask;
  /**
   * The Entity's AppTask (for navigation and access validation purposes), 
   * (Default=this.entityName.toLower)
   */
  public final String subtask;
  /**
   * The flag controlling whether user access to this Entity require validation.
   * (Default = false).
   */
  public final Boolean doValidation;
  /**
   * The Access Flag managing the record level access to the entity record(s). 
   * <p><b>Note:</b> Access set by these flags applies regardless if {@linkplain 
   * #doValidation this.doValidation} = false.</p>
   * @see AccessFlags for access flag options
   */
  public final EnumSet<AccessFlags> accessFlags;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor of the Default access to an entity class (i.e. wrapperClass =
   * EntityWrapper.Void.class, doValidation = false; appTask = CNTMNG; subtask =
   * entityClass.getSimpleName().toLowerCase(), accessFlags = {FullAccess}).
   * @param entityClass the owner entity class (throws NullException is unassigned)
   */
  public EntityValidation(Class<? extends Serializable> entityClass) {
    if (entityClass == null) {
      throw new NullPointerException("The EntityValidation's entityClass reference "
              + "cannot be unassigned.");
    }
    this.wrapperClass = EntityWrapper.Void.class;
    this.doValidation = false;
    this.appTask = AppTasks.CNTMNG;
    this.subtask = entityClass.getSimpleName().toLowerCase();
    this.accessFlags = 
                  AccessFlags.getEnumSet(AccessFlags.FullAccess.flagValue);
  }
  
  /**
   * Public Constructor based on the AccessValidation settings
   * @param accessAnnot the annotation settings
   * @param entityClass the owner entity class (throws NullException is unassigned)
   * @exception IllegalArgumentException if the specified AppTask is invalid.
   */
  public EntityValidation(AccessValidation accessAnnot, 
                                        Class<? extends Serializable> entityClass) {
    if (accessAnnot == null) {
      throw new NullPointerException("The EntityValidation's AccessValidation "
              + "annotion reference cannot be unassigned.");
    }
    if (entityClass == null) {
      throw new NullPointerException("The EntityValidation's entityClass reference "
              + "cannot be unassigned.");
    }    
    this.wrapperClass = (accessAnnot.wrapperClass() == null)? 
                              EntityWrapper.Void.class: accessAnnot.wrapperClass();
    this.doValidation = accessAnnot.doValidation();  
    AppTasks task = accessAnnot.appTask();  
    this.appTask = (task == null)? AppTasks.CNTMNG: task;
    String annotVal = DataEntry.cleanLoString(accessAnnot.subTask());
    this.subtask = 
              (annotVal == null)? entityClass.getSimpleName().toLowerCase(): annotVal;
    
    this.accessFlags = EnumSet.noneOf(AccessFlags.class);
    AccessFlags[] flags = accessAnnot.accessFlags();
    boolean readOnly = false;
    if ((flags != null) && (flags.length > 0)) {
      for (AccessFlags enumValue : flags) {
        if (enumValue == null) {
          continue;
        }
        if (AccessFlags.ReadOnly.equals(enumValue)) {
          readOnly = (flags.length == 1);
          continue;
        } else if (AccessFlags.FullAccess.equals(enumValue)) {
          this.accessFlags.clear();
          break;
        }
        
        if (!this.accessFlags.contains(enumValue)) {
          this.accessFlags.add(enumValue);
        }
      }
    } 
    
    if ((!readOnly) && (this.accessFlags.isEmpty())) {
      this.accessFlags.add(AccessFlags.AllowAdd);
      this.accessFlags.add(AccessFlags.AllowEdits);
      this.accessFlags.add(AccessFlags.AllowDelete);
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
   * Check if the EnityValidation allows ReadOnly access.
   * @return true if (this.accessFlags = empty) or ((this.accessFlags.size=1) and
   * this.accessFlags.contains(ReadOnly))
   */
  public boolean isReadOnly() {
    return ((this.accessFlags.isEmpty()) ||
            ((this.accessFlags.size() == 1) &&
             (this.accessFlags.contains(AccessFlags.ReadOnly))));
  }
  
  /**
   * Check if the EnityValidation allows adding of new records.
   * @return true if this.accessFlags.contains(AllowAdd)
   */
  public boolean allowAdd() {
    return this.accessFlags.contains(AccessFlags.AllowAdd);
  }
  
  /**
   * Check if the EnityValidation allows record edit access
   * @return true if this.accessFlags.contains(AllowEdits)
   */
  public boolean allowEdits() {
    return this.accessFlags.contains(AccessFlags.AllowEdits);
  }
  
  /**
   * Check if the EnityValidation allows delete of records 
   * @return true if this.accessFlags.contains(AllowDelete)
   */
  public boolean allowDelete() {
    return this.accessFlags.contains(AccessFlags.AllowDelete);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Object Overrids">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of EntityValidation.
   * Otherwise, return true if this.wrapperClass.equals(obj.wrapperClass)</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EntityValidation));
    if (result) {
      EntityValidation trgObj = (EntityValidation) obj;
      result = (this.wrapperClass.equals(trgObj.wrapperClass));
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return a hashCode based on this.wrapperClass</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 83 * hash + Objects.hashCode(this.wrapperClass);
    return hash;
  }
  
  /**
   * <p>Get the EntityValidation settings in a report format</p>
   * @return a HTML formatted string
   */
  public String toHtmlString() {
    String result = "<b>EntityValidation[" + this.wrapperClass.getSimpleName() 
            +"]:<b><ul>";
    result += "<li>doValidation = " + this.doValidation + "</li>";
    result += "<li>AppTask = " + this.appTask + "</li>";
    result += "<li>Subtask = " + this.subtask + "</li>";
    result += "<li>AccessFlags = " + this.accessFlags + "</li></ul>";
    return result;
  }
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return the EntityValidation settings in a report format</p>
   */
  @Override
  public String toString() {
    String result = "EntityValidation[" + this.wrapperClass.getSimpleName() +"]:\n";
    result += "\t-doValidation = " + this.doValidation + "\n";
    result += "\t-AppTask = " + this.appTask + "\n";
    result += "\t-Subtask = " + this.subtask + "\n";
    result += "\t-AccessFlags = " + this.accessFlags;
    return result;
  }
  //</editor-fold>
}
