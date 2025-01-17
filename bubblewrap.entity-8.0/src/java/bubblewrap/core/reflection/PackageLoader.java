package bubblewrap.core.reflection;

import bubblewrap.io.DataEntry;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Abstract class for loading a Classes for a specified Package
 * @author kprins
 */
public abstract class PackageLoader implements Serializable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(PackageLoader.class.getName());
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Private Field">
  /**
   * The Package Name
   */
  private String packageName;
  /**
   * A flag to control whether Abstract classes should be ignored. (default=true)
   */
  private Boolean doIgnoreAbstract;
  /**
   * A flag to control whether Interfaces should be ignored. (Default=true)
   */
  private Boolean doIgnoreInterfaces;
  /**
   * A Placeholder for a base class filter
   */
  private Class baseClass;
  /**
   * A Placeholder for a base class filter
   */
  private Class interfaceClass;
  /**
   * A Placeholder for a base class filter
   */
  private Class annotationClass;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public PackageLoader(String sName) {
    sName = DataEntry.cleanString(sName);
    if (sName == null) {
      throw new NullPointerException("The Package Name cannot ne undefined.");
    }    
    this.packageName = sName;
    this.doIgnoreAbstract = null;
    this.doIgnoreInterfaces = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Package's name
   * @return the assigned name
   */
  public final String getPackageName() {
    return packageName;
  }
  
  /**
   * Get the "/" path for based on the Package Name
   * @return the Package's relative path
   */
  public final String getPackagePath() {
    return "/" + this.packageName.replace(".", "/") + "/";
  }
  
  /**
   * Set the Class Retrieval filter
   * @param bIgnoreAbstract true to ignore Abstract classes (default=true)
   * @param bIgnoreInterfaces true to ignore Interfaces (default=true)
   */
  protected void setFilters(boolean bIgnoreAbstract, boolean bIgnoreInterfaces) {
    this.doIgnoreAbstract = (bIgnoreAbstract)? null: bIgnoreAbstract;
    this.doIgnoreInterfaces = (bIgnoreInterfaces)? null: bIgnoreInterfaces;
  }
  
  /**
   * Called to set the Loader's Class Filters.
   * @param baseClass the base class
   * @param interfaceClass the base interface class
   * @param annotationClass the Type annotation class
   */
  protected void setClassFilters(Class baseClass, Class interfaceClass, 
            Class annotationClass) {
    this.baseClass = baseClass;
    this.interfaceClass = interfaceClass;
    this.annotationClass = annotationClass;
  }
  
  /**
   * Get whether the doIgnoreAbstract flag is set 
   * @return return true to ignore Abstract classes
   */
  public boolean doIgnoreAbstract() {
    return ((this.doIgnoreAbstract == null) || (this.doIgnoreAbstract));
  }
  
  /**
   * Get whether the doIgnoreInterfaces flag is set 
   * @return return true to ignore interfaces
   */
  public boolean doIgnoreInterfaces() {
    return ((this.doIgnoreInterfaces == null) || (this.doIgnoreInterfaces));
  }
  
  /**
   * Get the BaseClass to filter on
   * @return return assigned filter value - can be null
   */
  public Class getBaseClass() {
    return this.baseClass;
  }
  
  /**
   * Get the IntefaceClass to filter on
   * @return return assigned filter value - can be null
   */
  public Class getIntefaceClass() {
    return this.interfaceClass;
  }
  
  /**
   * Get the AnnotationClass to filter on
   * @return return assigned filter value - can be null
   */
  public Class getAnnotationClass() {
    return this.annotationClass;
  }
  //</editor-fold>
  
  /**
   * ABSTRACT: Get the Classes from the package in the package
   * @return a list of classes
   */
  public abstract List<Class> getClasses();
}
