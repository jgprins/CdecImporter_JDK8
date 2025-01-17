package bubblewrap.io.enums;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
public enum GroupDelimiter {
  DBLQUOTES(0,"\"",1,"\"",1),
  SGLQUOTES(1,"\'",1,"\'",1),
  PIPE(2,"|",1,"|",1),
  BACKSLASH(3,"\\",1,"\\",1),
  FOWARDSLASH(4,"/",1,"/",1),
  SQUAREBRACKETS(5,"[",1,"]",1),
  CURLYBRACKETS(6,"{",1,"}",1),
  DBLPIPES(7,"||",2,"||",2),
  MARKUP(7,"<",1,"/>",2);
  
  // <editor-fold defaultstate="collapsed" desc="Enum Definition">
  // <editor-fold defaultstate="collapsed" desc="Public Final Fields">
  /**
   * A Defined enum value (not its ordinate)
   */
  public final int value;
  /**
   * The character that opens the group
   */
  public final String open;
  /**
   * The character that closes the group
   */
  public final String close; 
  /**
   * The number of characters in the Open delimiter
   */
  public final int openLen; 
  /**
   * The number of characters in the Close delimiter
   */
  public final int closeLen;
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Constructor">  
  /**
   * Private Constructor  
   * @param value the option value
   * @param label the option label
   */
  private GroupDelimiter(int value, String open, int openLen, String close, int closeLen) {
    this.value = value;
    this.open = open;
    this.close = close;
    this.openLen = openLen;    
    this.closeLen = closeLen;
  }
  // </editor-fold>
  // </editor-fold>
  
//  // <editor-fold defaultstate="collapsed" desc="Public Static Methods">
//  /**
//   * Get the GroupDelimiter associated with <tt>value</tt>
//   * @param value the GroupDelimiter.value to search for
//   * @return the matching GroupDelimiter or NONE if not found.
//   */
//  public static GroupDelimiter fromValue(int value) {
//    GroupDelimiter result = GroupDelimiter.NONE;
//    for (GroupDelimiter enumVal : GroupDelimiter.values()) {
//      if (enumVal.value == value) {
//        result = enumVal;
//        break;
//      }
//    }
//    return result;
//  }
//  // </editor-fold>
}
