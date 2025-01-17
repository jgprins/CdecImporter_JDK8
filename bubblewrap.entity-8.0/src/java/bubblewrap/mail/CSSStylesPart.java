package bubblewrap.mail;


import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.ContentTypes;
import java.util.HashMap;
import javax.mail.MessagingException;

/**
 *
 * @author kprins
 */
public class CSSStylesPart extends MailBodyPart {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private HashMap<String,String> cssStyles = null;
  // </editor-fold>  

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor -
   * @param sContentType
   * @throws Exception
   */
  public CSSStylesPart() throws Exception {
    super(ContentTypes.CSS);
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get the assigned CSSStyles
   * @return 
   */
  public String getCSSStyles() {
    String result = "<style type='text/css'>";
    for (String styleClass : this.cssStyles.keySet()) {
      String styles = this.cssStyles.get(styleClass);

      result += "\n";
      result += "  " + styleClass + " {\n";
      result += "  " + styles + "\n";
      result += "  }\n";
    }
    result += "</style>\n";
    return result;
  }
  
  /**
   * Set the Styles for sClass. Ignored is sClass is undefined. If sStyle=null and
   * the class is defined, the style class will be removed, otherwise add the definition
   * or override the existing definition.
   * @param styleClass String
   * @param styles String
   */
  public void setCSSClass(String styleClass, String styles) {
    styleClass = DataEntry.cleanString(styleClass);
    styles = DataEntry.cleanString(styles);
    boolean hasStyles = ((this.cssStyles != null) && (!this.cssStyles.isEmpty()));
    if (styleClass != null) {
      if (styles == null) {
        if ((hasStyles) && (this.cssStyles.containsKey(styleClass))) {
          this.cssStyles.remove(styleClass);
        }
      } else {
        if (this.cssStyles == null) {
          this.cssStyles = new HashMap<>();
        }
        this.cssStyles.put(styleClass, styles);
      }
    }
  }
  // </editor-fold>
    
  // <editor-fold defaultstate="collapsed" desc="MailBodyPart Overrides">
  /**
   * IMPLEMENT: Generate a String containing the CSS definitions between the <style>
   * </style> tags. Call addContent(CSS String) to add it to the BodyPart
   * @throws Exception
   */
  @Override
  protected void onBuildBodyPart() throws MessagingException {
    String styles = null;
    if ((this.cssStyles == null) || (this.cssStyles.isEmpty()) ||
            ((styles = this.getCSSStyles()) == null)) {
      return;
    }
    this.addContent(styles);
  }
// </editor-fold>
}
