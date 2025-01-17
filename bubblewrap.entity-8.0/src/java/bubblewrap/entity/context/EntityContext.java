package bubblewrap.entity.context;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.app.context.BwAppExtension;
import bubblewrap.core.enums.PrimaryKeyType;
import bubblewrap.core.reflection.*;
import bubblewrap.entity.annotations.*;
import bubblewrap.entity.core.*;
import bubblewrap.entity.enums.AccessFlags;
import bubblewrap.entity.validators.FieldValidator;
import bubblewrap.io.DataEntry;
import bubblewrap.navigation.enums.AppTasks;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.*;
/**
 * <p>The EntityContext provide access to the context of a specified registered Entity.
 * Each application entity should be registered to the {@linkplain BwAppContext}'s
 * Entity Context Registry using a {@linkplain BwAppExtension}.</p> <p>At runtime the
 * EntityContext can be access from the BwAppContext by its class and all sessions will
 * share this stateless information.</p>
 *
 * @author Koos Prins
 */
public class EntityContext<TBean extends Serializable> implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="Static Method/Fields">
  /**
   * Static Constructor used by the {@link BwAppContext#registerPuEntities(bubblewrap.core.interfaces.IEntityLoader) BwAppContext#registerPuEntities()} method to
   * initiate a new EntityContext instance of a Entity Class.
   *
   * @param <TBean> extends Serializable
   * @param entityClass the Entity's Class
   * @param puMngClass the Entity's PuEntityManager class reference.
   * @exception NullPointerException if entityClass or entMngClass is unassigned.
   * @exception IllegalArgumentException if the entityClass is not a Persistent Entity
   * Class or if the entity context initiation failed.
   * @exception IllegalStateException if the entity is already registered.
   */
  public static <TBean extends Serializable> EntityContext<TBean> newInstance(
          Class<TBean> entityClass, Class<? extends PuEntityManager> puMngClass) {
    EntityContext<TBean> result = null;
    if (entityClass == null) {
      throw new NullPointerException("The Entity class reference is unassigned.");
    } else if (!ReflectionInfo.isEntity(entityClass)) {
      throw new IllegalArgumentException("Class[" + entityClass.getSimpleName()
              + "] is not a persistent Entity Bean.");
    } else if (EntityContext.hasContext(entityClass)) {
      throw new IllegalStateException("Class[" + entityClass.getSimpleName()
              + "]'s EntityContext is already registered and could be retrieved "
              + "using the EntityContext.getContext() method.");
    }

    if (puMngClass == null) {
      throw new NullPointerException("The PuEntityManager class reference cannot be "
              + "unassigned.");
    }

    result = new EntityContext<>(entityClass, puMngClass);
    if ((result != null) && (result.hasContextErrors())) {
      logger.log(Level.WARNING, result.getContextErrors());
    }
    return result;
  }

  /**
   * Called to check if this specified entity class has a Registered EntityContext in
   * the {@linkplain BwAppContext}.
   *
   * @param entityClass the entity class of interest
   * @return true if the entity class is has a registered EntityContext
   */
  public static <TBean extends Serializable> boolean hasContext(
          Class<TBean> entityClass) {
    boolean result = false;
    try {
      BwAppContext pBwCtx = BwAppContext.doLookup();
      result = ((pBwCtx == null) && (pBwCtx.hasEntityContext(entityClass)));
    } catch (Exception pExp) {
    }
    return result;
  }

  /**
   * Retrieve the Registered EntityContext for this specified entity class from the
   * {@linkplain BwAppContext}.
   *
   * @param entityClass the entity class of interest
   * @return the registered EntityContext
   * @exception NullPointerException if BwAppContext cannot be accessed and Entity
   * class' EntityContext is not registered.
   */
  public static <TBean extends Serializable> EntityContext<TBean> getContext(
          Class<TBean> entityClass) {
    if (entityClass == null) {
      throw new NullPointerException("The Entity Class is not specified.");
    }

    BwAppContext pBwCtx = BwAppContext.doLookup();
    if (pBwCtx == null) {
      throw new NullPointerException("Unable to access the Applications BwAppContext.");
    }
    EntityContext<TBean> result =
            (EntityContext<TBean>) pBwCtx.getEntityContext(entityClass);
    return result;
  }

  /**
   * Retrieve the entity Bean of the specified class and recordId. It obtains access to
   * the EntityClass' PuManager and its facade. If the facade has an primary key of with
   * a matching recordId, it return the FkFilter's entity. Otherwise, it retrieve a
   * new entity record from the database.
   * @param entityClass the entity class of interest
   * @param recordId of the bean to retrieve.
   * @return the entity Bean or null if recordId = null.
   * @exception NoSuchRecordException if BwAppContext cannot be accessed and Entity
   * class' EntityContext is not registered; if the PuEntityManager or its facade cannot
   * be retrieved - see {@linkplain PuEntityManager#doLookup(java.lang.Class)
   * PuEntityManager.doLookup} for more information - or the record cannot be found.
   */
  public static <TBean extends Serializable> TBean getEntityBean(
          Class<TBean> entityClass, Serializable recordId) {
    TBean result = null;
    if (recordId == null) {
      return result;
    }

    try {
      EntityContext<TBean> entCtx = EntityContext.getContext(entityClass);
      PuEntityManager puMngr = PuEntityManager.doLookup(entCtx.getPuManagerClass());
      if (puMngr == null) {
        throw new Exception("Unable to access the PuEntityManager for Entity["
                + entityClass.getSimpleName() + "].");
      }

      EntityFacade<TBean> entFacade = puMngr.getFacade(entityClass);
      if (entFacade == null) {
        throw new Exception("Unable to access the EntityFacade for Entity["
                + entityClass.getSimpleName() + "].");
      }

      if ((entFacade.hasPkFilter()) && (entFacade.isPkFilterRecordId(recordId))) {
        EntityWrapper<TBean> wrapper = (EntityWrapper<TBean>) entFacade.getPkFilter();
        if (!wrapper.isNew()) {
          result = wrapper.getEntity();
        }
      } else {
        result = entFacade.find(recordId);
        if (result == null) {
          throw new Exception("Unable to retrieve the Entity["
                  + entityClass.getSimpleName() + "] for RecordId["
                  + recordId.toString() + "].");
        }
      }
    } catch (Exception pExp) {
      throw new NoSuchRecordException("EntityContext.getEntityBean Error:\n "
              + pExp.getMessage());
    }
    return result;
  }
  
  /**
   * Protected Exception Logger for writing to the server log
   */
  protected static final Logger logger = 
                                      Logger.getLogger(EntityContext.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the registered EntityClass
   */
  private Class<TBean> entityClass;
  /**
   * Reference to the Entity's Common PuEntityManager
   */
  private Class<? extends PuEntityManager> puManagerClass;
  /**
   * A HashMap containing the entity's {@linkplain EntityValidation EntityValidations}
   * for one or more EnityWrappers as Defined by the Entity's {@linkplain
   * AccessValidation} annotation. If no annotation is assigned. it will initiate a   
   * {@linkplain EntityValidation#EntityValidation(java.lang.Class) default 
   * EntityValidation}.
   */
  private HashMap<Class<? extends EntityWrapper>, EntityValidation> validationMap;
  /**
   * A HashMap for maintaining the Bean Class' {@linkplain FieldInfo}
   */
  private HashMap<String, FieldInfo> fieldInfoMap;
  /**
   * A HashMap for maintaining the alias field mapping (key=alias, value=fieldName)
   */
  private HashMap<String, String> aliasMap;
  /**
   * A HashMap of child field name that has an associated Parent ForeignKeys 
   * (key= fieldName; value = parentClass).
   */
  private HashMap<String, Class<? extends Serializable>> parentForeignKeys;
  /**
   * A list of child field name that has an associated Child ForeignKeys 
   * (key= fieldName).
   */
  private HashMap<String, Class<? extends Serializable>> childForeignKeys;
  /**
   * The FieldInfo for the Entity's PrimaryKey field
   */
  private FieldInfo primaryKey;
  /**
   * A list of fields that required user input (excludes the recordId field).
   */
  private List<String> requiredFields;
  /**
   * A list of fields that required validation of user input (excludes the recordId
   * field).
   */
  private List<String> validationFields;
  /**
   * A list of Entity Context registration errors.
   */
  private List<String> contextErrors;
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * private Constructor called by the static constructor
   */
  private EntityContext(Class<TBean> entityClass,
          Class<? extends PuEntityManager> entMngClass) {
    this.entityClass = entityClass;
    this.puManagerClass = entMngClass;
    this.primaryKey = null;
    this.fieldInfoMap = new HashMap<>();
    this.aliasMap = new HashMap<>();
    this.validationMap = new HashMap<>();
    this.requiredFields = new ArrayList<>();
    this.validationFields = new ArrayList<>();
    this.contextErrors = new ArrayList<>();
    this.parentForeignKeys = new HashMap<>();
    this.childForeignKeys = new HashMap<>();
    
    this.initClassAttributes(entityClass);
    this.initFieldInfo(entityClass);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Context Error/Report Management"> 
  /**
   * Check if the Entity Has Context Registrations Errors
   *
   * @return
   */
  public boolean hasContextErrors() {
    return (!this.contextErrors.isEmpty());
  }

  /**
   * Get the Context Errors as text list
   *
   * @return
   */
  public String getContextErrors() {
    String result = this.entityClass.getSimpleName() + " Context Errors:";
    if (this.contextErrors.isEmpty()) {
      result += " No Errors are reported.";
    } else {
      for (String errMsg : this.contextErrors) {
        result += "\n\t ->> " + errMsg + "";
      }
    }
    return result;
  }

  /**
   * Get the Reported Context Errors in an HTML List
   *
   * @return the HTML formatted list
   */
  public String getHtmlContextErrors() {
    String result = "<b>" + this.entityClass.getSimpleName()
            + " Context Errors:</b><ul>";
    if (this.contextErrors.isEmpty()) {
      result += "<li>No Errors are reported.</li>";
    } else {
      for (String errMsg : this.contextErrors) {
        result += "<li>" + errMsg + "</li>";
      }
    }
    result += "</ul>";
    return result;
  }

  /**
   * Internally called to add a Error message to the Context Registration Error list.
   *
   * @param errMsg the new error added - ignored if null|""
   */
  private void logContextError(String errMsg) {
    errMsg = DataEntry.cleanString(errMsg);
    if (errMsg != null) {
      this.contextErrors.add(errMsg);
    }
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Private/Protected methods"> 
  /**
   * Initiate the Entity's Class Level {@linkplain AccessValidation} annotation
   * settings. If the initiation of a EntityValidation for an assigned AccessValidation
   * failed an Warning will be logged and the process will proceed. If no
   * AccessValidation annotations are assigned or all EntityValidation failed, a  
   * {@linkplain EntityValidation#EntityValidation(java.lang.Class) default 
   * EntityValidation} will be added to the {@linkplain #validationMap
   * this.validationMap}.
   * @param entityClass the class to initiate.
   */
  private void initClassAttributes(Class<TBean> entityClass) {
    AccessValidation accessAnnot = null;
    AccessValidation[] annotArr = null;
    AccessValidationList annotList = null;
    if ((entityClass.isAnnotationPresent(AccessValidation.class)) &&
            (accessAnnot = entityClass.getAnnotation(AccessValidation.class)) != null) {
      annotArr = new AccessValidation[]{accessAnnot};
    } else if ((entityClass.isAnnotationPresent(AccessValidationList.class)) &&
       ((annotList = entityClass.getAnnotation(AccessValidationList.class)) != null)) {
      annotArr = annotList.value();
    }
    if ((annotArr != null) && (annotArr.length > 0)) {
      for (AccessValidation annot : annotArr) {
        try {
          EntityValidation validation =
                  new EntityValidation(annot, entityClass);
          if (this.validationMap.containsKey(validation.wrapperClass)) {
            throw new Exception("Duplicate AccessValidation annotation for "
                    + "EnityWrapper[" + validation.wrapperClass.getSimpleName()
                    + "]");
          }
          this.validationMap.put(validation.wrapperClass, validation);
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.Initiate EntityValidation Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      }
    } else {
      /** Add the default EntityValidation if not already added */
      EntityValidation validation = new EntityValidation(entityClass);
      if (!this.validationMap.containsKey(validation.wrapperClass)) {
        this.validationMap.put(validation.wrapperClass, validation);
      }
    }
  }

  /**
   * Called by the constructor to initiate the EntityFieldInfo for the PrimaryKey, 
   * field aliases, field validator, and value converters for all Entities the fields
   * and methods.
   * @param entityClass the class to initiate.
   */
  @SuppressWarnings("unchecked")
  private void initFieldInfo(Class<TBean> entityClass) {
    if (entityClass == null) {
      throw new NullPointerException("The Entity Class is not specified.");
    }

    BwAppContext appCtx = BwAppContext.doLookup();
    if (appCtx == null) {
      throw new NullPointerException("Unable to access the Application's"
              + " BwAppContext.");
    }

    List<String> processMethods = new ArrayList<>();
    String entClass = entityClass.getSimpleName();
    for (Field field : entityClass.getDeclaredFields()) {
      if ((Modifier.isStatic(field.getModifiers()))
              || (Modifier.isTransient(field.getModifiers()))
              || (field.isAnnotationPresent(Transient.class))) {
        continue;
      }

      String fieldName = field.getName();
      try {
        Method getMethod = null;
        Method setMethod = null;
        if (field.isAnnotationPresent(FieldMethods.class)) {
          FieldMethods annot = field.getAnnotation(FieldMethods.class);
          String methodName = DataEntry.cleanString(annot.get());
          if (methodName == null) {
            throw new Exception("Field[" + fieldName + "]'s FieldMethods annotation "
                    + "is incomplete. The Get-Method's name is not set.");
          }
          getMethod = ReflectionInfo.getMethod(entityClass, methodName);
          if (getMethod == null) {
            throw new Exception("Field[" + fieldName + "]'s FieldMethods annotation "
                    + "is invalid. The Get-Method[" + methodName + "] does not exist.");
          }
          methodName = DataEntry.cleanString(annot.set());
          if (methodName != null) {
            setMethod = ReflectionInfo.getMethod(entityClass, methodName);
            if (getMethod == null) {
              throw new Exception("Field[" + fieldName + "]'s FieldMethods annotation "
                      + "is invalid. The Set-Method[" + methodName + "] does not exist.");
            }
          }
        } else {
          getMethod = ReflectionInfo.getGetMethod(entityClass, fieldName);
          if (getMethod == null) {
            if (!field.isAnnotationPresent(NoMethods.class)) {
              throw new Exception("Unable to locate Entity[" + entClass + "].Field["
                      + fieldName + "]'s GET-Method.");
            }
          }

          setMethod = ReflectionInfo.getSetMethod(entityClass, fieldName);
        }
        if (getMethod != null) {
          processMethods.add(getMethod.getName());
        }
        
        EntityPath entityPath = new EntityPath(entityClass, fieldName);

        FieldInfo fieldInfo = new FieldInfo(entityPath, getMethod, setMethod, field);
        if (fieldInfo == null) {
          throw new Exception("Initiating FieldInfo[" + entityPath.toString() 
                  + "] failed");
        }
        
        if (fieldInfo.isPrimaryKey) {
          if (this.primaryKey != null) {
            throw new Exception("A duplicate RecordIdField or Id annotation is "
                    + "assigned to Field[" + fieldInfo.entityPath + "].");
          }

          this.primaryKey = fieldInfo;
          if (setMethod == null) {
            if (this.allowAdd()) {
              throw new Exception("Field[" + fieldInfo.entityPath 
                      + "] - as a primaryKey field - cannot be ReadOnly.");
            }
          }
        }

        this.fieldInfoMap.put(fieldName.toLowerCase(), fieldInfo);
        if (fieldInfo.isNotNull()) {
          this.requiredFields.add(fieldName.toLowerCase());
        }

        if (fieldInfo.validator != null) {
          this.validationFields.add(fieldName.toLowerCase());
        }
        
        
        if (fieldInfo.alias != null) {
          String alias = fieldInfo.alias.toLowerCase();
          if (this.aliasMap.containsKey(alias)) {
            this.logContextError("InitFieldInfo Error: The Entity has more than field "
                        + "assigned to Alias[" + fieldInfo.alias + "].");
          } else {
            this.aliasMap.put(fieldInfo.alias.toLowerCase(), fieldName.toLowerCase());
          }
        }
      } catch (Exception exp) {
        this.logContextError("InitFieldInfo Error: " + exp.getMessage());
      }
    }

    for (Method method : entityClass.getDeclaredMethods()) {
      if ((method.getReturnType().equals(void.class)) ||
              (processMethods.contains(method.getName()))) {
        continue;
      }
      
      String fieldName = ReflectionInfo.getFieldname(method.getName());
      if ((fieldName != null) &&
              (!this.fieldInfoMap.containsKey(fieldName.toLowerCase()))) {
        EntityPath<TBean> entityPath = new EntityPath<>(entityClass, fieldName);
        try {
          Method setMethod = ReflectionInfo.getSetMethod(entityClass, fieldName);

          FieldInfo fieldInfo = new FieldInfo(entityPath, method, setMethod, null);
          if (fieldInfo == null) {
            throw new Exception("Initiating FieldInfo[" + entityPath.toString() 
                    + "] failed");
          }

          this.fieldInfoMap.put(fieldName.toLowerCase(), fieldInfo);
          if (fieldInfo.isNotNull()) {
            this.requiredFields.add(fieldName.toLowerCase());
          }

          if (fieldInfo.validator != null) {
            this.validationFields.add(fieldName.toLowerCase());
          }

          if (fieldInfo.alias != null) {
            String alias = fieldInfo.alias.toLowerCase();
            if (this.aliasMap.containsKey(alias)) {
              this.logContextError("InitFieldInfo Error: The Entity has more than "
                          + "field assigned to Alias[" + alias + "].");
            } else {
              this.aliasMap.put(alias, fieldName);
            }
          }
        } catch (Exception exp) {
          this.logContextError("InitFieldInfo Error: " + exp.getMessage());
        }
      }
    }
  }
  
  /**
   * Called from {@linkplain BwAppContext#registerPuEntities(
   * bubblewrap.core.interfaces.IEntityLoader) appCtx.registerPuEntities} on a 2nd 
   * pass to register the ForeignKey Associations (with the exception of the children 
   * of the OneToMany associations).
   * @param appCtx the calling BwAppContext
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public void registerAssociations(BwAppContext appCtx) {
    try {
      boolean hasOwnerParent = false;
      ForeignKey foreignKey = null;
      for (FieldInfo fieldInfo : this.fieldInfoMap.values()) {
        if (fieldInfo.hasAnnotation(ManyToOne.class)) {
          if ((foreignKey = this.initManyToOneFk(fieldInfo, appCtx)) != null) {
            if (foreignKey.isOwnerParent()) {
              if (hasOwnerParent) {
                foreignKey.setOwnerParent(false);
                this.logContextError("InitFieldInfo Error: The Entity has more than "
                        + "one OwnerParent ForeignKey Constraints.");
              } else {
                foreignKey.setOwnerParent(true);
                hasOwnerParent = true;
              }
            }
            appCtx.setForeignKey(foreignKey);
            this.parentForeignKeys.put(fieldInfo.entityPath.fieldName,
                                               foreignKey.associationPath.parentClass);
          }
        } else if (fieldInfo.hasAnnotation(OneToMany.class)) {
          this.childForeignKeys.put(fieldInfo.entityPath.fieldName, VoidEntity.class);
        } else if (fieldInfo.hasAnnotation(OneToOne.class)) {          
          if ((foreignKey = this.initOneToOneFk(fieldInfo, appCtx)) != null) {
            if (foreignKey.isChild(fieldInfo.entityPath)) {
              if (foreignKey.isOwnerParent()) {
                if (hasOwnerParent) {
                  foreignKey.setOwnerParent(false);
                  this.logContextError("InitFieldInfo Error: The Entity has more than "
                          + "one OwnerParent ForeignKey Constraints.");
                } else {
                  foreignKey.setOwnerParent(true);
                  hasOwnerParent = true;
                }
              }
              appCtx.setForeignKey(foreignKey);
              this.parentForeignKeys.put(fieldInfo.entityPath.fieldName,
                                               foreignKey.associationPath.parentClass);
            } else {
              this.childForeignKeys.put(fieldInfo.entityPath.fieldName, 
                                               foreignKey.associationPath.childClass);
            }
          }
        } else if (fieldInfo.hasAnnotation(ManyToMany.class)) {
          if ((foreignKey = this.initManyToManyFk(fieldInfo, appCtx)) != null) {
            if (foreignKey.isChild(fieldInfo.entityPath)) {
              if (foreignKey.isOwnerParent()) {
                if (hasOwnerParent) {
                  foreignKey.setOwnerParent(false);
                  this.logContextError("InitFieldInfo Error: The Entity has more than "
                          + "one OwnerParent ForeignKey Constraints.");
                } else {
                  foreignKey.setOwnerParent(true);
                  hasOwnerParent = true;
                }
              }
              appCtx.setForeignKey(foreignKey);
              this.parentForeignKeys.put(fieldInfo.entityPath.fieldName,
                                               foreignKey.associationPath.parentClass);
            } else {
              this.childForeignKeys.put(fieldInfo.entityPath.fieldName,
                                               foreignKey.associationPath.childClass);
            }
          }
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.regsiterAssociations Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  
  /**
   * Called from {@linkplain BwAppContext#registerPuEntities(
   * bubblewrap.core.interfaces.IEntityLoader) appCtx.registerPuEntities} on a 3rd
   * pass to resolve all unresolved ChildForeinKey associations. It cycle through
   * this.chidlForeignKeys and if the assigned childClass = VoideEntity.class, it 
   * retrieves for appCtx the Parent ForeignKey for path[this.entityClass, fieldName].
   * If found, it assign the ForeigKey.associationPath.childClass as this field's
   * childClass. If not found, it logs an exception to the SeverLog
   * @param appCtx the calling BwAppContext
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  public void registerChildForeignKeys(BwAppContext appCtx) {
    try {
      Class<? extends Serializable> childClass = null;
      for (String fieldName : this.childForeignKeys.keySet()) {
        if (((childClass = this.childForeignKeys.get(fieldName)) != null) &&
                (VoidEntity.class.equals(childClass))) {
          EntityPath parentPath = new EntityPath(this.entityClass, fieldName);
          ForeignKey parentFk = appCtx.getForeignKeyForParentPath(parentPath);
          if (parentFk == null) {
            throw new Exception("Unable to resolce the Child ForeignKey for the "
                    + "parent EntityPath[" + parentPath.toString() +"].");
          }
          
          this.childForeignKeys.put(fieldName, parentFk.associationPath.childClass);
        }
      }
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.registerChildForeignKeys Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
    
  /**
   * Called to retrieve the One-To-Many ForeignKey for the child (source)
   * field. It calls {@linkplain #getOneToManyMappedBy(java.lang.Class, 
   * java.lang.String, java.lang.Class) this.getOneToManyMappedBy} to retrieve the  
   * target field of the parent entity (assumed to be the sourceField's return type).
   * It uses the source and target fields' {@linkplain Association} annotation (if 
   * assigned) to resolve the owner-parent relationship and whether the parent or 
   * children EntityWrappers should be cached.
   * @param sourceField the child entity's FieldInfo
   * @param appCtx the application's BwAppContext
   * @return the ForeignKey or null if not found or an error occurred.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  private ForeignKey initManyToOneFk(FieldInfo sourceField, BwAppContext appCtx) {
    ForeignKey result = null; 
    try {
      ManyToOne srcAnnot = sourceField.getAnnotation(ManyToOne.class);
      if (srcAnnot == null) {
        return result;
      }
      
      Boolean ownerParent = null;
      Boolean cacheChild = null;
      Boolean cacheParent = null;
      
      Class targetEntity = sourceField.returnType;
      if ((targetEntity == null) || (void.class.equals(targetEntity))) {
        throw new NullPointerException("The ManyToOne Association's Target Entity "
                + "class cannot be undefined.");
      }
      if (!ReflectionInfo.isEntity(targetEntity)) {
        if (((targetEntity = srcAnnot.targetEntity()) == null) ||
                (Void.class.equals(targetEntity))) {
          throw new NullPointerException("The ManyToOne Association's Target Class[" 
                + targetEntity.getSimpleName() + "] is not an Entity Bean.");
        }
      }
      
      Association assocAnnot = sourceField.getAnnotation(Association.class);
      if (assocAnnot != null) {
        ownerParent = assocAnnot.ownerParent();
        cacheParent = assocAnnot.cache();
      }

      FieldInfo targetField = 
                          this.getOneToManyMappedBy(sourceField, targetEntity, appCtx);
      if (targetField == null) {
        throw new NullPointerException("Unable to resolve the OneToMany field in "
                + "Class[" + targetEntity.getSimpleName() + "] satisfies the "
                + "the ManyToOne Association for EntityPath[" 
                + sourceField.entityPath.toString()+ "].");
      }
      
      if ((targetField.hasAnnotation(Association.class)) &&
              ((assocAnnot = targetField.getAnnotation(Association.class)) != null)) {
        cacheChild = assocAnnot.cache();
      }
      
      OneToMany trgAnnot = targetField.getAnnotation(OneToMany.class);
      if (ownerParent == null) {
        EnumSet<CascadeType> cascaseSet = 
                                  AssociationDef.getCascadeSet(trgAnnot.cascade());
        ownerParent = ((cascaseSet.contains(CascadeType.REFRESH)) ||
                         (cascaseSet.contains(CascadeType.MERGE)));
      }
      
      AssociationDef parentAssoc = new AssociationDef(srcAnnot, targetField.entityPath, 
                                                cacheParent, ownerParent, null);
      AssociationDef childAssoc = new AssociationDef(trgAnnot, sourceField.entityPath,
                                                cacheChild, null);
      result = new ForeignKey(parentAssoc, childAssoc);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initManyToOneFk Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Search the targetEntity to locate the association mappedBy field (i.e., the field
   * with return type = Collection and comply with one of the following conditions:<ul>
   * <li>OneToMany.targetEntity != null and OneToMany.targetEntity = sourceEntity
   * </li>
   * <li>OR field.name.lowerCase.startsWidth(soruceEntity.class.simpleName.lowerCase)
   * </li>
   * <li>AND OneToMany.mappedBy != null and sourceField != null and OneToMany.mappedBy
   * = sourceField.</li>
   * </ul>
   * @param <TTarget>
   * @param sourceField the source Entity's FieldInfo
   * @param targetEntity the target Entity class (parent)
   * @param appCtx the application's BwAppContext
   * @return the mappedBy fieldInfo or null if not found.
   */
  private <TTarget extends Serializable> FieldInfo getOneToManyMappedBy(FieldInfo 
          sourceField, Class<TTarget> targetEntity, BwAppContext appCtx) {
    FieldInfo result = null;
    OneToMany annot = null;
    EntityContext<TTarget> trgCtx = null;
    if ((sourceField != null) && (targetEntity != null) &&
            ((trgCtx = appCtx.getEntityContext(targetEntity)) != null)) {
      String srcPrefix = this.entityClass.getSimpleName().toLowerCase();
      for (FieldInfo fieldInfo : trgCtx.getAllFieldInfos()) {
        if ((fieldInfo.hasAnnotation(OneToMany.class)) && 
                (Collection.class.isAssignableFrom(fieldInfo.returnType)) &&
                ((annot = fieldInfo.getAnnotation(OneToMany.class)) != null)) {
          if ((((annot.targetEntity() != null) && 
               (this.entityClass.equals(annot.targetEntity()))) ||
               (fieldInfo.entityPath.fieldName.toLowerCase().startsWith(srcPrefix))) &&
              ((annot.mappedBy() != null) && 
               (DataEntry.isEq(sourceField.entityPath.fieldName, annot.mappedBy(), 
                       true)))) {
            result = fieldInfo;
            break;
          }
        }
      }
    }
    return result;
  }
    
  /**
   * Called to retrieve the One-To-Many ForeignKey for the child (source)
   * field. It calls {@linkplain #getOneToManyMappedBy(java.lang.Class, 
   * java.lang.String, java.lang.Class) this.getOneToManyMappedBy} to retrieve the  
   * target field of the parent entity (assumed to be the sourceField's return type).
   * It uses the source and target fields' {@linkplain Association} annotation (if 
   * assigned) to resolve the owner-parent relationship and whether the parent or 
   * children EntityWrappers should be cached.
   * @param sourceField the child entity's field
   * @param appCtx the application's BwAppContext
   * @return the ForeignKey or null if not found or an error occurred.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  private ForeignKey initOneToOneFk(FieldInfo sourceField, BwAppContext appCtx) {
    ForeignKey result = null; 
    try {
      OneToOne srcAnnot = sourceField.getAnnotation(OneToOne.class);
      if (srcAnnot == null) {
        return result;
      }
      
      Boolean ownerParent = null;
      Boolean cacheChild = null;
      Boolean cacheParent = null;
      
      Class targetEntity = sourceField.returnType;
      if ((targetEntity == null) || (void.class.equals(targetEntity))) {
        throw new NullPointerException("The OneToOne Association's Target Entity "
                + "class cannot be undefined.");
      }
      if (!ReflectionInfo.isEntity(targetEntity)) {
        throw new NullPointerException("The OneToOne Association's Target Class[" 
                + targetEntity.getSimpleName() + "] is not an Entity Bean.");
      }

      FieldInfo targetField = this.getOneToOneMappedBy(targetEntity, appCtx);
      if (targetField == null) {
        throw new NullPointerException("Unable to resolve the OneToMany field in "
                + "Class[" + targetEntity.getSimpleName() + "] satisfies the "
                + "the ManyToOne Association with EntityPath[" 
                + sourceField.entityPath.toString() + "].");
      }
            
      OneToOne trgAnnot = targetField.getAnnotation(OneToOne.class);
      
      
      FieldInfo parentField = null;
      Association srcAssoc = null;
      if ((srcAssoc = sourceField.getAnnotation(Association.class)) != null) {
        if (srcAssoc.parent()) {
          parentField = sourceField;
        }
      }
      
      Association trgAssoc = null;
      if ((trgAssoc = targetField.getAnnotation(Association.class)) != null) {
        if (trgAssoc.parent()) {
          if (parentField != null) {
            throw new Exception("Invalid OneToOne Association definition. Both "
                    + "entities are set as the parent entity in the association.");
          } else {
            parentField = targetField;
          }
        }
      }
      
      if (parentField == null) {
        throw new Exception("Incomplete OneToOne Association definition. Neither "
                + "entitiies are identified as the parent in the association.");
      }
          
      AssociationDef childAssoc = null;
      AssociationDef parentAssoc = null;
      if (parentField == sourceField) {
        if (srcAssoc != null) {
          cacheChild = srcAssoc.cache();
        }
        if (trgAssoc != null) {
          cacheParent = trgAssoc.cache();
          ownerParent = trgAssoc.ownerParent();
        }
        if (ownerParent == null) {
          EnumSet<CascadeType> cascaseSet = 
                                  AssociationDef.getCascadeSet(srcAnnot.cascade());
          ownerParent = ((cascaseSet.contains(CascadeType.REFRESH)) ||
                           (cascaseSet.contains(CascadeType.MERGE)));
        }
        
        childAssoc = new AssociationDef(srcAnnot, targetField.entityPath,
                                        cacheParent, null, true);
        parentAssoc = new AssociationDef(trgAnnot, sourceField.entityPath, 
                                        cacheChild, ownerParent, false);
      } else { 
        if (srcAssoc != null) {
          cacheParent = srcAssoc.cache();
          ownerParent = srcAssoc.ownerParent();
        }
        
        if (trgAssoc != null) {
          cacheChild = trgAssoc.cache();
        }        
        if (ownerParent == null) {
          EnumSet<CascadeType> cascaseSet = 
                                  AssociationDef.getCascadeSet(trgAnnot.cascade());
          ownerParent = ((cascaseSet.contains(CascadeType.REFRESH)) ||
                           (cascaseSet.contains(CascadeType.MERGE)));
        }
        parentAssoc = new AssociationDef(srcAnnot, targetField.entityPath, 
                                       cacheParent, ownerParent, false);
        childAssoc = new AssociationDef(trgAnnot, sourceField.entityPath, 
                                       cacheChild, null, true);
      }
      result = new ForeignKey(parentAssoc, childAssoc);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.sourceField Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Search the targetEntity to locate the association mappedBy field (i.e., the field
   * with return type = sourceEntity with an OneToOne annotation.
   * @param targetEntity the target Entity class (parent)
   * @param appCtx the application's BwAppContext
   * @return the mappedBy field or null if not found.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  private <TTarget extends Serializable> FieldInfo getOneToOneMappedBy(Class<TTarget> 
          targetEntity, BwAppContext appCtx) {
    FieldInfo result = null;
    OneToOne annot = null;
    EntityContext<TTarget> trgCtx = null;
    if ((targetEntity != null) &&
            ((trgCtx = appCtx.getEntityContext(targetEntity)) != null)) {
      String srcPrefix = this.entityClass.getSimpleName().toLowerCase();
      for (FieldInfo fieldInfo : trgCtx.getAllFieldInfos()) {
        if ((fieldInfo.hasAnnotation(OneToOne.class)) && 
                (this.entityClass.equals(fieldInfo.returnType)) &&
                ((annot = fieldInfo.getAnnotation(OneToOne.class)) != null)) {
          result = fieldInfo;
          break;
        }
      }
    }
    return result;
  }
    
  /**
   * Called to retrieve the One-To-Many ForeignKey for the child (source)
   * field. It calls {@linkplain #getOneToManyMappedBy(java.lang.Class, 
   * java.lang.String, java.lang.Class) this.getOneToManyMappedBy} to retrieve the  
   * target field of the parent entity (assumed to be the sourceField's return type).
   * It uses the source and target fields' {@linkplain Association} annotation (if 
   * assigned) to resolve the owner-parent relationship and whether the parent or 
   * children EntityWrappers should be cached.
   * @param sourceField the child entity's fieldInfo
   * @param appCtx the application's BwAppContext
   * @return the ForeignKey or null if not found or an error occurred.
   */
  @SuppressWarnings({"unchecked", "rawtype"})
  private ForeignKey initManyToManyFk(FieldInfo sourceField, BwAppContext appCtx) {
    ForeignKey result = null; 
    try {
      ManyToMany srcAnnot = null;
      if (((srcAnnot = sourceField.getAnnotation(ManyToMany.class)) == null) ||
              (Collection.class.isAssignableFrom(sourceField.returnType))) {
        return result;
      }
      
      Boolean ownerParent = null;
      Boolean cacheChild = null;
      Boolean cacheParent = null;
      
      Class targetEntity = srcAnnot.targetEntity();
      if ((targetEntity == null) || (void.class.equals(targetEntity))) {
        throw new NullPointerException("The ManyToMany Association's Target Entity "
                + "class cannot be undefined.");
      }
      if (!ReflectionInfo.isEntity(targetEntity)) {
        throw new NullPointerException("The ManyToMany Association's Target Class[" 
                + targetEntity.getSimpleName() + "] is not an Entity Bean.");
      }

      FieldInfo targetField = 
                         this.getManyToManyMappedBy(sourceField, targetEntity, appCtx);
      if (targetField == null) {
        throw new NullPointerException("Unable to resolve the ManyToMany field in "
                + "Class[" + targetEntity.getSimpleName() + "] satisfies the "
                + "the ManyToMany Association with EntityPath[" 
                + sourceField.entityPath.toString() + "].");
      }
            
      ManyToMany trgAnnot = targetField.getAnnotation(ManyToMany.class);      
      
      FieldInfo parentField = null;
      Association srcAssoc = null;
      if ((srcAssoc = sourceField.getAnnotation(Association.class)) != null) {
        if (srcAssoc.parent()) {
          parentField = sourceField;
        }
      }
      
      Association trgAssoc = null;
      if ((targetField.hasAnnotation(Association.class)) &&
              ((trgAssoc = targetField.getAnnotation(Association.class)) != null)) {
        if (trgAssoc.parent()) {
          if (parentField != null) {
            throw new Exception("Invalid ManyToMany Association definition. Both "
                    + "entities are set as the parent entity in the association.");
          } else {
            parentField = targetField;
          }
        }
      }
      
      if (parentField == null) {
        throw new Exception("Incomplete ManyToMany Association definition. Neither "
                + "entitiies are identified as the parent in the association.");
      }
          
      AssociationDef childAssoc = null;
      AssociationDef parentAssoc = null;
      if (parentField == sourceField) {
        if (srcAssoc != null) {
          cacheChild = srcAssoc.cache();
        }
        if (trgAssoc != null) {
          cacheParent = trgAssoc.cache();
          ownerParent = trgAssoc.ownerParent();
        }
        if (ownerParent == null) {
          EnumSet<CascadeType> cascaseSet = 
                                  AssociationDef.getCascadeSet(srcAnnot.cascade());
          ownerParent = ((cascaseSet.contains(CascadeType.REFRESH)) ||
                         (cascaseSet.contains(CascadeType.MERGE)));
        }
        
        childAssoc = new AssociationDef(srcAnnot, targetField.entityPath,
                                      cacheParent, null, true);
        parentAssoc = new AssociationDef(trgAnnot, sourceField.entityPath, 
                                      cacheChild, ownerParent, false);
      } else { 
        if (srcAssoc != null) {
          cacheParent = srcAssoc.cache();
          ownerParent = srcAssoc.ownerParent();
        }
        
        if (trgAssoc != null) {
          cacheChild = trgAssoc.cache();
        }        
        if (ownerParent == null) {
          EnumSet<CascadeType> cascaseSet = 
                                  AssociationDef.getCascadeSet(trgAnnot.cascade());
          ownerParent = ((cascaseSet.contains(CascadeType.REFRESH)) ||
                         (cascaseSet.contains(CascadeType.MERGE)));
        }
        parentAssoc = new AssociationDef(srcAnnot, targetField.entityPath, 
                                      cacheParent, ownerParent, false);
        childAssoc = new AssociationDef(trgAnnot, sourceField.entityPath, 
                                      cacheChild, null, true);
      }
      result = new ForeignKey(parentAssoc, childAssoc);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.sourceField Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
    return result;
  }
  
  /**
   * Search the targetEntity to locate the association mappedBy field (i.e., the field
   * with return type = Collection and comply with one of the following conditions:<ul>
   * <li>ManyToMany.targetEntity != null and OneToMany.targetEntity = sourceEntity
   * </li>
   * <li>OR field.name.lowerCase.startsWidth(sourceEntity.class.simpleName.lowerCase)
   * </li>
   * <li>AND OneToMany.mappedBy != null and sourceField != null and ManyToMany.mappedBy
   * = sourceField.</li>
   * </ul>
   * @param sourceField the source Entity's field (child.field)
   * @param targetEntity the target Entity class (parent)
   * @param appCtx the application's BwAppContext
   * @return the mappedBy field or null if not found.
   */
  private <TTarget extends Serializable> FieldInfo getManyToManyMappedBy(FieldInfo 
          sourceField, Class<TTarget> targetEntity, BwAppContext appCtx) {
    FieldInfo result = null;
    ManyToMany annot = null;
    EntityContext<TTarget> trgCtx = null;
    if ((targetEntity != null) &&
            ((trgCtx = appCtx.getEntityContext(targetEntity)) != null)) {
      String srcPrefix = this.entityClass.getSimpleName().toLowerCase();
      for (FieldInfo fieldInfo : trgCtx.getAllFieldInfos()) {
        if ((Collection.class.isAssignableFrom(sourceField.returnType)) &&
                ((annot = fieldInfo.getAnnotation(ManyToMany.class)) != null)) {
          if ((((annot.targetEntity() != null) && 
              (this.entityClass.equals(annot.targetEntity()))) ||
              (fieldInfo.entityPath.fieldName.toLowerCase().startsWith(srcPrefix))) &&
              ((annot.mappedBy() != null) && (sourceField != null) &&
              (DataEntry.isEq(sourceField.entityPath.fieldName, annot.mappedBy(), 
                      true)))) {
            result = fieldInfo;
            break;
          }
        }
      }
    }
    return result;
  }

  //<editor-fold defaultstate="collapsed" desc="Deprecated Code">
  // /**
  //   * Called by the constructor to initiate the EntityFieldInfo for the PrimaryKey,
  //   * RecordNameField, DisplayIdx, Disabled, SystemItem, and SecurityLevelField fields.
  //   * @param entityClass the class to initiate.
  //   */
  //  private void initFieldInfo(Class<TBean> entityClass) {
  //    FieldInfo fieldInfo = this.onGetFieldInfo(entityClass, PrimaryKey.class);
  //    if (fieldInfo == null) {
  //      throw new IllegalArgumentException("Entity[" + this.getEntityName()
  //              + "] has no RecordId Field (i.e., with annotation RecordId).");
  //    } else if (fieldInfo.getSetName() == null) {
  //      throw new IllegalArgumentException("Entity[" + this.getEntityName()
  //              + "]'s recordId field cannot be ReadOnly.");
  //    }
  //    this.recordIdField = fieldInfo.getFieldName();
  //    this.fieldInfoMap.put(this.recordIdField.toLowerCase(), fieldInfo);
  //
  //    PrimaryKey recIdAnnot = fieldInfo.getAnnotation(PrimaryKey.class);
  //    this.primaryKeyType = -1;
  //    if (recIdAnnot !=  null) {
  //      int eFlag = recIdAnnot.primaryKeyType();
  //      if (EnumInfo.isValidEnumOption(PrimaryKeyType.class, eFlag)) {
  //        this.primaryKeyType = eFlag;
  //      }
  //    }
  //
  //    if (this.primaryKeyType < 0) {
  //      if ((Integer.class.equals(fieldInfo.getReturnType())) ||
  //              (Long.class.equals(fieldInfo.getReturnType()))) {
  //        this.primaryKeyType = PrimaryKeyType.AUTOINTEGER;
  //      } else if (String.class.equals(fieldInfo.getReturnType())) {
  //        this.primaryKeyType = PrimaryKeyType.GUID;
  //      }
  //    }
  //
  //    fieldInfo = this.onGetFieldInfo(entityClass, RecordNameField.class);
  //    if (fieldInfo != null) {
  //      this.recordNameField = fieldInfo.getFieldName();
  //      this.fieldInfoMap.put(this.recordNameField.toLowerCase(), fieldInfo);
  //    }
  //    fieldInfo = this.onGetFieldInfo(entityClass, DisplayIdxField.class);
  //    if (fieldInfo != null) {
  //      this.displayIdxField = fieldInfo.getFieldName();
  //      this.fieldInfoMap.put(this.displayIdxField.toLowerCase(), fieldInfo);
  //    }
  //    fieldInfo = this.onGetFieldInfo(entityClass, IsDisabledField.class);
  //    if (fieldInfo != null) {
  //      this.disabledField = fieldInfo.getFieldName();
  //      this.fieldInfoMap.put(this.disabledField.toLowerCase(), fieldInfo);
  //    }
  //    fieldInfo = this.onGetFieldInfo(entityClass,  IsSystemItemField.class);
  //    if (fieldInfo != null) {
  //      this.systemItemField = fieldInfo.getFieldName();
  //      this.fieldInfoMap.put(this.systemItemField.toLowerCase(), fieldInfo);
  //    }
  //    fieldInfo = this.onGetFieldInfo(entityClass, FieldEditAccess.class);
  //    if (fieldInfo != null) {
  //      this.accessCodeField = fieldInfo.getFieldName();
  //      this.fieldInfoMap.put(this.accessCodeField.toLowerCase(), fieldInfo);
  //    }
  //  }
  //
//  /**
//   * Called by the constructor to retrieve and register the Entity's ForeignKey with
//   * the BwAppContext based on assigned {@linkplain ForeignKeyField} annotations. If
//   * the ForeignKeyField annotation is incomplete, a warning will be logged and the
//   * foreignKey will be ignored.
//   * @param entityClass the class to process.
//   */
//  private void registerForeignKeys(Class<TBean> entityClass) {
//    List<Field> fieldList = this.onGetFieldsByAnnot(entityClass, ForeignKeyField.class);
//    if (!fieldList.isEmpty()) {
//      BwAppContext appCtx = BwAppContext.doLookup();
//      if (appCtx == null) {
//        throw new NullPointerException("Unable to access the Application's"
//                + " BwAppContext.");
//      }
//      for (Field field : fieldList) {
//        ForeignKeyField annot = field.getAnnotation(ForeignKeyField.class);
//        Class<? extends Serializable> parentClass = null;
//        Boolean noConstriant = annot.noFKConstriant();
//        if (noConstriant) {
//          parentClass = annot.parentClass();
//          if (parentClass == null) {
//            logger.log(Level.WARNING, "Field[{0}]''s ForeignKey "
//                    + "Parent Class is not defined. The ForeignKey is ignored.", 
//                    field.getName());
//            continue;
//          }
//        } else {
//          parentClass = (Class<? extends Serializable>) field.getType();
//        }
//        
//        String parentFld = DataEntry.cleanString(annot.parentField());
//        if (parentFld == null) {
//          logger.log(Level.WARNING, "Field[{0}]'s ForeignKey "
//                    + "Parent Field is not defined. The ForeignKey is ignored.", 
//                    field.getName());
//          continue;
//        }
//        Boolean isNullable = annot.nullable();
//        
//        ForeignKey fkInfo = new ForeignKey(parentClass, parentFld, 
//                               entityClass, field.getName(), noConstriant, isNullable);
//        appCtx.registerForeignKey(fkInfo);
//      }
//    }
//  }
//  
//  /**
//   * Called by the constructor to initiate the fieldInfo and the requiredFields list -
//   * field with the NotNull constraint. These field must have a Get- and Set-method.
//   * The Entity Wrapper use this list to validate that all required fields have valid
//   * input before submitting the record.
//   * @param entityClass the class to process. 
//   */
//  private void initRequiredFieldInfo(Class<TBean> entityClass) {
//    List<Field> fieldList = this.onGetFieldsByAnnot(entityClass,NotNull.class);
//    String fieldName = null;
//    Annotation[] annotArr = null;
//    if ((fieldList != null) && (!fieldList.isEmpty())) {
//      for (Field field : fieldList) {
//        fieldName = field.getName();
//        if (DataEntry.isEq(fieldName, this.recordIdField, true)) {
//          continue;
//        }
//        
//        annotArr = field.getAnnotations();
//        Method get = ReflectionInfo.getGetMethod(this.entityClass, fieldName);
//        Method set = ReflectionInfo.getSetMethod(this.entityClass, fieldName);
//        if (get == null) {
//          throw new IllegalArgumentException("Unable to locate required Entity[" 
//                  + this.getEntityName() + "].Field[" + fieldName + "]'s GET-Method.");
//        }
//        if (set == null) {
//          throw new IllegalArgumentException("Unable to locate required  Entity[" 
//                  + this.getEntityName() + "].Field[" + fieldName + "]'s SET-Method.");
//        }
//        
//        String fieldKey = fieldName.toLowerCase();
//        if (!this.requiredFields.contains(fieldKey)) {
//          this.requiredFields.add(fieldKey);
//        }
//        
//        FieldInfo fieldInfo = new FieldInfo(fieldName, get.getName(), 
//                           set.getName(), get.getReturnType(), annotArr);
//        if (!this.fieldInfoMap.containsKey(fieldKey)) {
//          this.fieldInfoMap.put(fieldKey, fieldInfo);
//        }
//      }
//    }
//  }
//  
//  /**
//   * <p>Initiate the FieldInfo for a a Specified FieldId based on its associated 
//   * Annotation class. It search both the fields and methods for annotation assignments.
//   * </p> 
//   * @param entityClass the bean class to initiate
//   * @param annotClass the field associated Annotation class
//   * @return the field information or null if not found.
//   */
//  private FieldInfo onGetFieldInfo(Class<TBean> entityClass, Class annotClass) {
//    FieldInfo result = null;    
//    List<Field> fieldList = this.onGetFieldsByAnnot(entityClass,annotClass);
//    String fieldName = null;
//    Annotation[] annotArr = null;
//    Method get = null;
//    Method set = null;
//    if (!fieldList.isEmpty()) {
//      if (fieldList.size() > 1) {
//        throw new IllegalArgumentException("Entity[" + this.getEntityName() 
//              + "] has more than one field with Annotation[" 
//              + annotClass.getSimpleName() + "].");
//      }
//      
//      Field field = fieldList.get(0);
//      fieldName = field.getName();
//      annotArr = field.getAnnotations();
//      
//      get = ReflectionInfo.getGetMethod(entityClass, fieldName);
//      if (get == null) {
//        throw new IllegalArgumentException("Unable to locate Entity[" 
//                + this.getEntityName() + "].Field[" + fieldName + "]'s GET-Method.");
//      }
//
//      set = ReflectionInfo.getSetMethod(entityClass, fieldName);
//    } else {
//      List<Method> methodList = this.onGetMethodsByAnnot(entityClass,annotClass);
//      if (!methodList.isEmpty()) {
//        if (methodList.size() > 1) {
//          throw new IllegalArgumentException("Entity[" + this.getEntityName() 
//              + "] has more than one field with Annotation[" 
//              + annotClass.getSimpleName() + "].");
//        }
//        
//        Method method = methodList.get(0);
//        fieldName = ReflectionInfo.getFieldname(method.getName());
//        annotArr = method.getAnnotations();
//        Class returnType = method.getReturnType();
//        if ((returnType == null) && (method.getReturnType().equals(void.class))) {
//          set = method;
//          get = ReflectionInfo.getGetMethod(entityClass, fieldName);
//          if (get == null) {
//            throw new IllegalArgumentException("Unable to locate Entity[" 
//                  + this.getEntityName() + "].Field[" + fieldName + "]'s GET-Method.");
//          }
//        } else {
//          get = method;
//          set = ReflectionInfo.getSetMethod(entityClass, fieldName);
//        }                
//      }
//    }
//      
//    if (fieldName != null) {
//      result = new FieldInfo(fieldName, get.getName(), 
//                      ((set == null)? null: set.getName()), 
//                      get.getReturnType(), annotArr);      
//    }
//      
//    return result;
//  }
//   
//  /**
//   * Get a list of fields that has the specified annotation class assigned.
//   * @param entityClass the bean class to initiate
//   * @param annotClass the annotation class to search for
//   * @return the list of matching fields or an empty list if none are found or the
//   * specified annotation class is unassigned.
//   */
//  protected List<Field> onGetFieldsByAnnot(Class<TBean> entityClass, 
//                                              Class<? extends Annotation> annotClass) {
//    List<Field> result = new ArrayList<>();
//    if ((annotClass != null) && (entityClass != null)) { 
//      for (Field field : entityClass.getDeclaredFields()) {
//        if (field.isAnnotationPresent(annotClass)) {
//          result.add(field);
//        }
//      }
//    }
//    return result;
//  }
//   
//  /**
//   * Get a list of Methods that has the specified annotation class assigned.
//   * @param entityClass the bean class to initiate
//   * @param annotClass the annotation class to search for
//   * @return the list of matching methods or an empty list if none are found or the
//   * specified annotation class is unassigned.
//   */
//  protected List<Method> onGetMethodsByAnnot(Class<TBean> entityClass, 
//                                              Class<? extends Annotation> annotClass) {
//    List<Method> result = new ArrayList<>();
//    if ((annotClass != null) && (entityClass != null)) { 
//      for (Method method : entityClass.getMethods()) {
//        if (method.isAnnotationPresent(annotClass)) {
//          result.add(method);
//        }
//      }
//    }
//    return result;
//  }
//  //</editor-fold>
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Properties">  
  /**
   * Get the registered EntityContext's Entity Bean class
   *
   * @return the assigned class.
   */
  public Class<TBean> getEntityClass() {
    return this.entityClass;
  }

  /**
   * Get the Entity Bean's Class Name
   *
   * @return this.entityClass.name
   */
  public String getEntityClassName() {
    return this.entityClass.getName();
  }

  /**
   * Get the Entity Bean Name
   *
   * @return this.entityClass.simpleName
   */
  public String getEntityName() {
    return this.entityClass.getSimpleName();
  }

  /**
   * Get the Entity's PuEntityManager through which persistent entities can be accessed.
   *
   * @return the assigned class
   */
  public Class<? extends PuEntityManager> getPuManagerClass() {
    return puManagerClass;
  }
  
  /**
   * Get a Collection of FieldInfo for a all fields supported by the Entity
   * @return this.fieldInfoMap.values
   */
  public Collection<FieldInfo> getAllFieldInfos() {
    return this.fieldInfoMap.values();
  }

  /**
   * Get the FieldInfo for the Entity Class' Field[fieldName]. If not located, try
   * this.getAliasField(fieldName) to retrieve the field's FieldInfo.
   * <p><b>Note:</b>Errors in
   * locating the field is logged and this method will return null.</p>
   * @param fieldName the field to search for (not case sensitive)
   * @return the FieldInfo or null if not found.
   */
  public FieldInfo getFieldInfo(String fieldName) {
    FieldInfo result = null;
    try {
      if (((fieldName = DataEntry.cleanLoString(fieldName)) != null) &&
              (this.fieldInfoMap.containsKey(fieldName))) {
        result = this.fieldInfoMap.get(fieldName);
      } else if (fieldName != null) {
        result = this.getAliasField(fieldName);
      }
    } catch (Exception pExp) {
      result = null;
    }
    return result;
  }

  /**
   * Get the Specified field's Field Input Validator. Call {@linkplain #getFieldInfo(
   * java.lang.String) getFieldInfo} to retrieve the field FieldInfo. If defined, it
   * calls the {@linkplain FieldInfo#getFieldValidator()} method.
   *
   * @param fieldName the field name to search for
   * @return the validator or null if the field's FieldInfo cannot be retrieved.
   */
  public FieldValidator getFieldValidator(String fieldName) {
    FieldInfo fieldInfo = this.getFieldInfo(fieldName);
    return (fieldInfo == null)? null : fieldInfo.validator;
  }

  /**
   * Get whether the Entity supports a GET-method for the specified field.
   *
   * @param fieldName the field to search for
   * @return true if the method is supported.
   */
  public boolean hasGetMethod(String fieldName) {
    return (this.getGetMethod(fieldName) != null);
  }

  /**
   * Get the Entity's GET-method for the specified field.
   * <p>
   * <b>NOTE:</b> The Field's GTE-Method could by non-public, but not static</p>
   * @param fieldName the field to search for
   * @return the method if supported or null.
   */
  public Method getGetMethod(String fieldName) {
    Method result = null;
    FieldInfo fieldInfo = this.getFieldInfo(fieldName);
    if (fieldInfo == null) {
      result = ReflectionInfo.getGetMethod(this.entityClass, fieldName);
      if ((result != null) && (Modifier.isStatic(result.getModifiers()))) {
        result = null;
      }
    } else {
      result = fieldInfo.getMethod;
    }
    return result;
  }

  /**
   * Get whether the Entity supports a SET-method for the specified field.
   *
   * @param fieldName the field to search for
   * @return true if the method is supported.
   */
  public boolean hasSetMethod(String fieldName) {
    return (this.getSetMethod(fieldName) != null);
  }

  /**
   * Get the Entity's SET-method for the specified field.
   * <p>
   * <b>NOTE:</b> The Field's GTE-Method could by non-public, but not static</p>
   * @param fieldName the field to search for
   * @return the method if supported or null.
   */
  public Method getSetMethod(String fieldName) {
    Method result = null;
    FieldInfo fieldInfo = this.getFieldInfo(fieldName);
    if (fieldInfo == null) {
      result = ReflectionInfo.getSetMethod(this.entityClass, fieldName);
      if ((result != null) && (Modifier.isStatic(result.getModifiers()))) {
        result = null;
      }
    } else {
      result = fieldInfo.setMethod;
    }
    return result;
  }

  /**
   * Get the Entity's list of Required Fields
   *
   * @return return the current list of required fields
   */
  public List<String> getRequiredFields() {
    return this.requiredFields;
  }

  /**
   * Get the Entity's list of Fields that requires some form of input validations. It
   * include the required fields, but exclude recordId.
   * @return return the current list of required fields
   */
  public List<String> getValidationFields() {
    return this.validationFields;
  }
  //</editor-fold>  

  //<editor-fold defaultstate="collapsed" desc="Entity Access Management">
  /**
   * Get the EntityValidation for the specified EntityWrpper class.
   *
   * @param reqClass the specified EntityWrpper class
   * @return the assigned EntityValidation if found or {@linkplain
   * #getDefaultEntityValidation() this.defaultEntityValidation} if not found.
   */
  public EntityValidation getEntityValidation(Class<? extends EntityWrapper> reqClass) {
    EntityValidation result = null;
    if (this.validationMap.containsKey(reqClass)) {
      result = this.validationMap.get(reqClass);
    }
    if (result == null) {
      result = this.getDefaultEntityValidation();
    }
    return result;
  }

  /**
   * Get the default EnityValidation (for EntityWrapper.Void.class)
   *
   * @return the assigned validation or new EntityValidation(this.entityClass) if not
   * found.
   */
  public EntityValidation getDefaultEntityValidation() {
    EntityValidation result = null;
    Class<? extends EntityWrapper> voidClass = EntityWrapper.Void.class;
    if (this.validationMap.containsKey(voidClass)) {
      result = this.validationMap.get(voidClass);
    }
    if (result == null) {
      result = new EntityValidation(this.entityClass);
    }
    return result;
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.accessFlags that specifies the allowed access to the
   * entity record.
   *
   * @return this.defaultEntityValidation.accessFlags
   */
  public EnumSet<AccessFlags> getAccessFlags() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.accessFlags;
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.isReadOnly setting.
   * @return this.defaultEntityValidation.isReadOnly
   */
  public boolean isReadOnly() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.isReadOnly();
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.allowEdits setting.
   * @return this.defaultEntityValidation.allowEdits
   */
  public boolean allowEdits() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.allowEdits();
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.allowAdd setting.
   * @return this.defaultEntityValidation.allowAdd
   */
  public boolean allowAdd() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.allowAdd();
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.allowDelete setting.
   *
   * @return this.defaultEntityValidation.allowDelete
   */
  public boolean allowDelete() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.allowDelete();
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.appTask setting.
   *
   * @return this.defaultEntityValidation.appTask
   */
  public AppTasks getAppTask() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.appTask;
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.subtask setting.
   *
   * @return this.defaultEntityValidation.subtask
   */
  public String getSubtask() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.subtask;
  }

  /**
   * Get the Entity's {@linkplain #getDefaultEntityValidation()
   * this.defaultEntityValidation}.doValidation setting.
   *
   * @return this.defaultEntityValidation.doValidation
   */
  public boolean doAccessValidation() {
    EntityValidation validation = this.getDefaultEntityValidation();
    return validation.doValidation;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Field Attributes">
  /**
   * Get the PrimaryKey' Type
   * @return this.primaryKey.primaryKeyType or null if this.primaryKey = null.
   */
  public PrimaryKeyType getPrimaryKeyType() {
    return (this.primaryKey == null)? null: this.primaryKey.getPrimaryKeyType();
  }

  /**
   * Get the PrimaryKey Field's FieldInfo
   * @return the assigned value
   */
  public FieldInfo getPrimaryKey() {
    return this.primaryKey;
  }

  /**
   * Get the FieldInfo for the specified alias.
   * @param alias the field alias to search for
   * @return the assigned value (can be null if not supported).
   */
  public FieldInfo getAliasField(String alias) {
    FieldInfo result = null;
    String fieldName = null;
    if (((alias = DataEntry.cleanLoString(alias)) != null) &&
            (this.aliasMap.containsKey(alias)) &&
            ((fieldName = aliasMap.get(alias)) != null) &&
            (this.fieldInfoMap.containsKey(fieldName))) {
      result = this.fieldInfoMap.get(fieldName);
    }
    return result;
  }

  /**
   * Check if the Entity has Field with the specified alias
   * @param alias the field alias to search for
   * @return true the entity has one field with an assigned {@linkplain RecordNameField}
   * annotation.
   */
  public boolean hasAliasField(String alias) {
    return (this.getAliasField(alias) != null);
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Report Writer">
  /**
   * Get the EnityContext Definition in HTML Format
   * @return an HTML formatted Strings
   */
  public String toHtmlString() {
    String result = "<h3 class='bwReport'>EntityContext[" + this.entityClass.getName()
            + "</h3><ul>";
    result += "<li>PuManager = " + this.getPuManagerClass().getSimpleName() + "</li>";
    result += "<li>PrimaryKey Type = " + this.getPrimaryKeyType() + "</li>";
    result += "<li><b>Enity Validation Settings:<b><ul>";
    for (EntityValidation validation : this.validationMap.values()) {
      result += "<li>" + validation.toHtmlString() + "</li>";
    }
    result += "</ul></li>";
    
    result += "<li><b>Field Information Definitions:<b><ul>";
    for (FieldInfo fieldInfo : this.fieldInfoMap.values()) {
      result += "<li>" + fieldInfo.toHtmlString() + "</li>";
    }
    result += "</ul></li>";
    return result;
  }
//</editor-fold>
}
