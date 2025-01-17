package bubblewrap.entity.enums;

/**
 * Constants Used for the generic or specific SearchId to define search and sub-searches
 * @author kprins
 */
public class SearchIds {

  //<editor-fold defaultstate="collapsed" desc="Core Entity Search Ids">
  /**
   * Search on a Entity.recordName [{@value}]
   */
  public final static String RecordName = "siRecordName";
  /**
   * Search on a Entity.recordId [{@value}]
   */
  public final static String RecordId = "siRecordId";
  /**
   * Search on a Entity.recordDesc [{@value}]
   */
  public final static String RecordDesc = "siRecordDesc";
  /**
   * Search on a Entity.recordDesc [{@value}]
   */
  public final static String Keywords = "siKeywords";
  /**
   * Search on a Entity.recordDesc [{@value}]
   */
  public final static String MapKey = "siMapKey";
  /**
   * A General Custom Content Search (a combination of fields using a Keyword Search)
   *  [{@value}]
   */
  public final static String Content = "siContent";
  /**
   * A General search on AdminCategories  [{@value}]
   */
  public final static String Category = "siCategory";
  /**
   * The ID of the Search's Hide System Items Filter  [{@value}]
   */
  public final static String HideSystemItem = "siHideSystemItem";
  /**
   * The ID of the Search's Hide Disabled Items Filter  [{@value}]
   */
  public final static String ActiveOnly = "siActiveOnly";
  /**
   * The ID of the Search's Apply ForeignKey Filter  [{@value}]
   */
  public final static String ApplyForeignKey = "siForeignKey";
  /**
   * The ID of the Search's Static Filter  [{@value}]
   */
  public final static String StaticFilter = "siStaticFilter";
  /**
   * The ID of the Search's UserGroup (Typically used by the AppUserSearch)  [{@value}]
   */
  public final static String UserGroup = "siUserGroup";
  /**
   * The ID of the Search's UserName (Typically used by the AppUserSearch)  [{@value}]
   */
  public static final String UserName = "siUserName";
  /**
   * The ID of the Search's AssignedTo (Typically used by the BwUserTask Search)  [{@value}]
   */
  public static final String AssignedTo = "siAssignedTo";
  /**
   * The ID of the Search's AssignedTo (Typically used by the BwUserTask Search)  [{@value}]
   */
  public static final String Handled = "siHandled";
 //</editor-fold>
}
