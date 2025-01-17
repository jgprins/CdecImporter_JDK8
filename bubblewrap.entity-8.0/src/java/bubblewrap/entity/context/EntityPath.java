package bubblewrap.entity.context;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Entity Path with and entity class and field reference (i.e., the path to an entity
 * field).
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class EntityPath<TBean extends Serializable> implements Serializable,
                                                        Comparable<EntityPath<TBean>>{
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger =
          Logger.getLogger(EntityPath.class.getSimpleName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Field's Entity Class
   */
  public final Class<TBean> entityClass;
  /**
   * The Field's FieldName
   */
  public final String fieldName;
  /**
   * A Placeholder for the field's cached FieldInfo reference
   */
  private FieldInfo fieldInfo;
  /**
   * A Flag set if the EntityPath's FieldInfo could not be resolved on request.
   * (default = null)
   */
  private Boolean invalid;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public EntityPath(Class<TBean> entityClass, String fieldName) {
    if (entityClass == null) {
      throw new NullPointerException("An EntityPath's Class cannot be unassigned.");
    }
    if ((fieldName= DataEntry.cleanString(fieldName)) == null) {
      throw new NullPointerException("An EntityPath's fieldName cannot be unassigned.");
    }
    this.entityClass = entityClass;
    this.fieldName = fieldName;
    this.fieldInfo = null;
    this.invalid = null;
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
   * Check if the EntityPath is valid. 
   * @return 
   */
  public boolean isValid() {
    if (this.invalid == null) {
      this.getFieldInfo();
    }
    return (!this.invalid);
  }
  
  /**
   * Called to retrieve the EntityPath's associated FieldInfo. It retrieves the
   * entityContext by calling {@linkplain BwAppContext#getEntityContext(
   * java.lang.Class) appCtx.getEntityContext(this.entityClass)} and if the
   * entityContext is accessible, call {@linkplain EntityContext#getFieldInfo(
   * java.lang.String) entCtx.getFieldInfo(this.fieldName)}.
   * <p>
   * this.IsValid = true if the fieldInfo can be retrieved and false otherwise.
   * <p><b>Note:</b> This.fieldInfo reference is cached on first request.</p>
   * @return the fieldInfo or null if (!this.isValid)
   */
  public FieldInfo getFieldInfo() {
    if ((this.fieldInfo == null) && (this.invalid == null)) {
      try {
        BwAppContext appCtx = BwAppContext.doLookup();
        if (appCtx == null) {
          throw new NullPointerException("Unable to access the Application Context.");
        }
        EntityContext<TBean> entCtx = appCtx.getEntityContext(this.entityClass);
        if (entCtx == null) {
          throw new NullPointerException("Unable to retieve the Entity Context for "
                  + "Entity[" + this.entityClass.getSimpleName() + "].");
        }
        this.fieldInfo = entCtx.getFieldInfo(this.fieldName);
      } catch (Exception exp) {
        this.fieldInfo = null;
        logger.log(Level.WARNING, "{0}.getFieldInfo Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      } finally {
        this.invalid = (this.fieldInfo == null);
      }
    }
    return this.fieldInfo;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return this.entityClass.getSimpleName() + "." + this.fieldName</p>
   */
  @Override
  public String toString() {
    return this.entityClass.getSimpleName() + "." + this.fieldName;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return false if obj=null or not an instance of EntityPath.
   * Otherwise, return true if the following match: entityClass and fieldName</p>
   */
  @Override
  public boolean equals(Object obj) {
    boolean result = ((obj != null) && (obj instanceof EntityPath));
    if (result) {
      EntityPath trgObj = (EntityPath) obj;
      result = ((DataEntry.isEq(this.fieldName, trgObj.fieldName, true))
              && (this.entityClass.equals(trgObj.entityClass)));
    }
    return result;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Return a hashCode based on entityClass and fieldName fields</p>
   */
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 47 * hash + Objects.hashCode(this.entityClass);
    hash = 47 * hash + Objects.hashCode(this.fieldName);
    return hash;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: compare this.toString() to other.toString. Return -1 if other=null.</p>
   */
  @Override
  public int compareTo(EntityPath<TBean> other) {
    int result = 0;
    if (other == null) {
      result = -1;
    } else {
      String str1 = this.toString();
      String str2 = other.toString();
      result = str1.compareTo(str2);
    }
    return result;
  }
//</editor-fold>
}
