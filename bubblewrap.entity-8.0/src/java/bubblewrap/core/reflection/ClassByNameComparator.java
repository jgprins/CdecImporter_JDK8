package bubblewrap.core.reflection;

import java.util.Comparator;

/**
 * A Comparator for sorting classes by in Ascending Order using their simpleNames.
 * @author kprins
 */
public class ClassByNameComparator implements Comparator<Class> {
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public ClassByNameComparator() {
    super(); 
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement Comparator">
  /**
   * {@inheritDoc} <p>OVERRIDE: Return 0 if both classes is unassigned, -1 if pClass1 is
   * unassigned, 1 if pClass2 is unassigned, or return
   * pClass1.simpleName.CompareTo(pClass2.simpleName).</p>
   */
  @Override
  public int compare(Class pClass1, Class pClass2) {
    int iResult = 0;
    if ((pClass1 != null) && (pClass2 != null)) {
      if (pClass1 == null) {
        iResult = -1;
      } else if (pClass2 == null) {
        iResult = 1;
      } else {
        String sName1 = pClass1.getSimpleName();
        String sName2 = pClass2.getSimpleName();
        iResult = sName1.compareTo(sName2);
      }
    }
    return iResult;
  }
  //</editor-fold>  
}
