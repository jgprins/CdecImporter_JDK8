package bubblewrap.navigation.core;

import bubblewrap.navigation.enums.AppActions;
import java.util.Comparator;

/**
 * Sort the NavigationTargets by AppTask-SubmitTask-Target-Action
 * @author kprins
 */
public class NavigationTargetComparator implements Comparator<NavigationTarget> {

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor  
   */
  public NavigationTargetComparator() {
    super();        
  }
  // </editor-fold>

  /**
   * Compare the properties of pTrg1 and pTrgs2 and return -1 if pTrg1 > pTrg2;
   * 1 if pTrg1 < pTrg2 and 0 if they are equal.
   * @param pTrg1 NavigationTarget
   * @param pTrg2 NavigationTarget
   * @return int
   */
  @Override
  public int compare(NavigationTarget pTrg1, NavigationTarget pTrg2) {
    int eResult = 0;
    if ((pTrg1 != null) && (pTrg2 != null)) {
      if (pTrg1 == null) {
        eResult = -1;
      } else if (pTrg2 == null) {
        eResult = 1;
      } else {
        if (eResult == 0) {
          eResult = pTrg1.getTarget().compareTo(pTrg2.getTarget());
        }        
        if (eResult == 0) {
          eResult = pTrg1.getAppTask().compareTo(pTrg2.getAppTask());
        }
        if (eResult == 0) {
          eResult = (pTrg1.getAction().compareTo(pTrg2.getAction()));
        }        
        if (eResult == 0) {
          eResult = this.compare(pTrg1.getSubTask(), pTrg2.getSubTask());
        }
      }
    }
    return eResult;
  }
  
  /**
   * Call to compare the two String.
   * @param sStr1 String
   * @param sStr2 String
   * @return int
   */
  private int compare(String sStr1, String sStr2) {
    int iResult = 0;
    if ((sStr1 != null) && (sStr2 != null)) {
      if (sStr1 == null) {
        iResult = -1;
      } else if (sStr2 == null) {
        iResult = 1;
      } else {
        iResult = sStr1.compareToIgnoreCase(sStr2);
      }
    }
    return iResult;
  }
 
  /**
   * Call to compare the two String.
   * @param iVal1 int
   * @param iVal2 int
   * @return int
   */
  private int compare(int iVal1, int iVal2) {
    int iResult = 0;
    if (iVal1 > iVal2) {
      iResult = -1;
    } else if (iVal1 < iVal2) {
      iResult = 1;
    }
    return iResult;
  }
  
}
