package bubblewrap.mail;

import java.util.logging.Level;

/**
 * The is the base class for generating an email based on a predefined XHTML email 
 * template. The class uses a MailTemplateReader to generate the email body using the
 * template file. It also initiates the mail session and send to email to recipients as
 * defined in the ToAddresses, CcAddresses, and the BccAddresses. It also allow 
 * attachment of files.
 * @author hdunsford/kprins
 */
public class TemplateEmail extends EmailSender {

  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private MailTemplateReader templateReader;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - Initiate the Session and the EMail Content - Call initContent
   * to format the Content
   * @throws Exception
   */
  public TemplateEmail(String sessionName) throws Exception {
    super(sessionName);
    this.templateReader = null;
  }

  /**
   * Override: Call the super method before releasing the local resources.
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.templateReader = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Get a reference to the Templates MailTemplate Reader
   * @return the cached MailTemplateReader
   */
  public MailTemplateReader getTemplateReader() {
    if (this.templateReader == null) {
      try {
        MailTemplateReader newReader = new MailTemplateReader();
        this.initTemplateReader(newReader);
        this.templateReader = newReader;
      } catch (Exception exp) {
        logger.log(Level.WARNING, "{0}.getTemplateReader Error:\n {1}",
                new Object[]{this.getClass().getSimpleName(), exp.getMessage()});
      }
    }
    return this.templateReader;
  }
  // </editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Implement EmailSender">
  /**
   * Called by {@link #getTemplateReader() this.getTemplateReader} to initiate the
   * template's MailTemplateReader 
   * @param templateReader the new template to initiate before its becomes 
   * this.templateReader
   */
  protected void initTemplateReader(MailTemplateReader templateReader) throws Exception {
  }
  
  /**
   * {@inheritDoc }
   * <p>OVERRIDE: Initiate a new EmailContent and assign this.template.text as the
   * EmailContent's BodyHtml and set the EmailContent's subject=this.template.subject
   * </p>
   */
  @Override
  protected EmailContent onGetContent() {
    EmailContent result = null;
    try {
      result = new EmailContent();      
      String mailMsg = this.templateReader.getText();
      result.setBodyHtml(mailMsg);
      result.setSubject(this.templateReader.getSubject());
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.onGetContent Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  //</editor-fold>
}
