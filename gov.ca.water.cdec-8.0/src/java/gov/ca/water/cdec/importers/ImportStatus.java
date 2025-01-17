package gov.ca.water.cdec.importers;

/**
 * Enums with the following Download Status values:<ul>
 *  <li><b>{@linkplain #NOTSTARTED}:</b> - (label=Not Started/Stopped) The Import has
       stopped (manually) or not been started.</li>
 *  <li><b>{@linkplain #PENDING}:</b> - (label=Pending) The Import process is 
 *    pending execution.</li>
 *  <li><b>{@link #IMPORTING}:</b> - (label=Importing) The Import is in 
 *    process.</li>
 *  <li><b>{@linkplain #COMPLETED}:</b> - (label=Completed) The Import was 
 *    successfully completed.</li>
 *  <li><b>{@linkplain #NOTFOUND}:</b> - (label=Not Found) No new data have been
 *    found.
 *    </li>
 *  <li><b>{@linkplain #ERROR}:</b> - (label=Import Error) A Import Error was 
 *  reported.
 *    </li>
 * </ul>
 * @author kprins
 */
public enum ImportStatus {
  
  //<editor-fold defaultstate="collapsed" desc="Enum Values">
  /**
   * The Import has Stopped (manually) or not been started. [0]
   */
  NOTSTARTED(0,"Not Started/Stopped"),
  /**
   * The Import Process is pending execution. [1]
   */
  PENDING(1,"Pending"),
  /**
   * The Import is in process. [2]
   */
  IMPORTING(2,"Importing"),
  /**
   * The Import Process was successfully completed. [3]
   */
  COMPLETED(3,"Completed"),
  /**
   * The Import Data could not be found. [4]
   */
  NOTFOUND(4,"Not Found"),
  /**
   * An Import Data error was reported. [4]
   */
  ERROR(5,"Import Error"),
  /**
   * The An Connection Problem occurred and the process will be re-executed. [4]
   */
  RETRY(6,"Retry Process");
//</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Enum Constructor">
  public final int intValue;
  public final String label;
  private ImportStatus(int intValue, String label) {
    this.intValue = intValue;
    this.label = label;
  }
//</editor-fold>
}
