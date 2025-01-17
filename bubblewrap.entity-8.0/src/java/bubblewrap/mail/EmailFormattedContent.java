package bubblewrap.mail;

import javax.mail.MessagingException;
import bubblewrap.io.DataEntry;

/**
 * The Extension of the MailBodyPart class contains the sub BodyParts for the HTML
 * Email's CSS classes, the email content, and/or email attachments.  The email content
 * if broken down in three regions: a header, body, signature, and footer region.
 * Only the body is required.
 * @author kprins
 */
public class EmailFormattedContent extends EmailContent {

  // <editor-fold defaultstate="collapsed" desc="Publis Static CSSClasses">
  /**
   * body - The table cell for the full body of the email window;
   */
  public static String CSSDocBody = "body";
  /**
   * td.EmailPage - The table cell for the full body of the email window;
   */
  public static String CSSPage = "td.EmailPage";
  /**
   * table.EmailFrame - for the frame containing the Header,Body,Signature, and Footer
   */
  public static String CSSFrame = "table.EmailFrame";
  /**
   * td.EmailHeader - for the Header region
   */
  public static String CSSHeader = "td.EmailHeader";
  /**
   * table.EmailBody - for the Body region
   */
  public static String CSSBody = "td.EmailBody";
  /**
   * table.EmailBody - for the Body region
   */
  public static String CSSSubject = "td.EmailSubject";
  /**
   * table.h2#BodyHeader - for the Body region's Header (displaying the Subject)
   */
  public static String CSSBodyHeader = "h2#BodyHeader";
  /**
   * table.EmailBody - for the Body region
   */
  public static String CSSBodyText = "td.EmailBodyText";
  /**
   * td.EmailSignature - for the Signature region
   */
  public static String CSSSignature = "td.EmailSignature";
  /**
   * table.EmailFooter - for the Footer region
   */
  public static String CSSFooter = "td.EmailFooter";
// </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private CSSStylesPart cssStyles = null;
  private AttachmentsBodyPart attachments = null;
  private String headerText = null;
  private String bodyText = null;
  private String signature = null;
  private String footerText = null;
// </editor-fold>



  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.attachments = null;
    this.cssStyles = null;
  }
// </editor-fold>


  /**
   * Add the Email's Content - First the Styles, then the Body, followed by the
   * Attachments.  If the BodyText is undefined - nothing is added to the internal
   * BodyPart.  The Body's HTML text is build based on the HeaderText, BodyText,
   * Signature, and FooterText settings. It is build using as a HTML table representing
   * the e-mail page (with=100%;height=100%) with one cell (class=td.EmailPage). Inside
   * this cell is a nested table (class=table.EmailFrame) with one-cell rows for the
   * Header, Body, Signature, and Footer (as needed).
   * It uses the following StyleSheet classes:
   * - td.EmailPage - The table cell for the full body of the email window;
   * - table.EmailFrame - for the frame containing the Header,Body,Signature, and Footer
   * - td.EmailHeader - for the Header region
   * - td.EmailBody - for the Body region
   * - td.EmailSignature - for the Signature region
   * - td.EmailFooter - for the Footer region
   * @throws Exception
   */
  @Override
  protected void onBuildBodyPart() throws MessagingException {
    if (this.bodyText == null) {
      return;
    }

    String html = "";
    if (this.cssStyles != null) {
      String sStyles = this.cssStyles.getCSSStyles();
      //pParts.add(this.mpEmailStyles);
      html += sStyles+"\n";
    }

    html += "<table border='0' cellpadding='0' cellspacing='0' " +
              "style='width:100%;'>\n";
    html += "<tr><td class='EmailPage' style='width:100%;'>\n";
    html += "<table border='0' cellpadding='0' cellspacing='0' class='EmailFrame'>\n";

    if (this.headerText !=  null) {
      html += "  <tr><td class='EmailHeader' style='width:100%;'>\n";
      html += this.headerText+"\n";
      html += "  </td></tr>\n";
    }

    html += "  <tr><td class='EmailBody' style='width:100%;'>\n";
    html += "    <table border='0' cellpadding='0' cellspacing='0' style='width:100%;'>\n";

    if (this.getSubject() != null) {
      html += "      <tr><td class='EmailSubject' style='width:100%;'>\n";
      html += "        <h2 id='BodyHeader'>"+this.getSubject()+"</h2>\n";
      html += "      </td></tr>\n";
    }

    html += "      <tr><td class='EmailBodyText' style='width:100%;'>\n";
    html += this.bodyText+"\n";
    html += "      </td></tr>\n";

    if (this.signature !=  null) {
      html += "      <tr><td class='EmailSignature' style='width:100%;'>\n";
      html += this.signature+"\n";
      html += "      </td></tr>\n";
    }

    html += "    </table>\n";

    if (this.footerText !=  null) {
      html += "  <tr><td class='EmailFooter' style='width:100%;'>\n";
      html += this.footerText+"\n";
      html += "  </td></tr>\n";
    }

    html += "</table>\n";
    html += "</td></tr>\n";
    html += "</table>\n";

    this.setBodyHtml(html);
    super.onBuildBodyPart();
  }
