package bubblewrap.core.enums;

/**
 * FacadeFilterEnums is used to set the Facade Filter Options for standard
 * filter filters.
 * @author kprins
 */
public class FacadeFilterEnums {

  //<editor-fold defaultstate="collapsed" desc="Satic Public Enums">
  /**
   * Apply none of the Standard Filters
   */
  public static final int NONE = 0x00;
  /**
   * Return all the enabled entities (i.e., Disabled=false)
   */
  public static final int ENABLEDONLY = 0x01;
  /**
   * Return all the non-system items (i.e., SystemItem=false)
   */
  public static final int NOTSYSTEM = 0x04;
  /**
   * Return only records that match the defined ForeingKeys (if set).
   */
  public static final int FKFILTER = 0x08;
  /**
   * Return only records that match the ENABLEDONLY, NONSYSTEM, and FKFILTER
   * conditions.
   */
  public static final int APPLYALL = (FacadeFilterEnums.ENABLEDONLY |
          FacadeFilterEnums.NOTSYSTEM |
          FacadeFilterEnums.FKFILTER);
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static Public Methods">
  /**
   * Return a flag with Filter Options set according to the bActive, bNotSystem,
   * and bDoFK settings.
   * @param bActive boolean
   * @param bNotSystem boolean
   * @param bDoFK boolean
   * @return int
   */
  public static final int setFilter(boolean bActive, boolean bNotSystem, boolean bDoFK) {
    int eOption = 0;
    eOption = (bActive)? (eOption | FacadeFilterEnums.ENABLEDONLY): eOption;
    eOption = (bNotSystem)? (eOption | FacadeFilterEnums.NOTSYSTEM): eOption;
    eOption = (bDoFK)? (eOption | FacadeFilterEnums.FKFILTER): eOption;
    return eOption;
  }
  //</editor-fold>
}
