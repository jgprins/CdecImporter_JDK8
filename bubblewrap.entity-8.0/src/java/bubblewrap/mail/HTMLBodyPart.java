package bubblewrap.mail;

import javax.mail.MessagingException;
import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.ContentTypes;

/**
 * Encapsulate a BodyPart containing HTML content
 * @author kprins
 */
public class HTMLBodyPart extends MailBodyPart {

  // <editor-fold defaultstate="collapsed" desc="Private String">
  /**
   * Placeholder for the HTML BodyPart's Text
   */
  private String htmlText = null;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public COnstructore - call super(ContentTypes.HTML)
   * @throws Exception
   */
  public HTMLBodyPart(){
    super(ContentTypes.HTML);
  }
  // </editor-fold>

  /**
   * OVERRIDE: call setContent(msHtmlText)
   * @throws Exception
   */
  @Override
  protected void onBuildBodyPart() throws MessagingException {
    this.addContent(this.htmlText);
  }
// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public method">
  /**
   * Set the Body's HTML Text.
   * @param htmlText String
   */
  public void setContent(String htmlText) {
    this.htmlText = DataEntry.cleanString(htmlText);
  }
// </editor-fold>
}
