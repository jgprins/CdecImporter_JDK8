package bubblewrap.app.context;

import bubblewrap.io.DataEntry;

/**
 * A Class that encapsulate the Application's Main Properties - used by {@linkplain 
 * BwAppContext}
 * @author kprins
 */
public class BwAppInfo {
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the Application Name (to be assigned by the Application).
   */
  private String msAppName;
  /**
   * Placeholder for the Organization Name (to be assigned by the Application).
   */
  private String msOrgName;
  /**
   * Placeholder for the Acronym for the Organization Name (to be assigned by the
   * Application).
   */
  private String msOrgAcronym;
  /**
   * Placeholder for the Organization's Contact E-mail (to be assigned by the
   * Application).
   */
  private String msEmail;
  /**
   * Placeholder for the Organization's Contact TelNo (to be assigned by the
   * Application).
   */
  private String msTelNo;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public BwAppInfo(String sAppName, String sOrgName, String sOrgAcronym, String sEmail,
          String sTelNo) {
    sAppName = DataEntry.cleanString(sAppName);
    if (sAppName == null) {
      throw new NullPointerException("The AppInfo's Application Name cannot be "
              + "unassigned.");
    }
    sOrgName = DataEntry.cleanString(sOrgName);
    if (sOrgName == null) {
      throw new NullPointerException("The AppInfo's Organization Name cannot be "
              + "unassigned.");
    }
    sOrgAcronym = DataEntry.cleanString(sOrgAcronym);
    if (sOrgAcronym == null) {
      throw new NullPointerException("The AppInfo's Organization Acronym cannot be "
              + "unassigned.");
    }
    sEmail = DataEntry.cleanString(sEmail);
    if (sEmail == null) {
      throw new NullPointerException("The AppInfo's Organization Contact Email "
              + "cannot be unassigned.");
    }
    sTelNo = DataEntry.cleanString(sTelNo);
    if (sTelNo == null) {
      throw new NullPointerException("The AppInfo's Organization Contact Telephone "
              + "Number cannot be unassigned.");
    }
    
    this.msAppName = sAppName;
    this.msOrgName = sOrgName;
    this.msOrgAcronym = sOrgAcronym;
    this.msEmail = sEmail;
    this.msTelNo = sTelNo;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methdos">
  /**
   * Get the Application's Name
   * @return the assigned value
   */
  public String getAppName() {
    return msAppName;
  }
  
  /**
   * Get the Organization's Name
   * @return the assigned value
   */
  public String getOrgName() {
    return msOrgName;
  }
  
  /**
   * Get the Organization's Name Acronym
   * @return the assigned value
   */
  public String getOrgAcronym() {
    return msOrgAcronym;
  }
  
  /**
   * Get the Organization's Contact email
   * @return the assigned value
   */
  public String getEmail() {
    return msEmail;
  }
  
  /**
   * Get the Organization's Contact Telephone Name
   * @return the assigned value
   */
  public String getTelNo() {
    return msTelNo;
  }
  //</editor-fold>
}
