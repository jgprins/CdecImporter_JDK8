package bubblewrap.mail;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.NamingException;
import bubblewrap.io.DataEntry;

/**
 * The is the base class for generating an email based on a predefined XHTML email 
 * template. The class uses a MailTemplateReader to generate the email body using the
 * template file. It also initiates the mail session and send to email to recipients as
 * defined in the ToAddresses, CcAddresses, and the BccAddresses. It also allow 
 * attachment of files.
 * @author hdunsford/kprins
 */
public abstract class EmailSender implements Serializable {

  // <editor-fold defaultstate="collapsed" desc="Public Static Fields">
  public static String SessionNameKey = EmailSender.class + ".SessionName";
  public static String DefaultFromKey = EmailSender.class + ".DefaultFrom";
  /**
   * Protected Static Logger object for logging errors, warnings, and info messages.
   */
  protected static final Logger logger = Logger.getLogger(EmailSender.class.getName());
  // </editor-fold>
  
  // <editor-fold defaultstate="collapsed" desc="Private Fields">
  private MailSession mailSession;
  private List<String> attachmentFiles;
  private List<InternetAddress> toAddresses;
  private List<InternetAddress> ccAddresses;
  private List<InternetAddress> bccAddresses;
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor - Initiate the Session and the EMail Content - Call initContent
   * to format the Content
   * @throws Exception
   */
  public EmailSender(String sessionName) throws Exception {
    this.toAddresses = null;
    this.ccAddresses = null;
    this.bccAddresses = null;
    this.attachmentFiles = null;
    this.mailSession = null;
            
    sessionName = DataEntry.cleanString(sessionName);
    if (sessionName == null) {
      throw new NamingException("The JavaMail Session Name cannot be unssigned");
    }
    
    try {      
      this.mailSession = new MailSession(sessionName);
    } catch (NamingException exp) {
      logger.log(Level.SEVERE, "The Email Session Name [" + sessionName
              + "] is invalid", exp);
      throw exp;
    } 
  }

  /**
   * Override: Call the super method before releasing the local resources.
   * @throws Throwable
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.mailSession = null;
  }
  // </editor-fold>

  // <editor-fold defaultstate="collapsed" desc="Public Methods">
  /**
   * Reset the To/From Addresses, the RecipientType, and clear the Body Text.
   */
  public void Reset() {
    this.clearToAddresses();
  }

  /**
   * Clear only the To, BCC, and CC addresses
   */
  public void clearToAddresses() {
    this.ccAddresses = null;
    this.bccAddresses = null;
    this.toAddresses = null;
  }
 
  /**
   * Add a To Address to To Address list
   * @param addressList an array of email addresses
   * @throws AddressException 
   */
  public void addToAddress(InternetAddress...addressList) throws AddressException {
    if ((addressList != null) && (addressList.length > 0)) {
      if (this.toAddresses == null) {
        this.toAddresses = new ArrayList<>();
      }
      this.toAddresses.addAll(Arrays.asList(addressList));
    }
  }
  
  /**
   * Add a CC Address to CC Address list
   * @param addressList an array of email addresses
   * @throws AddressException 
   */
  public void addCcAddress(InternetAddress...addressList) throws AddressException {
    if ((addressList != null) && (addressList.length > 0)) {
      if (this.ccAddresses == null) {
        this.ccAddresses = new ArrayList<>();
      }
      this.ccAddresses.addAll(Arrays.asList(addressList));
    }
  }
  
  /**
   * Add a BCC Address to BCC Address list
   * @param  addressList an array of email addresses
   * @throws AddressException 
   */
  public void addBccAddress(InternetAddress...addressList) throws AddressException {
    if ((addressList != null) && (addressList.length > 0)) {
      if (this.bccAddresses == null) {
        this.bccAddresses = new ArrayList<>();
      }
      this.bccAddresses.addAll(Arrays.asList(addressList));
    }
  }

  /**
   * Set the Sender's From address. Throws an exception is the sAddress is invalid. 
   * @see MailSession#setFrom(java.lang.String) 
   * @param address the from email address
   * @throws AddressException
   */
  public void setFromAddress(String address) throws AddressException {
    if (this.mailSession != null) {
      this.mailSession.setFrom(address);
    }
  }

