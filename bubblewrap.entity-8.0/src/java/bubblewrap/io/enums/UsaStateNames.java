package bubblewrap.io.enums;

import bubblewrap.io.DataEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.faces.model.SelectItem;

/**
 * A Static (Enum) class for generating a USA State Name (or acronym) selection list. 
 * it supports to list styles:<ul>
 *  <li><b>{@linkplain #STYLE_ACRONYM}:</b>- showing the Acronym as 
 *      select option value and label.</li>
 *  <li><b>{@linkplain #STYLE_NAME}:</b>- showing the State 
 * name as select label value with the acronym as the option value).</li>
 * </ul>   
 * @author kprins
 */
public class UsaStateNames {
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Sub-Class">
  /**
   * Public Class containing Each State's Name and Acronym
   */
  public static class StateName {
    //<editor-fold defaultstate="collapsed" desc="Pivate Fields">
    /**
     * Placeholder for State Acronym
     */
    private String acronym;
    /**
     * Placeholder for State Name
     */
    private String name;
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public StateName(String name, String acronym) {
      this.acronym = acronym;
      this.name = name;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Publi Methods">
    /**
     * Get the state's 2 character acronym
     * @return the assigned acronym
     */
    public String getAcronym() {
      return this.acronym;
    }
    
    /**
     * Get the state Name
     * @return the assigned name
     */
    public String getName() {
      return this.name;
    }
    //</editor-fold>    
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Selection List Styles">
  /**
   * Selection List Style showing Acronyms as labels and names only [{@value }]
   */
  public static final int STYLE_ACRONYM = 0;
  /**
   * Selection List Style showing Names as labels and Acronyms as values [{@value }]
   */
  public static final int STYLE_NAME = 1;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Static StateNem array">
  /**
   * Static Array with State Names
   */
  public final static StateName[] stateNames = new StateName[]{
    new StateName("Alabama", "AL"),
    new StateName("Alaska", "AK"),
    new StateName("Arizona", "AZ"),
    new StateName("Arkansas", "AR"),
    new StateName("California", "CA"),
    new StateName("Colorado", "CO"),
    new StateName("Colorado", "CO"),
    new StateName("Connecticut", "CT"),
    new StateName("Delaware", "DE"),
    new StateName("Florida", "FL"),
    new StateName("Georgia", "GA"),
    new StateName("Hawaii", "HI"),
    new StateName("Idaho", "ID"),
    new StateName("Illinois", "IL"),
    new StateName("Indiana", "IN"),
    new StateName("Iowa", "IA"),
    new StateName("Kansas", "KS"),
    new StateName("Kentucky", "KY"),
    new StateName("Louisiana", "LA"),
    new StateName("Maine", "ME"),
    new StateName("Maryland", "MD"),
    new StateName("Massachusetts", "MA"),
    new StateName("Michigan", "MI"),
    new StateName("Minnesota", "MN"),
    new StateName("Mississippi", "MS"),
    new StateName("Missouri", "MO"),
    new StateName("Montana", "MT"),
    new StateName("Nebraska", "NE"),
    new StateName("Nevada", "NV"),
    new StateName("New Hampshire", "NH"),
    new StateName("New Hampshire", "NH"),
    new StateName("New Jersey", "NJ"),
    new StateName("New Mexico", "NM"),
    new StateName("New York", "NY"),
    new StateName("North Carolina", "NC"),
    new StateName("North Dakota", "ND"),
    new StateName("Ohio", "OH"),
    new StateName("Oklahoma", "OK"),
    new StateName("Oregon", "OR"),
    new StateName("Pennsylvania", "PA"),
    new StateName("Rhode Island", "RI"),
    new StateName("South Carolina", "SC"),
    new StateName("South Dakota", "SD"),
    new StateName("Tennessee", "TN"),
    new StateName("Texas", "TX"),
    new StateName("Utah", "UT"),
    new StateName("Vermont", "VT"),
    new StateName("Virginia", "VA"),
    new StateName("Washington", "WA"),
    new StateName("West Virginia", "WV"),
    new StateName("Wisconsin", "WI"),
    new StateName("Wyoming", "WY"),
  };
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="StateNameCoparator Comparator class">
  /**
   * Private Comparator Class to sort the StateNames by Acronym or Name depending on
   * the listStyle
   */
  private static class StateNameCoparator implements Comparator<StateName> {
    
    private int listStyle = UsaStateNames.STYLE_ACRONYM;
    
    //<editor-fold defaultstate="collapsed" desc="Constructors">
    /**
     * Public Constructor
     */
    public StateNameCoparator(int listStyle) {
      this.listStyle = listStyle;
    }
    //</editor-fold>
    
    @Override
    public int compare(StateName pState1, StateName pState2) {
      int result = 0;
      if (this.listStyle == UsaStateNames.STYLE_ACRONYM) {
        result = pState1.getAcronym().compareTo(pState2.getAcronym());
      } else {
        result = pState1.getName().compareTo(pState2.getName());
      }
      return result;
    }
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Public Static Methods">
  /**
   * Called to get a selection list with all the state names according to the specified
   * list style (showing Acronyms or full state Names)
   * @param listStyle the required list style
   * @return the selection list
   */
  @SuppressWarnings("unchecked")
  public static List<SelectItem> getSelectOptions(int listStyle) {
    List<SelectItem> result = new ArrayList<>();
    listStyle = (listStyle == UsaStateNames.STYLE_NAME)? listStyle:
            UsaStateNames.STYLE_ACRONYM;
    List<StateName> nameList = new ArrayList(Arrays.asList(UsaStateNames.stateNames));
    if (listStyle == UsaStateNames.STYLE_ACRONYM) {
      Collections.sort(nameList, new StateNameCoparator(listStyle));
      for (StateName stateName : nameList) {
        result.add(new SelectItem(stateName.getAcronym(), stateName.getAcronym()));
      }
    } else {
      for (StateName stateName : nameList) {
        result.add(new SelectItem(stateName.getAcronym(), stateName.getName()));
      }
    }
    return result;
  }
  
  /**
   * Get the full state name for a specified acronym
   * @param acronym the acronym to search a match for.
   * @return the matching state name or null if no match were found.
   */
  public static String getStateName(String acronym) {
    String result = null;
    acronym = DataEntry.cleanString(acronym);
    if (acronym != null) {
      acronym = acronym.toUpperCase();
      for (StateName stateName : UsaStateNames.stateNames) {
        if (acronym.equals(stateName.getAcronym())) {        
          result = stateName.getName();
          break;
        }
      }
    }
    return result;
  }
  //</editor-fold>
}
