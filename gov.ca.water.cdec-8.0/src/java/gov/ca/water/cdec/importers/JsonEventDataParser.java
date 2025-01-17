package gov.ca.water.cdec.importers;

import gov.ca.water.cdec.core.*;
import gov.ca.water.cdec.entities.EventData;
import java.util.TimeZone;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class JsonEventDataParser extends JsonTimeSeriesParser<EventData, EventStepKey, 
                                                              EventDataMap>{

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public JsonEventDataParser() {
    super(TimeZone.getTimeZone("PST"));  
  }
  // </editor-fold>
}
