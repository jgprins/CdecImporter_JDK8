package gov.water.cdec.reservoirs;

import bubblewrap.io.DataEntry;
import gov.ca.water.cdec.core.CdecSensorTypes;
import gov.water.cdec.reservoirs.annotations.ReservoirDef;
import gov.water.cdec.reservoirs.annotations.ReservoirDefs;
import gov.water.cdec.reservoirs.annotations.ReservoirImpl;
import gov.water.cdec.reservoirs.annotations.SensorDef;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

@ReservoirDefs({
  @ReservoirDef(
          id = "PNF",
          name = "Kings River",
          capacity = 1000.0,
          sensorQin = @SensorDef(
                  stationId = "PNF",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "PNF",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "PNF",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "PNF",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "PNF",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "KGF"
  ),
  @ReservoirDef(
          id = "MIL",
          name = "San Joaquin River",
          capacity = 520.50,
          sensorQin = @SensorDef(
                  stationId = "MIL",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "MIL",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "MIL",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "MIL",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "MIL",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "SJF"
  ),
  @ReservoirDef(
          id = "EXC",
          name = "Merced River",
          capacity = 1024.60,
          sensorQin = @SensorDef(
                  stationId = "EXC",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "EXC",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "EXC",
                  sensorType = CdecSensorTypes.DailyResIn                  
          ),
          sensorStor = @SensorDef(
                  stationId = "EXC",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "EXC",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "MRC"
  ),
  @ReservoirDef(
          id = "DNP",
          name = "Tuolumne  River",
          capacity = 2030.0,
          sensorQin = @SensorDef(
                  stationId = "DNP",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "DNP",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "TLG",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "DNP",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "DNP",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "TLG"
  ),
  @ReservoirDef(
          id = "NML",
          name = "Stanislaus River",
          capacity = 2400.0,
          sensorQin = @SensorDef(
                  stationId = "NML",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "NML",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "NML",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "NML",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "NML",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "SNS"
  ),
  @ReservoirDef(
          id = "FOL",
          name = "American River",
          capacity = 977.0,
          sensorQin = @SensorDef(
                  stationId = "FOL",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "FOL",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "FOL",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "FOL",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "FOL",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "AMF"
  ),
  @ReservoirDef(
          id = "ORO",
          name = "Feather River",
          capacity = 3537.60,
          sensorQin = @SensorDef(
                  stationId = "ORO",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "ORO",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "ORO",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "ORO",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "ORO",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "FTO"
  ),
  @ReservoirDef(
          id = "SHA",
          name = "Shasta Lake",
          capacity = 4552.0,
          sensorQin = @SensorDef(
                  stationId = "SHA",
                  sensorType = CdecSensorTypes.DailyResIn
          ),
          sensorQout = @SensorDef(
                  stationId = "SHA",
                  sensorType = CdecSensorTypes.DailyResOut                  
          ),
          sensorFnf = @SensorDef(
                  stationId = "SHA",
                  sensorType = CdecSensorTypes.DailyFNF                  
          ),
          sensorStor = @SensorDef(
                  stationId = "SHA",
                  sensorType = CdecSensorTypes.DailyResStorage                  
          ),
          sensorToc = @SensorDef(
                  stationId = "SHA",
                  sensorType = CdecSensorTypes.DailyTOC                 
          ),
          wsFcastId = "SIS"
  )
})
/**
 * A Singleton Map Class for storing the Reservoir Definitions
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ReservoirDataMap implements Serializable, Iterable<ReservoirData> {
  
  //<editor-fold defaultstate="collapsed" desc="Static Logger">
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger
          = Logger.getLogger(ReservoirDataMap.class.getName());
  //</editor-fold>        
  
  //<editor-fold defaultstate="collapsed" desc="ReservoirDataMap Singleton">
  /**
   * Static class for holding and initiating the ReservoirDataMap singleton in.
   */
  private static class ReservoirInfoMapHolder {
    private static final ReservoirDataMap INSTANCE = new ReservoirDataMap();
  }

  /**
   * Static method for accessing the Singleton
   * @return ReservoirInfoMapHolder.INSTANCE
   */
  public synchronized static ReservoirDataMap getInstance() {
    return ReservoirInfoMapHolder.INSTANCE;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private HashMap<String, ReservoirData> resInfoMap;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  private ReservoirDataMap() {
    super(); 
    this.resInfoMap = new HashMap<>();
    this.initReservoirData();
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private/Protected Methods">
  private void initReservoirData() {
    try {      
      ReservoirDefs annotArr = null;
      if ((!this.getClass().isAnnotationPresent(ReservoirDefs.class)) ||
          ((annotArr = (ReservoirDefs) 
                        this.getClass().getAnnotation(ReservoirDefs.class)) == null) ||
          (annotArr.value() == null) || (annotArr.value().length == 0)) { 
        throw new Exception("The ReservoirInfoMap's ReservoirDefs annotaion is not "
                + "accessible or is undefined or empty");
      }
      
      ReservoirImpl annotImpl = null;
      for (ReservoirDef annot : annotArr.value()) {
        try {
          if ((annotImpl = new ReservoirImpl(annot)) == null) {
            throw new Exception("Loading ReservoirDef[" + annot.id() + "] failed.");
          }
          
          if (this.resInfoMap.containsKey(annotImpl.id())){
            throw new Exception("Duplicate ReservoirDef[" + annot.id() + "].");
          }
          
          ReservoirData resData = new ReservoirData(annotImpl);
          this.resInfoMap.put(annotImpl.id(), resData);          
        } catch (Exception exp) {
          logger.log(Level.WARNING, "{0}.read ReservoirDefs Error:\n {1}",
                  new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
        }
      }      
    } catch (Exception exp) {
      logger.log(Level.WARNING, "{0}.initReservoirData Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Map Methods">
  /**
   * Check if the Map is empty
   * @return this.resInfoMap.isEmpty()
   */
  public synchronized boolean isEmpty() {
    return this.resInfoMap.isEmpty();
  }

  /**
   * Get the ReservoirData for the specified reservoir ID
   * @param resId the specified reservoir ID
   * @return the reservoir Info or null if not found
   */
  public synchronized ReservoirData get(String resId) {
    ReservoirData result = null;
    if (((resId = DataEntry.cleanUpString(resId)) != null) &&
            (this.resInfoMap.containsKey(resId))) {
      result = this.resInfoMap.get(resId);
    }
    return result;
  }

  /**
   * Check if the Map containing the Reservoir Information for the specified reservoir ID
   * @param resId the specified reservoir ID
   * @return true if found
   */
  public synchronized boolean containsKey(String resId) {
    return (((resId = DataEntry.cleanUpString(resId)) != null) &&
            (this.resInfoMap.containsKey(resId)));
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: </p>
   */
  @Override
  public String toString() {
    return super.toString();
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Iterator Object">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: return this.resInfoMap.values().iterator()</p>
   */
  @Override
  public synchronized Iterator<ReservoirData> iterator() {
    return this.resInfoMap.values().iterator();
  }
  // </editor-fold>
}
