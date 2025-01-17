package bubblewrap.io;

import java.io.Serializable;

/**
 * The VersionDef class maintains a version specification by three version numbers:
 * a Major (0..99), Minor(0..99), and Build (0..9999)version number.
 * The class toString method created a formatted format string (e.g., "02.01.0012") and
 * the classes static Parse method can be used to convert a formatted version string
 * to a VersionDef instance. The class implements the Comparable interface, which can
 * be used for compare two VersionDefs to determine which is the latest version.
 * @author kprins
 */
public class VersionDef implements Serializable, Comparable<VersionDef> {

  // <editor-fold defaultstate="collapsed" desc="Static Fields/Methods">
  public static final int MAJORMAX = 99;
  public static final int MINORMAX = 99;
  public static final int BUILDMAX = 9999;

  /**
   * Parsing sVersion (assumed to be in the format MM.mm.dddd, where M, m, b
   * represents the Major, Minor, and Build versions, respectively) and return
   * in VersionDef instance.  If an error occur or the subversion is undefined, the sub
   * version will beset to 0.  Version=0.0.0 will be returned if sVersion=null|""
   * @param sVersion String
   * @return VersionDef
   */
  public static VersionDef Parse(String sVersion) {
    VersionDef pVersion = null;
    int iMajor = 0;
    int iMinor = 0;
    int iBuild = 0;

    sVersion = DataEntry.cleanString(sVersion);
    if (sVersion != null) {
      int iLevel = 0;
      int iStart = 0;
      int iPos = sVersion.indexOf(".");
      int iVal = 0;
      String sSub = null;
      while (iStart >= 0) {
        sSub = (iPos > iStart) ? sVersion.substring(iStart, iPos)
                : sVersion.substring(iStart);
        sSub = DataEntry.cleanString(sSub);
        iVal = 0;
        if (sSub != null) {
          try {
            iVal = Integer.parseInt(sSub);
          } catch (Exception e) {
            iVal = 0;
          }
        }
        if (iLevel == 0) {
          iMajor = iVal;
        } else if (iLevel == 1) {
          iMinor = iVal;
        } else if (iLevel == 2) {
          iBuild = iVal;
        } else {
          break;
        }
        iLevel++;
        iStart = (iPos < 0) ? iPos : iPos + 1;
        iPos = (iStart < 0) ? iStart : sVersion.indexOf(".", iStart);
      }
    }

    pVersion = new VersionDef(iMajor, iMinor, iBuild);
    return pVersion;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private Integer miMajor;
  private Integer miMinor;
  private Integer miBuild;
  private Integer miMajorDigits;
  private Integer miMinorDigits;
  private Integer miBuildDigits;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Parameterless constructor - Version=0.0.0
   */
  public VersionDef() {
    this.miMajor = 0;
    this.miMinor = 0;
    this.miBuild = 0;
    this.miMajorDigits = 2;
    this.miMinorDigits = 2;
    this.miBuildDigits = 4;
  }

  /**
   * Constructor with specified versions. Valid Ranges are iMajor = [0..MAJORMAX],
   * iMinor = [0..MINORMAX], and iBuild = [0..BUILDMAX]. A NullPointerException is
   * thrown if iMinor and/or iBuild > 0 and iMajor less or equal to 0.
   * Negative values are set to zero and values are capped to the version limits.
   * @param iMajor int
   * @param iMinor int
   * @param iBuild int
   */
  public VersionDef(int iMajor, int iMinor, int iBuild) {
    this();
    this.miMajor = (iMajor <= 0) ? 0
            : ((iMajor > VersionDef.MAJORMAX) ? VersionDef.MAJORMAX : iMajor);
    iMinor = (iMinor <= 0) ? 0
            : ((iMinor > VersionDef.MINORMAX) ? VersionDef.MINORMAX : iMinor);
    iBuild = (iBuild <= 0) ? 0
            : ((iBuild > VersionDef.BUILDMAX) ? VersionDef.BUILDMAX : iBuild);
    if (((this.miMajor == 0) && ((iMinor > 0) || (iBuild >0)))) {
      throw new NullPointerException("The Major Version is undefined. " +
         "The Minor or Build Versions cannot be set without setting the Major Version");
    }
    this.miMinor = iMinor;
    this.miBuild = iBuild;
  }
  
  /**
   * Set the number of fix digits for each version category. If the version is less
   * that these specified number of digits, it will be '0' padded. The default format
   * if MajorDigits=2,  MinorDigits=2, and  BuildDigits=4. If the iMinorDigits or 
   * iBuildDigits is less than zero the digits will by default be hidden. If set
   * @param iMajorDigits the number of major version digits
   * @param iMinorDigits the number of minor version digits
   * @param iBuildDigits the number of build version digits
   */
  public void setFormat(int iMajorDigits, int iMinorDigits, int iBuildDigits) {
    this.miMajorDigits = (iMajorDigits <= 0)? 0: iMajorDigits;
    this.miMinorDigits = (iMinorDigits < 0)? null: iMinorDigits;
    this.miBuildDigits = (iBuildDigits < 0)? null: iBuildDigits;
  }      
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public methods">
  /**
   * Get the Major sub-version
   * @return int
   */
  public int getMajor() {
    return miMajor;
  }

  /**
   * Set the Major Sub-versions Range[1..MAJORMAX]. Throws a NullPointerException if
   * iMajor is zero or negative.
   * @param iMajor
   */
  public void setMajor(int iMajor) {
    if (iMajor <= 0) {
      throw new NullPointerException("The Major version cannot be set to zero or a " +
              "negative number");
    }
    this.miMajor = (iMajor > VersionDef.MAJORMAX) ? VersionDef.MAJORMAX : iMajor;
  }

  /**
   * Get the Minor sub-version
   * @return int
   */
  public int getMinor() {
    return miMinor;
  }

  /**
   * Set the Minor Sub-versions Range[0..MINORMAX]. It throws a NullPointerException if
   * the iMinor > 0 and the Major Version = 0.
   * @param iMinor int
   */
  public void setMinor(int iMinor) {
    iMinor = (iMinor <= 0) ? 0
            : ((iMinor > VersionDef.MINORMAX) ? VersionDef.MINORMAX : iMinor);
    if ((this.miMajor == 0) && (iMinor > 0)) {
      throw new NullPointerException("The Major Version is undefined. " +
         "The Minor Version cannot be set without setting the Major Version");
    }
    this.miMinor = iMinor;
  }

  /**
   * Get the Build sub-version
   * @return int
   */
  public int getBuild() {
    return miBuild;
  }

  /**
   * Set the Build Sub-versions Range[0..BUILDMAX]. It throws a NullPointerException if
   * the iBuild > 0 and the Major Version = 0.
   * @param iBuild int
   */
  public void setBuild(int iBuild) {
    iBuild = (iBuild <= 0) ? 0
            : ((iBuild > VersionDef.BUILDMAX) ? VersionDef.BUILDMAX : iBuild);
    if ((this.miMajor == 0) && (iBuild > 0)) {
      throw new NullPointerException("The Major Version is undefined. " +
         "The Build Version cannot be set without setting the Major Version");
    }
    this.miBuild = iBuild;
  }

  /**
   * Increment the Major Version (up to MAJORMAX) and reset both this.minor and
   * this.build to zero.
   */
  public void incMajor() {
    if (this.miMajor < VersionDef.MAJORMAX) {
      this.miMajor++;
      this.miMinor = 0;
      this.miBuild = 0;
    }
  }

  /**
   * Increment the Minor sub-version. This process follows the following rules:
   * 1) If (this.major = 0), increment only the this.major (i.e., version=01.00.0000)
   * 2) If (this.minor less than MINORMAX), increment this.minor and set this.build=0.
   * 3) If (this.minor = MINORMAX), increment this.major and reset this.major and
   *    this.build to zero.
   */
  public void incMinor() {
    if (this.miMajor == 0) {
      this.incMajor();
    } else {
      if (this.miMinor < VersionDef.MINORMAX) {
        this.miMinor++;
        this.miBuild = 0;
      } else {
        this.incMajor();
      }
    }
  }

  /**
   * Increment the Build sub-version This process follows the following rules:
   * 1) If (this.major = 0), increment only the this.major (i.e., version=01.00.0000)
   * 2) If (this.build less than BUILDMAX), increment this.build.
   * 3) If (this.build = BUILDMAX), increment this.minor and reset this.build to zero.
   */
  public void incBuild() {
    if (this.miMajor == 0) {
      this.incMajor();
    } else  if (this.miBuild < VersionDef.BUILDMAX) {
      this.miBuild++;
    } else {
      this.incMinor();
    }
  }

  /**
   * Return true if sVersion (after parsing) is an earlier version than this version
   * @param sVersion String
   * @return boolean
   */
  public boolean isNewerVersion(String sVersion) {
    VersionDef pObj = VersionDef.Parse(sVersion);
    return (this.compareTo(pObj) < 0);
  }

  /**
   * Return true if pObj is an earlier version than this version.
   * Return true if (pObj=null).
   * @param pObj VersionDef
   * @return boolean
   */
  public boolean isNewerVersion(VersionDef pObj) {
    return (pObj == null) || (this.compareTo(pObj) < 0);
  }

  /**
   * Return true if sVersion (after parsing) is an earlier version than this version
   * @param sVersion String
   * @return boolean
   */
  public boolean isOlderVersion(String sVersion) {
    VersionDef pObj = VersionDef.Parse(sVersion);
    return ((pObj != null) && (this.compareTo(pObj) > 0));
  }

  /**
   * Return true if pObj is an earlier version than this version.
   * Return true if (pObj=null).
   * @param pObj VersionDef
   * @return boolean
   */
  public boolean isOlderVersion(VersionDef pObj) {
    return ((pObj == null) || (this.compareTo(pObj) > 0));
  }

  /**
   * Return true if this instance Major version number is not set.
   * @return boolean
   */
  public boolean isEmpty() {
    return (this.miMajor == 0);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Protected Methods">
  /**
   * Check if the Minor Digit should be added to output string
   * @return (this.miMinorDigits != null)
   */
  protected boolean doAddMinor() {
    return (this.miMinorDigits != null);
  }
  
  /**
   * Check if the Minor Digit should be added to output string
   * @return (this.miMinorDigits != null)
   */
  protected boolean doAddBuild() {
    return (this.miBuildDigits != null);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Object Overrides">
  /**
   * Return a clone of this VersionDef instance
   * @return VersionDef
   * @throws CloneNotSupportedException
   */
  @Override
  protected VersionDef clone() throws CloneNotSupportedException {
    return new VersionDef(this.miMajor,this.miMinor,this.miBuild);
  }

  @Override
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  public boolean equals(Object obj) {
    boolean bResult = ((obj != null) &&
                       ((obj instanceof VersionDef) || (obj instanceof String)));
    if ((bResult) && (obj != this)) {
      VersionDef pVersion = null;
      if (obj instanceof String) {
        pVersion = VersionDef.Parse((String) obj);
      } else if (obj instanceof VersionDef) {
        pVersion = (VersionDef) obj;
      }

      if (pVersion != null) {
        bResult = ((this.miMajor == pVersion.getMajor())
                && (this.miMinor == pVersion.getMinor())
                && (this.miBuild == pVersion.getBuild()));
      }
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
    hash = 59 * hash + this.miMajor;
    hash = 59 * hash + this.miMinor;
    hash = 59 * hash + this.miBuild;
    return hash;
  }

  /**
   * Return the String value for the version in the format MM.mm.bbbb, where M, m, b
   * represents the Major, Minor, and Build versions respectively.
   * @return String
   */
  @Override
  public String toString() {
    return this.toString((this.miMinorDigits != null), (this.miBuildDigits != null));
  }
  
  /**
   * Return the String value for the version in the format M.m.b, where M, m, b
   * represents the Major, Minor, and Build versions, respectively. Each version's 
   * number of digits will depend on the format settings (see setFormat). if 
   * (!bAddBuild), the Build version will be dropped, if (!bAddMinor) only the major 
   * version will be returned.
   * @see #setFormat(int, int, int) 
   * @param bAddMinor add the minor version
   * @param bAddBuild add the build version
   * @return the formatted version string
   */
  public String toString(boolean bAddMinor, boolean bAddBuild) {
    String sResult = null;
    String sFormat = ((this.miMajorDigits == null) || (this.miMajorDigits == 0))? 
                              "%1$d": ("%1$0" + this.miMajorDigits + "d");
    if (bAddMinor) {
      sFormat += ".";
      sFormat += ((this.miMinorDigits == null) || (this.miMinorDigits == 0))? 
                              "%2$d": ("%2$0" + this.miMinorDigits + "d");
      if (bAddBuild) {
        sFormat += ".";
        sFormat += ((this.miBuildDigits == null) || (this.miBuildDigits == 0))? 
                                "%3$d": ("%3$0" + this.miBuildDigits + "d");
      }      
    }
//    String sResult = DataEntry.leftPadString(Integer.toString(this.miMajor),"0",2);
//    if (bAddMinor) {
//      sResult += "." + DataEntry.leftPadString(Integer.toString(this.miMinor),"0",2);
//      if (bAddBuild) {
//        sResult += "." + DataEntry.leftPadString(Integer.toString(this.miBuild),"0",4);
//      }
//    }
    sResult = String.format(sFormat, 
                new Object[]{this.miMajor,this.miMinor,this.miBuild});
    return (sResult == null)? "": sResult;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Implement Comparable">
  /**
   * Compare the two versions by sub-versions. If this version is: equal to pObj it will
   * return 0; less than the pObj (earlier version) it will return 1; or visa versa
   * (i.e., for sorting in a ascending order (earlier to later)).
   * @param pObj VersionDef
   * @return int
   */
  @Override
  public int compareTo(VersionDef pObj) {
    int iCompare = (pObj == null) ? 1 : 0;
    if (iCompare == 0) {
      iCompare = (this.getMajor() == pObj.getMajor()) ? 0 :
        ((this.getMajor() > pObj.getMajor()) ? -1 : 1);
    }
    if (iCompare == 0) {
      iCompare = (this.getMinor() == pObj.getMinor()) ? 0 :
        ((this.getMinor() > pObj.getMinor()) ? -1 : 1);
    }
    if (iCompare == 0) {
      iCompare = (this.getBuild() == pObj.getBuild()) ? 0 :
        ((this.getBuild() > pObj.getBuild()) ? -1 : 1);
    }
    return iCompare;
  }
  // </editor-fold>
}
