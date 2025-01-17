package bubblewrap.entity.enums;

import bubblewrap.io.DataEntry;
import java.util.EnumSet;

/**
 * <p>The AccessFlags enums support the following bitmap options:</p><ul>
 * <li><b>{@linkplain #ReadOnly}:</b> - Readonly access (default)</li>
 * <li><b>{@linkplain #AllowEdits}:</b> - Allow Editing of the Entity Values</li>
 * <li><b>{@linkplain #AllowAdd}:</b> - Allow adding new entity records</li>
 * <li><b>{@linkplain #AllowDelete}:</b> - Allow deleting of existing records</li>
 * </ul>
 * <p><b>Note:</b>All content by default has read access. The ReadOnly access is only
 * applicable if none of the other flags are set.</p>
 * @author kprins
 */
public enum AccessFlags {
  
  //<editor-fold defaultstate="collapsed" desc="Public Enum Values">
  /**
   * Only read only access [{@value }]
   */
  ReadOnly    (0x0000,"Read Only Access"),
  /**
   * Only allow editing of existing record [{@value }]
   */
  AllowEdits   (0x0001,"Allow Editing of Records"),
  /**
   * Only allow adding of new record [{@value }]
   */
  AllowAdd    (0x0002,"Allow Adding of New Records"),
  /**
   * Only allow deleting of existing record [{@value }]
   */
  AllowDelete (0x0004,"Allow Deleting of Records"),
  /**
   * Allow access to add, edits, and delete access [{@value }]
   */
  FullAccess    (0x0007,"Allow Full Access"),;
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
  private AccessFlags(int flag, String label) {
    this.flagValue = flag;
    this.label = DataEntry.cleanString(label);
  }
  //</editor-fold>
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">  
  /**
   * Convert the specified flagValue mask to a EnumSet of AccessFlags. If 
   * (flagMask=0), return EnumSet{ReadOnly}, else return am EnumSet with all supported
   * access flags. 
   * @param flagMask the specified flagValue mask
   * @return a EnuMSet of AccessFlags supported by the flagMask
   */
  public static EnumSet<AccessFlags> getEnumSet(int flagMask) {
    EnumSet<AccessFlags> result = EnumSet.noneOf(AccessFlags.class);
    if (AccessFlags.isReadOnly(flagMask)) {
      result.add(AccessFlags.ReadOnly);
    } else {
      if (AccessFlags.getAllowEdit(flagMask)) {
        result.add(AccessFlags.AllowEdits);
      }
      if (AccessFlags.getAllowAdd(flagMask)) {
        result.add(AccessFlags.AllowAdd);
      }
      if (AccessFlags.getAllowDelete(flagMask)) {
        result.add(AccessFlags.AllowDelete);
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
  public static int getFlagMask(AccessFlags...enums) {
    int result = 0;
    if ((enums != null) && (enums.length > 0)) {
      for (AccessFlags enumVal : enums) {
        result = (result | enumVal.flagValue);
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
  public static int getFlagMask(boolean allowEdit, boolean allowAdd,
          boolean allowDelete) {
    int result = 0;
    if (allowEdit) {
      result = (result | AccessFlags.AllowEdits.flagValue);
    }
    if (allowAdd) {
      result = (result | AccessFlags.AllowAdd.flagValue);
    }
    if (allowDelete) {
      result = (result | AccessFlags.AllowDelete.flagValue);
    }
    return result;
  }
  
  /**
   * Check if the flagMask allows only ReadOnly access
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow no editing, adding, or deleting of records.
   */
  public static boolean isReadOnly(int flagMask) {
    return (flagMask == AccessFlags.ReadOnly.flagValue);
  }

  /**
   * Get whether the flagMask allows record editing.
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow editing.
   */
  public static boolean getAllowEdit(int flagMask) {
    return ((flagMask & AccessFlags.AllowEdits.flagValue) == 
              AccessFlags.AllowEdits.flagValue);
  }

  /**
   * Get whether the flagMask allows adding of new records.
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow addition.
   */
  public static boolean getAllowAdd(int flagMask) {
    return ((flagMask & AccessFlags.AllowAdd.flagValue) == 
            AccessFlags.AllowAdd.flagValue);
  }

  /**
   * Get whether the flagMask allows deleting of records.
   * @param flagMask the access flag to evaluate
   * @return true if the setting allow deletion.
   */
  public static boolean getAllowDelete(int flagMask) {
    return ((flagMask & AccessFlags.AllowDelete.flagValue) == 
            AccessFlags.AllowDelete.flagValue);
  }
  //</editor-fold>
}
