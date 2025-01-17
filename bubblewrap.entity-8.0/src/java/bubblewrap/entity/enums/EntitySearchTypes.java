package bubblewrap.entity.enums;

/**
 * An enum class that define the SearchFilter Type. These types distinguish how the 
 * search entry is processed and when search interface if supports on a FacePage.
 * @author kprins
 */
public enum EntitySearchTypes {
  
  /**
   * Search on a user entered input string
   */
  TEXTINPUT,
  /**
   * Search using a single user selection from a set of defined options
   */
  SINGLESELECT,
  /**
   * Search using multiple user selections from a set of defined options
   */
  MULTISELECT,
  /**
   * Search using a boolean (true/false) selection
   */
  BOOLEAN;
}
