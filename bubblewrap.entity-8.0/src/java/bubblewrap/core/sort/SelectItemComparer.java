package bubblewrap.core.sort;

import java.util.Comparator;
import javax.faces.model.SelectItem;

/**
 *
 * @author kprins
 */
public class SelectItemComparer implements Comparator<SelectItem> {

  private boolean mbByLabel = false;
  private boolean mbBitValue = false;

  /**
   * Parameterless Constructor if Comparing by Value
   */
  public SelectItemComparer() {}

  /**
   * Constructor with a parameter to set the CompareByLabel flag -
   * the default=false (i.e., campare by Value)
   * @param bByLabel boolean
   */
  public SelectItemComparer(boolean bByLabel) {
    this.mbByLabel = bByLabel;
  }

  /**
   * Set the CompareByLabel flag - the default=false (i.e., compare by Value)
   * @param bByLabel boolean
   */
  public void setCompareByLabel(boolean bByLabel) {
    this.mbByLabel = bByLabel;
  }

  /**
   * Get the Compare by Value Flag
   * @return boolean
   */
  public boolean getCompareByLabel() {
    return this.mbByLabel;
  }

  /**
   * Set the Values-as-Bits flag - the default=false
   * (i.e., compare Value as integers)
   * @param bByLabel boolean
   */
  public void setCompareValuesAsBits(boolean bBitValues) {
    this.mbBitValue = bBitValues;
  }

  /**
   * Get the Compare Values-as-Bits Flag
   * @return boolean
   */
  public boolean getCompareValuesAsBits() {
    return this.mbBitValue;
  }

  /**
   * Compare two SelectItems based on their Values or Labels based on the
   * setting of SortByLabel
   * @param o1 SelectItem
   * @param o2 SelectItem
   * @return int
   */
  @Override
  public int compare(SelectItem o1, SelectItem o2) {
    int iCompare = 0;
    if ((o1 == null) && (o2 == null)) {
      iCompare = 0;
    } else if (o1 == null) {
      iCompare = -1;
    } else if (o2 == null) {
      iCompare = 1;
    } else if (this.mbByLabel) {
      String sLab1 = o1.getLabel();
      String sLab2 = o2.getLabel();
      if ((sLab1 == null) && (sLab2 == null)) {
        iCompare = 0;
      } else if (sLab1 == null) {
        iCompare = -1;
      } else if (sLab2 == null) {
        iCompare = 1;
      } else {
        iCompare = sLab1.compareToIgnoreCase(sLab2);
      }
    } else if (this.mbBitValue) {
      Integer iMax = 0xFFFFFFFF;
      String sMax = Integer.toBinaryString(iMax);
      String sVal = (String) o1.getValue();
      Integer iVal1 = ((sVal == null) || (sVal.trim().isEmpty()))?
                                              iMax: Integer.valueOf(sVal);
      String sVal1 = Integer.toBinaryString(iVal1);

      sVal = (String) o2.getValue();
      Integer iVal2 = ((sVal == null) || (sVal.trim().isEmpty()))?
                                              iMax: Integer.valueOf(sVal);
      String sVal2 = Integer.toBinaryString(iVal2);
      //Integer iVal1 = Integer.getInteger((String) o1.getValue(),null);
      //Integer iVal2 = Integer.getInteger((String) o2.getValue(),null);
      if ((sVal1 == null) && (sVal2 == null)) {
        iCompare = 0;
      } else if (sVal1 == null) {
        iCompare = -1;
      } else if (sVal2 == null) {
        iCompare = 1;
      } else {
        iCompare = sVal1.compareTo(sVal2);
      }
    } else {
      int iMax = 0xFFFFFFFF;
      String sVal = (String) o1.getValue();
      Integer iVal1 = ((sVal == null) || (sVal.trim().isEmpty()))?
                                              iMax: Integer.valueOf(sVal);
      sVal = (String) o2.getValue();
      Integer iVal2 = ((sVal == null) || (sVal.trim().isEmpty()))?
                                              iMax: Integer.valueOf(sVal);
      //Integer iVal1 = Integer.getInteger((String) o1.getValue(),null);
      //Integer iVal2 = Integer.getInteger((String) o2.getValue(),null);
      if ((iVal1 == null) && (iVal2 == null)) {
        iCompare = 0;
      } else if (iVal1 == null) {
        iCompare = -1;
      } else if (iVal2 == null) {
        iCompare = 1;
      } else {
        iCompare = iVal1.compareTo(iVal2);
      }
    }

    return iCompare;
  }
}
