package gov.water.cdec.importer;

import gov.ca.water.cdec.core.CdecSensorTypes;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONObject;

/**
 * A Class to capture the CDEC Data Import settings of a request.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class TimeSeriesImportRequest extends CdecImportRequest {

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Import Start Date
   */
  public final Date startDate;
  /**
   * The Import Start Date
   */
  public final Date endDate;
  /**
   * The Import Start Date
   */
  public final CdecSensorTypes[] sensorTypes;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public TimeSeriesImportRequest(String requestType, Date startDate, Date endDate,
                                                     CdecSensorTypes...sensorTypes) {
    super(requestType);      
    if (startDate == null) {
      throw new NullPointerException("The CdecImportRequest's startDate is unassigned.");
    }
    if (endDate == null) {
      throw new NullPointerException("The CdecImportRequest's endDate is unassigned.");
    }
    if ((sensorTypes == null) || (sensorTypes.length == 0)) {
      throw new NullPointerException("The CdecImportRequest's sensorTypes is unassigned.");
    }
    this.startDate = startDate;
    this.endDate = endDate;
    this.sensorTypes = sensorTypes;
  }
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Override CdecImportRequest">
  /**
   * {@inheritDoc}
   * <p>
   * OVERRIDE: Add the startDate and endDate properties</p>
   */
  @Override
  protected void onAsJson(JSONObject jsonObj) {
    SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
    jsonObj.put("startDate", formatter.format(this.startDate));
    jsonObj.put("endDate", formatter.format(this.endDate));
    
  }
  // </editor-fold>
}
