package bubblewrap.threads.core;

/**
 * <p>A PostExecuteEventHandler that will fire the Post Execute Event only if the
 * Scheduler's queueCount=0.</p>
 * @author kprins
 */
public class PostExecuteOnEmptyQueueHandler extends PostExecuteEventHandler{
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public PostExecuteOnEmptyQueueHandler() {
    super();        
  }
  // </editor-fold>


  //<editor-fold defaultstate="collapsed" desc="Implements PostExecuteEventHandler">
  /**
   * {@inheritDoc} <p>IMPLEMENT: Return true is the Scheduler is accessible and the
   * Scheduler's queuedCount=0.</p>
   */
  @Override
  public boolean doEvent() {
    ExecProcessScheduler pScheduler = this.getScheduler();
    int iCount = (pScheduler == null)? -1: pScheduler.getQueuedCount();
    return (iCount == 0);
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Does nothing</p>
   */
  @Override
  public void reset() {  }
  //</editor-fold>
  
}
