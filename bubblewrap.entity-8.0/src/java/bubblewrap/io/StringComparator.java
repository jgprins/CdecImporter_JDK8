package bubblewrap.io;

import java.util.Comparator;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng., P.E.; kprins@folsomperio.com;
 */
public class StringComparator implements Comparator<String>{
  
  //<editor-fold defaultstate="collapsed" desc="Private Fields">
  /**
   * A Flag to set if the comparison is case insensitive
   */
  private final Boolean ignoreCase;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  public StringComparator(Boolean ignoreCase) {
    super();
    this.ignoreCase = ((ignoreCase == null) || (!ignoreCase))? null: ignoreCase;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Call the super method before disposing local resources</p>
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
  }  
  //</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparator">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Clean O1 and O2 before comparison. Use case sensitive comparison if
   * this.ignoreCase != true.</p>
   */
  @Override
  public int compare(String o1, String o2) {
    int result = 0;
    if (((o1 = DataEntry.cleanString(o1)) != null) &&
        ((o2 = DataEntry.cleanString(o2)) != null)) {
      if (o1 == null) {
        result = -1;
      } else if (o2 == null) {
        result = 1;
      } else if ((this.ignoreCase != null) && (this.ignoreCase)) {
        result = o1.compareToIgnoreCase(o2);
      } else {
        result = o1.compareTo(o2);
      }
    }
    return result;
  }
//</editor-fold>
}
