package bubblewrap.threads.core;

/**
 * <p>A PostExecuteEventHandler that will fire the Post Execute Event only if the
 * Scheduler's queueCount=0.</p>
 * @author kprins
 */
public class PostExecuteOnCountHandler extends PostExecuteEventHandler{
  
  /**
   * Placeholder for the Maximum Count before triggering the Event
   * (Default = -1) - no limit
   */
  private int miMaxCount = -1;
  /**
   * Placeholder for the call counter.
   */
  private int miCount = 0;
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor with a MaxCount limit
   * @param iMaxCount the Call Counter limit that will trigger the event. 0 or negative
   * values indicate no limit - the event is never triggered.
   */
  public PostExecuteOnCountHandler(int iMaxCount) {
    super();        
    this.miMaxCount = iMaxCount;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implements PostExecuteEventHandler">
  /**
   * {@inheritDoc} <p>IMPLEMENT: It increments the Call Counter with every call. It
   * returns true if (this.maxCount>0 and the call count is equal to or greater than
   * this.maxCount.</p>
   */
  @Override
  public boolean doEvent() {
    this.miCount++;
    return ((this.miMaxCount > 0) && (this.miCount >= this.miMaxCount));
  }
  
  /**
   * {@inheritDoc} <p>IMPLEMENT: Does nothing</p>
   */
  @Override
  public void reset() { 
    this.miCount = 0;
  }
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Override Object">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return "MaxCount=?; CallCount=?"</p>
   */
  @Override
  public String toString() {
    return String.format("MaxCount=%d; CallCount=%d",
            new Object[]{this.miMaxCount,this.miCount});
  }
  //</editor-fold>
}
