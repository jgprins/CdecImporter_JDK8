package bubblewrap.mail;

import bubblewrap.app.context.BwAppContext;
import bubblewrap.app.context.BwWebServiceContext;

/**
 * An abstract class using a EmailFormattedContent class to define the email's content.
 * @author kprins
 */
public abstract class AdminMail extends EmailSender {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private EmailFormattedContent mpContent = null;  
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - Initiate the Session and the EMail Content - Call initContent
   * to format the Content
   * @throws Exception
   */
  public AdminMail(String sSessionName) throws Exception {
    super(sSessionName);
    this.mpContent = new EmailFormattedContent();
    this.initContent(this.mpContent);
  }

  /**
   * Override: Call the super method before releasing the local resources.
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.mpContent = null;
  }

  /**
   * CAN OVERRIDE: Called by the constructor to initiate the content format and layout.
   * Inheritors can override this to define the EmailFormattedContent styles, the Header, and
   * the Footer.
   * @param pContent EmailFormattedContent
   */
  protected final void initContent(EmailFormattedContent pContent) throws Exception {
    if (pContent == null) {
      return;
    }
    
    /** Set the CSS Styles */
    pContent.setCSSClass(EmailFormattedContent.CSSDocBody,
            "  background-color: #ffffcc;\n" +
            "  text-align: center;\n" +
            "  margin-top: 0px;\n" +
            "  padding-top: 0px;");
    pContent.setCSSClass(EmailFormattedContent.CSSPage,
            "  background-color: #ffffcc;\n" +
            "  text-align: center;");
    pContent.setCSSClass(EmailFormattedContent.CSSFrame,
            "  background-color: #ecece7;\n" +
            "  width: 600px;\n" +
            "  border-left-width: 2px;\n" +
            "  border-left-color: #bcd2ed;\n" +
            "  border-left-style: solid;\n" +
            "  border-top-width: 2px;\n" +
            "  border-top-color: #bcd2ed;\n" +
            "  border-top-style: solid;\n" +
            "  border-right-width: 2px;\n" +
            "  border-right-color: #295d9c;\n" +
            "  border-right-style: solid;\n" +
            "  border-bottom-width: 2px;\n" +
            "  border-bottom-color: #295d9c;\n" +
            "  border-bottom-style: solid;\n");
    pContent.setCSSClass(EmailFormattedContent.CSSHeader,
            "  width: 100%;\n" +
            "  height: 50px;\n" +
            "  padding-left: 4px;\n" +
            "  padding-right: 4px;\n" +
            "  border-bottom-color: #bcd2ed;\n" +
            "  border-bottom-style: solid;\n" +
            "  border-bottom-width: 2px;\n" +
            "  text-align: left;");
    pContent.setCSSClass(EmailFormattedContent.CSSFooter,
            "  width: 100%;\n" +
            "  height: 25px;\n" +
            "  padding-left: 4px;\n" +
            "  padding-right: 4px;\n" +
            "  border-top-color: #bcd2ed;\n" +
            "  border-top-style: solid;\n" +
            "  border-top-width: 2px;\n" +
            "  text-align: center;");
    pContent.setCSSClass(EmailFormattedContent.CSSBody,
            "  background-color: #ffffff;\n" +
            "  padding-top: 4px;\n" +
            "  padding-left: 4px;\n" +
            "  padding-right: 4px;\n" +
            "  padding-bottom: 4px;\n" +
            "  width: 100%;\n" +
            "  height: 450px;\n" +
            "  text-align: left;\n" +
            "  vertical-align: top;");
    pContent.setCSSClass(EmailFormattedContent.CSSSubject,
            "  width: 100%;\n" +
            "  height: 30px;\n" +
            "  text-align: left;\n" +
            "  vertical-align: bottom;");
    pContent.setCSSClass(EmailFormattedContent.CSSBodyHeader,
            "  padding-bottom: 2px;\n" +
            "  border-bottom-width : 2px;\n" +
            "  border-bottom-style: dotted;\n" +
            "  border-bottom-color:  #999;\n" +
            "  font-size: 12pt;\n" +
            "  font-weight: bold;\n" +
            "  font-family: Georgia, 'Times New Roman', Times, serif;\n" +
            "  color: #295d9c;\n" +
            "  line-height: 100%;");
    pContent.setCSSClass(EmailFormattedContent.CSSBodyText,
            "  width: 100%;\n" +
            "  padding-top: 8px;\n" +
            "  padding-bottom: 8px;\n" +
            "  text-align: left;\n" +
            "  vertical-align: top;");
    pContent.setCSSClass(EmailFormattedContent.CSSSignature,
            "  width: 100%;\n" +
            "  text-align: left;\n" +
            "  padding-top: 20px;\n" +
            "  vertical-align: top;");
    pContent.setCSSClass("p",
            "  background-color: #ffffff;\n" +
            "  width: 100%;");
    pContent.setCSSClass("a:link, a:visited",
            "  color: #045491;\n" +
            "  text-decoration: none;");
    pContent.setCSSClass("a:hover, a:link:hover, a:visited:hover",
            "  color: #045491;\n" +
            "  text-decoration : underline;\n" +
            "  cursor: pointer;");
    pContent.setCSSClass("p, ul, li",
            "  font-size: 10pt;\n" +
            "  font-family: Arial,Helvetica,sans-serif;\n" +
            "  color: #000000;");
//    pContent.setCSSClass("span.DWR",
//            "  font-size: 12pt;\n" +
//            "  font-weight: bold;\n" +
//            "  font-family: Arial,Helvetica,sans-serif;\n" +
//            "  color: #660000;");
//    pContent.setCSSClass("span.LOM",
//            "  font-size: 22pt;\n" +
//            "  font-weight: bold;\n" +
//            "  font-family: Arial,Helvetica,sans-serif;\n" +
//            "  color: #295d9c;");
    pContent.setCSSClass("span.Footer",
            "  font-size: 10pt;\n" +
            "  font-family: Arial,Helvetica,sans-serif;\n" +
            "  color: #000000;");
//    //
//    // Set the Header
//    //
//    String sHeader = "";
//    sHeader += "<span class='DWR'>Department of Water Resources</span><br/>\n";
//    sHeader += "<span class='LOM'>Library of Models</span><br/>\n";
//    pContent.setHeader(sHeader);
    
    BwWebServiceContext webCtx = BwWebServiceContext.doLookUp();
    String sFooter = null;
    if (webCtx != null) {
      sFooter = "<span class='Footer'>For more information visit our web site at " +
            "<a href='"+ webCtx.getHostURL()+"'>"+
            webCtx.getHostURL()+"</a></span>\n";
      pContent.setFooter(sFooter);
    }
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Reset the To/From Addresses, the RecipientType, and clear the Body Text.
   */
  @Override
  public void Reset() {
    super.Reset();
    this.mpContent.clearBodyContent();
  }
  
  /**
   * Add the Email's Subject.
   * @param sText String
   */
  public void setSubject(String sText) {
    this.mpContent.setSubject(sText);
  }

  /**
   * Add the Email's Signature.
   * @param sText String
   */
  public void setSignature(String sText) {
    this.mpContent.setSignature(sText);
  }

  /**
   * Add HTML Formatted text to the EMail Body.
   * @param sText String
   */
  public void addEmailText(String sText) {
    this.mpContent.addBodyText(sText);
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement EmailSender">
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: return this instance's EmailFormattedContent.</p>
   */
  @Override
  protected EmailContent onGetContent() {
    return this.mpContent;
  }
  //</editor-fold>
}
