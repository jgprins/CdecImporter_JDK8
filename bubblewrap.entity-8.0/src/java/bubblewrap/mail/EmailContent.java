package bubblewrap.mail;

import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import bubblewrap.io.DataEntry;
import bubblewrap.io.enums.ContentTypes;

/**
 * A MailBodyPart extension to support a simple HTML email content with attachments
 * @author hdunsford/kprins
 */
public class EmailContent extends MailBodyPart {
    
  //<editor-fold defaultstate="collapsed" desc="Private fields">
  /**
   * Placeholder for the Attachment BodyParts
   */
  private AttachmentsBodyPart attachments;
  /**
   * Placeholder for the emails HTML body
   */
  private String bodyHtml;
  /**
   * Placeholder for the email Subject
   */
  private String subject;
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Constructor
   * @throws Exception
   */
  public EmailContent() {
    super(ContentTypes.MULTIPART);
  }
//</editor-fold>

  //<editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * @return the attachments
   */
  public AttachmentsBodyPart getAttachments() {
    return this.attachments;
  }
  
  /**
   * @param attachments the attachments to set
   */
  public void setAttachments(AttachmentsBodyPart attachments) {
    this.attachments = attachments;
  }
  
  /**
   * @return the bodyHtml
   */
  public String getBodyHtml() {
    return this.bodyHtml;
  }
  
  /**
   * @param bodyHtml the bodyHtml to set
   */
  public void setBodyHtml(String bodyHtml) {
    this.bodyHtml = DataEntry.cleanString(bodyHtml);
  }
  
  /**
   * Check if the Email's Body has been defined.
   * @return true if undefined.
   */
  public boolean isEmpty() {
    return (this.bodyHtml == null);
  }
  
  /**
   * @return the msSubject
   */
  public String getSubject() {
    return subject;
  }
  
  /**
   * @param msSubject the msSubject to set
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
  //</editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="MailBodyPart Overrides">
  @Override
  protected void onBuildBodyPart() throws MessagingException {
    if (this.bodyHtml == null) {
      return;
    }
    
    List<MailBodyPart> pParts = new ArrayList<>();
    HTMLBodyPart pBodyContent = new HTMLBodyPart();
    pBodyContent.setContent(this.bodyHtml);
    
    pParts.add(pBodyContent);
    if (this.attachments != null) {
      pParts.add(this.attachments);
    }
    
    this.addContent(pParts, "mixed");
  }
  //</editor-fold>
}
