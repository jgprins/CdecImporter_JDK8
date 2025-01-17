package bubblewrap.entity.context;

/**
 * A runtime exception to throw if a specified record is not accessible.
 * @author kprins
 */
public class NoSuchRecordException extends RuntimeException {
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public NoSuchRecordException(String errorMsg) {
    super(errorMsg);
  }
  /**
   * Public Constructor
   */
  public NoSuchRecordException(Throwable throwable) {
    super(throwable);
  }
  /**
   * Public Constructor
   */
  public NoSuchRecordException(String errorMsg, Throwable throwable) {
    super(errorMsg, throwable);
  }
  //</editor-fold>
}
