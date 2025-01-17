package bubblewrap.core.reflection;

import bubblewrap.core.interfaces.IEntityLoader;
import bubblewrap.entity.context.PuEntityManager;
import bubblewrap.io.DataEntry;
import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javax.persistence.Entity;

/**
 * A PackageLoader for retrieving the Bean Entity Classes from a specified Jar-file 
 * and package. 
 * <p>
 * <b>NOTE:</b> This Class must be locally inheritor to get a reference to the local Jar 
 * file. It cannot be used outside its own jar.</p>
 * @author kprins
 */
public abstract class JarEntityLoader extends PackageLoader implements IEntityLoader{
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The name of the Jar file
   */
  private String jarName;
  /**
   * Reference to the Entities' Common PuEntityManager
   */
  private Class<? extends PuEntityManager> entityManagerClass;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * A Constructor with a Package Name and a Jar file reference
   * @param sName the Package Name
   * @param sJarName the Jar File Name
   */
  public JarEntityLoader(String sName, Class<? extends PuEntityManager> entMngrClass) {
    super(sName);  
    this.jarName = this.getJarFile();
    if (this.jarName == null) {
      throw new NullPointerException("The package's Jar File name cannot be "
              + "unassigned");
    }
    if (entMngrClass == null) {
      throw new NullPointerException("The EntityLoader's PuEntityManager cannot be "
              + "unassigned.");
    }
    this.entityManagerClass = entMngrClass;
    this.setFilters(true, true);
  }
  
  private String getJarFile() throws IllegalArgumentException {
    String result = null;
    try{
      Class srcClass = this.getClass();
      String path = srcClass.getResource(srcClass.getSimpleName() + ".class").getPath();
      String jarFilePath = path.substring(path.indexOf(":") + 1, path.indexOf("!"));
      jarFilePath = URLDecoder.decode(jarFilePath, "UTF-8");
      File jarFile = new File(jarFilePath);
      result = jarFile.getName();
    } catch (Exception exp) {
      throw new IllegalArgumentException(this.getClass().getSimpleName()
              + ".getJarFile Error:\n " + exp.getMessage());
    }
    return result;
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
    List<Class> result = new ArrayList<>();
    try {
      String classPathName = 
          this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
      File classPath = new File(classPathName);
      String libPath = classPath.getParent();
      String packageName = this.getPackageName();     
      
      File jarPath = new File(libPath,this.jarName);
      if (!jarPath.exists()){
        throw new Exception("Unable to locate Jar File[" + this.jarName 
                                                  + "] in Path[" + libPath + "].");
      }

      JarFile jarFile = new JarFile(jarPath);
      if (jarFile == null) {
        throw new Exception("Unable to access Jar File[" + this.jarName + "].");
      }
      
      String packagePath = packageName.replace(".", "/") + "/";      
      Enumeration<JarEntry> entries = jarFile.entries();
      while (entries.hasMoreElements()) {
        JarEntry jarEntry = entries.nextElement();
        if (jarEntry.isDirectory()) {
          continue;
        }
        
        String entryName = jarEntry.getName();
        if (!entryName.startsWith(packagePath) || (!entryName.endsWith(".class"))) {
          continue;
        }
        entryName = entryName.substring(0, entryName.length()-6);
        String className = entryName.replaceAll("/", "\\.");
        try {
          Class<?> entryClass = Class.forName(className);
          if (entryClass != null) {
            if ((this.doIgnoreAbstract())
                    && (Modifier.isAbstract(entryClass.getModifiers()))) {
              continue;
            } else if ((this.doIgnoreInterfaces())
                    && (entryClass.isInterface())) {
              continue;
            } else if (entryClass.isAnnotationPresent(Entity.class)) {
              result.add(entryClass);
            }
          }
        } catch (ClassNotFoundException pExp) {
          logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n "
                  + "Class[{1}] seems to be an invalid class "
                  + "or could not be found in the project.",
                  new Object[]{this.getClass().getSimpleName(), className});
        }        
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
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
