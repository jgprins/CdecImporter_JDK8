package bubblewrap.entity.enums;

/**
 * Enums to set the String Search Options - used by EntitySearch classes.
 * @author kprins
 */
public enum StringSearchOptions {
   /**
   * Search Values must match the whole word or phrases. The entered SearchValue will be
   * stripped from any leading and pending spaces and must match the word
    * (e.g., "sender" to match "sender").
   */
  WHOLE,
  /**
   * Search Values can represents any partial word. The entered SearchValue will be
   * appended with "%" to return any matching word (e.g., "send%" to match "sender").
   */
  PARTIAL,
  /**
   * Search Values can represents any partial phrase. The entered SearchValue will be
   * pre- and appended with "%" to return any matching phrases (e.g., "%end%" to
   * match say "sender").
   */
  SEGMENTS;
}
