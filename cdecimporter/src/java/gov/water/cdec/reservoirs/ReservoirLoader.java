package gov.water.cdec.reservoirs;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
//@Startup
//@Singleton(name = "ReservoirLoader")
//@DependsOn(value = "BwAppContext")
public class ReservoirLoader {
  //<editor-fold defaultstate="collapsed" desc="Static Logger">

  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ReservoirLoader.class.getName());
  //</editor-fold>        
  
  private static long LazyLoadWait = 10000l;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ReservoirLoader() {
    super();  
  }
  /**
   * <p>
   * A private PostConstruct method to initiate the BwAppContext's core settings:</p>
   * <ul>
   * <li>It assign this instance as static this.mpAppContext</li>
   * <li>It adds BwCoreExtension to the AppExtension list.</li>
   * </ul>
   */
  @PostConstruct
  protected void initAppContext() {
    String sClass = this.getClass().getSimpleName();
    logger.log(Level.INFO, "{0}.initReservoirLoader Start", sClass);
    try {
      ReservoirDataMap dataMap = ReservoirDataMap.getInstance();
      if ((dataMap == null) || (dataMap.isEmpty())) {
        throw new Exception("The ReservoirDataMap is not accesible or empty");
      }
      
      ReservoirLoadRunnable runnable = new ReservoirLoadRunnable(dataMap);
      Thread loadThread = new Thread(runnable);
      loadThread.start();
      logger.log(Level.INFO, "{0}.initReservoirLoader Done", sClass);
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initReservoirLoader Failed:\n {1}",
              new Object[]{sClass, exp.getMessage()});
    }
  }

  /**
   * <p>
   * A private PreDestroy method to reset this.mpAppContext and to clear the
   * Application Context's content. Calling {@linkplain #clearAppContext()
   * clearAppContext}.</p>
   */
  @PreDestroy
  protected void destroyAppContext() {
  }
  // </editor-fold>
}
