package bubblewrap.threads.core;

/**
 * An Abstract class for scheduling PostExecute events used by the owner
 * ExecProcessScheduler.
 * @author kprins
 */
public abstract class PostExecuteEventHandler {
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * Placeholder for the reference to the owner Scheduler
   */
  private ExecProcessScheduler scheduler;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public PostExecuteEventHandler() {
    super();    
    this.scheduler = null;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Method">
  /**
   * Assign the Scheduler reference to the PostExecuteEventHandler
   * @param scheduler the owner Scheduler
   */
  public final void setScheduler(ExecProcessScheduler scheduler) {
    this.scheduler = scheduler;
  }
  
  /**
   * Get the owner Scheduler reference
   * @return the assigned reference or null if not assigned.
   */
  public ExecProcessScheduler getScheduler() {
    return this.scheduler;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Call to check if the Event should be fired.
   * @return boolean
   */
  public abstract boolean doEvent();
  
  /**
   * ABSTRACT: Call to reset the EventHandler after an event has been fired.
   */
  public abstract void reset();
  //</editor-fold>
}
