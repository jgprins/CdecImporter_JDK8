package bubblewrap.io.enums;

/**
 * String Match type used in keyword Searched
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum StringMatch {
  /**
   * Must be a Full Match
   */
  FULL,
  /**
   * Must start with
   */
  STARTWITH,
  /**
   * Must end with
   */
  ENDWITH,
  /**
   * Must be contained within
   */
  CONTAINS;
}
