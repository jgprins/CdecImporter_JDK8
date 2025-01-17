package bubblewrap.io.enums;

/**
 * These enums are used by FileUploadingArgs and FileUploadedArgs to communicate the
 * FileUpload Action to the IDataSourceOwner and FileResource classes the upload action
 * when files are uploaded in chunks.
 * @author kprins
 */
public class FileUploadActions {
  
  /**
   * The default for uploading file in one piece.
   */
  public static final int NoChunks = 0;
  /**
   * With uploading the first chuck - to prepare for uploading the chunks
   */
  public static final int StartChunks = 1;
  /**
   * With the uploading of each chunk
   */
  public static final int AddChunk = 2;
  /**
   * With the uploading of last chunk and to the merged into one file.
   */
  public static final int EndChunks = 3;
  /**
   * With canceling the chucked upload process.
   */
  public static final int CancelChunks = 4;
}
