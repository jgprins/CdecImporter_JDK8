package gov.water.cdec.reservoirs;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Runnable to ZLazy-Load the ReservoirDataMap data on deployment
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirLoadRunnable implements Runnable {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ReservoirLoadRunnable.class.getName());
  //</editor-fold>        

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the owner ReservoirDataMap
   */
  private ReservoirDataMap dataMap;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ReservoirLoadRunnable(ReservoirDataMap dataMap) {
    super();      
    this.dataMap = dataMap;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Override Runnable">
  @Override
  public void run() {
    if ((this.dataMap == null) || (this.dataMap.isEmpty())) {
      return;
    }
    
    for (ReservoirData resData : this.dataMap) {
      try {
        resData.update(false);
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.LazyLoad Reservoir[{1}] Error:\n {2}",
                new Object[]{this.getClass().getSimpleName(), resData.reservoidDef.id(),
                  exp.getMessage()});
      }
    }
  }
  // </editor-fold>
}
