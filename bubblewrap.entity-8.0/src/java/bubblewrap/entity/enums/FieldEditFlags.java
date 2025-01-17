package bubblewrap.entity.enums;

import bubblewrap.io.DataEntry;
import java.util.EnumSet;

/**
 * <p>The FieldEditFlags enums support the following bitmap options:</p><ul>
 * <li><b>{@linkplain #NoConstaints}:</b> - No Field edit constraints apply (default)
 * </li>
 * <li><b>{@linkplain #ReadOnly}:</b> - No Editing is allowed</li>
 * <li><b>{@linkplain #NoIsSystem}:</b> - No Editing is allowed when the record is a 
 * system item</li>
 * <li><b>{@linkplain #AdminOnly}:</b> - No Editing is allowed unless the logged-in 
 * user is the system administrator</li>
 * </ul>
 * <p><b>Note:</b>All content by default has read access. The ReadOnly access is only
 * applicable if none of the constraints other flags are set.</p>
 * @author kprins
 */
public enum FieldEditFlags {
  
  //<editor-fold defaultstate="collapsed" desc="Public Enum Values">
  /**
   * No Field edit constraints apply (default) [{@value }]
   */
  NoConstaints  (0x0000,"No Field edit constraints apply"),
  /**
   * Read Only field - no Editing is allowed [{@value }]
   */
  ReadOnly     (0x0001,"No Editing is allowed"),
  /**
   * No Editing is allowed when the record is a system item [{@value }]
   */
  NoIsSystem  (0x0002,"No Editing is allowed when the record is a system item"),
  /**
   * No Editing is allowed unless the logged-in user is the system administrator 
   * [{@value }]
   */
  AdminOnly    (0x0004,"No Editing is allowed unless the logged-in user is the system "
          + "administrator");
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Enum Class Definition">
  
  //<editor-fold defaultstate="collapsed" desc="Public final Fields">
  /**
   * The enum's label
   */
  public final String label;
  /**
   * the enum's flag value
   */
  public final int flagValue;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructors">
  /**
   * Public Constructor
   */
  private FieldEditFlags(int flag, String label) {
    this.flagValue = flag;
    this.label = DataEntry.cleanString(label);
  }
  //</editor-fold>
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Methods">  
  /**
   * Convert the specified flagValue mask to a EnumSet of EntityAccessFlags. If 
   * (flagMask=0), return EnumSet{NoConstaints}, if FieldEditFlags.isReadOnly(flagMask)
   * return EnumSet{ReadOnly}, else return an EnumSet with all supported
   * access flags. 
   * @param flagMask the specified flagValue mask
   * @return a EnumSet of FieldEditFlags supported by the flagMask
   */
  public static EnumSet<FieldEditFlags> getEnumSet(int flagMask) {
    EnumSet<FieldEditFlags> result = EnumSet.noneOf(FieldEditFlags.class);
    if (FieldEditFlags.isNoConstraints(flagMask)) {
      result.add(FieldEditFlags.NoConstaints);
    } else if (FieldEditFlags.isReadOnly(flagMask)) {
      result.add(FieldEditFlags.ReadOnly);
    } else {
      if (FieldEditFlags.getNoSystem(flagMask)) {
        result.add(FieldEditFlags.NoIsSystem);
      }
      if (FieldEditFlags.getAdminOnly(flagMask)) {
        result.add(FieldEditFlags.AdminOnly);
      }
    }    
    return result;
  }
  
  /**
   * Get a EntityAccess Flag that encapsulate the specified settings
   * @param allowEdit true to allow editing
   * @param allowAdd true to allow adding of new records
   * @param allowDelete true to allow deleting of new records
   * @return the accessFlag
   */
  public static int getFlagMask(FieldEditFlags...enums) {
    int result = 0;
    if ((enums != null) && (enums.length > 0)) {
      for (FieldEditFlags enumVal : enums) {
        result = (result | enumVal.flagValue);
      }
    }
    return result;
  }
  
  /**
   * Get a EntityAccess Flag that encapsulate the specified settings
   * @param readOnly true to set readOnly (exclusive)
   * @param noIsSystem true to deny editing if the record is a system item
   * @param adminOnly true to allow only be the System Administrator
   * @return a bitmap integer
   */
  public static int getFlagMask(boolean readOnly, boolean noIsSystem,
          boolean adminOnly) {
    int result = 0;
    if (readOnly) {
      result = FieldEditFlags.ReadOnly.flagValue;
    } else {
      if (noIsSystem) {
        result = (result | FieldEditFlags.NoIsSystem.flagValue);
      }
      if (adminOnly) {
        result = (result | FieldEditFlags.AdminOnly.flagValue);
      }
    }
    return result;
  }
  
  /**
   * Check if the flagMask allows only NoConstraints access
   * @param flagMask the access flag to evaluate
   * @return true if full edit access is allowed (default).
   */
  public static boolean isNoConstraints(int flagMask) {
    return (flagMask == FieldEditFlags.NoConstaints.flagValue);
  }
  
  /**
   * Check if the flagMask allows only ReadOnly access
   * @param flagMask the access flag to evaluate
   * @return true if editing is allowed.
   */
  public static boolean isReadOnly(int flagMask) {
    return ((flagMask & FieldEditFlags.ReadOnly.flagValue) == 
              FieldEditFlags.ReadOnly.flagValue);
  }

  /**
   * Get whether the flagMask allows record editing.
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow editing.
   */
  public static boolean getNoSystem(int flagMask) {
    return ((flagMask & FieldEditFlags.NoIsSystem.flagValue) == 
              FieldEditFlags.NoIsSystem.flagValue);
  }

  /**
   * Get whether the flagMask allows adding of new records.
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow addition.
   */
  public static boolean getAdminOnly(int flagMask) {
    return ((flagMask & FieldEditFlags.AdminOnly.flagValue) == 
            FieldEditFlags.AdminOnly.flagValue);
  }
  //</editor-fold>
}
