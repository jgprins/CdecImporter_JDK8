package bubblewrap.core.reflection;

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

/**
 * A PackageLoader for retrieving classes from a specified Jar-file and package.
 * @author kprins
 */
public class JarPackageLoader extends PackageLoader {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The name of the Jar file
   */
  private String jarName;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * A Constructor with a Package Name and a Jar file reference
   * @param packageName the Package Name
   */
  public JarPackageLoader(String packageName) {
    super(packageName);  
    this.jarName =  this.getJarFile(); 
    if (this.jarName == null) {
      throw new NullPointerException("The package's Jar File name cannot be "
              + "unassigned");
    }
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
  @SuppressWarnings("unchecked")
  @Override
  public List<Class> getClasses() {
    List<Class> pResult = new ArrayList<>();
    try {
      String sClassPath = 
          this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
      File pMyJar = new File(sClassPath);
      String sLibPath = pMyJar.getParent();
      String sPackageName = this.getPackageName();     
      
      File pJarPath = new File(sLibPath,this.jarName);
      if (!pJarPath.exists()){
        throw new Exception("Unable to locate Jar File[" + this.jarName 
                                                  + "] in Path[" + sLibPath + "].");
      }

      JarFile pJarFile = new JarFile(pJarPath);
      if (pJarFile == null) {
        throw new Exception("Unable to access Jar File[" + this.jarName + "].");
      }
      
      Class baseClass = this.getBaseClass();
      Class intfClass = this.getIntefaceClass();
      Class annotClass = this.getAnnotationClass();
      
      String sPackagePath = sPackageName.replace(".", "/") + "/";      
      Enumeration<JarEntry> pEntries = pJarFile.entries();
      while (pEntries.hasMoreElements()) {
        JarEntry pJarEntry = pEntries.nextElement();
        if (pJarEntry.isDirectory()) {
          continue;
        }
        
        String sEntryName = pJarEntry.getName();
        if (!sEntryName.startsWith(sPackagePath) || (!sEntryName.endsWith(".class"))) {
          continue;
        }
        sEntryName = sEntryName.substring(0, sEntryName.length()-6);
        String sClassName = sEntryName.replaceAll("/", "\\.");
        try {
          Class<?> pClass = Class.forName(sClassName);
          if (pClass != null) {
            if ((this.doIgnoreAbstract())
                    && (Modifier.isAbstract(pClass.getModifiers()))) {
              continue;
            } else if ((this.doIgnoreInterfaces())
                    && (pClass.isInterface())) {
              continue;
            } else if ((baseClass != null) && (!baseClass.isAssignableFrom(pClass))) {
              continue;
            } else if ((intfClass != null) && (!intfClass.isAssignableFrom(pClass))) {
              continue;
            } else if ((annotClass != null) && 
                       (!pClass.isAnnotationPresent(annotClass))) {
              continue;
            }

            pResult.add(pClass);
          }
        } catch (ClassNotFoundException pExp) {
          logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n "
                  + "Class[{1}] seems to be an invalid class "
                  + "or could not be found in the project.",
                  new Object[]{this.getClass().getSimpleName(), sClassName});
        }        
      }
      
      if (pResult.isEmpty()) {
        throw new Exception("No inheritor of Class[" + baseClass.getName()
                + "] found in jar[" + this.jarName + "]");
      }
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetClassesInPackage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return pResult;
  }
}