  /**
   * Set the Sender's From address. Throws an exception is the sAddress is invalid. 
   * @see MailSession#setFrom(javax.mail.internet.InternetAddress)  
   * @param sAddress the from email address
   * @throws AddressException
   */
  public void setFromAddress(InternetAddress address) throws AddressException {
    if (this.mailSession != null) {
      this.mailSession.setFrom(address);
    }
  }

  /**
   * Add an attachment to the E-mail.  This will also verify that the 
   * file exists and throw a FileNotFoundException if not.
   * @param sFilePath String
   * @throws FileNotFoundException
   */
  public void addAttachment(String filePath) throws FileNotFoundException {
    File attachFile = new File(filePath);
    if (!attachFile.exists()) {
      throw new FileNotFoundException("Attachment Path[" + filePath + 
              "] is not not for a file.");
    }
    if (this.attachmentFiles == null) {
      this.attachmentFiles = new ArrayList<>();
    }
    this.attachmentFiles.add(filePath);
  }

  /**
   * If the specified path is in the attachments, this will remove the specified path.
   * @param sFilePath 
   */
  public void removeAttachment(String filePath) {
    if ((this.attachmentFiles != null) 
                          && (this.attachmentFiles.contains(filePath))) {
      this.attachmentFiles.remove(filePath);
    }
  }
  
  /**
   * Get a list of attached files
   * @return the mpAttachmentFiles
   */
  public List<String> getAttachmentFiles() {
    return attachmentFiles;
  }

  /**
   * Assign a list paths to the of attached files.
   * @param mpAttachmentFiles List<String>
   */
  public void setAttachmentFiles(List<String> attachmentFiles) {
    this.attachmentFiles = attachmentFiles;
  }

  /**
   * Send the Email Message
   */
  public boolean sendMessage() {
    boolean result = false;
    
    EmailContent content = this.onGetContent();
    if (content == null) {
      throw new NullPointerException("The Email Cotent is not initiated.");
    }

    AttachmentsBodyPart atachmentPart = new AttachmentsBodyPart();
    if ((this.attachmentFiles != null) && (!this.attachmentFiles.isEmpty())) {
      for (String attachment : attachmentFiles) {
        try {
          atachmentPart.addAttachment(attachment);
        } catch (FileNotFoundException pExp) {
          logger.log(Level.WARNING, "{0}.sendMessage.AttachedFile Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
        }
      }
      if (!atachmentPart.isEmpty()) {
        content.setAttachments(atachmentPart);
      }
    }

    try {
      boolean hasSendError = false;
      if ((this.toAddresses != null) && (!this.toAddresses.isEmpty())) {
        InternetAddress[] addressArr = 
                                   this.toAddresses.toArray(new InternetAddress[]{});
        this.mailSession.sendMessage(content, addressArr, RecipientType.TO);
        hasSendError = this.mailSession.hasSendError();
      }
      if ((!hasSendError) && 
                  (this.ccAddresses != null) && (!this.ccAddresses.isEmpty())) {
        InternetAddress[] addressArr = 
                                   this.ccAddresses.toArray(new InternetAddress[]{});
        this.mailSession.sendMessage(content, addressArr, RecipientType.CC);
        hasSendError = this.mailSession.hasSendError();
      }
      if ((!hasSendError) && 
                  (this.bccAddresses != null) && (!this.bccAddresses.isEmpty())) {
        InternetAddress[] addressArr = 
                                  this.bccAddresses.toArray(new InternetAddress[]{});
        this.mailSession.sendMessage(content, addressArr, RecipientType.BCC);
        hasSendError = this.mailSession.hasSendError();
      }  
      result = (!hasSendError);
    } catch (Exception pExp) {
      logger.log(Level.WARNING, "{0}.sendMessage Error:\n {1}",
              new Object[]{this.getClass().getSimpleName(), pExp.getMessage()});
    }
    return result;
  }
  
  /**
   * Return the MainSession's Send Error
   * @param detailed true if a detailed detailed is required.
   * @return current sendError message or null if the session is undefined or has no 
   * errors
   */
  public String getSendErrorMsg(boolean detailed) {
    return (this.mailSession == null)? null: this.mailSession.getErrorMsg(detailed);
  }
  // </editor-fold>
  
  //<editor-fold defaultstate="collapsed" desc="Abstract Methods">
  /**
   * ABSTRACT: Inheritors must override this method to generate the EamilContent to
   * send to the recipients
   * @return the content to send (must be defined).
   */
  protected abstract EmailContent onGetContent();
  //</editor-fold>
}