// </editor-fold>
  
// <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Return true if the E-mail Body Text is not defined.
   * @return boolean
   */
  @Override
  public boolean isEmpty() {
    return (this.bodyText == null);
  }

  /**
   * Clear the All E-mail Content, styles, attachments, etc.
   */
  public void clearAll() {
    this.cssStyles = null;
    this.attachments = null;
    this.headerText = null;
    this.bodyText = null;
    this.signature = null;
    this.footerText = null;
  }

  /**
   * Set the Styles for sClass. Ignored is sClass is undefined. If sStyle=null and
   * the class is defined, the style class will be removed, otherwise add the definition
   * or override the existing definition.
   * @param styleClass String
   * @param cssStyle String
   */
  public void setCSSClass(String styleClass, String cssStyle) throws Exception {
    styleClass = DataEntry.cleanString(styleClass);
    cssStyle = DataEntry.cleanString(cssStyle);
    if ((styleClass == null) || (cssStyle == null)) {
      return;
    }

    if (this.cssStyles == null) {
      this.cssStyles = new CSSStylesPart();
    }

    this.cssStyles.setCSSClass(styleClass, cssStyle);
  }

  /**
   * Set the EMail Header (not the subject)
   * @param header String
   */
  public void setHeader(String header) {
    this.headerText = DataEntry.cleanString(header);
  }

  /**
   * Set the EMail Footer (not the subject)
   * @param sSignature String
   */
  public void setSignature(String signature) {
    this.signature = DataEntry.cleanString(signature);
  }

  /**
   * Set the EMail Footer (not the subject)
   * @param sFooter String
   */
  public void setFooter(String footerText) {
    this.footerText = DataEntry.cleanString(footerText);
  }

  /**
   * Clear only the Body Content (the Subject, Text, & Signature)
   */
  public void clearBodyContent() {
    this.setSubject(null);
    this.bodyText = null;
    this.signature = null;
  }

  

  /**
   * Set/Append sBody to BodyText
   * @param sBody String
   */
  public void addBodyText(String bodyText) {
    bodyText = DataEntry.cleanString(bodyText);
    if (bodyText == null) {
      return;
    }

    if (this.bodyText == null) {
      this.bodyText = bodyText;
    } else {
      this.bodyText = "\n" + bodyText;
    }
  }

  /**
   * Add a new Attachment to the E-mail
   * @param filePath String
   * @throws Exception
   */
  public void addAttachment(String filePath) throws Exception {
    if (this.attachments == null) {
      this.attachments = new AttachmentsBodyPart();
    }
    this.attachments.addAttachment(filePath);
  }

}
