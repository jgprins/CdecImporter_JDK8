package bubblewrap.admin.events;

import bubblewrap.core.events.EventArgs;
import bubblewrap.io.DataEntry;

/**
 * An EventArgs used by the {@linkplain LoggedOnEventHandler}. If contains the username
 * of the logged-on user.
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class LoggedOnEventArgs extends EventArgs{
  
  /**
   * The Logged-on User's UserName
   */
  public final String userName;
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public LoggedOnEventArgs(String userName) {
    userName = DataEntry.cleanString(userName);
    if (userName == null) {
      throw new NullPointerException("LoggedOnEventArgs: The Logged On UserName "
              + "cannot be unassigned.");
    }
    this.userName = userName;
  }
  //</editor-fold>
}
