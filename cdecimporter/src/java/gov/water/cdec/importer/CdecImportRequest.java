package gov.water.cdec.importer;

import gov.ca.water.cdec.importers.ImportUtils;
import java.io.Serializable;
import org.json.JSONObject;

/**
 * Base CDEC Import Request
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public abstract class CdecImportRequest implements Serializable {

  //<editor-fold defaultstate="collapsed" desc="CdecImportRequest.Status Enum">
  /**
   * A Request Status Enum
   */
  public enum Status {
    Pending,
    Executing,
    Completed,
    Failed;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * The Import request's ID
   */
  public final String requestId;
  /**
   * The Import request's ID
   */
  public final String requestType;
  /**
   * The request current status
   */
  private Status requestStatus;
  /**
   * The request current percent completed
   */
  private Integer percCompleted;
  /**
   * The Launch Error Message
   */
  private String error;
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor
   * @param requestType the type of request
   */
  protected CdecImportRequest(String requestType) {
    super();
    if ((requestType = ImportUtils.cleanString(requestType)) == null) {
      throw new NullPointerException("The CdecImportRequest's requestType is unassigned.");
    }
    this.requestId = ImportUtils.newUniqueID();
    this.requestType = requestType;
    this.requestStatus = Status.Pending;
    this.percCompleted = null;
  }
  //</editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the current Request Status  
   * @return the current status
   */
  public Status getRequestStatus() {
    return this.requestStatus;  
  }

  /**
   * Check if the Process is Pending
   * @return this.requestStatus = Pending
   */
  public boolean isPending() {
    return this.requestStatus.equals(Status.Pending);
  }

  /**
   * Check if the Process is executing
   * @return this.requestStatus = Executing
   */
  public boolean isExecting() {
    return this.requestStatus.equals(Status.Executing);
  }

  /**
   * Check if the Process is completed
   * @return this.requestStatus = Completed
   */
  public boolean isCompleted() {
    return this.requestStatus.equals(Status.Completed);
  }

  /**
   * Check if the Process has failed
   * @return this.requestStatus = Failed
   */
  public boolean isFailed() {
    return this.requestStatus.equals(Status.Failed);
  }

  /**
   * Get the percent completed. 0 if this.status = Pending; 100 if this.status =
   * Completed. Otherwise the assigned value.
   * @return a value between 0..100
   */
  public Integer getPercCompleted() {
    Integer result = 0;
    if (this.requestStatus.equals(Status.Executing)) {
      result = (this.percCompleted == null) ? 0 : this.percCompleted;
    } else if (this.requestStatus.equals(Status.Completed)) {
      result = 100;
    }
    return result;
  }

  /**
   * Set the percent completed.
   * @param percCompleted a value between 0..100.
   */
  public void setPercCompleted(Integer percCompleted) {
    if (this.requestStatus.equals(Status.Executing)) {
      this.percCompleted = ((percCompleted == null) || (percCompleted < 0)) ? null : 
                            ((percCompleted > 100) ? 100 : percCompleted);
    }
  }

  /**
   * Called to set this.status = Executing
   */
  public void startExecution() {
    this.requestStatus = Status.Executing;
  }

  /**
   * Called to set this.status = Completed
   */
  public void completeExecution() {
    this.requestStatus = Status.Completed;
    this.percCompleted = 100;
  }

  /**
   * Called to set this.error. It also set this.status = Failed
   * @param errMsg the error message (set as "Unknown error." if null)
   */
  public void setError(String errMsg) {
    if ((errMsg = ImportUtils.cleanString(errMsg)) == null) {
      errMsg = "Unknown error.";
    }
    this.error = errMsg;
    this.requestStatus = Status.Failed;
    this.percCompleted = null;
  }

  /**
   * Get the Import Request instance properties in JSON Object with the following:
   * <p>
   * Element["request"].{"requestId", "requestType", "startDate", "endDate", "status"
   * (,"percCompleted")(,"error")}
   * @return the JSONObject
   */
  public JSONObject getAsJSON() {
    JSONObject result = new JSONObject();
    result.put("requestId", this.requestId);
    result.put("requestType", this.requestType);
    result.put("status", this.requestStatus.toString());
    if ((this.percCompleted != null) && (!this.requestStatus.equals(Status.Pending)) && (!this.requestStatus.equals(Status.Failed))) {
      result.put("percCompleted", this.getPercCompleted());
    }
    if (this.error != null) {
      result.put("error", this.error);
    }
    this.onAsJson(result);
    return result;
  }
  
  /**
   * CAN OVERRIDE: Override to add additional properties to the JSON Object
   * @param jsonObj the JSONObject to update
   */
  protected void onAsJson(JSONObject jsonObj){}
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * {@inheritDoc}
   * <p>OVERRIDE: CdecImportRequest[requestId = ??; requestType = ??; status = ??;
   * (; percCompleted = ??%)]</p>
   */
  @Override
  public String toString() {
    String result = this.getClass().getSimpleName() 
            + "[requestId = " + this.requestId 
            + "; requestType = " + this.requestType 
            + "; status = "  + this.requestStatus.toString();
    if ((this.percCompleted != null) && (!this.requestStatus.equals(Status.Pending)) && 
            (!this.requestStatus.equals(Status.Failed))) {
      result += "; percCompleted = " + this.percCompleted.toString() + "%";
    }
    result += "]";
    return result;
  }
  // </editor-fold>
}
