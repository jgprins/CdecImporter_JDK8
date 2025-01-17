package bubblewrap.admin.events;

import bubblewrap.io.DataEntry;

/**
 *
 * @author kprins
 */
@Deprecated
public class ValidateAccessArgs {

// <editor-fold defaultstate="collapsed" desc="Private Fields">

  /**
   * Placeholder for a Reason why access was denied
   */
  private String msReason = null;
  /**
   * Falg controling the hasAccess state (default=true)
   */
  private boolean mbHasAccess = true;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor
   */
  public ValidateAccessArgs() {
  }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the Arg's hasAccess State
   * @return
   */
  public boolean hasAccess() {
    return this.mbHasAccess;
  }

  /**
   * Get the reason for an access denial - return null if hasAccess. Otherwise, return
   * the set reason or "Unknown Reason" if not set.
   * @return String
   */
  public String getReason() {
    String sResult = null;
    if (!this.mbHasAccess) {
      sResult = (this.msReason == null)? "Unknown Reason": this.msReason;
    }
    return sResult;
  }

  /**
   * Set the hasAccess=false and supply a reason for the denial.
   * @param sReason String
   */
  public void denyAccess(String sReason) {
    this.msReason = DataEntry.cleanString(sReason);
    this.mbHasAccess = false;
  }
// </editor-fold>
}
