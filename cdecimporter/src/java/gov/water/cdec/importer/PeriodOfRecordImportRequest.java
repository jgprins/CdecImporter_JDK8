package gov.water.cdec.importer;

import org.json.JSONObject;


/**
 * A Class to capture the CDEC Data Import settings of a request.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class PeriodOfRecordImportRequest extends CdecImportRequest {

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The Import Start Date
   */
  public final Integer sensorId;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public PeriodOfRecordImportRequest(Integer sensorId) {
    super("Period of Record Data");      
    if (sensorId == null) {
      throw new NullPointerException("The PeriodOfRecordImportRequest's sensorId"
              + " is unassigned.");
    }
    this.sensorId = sensorId;
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
    jsonObj.put("sensorId", this.sensorId);
  }
  // </editor-fold>
}
