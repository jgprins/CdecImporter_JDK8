package bubblewrap.core.reflection;

import bubblewrap.core.interfaces.IEntityLoader;
import bubblewrap.entity.context.PuEntityManager;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.Entity;

/**
 * A PackageLoader for retrieving the Bean Entity Classes of a "local" (or project) 
 * package.
 * @author kprins
 */
public class LocalEntityLoader extends PackageLoader implements IEntityLoader {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Reference to the Entities' Common PuEntityManager
   */
  private Class<? extends PuEntityManager> entityManagerClass;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public LocalEntityLoader(String sName, Class<? extends PuEntityManager> entMngrClass) {
    super(sName);  
    this.setFilters(true, true);
    if (entMngrClass == null) {
      throw new NullPointerException("The EntityLoader's PuEntityManager cannot be "
              + "unassigned.");
    }
    this.entityManagerClass = entMngrClass;
  }
  // </editor-fold>

  /**
   * {@inheritDoc} <p>OVERRIDE: Get local resource path for the Package, retrieve all
   * classes and return the classes. If this.doIgnoreAbstract and/or 
   * this.doIgnoreInterfaces, the Abstract classes and/or interfaces are ignored,
   * respectively.</p>
   */
  @Override
  public List<Class> getClasses() {
    List<Class> pResult = new ArrayList<>();
    try {
      String sPackageName = this.getPackageName();
      String sPackagePath = this.getPackagePath();      
      // Get a File object for the package
      URL pPackageUrl =
              Thread.currentThread().getContextClassLoader().getResource(sPackagePath);
      if (pPackageUrl == null) {
        throw new Exception("Could not retrieve URL resource: " + sPackagePath);
      }

      String sPackageDir = pPackageUrl.getFile();
      if (sPackageDir == null) {
        throw new Exception("Could not find directory for URL resource: " 
                + sPackagePath);
      }

      File pFolder = new File(sPackageDir);
      if (!pFolder.exists()) {
        throw new Exception("Directory[" + sPackageDir
                + "] is a invalid path on the file system.");
      }
      // Get the list of the files contained in the package
      String[] pFileList = pFolder.list();
      for (String sFileName : pFileList) {
        // We are only interested in .class files
        if (sFileName.endsWith(".class")) {
          // Remove the .class extension
          String sClassName = sFileName.substring(0, sFileName.length() - 6);
          sClassName = sPackageName + "." + sClassName;
          try {
            Class<?> pClass = Class.forName(sClassName);
            if (pClass != null) {
              if ((this.doIgnoreAbstract()) 
                                    && (Modifier.isAbstract(pClass.getModifiers()))) {
                continue;
              } else if ((this.doIgnoreInterfaces()) 
                                    && (pClass.isInterface())) {
                continue;
              } else if (pClass.isAnnotationPresent(Entity.class)) {
                pResult.add(pClass);
              }
            }
          } catch (ClassNotFoundException pExp) {
            logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n "
                    +"Class[{1}] seems to be an invalid class "
                    + "or could not be found in the project.",
                    new Object[]{this.getClass().getSimpleName(), sClassName});
          }
        }
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }

  /**
   * {@inheritDoc }
   * <p>IMPLEMENT: Return the assigned Manager Class</p>
   */
  @Override
  public Class<? extends PuEntityManager> getEntityManagerClass() {
    return this.entityManagerClass;
  }
}
