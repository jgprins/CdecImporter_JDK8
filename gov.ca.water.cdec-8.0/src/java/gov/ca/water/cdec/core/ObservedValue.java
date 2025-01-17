package gov.ca.water.cdec.core;

import gov.ca.water.cdec.importers.ImportUtils;
import java.io.Serializable;
import java.util.Date;

/**
 * A wrapper used in submitting observed data to the CDEC Database.
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public class ObservedValue implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * The observed value (can be null)
   */
  public Double obsValue;
  /**
   * The (optional) observed date
   */
  public Date obsDate;
  /**
   * The (optional) Data Flag (only assigned to new records). DataFlag = 'r' is used
   * for updated records. I can only be one character.
   */
  public String dataFlag;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public ObservedValue(Double obsValue, Date obsDate, String dataFlag) {
    super();  
    this.obsValue = obsValue;
    this.obsDate = obsDate;
    if (((dataFlag = ImportUtils.cleanString(dataFlag)) != null) &&
        (dataFlag.length() > 1)) {
      dataFlag = dataFlag.substring(0, 1);
    }
    this.dataFlag = dataFlag;
  }
  // </editor-fold>
}
