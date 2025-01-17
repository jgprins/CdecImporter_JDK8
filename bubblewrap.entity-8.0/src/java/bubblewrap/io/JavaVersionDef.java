package bubblewrap.io;

/**
 *
 * @author kprins
 */
public class JavaVersionDef extends VersionDef {
  
  /**
   * Parse a Java Version (sVersion) into its four components (Major, Minor, Build, and
   * Update) and initiate a JavaVersionDef instance. The expected version format is: 
   * major.minor.build_update (e.g. "1.6.0_20").
   * @param sVersion the java version string 
   * @return the parse resulting JavaVersionDef
   */
  public static JavaVersionDef javaParse(String sVersion) {
    JavaVersionDef pResult = null;
    sVersion = DataEntry.cleanString(sVersion);
    if (sVersion != null) {
      String sBaseVersion = null;
      String sUpdateVersion = null;
      
      int iPos = sVersion.indexOf("_");
      if (iPos > 0) {
        sBaseVersion = DataEntry.cleanString(sVersion.substring(0, iPos));
        if (iPos < (sVersion.length()-1)) {
          sUpdateVersion = DataEntry.cleanString(sVersion.substring(iPos+1));
        }
      }
      VersionDef pVersion = 
                          (sBaseVersion == null)? null: VersionDef.Parse(sBaseVersion);
      if (pVersion != null) {
        int iMajor = pVersion.getMajor();
        int iMinor = pVersion.getMinor();
        int iBuild = pVersion.getBuild();
        int iUpdate = 0;
        if (sUpdateVersion != null) {
          iUpdate = Integer.parseInt(sUpdateVersion);
        }
        
        pResult = new JavaVersionDef(iMajor, iMinor, iBuild, iUpdate);
      }
    }
    return pResult;
  }
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Field">
  /**
   * Maximum Update Increment
   */
  public static final int UPDATEMAX = 99;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="private Fields">
  /**
   * Placeholder for the Version's Update increment
   */
  private Integer miUpdate;
  private Integer miUpdateDigits;
  //</editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public JavaVersionDef() {
    super();  
    this.miUpdate = 0;
    this.miUpdateDigits = 2;
  }
  
  /**
   * Public Constructor with a Major, Minor, Built and Update Digit
   */
  public JavaVersionDef(int iMajor, int iMinor, int iBuilt, int iUpdate) {
    super(iMajor, iMinor, iBuilt);
    this.miUpdate = (iUpdate < 0)? 0: iUpdate;
  }
  
  /**
   * Set the Version Format
   * @param iMajorDigits the number of major version digits
   * @param iMinorDigits the number of minor version digits
   * @param iBuildDigits the number of build version digits
   * @param iUpdateDigit the number of update version digits
   */
  public void setFormat(int iMajorDigits, int iMinorDigits, int iBuildDigits, 
                                                            int iUpdateDigit) {
    super.setFormat(iMajorDigits, iMinorDigits, iBuildDigits);
    this.miUpdateDigits = (iUpdateDigit < 0)? null: iUpdateDigit;
  }
  // </editor-fold>

  /**
   * Get the Version Update
   * @return the currently assigned update
   */
  public int getUpdate() {
    return this.miUpdate;
  }
  
  /**
   * Set the Version Update
   * @param iUpdate the new update 
   */
  public void setUpdate(int iUpdate) {
    this.miUpdate = (iUpdate < 0)? 0: iUpdate;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Reset the update Version</p>
   */
  @Override
  public void incMajor() {
    super.incMajor();
    this.miUpdate = 0;
  }

  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Reset the update Version</p>
   */
  @Override
  public void incMinor() {
    super.incMinor();
    this.miUpdate = 0;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Reset the update Version</p>
   */
  @Override
  public void incBuild() {
    super.incBuild();
    this.miUpdate = 0;
  }
  
  /**
   * Increment the Update sub-version This process follows the following rules:
   * 1) If (this.major = 0), increment only the this.major (i.e., version=01.00.0000)
   * 2) If (this.update less than UPDATEMAX), increment this.build.
   * 3) If (this.update = UPDATEMAX), increment this.build and reset this.update to zero.
   */
  public void incUpdate() {
    if (this.getMajor() == 0) {
      this.incMajor();
    } else  if (this.miUpdate < JavaVersionDef.UPDATEMAX) {
      this.miUpdate++;
    } else {
      this.incBuild();
    }
  }


  //<editor-fold defaultstate="collapsed" desc="VersionDef Overrides">
  /**
   * Parse sVersion as a JavaVersioNdef and call this.isNewerVersion passing the 
   * parsed version
   * @param sVersion the version to compare
   * @return true is sVersion represents an earlier version
   */
  @Override
  public boolean isNewerVersion(String sVersion) {
    JavaVersionDef pVersion = JavaVersionDef.javaParse(sVersion);
    return this.isNewerVersion(pVersion);
  }
  
  /**
   * If pVersion!=null and a JavaVersionDef, compare the individual components and to
   * see if pVersion represents an earlier version
   * @param pVersion the JavaVersioNDef to compare
   * @return true is pVersion represents an earlier version
   */
  @Override
  public boolean isNewerVersion(VersionDef pVersion) {
    boolean bResult = ((pVersion != null) && (pVersion instanceof JavaVersionDef) 
                       && super.isNewerVersion(pVersion));
    if (bResult) {
      JavaVersionDef pJavaVs = (JavaVersionDef) pVersion;
      bResult = (this.miUpdate > pJavaVs.miUpdate);
    }
    return bResult;
  }
  
  /**
   * Parse sVersion as a JavaVersioNdef and call this.isOdlerVersion passing the 
   * parsed version
   * @param sVersion the version to compare
   * @return true is sVersion represents a later version
   */
  @Override
  public boolean isOlderVersion(String sVersion) {
    JavaVersionDef pVersion = JavaVersionDef.javaParse(sVersion);
    return this.isOlderVersion(pVersion);
  }
  
  /**
   * If pVersion!=null and a JavaVersionDef, compare the individual components and to
   * see if pVersion represents a later version
   * @param pVersion the JavaVersioNDef to compare
   * @return true is pVersion represents a later version
   */
  @Override
  public boolean isOlderVersion(VersionDef pVersion) {
    boolean bResult = ((pVersion != null) && (pVersion instanceof JavaVersionDef) 
                       && super.isOlderVersion(pVersion));
    if (bResult) {
      JavaVersionDef pJavaVs = (JavaVersionDef) pVersion;
      bResult = (this.miUpdate < pJavaVs.miUpdate);
    }
    return bResult;
  }

  /**
   * OVERRIDE: return the combined hash code of the versions
   * @return int
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + this.getMajor();
    hash = 59 * hash + this.getMinor();
    hash = 59 * hash + this.getBuild();
    hash = 59 * hash + this.getUpdate();
    return hash;
  }

  /**
   * Compare obj to this JavaVersionDef
   * @param obj the object to compare
   * @return true if (obj!=null), (object instance JavaVersionDef), and all version 
   * properties match.
   */
  @Override
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  public boolean equals(Object obj) {
    boolean bResult = ((obj != null) &&
                       ((obj instanceof JavaVersionDef) || (obj instanceof String)));
    if ((bResult) && (obj != this)) {
      JavaVersionDef pVersion = null;
      if (obj instanceof String) {
        pVersion = JavaVersionDef.javaParse((String) obj);
      } else if (obj instanceof JavaVersionDef) {
        pVersion = (JavaVersionDef) obj;
      }

      if (pVersion != null) {
        bResult = ((this.getMajor() == pVersion.getMajor())
                && (this.getMinor() == pVersion.getMinor())
                && (this.getBuild() == pVersion.getBuild())
                && (this.getUpdate() == pVersion.getUpdate()));
      }
    }
    return bResult;
  }

  /**
   * Compare the two versions by sub-versions. If this version is: equal to pObj it will
   * return 0; less than the pObj (earlier version) it will return 1; or visa versa
   * (i.e., for sorting in a ascending order (earlier to later)).
   * @param pObj VersionDef
   * @return 0=equal;-1=this instance is the newer version; 1=this instance is the 
   * older version. -1 if pObj=null or major,minor, and build is equal and pObj is not
   * a JavaVersiondef.
   */
  @Override
  public int compareTo(VersionDef pObj) {
    int iCompare = (pObj == null) ? -1 : 0;
    if (iCompare == 0) {
      iCompare = super.compareTo(pObj);
    }
    if (iCompare == 0) {
      if (pObj instanceof JavaVersionDef) {
        JavaVersionDef pJavaVs = (JavaVersionDef) pObj;
        iCompare = (this.miUpdate == pJavaVs.miUpdate) ? 0 :
          ((this.miUpdate > pJavaVs.miUpdate) ? -1 : 1);
      } else {
        iCompare = -1;
      }
    }    
    return iCompare;
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: If (doAddBuild & doAddMinor & (this.updateDigits != null) call
   * addUpdateVersion to add the Update Digits</p>
   * @return the default version as a formatted string.
   */
  @Override
  public String toString() {
    String sResult = this.toString();
    if ((this.doAddBuild()) && (this.doAddMinor())) {
      sResult = this.addUpdateVersion(sResult, (this.miUpdateDigits != null));
    }
    return sResult;
  }
  
  /**
   * Convert the version to a formatted string using the format settings and adding the
   * sub-versions as specified by bAddMinor, bAddBuild, and bAddUpdate. Call the
   * super.toString(bAddMinor, bAddBuild) before calling addUpdateVersion if
   * bAddUpdate=true.
   * @param bAddMinor add the minor version
   * @param bAddBuild add the build version
   * @param bAddUpdate add the update version
   * @return the formatted version string
   */
  public String toString(boolean bAddMinor, boolean bAddBuild, boolean bAddUpdate) {
    String sResult = this.toString(bAddMinor, bAddBuild);
    if ((bAddBuild) && (bAddBuild) && (bAddUpdate)) {
      sResult = this.addUpdateVersion(sResult, bAddUpdate);
    }
    return sResult;
  }
  
  /**
   * Add the update version according to the format settings if bAddUpdate=true.
   * @param sVersion the base formatted string
   * @param bAddUpdate true to add the update version
   * @return the formatted string.
   */
  private String addUpdateVersion(String sVersion, boolean bAddUpdate) {
    String sResult = sVersion;
    if (bAddUpdate) {
      String sFormat = ((this.miUpdateDigits == null) || (this.miUpdateDigits == 0))?
              "_%1$d": ("_%1$0" + this.miUpdateDigits + "d");
      sResult += String.format(sFormat, this.miUpdate);
    }
    return sResult;
  }
  //</editor-fold> 
}
