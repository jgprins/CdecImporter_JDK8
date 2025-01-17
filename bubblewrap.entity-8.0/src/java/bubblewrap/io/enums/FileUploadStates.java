/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bubblewrap.io.enums;

/**
 * Enums used during the FileUpload Process
 * @author kprins
 */
public class FileUploadStates {
  
  //<editor-fold defaultstate="collapsed" desc="File Upload Processing States">
  /**
   * State when waiting for the user to select an file
   */
  public final static int BROWSING = 0;
  /**
   * State while processing the uploading of the file
   */
  public final static int UPLOADING = 1;
  /**
   * State when the user selected to pause the File Upload Process
   */
  public final static int PAUSED = 3;
  /**
   * State when the File has been successfully uploaded
   */
  public final static int COMPLETED = 4;
  /**
   * State when the File has been canceled/aborted
   */
  public final static int CANCELED = 8;
  //</editor-fold>
  
}
