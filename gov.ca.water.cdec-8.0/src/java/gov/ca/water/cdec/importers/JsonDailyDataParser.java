package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.DailyDataMap;
import gov.ca.water.cdec.core.DateKey;
import gov.ca.water.cdec.entities.DailyData;
import java.util.TimeZone;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class JsonDailyDataParser extends JsonTimeSeriesParser<DailyData, DateKey, 
                                                              DailyDataMap>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public JsonDailyDataParser() {
    super(TimeZone.getTimeZone("PST"));  
  }
  // </editor-fold>
}
